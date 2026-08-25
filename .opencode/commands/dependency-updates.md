---
description: Run the Gradle dependency update report
---

Run `./gradlew dependencyUpdates` from the repository root and report the results.

The report covers only catalog-owned coordinates (exact `version.ref` in `gradle/libs.versions.toml`); BOM-inherited
modules are not listed. Major-version updates for groups listed in `config/dependency-updates/major-disabled.properties`
are suppressed, while their minor/patch updates and all other catalog-owned majors remain reported.

Summarize the available updates grouped by module, flag any that require attention (e.g. major jumps, new majors, or
coordinates no longer in the catalog), and note any errors. Do not apply any dependency changes unless asked.

Ignore the "dependencies have later milestone versions" section entirely: milestone/non-stable candidates are already
rejected by the `isNonStable` filter, so those rows are never stable updates and are never actionable. Only stable
updates (a "dependencies with newer versions" section) matter.

Also surface the `Gradle CURRENT updates` section at the end of the report: state the current/pinned wrapper version
(from `gradle/wrapper/gradle-wrapper.properties`) and whether a newer Gradle release is available. If a newer stable
Gradle exists, flag it for attention and suggest running `/gradle-update`. When the report shows `UP-TO-DATE`, note
that Gradle is current.
