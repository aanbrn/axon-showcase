---
description: Update the Gradle wrapper to the latest stable version
---

Update the Gradle wrapper when a newer stable version is available.

**Trigger** — the owner invokes `/gradle-update`, or `/dependency-updates` flagged a newer stable Gradle.

**Procedure**

1. Run `./gradlew dependencyUpdates` and read the `Gradle CURRENT updates` section from the report **this run
   produced**. A candidate version comes only from a successfully regenerated report.
2. Compare the pinned wrapper version in `gradle/wrapper/gradle-wrapper.properties` with the newest stable Gradle.
3. IF a newer stable version exists, THEN run `python3 scripts/update-gradle-wrapper.py --version=<latest>` and read its
   report. The tool resolves that version's published checksum, captures the four wrapper files' bytes and executable
   modes, runs both `wrapper` passes with `--gradle-distribution-sha256-sum`, and verifies that the resulting
   `distributionUrl` and `distributionSha256Sum` agree. It never chooses a version itself: the version it is given is
   the one this command selected.
4. The bump is complete when the tool exits zero. Its `distributionUrl` names the target version's `-all.zip` and its
   `distributionSha256Sum` is the checksum the tool fetched — **not** when all four wrapper files differ: the generated
   `gradle-wrapper.jar` is byte-identical when the running and target versions match, so a differing-files criterion
   reports a complete bump as incomplete.

**Decision points**

- IF the report shows the wrapper current (`Gradle: [<version>: UP-TO-DATE]`), THEN report that no update is needed and
  stop — do not run the wrapper task or the tool.
- IF the report failed or is indeterminate, THEN treat the candidate version as indeterminate: report that, and do not
  read a report file an earlier run left behind.
- IF the tool exits non-zero, THEN read its report for which of three outcomes it states, and pass that on as it stands:
  the files were **restored** to the bytes and modes the run found; the restore is **incomplete**, in which case the
  snapshot directory it names holds those bytes; or the run stopped **before touching anything** — a non-stable version,
  a checksum it could not fetch, an empty or malformed checksum body, or a missing wrapper file — and says so. Do not
  re-run the tool to "push through" a failure.
- IF the tool was killed mid-run, THEN no final report may exist; recover the four files from the snapshot path it
  announced before the passes, which it prints only once the capture has completed. Do that before anything else.
- Only stable releases: never a `release-candidate` or `nightly` label, and the tool refuses a version that is not one.
- IF the owner asks for the update to ship, THEN it is a normal standalone unit under the repository lifecycle — not a
  push from this command.

**Failure handling** — on a failed pass or a mismatched result the tool attempts restoration and reports whether it
completed. A checksum-fetch failure leaves the wrapper files untouched, so there is nothing to restore. If restoration
itself fails while the tool is still running, it reports the incomplete restore and retains the snapshot holding the
bytes it found. If the process is killed, no final report may exist at all: recover from the snapshot path it announced
before the passes, which it prints only once the capture has completed. The wrapper files are never hand-edited to
repair a bump.

**Verification** — the tool's exit status, the properties' `distributionUrl`/`distributionSha256Sum` agreement, and
`./gradlew --version` under a cleared wrapper home (`GRADLE_USER_HOME`) so the download is verified against the pin
rather than served from a cache. `./gradlew compileJava` then confirms the build still runs on the new version.

**Completion criteria** — the pinned wrapper version before and after, the new Gradle version, the checksum the tool
fetched, and the tool's report are stated; or, when the wrapper was current or the report indeterminate, that outcome is
reported with no change made.

**Repository lifecycle**

- A new unit starts on its own branch, created from a freshly fetched `origin/main`; a unit continuing an existing
  branch or pull request works on that branch.
- The unit's working tree — the primary checkout when no other unit holds it, otherwise the sibling worktree
  `../axon-showcase-<unit>` — is established and recorded before the first write, with an ownership it cannot establish
  treated as occupied, per the **Workspace** rule of the lifecycle the bindings declare
  (`docs/contracts/process-bindings.md`); a declaration that names none stops the unit rather than being inferred.
- The unit begins only on the owner's authorization, and its proposal passes the owner's manual review before any
  implementation edit.
- Unrelated dirty-worktree changes are preserved.
- The work stays uncommitted until the implementation's manual approval pass.
- Nothing is committed, pushed, turned into a pull request, archived, or merged without that explicit approval.
- The full per-unit review gate runs over the result. The lifecycle and the review gate the bindings declare
  (`docs/contracts/process-bindings.md`) own the branch, uncommitted-work, approval, and review steps, including the
  reviewer-session preflight; this command points at them rather than restating them.
