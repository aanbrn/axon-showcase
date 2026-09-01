## Context

The gradle-helm-plugin (io.github.build-extensions-oss.helm 3.1.2) models kube context at three levels: the global
`helm.kubeContext` project property, per-release-target via `releaseTargets { <name> { kubeContext = ... } }`, and
per-release. A release target is selected per build by `helm.release.target` (default `local`). The repo currently
declares a single `local` target with `selectTags = "*"` and relies on an undocumented `helm.kubeContext=colima` in
`~/.gradle/gradle.properties` to pin local deploys to the macOS colima cluster. See proposal.md for motivation.

Key constraint discovered during exploration: the local cluster's context name is per-developer (colima on macOS,
kind/minikube on Linux), so it cannot be hard-coded in the versioned build. Any remote target (staging) uses a shared,
fixed context name. The plugin's per-target kube context is the native mechanism to express this.

## Goals / Non-Goals

**Goals:**
- Each release target declares its own kube context in the build.
- The `local` target resolves per-machine: it uses the developer's local cluster regardless of OS or context name.
- The convention is documented so a fresh clone or a non-macOS contributor deploys deterministically.

**Non-Goals:**
- Introducing a second release target (staging). This change establishes the pattern and the `local` resolution; a
  concrete remote target is future work.
- Changing the chart or any release's namespace/values.
- Automatically detecting the local cluster; the fallback to the current kube context already handles the
  single-cluster case without extra machinery.

## Decisions

**D1: Use the plugin's per-target `kubeContext`, resolved from a per-machine property for `local`.**
`releaseTargets.create("local") { kubeContext = providers.gradleProperty("helm.local.kubeContext").orElse(...) }`.
Rationale: `kubeContext` on the target is a settable `Property<String>`, so it can be wired to a Gradle property
provider. `helm.local.kubeContext` is per-machine (user-level `gradle.properties` or `-P`), keeping the repo
cluster-agnostic while letting each developer pin their local cluster. Alternative (rejected): hard-coding
`kubeContext = "colima"` in the build breaks Linux contributors and bakes a macOS assumption into the repo.

**D2: Fall back to the current kube context when `helm.local.kubeContext` is unset.**
The plugin's default (no kube context) is to use the current kube context. For a single-cluster setup (one minikube or
colima context), the current context is the local cluster, so no per-machine property is needed at all. Rationale:
avoids forcing every contributor to set a property; the property is only required when a developer has multiple
contexts and needs to pin the local one. Alternative (rejected): failing the build when unset adds friction for the
common single-cluster case.

**D3: Remote targets (future) declare a shared, fixed kube context in the build.**
`releaseTargets.create("staging") { kubeContext = "gke_my-proj_staging" }` (illustrative). Rationale: a remote target is
the same cluster for every contributor, so its context name belongs in the versioned build. This is the documented
template; no staging target is added in this change.

**D4: Remove `helm.kubeContext=colima` from `~/.gradle/gradle.properties`, replace with
`helm.local.kubeContext=colima`.**
Rationale: the old global property was undocumented and, once per-target resolution exists, redundant for the `local`
target. The new name is self-describing and scoped to the target that needs it.

## Risks / Trade-offs

- [Per-machine property not set on a multi-context machine] → Falls back to the current context; if that is a remote
  cluster, a local deploy goes to the wrong place. Mitigation: document that multi-context developers must set
  `helm.local.kubeContext`; the `local` fallback is only safe for single-cluster setups.
- [Property name churn for contributors who already set `helm.kubeContext`] → This is a single-developer repo today;
  the machine-local change is documented. No in-repo references exist (verified by grep).
- [Plugin's default `local` target suppression] → The docs note that defining any release target replaces the implicit
  `default` target. The repo already defines `local`, so no behavior change here.
- [Gradle property provider wiring nuances] → `providers.gradleProperty(...).orElse(...)` is the documented Gradle
  idiom; verified against the plugin's settable `kubeContext` Property.

## Migration Plan

1. Wire the `local` target's `kubeContext` in `build.gradle.kts` (D1, D2).
2. Update the developer's `~/.gradle/gradle.properties`: `helm.kubeContext=colima` → `helm.local.kubeContext=colima`.
3. Document the convention in AGENTS.md / README.md.
4. Verify `./gradlew helmInstallToLocal` still targets the local cluster and `helm template`/lint pass.

Rollback: revert the `build.gradle.kts` change and restore `helm.kubeContext=colima`; the previous behavior is preserved
by git.

## Open Questions

None. The plugin capability, per-machine resolution, and the fallback behavior are all confirmed; a concrete staging
target is deliberately deferred as non-goal.
