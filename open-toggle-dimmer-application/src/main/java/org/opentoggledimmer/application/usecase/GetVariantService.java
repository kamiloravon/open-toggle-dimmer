package org.opentoggledimmer.application.usecase;

import org.opentoggledimmer.application.dto.VariantResultDto;
import org.opentoggledimmer.application.port.in.GetVariantUseCase;
import org.opentoggledimmer.domain.model.FeatureFlag;
import org.opentoggledimmer.domain.model.FeatureName;
import org.opentoggledimmer.domain.model.EvaluationContext;
import org.opentoggledimmer.domain.service.FlagEvaluationService;

/**
 * Реализация {@link GetVariantUseCase}.
 * Implementation of {@link GetVariantUseCase}.
 *
 * <hr>
 *
 * <p>Подставляет {@code currentTime}, находит флаг и делегирует выбор варианта
 * доменному сервису. Неизвестный флаг → fallback {@code disabled}.
 * Supplies {@code currentTime}, finds the flag and delegates variant selection
 * to the domain service. An unknown flag → {@code disabled} fallback.</p>
 */
public final class GetVariantService implements GetVariantUseCase {

    private final EvaluationSupport support;

    /**
     * Создаёт сервис.
     * Creates the service.
     *
     * @param support поддержка вычисления / the evaluation support
     */
    public GetVariantService(EvaluationSupport support) {
        this.support = support;
    }

    /**
     * {@inheritDoc}
     * @param name    имя флага / the flag name
     * @param context контекст / the context
     * @return результат варианта / the variant result
     */
    @Override
    public VariantResultDto getVariant(FeatureName name, EvaluationContext context) {
        FeatureFlag flag = support.findFlag(name);
        if (flag == null) {
            return VariantResultDto.disabled(false);
        }
        EvaluationContext normalized = support.normalize(context);
        FlagEvaluationService.VariantResult result = support.evaluationService().getVariant(
                flag,
                normalized,
                support.flagsByName(),
                support.segments());
        if (!result.enabled()) {
            return VariantResultDto.disabled(result.featureEnabled());
        }
        return VariantResultDto.of(result.name(), result.payload());
    }
}