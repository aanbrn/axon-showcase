# Design

## Context

See `proposal.md` for motivation and `specs/showcase/quality/commit-hygiene/spec.md` for the requirements.

The repository has no commit-time guard today: `core.hooksPath` in this clone points at `scripts/git-hooks`, but that
directory does not exist and is untracked, so no hook runs. The four defects are currently caught by a human or a review
pass, and the `captured:` placement convention lives only in `AGENTS.md` prose.

Constraints that shape the approach:

- The root `check` task already aggregates root-level checks — `verifyInfraImageVersions`, `workflowLint`, and
  `verifyModuleDependencies` are registered in `build.gradle.kts` and added via
  `tasks.named("check") { dependsOn(...) }` — so a content check can join the CI `build` gate without editing `ci.yml`.
- Python 3 is already a documented prerequisite (used by `scripts/setup-idea.sh`), and the CI runner ships it.
- The staged set is a local, pre-commit concern: CI never sees an index, so the index-only defects cannot be gated
  remotely.

## Goals / Non-Goals

**Goals:**

- Block the four defect classes before a commit is created, with a guard that runs locally and cheaply.
- Gate the two content-only defect classes (formatting, marker placement) in the CI `build` gate as well, so they hold
  even when the hook is not installed or is bypassed.
- Keep one implementation of each check so the local guard and the build check cannot drift.

**Non-Goals:**

- A hook manager (husky, lefthook, pre-commit) or auto-installation without an explicit step.
- Commit-message linting or any other commit-time policy beyond the four defect classes.
- An ADR: this is change-scoped tooling, not an architectural decision — it changes no service boundary, module
  dependency, or runtime behavior.

## Decisions

### Decision: A tracked git `pre-commit` hook backed by a Python checker

The guard is `scripts/git-hooks/pre-commit` (tracked) invoking `scripts/commit-hygiene.py`.

- **Alternative — a Gradle task run manually or in CI:** the staged set is local and pre-commit; CI cannot see the
  index, and a task still needs a hook to run automatically, so this does not remove the hook.
- **Alternative — a bash checker:** the repository has no shell test harness, and the marker/item-boundary logic (the
  exact logic that produced the recurring slip) is far easier to cover with Python's built-in `unittest`.

### Decision: One Python checker with a `--staged` and a `--markers` mode

`commit-hygiene.py` implements all four checks; the hook runs `--staged` (index checks plus the marker check on the
staged `AGENTS.md`), and the build runs `--markers` (content-only). One implementation serves both entry points.

- **Alternative — a Kotlin build-logic task for the CI marker check:** duplicates the marker logic in a second language,
  which the repository's "one implementation, no drift" instinct rejects; the build task instead just runs the checker.

### Decision: The format check delegates to the project's formatter check

The checker's format check runs the project's canonical formatter check (`./gradlew spotlessCheck`) when a
formatter-owned file is staged and refuses the commit when it fails; the command is configurable so tests can stub it.
The check covers the working tree, not the index; a partially-staged file is covered by the staged-then-edited check.

- **Alternative — run `spotlessApply` and diff:** the guard must not modify the working tree or the index, and
  `spotlessApply` rewrites files.
- **Alternative — reimplement palantir/ktfmt/Prettier checks:** drifts from the gate CI actually runs.

### Decision: The marker criterion is "inside a rule block, never on a plain bullet"

The checkable invariant is that each bare `captured:` marker lies within a rule block — a bold-lead bullet (`- **…**`)
or a bold-lead paragraph (`**…**`) — and never on a plain `- Text` bullet. The criterion is the nearest preceding
column-0 `- `/`**` lead: a marker is accepted when that lead is a bold-lead rule, so a rule's continuation text —
indented or not — is accepted, because the file continues a bold-lead rule in further paragraphs that carry its markers.
This is the mechanically sound form of the `AGENTS.md` placement convention: "at the end of the rule it records" is
authoring intent, not an invariant the file holds, because a later capture that merges into a bullet appends text after
the existing marker. Verified against the current `AGENTS.md`: 44 bare markers, 0 on a plain bullet, so the check passes
the tree as it stands.

- **Alternative — enforce "the marker is the last line of its item":** falsified by the current file (most markers are
  mid-item), so the check would fail an unmodified tree.
- **Alternative — treat any `captured:` token as a marker:** the file also mentions the token in prose (backticked
  `` `captured:` ``); the checker ignores backticked occurrences.

### Decision: Activation via `core.hooksPath` and an install script

`scripts/install-git-hooks.sh` sets `core.hooksPath` to the tracked `scripts/git-hooks` directory, and the README/AGENTS
docs describe it.

- **Alternative — write into `.git/hooks`:** untracked, per-clone, and invisible to review.
- **Alternative — a hook-manager dependency:** a new third-party dependency for a single hook.

### Decision: The build-gated marker check is a root Gradle task that runs the checker

`build.gradle.kts` registers `verifyCapturedMarkers` (runs `commit-hygiene.py --markers`) and `testCommitHygiene` (runs
the Python unit tests), both added to `tasks.named("check")` and declaring their inputs (`AGENTS.md` and the scripts).

- **Alternative — a CI workflow step running Python:** deviates from the "workflows run existing Gradle gates" principle
  and would need a `ci.yml` edit plus a `merge-governance` delta; wiring into `check` needs neither.

## Risks / Trade-offs

- **Hooks are not cloned** → the install script and docs make activation explicit; the build marker check still gates
  the marker class in CI regardless of local hook state.
- **`--no-verify` bypasses the guard** → accepted by design (the spec requires the bypass); the CI `check` still gates
  the two content classes, while the index-only classes are caught only locally.
- **The formatter check may need the toolchain on a cold cache** (Gradle dependencies, and Prettier via npm) → the guard
  runs the same check CI runs and is expected to run with a warm cache; a cold cache may resolve dependencies, so the
  requirement does not claim offline operation.
- **The format check's Gradle invocation adds per-commit latency** → run it only when a formatter-owned file is staged.
- **Marker boundary parsing is the logic that produced the slip** → the checker's boundary logic is unit-tested with
  five fixtures: correctly placed (pass), a marker on a plain `- Text` bullet (fail), a wrapped marker whose token
  starts the next line (pass), a backticked prose mention (ignored), and a merged-into bullet with the marker mid-item
  (pass).
- **Python is a prerequisite** → already documented and present on the CI runner; a missing `python3` fails with a clear
  message naming it.

## Migration Plan

No data or deployment migration. Rollout: land the scripts and the build wiring, then run `scripts/install-git-hooks.sh`
in a clone (a no-op where `core.hooksPath` is already set). Rollback: remove the hook directory or unset
`core.hooksPath`; the build tasks can be dropped from `check`.
