---
description: Check whether this machine has everything the build, tests, and deployment need
---

Check this machine's toolchain against the project's prerequisites.

**Trigger** — the owner invokes `/check-tooling`.

**Procedure**

1. Run `./scripts/doctor.sh` from the repository root — the script is the authority for what it probes, its
   required/optional classification, and the install command it prints for anything missing or too old.
2. Report the result to the owner as what to do, not as a raw dump: for each unsatisfied or too-old prerequisite, give
   the exact command the doctor printed.

**Decision points**

- IF every required prerequisite is satisfied, THEN say so in one line and list only the optional ones that are missing.
- Report the doctor's own required/optional classification rather than reciting one: its default run exits non-zero only
  when a required prerequisite is unsatisfied, so a prerequisite the doctor lists as optional is not blocking.
- IF the doctor reports a prerequisite as `unknown` (its version could not be read), THEN say that explicitly rather
  than treating it as satisfied.
- `./scripts/doctor.sh --all` also fails on a missing optional prerequisite; use the default invocation unless the owner
  wants the stricter verdict.

**Failure handling** — a doctor run that cannot complete is reported with its output.

**Completion criteria** — the owner has the verdict, with an install command for each unsatisfied prerequisite.

Do not restate the tool list or the install commands from memory — the doctor's output is the source, and the README's
Prerequisites table is the documented list.
