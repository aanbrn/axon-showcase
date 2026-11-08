# Unit: generalize-process-defects-2026-11-08

Workspace:
`unit=generalize-process-defects-2026-11-08; path=/Users/afanasyev/Development/axon-showcase; branch=generalize-process-defects-2026-11-08; writer=ses_e4add21daffe0vW0OISMmtQS3i`

State: closed

A guidance unit with no change directory: it runs the generalization pass `AGENTS.md`'s "Workflow: Generalize process
defects" triggers, presents every undecided process-defect record's disposition, applies the improvements the owner
approves, and appends a `Decision` line to each analyzed record. A proposal that changes an agent or skill definition's
specified behavior would owe an OpenSpec change; the dispositions below propose no such edit, and that is re-checked
before any edit.

`Packs: none` — the work edits guidance prose, `docs/ideas.md`, and process-defect records; no JVM production or test
code, no Gradle build decision, and no deployment surface.

## Proposal

Base revision: `origin/main` `527d1754fd4c258868679b377cb90a76d8905cf8`, read 2026-11-08. The corpus holds 307
process-defect records across 141 `process-defects.md` files; 15 carry no `Decision` line, in six units. The whole set
is the selection, since the owner named no units.

The recurrence disposition follows `AGENTS.md`'s "Where the repository already records a recurrence decides its
disposition" bullet. Eleven records are covered by an existing rule (or by a rule plus a parked idea whose condition is
not met) and two are below the promotion gate. Two are candidates: P1 carries the promotion gate's six criteria, and the
other is routed out of the guidance corpus as a contract limitation.

