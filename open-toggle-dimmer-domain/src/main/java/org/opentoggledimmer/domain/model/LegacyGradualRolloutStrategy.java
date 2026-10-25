package org.opentoggledimmer.domain.model;

import java.util.List;

/**
 * Legacy gradual-стратегии ({@code gradualRolloutUserId},
 * {@code gradualRolloutSessionId}, {@code gradualRolloutRandom}).
 * Legacy gradual strategies ({@code gradualRolloutUserId},
 * {@code gradualRolloutSessionId}, {@code gradualRolloutRandom}).
 *
 * <hr>
 *
 * <p>Старое представление gradual rollout. {@code length} — процент.
 * Кодируются единым типом с явным {@code kind}.
 * The older gradual rollout representation. {@code length} is the percentage.
 * Encoded as a single type with an explicit {@code kind}.</p>
 *
 * @param kind        вид ({@code userId}|{@code sessionId}|{@code random}) /
 *                    the kind ({@code userId}|{@code sessionId}|{@code random})
 * @param percentage  процент / the percentage
 * @param groupId     идентификатор группы / the group id
 * @param constraints constraints / constraints
 * @param segmentIds  id сегментов / segment ids
 * @param disabled    отключена ли / whether disabled
 * @param sortOrder   порядок / order
 */
public record LegacyGradualRolloutStrategy(
        LegacyKind kind,
        RolloutPercentage percentage,
        GroupId groupId,
        List<Constraint> constraints,
        List<Long> segmentIds,
        boolean disabled,
        int sortOrder)
        implements ActivationStrategy {

    /**
     * Вид legacy gradual-стратегии.
     * Kind of legacy gradual strategy.
     */
    public enum LegacyKind {
        /** По {@code userId} / by {@code userId}. */
        USER_ID,
        /** По {@code sessionId} / by {@code sessionId}. */
        SESSION_ID,
        /** Случайно / random. */
        RANDOM
    }

    /**
     * Компактный конструктор с нормализацией.
     * Compact constructor with normalization.
     */
    public LegacyGradualRolloutStrategy {
        if (kind == null || percentage == null) {
            throw new org.opentoggledimmer.domain.exception.ValidationException(
                    "kind and percentage are required / kind и percentage обязательны");
        }
        groupId = groupId == null ? GroupId.of("default") : groupId;
        constraints = constraints == null ? List.of() : List.copyOf(constraints);
        segmentIds = segmentIds == null ? List.of() : List.copyOf(segmentIds);
    }

    /**
     * {@inheritDoc}
     * @return имя legacy-стратегии / the legacy strategy name
     */
    @Override
    public String type() {
        return switch (kind) {
            case USER_ID -> "gradualRolloutUserId";
            case SESSION_ID -> "gradualRolloutSessionId";
            case RANDOM -> "gradualRolloutRandom";
        };
    }
}