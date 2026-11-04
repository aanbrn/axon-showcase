# Guard the `Recreate` strategy on every service Deployment

## Why

Only the projection-service template clears `strategy.rollingUpdate` when the strategy type is `Recreate`. The other
four service Deployments — command-service, query-service, api-gateway, web-ui — render `strategy` from `updateStrategy`
alone, and each service's values document `Recreate` as a supported choice. A target that picks it there hits the
upgrade rejection the projection service already recorded: a server-side apply that changes only the type keeps the live
release's `rollingUpdate` block, and the API refuses the pair.

## What Changes

- The `rollingUpdate: null` rule moves into one named template, so all five service Deployments render the guard from a
  single place rather than five copies of it.
- A committed check renders the chart twice — once with every service set to `Recreate`, once with all five set to
  `RollingUpdate` — and asserts the guard appears in the first and nowhere in the second, with its own tests.
- The `helm-chart` capability gains the outcome: a service Deployment whose strategy type is `Recreate` renders the
  block that lets a Helm-owned upgrade remove the previous release's `rollingUpdate`; a block owned by another manager
  stays the separate parked entry's subject.
- The parked `docs/ideas.md` entry is removed by the change that implements it.

### New Capabilities

None.

### Modified Capabilities

- `showcase/deployment/helm-chart` — "Service deployments" gains a scenario for a `Recreate` strategy, and the reason
  the rendered block is required.
