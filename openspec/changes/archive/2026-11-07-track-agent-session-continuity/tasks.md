# Tasks

## 1. Plan

- [x] 1.1 Run the owner-authorized probe and record its result — the same-parent continuation, the cross-parent refusal,
      the unknown-identifier refusal, and continuing a main session by its identifier — in
      `docs/units/track-agent-session-continuity/unit.md` under `## Plan probe`. **Done** — the record carries both
      identifiers, the exact error strings, and the marker each continuation returned.

## 2. The record and its gate

- [x] 2.1 `scripts/commit-hygiene.py`: add the `sessions.tsv` record's constants (basename, the thirteen field width,
      the registered agent set, the outcome and transition sets), add `parent-session-changed` to
      `SESSION_REPLACEMENT_REASONS`, and add the `--agent-sessions` mode implementing the delta's rules — field width
      and order, the unit column, agent membership, pass contiguity per `(unit, agent, scope, opinion)`, the transition
      and reason rules, the unresolved-row rule, the `mismatch` rule, the main-session parentage consistency, and the
      hand-over slug resolved against `review-handover.md`. Verification:
      `python3 scripts/commit-hygiene.py --agent-sessions` exits `0` on the empty register. **Done** — the mode runs and
      exits 0 on the unit's register; `parent-session-changed` is in the constants.

- [x] 2.2 `scripts/test-commit-hygiene.py`: add a known-bad and a known-good case per rule 2.1 adds — a wrong field
      count, a row filed under another unit, `review-quick` as the agent, a duplicated and a gapped pass, a first row
      that is not `created`, a same-identifier row without `continued`, a different identifier without `replaced`, an
      unknown reason, a reason on a row whose transition is not `replaced`, a `report` row without a returned
      identifier, a `report` row returning another requested identifier, a `created` row naming a requested identifier,
      a `continued` row without the resolved one, a `replaced` row carrying one, a `stalled`/`failed` row carrying a
      returned one, a `mismatch` row without a requested or a returned identifier, a transition on a `stalled`,
      `failed`, or `mismatch` row, a `continued` row from another main session, a `parent-session-changed` row from the
      same one, a `mismatch` whose identifiers agree, and a slug with no matching heading. Verification:
      `python3 scripts/test-commit-hygiene.py` passes with each new case reaching its assertion. **Done** — the register
      class's cases pass beside the existing suite.

- [x] 2.3 `scripts/commit-hygiene.py`: add `sessions.tsv` to `UNIT_LAYOUT_FILES`. Verification: a fixture unit holding
      an unadmitted path still fails, and one holding `sessions.tsv` passes, in `--unit-layout`'s tests. **Done** —
      `UNIT_LAYOUT_FILES` carries `sessions.tsv`; the layout tests pass.

- [x] 2.4 `scripts/commit-hygiene.py` `check_staged`: refuse a staged `sessions.tsv` whose `--agent-sessions`
      verification fails, judging the index's content, refuse a staged record that removes a row the committed record
      carries, and refuse one whose committed prefix differs from the committed record's rows, so a rewritten row is
      caught. Verification: `scripts/test-commit-hygiene.py` carries the removal, the rewrite, and the violation cases.
      **Done** — the staged block runs the mode and the committed-prefix check; the removal, rewrite, and violation
      cases pass.

- [x] 2.5 `build.gradle.kts`: register `verifyAgentSessionContinuity` as an `Exec` task over
      `scripts/commit-hygiene.py --agent-sessions` with `scripts/commit-hygiene.py` and `docs/units` as inputs and
      `outputs.upToDateWhen { false }`, and add it to `check`. Verification: `./gradlew verifyAgentSessionContinuity`
      succeeds, and `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` lists it. **Done** —
      `verifyAgentSessionContinuity` runs in `check` (BUILD SUCCESSFUL).

- [x] 2.6 `scripts/git-hooks/pre-commit`'s header comment: add the agent-session record to the enumerated refusals.
      Verification: the header names it, and the hook still runs `--staged` unchanged. **Done** — the header names the
      agent-session register's refusals.

