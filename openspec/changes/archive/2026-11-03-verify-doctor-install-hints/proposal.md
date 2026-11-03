# Proposal

## Why

`showcase/quality/toolchain-check` requires the doctor's documented install guidance to agree with the README's
Prerequisites install column per platform, and `scripts/test-doctor.sh` asserts only that a hint **exists** for each
declared tool and platform. The two surfaces have therefore already drifted: the `compose` probe's hint differs from its
row's cell on both platforms, and `python3`'s Debian hint says something else again. A contributor who reads the table
and a contributor who runs the doctor are given different commands.

## What Changes

- `scripts/test-doctor.sh` asserts the agreement: every declared `tool` probe the README documents must print, for each
  install column the README carries, exactly the text of that column's cell for its row — with the cell's markdown
  (backticks, escaped pipes) and whitespace normalized, and an unmapped column or row failing rather than being skipped.
- The README's `Docker & Compose` row becomes two rows, `Docker` and `Compose`, because one row cannot carry two
  different installs per platform.
- `docker`'s Debian hint and cell drop `docker-compose-v2`, which is Compose's package and now Compose's row.
- The README's Python Debian cell takes the doctor's wording, which leads with the command the column is for.
- Both surfaces name the step that makes the macOS Compose plugin discoverable (`symlink into ~/.docker/cli-plugins`),
  because the install alone leaves `docker compose` unable to find it, and a column is a claim that following it
  satisfies the probe.
- The delta widens the scenario that enumerates what the build's check asserts, so the new agreement assertion is part
  of the spec's stated check contract.
- The parked idea is removed from `docs/ideas.md`.

## Capabilities

### New Capabilities

none

### Modified Capabilities

`showcase/quality/toolchain-check`: the check the build runs now also asserts that each documented probe's install hint
equals its README column's cell for every platform the README covers.

## Impact

- `scripts/test-doctor.sh` (the assertion and its controls), `scripts/doctor.sh` (two hint texts — the Debian Docker one
  and the macOS Compose one), `README.md` (the row split, the cells it changes, the sentence describing what the check
  keeps in step, and the required-set sentence Compose now belongs in), `docs/ideas.md`, and the unit's process-defect
  record.
- No behaviour change in how a probe is detected or classified: only the guidance text the doctor prints when a tool is
  missing, and the table it must agree with.
