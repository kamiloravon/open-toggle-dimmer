package org.opentoggledimmer.domain.model;

import java.util.List;

/**
 * Стратегия gradual rollout ({@code flexibleRollout}).
 * Gradual rollout strategy ({@code flexibleRollout}).
 *
 * <hr>
 *
 * <p>Включает флаг для доли пользователей по детерминированному хешу
 * (MurmurHash3). Параметры: {@code rollout} (0–100), {@code stickiness},
 * {@code groupId} (по умолчанию — имя флага).
 * Enables the flag for a share of users via a deterministic hash (MurmurHash3).
 * Parameters: {@code rollout} (0–100), {@code stickiness}, {@code groupId}
 * (defaults to the flag name).</p>
 *
 * @param rollout     процент развёртывания / rollout percentage
 * @param stickiness  правило stickiness / the stickiness rule
 * @param groupId     идентификатор группы / the group id
 * @param constraints constraints / constraints
 * @param segmentIds  id сегментов / segment ids
 * @param disabled    отключена ли / whether disabled
 * @param sortOrder   порядок / order
 */
public record GradualRolloutStrategy(
        RolloutPercentage rollout,
        String stickiness,
        GroupId groupId,
        List<Constraint> constraints,
        List<Long> segmentIds,
        boolean disabled,
        int sortOrder)
        implements ActivationStrategy {

    /**
     * Компактный конструктор с нормализацией.
     * Compact constructor with normalization.
     *
     * @throws org.opentoggledimmer.domain.exception.ValidationException при null /
     *         on null values
     */
    public GradualRolloutStrategy {
        if (rollout == null || groupId == null) {
            throw new org.opentoggledimmer.domain.exception.ValidationException(
                    "rollout and groupId are required / rollout и groupId обязательны");
        }
        stickiness = stickiness == null || stickiness.isBlank() ? "default" : stickiness;
        constraints = constraints == null ? List.of() : List.copyOf(constraints);
        segmentIds = segmentIds == null ? List.of() : List.copyOf(segmentIds);
    }

    /**
     * {@inheritDoc}
     * @return {@code "flexibleRollout"} / {@code "flexibleRollout"}
     */
    @Override
    public String type() {
        return "flexibleRollout";
    }
}