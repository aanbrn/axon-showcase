---
description: Run the Gradle dependency update report
---

Run `./gradlew dependencyUpdates` from the repository root and report the results.

The report covers only catalog-owned coordinates (exact `version.ref` in `gradle/libs.versions.toml`); BOM-inherited
modules are not listed. Major-version updates for groups listed in `config/dependency-updates/major-disabled.properties`
are suppressed, while their minor/patch updates and all other catalog-owned majors remain reported.

Summarize the available updates grouped by module, flag any that require attention (e.g. major jumps, new majors, or
coordinates no longer in the catalog), and note any errors. Do not apply any dependency changes unless asked.
