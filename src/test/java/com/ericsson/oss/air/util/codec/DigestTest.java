/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.air.util.codec;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

class DigestTest {

    @Test
    void getDigestAsHex() {

        final String[] input = { "csac", "plmnid", "snssai", "managedelement", "movalue" };

        final String expected = new Digest().getDigestAsHex(input);
        final String digest = new Digest().getDigestAsHex(input);

        assertEquals(40, digest.length());

        // ensure that the digest is reproducible. It is a hashing algorithm so it must be reproducible.
        assertEquals(expected, digest);

        assertEquals(digest, digest.toLowerCase());
    }

    @Test
    void getDigestAsHex_ListInput() {

        final String digest = new Digest().getDigestAsHex(List.of("csac", "plmnid", "snssai", "managedelement", "movalue"));

        assertEquals(40, digest.length());

        assertEquals(digest, digest.toLowerCase());
    }

    @Test
    void getDigestAsHex_MD5() {

        final String digest = Digest.withAlgorithm(Digest.Algorithm.MD5).getDigestAsHex("csac", "plmnid", "snssai", "managedelement", "movalue");

        assertEquals(32, digest.length());

        assertEquals(digest, digest.toLowerCase());
    }

    @Test
    void getDigestAsHex_MD5_ListInput() {

        final String digest = Digest.withAlgorithm(Digest.Algorithm.MD5)
                .getDigestAsHex(List.of("csac", "plmnid", "snssai", "managedelement", "movalue"));

        assertEquals(32, digest.length());

        assertEquals(digest, digest.toLowerCase());
    }

    @Test
    void getDigestAsHex_SHA256() {

        final String digest = Digest.withAlgorithm(Digest.Algorithm.SHA256)
                .getDigestAsHex("csac", "plmnid", "snssai", "managedelement", "movalue");

        assertEquals(64, digest.length());

        assertEquals(digest, digest.toLowerCase());
    }

    @Test
    void getDigestAsHex_SHA256_ListInput() {

        final String digest = Digest.withAlgorithm(Digest.Algorithm.SHA256)
                .getDigestAsHex(List.of("csac", "plmnid", "snssai", "managedelement", "movalue"));

        assertEquals(64, digest.length());

        assertEquals(digest, digest.toLowerCase());
    }

}