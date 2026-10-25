package org.opentoggledimmer.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opentoggledimmer.domain.model.ApplicationHostnameStrategy;
import org.opentoggledimmer.domain.model.Constraint;
import org.opentoggledimmer.domain.model.ConstraintOperator;
import org.opentoggledimmer.domain.model.CustomStrategy;
import org.opentoggledimmer.domain.model.DefaultStrategy;
import org.opentoggledimmer.domain.model.GradualRolloutStrategy;
import org.opentoggledimmer.domain.model.GroupId;
import org.opentoggledimmer.domain.model.LegacyGradualRolloutStrategy;
import org.opentoggledimmer.domain.model.RemoteAddressStrategy;
import org.opentoggledimmer.domain.model.RolloutPercentage;
import org.opentoggledimmer.domain.model.Segment;
import org.opentoggledimmer.domain.model.EvaluationContext;
import org.opentoggledimmer.domain.model.UserIdsStrategy;

/**
 * Тесты {@link StrategyEvaluator}.
 * Tests for {@link StrategyEvaluator}.
 */
class StrategyEvaluatorTest {

    private final StickinessCalculator stickiness = new StickinessCalculator((o, b) -> o);
    private final ConstraintEvaluator constraints = new ConstraintEvaluator();

    private StrategyEvaluator evaluator(List<Segment> segments, String hostname) {
        return new StrategyEvaluator(
                constraints,
                stickiness,
                () -> hostname,
                segments.stream().collect(java.util.stream.Collectors.toMap(Segment::id, s -> s)));
    }

    @Test
    @DisplayName("default-стратегия всегда true")
    void defaultStrategyAlwaysTrue() {
        DefaultStrategy strategy = new DefaultStrategy("default", List.of(), List.of(), false, 1);
        assertThat(evaluator(List.of(), "host").evaluate(strategy, "flag", EvaluationContext.empty())).isTrue();
    }

    @Test
    @DisplayName("отключённая стратегия → false")
    void disabledStrategy() {
        DefaultStrategy strategy = new DefaultStrategy("default", List.of(), List.of(), true, 1);
        assertThat(evaluator(List.of(), "host").evaluate(strategy, "flag", EvaluationContext.empty())).isFalse();
    }

    @Test
    @DisplayName("gradual rollout: flag:123 нормализуется в 48, при 60% включён")
    void gradualRolloutIncluded() {
        GradualRolloutStrategy strategy = new GradualRolloutStrategy(
                RolloutPercentage.of(60), "userId", GroupId.of("flag"), List.of(), List.of(), false, 1);
        EvaluationContext context = new EvaluationContext(null, null, "123", null, null, null, Map.of());
        assertThat(evaluator(List.of(), "host").evaluate(strategy, "flag", context)).isTrue();
    }

    @Test
    @DisplayName("gradual rollout: при 47% выключен (48 > 47)")
    void gradualRolloutExcluded() {
        GradualRolloutStrategy strategy = new GradualRolloutStrategy(
                RolloutPercentage.of(47), "userId", GroupId.of("flag"), List.of(), List.of(), false, 1);
        EvaluationContext context = new EvaluationContext(null, null, "123", null, null, null, Map.of());
        assertThat(evaluator(List.of(), "host").evaluate(strategy, "flag", context)).isFalse();
    }

    @Test
    @DisplayName("gradual rollout default stickiness без userId/sessionId → false")
    void gradualRolloutMissingSeed() {
        GradualRolloutStrategy strategy = new GradualRolloutStrategy(
                RolloutPercentage.of(100), "default", GroupId.of("flag"), List.of(), List.of(), false, 1);
        assertThat(evaluator(List.of(), "host").evaluate(strategy, "flag", EvaluationContext.empty())).isFalse();
    }

    @Test
    @DisplayName("gradual rollout random использует RandomSource")
    void gradualRolloutRandom() {
        GradualRolloutStrategy strategy = new GradualRolloutStrategy(
                RolloutPercentage.of(50), "random", GroupId.of("flag"), List.of(), List.of(), false, 1);
        StrategyEvaluator withRandomOne = new StrategyEvaluator(
                constraints, new StickinessCalculator((o, b) -> 1), () -> "host", Map.of());
        assertThat(withRandomOne.evaluate(strategy, "flag", EvaluationContext.empty())).isTrue();
    }

    @Test
    @DisplayName("userWithId: точное совпадение")
    void userIds() {
        UserIdsStrategy strategy = new UserIdsStrategy(List.of("u-1", "u-2"), List.of(), List.of(), false, 1);
        EvaluationContext context = new EvaluationContext(null, null, "u-2", null, null, null, Map.of());
        assertThat(evaluator(List.of(), "host").evaluate(strategy, "flag", context)).isTrue();
    }

    @Test
    @DisplayName("remoteAddress: CIDR-вхождение")
    void remoteAddress() {
        RemoteAddressStrategy strategy = new RemoteAddressStrategy(List.of("127.0.0.0/8"), List.of(), List.of(), false, 1);
        EvaluationContext context = new EvaluationContext(null, null, null, null, "127.0.0.1", null, Map.of());
        assertThat(evaluator(List.of(), "host").evaluate(strategy, "flag", context)).isTrue();
    }

    @Test
    @DisplayName("applicationHostname: case-insensitive")
    void hostname() {
        ApplicationHostnameStrategy strategy = new ApplicationHostnameStrategy(List.of("my-host"), List.of(), List.of(), false, 1);
        assertThat(evaluator(List.of(), "MY-HOST").evaluate(strategy, "flag", EvaluationContext.empty())).isTrue();
    }

    @Test
    @DisplayName("custom-стратегия всегда false")
    void customStrategy() {
        CustomStrategy strategy = new CustomStrategy("myCustom", List.of(), List.of(), false, 1);
        assertThat(evaluator(List.of(), "host").evaluate(strategy, "flag", EvaluationContext.empty())).isFalse();
    }

    @Test
    @DisplayName("пустой сегмент → true")
    void emptySegment() {
        Segment segment = Segment.of(1L, "empty", List.of());
        DefaultStrategy strategy = new DefaultStrategy("default", List.of(), List.of(1L), false, 1);
        assertThat(evaluator(List.of(segment), "host").evaluate(strategy, "flag", EvaluationContext.empty())).isTrue();
    }

    @Test
    @DisplayName("constraint стратегии не выполнен → false")
    void strategyConstraintFails() {
        Constraint c = Constraint.of("plan", ConstraintOperator.IN, List.of("premium"));
        DefaultStrategy strategy = new DefaultStrategy("default", List.of(c), List.of(), false, 1);
        EvaluationContext context = new EvaluationContext(null, null, null, null, null, null, Map.of("plan", "free"));
        assertThat(evaluator(List.of(), "host").evaluate(strategy, "flag", context)).isFalse();
    }

    @Test
    @DisplayName("legacy gradualRolloutRandom")
    void legacyRandom() {
        LegacyGradualRolloutStrategy strategy = new LegacyGradualRolloutStrategy(
                LegacyGradualRolloutStrategy.LegacyKind.RANDOM,
                RolloutPercentage.of(50),
                GroupId.of("flag"),
                List.of(), List.of(), false, 1);
        StrategyEvaluator withRandomOne = new StrategyEvaluator(
                constraints, new StickinessCalculator((o, b) -> 1), () -> "host", Map.of());
        assertThat(withRandomOne.evaluate(strategy, "flag", EvaluationContext.empty())).isTrue();
    }
}