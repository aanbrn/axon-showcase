# showcase/quality/commit-hygiene Specification

## Purpose

Defines the mechanical guards over the commit/artifact hygiene the standard quality gates cannot see — a local
pre-commit hook that inspects the staged set, and build checks that verify `captured:` marker placement and that the
tracked set excludes ignored files — so slips that were caught only by a human or a review pass are blocked by
construction.

## Requirements

### Requirement: A pre-commit guard blocks a mechanically defective staged set

The repository SHALL provide a tracked git `pre-commit` hook that inspects the staged set before a commit is created and
refuses the commit when it detects a mechanically defective state: the project's formatter check fails; a staged path is
a generated artifact that must not be committed; a path is staged and then modified again so the index no longer matches
the working tree; or a `captured:` marker is misplaced in a staged `AGENTS.md`. The hook SHALL name each offending path
and the reason and SHALL exit non-zero.

#### Scenario: A failing formatter check refuses the commit

- **WHEN** the project's formatter check fails while a formatter-owned file is staged
- **THEN** the guard refuses the commit and reports the formatter check's failure

#### Scenario: A force-staged generated artifact refuses the commit

- **WHEN** a generated artifact that the repository excludes (for example a Python bytecode file or a build output) is
  staged
- **THEN** the guard refuses the commit and names the offending path

#### Scenario: A path staged and then edited refuses the commit

- **WHEN** a path is staged and then modified again so the index and the working tree differ for that path
- **THEN** the guard refuses the commit and names the path

#### Scenario: A misplaced captured marker refuses the commit

- **WHEN** a staged `AGENTS.md` carries a `captured:` marker that is not inside a rule block (for example one sitting on
  a plain `- Text` bullet)
- **THEN** the guard refuses the commit and names the offending marker's location

#### Scenario: A clean staged set proceeds

- **WHEN** the staged set carries none of the detected defects
- **THEN** the guard passes and the commit proceeds

### Requirement: The guard is activated per clone and does not alter state

The repository SHALL document how a clone activates the guard by pointing `core.hooksPath` at the tracked hooks
directory, and SHALL provide an install step that applies it. The guard SHALL be bypassable with
`git commit --no-verify` for a deliberate exception and SHALL NOT modify the working tree or the index.

#### Scenario: A clone activates the guard

- **WHEN** a contributor runs the documented install step
- **THEN** `core.hooksPath` points at the tracked hooks directory and the guard runs on subsequent commits

#### Scenario: A deliberate exception can bypass the guard

- **WHEN** a contributor commits with `--no-verify`
- **THEN** the guard does not run and the commit proceeds

#### Scenario: The guard does not change repository state

- **WHEN** the guard runs
- **THEN** the working tree and the index are unchanged by it

### Requirement: Captured marker placement is verified by the build

The standard `check` task SHALL verify that each `captured:` provenance marker in `AGENTS.md` lies within a rule block —
a bold-lead bullet or a bold-lead paragraph — and never on a plain `- Text` bullet, so a misplaced marker fails the CI
`build` gate even when no local hook is installed or the hook was bypassed. A backticked mention of the token in prose
(`` `captured:` ``) is not a marker and SHALL NOT be treated as one. The marker's existence rule (each captured rule
carries a greppable `captured: <change>` origin) is specified in `showcase/quality/agent-skills`.

#### Scenario: The standard check verifies marker placement

- **WHEN** the standard `check` task runs
- **THEN** it includes the `captured:` marker-placement verification for `AGENTS.md`

#### Scenario: A marker outside a rule block fails the build

- **WHEN** a `captured:` marker in `AGENTS.md` sits on a plain `- Text` bullet rather than inside a rule block
- **THEN** the marker-placement verification fails and reports the offending location

#### Scenario: A backticked prose mention is not a marker

- **WHEN** `AGENTS.md` mentions the token in prose as a backticked `` `captured:` ``
- **THEN** the marker-placement verification does not treat that mention as a marker

#### Scenario: Correctly placed markers pass

- **WHEN** every `captured:` marker in `AGENTS.md` lies within a rule block
- **THEN** the marker-placement verification passes

### Requirement: The guard's checks are covered by tests run in the build

Each defect class the guard detects SHALL be covered by an automated test that exercises the checker, and the tests
SHALL run in the standard `check` task, so a regression that disables or weakens a check fails the build.

#### Scenario: The checker's tests run in the standard check

- **WHEN** the standard `check` task runs
- **THEN** it runs the checker's automated tests

#### Scenario: A disabled check fails a test

- **WHEN** a check is disabled or its logic regresses
- **THEN** at least one test that exercises that defect class fails

### Requirement: Tracked-file hygiene is verified by the build

The standard `check` task SHALL verify that no tracked file is excluded by the repository's ignore rules — the CI-gated
counterpart of the pre-commit guard's force-staged-artifact check — so a generated artifact force-added at any point
fails the CI `build` gate even when no local hook is installed. The verification SHALL name each offending path.

#### Scenario: The standard check verifies tracked-file hygiene

- **WHEN** the standard `check` task runs
- **THEN** it includes the tracked-file hygiene verification

#### Scenario: A tracked ignored file fails the build

- **WHEN** a file matching the repository's ignore rules is tracked (for example a bytecode or build-output file
  force-added to the index)
- **THEN** the tracked-file hygiene verification fails and names the offending path

#### Scenario: A clean repository passes

- **WHEN** no tracked file matches the repository's ignore rules
- **THEN** the tracked-file hygiene verification passes
