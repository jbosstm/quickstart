# JWT with LRA on Quarkus

Quarkus-based quickstarts combining JSON Web Tokens (JWTs) with Long Running Actions (LRA).

- **[`token-propagation`](./token-propagation/README.md)** — carry the caller's JWT through an
  LRA saga with Narayana's `@PropagateToken` and `@Inject JsonWebToken` enforcing identity at each participant.
