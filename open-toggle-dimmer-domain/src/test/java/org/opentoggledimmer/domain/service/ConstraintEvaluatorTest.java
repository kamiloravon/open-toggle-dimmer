package org.opentoggledimmer.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.opentoggledimmer.domain.model.Constraint;
import org.opentoggledimmer.domain.model.ConstraintOperator;
import org.opentoggledimmer.domain.model.ConstraintValueType;
import org.opentoggledimmer.domain.model.EvaluationContext;

/**
 * Тесты {@link ConstraintEvaluator}: матрицы A/B/C и все операторы.
 * Tests for {@link ConstraintEvaluator}: tables A/B/C and all operators.
 */
class ConstraintEvaluatorTest {

    private final ConstraintEvaluator evaluator = new ConstraintEvaluator();

    private static EvaluationContext ctx(Map<String, String> props) {
        return new EvaluationContext(null, null, null, null, null, null, props);
    }

    @Nested
    @DisplayName("Таблица A: numeric_list")
    class NumericList {

        private final Constraint base = Constraint.of("age", ConstraintOperator.IN, List.of("18", "21"), ConstraintValueType.NUMBER);

        @Test
        @DisplayName("IN попадает в список")
        void inContains() {
            assertThat(evaluator.evaluate(base, ctx(Map.of("age", "21")))).isTrue();
        }

        @Test
        @DisplayName("IN пустой список → false")
        void inEmpty() {
            Constraint c = Constraint.of("age", ConstraintOperator.IN, List.of(), ConstraintValueType.NUMBER);
            assertThat(evaluator.evaluate(c, ctx(Map.of("age", "21")))).isFalse();
        }

        @Test
        @DisplayName("NOT_IN пустой список → true")
        void notInEmpty() {
            Constraint c = Constraint.of("age", ConstraintOperator.NOT_IN, List.of(), ConstraintValueType.NUMBER);
            assertThat(evaluator.evaluate(c, ctx(Map.of("age", "21")))).isTrue();
        }

        @Test
        @DisplayName("NOT_IN отсутствующее значение → false")
        void notInMissingValue() {
            Constraint c = Constraint.of("age", ConstraintOperator.NOT_IN, List.of("18"), ConstraintValueType.NUMBER);
            assertThat(evaluator.evaluate(c, ctx(Map.of()))).isFalse();
        }

        @Test
        @DisplayName("NOT_IN нечисловое значение → false")
        void notInNonNumeric() {
            Constraint c = Constraint.of("age", ConstraintOperator.NOT_IN, List.of("18"), ConstraintValueType.NUMBER);
            assertThat(evaluator.evaluate(c, ctx(Map.of("age", "abc")))).isFalse();
        }
    }

    @Nested
    @DisplayName("Таблица B: string_list")
    class StringList {

        private final Constraint base = Constraint.of("plan", ConstraintOperator.IN, List.of("premium", "plus"));

        @Test
        @DisplayName("IN попадает в список")
        void inContains() {
            assertThat(evaluator.evaluate(base, ctx(Map.of("plan", "plus")))).isTrue();
        }

        @Test
        @DisplayName("IN пустой список → false")
        void inEmpty() {
            Constraint c = Constraint.of("plan", ConstraintOperator.IN, List.of());
            assertThat(evaluator.evaluate(c, ctx(Map.of("plan", "plus")))).isFalse();
        }

        @Test
        @DisplayName("NOT_IN пустой список → true")
        void notInEmpty() {
            Constraint c = Constraint.of("plan", ConstraintOperator.NOT_IN, List.of());
            assertThat(evaluator.evaluate(c, ctx(Map.of("plan", "plus")))).isTrue();
        }

        @Test
        @DisplayName("NOT_IN отсутствующее значение → true")
        void notInMissingValue() {
            Constraint c = Constraint.of("plan", ConstraintOperator.NOT_IN, List.of("premium"));
            assertThat(evaluator.evaluate(c, ctx(Map.of()))).isTrue();
        }

        @Test
        @DisplayName("IN отсутствующее значение → false")
        void inMissingValue() {
            assertThat(evaluator.evaluate(base, ctx(Map.of()))).isFalse();
        }
    }

    @Nested
    @DisplayName("Таблица C: прочие операторы")
    class OtherOperators {

        @Test
        @DisplayName("NUM_GT / NUM_EQ / NUM_LT")
        void numeric() {
            EvaluationContext context = ctx(Map.of("age", "20"));
            assertThat(evaluator.evaluate(Constraint.of("age", ConstraintOperator.NUM_GT, List.of("18")), context)).isTrue();
            assertThat(evaluator.evaluate(Constraint.of("age", ConstraintOperator.NUM_LT, List.of("18")), context)).isFalse();
            assertThat(evaluator.evaluate(Constraint.of("age", ConstraintOperator.NUM_EQ, List.of("20")), context)).isTrue();
        }

