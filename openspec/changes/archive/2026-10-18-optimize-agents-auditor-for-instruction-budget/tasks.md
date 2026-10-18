# Tasks

## 1. The spec delta

- [x] 1.1 `openspec/changes/optimize-agents-auditor-for-instruction-budget/specs/showcase/quality/agent-skills/spec.md`
      — two `MODIFIED` blocks, each copying the main spec's requirement verbatim (its body and all its scenarios, in
      order): the audit requirement gains the instruction-budget ledger, the root-guidance rule, the extraction
      disposition and its conditions, the reduction candidates' seven fields, and eight scenarios; the reduction-unit
      requirement names `extraction` and gains one scenario. Verify with `openspec validate --changes` that no scenario
      is dropped or renamed. **Done** — written at the proposal; `openspec validate --changes` passes with the 15 + 7
      original scenarios preserved and 8 + 1 added.

## 2. The definition and its describing artifacts

- [x] 2.1 `.opencode/agent/agents-auditor.md` — the frontmatter `description`; the conciseness axis's standing analyses
      gain the extraction disposition and the reduction candidates' seven-field table; the new
      `Instruction-budget optimization pass` section (the ledger's figures; the effective-bound rule; the growth
      derivation; the bytes safely removable; the must-stay list with its unavailability risk; the on-demand list with
      each rule's trigger and retained pointer; and the post-change verification); the extraction analysis (the
      conditions, the plan table, the destination-type table, the control-flow caveat, the retrieval-path test); the
      prioritization order; and the report contract's `Instruction-budget ledger` section. Verification: read it against
      the delta's obligations clause for clause. **Done** — the definition carries the budget pass (the ledger's ten
      figures, the root-only principle, the candidate field table, and the prioritization order), the extraction
      analysis (the eight conditions, the ten-field plan table, the destination table, the control-flow caveat, and the
      retrieval-path test), the conciseness axis's fourth standing analysis, the `extract` label, the report contract's
      `Instruction-budget ledger` bullet, the frontmatter `description`, and the blame limitation line. Read clause for
      clause against the delta.

- [x] 2.2 `.opencode/commands/audit-agents.md` — its `description`, its step-1 read-list (the budget pass and the
      extraction disposition), and its step-2 report list (the ledger section). Verification: every clause it lists
      resolves to the definition. **Done** — its `description`, step 1's read-list (the budget pass's figures), and step
      2's report list (the ledger section with its extraction candidates) are updated; every clause it lists resolves to
      the definition.

- [x] 2.3 `.opencode/commands/apply-audit-reductions.md` — its enumerations of the candidate kinds and of the
      application modes: the frontmatter `description`'s "(merge/removal/route)", step 2's "a merge, removal, or route
      candidate", and step 4's application-mode list. Each includes an extraction, whose application is a content-move
      routed to the change workflow. Verification: each matches the reduction-unit requirement's. **Done** — three sites
      in the file are widened, not two: the frontmatter `description`'s `(merge/removal/route/extraction)`; step 2's
      `a merge, removal, route, or extraction candidate`; and step 4's application-mode list, which gains an
      `extraction` bullet routing the move to the change workflow (found in round 5, after the sweep's phrase pattern
      missed it). All match the reduction-unit requirement's body.

- [x] 2.4 `AGENTS.md` — the `agents-auditor` bullet names the budget pass and the extraction disposition. Verification:
      `./gradlew verifyInstallCommands verifyDocumentedPins verifyCapturedMarkers` pass. **Done** — the bullet names the
      budget pass (its ledger's figures trimmed to the ones a reader needs, per the capture's retirement) and its class
      list gains `extraction`; `verifyInstallCommands`, `verifyDocumentedPins`, and `verifyCapturedMarkers` pass.
      `AGENTS.md` moved 357,100 → 357,537 bytes (+437) and 3,622 → 3,625 lines (+3) at this task.

- [x] 2.5 `README.md` — the audit prose, the agent table row, and the slash-command table row. Verification: the three
      sites state the same contract as the definition. **Done** — the audit prose, the `agents-auditor` agent-table row,
      and the `/audit-agents` slash-command row are updated; the sweep (2.7) found a third row this task had not named —
      the `/apply-audit-reductions` row's `merge/removal/route` enumeration — and it is widened too.

