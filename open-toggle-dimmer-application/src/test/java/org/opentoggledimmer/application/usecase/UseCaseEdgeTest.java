package org.opentoggledimmer.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opentoggledimmer.application.dto.FeatureSnapshot;
import org.opentoggledimmer.application.dto.VariantResultDto;
import org.opentoggledimmer.application.port.out.DomainEventPublisher;
import org.opentoggledimmer.application.port.out.FeatureFlagRepository;
import org.opentoggledimmer.application.port.out.SegmentRepository;
import org.opentoggledimmer.application.port.out.SnapshotLoader;
import org.opentoggledimmer.application.port.out.SnapshotStore;
import org.opentoggledimmer.domain.exception.DomainException;
import org.opentoggledimmer.domain.model.DefaultStrategy;
import org.opentoggledimmer.domain.model.FeatureFlag;
import org.opentoggledimmer.domain.model.FeatureName;
import org.opentoggledimmer.domain.model.ProjectId;
import org.opentoggledimmer.domain.model.EvaluationContext;
import org.opentoggledimmer.domain.model.Variant;
import org.opentoggledimmer.domain.port.Clock;
import org.opentoggledimmer.domain.service.ConstraintEvaluator;
import org.opentoggledimmer.domain.service.FlagEvaluationService;
import org.opentoggledimmer.domain.service.StickinessCalculator;

/**
 * Дополнительные тесты сценариев: ошибки, синхронизация, варианты.
 * Additional use case tests: errors, synchronization, variants.
 */
@ExtendWith(MockitoExtension.class)
class UseCaseEdgeTest {

    private static final OffsetDateTime FIXED = OffsetDateTime.of(2022, 6, 6, 0, 0, 0, 0, ZoneOffset.UTC);

    @Mock
    private FeatureFlagRepository featureFlagRepository;
    @Mock
    private SegmentRepository segmentRepository;
    @Mock
    private DomainEventPublisher eventPublisher;
    @Mock
    private SnapshotLoader loader;
    @Mock
    private SnapshotStore store;

    private Clock clock;
    private EvaluationSupport support;

    @BeforeEach
    void setUp() {
        clock = () -> FIXED;
        FlagEvaluationService evaluation = new FlagEvaluationService(
                new ConstraintEvaluator(),
                new StickinessCalculator((o, b) -> o),
                (o, b) -> o,
                () -> "host");
        support = new EvaluationSupport(featureFlagRepository, segmentRepository, evaluation, clock);
    }

    @Test
    @DisplayName("replace: неизвестный флаг → DomainException")
    void replaceUnknown() {
        when(featureFlagRepository.findByName(FeatureName.of("ghost"))).thenReturn(Optional.empty());
        ManageFeatureFlagService service = new ManageFeatureFlagService(featureFlagRepository, eventPublisher, clock);
        assertThatThrownBy(() -> service.replace(FeatureName.of("ghost"), true, List.of(), List.of(), List.of()))
                .isInstanceOf(DomainException.class);
    }

    @Test
    @DisplayName("addStrategy: публикует событие и сохраняет")
    void addStrategy() {
        FeatureFlag flag = new FeatureFlag(
                FeatureName.of("f"), ProjectId.defaultProject(), true, List.of(), List.of(), List.of());
        when(featureFlagRepository.findByName(FeatureName.of("f"))).thenReturn(Optional.of(flag));
        ManageFeatureFlagService service = new ManageFeatureFlagService(featureFlagRepository, eventPublisher, clock);
        FeatureFlag updated = service.addStrategy(
                FeatureName.of("f"), new DefaultStrategy("default", List.of(), List.of(), false, 1));
        assertThat(updated.strategies()).hasSize(1);
    }

    @Test
    @DisplayName("sync: успешная загрузка сохраняется в store")
    void syncSuccess() {
        FeatureSnapshot snapshot = FeatureSnapshot.of(List.of(), List.of());
        when(loader.load()).thenReturn(snapshot);
        SyncFeatureConfigurationService service = new SyncFeatureConfigurationService(loader, store);
        assertThat(service.sync()).isEqualTo(snapshot);
        org.mockito.Mockito.verify(store).store(snapshot);
    }

    @Test
    @DisplayName("sync: нет снапшота и загрузчик упал → DomainException")
    void syncNoFallback() {
        when(loader.load()).thenThrow(new IllegalStateException("boom"));
        when(loader.sourceName()).thenReturn("test");
        when(store.loadLast()).thenReturn(Optional.empty());
        SyncFeatureConfigurationService service = new SyncFeatureConfigurationService(loader, store);
        assertThatThrownBy(service::sync).isInstanceOf(DomainException.class);
    }

    @Test
    @DisplayName("getVariant: включённый флаг с вариантом")
    void getVariantSelected() {
        Variant v = Variant.of("blue", 1000);
        FeatureFlag flag = new FeatureFlag(
                FeatureName.of("f"), ProjectId.defaultProject(), true, List.of(), List.of(), List.of(v));
        when(featureFlagRepository.findByName(FeatureName.of("f"))).thenReturn(Optional.of(flag));
        when(featureFlagRepository.findAllByName()).thenReturn(java.util.Map.of("f", flag));
        when(segmentRepository.findAllById()).thenReturn(java.util.Map.of());
        GetVariantService service = new GetVariantService(support);
        VariantResultDto result = service.getVariant(
                FeatureName.of("f"), new EvaluationContext(null, null, "u", null, null, null, java.util.Map.of()));
        assertThat(result.enabled()).isTrue();
        assertThat(result.name()).isEqualTo("blue");
    }

    @Test
    @DisplayName("isEnabled: флаг с зависимостью от отсутствующего родителя")
    void dependencyMissing() {
        FeatureFlag child = new FeatureFlag(
                FeatureName.of("child"), ProjectId.defaultProject(), true, List.of(),
                List.of(org.opentoggledimmer.domain.model.FeatureDependency.enabledWhen("ghost")), List.of());
        when(featureFlagRepository.findByName(FeatureName.of("child"))).thenReturn(Optional.of(child));
        when(featureFlagRepository.findAllByName()).thenReturn(java.util.Map.of("child", child));
        when(segmentRepository.findAllById()).thenReturn(java.util.Map.of());
        EvaluateFeatureService service = new EvaluateFeatureService(support);
        assertThat(service.isEnabled(FeatureName.of("child"), EvaluationContext.empty())).isFalse();
    }
}