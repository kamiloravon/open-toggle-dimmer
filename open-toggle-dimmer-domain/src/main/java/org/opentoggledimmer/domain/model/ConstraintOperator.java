package org.opentoggledimmer.domain.model;

/**
 * Перечисление операторов constraints.
 * Enumeration of constraint operators.
 *
 * <hr>
 *
 * <p>Полный набор операторов, поддерживаемых движком. Классификация:
 * базовые ({@code IN}/{@code NOT_IN}), числовые, датовые, строковые, semver,
 * регулярные выражения и CIDR.
 * The full set of operators supported by the engine. Groups: basic
 * ({@code IN}/{@code NOT_IN}), numeric, date, string, semver, regular
 * expressions and CIDR.</p>
 */
public enum ConstraintOperator {

    /** Значение контекста равно одному из списка (case-sensitive). / Context value equals one of the list (case-sensitive). */
    IN,
    /** Значение контекста не равно ни одному из списка. / Context value equals none of the list. */
    NOT_IN,
    /** Числовое равенство. / Numeric equality. */
    NUM_EQ,
    /** Числовое строгое «больше». / Numeric strictly greater. */
    NUM_GT,
    /** Числовое «больше или равно». / Numeric greater or equal. */
    NUM_GTE,
    /** Числовое строгое «меньше». / Numeric strictly less. */
    NUM_LT,
    /** Числовое «меньше или равно». / Numeric less or equal. */
    NUM_LTE,
    /** Дата контекста позже значения. / Context date is after the value. */
    DATE_AFTER,
    /** Дата контекста раньше значения. / Context date is before the value. */
    DATE_BEFORE,
    /** Строка содержит любое значение. / String contains any value. */
    STR_CONTAINS,
    /** Строка оканчивается любым значением. / String ends with any value. */
    STR_ENDS_WITH,
    /** Строка начинается с любого значения. / String starts with any value. */
    STR_STARTS_WITH,
    /** SemVer-равенство. / SemVer equality. */
    SEMVER_EQ,
    /** SemVer строго «больше». / SemVer strictly greater. */
    SEMVER_GT,
    /** SemVer «больше или равно». / SemVer greater or equal. */
    SEMVER_GTE,
    /** SemVer строго «меньше». / SemVer strictly less. */
    SEMVER_LT,
    /** SemVer «меньше или равно». / SemVer less or equal. */
    SEMVER_LTE,
    /** Регулярное выражение. / Regular expression. */
    REGEX,
    /** IP-адрес входит в CIDR-блок. / IP address belongs to a CIDR block. */
    IN_CIDR;

    /**
     * Определяет, относится ли оператор к строковым (учитывает {@code caseInsensitive}).
     * Tells whether the operator is string-based (honors {@code caseInsensitive}).
     *
     * @return {@code true} для {@code STR_*} и {@code REGEX} /
     *         {@code true} for {@code STR_*} and {@code REGEX}
     */
    public boolean isStringBased() {
        return this == STR_CONTAINS || this == STR_ENDS_WITH || this == STR_STARTS_WITH || this == REGEX;
    }

    /**
     * Определяет, относится ли оператор к списковым {@code IN}/{@code NOT_IN}.
     * Tells whether the operator is list-based {@code IN}/{@code NOT_IN}.
     *
     * @return {@code true} для {@code IN}/{@code NOT_IN} /
     *         {@code true} for {@code IN}/{@code NOT_IN}
     */
    public boolean isListBased() {
        return this == IN || this == NOT_IN;
    }
}