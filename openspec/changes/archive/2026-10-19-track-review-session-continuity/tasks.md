# Tasks

## 1. Record the pre-write derivation pass

- [x] 1.1 Run the pre-write derivation pass over the planning artifacts: read each claim about a prior artifact from
      that artifact, re-derive every count against the set it counts, single-source each rule stated more than once, and
      derive each mechanism claim from the tool it names. Record the result as the claims table in `design.md`, seeded
      with `scripts/claim-scan.py` and carrying its by-class dismissals beside it.

## 2. The rule in the capability specs

- [x] 2.1 `openspec/changes/track-review-session-continuity/specs/showcase/quality/agent-skills/spec.md` — a `MODIFIED`
      block for "Per-change quality-gate and analysis subagents are available", copied whole from
      `git show HEAD:openspec/specs/showcase/quality/agent-skills/spec.md` and edited in place, preserving the block's
      other scenarios and their order. The delta is the record of what the clause now says, so this task points at it
      rather than restating its content.
- [x] 2.2 `openspec/changes/track-review-session-continuity/specs/showcase/quality/commit-hygiene/spec.md` — an `ADDED`
      requirement recording the ledger's reviewer-session continuity, with one scenario per shape the checker rejects
      and per shape it must accept. The delta is the record of what the requirement says, so this task points at it
      rather than restating its content.
- [x] 2.3 `openspec/specs/showcase/quality/commit-hygiene/spec.md` — refresh the capability's `## Purpose` in the
      archive commit, since it enumerates the guards the capability's `check` verifies and this change adds one. A delta
      cannot carry a Purpose, so this is the change's one direct main-spec edit.

- [x] 2.4 `openspec/changes/track-review-session-continuity/specs/showcase/quality/agent-skills/spec.md` and `AGENTS.md`
      — strengthen the preflight from its intended outcome to the action: a fresh invocation on the first round, the
      saved identifier passed as the invocation's session-continuation parameter on every later round, a comparison of
      the returned identifier with the saved one (a difference stopping the round as a session-continuity failure), and
      a replacement recorded with both identifiers. The ledger's fields and validation are unchanged. **Done** — the
      delta's clause, its new sibling clause and the procedure's preflight bullet all name the action, and the design's
      Decision records why the ledger check alone cannot see how a session was obtained.

## 3. The ledger's contract

- [x] 3.1 `docs/review-rounds.tsv` — extend the header comment with the three fields, their allowed values, and the
      normalization the identifier is compared under, and state that they are validated from the boundary comment down.
- [x] 3.2 `docs/review-rounds.tsv` — add the boundary comment marking the first row to carry the fields, immediately
      above this change's first row.
- [x] 3.3 `docs/review-rounds.tsv` — append this change's own rows with the session fields filled from the actual
      invocation each round performed (see task 7.1).

## 4. The checker

- [x] 4.1 `scripts/commit-hygiene.py` — add `--session-continuity`: read the tracked ledger, require the boundary
      marker, and reject a row below it whose field count is not the contract's width, that carries no session
      identifier, whose transition is outside the three, that repeats its predecessor's identifier without a `continued`
      transition, whose identifier differs without a `replaced` transition, whose `replaced` transition has an absent or
      unlisted reason, that carries a reason without a replacement, or whose first row of a unit is not `created`; and,
      above the marker, a row whose field count is not the pre-fields width. Name each violation with its line, its
      unit, and its round.
- [x] 4.2 `scripts/commit-hygiene.py` — register the mode in the argument group, dispatch it in `main`, and describe it
      in the module docstring.
- [x] 4.3 Read the mode's output over the real ledger and over a scratch fixture carrying one row of each rejected
      shape, and confirm it names every one. **Done** — a scratch ledger with one row per branch exits 1, naming every
      branch.

## 5. The tests

- [x] 5.1 `scripts/test-commit-hygiene.py` — add a case per behaviour the checker rejects and per behaviour it must
      accept, each over a fixture ledger in a temporary directory. The rejected set mirrors task 4.1's shapes, and the
      accepted set covers a created first row, consecutive rounds sharing a session, a padded identifier naming the same
      session, an allowed replacement, the boundary exemption, and distinct units starting with distinct sessions.
- [x] 5.2 Confirm each case fails against the pre-change behaviour and passes against the change: revert the checker's
      continuity function to a stub that returns no violations and confirm the known-bad cases go red, then restore it.

## 6. The check task

- [x] 6.1 `build.gradle.kts` — register `verifyReviewSessionContinuity` (an `Exec` running
      `commit-hygiene.py --session-continuity`, with the script and the ledger as inputs and
      `outputs.upToDateWhen { false }`), and add it to `check` beside `verifyUniqueReviewRounds`.
- [x] 6.2 `build.gradle.kts` — confirm the task runs standalone and through `check` and reports the same result in both.
      **Done** — `verifyReviewSessionContinuity` exits 0 standalone and as a `check` member over the same ledger.

## 7. The documents

