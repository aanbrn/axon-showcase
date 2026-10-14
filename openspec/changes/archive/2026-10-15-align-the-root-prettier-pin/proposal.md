# Proposal

## Why

The root Spotless pins Prettier exactly at `3.9.6` while the web module resolves **3.9.10** — npm's latest. A markdown
construct the repository writes (a fenced block nested under a checklist item) **converges under 3.9.10 but not under
3.9.6**, and Spotless **skips** a non-converging file with a zero exit
(`Skipping '<path>' because it does not converge`), so that file is silently ungated while the gate reads green — it
happened to this repository's own `make-the-p99-verdict-confirm-first` `tasks.md`, which no `spotlessCheck` covered.
Aligning the pin to the version that converges removes the known trigger, and moving it into the version catalog puts it
where the repository's convention places build-tool versions.

## What Changes

- **`gradle/libs.versions.toml`** — a `[versions]`-only `prettier` entry (no `[libraries]` module: it is not a
  `group:name` dependency), holding the version the web module resolves and npm publishes as latest.
- **`build.gradle.kts`** — both `prettier("3.9.6")` sites read that entry, so the two targets' Prettier versions are
  aligned to it, rather than an exact pin beside a caret.
- **`docs/ideas.md`** — the answered entry removed.
- **An upstream report** that a skipped file is indistinguishable from a formatted one, so a formatter's green gate can
  hide an ungated file — drafted and posted through the review gate, since it is an outward artifact, and cited with its
  close-out in `AGENTS.md`'s skip rule, where the closure report reads it.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None: `skip_specs: true`. A formatter version pin; no specified behavior changes. (The `quality/code-quality` spec owns
the markdown scope and the formatter gate, not the tool's version.)

## Impact

- Files: `gradle/libs.versions.toml`, `build.gradle.kts`, `docs/ideas.md`, and `AGENTS.md` (the citation of the upstream
  report, with its close-out).
- **Verified by the tree, not by an assumption**: `spotlessApply` then `spotlessCheck` green, and the working tree
  showing no diff beyond this change's own edits — the alignment's risk is a tree-wide reflow, and a measured sample
  (`AGENTS.md`, `README.md`, `docs/ideas.md`, the load-tests spec) already reproduces the committed formatting
  byte-for-byte under 3.9.10.
- The web module's own Prettier is untouched: its `^3.9.6` caret already resolves 3.9.10, so tightening it would be
  explicitness with no behaviour change — a separate, non-goal edit in that module's dependency set.
