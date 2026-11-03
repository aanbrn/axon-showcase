# Process defects — inventory-process-reuse-dependencies

Process defects observed while working this unit. Recorded under `AGENTS.md`'s "Workflow: Record process defects".

### inventory-process-reuse-dependencies#1 — The enumeration claim outran the enumeration method

- **Recorded** — 2026-11-02, by the unit's agent.
- **Stage** — review.
- **Expected** — a record whose value is that its figures derive from the tree should state the method that produces
  them, and a figure it cannot derive should not appear.
- **Observed** — the first draft asserted a complete population and a dependency count produced by hand-maintained
  lists. Later rounds found reference forms the stated rules did not yet cover — a wrapped heading, `opsx-tool-update`
  filed under the generated prefix, spec capability ids, `Source:` lines, `## Conventions`, `README.md` — and two
  figures (`54` repository paths, and a per-row count of 4 where the tree gives 6) that could not be derived at all.
- **Evidence** — the ledger's rows for this unit, by round and class: 1
  `incomplete-enumeration, contradiction, claim-unreproducible, claim-missing, missing-task, over-claim, wording`; 2
  `incomplete-enumeration, claim-missing, claim-unreproducible, coherence`; 3 `incomplete-enumeration, contradiction`; 4
  `contradiction, claim-unreproducible, formatting`; 5 `incomplete-enumeration`; 6 none; 7
  `incomplete-enumeration, claim-unreproducible, structured-writing`; 8 `incomplete-enumeration, claim-unreproducible`;
  9 `over-claim`; 10 `incomplete-enumeration`; 11 `wording, claim-missing`; 12 `claim-unreproducible`. Seven of the
  twelve carry `incomplete-enumeration`; the two deep-review passes added the subject table's count and the capability
  ids.
- **Consequence** — the record was corrected by stating rules instead of adding rows, and its coverage claim was
  reframed to "the dependencies the stated forms find". One round was spent on a spliced sentence and one on an
  underivable figure; no work shipped during the loop.
- **Knowledge boundary** — observed: the round sequence, the classes and the corrections. Assumed: that the stated forms
  now cover the forms the tree uses, which the last review could not establish either. Not checked: whether another
  component uses a form none of the six names.

- **Decision** — no-change: the single-sourcing rule already states that an enumeration repeated at many sites cannot be
  proven complete by any grep, read, or sweep.