- [x] 2.7 `scripts/claim-scan.py`: treat `sessions.tsv` as a data record beside `rounds.tsv` (`DATA_RECORD`), so its
      date, pass, and identifier columns are not claim candidates. Verification: `scripts/test-claim-scan.py` gains a
      case for the new basename beside the existing one. **Done** — `DATA_RECORD` covers both basenames; 58 claim-scan
      cases pass.

## 3. The procedure, its commands, and the declaration

- [x] 3.1 `docs/contracts/process-bindings.md`: add the `agent-session-record` role (`docs/units/<unit>/sessions.tsv`,
      owned by the commit-hygiene format requirement) and the consumer-trace rows for `review-gate`, `review-thorough`,
      `consult`, `pause-resume-unit`, and the checker's non-adopter row. Verification: every component that reads a
      session resolves the role, and the non-adopter list names the tooling that embeds the path. **Done** — the
      `agent-session-record` role and its consumer and non-adopter rows are in the declaration.

- [x] 3.2 `.opencode/skills/review-gate/SKILL.md` §2: generalize the preflight to every registered invocation — resolve
      the row, compare the returned identifier, write the hand-over before the call, append the row, and take the
      replacement rules from the register's spec — and replace "The deep review's hand-over stays `/review-thorough`'s"
      with the unit-file rule while keeping `/review-thorough` as its fields' owner. Verification: the preflight names
      one procedure for the quick and registered agents, and no second copy of the reason set. **Done** — the preflight
      covers every registered invocation and the deep hand-over line is replaced.

