# Merge governance for the showcase repository — Design

## Context

See proposal.md — Why. The repository has no CI: only `opencode.yml` (an agent listener, not a gate). All quality
gates already exist as Gradle tasks: `check` runs compile → spotless/checkstyle/spotbugs/errorprone → unit →
component → integration (Testcontainers) → JaCoCo coverage, with `-PskipITs` supported to drop the Docker-dependent
integration tier (`build-logic/src/main/kotlin/java-conventions.gradle.kts`). `e2eTest` (builds all four service
images) and `dependencySecurityCheck` (Snyk) are separate, opt-in, and never part of `check`.

GitHub currently enforces four `main-*` branch rulesets (force-push, linear history, PR+approval squash, deletion).
The owner (`aanbrn`) bypasses the PR-approval and force-push rulesets; linear-history and deletion have no bypass.
`required_status_checks` is not yet enforced by any ruleset.

The integration-tier failures that motivated this change — the nanosecond date truncation and the non-deterministic
IT index lifecycle — are already fixed and verified on `main` (`fix-date-nanos-precision` `44faab5`,
`opensearch-it-index-lifecycle` `eb6834d`), so the CI gate this change adds is expected to be green from the start.

## Goals / Non-Goals

**Goals:**
- A PR gate that runs the Docker-free quality tiers (`check -PskipITs`) plus `openspec validate --all`, fast enough to
  be a required check on every PR.
- Reuse the Gradle dependency and build caches across CI runs so gates do not re-download dependencies and recompile
  unchanged modules on every job.
- A full gate on `main` (`check` with integration tests and the coverage gate) so wiring bugs surface at the merge
  point, not before.
- A required-check ruleset with **no bypass actors** so the quality gate binds the owner as well as contributors —
  owner bypasses *process*, never *quality*.
- Reproduce locally-runnable gates exactly (`./gradlew check` semantics) rather than re-implementing checks in YAML.
- The existing governance baseline is versioned in the spec and verifiable against live GitHub state.

**Non-Goals:**
- Running `e2eTest` or `dependencySecurityCheck` on every PR — heavy and credentialed; deferred to follow-up tasks.
- Replacing or restructuring existing Gradle gates — CI only invokes existing tasks.
- New push rulesets, tag rulesets, or other branch-protection changes beyond the required-check ruleset.
- Recreating the existing rulesets — they already match the documented baseline; the change records behavior, not
  creation.
- Self-hosted runners, caching farms, or a remote build-cache service — the change reuses the standard GitHub
  Actions Gradle cache (`gradle/actions/setup-gradle`), not dedicated cache infrastructure.

## Decisions

### D1: Two-trigger single workflow (`ci.yml`) — fast on PR, full on `main`

One workflow, two events, same job shape with event-dependent command:

```
on:
  pull_request:                     # PR gate
  push:
    branches: [main]                # merge gate

job build (ubuntu-latest, Temurin 21, Docker preinstalled):
  PR:    ./gradlew check -PskipITs -Pcoverage.gate.enabled=false && openspec validate --all
  main:  ./gradlew check                                       && openspec validate --all
```

The `main` path runs the default `check` without serializing projects. An early hypothesis blamed the integration-tier
CI failures on Testcontainers resource contention under `org.gradle.parallel=true`; the spike disproved it — a
serialized run (`--no-parallel`) failed identically to the parallel one. The failures were the nanosecond date
truncation (`fix-date-nanos-precision`, `44faab5`) and the non-deterministic IT index lifecycle
(`opensearch-it-index-lifecycle`, `eb6834d`), both already fixed on `main`. With those fixes landed, the default
parallel `check` is expected to pass; `--no-parallel` is only reintroduced if a full four-suite `check` shows real
contention on the runner.

The PR path disables the coverage gate (`-Pcoverage.gate.enabled=false`) because the 0.80 baseline is calibrated
on integration-test coverage (AGENTS.md), and `-PskipITs` drops the integration tier — so the fast path cannot meet
the gate: verified empirically, `showcase-api-gateway` measures 0.74 and the query service fails without ITs. The
coverage gate therefore runs only on the `main` full gate, where integration tests are present. The flag itself is
fixed as part of this change (task 2.4): the current `coverageGateEnabled` compares the property against Boolean
`false`, but `-P` delivers a String `"false"`, so the gate never disables; task 2.4 rewrites it to treat the value
as a string (`"false"` from `-P`, Boolean `false` from per-module `extra`, `null` means enabled).

Alternatives rejected:
- **Full `check` on every PR** — every PR would pay Testcontainers + coverage time; the fast/slow split is the
  documented intent in `docs/ideas.md`.
- **Two separate workflows** — more surface for no benefit; one workflow keeps the check name (`build`) identical
  across both paths so the ruleset requires a single check.
- **`check -PskipITs` on `main` too** — would let integration-tier regressions slip past the merge gate.

### D2: Required check lives in a NEW ruleset (`main-required-checks`) with no bypass

The required check is *not* added to the existing `main-require-pr-on-merge` ruleset (which the owner bypasses).
A separate ruleset without bypass actors expresses the design split: the owner bypasses PR-approval and force-push
process, but the quality gate is universal.

