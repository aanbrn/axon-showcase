# Design

## Context

See `proposal.md` — Why. The preflight in `.opencode/skills/review-gate/SKILL.md` §2 requires a hand-over section before
the wait, and `review-quick.md`'s input contract describes what that hand-over must carry; neither is checked. The
evidence is process-defect evidence rather than command output:
`docs/units/surface-projector-backlog/process-defects.md` records five consecutive rounds whose section was missing, its
`findings/round-10.md` preserves the corresponding finding, and the archived `keep-review-handoff-per-unit` change left
the early hand-overs where they were recorded, without migrating them.

The register `track-agent-session-continuity` added checks the slug of a **registered** invocation's section
(`--agent-sessions`), and its format requirement excludes the quick reviewer. The quick rounds' headings are
`## Round <N>` in the same file. The two records therefore have two heading conventions, and only one of them is
checked.

## Goals / Non-Goals

**Goals:**

- A pre-delegation check that fails when the round's hand-over section is absent.
- A predicate that accepts the heading form the quick loop already writes, with a descriptive suffix.
- A failure for a malformed argument or an unreadable file.
- The check's meaning stated exactly, so a reader does not read it as proof of completeness, attribution, or timing.

**Non-Goals:**

- Changing the register's schema, the slug convention, or `--agent-sessions`.
- Unifying the two records' heading conventions, or the histories themselves.
- An aggregate scan over historical hand-overs, or a migration of them.
- A build task or a pre-commit rule for this mode; it is a caller-run preflight.
- Proving that the section was written before the delegation, or that it belongs to this invocation.

## Decisions

### D1 — Quick rounds only, with the heading form they already write

The mode checks a second-level heading that begins `Round`, then the round's exact decimal token, followed by the end of
the text or whitespace before a descriptive suffix, in the named unit's `docs/units/<unit>/review-handover.md`.
`Round 30` and `Round 3x` therefore do not satisfy round 3, and the token is not backticked.

Alternatives rejected:

- **One rule for both records.** The register's backticked slug and the rounds' `Round <N>` are different conventions
  serving different records; folding them together is the history unification this unit excludes.
- **A backticked slug for quick rounds.** It would rewrite every future round's heading and buy nothing the `Round <N>`
  form does not.

### D2 — The argument names the unit and the round, and no completed row is required

`--handover <unit>:<round>` supplies both, because the pre-call check cannot depend on a register or ledger row that is
appended only after the outcome is known. `<unit>` is one directory name under `docs/units/`, and `<round>` a canonical
positive decimal; the file is read from the working tree, so staging, a ledger row, and a register row are all
irrelevant to the check. A malformed argument exits non-zero.

Alternatives rejected:

- **Reading the round from the ledger.** The ledger row does not exist before the call, and a missing row would make the
  check either fail on a legitimate first round or silently skip.
- **A repository-wide scan.** It would need a migration or an exemption for the early hand-overs recorded elsewhere.

### D3 — The invocation sits after the section is written and before the delegation

`review-gate` §2 gains the invocation immediately before each quick-review call, and its failure stops the call.

Alternatives rejected:

- **A `check`-time or pre-commit-only check.** It catches the omission after the round, when the reviewer's work is
  already lost; the incident's cost is the wasted round, not the missing heading at commit.

### D4 — The check establishes presence at inspection, and says so

The requirement states that a matching heading existed when the mode ran — not that the section is complete, was
delivered, belongs to the invocation, or was written earlier. Copied content beneath a matching heading passes; an
unchanged heading naming another round does not.

Alternatives rejected:

- **Claiming delivery or ordering.** The checker has no harness-visible proof of the delegation, and a stronger claim
  would be false. The procedural ordering is the enforcement; the mode is the net that makes skipping visible.

### D5 — Tests and the mode mapping, no build or pre-commit integration

The mode is exercised by committed cases and the mode-coverage mapping, not by a Gradle task.

Alternatives rejected:

- **A `check` member.** It would run the check once per build over a set of rounds the build does not know; the mode's
  subject is a single invocation, supplied by the caller.
- **A pre-commit rule.** It would judge a staged file against no round, and the pre-commit guard's job is the staged set
  the build and the hook own.

## Risks / Trade-offs

- **A caller skips the check.** → The preflight names it before the delegation; the guard is a net, not an interlock,
  and the requirement says so.
- **Copied content beneath a matching heading, or a late heading, passes.** → Stated as the check's limit; the
  alternative is proving ordering the checker cannot see.
- **The heading convention drifts.** → The mode and the quick loop's writing both use `## Round <N>`; a change to one is
  a change to this check's predicate.
- **An early hand-over sits in a change directory or its archive.** → The argument names a unit and a round, so the mode
  reads the unit's file only; no scan and no migration.

## Claims table

The base revision is `55edb7a6ba391ec0ee546a3c8555b2443ee9731a` (`git merge-base HEAD origin/main`).

| Claim                                                                                        | Source                                                                                           | Reproduces                                                                                                                              |
| -------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------ | --------------------------------------------------------------------------------------------------------------------------------------- |
| `surface-projector-backlog` recorded five consecutive rounds without their hand-over section | `git show 55edb7a6:docs/units/surface-projector-backlog/process-defects.md`                      | the record states the five rounds and the section's absence                                                                             |
| Its round-10 finding preserves the same omission                                             | `git show 55edb7a6:docs/units/surface-projector-backlog/findings/round-10.md`                    | the entry names the missing hand-over                                                                                                   |
| `--agent-sessions` resolves a registered invocation's slug and excludes quick reviews        | `git show 55edb7a6:openspec/specs/showcase/quality/commit-hygiene/spec.md`                       | the register's format requirement states the exclusion and the slug rule                                                                |
| Quick-round hand-over headings are `## Round <N>`, optionally with a descriptive suffix      | `git grep -h -E '^## ' 55edb7a6 -- 'docs/units/*/review-handover.md'`                            | the quick-round headings carry that form, none backticks the round token, and the preflight names no heading, which task 1.3 prescribes |
| The preflight requires the hand-over section before the wait                                 | `git show 55edb7a6:.opencode/skills/review-gate/SKILL.md`                                        | §2 states one section per quick-review invocation, recorded before the wait                                                             |
| `review-quick.md` states what the hand-over must carry                                       | `git show 55edb7a6:.opencode/agent/review-quick.md`                                              | its opening paragraph enumerates the hand-over's fields                                                                                 |
| The archived change left the early hand-overs where they were recorded                       | `git show 55edb7a6:openspec/changes/archive/2026-11-03-keep-review-handoff-per-unit/proposal.md` | What Changes says they stay where they are and the new file is not a migration                                                          |
| The checker's modes and their coverage mapping exist                                         | `git show 55edb7a6:scripts/commit-hygiene.py` and `scripts/test-commit-hygiene.py`               | `MODE_FLAGS`, the parser, and `MODE_TEST_COVERAGE`                                                                                      |
| The parked idea proposes this mode and its limit                                             | `git show 55edb7a6:docs/ideas.md`                                                                | the 2026-11-07 entry names `--handover <unit>:<round>` and presence only                                                                |

## Verification

- `python3 scripts/test-commit-hygiene.py` — a case per predicate and the mode mapping.
- `openspec validate --all` and the affected `check`.
- `spotlessApply` after the last edit.
