# Decision record — adding ShellCheck to the project

A dated record of a feasibility probe and its decision. `openspec/changes/archive/2026-10-03-fix-shellcheck-scenario/`
corrected a spec scenario that promised ShellCheck coverage rather than installing the tool, and before this record
neither `docs/decisions/` nor `docs/ideas.md` carried a probe of the question. This record carries that probe and its
verdict. It binds no standing rule, which is why it lives in `docs/decisions/` rather than `docs/adr/`;
`docs/adr/README.md` states that boundary.

## The question

Should this repository install, document, and gate on ShellCheck?

The tool is not part of the project: `workflowLint` (`build-logic/src/main/kotlin/workflow-lint-conventions.gradle.kts`)
runs `actionlint` alone over `.github/workflows/*.yml` and passes no `-shellcheck` argument, `ci.yml` installs only
actionlint, `scripts/doctor.sh` has no probe for it, and `AGENTS.md` records the gate as actionlint alone. actionlint
consults `shellcheck` only when it happens to be on `PATH`, so the current coverage of `run:` blocks depends on the
machine.

A prior decision exists and was scoped, not final: `openspec/changes/archive/2026-10-03-fix-shellcheck-scenario/`
corrected a spec scenario that promised "a shell error that shellcheck reports", and its design lists "install and
document shellcheck" as a **rejected alternative for that change**, on the grounds that it adds a prerequisite and a CI
install for a tool actionlint uses opportunistically.

## The probe

- **Tool:** ShellCheck 0.11.0, as installed on the probing machine.
- **Revision:** every figure below is `c93359c2`'s — the revision the probe ran on, the unit's own base. Reproduce them
  against that revision (`git show c93359c2:<path>`) rather than the working tree, since the fix in this change moves
  two of the files and the counts with them.
- **Scope:** the tracked shell files at that revision — 17 `*.sh` outside `.opencode/`, plus the extensionless
  `scripts/git-hooks/pre-commit`, 2,574 lines in total. `gradlew` is generated and excluded. The set is reproducible
  with `git ls-files '*.sh' ':!:.opencode/*'` plus that hook.
- **Control:** a scratch script carrying a deliberate defect (an unquoted `echo $name` and `for f in $(ls /tmp)`) exits
  1 with `SC2045 (error)` and two `SC2086`, so a clean result below is a result and not an empty run.
- **Result at the default severity set:** 14 findings — 0 `error`, 4 `warning`, 10 `note`.
- **Result with `--enable=all`:** 1,035 findings, 842 of them `SC2250` (optional brace-style preference) over the same
  18 files — the 17 `*.sh` alone give 1,034 / 841 — so adopting the optional checks would require a suppression policy
  from the first day.

Every one of the 14 default findings is a deliberate idiom or a false positive. The line numbers are the probed
revision's:

| Code        | Site                                                                                                                  | Verdict                                                                 |
| ----------- | --------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------- |
| `SC2088`    | `scripts/doctor.sh:69` — `"~/.docker/cli-plugins/docker-compose"`                                                     | false positive: the tilde is advice text in an install hint, not a path |
| `SC1007` x2 | `scripts/new-worktree.sh:19,53` — `CDPATH= cd --`                                                                     | false positive: idiomatic environment prefix before a command           |
| `SC2209`    | `scripts/new-worktree.sh:33` — `mode=continue`                                                                        | false positive: `continue` is a shell builtin name                      |
| `SC2086` x4 | `scripts/doctor.sh:118,121,257` — `set -- $got`, `$want`, `$row`; `scripts/test-doctor.sh:253` — `printf … $compared` | deliberate word splitting of version arguments and a newline list       |
| `SC2012` x3 | `scripts/experience-analysis.sh:70`, `scripts/load-test-baseline.sh:60,108` — `ls -dt`                                | benign: `find` cannot sort by modification time here                    |
| `SC2016` x3 | `scripts/test-doctor.sh:96,341`, `scripts/test-setup-idea.sh:242`                                                     | deliberate: the fixtures assert on literal `$` and backticks            |

