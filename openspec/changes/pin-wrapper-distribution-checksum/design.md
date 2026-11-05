# Design — pin the wrapper's distribution checksum

## Context

`gradle/wrapper/gradle-wrapper.properties` names `gradle-9.8.2-all.zip` and carries no `distributionSha256Sum`, so the
wrapper accepts whatever the distribution service returns for that URL. Gradle's `wrapper` task writes the properties
file from its own generator and accepts `--gradle-distribution-sha256-sum`, so the checksum is a first-class field.

Gradle already protects a pinned checksum: a `wrapper` run whose target version differs from the properties, with no
checksum configured for that run, **fails** — probed on the pinned 9.8.2, the task answers "gradle-wrapper.properties
contains distributionSha256Sum property, but the wrapper configuration does not have one. Specify one in the wrapper
task configuration or with the --gradle-distribution-sha256-sum task option." A same-version run with no option keeps
the line. So the pin cannot be dropped by omission; what a bump needs is the _new_ version's value, and that is what
this change supplies.

The bump is `/gradle-update`'s two `wrapper` runs plus a four-file diff. Its failure handling says a failed run "is
reported with what it found; the old wrapper files are not hand-edited", so nothing restores them — the first run may
already have rewritten `distributionUrl` while the jar and scripts still name the old version, the state the command's
own decision point calls "incomplete".

The published checksum is served beside the distribution, at
`https://services.gradle.org/distributions/gradle-<version>-all.zip.sha256`, a single line of hex. The pin for the
current version is `a1bb7e9651ef09e4cd9a62d471564e0ee23b5757e5f21fd7358d7cfc21018fdf`.

## Goals / Non-Goals

**Goals**

- Make the wrapper reject a distribution whose bytes do not match the pinned checksum.
- Make the bump carry the checksum, and make a failed bump restore the four files it touched, bytes and modes alike.
- Fail the build when the properties carry no usable checksum — catching a hand edit, or a pin that was never added —
  rather than relying on Gradle's own refusal at bump time.

**Non-Goals**

- Pinning the wrapper jar itself, or adding a signature check on the distribution.
- Changing which Gradle version is pinned, or the update check that reports a newer one.
- Re-verifying a distribution Gradle has already accepted.
- Rolling back anything outside the four wrapper files.

## Decisions

1. **The checksum lives in `distributionSha256Sum`, pinned by this change and rewritten by every bump.** Gradle verifies
   each download against it before install, so a corrupted archive fails instead of installing. The guarantee starts at
   the pin: the value is taken from the same host that serves the archive, so a substitution accepted at pin time
   passes, and a repository-side post-download hash would only fail after Gradle had used the bytes. Alternative
   rejected: that post-download script, which also covers only the paths it is told about.
2. **The bump becomes `scripts/update-gradle-wrapper.py`, and `/gradle-update` becomes its trigger.** Rollback is the
   reason: restoring a properties file and three rewritten artifacts on failure is a procedure a prose command cannot
   execute reliably and no test can check. The tool resolves the target checksum, snapshots the four files, runs both
   wrapper passes with the checksum, verifies the result, and restores the captured bytes and executable modes on a
   handled failure. It does not refuse a dirty tree: the snapshot restores exactly the bytes it captured, so a
   pre-existing edit comes back unchanged rather than being lost, and refusing would block this unit's own rehearsal,
   whose work is uncommitted by design. Alternative rejected: keeping the prose procedure and documenting a
   `git checkout` rollback, which leaves the snapshot to whoever remembers it. Not adopted without assessment: Gradle's
   `gradle-wrapper-upgrade` skill, which the parked entry names — no released definition of it was inspected for this
   change, so its failure path and its refresh obligations are unknown here, and adopting it would add a third-party
   skill tree with its own provenance record.
