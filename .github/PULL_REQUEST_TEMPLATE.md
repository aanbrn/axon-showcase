<!-- Keep the two sections below: they mirror the shape recent pull requests in this repository follow. -->

## Summary

<!-- What changes, and why. Link the issue or OpenSpec change this implements. -->

## Test plan

<!-- How this was verified — the commands run, the gates that passed, and anything deliberately left undone.
     `./gradlew check` is the baseline (`-PskipITs -Pcoverage.gate.enabled=false` for a Docker-free run), and
     `./gradlew spotlessApply` belongs after the last edit to a Spotless-owned file. -->

- [ ] `./gradlew check` (or the Docker-free variant) is green
- [ ] `spotlessApply` run after the final edit to a Spotless-owned file
- [ ] Behavior changes went through an OpenSpec change (with its archive commit on this PR) — or this is a docs refresh,
      a standalone fix, or a dependency bump, which need no change dir
- [ ] The per-unit `lesson-capture` decision is recorded — it ran with its applied `AGENTS.md` delta, or it was skipped
      under `AGENTS.md`'s skip condition with the reason — as a task in the change's `tasks.md` or, for a unit with no
      change dir, by ticking this line and noting the outcome
- [ ] The pre-write derivation pass ran, its **claims table** recorded — in the change's `design.md` (or `tasks.md` for
      a unit that writes no `design.md`), or by ticking this line and noting the table for a unit with no change dir
