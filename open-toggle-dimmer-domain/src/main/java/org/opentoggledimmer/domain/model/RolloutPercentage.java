package org.opentoggledimmer.domain.model;

import org.opentoggledimmer.domain.exception.ValidationException;

/**
 * Value object: процент развёртывания (rollout).
 * Value object: a rollout percentage.
 *
 * <hr>
 *
 * <p>Целое число в диапазоне 0–100 включительно. Immutable.
 * An integer in the range 0–100 inclusive. Immutable.</p>
 *
 * @param value процент / the percentage
 */
public record RolloutPercentage(int value) {

    /**
     * Компактный конструктор с валидацией.
     * Compact constructor with validation.
     *
     * @throws ValidationException если значение вне диапазона 0–100 /
     *                             if the value is outside 0–100
     */
    public RolloutPercentage {
        if (value < 0 || value > 100) {
            throw new ValidationException(
                    "Rollout percentage must be in [0, 100]: " + value
                            + " / Процент развёртывания должен быть в [0, 100]: " + value);
        }
    }

    /**
     * Фабричный метод.
     * Factory method.
     *
     * @param value процент / the percentage
     * @return значение value object / the value object
     */
    public static RolloutPercentage of(int value) {
        return new RolloutPercentage(value);
    }

    /**
     * Проверяет, попадает ли нормализованный хеш в процент развёртывания.
     * Checks whether a normalized hash falls within the rollout percentage.
     *
     * @param normalized нормализованный хеш 1–100 / normalized hash 1–100
     * @return {@code true}, если {@code normalized <= value} /
     *         {@code true} if {@code normalized <= value}
     */
    public boolean includes(int normalized) {
        return normalized <= value;
    }
}