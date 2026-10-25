package org.opentoggledimmer.application.port.out;

import org.opentoggledimmer.domain.event.DomainEvent;

/**
 * Исходящий порт: публикатор доменных событий.
 * Outbound port: domain event publisher.
 *
 * <hr>
 *
 * <p>Публикует доменные события после успешного сохранения агрегата.
 * Publishes domain events after a successful aggregate save.</p>
 */
@FunctionalInterface
public interface DomainEventPublisher {

    /**
     * Публикует событие.
     * Publishes an event.
     *
     * @param event событие / the event
     */
    void publish(DomainEvent event);
}