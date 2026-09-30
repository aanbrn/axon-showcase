# Proposal

## Why

`scripts/doctor.sh` reports the Git hooks as a **required** prerequisite, so a fresh clone that has not run
`./scripts/install-git-hooks.sh` makes the diagnostic exit non-zero. That contradicts the capability's own spec — its
required/optional requirement exits non-zero only "when a prerequisite for the **default build-and-test path** is
unsatisfied", and the repo-state requirement's scenarios only ever have the hooks _reported_ — and it contradicts the
README's "only Java and Docker are required". A clone without hooks builds and tests perfectly; the hooks guard commits,
not builds. An audit of the agent tooling surfaced the disagreement, and a docs unit
(`apply-agents-audit-docs-findings`) already removed the command's now-false recitation of the split.

## What Changes

- `scripts/doctor.sh`: change the `git-hooks` probe's class from `required` to optional-family, so a clone without the
  hooks is **reported** (with `./scripts/install-git-hooks.sh` as the remedy) but does not fail the default run — making
  the code match the spec the capability already carries. The `docker-daemon` probe stays **required**: integration
  tests genuinely need a live daemon, which is on the build-and-test path.
- No change to the diagnostic's output shape, its platform guidance, or any other probe.

This is a one-line classification fix plus the spec delta that records the classification rule; it changes a shipped
outcome (a clone without hooks now exits 0 instead of 1), which is why it is its own change rather than an edit folded
into the docs unit.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/toolchain-check`: the repo-state requirement gains the classification rule — the repo-state checks
  are reported and do not by themselves fail the default run, with the Docker daemon an explicit exception because the
  integration tests require it.

## Impact

- Edited files: `scripts/doctor.sh` (the `git-hooks` probe's class), and the change's delta spec.
- The spec is a `MODIFIED` block for "The diagnostic reports repo-state prerequisites", carrying both of its existing
  scenarios plus the new classification scenario.
- No build, test, or deployment behavior changes; the build does not read the doctor (the host is not a build input, a
  deliberate non-goal of the original change). No other probe, output line, or exit path moves.
- No `showcase/quality/toolchain-check` **Purpose** refresh is owed: the Purpose describes the diagnostic as probing the
  toolchain the build, tests, and deployment depend on, and never ascribes a required/optional class to the hooks, so
  this change does not falsify its text. Recorded here so the archive-time sweep is closed explicitly rather than
  silently skipped.
- `.opencode/commands/check-tooling.md` needs no further edit: it already defers to the doctor's own classification,
  which is exactly what this change makes correct.
