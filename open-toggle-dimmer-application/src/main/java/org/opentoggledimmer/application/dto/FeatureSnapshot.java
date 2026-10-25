package org.opentoggledimmer.application.dto;

import java.util.List;
import org.opentoggledimmer.domain.model.FeatureFlag;
import org.opentoggledimmer.domain.model.Segment;

/**
 * DTO: снапшот конфигурации флагов и сегментов.
 * DTO: a snapshot of flag and segment configuration.
 *
 * <hr>
 *
 * <p>Неизменяемый набор флагов и сегментов, полученный от внешнего источника
 * или из хранилища. Служит входом для синхронизации и вычисления.
 * An immutable set of flags and segments obtained from an external source or a
 * store. Serves as input for synchronization and evaluation.</p>
 *
 * @param version  версия формата снапшота / the snapshot format version
 * @param flags    флаги / the flags
 * @param segments сегменты / the segments
 */
public record FeatureSnapshot(int version, List<FeatureFlag> flags, List<Segment> segments) {

    /**
     * Компактный конструктор с нормализацией.
     * Compact constructor with normalization.
     */
    public FeatureSnapshot {
        flags = flags == null ? List.of() : List.copyOf(flags);
        segments = segments == null ? List.of() : List.copyOf(segments);
    }

    /**
     * Создаёт снапшот версии 2.
     * Creates a version 2 snapshot.
     *
     * @param flags    флаги / flags
     * @param segments сегменты / segments
     * @return снапшот / the snapshot
     */
    public static FeatureSnapshot of(List<FeatureFlag> flags, List<Segment> segments) {
        return new FeatureSnapshot(2, flags, segments);
    }
}