`scripts/doctor.sh` already carries a `# shellcheck disable=SC2086` for the same splitting idiom at line 103 and not at
the three sites above, so the surface is inconsistent in its own suppressions rather than defective.

### The one real defect is invisible to ShellCheck

`scripts/setup-idea.sh` converted `IDEA_SEARCH_ROOTS` from its colon-separated form to spaces (`tr ':' ' '`) and then
iterated the result unquoted, so a root containing a space was split into several roots. Reproduced by running the base
revision's own two lines under bash 3.2.57 — the shell `#!/usr/bin/env bash` resolves to on macOS — with
`/Users/Test User/Applications:/Applications`, which produced `/Users/Test` and `User/Applications`; the default value
reaches `$HOME`, so a home directory with a space broke launcher discovery too.

ShellCheck reports nothing on that file (exit 0). `for root in $search_roots` is the word-splitting idiom it tolerates
by design, and the correct repair is not the quoting it would suggest but an array. The tool therefore does not point at
the defect class this repository actually had, and a "fix" applied from its advice would have broken the intended
splitting.

## The decision

ShellCheck is **not** added — no prerequisite, no `check` member, no CI install, no spec delta.

The alternatives and why each is rejected:

- **A standalone gate over the repository's shell files** would cost a new `check` member whose scope must span the root
  scripts, `showcase-web-ui/scripts/`, and the extensionless hook; a required probe in `scripts/doctor.sh` with install
  hints and its own fixtures; a row in the README prerequisites table and an edit to the "only Java, Docker, Python and
  actionlint" claim in `AGENTS.md`; a CI install step; and a delta on `showcase/quality/code-quality`. That is a
  standing obligation bought for zero defects found, plus three suppressions for false positives.
- **Wiring `actionlint -shellcheck`** scans the `run:` blocks of the workflows and none of the 18 repository files, so
  it does not reach the defect this probe found.
- **Opportunistic adoption** — leaving the tool on whatever machine has it — is the current state and yields no
  dependable coverage; actionlint uses it only when present.
- **Status quo** is what the probe supports: 17 of the 18 shell files open with a `set` line — `-euo pipefail` or `-eu`
  in most of them, `-uo pipefail` in the three scripts that inspect a command's exit status, and a bare `-e` in
  `showcase-web-ui/start.sh` — with `scripts/doctor.sh` the exception, the default scan's `SC2086` hits are the
  deliberate splitting sites, and the one genuine defect was a design error in how a delimiter was consumed rather than
  a quoting slip. Five of the eighteen files — `scripts/doctor.sh`, `setup-idea.sh`, `new-worktree.sh`,
  `audit-report-completeness.sh`, and `experience-analysis.sh` — are exercised by the five `scripts/test-*.sh` suites
  that `check` runs; the rest are not.

No commit message touching `*.sh` mentions quoting or word splitting (`git log --grep` for `quote`, `quoting`,
`word split`, `unquoted` returns nothing), so the "ShellCheck would have caught our past bug" argument has no instance
behind it — a message-level search, which is weaker than a history of every shell fix and is stated as what was run.

The probe did produce one artifact worth keeping, and it is not ShellCheck: the search-root splitting in
`scripts/setup-idea.sh` is fixed in the change that carries this record, with a case in `scripts/test-setup-idea.sh`
that fails against the unfixed script.

## Reopening condition

Reopen when any one of these holds — the clauses are a disjunction, so any single one is enough:

- **An internal trigger:** a shell defect reaches `main` that `bash -n`, the `scripts/test-*.sh` suites, and the build's
  own checks would all have passed, and a ShellCheck run over the same revision reports it. The reproduction is the
  defect plus that run's output, not the tool's general reputation.
- **An external coordinate:** ShellCheck ships a check that reports the class this probe found it silent on — a
  delimiter converted into a word-splitting separator and then iterated unquoted (`for root in $search_roots`) — so a
  later scan would name the defect rather than tolerate the idiom. Checkable against the tool's own release notes and a
  re-run over `scripts/setup-idea.sh`'s pre-fix revision.

A later attempt should re-run the probe rather than cite this record's counts: the suppression inventory, the scripts,
and the tool's own checks all move.
