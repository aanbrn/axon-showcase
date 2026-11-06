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

### make-unit-lifecycle-explicit#2 — A new skill's definition was delivered unregistered, and only CI noticed

- **Recorded** — 2026-11-06, by the main agent, after the unit merged.
- **Stage** — ship.
- **Expected** — a definition would be verified to register before it shipped; the `build` gate's definition-inventory
  assertion is the check that implements that expectation.
- **Observed** — `.opencode/skills/begin-unit/SKILL.md` was written without frontmatter, so the loader's resolved skill
  inventory omitted it while the probe's count of the tree's definition files read 21. The local `check`,
  `openspec validate --all`, and the marker/table checks all passed; the defect surfaced only in the pull request's
  `build` job, at the step that probes the OpenCode configuration, and required a fixup commit and a second run.
- **Evidence** — the failed run `42250066417` reports `agents=11 commands=31 skills=21` and then
  `##[error]OpenCode definition skill '.opencode/skills/begin-unit' is absent from the resolved skill inventory`; the
  repository's own note says the gate probes that every agent/command/skill definition resolves with its metadata
  intact, and the probe lives in the workflow's `build` job rather than in the `check` graph.
- **Consequence** — one fixup commit registered the frontmatter and the rerun passed; the delivered definition is
  correct, and no other artifact needed repair. None of the gates this unit ran detects the class locally.
- **Knowledge boundary** — observed: the failing step's log, the local green gates, and the workflow's step list. Not
  checked: whether adding the probe to `check` is feasible (it installs the v1 binary), and whether other definition
  kinds (agents, commands) fail the same way when unregistered.
