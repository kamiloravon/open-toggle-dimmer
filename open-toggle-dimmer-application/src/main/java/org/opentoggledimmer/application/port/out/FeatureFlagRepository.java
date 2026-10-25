package org.opentoggledimmer.application.port.out;

import java.util.Map;
import java.util.Optional;
import org.opentoggledimmer.domain.model.FeatureFlag;
import org.opentoggledimmer.domain.model.FeatureName;

/**
 * Исходящий порт: репозиторий флагов.
 * Outbound port: feature flag repository.
 *
 * <hr>
 *
 * <p>Предоставляет агрегаты {@link FeatureFlag} для вычисления. Реализуется
 * адаптером (in-memory по умолчанию).
 * Provides {@link FeatureFlag} aggregates for evaluation. Implemented by an
 * adapter (in-memory by default).</p>
 */
public interface FeatureFlagRepository {

    /**
     * Находит флаг по имени.
     * Finds a flag by name.
     *
     * @param name имя флага / the flag name
     * @return флаг, если найден / the flag if found
     */
    Optional<FeatureFlag> findByName(FeatureName name);

    /**
     * Возвращает все флаги по имени (для разрешения зависимостей).
     * Returns all flags by name (for dependency resolution).
     *
     * @return карта имя → флаг / a map of name → flag
     */
    Map<String, FeatureFlag> findAllByName();

    /**
     * Сохраняет флаг (создаёт или заменяет).
     * Saves a flag (creates or replaces).
     *
     * @param flag флаг / the flag
     */
    void save(FeatureFlag flag);

    /**
     * Удаляет флаг по имени (архивирование).
     * Removes a flag by name (archival).
     *
     * @param name имя / the name
     */
    void remove(FeatureName name);
}