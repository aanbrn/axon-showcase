- [claim-unreproducible] design.md:88 — the "subchart dependencies are unchanged" row does not reproduce: 93.1.0 ships
  kube-state-metrics 9.0.0, prometheus-node-exporter 5.0.0, grafana 14.4.0 and prometheus-windows-exporter 0.14.0
  against 92.17.0's 8.8.0, 4.60.1, 14.3.0 and 0.12.*.
- [premise] design.md:59 — "the render with values-local.yaml catches a dropped or renamed value" is false: the chart
  ships no values.schema.json, so helm template silently ignores a key the new chart no longer reads.
- [missing-task] tasks.md:28 — the deployed-signal step cannot read Grafana's /api/datasources and /api/search without
  the generated admin password, and its host-side helm/kubectl reads name no kube context.
- [claim-unreproducible] design.md:95 — `git grep -n 92\.17\.0 -- . ':!openspec/changes'` cannot exit 1: the unit record
  carries the old pin, and the task's archive allowance is unreachable under that pathspec.
- [stale-base] docs/units/bump-kps-chart-2026-11-09/review-handover.md — the tree was one commit behind origin/main,
  whose diff deletes docs/audits/2026-11-07.md, work the unit did not touch.
- [claim-missing] design.md:15 — the local cluster's kps release state (chart 92.17.0 from bump-kps-chart-2026-11-01)
  has no claims row and no fallback if the release is absent.
- [missing-task] tasks.md:31 — the verification runs no root build `check`, so a catalog-owned change is verified by
  verifyInstallCommands alone.
- [structured-writing] tasks.md:12 — tasks 1.3 and 2.4 each hide several independently actionable statements joined by
  "and" or commas.
