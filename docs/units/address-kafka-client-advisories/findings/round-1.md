- [missing-task] `tasks.md:12-26` — the plan never compiles the `e2eTest` classpath, which the `gradle-build` pack
  requires for a resolution-affecting change; the gateway's `e2eTest` suite resolves `kafka-clients`.
- [claim-unreproducible] `tasks.md:21-22` — the Docker-free `check` does not run the spec-delta validation;
  `openspec validate` runs only in CI.
- [claim-missing] `design.md:97-116` — no claims row for the arrival path
  `axon-kafka → cloudevents-kafka → kafka-clients`, nor for "the 3.9.x line ends at 3.9.2".
- [stale-base] `HEAD` — the tree is one commit behind `origin/main` (`21b100bf`), so its diff against that ref deletes
  `docs/audits/2026-11-07.md`.
- [formatting] `tasks.md:15` — five-space runs inside the inline code span, left by a hand-written line break.
- [formatting] `tasks.md:18` — the same five-space runs inside the inline code span of the integration-suite task.
