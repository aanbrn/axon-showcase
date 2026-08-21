# Proposal: Gradle version reporting and wrapper-update command

## Why

The `dependencyUpdates` report already appends a `Gradle CURRENT updates:` section to `report.txt` (the ben-manes
plugin with `gradleReleaseChannel = "CURRENT"` always emits it), but the `/dependency-updates` command instruction
never tells the agent to surface it. When a newer stable Gradle is released, the info sits in the report file while the
command's summary silently omits it. And once the report flags a newer Gradle, there is no convenient, repeatable way
to update the wrapper — only a manual edit or a one-off `./gradlew wrapper --gradle-version=...` invocation.

## What Changes

- `.opencode/commands/dependency-updates.md`: extend the instruction so the agent also surfaces the `Gradle CURRENT
  updates` section — stating the current/pinned wrapper version and whether a newer Gradle release is available,
  flagging it for attention when one exists, and pointing at the `/gradle-update` command when an update is warranted.
- `.opencode/commands/gradle-update.md`: new command that reads the Gradle version reported by `dependencyUpdates`,
  and when a newer stable Gradle is available, runs `./gradlew wrapper --gradle-version=<latest>`, verifies the build
  (compile/tests) still passes, and reports the change. When already current, it reports that no update is needed.
- The wrapper update uses the built-in `wrapper` task with `--gradle-version`; no plugin changes.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None. No spec-level requirement changes: the `dependency-management` capability covers the report mechanism, which is
untouched; these are command/tooling additions. `skip_specs: true`.

## Impact

- **Code**: `.opencode/commands/dependency-updates.md` (instruction text), `.opencode/commands/gradle-update.md` (new).
- **Docs**: AGENTS.md / README.md mention the commands in the Build & Test section; add `/gradle-update` alongside
  `/dependency-updates` (docs refresh on change).
- **Build**: no change to `dependencyUpdates` output; `/gradle-update` invokes the existing `wrapper` task.
- **Tests**: no test changes.