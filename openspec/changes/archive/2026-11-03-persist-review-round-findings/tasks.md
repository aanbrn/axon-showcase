# Tasks

The unit record — its `Workspace:`, state, and pause checkpoint — is `docs/units/persist-review-round-findings/unit.md`.

## 1. The store's check

- [x] 1.1 `scripts/commit-hygiene.py` gains a `--finding-instances` mode and the boundary constant
      `FINDING_INSTANCES_THROUGH` (the last exempt day, set to the change's landing date): it reads each unit's record
      and its `findings/round-<N>.md` and reports every failure the `commit-hygiene` requirement names.
- [x] 1.2 The pre-commit guard judges the index's record and instances files for the affected unit, running when either
      side changes and reading the index rather than the working tree, so a staged instances deletion beside an
      unchanged record is refused and a file an earlier commit recorded is seen.
- [x] 1.3 `scripts/test-commit-hygiene.py` covers every failure the requirement names, plus a clean round's `- none`,
      the boundary's exemption, a wrapped entry and a two-entry file, the guard's staged-deletion refusal, its index
      snapshot, and its unreadable-file failure.

## 2. The build

- [x] 2.1 `build.gradle.kts` registers the verification as a `check` task whose inputs include `docs/units`, and the
      mode's mapping in the coverage test names it.

## 3. The writer and the readers

- [x] 3.1 `.opencode/skills/review-gate/SKILL.md` writes the round's file when its report arrives, beside the ledger
      row, and has the hand-over read it.
- [x] 3.2 `.opencode/skills/pause-resume-unit/SKILL.md` states that the checkpoint's "Findings no record holds" field
      points at the store, and that a round predating the store falls back to the checkpoint or the hand-over, with
      neither read as unrecovered context rather than as a clean round.
- [x] 3.3 `docs/contracts/process-bindings.md` names the store as the `prior-findings` value, with that fallback stated
      in the role's owner.
- [x] 3.4 `AGENTS.md` names the store wherever its review-gate stage states the prior findings' retrieval — it states
      none, so only the sweep applies — and the sweep finds no live document stating the checkpoint field as the only
      copy.
- [x] 3.5 The parked idea for this work is removed from `docs/ideas.md`.

## 4. The gates

- [x] 4.1 The claims table in `design.md` is re-derived after the last edit, and each row reproduces.
- [x] 4.2 `openspec validate --all` passes, and `./gradlew spotlessApply` runs after the final edit.
- [x] 4.3 The Python suites run under the declared floor, and the touched build's `check` passes.
- [x] 4.4 `docs/process-defects/persist-review-round-findings.md` records what the unit observed.
- [x] 4.5 The review gate runs: quick rounds 1–3 with a clean closing round; the routing decision is `REQUIRED`
      (`requirement-block-added`); thorough pass 1 found `F1` (Purpose), `F2` (empty-file leniency), and `F3` (ADR
      pointer), all `FIXED_AND_VERIFIED` at pass 2, which reports nothing remains. The owner's manual pass approves the
      implementation.
- [x] 4.6 Before the archive commit, `FINDING_INSTANCES_THROUGH` is re-derived to the landing date and
      `verifyFindingInstances` passes over the records at that date.
- [x] 4.7 At the archive, the `showcase/quality/commit-hygiene` `## Purpose` is refreshed to name the finding-instances
      verification among the checks it enumerates, as the archive commit's own edit.
