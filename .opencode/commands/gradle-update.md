---
description: Update the Gradle wrapper to the latest stable version
---

Update the Gradle wrapper when a newer stable version is available.

Run `./gradlew dependencyUpdates` and read the `Gradle CURRENT updates` section from the report (or the report file at
`build/dependencyUpdates/report.txt`). Compare the pinned wrapper version in `gradle/wrapper/gradle-wrapper.properties`
with the newest stable Gradle.

- If the wrapper is already current (report shows `Gradle: [<version>: UP-TO-DATE]`), report that no update is needed
  and stop — do not run the wrapper task.
- If a newer stable Gradle is available, run `./gradlew wrapper --gradle-version=<latest>` to update the wrapper, then
  verify the build still passes (e.g. `./gradlew compileJava`), and report the version change. Do not use
  `release-candidate`/`nightly` labels — only stable versions.

Report the pinned wrapper version before and after, and the new Gradle version. Do not push unless asked.