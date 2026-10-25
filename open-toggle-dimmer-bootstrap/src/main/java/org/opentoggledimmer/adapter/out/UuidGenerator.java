package org.opentoggledimmer.adapter.out;

import java.util.UUID;
import org.opentoggledimmer.application.port.out.IdGenerator;

/**
 * Адаптер {@link IdGenerator} на {@link UUID#randomUUID()}.
 * {@link IdGenerator} adapter backed by {@link UUID#randomUUID()}.
 *
 * <hr>
 *
 * <p>Единственное место в проекте, где допустим {@code UUID.randomUUID()}.
 * The only place in the project where {@code UUID.randomUUID()} is allowed.</p>
 */
public final class UuidGenerator implements IdGenerator {

    /**
     * {@inheritDoc}
     * @return случайный UUID / a random UUID
     */
    @Override
    public UUID generate() {
        return UUID.randomUUID();
    }
}