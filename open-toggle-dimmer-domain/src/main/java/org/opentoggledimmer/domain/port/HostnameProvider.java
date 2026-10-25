package org.opentoggledimmer.domain.port;

/**
 * Доменный порт: runtime-хостнейм процесса.
 * Domain port: the process runtime hostname.
 *
 * <hr>
 *
 * <p>Используется стратегией {@code applicationHostname}. Абстрагирует
 * {@code InetAddress} из домена.
 * Used by the {@code applicationHostname} strategy. Abstracts
 * {@code InetAddress} out of the domain.</p>
 */
@FunctionalInterface
public interface HostnameProvider {

    /**
     * Возвращает хостнейм текущего процесса.
     * Returns the hostname of the current process.
     *
     * @return хостнейм / the hostname
     */
    String hostname();
}