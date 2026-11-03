# Design

## Context

A quick-review round produces two artifacts today: its record row in `docs/units/<unit>/rounds.tsv` — date, unit, round,
classes raised, classes repeated, compliance misses, and the reviewer-session fields — and a free-form report in the
reviewer's session. The report's finding instances survive only if the caller copies them into the pause checkpoint's
"Findings no record holds" field, and the hand-over file leaves with the archive.

The review gate's `prior-findings` binding already requires an instance-level retrieval source: "the prior review
round's report, preserved in the `Resume checkpoint`'s 'Findings no record holds' field or the review hand-over". That
source is either session-scoped or archive-scoped, never per unit. The split of the ledger gave every unit a directory
(`docs/units/<unit>/`), so the store now has a natural home.

Consumers, named rather than assumed: the review gate's next round (which needs each prior finding's `file:line` and
claim), a resumed unit (whose checkpoint currently carries the only copy), and an audit or a later reader of a round's
report after the archive. The retrospective digest reads classes, not instances, and is not a consumer.

## Goals / Non-Goals

**Goals.**

- One file per recorded round, beside the record, holding that round's instances or an explicit statement that it raised
  none.
- The check fails a post-boundary round with no file, so the store cannot silently stay empty.
- The boundary is stated, not inferred, and every round before it is exempt.

**Non-Goals.**

- Changing the record format: `rounds.tsv` keeps its nine fields.
- Moving or replacing the pause checkpoint's field; it becomes a pointer, not the only copy.
- Feeding the instances into the retrospective digest or any aggregate.
- A retention policy: a round's file is small and permanent.

## Decisions

**1. One Markdown file per round, at `docs/units/<unit>/findings/round-<N>.md`.**

- Chosen: a bullet per instance, `- [<class>] <location> — <claim>`, the claim being the reviewer's own sentence; the
  separator is the first `—` after the location, so a claim may itself contain one.
- Chosen: a round that raised nothing carries the single line `- none`, so "clean" is recorded rather than inferred from
  an absent file.
- Chosen: an entry may be reflowed across source lines, since the file sits under `docs/` and the formatter wraps it at
  120 columns; the parser joins a continuation onto its entry, so a long claim keeps its fields. A probe confirmed both
  directions: `spotlessApply` wrapped a long entry, and `parse_instances` read its three fields from the joined text.
- Rejected — a column on `rounds.tsv`: the record format is stated once and its rows are data with a fixed width; an
  unbounded claim does not fit a column.
- Rejected — one `findings.tsv` per unit: a round's file is written once and read as a unit, and a per-round file makes
  the missing-file failure the check's simplest signal.

**2. The boundary is the last day a round may lack instances, one constant in the checker.**

- Chosen: `FINDING_INSTANCES_THROUGH`, the last date a round may have no instances file, set to the change's landing
  date. A round dated on or before it is exempt because it was recorded before the store existed; a later round is
  required, so the check cannot be satisfied by silence, and the transition is a stated boundary rather than an
  inference.
- Chosen: the constant is assigned at implementation to the actual landing date, so the rounds recorded on that day —
  before the merge — are covered by the exemption instead of failing as missing. The landing step re-derives it if the
  merge slips, and `verifyFindingInstances` confirms the records at that date; a unit that recorded a round on the
  original date keeps its exemption either way.
- Rejected — inferring the boundary from a missing file, which makes every missed file look historical.
- Rejected — an "on or after the landing date" bound: the landing day's own rounds predate the merge, so they would be
  required and could not be recovered.

**3. The check is a `commit-hygiene.py` mode, and the guard applies the same judgment to the index.**

- The mode reads the tracked records and each round's file and reports every failure the requirement
  `Every review round recorded after the boundary keeps its finding instances` names, so the failure set is stated once
  and this decision carries no copy of it.
- The guard judges the index snapshot for the affected unit: it runs when either side of the pair changes, reads the
  record and the instances files as the index holds them, and therefore also covers a staged instances-file deletion
  beside an unchanged record. Naming "the staged set" alone would miss a file committed by an earlier commit.

**4. The writer is the review gate's round step, and the readers are the gate and the checkpoint.**

- The gate writes the file when the report arrives, beside the ledger row, from the report's own finding lines.
- The checkpoint's "Findings no record holds" field becomes a pointer to the store; the hand-over reads it, so a resumed
  round reads the same instances from the same place.
- A round dated on or before the boundary has no store entry by design, so `prior-findings` reads the checkpoint or the
  hand-over for it, and a unit holding neither reads as unrecovered context rather than as a clean round; the bindings'
  role value states that fallback, and the pause-and-resume skill states it where the field is written.

## Risks / Trade-offs

- The boundary exempts the day it lands on, so every round recorded before the merge — this unit's included — passes
  without a store entry; a round on the next day is required. A unit that recorded a round on the landing day but merges
  later still reads as exempt, which is the transition the boundary exists for.
- One extra file per round grows `docs/units/` by one small file per round; the records directory already grows by one
  directory per unit, and this is the same order.
- The format's claim field is free text, so a claim carrying `—` is split at its first occurrence; the class and
  location stay parseable, and the claim keeps the rest verbatim.

## Claims table

Base revision: `7130f687`.

| #   | Claim                                                                  | Source                                                                                | Result                                                                        |
| --- | ---------------------------------------------------------------------- | ------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------- |
| 1   | The repository holds 257 round records outside this unit               | `ls -d docs/units/*/rounds.tsv \| grep -v '/persist-review-round-findings/' \| wc -l` | 257                                                                           |
| 2   | No round has an instances store                                        | `ls -d docs/units/*/findings 2>/dev/null \| wc -l`                                    | 0                                                                             |
| 3   | The `prior-findings` binding names a checkpoint field or the hand-over | `rg -n "prior-findings" docs/contracts/process-bindings.md`                           | three lines: the role row, and the pause-resume and review-gate consumer rows |

**Dismissals**, from `python3 scripts/claim-scan.py --base origin/main`, one reason per kind:

- **absolute** — each candidate states a rule this change adds or the boundary it sets.
- **count** — each is a figure a row states and its command reproduces, or a count of the store this change creates.
- **cross-ref** — the candidate is a phrase of this change's own artifacts.
- **path** — a planned path names a file this change creates.
