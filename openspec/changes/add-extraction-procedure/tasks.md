# Tasks

## 1. The procedure and its pointer

- [x] 1.1 Add `Workflow: Extract an AGENTS.md section` to `AGENTS.md` after `Workflow: Author a spec delta and archive`,
      with the move steps and a completion criterion. Verify it points at the audit's retrieval-path conditions and the
      growth-bound bullet's trim verification instead of restating them. **Done** — the procedure states eight
      one-action steps (verify the retrieval path for an existing or a not-yet-existing destination; check a gate that
      reads `AGENTS.md`; create the destination; land the content; replace the section with the retained pointer;
      retarget the cross-references; re-run the retrieval path; re-measure) and three `IF … THEN` decision points. It
      names the audit's conditions (`showcase/quality/agent-skills`) and the growth-bound bullet's trim verification
      rather than restating either.
- [x] 1.2 Point the extraction bullet in `.opencode/commands/apply-audit-reductions.md` at that procedure, without
      carrying a copy. **Done** — the bullet now routes the move to the change and names `AGENTS.md`'s "Workflow:
      Extract an `AGENTS.md` section".
- [x] 1.3 Remove the implemented 2026-10-18 entry from `docs/ideas.md`. **Done** — removed; the file's new 2026-10-19
      entry parks the capture's authoring-system recommendation.

## 2. Pre-write derivation pass

- [x] 2.1 The pre-write derivation pass ran: `python3 scripts/claim-scan.py` over the authored set, each candidate a row
      of the claims table in `design.md` or a by-kind dismissal beside it, and each row re-derived from its command.
      **Done** — eleven rows plus four by-kind dismissals; the scan's candidates are all accounted for. Row 8 anchors to
      the pre-change `docs/ideas.md` (`git show HEAD:`), since this change removes the entry it cites, and rows 9-11
      were added after review round 1 (the report's ledger, the pre-change command, and the narrowed retention basis).

## 3. Verification and the review gate

- [x] 3.1 `./gradlew spotlessApply` then `./gradlew spotlessCheck` and `openspec validate --all`; confirm clean.
      **Done** — clean; `openspec validate --all` 27/27.
- [x] 3.2 The 120-character check over the change's whole changed set, using the `perl -CSD -lne` recipe. **Done** — no
      added prose line exceeds 120; only the pre-existing long Gradle command, the formatter-exempt table rows, and the
      one-line-by-contract ledger rows are flagged.
- [x] 3.3 The review gate ran: `review-quick` until clean, each round's row in `docs/review-rounds.tsv` with the
      identifier the invocation returned. **Done** — the gate ran against one reviewer session
      (`ses_eac044363ffeZ5lGcaU9SW2qr9`), created on round 1 and continued through every later round with the returned
      identifier matching. Each round's row is in the ledger with its classes and transition, and every finding a
      recorded round raised is applied before the next.
- [x] 3.4 The deep-review routing decision is recorded on this task, derived with
      `python3 scripts/review-routing.py --change add-extraction-procedure`. **Done** — the derivation reports
      `RECOMMENDED` on `[repeated-class]` (the ledger's rows repeat classes across rounds). Resolved to proceed without
      a deep review: the repeated classes map to the failure-handling shapes now recorded, and the change ships guidance
      prose plus a command pointer.

## 4. Capture

- [x] 4.1 The `lesson-capture` subagent ran, or the skip condition held in full with its reason recorded, with the
      applied net `AGENTS.md` delta on this task. **Done** — the subagent ran (the rounds recorded classes beyond the
      local-implementation set) and proposed two changes; both were reassessed against the promotion gate before being
      recorded. The claim-sourcing clause (derive a claim from a pointer, not its target) was already covered: the
      existing bullet requires deriving the claim from the surface, and the clause changed no action, so it was removed.
      The recurring-class bullet inferred a drafting cause from the class label; it was removed, and its distinct
      increment — a whole-section rewrite conditioned on the findings showing a shared cause, correcting verified
      errors, removing verified duplication, preserving every valid requirement, exception, warning, and identifier, and
      verifying with the growth-bound trim sweep — is retained as a failure-handling bullet in the non-converging-loop
      list, which the Decision point delegates to. Net `AGENTS.md` delta: **+5 lines** (the bullet's `+5` hunk),
      measured after `spotlessApply` and separate from the extraction procedure's 30-line section. No rule retired. The
      authoring-system recommendation — the audit ledger's safe-to-load-on-demand class should carry its retrieval-path
      result — is a mechanism fix (the `agent-skills` requirement and the `agents-auditor` definition) and is parked in
      `docs/ideas.md`. The `lesson-capture` definition is unchanged: its existing decision-difference test and cause
      discipline already cover both misses, so no authoring-system gap was found.

## 5. Manual review and archive

- [x] 5.1 The owner's manual review pass was requested after the loop was clean and the capture ran. **Done** —
      requested on 2026-10-19 once round 6 was clean and the capture applied; the work stays uncommitted until the owner
      answers.
- [ ] 5.2 The change was archived with `openspec archive add-extraction-procedure --yes`, and the archive commit rode
      this unit's PR.

## Workflow follow-up

- Nothing committed, pushed, or archived before the owner approves the implementation.