- [x] 7.1 `AGENTS.md` — add the reviewer-session preflight to the review-gate procedure, immediately before the
      `review-quick` invocation and inside the fix-and-re-run loop: create and retain the identifier on the unit's first
      round, load it before every later round, continue that session by default, replace it only for a recovery case
      with the reason recorded first, and append each round's row with the ledger's session fields supplied from the
      invocation performed. Point at the capability spec and the ledger for the rule, its recovery cases, and the
      fields; name the failing check.
- [x] 7.2 `AGENTS.md` — reduce the session decision bullet to the pointer and the imperative it must still carry, so the
      recovery cases are stated once, in the capability spec.
- [x] 7.3 `AGENTS.md` — add `verifyReviewSessionContinuity` to the `check` note's member list.
- [x] 7.4 `README.md` — add the verification to the prose paragraph that names its siblings and to the command list;
      re-align that list's comment column to its new longest member. **Done** — the prose names it, the command list
      carries it, and the column moved one character for all 19 lines.
- [x] 7.5 `.opencode/agent/review-quick.md` — state in the report contract that the caller appends the session fields
      from the invocation it performed, and that the reviewer must not invent an identifier.

## 8. The parked idea

- [x] 8.1 `docs/ideas.md` — remove the `## 2026-10-18` entry this change implements, and widen the `## 2026-09-24`
      test-coverage entry's list of checks whose requirement carries no coverage clause, since this change adds one of
      them. **Done** — the entry is gone and the list names `verifyReviewSessionContinuity`.

## 9. Verification

- [x] 9.1 `./gradlew spotlessApply` then `spotlessCheck`.
- [x] 9.2 `./gradlew testCommitHygiene verifyReviewSessionContinuity verifyUniqueReviewRounds verifyCapturedMarkers`.
- [x] 9.3 `openspec validate --all` and `openspec validate --changes`.
- [x] 9.4 Run the continuity validation over the real ledger and confirm it passes, then over a scratch copy with one
      field removed and confirm it fails — read the output of each run. **Done** — the real ledger exits 0, and a
      scratch row with its identifier removed exits 1 naming the line, the unit, and the round.
- [x] 9.5 Run the 120-character check over the changed files no formatter owns (`scripts/*.py`).
- [x] 9.6 `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` (the pull-request gate), or the touched tasks where
      that is unavailable. **Done** — `BUILD SUCCESSFUL`, re-run after the deep review's fixes so the tick reflects the
      final state.

## 10. Review gate

- [x] 10.1 Run the `review-quick` loop — proposal round then implementation round — and record each round's row in
      `docs/review-rounds.tsv` with the session it ran in and its transition. **Done** — every round is in
      `ses_eb03e0bc3ffeoefevnvoZ89bEg`: round 1 `created`, every later round `continued`, no replacement; the loop first
      reported `nothing remains` at round 14, and the rounds after the deep review (which the routing made `REQUIRED`)
      found and cleared the residues that fix left.
- [x] 10.2 Derive the deep-review routing decision with
      `python3 scripts/review-routing.py --change track-review-session-continuity`, decide at or above the floor, record
      the outcome and its signals here, and run a `REQUIRED` review before completion or record a `RECOMMENDED` one's
      explicit resolution. **Done** — the derivation reports `REQUIRED` on the `[requirement-block-added]` signal (the
      `commit-hygiene` delta's `ADDED` requirement), so the deep review runs before completion; its session and findings
      are recorded here as they arrive. **Deep review (session `ses_eb004d139ffejzz33ZJ52V8Jr3`): 2 findings, both
      fixed.** Its should-fix was that the boundary marker's _position_ was not enforced — only its presence — so moving
      the marker down past a nine-field row silently exempted it; the checker now rejects a row above the marker whose
      field count is not the pre-fields width, with a paired case and a delta scenario, and the Risks state the
      invariant. Its nitpick was that "the digest … [is] unaffected" overstated it, since the digest re-emits whole
      rows; the claims row and the proposal now say the digest's figures and the routing tool's reads are unaffected,
      and that the echoed lines carry the new fields.
- [x] 10.3 Run the per-unit `lesson-capture` decision: run the `lesson-capture` subagent unless the skip condition holds
      in full, apply its durable proposals, and record the applied net `AGENTS.md` delta or the skip and its reason
      here. **Done** — the skip condition did not hold (seven round classes beyond the local-implementation set, and a
      diff touching `AGENTS.md`, `.opencode/`, a check and the delta specs), so the subagent ran (session
      `ses_eafee9431ffeuNavcm4dBrxGA1`) and proposed **three merges, no retirements, one authoring-system
      recommendation**. Applied: the self-record bullet's remedy is widened from "the diff" to the source a record
      names; the edit-script bullet's scope drops "to a commit" and gains the assert-the-anchor clause; and the
      conservative-check bullet gains the exemption-boundary rule. The authoring-system recommendation was applied to
      `openspec/config.yaml`'s `design` claims-table rule, which carried the same over-narrow "derived from the diff".
      The unit's own `AGENTS.md` diff retires the session decision bullet's rule statement (task 7.2), so the capture's
      additions are offset by that reduction; the file's net is the number `git diff --numstat AGENTS.md` reports.
- [ ] 10.4 Ask the user for the manual review pass of the uncommitted diff. Do not commit, push, open a pull request,
      archive, or merge before that approval.
