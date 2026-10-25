package org.opentoggledimmer.domain.event;

import java.time.OffsetDateTime;

/**
 * Доменное событие: флаг создан.
 * Domain event: a feature flag was created.
 *
 * <hr>
 *
 * @param featureName имя флага / feature flag name
 * @param enabled     начальное состояние / initial enabled state
 * @param occurredAt  момент события / event timestamp
 */
public record FeatureFlagCreated(String featureName, boolean enabled, OffsetDateTime occurredAt)
        implements DomainEvent { }