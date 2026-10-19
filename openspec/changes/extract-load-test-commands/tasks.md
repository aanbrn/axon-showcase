# Tasks

## 1. The identifier audit (the opportunity's own precondition)

- [x] 1.1 Audit every identifier the block carries against its home — the load-tests spec, the script's docstring, the
      build's `-P` forwarding, or the retained root pointer — and record the result in `design.md`'s Context. Verify
      each finding with the command the claims table names. **Done** — the audit is in `design.md`'s Context, each
      finding carrying the command the claims table names. The spec already carries twelve properties with their
      defaults and property-provided scenarios, and the SSE connection count by scenario alone (11 lines, 190-253); the
      `-P` spelling is the build's whitelist; the script's docstring names `TOLERANCE`, `REFRESH_BASELINE`, `report.md`,
      and the resource sampling, while `CALIBRATE_MAX_RATE` and the other environment variables live in the script's
      body, and the floors were in neither; `baselineTrend` is in neither the spec nor the script.
- [x] 1.2 Draw the stay/move boundary from that audit: what stays at root (the invocation, the `:load-tests:test`
      warning, the pointer), what the delta adds (the `-P` spelling, the `sseConnections` identifier), what the
      docstring gains (the floors), and what is deleted as the spec's own restatement. Verify no identifier is left
      without a home. **Done** — the boundary: **stays at root** the invocation, the `:load-tests:test` warning, the
      property pointer, and the retained pointer; **the delta adds** the `-P` spelling and the `sseConnections`
      identifier; **the docstring gains** the floors; **deleted** the semantic prose (the threshold derivation, the
      fallback values, and the baseline-replacement rules), which the spec owns.

## 2. The spec delta

- [x] 2.1 Write `openspec/changes/extract-load-test-commands/specs/showcase/quality/load-tests/spec.md` as a `MODIFIED`
      block for the requirement that owns the property set, copying the full requirement (description + every scenario,
      in order) and adding the `-P` spelling and the `sseConnections` identifier and default. Verify with
      `openspec validate --changes`. **Done** — the delta is one `MODIFIED` block for "Configurable target, profile,
      rate, ratio, and duration", copied whole (all 10 scenarios in order) with the `-P` spelling added and the
      `sseConnections` identifier and default; `openspec validate extract-load-test-commands` is valid.

## 3. The extraction

- [x] 3.1 In `AGENTS.md`, replace the load-test block's semantic prose with the retained pointer, keeping the
      invocation, the `:load-tests:test` warning, and the property pointer. Verify the block's identifiers all resolve
      to a home from task 1.2. **Done** — the block went from 28 lines to 5: the invocation, the `:load-tests:test`
      warning, and a pointer naming the property set, the profile set and assertions, the threshold derivation and
      fallbacks, and the baseline mechanics as the spec's, plus the measurement script. The property list is **pointed
      at rather than enumerated**, since the delta now carries the `-P` spelling and a second copy would be the drift
      surface.
- [x] 3.2 In `AGENTS.md`, shrink the `baselineTrend` block to its invocation and a pointer to the spec. Verify no
      identifier it carried is left homeless. **Done** — the trend block went from 8 lines to 5: the invocation,
      `-Precords`, and the git-provenance note; the field enumeration is the spec's.
- [x] 3.3 In `scripts/load-test-baseline.sh`, add the tolerance floors to the docstring. Verify by reading it back.
      **Done** — the docstring gained the floors, **at the measured values, not the deleted block's**: the reference
      records `floorMeanMs=50`, `floorP95Ms=100`, `floorP99Ms=200`, while the `AGENTS.md` text the extraction removed
      claimed "floors 5/10/20 ms" — a false claim this change deletes rather than relocates.

## 4. Pre-write derivation pass

