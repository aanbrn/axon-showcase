# Design

This design accompanies `proposal.md`: the routing floor must read a unit's recorded rounds whether or not the unit has
a change directory.

## Context

- The deep-review routing requirement (`showcase/quality/agent-skills`, "The deep review is routed and bounded") makes
  one decision **per unit**, with a deterministic floor derived from the change's facts including "its recorded rounds";
  a missing or failed derivation is `REQUIRED`.
- `review-routing.py`'s `ledger_signal(rows, change)` returns `[]` when no change name is supplied, and `--change` must
  name an existing directory under `openspec/changes/`; the suite encodes that boundary
  (`test_no_change_name_carries_no_signal`).
- The review-gate skill tells a unit with no change dir to omit `--change` and says "the omission is not a lower floor",
  which the tool contradicts: with the flag omitted the repeated-class signal cannot fire.
- `docs/ideas.md` 2026-10-26 records the defect and the observed cost: a unit without a change directory reached
  `NOT_NEEDED` while its ledger rows carried repeated classes.
- The ledger's unit field is "the unit (its branch or change name)" per `review-quick.md`'s record line, so the identity
  is not always the branch name — a continued cloud branch is recorded under its unit's name.

## Goals / Non-Goals

### Goals

- A unit with no change directory gets the same repeated-class signal as one with a directory.
- Every invocation that names a change or a unit keeps its outcome; an invocation that names neither now fails instead
  of returning an empty signal.
- A caller that cannot name its unit gets a visible failure, never a silent `NOT_NEEDED`.

### Non-Goals

- No change to the routing outcomes, the signal classes, or the risk thresholds.
- No automatic discovery of the unit from the branch name: the ledger's unit field is recorded by the caller, so the
  caller supplies it.
- No change to the change-directory validation or to `REQUIRED` precedence.

## Decisions

### D1 — `--unit` is the ledger identity; `--change` stays the delta's directory

- What: `--unit` filters the ledger rows; `--change` only locates the delta spec for the requirement-block kinds.
- Why: the two facts are independent — a unit can have recorded rounds without a change directory, and a change
  directory names its delta but not necessarily the ledger unit.
- Default: `--unit` falls back to `--change` when only the latter is given, so every change-unit invocation is
  unchanged.

### D2 — No identity is a failed derivation, checked before the floor's short-circuit

- What: `main` validates the identity before calling `floor`, so a missing unit reports `REQUIRED` and exits non-zero
  even when a path or declaration signal would otherwise return early; `ledger_signal` also refuses a missing unit.
- Why: an empty signal is indistinguishable from "no repeats recorded", and a `REQUIRED` short-circuit would otherwise
  skip the validation and exit 0.

### D3 — The caller supplies the recorded name

- What: the review-gate skill states that a unit passes `--unit` whenever its ledger name differs from its `--change`
  name — a unit with no change dir always, and a change unit whose rows are recorded under another name (a continued
  cloud branch) as well — and omits `--change` only when there is no change directory.
- Why: the tool cannot derive a recorded name from a branch that may differ from it; the skill is where the caller
  learns the source.

### D4 — Controls perturb the ledger and the CLI, not only the helper

- What: the suite keeps the helper controls and adds a CLI run per branch of `main`/`floor` — change-only, unit-only,
  both with and without a block kind, and neither with and without a pre-empting `REQUIRED` signal — each asserting the
  outcome, the signal line, and the exit status; the task list names each case.
- Why: a unit test on `ledger_signal` alone would not catch the CLI's argument plumbing, which is where the omitted flag
  lived.

### D5 — No spec delta

- What: `skip_specs: true`; the design aligns the code with the existing per-unit, recorded-rounds contract.
- Why: the requirement already makes the decision per unit from its recorded rounds; the defect is the tool's argument
  and the skill's masking sentence, not a missing or changed contract. A delta would be owed only if the design changed
  the contract, which it does not.

## Claims table

| #   | Claim                                                            | Source                                                                                                                                              | Reproduced                                                                                                                                                                     |
| --- | ---------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| 1   | The helper drops the signal without a change name                | `git show 4475a8e4:scripts/review-routing.py \| sed -n '88,100p'`                                                                                   | `if not change: return []`                                                                                                                                                     |
| 2   | `--change` must name an existing directory                       | `git show 4475a8e4:scripts/review-routing.py \| sed -n '162,166p'`                                                                                  | `if not change_root.is_dir(): raise ValueError("no such change: …")`                                                                                                           |
| 3   | The suite encodes the boundary                                   | `git show 4475a8e4:scripts/test-review-routing.py \| sed -n '113,114p'`                                                                             | `test_no_change_name_carries_no_signal` expects `[]`                                                                                                                           |
| 4   | The contract is per unit from its recorded rounds                | `git show 4475a8e4:openspec/specs/showcase/quality/agent-skills/spec.md \| sed -n '2032,2086p'`                                                     | line 2034 "one routing decision per unit"; line 2071 "its recorded rounds"; line 2076 "A missing or failed routing decision cannot skip mandatory review"                      |
| 5   | The skill says to omit `--change` and calls it not a lower floor | `git show 4475a8e4:.opencode/skills/review-gate/SKILL.md \| grep -n -A2 "no change dir omits"`                                                      | the omission sentence the change corrects                                                                                                                                      |
| 6   | The observed cost                                                | `git show 4475a8e4:docs/ideas.md \| sed -n '217,226p'` and `git show 4475a8e4:docs/review-rounds.tsv \| grep generalize-process-defects-2026-10-28` | the entry records the tool's `NOT_NEEDED` and that "this one's floor came only from the owner's resolution"; the unit's rows carry repeated classes on rounds 2, 3, 4, 6 and 7 |
| 7   | The ledger unit is the branch or change name                     | `git show 4475a8e4:.opencode/agent/review-quick.md \| sed -n '159,163p'`                                                                            | "the unit (its branch or change name)"                                                                                                                                         |

## Risks

- A caller passing the wrong `--unit` would read another unit's rows; mitigated by the other-unit control and by the
  skill naming the source of the identity.
- A change unit whose ledger name differs from its `--change` name would read zero rows if the caller passes only
  `--change`; the tool cannot detect the mismatch, so the skill instructs `--unit` whenever the names differ and the CLI
  control asserts the different-identities case. The residual risk is a caller ignoring that instruction.
