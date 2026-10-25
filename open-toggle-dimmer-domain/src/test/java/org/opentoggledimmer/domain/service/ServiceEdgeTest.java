package org.opentoggledimmer.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opentoggledimmer.domain.model.ActivationStrategy;
import org.opentoggledimmer.domain.model.Constraint;
import org.opentoggledimmer.domain.model.ConstraintOperator;
import org.opentoggledimmer.domain.model.DefaultStrategy;
import org.opentoggledimmer.domain.model.FeatureDependency;
import org.opentoggledimmer.domain.model.FeatureFlag;
import org.opentoggledimmer.domain.model.FeatureName;
import org.opentoggledimmer.domain.model.GradualRolloutStrategy;
import org.opentoggledimmer.domain.model.GroupId;
import org.opentoggledimmer.domain.model.LegacyGradualRolloutStrategy;
import org.opentoggledimmer.domain.model.ProjectId;
import org.opentoggledimmer.domain.model.RolloutPercentage;
import org.opentoggledimmer.domain.model.Segment;
import org.opentoggledimmer.domain.model.EvaluationContext;
import org.opentoggledimmer.domain.model.Variant;

/**
 * Дополнительные тесты сервисов: ветки стратегий, зависимостей и вариантов.
 * Additional service tests: strategy, dependency and variant branches.
 */
class ServiceEdgeTest {

    private final ConstraintEvaluator constraintEvaluator = new ConstraintEvaluator();
    private final StickinessCalculator stickiness = new StickinessCalculator((o, b) -> o);

    private StrategyEvaluator evaluator(String hostname) {
        return new StrategyEvaluator(constraintEvaluator, stickiness, () -> hostname, Map.of());
    }

    @Test
    @DisplayName("applicationHostname: null хостнейм → false")
    void hostnameNull() {
        ActivationStrategy s = new org.opentoggledimmer.domain.model.ApplicationHostnameStrategy(
                List.of("host"), List.of(), List.of(), false, 1);
        assertThat(evaluator(null).evaluate(s, "f", EvaluationContext.empty())).isFalse();
    }

    @Test
    @DisplayName("applicationHostname: пустой список → false")
    void hostnameEmptyList() {
        ActivationStrategy s = new org.opentoggledimmer.domain.model.ApplicationHostnameStrategy(
                List.of(), List.of(), List.of(), false, 1);
        assertThat(evaluator("host").evaluate(s, "f", EvaluationContext.empty())).isFalse();
    }

    @Test
    @DisplayName("remoteAddress: пустой список и отсутствующий IP → false")
    void remoteEmpty() {
        ActivationStrategy empty = new org.opentoggledimmer.domain.model.RemoteAddressStrategy(
                List.of(), List.of(), List.of(), false, 1);
        assertThat(evaluator("h").evaluate(empty, "f", EvaluationContext.empty())).isFalse();
        ActivationStrategy withIps = new org.opentoggledimmer.domain.model.RemoteAddressStrategy(
                List.of("127.0.0.0/8"), List.of(), List.of(), false, 1);
        assertThat(evaluator("h").evaluate(withIps, "f", EvaluationContext.empty())).isFalse();
    }

    @Test
    @DisplayName("userWithId: отсутствующий userId → false")
    void userIdsMissing() {
        ActivationStrategy s = new org.opentoggledimmer.domain.model.UserIdsStrategy(
                List.of("u"), List.of(), List.of(), false, 1);
        assertThat(evaluator("h").evaluate(s, "f", EvaluationContext.empty())).isFalse();
    }

    @Test
    @DisplayName("legacy sessionId: отсутствующий sessionId → false, присутствующий → true")
    void legacySessionId() {
        ActivationStrategy s = new LegacyGradualRolloutStrategy(
                LegacyGradualRolloutStrategy.LegacyKind.SESSION_ID,
                RolloutPercentage.of(100), GroupId.of("g"), List.of(), List.of(), false, 1);
        assertThat(evaluator("h").evaluate(s, "f", EvaluationContext.empty())).isFalse();
        EvaluationContext c = new EvaluationContext(null, null, null, "s-1", null, null, Map.of());
        assertThat(evaluator("h").evaluate(s, "f", c)).isTrue();
    }

    @Test
    @DisplayName("legacy userId: присутствующий userId")
    void legacyUserId() {
        ActivationStrategy s = new LegacyGradualRolloutStrategy(
                LegacyGradualRolloutStrategy.LegacyKind.USER_ID,
                RolloutPercentage.of(100), GroupId.of("g"), List.of(), List.of(), false, 1);
        EvaluationContext c = new EvaluationContext(null, null, "u-1", null, null, null, Map.of());
        assertThat(evaluator("h").evaluate(s, "f", c)).isTrue();
    }

