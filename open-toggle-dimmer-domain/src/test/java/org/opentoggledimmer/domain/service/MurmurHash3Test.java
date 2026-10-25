package org.opentoggledimmer.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Тесты {@link MurmurHash3}: эталонные векторы и граничные случаи.
 * Tests for {@link MurmurHash3}: reference vectors and edge cases.
 */
class MurmurHash3Test {

    @ParameterizedTest(name = "murmur3(\"{0}\") = {1}")
    @MethodSource("knownHashVectors")
    @DisplayName("совпадает с эталонными векторами MurmurHash3 x86_32")
    void shouldMatchKnownVectors(String input, long expected) {
        assertThat(MurmurHash3.hash32(input, 0)).isEqualTo(expected);
    }

    static Stream<Arguments> knownHashVectors() {
        return Stream.of(
                Arguments.of("", 0L),
                Arguments.of("hello", 0x248bfa47L),
                Arguments.of("hello, world", 0x149bbb7fL),
                Arguments.of("The quick brown fox jumps over the lazy dog", 0x2e4ff723L));
    }

    @ParameterizedTest(name = "golden rollout {0}: seed0={1}, normalized={2}")
    @CsvSource({
        "flag:123, 3070718447, 48",
        "flag:456, 4071369860, 61",
        "flag:999, 3427176227, 28",
        "my-feature:user-1, 3964040424, 25",
        "my-feature:user-2, 178477840, 41",
        "groupId:alice, 148755210, 11",
        "123:flag, 1955587778, 79"
    })
    @DisplayName("golden-векторы rollout (seed 0, normalized 1-100)")
    void shouldMatchGoldenRolloutVectors(String input, long expectedHash, int expectedNormalized) {
        long hash = MurmurHash3.hash32(input, StickinessCalculator.ROLLOUT_SEED);
        assertThat(hash).isEqualTo(expectedHash);
        assertThat((int) (hash % 100) + 1).isEqualTo(expectedNormalized);
    }

    @ParameterizedTest(name = "golden variant {0}: seedV={1}, normalized={2}")
    @CsvSource({
        "flag:123, 3824365194, 195",
        "flag:456, 1563515370, 371",
        "flag:999, 2395847910, 911",
        "my-feature:user-1, 1726386792, 793",
        "my-feature:user-2, 1854756887, 888",
        "groupId:alice, 1837635777, 778",
        "123:flag, 1169842245, 246"
    })
    @DisplayName("golden-векторы вариантов (seed 86028157, normalized 1-1000)")
    void shouldMatchGoldenVariantVectors(String input, long expectedHash, int expectedNormalized) {
        long hash = MurmurHash3.hash32(input, StickinessCalculator.VARIANT_SEED);
        assertThat(hash).isEqualTo(expectedHash);
        assertThat((int) (hash % 1000) + 1).isEqualTo(expectedNormalized);
    }

    @Test
    @DisplayName("порядок groupId:value важен")
    void shouldDifferWhenOrderChanges() {
        assertThat(MurmurHash3.hash32("flag:123", 0))
                .isNotEqualTo(MurmurHash3.hash32("123:flag", 0));
    }

    @Test
    @DisplayName("UTF-8: кириллица хешируется как UTF-8 байты")
    void shouldHashUtf8() {
        long fromString = MurmurHash3.hash32("флаг:пользователь", 0);
        long fromBytes = MurmurHash3.hash32("флаг:пользователь".getBytes(java.nio.charset.StandardCharsets.UTF_8), 0);
        assertThat(fromString).isEqualTo(fromBytes);
    }
}