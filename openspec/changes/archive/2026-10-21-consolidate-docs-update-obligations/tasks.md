# Tasks

## 1. Apply the consolidation

- [x] 1.1 Replace the two Gotchas rules with Draft 1. Delete the `A "the only X" claim…` bullet and the blank separator
      that follows it, and replace the `A doc-consistency sweep is scoped…` bullet with `design.md`'s Draft 1 verbatim.
      Verify: `git show HEAD:AGENTS.md | grep -c 'the only X'` is `2` before the edit, and
      `grep -c 'the only X' AGENTS.md` is `1` after it (1.3 takes it to `0`); the merged rule is the only changed span
      carrying the four `captured:` markers. **Done** — merged rule in place; the block carries the four markers,
      `grep -c 'captured:' AGENTS.md` is unchanged at 210, and `grep -c 'the only X' AGENTS.md` is `0`.
- [x] 1.2 Confirm the merged rule adds no obligation. For each of the three generalizations `design.md` D4 removed — a
      same-change timing in the lead, member removal, a config-key/workflow rename — confirm the implemented text does
      not carry it, and that each of B's and C's obligations resolves in Draft 1 by the preservation map. Report any
      obligation with no home rather than dropping it. **Done** — the three generalizations are absent — the lead has no
      timing clause, row 7 reads "gained a member", row 8 reads "a code symbol or file"; every preservation-map row
      resolves.
- [x] 1.3 Replace span A with Draft 2. Apply the four statements: the un-gated copies, the auditor ownership sentence,
      the delivery boundary's first branch, and the idea-removal route. Verify: outside those four statements the span,
      whitespace-normalized, equals `git show HEAD:AGENTS.md`'s version — Prettier re-wraps the paragraph, so compare
      flattened text, not bytes. **Done** — a word-level diff of the span against `HEAD` shows only the four statements
      changed (20 diff regions, all inside them); the `SECURITY.md` sentence and the rest are text-identical after
      whitespace normalization, with only re-wrapping in the diff.
- [x] 1.4 Retarget the reference in the recurring-unit-type bullet (`enumerates the "the only X" bullet's member grep` →
      `enumerates the sweep rule's member grep`). Verify: `grep -n 'the only X' AGENTS.md` returns no match; the merged
      rule calls the class "an `only`/`sole`/`never` claim". **Done** — reference retargeted;
      `grep -n 'the only X' AGENTS.md` returns nothing, and row 9 names the class as "an `only`/`sole`/`never` claim".
- [x] 1.5 Merge the two evidence-record sections into Draft 3. Delete `## consistency-sweep` and `## uniqueness-claims`
      and insert `## claim-site-sweep` at the first position, reusing both bodies. Verify against
      `git show HEAD:docs/guidance-evidence/documentation-and-durable-claims.md`: the `Rule:` paragraph, whitespace
      normalized, equals the merged rule's bolded lead; all four `captured:` markers appear; and every backticked
      identifier of both old sections is present. **Done** — `## claim-site-sweep` in place; its `Rule:` paragraph
      matches the merged lead with whitespace collapsed; both bodies and all four markers present; the only dropped
      backticked token is `.opencode/`.
- [x] 1.6 Run `./gradlew spotlessApply` after the final edit to either file, then `./gradlew spotlessCheck`, and confirm
      the two files are reported as formatted — a "does not converge" skip is a failure for that file. **Done** —
      `spotlessApply` then `spotlessCheck` pass for both files (no "does not converge" skip reported).

## 2. Verify the result

- [x] 2.1 Re-measure and record on the task: `git show HEAD:AGENTS.md | wc -c -l` against `wc -c -l AGENTS.md` after
      `spotlessApply`. The design's figures are 323,520 bytes / 3,354 lines against 324,296 / 3,361; report the measured
      pair, and if it differs, re-derive the design's and the proposal's figures before review. **Done** — measured
      `323,520` / `3,354` against `324,296` / `3,361` — the predicted −776 / −7.
