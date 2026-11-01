# Process defects — bump-kps-chart-2026-11-01

### bump-kps-chart-2026-11-01#1 — No process defect observed

- **Recorded** — 2026-11-01, by the implementing agent.
- **Stage** — implementation.
- **Expected** — a defect of the rules or the plan would be recorded here as it was observed.
- **Observed** — no process defect observed in this unit's implementation: the workspace tree was created and prepared,
  the check resolved one row, both pin sites were moved, and the gates were run as the plan named them. One artifact
  outside the unit appeared — `showcase-web-ui/package-lock.json` carried a `devOptional` → `dev` normalization on two
  entries right after the tree was created, with `node_modules` written at the same instant — and it was reverted
  unstaged, not committed.
- **Evidence** — `git status --porcelain` after `git restore showcase-web-ui/package-lock.json` listed no third modified
  path; `./gradlew verifyInstallCommands` and `./gradlew helmUpdates` were both run afterwards and left that file
  untouched.
- **Consequence** — none in the unit: the file was restored to its committed content before any review round, and no
  commit carries it. What still stands is that an unidentified writer ran a package install in a freshly created tree.
- **Knowledge boundary** — observed: the file's content change, its timestamp, and that the two Gradle runs after the
  restore did not reproduce it. Not checked: which process wrote it, so the trigger stays unidentified rather than
  attributed.

### bump-kps-chart-2026-11-01#2 — A reply switched to a language the owner never used

- **Recorded** — 2026-11-01, by the implementing agent, after the owner reported it.
- **Stage** — ship: the unit was already committed, pushed and opened as pull request #787.
- **Expected** — a reply the owner reads uses the language of the owner's current request, Russian, per the
  output-language convention.
- **Observed** — the turn reporting pull request #787 was written in Japanese ("PR 開きました …"), a language this
  conversation never used.
- **Evidence** — the owner's verbatim reply: "Это что за иероглифы?".
- **Consequence** — one unreadable message and one owner round trip; restated in Russian in the next turn. Nothing in
  the repository or the pull request was affected.
- **Knowledge boundary** — observed: the message's language and the owner's response to it. Not checked: which step
  produced the switch, so the cause stays unidentified rather than attributed.
