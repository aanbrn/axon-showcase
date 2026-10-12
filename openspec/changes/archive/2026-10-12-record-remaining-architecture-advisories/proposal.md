# Proposal

## Why

Three of the 2026-10-11 architecture audit's advisories concern the completeness of the decision record: two structural
choices whose rationale is recorded nowhere, and a stale ADR-0013 parenthetical. (Of its other two, the ADR-0011 item
shipped in `#553` and the ADR-0003 gate stays parked.) The unrecorded two are the repo-wide substitution of yawkat's
`at.yawk.lz4:lz4-java` for the unmaintained `org.lz4:lz4-java` (whose `.because()` restates only what it does) and the
read side's `co.elastic.clients:elasticsearch-java` declaration (which no source imports).

Both are now evidenced. The LZ4 fork's README states it exists to fix **CVE-2025-12183** in the unmaintained original
and recommends exactly this substitution. A probe removing the Elasticsearch client from its three modules **failed the
read side's suites** with `NoClassDefFoundError: co/elastic/clients/ApiClient` from Spring Data Elasticsearch's
`client.elc` classes at context startup — so it is load-bearing, not a leftover.

## What Changes

- New `ADR-0020` (retrospective, dated to its shipping commit): substitute yawkat's maintained `lz4-java` for the
  unmaintained, CVE-2025-12183-affected `org.lz4` coordinate repo-wide, so no consumer needs its own exclude.
- `ADR-0012`: record the read side's Elasticsearch client coordinate — why it is load-bearing at runtime (Spring Data
  Elasticsearch's `client.elc` classes, reached transitively through `spring-data-opensearch`, are loaded by Spring
  Boot's Elasticsearch data auto-configuration), the catalog pin above Spring Boot's managed version, and the probe's
  evidence that removing it breaks the read side.
- `ADR-0013`: refresh the `shared` parenthetical to the layer's current slices.
- `AGENTS.md`: the LZ4 relocation line points at ADR-0020 for the rationale.
- `docs/ideas.md`: remove the two parked entries this change implements.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None: `skip_specs: true`. The change records rationale in ADRs and guidance; it alters no specified behavior. The
`dependency-security` requirement's Jackson-alignment scenario remains accurate — a module that depends on
`elasticsearch-java` still does.

## Impact

- Files: `docs/adr/0020-*.md` (new), `docs/adr/0012-*.md`, `docs/adr/0013-*.md`, `AGENTS.md`, `docs/ideas.md`.
- No code, build, or deployment impact. The probe's build-file edits were reverted; the declarations stay.
