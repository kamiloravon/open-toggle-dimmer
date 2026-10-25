package org.opentoggledimmer.application.port.out;

import org.opentoggledimmer.domain.event.ImpressionEvent;

/**
 * Исходящий порт: публикатор impression-событий.
 * Outbound port: impression event publisher.
 *
 * <hr>
 *
 * <p>Публикует impression-события для флагов с включённым {@code impressionData}.
 * Publishes impression events for flags with {@code impressionData} enabled.</p>
 */
@FunctionalInterface
public interface ImpressionEventPublisher {

    /**
     * Публикует impression-событие.
     * Publishes an impression event.
     *
     * @param event событие / the event
     */
    void publish(ImpressionEvent event);
}