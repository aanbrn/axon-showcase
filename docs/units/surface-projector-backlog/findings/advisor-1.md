# Advisor consultation — implementation pass, round 1

- [over-claim] openspec/changes/surface-projector-backlog/tasks.md:95-107 — task 2.5 was marked complete while its own
  text records that no positive deployed backlog was observed; the completion claim exceeded the agreed boundary.
- [wording] helm/chart/src/main/helm/files/grafana-dashboards/axon-showcase.json:4081 — the sample panel's description
  called "a fresh age with an empty backlog" a healthy zero, which a never-committed group also produces with no numeric
  distance at all.
- [hypothesis] helm/chart/src/main/helm/files/grafana-dashboards/axon-showcase.json:4105-4107 — the field override
  matched a literal `{{pod}} unresolved`, a templated legend whose resolved name provisioning cannot establish.
- [open question] the deployed positive distance through the exact panel expression is still unobserved; settlement
  needs an approved reversible control that holds consumption behind while the sampler keeps sampling.
