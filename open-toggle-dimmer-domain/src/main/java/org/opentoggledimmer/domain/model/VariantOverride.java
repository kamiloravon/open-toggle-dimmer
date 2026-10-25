package org.opentoggledimmer.domain.model;

import java.util.List;

/**
 * Value object: переопределение варианта по значению контекста.
 * Value object: a variant override by context value.
 *
 * <hr>
 *
 * <p>Если значение поля {@code contextName} входит в {@code values}, вариант
 * выбирается до хеширования.
 * If the value of the {@code contextName} field is in {@code values}, the
 * variant is chosen before hashing.</p>
 *
 * @param contextName имя поля контекста / the context field name
 * @param values      значения / the values
 */
public record VariantOverride(String contextName, List<String> values) {

    /**
     * Компактный конструктор с валидацией.
     * Compact constructor with validation.
     */
    public VariantOverride {
        if (contextName == null || contextName.isBlank()) {
            throw new org.opentoggledimmer.domain.exception.ValidationException(
                    "Override contextName must not be blank / Имя поля override не может быть пустым");
        }
        values = values == null ? List.of() : List.copyOf(values);
    }
}