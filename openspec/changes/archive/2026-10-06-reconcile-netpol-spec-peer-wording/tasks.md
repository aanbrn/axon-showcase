# Tasks

## 1. Reconcile the spec wording

- [x] 1.1 Add the `MODIFIED` `Network policies` requirement to
      `openspec/changes/reconcile-netpol-spec-peer-wording/specs/showcase/deployment/helm-chart/spec.md`, carrying all
      six of the main spec's scenarios in order with the three peer-wording corrections ("the same service" /
      "same-service pods" → the release), and verify `openspec validate --changes` passes. **Outcome: the delta carries
      all six scenarios in order; `openspec validate --changes` passes.**
- [x] 1.2 Remove the implemented idea from `docs/ideas.md` (the 2026-10-06 netpol-wording entry) and refresh any prose
      that calls it the proposed fix. **Outcome: the 2026-10-06 section is removed (no other idea referenced it).**
- [x] 1.3 Run `./gradlew spotlessApply` + `spotlessCheck` after the final edit to a Spotless-owned file (the delta and
      `docs/ideas.md` are markdown-gated), and `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`. **Outcome: all
      green.**
- [x] 1.4 Run the `lesson-capture` subagent over the diff and review findings, apply its durable proposals, record the
      applied net `AGENTS.md` delta on this task, and re-run `spotlessApply` + `spotlessCheck` after the final edit.
      **Outcome: one proposal applied (merged into the "Verify documented infrastructure/deployment numbers against the
      config files, not memory" bullet — read a chart-rendering claim from the selector the template emits, not its
      comments); no retirements. Net `AGENTS.md` delta: +4 lines. `spotlessApply`/`spotlessCheck` and
      `verifyCapturedMarkers` pass after the final edit.**
