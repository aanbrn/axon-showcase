# Restructure the spec corpus per the audit findings

## Why

The two scheduled audit reports recorded three spec-corpus findings that are structural rather than behavioural, and
each needs a delta rather than a text edit: two Helm requirements sit in `merge-governance`, whose Purpose is merge and
CI governance, although they describe the Gradle Helm-release configuration (a deployment surface); three "details"
requirements in `helm-chart` refine subjects that already have a core requirement, having been added as new requirements
instead of folded in; and seven requirement headers across `web-ui` and `live-events` are bare imperative verb phrases
where the corpus convention — stated in the `agent-skills` spec that defines this audit — is a declarative noun phrase.

## What Changes

- **`showcase/quality/merge-governance`** — a `REMOVED` block for the two misplaced Helm requirements ("Helm release
  namespaces are declared in the build", "Each Helm release target declares its kube context") and an `ADDED` block for
  them in `deployment/helm-chart`, whose Purpose widens to name the Gradle Helm release-target configuration.
- **`showcase/deployment/helm-chart`** — three merges, each `MODIFIED` carrying the core requirement's scenarios plus
  the details requirement's, so no behaviour is lost: "Horizontal autoscaling details" and "Vertical autoscaling
  details" fold into "Horizontal and vertical autoscaling"; "Pod disruption budget details" folds into "Pod disruption
  budgets".
- **`showcase/clients/web-ui`** and **`showcase/gateway/live-events`** — the seven imperative headers retitled to noun
  phrases, each as a `REMOVED` + `ADDED` pair carrying the full body and scenarios (a `MODIFIED` block cannot rename a
  header).

## Impact

- **Build**: none — spec content only.
- **Tests**: none.
- **Specs**: four capabilities change; `helm-chart` needs its `## Purpose` refreshed in the archive commit (it gains the
  release-target requirements and absorbs the details ones; a delta cannot carry a Purpose), while `merge-governance`'s
  is unchanged — the removed requirements were never described by it.

## New Capabilities

None.

## Modified Capabilities

- `showcase/quality/merge-governance` — loses the two Helm release-configuration requirements.
- `showcase/deployment/helm-chart` — gains them, and its three "details" requirements merge into their core
  requirements.
- `showcase/clients/web-ui` — six requirement headers retitled.
- `showcase/gateway/live-events` — one requirement header retitled.
