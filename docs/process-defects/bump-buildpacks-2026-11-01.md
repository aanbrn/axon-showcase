# bump-buildpacks-2026-11-01 process defects

### bump-buildpacks-2026-11-01#1 — New worktree not prepared for the IDE before implementation

**Recorded**: 2026-11-01, by the implementing agent at the owner's prompt.

**Stage**: implementation.

**Expected**: after creating the unit's tree and verifying its path and branch, run `./scripts/setup-idea.sh` there and
report the tree IDE-ready only when the Python stage reports `CONFIGURED` (`AGENTS.md`, "Workflow: Change lifecycle" →
**Workspace**).

**Observed**: the worktree was created and the session moved, then the update check ran and the catalog and docs were
edited before any IDE preparation. The owner asked who would configure IDEA. The first `setup-idea.sh` run afterwards
reported `Settings: CONFIGURED` but `Python: NEEDS_ACTION`, because the new tree carried no `.python-version` (the
primary's selects the registered `3.12.16` interpreter).

**Evidence**: the owner message "А кто IDEA настраивать будет?"; then `./scripts/setup-idea.sh` →
`Python: NEEDS_ACTION — this tree's interpreter is not registered as a Python SDK.`; after copying the primary's
`.python-version` into the tree, the re-run → `Python: CONFIGURED — wrote module and registration`.

**Consequence**: IDE preparation ran after the unit's first edits instead of before the tree was opened, and the plugin
install was skipped because IDEA was running. Repaired in the unit: the primary's `.python-version` was copied in (no
existing one overwritten) and the setup re-run to `CONFIGURED`; no artifact was damaged.

**Knowledge boundary**: observed the missed step and the `NEEDS_ACTION` outcome; did not check whether the owner had
already opened the tree in IDEA, nor whether the IDE had loaded the unprepared settings.

- **Decision** — shipped: https://github.com/aanbrn/axon-showcase/pull/793 — the created-tree wrapper creates and
  prepares a tree in one operation.
