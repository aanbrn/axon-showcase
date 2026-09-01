## Why

The `showcase/deployment/helm-chart` spec captures the chart's high-level behavior but omits significant
deployer-tunable behavior (the API-gateway resilience4j and query-cache wiring, the projection-service projector
tuning, the command-service DB pool/scheduler and main showcase cache, cross-cutting metric tags and label/annotation
merges, and HPA/VPA/PDB/ServiceMonitor tunables). An investigation that relies on the spec alone must reverse-engineer
the chart templates to learn this. This change brings the spec to a complete behavioral contract at the category
level, so the chart's behavior is discoverable from the spec without reading template source.

## What Changes

- Expand the `showcase/deployment/helm-chart` spec with requirements (at category level, not per-env-var) for:
  - API-gateway runtime tuning: read-path routing to the query service, the two Caffeine query caches, and the
    resilience4j environment (time limiter, circuit breaker, retry) with defaults and per-service (command/query)
    overrides.
  - Projection-service projector tuning: concurrency, batching, retry, and restart-delay environment.
  - Command-service DB pool and scheduler environment, and the main showcase cache environment.
  - Cross-cutting metrics tags (`MANAGEMENT_METRICS_TAGS_APPLICATION`) and the label/annotation merge model
    (service labels/podLabels/annotations/podAnnotations merged with `commonLabels`/`commonAnnotations`).
  - Autoscaling/HA details already in the chart but under-captured: HPA memory and custom metrics and scale behavior,
    VPA min/max allowed and update policy, PDB minAvailable logic, ServiceMonitor interval/scrapeTimeout tunables,
    Ingress extraPaths/extraHosts, HTTPRoute filters/extraRules.
- Correct the NetworkPolicy management-port wording to describe all ingress source mechanisms (client labels,
  ingress pod match labels, namespace+pod match labels, extra ingress rules) rather than a "configured management pods"
  catch-all.
- No chart or code changes; this is spec documentation of existing behavior.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/deployment/helm-chart`: add requirements capturing the chart's full deployer-tunable surface at category
  level, and correct the NetworkPolicy management-port requirement wording.

## Impact

- `openspec/specs/showcase/deployment/helm-chart/spec.md` — expanded with new requirements and one corrected
  requirement.
- No application code, chart templates, values, or build changes.