Alternative rejected: folding `required_status_checks` into `main-require-pr-on-merge` — the owner's always-bypass
would silently exempt them from the CI gate.

### D3: The ruleset is created only after the check has run green

GitHub treats a required status check that has never run as "not passing" — every merge would be blocked. Therefore
the ruleset is the final step, added only after `ci.yml` has produced a green `build` check on a real commit.

### D4: `openspec validate --all` as an always-on spec gate

Nearly free (no Docker, no network), catches malformed changes/specs on both paths. Matches the spec-driven
development model: specs are behavior's source of truth, so they must be valid to merge.

### D5: Check name is the job name (`build`)

GitHub identifies a required status check by the job's check-run name. A single job named `build` keeps the ruleset
stable and lets both event paths share one required check.

### D6: The governance baseline is documented as behavior, not recreated

The four existing `main-*` rulesets are captured in the spec as behavioral requirements (force-push blocked, linear
history, PR+squash approval, no deletion) rather than being recreated by this change. Specs describe observable
behavior regardless of provenance — the `load-tests` spec documents existing behavior the same way. The alternative,
redesigning the change as if the rulesets never existed and recreating them on apply, was rejected: it would claim
creation of state that already exists and describe a fiction to satisfy symmetry.

### D7: Drift-check is read-only reconciliation, never delete/recreate

Task 3.1 verifies the live rulesets match the spec baseline via the read-only rulesets API; if drift is found,
task 3.2 reconciles in place (update the existing ruleset) rather than deleting and recreating. Delete/recreate was
rejected: it opens an unprotected window between delete and create, leaves `main` open if the create fails, churns
ruleset IDs, and gains nothing since the rulesets already match the spec.

### D8: Reuse the Gradle cache across runs via `gradle/actions/setup-gradle`

GitHub Actions runners start fresh, so without caching every run re-downloads the full dependency set, the Gradle
wrapper distribution (~130 MB for `-all`), and — because `org.gradle.caching=true` is already set — rebuilds every
module. The job uses the official `gradle/actions/setup-gradle` step, which restores and saves the Gradle User Home
(`~/.gradle/caches/modules-2`, the wrapper, and the local build cache `~/.gradle/caches/build-cache-1`) keyed on the
build scripts and lockfiles.

Only the Gradle User Home is cached — **never the workspace `build/` directories**. `jacocoTestCoverageVerification`
reads `.exec` files from `build/jacoco`, so restoring a stale workspace `build/` would corrupt the coverage gate.
The Gradle build cache is safe because task outputs are keyed by their inputs; raw `build/` caching is not.

Alternatives rejected:
- `actions/cache` with hand-maintained paths — the `setup-gradle` action manages keys and save/restore automatically;
  a manual `actions/cache` step duplicates that with more surface for no benefit.
- Remote build cache (Gradle Build Cache Node / Gradle Enterprise) — out of scope per Non-Goals; the ephemeral
  runner-local cache already covers the dominant re-download/recompile cost.

## Risks / Trade-offs

- **Check chicken-and-egg** → the ruleset is created last (D3); documented ordering in tasks.md, not implied.
- **Owner can bypass `check -PskipITs` scope on PR** → by design, the integration tier and the coverage gate only run
  on `main` (the coverage gate cannot be met without integration-test coverage — 0.74 vs 0.80 for the gateway on the
  fast path). A wiring or coverage regression would surface as a failing `main` gate. Mitigation: the `main` gate is
  required (same `build` check, no bypass).
- **Slow `main` gate on every merge** → `check` with integration tests is the heaviest tier; acceptable because merges
  are squash-only (one run per merged PR) and gating correctness over speed at the trunk. The default parallel run is
  expected to pass now that the nanosecond date truncation and IT index-lifecycle fixes are on `main` (see D1);
  reintroduce `--no-parallel` only if a real contention failure is observed.
- **One job means one failure mode for both paths** → acceptable at this scale; split workflows only if the
  main-gate runtime becomes a practical problem.
- **Cache misses on lockfile changes** → the Gradle cache key includes the build scripts and lockfiles, so any
  dependency bump invalidates the cache for that run; the next run rebuilds it. This is the expected behavior of
  D8, not a regression.
- **No e2e/Snyk in the PR path** → deferred by design (Non-Goals); a broken e2e or a vulnerable dependency could
  reach `main`. Mitigated by follow-up heavy-workflow tasks, and by the existing dependency-security platform
  constraints (Snyk scans clean today).

## Migration Plan

1. Add `.github/workflows/ci.yml`.
2. Open a PR carrying the workflow; observe the `build` check run green (PR path, `-PskipITs`).
3. Merge (squash); observe the `build` check run green on `main` (full `check`).
4. Drift-check the four baseline rulesets against the spec (D7); reconcile in place if drifted.
5. Create `main-required-checks` ruleset requiring `build`, no bypass.
6. Follow-up (separate change): heavy workflow for e2e + Snyk.

Rollback: deleting `ci.yml` removes the check; the required-check ruleset then blocks merges — so rollback order is
inverse (remove the ruleset first, then the workflow).

## Open Questions

None — the fast-PR/full-main split, the no-bypass ruleset placement, and the creation ordering are all settled and
captured above. The e2e/Snyk cadence is explicitly deferred to follow-up work, not a blocker.