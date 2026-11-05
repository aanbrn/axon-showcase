- [contradiction] openspec/changes/add-operational-status-overview/tasks.md:17-18 — the plan mixed panel-count
  conventions: the README counts non-row panels, while task 4.3 said 51 panels and task 1.1 said "grew by exactly two".
- [premise] openspec/changes/add-operational-status-overview/design.md:47-53 — a per-target "No telemetry" state cannot
  exist in a stat panel, since a target with no series yields no entry.
- [missing-task] openspec/changes/add-operational-status-overview/tasks.md:39-40 — the README bullet's coverage sentence
  has no task, although its enumeration gains the operational-status coverage.
- [claim-unreproducible] openspec/changes/add-operational-status-overview/design.md:59-61 — `up` does carry `service`,
  the kube-state series carry it too, and the dashboard uses no `job=`/`deployment=` selectors, so the convention
  analogy is false.
- [over-claim] openspec/changes/add-operational-status-overview/design.md:120 — "no alerting exists" is wider than the
  source: the chart ships default alert groups and only Alertmanager delivery is disabled.
- [claim-unreproducible] openspec/changes/add-operational-status-overview/design.md:119 — the gateway's bucket series
  carry 73 `le` values, not 74.
- [coherence] openspec/changes/add-operational-status-overview/design.md:80-82 — the named `textMode: value_and_name`
  fallback does not keep the per-Deployment pairing decision 3 requires.
- [wording] openspec/changes/add-operational-status-overview/proposal.md:8-9 — "the two SLO boundary entries" names no
  two entries: the query service has one.
- [coherence] openspec/changes/add-operational-status-overview/tasks.md:62-63 — task 5.2 lists the proposal consultation
  as pending while the design records it as run.
- [structured-writing] openspec/changes/add-operational-status-overview/design.md:47-53 — decision 2 packs five
  implementation facts into one sentence.
