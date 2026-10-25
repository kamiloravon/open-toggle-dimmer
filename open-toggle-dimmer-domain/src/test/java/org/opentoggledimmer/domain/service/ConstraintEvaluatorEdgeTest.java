package org.opentoggledimmer.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opentoggledimmer.domain.model.Constraint;
import org.opentoggledimmer.domain.model.ConstraintOperator;
import org.opentoggledimmer.domain.model.EvaluationContext;

/**
 * Дополнительные тесты {@link ConstraintEvaluator}: semver, отсутствующие
 * значения и невалидные входы.
 * Additional tests for {@link ConstraintEvaluator}: semver, missing values and
 * invalid inputs.
 */
class ConstraintEvaluatorEdgeTest {

    private final ConstraintEvaluator evaluator = new ConstraintEvaluator();

    private static EvaluationContext ctx(String key, String value) {
        return new EvaluationContext(null, null, null, null, null, null, Map.of(key, value));
    }

    @Test
    @DisplayName("SEMVER_GTE / SEMVER_LTE")
    void semverGteLte() {
        EvaluationContext c = ctx("v", "1.2.3");
        assertThat(evaluator.evaluate(Constraint.of("v", ConstraintOperator.SEMVER_GTE, List.of("1.2.3")), c)).isTrue();
        assertThat(evaluator.evaluate(Constraint.of("v", ConstraintOperator.SEMVER_LTE, List.of("1.2.3")), c)).isTrue();
        assertThat(evaluator.evaluate(Constraint.of("v", ConstraintOperator.SEMVER_GTE, List.of("1.2.4")), c)).isFalse();
    }

    @Test
    @DisplayName("SEMVER: build metadata игнорируется, prerelease-сравнение")
    void semverMetadataAndPrerelease() {
        assertThat(evaluator.evaluate(
                Constraint.of("v", ConstraintOperator.SEMVER_EQ, List.of("1.2.3")), ctx("v", "1.2.3+build.5"))).isTrue();
        assertThat(evaluator.evaluate(
                Constraint.of("v", ConstraintOperator.SEMVER_LT, List.of("1.2.3-rc.2")), ctx("v", "1.2.3-rc.1"))).isTrue();
        assertThat(evaluator.evaluate(
                Constraint.of("v", ConstraintOperator.SEMVER_GT, List.of("1.2.3-1")), ctx("v", "1.2.3-alpha"))).isTrue();
        assertThat(evaluator.evaluate(
                Constraint.of("v", ConstraintOperator.SEMVER_EQ, List.of("1.2.3")), ctx("v", "bad"))).isFalse();
    }

    @Test
    @DisplayName("SEMVER: отсутствующее значение и пустой список → false")
    void semverMissing() {
        assertThat(evaluator.evaluate(
                Constraint.of("v", ConstraintOperator.SEMVER_EQ, List.of("1.2.3")), ctx("other", "x"))).isFalse();
        assertThat(evaluator.evaluate(
                Constraint.of("v", ConstraintOperator.SEMVER_EQ, List.of()), ctx("v", "1.2.3"))).isFalse();
    }

    @Test
    @DisplayName("STR_*: отсутствующее значение → false")
    void stringMissing() {
        assertThat(evaluator.evaluate(
                Constraint.of("email", ConstraintOperator.STR_CONTAINS, List.of("x")), ctx("other", "y"))).isFalse();
    }

    @Test
    @DisplayName("DATE_*: отсутствующее/невалидное значение → false")
    void dateInvalid() {
        assertThat(evaluator.evaluate(
                Constraint.of("currentTime", ConstraintOperator.DATE_AFTER, List.of("2022-06-05T00:00:00Z")),
                ctx("other", "x"))).isFalse();
        assertThat(evaluator.evaluate(
                Constraint.of("currentTime", ConstraintOperator.DATE_AFTER, List.of("not-a-date")),
                ctx("currentTime", "2022-06-05T00:00:00Z"))).isFalse();
    }

    @Test
    @DisplayName("NUM_*: отсутствующее значение и пустой список → false")
    void numericMissing() {
        assertThat(evaluator.evaluate(
                Constraint.of("age", ConstraintOperator.NUM_GT, List.of("18")), ctx("other", "1"))).isFalse();
        assertThat(evaluator.evaluate(
                Constraint.of("age", ConstraintOperator.NUM_GT, List.of()), ctx("age", "20"))).isFalse();
        assertThat(evaluator.evaluate(
                Constraint.of("age", ConstraintOperator.NUM_GT, List.of("x")), ctx("age", "20"))).isFalse();
    }

    @Test
    @DisplayName("NUM_GTE / NUM_LTE")
    void numericBoundaries() {
        EvaluationContext c = ctx("age", "20");
        assertThat(evaluator.evaluate(Constraint.of("age", ConstraintOperator.NUM_GTE, List.of("20")), c)).isTrue();
        assertThat(evaluator.evaluate(Constraint.of("age", ConstraintOperator.NUM_LTE, List.of("20")), c)).isTrue();
    }

    @Test
    @DisplayName("REGEX: невалидный шаблон и отсутствующее значение → false")
    void regexInvalid() {
        assertThat(evaluator.evaluate(
                Constraint.of("email", ConstraintOperator.REGEX, List.of("[")), ctx("email", "x"))).isFalse();
        assertThat(evaluator.evaluate(
                Constraint.of("email", ConstraintOperator.REGEX, List.of(".*")), ctx("other", "x"))).isFalse();
        Constraint ci = Constraint.of("email", ConstraintOperator.REGEX, List.of(".*@USER\\.COM"))
                .withCaseInsensitive(true);
        assertThat(evaluator.evaluate(ci, ctx("email", "hello@user.com"))).isTrue();
    }

    @Test
    @DisplayName("IN_CIDR: невалидный блок и отсутствующее значение → false")
    void cidrInvalid() {
        assertThat(evaluator.evaluate(
                Constraint.of("remoteAddress", ConstraintOperator.IN_CIDR, List.of("bad")),
                new EvaluationContext(null, null, null, null, "127.0.0.1", null, Map.of()))).isFalse();
        assertThat(evaluator.evaluate(
                Constraint.of("remoteAddress", ConstraintOperator.IN_CIDR, List.of("127.0.0.0/8")),
                ctx("other", "x"))).isFalse();
    }

    @Test
    @DisplayName("IN numeric: значение присутствует и равно")
    void numericInEqual() {
        assertThat(evaluator.evaluate(
                Constraint.of("age", ConstraintOperator.IN, List.of("20"), org.opentoggledimmer.domain.model.ConstraintValueType.NUMBER),
                ctx("age", "20"))).isTrue();
        assertThat(evaluator.evaluate(
                Constraint.of("age", ConstraintOperator.IN, List.of("x"), org.opentoggledimmer.domain.model.ConstraintValueType.NUMBER),
                ctx("age", "20"))).isFalse();
    }
}