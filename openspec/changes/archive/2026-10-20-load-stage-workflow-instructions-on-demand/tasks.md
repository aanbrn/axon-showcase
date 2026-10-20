# Tasks

## Planning verification (completed in the proposal round)

- [x] P.1 Measure the baseline, both procedure blocks, both complete drafts, and the capture-contract edits on scratch
      copies — verification: every figure in `design.md`'s baseline table reproduces from its stated command.
- [x] P.2 Run the pre-write derivation pass and `scripts/claim-scan.py`; resolve each candidate as a claims-table row or
      a class-level dismissal — verification: the design's claims table and Dismissals section cover every kind the scan
      prints.
- [x] P.3 Run the `review-quick` loop over the planning artifacts, preserving one reviewer session across rounds and
      appending each round's row to `docs/review-rounds.tsv` — verification: one `created` session is continued on every
      later round, and the loop ends on a round that reports nothing remains.
- [x] P.4 Derive the deep-review routing floor for the planning artifacts — verification:
      `python3 scripts/review-routing.py --change load-stage-workflow-instructions-on-demand` reports `REQUIRED` with
      `requirement-block-added`. **Phase:** the deep review itself runs in the implementation phase, from the completed
      implementation, before the change is reported done; it is task 6.3, not a planning step.
- [x] P.5 Probe the load mechanism in scratch repositories (A/B/C/E) and record what each probe did and did not exercise
      — verification: the design's retrieval-evidence section states the 430-byte synthetic root and the untested parts.

## 1. Owner gate and baseline

- [x] 1.1 **Owner decision gate.** Present the revised trade-off, the narrow placement delta, and the retention
      fallback; record the owner's answer in the change directory. If the owner rejects the delta, stop and apply group
      7 — verification: the recorded answer names the chosen option and the remaining groups match it. **Result:** the
      owner approved option C and the revised delta on 2026-10-20.
- [x] 1.2 **Re-verify the baseline** on the implementation branch: freshly fetched `origin/main`, clean tree, the two
      blocks still 2,043 and 3,632 bytes, `AGENTS.md` still 334,308 bytes — verification: the recorded `wc -c -l` output
      matches the design, or the design is corrected before proceeding.

## 2. Root entry point and capture routing

- [x] 2.1 **Replace both procedure blocks** in `AGENTS.md` (lines 767–848 at the baseline) with the design's complete
      root draft — verification: both headings match once, the replacement measures 2,293 bytes / 41 lines, and the
      replaced span is 5,676 bytes / 82 lines.
- [x] 2.2 **Preserve the one scanned upstream reference.** Confirm `anomalyco/opencode#50247` and its `retires when`
      clause remain in the root cloud entry point — verification: the extraction grep prints it, and
      `./gradlew upstreamReferences` reports an `AGENTS.md` citation.
- [x] 2.3 **Add the capture destination to the three `AGENTS.md` enumerations** — capture step 2, the "Every proposal
      names the existing rule" decision point, and the merge-time capture bullet — verification: each of the three sites
      names the `ship-change` skill, and the file grows by 134 bytes.
- [x] 2.4 **Add the destination to the four `lesson-capture.md` paragraphs** (frontmatter description, opening
      paragraph, "You are given" bullet, "artifact it belongs to" paragraph, which carries two occurrences) —
      verification: `grep -c 'ship-change' .opencode/agent/lesson-capture.md` is at least 5, and the file grows by 209
      bytes.
- [x] 2.5 **Confirm the delta's capture requirement** names the same destination, so the definition and the spec agree —
      verification: `openspec validate --change` passes and the MODIFIED block's two enumerations carry the skill.

## 3. Destination skill

- [x] 3.1 **Create `.opencode/skills/ship-change/SKILL.md`** with the design's complete draft, including the frontmatter
      description and **its own `Source:` line in each procedure section**, each quoting its replaced `AGENTS.md`
      heading verbatim — verification: `test -f`; two `Source:` lines; each quoted heading matches a live root heading
      (`grep -F` on both).
- [x] 3.2 **Check the skill is discoverable and loadable** — verification: the skill inventory lists `ship-change` with
      its description, and invoking it returns the body.

## 4. Implementation verification

