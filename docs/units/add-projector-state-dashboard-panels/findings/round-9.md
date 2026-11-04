- [contradiction] build-logic/src/main/kotlin/DashboardJsonRules.kt:108-110 — the expression rule skips row panels while
  the delta states the obligation for every top-level panel's declared target.
- [contradiction] docs/units/add-projector-state-dashboard-panels/unit.md:15-26 — the resume checkpoint still describes
  the proposal stage and calls the approval pending, while the implementation is staged and task 6.2 is unticked.
- [claim-unreproducible] openspec/changes/add-projector-state-dashboard-panels/design.md:65-66 — the quoted age-panel
  legend does not match the JSON's `legendFormat`, which reads "since last resolved batch (idle time included)".
- [test-control] build-logic/src/test/kotlin/DashboardJsonRulesTests.kt — the non-numeric `gridPos` guard and the
  untitled-panel naming have no case.
