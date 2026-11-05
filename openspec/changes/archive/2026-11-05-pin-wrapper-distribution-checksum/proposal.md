# Pin the wrapper's distribution checksum

## Why

The Gradle wrapper downloads the distribution named by `distributionUrl` and, without a checksum, accepts whatever the
server returns: `gradle/wrapper/gradle-wrapper.properties` carries no `distributionSha256Sum`, so a corrupted archive
installs without complaint. The bump is two `wrapper` runs verified only by diffing four files and compiling, and a bump
that fails part-way leaves the wrapper half-rewritten with nothing restores it — the command's own failure handling says
the files are simply "not hand-edited". Gradle's own task already refuses to drop a pinned checksum, but nothing pins
one today, and the bump's failure path has no rollback.

## What Changes

- `gradle/wrapper/gradle-wrapper.properties`: pin `distributionSha256Sum` to the published SHA-256 of the pinned `-all`
  distribution, so every download after the pin is verified before install. Gradle already refuses a version bump that
  would leave the pin without a value for the new release, so the pin cannot be lost by omission — what a bump needs is
  the new release's value, which the tool fetches.
- `scripts/verify-wrapper-checksum.py` and a `verifyWrapperChecksum` task in `check`: the gate that the pin is present,
  64 hex characters, and names an `-all.zip` archive; `scripts/test-verify-wrapper-checksum.py` carries its controls.
- `scripts/update-gradle-wrapper.py`: one tool that performs the bump — resolve the target checksum from the
  distribution service, snapshot the four wrapper files, run the two `wrapper` passes with
  `--gradle-distribution-sha256-sum`, verify, and restore the captured bytes and executable modes when a step fails.
  `scripts/test-update-gradle-wrapper.py` drives it against fixtures.
- `/gradle-update` becomes the tool's trigger, and the `gradle-build` pack's wrapper bullet states the tool, the
  checksum and the rollback in place of the two-run prose.

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/commit-hygiene` — adds "The wrapper's distribution checksum is verified by the build", a mechanical
  guard beside the executable-bit requirement the same capability already owns.

## Impact

- **Build**: `gradle/wrapper/gradle-wrapper.properties`; the root `build.gradle.kts` gains `verifyWrapperChecksum`,
  `testVerifyWrapperChecksum`, and `testUpdateGradleWrapper` in `check`.
- **Tooling**: `scripts/verify-wrapper-checksum.py`, `scripts/test-verify-wrapper-checksum.py`,
  `scripts/update-gradle-wrapper.py`, `scripts/test-update-gradle-wrapper.py`; `.opencode/commands/gradle-update.md` and
  `.opencode/skills/gradle-build/SKILL.md` point at the tool.
- **Dependencies, deployment, behavior**: none.
- **Verification**: the two test suites from `check`; a checksum mismatch refused in a throwaway `GRADLE_USER_HOME`; one
  real bump rehearsed and rolled back against the live distribution service.
