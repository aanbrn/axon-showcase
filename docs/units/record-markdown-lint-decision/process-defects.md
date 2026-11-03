# Process defects — record-markdown-lint-decision

### record-markdown-lint-decision#1 — Three tool-behaviour and cost claims reached the owner before verification

- **Recorded** — 2026-11-02, by the writing session (`ses_e6624962affe45N9fuDQCsRc09`), during the proposal stage.
- **Stage** — proposal.
- **Expected** — `AGENTS.md`'s rule for tool-, library- and framework-behaviour claims requires verifying each against
  the tool or CLI before stating it to the owner as a basis for a decision, or labelling it an unverified hypothesis.
- **Observed** — three claims were stated as established while still unverified: that MD038 closes the recorded
  gate-less class of a whitespace run inside an inline code span; that a Node-based linter would add a
  `scripts/doctor.sh` probe; and that its exclusion set would duplicate `markdownTargetExclude`. The first was used as a
  reason to adopt a linter twice, and the other two as costs of doing so.
- **Evidence** — the MD038 claim fails a direct check: `markdownlint-cli2` 0.24.0 and `rumdl` 0.3.2 both flag
  `` ` code` `` and `` `code ` `` and neither flags `` `paketo-buildpacks/procfile     5.15.0` `` (an internal run),
  each reporting no issues for the internal-run file alone. `grep -n 'node\|npm' scripts/doctor.sh` returns only the
  install hints for `snyk` and `ast-grep`, and the build downloads Node (`frontend-conventions.gradle.kts`).
  `markdownTableFiles` derives its file tree from `markdownTarget`/`markdownTargetExclude` (`build.gradle.kts:699-703`).
  The technical advisor had marked the coverage claim as an unverified hypothesis before the check was run.
- **Consequence** — two owner-facing messages carried the wrong reason for adoption; the decision record states the
  measured result instead, and no repository artifact asserts any of the three claims.
- **Knowledge boundary** — observed in this session, on the two CLIs at the versions above and a two-line fixture, and
  on `HEAD`'s copies of `doctor.sh`, `frontend-conventions.gradle.kts` and `build.gradle.kts`. Not checked whether a
  later rule or option covers internal whitespace runs, nor whether any other unattended path probes Node.

- **Decision** — no-change: the tool-behavior-claims rule already requires verifying a claim against the tool before
  stating it to the owner as the basis for a decision.

### record-markdown-lint-decision#2 — Two tool runs were compared across different corpus snapshots

- **Recorded** — 2026-11-02, by the writing session (`ses_e6624962affe45N9fuDQCsRc09`), during the review stage.
- **Stage** — review (found by quick-review round 3).
- **Expected** — a comparison between two runs attributes a difference only when nothing but the tool varies, and a
  record's counts name the corpus they came from.
- **Observed** — both linter runs were made over copies taken with `git ls-files` from a working tree whose index
  carried a live change directory, so the two copies held differently-named snapshots, while the record stated the
  counts were about the base revision.
- **Evidence** — `$TMPDIR/opencode/rumdl-check.json` reports MD041 in `openspec/changes/process-bindings-contract/…` and
  `$TMPDIR/opencode/mdnode/out.txt` reports it in `openspec/changes/adopt-process-bindings/…`; the base tree carries
  neither. Re-running both over `git archive 6258011538161498fb48cf614bbb30454d12e007` moved the in-scope totals from
  492 and 372 to 484 and 370.
- **Consequence** — six totals and two per-rule columns were wrong; every figure was re-derived over the extracted base
  tree, and the record now names that corpus.
- **Knowledge boundary** — observed in this unit's own outputs. Not checked whether probe counts in other units were
  taken over the same kind of moving tree.

- **Decision** — no-change: the comparison rule already requires isolating the varied dimension before attributing a
  difference between two runs.

### record-markdown-lint-decision#3 — A review round's ledger row was not appended when its report arrived

- **Recorded** — 2026-11-02, by the writing session (`ses_e6624962affe45N9fuDQCsRc09`), during the review stage.
- **Stage** — review (found by quick-review round 5).
- **Expected** — the review gate's loop appends each round's ledger row as its report arrives and then runs both ledger
  checks, so a gap is caught by the round that created it.
- **Observed** — round 4's report arrived, its findings were fixed and round 5 was dispatched without appending the row
  or running the checks.
- **Evidence** — with rounds 1 to 3 recorded and round 4 reported,
  `grep -c record-markdown-lint-decision docs/review-rounds.tsv` returned 3;
  `commit-hygiene.py --unique-rounds --expect record-markdown-lint-decision:4` was not run at that boundary. Round 5's
  review reported the gap.
- **Consequence** — the ledger read 1, 2, 3, 5 until rows 4 and 5 were appended; both checks then passed, and
  `--expect-clean` passed after the clean round 6.
- **Knowledge boundary** — observed here. Not checked whether earlier units' ledgers carry the same gap, which the
  contiguity guard passes silently.

- **Decision** — no-change: already parked — the `docs/ideas.md` 2026-10-22 reviewer-wrapper entry, whose reopening
  condition is not met (no `Acknowledged gap` row after 2026-10-22 states the unrecoverable fields).
