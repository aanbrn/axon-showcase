# Design

## Context

See `proposal.md` — Why. The constraints that shape the approach:

- **The bootstrap paradox is the load-bearing constraint.** A Gradle task cannot be the doctor: it needs Java, a
  resolvable Gradle distribution, and (on a cold cache) a network — three of the very prerequisites a doctor most needs
  to report. A script needing only a POSIX shell runs on the machine that cannot build.
- **The repo already has a `scripts/` convention for exactly this shape.** `setup-idea.sh` (owned by
  `showcase/quality/ide-config`), `install-git-hooks.sh` (owned by `showcase/quality/commit-hygiene`), and their
  stdlib-only Python helper `ensure-idea-settings.py`. `AGENTS.md` records the interpreter constraint: repo Python must
  run under macOS's system `/usr/bin/python3`. Shell is a strictly smaller dependency, and the doctor's whole point is
  to run before anything else — so it is shell, not Python.
- **The prerequisites are already enumerated, as data, in `README.md`'s Prerequisites table** with an install command
  each. That table is the source of truth for the list; the doctor must not become a second, un-gated copy of it.
- **Environment probes are not verifiable by `check`.** The host is not an input to the build, and a check that reads
  the host fails for reasons the change did not cause — the same reasoning the repo applies to repository-settings drift
  (`docs/ideas.md`, "The GitHub description and topics are un-gated"). So the doctor is a tool, not a `check` member.
- **`timeout` is not available on macOS by default** (`coreutils` only). A Docker-daemon probe must not depend on it.

## Goals / Non-Goals

**Goals:**

- One command answers "can this machine run and verify the project?" — presence, version floors, and repo state in one
  pass, exiting non-zero only when the default build-and-test path is blocked.
- The diagnostics are charter-based and version-pinned where a version is cheaply readable — not an open-ended open
  source of subtly-wrong report lines.
- The list of tools stays single-sourced: the doctor prints the README's install command, and a test keeps them in step.
- An agent (via `/check-tooling`) can run it and translate the output into "do this", without the agent restating the
  table in prose.

**Non-Goals:**

- **Not a `check` member, and not a CI job.** The host is not a build input (see Context).
- **No silent "looks current" fallbacks** — the AGENTS.md rule for update checks ("a lookup that yields no readable
  version reads as current") applies in reverse here: an unreadable version is reported **unknown**, never as satisfied.
- Not a package manager or installer. It never installs anything; it reports the command that would.
- Not a replacement for the two `setup-*` commands (`/setup-agent-tools` global MCP config, `/setup-idea` IDE config):
  those configure external state and can act; this only diagnoses and hands the action back.
- Not a skill. A skill would restate the README's table (a second un-gated copy); a command that runs a script and
  interprets its output does not.

## Decisions

### A shell script, not a Gradle task (`scripts/doctor.sh`)

Rejected: a build-logic `DoctorTask` (cannot diagnose its own prerequisites; would sit behind a working JDK + network),
and a Python script (heavier precondition than shell, and the repo already documents the system-interpreter trap). The
cost accepted: no build-logic unit tests for the probe logic — mitigated by a shell test in `scripts/test-doctor.sh` run
by the change's tasks, and by keeping the probe logic a small, deterministic data table.

The script is **strictly POSIX `sh`**, unlike every sibling `scripts/*.sh`, which are `bash`. The divergence is
deliberate and is the point of the tool: a doctor whose job is to run on a machine where nothing else works yet must not
itself require `bash`, and `sh` is the one interpreter guaranteed on a bare Unix. Verified by running it under `dash`,
not only under macOS's `bash`-backed `sh` (macOS's `/bin/sh` is bash in POSIX mode, so it would mask bashisms).

### Platform-appropriate install guidance

The prerequisites are cross-platform, but install commands are not — the README's install column is macOS, and the repo
supports Linux too (CI runs Linux; the deployment docs name kind/minikube alongside colima). The doctor therefore
detects the platform (`uname`, plus `/etc/os-release` for the Debian/Ubuntu family) and selects the matching hint,
falling back to naming the tool without a command on an unrecognized platform — a wrong `brew` command on Linux is worse
than no command. Rejected: a portable "see the README" pointer (pushes back onto the user the lookup the tool exists to
spare), and gating only the macOS hints (leaves Linux with no guidance at all). The cost is a per-platform hint table
that can drift from the README's columns; `scripts/test-doctor.sh` asserts every charter tool has a hint on each
documented platform, and the README carries one install column per platform.

### One declarative charter table drives the probes

Each prerequisite is one row: `name`, the executable(s) it is probed through, the version argument, a version floor, a
`required`/`optional` class, and its per-platform install hint. The prober reads the table — the repo already treats a
single declarative table as the shape for a check family (`HelmUpdateRules`, `InfraImageVersionRules`,
`ModuleDependencyRules`, `InstallCommandRules` in `build-logic`). This bounds the diagnostics and makes the README
agreement checkable.

### Version floors, pinned only where a floor exists

Floors: Java 21+, Helm 4.x, Docker Compose v2 (`docker compose version`), Python 3. Node/Gradle/Kubernetes have no floor
to pin (Gradle is wrapper-pinned; the cluster is any working context), so they are presence-only — inventing a floor
would be a claim the repo does not make. A tool whose version cannot be read is reported `unknown`, not satisfied.

### The exit status is the verdict

Non-zero when a **required** prerequisite (Java 21+, Docker + a reachable daemon, and the repo-state checks) is
unsatisfied; optional tools (Helm, cluster, Snyk, the agent CLIs) are reported and never fail by default. This makes the
script usable as a check in the future without pretending the host is a build input today.

### A command, not a skill, for the agent entry point

`/check-tooling` runs the script and interprets the output. The `agents-auditor` scopes its audit by directory
(`.opencode/commands/` among them), so the new command is covered by the existing scope with no spec change — and the
repo's standing lesson is that a skill duplicating documented facts is an un-gated copy, so the README's table stays the
doc and the command is the entry point. Do not restate the tool list in the command body.

## Risks / Trade-offs

- **A probe can report a false absence when a tool is installed off `PATH`** (e.g. a GUI-launched shell with a minimal
  `PATH` — a failure mode `AGENTS.md` already records for Gradle execs). Mitigation: report the tool as not found and
  print the install command; the operator can judge. The doctor does not guess at alternate locations.
- **The README and the doctor's table can drift** (the repo's recurring "second copy" hazard). Mitigation: a test that
  asserts every tool the doctor names appears in the README table and vice versa, so the list is single-sourced in fact,
  not just in intent.
- **A version probe can hang** (a wrapper waiting on input). Mitigation: probes are non-interactive reads of the tool's
  own version output, with no `timeout` dependency (absent on macOS); a tool that prints nothing is reported `unknown`.
- **Scope creep toward "install things".** Mitigation: the non-goal above — the doctor never mutates the machine, so its
  blast radius is one report.
