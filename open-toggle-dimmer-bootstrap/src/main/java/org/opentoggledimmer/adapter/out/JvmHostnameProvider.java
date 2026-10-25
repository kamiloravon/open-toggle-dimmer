package org.opentoggledimmer.adapter.out;

import java.net.InetAddress;
import java.net.UnknownHostException;
import org.opentoggledimmer.domain.port.HostnameProvider;

/**
 * Адаптер {@link HostnameProvider} на {@link InetAddress}.
 * {@link HostnameProvider} adapter backed by {@link InetAddress}.
 *
 * <hr>
 *
 * <p>Возвращает локальный хостнейм процесса. При ошибке возвращает пустую
 * строку (стратегия тогда не срабатывает).
 * Returns the local process hostname. On error, returns an empty string (the
 * strategy then does not match).</p>
 */
public final class JvmHostnameProvider implements HostnameProvider {

    /**
     * {@inheritDoc}
     * @return хостнейм / the hostname
     */
    @Override
    public String hostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "";
        }
    }
}