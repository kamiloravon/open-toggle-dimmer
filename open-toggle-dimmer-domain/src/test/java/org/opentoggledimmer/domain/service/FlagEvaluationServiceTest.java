package org.opentoggledimmer.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opentoggledimmer.domain.model.ActivationStrategy;
import org.opentoggledimmer.domain.model.DefaultStrategy;
import org.opentoggledimmer.domain.model.FeatureDependency;
import org.opentoggledimmer.domain.model.FeatureFlag;
import org.opentoggledimmer.domain.model.FeatureName;
import org.opentoggledimmer.domain.model.ProjectId;
import org.opentoggledimmer.domain.model.Segment;
import org.opentoggledimmer.domain.model.EvaluationContext;
import org.opentoggledimmer.domain.model.Variant;

/**
 * Тесты {@link FlagEvaluationService}: активация, зависимости, варианты.
 * Tests for {@link FlagEvaluationService}: activation, dependencies, variants.
 */
class FlagEvaluationServiceTest {

    private final StickinessCalculator stickiness = new StickinessCalculator((o, b) -> o);
    private final FlagEvaluationService service = new FlagEvaluationService(
            new ConstraintEvaluator(), stickiness, (o, b) -> o, () -> "host");

    private static FeatureFlag flag(String name, boolean enabled, List<ActivationStrategy> strategies) {
        return new FeatureFlag(FeatureName.of(name), ProjectId.defaultProject(), enabled, strategies, List.of(), List.of());
    }

    private static FeatureFlag flagWithDeps(
            String name, boolean enabled, List<ActivationStrategy> strategies, List<FeatureDependency> deps) {
        return new FeatureFlag(FeatureName.of(name), ProjectId.defaultProject(), enabled, strategies, deps, List.of());
    }

    @Test
    @DisplayName("enabled=false → false, стратегии не вычисляются")
    void disabledFlag() {
        FeatureFlag f = flag("f", false, List.of(new DefaultStrategy("default", List.of(), List.of(), false, 1)));
        assertThat(service.isEnabled(f, EvaluationContext.empty(), Map.of(), Map.of())).isFalse();
    }

    @Test
    @DisplayName("пустой список стратегий → true (эквивалент enabled)")
    void emptyStrategiesEnabled() {
        FeatureFlag f = flag("f", true, List.of());
        assertThat(service.isEnabled(f, EvaluationContext.empty(), Map.of(), Map.of())).isTrue();
    }

    @Test
    @DisplayName("OR между стратегиями")
    void orStrategies() {
        FeatureFlag f = flag("f", true, List.of(
                new DefaultStrategy("default", List.of(
                        org.opentoggledimmer.domain.model.Constraint.of(
                                "plan", org.opentoggledimmer.domain.model.ConstraintOperator.IN, List.of("premium"))),
                        List.of(), false, 1),
                new DefaultStrategy("standard", List.of(), List.of(), false, 2)));
        assertThat(service.isEnabled(f, EvaluationContext.empty(), Map.of(), Map.of())).isTrue();
    }

    @Test
    @DisplayName("зависимость: родитель включён → дочерний включён")
    void dependencyEnabled() {
        FeatureFlag parent = flag("parent", true, List.of());
        FeatureFlag child = flagWithDeps("child", true, List.of(), List.of(FeatureDependency.enabledWhen("parent")));
        Map<String, FeatureFlag> all = Map.of("parent", parent, "child", child);
        assertThat(service.isEnabled(child, EvaluationContext.empty(), all, Map.of())).isTrue();
    }

    @Test
    @DisplayName("зависимость: родитель выключен → дочерний выключен")
    void dependencyDisabledParent() {
        FeatureFlag parent = flag("parent", false, List.of());
        FeatureFlag child = flagWithDeps("child", true, List.of(), List.of(FeatureDependency.enabledWhen("parent")));
        Map<String, FeatureFlag> all = Map.of("parent", parent, "child", child);
        assertThat(service.isEnabled(child, EvaluationContext.empty(), all, Map.of())).isFalse();
    }

    @Test
    @DisplayName("зависимость: отсутствующий родитель → false")
    void dependencyMissingParent() {
        FeatureFlag child = flagWithDeps("child", true, List.of(), List.of(FeatureDependency.enabledWhen("ghost")));
        assertThat(service.isEnabled(child, EvaluationContext.empty(), Map.of(), Map.of())).isFalse();
    }

