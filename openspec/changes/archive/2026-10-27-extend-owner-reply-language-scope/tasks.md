# Tasks — extend the owner-reply language scope

`Workspace: unit=extend-owner-reply-language-scope; path=/Users/afanasyev/Development/axon-showcase; branch=extend-owner-reply-language-scope; writer=ses_e83f5e016ffeTdW9h7oO8TqFYO`

## 1. Planning record

- [x] 1.1 Record the review gate's pre-write derivation pass as the claims table in `design.md`, each row anchored to
      `git merge-base HEAD origin/main` (`50799b60b35dcbc6790b0a1c7ecf773bf73f4594`). Verify each row's command
      reproduces its stated result; re-derive the table after the unit's last edit to its own artifacts, and again if a
      branch refresh moves the base. **Done** — `design.md` carries the table and every row was re-run at base
      `50799b60`; two rows were corrected to base-pinned reads after their ranges failed to reproduce (record #1).
- [x] 1.2 Keep the delta at the mirrored path
      `openspec/changes/extend-owner-reply-language-scope/specs/showcase/quality/agent-skills/spec.md` and run
      `openspec validate --changes`; record the result. **Done** — `openspec validate --changes` passes for the change
      and `openspec validate --all` reports 27 passed, 0 failed; the delta sits at the mirrored path.

## 2. The convention

- [x] 2.1 In `AGENTS.md`'s output-language convention, widen the owner-reply sub-bullet so every assistant message the
      owner can see in the turn — the progress narration around tool calls included — follows the owner's request
      language. Verify `grep -n "progress narration" AGENTS.md` returns one hit inside that bullet, that the bullet
      keeps the explicit-request exception and the later-switch rule, and that its em-dashes are paired. **Done** —
      `AGENTS.md:1127-1129`; `grep -n "progress narration" AGENTS.md` returns one hit inside the bullet, which keeps the
      explicit-request exception and the later-switch rule, and its two em-dashes pair.
- [x] 2.2 Extend the relayed-report sub-bullet so a presented report's prose — headings, labels, and count descriptions
      — is translated into the owner's language while its structure, evidence, and uncertainty are kept. Verify
      `grep -n "count descriptions" AGENTS.md` returns one hit and that it sits in the same bullet as the
      structure-and-evidence obligation, not in a second bullet. **Done** — `AGENTS.md:1134-1135`;
      `grep -n "count descriptions" AGENTS.md` returns one hit in the same bullet that keeps the structure-and-evidence
      obligation.

## 3. The requirement

- [x] 3.1 Write a `MODIFIED` block for "Agent output language follows the output's audience" that copies the current
      requirement in full and adds the two scenarios — progress narration, and a presented report's translated prose —
      preserving the existing scenarios' relative order. Verify the block's header matches the main spec verbatim, that
      `grep -c "^#### Scenario:"` on the delta returns 10, and that `openspec validate --all` passes. **Done** — the
      delta header matches `openspec/specs/showcase/quality/agent-skills/spec.md:2469` verbatim, it carries 10 scenarios
      with the original eight in order, and `openspec validate --all` passes.

## 4. Verification

- [x] 4.1 Run `./gradlew spotlessApply`, then `./gradlew spotlessCheck`, after the last edit to any markdown file.
      **Done** — `spotlessApply` then `spotlessCheck` ran green after the last markdown edit.
- [x] 4.2 Sweep the live markdown outside the change directory for a second statement of the widened rule. Match the
      convention's phrases — `reply the owner reads`, `progress narration`, `relayed report keeps`, and
      `count descriptions` — plus the paraphrase category `owner-facing replies`, with
      `git grep -n "<phrase>" -- AGENTS.md README.md .opencode docs openspec/specs`. **Done** — the phrases hit only
      `AGENTS.md:1127`, `AGENTS.md:1134`, and `AGENTS.md:1135`, plus the main spec's "A relayed report keeps its
      contract" scenario title at `spec.md:2501`; the paraphrase hits only the Purpose at `spec.md:14`, which task 5.1
      rewrites. The direct reads return `README.md:366` (the summary) and `consult.md:53` (its step's imperative).
- [x] 4.3 Check `docs/` and `.opencode/` for a language statement the change makes stale
      (`grep -rn "owner-facing\|agent-facing\|language of the owner" --include='*.md' docs/ .opencode/`); correct any
      hit. **Done** — the hits are classification questions (`technical-advisor.md:56`, `consult.md:11,31`) and the
      unit's own record; no site restates the rule, so no correction was owed.
- [x] 4.4 Run the 120-character check over the changed markdown with the boundary-safe recipe
      (`perl -CSD -lne 'print if length > 120'`), treating a formatter skip as a failure for that file and a long
      Prettier-owned table row or the mandated single-line `Workspace` record as acceptable. **Done** — the over-long
      lines are Prettier-owned table rows and the `Workspace` record; the check also surfaced the three split
      claims-table rows fixed in record #2.
- [x] 4.5 Write `docs/process-defects/extend-owner-reply-language-scope.md`, one record per defect this unit observed,
      or the statement that it observed none. **Done** — `docs/process-defects/extend-owner-reply-language-scope.md`
      records every defect this unit observed, from the first record through the last; no observed defect is left
      unrecorded.

## 5. Archive-stage edit

- [x] 5.1 Apply the `## Purpose` refresh for `showcase/quality/agent-skills` in the archive commit: its "owner-facing
      replies in the owner's request language" becomes the owner-visible messages the convention now covers. A delta
      cannot carry a Purpose, so the edit belongs here and the change's report states the deferral. **Done** — the
      Purpose now reads "owner-visible messages in the owner's request language", which covers the narration and the
      presented-report prose the requirement's scenarios assert.

## 6. Review, routing, and capture

- [x] 6.1 Run the `review-quick` loop through the `review-gate` skill's preflight, appending each round's row to
      `docs/review-rounds.tsv` as its report arrives, and run both ledger checks plus `--expect-clean` before the manual
      pass. **Done** — the loop ran to a clean closing round in session `ses_e83e4537bffepsd6fBXMZj623I` (created, then
      continued), each round's row appended to `docs/review-rounds.tsv`, which records them; both ledger checks passed
      after every round and `--expect-clean` passed on the closing round.
- [x] 6.2 Derive the deep-review routing outcome with
      `python3 scripts/review-routing.py --change extend-owner-reply-language-scope`, decide at or above it, and record
      the outcome and its signals on this task. **Done** — `RECOMMENDED`, with the signal `repeated-class` (two rows
      repeat a prior class), resolved to run the pass. **Deep pass 1** (session `ses_e83d86dacffefA9Hd8KBPdpNRF`,
      initial review): verdict `1 nitpick`, nothing blocking or should-fix. **N1 OPEN** —
      `specs/showcase/quality/agent-skills/spec.md:18-19` (delta): the progress-narration scenario's "in another
      language" names no referent. **FIXED_AND_VERIFIED** — the precondition was dropped, since the policy is
      destination-based, and the explicit-override clause kept; deep pass 2 verified the defect gone, the sibling and
      `AGENTS.md` consistent, and nothing else moved.
- [x] 6.3 Re-run every claims-table row's command and every ticked task's Done-note command against the state being
      handed over, and re-check every count or universal statement in them against the artifact it counts, correcting
      any that no longer reproduces; then ask the owner for the manual review pass and end the turn; commit, push,
      archive, and merge only after that approval. **Done** — every row and Done-note command reproduced: 8 base
      scenarios and 0 label hits, the pointer body, `consult.md:53`, the single `JavaLanguageVersion` build hit,
      archived lines 49-50, `README.md:366`, probe lines 113/117, `progress narration` and `count descriptions` one hit
      each, 10 delta scenarios, the five phrase greps and two direct reads, the 4.3 classification hits, and the
      120-character check (only the mandated `Workspace` line and the formatter-owned table rows exceed it); both ledger
      checks and `--expect-clean` pass. The manual pass is requested below.
