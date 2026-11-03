# Process defects — record-ast-grep-skill-usage

### record-ast-grep-skill-usage#1 — Measurement figures were read from command output parsed at the wrong granularity

- **Recorded** — 2026-11-02, by the recording agent, session `ses_e6433f957ffeeYNmGp54kGNMxQ`.
- **Stage** — implementation.
- **Expected** — a figure quoted to the owner and then written into the record is taken from the command's output as the
  command emits it.
- **Observed** — the usage counts were extracted by splitting `sqlite3`'s default `|`-separated output on newlines. The
  queried commands contain newlines and `|`, so records were fragmented: the same window and the same predicate returned
  34, then 35, then 32 shell calls across three runs. The first figures (34 calls, 56 `run`, 23 patterns, and a "0.4% of
  shell calls" share) had already been reported to the owner.
- **Evidence** — the same predicate over the same window produced different totals run to run, with the row values split
  across lines in the captured output; re-reading the same query with `sqlite3 -json` and `json.loads` returned 32
  calls, 54 `run`, 11 `scan` and 22 patterns, stable across runs once the window's end was frozen.
- **Consequence** — the owner received three superseded figures in the conversation; the decision record carries the
  corrected ones, marks the share figure superseded, and states the predicate; the draft pull-request body carries the
  query block whose output was re-run and compared against every row it serves.
- **Knowledge boundary** — observed in this session, on this database and these commands. Not checked whether the
  earlier exploratory queries in this unit carry the same parse defect: the remaining figures came from SQL aggregates
  rather than from split text, and the chat-reported figures are not repository artifacts.

- **Decision** — proposed: https://github.com/aanbrn/axon-showcase/pull/836 — applied as the machine-readable-output
  clause that replaces the git-only remedy in the parsed-output rule (`-z` for git paths, `-json` for `sqlite3` rows).

### record-ast-grep-skill-usage#2 — Verification lines quoted values later steps changed

- **Recorded** — 2026-11-02, by the recording agent, session `ses_e6433f957ffeeYNmGp54kGNMxQ`.
- **Stage** — review.
- **Expected** — a verification line states the result of the command it names as of the step that runs it, and states
  the resulting state separately, so a later step cannot falsify it.
- **Observed** — review rounds 6–9 reported four verification-line defects. A `--review-ready` quotation was taken while
  the index still carried an earlier edit of the decision record, and again while the ledger carried a
  staged-then-edited state. A line stating "5 rounds … all checks exit 0" carried a round count its own next append
  moved, beside an `--expect-clean` result that was already failing. Round 7's ledger row was not appended until
  round 8. A routing-signal count stated in the pull-request body moved from 3 to 6 to 7 rows as ledger rows were
  appended.
- **Observed, later rounds** — the same class recurred on an input the unit does not control. Round 11 found the
  readiness claim false because `origin/main` had advanced from `f0486b6f` to `ca4cd1b1`, and round 13 found it false
  again after the second advance, to `ff21066a`, whose `.opencode/` definition changes the build's OpenCode probe and
  the Spotless markdown target read. Round 12 found this entry itself pinning a ledger total — "rows 1–10 with the
  closing round clean" — which the next appended row moved, and found the full `check` result resting on the pre-refresh
  base after that base had changed `build.gradle.kts`, `scripts/commit-hygiene.py` and `scripts/test-commit-hygiene.py`.
- **Evidence** — `docs/review-rounds.tsv` rows 6–9 record `claim-unreproducible` and `missing-task` with their
  compliance misses; `git status --porcelain` showed `AM docs/decisions/2026-11-02-ast-grep-skill-usage.md` and later
  `MM docs/review-rounds.tsv`; `--unique-rounds --expect record-ast-grep-skill-usage:7` exited 1 with "has no row for
  round 7, the round whose report arrived"; `review-routing.py --unit record-ast-grep-skill-usage` printed
  `[repeated-class] 3 rows`, then 6, then 7 rows for the same unit. `--review-ready` exited 1 with "'origin/main'
  carries 1 commit(s) HEAD does not, so the unit is not on that base", once per advance;
  `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` reported `BUILD SUCCESSFUL in 1m 35s` (399 tasks) on
  `ca4cd1b1` and `in 1m 1s` (399 tasks) on `ff21066a`.
- **Consequence** — the rounds' findings are recorded; the body's verification lines were rewritten to state the
  mechanism and the command's outcome rather than a count, and the pinned ledger total was removed rather than updated.
  Each refresh re-applied the staged set, re-appended the unit's ledger rows once rather than twice, and re-ran the
  readiness check and the full gate on the new base.
- **Knowledge boundary** — observed in this session from those commands' output. The ledger preserves finding classes,
  not the instances, and no elapsed cost was measured.

- **Decision** — no-change: the point-in-time-count rule already forbids pinning a count the next append moves; the
  ledger-row half is parked in the `docs/ideas.md` 2026-10-22 wrapper entry, whose reopening condition is not met.
