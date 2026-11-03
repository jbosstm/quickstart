# Propagating a caller's JWT through an LRA saga

This quickstart shows how the caller's **JSON Web Token (JWT)** is carried through a
[MicroProfile LRA](https://github.com/eclipse/microprofile-lra) saga using Narayana's
`@PropagateToken` annotation, so that every service taking part in the distributed
transaction sees the same authenticated identity.

It is a self-contained Quarkus application (like the sibling
[`cdi-participant`](../../../cdi-participant/README.md) example) that bundles the Narayana LRA
implementation, so both `@PropagateToken` and `@Inject JsonWebToken` work out of the box.

> This example is about **propagating the caller identity** between services in a saga.
> The sibling [`wildfly`](../../wildfly/README.md) example covers the complementary concern of
> **securing the coordinator's own REST endpoints** with JWT roles.

## The scenario

A small e-commerce order flow, orchestrated as a saga:

```
                       ┌──────────────────┐  reserve / release
Client ──Bearer JWT──▶ │  OrderService    │ ─────────────────▶ InventoryService
                       │  @LRA(REQUIRED)  │  process / refund
                       │  @PropagateToken │ ─────────────────▶ PaymentService
                       └──────────────────┘
```

* `OrderService` starts an LRA and calls the two participants, forwarding the caller's token.
* `InventoryService` and `PaymentService` join the LRA (`@LRA(MANDATORY)`) and expose
  `@Complete`/`@Compensate` callbacks.
* If payment is declined the order fails, the LRA is cancelled and the inventory reservation
  that already joined is compensated (released).

All three services and a stand-in identity provider live in the **same** deployment so the
example needs nothing but a running LRA coordinator.

## What `@PropagateToken` actually does

On the **forward path** `@PropagateToken` makes token handling automatic:

1. `OrderService.createOrder` is annotated with `@PropagateToken`. Narayana's
   `ServerLRAFilter` reads the inbound `Authorization: Bearer <jwt>` header and stashes the
   token in a thread-local for the duration of the request.
2. Every LRA-protocol call the service then makes to the coordinator (start, join, close,
   cancel) is decorated by Narayana's client filter, which re-attaches the token as
   `Authorization: Bearer <jwt>`.
3. The calls from `OrderService` to the inventory and payment participants forward the same
   token explicitly, so those participants can verify the caller and in turn propagate it when
   they enlist with the coordinator.

The result: `@Inject JsonWebToken` is populated on `/inventory/reserve` and
`/payment/process`, and their logs report `authHeader=present`.

### A note on the callbacks

The callback path works differently from the forward path. The coordinator does not persist the
caller's JWT and replay it on the `@Complete`/`@Compensate` callbacks. It attaches a token to
callbacks only when it has been configured to do so; the token it sends is either a request-scoped
token (when a close/cancel is driven by an inbound authenticated request that the coordinator
itself validates) or a pre-provisioned **service token**. Configuring that requires coordinator-side
properties:

```properties
# on the coordinator, not the participant
# attaches the token to callbacks
lra.http-client.providers=io.narayana.lra.coordinator.security.JwtTokenCallbackRequestFilter
# token used by recovery/callbacks
lra.security.service-token.location=file:///etc/lra/service-token.jwt
lra.security.service-token.refresh-seconds=300
```

The standalone coordinator used below is **not** configured this way, so in this example the
callbacks arrive **without** an `Authorization` header — you will see `authHeader=absent` and
`caller=null` in the `@Complete`/`@Compensate` log lines. That is the correct, observable
behaviour for an unconfigured coordinator, and it is exactly what distinguishes forward
propagation (zero-config) from authenticated callbacks (coordinator configuration required).
See [Securing the callbacks](#securing-the-callbacks) for what a real deployment needs.

## Prerequisites

* JDK 17+
* Maven 3.8+

Build everything from the quickstart root first:

```bash
mvn -pl rts/lra-examples/lra-jwt/quarkus/token-propagation -am clean package
```

## 1. Start an LRA coordinator

This example needs an external coordinator. The
[`lra-coordinator`](../../../lra-coordinator/README.md) module builds a runnable Quarkus jar for
a standalone coordinator that listens on port 8080. From this module's directory:

```bash
java -jar ../../../lra-coordinator/target/lra-coordinator-quarkus.jar &
```

## 2. Start the participant application

The application reads its port (8081) and the coordinator URL from
`src/main/resources/application.properties`, so no system properties are required:

```bash
java -jar target/quarkus-app/quarkus-run.jar
```

## 3. Get a token

`GET /auth/token` mints a sample JWT (issuer `https://demo.narayana.io`) and returns it already prefixed with `Bearer `. The matching public key is
published at `/auth/jwks`, which is what `mp.jwt.verify.publickey.location` points at, so the
application can verify the tokens it issues.

```bash
TOKEN=$(curl -s "http://localhost:8081/auth/token?subject=example-user")
echo "$TOKEN"
```

> The token generator creates its RSA key pair in memory at start-up. No key material is ever
> written to disk or committed to source control. In production you would replace it with a
> real identity provider (Keycloak, Entra ID, Auth0, ...) and point
> `mp.jwt.verify.publickey.location` at that provider's JWKS endpoint.

## 4. Create an order (happy path)

```bash
curl -s -X POST http://localhost:8081/orders/create \
  -H "Content-Type: application/json" \
  -H "Authorization: $TOKEN" \
  -d '{"orderId":"ORDER-001","itemId":"ITEM-123","quantity":2,"amount":99.99}'
```

Response:

```json
{"orderId":"ORDER-001","status":"CONFIRMED"}
```

In the application log you will see the token propagated on the forward path and the callbacks
firing after the LRA closes:

```
Creating order ORDER-001 in LRA http://localhost:8080/lra-coordinator/0_ffff... (caller=example-user)
Reserving 2 x ITEM-123 for order ORDER-001 in LRA ... (caller=example-user, authHeader=present)
Processing payment of 99.99 for order ORDER-001 in LRA ... (caller=example-user, authHeader=present)
Order ORDER-001 created successfully
Complete callback for LRA ... (caller=null, authHeader=absent): committed ITEM-123
Complete callback for LRA ... (caller=null, authHeader=absent): captured 99.99
```

Note how `reserve`/`process` see `authHeader=present` and `caller=example-user` (forward
propagation), while the `@Complete` callbacks see `authHeader=absent` (unconfigured
coordinator), as explained above.

## 5. Trigger a compensation

A negative `amount` makes the payment decline (HTTP 402). `OrderService` turns that into a
failure, the LRA is cancelled and the inventory reservation that already joined is compensated:

```bash
curl -i -X POST http://localhost:8081/orders/create \
  -H "Content-Type: application/json" \
  -H "Authorization: $TOKEN" \
  -d '{"orderId":"ORDER-002","itemId":"ITEM-123","quantity":1,"amount":-5.00}'
```

The client receives `HTTP/1.1 502 Bad Gateway`, and the log shows the reservation being
released:

```
Reserving 1 x ITEM-123 for order ORDER-002 in LRA ... (caller=example-user, authHeader=present)
Payment declined for order ORDER-002 (negative amount)
Compensate callback for LRA ... (caller=null, authHeader=absent): released ITEM-123
```

## 6. Authentication in action

`createOrder` and the `reserve`/`process` participant endpoints are annotated with
`@Authenticated`, so a request without a valid token is rejected before any work runs
(`@LRA(MANDATORY)` requires an LRA context, not an identity, so the participants need it too):

```bash
# No token -> 401 Unauthorized
curl -s -o /dev/null -w '%{http_code}\n' -X POST http://localhost:8081/orders/create \
  -H "Content-Type: application/json" \
  -d '{"orderId":"ORDER-003","itemId":"ITEM-123","quantity":1,"amount":10.0}'
```

Kill the participant and the coordinator when you are done.

## How it works

| Component | Role | Key annotations |
| --------- | ---- | --------------- |
| `service/OrderService` | Saga orchestrator | `@LRA(REQUIRED)`, `@AfterLRA`, `@PropagateToken`, `@Authenticated` |
| `service/InventoryService` | LRA participant | `@LRA(MANDATORY)`, `@Authenticated`, `@PropagateToken`, `@Complete`, `@Compensate` |
| `service/PaymentService` | LRA participant | `@LRA(MANDATORY)`, `@Authenticated`, `@PropagateToken`, `@Complete`, `@Compensate` |
| `service/IdentityResource` | Stand-in identity provider (`/auth/token`, `/auth/jwks`) | — |
| `util/JwtKeyGenerator` | Runtime RSA key + token minting | — |

### Configuration (`application.properties`)

| Property | Value | Purpose |
| -------- | ----- | ------- |
| `quarkus.http.port` | `8081` | Participant port (coordinator uses 8080) |
| `lra.coordinator.url` | `http://localhost:8080/lra-coordinator` | Where the LRA client reaches the coordinator |
| `mp.jwt.verify.publickey.location` | `http://localhost:8081/auth/jwks` | JWKS used to verify inbound tokens (served by this app) |
| `mp.jwt.verify.issuer` | `https://demo.narayana.io` | Required `iss` claim |
| `quarkus.arc.exclude-types` | LRA filter classes | Let the LRA runtime own its framework classes instead of Arc |

## Securing the callbacks

The forward path above secures participant → coordinator calls. The reverse path — the
coordinator's `@Complete`/`@Compensate`/`@Status` callbacks — is secured separately, because
those calls are made by the coordinator, not the end user. Two pieces are needed:

1. **Enforce auth on the participant's callback endpoints.** This module ships the policy
   switched off by default; enable it by starting the participant with
   `-Dparticipant.callback.auth.policy=authenticated` (see `application.properties`). The
   complete/compensate paths then reject any call without a valid token (HTTP 401).
2. **Have the coordinator present a token on callbacks.** Run a coordinator (e.g. the
   `lra-coordinator-quarkus` app, LRA 2.0.1.Final+) with:
   ```
   -Dlra.http-client.providers=io.narayana.lra.coordinator.security.JwtTokenCallbackRequestFilter
   -Dlra.security.service-token.location=/path/to/service-token.jwt
   ```
   The filter attaches a bearer token to every outbound callback. The token is resolved as:
   the **propagated caller token** for callbacks that run inline with the saga (still on a
   request thread), falling back to the **service token** for recovery-driven callbacks, which
   run on background threads with no inbound request — and where the user's original token may
   anyway have expired.

Because this example publishes its own JWKS, it can also mint the coordinator's service token, so
the two share a verification key:
```
curl -s "http://localhost:8081/auth/token?subject=lra-coordinator" | sed 's/^Bearer //' > /tmp/service-token.jwt
```
With both sides configured, the participant's callback log shows `authHeader=present` (versus
`authHeader=absent` when the coordinator sends no token). In a real deployment the service token
comes from the same identity provider as the forward-path tokens (a shared issuer/JWKS), not
from this example endpoint.

## Running against a containerized coordinator (service token as a mounted secret)

Rather than passing the service token as a file path on the host, a real deployment mounts it as
a secret volume into the coordinator container. The [`lra-coordinator-quarkus`](https://github.com/jbosstm/lra-coordinator-quarkus)
app (LRA 2.0.1.Final+) supports this; the steps below use `podman` (swap in `docker` as needed).

1. Pull the published coordinator image:
   ```
   podman pull quay.io/jbosstm/lra-coordinator:latest
   ```
2. Start the participant with its callback endpoints protected, so the coordinator must present a
   valid token on its `@Complete`/`@Compensate` calls (it also serves the JWKS and token endpoint
   used below). Build it (`mvn package`) if you have not already, then run:
   ```
   java -Dparticipant.callback.auth.policy=authenticated -jar target/quarkus-app/quarkus-run.jar &
   ```
3. Mint a **fresh** service token into a host directory that will back the secret volume. Mode
   `0644` matches a Kubernetes secret volume's default and lets the container's non-root runtime
   user read it (a `0600` file isn't readable across the rootless user namespace):
   ```
   mkdir -p ~/lra-secrets
   curl -s "http://localhost:8081/auth/token?subject=lra-coordinator" | sed 's/^Bearer //' > ~/lra-secrets/service-token.jwt
   chmod 644 ~/lra-secrets/service-token.jwt
   ```
4. Run the coordinator with the secret mounted read-only and the config supplied as env vars
   (Quarkus maps `lra.security.service-token.location` → `LRA_SECURITY_SERVICE_TOKEN_LOCATION`,
   etc.). `--network host` lets the container reach the host participant's JWKS and callback URLs
   (`localhost:8081`) and the participant reach the coordinator (`localhost:8080`); in
   compose/Kubernetes you would use service DNS names instead:
   ```
   podman run -d --name lra-coord-jwt --network host \
     -v ~/lra-secrets:/var/run/secrets/lra:ro,Z \
     -e LRA_AUTH_POLICY=authenticated \
     -e MP_JWT_VERIFY_PUBLICKEY_LOCATION=http://localhost:8081/auth/jwks \
     -e MP_JWT_VERIFY_ISSUER=https://demo.narayana.io \
     -e LRA_HTTP_CLIENT_PROVIDERS=io.narayana.lra.coordinator.security.JwtTokenCallbackRequestFilter \
     -e LRA_SECURITY_SERVICE_TOKEN_LOCATION=/var/run/secrets/lra/service-token.jwt \
     -e LRA_SECURITY_SERVICE_TOKEN_REFRESH_SECONDS=60 \
     quay.io/jbosstm/lra-coordinator:latest
   ```

The coordinator re-reads the mounted file every `refresh-seconds`, so rotating the secret (a
Vault Agent sidecar, a Kubernetes projected volume, or just re-writing the file) keeps it from
ever serving an expired token.
Running the secured saga then shows inline callbacks carrying the **caller** token
(`caller=example-user`) and recovery/timeout-driven callbacks carrying the **service** token
(`caller=lra-coordinator`), both `authHeader=present`.

## Production notes

* Replace `JwtKeyGenerator`/`IdentityResource` with a real identity provider and point
  `mp.jwt.verify.publickey.location` at its JWKS endpoint.
* Use HTTPS for the coordinator URL and all inter-service calls.
* Prefer short-lived access tokens for the forward path and a dedicated service token for
  callbacks, as above.
