package org.opentoggledimmer.domain.event;

import java.time.OffsetDateTime;

/**
 * Доменное событие: флаг архивирован.
 * Domain event: a feature flag was archived.
 *
 * <hr>
 *
 * @param featureName имя флага / feature flag name
 * @param occurredAt  момент события / event timestamp
 */
public record FeatureFlagArchived(String featureName, OffsetDateTime occurredAt)
        implements DomainEvent { }