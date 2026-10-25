package org.opentoggledimmer.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opentoggledimmer.domain.event.DomainEvent;
import org.opentoggledimmer.domain.event.FeatureFlagCreated;
import org.opentoggledimmer.domain.event.ImpressionEvent;
import org.opentoggledimmer.domain.exception.ValidationException;

/**
 * Тесты агрегатов и событий домена.
 * Tests of domain aggregates and events.
 */
class AggregateTest {

    private static final OffsetDateTime NOW = OffsetDateTime.of(2022, 6, 6, 0, 0, 0, 0, ZoneOffset.UTC);

    @Test
    @DisplayName("FeatureFlag.create выпускает FeatureFlagCreated")
    void createEmitsEvent() {
        FeatureFlag.Created created = FeatureFlag.create(FeatureName.of("f"), ProjectId.defaultProject(), true, NOW);
        assertThat(created.event()).isInstanceOf(FeatureFlagCreated.class);
        assertThat(created.flag().enabled()).isTrue();
    }

    @Test
    @DisplayName("FeatureFlag: зависимость на себя запрещена")
    void selfDependency() {
        assertThatThrownBy(() -> new FeatureFlag(
                FeatureName.of("f"), ProjectId.defaultProject(), true, List.of(),
                List.of(FeatureDependency.enabledWhen("f")), List.of()))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("FeatureFlag: дубликат groupId среди стратегий запрещён")
    void duplicateGroupId() {
        ActivationStrategy a = new GradualRolloutStrategy(
                RolloutPercentage.of(50), "userId", GroupId.of("g"), List.of(), List.of(), false, 1);
        ActivationStrategy b = new GradualRolloutStrategy(
                RolloutPercentage.of(50), "userId", GroupId.of("g"), List.of(), List.of(), false, 2);
        assertThatThrownBy(() -> new FeatureFlag(
                FeatureName.of("f"), ProjectId.defaultProject(), true, List.of(a, b), List.of(), List.of()))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("FeatureFlag.replaceConfiguration и addStrategy выпускают события")
    void changedEvents() {
        FeatureFlag flag = FeatureFlag.create(FeatureName.of("f"), ProjectId.defaultProject(), true, NOW).flag();
        FeatureFlag.Changed replaced = flag.replaceConfiguration(false, List.of(), List.of(), List.of(), NOW);
        assertThat(replaced.flag().enabled()).isFalse();
        FeatureFlag.Changed added = flag.addStrategy(
                new DefaultStrategy("default", List.of(), List.of(), false, 1), NOW);
        assertThat(added.flag().strategies()).hasSize(1);
    }

    @Test
    @DisplayName("FeatureFlag.archive выпускает FeatureFlagArchived")
    void archiveEvent() {
        FeatureFlag flag = FeatureFlag.create(FeatureName.of("f"), ProjectId.defaultProject(), true, NOW).flag();
        DomainEvent event = flag.archive(NOW);
        assertThat(event.featureName()).isEqualTo("f");
        assertThat(event.occurredAt()).isEqualTo(NOW);
    }

    @Test
    @DisplayName("стратегии: типы и legacy kind")
    void strategyTypes() {
        assertThat(new DefaultStrategy(null, null, null, false, 1).type()).isEqualTo("default");
        assertThat(new UserIdsStrategy(null, null, null, false, 1).type()).isEqualTo("userWithId");
        assertThat(new RemoteAddressStrategy(null, null, null, false, 1).type()).isEqualTo("remoteAddress");
        assertThat(new ApplicationHostnameStrategy(null, null, null, false, 1).type()).isEqualTo("applicationHostname");
        assertThat(new CustomStrategy("custom", null, null, false, 1).type()).isEqualTo("custom");
        assertThat(new GradualRolloutStrategy(
                RolloutPercentage.of(1), null, GroupId.of("g"), null, null, false, 1).type())
                .isEqualTo("flexibleRollout");
    }

    @Test
    @DisplayName("LegacyGradualRolloutStrategy: все виды")
    void legacyKinds() {
        assertThat(new LegacyGradualRolloutStrategy(
                LegacyGradualRolloutStrategy.LegacyKind.USER_ID, RolloutPercentage.of(1), null, null, null, false, 1)
                .type()).isEqualTo("gradualRolloutUserId");
        assertThat(new LegacyGradualRolloutStrategy(
                LegacyGradualRolloutStrategy.LegacyKind.SESSION_ID, RolloutPercentage.of(1), null, null, null, false, 1)
                .type()).isEqualTo("gradualRolloutSessionId");
        assertThat(new LegacyGradualRolloutStrategy(
                LegacyGradualRolloutStrategy.LegacyKind.RANDOM, RolloutPercentage.of(1), null, null, null, false, 1)
                .type()).isEqualTo("gradualRolloutRandom");
        assertThatThrownBy(() -> new LegacyGradualRolloutStrategy(null, null, null, null, null, false, 1))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("CustomStrategy: пустое имя запрещено")
    void customStrategyValidation() {
        assertThatThrownBy(() -> new CustomStrategy("", null, null, false, 1))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("ImpressionEvent: типы события")
    void impressionEvent() {
        ImpressionEvent event = new ImpressionEvent(
                ImpressionEvent.ImpressionEventType.IS_ENABLED,
                java.util.UUID.randomUUID(),
                EvaluationContext.empty(),
                true,
                "f",
                null,
                NOW);
        assertThat(event.eventType()).isEqualTo(ImpressionEvent.ImpressionEventType.IS_ENABLED);
        assertThat(ImpressionEvent.ImpressionEventType.values()).hasSize(2);
    }

    @Test
    @DisplayName("ConstraintOperator: классификация")
    void constraintOperatorClassification() {
        assertThat(ConstraintOperator.STR_CONTAINS.isStringBased()).isTrue();
        assertThat(ConstraintOperator.REGEX.isStringBased()).isTrue();
        assertThat(ConstraintOperator.IN.isListBased()).isTrue();
        assertThat(ConstraintOperator.NOT_IN.isListBased()).isTrue();
        assertThat(ConstraintOperator.NUM_GT.isStringBased()).isFalse();
    }

    @Test
    @DisplayName("VariantOverride: валидация")
    void variantOverride() {
        assertThatThrownBy(() -> new VariantOverride("", List.of()))
                .isInstanceOf(ValidationException.class);
    }
}