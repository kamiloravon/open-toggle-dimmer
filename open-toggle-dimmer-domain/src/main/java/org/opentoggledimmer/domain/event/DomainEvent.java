package org.opentoggledimmer.domain.event;

import java.time.OffsetDateTime;

/**
 * Базовый маркер доменных событий.
 * Base marker for domain events.
 *
 * <hr>
 *
 * <p>Все доменные события — неизменяемые записи, помеченные этим интерфейсом.
 * All domain events are immutable records tagged with this interface.</p>
 */
public sealed interface DomainEvent
        permits FeatureFlagCreated,
                FeatureFlagConfigChanged,
                StrategyAdded,
                FeatureFlagArchived {

    /**
     * Имя флага, к которому относится событие.
     * The flag name the event belongs to.
     *
     * @return имя флага / the flag name
     */
    String featureName();

    /**
     * Момент события.
     * The event instant.
     *
     * @return момент / the instant
     */
    OffsetDateTime occurredAt();
}