| Record                                    | Stage                                                | Title                                                                     | Disposition                                                                                                                                                                                               |
| ----------------------------------------- | ---------------------------------------------------- | ------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `gate-review-handover-section#1`          | review                                               | A section heading was deleted by an edit anchored past it                 | covered — `AGENTS.md` programmatic-edit rule (anchor on the text being changed) and the span-edit rule                                                                                                    |
| `generalize-process-defects-2026-11-07#1` | review                                               | Round 1's ledger row was appended after its fixes                         | covered — `review-gate` §2 step 2 (row and instances before the fix); the ledger-wrapper idea stays parked, its revisit condition unmet                                                                   |
| `generalize-process-defects-2026-11-07#2` | review                                               | A green formatter claim was recorded before the section's last edit       | covered — `AGENTS.md` Formatting (run the formatter after the final write)                                                                                                                                |
| `generalize-process-defects-2026-11-07#3` | ship                                                 | The `proposed` Decision lines were merged without their pull-request link | covered — this command's step 7 (a `Decision` line records `proposed` with a link); its recurrence site is also the parked `docs/ideas.md` 2026-11-01 `Decision`-line check                               |
| `link-generalization-decision-records#1`  | implementation                                       | The commit guard refused a follow-up's edit of a closed unit record       | candidate — a contract limitation, routed to a change that amends the requirement (parked, not fixed here): the guard's requirement specifies the branch/path comparison while the build's check does not |
| `record-upstream-helm-defects#1`          | implementation (rebasing the upstream branch)        | The rebase was piped into `head` and stopped mid-flight                   | below gate — one incident, repaired by an abort and a re-run; the pipe and exit-code class is recorded for reporting, not for mutating commands                                                           |
| `record-upstream-helm-defects#2`          | implementation (the Windows functional-test fixture) | The first edit preceded the `Packs:` classification line                  | covered — `AGENTS.md` packs rule (state the `Packs:` line before the first edit)                                                                                                                          |
| `record-upstream-helm-defects#3`          | review (a delegated `review-quick` pass)             | A reviewer handoff omitted the subject pack                               | covered — the packs rule's delegated-agent sentence and its applied edited-paths bullet                                                                                                                   |
| `record-upstream-helm-defects#4`          | review (reporting the pull requests' check results)  | A gate's status was reported while its bot comment flagged uncovered code | candidate — two independent occurrences (#241, #240); the existing verdict-echo rule covers a local script's echo, and this is a CI gate's status flag                                                    |
| `record-upstream-helm-defects#5`          | review (the implementation round's first invocation) | A quick-review hand-over was written after the invocation returned        | covered — `review-gate` §2 preflight (record the hand-over before you wait); the `--handover` presence check now runs before each delegation, so only the ordering is unenforced                          |
| `track-agent-session-continuity#1`        | proposal review                                      | Two rounds' ledger rows were appended after the fixes they record         | covered — `review-gate` §2 step 2; the ledger-wrapper idea stays parked, its revisit condition unmet                                                                                                      |
| `track-agent-session-continuity#2`        | implementation review                                | The implementation-readiness consultation ran unrecorded                  | covered — `/consult` §4 and its direct-delegation paragraph, and the `agent-skills` register requirement; no mechanical check can observe a call that leaves no trace                                     |
| `unify-review-event-history#1`            | proposal review                                      | The proposal-readiness consultations ran unrecorded                       | covered — same rule as `track-agent-session-continuity#2`; the advisor's manual-reconciliation idea is a hypothesis, not a promoted rule                                                                  |
| `unify-review-event-history#2`            | implementation                                       | A new helper shadowed an existing function of the same name               | below gate — one incident, caught by its own first test run; the module's suite caught it before any review                                                                                               |
| `unify-review-event-history#3`            | implementation                                       | A batch edit script wrote its pre-edit copy and dropped an earlier edit   | covered — `AGENTS.md` edit-verification gotcha (a batching script is a third mode; write each edit as it succeeds and verify the batch)                                                                   |

### Candidates

- **`link-generalization-decision-records#1`** — the local pre-commit guard's `--staged` check rejects a follow-up
  unit's staged edit of a closed unit record (the record names another branch), while the build's `--workspace-records`
  accepts the same edit, so CI is green and only the local commit is blocked, which forced `--no-verify`. The two checks
  disagree because the guard's requirement **specifies** the branch/path comparison for a staged record and the build's
  check does not: a contract limitation, not an implementation slip. The disposition is an `docs/ideas.md` entry for a
  change that amends the requirement — and that change owes a spec delta and must keep the ownership protection for the
  committing unit's own record.
- **`record-upstream-helm-defects#4`** — a gate's status flag was reported as its outcome while the tool's own comment
  on the same run flagged uncovered code, twice (#241's `codecov/patch`, #240's five uncovered lines). The existing
  verdict-echo rule governs a local script's own echo; whether naming a CI gate's comment is a separate increment or the
  same rule's scope is the authoring question for the analysis.

## Review evidence supplied to the analysis

Each record's own `Evidence` and `Knowledge boundary` fields, the unit record of `generalize-process-defects-2026-11-07`
(the prior pass's dispositions and its applied P1), `docs/ideas.md`'s parked ledger-wrapper entry with its revisit
condition, `AGENTS.md`'s verdict-echo rule and "Checking CI status" bullet, the `showcase/quality/commit-hygiene`
workspace-record requirement, and the `review-gate` skill's §2 and §5.

## Analysis

`lesson-capture`'s verdict: **1 durable proposal (1 addition, 0 retirements), 0 authoring-system recommendations, net +1
guidance line**, plus one candidate routed out of the guidance corpus. It read all 15 records, the review artifacts each
cites, and the live Codecov comments (confirming that `#240`'s comment reads `90.19608%` with five uncovered lines
beside a `codecov/patch` `conclusion: success`, and that `#241`'s comment was rewritten after the covering push, so the
record's quoted `81.81818%` is the pre-fix state it observed).

### P1 — a check's conclusion is not its finding

- **Destination** — `AGENTS.md`, `### CI & GitHub Actions`, the existing `**Checking CI status**` bullet.
- **Extends** — that bullet's "Verify the run itself (`gh run view <run-id> --json conclusion`), never a pipeline's exit
  code" clause, whose condition is a run's status read through a proxy.
- **Decision difference** — _before:_ a `conclusion: success` is the run's outcome, so "the checks are all green" is
  reportable from the status list. _after:_ the report is derived from what the check emitted, and a green conclusion
  beside an advisory comment is reported as an advisory.
- **Exact text** — replace the bullet's final sentences, from the parenthetical
  "(`gh run view <run-id> --json conclusion`), never a pipeline's exit code" to the bullet's end, with:

  > (`gh run view <run-id> --json conclusion`), never a pipeline's exit code — `gh run watch … | tail -6; echo $?`
  > reports `tail`'s, so a failed run reads as green. A check's conclusion is not its finding: read the artifact it
  > produced — its annotation, bot comment, or report — before reporting the outcome, since a check can pass its status
  > while its comment flags uncovered code. captured: record-upstream-helm-defects captured: fix-timeout-load-flakes

  The removed "Idle sleep loops only waste time and add no information." is rationale for the bullet's opening
  imperative, not an obligation; the `captured:` chain keeps both markers. Net +1 line.

- **Gate** —
  - true: two independent occurrences, #240 read live and #241 as the record states it;
  - actionable: read the artifact before reporting the outcome;
  - not automatable: no check can police what an agent reports about a gate;
  - material: uncovered branches shipped on #240;
  - general: every report of pull-request check results, an upstream contribution included;
  - high-confidence: the status/comment divergence is directly observed, and the mutable figures stay behind the marker.

### The idea to park

- **Destination** — `docs/ideas.md`, a new `## 2026-11-08` section (the current newest is `## 2026-11-07`); no entry
  records this defect.
- **Exact text** —

  > - Let the pre-commit guard admit a follow-up's edit of another unit's closed record — parked; no change yet. The
  >   guard's requirement specifies the branch/path comparison for a staged record, so a follow-up correcting a merged
  >   unit's record is refused and must commit with `--no-verify`, while the build's `--workspace-records` accepts the
  >   same edit and CI stays green. This is a contract limitation rather than an implementation slip: the change amends
  >   the requirement — scoping the comparison to the record of the unit the commit's claim or branch assigns while
  >   keeping the ownership protection for the committing unit's own record — and owes a spec delta. Origin:
  >   `captured: link-generalization-decision-records`.

  The entry states the route the amendment takes, not a fix already made: this unit parks it, and no guard change is
  applied here.

### Dispositions confirmed by the analysis

All fifteen keep the disposition the table above states. `generalize-process-defects-2026-11-07#3` is covered by this
command's step 7, and its recurrence site is the parked `docs/ideas.md` 2026-11-01 `Decision`-line check — an
already-parked problem rather than a new rule. `record-upstream-helm-defects#5` cites the `--handover` presence check
that now runs before each delegation, so only the ordering itself is unenforced; the pre-invocation-check idea is not
cited, because `2026-11-08-gate-review-handover-section` implemented it and removed its entry.

### Decision lines to append on approval

- `record-upstream-helm-defects#4` — `proposed`, applied as P1 here, with this unit's pull request added to the line
  before its merge.
- `link-generalization-decision-records#1` — `proposed`, recorded as a **parked proposal** rather than an implemented
  fix: this unit parks the `docs/ideas.md` entry above, and the same pull-request link is added before the merge.
- the other thirteen — `no-change`, each naming the rule or the parked site the table states.
- **Tracked step** — before this unit's merge, add its pull-request link to both `proposed` lines and verify the link
  with a `grep`, as a line in the pull-request body's checklist; the prior pass's `#3` is exactly the failure of an
  untracked version of this promise.

## Applied

The owner approved the proposal on 2026-11-08. Applied in this unit:

- **P1** — the `AGENTS.md` `### CI & GitHub Actions` bullet `**Checking CI status**` gains "A check's conclusion is not
  its finding: read the artifact it produced — its annotation, bot comment, or report — before reporting the outcome,
  since a check can pass its status while its comment flags uncovered code.", replacing the bullet's closing rationale
  sentence. `git diff --numstat AGENTS.md` after `spotlessApply`: **+3 −2, net +1 line**, the analysis's predicted
  delta.
- **The parked idea** — `docs/ideas.md` gains a `## 2026-11-08` section with the guard defect as a contract limitation
  that owes a spec amendment (origin `captured: link-generalization-decision-records`).
- **Decision lines** — one on each of the fifteen analyzed records: `proposed` for `record-upstream-helm-defects#4` (P1)
  and `link-generalization-decision-records#1` (the parked idea), `no-change` for the other thirteen. This unit's
  pull-request link is added to the two `proposed` lines before the merge.
- **Retirements** — none proposed; the analysis reported no rule this change makes obsolete.
