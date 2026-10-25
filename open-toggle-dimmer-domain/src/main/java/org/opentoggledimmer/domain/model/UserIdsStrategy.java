package org.opentoggledimmer.domain.model;

import java.util.List;

/**
 * Legacy-стратегия {@code userWithId}: включение для списка пользователей.
 * Legacy {@code userWithId} strategy: enable for a list of user ids.
 *
 * <hr>
 *
 * <p>Сравнивает {@code context.userId} со списком точно и с учётом регистра.
 * Compare {@code context.userId} with the list exactly and case-sensitively.</p>
 *
 * @param userIds     список идентификаторов / list of user ids
 * @param constraints constraints / constraints
 * @param segmentIds  id сегментов / segment ids
 * @param disabled    отключена ли / whether disabled
 * @param sortOrder   порядок / order
 */
public record UserIdsStrategy(
        List<String> userIds, List<Constraint> constraints, List<Long> segmentIds, boolean disabled, int sortOrder)
        implements ActivationStrategy {

    /**
     * Компактный конструктор с нормализацией.
     * Compact constructor with normalization.
     */
    public UserIdsStrategy {
        userIds = userIds == null ? List.of() : List.copyOf(userIds);
        constraints = constraints == null ? List.of() : List.copyOf(constraints);
        segmentIds = segmentIds == null ? List.of() : List.copyOf(segmentIds);
    }

    /**
     * {@inheritDoc}
     * @return {@code "userWithId"} / {@code "userWithId"}
     */
    @Override
    public String type() {
        return "userWithId";
    }
}