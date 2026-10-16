# Tasks

## 1. The pins

- [x] 1.1 Re-resolve the OpenSpec CLI against its own lookup (`npm view @fission-ai/openspec version` → `1.14.2`) and
      set the pin in **every** workflow that declares it: `.github/workflows/ci.yml`, `opencode.yml` and `audit.yml`.
- [x] 1.2 Give the check an entry for each file, as its own rule requires: `build.gradle.kts` now carries
      `openspec-cli (ci.yml)`, `openspec-cli (opencode.yml)` and `openspec-cli (audit.yml)`, with the two newly-read
      files registered in `pinFiles`. Verified by a negative control — pinning `audit.yml` back to `1.14.1` makes the
      check report `openspec-cli (audit.yml): 1.14.1 -> 1.14.2`, which it could not have done before.
- [x] 1.3 Regenerate the instruction files for the new CLI (`npx @fission-ai/openspec@1.14.2 update`) and inspect the
      result — what changed, and the per-file split, is stated once in `proposal.md`'s What Changes; the inventory the
      generated files belong to is unchanged.
- [x] 1.4 Re-resolve the Snyk CLI (`gh api repos/snyk/cli/releases/latest` → `v1.1308.1`) and set `snyk-version` in
      `.github/workflows/dependency-security.yml`. The action's floating ref stays.

## 2. Verification

- [x] 2.1 Run `./gradlew workflowLint` (actionlint) over the four edited workflows and `./gradlew toolingUpdates`;
      **lint clean, and the report now reads "No tooling updates available."**
- [x] 2.2 Verify the new CLI still reads every declared list surface the command names: a probe change reads populated
      `rules` (for `proposal`) and `operationGuidance` (for `apply`) with **no warning** on a healthy config, and each
      warning pattern fires when exercised — an unquoted `: ` in a `rules` item, the same in an `operations.*.guidance`
      item, and a dropped closing quote on a quoted item (`could not parse … Missing closing 'quote at line 55`). The
      probe was removed and `openspec/config.yaml` restored to a zero-line diff.
- [x] 2.3 Run `spotlessApply`/`spotlessCheck`, `openspec validate --all`, and the manual 120-character check over the
      changed set.
- [x] 2.4 Sweep the five document surfaces a version bump makes stale for the old pin. Two sites named it: `AGENTS.md`'s
      config-read-path gotcha and the parked idea that closes on the same upstream work. Both now name the mechanism
      rather than the version, and the idea's version-pinned threshold — a restatement of the condition its own sentence
      states — is deleted rather than renumbered. `docs/audits/2026-10-11.md`'s mention is a dated record and stays.
- [x] 2.5 Run a `review-quick` pass over the proposal and the implementation, repeating until clean: six rounds (2, 5,
      1, 1, 1, clean). Four of them repeated `wording` on the same claim stated in five places — the remedy was
      single-sourcing it (the What Changes bullet owns it; the rest point), plus, for the last three, sweeping the
      replaced text's tail tokens across the change dir to catch the fragments my block edits left behind.

## 3. The dispatch, the capture and the manual pass

- [ ] 3.1 **After the owner's approval pushes the branch** (before the archive commit), dispatch the credentialed
      workflow against it (`gh workflow run dependency-security.yml --ref bump-tooling-pins-2026-10-16`) and watch it:
      it must install the pinned CLI and complete the scan. A red run attributed to the pin means the tag does not
      install; one attributed to a finding means the policy, not the pin.
- [x] 3.2 Run the `lesson-capture` subagent over the diff, apply its durable proposals, and record the applied net
      `AGENTS.md` delta here: the change's whole `AGENTS.md` diff is **+6 net** (`git diff --numstat`), the version
      sweep's rewrite (−1) and the capture's two clauses together. Both clauses merged into rules the incidents
      repeatedly missed — the removed-token sweep widened from "a trim" to any block/span replace (four `wording` rounds
      were survivors of my own edits), and a widened check's describers going stale (the unit's own `/opsx-tool-update`
      sentence, fixed here). It also found that defect and had me fix it, flagged a `docs/ideas.md` clause the weekly
      reference sweep makes misleading (corrected), and routed the check-coverage lesson to a machine assertion — parked
      in `docs/ideas.md`, since its landing would retire the `toolingUpdates` bullet's hazard clause.
- [ ] 3.3 Ask the owner for the manual review pass, after 2.5 is clean and 3.2 is done.

## 4. The change's own records

- [x] 4.1 Record the pre-write derivation pass as a task (every unit owes it; a recurring unit's derived task set is a
      floor, not the whole set). It ran: the claims table is in `design.md`, and its rows were derived — the pins from
      each tool's own lookup, the pin's three homes from a repo grep, the eight regenerated files from `git status`, and
      the spec-position reading from `grep -rln … openspec/specs/`.

## Workflow follow-up

- Archive the change after the owner approves; its archive commit rides this PR.
- The Helm and Paketo findings are separate units, each with an environment-specific verification.
