package org.opentoggledimmer.domain.model;

import java.util.List;

/**
 * Value object: зависимость флага от родителя.
 * Value object: a flag dependency on a parent.
 *
 * <hr>
 *
 * <p>Описывает ожидаемое состояние родителя ({@code enabled}) и, опционально,
 * ожидаемые варианты родителя (сверяются по {@code variant.name}).
 * Describes the expected parent state ({@code enabled}) and, optionally, the
 * expected parent variants (compared by {@code variant.name}).</p>
 *
 * @param feature  имя родительского флага / the parent flag name
 * @param enabled  ожидаемое состояние родителя / the expected parent state
 * @param variants ожидаемые варианты (пусто — не проверять) /
 *                 expected variants (empty — do not check)
 */
public record FeatureDependency(String feature, boolean enabled, List<String> variants) {

    /**
     * Компактный конструктор с валидацией.
     * Compact constructor with validation.
     */
    public FeatureDependency {
        if (feature == null || feature.isBlank()) {
            throw new org.opentoggledimmer.domain.exception.ValidationException(
                    "Dependency feature must not be blank / Имя родительского флага не может быть пустым");
        }
        variants = variants == null ? List.of() : List.copyOf(variants);
    }

    /**
     * Создаёт зависимость «родитель включён».
     * Creates an "parent enabled" dependency.
     *
     * @param feature родитель / the parent
     * @return зависимость / the dependency
     */
    public static FeatureDependency enabledWhen(String feature) {
        return new FeatureDependency(feature, true, List.of());
    }

    /**
     * Создаёт зависимость «родитель выключен».
     * Creates a "parent disabled" dependency.
     *
     * @param feature родитель / the parent
     * @return зависимость / the dependency
     */
    public static FeatureDependency disabledWhen(String feature) {
        return new FeatureDependency(feature, false, List.of());
    }

    /**
     * Проверяет, заданы ли ожидаемые варианты.
     * Tells whether expected variants are set.
     *
     * @return {@code true}, если список вариантов непуст /
     *         {@code true} if the variant list is non-empty
     */
    public boolean hasVariantExpectation() {
        return !variants.isEmpty();
    }
}