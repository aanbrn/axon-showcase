# Tasks

## 1. The aggregation tool, and its tests

- [x] 1.1 Add `scripts/review-rounds.py`: it takes `--since <YYYY-MM-DD>` and `--record <path>` (default
      `docs/review-rounds.tsv`), and prints the digest's whole review-rounds block — the heading, each row verbatim, the
      `since`-inclusive figures, the window-proper figures (rows dated after `--since`), and the classes raised tallied
      over the window-proper rows. The tally normalizes a label on a row dated on or before the declared pre-canonical
      boundary (`2026-10-15`) through the tool's declared mapping, and reports a label the mapping does not cover under
      its own `unmapped (kept as recorded)` heading. Stdlib only, 3.9-compatible. Verify: running it over the live
      ledger with `--since 2026-10-13` prints an inclusive pair that exceeds the window-proper pair by exactly the rows
      dated `2026-10-13` (their count and their `compliance_misses` sum), and with `--since 2026-10-12` — whose window
      proper holds the 2026-10-13 rows — reports `compliance-miss` under `unmapped` and lists no other label outside the
      reviewer's canonical list. **Done** — `--since 2026-10-13` prints an inclusive pair exceeding the window-proper
      pair by exactly the 43 rows dated 2026-10-13 and their 58 misses, the tally's classes all canonical;
      `--since 2026-10-12` reports `compliance-miss 1` under `unmapped`; `--since 2026-10-16` reports none. Run under
      `/usr/bin/python3` (3.9).
- [x] 1.2 Declare the mapping in `scripts/review-rounds.py` as data: the 54 pairs from #577's table in
      `openspec/changes/archive/2026-10-15-give-the-review-a-canonical-class-list/design.md`, plus identity rows for the
      two canonical labels that table omits (`claim-missing`, `claim-unreproducible`). Verify: the 54 transcribed pairs
      are asserted equal to the archived table by 1.3's test, and the identity rows are asserted present by it. **Done**
      — `PRE_CANONICAL_MAP` declares 56 pairs; 1.3's tests assert the 54 equal to the archived table and the two
      identity rows present.
- [x] 1.3 Add `scripts/test-review-rounds.py` (stdlib `unittest`, loaded by path the way `scripts/test-claim-scan.py`
      loads its subject) carrying the known-good and known-bad cases over its own fixture ledger: the
      inclusive/window-proper split on a fixture that straddles the `since` date; a pre-boundary label normalized to its
      class; a label the mapping lacks reported unmapped; the mapping equal to #577's archived table plus the two
      identity rows; and the mapping covering every distinct label in the real ledger's pre-boundary rows. The fixture
      is the ledger's own substitute here, since the retrospective's historical figures are not reproducible from
      today's ledger (the record grows). Verify: `python3 scripts/test-review-rounds.py` passes, and the known-bad
      control fails — perturb one declared pair (e.g. map `over-claim` to `wording`) and confirm the equality assertion
      goes red, then restore; assert every label the ledger's pre-boundary rows carry is covered except
      `compliance-miss`. **Done** — the suite passes under `/usr/bin/python3`; the control (mapping `over-claim` to
      `wording`) fails exactly `test_the_declared_pairs_equal_the_archived_table`, and the restore passes the suite; the
      ledger's 57 pre-boundary labels leave only `compliance-miss` uncovered.
- [x] 1.4 Register `testReviewRounds` in `build.gradle.kts` (an `Exec` task in the `verification` group, like
      `testClaimScan`) with `inputs.file` for the tool, its test, and `docs/review-rounds.tsv`, and add it to `check`'s
      `dependsOn` beside `testClaimScan`. Verify: `./gradlew testReviewRounds` runs the test, and
      `./gradlew check -PskipITs -Pcoverage.gate.enabled=false --dry-run` lists it as a `check` member. **Done** —
      `./gradlew testReviewRounds` is `BUILD SUCCESSFUL` (1 actionable task executed, configuration cache reused), and
      the dry-run lists `:testReviewRounds` beside `:testClaimScan`/`:testDoctorCharter`.

## 2. The digest and the record

