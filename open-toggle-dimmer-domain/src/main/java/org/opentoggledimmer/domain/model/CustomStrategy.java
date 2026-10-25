package org.opentoggledimmer.domain.model;

import java.util.List;

/**
 * Кастомная/неизвестная стратегия.
 * Custom/unknown strategy.
 *
 * <hr>
 *
 * <p>Раскрывается в {@code external_value["customStrategyN"]}; если реализация
 * не зарегистрирована, значение считается {@code false}. Такая стратегия не
 * выключает флаг целиком — она просто не срабатывает в OR-цепочке.
 * Expands to {@code external_value["customStrategyN"]}; if the implementation is
 * not registered, the value is {@code false}. Such a strategy does not disable
 * the whole flag — it simply does not match in the OR chain.</p>
 *
 * @param name        имя стратегии / the strategy name
 * @param constraints constraints / constraints
 * @param segmentIds  id сегментов / segment ids
 * @param disabled    отключена ли / whether disabled
 * @param sortOrder   порядок / order
 */
public record CustomStrategy(
        String name, List<Constraint> constraints, List<Long> segmentIds, boolean disabled, int sortOrder)
        implements ActivationStrategy {

    /**
     * Компактный конструктор с валидацией.
     * Compact constructor with validation.
     */
    public CustomStrategy {
        if (name == null || name.isBlank()) {
            throw new org.opentoggledimmer.domain.exception.ValidationException(
                    "Custom strategy name must not be blank / Имя кастомной стратегии не может быть пустым");
        }
        constraints = constraints == null ? List.of() : List.copyOf(constraints);
        segmentIds = segmentIds == null ? List.of() : List.copyOf(segmentIds);
    }

    /**
     * {@inheritDoc}
     * @return имя стратегии / the strategy name
     */
    @Override
    public String type() {
        return name;
    }
}