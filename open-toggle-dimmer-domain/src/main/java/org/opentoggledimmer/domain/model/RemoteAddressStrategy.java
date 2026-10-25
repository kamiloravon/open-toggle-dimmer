package org.opentoggledimmer.domain.model;

import java.util.List;

/**
 * Стратегия {@code remoteAddress}: включение для IP из CIDR-блоков.
 * {@code remoteAddress} strategy: enable for IPs within CIDR blocks.
 *
 * <hr>
 *
 * <p>Семантика — CIDR-вхождение, а не точное совпадение: {@code context.remoteAddress}
 * должен попадать в один из блоков {@code ips}. Одиночный IP без маски
 * трактуется как {@code /32} или {@code /128}.
 * The semantics are CIDR membership, not exact matching: {@code context.remoteAddress}
 * must fall into one of the {@code ips} blocks. A bare IP is treated as
 * {@code /32} or {@code /128}.</p>
 *
 * @param ips         CIDR-блоки / CIDR blocks
 * @param constraints constraints / constraints
 * @param segmentIds  id сегментов / segment ids
 * @param disabled    отключена ли / whether disabled
 * @param sortOrder   порядок / order
 */
public record RemoteAddressStrategy(
        List<String> ips, List<Constraint> constraints, List<Long> segmentIds, boolean disabled, int sortOrder)
        implements ActivationStrategy {

    /**
     * Компактный конструктор с нормализацией.
     * Compact constructor with normalization.
     */
    public RemoteAddressStrategy {
        ips = ips == null ? List.of() : List.copyOf(ips);
        constraints = constraints == null ? List.of() : List.copyOf(constraints);
        segmentIds = segmentIds == null ? List.of() : List.copyOf(segmentIds);
    }

    /**
     * {@inheritDoc}
     * @return {@code "remoteAddress"} / {@code "remoteAddress"}
     */
    @Override
    public String type() {
        return "remoteAddress";
    }
}