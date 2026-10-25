package org.opentoggledimmer.domain.model;

import java.util.List;
import org.opentoggledimmer.domain.exception.ValidationException;

/**
 * Value object: constraint (ограничение) стратегии или сегмента.
 * Value object: a constraint of a strategy or segment.
 *
 * <hr>
 *
 * <p>Состоит из имени поля контекста, оператора, списка значений, типа значений
 * и флагов {@code caseInsensitive}/{@code inverted}. Неизменяем; список значений
 * копируется в неизменяемый.
 * Consists of a context field name, an operator, a list of values, a value type
 * and the {@code caseInsensitive}/{@code inverted} flags. Immutable; the values
 * list is copied into an immutable one.</p>
 *
 * @param contextName      имя поля контекста / context field name
 * @param operator         оператор / the operator
 * @param values           значения / the values
 * @param valueType        тип значений / the value type
 * @param caseInsensitive  игнорировать регистр / ignore case
 * @param inverted         инвертировать результат / invert the result
 */
public record Constraint(
        String contextName,
        ConstraintOperator operator,
        List<String> values,
        ConstraintValueType valueType,
        boolean caseInsensitive,
        boolean inverted) {

    /**
     * Компактный конструктор с валидацией.
     * Compact constructor with validation.
     *
     * @throws ValidationException при некорректных полях / on invalid fields
     */
    public Constraint {
        if (contextName == null || contextName.isBlank()) {
            throw new ValidationException("Constraint contextName must not be blank / Имя поля constraint не может быть пустым");
        }
        if (operator == null) {
            throw new ValidationException("Constraint operator must not be null / Оператор constraint не может быть null");
        }
        values = values == null ? List.of() : List.copyOf(values);
        valueType = valueType == null ? ConstraintValueType.STRING : valueType;
    }

    /**
     * Создаёт строковый constraint с настройками по умолчанию.
     * Creates a string constraint with default settings.
     *
     * @param contextName имя поля / field name
     * @param operator    оператор / operator
     * @param values      значения / values
     * @return constraint / the constraint
     */
    public static Constraint of(String contextName, ConstraintOperator operator, List<String> values) {
        return new Constraint(contextName, operator, values, ConstraintValueType.STRING, false, false);
    }

    /**
     * Создаёт constraint с явным типом значений.
     * Creates a constraint with an explicit value type.
     *
     * @param contextName имя поля / field name
     * @param operator    оператор / operator
     * @param values      значения / values
     * @param valueType   тип значений / value type
     * @return constraint / the constraint
     */
    public static Constraint of(
            String contextName, ConstraintOperator operator, List<String> values, ConstraintValueType valueType) {
        return new Constraint(contextName, operator, values, valueType, false, false);
    }

    /**
     * Возвращает копию с флагом {@code inverted}.
     * Returns a copy with the {@code inverted} flag.
     *
     * @param inverted инверсия / inversion
     * @return новый constraint / a new constraint
     */
    public Constraint withInverted(boolean inverted) {
        return new Constraint(contextName, operator, values, valueType, caseInsensitive, inverted);
    }

    /**
     * Возвращает копию с флагом {@code caseInsensitive}.
     * Returns a copy with the {@code caseInsensitive} flag.
     *
     * @param caseInsensitive регистронезависимость / case insensitivity
     * @return новый constraint / a new constraint
     */
    public Constraint withCaseInsensitive(boolean caseInsensitive) {
        return new Constraint(contextName, operator, values, valueType, caseInsensitive, inverted);
    }
}