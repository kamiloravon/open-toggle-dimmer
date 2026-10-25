package org.opentoggledimmer.domain.model;

import java.util.List;
import java.util.Optional;

/**
 * Value object: вариант флага или стратегии.
 * Value object: a flag or strategy variant.
 *
 * <hr>
 *
 * <p>Состоит из имени, веса, типа веса, stickiness, payload и overrides.
 * Веса суммируются как есть; {@code weightType} движком не пересчитывается
 * (нормировка — на стороне конфигурации).
 * Consists of a name, weight, weight type, stickiness, payload and overrides.
 * Weights are summed as-is; {@code weightType} is not recalculated by the engine
 * (normalization happens on the configuration side).</p>
 *
 * @param name        имя / the name
 * @param weight      вес / the weight
 * @param weightType  тип веса / the weight type
 * @param stickiness  правило stickiness / the stickiness rule
 * @param payload     payload / the payload
 * @param overrides   overrides / the overrides
 */
public record Variant(
        VariantName name,
        VariantWeight weight,
        WeightType weightType,
        String stickiness,
        VariantPayload payload,
        List<VariantOverride> overrides) {

    /**
     * Тип веса варианта.
     * Variant weight type.
     */
    public enum WeightType {
        /** Переменный вес / variable weight. */
        VARIABLE,
        /** Фиксированный вес / fixed weight. */
        FIX
    }

    /**
     * Компактный конструктор с нормализацией.
     * Compact constructor with normalization.
     */
    public Variant {
        if (name == null || weight == null) {
            throw new org.opentoggledimmer.domain.exception.ValidationException(
                    "Variant name and weight are required / Имя и вес варианта обязательны");
        }
        weightType = weightType == null ? WeightType.VARIABLE : weightType;
        overrides = overrides == null ? List.of() : List.copyOf(overrides);
    }

    /**
     * Создаёт простой вариант.
     * Creates a simple variant.
     *
     * @param name   имя / name
     * @param weight вес / weight
     * @return вариант / the variant
     */
    public static Variant of(String name, int weight) {
        return new Variant(VariantName.of(name), VariantWeight.of(weight), WeightType.VARIABLE, null, null, List.of());
    }

    /**
     * Возвращает payload как {@link Optional}.
     * Returns the payload as {@link Optional}.
     *
     * @return payload, если задан / the payload if present
     */
    public Optional<VariantPayload> payloadOptional() {
        return Optional.ofNullable(payload);
    }
}