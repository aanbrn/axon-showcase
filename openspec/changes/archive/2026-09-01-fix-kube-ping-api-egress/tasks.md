## 1. Chart template fix

- [x] 1.1 Change the `lookup` namespace from `common.names.namespace` to `"default"` for the `kubernetes` EndpointSlice
      in `helm/chart/src/main/helm/templates/command-service/networkpolicy.yaml` and
      `helm/chart/src/main/helm/templates/api-gateway/networkpolicy.yaml`, keeping lines within 120 characters
- [x] 1.2 Verify the rendered NetworkPolicies include the API egress rule: `helm template` (or a real install) shows an
      `ipBlock` for the apiserver address on the apiserver port for command-service and api-gateway

## 2. Spec and verification

- [x] 2.1 Sync the `showcase/deployment/helm-chart` spec's "Network policies" requirement and its kube-ping scenario
      (the API egress rule SHALL render from the `default` namespace's `kubernetes` EndpointSlice), keeping lines within
      120 characters
- [x] 2.2 Redeploy the app chart and verify the command path works end-to-end: a POST `/showcases` succeeds (JGroups
      cluster forms across command-service and api-gateway, KUBE_PING no longer blocked)
- [x] 2.3 Run `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` and `openspec validate --all`, confirming the
      build and spec validation pass