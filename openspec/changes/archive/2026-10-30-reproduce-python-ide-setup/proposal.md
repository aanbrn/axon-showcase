# Proposal

## Why

The Python analysis setup in IntelliJ IDEA exists only as local IDE state: a `PYTHON_MODULE` covering `scripts/` plus an
interpreter registered in the IDE's global SDK table. `.idea/` is git-ignored, so every new worktree opens with no
interpreter for `scripts/*.py` — no code insight and no working Run/Debug default — and the setup has to be rebuilt by
hand per tree. `scripts/setup-idea.sh` merges the formatter settings but provisions nothing for Python, and `README.md`
says so explicitly.

## What Changes

- `scripts/ensure-idea-settings.py` gains a Python stage: it resolves the interpreter, reads the selected IDEA
  configuration's SDK table, and generates or reconciles the `scripts` module.
- `scripts/setup-idea.sh` gains an `--idea-config-dir` option and reports the Python stage's own outcome line, whose
  non-zero cases make the whole setup exit non-zero.
- `scripts/test-ensure-idea-settings.py` is added as the stage's committed control set, and `build.gradle.kts` runs it
  through a new `testEnsureIdeaSettings` task in `check`.
- The generated module is `.idea/modules/scripts.iml` (type `PYTHON_MODULE`, content root `scripts/`, one `Python SDK`
  order entry) plus its entry in `.idea/modules.xml`; other modules and IDE-managed content are preserved.
- The interpreter SHALL be the first `python3` on `PATH` resolved from the working tree's root, at its real binary path,
  at Python 3.12 or newer — the same selection rule the build uses, and the same interpreter Gradle and the Git hooks
  run.
- The SDK name is read from the selected IDEA configuration's table rather than derived from the interpreter path; a
  missing or ambiguous match reports the action instead of writing a reference the IDE cannot resolve.
- `README.md`, `.opencode/commands/setup-idea.md` and `.opencode/skills/setup-idea/SKILL.md` state the one-time
  prerequisite per IDEA configuration and interpreter, and the new outcome vocabulary.
- `.gitignore` is unchanged: no `.venv`, no committed `.idea/` fragment, no `.python-version` management.

## Capabilities

### New Capabilities

None — the behavior belongs to the capability that already owns the setup script.

### Modified Capabilities

- `showcase/quality/ide-config`: adds a requirement that the same setup reproduces, per working tree, the IntelliJ
  Python analysis module for `scripts/*.py`, with the interpreter selection, the SDK-name lookup, the outcome
  vocabulary, and its exit-code contract.

## Impact

- **Scripts**: `scripts/setup-idea.sh`, `scripts/ensure-idea-settings.py`, new `scripts/test-ensure-idea-settings.py`.
- **Build**: a new `testEnsureIdeaSettings` task joins the root `check` block, so the Python stage's controls run in the
  pull-request gate.
- **IDE state**: `.idea/modules/scripts.iml` and `.idea/modules.xml` are written per working tree; both stay ignored and
  no repository file becomes versioned IDE configuration.
- **Docs and agent tooling**: three README passages that describe what the setup does — the `/setup-idea` row in the
  slash-command table, the drift bullet in the IntelliJ section, and the Python-analysis paragraph that currently states
  the setup does not register an interpreter — plus the `AGENTS.md` bullet describing what the setup merges, the
  `/setup-idea` command, and its skill.
- **Out of scope**: creating a `.venv`, writing the IDE's global SDK table, registering interpreters automatically,
  managing `.python-version`, changing CI or the toolchain doctor, and promising identical patch versions across working
  trees.
