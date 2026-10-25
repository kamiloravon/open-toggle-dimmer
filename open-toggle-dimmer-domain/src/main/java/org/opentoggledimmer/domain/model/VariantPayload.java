package org.opentoggledimmer.domain.model;

import java.util.Optional;

/**
 * Value object: payload варианта.
 * Value object: a variant payload.
 *
 * <hr>
 *
 * <p>Необязательные данные варианта: тип ({@code json}, {@code csv},
 * {@code string}, {@code number}) и строковое значение.
 * Optional variant data: a type ({@code json}, {@code csv}, {@code string},
 * {@code number}) and a string value.</p>
 *
 * @param type  тип ({@code json}|{@code csv}|{@code string}|{@code number}) /
 *              the type ({@code json}|{@code csv}|{@code string}|{@code number})
 * @param value значение / the value
 */
public record VariantPayload(String type, String value) {

    /**
     * Компактный конструктор с валидацией.
     * Compact constructor with validation.
     *
     * @throws org.opentoggledimmer.domain.exception.ValidationException
     *         при пустых полях / on blank fields
     */
    public VariantPayload {
        if (type == null || type.isBlank()) {
            throw new org.opentoggledimmer.domain.exception.ValidationException(
                    "Variant payload type must not be blank / Тип payload варианта не может быть пустым");
        }
        if (value == null) {
            value = "";
        }
    }

    /**
     * Создаёт payload.
     * Creates a payload.
     *
     * @param type  тип / type
     * @param value значение / value
     * @return payload / the payload
     */
    public static VariantPayload of(String type, String value) {
        return new VariantPayload(type, value);
    }

    /**
     * Возвращает payload как {@link Optional} или пусто.
     * Returns the payload as {@link Optional} or empty.
     *
     * @param payload payload или {@code null} / the payload or {@code null}
     * @return {@link Optional} с payload / {@link Optional} with the payload
     */
    public static Optional<VariantPayload> optional(VariantPayload payload) {
        return Optional.ofNullable(payload);
    }
}