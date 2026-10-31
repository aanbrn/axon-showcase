# Design — auto-setup-new-worktree

## Context

Each unit works in its own tree (the `Workspace` rule in `AGENTS.md`), created from `origin/main`. A new tree carries no
`.idea/`, and the Python stage of `scripts/setup-idea.sh` resolves the first `python3` on `PATH` from the tree's root —
so a tree without a `.python-version` resolves the machine's global interpreter, which need not be the one the primary
registered as an SDK. The stage then reports `NEEDS_ACTION` and writes no module.

The primary checkout on the owner's machine carries that configuration: `.python-version` names `3.12.16`, and
`.idea/python/scripts.iml` references the matching SDK. Both are git-ignored, so the configuration is personal and does
not travel with the repository.

The owner's requirement: a contributor who configured Python once in the primary should not repeat it in every new tree
— and, since this repository's trees are created by the process rather than by the user, the preparation belongs to the
process. A manually created tree stays outside it.

## Decisions

### D1 — The agent prepares the tree; no hook and no wrapper

The preparation is a step in the existing `Workspace` procedure, which already creates the tree, moves the session, and
verifies path and branch.

- **Alternative — a `post-checkout` hook.** Rejected: it fires on every checkout in a linked worktree, so "a newly
  created tree" has to be inferred from an unborn previous HEAD plus a git-dir comparison; and the owner's requirement
  stops at process-created trees.
- **Alternative — a wrapper script.** Rejected: it helps only callers that use it, while the procedure every unit
  follows anyway is the place the step belongs.

### D2 — Inherit the selection, not the generated module file

The tree inherits the primary's `.python-version`; the setup then resolves the interpreter the tree's `PATH` reaches
through that selection and writes the module and its SDK reference. Copying `.idea/python/scripts.iml` would carry the
`jdkName` alone and leave the tree's `python3` on another interpreter, which the spec's alignment requirement forbids.

### D3 — The gate is the primary's IntelliJ setup; a tree's own selection is kept

The step prepares a tree when the primary carries `.idea/` — the checkout has been set up for IntelliJ. The inheritance
copies only into a tree without `.python-version`, so a deliberate choice there is not overwritten, and a primary
without a selection makes it a no-op: the setup still runs and reports the interpreter the tree resolved.

### D4 — Readiness is the Python stage's `CONFIGURED`

The step records the `Settings:` and `Python:` lines, the exit status, and the plugin outcome, and reports the tree
IDE-ready only on `CONFIGURED`. `NEEDS_ACTION` and `ERROR` are reported with their remedy; neither a copied
`.python-version` nor a zero exit from another stage turns them into success.

### D5 — No new script

The step is a documented copy plus the existing setup. It runs rarely, its inputs are one file, and a helper would need
the same guards the procedure states.

## Purpose determination

The `showcase/quality/ide-config` Purpose names the setup's classes, including the Python analysis module, and says a
working tree reproduces them from one run. This change says how a new tree comes to have the same interpreter selection
before that run; it adds no class and removes none. No Purpose refresh is owed.

## Claims table

| Claim                                                                                     | Source / command                                                                            | Result                                                                                          |
| ----------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------- |
| The primary carries a selection and a configured module                                   | `cat .python-version` and `cat .idea/python/scripts.iml` in the primary                     | `3.12.16`; the module references the matching SDK name                                          |
| The setup resolves the first `python3` on `PATH` from the tree's root                     | `grep -n "PATH\|which\|python3" scripts/ensure-idea-settings.py`                            | the resolution runs from the working tree's root                                                |
| The interpreter requirement states the resolution rule and no inheritance                 | `git show <base>:openspec/specs/showcase/quality/ide-config/spec.md`                        | the requirement at `:172` covers resolution and SDK matching only                               |
| The Workspace section carries no IDE-preparation step                                     | `git show <base>:AGENTS.md`                                                                 | the section's bullets cover creation, occupancy, release, the record, the cwd, and pause/resume |
| The inherited selection resolves to the registered SDK and the stage reports `CONFIGURED` | a disposable worktree, the primary's `.python-version` copied in, `./scripts/setup-idea.sh` | `Python: CONFIGURED`, the module references `~/.pyenv/versions/3.12.16/bin/python3`             |
| The change alters no code, script, or test                                                | `git diff --cached --stat`                                                                  | guidance and the delta only; `AGENTS.md` is `+5/−0`                                             |
| The delta validates                                                                       | `openspec validate --all` and `openspec validate --changes`                                 | 27 passed, 0 failed; the change validates                                                       |
| The Docker-free gate passes over the touched paths                                        | `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`                                   | BUILD SUCCESSFUL                                                                                |

Base revision: `edabc06cdf2ba4d939896cad26726744665a0d5d`.

## Dismissals (claim-scan candidates)

- **`count`** — every hit is a value the change itself declares or a record reports — a task number, a declared floor,
  an exit status — never a corpus tally or an unattended count.
- **`absolute`** — every hit is a scope word in the change's own artifacts (its planning files and the guidance it
  ships), or a record's own measured observation.
- **`path`** — every hit is a path, command, or path-shaped token in the change's prose or code, and every one names
  something the change reads, runs, or writes.
- **`cross-ref`** — every hit is a slash-bearing literal in prose (a path, a ratio), never a reference to another
  artifact's section.

## Risks

- **The inheritance depends on the primary's git-ignored state.** A clone whose primary has no `.python-version` gets no
  inheritance, and the setup reports the interpreter the tree resolved, `NEEDS_ACTION` included.
- **The inherited selection writes a personal file into the tree.** That is its purpose — the tree's build-visible
  `python3` then agrees with the analysis interpreter — and `.gitignore` keeps it out of the repository.
- **Only process-created trees are covered.** A tree the owner creates by hand keeps its own setup, as the owner chose.
- **The inheritance acts only through a selector on `PATH`.** The copied `.python-version` changes the tree's
  interpreter only where `python3` resolves through the tool that reads it (pyenv's shims ahead of other installations);
  elsewhere the file is inert and the stage reports the interpreter the tree resolved.
- **The step cannot install or register an interpreter.** A selection whose interpreter is not registered still ends in
  `NEEDS_ACTION`, reported rather than bypassed.
