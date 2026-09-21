## Context

The scheduled dependency scan (`snyk.yml`) runs weekly against `all-sub-projects` with the root `.snyk` policy. On
2026-09-21 it failed with five new advisories (run `35565636502`). The repository's policy, stated in the
`dependency-security` spec and practised in `.snyk`, is to **constrain flagged transitives to their patched versions**
where one exists, and to suppress only where none does — pinned to the exact assessed version, with a short expiry so
the scanner re-surfaces it.

## Goals / Non-Goals

**Goals:**

- Clear the scan by fixing what is fixable and suppressing only what is not, with each suppression earning its place.
- Keep each suppression narrow: one advisory id, one exact version, one expiry.

**Non-Goals:**

- Removing `snappy-java` from the production classpath. It is a hard, non-optional `kafka-clients` dependency (verified
  via the resolved runtime classpath), so an exclusion would be a behavioral change needing a live smoke test, not a
  dependency-hygiene fix. Recorded as a consideration below rather than pursued.
- Chasing the Spring Framework advisories already suppressed under ADR-0004; those expire on their own schedule.

## Decisions

### Bump `netty-bom` rather than suppress the netty advisory

`SNYK-JAVA-IONETTY-19778369` has a patched version: `4.2.18.Final` is published, is a patch-level step on the same
`4.2.x` line our BOM already pins, and is what the scanner itself names as the fix. Our own catalog entry forces
`4.2.17.Final` (the framework BOMs resolve through ours), so the bump is one catalog line. This is the spec's "constrain
to patched versions" path and needs no suppression.

### Suppress `snappy-java` and `t-digest`, because no patched release exists

All three `snappy-java` advisories read "**through 1.1.10.8**" — the newest release is itself affected — and `t-digest`
has no release after `3.3`. Verified against the advisory records (OSV), not the scanner's summary alone. So a bump
cannot clear them and a suppression is the honest route. Each reason states the dependency path and why no patch exists.

`snappy-java` reaches the **production** runtime classpath of `showcase-command-service` (via `axon-kafka` →
`cloudevents-kafka` → `kafka-clients`), unlike the existing `t-digest` suppression (Gatling, load-tests only) — so its
reason names that path, and it carries the same ~90-day rolling expiry so it is revisited rather than forgotten.

### Considered and rejected: excluding `snappy-java` from `kafka-clients`

`kafka-clients` declares `snappy-java` as a hard dependency, and our config enables no compression codec (checked: no
`compression.type`/codec setting anywhere in the services' config). An exclusion would therefore likely work today, but
it changes the production classpath of a running service for a vulnerability in a code path we do not invoke, and would
surface only under a codec change or a live smoke test. Suppressing with a named path and an expiry keeps the signal
without a silent behavioral edge; the exclusion stays available if the advisors ever enable Snappy.

## Risks / Trade-offs

- **The netty bump is a runtime dependency change.** It is a patch bump on a line the framework BOMs already select, and
  `check` plus the integration tests exercise the HTTP stack, but the gateway/query-service paths (Netty) are worth a
  green `build` before merging.
- **Suppressions hide a real (if uninvokable) vulnerability** in a production dependency for its expiry window — the
  trade the existing policy already accepts, mitigated by the short expiry and the named path.

## Migration Plan

None — no stored data, API, or deployment change.

## Open Questions

None.
