---
description: Run the Gradle and web UI dependency update reports
---

Run `./gradlew dependencyUpdates` (Gradle) and `./gradlew :showcase-web-ui:npmOutdated` (web UI) from the repository
root and report the results.

The web UI's npm dependencies are reported separately by `:showcase-web-ui:npmOutdated`, which lists the outdated npm
packages and writes them to `showcase-web-ui/build/npm-outdated.txt`; they do not appear in the Gradle report.

The report covers only catalog-owned coordinates (exact `version.ref` in `gradle/libs.versions.toml`); BOM-inherited
modules are not listed. Major-version updates for groups listed in `config/dependency-updates/major-disabled.properties`
are suppressed, while their minor/patch updates and all other catalog-owned majors remain reported.

Summarize the available updates grouped by module, flag any that require attention (e.g. major jumps, new majors, or
coordinates no longer in the catalog), and note any errors. Do not apply any dependency changes unless asked.

The report's actionable catalog section is `The following dependencies have later release versions:` (the plugin's
`revision = release` strategy after the `isNonStable` filter); the `are using the latest release version` section lists
the up-to-date coordinates, and any `Failed to …` section is a resolution failure to note, not an update.

Also surface the `Gradle CURRENT updates` section at the end of the report: state the current/pinned wrapper version
(from `gradle/wrapper/gradle-wrapper.properties`) and whether a newer Gradle release is available. If a newer stable
Gradle exists, flag it for attention and suggest running `/gradle-update`. When the report shows `UP-TO-DATE`, note that
Gradle is current.
