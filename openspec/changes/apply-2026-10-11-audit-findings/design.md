# Design

## Context

See `proposal.md` — Why. The 2026-10-11 agent-tooling audit's three findings and its advisory were verified live on this
branch before planning (each site still reads as reported, and nothing since #544 edited it):

- `AGENTS.md:22-23` names the gateway's specs as `gateway/rest-api` plus `gateway/live-events`, while
  `openspec/specs/showcase/gateway/client-telemetry/spec.md` exists (added by `add-web-ui-rum-observability`).
- `AGENTS.md:1376` lists a module's local tooling as `showcase-web-ui/scripts/outdated-report.sh` and
  `showcase-web-ui/start.sh`, while the rule's own derivation, `git ls-files -s | awk '$1=="100755"'`, puts three files
  under the module: those two and `showcase-web-ui/scripts/audit-report.sh` (the `npmAudit` task's raw-report writer).
- `AGENTS.md:2774-2775` says a file no `toolingUpdates` entry names has its pin unchecked and gives `pack-cli (e2e.yml)`
  as the per-file example, while `build.gradle.kts` now declares three `pack-cli` entries (`e2e.yml`, `release.yml`,
  `deployment-smoke.yml`).
- `AGENTS.md:321` (the "Sync the main spec only at archive" rule) carries no sentence marking `/opsx-sync`
  archive-internal; no spec mentions `opsx-sync`, `openspec sync`, or the rule, so no delta is owed.
- `README.md:500`'s slash-command table renders `/opsx-sync` as "Syncs a change's delta spec to the main spec without
  archiving" — a second live copy of the standalone framing the rule forbids, in the human-facing command list, so the
  sweep covers it too.

## Goals / Non-Goals

**Goals:**

- Correct the three enumerations to their full sets, and close the `/opsx-sync` advisory (a recurrence across two
  audits) in the rule it concerns.
- Retire the `docs/ideas.md` entry the advisory sentence implements.

**Non-Goals:**

- The report's architecture advisories — the LZ4 and Elasticsearch-client rationales, the ADR-0013 shared-layer list,
  and the two expiring ADR deferral gates (ADR-0003's Jackson 3, ADR-0011's Axon 5). Those are a recording/decision
  route, and the Axon-5 gate is an owner decision, not a drive-by edit.
- A mechanism to keep the enumerations current: the scheduled audit already owns that, and a second check would restate
  it.

## Decisions

**Apply each finding as the report's suggested rewrite, re-derived from the full set.** Each rewrite is derived from the
set the rule itself names as authoritative — the spec corpus for finding 1, `git ls-files -s` for finding 2, the
`toolingUpdates` declared list for finding 3 — rather than from the report's prose, so the correction carries the
complete set rather than the one member the finding happened to name.

**State the `/opsx-sync` fix as a sentence in the archive-only rule, not a change to the generated files.** The
generated `opsx-sync` command and skill are OpenSpec's, not the repository's, and the report decided this is our usage
to change; the rule is the surface an agent reads before running the command, so the sentence belongs there, and the
README's table row — a second live copy of the same framing — is corrected in the same sweep.

## Risks / Trade-offs

- The three enumerations can go stale again as their sets grow → the scheduled agent-tooling audit reports exactly this
  class, so no new guard is added here.
- The `/opsx-sync` sentence is guidance, not an enforced gate (`openspec validate` cannot flag a standalone sync) → the
  rule's enforcement is the reader, which is why it is stated where the command is chosen.

## Migration Plan

None: guidance prose and a scratchpad entry, taking effect on the next read.