- [x] 2.6 `docs/ideas.md` — the "Trend the audit counts across reports" entry names the size ledger as a trendable
      series and stays parked. Verification: the entry's text and its trailing status tag. **Done** — the entry names
      the ledger's size, section, and growth series as a further trendable series and keeps its
      `— parked; no change yet.` tag.

- [x] 2.7 Sweep every live site that enumerates what the audit reports, derived from `grep -rn` for the audit's class
      names across `AGENTS.md`, `README.md`, `.opencode/`, `docs/`, and `openspec/config.yaml` — not a hand-written
      list. Verification: each hit is updated or recorded as deliberately unchanged.

      **Done** — the phrase sweep over `AGENTS.md`, `README.md`, `.opencode/`, `docs/`, and `openspec/config.yaml` found
      `AGENTS.md`'s bullet, `README.md`'s rows and prose, the definition, and both commands; all are updated.
      Deliberately unchanged, with the reason: `docs/audits/2026-10-11.md` (a historical record, left as recorded) and
      the main spec's own sites (the `## Purpose` is task 4.3; the verdict-line scenario and the reduction requirement
      are the delta's to change). The sweep could not see the reduction unit's step-4 bulleted list of application
      modes — the implementation review found it in round 5 — so what the sweep establishes is the sites its phrase
      pattern reaches, not that every enumeration was caught.

## 3. Verification

- [x] 3.1 `./gradlew spotlessApply spotlessCheck` and `openspec validate --all` pass; the 120-character recipe is clean
      over the changed set the formatter does not cover (the change dir is untracked, so the set comes from
      `git status --porcelain --untracked-files=all`, not `git diff --name-only`). Verification: the commands' output.
      **Done** — `spotlessApply`/`spotlessCheck` green after the last edit; `openspec validate --all` 27/27 and
      `--changes` 1/1. The 120-character recipe over the set `git status --porcelain --untracked-files=all` yields is
      clean apart from the formatter-owned markdown table rows (the design's two tables, `README.md`'s rows) and the
      one-line `docs/review-rounds.tsv` records, both exempted by the Formatting convention.
- [x] 3.2 Run the `review-quick` subagent over the planning artifacts, then the implementation, repeating until it
      reports no new observations; append each round's record line to `docs/review-rounds.tsv` as its report arrives.
      Verification: the loop's last round reports nothing remains, and the ledger's rows are appended. **Done** — 8
      rounds (4 proposal, 4 implementation), clean at round 8; each round's record line appended as its report arrived.
      **Re-opened by the revision (5.1-5.6)** - rounds 1-8 verified the pre-revision artifacts; the revision's delta,
      design, and implementation must pass the loop again, with the numbering continuing from round 9. **Done
      (revision)** - the loop re-ran for the revision: rounds 9, 10, 11, clean at round 11; each round's record line
      appended as its report arrived.
