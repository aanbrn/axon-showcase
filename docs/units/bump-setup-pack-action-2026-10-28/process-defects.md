# bump-setup-pack-action-2026-10-28

Process defects observed while working this unit. Recorded under `AGENTS.md`'s "Workflow: Record process defects";
analysis is a later, owner-triggered unit.

### bump-setup-pack-action-2026-10-28#1 — A bot-authored pull request body satisfies none of the template's demands

- **Recorded**: 2026-10-28, by the writing session (`ses_e7fbeb9f7ffeBJsc9n28836RyI`).
- **Stage**: review.
- **Expected**: `.github/PULL_REQUEST_TEMPLATE.md` governs every pull request — its `## Summary` and `## Test plan`
  sections, in that order, plus its five checklist lines, two of which send a unit with no change dir to the
  process-defect statement and the claims-table note.
- **Observed**: the unit is the Dependabot pull request #717, whose body is the bot's bump template. It carries neither
  section and no checklist line, so nothing could be ticked; the `## Summary`, `## Test plan`, and `## Review record`
  sections were appended to the body instead.
- **Evidence**: `gh pr view 717 --json body` returns the bump description, the release notes, and the Dependabot
  commands block only — no `## Summary` and no `- [ ]` line — and the template's own sentence, "for a unit with no
  change dir, by ticking this line".
- **Consequence**: the pull request reached review without the sections the template requires, and the two no-change-dir
  records had no line to tick; both are repaired by the appended sections. Nothing else remains broken.
- **Knowledge boundary**: observed — the template's wording and this pull request's body. Unchecked — whether an earlier
  Dependabot pull request carried the sections or either record.

- **Decision** — no-change: `.github/PULL_REQUEST_TEMPLATE.md` governs every pull request, and review-gate §1 already
  requires a unit with no change dir to record its claims table on the pull-request checklist, so the bot body's
  nonconformance is a compliance miss rather than a missing rule.

### bump-setup-pack-action-2026-10-28#2 — A request to refresh the pull-request branch was answered with a comparison

- **Recorded**: 2026-10-28, by the writing session (`ses_e7fbeb9f7ffeBJsc9n28836RyI`).
- **Stage**: ship.
- **Expected**: the owner's `/sync-branch` invocation refreshes the branch, as the same procedure's decision points
  state: "An explicit invocation authorizes this refresh."
- **Observed**: the invocation produced the comparison and the named route only. Its step 3 states "IF a pull request is
  open, THEN the route is `gh pr update-branch`. Name it and hand it to the owner rather than running it", so no remote
  mutation ran, and the head stayed `dfe142fb` until the owner asked, in a third message, why the synchronization had
  not happened — after which `gh pr update-branch` produced `6dd2a768`.
- **Evidence**: the procedure's step 3 and its decision-point wording as supplied with the invocation;
  `gh pr view 717 --json headRefOid` returned `dfe142fb` after the invocation and `6dd2a768` after the later run; the
  state the refresh removes was what the owner reported from the Changes panel — changes coming from `main`.
- **Consequence**: two further owner turns and one round of the owner diagnosing a behind-branch diff in the IDE; the
  refresh is complete (`6dd2a768`, `build` green). Nothing remains broken.
- **Knowledge boundary**: observed — the two clauses, the head values, and the exchange. Unchecked — whether an earlier
  unit's `/sync-branch` invocation met the same gap.

- **Decision** — no-change: routed to a change of its own — direction A, where step 3 runs `gh pr update-branch` on an
  explicit invocation and the approval boundary is reconciled with a `MODIFIED` delta on the `agent-skills` requirement;
  this unit records the direction and edits no command.

### bump-setup-pack-action-2026-10-28#3 — Exit statuses were read from pipelines instead of from the commands

- **Recorded**: 2026-10-28, by the writing session (`ses_e7fbeb9f7ffeBJsc9n28836RyI`).
- **Stage**: review.
- **Expected**: `AGENTS.md`'s CI gotcha states that a pipeline reports the last command's status, so a status claim is
  captured from the command itself.
- **Observed**: four commands whose status the unit's reports stated were run as
  `<command> 2>&1 | tail -N; echo "… exit=$?"`, which prints `tail`'s status: `git fetch origin` (reported as a
  successful fetch), `gh pr update-branch 717` (reported as exit 0), and `./gradlew spotlessApply` and
  `./gradlew spotlessCheck` (each reported as exit 0).
- **Evidence**: those four command lines as run; bare re-runs returned `git fetch origin` exit 0 with empty output,
  `./gradlew spotlessApply` exit 0, and `./gradlew spotlessCheck` exit 0 (`BUILD SUCCESSFUL in 21s`), while the
  `update-branch` outcome is evidenced independently by the branch head moving `dfe142fb` → `6dd2a768`.
- **Consequence**: four status claims rested on the wrong process; all four outcomes proved correct when re-checked, so
  no decision turned on them, and the owner's challenge of the comparison is where the missing evidence surfaced.
  Nothing remains broken.
- **Knowledge boundary**: observed — the four command forms and their bare re-runs. Unchecked — whether a report in this
  session stated another status whose command was not re-run.

- **Decision** — no-change: the exit-status rule already requires capturing a command's own status rather than a
  pipeline's, and check-and-test-authoring states the sink-status half for a control.

### bump-setup-pack-action-2026-10-28#4 — A review round's ledger row was appended a round late

- **Recorded**: 2026-10-28, by the writing session (`ses_e7fbeb9f7ffeBJsc9n28836RyI`).
- **Stage**: review.
- **Expected**: `review-gate` §2 appends each round's row when its report arrives, before the next round's invocation.
- **Observed**: round 4's report arrived and its two findings were applied, but no row was appended; round 5's review of
  the ledger found rounds 1-3 only and its check failed for round 4. The row was appended afterwards, together with
  round 5's own.
- **Evidence**: round 5's report line, "the ledger has no round-4 row, and its own check fails", and the check's output
  for `--unique-rounds --expect bump-setup-pack-action-2026-10-28:4` reporting no row for that round, while the unit's
  rounds 1-3 were present in `docs/review-rounds.tsv`.
- **Consequence**: one round's record was absent for the duration of that round; nothing else was wrong. The row is
  restored and round 5's row accompanies it. Nothing remains broken.
- **Knowledge boundary**: observed — the missing row and the failing check. Unchecked — whether another unit's loop has
  the same omission; the archived record `separate-lesson-capture-from-implementation#2` reports one.

- **Decision** — no-change: already parked — `docs/ideas.md` 2026-10-22 "reviewer wrapper" (reconsidered 2026-10-27) and
  2026-10-24 "pre-commit ledger guard"; the wrapper's `Revisit when:` condition, an `Acknowledged gap` row naming the
  recovery, is not met by this record.
