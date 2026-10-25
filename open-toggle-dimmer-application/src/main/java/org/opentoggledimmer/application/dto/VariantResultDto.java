package org.opentoggledimmer.application.dto;

import org.opentoggledimmer.domain.model.VariantPayload;

/**
 * DTO: результат выбора варианта.
 * DTO: the variant selection result.
 *
 * <hr>
 *
 * @param name           имя варианта / the variant name
 * @param enabled        выбран ли вариант / whether the variant is selected
 * @param featureEnabled включён ли флаг / whether the flag is enabled
 * @param payload        payload / the payload
 */
public record VariantResultDto(String name, boolean enabled, boolean featureEnabled, PayloadDto payload) {

    /**
     * DTO: payload варианта.
     * DTO: the variant payload.
     *
     * @param type  тип / the type
     * @param value значение / the value
     */
    public record PayloadDto(String type, String value) { }

    /**
     * Создаёт fallback {@code disabled}.
     * Creates the {@code disabled} fallback.
     *
     * @param featureEnabled включён ли флаг / whether the flag is enabled
     * @return результат / the result
     */
    public static VariantResultDto disabled(boolean featureEnabled) {
        return new VariantResultDto("disabled", false, featureEnabled, null);
    }

    /**
     * Создаёт результат из доменного варианта.
     * Creates a result from a domain variant.
     *
     * @param name           имя / name
     * @param payload        payload / payload
     * @return результат / the result
     */
    public static VariantResultDto of(String name, VariantPayload payload) {
        PayloadDto dto = payload == null ? null : new PayloadDto(payload.type(), payload.value());
        return new VariantResultDto(name, true, true, dto);
    }
}