- [x] 2.1 Replace `scripts/experience-analysis.sh`'s inline review-rounds `awk` block with a call to
      `python3 scripts/review-rounds.py --since "${SINCE}"`, and update the script's header comment where it describes
      the block. Verify: `./scripts/experience-analysis.sh 2026-10-13` prints the new block (heading, rows, both figure
      pairs, the tally), and the script holds no remaining `$1 >= s` filter. **Done** — the block is now
      `python3 "$(dirname "$0")/review-rounds.py" --since "${SINCE}"` (the tool prints the heading too, so the digest's
      own `echo` is gone), the header names the tool and what it prints, no `$1 >= s` filter remains, and the digest's
      run prints the heading, the rows, both figure pairs, and the tally.
- [x] 2.2 Update `docs/review-rounds.tsv`'s header comment: it currently says the rows are aggregated by
      `scripts/experience-analysis.sh`, and it states the pre-canonical boundary in prose. Verify: the header names the
      aggregator that reads it (`scripts/review-rounds.py`) and still states that rows recorded before the canonical
      list keep their labels, with no row of the ledger edited. **Done** — the header names `scripts/review-rounds.py`
      as the aggregator and as the applier of the recorded mapping, keeps the pre-canonical boundary sentence, and
      `git diff docs/review-rounds.tsv` changes no data row (this unit's round rows are appended below them, and the
      note records no count of them, which grows while the loop runs).

## 3. The spec

- [x] 3.1 The `MODIFIED` delta for `showcase/quality/agent-skills` at
      `openspec/changes/normalize-the-review-round-digest/specs/showcase/quality/agent-skills/spec.md`, written in the
      proposal round, makes the derived-facts clause state the review-round figures for the window proper beside the
      `since`-inclusive ones, the classes normalized to the reviewer's canonical vocabulary, and a pre-canonical label
      that cannot be normalized reported as such; it carries the requirement's four scenarios in their existing order.
      Verify: `openspec validate normalize-the-review-round-digest --strict` passes, and the block's scenarios match the
      main spec's set and order. **Done** — written in the proposal round; `--strict` is valid, and the block carries
      the requirement's description and all four scenarios verbatim in the main spec's order with only the two intended
      clauses edited.

## 4. The docs

- [x] 4.1 `AGENTS.md` and `README.md` — widen the live enumerations the new tool joins, deriving the sites by grepping
      the set's members (`testClaimScan`, `install-git-hooks.sh`, `claim-scan.py`, `experience-analysis.sh`) across both
      files: `AGENTS.md`'s commit-hygiene `check`-member list gains `testReviewRounds`; its `scripts/`-tool bullet's
      untriggered examples gain `review-rounds.py`; its Python prerequisite's reasons gain it; its sentence naming the
      digest's aggregation owner names the tool; and `README.md`'s prerequisite-table Python row (line 545), which names
      the same reasons, gains it too. Verify: each edited sentence reads against the set's members, and the `check`
      dry-run from 1.4 agrees with the member list. **Done** — the grep for the four members returned five live sites
      (the check-member list, the untriggered examples, the Python reasons, the aggregation-owner sentence, and the
      README row), all five edited; every other hit names a site this change does not move, so each stays true. The
      `check` dry-run's member list matches the edited enumeration.
- [x] 4.2 `docs/ideas.md` — remove the 2026-10-17 "Make the digest's review-round aggregation comparable and quotable"
      entry this change implements, keeping the section's other three entries. Verify:
      `grep -n "comparable and quotable" docs/ideas.md` returns nothing and the `## 2026-10-17` section still holds its
      remaining entries. **Done** — the grep returns nothing, the section keeps its other entries, and S2's "the other
      enabler of that comparison" (which depended on the removed entry) reads "the remaining enabler of the window
      comparison its baseline names".

## 5. Verification

- [x] 5.1 Run the tool over the real ledger at both ends of the boundary: `--since 2026-10-13` (straddling it, window
      proper post-boundary) and `--since 2026-10-12` (whose window proper holds the 2026-10-13 rows). Verify: the
      straddling run's inclusive pair exceeds its window-proper pair by the 2026-10-13 rows' count and miss sum, and its
      tally shows all pre-boundary labels normalized to canonical classes; the `2026-10-12` run lists `compliance-miss`
      under `unmapped` and nothing else; each figure is read from the run's own output. **Done** — the straddling run's
      inclusive pair exceeds the window-proper pair by exactly the 43 rows dated 2026-10-13 and their 58 misses, with
      the tally's classes all canonical and `unmapped: none`; the `2026-10-12` run prints
      `unmapped (window-proper, kept as recorded): compliance-miss 1` and no other non-canonical label.
