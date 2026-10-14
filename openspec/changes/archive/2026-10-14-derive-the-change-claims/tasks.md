# Tasks

## 0. The pre-write derivation pass (this change carries its own record and claims table)

- [x] 0.1 Ran the pre-write derivation pass and recorded its output as the claims table in `design.md` — each fact this
      change states about its evidence, with the source or the deriving command — and confirmed every row re-runs to its
      figure.

## 1. The pass's output: the claims table

- [x] 1.1 Stated it in `AGENTS.md`'s review-loop bullet: the pass's output is the change's claims table, its rows seeded
      from the recurring classes `docs/review-rounds.tsv` records.
- [x] 1.2 Added the `design` rule carrying the claims table and reworded the `tasks` rule to name the pass's output; the
      rule parses (a clean `openspec new change`), and the known-bad arm (an injected unquoted `: `) makes the file
      unparseable — the CLI warns and ignores it (`must be an array of strings, ignoring this artifact's rules`).
- [x] 1.3 `openspec/config.yaml` is not the only surface: `.github/PULL_REQUEST_TEMPLATE.md`'s pass line records the
      claims table for a unit with no change dir.

## 2. Pass instances, not only classes

- [x] 2.1 In `AGENTS.md`'s review-loop bullet, the caller passes each prior finding's instance (`file:line` and the
      claim) from the prior round's report, not only its class.
- [x] 2.2 In `.opencode/agent/review-quick.md`, the instructions list gains the claims-table check, and the repeat
      bullet distinguishes an unfixed prior instance from a new instance of an exhausted class.

## 3. Capture the behavior in the spec

- [x] 3.1 The delta for `showcase/quality/agent-skills`: a `MODIFIED` block carrying the requirement's description and
      all nine scenarios, with the two clauses added to the quick-review scenario.

## 4. Docs the change owns

- [x] 4.1 Read `README.md`'s self-learning bullet and the `agent-skills` `## Purpose`: no edit owed — the bullet already
      names the pre-write pass's record, and the Purpose describes the subagent inventory, not the pass's output.

## 5. Verification

- [x] 5.1 Re-ran every claims-table row with its filter: five units running 1–13 rounds; `documented-numbers` and
      `contradiction` 15 each; the compliance misses 20/18/13/7/0; 24 distinct classes; the six recurring classes.
- [x] 5.2 `./gradlew spotlessApply` then `spotlessCheck`, `openspec validate --all` (27 passed, 0 failed), and the
      manual 120-character check over the changed non-formatter files (`openspec/config.yaml` clean).
- [ ] 5.3 Run a `review-quick` pass over the implementation, passing the prior round's instances.
- [x] 5.4 Ran the per-unit `lesson-capture`; applied its two clause merges (the documented-numbers clause now excepts a
      count that IS the evidence, with a self-excluding source; the loop's escalation sentence gains delete-and-shrink).
      Applied net `AGENTS.md` delta: +10 lines (the reflow included).
- [ ] 5.5 Request the user's manual review pass.
