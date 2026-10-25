package org.opentoggledimmer.application.port.in;

import org.opentoggledimmer.application.dto.FeatureSnapshot;

/**
 * Входящий порт: синхронизация конфигурации.
 * Inbound port: configuration synchronization.
 *
 * <hr>
 *
 * <p>Загружает снапшот через {@code SnapshotLoader} и сохраняет последний
 * успешный снапшот в {@code SnapshotStore}. При ошибке загрузки возвращает
 * последний сохранённый снапшот.
 * Loads a snapshot via {@code SnapshotLoader} and stores the last successful
 * snapshot in {@code SnapshotStore}. On a load error, returns the last stored
 * snapshot.</p>
 */
public interface SyncFeatureConfigurationUseCase {

    /**
     * Синхронизирует конфигурацию.
     * Synchronizes the configuration.
     *
     * @return актуальный снапшот / the current snapshot
     */
    FeatureSnapshot sync();
}