- [x] 2.2 Verify the replacement spans: format Draft 1 and Draft 2 (extracted from `design.md`'s fences) with the
      repository's Prettier configuration and `wc -c -l` them — expected 3,066 / 35 and 6,838 / 58, together 9,904 / 93
      against the baseline's 10,670 / 99. Do not run the span script against the implemented file: its
      `assert len(st)==1` lookup matches the old titles, which 1.1 removes. **Done** — Draft 1 formats to `3,066` / `35`
      and Draft 2 to `6,838` / `58`, together `9,904` / `93` against `10,670` / `99`.
- [x] 2.3 Verify the record: `wc -c -l docs/guidance-evidence/documentation-and-durable-claims.md` — expected 16,460
      bytes / 244 lines (from 16,608 / 249). **Done** — record measured `16,460` bytes / `244` lines (from `16,608` /
      `249`).
- [x] 2.4 Run `openspec validate --all` and `openspec validate --changes`; confirm the change validates and the
      `skip_specs` declaration raises no missing-delta error. Confirm the merge criterion still holds against the
      implemented text: each of the eleven rows carries its own trigger and operand, and no row's remedy is qualified by
      another's. **Done** — `openspec validate --all` passes (27 items); each of the eleven rows carries its own trigger
      and operand, and no row's remedy is qualified by another's.
- [x] 2.5 Docs refresh: grep `README.md`, `docs/adr/`, and `openspec/config.yaml` for the two old rule titles and for
      `docs-refresh convention`; record that no site needs an edit beyond the one retargeted in 1.4, or apply the edit
      the grep surfaces. **Done** — `grep` over `README.md`, `docs/adr/`, `openspec/config.yaml`, and `SECURITY.md` for
      the two old titles and `docs-refresh convention` returns no hit; no docs edit was owed beyond 1.4's retarget.

## 3. Review, capture, and delivery

- [x] 3.1 Re-derive `design.md`'s claims table from the implemented state once the edits land (rows about `AGENTS.md`
      and the record anchor to `git show HEAD:<path>`, so they must still reproduce). Seed it with
      `scripts/claim-scan.py` and record each candidate as a row or a dismissal beside the table. **Done** —
      `scripts/claim-scan.py --base origin/main` was run over the implemented diff; the claims rows were re-derived
      against the implemented file and every figure reproduces (the capture's row was added after); the candidates
      resolve by the dismissal kinds recorded in `design.md`.
- [x] 3.2 Run `review-quick` over the implementation, continuing reviewer session `ses_ea64796d0ffe1yn1uOmqkSNzup` from
      the proposal round, and append each round's row to `docs/review-rounds.tsv` with the returned identifier and
      transition. Fix findings and re-run until no new observation is reported. **Done** — every round is recorded in
      `docs/review-rounds.tsv` with its session identifier and transition; the ledger states the loop's state.
- [x] 3.3 Derive the deep-review floor with
      `python3 scripts/review-routing.py --change consolidate-docs-update-obligations`, decide at or above it, and
      record the outcome and its signals on this task. **Done** —
      `python3 scripts/review-routing.py --change consolidate-docs-update-obligations` returned `RECOMMENDED` with
      `[repeated-class] 7 rows repeat a prior class`. Decided to run the deep review, which reported two findings — the
      row-9 target widening (fixed by splitting the row into a uniqueness row and an absence row) and this task record's
      drift (fixed) — and no others.
- [x] 3.4 Run the `lesson-capture` subagent over the unit; apply the durable proposals, or record the skip and its
      reason. Record the applied net `AGENTS.md` delta (`git diff --numstat AGENTS.md` after `spotlessApply`) here.
      **Done** — the subagent proposed two additions. Applied: the merge-verification clause in `Avoid redundancy` (+418
      bytes, one severe verified incident — a shared arrow widened a branch's target past the quick rounds). Rejected:
      "an audit's empty result carries no verdict" — one non-severe occurrence, below the gate's evidence threshold —
      and recorded in `design.md`'s prior-decisions table instead. No retirements, no authoring-system recommendation.
- [x] 3.5 Re-derive the unit's whole figures after the capture: the consolidation subtotal (−776 root) plus the
      capture's net delta, and the record's −148; report them separately in the change's report. **Done** —
      consolidation `−776` / `−7`; capture `+418`; whole unit `−358` / `+1` (`324,296` → `323,938`; `3,361` → `3,362`,
      `git diff --numstat AGENTS.md` → `79 78`); record `−148` / `−5`; total maintained `−506`.
- [x] 3.6 Stop and request the owner's manual review pass. Do not commit, push, open a PR, archive, or merge until the
      owner approves.
      **Done** — the owner approved the implementation in the session and the change ships in PR #653.
