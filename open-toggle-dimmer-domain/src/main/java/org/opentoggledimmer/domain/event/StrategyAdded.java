package org.opentoggledimmer.domain.event;

import java.time.OffsetDateTime;

/**
 * Доменное событие: к флагу добавлена стратегия.
 * Domain event: a strategy was added to a flag.
 *
 * <hr>
 *
 * @param featureName   имя флага / feature flag name
 * @param strategyType  тип стратегии / the strategy type
 * @param occurredAt    момент события / event timestamp
 */
public record StrategyAdded(String featureName, String strategyType, OffsetDateTime occurredAt)
        implements DomainEvent { }