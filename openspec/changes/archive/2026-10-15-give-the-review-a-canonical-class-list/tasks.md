# Tasks

## 0. The pre-write derivation pass (this change carries its own claims table)

- [x] 0.1 Run the pre-write derivation pass and record its output as the claims table in `design.md`.

## 1. The vocabulary

- [x] 1.1 `.opencode/agent/review-quick.md`'s class clause carries the canonical list from `design.md`, each class
      naming what must change, plus the extension rule (a finding fitting none adds a class here in the same change) —
      keeping the existing "a repeated finding keeps its class and is marked as a repeat, never relabelled" clause.
- [x] 1.2 Verify the mapping is complete: every label the record holds maps to a canonical class — the labels it holds
      are the left-hand set plus `compliance-miss`, the record's own flag rather than a class, so a mapping that misses
      one is caught rather than assumed.

## 2. The record

- [x] 2.1 `docs/review-rounds.tsv`'s header names the canonical vocabulary and the boundary: rows before this change
      keep their labels, since a record's measured data is what it records.

## 3. Capture the behavior in the spec

- [x] 3.1 The delta for `showcase/quality/agent-skills`: a `MODIFIED` block carrying the requirement's description and
      **all** of its scenarios by name and in the main spec's order, with the classification clause naming the canonical
      list and the extension rule — never a renamed scenario.

## 4. Docs the change owns

- [x] 4.1 Sweep `AGENTS.md` (its pointer to the reviewer's finding classes), `README.md`, `openspec/config.yaml` and
      `docs/` for statements the change falsifies or should name the vocabulary. Outcome: `AGENTS.md`'s pointer now says
      the classes are the canonical list its own contract carries; the README, the config and `docs/` name no class
      list.
- [x] 4.2 Record the capability's `## Purpose` outcome: **no refresh owed** — it describes the subagents and their
      contract, not the class labels, and this change refines a label vocabulary rather than the capability's scope.

## 5. Verification

- [x] 5.1 Re-run the claims-table rows with their sources and confirm each reproduces.
- [x] 5.2 Run `./gradlew spotlessApply` then `spotlessCheck`, `openspec validate --all`, and the manual 120-character
      check over the changed set.
- [x] 5.3 Confirm the vocabulary is usable: a review round's report can classify every finding it raises from the list.
      Outcome: the first round's report classified all eight findings from the list (`claim-unreproducible`,
      `contradiction`, `docs-residue`, `impact-omission`, `missing-task`, `coherence`, `wording`), none invented.
- [x] 5.4 Run a `review-quick` pass over the implementation, passing the prior round's instances.
- [x] 5.5 Run the per-unit `lesson-capture` and record the applied net `AGENTS.md` delta. Outcome: **nothing durable**
      (0 additions, 0 retirements; net 0) — every class was a compliance miss of a rule the file already carries. The
      capture also found a live defect it routed here: the count claims said 55 mapped where the table maps 54
      (`compliance-miss` is the record's flag, not a class), now corrected in the design, the proposal and task 1.2.
- [x] 5.6 Request the user's manual review pass.
