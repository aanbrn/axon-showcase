---
description: Run the Gradle, web UI, and build-logic pin dependency update reports
---

Report the available dependency updates. This command reports only — it never applies a dependency change unless asked.

**Trigger** — the owner invokes `/dependency-updates`, or the weekly `dependency-updates` workflow runs the same report
tasks and opens its issue.

**Procedure**

1. Run the three update tasks from the repository root in **one** Gradle invocation where the task graph supports it:
   `./gradlew dependencyUpdates :showcase-web-ui:npmOutdated buildLogicUpdates`.
2. Summarize only the output the current successful invocation produced. Do not fall back to a report file an earlier
   run left behind, and do not present a stale report as this run's result.
3. Summarize the available updates grouped by module, and flag those needing attention — a major jump, a new major, a
   coordinate no longer in the catalog.

**Decision points**

- IF a task fails, THEN report that failure as a task failure, separately from any available update — never as "no
  updates available".
- IF the web UI task reports a failure, THEN surface its own evidence: it writes
  `showcase-web-ui/build/npm-outdated.txt` (the filtered report), `npm-outdated-raw.txt`, `npm-outdated.err.txt`, and
  `npm-outdated.exit`, and logs an error when the exit is non-zero.
- IF a build-logic-report row names `unread`, THEN report it as a lookup that returned no version — a repository failure
  to note, not an update.
- IF the owner asks for a change, THEN treat it as a normal update unit under the repository lifecycle rather than
  applying it here.

**What each report covers** — `dependencyUpdates` covers the catalog-owned coordinates (an exact `version.ref` in
`gradle/libs.versions.toml`) and the wrapper status; BOM-inherited modules are not listed. `npmOutdated` covers the web
UI's npm packages, with the majors in `config/web-ui-updates/major-disabled.txt` filtered out — they do not appear in
the Gradle report. `buildLogicUpdates` covers the plugin pins only the included `build-logic` build resolves, which the
Gradle report cannot see; it filters with the same stability rule and the same suppression files. The suppression and
hold-back files themselves, and the reporting rules they drive, are `showcase/quality/dependency-management`'s — read
them there rather than from memory.

**Sections to read** — the actionable catalog section is `The following dependencies have later release versions:` (the
plugin's `revision = release` strategy after the `isNonStable` filter); `are using the latest release version` lists the
up-to-date coordinates, and any `Failed to …` section is a resolution failure to note, not an update. The report's
`Gradle CURRENT updates` section carries the wrapper status: state the pinned version from
`gradle/wrapper/gradle-wrapper.properties` and whether a newer stable Gradle exists, flagging it for attention with
`/gradle-update` as the follow-up.

**Failure handling** — a failed task is reported with its own output; nothing is summarized from a file it did not
write.

**Completion criteria** — the owner has the current run's updates, grouped and flagged, with every task's outcome
stated.
