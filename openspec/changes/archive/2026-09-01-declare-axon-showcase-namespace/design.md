## Context

See `proposal.md` for the motivation. Today only kps/tempo declare `namespace = "monitoring"` in `build.gradle.kts`;
the four app/infra releases (db-events, kafka, os-views, axon-showcase) rely on the plugin's `helm.namespace`
convention or the kube context's current namespace, so `helmInstallToLocal` is not reproducible across machines. The
app reaches its infra by short DNS names (`axon-showcase-db-events`, `axon-showcase-kafka:9092`,
`http://axon-showcase-os-views:9200`), so the four releases must share a namespace. The `monitoring` literals inside
the per-release values files (NetworkPolicy ingress labels, the tempo endpoint, ServiceMonitor namespace references)
are the integration contract with the observability stack and are intentionally out of scope.

## Goals / Non-Goals

**Goals:**
- Declare a dedicated `axon-showcase` namespace for the four app/infra releases, matching the explicit `monitoring`
  namespace kps/tempo already declare.
- Make `helmInstallToLocal` reproducible: the four releases deploy to a declared namespace, not a machine-dependent
  default.

**Non-Goals:**
- Not making namespaces configurable per release target — the project has a single `local` target today, and the
  plugin's `forTarget("prod") { namespace = ... }` is the documented extension point when a second target appears.
- Not parameterizing the `monitoring` literals in the values files — they are the observability integration contract
  (NetworkPolicy ingress from the monitoring namespace, the tempo endpoint, ServiceMonitor placement) and stay as-is.
- Not adding gradle-property providers, filter tasks, or placeholders — the minimal explicit declaration.

## Decisions

**Decision: hardcode `namespace = "axon-showcase"` + `createNamespace = true` on the four app/infra releases.**
Mirrors kps/tempo's existing `namespace = "monitoring"` style. Rationale: the user chose the minimal explicit
declaration over the earlier parameterization approaches (providers, generated values, filter tasks), which were
over-engineered for a single-target reference app. The four releases must move together because the app resolves its
infra by short DNS names. Alternatives considered (all rejected as over-engineered):
- *Gradle-property providers with defaults* — user-overridable but adds indirection with no current per-target need.
- *Generated/filtered values files with placeholders* — the plugin's `values-<target>.yaml` mechanism plus a filter
  task; several failed attempts showed this fights the values files' purpose for a fixed single-target layout.
- *Reusing the kube context's default namespace* — the status quo; non-reproducible.

**Decision: keep the `monitoring` values-file literals unchanged.**
The NetworkPolicy ingress labels and tempo endpoint must name the namespace where kps/tempo run (`monitoring`); the
ServiceMonitor namespace could theoretically follow the release namespace, but moving it is a separate concern and the
current placement in `monitoring` (where kps discovers ServiceMonitors across all namespaces) is fine. Changing these
is out of scope.

## Risks / Trade-offs

- [A user with a stale local deployment has the four releases in `default`, not `axon-showcase`] → Re-running
  `helmInstallToLocal` installs them into the new namespace; an uninstall of the old releases is a one-time manual
  step noted in the migration plan.
- [A future `prod` target needs a different app namespace] → The plugin's `forTarget("prod") { namespace = ... }`
  override is the documented, localized extension point; not built today.

## Migration Plan

1. Set `namespace = "axon-showcase"` and `createNamespace = true` on the four app/infra releases in `build.gradle.kts`.
2. Update the manual `helm install` commands in `AGENTS.md`/`README.md` with `--namespace axon-showcase
   --create-namespace` for those releases.
3. Verify `helmInstallToLocal` renders all six releases into their namespaces; run
   `check -PskipITs -Pcoverage.gate.enabled=false` and `openspec validate --all`.
4. Rollback: revert the release DSL and doc edits.

## Open Questions

None.