## Context

See `proposal.md` for the motivation. The Axon distributed command bus uses JGroups with `KUBE_PING` discovery, which
queries the Kubernetes API to find cluster members by pod labels. The chart's `commandService` and `apiGateway`
NetworkPolicy templates already contain an egress rule intended to allow access to the Kubernetes API — it looks up
the `kubernetes` EndpointSlice and renders an `ipBlock`+port rule for the apiserver. But the `lookup` targets the
**release namespace** (`common.names.namespace`, e.g. `axon-showcase`), while the `kubernetes` EndpointSlice always
lives in the **`default`** namespace. The lookup returns nil, the `{{- with }}` block silently renders nothing, and
KUBE_PING's connection to the API is refused — so the JGroups cluster never forms across nodes and command dispatch
fails (HTTP 503). This was proven by inspecting the deployed NetworkPolicy: it had no `ipBlock` egress rule at all.

## Goals / Non-Goals

**Goals:**
- Fix the `lookup` namespace in the `command-service` and `api-gateway` NetworkPolicy templates so the Kubernetes-API
  egress rule actually renders.
- Confirm the rendered NetworkPolicies allow egress to the apiserver (the `default`/`kubernetes` EndpointSlice), so
  KUBE_PING discovery works.
- Align the spec's kube-ping scenario with the intended (now fixed) behavior.

**Non-Goals:**
- Not changing the namespace declaration change (`declare-axon-showcase-namespace`), which is correct and unrelated.
- Not touching the query/projection NetworkPolicies (they do not join the JGroups cluster).
- Not adding values-file overrides — the chart's intended rule is correct once the lookup namespace is fixed.

## Decisions

**Decision: change the `lookup` namespace from the release namespace to `default` in both NetworkPolicy templates.**
The `kubernetes` service and its EndpointSlice always live in the `default` namespace; looking them up in the release
namespace (`common.names.namespace`) returns nil and silently drops the API egress rule. Changing the lookup to
`"default"` makes it find the EndpointSlice and render the `ipBlock`+port rule for the apiserver. Rationale: the chart
already expresses the intended rule; the one-line lookup fix is minimal and correct. Alternatives considered:
- *`extraEgress` in `values-local.yaml`* — unnecessary; the chart's rule is meant to handle this, and per-environment
  overrides would hide the chart bug.
- *Allow port 443 only* — proven insufficient; kube-router evaluates egress on the post-DNAT destination (the
  apiserver endpoint port), which the EndpointSlice lookup provides correctly.

**Decision: rely on the EndpointSlice lookup for the apiserver address and port.**
The `default`/`kubernetes` EndpointSlice carries the apiserver's real address (`192.168.64.2`) and port (e.g. `63062`
on colima, `6443` on standard k8s). The egress rule renders an `ipBlock` for that address on that port, covering the
post-DNAT destination kube-router checks. This is environment-agnostic — no hardcoded port needed.

## Risks / Trade-offs

- [The apiserver endpoint port is dynamic on colima (changes per colima recreate)] → The EndpointSlice lookup picks it
  up automatically at install time; no manual port tracking needed.
- [Allowing egress to the apiserver widens the egress surface] → Necessary for KUBE_PING; scoped to the single
  apiserver address/port from the EndpointSlice, not broad internet egress.
- [kube-router's post-DNAT egress evaluation is implementation-specific] → This is the observed k3s/kube-router
  behavior; the EndpointSlice-derived rule covers it by allowing the actual endpoint port.

## Migration Plan

1. Change the `lookup` namespace from `common.names.namespace` to `"default"` in
   `helm/chart/src/main/helm/templates/command-service/networkpolicy.yaml` and
   `helm/chart/src/main/helm/templates/api-gateway/networkpolicy.yaml`.
2. Reinstall the app chart and verify the deployed NetworkPolicies include the `ipBlock` egress rule for the apiserver.
3. Verify a POST `/showcases` succeeds (the JGroups cluster forms across command-service and api-gateway).
4. Update the "Network policies" requirement and the kube-ping scenario in the spec.
5. Run `check -PskipITs -Pcoverage.gate.enabled=false` and `openspec validate --all`.
6. Rollback: revert the two template lookup changes and the spec edits.

## Open Questions

None.