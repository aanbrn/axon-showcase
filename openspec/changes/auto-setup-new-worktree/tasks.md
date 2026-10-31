# Tasks — auto-setup-new-worktree

Workspace: unit=auto-setup-new-worktree; path=/Users/afanasyev/Development/axon-showcase-auto-setup-new-worktree;
branch=auto-setup-new-worktree; writer=ses_e6f985602ffeUDJ4DtV0zmSDfc

## 1. The spec delta

- [x] 1.1 Write `specs/showcase/quality/ide-config/spec.md` as a `MODIFIED` block for "The Python analysis interpreter
      is the one the build runs", copying the current block whole (description and all six scenarios in their order) and
      extending the description with the new-tree inheritance.
- [x] 1.2 Add the scenario pinning the inherited selection, at the end of the requirement.
- [x] 1.3 Record the Purpose determination on this task: whether the change falsifies
      `openspec/specs/showcase/quality/ide-config/spec.md`'s Purpose. Outcome: no refresh is owed — the classes are
      unchanged, and `design.md` carries the determination.

## 2. The procedure

- [x] 2.1 `AGENTS.md`'s `Workspace` section gains the preparation step: after a tree is created — a new unit's or a
      continued unit's — and its path and branch verified, prepare it when the primary carries `.idea/`; readiness on
      the Python stage's `CONFIGURED`, with the skill owning the inheritance and the failure cases.
- [x] 2.2 `.opencode/skills/setup-idea/SKILL.md` gains the initialization section: the inheritance rule, not overwriting
      a tree's own selection, the no-primary-selection case, the outcome recording, and that manual trees are out of
      scope.
- [x] 2.3 `README.md`'s Python-analysis bullet states the inheritance ahead of the setup run; the skill's frontmatter
      `description` covers the initialization mode.

## 3. The pre-write pass and the claims table

- [x] 3.1 Run the pre-write derivation pass and record its claims table in `design.md`, seeded with
      `python3 scripts/claim-scan.py` and its dismissals. Outcome: the table and the four per-kind dismissals are in
      `design.md`, and every row reproduces.
- [x] 3.2 Record `AGENTS.md`'s applied net delta on this task after the last edit. Outcome: `+5/−0` lines.

## 4. Verification

- [x] 4.1 Run `./gradlew spotlessApply` after the last edit, then `spotlessCheck`.
- [x] 4.2 Run `openspec validate --all` and confirm the delta validates, scenario preservation included.
- [x] 4.3 Run the Docker-free `check` (`./gradlew check -PskipITs -Pcoverage.gate.enabled=false`).
- [x] 4.4 Re-run the procedure by hand in a disposable tree: copy the primary's `.python-version`, run the setup, and
      confirm the Python stage reports `CONFIGURED` with the inherited interpreter. Outcome: `CONFIGURED`, the module
      references `~/.pyenv/versions/3.12.16/bin/python3`; the probe tree was removed.

## 5. The unit's record

- [x] 5.1 Write `docs/process-defects/auto-setup-new-worktree.md`, or state that none was observed. Outcome: one record
      — the trigger model was written into three guidance sites before it was settled.

## 6. The review gate

- [ ] 6.1 Run the `review-quick` loop and append each round's row to `docs/review-rounds.tsv`; both ledger checks pass,
      and `--expect-clean` is green at the closing round.
- [ ] 6.2 Record the deep-review routing outcome on this task: run `scripts/review-routing.py` with `--unit` and
      `--change` set to this change's name. Outcome: `RECOMMENDED` on the `[repeated-class]` signal; the advisor
      recommended skipping and the owner agreed — no deep pass for this unit.
- [ ] 6.3 Ask the owner for the manual review pass and end the turn there.

## 7. Ship

- [ ] 7.1 After the owner's approval: commit, push, open the pull request, wait for `build`, archive, and merge.