- [x] 3.3 Smoke-run the edited definition after a reload, with **positive and negative controls**: a seeded
      large-section growth case must yield an `Instruction-budget ledger` naming the size, the sections, the effective
      bound with its source, and the growth; a seeded rule that is lengthy but governs a decision must **not** be
      reported as a removal candidate; and a seeded candidate whose destination a task would never read must fail the
      retrieval-path test and be reported as a reason to retain. A definition edit is not exercised until it is picked
      up (the subagent-reload gotcha), so this gates the archive. Verification: the post-reload run's report, and the
      control outcomes. **Open — the archive gate, and the reason it is open:** the controls are exercised by the run
      recorded in 3.4 (the ledger is produced; no lengthy-but-decision-bearing rule is a removal candidate; the
      extraction analysis's failed retrieval path is reported as a reason to retain), but the _reload_ precondition is
      not met in this session. An edited definition is served from the pre-change registration until a reload or a new
      session, and the subagent-reload gotcha says a run echoing the new text is no proof of pickup because the subagent
      reads its own definition from disk — so the run above is evidence of the behavior, not of the registration. The
      definitive post-restart run is required before the archive, and the change's report names it. **Also re-opened by
      the revision** - the run recorded here exercised the pre-revision definition; the post-revision definition needs
      its own post-restart run, which remains the archive gate. **Post-revision run recorded** - the run in 3.4
      exercised every class of the revised definition (including the extraction opportunity), but the definition was
      edited after the restart, so its registration is again undecidable in-session; one further restart and run is the
      definitive gate. **Done (post-revision)** - the definitive post-reload run: `0 findings`, `0 advisory`,
      `~159 accreted`, `0 authoring-system gap`, plus the ledger (358,230 bytes / 3,632 lines; no size cap; the bound
      reported as a discipline rather than a configured limit, sourced to `AGENTS.md`'s own growth-bound bullet, with no
      invented figure; the threshold reported as unconfigured; the size-by-section table with the per-section growth; ~0
      bytes safely removable; the must-stay list; the on-demand list held aspirational because no destination carries
      the delegated content; and the post-change verification), two **extraction opportunities** (the 154,935-byte
      Gotchas corpus and the 24,726-byte Docker/Kubernetes/Local-Development reference), each with all ten fields and
      each stated as a source the auditor would have had to retain silently under the pre-revision contract, and the
      lengthy-rule control (four rules considered; none a removal candidate). The three controls therefore behaved as
      the revised contract requires. Recorded limitation: this run did not re-report the stale `testReviewRouting` /
      `review-routing.py` enumeration gap that the earlier run found and that I verified, so the two runs differ in
      recall; the gap is parked in `docs/ideas.md`.
- [x] 3.4 Run the updated auditor in report mode against the current repository and present, as separate deliverables:
      the audit report; a prioritized list of proposed `AGENTS.md` reductions; an extraction plan for every extraction
      candidate; the measured `AGENTS.md` size before and after implementing the auditor; and the files a separate
      follow-up unit would modify if the owner approves the reductions. Verification: each deliverable is present and
      the figures re-derive from the repository. **Done** — the run's report is the deliverable and is reproduced in
      this change's report: `0 findings (0 merge, 0 removal, 0 route)`, `0 advisory`,
      `~135 accreted (~40 new since 2026-10-11)`, `0 authoring-system gap`, plus the **Instruction-budget ledger**
      (358,092 bytes / 3,630 lines; no size cap in the load path; the effective bound is the pinned model's
      1,000,000-token context window, sourced to the models catalog; the first rule beyond the derived threshold as a
      labelled section-boundary estimate; the size-by-section table; the growth of +731 lines over the 2026-10-11
      report's 2,899; the bytes safely removable reported as _none verified_ rather than a round figure; the must-stay
      list; no on-demand list, because no destination passes the retrieval-path test; no extraction candidate, for the
      same reason; and the post-change verification). The prioritized reduction list and the extraction plans are
      **empty** — nothing passed its test — so the files a follow-up unit would modify are none. `AGENTS.md` measured
      357,100 bytes / 3,622 lines before this change and 358,092 / 3,630 after (+992 bytes, +8 lines). The three
      controls behaved as the new contract requires: the ledger was produced; no lengthy-but-decision-bearing rule was
      reported as a removal candidate; and the extraction analysis's failed retrieval path was reported as a reason to
      retain. **Re-opened by the revision** - its deliverables describe the pre-revision report; the revision's run
      supplies the final ones. **Done (revision)** - the post-revision run's report is reproduced in this change's
      report: `1 finding (0 merge, 0 removal, 0 route)`, `1 advisory`, `~137 accreted`, `0 authoring-system gap`, plus
      the ledger (3,632 lines / 358,230 bytes; no size cap in the load path; the effective bound reported as the model's
      context window, which the repository does not configure; the first rule beyond a threshold reported as none
      derivable, since no threshold exists; the size-by-section table with the six largest gotcha sub-clusters; the
      growth of +733 lines over the 2026-10-11 report's 2,899; the bytes safely removable as approximately zero, the
      single finding being an addition; the must-stay list; the on-demand list marked aspirational because no
      destination yet carries the complete contract; the extraction candidates with their retention reasons; and the
      post-change verification), one **extraction opportunity** (the 23,431-byte check-writing gotcha cluster,
      classified as incident history / diagnostic evidence, its proposed destination a design hypothesis), and the
      lengthy-rule control (the premise-interrogation invariant governs a decision and is not a removal candidate). Its
      verdict on the revision: every class was exercised, and the opportunity it reported is one it states it would have
      had to retain silently under the pre-revision contract.
- [x] 3.5 Run the `lesson-capture` subagent over the diff, apply its durable proposals, and record the applied net
      `AGENTS.md` delta on this task. **Also report** any rule this change obsoletes or makes redundant. Verification:
      the applied delta measured with `git diff -U0 AGENTS.md`, or the skip and its reason. **Done** — the change
      touches `AGENTS.md`, so the skip condition cannot hold. The run returned 4 durable proposals (3 additions, 1
      retirement) and 1 authoring-system recommendation. Applied: A1 (the manual check's changed set derives from
      `git status --porcelain --untracked-files=all`, since `git diff --name-only` omits the untracked change dir) and
      A2 (a bare `|` in a markdown table row splits the cell, so escape it `\|`) merge into the Formatting bullet, with
      the marker at that rule's end; A3 (a heading at column zero ends a markdown list, so a script's boundary search
      must not cross it) merges into the Programmatic-edits bullet. R1's ledger half trims the auditor bullet's figure
      list to a pointer at the requirement; its class-enumeration half is **not** applied — a pre-existing
      single-sourcing question outside the capture's stated subject, left for the auditor's next run. The recommendation
      (a member-grep rule for `openspec/config.yaml`'s `rules.tasks`) is applied as a `rules.tasks` entry, its
      obligation already stated in `AGENTS.md` twice. The applied net `AGENTS.md` delta is **+5 lines / +555 bytes**
      (357,537 → 358,092 bytes; 3,625 → 3,630 lines), measured after `spotlessApply`; the capture's own prediction (+3
      lines, or +2 with R1) did not match the reflow.
