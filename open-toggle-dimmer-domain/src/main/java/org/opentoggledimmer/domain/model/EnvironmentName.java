package org.opentoggledimmer.domain.model;

import org.opentoggledimmer.domain.exception.ValidationException;

/**
 * Value object: имя окружения.
 * Value object: an environment name.
 *
 * <hr>
 *
 * <p>Непустая строка (например, {@code development}, {@code production}).
 * Immutable.
 * A non-blank string (e.g. {@code development}, {@code production}). Immutable.</p>
 *
 * @param value имя окружения / the environment name
 */
public record EnvironmentName(String value) {

    /**
     * Компактный конструктор с валидацией.
     * Compact constructor with validation.
     *
     * @throws ValidationException если значение пустое / if the value is blank
     */
    public EnvironmentName {
        if (value == null || value.isBlank()) {
            throw new ValidationException("Environment name must not be blank / Имя окружения не может быть пустым");
        }
    }

    /**
     * Фабричный метод.
     * Factory method.
     *
     * @param value имя / the name
     * @return значение value object / the value object
     */
    public static EnvironmentName of(String value) {
        return new EnvironmentName(value);
    }

    /**
     * Возвращает имя окружения по умолчанию {@code "default"}.
     * Returns the default environment name {@code "default"}.
     *
     * @return окружение по умолчанию / the default environment
     */
    public static EnvironmentName defaultEnvironment() {
        return new EnvironmentName("default");
    }
}