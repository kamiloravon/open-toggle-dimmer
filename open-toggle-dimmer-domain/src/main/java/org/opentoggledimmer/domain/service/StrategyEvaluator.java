package org.opentoggledimmer.domain.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.opentoggledimmer.domain.model.ActivationStrategy;
import org.opentoggledimmer.domain.model.ApplicationHostnameStrategy;
import org.opentoggledimmer.domain.model.Constraint;
import org.opentoggledimmer.domain.model.CustomStrategy;
import org.opentoggledimmer.domain.model.DefaultStrategy;
import org.opentoggledimmer.domain.model.GradualRolloutStrategy;
import org.opentoggledimmer.domain.model.GroupId;
import org.opentoggledimmer.domain.model.LegacyGradualRolloutStrategy;
import org.opentoggledimmer.domain.model.RemoteAddressStrategy;
import org.opentoggledimmer.domain.model.Segment;
import org.opentoggledimmer.domain.model.EvaluationContext;
import org.opentoggledimmer.domain.model.UserIdsStrategy;
import org.opentoggledimmer.domain.port.HostnameProvider;

/**
 * Доменный сервис: вычисление одной стратегии активации.
 * Domain service: evaluation of a single activation strategy.
 *
 * <hr>
 *
 * <p>Порядок: сначала проверяются segments (AND) и constraints (AND); при
 * успехе вычисляется основная логика стратегии. Отключённая стратегия даёт
 * {@code false}. Кастомная стратегия всегда {@code false}.
 * Order: segments (AND) and constraints (AND) are checked first; on success the
 * strategy's main logic is evaluated. A disabled strategy yields {@code false}.
 * A custom strategy is always {@code false}.</p>
 */
public final class StrategyEvaluator {

    private final ConstraintEvaluator constraintEvaluator;
    private final StickinessCalculator stickinessCalculator;
    private final HostnameProvider hostnameProvider;
    private final Map<Long, Segment> segments;

    /**
     * Создаёт вычислитель стратегий.
     * Creates the strategy evaluator.
     *
     * @param constraintEvaluator вычислитель constraints / the constraint evaluator
     * @param stickinessCalculator вычислитель stickiness / the stickiness calculator
     * @param hostnameProvider    источник хостнейма / the hostname provider
     * @param segments            сегменты по id / segments by id
     */
    public StrategyEvaluator(
            ConstraintEvaluator constraintEvaluator,
            StickinessCalculator stickinessCalculator,
            HostnameProvider hostnameProvider,
            Map<Long, Segment> segments) {
        this.constraintEvaluator = constraintEvaluator;
        this.stickinessCalculator = stickinessCalculator;
        this.hostnameProvider = hostnameProvider;
        this.segments = Map.copyOf(segments);
    }

    /**
     * Вычисляет стратегию для контекста.
     * Evaluates the strategy for a context.
     *
     * @param strategy   стратегия / the strategy
     * @param featureName имя флага (для дефолтного groupId) / the flag name
     * @param context    контекст / the context
     * @return {@code true}, если стратегия активирует флаг /
     *         {@code true} if the strategy activates the flag
     */
    public boolean evaluate(ActivationStrategy strategy, String featureName, EvaluationContext context) {
        if (strategy.disabled()) {
            return false;
        }
        if (!segmentsMatch(strategy.segmentIds(), context)) {
            return false;
        }
        if (!constraintsMatch(strategy.constraints(), context)) {
            return false;
        }
        GroupId groupId = resolveGroupId(strategy, featureName);
        return switch (strategy) {
            case GradualRolloutStrategy gradual -> evaluateGradual(gradual, groupId, context);
            case DefaultStrategy ignored -> true;
            case UserIdsStrategy userIds -> evaluateUserIds(userIds, context);
            case RemoteAddressStrategy remote -> evaluateRemote(remote, context);
            case ApplicationHostnameStrategy hostname -> evaluateHostname(hostname, context);
            case LegacyGradualRolloutStrategy legacy -> evaluateLegacy(legacy, groupId, context);
            case CustomStrategy ignored -> false;
        };
    }

