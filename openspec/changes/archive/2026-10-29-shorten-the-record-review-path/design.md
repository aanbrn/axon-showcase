# Design

This design accompanies `proposal.md`: a record-only diff must not be lifted to `RECOMMENDED` by the repeated-class
signal alone, and the caller's procedure must name the escalation branch the spec already requires for a record
artifact.

## Context

- `scripts/review-routing.py` derives a floor from the change's facts: mandatory path, declaration, and
  requirement-block signals give `REQUIRED`; a class repeated on two or more of the unit's ledger rows gives
  `RECOMMENDED`; otherwise `NOT_NEEDED`. It lifts the floor whenever two ledger rows repeat a class, including on a diff
  whose changed paths are all records.
- `agent-skills` requires a record artifact's later round that raises only new instances of an already-raised class to
  stop and escalate to the owner, because "such an artifact has no shipped behavior to converge on". The routing floor
  does not reflect that class, and the caller's procedure names only "fix everything and re-run".
- The unit that exposed this took nine quick rounds and one unasked deep pass for two record files.
- `testReviewRouting` runs the tool's suite inside `check`, so a new control runs in the gate rather than only locally.

## Goals / Non-Goals

### Goals

- A diff whose changed paths are only record artifacts or the review-round ledger is not lifted to `RECOMMENDED` by the
  repeated-class signal alone.
- The exemption is visible in the tool's output, so the decision stays explainable.
- The caller's procedure names the record-artifact escalation branch at the step that applies it, and states what the
  stop leaves pending.

### Non-Goals

- No change to the mandatory (`REQUIRED`) signals, to a failed derivation's `REQUIRED` outcome, or to the agent's
  ability to decide above the floor on evidence.
- No round cap, no change to how a record is published or which unit owns it, and no new recording obligation.
- No change to any other signal, to the manual pass, or to the ledger's own checks.

## Decisions

### D1 — The record-only set is the Markdown record files plus the ledger the invocation reads

- What: a diff is record-only when its changed-path set is non-empty and every path is a Markdown file under
  `docs/process-defects/`, `docs/retrospectives/`, or `docs/load-tests/`, or is the ledger path the invocation reads
  (`--ledger`, defaulting to `docs/review-rounds.tsv`).
- What: a path of another type or location keeps normal routing, even inside those directories.
- What: a rename is judged by both its source and its destination, so a code path renamed into a record path is not
  record-only; the two sides come from `git diff --name-status`, since `--name-only` prints only a rename's destination.
- What: one predicate owns the classification, so the tool, its controls and the spec cannot drift apart.
- Why: the spec's escalation clause names a record-artifact class, and a record's supporting data beside it is not the
  record; an exemption claimed from a directory rather than from the class would be broader than its rationale.

### D2 — The exemption narrows the repeat signal, and reports itself

- What: the tool still computes the repeated-class signal; on a record-only diff it does not lift the floor and prints
  the exemption beside the signal, so an observed but suppressed signal is distinguishable from one that decided the
  floor.
- What: the reporting applies only where the exemption applies. A diff that carries a mandatory signal resolves to
  `REQUIRED` without the ledger's signal being derived — the ledger is still read and validated during gathering, so an
  unreadable ledger fails the derivation (exit non-zero, `REQUIRED`) whatever signals the paths carry.
- Why: a floor that silently dropped the signal would stop being explainable, and the spec requires the agent's decision
  to record the signals it relied on.

### D3 — Mandatory signals and a failed derivation are unchanged

- What: a record-only diff that also changes a mandatory path, a delta's requirement block, or a declaration is still
  `REQUIRED`; a derivation that fails is still `REQUIRED` with a non-zero exit.
- Why: the exemption addresses one signal's meaning for a class of diff, not the gate's evidence standard.

### D4 — The caller's procedure names the branch and what the stop leaves pending

- What: `.opencode/skills/review-gate/SKILL.md`'s fix-and-re-run step names the record-artifact case: a later round
  raising only new instances of an already-raised class stops and escalates to the owner instead of iterating.
- What: it states what the escalation preserves — the finding-bearing ledger row, the finding instances, and the owner's
  decision recorded on the unit's review task or pull-request review record — and that the loop resumes per that
  decision without manufacturing a clean round.
- What: the escalation is not clearance. `--expect-clean` is unchanged, so a unit whose highest recorded round still
  carries a finding class does not proceed to delivery; the owner's decision is on the work (continue, narrow, or set it
  aside). Delivering with accepted unresolved quick findings would change that check and is out of this change's scope.
- Why: the obligation exists in the spec and is invisible at the step it governs; naming it without its pending state
  would let an agent read the stop as either automatic clearance or a permanent blocker.

