# Tasks — clarify-python-ide-activation

Workspace: unit=clarify-python-ide-activation;
path=/Users/afanasyev/Development/axon-showcase-clarify-python-ide-activation; branch=clarify-python-ide-activation;
writer=ses_e6f985602ffeUDJ4DtV0zmSDfc

## 1. The spec delta

- [x] 1.1 Write `specs/showcase/quality/ide-config/spec.md` as a `MODIFIED` block for "The setup reproduces the Python
      analysis module per working tree", copying the current block whole and extending the description with the
      activation procedure: the project opens after a run made while it was closed, and a run made while it was open is
      completed by closing the project, running the setup again, and opening it; a reload is not presented as the way
      the module is added. Outcome: the delta is at that path and `openspec validate --changes` passes.
- [x] 1.2 Add the scenario pinning the reported procedure, at the end of the requirement, and give the build-model
      scenario the precondition that the project has opened the module.
- [x] 1.3 Record the Purpose determination on this task: whether the change falsifies
      `openspec/specs/showcase/quality/ide-config/spec.md`'s Purpose. Outcome: no refresh is owed — the classes are
      unchanged and the run writes the same setup; `design.md` carries the determination.

## 2. The guidance sites

- [x] 2.1 `.opencode/skills/setup-idea/SKILL.md` step 2: closing the project and re-running is what makes the module
      appear, while the plugin install needs the whole application closed.
- [x] 2.2 `.opencode/skills/setup-idea/SKILL.md` step 4: `CONFIGURED` means the module is written and appears when the
      project is opened.
- [x] 2.3 `.opencode/skills/setup-idea/SKILL.md` step 5: the hand-back gives the activation procedure, the
      close-and-re-run step for a run made while the project was open, the Gradle-project load prompt, and scopes the
      reload-or-restart note to the merged formatter and inspection settings.
- [x] 2.4 `scripts/setup-idea.sh` closes with a distinct action section — the heading `=== Next step in IntelliJ ===`,
      the open step, the close-and-re-run recovery, the note that reloading an open project does not add the module, the
      Gradle-project load prompt, and the formatter note last.
- [x] 2.5 `README.md` states the procedure and scopes the reload-or-restart note to the settings.
- [x] 2.6 `AGENTS.md` states the same in the IntelliJ gotcha bullet.

## 3. The committed control

- [x] 3.1 Assert the closing section's clauses in `scripts/test-setup-idea.sh` — one heading, a blank line before it,
      placement after every stage and plugin line, each clause, and the formatter note last — with a control per clause
      and per placement. Outcome: the controls fail for their own named reasons.
- [x] 3.2 Add a fixture whose `.idea/modules.xml` already lists a module, and assert the same closing section, so the
      instruction is shown not to vary with the starting state.

## 4. The unit's records

- [x] 4.1 Keep `docs/process-defects/clarify-python-ide-activation.md` reflecting what the unit observes. Outcome: it
      carries the hand-back episode and the probe verdict that did not reproduce; a later observation is appended as it
      occurs.
- [x] 4.2 Rewrite `docs/decisions/2026-10-31-python-module-activation-probe.md` with what the experiments established —
      the mechanism, the two measured flows, and the alternatives set aside — and mark the probe's own verdict
      superseded. Outcome: the record's "Reopened — 2026-10-31" section carries the measurements the design cites.

## 5. The pre-write pass and the claims table

- [x] 5.1 Run the pre-write derivation pass and record its claims table in `design.md`, seeded with
      `python3 scripts/claim-scan.py` and its dismissals. Outcome: the table and the four per-kind dismissals are in
      `design.md`, and every row reproduces at the base it names.
- [x] 5.2 Re-derive `AGENTS.md`'s applied net delta after the last edit and record it here. Outcome: `+13/−10`, net `+3`
      lines.

## 6. Verification

- [x] 6.1 Run `./gradlew spotlessApply` after the last edit to a Spotless-owned file, then `spotlessCheck`. Outcome:
      both green after the final edit.
- [x] 6.2 Run `openspec validate --all` and confirm the delta validates, scenario preservation included. Outcome: 27
      passed, 0 failed; the change validates.
- [x] 6.3 Run `sh scripts/test-setup-idea.sh` and confirm its cases and controls pass. Outcome: all cases passed.
- [x] 6.4 Run the Docker-free `check` (`./gradlew check -PskipITs -Pcoverage.gate.enabled=false`). Outcome: BUILD
      SUCCESSFUL.
- [x] 6.5 Sweep every live copy of the old reload instruction (`grep -rn "Reload All from Disk"` over the working tree)
      and confirm each remaining one is the formatter-and-inspection settings' statement. Outcome: the remaining
      mentions are the formatter-and-inspection note and the records.

## 7. The review gate

- [x] 7.1 Run the `review-quick` loop over the rewritten artifacts and append each round's row to
      `docs/review-rounds.tsv`; both ledger checks pass, and `--expect-clean` is green at the closing round. Outcome:
      every round's row is appended as it closes, the deep review's fixes were reviewed in a further round, and
      `--expect-clean` is green at the closing round.
- [x] 7.2 Record the deep-review routing outcome on this task: run `scripts/review-routing.py` with `--unit` and
      `--change` set to this change's name. Outcome: `RECOMMENDED` (on the `[repeated-class]` signal); the owner ran the
      routed deep review, recorded on 7.3.
- [x] 7.3 Run the routed deep review (`/review-thorough`) in the advisor's focus: obligation → implementation →
      evidence. Outcome: 4 findings (3 should-fix, 1 nitpick) — the activation clause printed for every outcome, a stale
      7.1 outcome, the record's Gradle-sync bullet, and the claims-table headline — all fixed; the clause is now
      conditional on the Python stage's `CONFIGURED` (not the settings stage's).
- [ ] 7.4 Ask the owner for the manual review pass and end the turn there.

## 8. Ship

- [ ] 8.1 After the owner's approval: commit, push, open the pull request, wait for `build`, archive, and merge.
