package org.opentoggledimmer.adapter.out;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.opentoggledimmer.application.port.out.SegmentRepository;
import org.opentoggledimmer.domain.model.Segment;

/**
 * In-memory адаптер репозитория сегментов.
 * In-memory segment repository adapter.
 *
 * <hr>
 *
 * <p>Потокобезопасное хранилище сегментов по id.
 * A thread-safe store of segments by id.</p>
 */
public final class InMemorySegmentRepository implements SegmentRepository {

    private final Map<Long, Segment> segments = new ConcurrentHashMap<>();

    /**
     * {@inheritDoc}
     * @return карта сегментов / the segment map
     */
    @Override
    public Map<Long, Segment> findAllById() {
        return Map.copyOf(segments);
    }

    /**
     * {@inheritDoc}
     * @param segment сегмент / the segment
     */
    @Override
    public void save(Segment segment) {
        segments.put(segment.id(), segment);
    }
}