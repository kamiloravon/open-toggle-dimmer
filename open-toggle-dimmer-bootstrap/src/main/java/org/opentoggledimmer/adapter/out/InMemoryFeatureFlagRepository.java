package org.opentoggledimmer.adapter.out;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.opentoggledimmer.application.port.out.FeatureFlagRepository;
import org.opentoggledimmer.domain.model.FeatureFlag;
import org.opentoggledimmer.domain.model.FeatureName;

/**
 * In-memory адаптер репозитория флагов.
 * In-memory feature flag repository adapter.
 *
 * <hr>
 *
 * <p>Потокобезопасное хранилище флагов по имени. Подходит для тестов и
 * локального запуска; данные не переживают перезапуск.
 * A thread-safe store of flags by name. Suitable for tests and local runs; data
 * does not survive a restart.</p>
 */
public final class InMemoryFeatureFlagRepository implements FeatureFlagRepository {

    private final Map<String, FeatureFlag> flags = new ConcurrentHashMap<>();

    /**
     * {@inheritDoc}
     * @param name имя / name
     * @return флаг, если найден / the flag if found
     */
    @Override
    public Optional<FeatureFlag> findByName(FeatureName name) {
        return Optional.ofNullable(flags.get(name.value()));
    }

    /**
     * {@inheritDoc}
     * @return карта флагов / the flag map
     */
    @Override
    public Map<String, FeatureFlag> findAllByName() {
        return Map.copyOf(flags);
    }

    /**
     * {@inheritDoc}
     * @param flag флаг / the flag
     */
    @Override
    public void save(FeatureFlag flag) {
        flags.put(flag.name().value(), flag);
    }

    /**
     * {@inheritDoc}
     * @param name имя / name
     */
    @Override
    public void remove(FeatureName name) {
        flags.remove(name.value());
    }
}