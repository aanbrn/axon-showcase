# Unit: gate-review-handover-section

Workspace:
`unit=gate-review-handover-section; path=/Users/afanasyev/Development/axon-showcase; branch=gate-review-handover-section; writer=ses_e4960ba02ffeSVwO2K6w3UICYO`

State: active

A change unit: its planning artifacts live under `openspec/changes/gate-review-handover-section/`.

The owner's authorization (2026-11-07) approves the unit as the first live trial of the agent-session mechanism
`track-agent-session-continuity` introduced (#862): a quick-review hand-over presence guard, with no change to the
register's schema and no unification of histories. The trial plan is the technical advisor's, agreed with the owner:

- a consultation on the guard's scope and its overlap with the register's existing slug check, before the pause;
- a pause at that boundary, and a resume in a new main session with the writer hand-over;
- the follow-up consultation under the same decision and opinion, which exercises the `parent-session-changed` path;
- a deep review the owner explicitly requests, run through the normal procedure rather than manufactured by the routing
  floor.

The unit record also carries the trial's purpose: the advisor's pause plan requires a registered invocation before the
pause and a warranted invocation under the same key after the resume, since a pause alone proves only a controlled case.

## Resume checkpoint

- **Time and revision** — 2026-11-07, `HEAD` `55edb7a6` (the branch is forked from `origin/main` there); nothing
  committed.
- **Workspace** — the `Workspace:` line above.
- **Stage** — planning. The initial consultation (`handover-guard-scope`, `advisor-scope-1`) returned and its register
  row is recorded; the proposal, design, tasks, and the delta are drafted from its scope; the follow-up consultation
  (assessment of the proposal, same decision) and the proposal quick review are next.
- **Next step** — in a new main session, run the follow-up consultation under `handover-guard-scope`/`primary`. That
  session cannot continue the advisor child, so it records a `parent-session-changed` replacement and starts the
  follow-up from `advisor-scope-1`; then revise the proposal from its assessment and run the proposal quick review.
- **Change set** — `docs/units/gate-review-handover-section/{unit.md,review-handover.md,sessions.tsv}` and
  `openspec/changes/gate-review-handover-section/{.openspec.yaml,proposal.md,design.md,tasks.md,specs/...}`; all
  uncommitted, the unit's files staged.
- **Blockers and open decisions** — none.
- **Pending decision** — none: the resume request itself is the writer hand-over confirmation.
- **Findings no record holds** — none; no review round has run.
- **Operations in flight** — none.
- **Agent sessions** — `technical-advisor` / `handover-guard-scope` / `primary` pass 1, session
  `ses_e4969fe3effelQc5YYYHfxYF9E`; the next step continues that decision under the same key, and a new main session
  must record `parent-session-changed` rather than attempt the continuation.
