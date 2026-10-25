package org.opentoggledimmer.domain.service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.opentoggledimmer.domain.model.ActivationStrategy;
import org.opentoggledimmer.domain.model.FeatureDependency;
import org.opentoggledimmer.domain.model.FeatureFlag;
import org.opentoggledimmer.domain.model.FeatureName;
import org.opentoggledimmer.domain.model.Segment;
import org.opentoggledimmer.domain.model.EvaluationContext;
import org.opentoggledimmer.domain.model.Variant;
import org.opentoggledimmer.domain.model.VariantOverride;
import org.opentoggledimmer.domain.model.VariantPayload;
import org.opentoggledimmer.domain.port.HostnameProvider;
import org.opentoggledimmer.domain.port.RandomSource;

/**
 * Доменный сервис: вычисление активации флага и выбор варианта.
 * Domain service: flag activation and variant selection.
 *
 * <hr>
 *
 * <p>Реализует главное правило активации:
 * {@code flagEnabled = enabled AND parentDependencySatisfied AND strategiesRule},
 * где {@code strategiesRule} — OR по стратегиям ({@code true} при пустом списке).
 * Зависимости проверяются рекурсивно по имени родителя.
 * Implements the main activation rule:
 * {@code flagEnabled = enabled AND parentDependencySatisfied AND strategiesRule},
 * where {@code strategiesRule} is an OR over strategies ({@code true} for an
 * empty list). Dependencies are checked recursively by parent name.</p>
 */
public final class FlagEvaluationService {

    private final ConstraintEvaluator constraintEvaluator;
    private final StickinessCalculator stickinessCalculator;
    private final RandomSource randomSource;
    private final HostnameProvider hostnameProvider;

    /**
     * Создаёт сервис вычисления.
     * Creates the evaluation service.
     *
     * @param constraintEvaluator  вычислитель constraints / constraint evaluator
     * @param stickinessCalculator вычислитель stickiness / stickiness calculator
     * @param randomSource         источник случайности / randomness source
     * @param hostnameProvider     источник хостнейма / hostname provider
     */
    public FlagEvaluationService(
            ConstraintEvaluator constraintEvaluator,
            StickinessCalculator stickinessCalculator,
            RandomSource randomSource,
            HostnameProvider hostnameProvider) {
        this.constraintEvaluator = constraintEvaluator;
        this.stickinessCalculator = stickinessCalculator;
        this.randomSource = randomSource;
        this.hostnameProvider = hostnameProvider;
    }

    /**
     * Вычисляет, включён ли флаг для контекста.
     * Evaluates whether the flag is enabled for the context.
     *
     * @param flag       флаг / the flag
     * @param context    контекст / the context
     * @param flagsByName все флаги по имени (для зависимостей) /
     *                    all flags by name (for dependencies)
     * @param segments   сегменты / segments
     * @return {@code true}, если флаг активен / {@code true} if enabled
     */
    public boolean isEnabled(
            FeatureFlag flag,
            EvaluationContext context,
            Map<String, FeatureFlag> flagsByName,
            Map<Long, Segment> segments) {
        if (!flag.enabled()) {
            return false;
        }
        if (!dependenciesSatisfied(flag, context, flagsByName, segments)) {
            return false;
        }
        return strategiesMatch(flag.strategies(), flag.name().value(), context, segments);
    }

