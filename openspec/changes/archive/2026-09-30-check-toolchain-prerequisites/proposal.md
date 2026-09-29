# Proposal

## Why

A fresh clone has no way to ask whether its machine can run the project: the prerequisites are documented as prose in
`README.md`, and each missing tool surfaces only when the task that needs it fails — a missing `actionlint` or `pack`
reddens `check`, a missing Docker daemon fails `integrationTest`, a missing Helm or Snyk CLI fails the deployment or
security task. The reader diagnoses one failure at a time, and the failure names the task, not the machine's gap. The
two `setup-*` commands (`/setup-agent-tools`, `/setup-idea`) configure external state; nothing reports the local
toolchain as a whole.

## What Changes

- Add `scripts/doctor.sh` — a POSIX shell script that probes the local toolchain and prints one line per prerequisite
  with its status (present/absent/too old), the installed version where one is readable, and the README's install
  command for anything actionable. It requires nothing but a shell, so it can diagnose its own first prerequisite
  (Java).
- Probe three classes: **tool presence** (`java`, `docker`/`compose`, `actionlint`, `pack`, `helm`, `kubectl`, `snyk`,
  `python3`, the `git` hook state, `gh`/`opencode`), **version floors** (`java` 21+, `helm` 4.x, `docker` with Compose
  v2, `python3` 3), and **repo state** (git hooks installed via `scripts/install-git-hooks.sh`, Docker daemon reachable)
  — the setup steps beyond a bare install.
- Report a summary and exit non-zero when a required prerequisite is missing, so the script is usable as a check.
  Optional prerequisites (Helm/Snyk/cluster are deployment- and security-only) are reported but do not fail by default.
- Add a thin `/check-tooling` OpenCode command that runs the script, interprets its output, and tells the user exactly
  what to install or do — no skill (a skill would restate the README table, a second un-gated copy of those facts).
- Add a README row/link for the doctor in the Getting Started prerequisites section, pointing at the script rather than
  duplicating its logic.

## Capabilities

### New Capabilities

- `showcase/quality/toolchain-check`: diagnosing the local toolchain — presence, version floors, and repo state — and
  reporting it with actionable guidance, without requiring the build to run.

### Modified Capabilities

None. The new `/check-tooling` command is covered by the `agents-auditor`'s existing directory scope
(`.opencode/commands/`), so no requirement changes: `showcase/quality/agent-skills` describes the audit by directory,
not by an enumerated command list, and adding a command is not spec-level behavior.

## Impact

- New files: `scripts/doctor.sh`, `scripts/test-doctor.sh`, `.opencode/commands/check-tooling.md`, the change's delta
  spec (`showcase/quality/toolchain-check`).
- Edited files: `README.md` (prerequisite pointer + Slash Commands row + the Prerequisites install column widened to one
  column per documented platform), `AGENTS.md` (a Prerequisites bullet describing the doctor), `docs/ideas.md` (the
  adjacent entry's refresh).
- No spec under `openspec/specs/` changes: the auditor's directory scope already covers `.opencode/commands/`.
- Cross-platform: the doctor detects the platform and prints platform-appropriate install guidance, falling back to
  naming the tool where the platform is unrecognized; no `brew`-only advice on Linux.
- No build, test, or deployment behavior changes: the doctor is not wired into `check` (it diagnoses the host, and the
  host is not an input to the build — a green `check` on one machine says nothing about another). No new dependencies;
  the script is stdlib-only shell, compatible with macOS's system `/bin/sh`.
- `docs/ideas.md`: the change's own idea ("state once where agent-only tooling lives") is adjacent but separate; the new
  script is a `scripts/` tool, consistent with the existing convention.
