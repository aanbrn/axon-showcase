- [incomplete-enumeration] tasks.md:34 — the disabled-feature assertion names alertmanager, node-exporter and the two
  self-monitoring switches, but the values file also disables grafana's default dashboards, grafana's ServiceMonitor and
  grafana's alertmanager-datasource sidecar.
- [coherence] design.md:137 — the targets row keeps the pre-refinement predicate ("every active target up, none down")
  while the design and tasks now require a non-empty set that loses none of the baseline's named targets.
- [coherence] proposal.md:35 — the Impact still describes the pre-refinement verification, omitting the pre-upgrade
  baseline, its stop conditions, the migration-note gate, the disabled-feature render assertion and the baseline
  comparison.
- [formatting] tasks.md:48 — the inline code span reads `helm list -n     monitoring` with a whitespace run a formatter
  cannot reach.
