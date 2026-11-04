# Process defects — generalize-process-defects-2026-11-03

Process defects observed while working this unit. Recorded under `AGENTS.md`'s "Workflow: Record process defects".

### generalize-process-defects-2026-11-03#1 — A rule was proposed before its contradicting archive was read

- **Recorded** — 2026-11-03, by the writing session (`ses_e5dece39fffeYkl8XkXm6Yg9Q3`), during the analysis.
- **Stage** — proposal (the lesson-capture pass and the caller's presentation).
- **Expected** — a proposal is checked against the recorded decision it would extend or reverse before it is presented
  as an application candidate, since a captured rule or an archived design is the decision to adopt, not to re-derive.
- **Observed** — P5 ("state an exemption as its last exempt case — a `through` date, never the first date that owes the
  artifact") was drafted as a durable addition and presented. The archived design it came from rejects a bound stated as
  "on or after the landing date" because the landing day's own rounds predate the merge, not because a first required
  date is inherently wrong, so the universal rule the proposal stated is unsupported. The advisor found it.
- **Evidence** — `openspec/changes/archive/2026-11-03-persist-review-round-findings/design.md:51-63` (the rejected "on
  or after" bound, with its landing-day reason); the proposal text the first consultation received.
- **Consequence** — one consultation round and one withdrawn proposal; nothing was applied from it. The advisor also
  narrowed P1 and P3 to `already covered` and revised P4 and P6.
- **Knowledge boundary** — observed the proposal, the consultation's finding, and the archived text. Did not check
  whether the lesson-capture pass read that design before proposing; its report does not say.

### generalize-process-defects-2026-11-03#2 — The first consultation hand-off omitted the record map it was deciding on

- **Recorded** — 2026-11-03, by the writing session, during the consultation step.
- **Stage** — proposal (the advisor hand-off).
- **Expected** — a hand-off carries every field the decision needs, and this decision was "is the application set
  right", which rests on the 44 records' dispositions, not only on the six proposals.
- **Observed** — the hand-off named the six proposals, A1, both routings and the rejected candidates, and no disposition
  map. The advisor's report returned the gap as its one open question: the pass supported the candidate decisions, not
  the blanket correctness of the remaining classifications, and the mapping matters before every `Decision` line is
  appended. A second consultation carried the map.
- **Evidence** — the first report's "Open questions" section naming the missing map; the follow-up hand-off carrying the
  44 rows, which produced two row corrections.
- **Consequence** — one extra consultation round; the follow-up corrected two rows (`keep-review-handoff-per-unit#4`
  from already-parked to covered, `persist-review-round-findings#1`'s site).
- **Knowledge boundary** — observed both reports and the map. Did not check whether the advisor would have found the
  same two rows from the first hand-off alone.

### generalize-process-defects-2026-11-03#3 — A `captured:` marker landed mid-bullet

- **Recorded** — 2026-11-03, by the writing session, during the implementation.
- **Stage** — implementation (the `AGENTS.md` edit for the machine-readable-output clause).
- **Expected** — a `captured:` marker ends the bullet or paragraph it sits in, so the pre-commit guard passes and the
  provenance is readable without git.
- **Observed** — the replacement text ended with `captured: record-ast-grep-skill-usage`, but the bullet continues with
  "Deriving the record shape is not enough…", so the marker landed mid-bullet and fused with the next sentence. The
  bullet's existing marker chain at its end was where it belonged.
- **Evidence** — the edit's resulting line fused the marker with the next sentence, reading "record-ast-grep-skill-usage
  Deriving the record shape is not enough: a -z fix"; the corrected chain ends
  `declare-probes-with-shape-and-behavioural-checks captured: record-ast-grep-skill-usage`.
- **Consequence** — one corrective edit before any formatter or guard run; nothing was staged in the broken state.
- **Knowledge boundary** — observed the diff. Did not run `verifyCapturedMarkers` against the broken state to see
  whether it would have refused it.

### generalize-process-defects-2026-11-03#4 — A record was parked against the wrong defect

- **Recorded** — 2026-11-03, by the writing session, at the disposition step.
- **Stage** — proposal (the recurrence dispositions).
- **Expected** — a record is matched to a parked entry by the defect it reports, not by the artifact the two share.
- **Observed** — `keep-review-handoff-per-unit#4` (the hand-over recorded after the invocation instead of before it) was
  classified `already parked` against the 2026-10-22 reviewer-wrapper entry, which is about appending a round's ledger
  row when the report arrives. The record's defect is the preflight ordering that `review-gate` already requires. The
  advisor corrected it to covered.
- **Evidence** — the record's Expected/Observed fields (record before waiting); the wrapper entry's text
  (`docs/ideas.md`, 2026-10-22); the advisor's follow-up finding.
- **Consequence** — one row corrected before any `Decision` line was written; the same pass's
  `record-markdown-lint-decision#3` and `record-ast-grep-skill-usage#2` remain correctly parked.
- **Knowledge boundary** — observed the record, the entry, and the correction. Did not check the corpus for other rows
  whose subject merely shares an artifact with a parked entry, beyond the row the advisor named.

### generalize-process-defects-2026-11-03#5 — A findings entry's location carried a space

- **Recorded** — 2026-11-04, by the writing session, while writing round 5's instances file.
- **Stage** — implementation (the review loop's instance record).
- **Expected** — an instance entry's location is one token, since the checker matches `- [<class>] <\S+> — <claim>`.
- **Observed** — the location was written as `PR-body draft:8`, whose space meant the entry matched nothing, so
  `verifyFindingInstances` failed the build with "round 5 carries 1 line(s) outside the instance shape" while the entry
  read correctly to a human.
- **Evidence** — that build failure line; the corrected location is the draft's absolute path, which carries no space.
- **Consequence** — one failed `check` run, corrected before any commit.
- **Knowledge boundary** — observed the failure and the pattern. Did not check whether another unit's instances file
  names a free-text location that passes only because it happens to contain no space.

### generalize-process-defects-2026-11-03#6 — A round's hand-over was not recorded before its invocation

- **Recorded** — 2026-11-04, by the writing session, at round 6.
- **Stage** — review (the quick loop's preflight).
- **Expected** — `review-gate` §2's preflight records the invocation's hand-over at
  `docs/units/<unit>/review-handover.md` before the caller waits on it, because a stalled invocation cannot be cancelled
  and reports nothing.
- **Observed** — round 6's invocation was made with the file's last section still describing round 5, so the record a
  successor would recover from named the previous round's artifact set and prior finding.
- **Evidence** — `findings/round-6.md`'s first entry: "the file has no section for this invocation, so a stall would
  hand a successor the previous round's artifact set"; the Round 6 and Round 7 sections were appended afterwards.
- **Consequence** — one round's hand-over had to be reconstructed from the invocation prompt after the fact; nothing was
  lost, because the round answered.
- **Knowledge boundary** — observed the missing section and the finding. Did not check whether an earlier invocation in
  this unit ran without its section, beyond the fact that rounds 1–5 each had one written before its own.

### generalize-process-defects-2026-11-03#7 — An unescaped pipe in the PR body's claims table split a cell

- **Recorded** — 2026-11-04, by the writing session, after the closing review round.
- **Stage** — review (the artifact the unit will publish; found while supplying the draft to a consultation).
- **Expected** — a markdown table cell containing a pipe escapes it as `\|`; `verifyTableRows` enforces the shape for
  the files it scans.
- **Observed** — the claims table's "22 units" row carried a bare `|` inside an inline command
  (`sed -E 's/#[0-9]+$//' | sort -u | wc -l`), which splits that row into extra cells; the draft lives outside the
  repository, so no gate scanned it and the closing round reported clean.
- **Evidence** — the row as authored; after escaping the two pipes the same cell-count check reports `rows 16 bad 0`.
- **Consequence** — the draft's row would have published malformed; corrected before the manual pass, and the rest of
  the table re-checked for cell counts.
- **Knowledge boundary** — observed the row and the corrected table. Did not check whether any other unit's draft body
  carries the same defect, which no gate reads by construction.

### generalize-process-defects-2026-11-03#8 — A claims row credited a command that cannot observe its second half

- **Recorded** — 2026-11-04, by the writing session, on the consultation's report.
- **Stage** — review (the PR body's claims table).
- **Expected** — a row's source establishes the whole claim; where two commands are involved, each half names its own.
- **Observed** — one row claimed "the spec corpus validates and the config emits no list-shape warning" and cited
  `openspec validate --all` alone. That command emits no list-shape warning either way, which is why the repository's CI
  probes the config through `openspec new change` instead; the row would have read as a verification the source cannot
  give.
- **Evidence** — `.github/workflows/ci.yml`'s quality-gate step, whose comment says `validate` does not fail on the
  warning and whose `openspec new change` call carries the grep; the probe re-run here printed
  `Created change '<probe>' …` with no warning line, and the probe directory was removed.
- **Consequence** — the row is now two rows, each naming its own command, before the draft is published.
- **Knowledge boundary** — observed the CI step and the probe's output. Did not check whether any other row pairs two
  claims under one command, beyond re-reading this table.

### generalize-process-defects-2026-11-03#9 — The pull-request link sweep reached other units' records

- **Recorded** — 2026-11-04, by the writing session of `record-link-sweep-overreach`, from this unit's shipping step.
- **Stage** — ship (adding the pull request's link to this unit's `proposed` `Decision` lines).
- **Expected** — a sweep that adds one link to the unit's own appended lines touches only those lines, and its count is
  read before the commit.
- **Observed** — the replacement matched `- **Decision** — proposed: applied as` across `docs/units/*/`, a form earlier
  generalization units' records also use, so it wrote the link into 22 lines instead of 11 and edited nine records of
  already-merged units.
- **Evidence** — the sweep printed `lines linked: 22`; `git show --name-only` on the commit listed 16 record files where
  7 were intended; after reverting the nine unrelated files the count read `11` and the commit's file list was those 7.
- **Consequence** — one amended commit and a `--force-with-lease` push on this unit's own branch before the merge; the
  merged tree carries the 11 intended links and the nine reverted records are unchanged. The record ships in a follow-up
  docs unit, because this unit's files were already committed when the defect surfaced.
- **Knowledge boundary** — observed the two counts, the commit's file list, and the merged state. Did not check whether
  other units' `proposed` lines were meant to carry links and never received them.