- [x] 3.6 Carry the deep-review routing decision as a task: derive the floor with
      `python3 scripts/review-routing.py --change optimize-agents-auditor-for-instruction-budget`, decide at or above
      it, and record the outcome and the signals it relied on here. Verification: the recorded outcome matches the
      script's. **Done** — `python3 scripts/review-routing.py --change optimize-agents-auditor-for-instruction-budget`
      returns `RECOMMENDED` on the `repeated-class` signal (three ledger rows repeat a prior class). No `REQUIRED`
      signal: the change touches no public contract, domain surface, persistence, security declaration, or
      infrastructure path, and its delta carries no `ADDED`/`REMOVED`/`RENAMED` block. The `RECOMMENDED` resolution is
      the owner's explicit decision at the manual review — the evidence for one deep pass (the definition's new sections
      and the audit run's deliverables are its only behavioral verification, and the run is served from the registered
      body) against deferring it.
- [x] 3.7 Ask the user for the manual review pass once 3.2 is clean and 3.5 is done; do not commit, push, archive, or
      apply any reported reduction before they approve. Verification: the request is in the session and the outcome is
      the owner's. **Asked** — the request and the run's deliverables are in the session; the outcome is the owner's.
      **Re-opened by the revision** - the earlier request is superseded; the manual pass is owed on the revised change.
      **Asked (revision)** - the request and the revision's deliverables are in the session; the outcome is the owner's.

## 4. The change's own records

- [x] 4.1 Record the pre-write derivation pass (every unit owes it): the claims table is in `design.md`, its rows
      derived from the installed CLI's instruction-loading implementation, the models catalog, the three prior audit
      reports, the pre-change definition, command, and spec, and the describing sites the design's sweep names.
      Verification: every `scripts/claim-scan.py` candidate (base `origin/main`) is a row or a listed dismissal.
      **Done** — the pass ran; the table and the by-class dismissals are in `design.md`.
- [x] 4.2 Append this unit's review-round rows to `docs/review-rounds.tsv` as each round's report arrives. Verification:
      one row per round, no duplicate. **Done** — eight rows appended, one per round as its report arrived, with no
      duplicate (`verifyUniqueReviewRounds` green).
