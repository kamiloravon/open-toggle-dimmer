package org.opentoggledimmer.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opentoggledimmer.domain.model.EvaluationContext;

/**
 * Тесты {@link StickinessCalculator}: разрешение значения и нормализация.
 * Tests for {@link StickinessCalculator}: value resolution and normalization.
 */
class StickinessCalculatorTest {

    private final StickinessCalculator calculator = new StickinessCalculator((origin, bound) -> origin);

    @Test
    @DisplayName("default stickiness: userId имеет приоритет над sessionId")
    void shouldPreferUserIdForDefaultStickiness() {
        EvaluationContext context = new EvaluationContext(null, null, "u-1", "s-1", null, null, Map.of());
        assertThat(calculator.resolveSeed("default", context)).contains("u-1");
    }

    @Test
    @DisplayName("default stickiness: без userId берётся sessionId")
    void shouldFallBackToSessionId() {
        EvaluationContext context = new EvaluationContext(null, null, null, "s-1", null, null, Map.of());
        assertThat(calculator.resolveSeed("default", context)).contains("s-1");
    }

    @Test
    @DisplayName("default stickiness: нет ни userId, ни sessionId → пусто (без случайности)")
    void shouldReturnEmptyWhenNoSeed() {
        assertThat(calculator.resolveSeed("default", EvaluationContext.empty())).isEmpty();
    }

    @Test
    @DisplayName("custom context field читается из properties")
    void shouldResolveCustomFieldFromProperties() {
        EvaluationContext context = new EvaluationContext(null, null, null, null, null, null, Map.of("tenant", "acme"));
        assertThat(calculator.resolveSeed("tenant", context)).contains("acme");
    }

    @Test
    @DisplayName("random stickiness не даёт значения")
    void shouldReturnEmptyForRandom() {
        EvaluationContext context = new EvaluationContext(null, null, "u-1", null, null, null, Map.of());
        assertThat(calculator.resolveSeed("random", context)).isEmpty();
    }

    @Test
    @DisplayName("normalizedRollout для flag:123 равно 48")
    void shouldNormalizeRollout() {
        assertThat(calculator.normalizedRollout("flag", "123")).isEqualTo(48);
    }

    @Test
    @DisplayName("normalizedVariant для flag:123 при totalWeight 1000 равно 195")
    void shouldNormalizeVariant() {
        assertThat(calculator.normalizedVariant("flag", "123", 1000)).isEqualTo(195);
    }

    @Test
    @DisplayName("hashInput формирует groupId:value")
    void shouldBuildHashInput() {
        assertThat(StickinessCalculator.hashInput("flag", "123")).isEqualTo("flag:123");
    }

    @Test
    @DisplayName("resolveSeed для userId возвращает только userId")
    void shouldResolveUserIdOnly() {
        EvaluationContext context = new EvaluationContext(null, null, "u-1", "s-1", null, null, Map.of());
        Optional<String> seed = calculator.resolveSeed("userId", context);
        assertThat(seed).contains("u-1");
    }
}