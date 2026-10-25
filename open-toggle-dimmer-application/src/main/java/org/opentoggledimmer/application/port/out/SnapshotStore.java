package org.opentoggledimmer.application.port.out;

import java.util.Optional;
import org.opentoggledimmer.application.dto.FeatureSnapshot;

/**
 * Исходящий порт: хранилище последнего снапшота.
 * Outbound port: last snapshot store.
 *
 * <hr>
 *
 * <p>Обеспечивает отказоустойчивость: при недоступности {@link SnapshotLoader}
 * вычисление использует последний успешно сохранённый снапшот.
 * Provides resilience: when {@link SnapshotLoader} is unavailable, evaluation
 * uses the last successfully stored snapshot.</p>
 */
public interface SnapshotStore {

    /**
     * Сохраняет снапшот.
     * Stores a snapshot.
     *
     * @param snapshot снапшот / the snapshot
     */
    void store(FeatureSnapshot snapshot);

    /**
     * Возвращает последний сохранённый снапшот.
     * Returns the last stored snapshot.
     *
     * @return снапшот, если сохранён / the snapshot if stored
     */
    Optional<FeatureSnapshot> loadLast();
}