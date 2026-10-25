package org.opentoggledimmer.domain.model;

import org.opentoggledimmer.domain.exception.ValidationException;

/**
 * Value object: имя варианта (variant).
 * Value object: a variant name.
 *
 * <hr>
 *
 * <p>Непустая строка, уникальная в пределах флага/стратегии. Immutable.
 * A non-blank string, unique within a flag/strategy. Immutable.</p>
 *
 * @param value имя варианта / the variant name
 */
public record VariantName(String value) {

    /**
     * Компактный конструктор с валидацией.
     * Compact constructor with validation.
     *
     * @throws ValidationException если значение пустое / if the value is blank
     */
    public VariantName {
        if (value == null || value.isBlank()) {
            throw new ValidationException("Variant name must not be blank / Имя варианта не может быть пустым");
        }
    }

    /**
     * Фабричный метод.
     * Factory method.
     *
     * @param value имя / the name
     * @return значение value object / the value object
     */
    public static VariantName of(String value) {
        return new VariantName(value);
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