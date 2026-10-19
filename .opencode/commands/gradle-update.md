---
description: Update the Gradle wrapper to the latest stable version
---

Update the Gradle wrapper when a newer stable version is available.

**Trigger** — the owner invokes `/gradle-update`, or `/dependency-updates` flagged a newer stable Gradle.

**Procedure**

1. Run `./gradlew dependencyUpdates` and read the `Gradle CURRENT updates` section from the report **this run
   produced**. A candidate version comes only from a successfully regenerated report.
2. Compare the pinned wrapper version in `gradle/wrapper/gradle-wrapper.properties` with the newest stable Gradle.
3. IF a newer stable version exists, THEN run `./gradlew wrapper --gradle-version=<latest> --distribution-type all`
   **twice** — the first run rewrites `distributionUrl`, and the second (executing under the new version) regenerates
   `gradle-wrapper.jar` and `gradlew`/`gradlew.bat`. A single run leaves the jar and the scripts at the old version, and
   omitting the flag flips the repository's `-all` distribution to `-bin`.
4. Verify the result across all four wrapper files: `gradle/wrapper/gradle-wrapper.properties` (its `distributionUrl`
   still ending `-all.zip` and naming the new version), `gradle/wrapper/gradle-wrapper.jar`, `gradlew`, and
   `gradlew.bat`. `./gradlew --version` cannot confirm it — it reports the version from `distributionUrl` alone.

**Decision points**

- IF the report shows the wrapper current (`Gradle: [<version>: UP-TO-DATE]`), THEN report that no update is needed and
  stop — do not run the wrapper task.
- IF the report failed or is indeterminate, THEN treat the candidate version as indeterminate: report that, and do not
  read a report file an earlier run left behind.
- IF only `gradle-wrapper.properties` changed after the two runs, THEN the bump is incomplete — report it rather than
  success.
- Only stable releases: never a `release-candidate` or `nightly` label.
- IF the owner asks for the update to ship, THEN it is a normal standalone unit under the repository lifecycle — not a
  push from this command.

**Failure handling** — a wrapper run that fails, or a verification that disagrees with the four files, is reported with
what it found; the old wrapper files are not hand-edited.

**Verification** — the four-file diff above, plus the build still compiling (`./gradlew compileJava`).

**Completion criteria** — the pinned wrapper version before and after, the new Gradle version, and the four files' state
are reported; or, when the wrapper was current or the report indeterminate, that outcome is reported with no change
made.

**Repository lifecycle**

- The unit starts on its own branch, created from a freshly fetched `origin/main`.
- Unrelated dirty-worktree changes are preserved.
- The work stays uncommitted until the manual approval pass.
- Nothing is committed, pushed, turned into a pull request, archived, or merged without that explicit approval.
- The full per-unit review gate runs over the result. `AGENTS.md`'s "Workflow: Change lifecycle" and "Workflow: The
  review gate" own the branch, uncommitted-work, approval, and review steps, including the reviewer-session preflight;
  this command points at them rather than restating them.