- [x] 4.1 **Obligation preservation.** Walk the design's content-classification tables unit by unit and confirm each
      row's text survives at root or in the skill, including every named identifier — verification: a checklist naming
      each identifier and its file, with none absent from both. **Result:** all 17 identifiers are present in the skill
      — `gh pr merge --squash --delete-branch --admin`, `main-require-pr-on-merge`,
      `required_approving_review_count: 1`, `aanbrn`, `bypass_mode: always`, `mergeStateStatus`, `gh pr update-branch`,
      `openspec archive <change> --yes`, `Closes #<issue>`, `is:issue is:open`, `github.handler.ts`,
      `createPR(base, branch, title, body)`, `summarize`, `opencode/…`, `docs/audits/`, `skip_specs: true`, and
      `pull_request_review`; the root carries the authorization boundary, ordering, outcome, and missing-target stop, so
      no identifier is absent from both.
- [x] 4.2 **Semantic coverage in a disposable clone.** With no usable remote and a copy of the complete root text and
      skill, run one fresh session for each form — push, push with an explicit refspec, open a pull request, archive,
      merge, "CI is green; finish it", complete the cloud PR, a direct middle-step command, and an API/UI equivalent —
      plus one resumed session, one with the skill renamed, and one told to act without reading the skill —
      verification: each transcript shows the complete procedure available before the governed action; the
      missing-target run stops; a request to skip the read neither bypasses the requirement nor produces a governed
      action (the agent may load the procedure or stop); nothing is published, archived, or merged. **Result:** every
      probed form made the complete procedure available before the first governed action. The missing-skill run stopped
      and reported; the skip request loaded the skill anyway and refused to push; the resumed session reused the loaded
      procedure without re-reading. The explicit-refspec run's prerequisite read succeeded, and the run then timed out
      without publishing. Transcripts are recorded under `$TMPDIR/opencode/probe-*.log`, not committed.
- [x] 4.3 **Pointer resolution and the complete-read requirement.** Confirm the root names the path and requires reading
      it completely before the governed decision — verification: the path resolves (`test -f`), the root's wording is
      present verbatim, and 4.2's skipped-read run performs no governed action. **Result:** the root names the path and
      requires the complete read; the missing-skill probe stopped, and the skip probe loaded the skill and refused to
      act.
- [x] 4.4 **No lost unique terms.** Sweep the removed text's distinctive tokens across `AGENTS.md`, `README.md`,
      `docs/`, `openspec/specs/`, and `.opencode/` — verification: each token appears in the skill, the root, or the
      capture enumerations. **Result:** every identifier from the census appears in the root or the skill; the two
      phrase checks that differ only in case ("Say so explicitly", "Strip the closing keyword") were confirmed
      case-insensitively, and no token survived only in the deleted prose.
- [x] 4.5 **Capture routing.** Confirm a shipping-procedure lesson reaches the skill rather than root — verification:
      the five `lesson-capture.md` occurrences and the three `AGENTS.md` sites name it. **Result:** 5 occurrences in
      each file; the behavioural smoke-run of the edited definition is task 6.6, deferred until after a reload.

## 5. Formatting and checks

- [x] 5.1 Run `./gradlew spotlessApply`, then `./gradlew spotlessCheck` — verification: both succeed, with no skipped
      file reported for the markdown target.
- [x] 5.2 Run `openspec validate load-stage-workflow-instructions-on-demand --strict` and `openspec validate --all` —
      verification: both print valid.
- [x] 5.3 Run the applicable root gates — `./gradlew verifyCapturedMarkers verifyLargeFiles upstreamReferences` —
      verification: each succeeds, `upstreamReferences` still lists `anomalyco/opencode#50247`, and the new skill is
      under the 512 KiB limit.
- [x] 5.4 Run the manual 120-character check over the changed set derived with
      `git status --porcelain --untracked-files=all` — verification: the `perl -CSD -lne` recipe prints nothing for
      non-markdown files; markdown is formatter-gated.

## 6. Review gate (implementation phase)

- [x] 6.1 **Re-derive the claims table** from the implemented diff and re-run `scripts/claim-scan.py` — verification:
      every candidate is a row or a class-level dismissal, with the figures measured after the last edit. **Result:**
      the design's implementation-verification table and the re-run scan at rounds 22-25 record it.
- [x] 6.2 **Run the `review-quick` implementation round** in the same reviewer session where possible, appending the
      round row — verification: the loop reports nothing remains, and the ledger row carries the session identifier and
      transition. **Result:** rounds 22-25 in the same session; round 25 reports nothing remains.
