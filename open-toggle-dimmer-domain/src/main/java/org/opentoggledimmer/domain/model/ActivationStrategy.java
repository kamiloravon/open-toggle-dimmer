package org.opentoggledimmer.domain.model;

import java.util.List;

/**
 * Sealed-иерархия стратегий активации флага.
 * Sealed hierarchy of flag activation strategies.
 *
 * <hr>
 *
 * <p>Каждая стратегия оценивает контекст и возвращает {@code true}/{@code false}.
 * Стратегии объединяются через OR; у каждой есть общие атрибуты: {@code disabled}
 * и {@code sortOrder}. Constraints и сегменты проверяются до основной логики.
 * Each strategy evaluates the context and returns {@code true}/{@code false}.
 * Strategies are combined with OR; each has common attributes: {@code disabled}
 * and {@code sortOrder}. Constraints and segments are checked before the main
 * logic.</p>
 */
public sealed interface ActivationStrategy
        permits GradualRolloutStrategy,
                DefaultStrategy,
                UserIdsStrategy,
                RemoteAddressStrategy,
                ApplicationHostnameStrategy,
                LegacyGradualRolloutStrategy,
                CustomStrategy {

    /**
     * Возвращает порядок вычисления стратегии.
     * Returns the strategy evaluation order.
     *
     * @return порядок / the order
     */
    int sortOrder();

    /**
     * Сообщает, отключена ли стратегия.
     * Tells whether the strategy is disabled.
     *
     * @return {@code true}, если стратегия отключена / {@code true} if disabled
     */
    boolean disabled();

    /**
     * Возвращает набор constraints стратегии.
     * Returns the strategy constraints.
     *
     * @return constraints / the constraints
     */
    List<Constraint> constraints();

    /**
     * Возвращает идентификаторы сегментов стратегии.
     * Returns the strategy segment ids.
     *
     * @return id сегментов / the segment ids
     */
    List<Long> segmentIds();

    /**
     * Возвращает тип стратегии в виде строкового имени.
     * Returns the strategy type as a string name.
     *
     * @return имя типа / the type name
     */
    String type();
}