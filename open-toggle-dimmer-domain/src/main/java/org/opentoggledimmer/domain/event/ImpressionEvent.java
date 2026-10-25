package org.opentoggledimmer.domain.event;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.opentoggledimmer.domain.model.EvaluationContext;

/**
 * Доменное событие: impression (факт вычисления флага).
 * Domain event: impression (a flag evaluation happened).
 *
 * <hr>
 *
 * <p>Эмитится только для флагов с включённым {@code impressionData}. Содержит
 * тип события, уникальный id, контекст, результат и, для {@code GET_VARIANT},
 * имя варианта.
 * Emitted only for flags with {@code impressionData} enabled. Contains the
 * event type, a unique id, the context, the result and, for {@code GET_VARIANT},
 * the variant name.</p>
 *
 * @param eventType   тип события / the event type
 * @param eventId     уникальный идентификатор / a unique identifier
 * @param context     контекст / the context
 * @param enabled     результат / the result
 * @param featureName имя флага / the feature flag name
 * @param variant     имя варианта (только для {@code GET_VARIANT}) /
 *                    the variant name (only for {@code GET_VARIANT})
 * @param occurredAt  момент / the instant
 */
public record ImpressionEvent(
        ImpressionEventType eventType,
        UUID eventId,
        EvaluationContext context,
        boolean enabled,
        String featureName,
        String variant,
        OffsetDateTime occurredAt) {

    /**
     * Тип impression-события.
     * Impression event type.
     */
    public enum ImpressionEventType {
        /** Событие {@code isEnabled} / an {@code isEnabled} event. */
        IS_ENABLED,
        /** Событие {@code getVariant} / a {@code getVariant} event. */
        GET_VARIANT
    }
}