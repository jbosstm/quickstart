/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.narayana.quickstart.lra.jwt.util;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Mints RSA-signed JWTs and publishes the matching public key as a JWKS.
 *
 * <p>An RSA key pair is generated in memory when the class is first loaded, so no key
 * material is ever written to disk or committed to source control. This stands in for an
 * external identity provider (Keycloak, Auth0, Entra ID, ...) that would issue tokens in a
 * real deployment. The public half is exposed through {@link #publicJwks()} so that the
 * MicroProfile JWT runtime can verify the tokens this class produces.</p>
 */
public final class JwtKeyGenerator {

    /** Issuer claim placed in every minted token; must match {@code mp.jwt.verify.issuer}. */
    public static final String ISSUER = "https://demo.narayana.io";

    private static final long TOKEN_LIFETIME_MILLIS = 3600_000L;

    private static final RSAKey RSA_KEY = generateKey();

    private JwtKeyGenerator() {
    }

    private static RSAKey generateKey() {
        try {
            return new RSAKeyGenerator(2048)
                    .keyID(UUID.randomUUID().toString())
                    .keyUse(KeyUse.SIGNATURE)
                    .algorithm(JWSAlgorithm.RS256)
                    .generate();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to generate the demo RSA key pair", e);
        }
    }

    /**
     * Mints a signed JWT for the given subject and roles.
     *
     * @param subject the {@code sub}/{@code upn} claim value (the caller identity)
     * @param roles   the values placed in the {@code groups} claim (MicroProfile JWT roles)
     * @return the compact serialization of the signed JWT
     */
    public static String createToken(String subject, String... roles) {
        try {
            long now = System.currentTimeMillis();
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .issuer(ISSUER)
                    .subject(subject)
                    .claim("upn", subject)
                    .claim("groups", List.of(roles))
                    .jwtID(UUID.randomUUID().toString())
                    .issueTime(new Date(now))
                    .expirationTime(new Date(now + TOKEN_LIFETIME_MILLIS))
                    .build();

            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                    .keyID(RSA_KEY.getKeyID())
                    .type(JOSEObjectType.JWT)
                    .build();

            SignedJWT signedJWT = new SignedJWT(header, claims);
            signedJWT.sign(new RSASSASigner(RSA_KEY));

            return signedJWT.serialize();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to mint demo token for subject " + subject, e);
        }
    }

    /**
     * @return the public key as a JWKS document suitable for {@code mp.jwt.verify.publickey.location}
     */
    public static String publicJwks() {
        return new JWKSet(RSA_KEY.toPublicJWK()).toString();
    }

    /**
     * @return the public half of the demo key (used by the unit tests to verify signatures)
     */
    public static RSAKey publicKey() {
        return RSA_KEY.toPublicJWK();
    }
}
