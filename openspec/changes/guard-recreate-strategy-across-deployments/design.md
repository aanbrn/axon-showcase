# Design — guard the `Recreate` strategy on every service Deployment

## Context

`projectionService.updateStrategy.type` defaults to `Recreate` (`values.yaml`), and that template carries the guard its
upgrade needed: `rollingUpdate: null` beside the type, because a server-side apply that changes only the type keeps the
live release's `rollingUpdate` block and the API rejects the pair (ADR-0022 decision 6, and the template's own comment).

The other four service templates — `command-service`, `query-service`, `api-gateway`, `web-ui` — render `strategy` from
`updateStrategy` alone, and `values.yaml` documents `Recreate` for all five. A target that picks it on any of the four
fails its next upgrade.

## Decisions

1. **One named template owns the rule.** `axon-showcase.deploymentStrategy` in `_helpers.tpl` takes the service's
   `updateStrategy` value and the root context, carries the file's usual doc comment (its expected dot, and the reason
   the pair is rejected — the projection template's two-line rationale moving into it), renders `strategy:` with the
   mapping at two spaces, and appends `rollingUpdate: null` at the same level when the type is `Recreate`. The helper —
   not each call site — owns the `updateStrategy` truthiness guard. Each of the five Deployments replaces its two-line
   `strategy:` render with:
   `{{- include "axon-showcase.deploymentStrategy" (dict "value" .Values.<service>.updateStrategy "context" $) | nindent 2 }}`.
   Verified in a scratch chart: the helper renders `strategy:` / `type: Recreate` / `rollingUpdate: null` at 2/4/4 and
   parses.
2. **The renders go through the plugin's client, resolved by a script.** `scripts/render-chart-strategies.py` runs
   `helm template` over the collected chart with every service on one strategy, resolving the CLI from the Gradle Helm
   plugin's downloaded cache at the catalog's Helm version first (`.gradle/helm/client/<version>/<platform>/helm`,
   passed as `--client-version` and declared as a task input), then `$HELM_EXECUTABLE`, then `PATH`, then the newest
   cached client sorted numerically — so the render uses the CLI the catalog pins rather than whichever client `PATH`
   holds. The plugin's own `HelmTemplate` task type was the first choice and does not register outside its DSL: its rule
   expects a chart descriptor, while the public `chart` property is a path string, so a standalone registration fails
   configuration with "A dependency must not be empty". The script keeps the intent — no host Helm, the plugin's client,
   the packaging path as the source of truth.
3. **The negative render is explicit.** The chart's default is already `Recreate` for the projection service, so the
   guard's absence is asserted over a render with all five services set to `RollingUpdate`, not over the defaults.
4. **The assertion is a committed script with a committed control.** `scripts/verify-chart-strategies.py` parses each
   Deployment's `spec.strategy` mapping — comment lines excluded, so a commented `# rollingUpdate: null` is not a guard
   — and returns one violation per Deployment whose rendered strategy contradicts its render: a `Recreate` render
   without `rollingUpdate: null` (or without the type), a `RollingUpdate` render carrying `rollingUpdate: null` (or
   without its type), and a missing or strategy-less Deployment. `scripts/test-verify-chart-strategies.py` covers both
   directions, a stray guard, a wrong type, a commented guard, a nested wrong shape, an omitted Deployment and a render
   tree with two chart roots, over fixture trees (eight tests), so `check` exercises the assertions without Helm. A
   Python script with its test is the repository's checker shape (as the OpenSpec delta guard is); the rules-object form
   was proposed and is not required for a chart assertion whose only input is rendered text.
5. **Ordering and invalidation are declared, not assumed.** `verifyChartStrategies` depends on both render tasks and
   declares their output directories as inputs; each render task declares the render script and the collected chart
   directory as inputs and depends on the chart's dependency fetch, because the collection alone re-runs a task whose
   own inputs did not change — a template edit would otherwise leave the render UP-TO-DATE.
6. **The outcome is spec'd, not the mechanism.** `helm-chart`'s "Service deployments" gains a scenario: a service
   Deployment whose strategy type is `Recreate` renders the field that lets a Helm-owned upgrade remove the previous
   release's `rollingUpdate` block. The scenario, not this decision, carries the scope's boundary — a block owned by
   another manager is outside it.
7. **The `deployment-operations` pack's "Chart validation" paragraph is refreshed** with the new check and the render
   path it reads, since that paragraph is where the next agent looks for how the chart is validated.

8. **The gate cost is measured, not assumed.** On a warm local `check` the addition is ≈ 3.4 s (68.01 s without the four
   tasks, 71.38 s with them, same warm state, order reversed); the two renders alone take 1.30 s wall and the assertion
   2.22 s. The chart's collection and the `common` dependency fetch are shared with the packaging path, so no second
   fetch is paid. Routing on the implemented diff is `REQUIRED` (changed `helm/` paths), so the deep review runs.

## The helper

```gotemplate
{{- /*
Renders a service Deployment's update strategy, clearing the field a Recreate upgrade must remove.
Expected dot: .value (the service's updateStrategy) and .context (root context used to render templated values).
A server-side apply that only changes the type keeps the existing release's strategy.rollingUpdate, and the API
rejects that pair; the null removes a Helm-owned field during the upgrade.
*/ -}}
{{- define "axon-showcase.deploymentStrategy" -}}
{{- if .value }}
strategy:
  {{- include "common.tplvalues.render" (dict "value" .value "context" .context) | nindent 2 }}
  {{- if eq .value.type "Recreate" }}
  rollingUpdate: null
  {{- end }}
{{- end }}
{{- end }}
```

## Consequences

- A target that sets `Recreate` on any service upgrades when the live Deployment's strategy block is Helm's own to
  change — the transition ADR-0022 records. A Deployment whose `rollingUpdate` block another manager owns still fails;
  hardening that case is the separate parked entry ("Harden the `Recreate` upgrade against a foreign-owned
  `rollingUpdate`") and is not part of this change. A sixth service Deployment that calls the helper inherits the guard.
- The chart carries one strategy rule instead of five copies; `check` gains one verification whose failure names the
  Deployment missing the guard, and no host tool it did not already need.
