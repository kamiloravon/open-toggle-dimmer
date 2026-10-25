package org.opentoggledimmer.domain.port;

/**
 * Доменный порт: источник случайных чисел.
 * Domain port: random number source.
 *
 * <hr>
 *
 * <p>Используется только для не-sticky случаев: явный {@code random}-stickiness
 * в gradual rollout и отсутствующее значение stickiness при выборе варианта.
 * Инкапсуляция случайности делает домен детерминированным и тестируемым.
 * Used only for non-sticky cases: explicit {@code random}-stickiness in gradual
 * rollout and a missing stickiness value during variant selection.
 * Encapsulating randomness keeps the domain deterministic and testable.</p>
 */
@FunctionalInterface
public interface RandomSource {

    /**
     * Возвращает случайное число в диапазоне {@code [origin, bound]} включительно.
     * Returns a random number within {@code [origin, bound]} inclusive.
     *
     * @param origin нижняя граница (включительно) / lower bound (inclusive)
     * @param bound  верхняя граница (включительно) / upper bound (inclusive)
     * @return случайное число / a random number
     */
    int nextIntInclusive(int origin, int bound);
}