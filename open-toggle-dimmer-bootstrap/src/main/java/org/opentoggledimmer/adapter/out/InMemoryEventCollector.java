package org.opentoggledimmer.adapter.out;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.opentoggledimmer.domain.event.DomainEvent;
import org.opentoggledimmer.domain.event.ImpressionEvent;
import org.opentoggledimmer.application.port.out.DomainEventPublisher;
import org.opentoggledimmer.application.port.out.ImpressionEventPublisher;

/**
 * In-memory коллектор событий (доменных и impression).
 * In-memory event collector (domain and impression).
 *
 * <hr>
 *
 * <p>Сохраняет события в список для тестов и локальной диагностики. Реализует
 * оба порта публикации событий.
 * Stores events in a list for tests and local diagnostics. Implements both event
 * publishing ports.</p>
 */
public final class InMemoryEventCollector implements DomainEventPublisher, ImpressionEventPublisher {

    private final List<DomainEvent> domainEvents = new ArrayList<>();
    private final List<ImpressionEvent> impressionEvents = new ArrayList<>();

    /**
     * {@inheritDoc}
     * @param event событие / the event
     */
    @Override
    public void publish(DomainEvent event) {
        domainEvents.add(event);
    }

    /**
     * {@inheritDoc}
     * @param event событие / the event
     */
    @Override
    public void publish(ImpressionEvent event) {
        impressionEvents.add(event);
    }

    /**
     * Возвращает собранные доменные события.
     * Returns collected domain events.
     *
     * @return доменные события / domain events
     */
    public List<DomainEvent> domainEvents() {
        return List.copyOf(domainEvents);
    }

    /**
     * Возвращает собранные impression-события.
     * Returns collected impression events.
     *
     * @return impression-события / impression events
     */
    public List<ImpressionEvent> impressionEvents() {
        return List.copyOf(impressionEvents);
    }

    /**
     * Возвращает текущее время UTC (утилита для примеров).
     * Returns the current UTC time (a utility for examples).
     *
     * @return текущее время / the current time
     */
    public static OffsetDateTime now() {
        return OffsetDateTime.now(ZoneOffset.UTC);
    }
}