# Tasks

Workspace: unit=pause-resume-unit; path=/Users/afanasyev/Development/axon-showcase-pause-resume-unit;
branch=pause-resume-unit; writer=ses_e88bc904dffeKGky0FHVVIFNaq

## 1. The skill

- [x] 1.1 Author `.opencode/skills/pause-resume-unit/SKILL.md`: frontmatter (`name`, and a single-clause `description`
      containing no colon), `Trigger`, the pause `Procedure`, the resume `Procedure` with both of its branches — a unit
      that carries a checkpoint, and one left unfinished without one — `Decision points`, `Failure handling`, and
      `Completion criteria`, per `design.md` D1 and D3–D7, D10 — verified by reading each decision against the section
      that implements it and by `./gradlew spotlessCheck`.
- [x] 1.2 State the checkpoint's field set **once** in the skill — the observed time and revision, the workspace record
      it belongs to, the stage, the one next step, the staged/unstaged/untracked split, the blockers and temporary
      decisions, the owner's pending decision, the prior finding instances no permanent artifact holds, and each
      in-flight operation with its identifier and unknown outcome — and have every other artifact point at it — verified
      by grepping `AGENTS.md`, both commands, `README.md`, and the delta spec for a second enumeration.
- [x] 1.3 Carry the review gate's boundary as a pointer: the finding-instance handover is `review-gate`'s rule and the
      round ledger is `commit-hygiene`'s, and a pause starts no replacement reviewer session — verified by grepping the
      skill for the gate's rule name and by confirming it restates neither rule.
- [x] 1.4 State the resume's pack step as a pointer to the pack-loading requirement rather than a restatement of it, and
      give the resume's two verification branches: the available state (workspace records and the live tree) always, and
      the comparison with the checkpoint only when one exists, where a stale record is reported as stale and an absent
      one as unrecovered context rather than as a mismatch, and where a decision the unit awaited is restored unless the
      continuation request answers it, a bare request answering nothing — verified by reading the resume procedure
      against `design.md` D5 and D7.
- [x] 1.5 Make the writer handover the resume's first write, after the state check and independently of a checkpoint:
      compare the session with the record's `writer=<ses_…>`; when they differ, take the owner's confirmation that the
      prior writer has ended — carried by the continuation request when it says so, asked for otherwise, and never read
      out of a bare resume request — record this session as the writer, and only then write work in the tree; when the
      record names no writer, or a handover is required and its confirmation is not given, stop for the owner's decision
      writing no work, and say that no build or container need be stopped — verified by reading it against `design.md`
      D10 and `docs/adr/0024-tree-write-right-is-not-session-bound.md`, and by confirming it claims no mechanical check.

## 2. The triggers

- [x] 2.1 Add `.opencode/commands/pause-unit.md`: the `/pause-unit` trigger with its `Repository lifecycle` block
      stating that it starts no unit, creates no branch or tree, and changes no delivery state, and a completion
      criterion that the checkpoint is written and re-read — verified by comparing the block against
      `.opencode/commands/consult.md`'s precedent and by `./gradlew spotlessCheck`.
- [x] 2.2 Add `.opencode/commands/resume-unit.md`: the `/resume-unit` trigger for continuing any unfinished unit, from
      the session that left it or a fresh one, pointing at the skill's resume section and naming the writer handover as
      the step before any work write — conditional on the writer differing, never on the invocation alone — verified by
      the same reading and by confirming it states that it starts no new unit and points establishing the workspace at
      `AGENTS.md`'s rule.
- [x] 2.3 Confirm neither command owes the lifecycle preflight the mutating-unit requirement states, and record that
      reading in `design.md` D2 so its absence is not read as an omission — verified against
      `openspec/specs/showcase/quality/agent-skills/spec.md:1324`.

## 3. Root entry point and documentation

- [x] 3.1 Add one bullet to `AGENTS.md`'s "Workflow: Change lifecycle" **Workspace** block: the skill's path, that it
      loads before a pause and before continuing an unfinished unit, and that a missing skill stops the step — no
      procedure step at root — verified by grepping the bullet and by confirming no ordered pause or resume step exists
      in `AGENTS.md`.
- [x] 3.2 Add the two rows to `README.md`'s slash-command table and its development-workflow prose naming both pausing a
      unit and returning to one left unfinished — verified by reading the table's column count and the prose, and by
      `./gradlew spotlessCheck`.
- [x] 3.3 Re-derive the changed artifact set: grep `AGENTS.md`, `README.md`, `openspec/specs/`, `.opencode/`, and
      `openspec/config.yaml` for the lifecycle wording this change moves and for the word `checkpoint`, and fix every
      live site the change leaves stale — verified by the hits and their dispositions recorded in `design.md`.
- [x] 3.4 Confirm no `docs/ideas.md` entry covers the procedure, so the change removes none, and record the grep that
      establishes it in the task — verified by `grep -n -i "pause\|resume\|checkpoint\|interrupt" docs/ideas.md`
      returning only the round-checkpoint, the OpenSearch, and the 2026-10-23 program usages.
- [ ] 3.5 Refresh the capability's `## Purpose` in the archive commit, adding `pause-resume-unit` to the
      project-authored skills it names by member (a delta cannot carry a Purpose, so the edit is deferred and rides the
      archive commit) — verified by reading the refreshed Purpose against `ls .opencode/skills` and by
      `openspec validate --all`.

## 4. Gates

- [x] 4.1 Run `./gradlew spotlessApply`, then `spotlessCheck` and `openspec validate --all` — verified by their exit
      status and by reading the formatter's output for skipped files.
