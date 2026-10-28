/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.narayana.quickstart.lra.jwt;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * JAX-RS activator for the JWT + LRA token-propagation example.
 */
@ApplicationPath("/")
public class JwtLraApplication extends Application {
}