- [x] 6.3 **Run the deep review** from the completed implementation and the diff — the planning derivation is `REQUIRED`
      (`requirement-block-added`) — verification: the `review-thorough` pass has completed and its findings are fixed
      before the change is reported done. **Result:** the `review-thorough` pass ran (routing `REQUIRED`, signal
      `requirement-block-added`). Every finding is fixed and verified: S1, 6.1/6.2 checked with results; N1, "either
      skill" in the definition; N2, both non-normative tokens restored in the skill; N3, the census row relabelled. No
      finding was accepted without correction.
- [x] 6.4 **Run the implementation capture** and record its applied net `AGENTS.md` delta, or the skip and its reason —
      verification: the capture record exists on this task. **Result:** three additions applied (A1 normative amendment
      over interpretation; A2 an unmeasurable cost is a model; A3 a stand-in probe evidences the stand-in) and ASR1
      applied as a `design` rule in `openspec/config.yaml`; measured net `AGENTS.md` delta +840 bytes / +8 lines after
      `spotlessApply` (331,059 → 331,899); the later flag correction took it to 331,864 (−35).
- [x] 6.5 **Ask for the owner's manual review pass.** Do not commit, push, open a PR, archive, or merge before the
      answer — verification: the request is visible and no governed action has run. **Result:** the request is in the
      session's final message; no commit, push, PR, archive, or merge has run.

- [x] 6.6 **Pre-archive: smoke-run the edited capture definition.** Run the `lesson-capture` agent in a fresh CLI
      session (a new process loads the edited definition from disk) with a concrete shipping-procedure correction, and
      verify the routing — verification: the run names the `ship-change` skill and the Ship a change procedure, proposes
      the correction there rather than in `AGENTS.md`, preserves the promotion gate and decision difference, and edits
      nothing. **Result:** ran in a fresh `opencode run --standalone --auto --agent lesson-capture` process with the
      prompt "Run the implementation capture for unit load-stage-workflow-instructions-on-demand … a
      `gh pr update-branch` run failed because the PR branch conflicted with `main` … add a failure branch to the Ship a
      change procedure in the `ship-change` skill" (full text in `$TMPDIR/opencode/capture-smoke2.log`); the first
      attempt ended in a transport error before output, the second exited 0. Evidence the edited definition loaded: the
      run routed to the `ship-change` skill and quoted the skill's Failure handling. It named the skill and the Ship a
      change procedure, proposed the correction in the skill, stated the existing rule it extends and the decision
      difference, ran the promotion gate, made no edits, and noted that `AGENTS.md:781` assigns the BEHIND recovery to
      the skill. The seed's mechanism was verified against `gh` (the CLI refuses update-branch on a conflicting PR and
      prints the local recovery). The run also surfaced an invalid flag, `gh pr merge --update-branch`; it was dropped
      from the skill and from the BEHIND gotcha, and the figures re-measured.

## 7. Retention fallback (only if the owner rejects the extraction)

- [x] 7.1 **Leave both procedures at root**, revert groups 2 and 3 (including the capture-contract edits, which exist
      only to route lessons to the new skill), and set `skip_specs: true` — verification: `git diff` against those paths
      is empty and `openspec validate --change --strict` passes with the marker. **Not applicable:** the owner approved the
extraction on 2026-10-20, so group 7 was not applied.
- [x] 7.2 **Record the retention decision durably**, naming the measured figures and the parked idea that stays open —
      verification: the change report names both. **Not applicable** for the same reason.

## Workflow follow-up

- After the owner approves: commit, push, open the pull request, wait for `build`, archive the change in the same pull
  request, and merge — per the root entry point and the `ship-change` skill this change lands.
- At the archive, refresh `showcase/quality/agent-skills`'s `## Purpose`: its on-demand-skills enumeration names the
  `check-and-test-authoring` reference and the `setup-*` skills, and this change adds `ship-change` to that class.
- After merge, if generalization is considered, count F (fresh sessions) and L (distinct complete skill loads) manually
  over a period and apply `2,163 × F − 6,703 × L`; this unit adds no instrumentation. Do not extend the pattern to other
  procedures from this pilot.
- Treat relocating the action's PR-derivation paragraph as independent work: first verify that the `merge-governance`
  audit-workflow requirement owns the general rule, then propose the move in its own change.
