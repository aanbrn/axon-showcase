# Tasks — vision obtains pixels before it reports

The unit's record is `docs/units/fix-vision-image-loading/unit.md`.

## 0. The pre-write pass

- [x] 0.1 The review gate's pre-write derivation pass ran, seeded with `python3 scripts/claim-scan.py` over the staged
      artifacts; each candidate is resolved into `design.md`'s claims table or dismissed beside it, which states one
      reason per candidate kind. Verify: `design.md` carries the table and the dismissal line, and every scanned
      candidate is accounted for. Verified: the scan's candidates are the claims table's figures, the change's own
      statements and paths, and its verification commands; the dismissal line gives one reason per kind.

## 1. The definition

- [x] 1.1 `.opencode/agent/vision.md`: replace the single loading sentence with the ordered procedure — navigate to the
      `http(s)://` URL (`file://` stays blocked, so ask the caller to serve a local artifact over HTTP), call the
      browser's screenshot tool on the loaded page and inspect the image it returns, and treat navigation metadata or an
      accessibility snapshot as not being visual evidence. Verify: the definition states the two steps in order and
      names the snapshot as not-evidence. Verified: the definition states the two steps in order and names navigation
      metadata and an accessibility snapshot as not being visual evidence.
- [x] 1.2 The same file: state that any scaling or incomplete-frame limitation affecting the requested assessment is
      reported, and that a run whose navigation fails or whose capture returns no image reports the review as not
      performed, with the tools' own outputs under "Not assessable from this image", rather than returning a review of
      nothing. Verify: both rules are present, and the read-only boundary and the three report groups are unchanged.
      Verified: the scaling/incomplete-frame rule and the not-performed rule are present with the one trigger (a failed
      navigation or a capture that returns no image); the read-only boundary and the three report groups are unchanged.
- [x] 1.3 Confirm nothing else in the definition contradicts the procedure. Verify: `grep` for the old loading sentence
      finds no residue, and the file reads as one procedure rather than an instruction plus an appendix. Verified:
      `grep` for the old loading sentence finds no residue, and the file reads as one procedure.

## 2. Verification of the behaviour

- [x] 2.1 Record the diagnosis probe that isolated the cause: hand `vision` a served screenshot URL with the bare
      hand-off, then hand it the same URL with the explicit navigate-then-screenshot procedure. Verified: with the bare
      hand-off the reply reported no pixels — the browser returned a page wrapper and an accessibility snapshot, and
      `webfetch` refused `image/png`; with the two-step procedure the reply read `.playwright-mcp/replicas3.png` as five
      deployment rows with the command service at `2` and `2`.
- [x] 2.2 Serve a real screenshot and a closed port (`python3 -m http.server 18099 --bind 127.0.0.1` from the repository
      root, plus a port nothing listens on), and hand both URLs to `vision` **without coaching** the procedure. Verify:
      the screenshot yields a review naming the image's actual content, and the closed port yields an explicit "not
      performed" naming the tools' outputs rather than a review. Verified, uncoached: a served screenshot produced a
      review naming the panel's eight tiles, their labels and Up; a closed port produced "visual review not performed"
      quoting `net::ERR_CONNECTION_REFUSED` and no findings. That probe exercises the navigation-failure branch of step
      4's trigger; the other branch — a successful navigation whose capture returns no image — is covered by the
      definition's rule and was not observed.
- [x] 2.3 `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` and `openspec validate --all`. Verify: both pass.
      The definition inventory is asserted by the `build` job's own step in CI, not by a local task. Verified: `check`
      succeeded and `openspec validate --all` reports 27 items.
- [x] 2.4 `./gradlew spotlessApply` after the last edit, then confirm the definition's frontmatter is unchanged:
      `git diff` shows no change inside the `---` block. Verify: the description still reads as one value, and the tree
      carries no other formatter change. Verified: `spotlessApply` ran, and the frontmatter diff is empty — the change
      touches only the body, so the description still parses as one value.

## 3. The review gate and the records

- [x] 3.1 The proposal stage's quick review: `review-quick` over the planning artifacts until a clean round, with each
      round's row and findings under `docs/units/fix-vision-image-loading/`. Verify: `--expect-clean` and
      `--session-continuity` pass. Verified: rounds 1–5 in `rounds.tsv`, the closing round 5 with no finding class;
      `--expect-clean` and `--session-continuity` pass.

- [x] 3.2 The owner's manual pass over the proposal. Verify: the approval is recorded before the definition is edited.
      Verified: the owner approved the proposal after the advisor's formulation and instructed the implementation; the
      edit followed that approval.

- [x] 3.3 The implementation stage's quick review. Verify: the closing round is clean and the ledger checks pass.
      Verified: rounds 6–9 in `rounds.tsv` cover the implementation, the closing round 9 with no finding class;
      `--expect-clean`, `--session-continuity` and `--review-ready` pass.

- [x] 3.4 The deep-review routing decision on the completed diff. Verify: the outcome and its signals are recorded.
      Verified:
      `python3 scripts/review-routing.py --unit fix-vision-image-loading --change     fix-vision-image-loading` reports
      RECOMMENDED on one `[repeated-class]` signal (three rows repeat a prior class, all in the proposal rounds); the
      owner resolves whether to run the deep review.

- [x] 3.5 The deep review, when 3.4 selects one, with its findings applied and re-reviewed. Verify: the pass's status
      and its findings' disposition are recorded. Verified: The owner decided to skip the deep review on the advisor's
      recommendation: the routing's one `[repeated-class]` signal records a drafting discipline failure in the proposal
      rounds, and the change's surface — one paragraph of an agent definition, one delta scenario, two probes, a clean
      loop — leaves no implementation risk the pass would reach.

- [x] 3.6 The technical advisor's consultation on the implementation, when the owner authorizes it. Verify: the
      authorization and the findings' disposition are recorded. Verified: the owner authorized the pass ("Перед ручным
      ревью сделай прогон советника") and asked for it to run to agreement ("Отревьювь с советником до согласия"). Round
      1: retain the direction; two confirmed problems and one hypothesis — a claims row sourced from the edited file, an
      overstated failure-path coverage, and a categorical "snapshot is empty" assertion. All three were fixed (the row
      pins `b3402345`; the design's Risks and task 2.2 state that only the navigation-failure branch was observed; the
      definition and its planning repetitions state the rule without the universal claim). Round 2: one broader claim
      cell survived, corrected to name the diagnosis probe; the advisor then closed the consultation — "No further
      advisor round is needed solely to verify that literal correction" — and recommended skipping the deep review after
      that correction.

- [x] 3.7 The owner's manual pass over the finished unit. Verify: the approval is recorded, and nothing is committed,
      pushed, or merged before it. Verified: The owner reviewed the implementation and approved it ("Реализацию одобряю.
      Отгружай."), after the advisor's two rounds closed in agreement; the delivery request was in the same message, and
      nothing was committed before it.

- [x] 3.8 `docs/units/fix-vision-image-loading/process-defects.md`: one record per defect observed, or the statement
      that none was. Verify: the file exists before 3.7 is requested. Verified: Written as
      `docs/units/fix-vision-image-loading/process-defects.md`: one record (`#1`, a screenshot in the repository root
      entering the index through `git add -A`).
