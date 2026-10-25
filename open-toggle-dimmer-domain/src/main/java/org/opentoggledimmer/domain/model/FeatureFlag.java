package org.opentoggledimmer.domain.model;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.opentoggledimmer.domain.event.DomainEvent;
import org.opentoggledimmer.domain.event.FeatureFlagArchived;
import org.opentoggledimmer.domain.event.FeatureFlagConfigChanged;
import org.opentoggledimmer.domain.event.FeatureFlagCreated;
import org.opentoggledimmer.domain.event.StrategyAdded;
import org.opentoggledimmer.domain.exception.ValidationException;

/**
 * Aggregate root: feature-флаг в конкретном окружении.
 * Aggregate root: a feature flag within a specific environment.
 *
 * <hr>
 *
 * <p>Инварианты: валидное имя; не более одной зависимости от родителя; нет
 * дубликатов {@code groupId} среди стратегий; веса вариантов неотрицательны.
 * Главное правило вычисления: {@code enabled AND все проверки родителя AND
 * (любая стратегия = true)}; пустой список стратегий эквивалентен {@code true}.
 * Invariants: a valid name; at most one parent dependency; no duplicate strategy
 * ids; non-negative variant weights. The main evaluation rule is
 * {@code enabled AND parent checks AND (any strategy = true)}; an empty strategy
 * list is equivalent to {@code true}.</p>
 *
 * <p>Агрегат собирает доменные события, которые application-слой публикует
 * после сохранения. Aggregate collects domain events that the application layer
 * publishes after persisting.</p>
 *
 * @param name         имя флага / the flag name
 * @param projectId    проект / the project
 * @param enabled      мастер-переключатель / the master switch
 * @param strategies   стратегии / the strategies
 * @param dependencies зависимости / the dependencies
 * @param variants     варианты флага / the flag variants
 */
public record FeatureFlag(
        FeatureName name,
        ProjectId projectId,
        boolean enabled,
        List<ActivationStrategy> strategies,
        List<FeatureDependency> dependencies,
        List<Variant> variants) {

    /**
     * Компактный конструктор с проверкой инвариантов.
     * Compact constructor checking invariants.
     *
     * @throws ValidationException при нарушении инвариантов / on invariant violation
     */
    public FeatureFlag {
        Objects.requireNonNull(name, "name");
        projectId = projectId == null ? ProjectId.defaultProject() : projectId;
        strategies = strategies == null ? List.of() : List.copyOf(strategies);
        dependencies = dependencies == null ? List.of() : List.copyOf(dependencies);
        variants = variants == null ? List.of() : List.copyOf(variants);

        if (dependencies.size() > 1) {
            throw new ValidationException(
                    "A flag may have at most one parent / У флага может быть не более одного родителя: " + name);
        }
        for (FeatureDependency dependency : dependencies) {
            if (dependency.feature().equals(name.value())) {
                throw new ValidationException(
                        "A flag cannot depend on itself / Флаг не может зависеть от себя: " + name);
            }
        }
        Set<String> seenGroupIds = new HashSet<>();
        for (ActivationStrategy strategy : strategies) {
            if (strategy instanceof GradualRolloutStrategy gradual) {
                String group = gradual.groupId().value();
                if (!seenGroupIds.add(group)) {
                    throw new ValidationException(
                            "Duplicate groupId among strategies / Дублирующийся groupId среди стратегий: " + group);
                }
            }
        }
    }

    /**
     * Создаёт флаг и возвращает событие создания.
     * Creates a flag and returns the creation event.
     *
     * @param name      имя / name
     * @param projectId проект / project
     * @param enabled   состояние / state
     * @param now       момент / instant
     * @return пара «флаг + событие» / pair "flag + event"
     */
    public static Created create(FeatureName name, ProjectId projectId, boolean enabled, OffsetDateTime now) {
        FeatureFlag flag = new FeatureFlag(name, projectId, enabled, List.of(), List.of(), List.of());
        return new Created(flag, new FeatureFlagCreated(name.value(), enabled, now));
    }

    /**
     * Заменяет конфигурацию и выпускает событие изменения.
     * Replaces the configuration and emits a change event.
     *
     * @param newEnabled    новое состояние / new state
     * @param newStrategies стратегии / strategies
     * @param newDeps       зависимости / dependencies
     * @param newVariants   варианты / variants
     * @param now           момент / instant
     * @return пара «флаг + событие» / pair "flag + event"
     */
    public Changed replaceConfiguration(
            boolean newEnabled,
            List<ActivationStrategy> newStrategies,
            List<FeatureDependency> newDeps,
            List<Variant> newVariants,
            OffsetDateTime now) {
        FeatureFlag updated = new FeatureFlag(name, projectId, newEnabled, newStrategies, newDeps, newVariants);
        return new Changed(updated, new FeatureFlagConfigChanged(name.value(), newEnabled, now));
    }

    /**
     * Добавляет стратегию и выпускает событие.
     * Adds a strategy and emits an event.
     *
     * @param strategy стратегия / the strategy
     * @param now      момент / instant
     * @return пара «флаг + событие» / pair "flag + event"
     */
    public Changed addStrategy(ActivationStrategy strategy, OffsetDateTime now) {
        List<ActivationStrategy> updated = new ArrayList<>(strategies);
        updated.add(strategy);
        FeatureFlag flag = new FeatureFlag(name, projectId, enabled, updated, dependencies, variants);
        return new Changed(flag, new StrategyAdded(name.value(), strategy.type(), now));
    }

    /**
     * Архивирует флаг и выпускает событие.
     * Archives the flag and emits an event.
     *
     * @param now момент / instant
     * @return событие / the event
     */
    public DomainEvent archive(OffsetDateTime now) {
        return new FeatureFlagArchived(name.value(), now);
    }

    /**
     * Результат изменения: новое состояние флага и событие.
     * Result of a change: the new flag state and an event.
     *
     * @param flag  новый флаг / the new flag
     * @param event событие / the event
     */
    public record Changed(FeatureFlag flag, DomainEvent event) { }

    /**
     * Результат создания флага.
     * Result of flag creation.
     *
     * @param flag  новый флаг / the new flag
     * @param event событие / the event
     */
    public record Created(FeatureFlag flag, DomainEvent event) { }
}