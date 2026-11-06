# Process defects — make-unit-lifecycle-explicit

### make-unit-lifecycle-explicit#1 — The packs declaration followed the design decisions it governs

- **Recorded** — 2026-11-06, by the main agent.
- **Stage** — proposal.
- **Expected** — `AGENTS.md`'s classification table is read before a governed decision, and the `Packs:` line naming
  what was loaded in full is stated before the first edit that decision produces.
- **Observed** — the design's fifth decision (a Gradle `Task` with a rules object and its tests, wired into `check`) was
  written, and the review hand-over prepared, before `gradle-build` and `jvm-testing` were loaded; the `Packs:` line
  appeared only in the review request message that followed.
- **Evidence** — the review hand-over for that round carries the `Packs:` line, and the quick review's first round
  raised test-control and wiring findings against that decision and against the build conventions the packs own.
- **Consequence** — the check's control set and wiring verification were re-derived after the packs were read, and one
  review round's findings touched that decision; the design and tasks now carry the pack's rules (no `gradleTestKit()`
  in `build-logic`, rules objects unit-tested, a real run proving the failure).
- **Knowledge boundary** — observed: the edit order and the first round's findings. Not checked: whether loading the
  packs first would have produced the same decision shape, or whether a different wording would have avoided the
  findings.
