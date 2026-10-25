# Proposal

## Why

The repository tracks `.java-version` — content `21`, present since the initial commit — and never mentions it. A
visitor therefore cannot tell what the file is for, whether it is required, or what it influences: on this machine it
was read by jenv while `java` on `PATH` was a different JDK, and the Python analogue next to it _is_ documented as a
personal, ignored selection. The asymmetry is unexplained, and an undocumented file that reads as a shared Java
selection invites the wrong assumption — that having it is enough to switch Java.

## What Changes

- `README.md` gains a short block in **Getting Started → Build the Project**, immediately before `./gradlew build`,
  stating that the tracked file is an optional version-manager convention whose support depends on the manager and its
  configuration, and that the file alone does not switch Java.
- The block names the three mechanisms apart: a manager's `JAVA_HOME` export selects the JVM that launches `./gradlew`;
  the compile/test toolchain comes from `gradle/libs.versions.toml`; CI selects its own JDK. It closes by noting that
  `JAVA_HOME` and `PATH` can select different JDKs.
- No reader enumeration: which managers honour the file is the managers' own documentation, and a list here would rot.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None. `showcase/quality/toolchain-check` describes the diagnostic's outcome, `showcase/quality/merge-governance` the CI
gates, and `showcase/quality/ide-config` the IDE configuration the setup script merges; none states how a
version-manager selector file behaves, and this change alters no behavior. `skip_specs: true` records the judgement.

## Impact

- `README.md` is the shipped documentation change. `.java-version`, `gradle/libs.versions.toml`, `build-logic`'s
  toolchain convention, `ci.yml` and the merged Python policy are unchanged.
- The unit's capture rides the same branch: two guidance rules (`AGENTS.md`'s tool-behavior-claim trigger and
  `review-gate`'s pre-change-revision anchor, with the sibling prescription in `jvm-development` retargeted) and one
  parked-idea instance in `docs/ideas.md`. The two instances behind the `AGENTS.md` widening are one repo-evidenced
  correction and one session-record claim, as `tasks.md` 5.1 records.
- The owner's shell is configured outside the repository; no dotfile or shell configuration enters this change.
