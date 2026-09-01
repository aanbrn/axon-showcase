## Context

The `showcase/deployment/helm-chart` spec is the behavioral contract for the chart (see proposal.md for the gap list).
It currently captures the chart's deployment skeleton precisely (services, jobs, ports, probes, RBAC, wiring
categories, OTLP validation) but omits the tunable environment surface of the gateway, projection, and command
services, plus several autoscaling/HA details. The existing spec already names some exact env vars (saga caches) while
omitting others (resilience, projector, DB pool), so its detail level is inconsistent.

The goal is a spec that is complete enough to read without opening chart templates, at a consistent granularity.

## Goals / Non-Goals

**Goals:**
- Capture every deployer-tunable behavior category the chart exposes, at a level where an investigation needs no
  template reading to know *what* the chart wires.
- Fix the NetworkPolicy management-port wording to reflect the chart's actual ingress-source mechanisms.
- Keep the spec readable and maintainable, not bloated by per-env-var enumeration.

**Non-Goals:**
- Enumerating every env var name. Category-level wording names the settings generically (e.g. "the resilience4j time
  limiter, circuit breaker, and retry environment, with defaults and per-service command/query overrides") rather than
  a 60-line list.
- Changing the chart, values, or any code. This is documentation of existing behavior.
- Capturing the Bitnami `common` library's own surface (label helpers, affinity helpers, name truncation) beyond what
  the chart's templates directly use.

## Decisions

**D1: Use category-level granularity, not per-env-var enumeration.**
Each new requirement names the tuning *category* and the settings it exposes (e.g. "DB connection pool environment:
maximum size, minimum idle, connection timeout, leak-detection threshold"), without listing exact env var names.
Rationale: satisfies the goal (no reverse-engineering needed to know what the chart wires) while keeping the spec
readable. The existing saga-cache requirement already names exact env vars, so new categories keep that level where a
category is small (a few settings) but generalize where it is large (resilience, DB pool). Alternative (rejected):
full enumeration would make the spec unreadable and high-churn as values evolve.

**D2: Group the new requirements by concern, mirroring the existing requirement names.**
Add "API gateway runtime tuning", "Projection-service projector tuning", "Command-service database pool and scheduler
environment", "Command-service showcase cache environment", "Metrics tags", "Label and annotation merge model",
and extend the existing autoscaling/HA/ingress/route/ServiceMonitor requirements with the missing detail. Rationale:
fits the spec's existing structure (one requirement per concern) and keeps scenarios aligned with the chart.

**D3: Correct the NetworkPolicy management-port requirement rather than add a new one.**
The existing "Management port is restricted" scenario wording ("any configured management pods") under-describes the
chart's four ingress mechanisms. Replace it with wording covering client labels, ingress pod match labels, namespace
and pod match labels, and extra ingress rules. Rationale: it is a correction of an existing requirement, not a new
behavior.

**D4: Keep each new requirement's scenarios at the observable-behavior level.**
Scenarios describe what a rendered template SHALL do (e.g. "the gateway container receives the resilience environment
with command/query overrides") rather than listing env var names. Rationale: consistent with the spec instruction to
avoid implementation detail, and testable via `helm template` rendering.

## Risks / Trade-offs

- [Category-level wording may not name a setting an investigator needs] → Mitigation: each requirement names the
  settings within the category (e.g. "retry maximum attempts, wait duration, exponential backoff") even if not the
  exact env var; this is sufficient to know the surface without template reading.
- [Spec grows notably longer] → Mitigation: category grouping keeps it to a handful of new requirements; the
  readability goal explicitly trades per-var detail for maintainability.
- [Wording drifts from the chart as it evolves] → Mitigation: the chart's lint configs (full/minimal) already render
  the conditional branches; a spec-coverage change is a one-time sync, and future chart changes should update the spec
  as part of the change.

## Migration Plan

1. Update the spec with the new requirements and the corrected NetworkPolicy wording.
2. Validate with `openspec validate --all` and the project's Docker-free check (spec-only, no code).
3. Archive via the standard workflow.

Rollback: revert the spec change; no code or chart impact.

## Open Questions

None. The category-level granularity (D1) is the resolution of the exploration's core question; the per-requirement
grouping (D2) follows the chart's existing structure.