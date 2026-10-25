package org.opentoggledimmer.application.port.out;

import org.opentoggledimmer.application.dto.FeatureSnapshot;

/**
 * Исходящий порт: загрузчик снапшота конфигурации.
 * Outbound port: configuration snapshot loader.
 *
 * <hr>
 *
 * <p>Загружает снапшот конфигурации из внешнего источника, например
 * {@code /api/client/features}. При недоступности источника используется
 * последний сохранённый снапшот через {@link SnapshotStore}.
 * Loads a configuration snapshot from an external source, e.g.
 * {@code /api/client/features}. When the source is unavailable, the last
 * saved snapshot is used via {@link SnapshotStore}.</p>
 */
public interface SnapshotLoader {

    /**
     * Загружает снапшот.
     * Loads a snapshot.
     *
     * @return снапшот / the snapshot
     */
    FeatureSnapshot load();

    /**
     * Имя внешнего источника (для диагностики).
     * The external source name (for diagnostics).
     *
     * @return имя источника / the source name
     */
    String sourceName();
}