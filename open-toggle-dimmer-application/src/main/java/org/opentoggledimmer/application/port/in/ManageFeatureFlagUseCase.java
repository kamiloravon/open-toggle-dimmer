package org.opentoggledimmer.application.port.in;

import java.util.List;
import org.opentoggledimmer.domain.model.ActivationStrategy;
import org.opentoggledimmer.domain.model.FeatureDependency;
import org.opentoggledimmer.domain.model.FeatureFlag;
import org.opentoggledimmer.domain.model.FeatureName;
import org.opentoggledimmer.domain.model.ProjectId;
import org.opentoggledimmer.domain.model.Variant;

/**
 * Входящий порт: управление feature-флагами.
 * Inbound port: feature flag management.
 *
 * <hr>
 *
 * <p>Создание/изменение/архивирование флагов, добавление стратегий и
 * зависимостей. Реализация публикует доменные события после сохранения.
 * Creating/changing/archiving flags, adding strategies and dependencies. The
 * implementation publishes domain events after saving.</p>
 */
public interface ManageFeatureFlagUseCase {

    /**
     * Создаёт флаг.
     * Creates a flag.
     *
     * @param name      имя / name
     * @param projectId проект / project
     * @param enabled   состояние / state
     * @return созданный флаг / the created flag
     */
    FeatureFlag create(FeatureName name, ProjectId projectId, boolean enabled);

    /**
     * Заменяет конфигурацию флага.
     * Replaces the flag configuration.
     *
     * @param name       имя / name
     * @param enabled    состояние / state
     * @param strategies стратегии / strategies
     * @param deps       зависимости / dependencies
     * @param variants   варианты / variants
     * @return обновлённый флаг / the updated flag
     */
    FeatureFlag replace(
            FeatureName name,
            boolean enabled,
            List<ActivationStrategy> strategies,
            List<FeatureDependency> deps,
            List<Variant> variants);

    /**
     * Добавляет стратегию.
     * Adds a strategy.
     *
     * @param name     имя / name
     * @param strategy стратегия / the strategy
     * @return обновлённый флаг / the updated flag
     */
    FeatureFlag addStrategy(FeatureName name, ActivationStrategy strategy);

    /**
     * Архивирует флаг.
     * Archives the flag.
     *
     * @param name имя / name
     */
    void archive(FeatureName name);
}