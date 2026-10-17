# Tasks

## 1. The spec delta

- [x] 1.1 `specs/showcase/quality/agent-skills/spec.md` — the `MODIFIED` block copies the main spec's
      `### Requirement: Per-change quality-gate and analysis subagents are available` verbatim (its description and all
      nine scenarios, in the main spec's order) and adds the session-reuse `AND` clause to "Quick review runs after
      proposal and implementation", beside the instance-hand-off clause. Verify with `openspec validate --changes` that
      no scenario is dropped or renamed. **Note**: no `## Purpose` refresh is owed — the Purpose describes the
      capability's scope, not the loop's session behaviour (read at proposal time). **Done** — the delta validates and
      the reviewer diffed it line-for-line against the main spec (round 4).

## 2. The docs

- [x] 2.1 `AGENTS.md` — the review-loop clause's reuse sentence becomes an imperative (the caller continues the
      reviewer's session across rounds, passing its session id; fresh only on expiry) that points at the spec for the
      rationale, replacing the permission wording. Re-run `./gradlew spotlessApply`. **Done.**
- [x] 2.2 `docs/ideas.md` — remove the "Give the persistent-reviewer-session rejection a durable home" entry this change
      implements (and the `## 2026-10-16` section if it empties); leave the 2026-10-15 "rejected levers" entry, whose
      other levers this change does not touch. **Done** — the section keeps its two other entries.

## 3. Verification

- [x] 3.1 `openspec validate --all` passes; `./gradlew spotlessApply`/`spotlessCheck` pass; the manual 120-character
      check (the `Formatting` convention's `perl` recipe) is clean over the changed set (the change dir is untracked, so
      the set comes from `git status --porcelain --untracked-files=all`, not `git diff --name-only`). **Done** —
      `openspec validate --all` 27/27; `spotlessCheck` green; the only >120 lines are the claims-table rows (which
      Prettier does not wrap) and the `docs/review-rounds.tsv` record rows (one line by contract) — both exempt.
- [x] 3.2 Run the `review-quick` loop over the proposal and the implementation, repeating until clean — and, because
      this change specifies it, run the loop as the clause says: continue one reviewer session across the rounds by
      passing its `sessionID`, starting fresh only if it expires. Record each round in `docs/review-rounds.tsv`. **Round
      1 (proposal) — 3 classes, all fixed**: the Why named a third surface ("the per-unit loop task") that names no
      artifact and carries no claims row; it over-claimed that reuse "does not happen" where the record shows a prior
      session reusing one; and it called the delta's scenario a "requirement" at two sites. **Round 2 — 1 class, all
      fixed (same session)**: the claims table's "No operational surface states the reuse" row was broader than its
      source and contradicted by the `AGENTS.md` row beside it; narrowed to the two surfaces the artifacts name. **Round
      3 — 1 class (wording), repeating, all fixed (same session)**: the Decision's "the status quo that did not happen"
      clause did not say what it meant; reworded to the Goal's "may overlook it" form. **Round 4 — 3 classes, all
      repeating, all fixed (fresh session — the prior one hit the model input limit, the clause's expiry exception)**:
      the tasks record still said "same session" for a round that had to start fresh; the Impact still stated the
      unconditional "instead of starting a new one"; and the Non-Goals stated the rejected levers with no claims row.
      The three repeats named the root cause — statements written from memory — so they were re-derived from the clause
      and the 2026-10-15 entry in one sweep. **Round 5 — 1 class, all fixed (same session)**: the round-3 narrative
      filed that finding as over-claim where the record and reports class it wording; reconciled to wording. **Round 6 —
      1 class (contradiction), repeating, all fixed (same session)**: the round-3 narrative still attributed its repeat
      to round 1's over-claim where the record repeats `wording`; the attribution was re-derived from the tsv row and
      dropped. **Round 7 — nothing remains (same session): the proposal-stage loop closed.** Rounds 1-3 reused one
      session; rounds 4-7 another (round 1's hit the model input limit — the clause's expiry exception), so the loop
      exercised both the reuse and the expiry path. **Round 8 (implementation) — 3 classes, 2 repeating, all fixed (same
      session)**: the AGENTS.md pointer claimed the behaviour was not restated while the bullet restates it imperatively
      (narrowed to the rationale); the task ticked this loop closed before the implementation rounds ran (unticked —
      this one); and the 120-char claim omitted the tsv rows, while the claims table's `AGENTS.md` row quoted the
      pre-change clause without a `git show HEAD:` anchor. **Round 9 — 1 class (wording), repeating, all fixed (same
      session)**: the pointer's "Its rationale" bound the pronoun to the record rather than the rule; named the
      referent. **Round 10 — nothing remains (same session): the implementation-stage loop closed.** **Done.**
- [x] 3.3 Ask the owner for the manual review pass, after 3.2 is clean. **Asked** — the request is in the session; the
      outcome is the owner's.

## 4. Capture

- [x] 4.1 Run the `lesson-capture` subagent over the diff, apply its durable proposals, and record the applied net
      `AGENTS.md` delta on this task. **Done — the capture's verdict is nothing durable (0 additions, 0 retirements, net
      0 `AGENTS.md` lines).** The session-expiry frequency is rationale the delta's own clause holds (recorded in
      `design.md`'s Risks); the claims-row `git show HEAD:<path>` anchoring and the from-memory root cause are already
      in the pre-write bullet (the round-8 miss of the former is recorded as one of its 2 compliance misses); and the
      remaining candidates are single non-severe occurrences the loop's existing control caught.

## 5. The change's own records

- [x] 5.1 Record the pre-write derivation pass as a task (every unit owes it). It ran: the claims table is in
      `design.md`, its rows derived from the main spec's requirement block, the greps for `reuse`/`session`, the
      `subagent` tool's description, `AGENTS.md`'s clause and #604, and the two `docs/ideas.md` entries. **Done.**

## Workflow follow-up

- Archive the change after the owner approves; its archive commit rides this PR.
