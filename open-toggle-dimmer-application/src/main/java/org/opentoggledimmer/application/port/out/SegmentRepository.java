package org.opentoggledimmer.application.port.out;

import java.util.Map;
import org.opentoggledimmer.domain.model.Segment;

/**
 * Исходящий порт: репозиторий сегментов.
 * Outbound port: segment repository.
 *
 * <hr>
 *
 * <p>Предоставляет сегменты, индексированные по идентификатору.
 * Provides segments indexed by identifier.</p>
 */
public interface SegmentRepository {

    /**
     * Возвращает сегменты по id.
     * Returns segments by id.
     *
     * @return карта id → сегмент / a map of id → segment
     */
    Map<Long, Segment> findAllById();

    /**
     * Сохраняет сегмент.
     * Saves a segment.
     *
     * @param segment сегмент / the segment
     */
    void save(Segment segment);
}