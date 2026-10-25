package org.opentoggledimmer.domain.service;

/**
 * Чистая реализация MurmurHash3 (x86_32).
 * Clean implementation of MurmurHash3 (x86_32).
 *
 * <hr>
 *
 * <p>Намеренно реализован вручную в домене, без внешних библиотек. Вход всегда
 * кодируется в UTF-8; результат возвращается как беззнаковое 32-битное число в
 * {@code long}. Используется для stickiness и выбора вариантов в точном
 * соответствии с эталонной реализацией движка.
 * Deliberately implemented by hand in the domain, without external libraries.
 * Input is always UTF-8 encoded; the result is returned as an unsigned 32-bit
 * number in a {@code long}. Used for stickiness and variant selection in exact
 * accordance with the reference engine implementation.</p>
 */
public final class MurmurHash3 {

    private static final int C1 = 0xcc9e2d51;
    private static final int C2 = 0x1b873593;

    private MurmurHash3() {
        throw new AssertionError("Utility class / Утилитарный класс");
    }

    /**
     * Вычисляет 32-битный MurmurHash3 для строки в UTF-8.
     * Computes the 32-bit MurmurHash3 of a UTF-8 string.
     *
     * @param data входная строка / the input string
     * @param seed seed хеша / the hash seed
     * @return беззнаковое 32-битное значение в {@code long} /
     *         the unsigned 32-bit value in a {@code long}
     */
    public static long hash32(String data, int seed) {
        return hash32(data.getBytes(java.nio.charset.StandardCharsets.UTF_8), seed);
    }

    /**
     * Вычисляет 32-битный MurmurHash3 для массива байт.
     * Computes the 32-bit MurmurHash3 of a byte array.
     *
     * @param data байты в little-endian порядке блоков / bytes, blocks little-endian
     * @param seed seed хеша / the hash seed
     * @return беззнаковое 32-битное значение в {@code long} /
     *         the unsigned 32-bit value in a {@code long}
     */
    public static long hash32(byte[] data, int seed) {
        int length = data.length;
        int h1 = seed;
        int numberOfBlocks = length / 4;
        for (int i = 0; i < numberOfBlocks; i++) {
            int k1 = (data[i * 4] & 0xFF)
                    | ((data[i * 4 + 1] & 0xFF) << 8)
                    | ((data[i * 4 + 2] & 0xFF) << 16)
                    | ((data[i * 4 + 3] & 0xFF) << 24);
            h1 = mixBlock(h1, k1);
        }
        int k1 = 0;
        int tailIndex = numberOfBlocks * 4;
        switch (length & 3) {
            case 3:
                k1 ^= (data[tailIndex + 2] & 0xFF) << 16;
                k1 ^= (data[tailIndex + 1] & 0xFF) << 8;
                k1 ^= data[tailIndex] & 0xFF;
                k1 = Integer.rotateLeft(k1 * C1, 15) * C2;
                h1 ^= k1;
                break;
            case 2:
                k1 ^= (data[tailIndex + 1] & 0xFF) << 8;
                k1 ^= data[tailIndex] & 0xFF;
                k1 = Integer.rotateLeft(k1 * C1, 15) * C2;
                h1 ^= k1;
                break;
            case 1:
                k1 ^= data[tailIndex] & 0xFF;
                k1 = Integer.rotateLeft(k1 * C1, 15) * C2;
                h1 ^= k1;
                break;
            default:
                break;
        }
        h1 ^= length;
        h1 ^= h1 >>> 16;
        h1 *= 0x85ebca6b;
        h1 ^= h1 >>> 13;
        h1 *= 0xc2b2ae35;
        h1 ^= h1 >>> 16;
        return Integer.toUnsignedLong(h1);
    }

    /**
     * Смешивает один 4-байтный блок в состояние.
     * Mixes one 4-byte block into the state.
     *
     * @param h1 текущее состояние / current state
     * @param k1 блок / the block
     * @return новое состояние / the new state
     */
    private static int mixBlock(int h1, int k1) {
        int k = k1 * C1;
        k = Integer.rotateLeft(k, 15);
        k *= C2;
        int h = h1 ^ k;
        h = Integer.rotateLeft(h, 13);
        return h * 5 + 0xe6546b64;
    }
}