package org.opentoggledimmer.domain.model;

import org.opentoggledimmer.domain.exception.ValidationException;

/**
 * Value object: идентификатор группы стратегии (groupId).
 * Value object: a strategy group id (groupId).
 *
 * <hr>
 *
 * <p>Группа участвует в хешировании stickiness, позволяя коррелировать
 * распределение нескольких флагов. Непустая строка; по умолчанию берётся имя
 * флага. Immutable.
 * The group participates in stickiness hashing, allowing several flags to
 * share a distribution. Non-blank string; defaults to the flag name. Immutable.</p>
 *
 * @param value идентификатор группы / the group identifier
 */
public record GroupId(String value) {

    /**
     * Компактный конструктор с валидацией.
     * Compact constructor with validation.
     *
     * @throws ValidationException если значение пустое / if the value is blank
     */
    public GroupId {
        if (value == null || value.isBlank()) {
            throw new ValidationException("Group id must not be blank / Идентификатор группы не может быть пустым");
        }
    }

    /**
     * Фабричный метод.
     * Factory method.
     *
     * @param value идентификатор / the identifier
     * @return значение value object / the value object
     */
    public static GroupId of(String value) {
        return new GroupId(value);
    }

    /**
     * Возвращает группу по умолчанию — имя флага.
     * Returns the default group — the flag name.
     *
     * @param featureName имя флага / the flag name
     * @return группа, равная имени флага / a group equal to the flag name
     */
    public static GroupId forFeature(FeatureName featureName) {
        return new GroupId(featureName.value());
    }

    /**
     * Возвращает строковое представление группы.
     * Returns the string representation of the group.
     *
     * @return значение группы / the group value
     */
    @Override
    public String toString() {
        return value;
    }
}