- [x] 4.1 The pre-write derivation pass ran over the planning artifacts: each fact the artifacts state about their
      evidence resolved into a row of the claims table in `design.md` or a dismissal recorded beside it. **Done** — the
      table is in `design.md` under `## Claims`, seeded with `python3 scripts/claim-scan.py`, whose candidates are each
      resolved into a row or recorded as a by-kind dismissal beside the table. Each row's clauses were verified against
      the command it names after the last edit.

## 5. Verification and the review gate

- [x] 5.1 `./gradlew spotlessApply` then `./gradlew spotlessCheck` and `openspec validate --all`; confirm clean.
      **Done** — `spotlessCheck` clean after `spotlessApply`; `openspec validate --all` 27/27 (the delta is valid, so no
      `skip_specs` is declared and none is owed).
- [x] 5.2 The 120-character check over the change's whole changed set, derived with
      `git status --porcelain --untracked-files=all`, using the `perl -CSD -lne` recipe. **Done** — the recipe over the
      changed set reports no manually-wrapped line this change added: every over-120 line is either a formatter-gated
      markdown table row, a `docs/review-rounds.tsv` record row (one line by contract), or the pre-existing gateway e2e
      `bootBuildImage` line.
- [x] 5.3 The review gate ran: `review-quick` over the proposal and the implementation, repeating until clean, with each
      round's row in `docs/review-rounds.tsv` carrying the identifier the invocation returned. **Done** — six rounds in
      one session (`ses_eac844c92ffeKdWC3o7QPzkA7Z`), each row in `docs/review-rounds.tsv` carrying the identifier the
      invocation returned: 9 -> 5 -> 4 -> 1 -> 1 -> 0 findings. The recurring cause, named by the reviewer and this
      unit's lesson: a self-record claim written before the last edit and not re-derived after it, and a group attribute
      asserted over a list containing its exception, which no qualification of the exception repairs -- only the split
      form does.
- [x] 5.4 The deep-review routing decision is recorded on this task, derived with
      `python3 scripts/review-routing.py --change extract-load-test-commands`. **Derived, resolution pending the owner**
      — `python3 scripts/review-routing.py --change extract-load-test-commands` reports `RECOMMENDED` on
      `[repeated-class]` (four rows repeat a prior class); no path, declaration, or requirement-block signal matched. At
      or above the floor, so the outcome needs an explicit recorded resolution, which the owner is asked for with the
      manual-review request. **No deep-review session is created unless the owner resolves to run one.** **Done — the
      owner's resolution is the recorded decision**: asked with the manual-review request alongside the recommended
      reading, and the owner resolved to proceed without a deep review. **No deep-review session is created.**

## 6. Capture

- [x] 6.1 The `lesson-capture` subagent ran over the diff and the review findings, and its durable proposals were
      applied — or the skip condition held in full and its reason is recorded — with the applied net `AGENTS.md` delta
      on this task. **Done** — it ran (session `ses_eac658fabffe4bsPEtFlybwWOK`): **1 durable proposal applied** (a
      universal claim over an enumerated list that carries an exception is split, not qualified — merged into the
      review-gate procedure's universal-claim clause, whose text now names this unit's `captured:` marker), 0
      retirements, 0 authoring-system recommendations. **Applied net `AGENTS.md` delta +3 lines** (the
      `@@ -304,15 +304,18 @@` hunk); the unit's own net is -2,224 bytes, since the extraction removed 2,709. Its three
      other candidates were each `already covered`: the re-derive-after-the-last-edit rule (a compliance miss, its
      wording adequate), the precondition-first rule for an extraction, and an audit report's figures being claims
      rather than evidence.

## 7. Manual review and archive

- [x] 7.1 The owner's manual review pass was requested after the loop was clean and the capture ran. **Done** —
      requested after the loop was clean and the capture ran; the owner approved and resolved the routing question.
- [ ] 7.2 The change was archived with `openspec archive extract-load-test-commands --yes`, the change-dir move and the
      `openspec/specs` sync staged together, and the archive commit ridden on this unit's PR.

## Workflow follow-up

- Nothing committed, pushed, or archived before the owner approves the implementation.
