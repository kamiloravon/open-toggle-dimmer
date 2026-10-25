package org.opentoggledimmer.domain.model;

import org.opentoggledimmer.domain.exception.ValidationException;

/**
 * Value object: результат MurmurHash3 как беззнаковое 32-битное число.
 * Value object: a MurmurHash3 result as an unsigned 32-bit number.
 *
 * <hr>
 *
 * <p>Инвариант: {@code 0 <= value <= 0xFFFFFFFFL}. Хранит уже беззнаковое
 * значение, поэтому {@code Math.abs} не применяется (он некорректен для
 * {@code Integer.MIN_VALUE}). Нормализация выполняется методом
 * {@link #normalized(int)}.
 * Invariant: {@code 0 <= value <= 0xFFFFFFFFL}. Stores an already unsigned
 * value, so {@code Math.abs} is not applied (it is incorrect for
 * {@code Integer.MIN_VALUE}). Normalization is done via {@link #normalized(int)}.</p>
 *
 * @param value беззнаковое 32-битное значение / the unsigned 32-bit value
 */
public record MurmurHashValue(long value) {

    /**
     * Компактный конструктор с проверкой диапазона.
     * Compact constructor with range check.
     *
     * @throws ValidationException если значение вне {@code [0, 0xFFFFFFFF]} /
     *                             if the value is outside {@code [0, 0xFFFFFFFF]}
     */
    public MurmurHashValue {
        if (value < 0L || value > 0xFFFFFFFFL) {
            throw new ValidationException(
                    "MurmurHashValue must be an unsigned 32-bit value: " + value
                            + " / MurmurHashValue должен быть беззнаковым 32-битным значением: " + value);
        }
    }

    /**
     * Фабричный метод из беззнакового значения.
     * Factory method from an unsigned value.
     *
     * @param value беззнаковое 32-битное значение / the unsigned 32-bit value
     * @return значение value object / the value object
     */
    public static MurmurHashValue ofUnsigned(long value) {
        return new MurmurHashValue(value);
    }

    /**
     * Нормализует хеш в диапазон {@code 1..modulus}.
     * Normalizes the hash into the {@code 1..modulus} range.
     *
     * <hr>
     *
     * <p>Формула нормализации: {@code (hash % modulus) + 1}. Значение беззнаковое,
     * поэтому результат всегда в пределах {@code 1..modulus}.
     * Normalization formula: {@code (hash % modulus) + 1}. The value is unsigned, so
     * the result always lies within {@code 1..modulus}.</p>
     *
     * @param modulus модуль (100 для rollout, totalWeight для вариантов) /
     *                the modulus (100 for rollout, totalWeight for variants)
     * @return нормализованное значение {@code 1..modulus} /
     *         the normalized value {@code 1..modulus}
     * @throws ValidationException если {@code modulus <= 0} /
     *                             if {@code modulus <= 0}
     */
    public int normalized(int modulus) {
        if (modulus <= 0) {
            throw new ValidationException("Modulus must be positive / Модуль должен быть положительным: " + modulus);
        }
        return (int) ((value % modulus) + 1);
    }
}