package org.opentoggledimmer.adapter.out;

import java.util.Optional;
import org.opentoggledimmer.application.dto.FeatureSnapshot;
import org.opentoggledimmer.application.port.out.SnapshotStore;

/**
 * In-memory адаптер хранилища снапшотов.
 * In-memory snapshot store adapter.
 *
 * <hr>
 *
 * <p>Хранит последний снапшот в памяти для отказоустойчивости.
 * Keeps the last snapshot in memory for resilience.</p>
 */
public final class InMemorySnapshotStore implements SnapshotStore {

    private volatile FeatureSnapshot last;

    /**
     * {@inheritDoc}
     * @param snapshot снапшот / the snapshot
     */
    @Override
    public void store(FeatureSnapshot snapshot) {
        this.last = snapshot;
    }

    /**
     * {@inheritDoc}
     * @return последний снапшот / the last snapshot
     */
    @Override
    public Optional<FeatureSnapshot> loadLast() {
        return Optional.ofNullable(last);
    }
}