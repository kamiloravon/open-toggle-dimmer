package org.opentoggledimmer.domain.model;

import java.util.List;

/**
 * Стратегия «включено для всех» ({@code default}/{@code standard}).
 * "Enabled for everyone" strategy ({@code default}/{@code standard}).
 *
 * <hr>
 *
 * <p>Разворачивается в правило {@code "true"}: эквивалент gradual rollout 100%.
 * Expands to the rule {@code "true"}: equivalent to a 100% gradual rollout.</p>
 *
 * @param typeName    имя типа ({@code default} или {@code standard}) /
 *                    type name ({@code default} or {@code standard})
 * @param constraints constraints / constraints
 * @param segmentIds  id сегментов / segment ids
 * @param disabled    отключена ли / whether disabled
 * @param sortOrder   порядок / order
 */
public record DefaultStrategy(
        String typeName,
        List<Constraint> constraints,
        List<Long> segmentIds,
        boolean disabled,
        int sortOrder)
        implements ActivationStrategy {

    /**
     * Компактный конструктор с нормализацией.
     * Compact constructor with normalization.
     */
    public DefaultStrategy {
        typeName = typeName == null || typeName.isBlank() ? "default" : typeName;
        constraints = constraints == null ? List.of() : List.copyOf(constraints);
        segmentIds = segmentIds == null ? List.of() : List.copyOf(segmentIds);
    }

    /**
     * {@inheritDoc}
     * @return имя типа / the type name
     */
    @Override
    public String type() {
        return typeName;
    }
}