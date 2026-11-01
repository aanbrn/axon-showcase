# refresh-upstream-references-2026-11-01 — process defects

### refresh-upstream-references-2026-11-01#1 — The new tree was set up before inheriting the primary's Python selection

- **Recorded** — 2026-11-01, main agent session.
- **Stage** — implementation.
- **Expected** — the `setup-idea` skill's initialization step 1 copies the primary checkout's `.python-version` into a
  created tree before step 2 runs `./scripts/setup-idea.sh` there.
- **Observed** — the script was run first, on a tree carrying no `.python-version` (the file is git-ignored, so a new
  worktree never has one). `python3` resolved to 3.12.1 through pyenv's shims, and the Python stage reported the
  interpreter as unregistered, printing the SDK-registration remedy for a version the primary does not use.
- **Evidence** — the first run's stage line
  `Python: NEEDS_ACTION — this tree's interpreter is not registered as a Python SDK.`; after copying `3.12.16` and
  re-running, `Python: CONFIGURED — wrote module and registration`, with the interpreter
  `/Users/afanasyev/.pyenv/versions/3.12.16/bin/python3`.
- **Consequence** — one extra setup run and a NEEDS_ACTION block that read as a host problem rather than a missing
  inheritance step; the tree was IDE-ready after the second run.
- **Knowledge boundary** — both runs were observed in this session. Not checked: whether the script could read the
  primary's selection itself rather than relying on the caller's copy.

### refresh-upstream-references-2026-11-01#2 — The `Packs:` classification line was never stated

- **Recorded** — 2026-11-01, main agent session.
- **Stage** — implementation.
- **Expected** — one `Packs:` line naming every subject pack loaded in full, or `Packs: none` with its reason, stated
  before the unit's first edit.
- **Observed** — the classification was decided (no row of the table matched a docs edit to `AGENTS.md`,
  `docs/ideas.md`, and a command definition) and carried into the reviewer hand-over, but no such line was written
  before the edits.
- **Evidence** — the session's messages before the first `edit` call carry no `Packs:` line; the round-1 reviewer
  hand-over is the first place the classification appears.
- **Consequence** — none material: no pack's subject was decided, and the reviewers were told the classification. The
  obligation itself was skipped rather than satisfied.
- **Knowledge boundary** — observed in this session's own transcript. Not checked: whether a reader of the pull request
  would have needed the line to audit the classification.
