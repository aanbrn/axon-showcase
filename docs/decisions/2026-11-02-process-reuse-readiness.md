# Decision record — process reuse readiness

Date: 2026-11-02

Status: a bounded exploration. It binds no standing rule; it records the shape of the preparation the owner accepted.

## The goal

The owner declared a project goal: the agent process prepared for reuse in similar projects. Two bounded transfer pilots
(`2026-10-31-process-reuse-pilot.md`, `2026-10-31-lesson-capture-reuse.md`) measured what the declared bindings must
cover, and two renaming counterfactuals tested them. No real target project exists.

The owner then asked whether to prepare as well as possible before a target appears, so that at the moment of reuse only
small rough edges would remain.

## The question

Does preparing ahead of a real target need the mechanisms ADR-0025 §6 defers, or is there preparation that needs no
signal at all — and what may such preparation claim?

## What was decided

The owner accepted a narrowing on 2026-11-02: **prepare the transfer procedure, not an imagined destination.**

Admissible without a mechanism signal:

- a linked retrieval guide over the evidence the repository already holds (`docs/process-reuse.md`), so a transfer can
  reach the pilots' manifests, the declarations, and the counterfactual techniques from one place;
- an explicitly bounded rehearsal at owner direction, which adds no standing mechanism and claims only what it
  exercised.

Not authorized by this record: a template repository, a generator, broad extraction, a new auditor axis, an executable
transfer kit, or a new acceptance test for transfers. The four mechanisms ADR-0025 §6 defers keep that ADR's signals,
and ADR-0026's substitution engine keeps its own; ADR-0027 keeps its admission criterion, and the consolidation pause
ADR-0025 records keeps its conditions. This record neither lifts nor relaxes any of them. The executable kit and the
acceptance test are this record's own refusals, which no ADR defers.

The broader reuse program remains the goal. This record prepares **access to the evidence**, not the program, and its
acceptance is not an early step toward a template.

## Why not "prepare as well as possible"

- **It has no stopping boundary.** Taken at face value it authorizes every deferred mechanism on the grounds that each
  might shorten a future onboarding, which is the reasoning ADR-0027 replaced with an evidence test.
- **Exercises expose or confirm limits that prior preparation had not eliminated.** The pause/resume pilot's dependency
  manifest already named absent contracts before its transfer, and the transfer still found an overlooked writer-record
  adaptation; the lesson-capture pilot found an absent authoring mechanism; the first counterfactual left headings
  unverified; the heading counterfactual found a component that inferred an undeclared binding. That is evidence for
  bounded exercises, not for the stronger claim that such limits are discoverable only through execution — useful
  preparatory analysis is not what the narrowing rules out.
- **Being wrong about a mechanism is asymmetric — as a hypothesis.** A wrongly guessed mechanism must be maintained and
  then undone by the real target, while a retrieval guide absorbs a newly found incompatibility without imposing
  structure. The repository holds no measurement of that asymmetry, and this record does not claim one.
- **Both pilots refuse the general claim in their own ceilings.** Each ran on a target constructed to exercise that
  slice — the second on a copy of the first — and neither measured transfer latency.

## What a first transfer can record

The guide's "What a transfer records" section owns the item set and how each item is recorded, drawn from the pilots'
accounting and usable without their fixtures. This record adds only that the item set is a transfer's own output, not a
summary of the pilots'.

## The ceiling

**A retrieval aid for planning a bounded transfer, plus the option of a bounded rehearsal.** What that aid may and may
not claim is stated once, at the top of `docs/process-reuse.md`; this record adds no claim of its own beyond the scope
above.

## Reopening condition

Reopen this record when any one of the following holds:

- **A real target project becomes available** — run the guide against it; that transfer's record replaces this record's
  scope estimate (external trigger: an owner-named project).
- **A named transfer demonstrates an essential execution gap the guide cannot resolve** — the executable-kit scope is
  reconsidered then (internal trigger: that transfer's record).
- **The owner changes a mechanism signal or the pause** — this record's boundary follows the owning ADR, which keeps its
  own conditions (internal trigger: ADR-0025 or ADR-0026).
