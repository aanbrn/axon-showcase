# Proposal

## Why

PR #544 landed the 2026-10-11 repository audit **report-only**: its findings were never applied, and nothing since has
touched the sites. Three agent-tooling findings are stale enumerations the repository has outgrown — a member added to a
set the prose still lists incompletely — so `AGENTS.md` now under-describes the repository at exactly the places whose
purpose is to describe it. Its `/opsx-sync` advisory is a recurrence (a second audit found it unapplied): the generated
`opsx-sync` command and `openspec-sync-specs` skill still invite a standalone "sync delta specs to main specs without
archiving" run, which writes the main spec ahead of behavior the code has not been verified against, with
`openspec validate` unable to flag it.

## What Changes

- `AGENTS.md`: the three stale enumerations corrected (each to its full set) — the gateway's spec list gains
  `gateway/client-telemetry`; the tooling-placement list gains `showcase-web-ui/scripts/audit-report.sh`; the
  `toolingUpdates` worked example names all three `pack-cli` entries.
- `AGENTS.md`: the "Sync the main spec only at archive" rule gains the sentence marking `/opsx-sync` archive-internal
  and forbidding a standalone run, so an agent or user following the generated command cannot trip the drift.
- `README.md`: the slash-command table's `/opsx-sync` row corrected — it renders the command as a standalone "sync
  without archiving" step, the framing the rule now forbids.
- `docs/ideas.md`: the `/opsx-sync` advisory idea the sentence implements is removed, since it is now captured by the
  rule.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None: `skip_specs: true`. The change corrects always-loaded guidance (`AGENTS.md`) and retires its parked idea; no
capability spec owns the enumerations or the archive-only rule (verified: no spec mentions `opsx-sync`, `openspec sync`,
or the rule).

## Impact

- Files: `AGENTS.md`, `README.md`, `docs/ideas.md`.
- No code, build, test, or deployment impact. The architecture advisories from the same report (the LZ4 and
  Elasticsearch rationales, the ADR-0013 list, and the two expiring ADR deferral gates) are a separate route and are not
  part of this unit.
