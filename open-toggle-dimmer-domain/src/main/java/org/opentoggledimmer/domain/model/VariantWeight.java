package org.opentoggledimmer.domain.model;

import org.opentoggledimmer.domain.exception.ValidationException;

/**
 * Value object: вес варианта.
 * Value object: a variant weight.
 *
 * <hr>
 *
 * <p>Целое число в диапазоне 0–1000 включительно. Immutable.
 * An integer in the range 0–1000 inclusive. Immutable.</p>
 *
 * @param value вес / the weight
 */
public record VariantWeight(int value) {

    /**
     * Компактный конструктор с валидацией.
     * Compact constructor with validation.
     *
     * @throws ValidationException если значение вне диапазона 0–1000 /
     *                             if the value is outside 0–1000
     */
    public VariantWeight {
        if (value < 0 || value > 1000) {
            throw new ValidationException(
                    "Variant weight must be in [0, 1000]: " + value
                            + " / Вес варианта должен быть в [0, 1000]: " + value);
        }
    }

    /**
     * Фабричный метод.
     * Factory method.
     *
     * @param value вес / the weight
     * @return значение value object / the value object
     */
    public static VariantWeight of(int value) {
        return new VariantWeight(value);
    }
}