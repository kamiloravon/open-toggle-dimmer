package org.opentoggledimmer.domain.model;

/**
 * Тип значений списка constraint в исходной конфигурации.
 * The value type of a constraint list in the source configuration.
 *
 * <hr>
 *
 * <p>Определяет, как движок трактует список {@code IN}/{@code NOT_IN}:
 * {@link #NUMBER} соответствует {@code numeric_list} (незакавыченные числа),
 * {@link #STRING} — {@code string_list} (строки). Влияет на поведение при
 * отсутствующем значении контекста.
 * Determines how the engine treats an {@code IN}/{@code NOT_IN} list:
 * {@link #NUMBER} maps to {@code numeric_list} (unquoted numbers),
 * {@link #STRING} maps to {@code string_list} (strings). Affects behavior when
 * the context value is missing.</p>
 */
public enum ConstraintValueType {
    /** Список строк ({@code ["a", "b"]}) / list of strings ({@code ["a", "b"]}). */
    STRING,
    /** Список чисел ({@code [1, 2]}) / list of numbers ({@code [1, 2]}). */
    NUMBER
}