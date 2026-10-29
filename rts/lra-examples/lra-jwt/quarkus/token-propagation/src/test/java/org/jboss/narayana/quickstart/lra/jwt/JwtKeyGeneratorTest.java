/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.narayana.quickstart.lra.jwt;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyType;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.jboss.narayana.quickstart.lra.jwt.util.JwtKeyGenerator;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies the demo token minting against the JWT (RFC 7519), JWS (RFC 7515) and
 * MicroProfile JWT expectations that the propagation example relies on.
 */
class JwtKeyGeneratorTest {

    @Test
    void mintedTokenCarriesTheMicroProfileJwtClaims() throws Exception {
        String token = JwtKeyGenerator.createToken("alice", "user", "customer");

        JWTClaimsSet claims = SignedJWT.parse(token).getJWTClaimsSet();

        assertEquals(JwtKeyGenerator.ISSUER, claims.getIssuer(), "iss must match the configured issuer");
        assertEquals("alice", claims.getSubject(), "sub must be the requested subject");
        assertEquals("alice", claims.getStringClaim("upn"), "upn is used by MicroProfile JWT as the principal name");
        assertEquals(List.of("user", "customer"), claims.getStringListClaim("groups"),
                "groups carries the MicroProfile JWT roles");
        assertTrue(claims.getExpirationTime().after(new Date()), "exp must be in the future");
    }

    @Test
    void mintedTokenVerifiesWithThePublishedPublicKeyOnly() throws Exception {
        SignedJWT token = SignedJWT.parse(JwtKeyGenerator.createToken("bob", "customer"));

        assertTrue(token.verify(new RSASSAVerifier(JwtKeyGenerator.publicKey())),
                "the token must verify against the key published at /auth/jwks");

        // A token signed by any other key must be rejected: the signature is bound to the demo key.
        RSAKey foreignKey = new RSAKeyGenerator(2048).generate();
        SignedJWT forged = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(foreignKey.getKeyID()).build(),
                new JWTClaimsSet.Builder().issuer(JwtKeyGenerator.ISSUER).subject("mallory").build());
        forged.sign(new RSASSASigner(foreignKey));

        assertFalse(forged.verify(new RSASSAVerifier(JwtKeyGenerator.publicKey())),
                "a token signed by a foreign key must not verify");
    }

    @Test
    void publishedJwksExposesTheSigningKeyWithoutPrivateMaterial() throws Exception {
        String expectedKid = SignedJWT.parse(JwtKeyGenerator.createToken("carol", "customer"))
                .getHeader().getKeyID();

        JWKSet jwks = JWKSet.parse(JwtKeyGenerator.publicJwks());

        assertEquals(1, jwks.getKeys().size(), "the JWKS must advertise exactly one key");
        JWK published = jwks.getKeys().get(0);
        assertEquals(expectedKid, published.getKeyID(), "the published kid must match the token header kid");
        assertEquals(KeyType.RSA, published.getKeyType(), "the demo uses an RSA signing key");
        assertFalse(published.isPrivate(), "the JWKS must never expose the private key");
    }
}
