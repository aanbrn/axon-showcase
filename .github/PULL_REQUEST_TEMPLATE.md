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
- [ ] The per-unit `lesson-capture` ran — recorded as a task in the change's `tasks.md` with the applied `AGENTS.md`
      delta, or, for a unit with no change dir, by ticking this line and noting the applied delta