- [x] 3.3 `.opencode/commands/review-thorough.md`: replace step 2 with resolving the pass's session from the register
      (create, continue, or replace with an evidenced reason and the owner's approval where discretionary); write the
      hand-over section before the invocation; record the pass's row after it. Verification: the command's steps name
      the register, the hand-over, and the row, and the pass budget is unchanged. **Done** — step 2 resolves the
      register and writes the section before the call.

- [x] 3.4 `.opencode/commands/consult.md`: resolve a unit-bound consultation's session from the register by decision and
      opinion, record the invocation, keep an outside-unit consultation unrecorded, and keep an independent second
      opinion as its own conversation. Verification: step 4 and step 7 name the register and the opinion, and the
      outside-unit contract is unchanged. **Done** — steps 4 and 7 resolve the register, keep the outside-unit
      consultation unrecorded, and keep a second opinion its own conversation.

- [x] 3.5 `.opencode/skills/pause-resume-unit/SKILL.md`: add the checkpoint field naming the register rows the next step
      continues; state the same-main-session choice and its consequence in the pause report; add the resume step that
      continues a row whose main session matches and records `parent-session-changed` for one that does not, invoking
      nothing speculatively. Verification: the field set gains one row, and the resume steps name the register, the
      returned-identifier check, and the forced replacement. **Done** — the field set gains the sessions row, the pause
      report states the choice, and the resume steps continue or record.

## 4. Documents and the sweeps

- [x] 4.1 `README.md`: state the same-main-session choice in the pause/resume paragraph, the recorded deep-review and
      consultation sessions in the review paragraph, and name `verifyAgentSessionContinuity` in the check list.
      Verification: no sentence still says a pause always continues in a new session. **Done** — the pause, review, and
      check-list paragraphs are updated.

- [x] 4.2 `AGENTS.md`: read the review-gate, pause/resume, agent-tooling, and Formatting bullets for a sentence the
      change falsifies — the review-thorough command bullet, the pause skill bullet, the session/OpenCode bullets, and
      the unwrappable-record exception, which names `rounds.tsv` and now also covers `sessions.tsv` — and update only
      what the change makes wrong. Verification: `git diff AGENTS.md` shows no restated procedure, and the exception
      names both record files. **Done** — the unwrappable-record exception names both files; no other root sentence was
      falsified.

- [x] 4.3 The prohibited-content sweep the new requirements demand: read every artifact this change creates for a
      quick-reviewer row, an outside-unit consultation row, a restated hand-over field list, or a per-unit record header
      the register requirement forbids, and remove any it finds. Verification: `grep` over the changed set for
      `review-quick` in the register's documents and for a second field list beside each command's contract. **Done** —
      the sweep found no admitted quick-reviewer row, no outside-unit consultation row, no second hand-over field list,
      and no per-unit header.

- [x] 4.4 The reason-set sweep: `grep -rn "stopped-responding\|parent-session-changed\|definition-changed"` over the
      changed set and every live enumeration of the reasons, so the record-format requirement owns the recorded values
      and each policy requirement names only its own permitted cases. Verification: each hit is a policy list, the
      recorded-value owner, or the checker's constant — never a second copy of the values presented as permission.
      **Done** — each hit is the policy owner, the format owner, or the tooling; the command no longer enumerates the
      values.

- [x] 4.5 Record this unit's own registered invocations: create `docs/units/track-agent-session-continuity/sessions.tsv`
      and add the Plan-stage consultation's row — agent `technical-advisor`, its decision slug, opinion `primary`, the
      returned identifier `ses_e4ad78a6fffe24kQF6NBeqrZfu`, `created`, and the hand-over slug — together with that
      section in the unit's `review-handover.md`, which states that it was written after the invocation and that its two
      reports are its source. Append the deep pass's row when 5.8 runs. Verification:
      `python3 scripts/commit-hygiene.py --agent-sessions` passes on the record, and the section's heading carries its
      slug. **Done** — the register carries the three consultation rows and the hand-over carries their three sections;
      `--agent-sessions` passes.

## 5. Verification and the review gate

- [x] 5.1 `openspec validate --all` passes, with both deltas at `specs/showcase/quality/...`. **Done** —
      `openspec validate --all`: 27 passed, 0 failed.

- [x] 5.2 `python3 scripts/test-commit-hygiene.py` passes. **Done** — the suite passes, its cases beside the existing
      ones.

- [x] 5.3 `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` passes, including `testCommitHygiene`,
      `verifyAgentSessionContinuity`, and `verifyUnitLayout`. **Done** —
      `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`: BUILD SUCCESSFUL.

- [x] 5.4 `./gradlew spotlessApply` then `spotlessCheck` pass after the last edit. **Done** — `spotlessApply` and
      `spotlessCheck` pass with the last edit.

- [x] 5.5 Run the pre-write derivation pass and re-derive the claims table in `design.md`, including the unit's own
      appended rows. **Done** — the table is re-anchored to `40088b3c` and its rows reproduce there.
- [x] 5.6 Run the proposal quick review and fix every finding until it reports no new observations. **Done** — rounds
      1–8 closed clean after the advisor's corrections.
- [x] 5.7 Run the implementation quick review and fix every finding until it reports no new observations. **Done** —
      rounds 9–14 closed clean, including the base refresh's round.
- [x] 5.8 Derive the deep-review routing decision with `python3 scripts/review-routing.py`, record it on the review
      task, and run the pass it selects. **Done** — the routing returned `REQUIRED` on two added requirement blocks;
      pass 1 ran in `deep-1` and raised four findings (B1 blocking, S1, S2, N1); the owner authorized the fixes, and
      pass 2 verified all four `FIXED_AND_VERIFIED`.
- [x] 5.9 Ask the owner for the manual review pass and end the turn there. **Done** — the implementation's surface was
      presented for the owner's manual pass, with approval pending.
- [x] 5.10 Write `docs/units/track-agent-session-continuity/process-defects.md`, or state that none was observed.
      **Done** — the record carries `#1` and `#2`.

## 6. Archive

- [x] 6.1 Refresh the `showcase/quality/commit-hygiene` Purpose in the archive commit — it enumerates the verification
      subjects and now gains the agent-session record — record the deferral here, and tick this task in the archived
      `tasks.md`. **Done** — the Purpose names the agent-session register's continuity and hand-over links.
- [x] 6.2 Run `openspec archive track-agent-session-continuity --yes` and stage both the change-dir move and the
      `openspec/specs/**` sync. **Done** — archived as `2026-11-07-track-agent-session-continuity`; both sides are staged with this commit.
