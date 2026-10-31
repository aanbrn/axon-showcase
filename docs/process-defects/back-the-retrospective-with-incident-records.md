# Process defects — back-the-retrospective-with-incident-records

Process defects observed while working this unit. Recorded under `AGENTS.md`'s "Workflow: Record process defects".

### back-the-retrospective-with-incident-records#1 — A scripted task edit wrote an outcome note under the next section heading

- **Recorded** — 2026-10-31, by the writing session (`ses_e6f985602ffeUDJ4DtV0zmSDfc`), during the implementation stage.
- **Stage** — implementation.
- **Expected** — a note appended to a `tasks.md` item stays inside that item's block, before the blank line that ends
  the list.
- **Observed** — the script that ticked 6.1–6.5 appended 6.5's `Outcome` lines after `## 7. Review and records`, because
  its insertion point was the next line starting `- [`, which crossed the heading and the blank line rather than ending
  the item.
- **Evidence** — the file's state after the script: the `## 7. Review and records` heading followed immediately by the
  three `Outcome:` lines, and 6.5 without them.
- **Consequence** — one corrective edit; no artifact shipped with the misplaced note, because the result was read after
  the script ran.
- **Knowledge boundary** — observed the misplacement and its repair. Not checked: whether any other scripted edit in
  this unit derived a boundary across a heading.

### back-the-retrospective-with-incident-records#2 — A shell test the build runs through `sh` required bash, and a green local `check` did not show it

- **Recorded** — 2026-10-31, by the writing session (`ses_e6f985602ffeUDJ4DtV0zmSDfc`), during the review stage.
- **Stage** — review (round 1's finding).
- **Expected** — a test registered with the build's `shExecutable` runs under the runner's `/bin/sh`, which is dash on
  the Linux image, so the script stays POSIX.
- **Observed** — the new test began `#!/usr/bin/env bash` with `set -euo pipefail`; `check` passed locally (macOS
  `/bin/sh` is bash 3.2), and `dash scripts/test-experience-analysis.sh` exited 2 with
  `set: Illegal option -o pipefail`.
- **Evidence** — the dash run's message and exit status; the sibling `test-doctor.sh` runs its `sh`-invoked test with
  `set -eu`.
- **Consequence** — one review round; the first pull-request `build` run would have failed. Fixed by `#!/bin/sh` and
  `set -eu`, verified under `dash` and `sh`.
- **Knowledge boundary** — observed the dash failure and the fix. Not checked: the Linux CI job itself, which the dash
  run stands in for.

### back-the-retrospective-with-incident-records#3 — The new integration test required a host tool the default path's prerequisites exclude

- **Recorded** — 2026-10-31, by the writing session (`ses_e6f985602ffeUDJ4DtV0zmSDfc`), during the review stage.
- **Stage** — review (the defect was introduced at implementation and found by the advisor's implementation review).
- **Expected** — a check in the default build-and-test path runs on a host that satisfies the documented prerequisites
  only (Java, Docker with Compose, Python 3.12+, and `actionlint`), and adds no other executable to that path.
- **Observed** — the new shell test's `gh` stub invoked the external `jq` binary and guarded on `command -v jq`. The
  local host has `jq` installed, so `check` passed here, and the dependency was found by the advisor's review rather
  than by a gate.
- **Evidence** — the stub's `printf '%s' "$payload" | jq -r "$filter"` and its `command -v jq` guard before the fix;
  `AGENTS.md`'s prerequisite list and the doctor's probe table name no `jq`, and the production digest uses the `gh`
  CLI's built-in `--jq`.
- **Consequence** — one review finding; fixed by emulating the two filter shapes the digest uses (`length` and
  `.[] | …`) in `sh`, with an unexpected shape failing the stub. A `PATH` shim whose `jq` exits 127 leaves the test
  green; the full Docker-free `check` then passed on the refreshed tree.
- **Knowledge boundary** — observed the local run passing with `jq` installed, and the shim standing in for its absence;
  did not run on a host that has no `jq` binary at all.
