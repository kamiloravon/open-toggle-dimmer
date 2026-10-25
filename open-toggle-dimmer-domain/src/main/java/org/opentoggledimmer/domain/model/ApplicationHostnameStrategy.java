package org.opentoggledimmer.domain.model;

import java.util.List;

/**
 * Стратегия {@code applicationHostname}: включение по имени хоста.
 * {@code applicationHostname} strategy: enable by host name.
 *
 * <hr>
 *
 * <p>Сравнивает runtime-хостнейм процесса (через порт
 * {@link org.opentoggledimmer.domain.port.HostnameProvider}) со списком
 * {@code hostNames} без учёта регистра.
 * Compares the process runtime hostname (via the
 * {@link org.opentoggledimmer.domain.port.HostnameProvider} port) with the
 * {@code hostNames} list case-insensitively.</p>
 *
 * @param hostNames   список хостов / the host names
 * @param constraints constraints / constraints
 * @param segmentIds  id сегментов / segment ids
 * @param disabled    отключена ли / whether disabled
 * @param sortOrder   порядок / order
 */
public record ApplicationHostnameStrategy(
        List<String> hostNames, List<Constraint> constraints, List<Long> segmentIds, boolean disabled, int sortOrder)
        implements ActivationStrategy {

    /**
     * Компактный конструктор с нормализацией.
     * Compact constructor with normalization.
     */
    public ApplicationHostnameStrategy {
        hostNames = hostNames == null ? List.of() : List.copyOf(hostNames);
        constraints = constraints == null ? List.of() : List.copyOf(constraints);
        segmentIds = segmentIds == null ? List.of() : List.copyOf(segmentIds);
    }

    /**
     * {@inheritDoc}
     * @return {@code "applicationHostname"} / {@code "applicationHostname"}
     */
    @Override
    public String type() {
        return "applicationHostname";
    }
}