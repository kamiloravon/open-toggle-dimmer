package org.opentoggledimmer.domain.model;

import org.opentoggledimmer.domain.exception.ValidationException;

/**
 * Value object: идентификатор проекта.
 * Value object: a project identifier.
 *
 * <hr>
 *
 * <p>Непустая строка. Проект группирует флаги; в рамках ядра используется
 * как метка принадлежности. Immutable.
 * A non-blank string. A project groups flags; within the core it is used as a
 * membership label. Immutable.</p>
 *
 * @param value идентификатор проекта / the project identifier
 */
public record ProjectId(String value) {

    /**
     * Компактный конструктор с валидацией.
     * Compact constructor with validation.
     *
     * @throws ValidationException если значение пустое / if the value is blank
     */
    public ProjectId {
        if (value == null || value.isBlank()) {
            throw new ValidationException("Project id must not be blank / Идентификатор проекта не может быть пустым");
        }
    }

    /**
     * Фабричный метод.
     * Factory method.
     *
     * @param value идентификатор / the identifier
     * @return значение value object / the value object
     */
    public static ProjectId of(String value) {
        return new ProjectId(value);
    }

    /**
     * Возвращает идентификатор по умолчанию {@code "default"}.
     * Returns the default identifier {@code "default"}.
     *
     * @return проект по умолчанию / the default project
     */
    public static ProjectId defaultProject() {
        return new ProjectId("default");
    }
}