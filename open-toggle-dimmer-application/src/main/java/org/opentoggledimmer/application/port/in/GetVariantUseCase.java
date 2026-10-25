package org.opentoggledimmer.application.port.in;

import org.opentoggledimmer.application.dto.VariantResultDto;
import org.opentoggledimmer.domain.model.FeatureName;
import org.opentoggledimmer.domain.model.EvaluationContext;

/**
 * Входящий порт: выбор варианта флага.
 * Inbound port: flag variant selection.
 *
 * <hr>
 *
 * <p>Сценарий использования: возвращает выбранный вариант или fallback
 * {@code disabled}.
 * Use case: returns the selected variant or the {@code disabled} fallback.</p>
 */
public interface GetVariantUseCase {

    /**
     * Возвращает вариант флага для контекста.
     * Returns the flag variant for the context.
     *
     * @param name    имя флага / the flag name
     * @param context контекст / the context
     * @return результат варианта / the variant result
     */
    VariantResultDto getVariant(FeatureName name, EvaluationContext context);
}