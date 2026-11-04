- [coherence] design.md:74-75 — "the batch-size panel stays dimensionless" contradicts task 5.2's ban on `unit: "none"`,
  which that panel carries. `unit: "none"`, which that panel carries.
- [claim-unreproducible] design.md:13 — "87 of 91 targets" does not reproduce: 97 targets, 76 with a window, 87 `[1m]`
  occurrences across 72 targets.
- [test-control] tasks.md:43-46 — the `$__rate_interval` inspection reads a panel that still carries `[1m]`, so it
  cannot discriminate the edit.
- [test-control] tasks.md:36-39 — the failure-panel verification states no series today, so it passes vacuously.
- [test-control] design.md:76-78 — the description rule is specified "at any depth" but no fixture is a nested
  target-bearing panel.
- [claim-missing] design.md:17-20 — the advisor consultation is asserted as evidence while no record holds the owner's
  authorization.
- [missing-task] design.md:52-58 — the cumulative-and-reset description content decision 5 requires has no task.
- [impact-omission] build-logic/src/main/kotlin/DashboardJsonRules.kt:12-16 — the rules object's KDoc and the Gradle
  task description stay JSON-only after the change.
- [incomplete-enumeration] design.md:72-73 — "the two panels whose metric is created on first use" under-matches: the
  by-id family backs three panels, web errors one.
- [incomplete-enumeration] design.md:41 — the gRPC families named omit the third live one,
  `grpc_server_call_duration_seconds_*`.
- [wording] proposal.md:31 — the delta is named by the main spec's path, which this unit must not edit.
- [structured-writing] proposal.md:19-25 — the Availability and rate-window bullets each join several independently
  actionable statements.
- [formatting] design.md:118-120 — unescaped `|` in the claims-table results splits the rows into more cells than the
  header.
- [formatting] tasks.md:78 — a five-space run inside an inline code span.
