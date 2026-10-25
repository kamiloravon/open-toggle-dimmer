package org.opentoggledimmer.domain.model;

import org.opentoggledimmer.domain.exception.ValidationException;

/**
 * Value object: имя стратегии активации.
 * Value object: an activation strategy name.
 *
 * <hr>
 *
 * <p>Непустая строка, соответствующая типу стратегии
 * (например, {@code flexibleRollout}, {@code userWithId}). Immutable.
 * A non-blank string matching a strategy type (e.g.
 * {@code flexibleRollout}, {@code userWithId}). Immutable.</p>
 *
 * @param value имя стратегии / the strategy name
 */
public record StrategyName(String value) {

    /**
     * Компактный конструктор с валидацией.
     * Compact constructor with validation.
     *
     * @throws ValidationException если значение пустое / if the value is blank
     */
    public StrategyName {
        if (value == null || value.isBlank()) {
            throw new ValidationException("Strategy name must not be blank / Имя стратегии не может быть пустым");
        }
    }

    /**
     * Фабричный метод.
     * Factory method.
     *
     * @param value имя / the name
     * @return значение value object / the value object
     */
    public static StrategyName of(String value) {
        return new StrategyName(value);
    }

    /**
     * Возвращает строковое представление имени.
     * Returns the string representation of the name.
     *
     * @return значение имени / the name value
     */
    @Override
    public String toString() {
        return value;
    }
}