    @Test
    @DisplayName("зависимость: parent с зависимостями → false")
    void dependencyGrandparent() {
        FeatureFlag grand = flag("grand", true, List.of());
        FeatureFlag parent = flagWithDeps("parent", true, List.of(), List.of(FeatureDependency.enabledWhen("grand")));
        FeatureFlag child = flagWithDeps("child", true, List.of(), List.of(FeatureDependency.enabledWhen("parent")));
        Map<String, FeatureFlag> all = Map.of("grand", grand, "parent", parent, "child", child);
        assertThat(service.isEnabled(child, EvaluationContext.empty(), all, Map.of())).isFalse();
    }

    @Test
    @DisplayName("вариант: override имеет приоритет")
    void variantOverride() {
        Variant v = new Variant(
                org.opentoggledimmer.domain.model.VariantName.of("blue"),
                org.opentoggledimmer.domain.model.VariantWeight.of(100),
                Variant.WeightType.VARIABLE,
                null,
                null,
                List.of(new org.opentoggledimmer.domain.model.VariantOverride("userId", List.of("u-1"))));
        FeatureFlag f = new FeatureFlag(
                FeatureName.of("f"), ProjectId.defaultProject(), true, List.of(), List.of(), List.of(v));
        EvaluationContext context = new EvaluationContext(null, null, "u-1", null, null, null, Map.of());
        FlagEvaluationService.VariantResult result = service.getVariant(f, context, Map.of("f", f), Map.of());
        assertThat(result.name()).isEqualTo("blue");
        assertThat(result.enabled()).isTrue();
    }

    @Test
    @DisplayName("вариант: пустой список → fallback disabled, featureEnabled=true")
    void variantEmptyFallback() {
        FeatureFlag f = flag("f", true, List.of());
        FlagEvaluationService.VariantResult result = service.getVariant(f, EvaluationContext.empty(), Map.of("f", f), Map.of());
        assertThat(result.name()).isEqualTo("disabled");
        assertThat(result.enabled()).isFalse();
        assertThat(result.featureEnabled()).isTrue();
    }

    @Test
    @DisplayName("вариант: накопительные интервалы весов")
    void variantCumulativeWeights() {
        Variant a = Variant.of("a", 300);
        Variant b = Variant.of("b", 700);
        FeatureFlag f = new FeatureFlag(
                FeatureName.of("f"), ProjectId.defaultProject(), true, List.of(), List.of(), List.of(a, b));
        EvaluationContext context = new EvaluationContext(null, null, "123", null, null, null, Map.of());
        FlagEvaluationService.VariantResult result = service.getVariant(f, context, Map.of("f", f), Map.of());
        assertThat(result.name()).isIn("a", "b");
        assertThat(result.enabled()).isTrue();
    }

    @Test
    @DisplayName("вариант: totalWeight <= 0 → fallback")
    void variantZeroWeight() {
        Variant a = Variant.of("a", 0);
        FeatureFlag f = new FeatureFlag(
                FeatureName.of("f"), ProjectId.defaultProject(), true, List.of(), List.of(), List.of(a));
        FlagEvaluationService.VariantResult result = service.getVariant(f, EvaluationContext.empty(), Map.of("f", f), Map.of());
        assertThat(result.name()).isEqualTo("disabled");
        assertThat(result.featureEnabled()).isTrue();
    }

    @Test
    @DisplayName("инвариант: не более одного родителя")
    void invariantSingleParent() {
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> flagWithDeps(
                "child", true, List.of(),
                List.of(FeatureDependency.enabledWhen("p1"), FeatureDependency.enabledWhen("p2"))))
                .isInstanceOf(org.opentoggledimmer.domain.exception.ValidationException.class);
    }

    @Test
    @DisplayName("сегменты применяются при вычислении стратегии")
    void segmentsApplied() {
        Segment segment = Segment.of(1L, "beta", List.of(
                org.opentoggledimmer.domain.model.Constraint.of(
                        "plan", org.opentoggledimmer.domain.model.ConstraintOperator.IN, List.of("premium"))));
        FeatureFlag f = flag("f", true, List.of(new DefaultStrategy("default", List.of(), List.of(1L), false, 1)));
        Map<Long, Segment> segments = Map.of(1L, segment);
        assertThat(service.isEnabled(f, EvaluationContext.empty(), Map.of("f", f), segments)).isFalse();
    }
}