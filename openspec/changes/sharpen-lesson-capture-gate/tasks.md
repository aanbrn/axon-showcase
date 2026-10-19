# Tasks — the capture's gate compares the decision, not the wording

## 1. The refined gate

- [x] 1.1 `.opencode/agent/lesson-capture.md` — the inputs name the existing guidance and the review evidence, with the
      withhold-when-unavailable rule. **Done** — the "You are given" list adds the unit's round rows and the existing
      guidance a candidate would extend, and the paragraph after it withholds an unsupported candidate.
- [x] 1.2 `.opencode/agent/lesson-capture.md` — the increment rule compares the existing obligation, its explicit
      condition, and the different action, and keeps the repository-backed example; evidence-only candidates are
      `already covered`; the replacement preserves obligations, warnings, exceptions, and identifiers and corrects only
      verified errors. **Done** — the rule is a bold-lead list with one actionable statement per bullet.
- [x] 1.3 `.opencode/agent/lesson-capture.md` — the gate separates a repeated observation from its cause and bounds the
      remedy to the verified evidence. **Done** — the cause rules are their own bold-lead list (recurrence, the
      confirmed-shared-cause condition for a remedy resting on it, the different-causes branch), and the increment rule
      derives the action and scope from the verified evidence.
- [x] 1.4 `AGENTS.md` — the capture workflow's step 2 passes the round rows and the existing guidance the gate's input
      rule requires; no gate text is copied into `AGENTS.md`. **Done** — `AGENTS.md` `git diff --numstat` measures
      `5 4`, net +1 line.
- [x] 1.5 `openspec/changes/sharpen-lesson-capture-gate/specs/showcase/quality/agent-skills/spec.md` — copy the current
      "Per-change quality-gate and analysis subagents are available" requirement whole and add the corresponding
      clauses, keeping every existing scenario in order. **Done** — the block is 414 lines against the main spec's
      402-line requirement, and `diff` against `sed -n '140,540p'` shows only the intended clause hunks.

## 2. Validate the revised instructions

- [x] 2.1 The pointer-source addition from `add-extraction-procedure` resolves to `already covered`. **Done** — the
      comparison names the existing source-derivation obligation and its condition, and the pointer-target bullet
      governs the opposite direction, so no action changes.
- [x] 2.2 The recurring-class addition separates the observed recurrence from the asserted cause and the prescribed
      rewrite. **Done** — recurrence is an observation; a remedy resting on a class's recurrence requires the shared
      cause confirmed; different causes take the cause-specific shape.
- [x] 2.3 A distinct preventive action with sufficient evidence still passes. **Done** (hypothetical) — the threshold is
      unchanged and a preventive guard names no inference from a class's recurrence.
- [x] 2.4 A lesson caused by a template or command is routed to the producing mechanism. **Done** — the
      mechanism-routing rule and the requirement's routing scenario are unchanged.
- [x] 2.5 A capture with no durable additions is a completed outcome. **Done** — the `already covered` branch says
      "propose nothing" and the verdict line carries `nothing durable (net 0)`.

## 3. Pre-write derivation and formatting

- [x] 3.1 `python3 scripts/claim-scan.py` over the authored set, each candidate a claims-table row or a by-kind
      dismissal, re-derived from its source. **Done** — ten rows plus five by-kind dismissals (path, count,
      absolute-quantifier, cross-reference, verbatim-copied spec text).
- [x] 3.2 `./gradlew spotlessApply`, `./gradlew spotlessCheck`, `openspec validate --all`, and the 120-character check
      over the changed set. **Done** — Spotless clean; `openspec validate --all` 27/27; the 120-character recipe flags
      only the pre-existing verdict-line sample and one pre-existing Gradle command. The commit-hygiene gates
      (`verifyReviewSessionContinuity`, `verifyUniqueReviewRounds`, `testCommitHygiene`, `testReviewRounds`,
      `testReviewRouting`, `testClaimScan`) pass with the ledger's five new rows.

## 4. Review gate

- [x] 4.1 `review-quick` against the proposal and implementation, each round's row in `docs/review-rounds.tsv` with the
      session identifier the invocation returned; fix every finding. **Done** — one reviewer session
      (`ses_eab8879a0ffeHbQ5BXDJ0Q02cU`), created on round 1 and continued to round 5; each round's row carries the
      returned identifier. Round 1 raised ten, round 2 two, round 3 one, round 4 one, round 5 clean; every instance is
      fixed.
- [x] 4.2 Record the deep-review routing decision derived from the change's own facts. **Done** —
      `python3 scripts/review-routing.py --change sharpen-lesson-capture-gate` returns `NOT_NEEDED`; no deep-review
      session was created.
- [x] 4.3 The `lesson-capture` subagent ran, or the skip condition held in full with its reason recorded, with the
      applied net `AGENTS.md` delta on this task. **Done** — the subagent ran (the diff touches an authoring surface and
      the rounds recorded classes beyond the local set). Verdict: 0 durable proposals (0 additions, 0 retirements), 1
      authoring-system recommendation, net 0 `AGENTS.md` lines. Its recommendation — the gate should confirm a rule
      excludes the incident that produced it — is the parked `docs/ideas.md` 2026-09-22 entry, whose recorded trigger is
      a second occurrence or the next retrospective; it is not applied in this unit, which exists for another purpose.
      Applied net `AGENTS.md` delta for this capture: **0** lines (the step-2 clause is the unit's own edit, +1 line).
- [x] 4.4 The edited `lesson-capture` definition's smoke-run is the pre-archive gate — reload OpenCode, then run it
      against the change's own cases, recording which branch each exercised: the pointer-source addition must resolve to
      `already covered` with the compared obligation and condition, the recurring-class addition must return the
      recurrence with the cause withheld, and the known-explained `AGENTS.md` class-rewrite bullet must not be
      re-proposed. **Done** — after the restart the registered body carries the new tokens (`opencode debug agents`).
      Run 1 (no guidance text supplied) exercised the withhold branch: candidate A was withheld for indeterminate scope.
      Re-seeded with the existing guidance text supplied, run 2 exercised all three named branches — A resolved to
      `already covered`, naming the source-derivation and pointer-target obligations and the unchanged action; B
      returned the `claim-unreproducible` recurrence with the cause withheld and no section rewrite; C (the
      known-explained class-rewrite bullet) was reported `already covered`, not re-proposed. No retirements, no
      authoring-system recommendation, net 0.

## 5. Manual review and archive

- [x] 5.1 Ask the owner for the manual review pass. **Done** — requested in the unit's report; the work stays
      uncommitted until the owner answers.
- [ ] 5.2 Archive only after the owner approves; not part of this unit's uncommitted state.
