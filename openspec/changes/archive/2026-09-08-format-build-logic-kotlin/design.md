## Context

The root `spotless` block in `build.gradle.kts` formats markdown (Prettier) and Gradle Kotlin DSL via a `kotlinGradle`
block:

```kotlin
kotlinGradle {
    target("*.gradle.kts", "build-logic/*.gradle.kts", "build-logic/src/**/*.gradle.kts")
    ktfmt().kotlinlangStyle().configure { it.setMaxWidth(120) }
}
```

The build-logic **Kotlin** task classes (`build-logic/src/main/kotlin/*.kt` — `PackBuildImageTask`, `HelmUpdatesTask`,
`VerifyInfraImageVersionsTask`, `AbstractHelmRepositoriesTask`) are not covered: no target matches `*.kt`, so they rely
on manual style. The `kotlinGradle` step cannot be reused for them (it is bound to `.gradle.kts` semantics), so a
separate `kotlin` format is needed.

## Goals / Non-Goals

**Goals:**

- Gate `build-logic/src/**/*.kt` with ktfmt via the existing root Spotless setup, in `check`.
- Match the `kotlinGradle` style exactly (`kotlinlangStyle`, 120-column max) so build-logic Kotlin and `.gradle.kts`
  look consistent.

**Non-Goals:**

- No repo-wide Kotlin style for module sources (services stay Java; this change is build-logic only).
- No ktfmt config changes — reuse `kotlinlangStyle()` as `kotlinGradle` does.

## Decisions

### D1: Add a `kotlin` format to the root Spotless block

Add a `kotlin` format parallel to `kotlinGradle`, targeting the build-logic task classes with the identical ktfmt style:

```kotlin
kotlin {
    target("build-logic/src/**/*.kt")
    ktfmt().kotlinlangStyle().configure { it.setMaxWidth(120) }
}
```

Spotless's `kotlin` format uses the same ktfmt engine as `kotlinGradle` (the `kotlinlangStyle` and `setMaxWidth(120)`
options are identical), so build-logic Kotlin and the Gradle Kotlin DSL share one canonical style. The target is
restricted to `build-logic/src/**/*.kt` — module `.kt` sources (none exist today; services are Java) are not swept in.

### D2: One-time reflow

Run `./gradlew spotlessApply` once to reflow the four task classes to ktfmt output (import order, wrapping at 120), then
the `spotlessCheck` gate keeps them stable. Review the diff — formatting-only, no behavioral change (the task classes'
public signatures are unchanged).

## Risks / Trade-offs

- **Reflow surprises** → ktfmt may adjust imports/wrapping in the task classes on the first run. Mitigation: review the
  `spotlessApply` diff before committing; ktfmt does not rename or reorder logic.
- **ktfmt import style** → the `kotlinlangStyle` groups imports as the repo's `.gradle.kts` already does, so no new
  convention is introduced; the `kotlin` format uses the same engine and options as `kotlinGradle`.
