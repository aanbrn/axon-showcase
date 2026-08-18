# ADR-0002: Configuration-property defaults are owned by the Java @ConfigurationProperties contract

Date: 2026-08-18

Status: Accepted

## Context

Each service's configuration defaults are declared in up to three places: the Java `@ConfigurationProperties` class,
the service `application.yml` (via `${ENV:default}` placeholders), and the Helm chart `values.yaml` (which renders env
vars that override the yml). These can drift apart. The API gateway's query caches were a concrete case: Java declared
`1000` entries, while `application.yml` and the chart declared `100000`/`1000000`. Since the yml is loaded in every
environment, the Java defaults were effectively dead and the "documented" default never ran.

## Decision

The Java `@ConfigurationProperties` class is the authoritative contract for a property's default value. The
`application.yml` `${ENV:default}` placeholders and the Helm chart values mirror it, using the same default as the
Java field, so a deployment with no env overrides behaves exactly as the Java contract declares. A property that must
differ in a given deployment is overridden through env vars, not by changing the default in the yml or chart.

## Consequences

- A single source of truth for defaults; yml and chart drift is a defect rather than an accepted divergence.
- Changing a default requires updating the Java class, the yml, and the chart together (and the property component
  tests that pin them), which is deliberate.
- Env-var overrides remain the way to tune a deployment, unaffected by the defaults.
