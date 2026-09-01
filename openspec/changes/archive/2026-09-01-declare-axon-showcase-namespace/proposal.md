## Why

The four application/infrastructure Helm releases (db-events, kafka, os-views, axon-showcase) declare no namespace, so
`helmInstallToLocal` deploys them wherever the kube context or `helm.namespace` gradle property points — a
machine-dependent, undeclared default. The intended layout is a deliberate two-namespace split (`monitoring` for the
observability stack, an application namespace for the app and its infra), but only half of it is declared: kps/tempo
set `namespace = "monitoring"` in the release DSL, while the four app/infra releases set nothing. This makes local
deployment non-reproducible across machines.

## What Changes

- Declare a dedicated `axon-showcase` namespace for the four app/infra releases (db-events, kafka, os-views,
  axon-showcase) by setting `namespace = "axon-showcase"` and `createNamespace = true` on each in `build.gradle.kts`,
  mirroring how kps/tempo already declare `namespace = "monitoring"`.
- Update the manual `helm install` commands in `AGENTS.md` and `README.md` to pass `--namespace axon-showcase
  --create-namespace` for those four releases, so the documented manual path matches `helmInstallToLocal`.
- The `monitoring` literals inside the per-release values files (NetworkPolicy ingress labels, the tempo endpoint, and
  the ServiceMonitor namespace references) are the integration contract with the observability stack and are left
  unchanged.

## Capabilities

### New Capabilities
<!-- none -->

### Modified Capabilities
- `showcase/quality/merge-governance`: the "Helm release namespaces are declared in the build" requirement documents
  the declared namespace layout — observability releases in `monitoring`, application and infrastructure releases in
  `axon-showcase`, each created on install.

## Impact

- `build.gradle.kts` — `namespace` + `createNamespace = true` on the four app/infra releases.
- `AGENTS.md`, `README.md` — `--namespace axon-showcase --create-namespace` flags on the manual `helm install`
  commands for those releases.
- `openspec/specs/showcase/quality/merge-governance/spec.md` — new requirement describing the release namespace
  layout.
- No chart, values-file, or test changes.