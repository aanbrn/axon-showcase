# Tasks — require owner authorization for a unit's start and its implementation

The unit's record is `docs/units/require-owner-authorization-for-start-and-implementation/unit.md`.

## 0. The pre-write pass

- [x] 0.1 The review gate's pre-write derivation pass ran, seeded with `scripts/claim-scan.py`; each candidate is
      resolved into the claims table below or dismissed beside it. Dismissals, one reason per kind: `absolute` — the
      flagged `no`/`never`/`only`/`all` words are the proposed rule's own statements and the design's, each backed by
      the row its subject has; `count` — the figures are the table's rows; `cross-ref` — the flags are the change's own
      paths and identifiers; `path` — the paths are this change's artifacts and the files it plans to edit.

## 1. The start gate

- [x] 1.1 `AGENTS.md`, "Workflow: Change lifecycle" → Invariants: add the design's three bullets — the start gate with
      its authorization routes and its "discussion is not authorization" rule, the proposal's manual pass with the
      conditioned single-pass route, and the writable-scope bullet (the unit's planning artifacts and
      `docs/units/<unit>/` records; no implementation target, no live rule file).
- [x] 1.2 `AGENTS.md`, the same section's Procedure: renumber and reshape to the design's eight steps, so step 1 is the
      authorization, steps 4–5 are the proposal stage and its manual pass, and steps 6–8 are the implementation, its
      pass, and the unchanged delivery request.

## 2. The pass at each boundary

- [x] 2.1 `.opencode/skills/review-gate/SKILL.md` §5: state that the pass runs at each boundary a unit has, naming
      `AGENTS.md`'s review-gate invariant as the owner of the single-pass route and its condition, and apply the
      existing preconditions to the stage it closes.
- [x] 2.3 `AGENTS.md`, "OpenSpec Workflow Agreement" → Invariants, the "The review gate covers every unit of work…"
      bullet: amend its mapping ("There is no proposal/implementation/archive vocabulary for those, so map the rule onto
      what exists") so the single-pass route applies only where the owner's authorization itself names each direct edit
      the unit may make — the condition §5 points at, corrected at its owner rather than restated elsewhere.
- [x] 2.2 `AGENTS.md`, "Workflow: The review gate" → Stages: the manual-review-pass bullet names both boundaries (the
      design's wording), so the root entry point asks at each one.

## 3. The command definitions

- [x] 3.1 Update the thirteen project-authored command definitions whose "Repository lifecycle" block starts a unit that
      ships — `apply-audit-reductions`, `audit-agents`, `audit-architecture`, `audit-readme`, `audit-specs`, `diagram`,
      `flag-process-defect`, `generalize-process-defects`, `gradle-update`, `ideas`, `opsx-tool-update`,
      `retrospective`, `sync-branch`; the set is the sixteen `Repository lifecycle` blocks minus `consult`,
      `pause-unit`, and `resume-unit`, which start no unit — adding the owner's authorization for the unit's start and
      their approval of its proposal before any implementation edit.

## 4. The specification

- [x] 4.1 The change's delta: an `ADDED` requirement for the start gate and the proposal stop, with scenarios, and a
      `MODIFIED` block on "Project-authored command definitions that start a mutating unit carry the lifecycle
      preflight" adding the two stops to what a command states.

## 5. Withdraw the parked entry

- [x] 5.1 `docs/ideas.md`: remove the `## 2026-11-04` entry, since the change implements it (`verifyIdeasSections` fails
      an empty section, so the heading goes with it).

## 6. Verify

- [x] 6.1 The rule names every unit kind the lifecycle covers — change, docs unit, audit application, dependency bump —
      and no procedure step begins work before the authorization.
- [x] 6.2 `openspec validate --all` passes; `./gradlew spotlessApply` and `spotlessCheck` run after the last edit; the
      applied `AGENTS.md` net delta is measured with `git diff --numstat` and a replacement of the existing lifecycle
      wording is preferred to an appended layer, so the file does not grow materially.
- [x] 6.3 The review gate runs over the change; the unit's process-defect record exists. Routing `REQUIRED`
      (`requirement-block-added`); the deep review ran and raised one should-fix (a `ship-change` step cross-reference
      stale after the renumbering), fixed and re-screened clean.

## Claims table

Each source is read at the unit's base revision `66069e0e`, so the rows keep reproducing after this change edits the
file they name.

| Claim                                                                       | Source or command                                                                                                                                     | Result                                                                                                                                           |
| --------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------ |
| The lifecycle's procedure has five steps and begins at "Propose the change" | `git show 66069e0e:AGENTS.md \| sed -n '/^\*\*Procedure\*\*/,/^\*\*Decision points\*\*/p' \| head -15 \| tail -13 \| grep -c "^[0-9]\."`              | `5` steps; step 1 is "Propose the change"                                                                                                        |
| The review gate's manual pass is stated as the implementation stage's       | `git grep -n "at the implementation stage" 66069e0e -- .opencode/skills/review-gate/SKILL.md`                                                         | `211` — §5's precondition list — and `336` in the completion criteria                                                                            |
| The specification states the approval stop once, for shipping               | `git grep -c "manual approval pass" 66069e0e -- openspec/specs/showcase/quality/agent-skills/spec.md`                                                 | `1`, in the mutating-command requirement                                                                                                         |
| Thirteen command definitions start a unit that ships                        | `git grep -l "Repository lifecycle" 66069e0e -- .opencode/commands \| wc -l`, less the three that start none (`consult`, `pause-unit`, `resume-unit`) | `16` blocks, `13` starting a unit that ships                                                                                                     |
| The parked entry for this rule is the only one in its section               | `git show 66069e0e:docs/ideas.md \| sed -n '/^## 2026-11-04/,/^## /p' \| grep -c '^- '`                                                               | `1` entry                                                                                                                                        |
| A unit with no separate proposal stage already maps to one pass             | `git grep -n "map the rule onto what exists" 66069e0e -- AGENTS.md`                                                                                   | one hit, `AGENTS.md:170`, in "OpenSpec Workflow Agreement" → "### Invariants" — the bullet beginning "The review gate covers every unit of work" |
