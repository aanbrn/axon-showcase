## Context

See `proposal.md` - Why. Measured against a booted gateway (`./gradlew :showcase-api-gateway:bootRun`, then `curl`
preflights from an allowed origin, `http://localhost:5173`):

| Preflight                                         | Result                                                           |
| ------------------------------------------------- | ---------------------------------------------------------------- |
| `OPTIONS` `PUT`, no requested headers             | 200, `Access-Control-Allow-Methods: GET,POST,PUT,DELETE,OPTIONS` |
| `OPTIONS` `POST`, no requested headers            | 200                                                              |
| `OPTIONS` `POST` + `content-type`                 | **403**                                                          |
| `OPTIONS` `POST` + `content-type,idempotency-key` | **403**                                                          |

The 200 responses carry no `Access-Control-Allow-Headers` — the registration sets none. Spring's
`CorsConfiguration.checkHeaders` returns `null` when `allowedHeaders` is empty, and WebFlux's `DefaultCorsProcessor`
rejects the preflight on `null` (verified in `spring-web` 6.2.19's sources).

`ShowcaseApiConfigurer.addCorsMappings` registers `.allowedOrigins(...)` and
`.allowedMethods("GET","POST","PUT","DELETE","OPTIONS")`. The UI's `features/create-showcase/api.ts` sends
`Content-Type: application/json` and `Idempotency-Key`, so its preflight is the rejected row.

## Goals / Non-Goals

**Goals:**

- Allow the request headers the UI sends, so a cross-origin deployment can create a showcase again.
- Guard it with a test that fails against the current registration (a check, not a comment).

**Non-Goals:**

- `traceparent` — the UI does not send it yet; the trace-propagation change adds it to this same list when it lands.
- Any change to origins or methods, which already work.

## Decisions

### Add `allowedHeaders`, mirroring the existing `allowedOrigins` property

The registration already exposes origins through `ShowcaseApiProperties.Cors`, so headers go the same way: a
`@NotNull List<@NotBlank String> allowedHeaders` defaulting to `Content-Type`, `Idempotency-Key`. This matches the
repo's config-owned-by-Java rule (ADR-0002) and keeps the CORS surface declared in one place. A hardcoded list in the
configurer was considered and rejected: the property costs one field and lets a deployment adjust without a rebuild.

**`"*"` was considered and rejected.** Spring's `allowedHeaders("*")` would permit any header, but it is broader than
the UI's contract and drifts from the fail-closed posture the origin default already takes (an empty list denies until a
deployment opts in). An explicit list says what the UI actually sends.

### Expose the header list through the Helm chart, as the origins already are

The chart's `apiGateway.cors.allowedOrigins` value feeds `SHOWCASE_CORS_ALLOWED_ORIGINS`, and the pairing is documented
in `AGENTS.md`. A new configurable header list whose only surface were a placeholder default would be inconsistent with
its sibling and unconfigurable in a deployment, so the chart gains `apiGateway.cors.allowedHeaders` feeding
`SHOWCASE_CORS_ALLOWED_HEADERS`, plus the `AGENTS.md` env-var line. Its default is the headers the UI sends — unlike the
origin list, which defaults to `[]` because a deployment's origin is unknown at chart-authoring time, the UI ships with
the chart, so a fail-closed empty default would ship the very bug this change fixes.

ADR-0002 names the surfaces a default is declared on — the Java field, the yml placeholder, the chart, and, where the
service carries the variable there, its `BPE_DEFAULT_*` map — so the header list is carried on all four, each with the
UI's headers. The image's BPE entry mirrors the Java default, as `SHOWCASE_EVENTS_KEEP_ALIVE_INTERVAL` already does; the
origins are the exception that proves the rule, baked empty because their permissive yml default must not reach an
image.

The CORS requirement already promises the UI can call the REST endpoints; the fix makes that true for the create
endpoint rather than adding a capability, so the delta adds the header clause and a scenario while keeping the
requirement's three existing scenarios and its fail-closed origin default.

### Test at the integration tier, where the CORS config is real

The registration lives in a `WebFluxConfigurer`, so a slice test may not load it; `ShowcaseApiApplicationIT`
(`@SpringBootTest(webEnvironment = RANDOM_PORT)`) already boots the real app. The test sends the UI's preflight and
asserts it is granted — it fails against the current registration (the 403 reproduced above is that failure), so it is a
real guard rather than a green-on-both-sides assertion.

## Risks / Trade-offs

- **The e2e still will not cover the cross-origin path** (it proxies same-origin). Mitigation: the new IT is the guard;
  an e2e that speaks cross-origin is its own change, noted rather than smuggled in.
- **A future UI header would need the list extended**, and the failure mode is a 403 at runtime. Mitigation: the
  property is visible and the IT names the headers it exercises, so the gap is one edit, not a hunt.

## Migration Plan

None — no data, config, or deployment change.

## Open Questions

None.
