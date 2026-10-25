package org.opentoggledimmer.domain.model;

import java.util.regex.Pattern;
import org.opentoggledimmer.domain.exception.ValidationException;

/**
 * Value object: имя feature-флага.
 * Value object: a feature flag name.
 *
 * <hr>
 *
 * <p>Имя уникально в пределах инсталляции и должно быть URL-safe:
 * 1–100 символов из набора {@code [A-Za-z0-9._~-]}. Значение неизменяемо.
 * The name is unique per installation and must be URL-safe: 1–100 characters
 * from {@code [A-Za-z0-9._~-]}. The value is immutable.</p>
 *
 * @param value строковое имя флага / the flag name string
 */
public record FeatureName(String value) {

    /** Допустимые символы URL-safe имени / allowed URL-safe name characters. */
    private static final Pattern URL_SAFE = Pattern.compile("^[A-Za-z0-9._~-]{1,100}$");

    /**
     * Компактный конструктор с валидацией.
     * Compact constructor with validation.
     *
     * @throws ValidationException если имя {@code null}, пустое, длиннее 100
     *                             символов или содержит недопустимые символы /
     *                             if the name is null, empty, longer than 100
     *                             characters, or contains invalid characters
     */
    public FeatureName {
        if (value == null || value.isBlank()) {
            throw new ValidationException("Feature name must not be blank / Имя флага не может быть пустым");
        }
        if (!URL_SAFE.matcher(value).matches()) {
            throw new ValidationException(
                    "Feature name must be URL-safe (1-100 chars of [A-Za-z0-9._~-]): " + value
                            + " / Имя флага должно быть URL-safe (1-100 символов [A-Za-z0-9._~-]): " + value);
        }
    }

    /**
     * Фабричный метод создания имени флага.
     * Factory method creating a feature name.
     *
     * @param value строка имени / the name string
     * @return значение value object / the value object
     */
    public static FeatureName of(String value) {
        return new FeatureName(value);
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
