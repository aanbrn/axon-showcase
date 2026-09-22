## MODIFIED Requirements

### Requirement: CORS allows the standalone UI origin

The gateway SHALL allow browser cross-origin requests from the standalone web UI origin, so the UI can call the
`/showcases` REST endpoints and the `/events` SSE endpoint directly. The allowed origins SHALL be configurable; the
container image default SHALL be empty (fail-closed — every deployment must allow its UI origin explicitly), while local
development (docker-compose and `bootRun`) SHALL permit the local UI dev-server and preview origins. The gateway SHALL
also allow the request headers the UI sends (`Content-Type`, `Idempotency-Key`), because a cross-origin request naming a
header the gateway does not allow is rejected at the browser's preflight; the allowed headers SHALL be configurable,
defaulting to the headers the UI sends rather than to an empty list, since a fail-closed header default would reject the
UI's own preflight.

#### Scenario: The UI origin is allowed

- **WHEN** a browser at the configured UI origin calls a gateway endpoint
- **THEN** the gateway responds with the CORS headers permitting the request, including for the SSE endpoint

#### Scenario: Other origins are not allowed

- **WHEN** a browser at an origin not in the configured allow-list calls a gateway endpoint
- **THEN** the gateway does not grant CORS access to that origin

#### Scenario: A deployment without CORS config is fail-closed

- **WHEN** the gateway is deployed without configuring allowed origins
- **THEN** the gateway does not grant CORS access to any browser origin

#### Scenario: The UI's request headers are allowed

- **WHEN** a browser at the configured UI origin sends a preflight for a state-changing request carrying the headers the
  UI sends (`Content-Type`, `Idempotency-Key`)
- **THEN** the gateway grants the preflight, listing those headers in `Access-Control-Allow-Headers`
