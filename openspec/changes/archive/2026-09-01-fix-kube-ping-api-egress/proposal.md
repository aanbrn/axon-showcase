## Why

The Axon distributed command bus uses JGroups `KUBE_PING` for peer discovery, which queries the Kubernetes API to find
cluster members. The chart's `commandService` and `apiGateway` NetworkPolicies restrict egress to DNS, same-namespace
traffic, and OTLP ports — they do not allow egress to the Kubernetes API. kube-router (k3s's NetworkPolicy controller)
enforces these egress rules, so KUBE_PING's connection to the API is refused, the JGroups cluster never forms across
nodes, and the gateway fails command dispatch (`NoHandlerForCommandException`, HTTP 503). This is proven on a fresh
k3s cluster: adding egress for the API to the two NetworkPolicies makes the cluster form and the command path work;
without it, discovery fails deterministically.

## What Changes

- Fix the Kubernetes-API egress rule in the `command-service` and `api-gateway` NetworkPolicy templates so it actually
  renders: the `lookup` for the `kubernetes` EndpointSlice targets the `default` namespace instead of the release
  namespace. The `kubernetes` EndpointSlice always lives in `default`, so looking it up in the release namespace
  returned nil and the `{{- with }}` block silently omitted the API egress rule — which is what blocked KUBE_PING.
- The egress rule allows TCP to the apiserver endpoint discovered from the `default`/`kubernetes` EndpointSlice
  (the service DNATs to the apiserver's actual address and port), so KUBE_PING can reach the Kubernetes API.
- No values-file or other chart change is needed — the templates already render the correct rule once the lookup
  namespace is fixed.

## Capabilities

### New Capabilities
<!-- none -->

### Modified Capabilities
- `showcase/deployment/helm-chart`: the "Network policies" requirement's kube-ping scenario is corrected — the
  Kubernetes-API egress rule SHALL render from the `default` namespace's `kubernetes` EndpointSlice, so KUBE_PING can
  discover cluster members.

## Impact

- `helm/chart/src/main/helm/templates/command-service/networkpolicy.yaml` — `lookup` namespace for the `kubernetes`
  EndpointSlice changed from the release namespace to `default`.
- `helm/chart/src/main/helm/templates/api-gateway/networkpolicy.yaml` — same fix.
- No image, service, or other test changes.