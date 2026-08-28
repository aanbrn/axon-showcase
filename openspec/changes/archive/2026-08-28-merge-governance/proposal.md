# Merge governance for the showcase repository

## Why

The repository has no CI: the only workflow (`opencode.yml`) is an agent listener, not a build gate. Every quality
gate already exists inside Gradle tasks (`check`, `integrationTest` via Testcontainers, `e2eTest`, Snyk, Helm lint),
so nothing runs them automatically — a broken commit can land on `main` through the PR workflow or a direct push. CI
makes the existing gates actually gate merges, and gives the rulesets a real check to require. The change also records
the existing branch-protection rulesets as the governance baseline in the spec, so the merge contract is versioned
and verifiable against live GitHub state — closing the gap where governance lived only in the settings UI.

## What Changes

- Add a `ci.yml` workflow that runs on every pull request and on every push to `main`:
  - **Pull request**: fast gate — `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` (Docker-free: unit +
    component + all static gates, coverage gate disabled because it cannot be met without integration tests) plus
    `openspec validate --all` — the check that the PR ruleset will require
  - **Push to `main`**: full gate — `./gradlew check` (with `integrationTest` via Testcontainers and the JaCoCo
    coverage gate) plus `openspec validate --all`
- Add a separate `main-required-checks` GitHub ruleset (branch ruleset on `refs/heads/main`) that requires the CI
  check with **no bypass actors**, so the quality gate binds everyone including the repository owner.
- Run Java 21 (Temurin) on `ubuntu-latest` (Docker available for integration tests).
- Reuse the Gradle cache across runs via the official `gradle/actions/setup-gradle` step — the Gradle User Home
  (dependencies, wrapper, and the local build cache) is restored/saved keyed on the build scripts, so runs do not
  re-download the toolchain and recompile unchanged modules. Only the Gradle User Home is cached, never the workspace
  `build/` directories (stale `jacoco` exec data would corrupt the coverage gate).
- Keep heavy or credentialed gates off the PR path: `e2eTest` (builds all four service images) and
  `dependencySecurityCheck` (Snyk) run post-merge / on a schedule — captured as follow-up tasks, not blocking PRs.

## Capabilities

### New Capabilities

- `showcase/quality/merge-governance`: how changes land on `main` — the existing branch-protection rulesets
  (force-push, linear history, PR+squash approval, deletion) documented as behavioral requirements, plus the new
  continuous-integration behavior: which gates run on pull requests vs. pushes to `main`, and that merge protection
  requires the CI check with no bypass actors.

### Modified Capabilities

None. The CI workflows automate existing gates; no application or build behavior changes.

## Impact

- **New files**: `.github/workflows/ci.yml` (plus optionally `ci-heavy.yml` for e2e/Snyk in follow-up tasks).
- **GitHub config**: new `main-required-checks` ruleset (no bypass). Existing four `main-*` rulesets unchanged
  (the required check is *not* added to the bypassable `main-require-pr-on-merge` ruleset) and documented as part of
  the `merge-governance` capability.
- **Ordering constraint**: the ruleset is created only after the CI check has run green on a real commit — GitHub
  treats a required check that has never run as not passing, so creating the ruleset first would block all merges.
- **Build/test**: no changes to Gradle tasks; CI only invokes existing tasks. `-PskipITs` is already supported by the
  build (`java-conventions.gradle.kts`).
- **Secrets**: `ci.yml` needs no secrets; the heavy e2e/Snyk follow-up needs a Snyk token only.