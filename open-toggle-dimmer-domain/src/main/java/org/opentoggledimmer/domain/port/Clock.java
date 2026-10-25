package org.opentoggledimmer.domain.port;

import java.time.OffsetDateTime;

/**
 * Доменный порт: источник текущего времени.
 * Domain port: current time source.
 *
 * <hr>
 *
 * <p>Единственный допустимый источник времени в домене. Запрещает прямой вызов
 * {@code OffsetDateTime.now()} и тип {@code Instant}. Реализуется адаптером.
 * The only permitted time source in the domain. Forbids direct
 * {@code OffsetDateTime.now()} calls and the {@code Instant} type. Implemented
 * by an adapter.</p>
 */
@FunctionalInterface
public interface Clock {

    /**
     * Возвращает текущий момент в UTC.
     * Returns the current instant in UTC.
     *
     * @return текущее время в UTC / the current time in UTC
     */
    OffsetDateTime nowUtc();
}