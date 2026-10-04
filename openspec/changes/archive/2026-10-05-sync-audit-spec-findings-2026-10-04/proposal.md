# Proposal

## Why

The 2026-10-04 spec-corpus audit found two places where the shipped specs under-describe behavior the build encodes.
`config/dependency-updates/major-disabled.txt` suppresses `org.springframework` with the rationale recorded only in a
config comment ("Spring major blocked: Spring Boot 4 migration deferred — see ADR-0004"), while the
`dependency-management` spec — which the other suppressions (`org.jgroups`, `org.flywaydb`, springdoc,
spring-data-opensearch, `org.axonframework`) each have a requirement for — omits it. Separately, the
`dependency-security` spec describes the `dependencySecurityCheck` task as running `snyk test --all-sub-projects`, but
the task passes `--policy-path=.snyk` (verified in
`build-logic/src/main/kotlin/dependency-security-conventions.gradle.kts:11`), and that flag is the mechanism by which
the `.snyk`-suppressed findings take effect. Both are silent drift: `openspec validate` passes either way, and the spec
is the authoritative rationale record, so a reader consulting it is misled.

## What Changes

- Add a `dependency-management` requirement documenting the `org.springframework` group-prefix suppression, with the two
  standard scenarios (major jump suppressed / minor-and-patch stay visible), mirroring the existing per-coordinate
  suppression requirements.
- Correct every `dependency-security` occurrence of the bare invocation: the `Local dependency security scan task`
  requirement and its "Developer runs the dependency security scan" scenario, and the
  `Vulnerable transitive dependencies are constrained to patched versions` requirement's "Dependency scan reports no
  vulnerable paths" scenario. Each names the full invocation `snyk test --all-sub-projects --policy-path=.snyk`,
  matching `merge-governance`'s cross-reference and the task's actual command line. The flag is load-bearing for that
  scenario's "only suppressed findings are those pinned in `.snyk`" assertion, so every occurrence must carry it.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/dependency-management`: adds a requirement for the `org.springframework` major-version suppression.
- `showcase/quality/dependency-security`: corrects every bare `dependencySecurityCheck` invocation
  (`Vulnerable transitive dependencies are constrained to patched versions` and `Local dependency security scan task`)
  to include `--policy-path=.snyk`.

## Impact

Spec-only change: the main specs under `openspec/specs/showcase/quality/` gain/correct requirement text at archive time.
No code, build, dependency, test, or deployment change — the build already behaves as the corrected specs will describe.
