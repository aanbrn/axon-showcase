# Tasks

Workspace: unit=carry-closure-disposition-in-upstream-report;
path=/Users/afanasyev/Development/axon-showcase-carry-closure-disposition;
branch=carry-closure-disposition-in-upstream-report; writer=ses_e8d42f936ffeP47ynKyIebqiZK

## 1. Implementation

- [x] 1.1 Add `build-logic/src/main/kotlin/UpstreamReferenceState.kt`: an `internal object` whose `parse(raw)` turns the
      `gh api` triple (`.state + "|" + (.state_reason // "") + "|" + (.closed_at // "")`) into `open`,
      `closed (<state_reason>) <yyyy-MM-dd>`, `closed <yyyy-MM-dd>`, or `unresolved`, with a KDoc stating the vocabulary
      and the absent-reason branch. Verification: `./gradlew :build-logic:compileKotlin`.

  **Done**: the object is added with the four-branch vocabulary; `:build-logic:compileKotlin` succeeded.

- [x] 1.2 Add `build-logic/src/test/kotlin/UpstreamReferenceStateTests.kt` with the disposition battery: `open`, a
      `completed` closure, a `not_planned` closure, a `duplicate` closure, a closure with no reason, a trailing newline
      on a real `gh` payload, a blank payload, and an unrecognized state. Each case asserts the rendered string.
      Verification: `./gradlew :build-logic:test`, and the known-bad proof in 2.2.

  **Done**: eight cases, one decision each; `:build-logic:test` green, and 2.2 records the control.

- [x] 1.3 Change `UpstreamReferencesTask.resolveState` to ask for `state_reason` in its `--jq` program and delegate the
      rendering to `UpstreamReferenceState.parse`, removing the parsing it currently does inline. Verification:
      `./gradlew :build-logic:test` and the report run in 2.3.

  **Done**: `resolveState` now builds the `state|state_reason|closed_at` query and returns
  `UpstreamReferenceState.parse(output)`; the inline `when` is gone, and 2.3 reads the report.

- [x] 1.4 Confirm the delta spec at
      `openspec/changes/carry-closure-disposition-in-upstream-report/specs/showcase/quality/merge-governance/spec.md`
      carries all eight existing scenarios verbatim in their relative order plus the two added ones, and that the
      amended clause matches the quote table in `design.md`. Verification: `openspec validate --all`.

  **Done**: the eight scenarios are carried verbatim in order with the two added after the state-related one;
  `openspec validate --all` exits 0 for the change.

- [x] 1.5 Update `README.md`'s update-check paragraph so the state enumeration names the closure disposition ("its state
      (open / closed with the closure reason the API reports / unresolved)") instead of "(open / closed / unresolved)".
      Verification: `rg -n 'open / closed' README.md` returns the updated sentence and no other copy.

  **Done**: README's update-check paragraph reads "its state (open / closed with the closure reason the API reports /
  unresolved)"; the grep returns that one line.

- [x] 1.6 Record the `not_planned` closure of `anomalyco/opencode#49127` in the `AGENTS.md` bullet about the `opencode`
      action's cache warning, beside its existing retirement condition, so a later sweep does not re-derive it.
      Verification: `rg -n -A2 'opencode#49127' AGENTS.md`.

  **Done**: the bullet states the issue "closed `not_planned`, so no fix is coming from it"; the report row reads
  `closed (not_planned) 2026-10-11`.

- [x] 1.7 Record the `not_planned` closure of `anomalyco/opencode#49904` in the `AGENTS.md` bullet that cites it and in
      `.opencode/skills/setup-agent-tools/SKILL.md`'s caveat beside it, both keeping the behavioral retirement condition
      (`mcp add` emitting the flat shape, still unmet on the installed 2.1.4). Verification:
      `rg -n 'opencode#49904' AGENTS.md .opencode/skills/setup-agent-tools/SKILL.md`.

  **Done**: both sites state the closure and keep the behavioural condition; the report row reads
  `closed (not_planned) 2026-10-15`.

- [x] 1.8 Record the `not_planned` closure of `anomalyco/opencode#50247` and the unmerged community fix
      `anomalyco/opencode#50277` in the `AGENTS.md` bullet about a `/oc` in a review body. Verification:
      `rg -n 'opencode#50247' AGENTS.md`.

  **Done**: the trigger bullet names the `not_planned` closure and its unmerged fix `#50277`; the report row reads
  `closed (not_planned) 2026-10-20`.

- [x] 1.9 Remove the implemented idea ("Carry a closed reference's disposition in the `upstreamReferences` report") from
      `docs/ideas.md`, keeping the section's other entries, and grep the file for the removed text. Verification:
      `rg -n 'closed reference.s disposition|state_reason' docs/ideas.md` returns no live copy.

  **Done**: the entry is gone and the 2026-10-19 section keeps its other entries; the grep matches nothing in
  `docs/ideas.md`.

## 2. Verification

- [x] 2.1 Re-run the pre-write derivation pass over the final tree: re-derive every row of `design.md`'s claims table
      with the command it names (`gh api` for the reference states,
      `git show 837aaa5284647c245b26db1d2a9bc87d4405dd81:<path>` for the base-revision facts), and confirm the figures
      still hold. Record any row that moved.

  **Done**: no row moved — the 11 closed references still split 5 `completed` / 4 `not_planned` / 1 `duplicate` / 1
  `null`, `warpdotdev/warp#5550` is still open, the workflow's filters are unchanged, and the base-revision README and
  idea text are as the rows state.