- [ ] 4.3 **Owed — for the archive commit**: the `agent-skills` `## Purpose` enumerates what the auditor reports ("the
      merge, removal, and route candidates it reports, and the authoring-system gaps it traces to a mechanism that
      produces an artifact") and now omits the budget ledger and the extraction disposition. A delta cannot carry a
      Purpose, so the edit lands with the archive. Verification: the Purpose names both.
- [x] 4.4 Record, in the change's report and in `docs/ideas.md`, that applying an `extraction` candidate is out of this
      change's scope and belongs to the standing reduction unit (whose procedure the change widens only by the
      enumeration and the routing). Verification: the parked entry's text and the report's statement. **Done** — the
      routing is written into the reduction unit's step 2 and its new step 4 bullet, so no work is deferred; the
      residual gap (the move's own procedure — creating or changing the destination, moving the content, adding the
      retained pointer) is parked in `docs/ideas.md` under 2026-10-18, and the change's report names the out-of-scope
      boundary.

## Workflow follow-up

- Archive the change after the owner approves, 3.3 has run, and 4.3 is applied; the archive commit rides this PR.

## 5. The extraction-opportunity revision

- [x] 5.1
      `openspec/changes/optimize-agents-auditor-for-instruction-budget/specs/showcase/quality/agent-skills/spec.md` -
      the audit requirement gains the **extraction opportunity** class (its non-applicability, its bounded conditions,
      and its ten fields), the destination mapping derived from the content's class with the no-generic-destination and
      new-destination-needs-an-owner rules, and the opportunity half of the retrieval-path rule; the ledger paragraph
      gains the `extraction opportunities` subsection; the reduction-unit requirement states that an opportunity is
      never applied. Scenarios: six added to the audit requirement (29 there now) and one to the reduction-unit
      requirement (9 there). Verify with `openspec validate --changes` that no scenario is dropped or renamed.
      **Done** - written and validated at the revision.
- [x] 5.2 `.opencode/agent/agents-auditor.md` - the extraction analysis gains the **opportunity** class: the
      classify-then-search workflow, the content-class to destination mapping table, the ten-field opportunity table,
      the retrieval-path rule for an opportunity (no false pass; record the blocking precondition; the dedicated change
      creates the target, adds the pointer, and runs the test), and the report contract's `Extraction opportunities`
      subsection. Verification: read it against the delta's obligations clause for clause. **Done** - the extraction
      analysis is now a seven-step workflow (identify, classify, derive, search, candidate, opportunity, retain), with
      the class-to-destination table, the candidate's eight conditions and ten-field plan, the opportunity's ten-field
      table and its three-step retrieval-path rule, and the report contract's opportunities subsection. Read clause for
      clause against the delta.
- [x] 5.3 `.opencode/commands/audit-agents.md` - its step-1 read-list and step-2 report list name the opportunities
      subsection and that it is excluded from the verdict. Verification: every clause resolves to the definition.
      **Done** - step 1's read-list names the opportunity class and that it is excluded from the verdict, and step 2's
      report list names the ledger's `Extraction opportunities` subsection.
- [x] 5.4 `.opencode/commands/apply-audit-reductions.md` - an extraction **opportunity** is never presented or applied,
      because it is not a candidate. Verification: it matches the reduction-unit requirement's body. **Done** - step 4
      gains a bullet stating that an opportunity is never presented or applied, matching the reduction-unit
      requirement's body.
- [x] 5.5 `AGENTS.md` and `README.md` - the auditor bullet and the audit prose name the new output class. Verification:
      `./gradlew verifyInstallCommands verifyDocumentedPins verifyCapturedMarkers` pass. **Done** - the `agents-auditor`
      bullet names the opportunity class, and the README's prose, agent-table row, and `/audit-agents` row name it.
      `verifyInstallCommands`, `verifyDocumentedPins`, and `verifyCapturedMarkers` pass. `AGENTS.md` moved 358,092 ->
      358,230 bytes and 3,630 -> 3,632 lines.
- [x] 5.6 Re-run the loop (3.2), the auditor (3.3, 3.4), and the capture if the revision's diff warrants one, after the
      revision lands; the numbering continues from round 9. Verification: the last round reports nothing remains, and
      the post-restart run is recorded. **Done** - the loop re-ran after the revision (rounds 9-11, clean at round 11)
      and the auditor re-ran in report mode; the capture was not re-run, since the revision's diff is spec/definition
      prose that the round-11 pass reviewed and the unit's capture already landed. The post-restart registration
      question is unchanged: this run read the revised definition from disk and exercised every class, which
      demonstrates the behavior, but a further restart is what makes the registration definitive - recorded on 3.3,
      which remains the archive gate.
