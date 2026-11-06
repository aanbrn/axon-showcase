# Proposal

## Why

The unit layout regulates five files. Two merged units wrote a sixth kind — `findings/advisor-<n>.md` — for advisor
consultations, and one recorded the deep review's round under `caller-restarted`, the ledger's word for an unexplained
restart. The requirement that owns the store states how it works, never why a per-round file sits beside a hand-over
that no longer leaves with the archive, so a reader's only stated reason is the stale one: that the hand-over left with
the archive.

## What Changes

- The four `advisor-*.md` files fold, entry by entry, into the hand-over sections that already summarize each pass, and
  the references to the deleted files are repointed there.
- `surface-projector-backlog` round 12 records `definition-changed` — the deep review's definition ran there — instead
  of an unexplained restart.
- The store's requirement states its rationale and its round-only scope: any other pass records in the hand-over.
- The store's verification fails on a file under `findings/` that is not a round's instances file, with tests for the
  failure and for a clean store.
- The review gate's writer step points at that rule rather than restating it.
- The two merged units record the invented-file class in their own process-defect records, added beside what each file
  already states rather than replacing it, and `assert-paketo-pin-coverage`'s record `#3` gains a dated correction of
  its file count.
- The idea for this work is removed from `docs/ideas.md`.

## Capabilities

### New Capabilities

none

### Modified Capabilities

- `showcase/quality/commit-hygiene`: the finding-instances requirement states why the store exists and that it holds
  review rounds only, and its verification rejects any other file in the store.

## Impact

- `docs/units/surface-projector-backlog/findings/advisor-{1,2}.md` and
  `docs/units/pin-wrapper-distribution-checksum/findings/advisor-{1,2}.md` — content folded into each unit's
  `review-handover.md`, then removed, with that hand-over's references to the files repointed.
- `docs/units/surface-projector-backlog/rounds.tsv` — round 12's reason.
- `docs/units/surface-projector-backlog/process-defects.md` and
  `docs/units/pin-wrapper-distribution-checksum/process-defects.md` — one record each for the invented-file class.
- `docs/units/assert-paketo-pin-coverage/process-defects.md` — a dated correction in `#3` only; that unit's own
  formatter defect is parked in `docs/ideas.md` rather than carried here.
- `openspec/specs/showcase/quality/commit-hygiene/spec.md` — one MODIFIED requirement, via the delta.
- `scripts/commit-hygiene.py` and `scripts/test-commit-hygiene.py` — the store's file-set check.
- `.opencode/skills/review-gate/SKILL.md` — the writer step points at the store's scope.
- `docs/ideas.md` — the idea's removal.
