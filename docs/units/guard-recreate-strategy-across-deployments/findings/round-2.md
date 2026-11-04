- [premise] openspec/changes/guard-recreate-strategy-across-deployments/design.md — the negative render asserted no
  `rollingUpdate: null` under the defaults, but the projection service's default is already `Recreate`, so a correct
  chart would fail it.
- [contradiction] openspec/changes/guard-recreate-strategy-across-deployments/design.md — the helper placed `nindent 4`
  mapping keys beside a two-space `rollingUpdate: null`, which renders invalid YAML.
- [premise] openspec/changes/guard-recreate-strategy-across-deployments/design.md — the check assumed `check` already
  invokes a host Helm, while the chart path runs the plugin's downloaded client and CI installs no Helm.
- [concurrency] openspec/changes/guard-recreate-strategy-across-deployments/tasks.md — the render task joined `check`
  beside the packaging with no ordering and no named render tree.
- [wording] openspec/changes/guard-recreate-strategy-across-deployments/tasks.md — one task conflated the checker with
  its test and left the registration file open.
- [missing-task] openspec/changes/guard-recreate-strategy-across-deployments/tasks.md — no task refreshed the
  `deployment-operations` pack's chart-validation paragraph.
