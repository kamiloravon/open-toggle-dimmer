package org.opentoggledimmer.application.usecase;

import org.opentoggledimmer.application.port.in.EvaluateFeatureUseCase;
import org.opentoggledimmer.domain.model.FeatureFlag;
import org.opentoggledimmer.domain.model.FeatureName;
import org.opentoggledimmer.domain.model.EvaluationContext;

/**
 * Реализация {@link EvaluateFeatureUseCase}.
 * Implementation of {@link EvaluateFeatureUseCase}.
 *
 * <hr>
 *
 * <p>Подставляет {@code currentTime}, находит флаг и делегирует вычисление
 * доменному сервису. Неизвестный флаг → {@code false}.
 * Supplies {@code currentTime}, finds the flag and delegates evaluation to the
 * domain service. An unknown flag → {@code false}.</p>
 */
public final class EvaluateFeatureService implements EvaluateFeatureUseCase {

    private final EvaluationSupport support;

    /**
     * Создаёт сервис.
     * Creates the service.
     *
     * @param support поддержка вычисления / the evaluation support
     */
    public EvaluateFeatureService(EvaluationSupport support) {
        this.support = support;
    }

    /**
     * {@inheritDoc}
     * @param name    имя флага / the flag name
     * @param context контекст / the context
     * @return включён ли флаг / whether the flag is enabled
     */
    @Override
    public boolean isEnabled(FeatureName name, EvaluationContext context) {
        FeatureFlag flag = support.findFlag(name);
        if (flag == null) {
            return false;
        }
        EvaluationContext normalized = support.normalize(context);
        return support.evaluationService().isEnabled(
                flag,
                normalized,
                support.flagsByName(),
                support.segments());
    }
}