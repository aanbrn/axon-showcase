# Tasks

## 1. AGENTS.md — the three stale enumerations

- [x] 1.1 In the Project Overview (`AGENTS.md:22-23`), extend the gateway's spec list from `gateway/rest-api` plus
      `gateway/live-events` to also name `gateway/client-telemetry` (derived from the spec corpus). Verify by reading
      the sentence back.
- [x] 1.2 In the "Tooling lives in a stated place" bullet (`AGENTS.md:1374-1376`), add
      `showcase-web-ui/scripts/audit-report.sh` to the module-local enumeration (derived from the rule's own
      `git ls-files -s | awk '$1=="100755"'`). Verify by re-running that derivation against the sentence.
- [x] 1.3 In the `toolingUpdates` gotcha (`AGENTS.md:2774-2775`), correct the worked example so it says the smoke's
      `pack` pin sat unlisted until it gained its own `pack-cli (deployment-smoke.yml)` entry and names all three
      per-file entries (`e2e.yml`, `release.yml`, `deployment-smoke.yml`), derived from `build.gradle.kts`'s declared
      list. Verify against that list.

## 2. AGENTS.md — the `/opsx-sync` advisory

- [x] 2.1 In the "Sync the main spec only at archive" rule (`AGENTS.md:321`), add the sentence marking the generated
      `/opsx-sync` as archive-internal — legitimate inline within `openspec archive`, never run standalone — since a
      standalone run writes the main spec ahead of verified behavior and `openspec validate` cannot flag it. Verify by
      reading the rule back.
- [x] 2.2 In `README.md`'s slash-command table (`README.md:500`), correct the `/opsx-sync` row so it no longer renders
      the command standalone — mark it internal to `/opsx-archive`. Verify the row and its neighbours read correctly.

## 3. docs/ideas.md

- [x] 3.1 Remove the `/opsx-sync` advisory entry (`docs/ideas.md:102-109`), now implemented by task 2.1. Verify the
      entry is gone and no other entry references it.

## 4. Verification

- [x] 4.1 Run `./gradlew spotlessApply` then `./gradlew spotlessCheck` and `openspec validate --all`; confirm clean. The
      changed files are markdown (formatter-owned, `spotlessMarkdownCheck`) plus the change dir's `.openspec.yaml`,
      whose three lines are within 120 — the only non-formatter content this change touches.
- [x] 4.2 Run a `review-quick` pass over the proposal and the implementation, repeating until clean.

## 5. Capture

- [x] 5.1 Run the `lesson-capture` subagent over the diff, apply its durable proposals, and record the applied net
      `AGENTS.md` delta on this task. Verdict: **nothing durable — net 0 `AGENTS.md` lines**. All three review findings
      are instances of rules `AGENTS.md` already carries (the docs-refresh live-copy rule, the tooling-placement
      bullet's own "derive from the complete set" sentence, and the documented-numbers set-derivation clause), and the
      `/opsx-sync` recurrence is the evidence the parked "carry findings forward across audit reports" idea awaited — no
      competing rule added. It also found and this unit fixed a live defect: a stray untracked `120` file a broken
      120-char check had written into the repo root (removed before staging). No retirements proposed.
- [x] 5.2 Ask the owner for the manual review pass, after 4.2 is clean and 5.1 is done.

## Workflow follow-up

- Archive the change after the owner approves; its archive commit rides this PR.
