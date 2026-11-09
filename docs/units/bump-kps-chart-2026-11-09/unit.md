# Unit: bump-kps-chart-2026-11-09

Workspace:
`unit=bump-kps-chart-2026-11-09; path=/Users/afanasyev/Development/axon-showcase-bump-kps-chart-2026-11-09; branch=bump-kps-chart-2026-11-09; writer=ses_e42e676faffeOZmBDpKjWId9pX`

State: active

The unit actions the Helm update check's `prometheus-community-stack` row, moving the catalog pin from `92.17.0` to the
newest reachable `92.x` (`92.22.0`) and verifying the bump with a live install on the local cluster; the `93.x` row is
held because its operator images are amd64-only. Its plan and tasks live in
`openspec/changes/bump-kps-chart-2026-11-09/`.
