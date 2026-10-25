package org.opentoggledimmer.domain.service;

import java.util.Optional;
import org.opentoggledimmer.domain.model.MurmurHashValue;
import org.opentoggledimmer.domain.model.EvaluationContext;
import org.opentoggledimmer.domain.port.RandomSource;

/**
 * Доменный сервис: вычисление stickiness-хеша.
 * Domain service: stickiness hash calculation.
 *
 * <hr>
 *
 * <p>Инкапсулирует точную семантику: вход хеша — {@code groupId + ":" +
 * contextValue} в UTF-8; 32-битный MurmurHash3; нормализация
 * {@code (hash % modulus) + 1}. Дополнительно определяет, какое поле контекста
 * использовать в зависимости от {@code stickiness}.
 * Encapsulates the exact semantics: the hash input is
 * {@code groupId + ":" + contextValue} in UTF-8; 32-bit MurmurHash3;
 * normalization {@code (hash % modulus) + 1}. It also resolves which context
 * field to use depending on {@code stickiness}.</p>
 */
public final class StickinessCalculator {

    /** Seed для gradual rollout / seed for gradual rollout. */
    public static final int ROLLOUT_SEED = 0;

    /** Seed для вариантов (VARIANT_NORMALIZATION_SEED) / seed for variants. */
    public static final int VARIANT_SEED = 86028157;

    private final RandomSource randomSource;

    /**
     * Создаёт калькулятор.
     * Creates the calculator.
     *
     * @param randomSource источник случайности / randomness source
     */
    public StickinessCalculator(RandomSource randomSource) {
        this.randomSource = randomSource;
    }

    /**
     * Разрешает значение контекста по {@code stickiness}.
     * Resolves the context value for {@code stickiness}.
     *
     * <hr>
     *
     * <p>Правила: {@code default} — {@code userId}, иначе {@code sessionId};
     * {@code userId}/{@code sessionId}/{@code environment}/{@code appName}/
     * {@code remoteAddress} — соответствующее поле; {@code random} — пусто;
     * иное имя — поле {@code properties}.
     * Rules: {@code default} — {@code userId}, else {@code sessionId};
     * {@code userId}/{@code sessionId}/{@code environment}/{@code appName}/
     * {@code remoteAddress} — the corresponding field; {@code random} — empty;
     * any other name — a {@code properties} entry.</p>
     *
     * @param stickiness правило stickiness / the stickiness rule
     * @param context    контекст / the context
     * @return значение контекста, если найдено / the context value if found
     */
    public Optional<String> resolveSeed(String stickiness, EvaluationContext context) {
        if (stickiness == null || "default".equals(stickiness)) {
            return Optional.ofNullable(context.userId())
                    .or(() -> Optional.ofNullable(context.sessionId()));
        }
        return switch (stickiness) {
            case "random" -> Optional.empty();
            case "userId" -> Optional.ofNullable(context.userId());
            case "sessionId" -> Optional.ofNullable(context.sessionId());
            case "environment" -> Optional.ofNullable(context.environment());
            case "appName" -> Optional.ofNullable(context.appName());
            case "remoteAddress" -> Optional.ofNullable(context.remoteAddress());
            default -> Optional.ofNullable(context.properties().get(stickiness));
        };
    }

    /**
     * Вычисляет нормализованный хеш для rollout-стратегии (диапазон 1–100).
     * Computes the normalized hash for the rollout strategy (range 1–100).
     *
     * @param groupId  идентификатор группы / the group id
     * @param stickiness значение контекста / the context value
     * @return нормализованное значение 1–100 / the normalized value 1–100
     */
    public int normalizedRollout(String groupId, String stickiness) {
        return hash(groupId, stickiness).normalized(100);
    }

    /**
     * Вычисляет нормализованный хеш для вариантов (диапазон 1..totalWeight).
     * Computes the normalized hash for variants (range 1..totalWeight).
     *
     * @param groupId     идентификатор группы / the group id
     * @param stickiness  значение контекста / the context value
     * @param totalWeight сумма весов вариантов / the total variant weight
     * @return нормализованное значение 1..totalWeight /
     *         the normalized value 1..totalWeight
     */
    public int normalizedVariant(String groupId, String stickiness, int totalWeight) {
        return variantHash(groupId, stickiness, totalWeight).normalized(totalWeight);
    }

    /**
     * Вычисляет сырой rollout-хеш.
     * Computes the raw rollout hash.
     *
     * @param groupId    идентификатор группы / the group id
     * @param stickiness значение контекста / the context value
     * @return значение хеша / the hash value
     */
    public MurmurHashValue hash(String groupId, String stickiness) {
        return MurmurHashValue.ofUnsigned(MurmurHash3.hash32(hashInput(groupId, stickiness), ROLLOUT_SEED));
    }

    /**
     * Вычисляет сырой хеш для вариантов.
     * Computes the raw variant hash.
     *
     * @param groupId     идентификатор группы / the group id
     * @param stickiness  значение контекста / the context value
     * @param totalWeight сумма весов вариантов / the total variant weight
     * @return значение хеша / the hash value
     */
    public MurmurHashValue variantHash(String groupId, String stickiness, int totalWeight) {
        return MurmurHashValue.ofUnsigned(
                MurmurHash3.hash32(hashInput(groupId, stickiness), VARIANT_SEED));
    }

    /**
     * Формирует входную строку хеша {@code groupId + ":" + value}.
     * Builds the hash input string {@code groupId + ":" + value}.
     *
     * @param groupId идентификатор группы / the group id
     * @param value   значение контекста / the context value
     * @return строка хеша / the hash input
     */
    public static String hashInput(String groupId, String value) {
        return groupId + ":" + value;
    }

    /**
     * Возвращает случайное число 1–100 для не-sticky rollout.
     * Returns a random 1–100 number for non-sticky rollout.
     *
     * @return случайное 1–100 / a random 1–100
     */
    public int randomRollout() {
        return randomSource.nextIntInclusive(1, 100);
    }

    /**
     * Возвращает случайное число 1..totalWeight для выбора варианта.
     * Returns a random number 1..totalWeight for variant selection.
     *
     * @param totalWeight сумма весов / total weight
     * @return случайное 1..totalWeight / a random 1..totalWeight
     */
    public int randomVariant(int totalWeight) {
        return randomSource.nextIntInclusive(1, totalWeight);
    }
}