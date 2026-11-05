- [test-control]
  showcase-projection-service/src/test/java/showcase/projection/ShowcaseProjectorBacklogSamplerTests.java:78-91 — the
  start/stop case passed with `shutdownNow()` removed, since `isRunning()` reads only the cleared reference.
- [contradiction] openspec/changes/surface-projector-backlog/proposal.md:26-27 — What Changes still said the lifecycle
  requirement gains the backlog outcomes the split moved to the added requirement.
- [missing-task] docs/units/surface-projector-backlog/review-handover.md — the round-7 hand-over section was not written
  before the invocation.
- [proposal-schema] openspec/changes/surface-projector-backlog/proposal.md:32-44 — the proposal omitted the template's
  `## Capabilities` parent and invented `### New Requirements`.
- [missing-task] openspec/changes/surface-projector-backlog/tasks.md:73-86 — no Done note recorded where the
  failed-sample counter is verified.
- [wording] .opencode/skills/deployment-operations/SKILL.md:366 — three sites pointed at "the requirement in
  `read-side/projection-service`" after the split left two projector requirements.
