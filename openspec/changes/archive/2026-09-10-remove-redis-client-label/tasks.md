## 1. Values cleanup

- [x] 1.1 Remove the `axon-showcase-redis-client: "true"` line from `projectionService.podLabels` in
      `helm/values/axon-showcase/values-local.yaml` and verify the file no longer references redis
- [x] 1.2 Remove the `axon-showcase-redis-client: "true"` line from `queryService.podLabels` in
      `helm/values/axon-showcase/values-local.yaml` and verify the file no longer references redis

## 2. Verification

- [x] 2.1 Render the chart with the local values and verify the projection and query service pods carry only their live
      `-client` labels (projection: kafka, os-views; query: os-views) and no `axon-showcase-redis-client`
- [x] 2.2 Run `openspec validate --changes` and verify the change is valid
