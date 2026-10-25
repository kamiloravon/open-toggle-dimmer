package org.opentoggledimmer.domain.service;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.opentoggledimmer.domain.model.CidrBlock;
import org.opentoggledimmer.domain.model.Constraint;
import org.opentoggledimmer.domain.model.ConstraintOperator;
import org.opentoggledimmer.domain.model.ConstraintValueType;
import org.opentoggledimmer.domain.model.EvaluationContext;

/**
 * Доменный сервис: вычисление constraints.
 * Domain service: constraint evaluation.
 *
 * <hr>
 *
 * <p>Реализует все операторы с точной семантикой движка, включая
 * поведение при отсутствующем значении контекста и при пустом списке значений
 * (таблицы A/B/C из спецификации). Неизвестный оператор даёт {@code false};
 * результат затем инвертируется при {@code inverted}.
 * Implements all operators with the engine's exact semantics, including
 * behavior for a missing context value and an empty value list (tables A/B/C
 * from the specification). An unknown operator yields {@code false}; the result
 * is then inverted when {@code inverted} is set.</p>
 */
public final class ConstraintEvaluator {

    /**
     * Погрешность для числового равенства (как {@code f64::EPSILON}).
     * Tolerance for numeric equality (like {@code f64::EPSILON}).
     */
    private static final double NUMERIC_EPSILON = Double.MIN_NORMAL;

    /**
     * Вычисляет constraint для контекста.
     * Evaluates the constraint for a context.
     *
     * @param constraint constraint / the constraint
     * @param context    контекст / the context
     * @return {@code true}, если constraint выполнен (с учётом {@code inverted}) /
     *         {@code true} if the constraint holds (respecting {@code inverted})
     */
    public boolean evaluate(Constraint constraint, EvaluationContext context) {
        boolean raw = evaluateRaw(constraint, context);
        return constraint.inverted() ? !raw : raw;
    }

    /**
     * Вычисляет constraint без учёта инверсии.
     * Evaluates the constraint without inversion.
     *
     * @param constraint constraint / the constraint
     * @param context    контекст / the context
     * @return результат оператора / the operator result
     */
    private boolean evaluateRaw(Constraint constraint, EvaluationContext context) {
        Optional<String> rawValue = context.get(constraint.contextName());
        ConstraintOperator operator = constraint.operator();
        return switch (operator) {
            case IN, NOT_IN -> evaluateList(constraint, rawValue);
            case NUM_EQ, NUM_GT, NUM_GTE, NUM_LT, NUM_LTE -> evaluateNumeric(constraint, rawValue);
            case DATE_AFTER, DATE_BEFORE -> evaluateDate(constraint, rawValue);
            case STR_CONTAINS, STR_ENDS_WITH, STR_STARTS_WITH -> evaluateString(constraint, rawValue);
            case SEMVER_EQ, SEMVER_GT, SEMVER_GTE, SEMVER_LT, SEMVER_LTE ->
                    evaluateSemver(constraint, rawValue);
            case REGEX -> evaluateRegex(constraint, rawValue);
            case IN_CIDR -> evaluateCidr(constraint, rawValue);
        };
    }

    /**
     * Вычисляет {@code IN}/{@code NOT_IN} по таблицам A/B.
     * Evaluates {@code IN}/{@code NOT_IN} per tables A/B.
     *
     * @param constraint constraint / the constraint
     * @param rawValue   значение контекста / the context value
     * @return результат / the result
     */
    private boolean evaluateList(Constraint constraint, Optional<String> rawValue) {
        List<String> values = constraint.values();
        boolean isNotIn = constraint.operator() == ConstraintOperator.NOT_IN;

        if (values.isEmpty()) {
            return isNotIn;
        }

        if (constraint.valueType() == ConstraintValueType.NUMBER) {
            if (rawValue.isEmpty()) {
                return false;
            }
            double contextNumber;
            try {
                contextNumber = Double.parseDouble(rawValue.get());
            } catch (NumberFormatException e) {
                return false;
            }
            boolean contained = values.stream()
                    .map(ConstraintEvaluator::parseDoubleOrNull)
                    .anyMatch(v -> v != null && Math.abs(v - contextNumber) < NUMERIC_EPSILON);
            return isNotIn != contained;
        }

        boolean contained = rawValue.isPresent() && values.contains(rawValue.get());
        return isNotIn != contained;
    }