- [x] 2.2 Prove the new test's known-bad side: on a scratch copy, make `UpstreamReferenceState.parse` return the
      pre-change shape (`closed <date>` without the reason) and confirm the `not_planned` and `duplicate` cases fail,
      then restore the file. Record the failing case names.

  **Done**: with the reason dropped from the `closed` branch, exactly the four reason-carrying cases failed — "A
  completed closure reports its disposition beside the closed date", "A duplicate closure reports its disposition rather
  than reading like a fix", "A dropped fix reports its not_planned disposition", "The trailing newline of a real gh
  payload does not reach the state" — while the absent-reason, open, and unresolved cases stayed green; the file was
  restored and its checksum matches the pre-perturbation one.

- [x] 2.3 Run `./gradlew upstreamReferences` and read `build/upstream-references/report.txt`: every closed row carries
      its disposition (`closed (not_planned) 2026-10-11`, `closed (duplicate) 2026-03-27`), `micromatch/braces#72`'s row
      reads `closed 2026-10-05` for its absent reason, and no row reads `unresolved`.

  **Done**: the report shows `closed (not_planned)` on the four declined references, `closed (duplicate) 2026-03-27` on
  `warpdotdev/warp#6320`, `closed 2026-10-05` on `micromatch/braces#72`, `open` on the open ones, and no `unresolved`
  row.

- [x] 2.4 Run `./gradlew :build-logic:test :build-logic:compileKotlin` and record the passing count.

  **Done**: the suite is green; `UpstreamReferenceStateTests` runs nine cases, beside the existing
  `UpstreamReferencePatternTests` and `UpstreamReferenceScopeTests`.

- [x] 2.5 Run `./gradlew spotlessApply` and then `./gradlew spotlessCheck`, after the last edit to a formatted file
      (`AGENTS.md`, `README.md`, `docs/ideas.md`, the Kotlin sources, the change's markdown).

  **Done**: `spotlessApply` then `spotlessCheck` succeeded after the capture's `AGENTS.md` clause and the two routed
  code changes, and again after each later review-driven wording fix; the last run follows this note's own edit.

- [x] 2.6 Run the 120-character check over the change's whole changed set, derived with
      `git status --porcelain --untracked-files=all` and `perl -CSD -lne 'print if length > 120'` on the files the
      formatter does not own (the Python/ASCII check is for non-markdown, non-Java/Kotlin content).

  **Done**: the Kotlin sources and the `.openspec.yaml` are clean, and no added prose line exceeds 120. The only longer
  added lines are `design.md`'s table rows, which the formatter cannot wrap. The recipe was proven on a 121-character
  control line first.

- [x] 2.7 Run `openspec validate --all` and confirm the change and its delta validate.

  **Done**: `openspec validate --all` exits 0 for the change and its delta; the warnings it prints are on other
  capabilities' long requirement bodies.

- [x] 2.8 Derive the deep-review routing decision with
      `python3 scripts/review-routing.py --change carry-closure-disposition-in-upstream-report`, decide at or above the
      floor it prints, and record the outcome and the signals it relied on here.

  **Done**: the derivation printed `RECOMMENDED` on one signal, `[repeated-class] 2 rows repeat a prior class` — round 2
  re-raised `claim-missing` and `claim-unreproducible` on the change's own records (a dismissal's completeness clause
  and a `Done` note's universal), and round 3 re-raised `formatting` on a claims-table cell. Resolved as **not run**,
  explicitly and on those instances: each repeat was a claim or a markdown-table defect in the change's artifacts,
  re-derived rather than patched, and round 4 confirmed all three clean; no round reported a defect in the parsing
  logic, the test, or the report's output, whose behaviour the unit cases, the recorded known-bad control, and the
  real-corpus report pin.

- [x] 2.9 Run the `lesson-capture` subagent over the diff, the review findings, and the change dir; apply its durable
      proposals or record the skip condition and its reason, with the applied net `AGENTS.md` delta.

  **Done**: the skip condition did not hold (three rounds raised classes beyond the local set), so the subagent ran and
  proposed one durable clause — applied to the `AGENTS.md` upstream-reference bullet with the unit's `captured:` marker:
  "A `not_planned` disposition means that closure brought no fix, so state that beside the note and re-derive its
  retirement condition to one that can fire." Its other candidates were `already covered` by existing rules, and it
  routed two items to the change dir, both applied: the `--jq` program now lives beside the parser
  (`UpstreamReferenceState.QUERY`, built from its separator) instead of being duplicated in the task, and the degenerate
  closed-without-date branch gained a case. Net `AGENTS.md` change for the unit: +27/-22 against the base revision.

- [ ] 2.10 At ship time, dispatch the workflow against the pushed branch
      (`gh workflow run upstream-references.yml --ref carry-closure-disposition-in-upstream-report`) and read the run
      log: the job runs the report and posts the rows with the disposition. Tick this task in the archive commit's edit,
      per `AGENTS.md`'s rule for a verification the local environment cannot run.
