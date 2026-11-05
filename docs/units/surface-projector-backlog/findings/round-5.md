- [test-control]
  showcase-projection-service/src/integrationTest/java/showcase/projection/ShowcaseProjectorBacklogIT.java:139-152 — the
  failed-sample counter had no assertion; deleting `failedSamples.increment()` left every test green.
- [contradiction] helm/chart/src/main/helm/files/grafana-dashboards/axon-showcase.json:4176 — panel 91 collapsed the
  unresolved count with `max(...)` while the design requires pod-matched distance, freshness, and unresolved counts.
- [contradiction] openspec/changes/surface-projector-backlog/specs/showcase/read-side/projection-service/spec.md:64-69 —
  "an unknown end offset" named a per-partition cause that cannot occur: a failed end-offset read fails the whole
  sample.
- [test-control] openspec/changes/surface-projector-backlog/design.md:163 — the `AdminClientConfig.configNames()` filter
  had no control; deleting the filter passed the suite.
- [impact-omission] showcase-projection-service/build.gradle.kts:97 — the integration suite's new dependency was named
  by no artifact.
- [incomplete-enumeration] .opencode/skills/deployment-operations/SKILL.md:364-367 — the unresolved-cause list read as
  exhaustive while the requirement names a fourth case, and the list was copied rather than pointed at.
- [structured-writing]
  openspec/changes/surface-projector-backlog/specs/showcase/read-side/projection-service/spec.md:5-14 — the description
  carried eight independent statements and repeated the sample time.
- [claim-unreproducible] openspec/changes/surface-projector-backlog/design.md:160 — the 44-panel claims row no longer
  reproduced from the shipped tree.
- [contradiction] openspec/changes/surface-projector-backlog/tasks.md:16-19 — task 1.1 stated a one-second timeout floor
  where the code enforces 100 milliseconds.
- [contradiction] openspec/changes/surface-projector-backlog/tasks.md:174-176 — task 4.8 certified a record count the
  next record falsifies.
