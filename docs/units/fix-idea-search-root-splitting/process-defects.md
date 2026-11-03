# Process defects — fix-idea-search-root-splitting

Process defects observed while working this unit. Recorded under `AGENTS.md`'s "Workflow: Record process defects".

### fix-idea-search-root-splitting#1 — The probe reproduction ran under the agent's shell, not the script's

- **Recorded** — 2026-11-03, by the writing session (`ses_e6185b401ffe8Ig0AYWmY1TigN`), during the proposal stage.
- **Stage** — proposal (the ShellCheck probe that surfaced this unit's fix).
- **Expected** — reproduce a shell script's word-splitting behavior under the shell that script declares
  (`#!/usr/bin/env bash`), since the defect is the shell's expansion, not the caller's.
- **Observed** — the first reproduction ran in the agent's own interactive shell, where an unquoted stored value is not
  word-split, so `IDEA_SEARCH_ROOTS='/Users/Test User/Applications:/Applications'` printed as a single word and the
  "confirmed problem" read as non-reproducible.
- **Evidence** — the first run printed `word: [/Users/Test User/Applications /Applications]`; the same snippet under
  `bash` printed `word: [/Users/Test]` and `word: [User/Applications]`. A ShellCheck scan in the same shell also passed
  its whole newline-separated file list as one argument and reported `openBinaryFile: does not exist` for it.
- **Consequence** — the first result would have recorded the defect as unreproducible and could have closed the fix as a
  non-issue. Repaired in the same session by re-running under `bash` before any finding was written down; nothing
  shipped from the wrong run.
- **Knowledge boundary** — observed these two outputs. Assume the difference is the interactive shell's handling of
  unquoted parameter expansion, which was not read from its documentation or probed further; did not try other shells.

- **Decision** — no-change: the wrong-resolution-context rule already covers it ("The binary is part of that context");
  the advisor declined an interpreter clause as a restatement of that obligation.

### fix-idea-search-root-splitting#2 — A negative control mutated its own restore point

- **Recorded** — 2026-11-03, by the writing session (`ses_e6185b401ffe8Ig0AYWmY1TigN`), during the implementation.
- **Stage** — implementation (the review round's `test-control` finding).
- **Expected** — a negative control that mutates the production script to prove a new test case discriminates leaves the
  fixed script in place afterwards, restored from a copy taken before any mutation.
- **Observed** — the control copied the fixed script to a backup and then mutated **that backup** in place, so the
  "restore" copied the mutated variant back over the fixed script; the fix was gone from the working tree while the
  staged index still held it.
- **Evidence** — after the restore, `grep -n 'IFS=: read -r -a search_roots' scripts/setup-idea.sh` printed nothing, and
  the file held `search_roots="$(printf '%s' … | tr ':' ' ')"` with `for root in "$search_roots"`.
- **Consequence** — the fix was re-applied by hand in two edits and re-verified (`sh scripts/test-setup-idea.sh` →
  `all cases passed`, `bash -n`, `sh -n`, `shellcheck` → exit 0). Nothing was lost permanently and nothing was committed
  in the mutated state; the mutation had been read as the control's expected failure, so the loss surfaced only from the
  closing grep.
- **Knowledge boundary** — observed this sequence. Did not check whether any other verification in the same batch read
  the mutated tree; the suite run after the mutation failed as the control intended.

- **Decision** — proposed: applied as `check-and-test-authoring`'s restore-from-snapshot bullet, which verifies the
  restored content against the pre-mutation snapshot instead of a token grep.