    /**
     * Вычисляет числовой оператор.
     * Evaluates a numeric operator.
     *
     * @param constraint constraint / the constraint
     * @param rawValue   значение контекста / the context value
     * @return результат / the result
     */
    private boolean evaluateNumeric(Constraint constraint, Optional<String> rawValue) {
        if (rawValue.isEmpty() || constraint.values().isEmpty()) {
            return false;
        }
        Double contextNumber = parseDoubleOrNull(rawValue.get());
        Double target = parseDoubleOrNull(constraint.values().get(0));
        if (contextNumber == null || target == null) {
            return false;
        }
        return switch (constraint.operator()) {
            case NUM_EQ -> Math.abs(contextNumber - target) < NUMERIC_EPSILON;
            case NUM_GT -> contextNumber > target;
            case NUM_GTE -> contextNumber >= target;
            case NUM_LT -> contextNumber < target;
            case NUM_LTE -> contextNumber <= target;
            default -> false;
        };
    }

    /**
     * Вычисляет датовый оператор.
     * Evaluates a date operator.
     *
     * @param constraint constraint / the constraint
     * @param rawValue   значение контекста / the context value
     * @return результат / the result
     */
    private boolean evaluateDate(Constraint constraint, Optional<String> rawValue) {
        if (rawValue.isEmpty() || constraint.values().isEmpty()) {
            return false;
        }
        OffsetDateTime contextTime;
        OffsetDateTime target;
        try {
            contextTime = OffsetDateTime.parse(rawValue.get());
            target = OffsetDateTime.parse(constraint.values().get(0));
        } catch (DateTimeParseException e) {
            return false;
        }
        return switch (constraint.operator()) {
            case DATE_AFTER -> contextTime.isAfter(target);
            case DATE_BEFORE -> contextTime.isBefore(target);
            default -> false;
        };
    }

    /**
     * Вычисляет строковый оператор.
     * Evaluates a string operator.
     *
     * @param constraint constraint / the constraint
     * @param rawValue   значение контекста / the context value
     * @return результат / the result
     */
    private boolean evaluateString(Constraint constraint, Optional<String> rawValue) {
        if (rawValue.isEmpty()) {
            return false;
        }
        String value = constraint.caseInsensitive() ? rawValue.get().toLowerCase() : rawValue.get();
        List<String> values = constraint.caseInsensitive()
                ? constraint.values().stream().map(String::toLowerCase).toList()
                : constraint.values();
        return switch (constraint.operator()) {
            case STR_CONTAINS -> values.stream().anyMatch(value::contains);
            case STR_ENDS_WITH -> values.stream().anyMatch(value::endsWith);
            case STR_STARTS_WITH -> values.stream().anyMatch(value::startsWith);
            default -> false;
        };
    }

    /**
     * Вычисляет semver-оператор (SemVer 2.0.0).
     * Evaluates a semver operator (SemVer 2.0.0).
     *
     * @param constraint constraint / the constraint
     * @param rawValue   значение контекста / the context value
     * @return результат / the result
     */
    private boolean evaluateSemver(Constraint constraint, Optional<String> rawValue) {
        if (rawValue.isEmpty() || constraint.values().isEmpty()) {
            return false;
        }
        SemVer contextVersion = SemVer.parseOrNull(rawValue.get());
        SemVer target = SemVer.parseOrNull(constraint.values().get(0));
        if (contextVersion == null || target == null) {
            return false;
        }
        int comparison = contextVersion.compareTo(target);
        return switch (constraint.operator()) {
            case SEMVER_EQ -> comparison == 0;
            case SEMVER_GT -> comparison > 0;
            case SEMVER_GTE -> comparison >= 0;
            case SEMVER_LT -> comparison < 0;
            case SEMVER_LTE -> comparison <= 0;
            default -> false;
        };
    }

    /**
     * Вычисляет регулярное выражение.
     * Evaluates a regular expression.
     *
     * @param constraint constraint / the constraint
     * @param rawValue   значение контекста / the context value
     * @return результат / the result
     */
    private boolean evaluateRegex(Constraint constraint, Optional<String> rawValue) {
        if (rawValue.isEmpty() || constraint.values().isEmpty()) {
            return false;
        }
        int flags = constraint.caseInsensitive() ? Pattern.CASE_INSENSITIVE : 0;
        try {
            return Pattern.compile(constraint.values().get(0), flags).matcher(rawValue.get()).matches();
        } catch (PatternSyntaxException e) {
            return false;
        }
    }

