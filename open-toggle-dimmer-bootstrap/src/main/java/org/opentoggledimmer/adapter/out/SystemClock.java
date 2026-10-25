package org.opentoggledimmer.adapter.out;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.opentoggledimmer.domain.port.Clock;

/**
 * Адаптер {@link Clock} на системных часах.
 * {@link Clock} adapter backed by the system clock.
 *
 * <hr>
 *
 * <p>Возвращает текущее время в UTC. Единственное место, где допустим
 * {@code OffsetDateTime.now()}.
 * Returns the current time in UTC. The only place where
 * {@code OffsetDateTime.now()} is allowed.</p>
 */
public final class SystemClock implements Clock {

    /**
     * {@inheritDoc}
     * @return текущее время UTC / the current UTC time
     */
    @Override
    public OffsetDateTime nowUtc() {
        return OffsetDateTime.now(ZoneOffset.UTC);
    }
}