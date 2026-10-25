package org.opentoggledimmer.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opentoggledimmer.application.dto.VariantResultDto;
import org.opentoggledimmer.application.port.out.DomainEventPublisher;
import org.opentoggledimmer.application.port.out.FeatureFlagRepository;
import org.opentoggledimmer.application.port.out.SegmentRepository;
import org.opentoggledimmer.domain.event.DomainEvent;
import org.opentoggledimmer.domain.event.FeatureFlagCreated;
import org.opentoggledimmer.domain.model.DefaultStrategy;
import org.opentoggledimmer.domain.model.FeatureFlag;
import org.opentoggledimmer.domain.model.FeatureName;
import org.opentoggledimmer.domain.model.ProjectId;
import org.opentoggledimmer.domain.model.EvaluationContext;
import org.opentoggledimmer.domain.port.Clock;
import org.opentoggledimmer.domain.service.ConstraintEvaluator;
import org.opentoggledimmer.domain.service.FlagEvaluationService;
import org.opentoggledimmer.domain.service.StickinessCalculator;

/**
 * Тесты сценариев применения: вычисление, варианты, управление, синхронизация.
 * Tests of application use cases: evaluation, variants, management, sync.
 */
@ExtendWith(MockitoExtension.class)
class UseCaseTest {

    private static final OffsetDateTime FIXED = OffsetDateTime.of(2022, 6, 6, 0, 0, 0, 0, ZoneOffset.UTC);

    @Mock
    private FeatureFlagRepository featureFlagRepository;
    @Mock
    private SegmentRepository segmentRepository;
    @Mock
    private DomainEventPublisher eventPublisher;

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
    @DisplayName("isEnabled: подставляет currentTime через Clock и делегирует домену")
    void isEnabledDelegates() {
        FeatureFlag flag = new FeatureFlag(
                FeatureName.of("f"), ProjectId.defaultProject(), true,
                List.of(new DefaultStrategy("default", List.of(), List.of(), false, 1)), List.of(), List.of());
        when(featureFlagRepository.findByName(FeatureName.of("f"))).thenReturn(Optional.of(flag));
        when(featureFlagRepository.findAllByName()).thenReturn(Map.of("f", flag));
        when(segmentRepository.findAllById()).thenReturn(Map.of());

        EvaluateFeatureService service = new EvaluateFeatureService(support);
        assertThat(service.isEnabled(FeatureName.of("f"), EvaluationContext.empty())).isTrue();
        verify(segmentRepository).findAllById();
    }

    @Test
    @DisplayName("isEnabled: неизвестный флаг → false")
    void isEnabledUnknown() {
        when(featureFlagRepository.findByName(FeatureName.of("ghost"))).thenReturn(Optional.empty());
        EvaluateFeatureService service = new EvaluateFeatureService(support);
        assertThat(service.isEnabled(FeatureName.of("ghost"), EvaluationContext.empty())).isFalse();
    }

    @Test
    @DisplayName("getVariant: неизвестный флаг → disabled fallback")
    void getVariantUnknown() {
        when(featureFlagRepository.findByName(FeatureName.of("ghost"))).thenReturn(Optional.empty());
        GetVariantService service = new GetVariantService(support);
        VariantResultDto result = service.getVariant(FeatureName.of("ghost"), EvaluationContext.empty());
        assertThat(result.name()).isEqualTo("disabled");
        assertThat(result.featureEnabled()).isFalse();
    }

    @Test
    @DisplayName("create: сохраняет флаг и публикует FeatureFlagCreated")
    void createPublishesEvent() {
        ManageFeatureFlagService service =
                new ManageFeatureFlagService(featureFlagRepository, eventPublisher, clock);
        FeatureFlag flag = service.create(FeatureName.of("new"), ProjectId.defaultProject(), true);
        assertThat(flag.name().value()).isEqualTo("new");
        verify(featureFlagRepository).save(any(FeatureFlag.class));
        verify(eventPublisher).publish(any(FeatureFlagCreated.class));
    }

    @Test
    @DisplayName("archive: удаляет флаг и публикует событие")
    void archivePublishesEvent() {
        FeatureFlag flag = new FeatureFlag(
                FeatureName.of("f"), ProjectId.defaultProject(), true, List.of(), List.of(), List.of());
        when(featureFlagRepository.findByName(FeatureName.of("f"))).thenReturn(Optional.of(flag));
        ManageFeatureFlagService service =
                new ManageFeatureFlagService(featureFlagRepository, eventPublisher, clock);
        service.archive(FeatureName.of("f"));
        verify(featureFlagRepository).remove(FeatureName.of("f"));
        verify(eventPublisher).publish(any(DomainEvent.class));
    }

    @Test
    @DisplayName("sync: при ошибке загрузчика возвращает последний снапшот")
    void syncFallsBackToStore() {
        org.opentoggledimmer.application.port.out.SnapshotLoader loader =
                new org.opentoggledimmer.application.port.out.SnapshotLoader() {
                    @Override
                    public org.opentoggledimmer.application.dto.FeatureSnapshot load() {
                        throw new IllegalStateException("boom");
                    }

                    @Override
                    public String sourceName() {
                        return "test-loader";
                    }
                };
        FeatureSnapshotHolder holder = new FeatureSnapshotHolder();
        SyncFeatureConfigurationService service = new SyncFeatureConfigurationService(loader, holder);
        holder.store(org.opentoggledimmer.application.dto.FeatureSnapshot.of(List.of(), List.of()));
        assertThat(service.sync().version()).isEqualTo(2);
    }

    /**
     * Простое хранилище снапшота для теста.
     * A simple snapshot store for the test.
     */
    private static final class FeatureSnapshotHolder
            implements org.opentoggledimmer.application.port.out.SnapshotStore {
        private org.opentoggledimmer.application.dto.FeatureSnapshot snapshot;

        @Override
        public void store(org.opentoggledimmer.application.dto.FeatureSnapshot snapshot) {
            this.snapshot = snapshot;
        }

        @Override
        public Optional<org.opentoggledimmer.application.dto.FeatureSnapshot> loadLast() {
            return Optional.ofNullable(snapshot);
        }
    }
}