    /**
     * Проверяет выполнение зависимостей флага.
     * Checks the flag dependencies.
     *
     * @param flag        флаг / the flag
     * @param context     контекст / the context
     * @param flagsByName флаги по имени / flags by name
     * @param segments    сегменты / segments
     * @return {@code true}, если все зависимости выполнены /
     *         {@code true} if all dependencies are satisfied
     */
    private boolean dependenciesSatisfied(
            FeatureFlag flag,
            EvaluationContext context,
            Map<String, FeatureFlag> flagsByName,
            Map<Long, Segment> segments) {
        for (FeatureDependency dependency : flag.dependencies()) {
            FeatureFlag parent = flagsByName.get(dependency.feature());
            if (parent == null) {
                return false;
            }
            if (!parent.dependencies().isEmpty()) {
                return false;
            }
            boolean parentEnabled = isEnabled(parent, context, flagsByName, segments);
            if (parentEnabled != dependency.enabled()) {
                return false;
            }
            if (dependency.hasVariantExpectation()) {
                Optional<Variant> parentVariant = selectVariant(parent, context, flagsByName, segments);
                if (parentVariant.isEmpty()
                        || !dependency.variants().contains(parentVariant.get().name().value())) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Вычисляет OR по стратегиям (пустой список → {@code true}).
     * Evaluates the OR over strategies (empty list → {@code true}).
     *
     * @param strategies  стратегии / strategies
     * @param featureName имя флага / flag name
     * @param context     контекст / context
     * @param segments    сегменты / segments
     * @return {@code true}, если хоть одна стратегия сработала /
     *         {@code true} if any strategy matches
     */
    private boolean strategiesMatch(
            List<ActivationStrategy> strategies,
            String featureName,
            EvaluationContext context,
            Map<Long, Segment> segments) {
        if (strategies.isEmpty()) {
            return true;
        }
        StrategyEvaluator evaluator = new StrategyEvaluator(
                constraintEvaluator, stickinessCalculator, hostnameProvider, segments);
        List<ActivationStrategy> ordered = strategies.stream()
                .sorted(Comparator.comparingInt(ActivationStrategy::sortOrder))
                .toList();
        for (ActivationStrategy strategy : ordered) {
            if (evaluator.evaluate(strategy, featureName, context)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Выбирает вариант флага для контекста.
     * Selects the flag variant for the context.
     *
     * @param flag        флаг / the flag
     * @param context     контекст / the context
     * @param flagsByName флаги по имени / flags by name
     * @param segments    сегменты / segments
     * @return выбранный вариант или пусто / the selected variant or empty
     */
    public Optional<Variant> selectVariant(
            FeatureFlag flag,
            EvaluationContext context,
            Map<String, FeatureFlag> flagsByName,
            Map<Long, Segment> segments) {
        if (!isEnabled(flag, context, flagsByName, segments)) {
            return Optional.empty();
        }
        List<Variant> variants = flag.variants();
        if (variants.isEmpty()) {
            return Optional.empty();
        }
        Optional<Variant> override = matchOverride(variants, context);
        if (override.isPresent()) {
            return override;
        }
        int totalWeight = variants.stream().mapToInt(v -> v.weight().value()).sum();
        if (totalWeight <= 0) {
            return Optional.empty();
        }
        String stickiness = variants.get(0).stickiness();
        int target;
        Optional<String> seed = stickinessCalculator.resolveSeed(stickiness, context);
        if (seed.isEmpty()) {
            target = stickinessCalculator.randomVariant(totalWeight);
        } else {
            target = stickinessCalculator.normalizedVariant(flag.name().value(), seed.get(), totalWeight);
        }
        int accumulated = 0;
        for (Variant variant : variants) {
            accumulated += variant.weight().value();
            if (accumulated >= target) {
                return Optional.of(variant);
            }
        }
        return Optional.empty();
    }

    /**
     * Ищет вариант по override.
     * Finds a variant by override.
     *
     * @param variants варианты / variants
     * @param context  контекст / context
     * @return вариант или пусто / the variant or empty
     */
    private Optional<Variant> matchOverride(List<Variant> variants, EvaluationContext context) {
        for (Variant variant : variants) {
            for (VariantOverride override : variant.overrides()) {
                Optional<String> value = context.get(override.contextName());
                if (value.isPresent() && override.values().contains(value.get())) {
                    return Optional.of(variant);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Результат вычисления варианта с признаком «включён флаг».
     * Variant evaluation result with the "flag enabled" marker.
     *
     * @param name           имя варианта / the variant name
     * @param enabled        выбран ли вариант / whether the variant is selected
     * @param featureEnabled включён ли флаг / whether the flag is enabled
     * @param payload        payload / the payload
     */
    public record VariantResult(String name, boolean enabled, boolean featureEnabled, VariantPayload payload) {

        /**
         * Возвращает fallback-вариант {@code disabled}.
         * Returns the {@code disabled} fallback variant.
         *
         * @param featureEnabled включён ли флаг / whether the flag is enabled
         * @return fallback / the fallback
         */
        public static VariantResult disabled(boolean featureEnabled) {
            return new VariantResult("disabled", false, featureEnabled, null);
        }

        /**
         * Возвращает выбранный вариант.
         * Returns a selected variant.
         *
         * @param variant вариант / the variant
         * @return результат / the result
         */
        public static VariantResult of(Variant variant) {
            return new VariantResult(variant.name().value(), true, true, variant.payload());
        }
    }

    /**
     * Возвращает результат варианта для флага.
     * Returns the variant result for the flag.
     *
     * @param flag        флаг / the flag
     * @param context     контекст / context
     * @param flagsByName флаги по имени / flags by name
     * @param segments    сегменты / segments
     * @return результат / the result
     */
    public VariantResult getVariant(
            FeatureFlag flag,
            EvaluationContext context,
            Map<String, FeatureFlag> flagsByName,
            Map<Long, Segment> segments) {
        boolean enabled = isEnabled(flag, context, flagsByName, segments);
        if (!enabled) {
            return VariantResult.disabled(false);
        }
        return selectVariant(flag, context, flagsByName, segments)
                .map(VariantResult::of)
                .orElseGet(() -> VariantResult.disabled(true));
    }

    /**
     * Возвращает имя флага (утилита для маппинга).
     * Returns the flag name (a mapping utility).
     *
     * @param flag флаг / the flag
     * @return имя / the name
     */
    public static FeatureName nameOf(FeatureFlag flag) {
        return flag.name();
    }
}