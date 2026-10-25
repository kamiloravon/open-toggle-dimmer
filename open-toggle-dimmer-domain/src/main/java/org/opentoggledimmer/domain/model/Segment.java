package org.opentoggledimmer.domain.model;

import java.util.List;
import org.opentoggledimmer.domain.exception.ValidationException;

/**
 * Aggregate root: сегмент (переиспользуемый набор constraints).
 * Aggregate root: a segment (a reusable set of constraints).
 *
 * <hr>
 *
 * <p>Инварианты: положительный {@code id}, непустое имя, constraints не {@code null}.
 * Пустой список constraints допустим и вычисляется как {@code true}.
 * Invariants: a positive {@code id}, a non-blank name, non-null constraints.
 * An empty constraint list is allowed and evaluates to {@code true}.</p>
 *
 * @param id          идентификатор сегмента / the segment id
 * @param name        имя сегмента / the segment name
 * @param constraints constraints / the constraints
 */
public record Segment(long id, String name, List<Constraint> constraints) {

    /**
     * Компактный конструктор с валидацией.
     * Compact constructor with validation.
     *
     * @throws ValidationException при некорректных полях / on invalid fields
     */
    public Segment {
        if (id <= 0) {
            throw new ValidationException("Segment id must be positive / Id сегмента должен быть положительным: " + id);
        }
        if (name == null || name.isBlank()) {
            throw new ValidationException("Segment name must not be blank / Имя сегмента не может быть пустым");
        }
        constraints = constraints == null ? List.of() : List.copyOf(constraints);
    }

    /**
     * Фабричный метод.
     * Factory method.
     *
     * @param id          идентификатор / id
     * @param name        имя / name
     * @param constraints constraints
     * @return сегмент / the segment
     */
    public static Segment of(long id, String name, List<Constraint> constraints) {
        return new Segment(id, name, constraints);
    }
}