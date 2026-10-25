package org.opentoggledimmer.domain.event;

import java.time.OffsetDateTime;

/**
 * Доменное событие: конфигурация флага была заменена.
 * Domain event: feature flag configuration was replaced.
 *
 * <hr>
 *
 * @param featureName имя флага / feature flag name
 * @param enabled     новое состояние / the new enabled state
 * @param occurredAt  момент события / event timestamp
 */
public record FeatureFlagConfigChanged(String featureName, boolean enabled, OffsetDateTime occurredAt)
        implements DomainEvent { }