### D5 — The recording preference keeps the evidence rules and adds brevity

- What: `AGENTS.md`'s recording workflow states the preference — a short account of the observed event; a quotation only
  where it reproduces; a figure attributed to a command's output taken from a run of that command; session-only text
  described rather than quoted — and points at the evidence rules that already own the requirements.
- What: the three evidence obligations already exist (exact quotation or explicit paraphrase; a figure derived from the
  mechanism that produces it; a durable claim the repository can evidence, an event sequence included); brevity is the
  new preference, not a fourth obligation.
- What: the preference never makes an observation or an event sequence inadmissible; it asks for fewer unnecessary
  counts and quotations, not for thinner evidence.
- Why: the three evidence classes are already obligations elsewhere, and brevity is the one class the churn produced
  that no rule stated.

### D6 — No idea entry

- What: `docs/ideas.md` gains nothing for this.
- Why: the owner directed implementation rather than parking.

## Claims table

| #   | Claim                                                                | Source                                                                                                                                                                  | Reproduced                                                                                                                                                                                                                                                                  |
| --- | -------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1   | The spec names a record artifact and requires the escalation branch  | `grep -n "for a record artifact" openspec/specs/showcase/quality/agent-skills/spec.md`                                                                                  | the clause naming a retrospective and a load-test record                                                                                                                                                                                                                    |
| 2   | The floor is the script's and is derived from the change's facts     | `grep -n "deterministic floor" openspec/specs/showcase/quality/agent-skills/spec.md`                                                                                    | the decision scenario's floor clause                                                                                                                                                                                                                                        |
| 3   | The tool lifts the floor at two repeat rows, with no path input      | `sed -n '90,102p' scripts/review-routing.py`                                                                                                                            | `if repeats >= 2: return [("repeated-class", ...)]`                                                                                                                                                                                                                         |
| 4   | The caller's step 3 has no record-artifact branch                    | `grep -n "Fix everything it finds" .opencode/skills/review-gate/SKILL.md`                                                                                               | the step's text                                                                                                                                                                                                                                                             |
| 5   | The three record directories exist                                   | `ls -d docs/process-defects docs/retrospectives docs/load-tests`                                                                                                        | three directories                                                                                                                                                                                                                                                           |
| 6   | The tool's suite runs inside `check`                                 | `grep -n "testReviewRouting" build.gradle.kts`                                                                                                                          | the task and the `check` dependency                                                                                                                                                                                                                                         |
| 7   | The tool is stdlib-only                                              | `grep -n "Stdlib" scripts/review-routing.py`                                                                                                                            | the docstring's sentence                                                                                                                                                                                                                                                    |
| 8   | The motivating unit took nine rounds and one deep pass for two files | `grep -c "record-the-figure-defect" docs/review-rounds.tsv` and `git show 3ede97b2 --stat`                                                                              | nine rows; two record files in the commit                                                                                                                                                                                                                                   |
| 9   | The deep pass ran without the owner's decision                       | `grep -n "resolved without the owner" docs/process-defects/record-the-figure-defect.md`                                                                                 | entry `#3`                                                                                                                                                                                                                                                                  |
| 10  | A rename prints only its destination under `--name-only`             | `git diff --name-status` vs `--name-only` in a scratch repository                                                                                                       | `R100 old new` versus `new`                                                                                                                                                                                                                                                 |
| 11  | `claim-scan.py`'s four candidate kinds are dismissed by shape        | `python3 scripts/claim-scan.py --base 3ede97b2`                                                                                                                         | `absolute`: a scope word used as a qualifier, in the copied requirement, the design's rules and the tasks; `count`: a task or step ordinal, or a ledger-row field; `cross-ref`: the one line copied verbatim from the main spec's scenario; `path`: a named path or command |
| 12  | The three evidence obligations D5 cites exist                        | `grep -n "Doc claims must match their source\|durable artifact may assert only\|derive each figure from the mechanism" AGENTS.md .opencode/skills/review-gate/SKILL.md` | the three matched lines: "Doc claims must match their source and their strength — quote verbatim or paraphrase explicitly"; "A durable artifact may assert only what the repository can evidence"; "derive each figure from the mechanism that produces it"                 |

## Risks

- A record-only diff that hides a behavior change under a record path would skip the deep review; the conservative
  predicate (Markdown records only, both sides of a rename), the mandatory signals, and the manual pass still apply, and
  a Markdown record cannot carry executable behavior into the build.
- The exemption could be read as "records never need a deep review"; D2's reported signal and D3's unchanged paths keep
  the decision explicit, and the agent may still decide above the floor.