    @Test
    @DisplayName("сегмент не найден по id → false")
    void segmentMissing() {
        ActivationStrategy s = new DefaultStrategy("default", List.of(), List.of(99L), false, 1);
        assertThat(evaluator("h").evaluate(s, "f", EvaluationContext.empty())).isFalse();
    }

    @Test
    @DisplayName("gradual: random-значение за пределами процента → false")
    void gradualRandomExcluded() {
        ActivationStrategy s = new GradualRolloutStrategy(
                RolloutPercentage.of(10), "random", GroupId.of("g"), List.of(), List.of(), false, 1);
        StrategyEvaluator high = new StrategyEvaluator(
                constraintEvaluator, new StickinessCalculator((o, b) -> 99), () -> "h", Map.of());
        assertThat(high.evaluate(s, "f", EvaluationContext.empty())).isFalse();
    }

    @Test
    @DisplayName("вариант: variant-dependency не выполнена → false")
    void variantDependencyNotSatisfied() {
        FlagEvaluationService service = new FlagEvaluationService(
                constraintEvaluator, stickiness, (o, b) -> o, () -> "h");
        Variant parentVariant = Variant.of("a", 1000);
        FeatureFlag parent = new FeatureFlag(
                FeatureName.of("parent"), ProjectId.defaultProject(), true, List.of(), List.of(),
                List.of(parentVariant));
        FeatureFlag child = new FeatureFlag(
                FeatureName.of("child"), ProjectId.defaultProject(), true, List.of(),
                List.of(new FeatureDependency("parent", true, List.of("b"))), List.of());
        Map<String, FeatureFlag> all = Map.of("parent", parent, "child", child);
        EvaluationContext c = new EvaluationContext(null, null, "u", null, null, null, Map.of());
        assertThat(service.isEnabled(child, c, all, Map.of())).isFalse();
    }

    @Test
    @DisplayName("вариант: variant-dependency выполнена → true")
    void variantDependencySatisfied() {
        FlagEvaluationService service = new FlagEvaluationService(
                constraintEvaluator, stickiness, (o, b) -> o, () -> "h");
        Variant parentVariant = Variant.of("a", 1000);
        FeatureFlag parent = new FeatureFlag(
                FeatureName.of("parent"), ProjectId.defaultProject(), true, List.of(), List.of(),
                List.of(parentVariant));
        FeatureFlag child = new FeatureFlag(
                FeatureName.of("child"), ProjectId.defaultProject(), true, List.of(),
                List.of(new FeatureDependency("parent", true, List.of("a"))), List.of());
        Map<String, FeatureFlag> all = Map.of("parent", parent, "child", child);
        EvaluationContext c = new EvaluationContext(null, null, "u", null, null, null, Map.of());
        assertThat(service.isEnabled(child, c, all, Map.of())).isTrue();
    }

    @Test
    @DisplayName("вариант: override по отсутствующему полю не срабатывает")
    void overrideMissingField() {
        FlagEvaluationService service = new FlagEvaluationService(
                constraintEvaluator, stickiness, (o, b) -> o, () -> "h");
        Variant v = new Variant(
                org.opentoggledimmer.domain.model.VariantName.of("a"),
                org.opentoggledimmer.domain.model.VariantWeight.of(1000),
                Variant.WeightType.VARIABLE, null, null,
                List.of(new org.opentoggledimmer.domain.model.VariantOverride("tenant", List.of("x"))));
        FeatureFlag f = new FeatureFlag(
                FeatureName.of("f"), ProjectId.defaultProject(), true, List.of(), List.of(), List.of(v));
        EvaluationContext c = new EvaluationContext(null, null, "u", null, null, null, Map.of());
        assertThat(service.selectVariant(f, c, Map.of("f", f), Map.of())).isPresent();
    }

    @Test
    @DisplayName("стратегия с constraint внутри сегмента")
    void segmentConstraint() {
        Segment segment = new Segment(1L, "seg", List.of(
                Constraint.of("plan", ConstraintOperator.IN, List.of("premium"))));
        StrategyEvaluator withSegment = new StrategyEvaluator(
                constraintEvaluator, stickiness, () -> "h", Map.of(1L, segment));
        ActivationStrategy s = new DefaultStrategy("default", List.of(), List.of(1L), false, 1);
        assertThat(withSegment.evaluate(s, "f", EvaluationContext.empty())).isFalse();
        EvaluationContext premium = new EvaluationContext(null, null, null, null, null, null, Map.of("plan", "premium"));
        assertThat(withSegment.evaluate(s, "f", premium)).isTrue();
    }
}