- [x] 5.2 Run `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`, `openspec validate --all`,
      `./gradlew spotlessApply` followed by `spotlessCheck`, and the manual 120-character check over the changed set
      (the untracked change dir included: `git status --porcelain`) with its positive control. Verify: each command's
      outcome is read from its own run, and a skip the formatter reports counts as a failure for that file. **Done** —
      `check` is `BUILD SUCCESSFUL` with `:testReviewRounds` among the executed tasks; `openspec validate --all` is 27
      passed / 0 failed; `spotlessApply` then `spotlessCheck` are `BUILD SUCCESSFUL` with no `Skipping` line; the
      120-character recipe flags nothing in the two Python tools or the shell script (Spotless owns the markdown), its
      positive control fires on a 121-character line and not on a 120-character one, and the ledger's over-long lines
      are its record rows, one line by contract.
- [x] 5.3 Run `review-quick` over the proposal, design, tasks, and delta, then over the implementation, repeating with
      each prior round's instances passed and appending each round's record line to `docs/review-rounds.tsv`, until the
      review reports no new observation. Verify: the reviewer's verdict lines and the appended ledger rows. **Done** —
      ten rounds in one reviewer session (five on the proposal, five on the implementation), each round's record line
      appended to the ledger as it arrived; rounds 1–4 and 6–9 raised classes, rounds 5 and 10 reported "nothing
      remains" and closed the loop.

## 6. Capture

- [x] 6.1 Run the `lesson-capture` subagent over the diff, apply its durable proposals, and record the applied net
      `AGENTS.md` delta on this task. Verify: the recorded delta reproduces with
      `git diff --numstat origin/main -- AGENTS.md`. **Done** — the capture proposed three durable rules, all applied:
      two merged into the review gate's step-2 bullet (the scan's candidates dismissed by the scan's own kind with no
      enumerating clause; a task's `**Done**` note stating a relative result, a command's outcome, or a text-quoted site
      rather than a count its own writes move) and one new mode bullet under the check-writing root (a declaration
      transcribed from a recorded artifact is re-derived and asserted equal, and its completeness asserted against the
      real input). It proposed no retirements, and verified none is owed. The applied net delta is
      `git diff --numstat origin/main -- AGENTS.md` = 35 insertions / 20 deletions (net +15), which includes this
      change's own four-site enumeration sweep; the capture's own contribution is the step-2 clause and sentence plus
      the new mode bullet. `verifyCapturedMarkers`, `verifyUniqueReviewRounds` and `spotlessCheck` pass with both
      markers in place.
- [x] 6.2 Ask the owner for the manual review pass, after 5.3 is clean and 6.1 is done. Verify: the request is in the
      session and the outcome is the owner's. **Asked** — the request is in this session, with the twelve review rounds
      and the applied capture reported beside it; the outcome is the owner's.

## 7. The change's own records

- [x] 7.1 Record the pre-write derivation pass — each fact the artifacts state about their evidence, derived from its
      source. Its output is `design.md`'s claims table, whose rows are derived from the digest's pre-change block
      (`git show HEAD:scripts/experience-analysis.sh`), the 2026-10-15 retrospective's figures, the ledger's label sets
      on each side of the boundary, #577's archived mapping table, the reviewer's canonical list, `doctor.sh`'s declared
      table, `AGENTS.md`'s `check`-member enumeration, and the removed idea. Verify: every row's command reproduces the
      figure it states. **Done** — the table was written in the proposal round and refined by the review rounds; each
      row's command reproduces, re-run against the implementation in round 6, including the `git show HEAD:` rows (the
      ledger header, `AGENTS.md` 759-760, `README.md` 545) and the live row with this unit's own rows excluded
      (`335 494` / `292 436`).
- [x] 7.2 Seed the table with `python3 scripts/claim-scan.py --base origin/main` over the unit's diff and resolve every
      candidate into a row or a dismissal grouped by kind beside the table, rather than letting the scan replace the
      derivation. Verify: every printed candidate maps to a row or a listed dismissal. **Done** — the scan over the
      implementation diff emits the scan's four kinds, and each candidate is disposed of by its kind's bullet
      (design.md's "Scan candidates" section), which states one reason per kind rather than listing referents or types.
      No count of the candidates is recorded: writing one into the artifacts adds `count` candidates, so only the
      disposal-by-kind result is stable.

## Workflow follow-up

- Archive the change after the owner approves: the archive commit carries the change-dir move and the main-spec sync,
  and rides this change's PR.
