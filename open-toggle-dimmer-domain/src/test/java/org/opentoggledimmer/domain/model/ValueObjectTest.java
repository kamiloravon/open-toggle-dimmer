package org.opentoggledimmer.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opentoggledimmer.domain.exception.DomainException;
import org.opentoggledimmer.domain.exception.ValidationException;

/**
 * Тесты value objects домена.
 * Tests of domain value objects.
 */
class ValueObjectTest {

    @Test
    @DisplayName("FeatureName: валидное имя и URL-safe проверка")
    void featureName() {
        assertThat(FeatureName.of("my.flag-1").value()).isEqualTo("my.flag-1");
        assertThatThrownBy(() -> FeatureName.of("")).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> FeatureName.of("bad name")).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> FeatureName.of("a".repeat(101))).isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("ProjectId: непустой и default")
    void projectId() {
        assertThat(ProjectId.of("p").value()).isEqualTo("p");
        assertThat(ProjectId.defaultProject().value()).isEqualTo("default");
        assertThatThrownBy(() -> ProjectId.of(" ")).isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("EnvironmentName: непустой и default")
    void environmentName() {
        assertThat(EnvironmentName.of("prod").value()).isEqualTo("prod");
        assertThat(EnvironmentName.defaultEnvironment().value()).isEqualTo("default");
        assertThatThrownBy(() -> EnvironmentName.of("")).isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("GroupId: непустой и дефолт от имени флага")
    void groupId() {
        assertThat(GroupId.of("g").value()).isEqualTo("g");
        assertThat(GroupId.forFeature(FeatureName.of("flag")).value()).isEqualTo("flag");
        assertThatThrownBy(() -> GroupId.of("")).isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("StrategyName: непустой")
    void strategyName() {
        assertThat(StrategyName.of("flexibleRollout").value()).isEqualTo("flexibleRollout");
        assertThatThrownBy(() -> StrategyName.of("")).isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("VariantName: непустой")
    void variantName() {
        assertThat(VariantName.of("blue").value()).isEqualTo("blue");
        assertThatThrownBy(() -> VariantName.of("")).isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("RolloutPercentage: диапазон и includes")
    void rolloutPercentage() {
        assertThat(RolloutPercentage.of(100).includes(50)).isTrue();
        assertThat(RolloutPercentage.of(0).includes(1)).isFalse();
        assertThatThrownBy(() -> RolloutPercentage.of(-1)).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> RolloutPercentage.of(101)).isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("VariantWeight: диапазон")
    void variantWeight() {
        assertThat(VariantWeight.of(0).value()).isZero();
        assertThat(VariantWeight.of(1000).value()).isEqualTo(1000);
        assertThatThrownBy(() -> VariantWeight.of(1001)).isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("MurmurHashValue: диапазон и нормализация")
    void murmurHashValue() {
        assertThat(MurmurHashValue.ofUnsigned(3070718447L).normalized(100)).isEqualTo(48);
        assertThat(MurmurHashValue.ofUnsigned(0).normalized(100)).isEqualTo(1);
        assertThatThrownBy(() -> MurmurHashValue.ofUnsigned(-1)).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> MurmurHashValue.ofUnsigned(0x1_0000_0000L)).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> MurmurHashValue.ofUnsigned(1).normalized(0)).isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("CidrBlock: вхождение IPv4/IPv6 и одиночный IP")
    void cidrBlock() {
        assertThat(CidrBlock.of("127.0.0.0/8").contains("127.0.0.1")).isTrue();
        assertThat(CidrBlock.of("10.0.0.0/8").contains("127.0.0.1")).isFalse();
        assertThat(CidrBlock.of("192.168.1.1").contains("192.168.1.1")).isTrue();
        assertThat(CidrBlock.of("2001:db8::/32").contains("2001:db8::1")).isTrue();
        assertThat(CidrBlock.of("127.0.0.0/8").contains("not-an-ip")).isFalse();
        assertThatThrownBy(() -> CidrBlock.of("")).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> CidrBlock.of("127.0.0.0/33")).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> CidrBlock.of("bad/8")).isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("Constraint: типы значений и флаги")
    void constraint() {
        Constraint c = Constraint.of("age", ConstraintOperator.IN, java.util.List.of("1"), ConstraintValueType.NUMBER);
        assertThat(c.valueType()).isEqualTo(ConstraintValueType.NUMBER);
        assertThat(c.withInverted(true).inverted()).isTrue();
        assertThat(c.withCaseInsensitive(true).caseInsensitive()).isTrue();
        assertThatThrownBy(() -> Constraint.of("", ConstraintOperator.IN, java.util.List.of())).isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("EvaluationContext: чтение стандартных полей и properties")
    void unleashContext() {
        EvaluationContext c = new EvaluationContext("app", "prod", "u", "s", "1.2.3.4", null, Map.of("tenant", "acme"));
        assertThat(c.get("appName")).contains("app");
        assertThat(c.get("userId")).contains("u");
        assertThat(c.get("tenant")).contains("acme");
        assertThat(c.get("missing")).isEmpty();
        assertThat(c.get("currentTime")).isEmpty();
        assertThat(c.withCurrentTime(java.time.OffsetDateTime.parse("2022-06-06T00:00:00Z")).get("currentTime")).isPresent();
        assertThat(EvaluationContext.empty().get(null)).isEmpty();
    }

    @Test
    @DisplayName("Variant: payload optional и вес")
    void variant() {
        Variant v = Variant.of("blue", 100);
        assertThat(v.payloadOptional()).isEmpty();
        assertThat(v.weight().value()).isEqualTo(100);
    }

    @Test
    @DisplayName("VariantPayload: валидация типа")
    void variantPayload() {
        assertThat(VariantPayload.of("json", "{}").value()).isEqualTo("{}");
        assertThat(VariantPayload.of("json", null).value()).isEmpty();
        assertThatThrownBy(() -> VariantPayload.of("", "x")).isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("FeatureDependency: фабрики")
    void featureDependency() {
        assertThat(FeatureDependency.enabledWhen("p").enabled()).isTrue();
        assertThat(FeatureDependency.disabledWhen("p").enabled()).isFalse();
        assertThat(FeatureDependency.enabledWhen("p").hasVariantExpectation()).isFalse();
        assertThatThrownBy(() -> new FeatureDependency("", true, null)).isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("Segment: инварианты")
    void segment() {
        assertThat(Segment.of(1L, "s", null).constraints()).isEmpty();
        assertThatThrownBy(() -> Segment.of(0L, "s", null)).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> Segment.of(1L, "", null)).isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("DomainException с причиной")
    void domainException() {
        Exception cause = new IllegalStateException("cause");
        assertThat(new DomainException("msg", cause)).hasCause(cause);
    }
}