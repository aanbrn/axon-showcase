## 1. Spec expansion

- [x] 1.1 Add the API gateway runtime tuning requirement (query-service routing, query caches, resilience4j with
      per-service overrides) to the `showcase/deployment/helm-chart` spec delta and verify it matches the chart
      template behavior
- [x] 1.2 Add the projection-service projector tuning requirement and verify it matches the chart template behavior
- [x] 1.3 Add the command-service database pool and scheduler environment requirement and verify it matches the chart
      template behavior
- [x] 1.4 Add the command-service showcase cache environment requirement and verify it matches the chart template
      behavior
- [x] 1.5 Add the metrics tags and label/annotation merge model requirements and verify they match the chart templates

## 2. Autoscaling, HA, and routing details

- [x] 2.1 Add the HPA details (memory target, custom metrics, scale behavior), VPA details (bounds, update mode), and
      PDB details (minAvailable) requirements and verify they match the chart templates
- [x] 2.2 Add the ServiceMonitor tunables, Ingress extra rules, and HTTPRoute extras requirements and verify they match
      the chart templates
- [x] 2.3 Correct the NetworkPolicy management-port requirement to describe all ingress source mechanisms
      (`addExternalClientAccess`, `ingressPodMatchLabels`, `ingressManagementNSMatchLabels`,
      `ingressManagementNSPodMatchLabels`, extra ingress rules) and verify against the chart template

## 3. Validation

- [x] 3.1 Run `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` and `openspec validate --all`, confirming the
      build and spec validation pass