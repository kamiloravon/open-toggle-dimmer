package org.opentoggledimmer.adapter.out;

import java.util.concurrent.ThreadLocalRandom;
import org.opentoggledimmer.domain.port.RandomSource;

/**
 * Адаптер {@link RandomSource} на {@link ThreadLocalRandom}.
 * {@link RandomSource} adapter backed by {@link ThreadLocalRandom}.
 *
 * <hr>
 *
 * <p>Возвращает случайное число в замкнутом диапазоне {@code [origin, bound]}.
 * Returns a random number in the closed range {@code [origin, bound]}.</p>
 */
public final class ThreadLocalRandomSource implements RandomSource {

    /**
     * {@inheritDoc}
     * @param origin нижняя граница / lower bound
     * @param bound  верхняя граница / upper bound
     * @return случайное число / a random number
     */
    @Override
    public int nextIntInclusive(int origin, int bound) {
        return ThreadLocalRandom.current().nextInt(origin, bound + 1);
    }
}