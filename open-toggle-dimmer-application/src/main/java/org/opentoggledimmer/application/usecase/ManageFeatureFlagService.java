package org.opentoggledimmer.application.usecase;

import java.util.List;
import org.opentoggledimmer.application.port.in.ManageFeatureFlagUseCase;
import org.opentoggledimmer.application.port.out.DomainEventPublisher;
import org.opentoggledimmer.application.port.out.FeatureFlagRepository;
import org.opentoggledimmer.domain.event.DomainEvent;
import org.opentoggledimmer.domain.model.ActivationStrategy;
import org.opentoggledimmer.domain.model.FeatureDependency;
import org.opentoggledimmer.domain.model.FeatureFlag;
import org.opentoggledimmer.domain.model.FeatureName;
import org.opentoggledimmer.domain.model.ProjectId;
import org.opentoggledimmer.domain.model.Variant;
import org.opentoggledimmer.domain.port.Clock;

/**
 * Реализация {@link ManageFeatureFlagUseCase}.
 * Implementation of {@link ManageFeatureFlagUseCase}.
 *
 * <hr>
 *
 * <p>Оркестрирует агрегат {@link FeatureFlag}: сохраняет новое состояние и
 * публикует доменные события. Бизнес-правила остаются в домене.
 * Orchestrates the {@link FeatureFlag} aggregate: saves the new state and
 * publishes domain events. Business rules stay in the domain.</p>
 */
public final class ManageFeatureFlagService implements ManageFeatureFlagUseCase {

    private final FeatureFlagRepository repository;
    private final DomainEventPublisher eventPublisher;
    private final Clock clock;

    /**
     * Создаёт сервис.
     * Creates the service.
     *
     * @param repository     репозиторий флагов / the flag repository
     * @param eventPublisher публикатор событий / the event publisher
     * @param clock          источник времени / the time source
     */
    public ManageFeatureFlagService(
            FeatureFlagRepository repository, DomainEventPublisher eventPublisher, Clock clock) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }

    /**
     * {@inheritDoc}
     * @param name      имя / name
     * @param projectId проект / project
     * @param enabled   состояние / state
     * @return флаг / the flag
     */
    @Override
    public FeatureFlag create(FeatureName name, ProjectId projectId, boolean enabled) {
        FeatureFlag.Created created = FeatureFlag.create(name, projectId, enabled, clock.nowUtc());
        repository.save(created.flag());
        eventPublisher.publish(created.event());
        return created.flag();
    }

    /**
     * {@inheritDoc}
     * @param name       имя / name
     * @param enabled    состояние / state
     * @param strategies стратегии / strategies
     * @param deps       зависимости / dependencies
     * @param variants   варианты / variants
     * @return флаг / the flag
     */
    @Override
    public FeatureFlag replace(
            FeatureName name,
            boolean enabled,
            List<ActivationStrategy> strategies,
            List<FeatureDependency> deps,
            List<Variant> variants) {
        FeatureFlag current = requireFlag(name);
        FeatureFlag.Changed changed =
                current.replaceConfiguration(enabled, strategies, deps, variants, clock.nowUtc());
        repository.save(changed.flag());
        eventPublisher.publish(changed.event());
        return changed.flag();
    }

    /**
     * {@inheritDoc}
     * @param name     имя / name
     * @param strategy стратегия / the strategy
     * @return флаг / the flag
     */
    @Override
    public FeatureFlag addStrategy(FeatureName name, ActivationStrategy strategy) {
        FeatureFlag current = requireFlag(name);
        FeatureFlag.Changed changed = current.addStrategy(strategy, clock.nowUtc());
        repository.save(changed.flag());
        eventPublisher.publish(changed.event());
        return changed.flag();
    }

    /**
     * {@inheritDoc}
     * @param name имя / name
     */
    @Override
    public void archive(FeatureName name) {
        FeatureFlag current = requireFlag(name);
        DomainEvent event = current.archive(clock.nowUtc());
        repository.remove(name);
        eventPublisher.publish(event);
    }

    /**
     * Находит флаг или бросает исключение.
     * Finds a flag or throws.
     *
     * @param name имя / name
     * @return флаг / the flag
     */
    private FeatureFlag requireFlag(FeatureName name) {
        return repository.findByName(name).orElseThrow(() -> new org.opentoggledimmer.domain.exception.DomainException(
                "Feature flag not found / Флаг не найден: " + name));
    }
}