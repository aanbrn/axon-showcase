# Proposal

## Why

`scripts/ensure-idea-settings.py` matches an SDK entry by the interpreter's resolved binary, and a virtual environment's
`bin/python` resolves to the base interpreter it was created from — so the venv's SDK matches an unrelated interpreter.
Where an IDE configuration still registers such an SDK, the stage reports a false ambiguity, exits non-zero, and writes
no module — or, when one configuration holds that entry, writes a module naming the venv's SDK for the base interpreter.

## What Changes

- `scripts/ensure-idea-settings.py` matches an SDK entry only when the entry's interpreter resolves to the same binary
  as the interpreter the probe ran and the entry is installed in the install that binary belongs to — the directory of
  the probe's path or, outside a virtual environment, the directory that path resolves to. Neither the binary nor a
  directory alone is sufficient, and inside a virtual environment the base installation is a different SDK.
- `probe_interpreter` reports the path the interpreter names for itself (`sys.executable`), its version, and whether it
  runs from a virtual environment, instead of its resolved target, so the report and the match use the interpreter the
  build selected rather than the file that path finally runs.
- `scripts/test-ensure-idea-settings.py` gains ten cases over fixtures built from real directories and symlinks: a
  virtual environment's SDK must not match the base interpreter it wraps; a different interpreter installed beside the
  probe's must not match either; that venv entry beside the base entry must not turn the match ambiguous; a
  `bin/python3` entry must still match the `bin/python3.12` it symlinks to; an install reached through a symlinked
  prefix must match its real path; an entry invoked from `bin` with its target outside that directory must match the
  path it was run from; an entry naming the link's target must match the link the probe's path points through; a venv
  probed through its own interpreter must match its own SDK; and that same probe must not select the base installation's
  SDK, whether it is the only entry or sits beside the venv's.
- The Python module moves to `.idea/python/scripts.iml`: IntelliJ's Gradle import rebuilds `.idea/modules/` and drops a
  registration written there, which is why a `CONFIGURED` run left the folder without Python analysis. A reconcile also
  removes the module file and the module-list entry the previous location left behind.
- The `NEEDS_ACTION` report becomes a short outcome line plus an indented, action-first block: the interpreter to
  register with the settings path and the re-run step, the alternative of running the tree on a registered interpreter,
  then the registered SDKs with their interpreter paths and the configurations searched. `CONFIGURED`, `SKIPPED`, and
  `ERROR` stay single-line.
- `scripts/ensure-idea-settings.py`'s canonical inspection constant is respelled as concatenated segments so the file
  keeps the repository's 120-column rule without changing the string it merges into the inspection profile.

## Capabilities

### New Capabilities

None — the behavior refines requirements the capability already states.

### Modified Capabilities

- `showcase/quality/ide-config`: the requirement "The Python analysis interpreter is the one the build runs" gains the
  matching rule (the same binary, installed in the install that binary belongs to — the reported path's directory or,
  outside a virtual environment, the directory that path resolves to — so a virtual environment and a different
  interpreter installed beside it each stay distinct from the base), with scenarios for a virtual environment, an SDK
  registered by the link's target, a virtual environment's probe given only the base installation's SDK, and a sibling
  interpreter; the requirement "An SDK the IDE cannot resolve is reported, not written" points at the rule instead of
  restating a resolved-path comparison; and the requirement "The setup reproduces the Python analysis module per working
  tree" gains a scenario for a build-model import keeping the module, whose file is written outside `.idea/modules/`,
  the directory that import rebuilds.

## Impact

- **Scripts and their control**: `scripts/ensure-idea-settings.py` and `scripts/test-ensure-idea-settings.py`; the suite
  runs as `testEnsureIdeaSettings` inside `check`, so the pull-request gate covers the new rule.
- **Documentation**: no live artifact outside the spec states how an SDK is matched, so the matching rule needs no doc
  edit; the `NEEDS_ACTION` report's new content does — the `setup-idea` skill and its command hand over both remedies,
  and the README's Python paragraph states what the message now names.
- **IDE state**: a tree whose table holds only a virtual environment's SDK now reports the registration action rather
  than a false ambiguity; the module file moves within ignored `.idea/` state and the generated shape is unchanged.
- **Out of scope**: registering interpreters, rewriting the IDE's SDK table, changing the interpreter selection rule,
  and widening the setup to a platform this change does not run on.