        @Test
        @DisplayName("NUM_* нечисловое значение → false")
        void numericNonNumeric() {
            EvaluationContext context = ctx(Map.of("age", "abc"));
            assertThat(evaluator.evaluate(Constraint.of("age", ConstraintOperator.NUM_GT, List.of("18")), context)).isFalse();
        }

        @Test
        @DisplayName("DATE_AFTER / DATE_BEFORE")
        void dates() {
            EvaluationContext context = new EvaluationContext(
                    null, null, null, null, null,
                    OffsetDateTime.of(2022, 6, 6, 0, 0, 0, 0, ZoneOffset.UTC), Map.of());
            assertThat(evaluator.evaluate(
                    Constraint.of("currentTime", ConstraintOperator.DATE_AFTER, List.of("2022-06-05T21:43:22Z")),
                    context)).isTrue();
            assertThat(evaluator.evaluate(
                    Constraint.of("currentTime", ConstraintOperator.DATE_BEFORE, List.of("2022-06-05T21:43:22Z")),
                    context)).isFalse();
        }

        @Test
        @DisplayName("STR_CONTAINS / ENDS_WITH / STARTS_WITH")
        void strings() {
            EvaluationContext context = ctx(Map.of("email", "hello@user.com"));
            assertThat(evaluator.evaluate(Constraint.of("email", ConstraintOperator.STR_ENDS_WITH, List.of("@user.com")), context)).isTrue();
            assertThat(evaluator.evaluate(Constraint.of("email", ConstraintOperator.STR_STARTS_WITH, List.of("hello")), context)).isTrue();
            assertThat(evaluator.evaluate(Constraint.of("email", ConstraintOperator.STR_CONTAINS, List.of("user")), context)).isTrue();
        }

        @Test
        @DisplayName("caseInsensitive для STR_*")
        void stringCaseInsensitive() {
            EvaluationContext context = ctx(Map.of("email", "HELLO@USER.COM"));
            Constraint c = Constraint.of("email", ConstraintOperator.STR_ENDS_WITH, List.of("@user.com"))
                    .withCaseInsensitive(true);
            assertThat(evaluator.evaluate(c, context)).isTrue();
        }

        @Test
        @DisplayName("SEMVER_GT / SEMVER_LT / SEMVER_EQ")
        void semver() {
            EvaluationContext context = ctx(Map.of("version", "1.2.4"));
            assertThat(evaluator.evaluate(Constraint.of("version", ConstraintOperator.SEMVER_GT, List.of("1.2.3")), context)).isTrue();
            assertThat(evaluator.evaluate(Constraint.of("version", ConstraintOperator.SEMVER_EQ, List.of("1.2.4")), context)).isTrue();
            assertThat(evaluator.evaluate(Constraint.of("version", ConstraintOperator.SEMVER_LT, List.of("1.2.3")), context)).isFalse();
        }

        @Test
        @DisplayName("SEMVER prerelease ниже релиза")
        void semverPrerelease() {
            EvaluationContext context = ctx(Map.of("version", "1.2.3-rc.1"));
            assertThat(evaluator.evaluate(Constraint.of("version", ConstraintOperator.SEMVER_LT, List.of("1.2.3")), context)).isTrue();
        }

        @Test
        @DisplayName("REGEX")
        void regex() {
            EvaluationContext context = ctx(Map.of("email", "hello@user.com"));
            assertThat(evaluator.evaluate(Constraint.of("email", ConstraintOperator.REGEX, List.of(".*@user\\.com")), context)).isTrue();
            assertThat(evaluator.evaluate(Constraint.of("email", ConstraintOperator.REGEX, List.of(".*@corp\\.com")), context)).isFalse();
        }

        @Test
        @DisplayName("IN_CIDR")
        void cidr() {
            EvaluationContext context = new EvaluationContext(
                    null, null, null, null, "127.0.0.1", null, Map.of());
            assertThat(evaluator.evaluate(Constraint.of("remoteAddress", ConstraintOperator.IN_CIDR, List.of("127.0.0.0/8")), context)).isTrue();
            assertThat(evaluator.evaluate(Constraint.of("remoteAddress", ConstraintOperator.IN_CIDR, List.of("10.0.0.0/8")), context)).isFalse();
        }
    }

    @Test
    @DisplayName("inverted инвертирует результат")
    void inverted() {
        EvaluationContext context = ctx(Map.of("plan", "premium"));
        Constraint c = Constraint.of("plan", ConstraintOperator.IN, List.of("premium")).withInverted(true);
        assertThat(evaluator.evaluate(c, context)).isFalse();
    }
}