    /**
     * Проверяет все сегменты стратегии (AND, пустой сегмент → true).
     * Checks all strategy segments (AND, empty segment → true).
     *
     * @param segmentIds id сегментов / the segment ids
     * @param context    контекст / the context
     * @return {@code true}, если все сегменты выполнены / {@code true} if all match
     */
    private boolean segmentsMatch(List<Long> segmentIds, EvaluationContext context) {
        for (Long segmentId : segmentIds) {
            Segment segment = segments.get(segmentId);
            if (segment == null) {
                return false;
            }
            for (Constraint constraint : segment.constraints()) {
                if (!constraintEvaluator.evaluate(constraint, context)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Проверяет все constraints стратегии (AND).
     * Checks all strategy constraints (AND).
     *
     * @param constraints constraints / the constraints
     * @param context     контекст / the context
     * @return {@code true}, если все constraints выполнены / {@code true} if all match
     */
    private boolean constraintsMatch(List<Constraint> constraints, EvaluationContext context) {
        for (Constraint constraint : constraints) {
            if (!constraintEvaluator.evaluate(constraint, context)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Определяет groupId стратегии (по умолчанию — имя флага).
     * Resolves the strategy groupId (defaults to the flag name).
     *
     * @param strategy    стратегия / the strategy
     * @param featureName имя флага / the flag name
     * @return группа / the group
     */
    private GroupId resolveGroupId(ActivationStrategy strategy, String featureName) {
        return switch (strategy) {
            case GradualRolloutStrategy gradual -> gradual.groupId();
            case LegacyGradualRolloutStrategy legacy -> legacy.groupId();
            default -> GroupId.of(featureName);
        };
    }

    /**
     * Вычисляет gradual rollout.
     * Evaluates a gradual rollout.
     *
     * @param gradual  стратегия / the strategy
     * @param groupId  группа / the group
     * @param context  контекст / the context
     * @return результат / the result
     */
    private boolean evaluateGradual(GradualRolloutStrategy gradual, GroupId groupId, EvaluationContext context) {
        int normalized;
        if ("random".equals(gradual.stickiness())) {
            normalized = stickinessCalculator.randomRollout();
        } else {
            Optional<String> seed = stickinessCalculator.resolveSeed(gradual.stickiness(), context);
            if (seed.isEmpty()) {
                return false;
            }
            normalized = stickinessCalculator.normalizedRollout(groupId.value(), seed.get());
        }
        return gradual.rollout().includes(normalized);
    }

    /**
     * Вычисляет {@code userWithId}.
     * Evaluates {@code userWithId}.
     *
     * @param userIds стратегия / the strategy
     * @param context контекст / the context
     * @return результат / the result
     */
    private boolean evaluateUserIds(UserIdsStrategy userIds, EvaluationContext context) {
        String userId = context.userId();
        return userId != null && userIds.userIds().contains(userId);
    }

    /**
     * Вычисляет {@code remoteAddress} через CIDR.
     * Evaluates {@code remoteAddress} via CIDR.
     *
     * @param remote  стратегия / the strategy
     * @param context контекст / the context
     * @return результат / the result
     */
    private boolean evaluateRemote(RemoteAddressStrategy remote, EvaluationContext context) {
        if (context.remoteAddress() == null || remote.ips().isEmpty()) {
            return false;
        }
        Constraint cidr = new Constraint(
                "remoteAddress",
                org.opentoggledimmer.domain.model.ConstraintOperator.IN_CIDR,
                remote.ips(),
                org.opentoggledimmer.domain.model.ConstraintValueType.STRING,
                false,
                false);
        return constraintEvaluator.evaluate(cidr, context);
    }

    /**
     * Вычисляет {@code applicationHostname} (case-insensitive).
     * Evaluates {@code applicationHostname} (case-insensitive).
     *
     * @param hostname стратегия / the strategy
     * @param context  контекст / the context
     * @return результат / the result
     */
    private boolean evaluateHostname(ApplicationHostnameStrategy hostname, EvaluationContext context) {
        String actual = hostnameProvider.hostname();
        if (actual == null) {
            return false;
        }
        String lower = actual.toLowerCase();
        return hostname.hostNames().stream()
                .map(String::toLowerCase)
                .anyMatch(lower::equals);
    }

    /**
     * Вычисляет legacy gradual-стратегию.
     * Evaluates a legacy gradual strategy.
     *
     * @param legacy  стратегия / the strategy
     * @param groupId группа / the group
     * @param context контекст / the context
     * @return результат / the result
     */
    private boolean evaluateLegacy(
            LegacyGradualRolloutStrategy legacy, GroupId groupId, EvaluationContext context) {
        int normalized = switch (legacy.kind()) {
            case RANDOM -> stickinessCalculator.randomRollout();
            case USER_ID -> {
                if (context.userId() == null) {
                    yield -1;
                }
                yield stickinessCalculator.normalizedRollout(groupId.value(), context.userId());
            }
            case SESSION_ID -> {
                if (context.sessionId() == null) {
                    yield -1;
                }
                yield stickinessCalculator.normalizedRollout(groupId.value(), context.sessionId());
            }
        };
        return normalized > 0 && legacy.percentage().includes(normalized);
    }
}