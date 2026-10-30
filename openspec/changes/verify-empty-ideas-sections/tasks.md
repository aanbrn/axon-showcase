# Tasks

## 1. The check

- [x] 1.1 In `scripts/commit-hygiene.py`, `find_empty_idea_sections(text)` reports each dated `## YYYY-MM-DD` section of
      `docs/ideas.md` that carries no `- ` entry, as its line and heading.
- [x] 1.2 `check_idea_sections(repo)` names each offender as `docs/ideas.md:<line> <heading>`, returns the count, and
      `--ideas-sections` dispatches to it.
- [x] 1.3 A file with no dated section passes; a missing file fails, naming it.

## 2. The wiring

- [x] 2.1 `build.gradle.kts` registers `verifyIdeasSections` like its siblings: `inputs.file` on the script and
      `docs/ideas.md`, `outputs.upToDateWhen { false }`,
      `commandLine(pythonExecutable, "scripts/commit-hygiene.py", "--ideas-sections")`.
- [x] 2.2 The `check` block depends on it.

## 3. The controls

- [x] 3.1 A fixture whose dated section carries no entry fails and names its line.
- [x] 3.2 A fixture whose sections carry entries passes.
- [x] 3.3 A fixture with no dated section passes, and a missing file fails.
- [x] 3.4 The entry point (`main([...])`) exits 1 and names the offender.
- [x] 3.5 The controls bite: disabling the predicate makes 3.1 and 3.4 fail; restored.

## 4. The file's own rule

- [x] 4.1 `docs/ideas.md`'s header states that removing a section's last entry removes the heading.
- [x] 4.2 `.opencode/commands/ideas.md` states the rule where a `drop` disposition is chosen, and points at the header's
      full text and the gate.

## 5. The records

- [x] 5.1 The unit's process-defect record.
- [x] 5.2 Record the absence claim as a claims-table row rather than prose; the row carries the command and its result.
- [ ] 5.3 The capability's `## Purpose` enumerates each build check this capability owns and a delta cannot carry one:
      add the ideas-section gate to it in the archive commit.
- [x] 5.4 Docs refresh: check `README.md`, `AGENTS.md`, and `docs/adr/` for an enumeration of the `check` members or of
      the ideas-file rule, and update what it states.

## 6. The claims

| Claim                                                          | Source                                                                                        | Result                                                                                                                                                                                            |
| -------------------------------------------------------------- | --------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| The gate's bad state is reachable and it fires                 | `verifyIdeasSections` run with an empty section planted before `## 2026-10-28`                | `Task :verifyIdeasSections FAILED`, naming `docs/ideas.md:28 ## 2026-10-28`                                                                                                                       |
| The live tree passes                                           | `./gradlew verifyIdeasSections`                                                               | `BUILD SUCCESSFUL`                                                                                                                                                                                |
| The file's own state has no emptied section                    | `git show b4b331f8:docs/ideas.md` in a scratch repo, run through `--ideas-sections`           | one offender, `docs/ideas.md:27 ## 2026-10-29` — the defect this change gates                                                                                                                     |
| The tests carry the controls                                   | `python3 scripts/test-commit-hygiene.py`; then `find_empty_idea_sections` neutered and re-run | `Ran 244 tests ... OK`; neutered: four of the new tests fail                                                                                                                                      |
| The mode joins the script's mode list                          | `MODE_FLAGS` against the parser's mutually exclusive group                                    | equal, asserted by `test_expect_without_the_mode_is_an_error_for_every_mode`                                                                                                                      |
| No gate read the ideas file's section shape before this change | `git grep -n 'docs/ideas.md' f7ca3050 -- build.gradle.kts scripts`                            | three files, each treating the file as a whole — the build's input list, the retrospective digest, and a routing test's path signal — none parsing its dated sections                             |
| The task is part of `check`                                    | `build.gradle.kts`'s `check` block                                                            | `dependsOn("verifyIdeasSections")`, and the Docker-free `check` ran the task                                                                                                                      |
| The delta describes what the code does                         | each scenario against a test or the build wiring                                              | the four behaviour scenarios map to `IdeaSectionTests` cases, the "standard check verifies" scenario to `verifyIdeasSections`'s `check` membership, and the missing-file scenario to its own case |

## 7. The review

- [x] 7.1 `review-quick`: the unit's rounds are recorded in `docs/review-rounds.tsv`.
- [x] 7.2 Deep-review routing: `REQUIRED`, signal `requirement-block-added`.
- [x] 7.3 `/review-thorough` ran and returned two findings, both applied: the new check joins the module docstring's
      mode enumeration and the parked idea's build-check enumeration; and a missing `docs/ideas.md` now fails, naming
      the file, rather than passing.