- [x] 4.2 Verify each new definition's frontmatter survives formatting: a `name`/`description` block with no unquoted
      `: ` at a wrapped line start, re-read after `spotlessApply` (the readme-auditor incident's failure mode) —
      verified by reading both frontmatters and the skill's.
- [ ] 4.3 Confirm the new definitions are reachable as far as the local binary allows: `opencode debug agents` resolves
      subagents only, `opencode debug config` prints a source list rather than a resolved document, and v2.1.4 has no
      `opencode debug skill`, so the resolved-inventory assertion is the CI probe's (v1) and is owed by this change's
      own pull-request `build` run, whose diff gate includes `.opencode/` — verified by recording the local output, its
      difference from the probe's shape in `design.md`'s claims table, and the run's log.

## 5. Review, capture, and hand-over

- [x] 5.1 Record the pre-write derivation pass and its claims table in `design.md`, seeded from
      `python3 scripts/claim-scan.py --base origin/main` with one disposition per candidate — verified by the table's
      rows against the commands they name and by the candidate list's dispositions (paths planned rather than existing,
      and the two `only` absolutes resolved into the corrected Why sentence and D3).
- [x] 5.2 Run `review-quick` over the change and fix its findings until it reports none: retain the reviewer session per
      the gate's preflight, append each round's row to `docs/review-rounds.tsv`, and run the ledger checks the gate's §2
      requires after each append — verified by the ledger rows and by those checks' exit status.

      **Loop:** one reviewer session (`ses_e887d6412ffe34Dnx2J2EQUQuX`) across 17 rounds, rows appended as each report
      arrived; `--unique-rounds --expect pause-resume-unit:<round>`, `--session-continuity`, and the closing
      `--expect-clean pause-resume-unit` all exit 0. Round 17 closed `nothing remains`, after the capture's record and
      its reverted addition.

- [x] 5.3 Record the deep-review routing decision from `python3 scripts/review-routing.py --change pause-resume-unit`,
      and run the routed deep review when it selects one — verified by the recorded outcome and, for a `REQUIRED` or
      owner-resolved `RECOMMENDED` outcome, the pass's recorded status.

      **Routing:** `REQUIRED`, signal `requirement-block-added` (the change adds requirement blocks to
      `showcase/quality/agent-skills`).

      **Deep review pass 1** (initial review, session `ses_e880db07effeWwFVEo18MBW3Sc`, `3 findings`, all nitpick):
      - nitpick, `FIXED_AND_VERIFIED` — the delta's requirement 1 says `AGENTS.md` carries "only" the trigger, path, and stop rule,
        while its own bullet also carries the scope clause; the review-gate precedent enumerates what the root carries
        and prohibits only the steps.
      - nitpick, `FIXED_AND_VERIFIED` — the skill's resume step 4 records the new writer without naming where, while the pause side
        names the record explicitly; the `Workspace:` record is the field's home.
      - nitpick, `FIXED_AND_VERIFIED` — `resume-unit.md` and design D2 say the command "creates no branch or tree", while a fresh
        session continuing a unit takes a tree on the existing branch per `AGENTS.md`'s Workspace rule; the block now
        points workspace establishment at that rule.

      **Deep review pass 2** (fix verification, same session): `1 finding`, nitpick — task 2.2's verification clause still
      said the resume command "creates no branch or tree", the claim pass 1's F3 removed; `FIXED_AND_VERIFIED` (the clause
      now checks the command's pointer form). The three pass-1 findings are `FIXED_AND_VERIFIED`.

- [x] 5.4 Run the `lesson-capture` subagent over the diff and round rows and apply its durable proposals, or record the
      skip and its reason — verified by the capture record written in this task list.

      **Capture:** ran (session `ses_e87fb6fddffeco4CPpaI9b8w3D`): `1 durable proposal, 1 authoring-system
      recommendation, 0 retirements`. Applied the recommendation, routed to its mechanism: `openspec/config.yaml`'s
      `rules.tasks` gains a clause that a normative clause prohibiting content carries its own task, reading every
      artifact the change creates for the prohibited content (+3 lines), which is not a restatement of the
      trace-every-clause step — a prohibition has no implementation site. Dismissed the `AGENTS.md` proposal as
      `already covered`: the "Doc claims must match their source and their strength" gotcha states "an unconditional rule
      must hold on every documented path, not only the default one", `review-gate` §2 names the site ("a delta
      requirement's description beside its scenarios") and already carries the `openspec validate` sentence, and the
      input-naming clause is the gate's handover rule applied (the instances live in the report; a context-losing caller
      writes them to a handover). Review round 16 reported the addition as `single-sourcing`, and it was reverted.
      Corrected the proposal's evidence list: the reconciliation instances are rounds 6 and 7, the deep pass's "only"
      finding, and round 13's completion criterion — round 3 was the claims table's `resumable state` term and the
      absence row. Also recorded as already covered: the absence-claim recurrence (the claims-table rule), the two
      requirements without an implementation site (change step 5), the residue-in-commands fix (the replace-the-whole-unit
      rule), and the stale `tasks.md` 2.2 repetition (the corrected-fact sweep). Net `AGENTS.md` delta from the capture:
      `0` lines (the unit's own +5 is the entry-point bullet); `openspec/config.yaml` `+3`.

- [ ] 5.5 Satisfy the gate's §5 preconditions, its boundary ledger invocation included, then ask the owner for the
      manual review pass and stop before any commit, push, pull request, archive, or merge — verified by the turn ending
      on that request.

## Workflow follow-up

- Archive the change after the owner's approval and a green `build` check, in the same pull request; that run's probe
  log is the reachability verification task 4.3 defers.
- No parked follow-up: the change closes the gap it opened, and `docs/ideas.md` gains nothing.
