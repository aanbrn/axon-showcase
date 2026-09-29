---
description: Check whether this machine has everything the build, tests, and deployment need
---

Check this machine's toolchain against the project's prerequisites.

Follow the project's `scripts/doctor.sh`: run it from the repo root. It detects the platform, probes each prerequisite
for presence and its version floor, checks the repo state (git hooks installed, Docker daemon reachable), and prints one
line per prerequisite with the platform-appropriate install command for anything missing or too old. The README's
Prerequisites table is the documented list; run the script rather than reciting it.

Report the result to the user as what to do, not as a raw dump:

- If every required prerequisite is satisfied, say so in one line and list only the optional ones that are missing.
- For each unsatisfied or too-old prerequisite, give the user the exact command the doctor printed — that is the install
  hint for their platform, which the README's Prerequisites table also carries.
- Only Java 21+ and Docker with a reachable Compose v2 are required for the build and tests; call everything else
  optional and do not present a missing optional tool as blocking.

Do not restate the tool list or the install commands from memory — the doctor's output is the source. If the doctor
reports a prerequisite as `unknown` (its version could not be read), say that explicitly rather than treating it as
satisfied.

`./scripts/doctor.sh --all` exits non-zero when an optional prerequisite is missing too; use the default invocation
unless the user wants the stricter verdict.
