package org.opentoggledimmer.application.usecase;

import java.time.OffsetDateTime;
import org.opentoggledimmer.application.port.out.FeatureFlagRepository;
import org.opentoggledimmer.application.port.out.SegmentRepository;
import org.opentoggledimmer.domain.model.FeatureFlag;
import org.opentoggledimmer.domain.model.FeatureName;
import org.opentoggledimmer.domain.model.Segment;
import org.opentoggledimmer.domain.model.EvaluationContext;
import org.opentoggledimmer.domain.port.Clock;
import org.opentoggledimmer.domain.service.FlagEvaluationService;

/**
 * Общая логика сценариев вычисления: нормализация контекста и сборка снимка.
 * Shared evaluation logic: context normalization and snapshot assembly.
 *
 * <hr>
 *
 * <p>Не является use case по отдельности; используется реализациями
 * {@code EvaluateFeatureUseCase} и {@code GetVariantUseCase}, чтобы избежать
 * дублирования и подставлять {@code currentTime} из {@link Clock}.
 * Not a use case on its own; used by {@code EvaluateFeatureUseCase} and
 * {@code GetVariantUseCase} implementations to avoid duplication and to supply
 * {@code currentTime} from the {@link Clock}.</p>
 */
public final class EvaluationSupport {

    private final FeatureFlagRepository featureFlagRepository;
    private final SegmentRepository segmentRepository;
    private final FlagEvaluationService evaluationService;
    private final Clock clock;

    /**
     * Создаёт поддержку вычисления.
     * Creates the evaluation support.
     *
     * @param featureFlagRepository репозиторий флагов / flag repository
     * @param segmentRepository     репозиторий сегментов / segment repository
     * @param evaluationService     доменный сервис / the domain service
     * @param clock                 источник времени / the time source
     */
    public EvaluationSupport(
            FeatureFlagRepository featureFlagRepository,
            SegmentRepository segmentRepository,
            FlagEvaluationService evaluationService,
            Clock clock) {
        this.featureFlagRepository = featureFlagRepository;
        this.segmentRepository = segmentRepository;
        this.evaluationService = evaluationService;
        this.clock = clock;
    }

    /**
     * Подставляет {@code currentTime} через {@link Clock}, если он не задан.
     * Supplies {@code currentTime} via the {@link Clock} if it is not set.
     *
     * @param context контекст / the context
     * @return нормализованный контекст / the normalized context
     */
    public EvaluationContext normalize(EvaluationContext context) {
        OffsetDateTime now = context.currentTime() == null ? clock.nowUtc() : context.currentTime();
        return context.withCurrentTime(now);
    }

    /**
     * Находит флаг по имени.
     * Finds a flag by name.
     *
     * @param name имя / the name
     * @return флаг или {@code null} / the flag or {@code null}
     */
    public FeatureFlag findFlag(FeatureName name) {
        return featureFlagRepository.findByName(name).orElse(null);
    }

    /**
     * Возвращает доменный сервис вычисления.
     * Returns the domain evaluation service.
     *
     * @return сервис / the service
     */
    public FlagEvaluationService evaluationService() {
        return evaluationService;
    }

    /**
     * Возвращает сегменты по id.
     * Returns segments by id.
     *
     * @return карта сегментов / the segment map
     */
    public java.util.Map<Long, Segment> segments() {
        return segmentRepository.findAllById();
    }

    /**
     * Возвращает все флаги по имени.
     * Returns all flags by name.
     *
     * @return карта имя → флаг / a map of name → flag
     */
    public java.util.Map<String, FeatureFlag> flagsByName() {
        return featureFlagRepository.findAllByName();
    }
}