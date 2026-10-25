package org.opentoggledimmer.domain.model;

import java.net.InetAddress;
import java.net.UnknownHostException;
import org.opentoggledimmer.domain.exception.ValidationException;

/**
 * Value object: CIDR-блок для проверки IP-адресов.
 * Value object: a CIDR block for IP address checks.
 *
 * <hr>
 *
 * <p>Поддерживает IPv4 и IPv6. Обозначение без маски трактуется как одиночный
 * адрес: {@code /32} для IPv4 и {@code /128} для IPv6 (поведение движка для
 * стратегии {@code remoteAddress} и оператора {@code IN_CIDR}).
 * Supports IPv4 and IPv6. A notation without a prefix is treated as a single
 * address: {@code /32} for IPv4 and {@code /128} for IPv6 (engine behavior for
 * the {@code remoteAddress} strategy and {@code IN_CIDR} operator).</p>
 *
 * @param notation строковое обозначение CIDR / the CIDR notation string
 */
public record CidrBlock(String notation) {

    /**
     * Компактный конструктор с валидацией и нормализацией.
     * Compact constructor with validation and normalization.
     *
     * @throws ValidationException если обозначение некорректно /
     *                             if the notation is invalid
     */
    public CidrBlock {
        if (notation == null || notation.isBlank()) {
            throw new ValidationException("CIDR block must not be blank / CIDR-блок не может быть пустым");
        }
        parseParts(notation.trim());
    }

    /**
     * Фабричный метод.
     * Factory method.
     *
     * @param notation обозначение CIDR / the CIDR notation
     * @return значение value object / the value object
     */
    public static CidrBlock of(String notation) {
        return new CidrBlock(notation);
    }

    /**
     * Разбирает обозначение на адрес и префикс.
     * Parses the notation into address and prefix.
     *
     * @param notation обозначение / the notation
     * @return пара {@code [address, prefix]} / pair {@code [address, prefix]}
     */
    private static Object[] parseParts(String notation) {
        int slash = notation.indexOf('/');
        String addressPart = slash < 0 ? notation : notation.substring(0, slash);
        int maxPrefix = addressPart.contains(":") ? 128 : 32;
        int prefix;
        if (slash < 0) {
            prefix = maxPrefix;
        } else {
            try {
                prefix = Integer.parseInt(notation.substring(slash + 1));
            } catch (NumberFormatException e) {
                throw new ValidationException("Invalid CIDR prefix / Некорректный префикс CIDR: " + notation);
            }
        }
        if (prefix < 0 || prefix > maxPrefix) {
            throw new ValidationException("CIDR prefix out of range / Префикс CIDR вне диапазона: " + notation);
        }
        InetAddress address = parseAddress(addressPart, notation);
        return new Object[] {address, prefix};
    }

    /**
     * Разбирает IP-адрес.
     * Parses an IP address.
     *
     * @param addressPart часть адреса / the address part
     * @param notation    исходное обозначение для сообщения об ошибке /
     *                    the original notation for error messages
     * @return {@link InetAddress} / the address
     */
    private static InetAddress parseAddress(String addressPart, String notation) {
        try {
            return InetAddress.getByName(addressPart);
        } catch (UnknownHostException e) {
            throw new ValidationException("Invalid IP in CIDR / Некорректный IP в CIDR: " + notation);
        }
    }

    /**
     * Проверяет, входит ли адрес в блок.
     * Checks whether an address is contained in the block.
     *
     * @param address строка IP-адреса / the IP address string
     * @return {@code true}, если адрес входит в блок; {@code false} при
     *         невалидном адресе / {@code true} if the address is in the block;
     *         {@code false} for an invalid address
     */
    public boolean contains(String address) {
        if (address == null || address.isBlank()) {
            return false;
        }
        InetAddress candidate;
        try {
            candidate = InetAddress.getByName(address.trim());
        } catch (UnknownHostException e) {
            return false;
        }
        Object[] parts = parseParts(notation.trim());
        InetAddress network = (InetAddress) parts[0];
        int prefix = (Integer) parts[1];
        byte[] candidateBytes = candidate.getAddress();
        byte[] networkBytes = network.getAddress();
        if (candidateBytes.length != networkBytes.length) {
            return false;
        }
        int fullBytes = prefix / 8;
        int remainderBits = prefix % 8;
        for (int i = 0; i < fullBytes; i++) {
            if (candidateBytes[i] != networkBytes[i]) {
                return false;
            }
        }
        if (remainderBits == 0) {
            return true;
        }
        int mask = 0xFF << (8 - remainderBits);
        return (candidateBytes[fullBytes] & mask) == (networkBytes[fullBytes] & mask);
    }
}