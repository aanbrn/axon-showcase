# Tasks

## 1. AGENTS.md — the corrected enumerations and claims

- [x] 1.1 In the Build & Test `check` comment, add `testReviewRouting` between `testReviewRounds` and
      `verifyTrackedIgnoredFiles`, derived from `build.gradle.kts`'s `check` block rather than from the report's prose.
      Verify by reading the comment back and re-wrapping the block within 120 characters. **Done** — the comment now
      reads `…testReviewRounds, testReviewRouting,` and re-wraps within 120 characters.
- [x] 1.2 In the Prerequisites Python list, add `review-routing.py` beside `claim-scan.py` and `review-rounds.py`,
      derived from `ls scripts/*.py`. Verify by re-running that derivation against the sentence. **Done** — the list now
      names `claim-scan.py`, `review-rounds.py`, and `review-routing.py`.
- [x] 1.3 In the "Tooling lives in a stated place" bullet, name `review-routing.py` as driven by the review gate's own
      procedure, since it falls in none of the clause's three categories (agent-driven with a trigger, no agent caller,
      hook/Gradle-driven). Verify against `grep -rn "review-routing" .opencode/` (no command drives it). **Done** — the
      clause now adds "and `review-routing.py` by the review gate's own procedure and by its `testReviewRouting` task".
- [x] 1.4 In the OpenCode model-pin gotcha, correct the exclude clause to name the current vision pin
      (`opencode-go/claude-haiku-5-5`) and its actual reason, keeping the historical `-vision-exp` sentence as history,
      derived from `.opencode/agent/vision.md`. Verify against that file and `README.md:364`. **Done** — the clause now
      reads "exclude the vision agent's pin (`opencode-go/claude-haiku-5-5`): the vision model is a separate multimodal
      model that is not part of the flash/pro line being bumped", and the historical sentence is untouched.

## 2. README.md — the Prerequisites row

- [x] 2.1 In the Prerequisites table's Python row — the AGENTS.md list's mirror — add `review-routing.py`. Verify the
      row and its neighbours still render as a table and that no other row names the same list. **Done** — the row now
      names `review-routing.py`; no other row names the list.

## 3. docs/ideas.md

- [x] 3.1 Remove the 2026-10-18 "Sweep the two enumerations `#628` left stale" entry, now implemented. Verify the entry
      is gone and no other entry references it. **Done** — the entry is gone and the section's other bullets are intact
      (five bullets under `## 2026-10-18` at `HEAD`, four in the working tree).

## 4. Pre-write derivation pass

- [x] 4.1 The pre-write derivation pass ran over the planning artifacts: every count, universal claim, and mechanism
      claim re-derived from its source, and each fact the artifacts state about their evidence resolved into a row of
      the claims table in `design.md` or a dismissal recorded beside it. **Done** — the table is in `design.md` under
      `## Claims`, seeded with `python3 scripts/claim-scan.py`, whose candidates are each resolved into a row or
      recorded as a by-kind dismissal beside the table.

## 5. Verification and the review gate

- [x] 5.1 `./gradlew spotlessApply` then `./gradlew spotlessCheck` and `openspec validate --all`; confirm clean. The
      changed files are markdown, formatter-owned, plus the change dir's `.openspec.yaml`. **Done** — `spotlessCheck`
      clean after `spotlessApply`, `openspec validate --all` 27/27, `--changes` 1/1 (with the `skip_specs` notice).
- [x] 5.2 The 120-character check over the change's whole changed set, derived with
      `git status --porcelain --untracked-files=all`, using the `perl -CSD -lne` recipe. **Done** — the recipe over the
      changed set reports the same over-120 count as `git show HEAD:<file>` (AGENTS.md 1, README.md 55): the edited
      README row was already over 120 as one of the formatter-padded Prerequisites table's rows, so the change neither
      adds nor removes a long line.
- [x] 5.3 The review gate ran: `review-quick` over the proposal and the implementation, repeating until clean, with each
      round's row in `docs/review-rounds.tsv` carrying the identifier the invocation returned. **Done** — five rounds in
      one session (`ses_eaceea27affeCHcibBzhMTUwz6`), each row in `docs/review-rounds.tsv` carrying the identifier the
      invocation returned: round 1 `claim-unreproducible,incomplete-enumeration` (4), round 2
      `claim-missing,claim-unreproducible,formatting` (3), round 3 `claim-unreproducible,claim-missing` (2), round 4
      `claim-unreproducible` (1), round 5 clean. Every finding fell on the change's own claims table, never on its
      substance; the reviewer named the root cause twice (clause granularity, then predicate-vs-subject), and the fix
      converged on re-deriving each row so every clause is a predicate its named command's output shows.
- [x] 5.4 The deep-review routing decision is recorded on this task, derived with
      `python3 scripts/review-routing.py --change apply-2026-10-19-audit-findings`. **Derived, resolution pending the
      owner** — `python3 scripts/review-routing.py --change     apply-2026-10-19-audit-findings` reports `RECOMMENDED`
      on `[repeated-class]` (three rows repeat a prior class); no path, declaration, or requirement-block signal
      matched. At or above the floor, so the outcome needs an explicit recorded resolution, which the owner is asked for
      with the manual-review request: the signal is the loop's own cost in this change's self-records, which round 5's
      clean report and the clause-level re-derivation resolve, while the change ships no code. **No deep-review session
      is created unless the owner resolves to run one.** **Done — the owner's resolution is the recorded decision**:
      asked with the manual-review request alongside the recommended reading (the signal is the loop's own cost in this
      change's self-records, which round 5's clean report and the clause-level re-derivation resolve, and the change
      ships no code), and the owner approved the plan and resolved to proceed without a deep review. **No deep-review
      session is created.**

## 6. Capture

- [x] 6.1 The `lesson-capture` subagent ran over the diff and the review findings, and its durable proposals were
      applied — or the skip condition held in full and its reason is recorded — with the applied net `AGENTS.md` delta
      on this task. **Done** — it ran (session `ses_eacd3fc90ffeOr5i0uvE5kelq4`): **1 durable proposal applied** (the
      claims-table row is re-derived clause by clause, from the named command's output — merged into the existing
      self-records failure-handling bullet, whose `captured:` marker now carries this unit), **applied net `AGENTS.md`
      delta +1 line** (the `@@ -460,8 +460,9 @@` hunk; the marker is mandatory and spotless's reflow absorbed one of the
      proposal's two lines), 0 retirements, 0 authoring-system recommendations. Its `already covered` verdicts: the
      audit-fix sweep class (the doc-consistency gotcha) and the drafting order (the addition's own procedure).

## 7. Manual review and archive

- [x] 7.1 The owner's manual review pass was requested after the loop was clean and the capture ran. **Done** —
      requested after the loop was clean and the capture ran; the owner approved and resolved the routing question.
- [ ] 7.2 The change was archived with `openspec archive apply-2026-10-19-audit-findings --yes`, the change-dir move and
      the `openspec/specs` sync staged together, and the archive commit ridden on this unit's PR.

## Workflow follow-up

- Nothing committed, pushed, or archived before the owner approves the implementation.