    /**
     * Вычисляет вхождение IP в CIDR.
     * Evaluates IP membership in a CIDR block.
     *
     * @param constraint constraint / the constraint
     * @param rawValue   значение контекста / the context value
     * @return результат / the result
     */
    private boolean evaluateCidr(Constraint constraint, Optional<String> rawValue) {
        if (rawValue.isEmpty() || constraint.values().isEmpty()) {
            return false;
        }
        for (String block : constraint.values()) {
            try {
                if (CidrBlock.of(block).contains(rawValue.get())) {
                    return true;
                }
            } catch (RuntimeException e) {
                return false;
            }
        }
        return false;
    }

    /**
     * Парсит {@code double}, возвращая {@code null} при ошибке.
     * Parses a {@code double}, returning {@code null} on failure.
     *
     * @param value строка / the string
     * @return число или {@code null} / the number or {@code null}
     */
    private static Double parseDoubleOrNull(String value) {
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Минимальная модель SemVer 2.0.0 для сравнения версий.
     * A minimal SemVer 2.0.0 model for version comparison.
     *
     * <hr>
     *
     * <p>Поддерживает {@code major.minor.patch}, prerelease и build metadata.
     * Prerelease ниже релиза; build metadata игнорируется.
     * Supports {@code major.minor.patch}, prerelease and build metadata.
     * Prerelease is lower than release; build metadata is ignored.</p>
     */
    private record SemVer(int major, int minor, int patch, List<String> prerelease) implements Comparable<SemVer> {

        /**
         * Парсит версию, возвращая {@code null} при ошибке.
         * Parses a version, returning {@code null} on failure.
         *
         * @param value строка версии / the version string
         * @return версия или {@code null} / the version or {@code null}
         */
        static SemVer parseOrNull(String value) {
            if (value == null) {
                return null;
            }
            String core = value.trim();
            int plus = core.indexOf('+');
            if (plus >= 0) {
                core = core.substring(0, plus);
            }
            List<String> prerelease = List.of();
            int dash = core.indexOf('-');
            if (dash >= 0) {
                prerelease = List.of(core.substring(dash + 1).split("\\."));
                core = core.substring(0, dash);
            }
            String[] parts = core.split("\\.");
            if (parts.length != 3) {
                return null;
            }
            try {
                return new SemVer(
                        Integer.parseInt(parts[0]),
                        Integer.parseInt(parts[1]),
                        Integer.parseInt(parts[2]),
                        prerelease);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        /**
         * Сравнивает версии по SemVer.
         * Compares versions per SemVer.
         *
         * @param other другая версия / the other version
         * @return отрицательное, ноль или положительное / negative, zero or positive
         */
        @Override
        public int compareTo(SemVer other) {
            int byMajor = Integer.compare(major, other.major);
            if (byMajor != 0) {
                return byMajor;
            }
            int byMinor = Integer.compare(minor, other.minor);
            if (byMinor != 0) {
                return byMinor;
            }
            int byPatch = Integer.compare(patch, other.patch);
            if (byPatch != 0) {
                return byPatch;
            }
            if (prerelease.isEmpty() && other.prerelease.isEmpty()) {
                return 0;
            }
            if (prerelease.isEmpty()) {
                return 1;
            }
            if (other.prerelease.isEmpty()) {
                return -1;
            }
            int length = Math.min(prerelease.size(), other.prerelease.size());
            for (int i = 0; i < length; i++) {
                String left = prerelease.get(i);
                String right = other.prerelease.get(i);
                int comparison = comparePrereleaseIdentifier(left, right);
                if (comparison != 0) {
                    return comparison;
                }
            }
            return Integer.compare(prerelease.size(), other.prerelease.size());
        }

        /**
         * Сравнивает идентификаторы prerelease.
         * Compares prerelease identifiers.
         *
         * @param left  левый / left
         * @param right правый / right
         * @return результат сравнения / comparison result
         */
        private static int comparePrereleaseIdentifier(String left, String right) {
            boolean leftNumeric = left.chars().allMatch(Character::isDigit);
            boolean rightNumeric = right.chars().allMatch(Character::isDigit);
            if (leftNumeric && rightNumeric) {
                return Long.compare(Long.parseLong(left), Long.parseLong(right));
            }
            if (leftNumeric) {
                return -1;
            }
            if (rightNumeric) {
                return 1;
            }
            return left.compareTo(right);
        }
    }
}