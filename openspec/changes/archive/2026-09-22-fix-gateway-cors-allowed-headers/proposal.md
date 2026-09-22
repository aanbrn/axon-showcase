## Why

The gateway's CORS registration sets `allowedOrigins` and `allowedMethods` but **no `allowedHeaders`**, and Spring
rejects a preflight whose requested headers are not allowed. Measured against a booted gateway: a preflight from an
allowed origin with `POST` + `content-type` returns **403**, while `PUT` with no requested headers returns 200. The UI's
create flow sends `Content-Type: application/json` and `Idempotency-Key` — both non-safelisted — so **creating a
showcase is blocked in any cross-origin deployment** (compose `:8084`, the Helm UI host), while the spec already
promises "the UI can call the `/showcases` REST endpoints". CI cannot see it: the web UI e2e serves the UI through a
`vite preview` proxy, so no preflight ever runs.

## What Changes

- `showcase-api-gateway/.../ShowcaseApiProperties.java`: add an `allowedHeaders` list to the `Cors` properties,
  defaulting to the headers the UI sends (`Content-Type`, `Idempotency-Key`), plus the matching
  `showcase.api.cors.allowed-headers` placeholder in `application.yml`.
- `showcase-api-gateway/.../ShowcaseApiConfigurer.java`: register `.allowedHeaders(...)` from those properties.
- `showcase-api-gateway/build.gradle.kts`: add `BPE_DEFAULT_SHOWCASE_CORS_ALLOWED_HEADERS` to the image's environment
  map, mirroring the Java default as `BPE_DEFAULT_SHOWCASE_EVENTS_KEEP_ALIVE_INTERVAL` does — not the origins' empty
  fail-closed override, which would reject the UI's own preflight.
- `specs/showcase/gateway/rest-api/spec.md`: extend the CORS requirement — it SHALL allow the request headers the UI
  sends, not only its origin.
- Add the tests: the header default, an env-var override, and a blank-value validation case in
  `ShowcaseApiPropertiesCT`, plus a preflight regression test that sends the UI's preflight and asserts it is granted
  (which fails against the current code).
- `helm/chart` (`values.yaml` + the gateway `deployment.yaml` template): expose `apiGateway.cors.allowedHeaders` →
  `SHOWCASE_CORS_ALLOWED_HEADERS`, mirroring the existing `allowedOrigins` → `SHOWCASE_CORS_ALLOWED_ORIGINS`; document
  the env var in `AGENTS.md`'s Key Environment Variables, which names the chart value for its sibling.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/gateway/rest-api`: the `CORS allows the standalone UI origin` requirement gains the request-header allowance
  it implicitly needs — the requirement is currently unmet for the create endpoint.

## Impact

- **Code**: `ShowcaseApiProperties.Cors` + `ShowcaseApiConfigurer` in `showcase-api-gateway`; the header default,
  override, and blank-value validation tests in `ShowcaseApiPropertiesCT`, plus a preflight regression test in
  `ShowcaseApiApplicationIT`.
- **Specs**: one `MODIFIED` delta on `gateway/rest-api`.
- **Deployment**: `helm/chart`'s `apiGateway.cors.allowedHeaders` value + the gateway deployment template's env var, and
  the image's `BPE_DEFAULT_*` map; a cross-origin deployment can create showcases again. `AGENTS.md` documents the new
  env var.
- **Docs**: `AGENTS.md`'s Key Environment Variables gains the header variable alongside `SHOWCASE_CORS_ALLOWED_ORIGINS`;
  `docs/ideas.md`'s parked RUM idea now names the list its `traceparent` work must extend.
