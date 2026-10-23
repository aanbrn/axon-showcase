# Tasks

Each task is one implementable chunk with its verification. The ordered sections are a gate: the skill must exist and
load before any root text is deleted, and the references must be retargeted before the change is reported done.

## 1. Create the review-gate skill

- [x] 1.1 Create `.opencode/skills/review-gate/SKILL.md` with frontmatter (`name: review-gate`, a `description` stating
      the trigger — load it before the review gate's first stage) and a `Source:` line quoting `AGENTS.md` "Workflow:
      The review gate". Verification: `head -12` shows both frontmatter fields and the `Source:` line, and
      `test -f .opencode/skills/review-gate/SKILL.md` exits 0.
- [x] 1.2 Write the five stages in order — pre-write pass and claims table; quick loop; routing decision; deep review;
      manual-pass request — and move each stage's steps, decision points, failure handling, completion criteria, and the
      identifiers its steps name from the root section, carrying each `captured:` marker with the rule it records and
      placing it at that rule's end rather than on a plain bullet. Verification: each stage heading is present, the
      identifiers `scripts/claim-scan.py`, `scripts/review-routing.py`, `commit-hygiene.py --unique-rounds --expect`,
      `docs/review-rounds.tsv`, and `/review-thorough` each occur in the skill, and each marker keeps its identity, not
      only a count: a list pairs every moved rule with the marker it carried, the same rule carries the same marker in
      the skill, and the skill's markers plus the root's equal the pre-change section's 41.
- [x] 1.3 Move the non-converging-loop failure handling and the procedural decision points (prior finding instance,
      reviewer-session continuation, scope sweep on a behaviour-adding finding) into the skill. Verification: each is
      present in the skill and absent from the root section.
- [x] 1.4 Confirm the skill needs no registration beyond its directory and frontmatter: a fresh session lists
      `review-gate` among the available skills, and `Read .opencode/skills/review-gate/SKILL.md` returns its body.
      Verification: the session's skill list and the returned body.

## 2. Reduce the root to the entry point

- [x] 2.1 Replace the body of `AGENTS.md`'s `### Workflow: The review gate` with the entry point: the trigger, the five
      stage names with the owner each delegates to, the authorization stops (a clean quick review is not approval; no
      commit, push, PR, archive, or merge before the owner's pass), the load-before-the-first-stage instruction, and the
      missing-skill stop. Verification: the section carries no numbered step and no `**Procedure**` block, and
      `git grep -n 'review-gate/SKILL.md' -- AGENTS.md` returns the pointer.
- [x] 2.2 Move the single-sourcing decision point verbatim into `### Invariants` and delete it from the gate's section.
      Verification: `git grep -c 'Single-source a claim' -- AGENTS.md` returns `1`, on a line inside `### Invariants`.
- [x] 2.3 Retarget the `## Conventions` reference that names the single-sourcing rule's old heading to `### Invariants`.
      Verification: `git grep -n 'single-sourcing rule under' -- AGENTS.md` names the new heading.
- [x] 2.4 Account for every line of the pre-change section: run a containment check over
      `git show HEAD:AGENTS.md | sed -n '344,545p'` against the skill, the new root entry point, and `### Invariants`,
      classifying each non-blank line as moved-to-skill, retained-at-root (the entry point or `### Invariants`), or
      deleted-with-reason. Verification: no line is unclassified; every deletion is a duplicated prose clause the entry
      point replaces; and each retained-at-root line is a trigger, a stage name, or a stop the entry point states, or a
      line of the single-sourcing rule moved into `### Invariants` (2.2). The obligation trace is the semantic check for
      the amended scenario text, not for root lines: its no-destination rule keeps a clause in the scenario, while a
      root line is classified against the skill, the entry point, and `### Invariants`.

## 3. Retarget the references the move breaks

- [x] 3.1 Re-derive the site list with `git grep -l 'Workflow: The review gate' -- .opencode AGENTS.md` and record it.
      Verification: the list holds the five command definitions the design's claims table names, `AGENTS.md`, and the
      skill's own `Source:` line, and nothing else.
- [x] 3.2 Retarget each command definition so it names the `review-gate` skill as the owner of the steps instead of
      `AGENTS.md` — one edit in each of `.opencode/commands/apply-audit-reductions.md`, `audit-agents.md`,
      `audit-architecture.md`, `audit-readme.md`, and `audit-specs.md`. Verification:
      `git grep -n 'Workflow: The review gate' -- .opencode/commands` returns nothing — the pattern is proved against a
      known match first (it matches the five files before the edits) — and each file names the skill.
- [x] 3.3 Re-run the derivation and classify every remaining match: the skill's `Source:` quote, the heading itself, or
      the change-lifecycle step pointing at the surviving entry point. Verification: no command and no live reference
      names `AGENTS.md` as the owner of the steps.

## 4. Delta and capability records

- [x] 4.1 Assert the delta carries the removed requirement's scenario set: a script compares the `#### Scenario:` names
      of `git show HEAD:openspec/specs/showcase/quality/agent-skills/spec.md` (requirement block 174–610) with the
      delta's names and reports `HEAD`-only names, which must be none, and delta-only names, which must be the six the
      two ownership requirements add. Verification: the `HEAD`-only set is empty, the delta-only set is those six, and
      `openspec validate consolidate-review-gate-ownership --strict` passes. The name check is mechanical only;
      obligation preservation is the design's per-obligation trace, which the implementation review verifies.
- [x] 4.2 Record the capability `## Purpose` refresh as close-out work for the archive commit: the Purpose enumerates
      the project-authored guidance skills, which the new one widens. Verification: the task is named in this change's
      close-out and the edit lands with the archive commit. **Done**: the Purpose now names "the `review-gate` procedure"
      beside the `check-and-test-authoring` and `ship-change` skills, in this archive commit.

## 5. Docs the change causes

- [x] 5.1 Check `README.md` for a statement of where the review gate's procedure lives, and update it only if one
      exists. Verification: `git grep -n 'review gate' -- README.md` is read line by line, and each surviving line
      states behaviour rather than the procedure's location.
- [x] 5.2 Check `openspec/config.yaml`'s `context` and `rules` for a statement about the review gate's location, and
      update it only if one exists. Verification: `grep -n 'review' openspec/config.yaml` is read, and
      `openspec context --json` still returns a root.
- [x] 5.3 Park only the concrete gaps this change found, each in a dated section with its trailing status tag: the
      capture requirement's scenarios still restating the promotion gate their definition owns, and the diagram/vision
      scenarios duplicating their definitions. Reference the already parked audit-findings carry-forward idea by its
      date instead of parking it again, and do not list shipping as unowned — `ship-change` owns it. Verification: the
      two new entries appear once each under their dated section with their tags, grepped by their own leading text, and
      `git grep -c 'Carry findings forward across audit reports' -- docs/ideas.md` returns `1`.

## 6. Verification

- [x] 6.1 Run the lost-term sweep: extract the removed procedure's distinctive tokens (its commands, paths, and
      identifiers) and confirm each survives in the skill, the root entry point, or the owner it names. Verification:
      one `grep` per token, with a known-present control proving the pattern can match.
- [x] 6.2 Run `./gradlew spotlessApply` as the last formatting write, then `./gradlew spotlessCheck`. Verification: both
      exit 0, and `spotlessCheck` reports no skipped file.
- [x] 6.3 Run `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`. Verification: exit 0, including
      `verifyCapturedMarkers`, `verifyLargeFiles`, and `verifyModuleDependencies`.
- [x] 6.4 Run `openspec validate --all`. Verification: exit 0 with every spec and change valid.
- [x] 6.5 Run the retrieval smoke-run in a scratch clone over five routes, checking that the contract is reachable
      rather than that it was executed: a proposal review; an implementation review whose routing outcome is `REQUIRED`;
      a quick-review continuation carrying the prior session identifier and findings; a hand-over with missing inputs;
      and the stop before the owner's decision. Verification: each route's transcript names the skill's stage and the
      contract it needs, or reports the missing input or stops; a positive control loads the skill by name; a
      missing-skill control reports it and runs no stage; and the sandbox follows the bullet stating that a probe's
      sandbox is state — a fresh clone per fixture, with an assertion that nothing escaped.
- [x] 6.6 Run the manual 120-character check over the change's changed set, derived with
      `git status --porcelain --untracked-files=all`. Verification: the recipe's exit status over the listed set.
- [x] 6.7 Measure the mandatory read set per route, before and after: the review gate's caller; a reviewer subagent that
      does not orchestrate the gate; and a consultation that does not run the gate — counting unique mandatory files per
      route, with lines and bytes, never a token count, cost, or duration. Verification: all three before/after pairs
      are recorded in the design's risk row, showing each route's gain and the read the move adds to the caller's route.

## 7. Review and close-out

- [x] 7.1 Run the pre-write derivation pass and record its output as the design's claims table, re-derived after the
      change's last edit. Verification: each row's command reproduces the figure it records. **Done**: the table's rows
      were re-derived twice after the last edits that moved their figures (rounds 12 and 15); every row's stated command
      reproduces the figure it records.
- [x] 7.2 Run the `review-quick` loop to a clean round, appending each round's row to `docs/review-rounds.tsv` as its
      report arrives and running `python3 scripts/commit-hygiene.py --unique-rounds --expect <unit>:<round>` immediately
      after each append. Verification: the final round reports `nothing remains` and every checkpoint passes. **Done**:
      rounds 1–22 in one reviewer session; round 22 reports `nothing remains`, and each of the 22 checkpoints passed in
      the same turn as its append. One `check` run in the final sweep failed with no task name or message retained (the
      output was truncated before the failing task); two subsequent runs were green. The failure is recorded as
      unreproduced with no established cause, not as transient or fixed.
- [x] 7.3 Derive the deep-review floor with
      `python3 scripts/review-routing.py --change consolidate-review-gate-ownership`, decide at or above it, and record
      the outcome and its signals on this task. Verification: the recorded outcome names the signals it relied on.
      **Done**: `REQUIRED`, on `requirement-block-added` and `requirement-block-removed` (the delta replaces one
      requirement with six); the decision is taken at the floor, with no signal reducing it. **Deep review** (session
      `ses_e996cad8bffedwyIQCvQHK0D9O`): pass 1 (initial) raised 1 nitpick — the delta's `REMOVED` Reason named three
      subjects and omitted the routed deep review (`specs/showcase/quality/agent-skills/spec.md`, the Reason paragraph)
      — status `FIXED_AND_VERIFIED`: the Reason now names the four subjects its Migration enumerates, quick round 16
      re-reviewed the fix, and pass 2 (targeted verification, same session) reports `nothing remains`. No finding was
      deferred, rejected, or accepted.
- [x] 7.4 Make the `lesson-capture` decision: run the subagent and apply its durable proposals, or record the skip and
      its reason. Verification: the applied net `AGENTS.md`/pack delta, or the recorded skip and reason. **Done**: the
      capture ran; applied A1 (a table row is confirmed inside its block) and A2 (a split carries a per-obligation
      trace) to `AGENTS.md`, and the authoring-system recommendation to `openspec/config.yaml`'s `rules.design`; net
      `AGENTS.md` +10 lines / +999 bytes over the pre-capture state (2,585 → 2,595 lines; 248,991 → 249,990 B),
      justified because both rules are gate-invisible defects (`spotlessCheck` reflows the prose; validation compares
      nothing across `REMOVED`/`ADDED`) and the unit removed 144 lines from the file. The narrow destination retarget
      the extraction rule required was applied after the owner's explicit permission: `review-gate` added to the capture
      destinations in `AGENTS.md`, `lesson-capture.md`, and the delta, plus the record's artifact list, with no other
      capture change. Deferred: the independent capture consolidation (the requirement's restated gate and the record's
      hand-listed enumeration), parked in `docs/ideas.md` (2026-10-23). The delta's capture scenario also gained
      `review-gate` in its "existing rule it extends" list, and `docs/ideas.md` no longer calls all four capture
      scenarios verbatim. Routing smoke-run after the retarget (synthetic gate-procedure lesson, the updated contract
      read first): the capture placed the correction in `.opencode/skills/review-gate/SKILL.md` and named the skill's
      stage 4 as the rule it extends, not `AGENTS.md` or `check-and-test-authoring`; its `already covered` verdict on
      the synthetic premise is the probe's own judgment, not a routing result; that disk-read probe is superseded as
      evidence by the registered-definition run in 7.7.
- [x] 7.5 Ask the owner for the manual review pass and end the turn. Verification: the request is made, and nothing is
      committed, pushed, archived, or merged before the owner answers. **Done**: the pass was requested with the
      implementation, the review trail, the gates, the capture record, and the deferred items; nothing was committed,
      pushed, archived, or merged.
- [x] 7.7 Verify the registered `lesson-capture` definition after its reload routes a stage-procedure correction to the
      `review-gate` skill: a positive control (a verified gap in the skill's stage 2 — an unparseable quick-review
      invocation is not addressed) and a negative control (the already-covered stage-4 status case). Verification: the
      positive probe names `.opencode/skills/review-gate/SKILL.md` and no other destination; the negative probe reports
      `already covered` and proposes nothing. Archive is blocked until this closes. **Done**: both probes ran against
      the definition registered after the owner's reload, and each quoted the contract line naming "a stage-procedure
      correction in `review-gate`". Positive: the verified gap (stage 2 states no disposal for output that arrives
      unparseable, while the reviewer contract requires a verdict line and a record line) produced one proposal at
      `.opencode/skills/review-gate/SKILL.md` stage 2 and no other destination. Negative: the stage-4 status case
      returned `already covered` with no proposal, citing the stage-4 clause and the completion criteria. Archive gate
      closed.

- [x] 7.6 After the owner's approval, refresh the capability Purpose in the archive commit, then archive and ship per
      the `ship-change` skill. Verification: the archived change carries the Purpose edit, and the pull request shows
      both. **Done**: the owner approved on 2026-10-23; the capability Purpose is refreshed in this archive commit; the
      change is archived as `2026-10-23-consolidate-review-gate-ownership` and ships on the same pull request.