3. **The tool refuses to invent a checksum.** It fetches the `.sha256` beside the distribution and stops before touching
   a file when the response is absent, empty, or not 64 hex characters. Alternative rejected: hashing the downloaded
   archive, which downloads the whole distribution to verify bytes Gradle is about to download again.
4. **A `check` gate validates the pin's configuration, not its correctness.** `verifyWrapperChecksum` fails when the
   properties carry no `distributionSha256Sum`, when the value is not 64 hex characters, or when `distributionUrl` does
   not end `-all.zip`. It does not assert that the checksum names the version in the URL: nothing in the repository owns
   that version except the properties file itself, so the assertion would need a second copy the bump would have to keep
   in step, and the tool already verifies the pair it writes. It does not fetch the published hash either, which would
   put an external service on `check`'s path. Alternative rejected: a build-logic rule class, which the repository uses
   for logic a build script composes; this gate reads one file and compares strings, the shape the existing
   `Exec`-plus-script gates take.
5. **The tool's tests drive fixtures, not the network.** A recorded `.sha256` body, a properties file, and a fake
   `gradlew` whose two runs can be told to succeed, fail, or write only `distributionUrl`, so the restore paths run
   without a download. Alternative rejected: a test against the real service, which would make the suite download a
   distribution and fail on a network fault.
6. **The tool requires an explicit target version; the candidate stays the command's decision.** `/gradle-update` reads
   `dependencyUpdates`, selects the version and passes `--version`; the tool never resolves "latest stable" itself, so
   one source decides every run and no second resolution can disagree with it. A missing `--version` is a usage error,
   not a cue to pick a version.

## Risks / Trade-offs

- **A wrong pin breaks every bootstrap.** The value comes from the distribution service's own `.sha256` file, and a
  mismatch is a loud failure at wrapper start rather than a silent install.
- **The restore covers what the tool handles, not a machine failure.** On a failed or incomplete `wrapper` run it puts
  back the four files' bytes and executable modes and says so; a restore that fails while the tool is running — a
  read-only target — is reported as incomplete, with the snapshot kept as the only copy of the bytes it found. A process
  that dies writes no report at all, so that case leaves the snapshot silently. The directory is printed only once the
  capture has completed, so a path a reader has is a usable one. A concurrent edit to those files during the run is
  overwritten by the restore, which the report makes visible.
- **The gate proves presence and shape, not that the value matches the archive.** The value's correctness rests on the
  tool's fetch and on a cold-cache bootstrap, which the verification section states.
- **The tool is new surface beside the command it replaces.** It is one script with a suite, in the place the
  repository's tooling rule puts it, and the command keeps the trigger and the lifecycle.

## Verification

```bash
# The tool's and the gate's controls — fixtures only, no network, no download
./gradlew testUpdateGradleWrapper testVerifyWrapperChecksum

# The gate and the repository's own checks
./gradlew verifyWrapperChecksum
./gradlew check -PskipITs -Pcoverage.gate.enabled=false
```

The pin's own evidence is a bootstrap that refuses a mismatch. A cached distribution never re-validates, so the probe
needs an empty wrapper home; pointing the URL at a tiny local archive keeps it fast, and the refusal is Gradle's own:

```bash
SCRATCH="$TMPDIR/opencode/wrapper-probe"
rm -rf "$SCRATCH" && mkdir -p "$SCRATCH/gradle/wrapper"
cp gradlew "$SCRATCH/" && cp gradle/wrapper/gradle-wrapper.jar "$SCRATCH/gradle/wrapper/"
printf 'not a gradle distribution' > "$SCRATCH/fake.zip"
{
  echo "distributionBase=GRADLE_USER_HOME"
  echo "distributionPath=wrapper/dists"
  echo "distributionUrl=file\://$SCRATCH/fake.zip"
  echo "distributionSha256Sum=$(printf '0%.0s' $(seq 64))"
} > "$SCRATCH/gradle/wrapper/gradle-wrapper.properties"
(cd "$SCRATCH" && GRADLE_USER_HOME="$SCRATCH/home" ./gradlew --version)
# expected: "Verification of Gradle distribution failed!" naming the expected and actual checksums
```

