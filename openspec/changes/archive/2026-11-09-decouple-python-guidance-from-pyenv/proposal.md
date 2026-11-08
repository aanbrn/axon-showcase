# Decouple the optional interpreter guidance from pyenv

## Why

The repository's Python guidance names pyenv as though it were the way to select an interpreter, yet nothing depends on
it: the build, the commit hook and the setup all take the first `python3` on `PATH`. A reader who uses uv instead meets
pyenv-specific remedies that do not apply, and a `.python-version` written by `uv python pin` changes what a bare
`python3` runs only where the local environment integrates it into the interpreter selection; the repository itself
selects `python3` from `PATH`.

## What Changes

- **`README.md`**, the optional interpreter-selection paragraph: state that the tooling runs the first `python3` on
  `PATH`, that a personal `.python-version` matters only where the environment integrates it into that selection
  (pyenv's shims do, and uv's own commands read the same file), that a pin does not by itself change which `python3` the
  shell resolves, and keep the `python3 --version` check.
- **`README.md`**, the `NEEDS_ACTION` remedy line: name `pyenv local` as an example and state the condition it must
  satisfy — `python3` resolves to that interpreter — instead of "where pyenv supplies it".
- **`.opencode/skills/setup-idea/SKILL.md`**, initialization step 5: describe the selector as one on `PATH` that reads
  the file, such as pyenv's shims, rather than naming pyenv as the mechanism.
- **`.opencode/skills/setup-idea/SKILL.md`**, the `NEEDS_ACTION` remedy in the procedure: the same condition and example
  as the script's message.
- **`scripts/ensure-idea-settings.py`**: the `NEEDS_ACTION` remedy states the condition and keeps one concrete example.
- **`scripts/test-ensure-idea-settings.py`**: pin the condition phrase beside the example.
- **Deliberately unchanged**: interpreter resolution (the first `python3` on `PATH`), `.python-version` inheritance, CI
  provisioning, the doctor's `python3` probe, and the README Prerequisites row.

### New Capabilities

None.

### Modified Capabilities

None. `showcase/quality/ide-config` already specifies the selection rule, the interpreter matching, and the
`.python-version` inheritance without naming a manager — `grep -rn "pyenv" openspec/specs/` returns no hits, and the
inheritance requirement states "the `.python-version` that names it" — so the wording change satisfies the existing
requirement, and the change is `skip_specs: true`.

## Impact

- **Files**: `README.md`, `.opencode/skills/setup-idea/SKILL.md`, `scripts/ensure-idea-settings.py`,
  `scripts/test-ensure-idea-settings.py`, and the change's own artifacts.
- **Prerequisites**: unchanged. The build needs `python3` on `PATH`; uv is not required, so it gains no doctor probe and
  no Prerequisites row — an optional doctor entry covers a distinct repository workflow, not another supplier of an
  existing prerequisite.
- **Verification**: every uv and pyenv behavior the wording rests on was probed on the installed `uv 0.13.7` and
  `pyenv 2.8.14` and is recorded in `design.md`; the script's test suite passes, `spotlessCheck` passes, and
  `openspec validate --all` accepts `skip_specs`.
