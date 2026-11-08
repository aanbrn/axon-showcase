# Tasks

The unit record — its `Workspace:`, state, and pause checkpoint — is
`docs/units/name-the-session-after-its-unit/unit.md`.

## 1. Implementation

- [x] 1.1 `AGENTS.md`: add one imperative sentence to the lifecycle's Workspace rules — a session's title is the name of
      the unit it is working on, set by the `begin-unit` and `pause-resume-unit` steps, whose conditions are the
      `showcase/quality/agent-skills` requirement's. Verification: the rule carries the imperative and points at the
      requirement, restating none of its conditions.
- [x] 1.2 `.opencode/skills/begin-unit/SKILL.md`: add a step that sets the current main session's title to the unit's
      established name — the name of its record directory at `docs/units/<unit>/` — after the workspace and record
      exist, the applicable claim step has run (a change-named branch may omit the claim), and before the concluding
      report, including when the session previously worked on another unit. The step names the mechanism
      (`opencode_session_rename`), awaits the rename, reads the title back, reports a failure without blocking the unit,
      and honours an explicit owner instruction to keep a different title. Verification: the step names its position,
      the name's source, the mechanism, the readback, the non-blocking failure, and the owner-override condition, and it
      does not require a claim a change-named branch may omit.
- [x] 1.3 `.opencode/skills/pause-resume-unit/SKILL.md`: add the same step after the unit is verified and any writer
      handover or legacy migration is done and before the work continues; it names the record directory as the name's
      source, names the mechanism (`opencode_session_rename`), awaits the rename, reads the title back, reports a
      failure without blocking the unit, and honours the owner-override condition. State at the stop branches that an
      unresolved attachment leaves the title unchanged. Verification: the step names its position, those conditions, and
      the unresolved-attachment rule.
- [x] 1.4 Confirm the delta is at the mirrored capability path
      `openspec/changes/name-the-session-after-its-unit/specs/showcase/quality/agent-skills/spec.md` and that every
      normative clause of its requirement names an implementation site: the Begin and Resume steps from tasks 1.2–1.3,
      the `AGENTS.md` pointer from task 1.1, and — for the clauses that no session outside a unit is renamed and that a
      pause or settlement keeps the last title — the absence of any other rename site. Verification:
      `grep -rn "session_rename" AGENTS.md .opencode/agent .opencode/commands .opencode/skills` returns exactly the two
      steps that 1.2 and 1.3 added; those two matches are the control, so an empty result fails as a vacuous run and a
      third match is inspected as a possible rename site. The requirement the steps reference resolves on the branch to
      the change's delta and, after the archive commit, to the main spec. `openspec validate --all` passes.
- [x] 1.5 `docs/ideas.md`: the parked IntelliJ-configuration entry rides this branch because the collision surfaced
      while this unit's Begin prepared its tree. Verification: the entry is present under its dated section and
      `scripts/commit-hygiene.py --ideas-sections` passes.

## 2. Verification

- [x] 2.1 `openspec validate --all` passes with the delta at the mirrored path.
- [x] 2.2 `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` passes.
- [x] 2.3 `./gradlew spotlessApply` then `spotlessCheck` pass after the last edit.
- [x] 2.4 Run the pre-write derivation pass and record the claims table in `design.md`, re-derived after the last edit.
      It records the persistence probe run on 2026-11-08 on disposable session `ses_e45fe96e3ffe7cosCoyndiX5pj` — a
      rename applied, was read back, and survived a further prompt turn; re-run the probe only if the harness's rename
      behaviour is in doubt.

## 3. Review gate

- [x] 3.1 Run the proposal quick review and fix every finding until it reports no new observations.
- [x] 3.2 Run the implementation quick review and fix every finding until it reports no new observations.
- [x] 3.3 Derive the routing decision with `python3 scripts/review-routing.py`, record it, and run any selected deep
      review through the normal procedure. - **Done** —
      `review-routing.py --unit name-the-session-after-its-unit --change name-the-session-after-its-unit` returned
      `REQUIRED` on `[requirement-block-added]` for the delta. Deep-review pass 1 (session
      `ses_e45b28fb8ffeao3f5E1jtYBFca`, recorded as `deep-1`) reported 0 findings, so no fix-verification pass was owed
      and no finding carries a status.
- [x] 3.4 Ask the owner for the manual review pass and end the turn there.
- [x] 3.5 Write `docs/units/name-the-session-after-its-unit/process-defects.md`, or state that none was observed.

## 4. Archive

- [x] 4.1 Refresh the `showcase/quality/agent-skills` Purpose in the archive commit if this requirement falsifies it,
      and tick this task in the archived `tasks.md`.
      - **Done** — the Purpose inventories the capability's components (skill sets, procedures, the lifecycle,
        the standards and contracts, the subagents, the commands, MCP recovery) and states no behaviour this
        requirement contradicts, so no refresh is owed.
- [x] 4.2 Run `openspec archive name-the-session-after-its-unit --yes` and stage both the change-dir move and the
      `openspec/specs/**` sync.
