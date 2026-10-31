## Why

The setup's hand-back tells a contributor to apply its work with **File → Reload All from Disk**, and the Python stage's
`CONFIGURED` outcome reads as "nothing to do". Neither holds for the Python analysis module: IntelliJ applies a module
registration written into `.idea/modules.xml` when the **project is opened**, not when an open project reloads — the
experiments in disposable worktrees measured the reload leaving the module inactive and the reopen activating it — and
the module's requirement states no activation step, so the skill, the script, the README, and `AGENTS.md` all repeat the
wrong instruction.

## What Changes

- `openspec/specs/showcase/quality/ide-config/spec.md` gains a `MODIFIED` delta for "The setup reproduces the Python
  analysis module per working tree": the run reports the module's activation procedure — the project is opened after a
  run made while it was closed, and a run made while it was open is completed by closing the project, running the setup
  again, and opening it — and it does not present reloading an open project as the way the module is added. The
  build-model scenario gains the precondition that the project has opened the module.
- `scripts/setup-idea.sh` closes with that procedure: open the project, close and re-run when the project was open,
  accept the Gradle-project load IntelliJ offers, and the formatter settings' reload-or-restart note.
- `.opencode/skills/setup-idea/SKILL.md` carries the procedure at the steps that hand the run back: `CONFIGURED` no
  longer means "nothing to do", a run made while the project was open names the close-and-re-run step, and the hand-back
  states the activation.
- `scripts/test-setup-idea.sh` asserts the closing section's clauses and keeps a control per clause, plus a fixture with
  an already existing module list so the instruction is shown not to vary with the case.
- `README.md` and `AGENTS.md` state the same procedure and scope the reload-or-restart note to the formatter and
  inspection settings.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/ide-config` — the Python analysis module requirement gains its activation procedure and the
  precondition on the build-model scenario.

## Impact

- Guidance: the `setup-idea` skill, the setup script's closing section and its test, `README.md`, and `AGENTS.md`.
- Specs: a `MODIFIED` delta on `showcase/quality/ide-config` only; the main spec changes at archive.
- Records: `docs/process-defects/clarify-python-ide-activation.md` and
  `docs/decisions/2026-10-31-python-module-activation-probe.md` ship with the change and carry the experiments.
- No runtime code, build, dependency, API, schema, image, chart, or deployment change; the script's Python logic and
  exit status are untouched, and no Gradle project or storage setting is added.