The accepting half is CI's: its cache configuration omits `wrapper/dists`, so a run with a cold cache downloads the
pinned distribution and verifies it. That accepting run has not been observed for this pin — the local round trip used a
warm cache — so the configuration is the claim, not a recorded result; it complements the static gate rather than
replacing it.

One real bump is rehearsed against the live distribution service: bump to a named release, confirm the four files and
the checksum, then bump back through the same tool. The rehearsal's own work is uncommitted, which decision 2's
snapshot-not-guard choice allows.

## Claims table

| Claim                                                                                  | Source                                                                                                                                | Result                                                                                                                                   |
| -------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------- |
| The wrapper carries no checksum today                                                  | `grep -n "distributionSha256Sum" gradle/wrapper/gradle-wrapper.properties`                                                            | no match                                                                                                                                 |
| Gradle's `wrapper` task accepts the checksum as an option                              | `./gradlew help --task wrapper`                                                                                                       | `--gradle-distribution-sha256-sum  The SHA-256 hash sum of the gradle distribution.`                                                     |
| The distribution service publishes the checksum beside the archive                     | `curl -sL https://services.gradle.org/distributions/gradle-9.8.2-all.zip.sha256`                                                      | `a1bb7e9651ef09e4cd9a62d471564e0ee23b5757e5f21fd7358d7cfc21018fdf`                                                                       |
| Gradle refuses a version bump that would leave the pin without a value                 | a `wrapper` run in a scratch project carrying a pinned checksum, targeting another version with no `--gradle-distribution-sha256-sum` | the task fails: "gradle-wrapper.properties contains distributionSha256Sum property, but the wrapper configuration does not have one"     |
| The generated wrapper jar is byte-identical when the running and target versions match | task 3.1's round trip 9.8.2 → 9.8.1 → 9.8.2, whose final tree differs from `HEAD` only by the checksum line                           | the jar and both scripts returned unchanged, so an all-four-files-differ criterion would call a complete bump incomplete                 |
| The current bump's failure handling restores nothing                                   | `.opencode/commands/gradle-update.md` (Failure handling)                                                                              | "the old wrapper files are not hand-edited"                                                                                              |
| No requirement states the wrapper's pin                                                | a `grep -rn` for the wrapper's names over `openspec/specs/`                                                                           | 11 hits — the wrapper's executable bit, the update report's currency clause, and three unrelated uses of the word; none names a checksum |
| The capability's mechanical guards are enumerated one requirement each                 | `openspec/specs/showcase/quality/commit-hygiene/spec.md` (Purpose)                                                                    | the executable bit, conflict markers, size, table rows, cron collisions and the rest each carry a requirement                            |
| The `check` task is where the repository's verification tasks are wired                | `build.gradle.kts` — the `dependsOn("verify…")` list                                                                                  | the list carries `verifyTableRows`, `verifyExecutableBits` and the rest; the new gate joins it                                           |

Dismissals, one reason per kind: `path` — every named path is a repository file or a distribution URL this change reads
or edits; `absolute` — the flagged words sit inside quoted command text or inside the change's own Non-Goals; `count` —
the figures are the four wrapper files, the 64 hex characters of a SHA-256, the two wrapper passes, and the eleven spec
hits the row reports; `cross-ref` — the flagged commands and paths are this change's own verification instructions.

## Consequences

- A wrapper download is verified against a pinned hash, so a corrupted archive fails loudly from the pin onwards.
- A bump either completes with its checksum or ends in one of the outcomes the risk section states — restored, an
  incomplete restore whose snapshot holds the bytes it found, or a killed process whose snapshot is its only report — so
  a run never leaves a half-rewritten wrapper without material to recover it.
- The wrapper's update procedure becomes executable and testable instead of prose an agent executes from memory.
