package org.opentoggledimmer.application.port.out;

import java.util.UUID;

/**
 * Исходящий порт: генератор идентификаторов для событий.
 * Outbound port: identifier generator for events.
 *
 * <hr>
 *
 * <p>Абстрагирует {@code UUID.randomUUID()} из домена, обеспечивая
 * детерминизм в тестах.
 * Abstracts {@code UUID.randomUUID()} out of the domain, ensuring determinism
 * in tests.</p>
 */
@FunctionalInterface
public interface IdGenerator {

    /**
     * Генерирует уникальный идентификатор.
     * Generates a unique identifier.
     *
     * @return UUID / the UUID
     */
    UUID generate();
}