# Design

This design accompanies `proposal.md`: a unit whose claims table lives on the pull-request checklist must draft and save
its body before the pre-write pass, and the pass's seed must scan that body's text, which is neither of the scan's input
sets (Context).

## Context

- The review gate's §1.2 puts a no-change-dir unit's claims table "on the pull-request checklist line", and the
  `agent-skills` scenario states the same destination. That line is part of the body, which is drafted after the pass in
  the current sequence.
- `scripts/claim-scan.py` reads two input sets: the diff's added lines and the untracked, non-ignored files. A saved
  body is neither, so the claims it states never reach the seed; and where a unit has no untracked file and its diff
  adds no line, the seed prints nothing and that empty list is indistinguishable from "nothing to resolve".
- The process-defect record is a different artifact with its own destination and purpose; it is not the claims table's
  home, and demanding it before an episode occurs would invent an obligation.

## Goals / Non-Goals

### Goals

- Units whose claims-table home is the body have that body saved from the template before the pre-write pass writes into
  it.
- The pass's seed scans the saved body's text in addition to the ordinary diff scan.

### Non-Goals

- No change to what the scan reports, to a change-dir unit's seeding, or to the tool's advisory contract.
- No change to the process-defect record's destination, and no new obligation to write one before an episode occurs.

## Decisions

### D1 — The body is drafted and saved before the pass, for the units that need it

- What: the review gate's pre-write step and the `/seed-claims` command state that a unit whose claims-table home is the
  pull-request body drafts it from `.github/PULL_REQUEST_TEMPLATE.md` and saves it (a scratch path under the agent's
  temporary directory) before the pass runs, then fills its table and dismissals during the pass and re-scans after an
  edit that can add candidate-shaped text.
- What: drafting is not publication — it writes no pull request and authorizes nothing.
- What: a unit with a change dir keeps its `design.md` or `tasks.md` home and needs no early body step.
- Why: the pass executes from those two sites, so a rule stated only in `AGENTS.md` would leave both designating a home
  created later.

### D2 — The scan accepts supplementary text, and the body is its prescribed input

- What: `scripts/claim-scan.py` gains `--include <path>` — additive and single-valued — scanning that file's lines as
  all-added candidates beside the ordinary diff scan, with its own path and 1-based line numbers in the output.
- What: a path-shaped candidate in the included text is kept even when it names a path the unit modifies: the text is
  authored, and naming a modified file does not establish that its surrounding assertion needs no verification. The
  default scan's filtering is unchanged, so the broader treatment belongs to the explicit input alone.
- What: one function reads an untracked file and an included file, with the explicit input's read failure surfaced
  rather than swallowed as the untracked scan swallows it.
- Why: the claims the pass exists to catch sit in the body, and the body is not one of the ordinary scan's input sets
  (Context).

### D3 — The explicit input's failure is a failure; emptiness is not

- What: a missing, unreadable, or undecodable `--include` file is reported with the path and exits non-zero; an empty
  included file prints no supplementary candidates and exits zero; the ordinary scan's candidates are kept whatever the
  included file holds.
- What: the tool still never fails on a candidate — finding candidate-shaped text is not an error — and empty output
  means "no recognized candidates in the scanned inputs", never that the claims table is complete.
- Why: the class this change exists for is a list that reads empty when it should not, and an unreadable explicit input
  is exactly such a case.

### D4 — The delta states what the seed scans, not what it finds

- What: the `agent-skills` requirement's skipped-pre-write scenario gains the ordering and states that the seed scans
  the saved body's text in addition to the ordinary diff, so a deletion-only diff does not hide the claims the body
  states.
- Why: the scenario already names the destination, and a promise that a scan finds candidates would be false.

## Claims table

| #   | Claim                                                  | Source                                                                                                                           | Reproduced                                                        |
| --- | ------------------------------------------------------ | -------------------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------- |
| 1   | The destination is the pull-request checklist line     | `grep -n "pull-request checklist line" openspec/specs/showcase/quality/agent-skills/spec.md`                                     | the skipped-pre-write scenario's clause                           |
| 2   | The gate's §1.2 names the same destination             | `grep -n "pull-request checklist line" .opencode/skills/review-gate/SKILL.md`                                                    | the pass's step                                                   |
| 3   | The command's step 2 names it too                      | `grep -n "pull-request checklist line" .opencode/commands/seed-claims.md`                                                        | the resolve step                                                  |
| 4   | The scan read the diff plus untracked files only       | `git show b27b6699:scripts/claim-scan.py \| grep -n "untracked, non-ignored files"`                                              | the module docstring at the base                                  |
| 5   | The tool never fails on a candidate                    | `grep -n "never fails on a candidate" scripts/claim-scan.py`                                                                     | the docstring's sentence and `main`'s `return 0`                  |
| 6   | The untracked scan's reader swallowed a read error     | `git show b27b6699:scripts/claim-scan.py \| sed -n '109,121p'`                                                                   | `except (OSError, UnicodeDecodeError): return claims` at the base |
| 7   | The template carries the record's line and the table's | `grep -n "process-defect record" .github/PULL_REQUEST_TEMPLATE.md` and `grep -n "claims table" .github/PULL_REQUEST_TEMPLATE.md` | two separate lines                                                |
| 8   | The record has its own destination, not the table's    | `grep -n "docs/process-defects/<unit>.md" AGENTS.md`                                                                             | the recording workflow's step 1                                   |

## Risks

- Drafting the body before the pass adds a step to the units that keep their table there; the alternative is a table
  written into a file that does not exist yet.
- `--include` could be read as replacing the diff scan; it is additive, and the command states that the ordinary scan
  still runs.
