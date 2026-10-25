package org.opentoggledimmer.application.usecase;

import org.opentoggledimmer.application.dto.FeatureSnapshot;
import org.opentoggledimmer.application.port.in.SyncFeatureConfigurationUseCase;
import org.opentoggledimmer.application.port.out.SnapshotLoader;
import org.opentoggledimmer.application.port.out.SnapshotStore;

/**
 * Реализация {@link SyncFeatureConfigurationUseCase}.
 * Implementation of {@link SyncFeatureConfigurationUseCase}.
 *
 * <hr>
 *
 * <p>Пытается загрузить снапшот из {@link SnapshotLoader}; при успехе сохраняет
 * его в {@link SnapshotStore} и возвращает. При ошибке возвращает последний
 * сохранённый снапшот; если его нет — бросает доменное исключение.
 * Tries to load a snapshot from {@link SnapshotLoader}; on success stores it in
 * {@link SnapshotStore} and returns it. On error, returns the last stored
 * snapshot; if none exists, throws a domain exception.</p>
 */
public final class SyncFeatureConfigurationService implements SyncFeatureConfigurationUseCase {

    private final SnapshotLoader loader;
    private final SnapshotStore store;

    /**
     * Создаёт сервис.
     * Creates the service.
     *
     * @param loader загрузчик / the loader
     * @param store  хранилище / the store
     */
    public SyncFeatureConfigurationService(SnapshotLoader loader, SnapshotStore store) {
        this.loader = loader;
        this.store = store;
    }

    /**
     * {@inheritDoc}
     * @return снапшот / the snapshot
     */
    @Override
    public FeatureSnapshot sync() {
        try {
            FeatureSnapshot snapshot = loader.load();
            store.store(snapshot);
            return snapshot;
        } catch (RuntimeException loadError) {
            return store.loadLast().orElseThrow(() -> new org.opentoggledimmer.domain.exception.DomainException(
                    "No snapshot available and the loader failed / Снапшот недоступен, загрузчик завершился ошибкой: "
                            + loader.sourceName(),
                    loadError));
        }
    }
}