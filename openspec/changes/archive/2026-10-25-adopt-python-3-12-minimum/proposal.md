# Proposal

## Why

The build's Python tooling runs under whichever `python3` the machine's PATH resolves, and the CI runner's default moves
with its image (3.12.3 today, and Ubuntu 26.04 in November 2026 replaces it). The floor itself sits in prose — the
README's `Python 3` row, the doctor's `3`, `AGENTS.md`'s note inherited from macOS Command Line Tools — and no gate
compares an interpreter against it, while `check` runs four Python test tasks unconditionally. Declaring a 3.12 minimum
and verifying it in the gate turns a silent environmental assumption into a contract; the sites and their evidence are
in `design.md` → Context.

## What Changes

- Declare **Python 3.12** as the minimum the build's Python tooling requires, and make the doctor's `python3` probe a
  required one at that floor.
- Teach the doctor's probe declaration a **minor-version floor**, so `3.12` is expressible and compared as a number
  rather than rejected by the declaration's own validation.
- Provision Python 3.12 in the CI `build` job before the quality gates, and assert that the interpreter the gate
  resolves reports at least that version.
- Tie the declared floor's copies together by asserting the doctor's declaration, the README's Prerequisites row, and
  the workflow's `setup-python` input against each other in `test-doctor.sh`, and give the below-floor path behavioural
  controls over a fixture directory prepended to `PATH`.
- Correct the prerequisites documentation: Python and `actionlint` are on the default build-and-test path, not optional;
  the Python compatibility note states the declared minimum instead of macOS's bundled interpreter; the README documents
  selecting a Python interpreter for IDE analysis, which `setup-idea.sh` does not provision.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/toolchain-check`: the Python interpreter joins the required prerequisites at a stated minimum, and
  the diagnostic reports it as too old below 3.12.
- `showcase/quality/merge-governance`: both quality gates provision the declared Python minimum and the fast gate
  asserts the resolved interpreter's version.

## Impact

- `scripts/doctor.sh`: floor representation and comparison, the `python3` probe's class and floor, its install hints.
- `scripts/test-doctor.sh`: the required set, the declared-floor agreements (spec, README, workflow), and below-floor
  behavioural controls.
- `.github/workflows/ci.yml`: the `setup-python` step and its assertion step.
- `README.md`: the Prerequisites row and the IntelliJ setup note.
- `AGENTS.md`: the prerequisites bullet, the Python compatibility gotcha, and the `ubuntu-latest` clause.
- `scripts/review-routing.py`, `scripts/setup-idea.sh`: live claims that name the Python prerequisite.
- `build.gradle.kts`: `testDoctorCharter`'s declared inputs and description, which now cover the workflow and the
  assertions the check reads.
- `docs/ideas.md`, `.opencode/skills/check-and-test-authoring/SKILL.md`: the two follow-ups this unit parks, and the
  capture's lesson about gate controls.
- `.gitignore`: a personal `.python-version` is ignored rather than tracked, and the README documents that optional use.
- No change to the interpreter resolution: `pythonExecutable` keeps resolving the first `python3` on PATH, so the CI
  provisioning is what selects the interpreter and no local machine's resolution changes. The build script changes only
  `testDoctorCharter`'s declared inputs and description, which now cover the workflow the check reads.
- No syntax migration: the scripts stay stdlib-only and run unchanged on the new floor.
