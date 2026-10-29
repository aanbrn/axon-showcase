# Design

This design accompanies `proposal.md`: the claim scanner's `absolute` class must surface the absence and
definite-singular claims the review loop keeps recording, and it deliberately prefers recall over a narrower match set.

## Context

- `scripts/claim-scan.py` prints one candidate per line for four shapes; its `ABSOLUTE` matches
  `only|every|all|never|always|none`, so `no` and `nothing` never surface and the definite singular (`the one`,
  `the sole`, `the single`) does not either.
- Four recorded instances needed a claims-table row the scan did not seed: "nothing is parked for it", "no finding
  reduced `AGENTS.md`", "Nothing in the repository reads `.java-version`", and "`git clean -fd` is the one loss vector".
- Measured over `AGENTS.md` (2,764 lines) by applying each class's regex to the raw line
  (`python3 -c 'import re,sys; p=re.compile(sys.argv[1], re.I); t=open("AGENTS.md").read().splitlines(); print(sum(1 for l in t if p.search(l)), len(t))' '<class>'`):
  the base class matches 442 lines (16%), the widened one 619 (22%). These are raw-text regex line shares: the scanner
  removes code spans before matching, so the shares describe this corpus's line incidence, not the scanner's output, a
  false-positive burden, or a resolution cost.

## Goals / Non-Goals

### Goals

- An absence claim (`nothing`, `no … is/are/has/…`) and a definite-singular claim surface as candidates.
- The incidence the widening adds is measured over a fixed corpus and stated as incidence only.

### Non-Goals

- No change to the other candidate kinds, to the tool's exit contract, or to the claims-table convention.
- No attempt to decide whether a candidate is a real claim: the scan is a lead list.

## Decisions

### D1 — The absence forms: `no`, `nothing`, and the `none` already matched

- What: `no` and `nothing` join the class outright, beside `none`.
- Why: a determiner's occurrence ("no change", "no path") is itself an absence statement a claims table may owe a row,
  and a predicate window was rejected because it excluded the very incident that motivated the change ("no finding
  **reduced**"). Every candidate costs a resolution, but a uniform dismissal may be grouped, so recall is worth more
  than a narrower match set.
- What: the class matches the scanner's processed prose, so a `no` inside a code span does not surface — the span is
  removed before the class is applied.
- What: a hyphenated compound (`no-op`, `no-Axon-Server`) also matches, and is judged an absence claim about the
  compound; the controls pin that shape beside the plain determiner.

### D2 — The definite singular joins the class

- What: `the one`, `the sole`, and `the single` match; `the only` is already covered by `only`.
- Why: "the one loss vector" is exactly the claim shape the class exists for, and its definiteness is what makes it a
  claim rather than an article.

### D3 — The measurement is a probe over one corpus, and says what it measures

- What: the Context states the two shares, the corpus, and the command that produced them; the record carries the run.
- Why: the idea asked for the noise to be re-measured before committing, and a share without its corpus or its command
  is not a figure; nor does incidence establish precision.

### D4 — No spec delta

- What: the change ships with `skip_specs: true`.
- Why: no requirement states the candidate kinds — `agent-skills` owns the claims table and the seed, and `/seed-claims`
  points at the tool — so a delta would add a corpus entry for a tool-internal list.

## Claims table

| #   | Claim                                        | Source                                                             | Reproduced                                       |
| --- | -------------------------------------------- | ------------------------------------------------------------------ | ------------------------------------------------ |
| 1   | The class matched six forms at the base      | `git show f2fae423:scripts/claim-scan.py \| grep -n "ABSOLUTE = "` | six forms: only, every, all, never, always, none |
| 2   | No requirement states the candidate kinds    | `grep -rn "absolute" openspec/specs/`                              | six hits, all load-tests' absolute thresholds    |
| 3   | The command points at the tool for its kinds | `grep -n "candidate kinds" .opencode/commands/seed-claims.md`      | the step's sentence                              |
| 4   | Four instances were recorded                 | `git show f2fae423:docs/ideas.md \| grep -n "nothing is parked"`   | the parked entry at the base                     |
| 5   | The measured shares are 16% and 22%          | the probe command in Context over `AGENTS.md`                      | 442 and 619 of 2,764 lines                       |
| 6   | The scan never fails on a candidate          | `grep -n "never fails on a candidate" scripts/claim-scan.py`       | the docstring                                    |

The scan's candidate set is re-derived rather than frozen, because the run reads this unit's own growing artifacts:
`python3 scripts/claim-scan.py --base f2fae423`. The rows above resolve the claims the table states; the rest are
dismissed by kind.

- `count` — task ordinals, table figures, and the unit's own ledger rows.
- `cross-ref` — artifacts the rows already name.
- `path` — quoted commands and references to files the change does not modify, kept because the scanner drops only the
  paths the diff modifies.
- `absolute` — the class's own prose, which the rows state once.

## Risks

- A bare `no` matches most occurrences of the word, so an author resolves them as a grouped dismissal; the class stays a
  lead list, and a missed candidate is the status quo rather than a regression.
- The added matches are mostly determiners ("no change"), resolved as a grouped dismissal rather than a row; a `no`
  inside a code span does not surface, because the span is removed before the class is applied.
