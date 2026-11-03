# Unit: persist-review-round-findings

Workspace:
`unit=persist-review-round-findings; path=/Users/afanasyev/Development/axon-showcase; branch=persist-review-round-findings; writer=ses_e5fa0fb2dffe0pwcgRMG3WCl2N`

State: closed

The unit's plan and tasks live in `openspec/changes/persist-review-round-findings/`.

## Resume checkpoint

- **Time and revision** — 2026-11-03, `git rev-parse HEAD` = `7130f687` (the branch's base; nothing committed).
- **Workspace** — the `Workspace:` record above: path `/Users/afanasyev/Development/axon-showcase`, branch
  `persist-review-round-findings`, writer `ses_e5fa0fb2dffe0pwcgRMG3WCl2N`.
- **Stage** — implementation complete and its tasks 1.1–4.4 ticked; the review gate is at the deep review, which
  finished pass 1 with three findings unfixed. Quick rounds 1–3 are clean at round 3, and the routing decision is
  `REQUIRED` (`requirement-block-added` on the commit-hygiene delta).
- **Next step** — apply the three findings from deep pass 1: `F1` add a task for the archive-time refresh of
  `showcase/quality/commit-hygiene`'s `## Purpose` (it enumerates the checks and does not name the new one); `F2` make
  `find_instance_violations` reject a post-boundary round whose instances file carries neither an entry nor `- none`,
  with a control; `F3` add the `see Amendment — 2026-11-03` pointer to ADR-0026's "no fixed filename" sentence. Then
  thorough pass 2, task 4.6 (re-derive `FINDING_INSTANCES_THROUGH` at landing), and the manual pass.
- **Change set** — nothing staged, nothing committed. Modified: `scripts/commit-hygiene.py`,
  `scripts/test-commit-hygiene.py`, `build.gradle.kts`, `.opencode/skills/review-gate/SKILL.md`,
  `.opencode/skills/pause-resume-unit/SKILL.md`, `docs/contracts/process-bindings.md`,
  `docs/adr/0026-process-bindings-are-declared.md`, `docs/ideas.md`. Untracked:
  `docs/process-defects/persist-review-round-findings.md`,
  `docs/units/persist-review-round-findings/{unit.md,rounds.tsv}`, and the change dir's seven files.
- **Blockers and open decisions** — every round this unit records is dated on the boundary day, so the store has no live
  entry and the manual pass sees its behaviour through the tests only; the class comparison equates class sets, as the
  delta states, so placeholder locations and claims pass. Neither is a defect against the delta.
- **Pending decision** — none from the owner. The manual-pass approval is the gate's stop and has not been requested
  while the deep review is open.
- **Findings no record holds** — deep pass 1: `F1` the `commit-hygiene` Purpose is under-described after this change and
  no task carries its archive-time refresh; `F2` an empty instances file is accepted for a clean round, so the delta's
  "state that the round raised none" is unenforced; `F3` ADR-0026's "no fixed filename" sentence has no inline amendment
  pointer, unlike the one beside it.
- **Operations in flight** — none.
