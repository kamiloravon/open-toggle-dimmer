package org.opentoggledimmer.application.port.in;

import org.opentoggledimmer.domain.model.FeatureName;
import org.opentoggledimmer.domain.model.EvaluationContext;

/**
 * Входящий порт: вычисление активации флага.
 * Inbound port: flag activation evaluation.
 *
 * <hr>
 *
 * <p>Сценарий использования: перед вычислением подставляет {@code currentTime}
 * через доменный порт {@code Clock}, если он не задан в контексте.
 * Use case: before evaluation, supplies {@code currentTime} via the domain
 * {@code Clock} port if it is not set in the context.</p>
 */
public interface EvaluateFeatureUseCase {

    /**
     * Проверяет, включён ли флаг для контекста.
     * Checks whether the flag is enabled for the context.
     *
     * @param name    имя флага / the flag name
     * @param context контекст / the context
     * @return {@code true}, если флаг активен / {@code true} if enabled
     */
    boolean isEnabled(FeatureName name, EvaluationContext context);
}