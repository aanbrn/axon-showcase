# Design

## Context

`toolingUpdates` reports the CLI versions pinned in workflow files, and its report is the unit's scope rather than the
tracker issue: issue #308 names the rows for the one coordinate it returned — the OpenSpec CLI pin in `ci.yml`,
`opencode.yml` and `audit.yml` — and re-resolving the target with `npm view @fission-ai/openspec version` returned
`1.16.0`, so nothing moved since the issue's report.

The pin carries a second consequence this unit inherits. `AGENTS.md`'s "Track the upstream reports that would retire
this project's config guards" bullet records that `Fission-AI/OpenSpec#1892`'s fix shipped in `v1.16.0`, where
`openspec validate` exits 1 on a config it cannot parse, so the CI probe's `could not parse` arm retires when the pin
reaches that release. The pinned CLI is now exactly that release, so the arm's removal is part of applying this row, not
an unrelated cleanup.

## Goals / Non-Goals

**Goals:**

- Apply the one coordinate the report names, re-resolved against its own lookup.
- Regenerate the instruction files with the pinned CLI and describe what changed in them.
- Retire the probe arm whose condition the pin's move meets, keeping the arm whose upstream fix is unmerged.

**Non-Goals:**

- Any other pin, dependency or chart: the Helm (#40), buildpack (#211), dependency (#13) and upstream-reference (#343)
  findings are separate units.
- `Fission-AI/OpenSpec#1894` (the rules-array half): it is open and unmerged, so the list-shape arm and the
  `/opsx-tool-update` re-verification stay.
- The `.opencode/package.json` `@opencode-ai/plugin` pin: the regeneration did not rewrite it in this working tree, and
  it is installer state rather than a pin this unit bumps.

## Decisions

1. **One unit, one coordinate.** The report carries that coordinate across three rows, so the check's own verification
   ("the bumped coordinates no longer appear") closes it; splitting the probe retirement out would leave a unit whose
   guard described a CLI the pin no longer installs.
2. **Regenerate, do not hand-edit, the generated skills.** The generator owns their content and the `generatedBy` stamp,
   so the diff is inspected and described rather than authored.
3. **Drop only the `could not parse` arm.** Under `1.16.0` the gate's own `openspec validate --all` runs before the
   probe and exits 1 on an unparseable config, so the arm cannot be the mechanism that catches that shape any more; the
   list-shape arm stays because `validate` does not fail on it and `#1894` is unmerged.
4. **Keep the whole-file control in `/opsx-tool-update`, aimed at its new mechanism.** The control now asserts
   `openspec validate --all` exits non-zero, so a release that stopped failing on an unparseable config surfaces at the
   next bump rather than reopening the hole silently.
5. **`skip_specs: true`.** `git grep -n -e "@fission-ai/openspec@" -e "1\.15\.0" -- openspec/specs/` returns no match,
   so no capability spec states the pin. `showcase/quality/merge-governance` specifies the config check's outcomes, and
   both remain met (decision 3 and the probe replay).

## Risks / Trade-offs

- **The two cloud workflows cannot be verified by this pull request.** `opencode.yml` and `audit.yml` install the same
  pin, and their next comment-triggered or scheduled run is its first execution there; the `build` gate verifies the pin
  itself by installing it and validating the corpus.
- **The retirement rests on `validate`'s exit status.** A later CLI that stopped failing on an unparseable config would
  reopen the shape silently, which is why decision 4 keeps a positive control over it in the bump procedure.

## Verification

- `./gradlew toolingUpdates` after the bump — "No tooling updates available.".
- `./gradlew workflowLint` — the three edited workflows lint clean.
- `openspec update` then `git status`/`git diff` — the regenerated set and what changed in it.
- The config probe, replayed against `1.16.0` exactly as `ci.yml` runs it: a healthy config passes `validate` and the
  grep does not match; an unquoted `rules` item and an unquoted `operations.apply.guidance` item each keep `validate` at
  exit 0 and make the probe print "must be an array of strings"; an unparseable config makes `validate` exit 1, so the
  gate fails before the probe. `openspec/config.yaml` was restored to a zero-line diff after each control.
- `./gradlew spotlessApply`/`spotlessCheck`, `openspec validate --all`, and
  `./gradlew :check -PskipITs -Pcoverage.gate.enabled=false`.

## Claims

| Claim                                                                | Source                                                                                                                                                                                                                                                   |
| -------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| The report's one coordinate, its three rows, and its target          | Issue #308's body (`openspec-cli (ci.yml\|opencode.yml\|audit.yml): 1.15.0 -> 1.16.0`) and `npm view @fission-ai/openspec version` → `1.16.0`                                                                                                            |
| Each home of the pin, and no fourth                                  | `git grep -n "npm install --global @fission-ai/openspec@" -- .github/workflows` → `ci.yml`, `opencode.yml`, `audit.yml`; `build.gradle.kts`'s `toolingUpdates` registration declares one `ToolingUpdateCheck` per file and registers every file it reads |
| The check reports nothing after the bump                             | `./gradlew toolingUpdates` then `cat build/tooling-updates/report.txt` → "No tooling updates available."                                                                                                                                                 |
| Six generated files change, each stamp-only; no command body changes | `git diff --numstat -- .opencode/skills/openspec-*` → `1 1` for each of the six; `git diff -- .opencode/` shows one `generatedBy` line per file and no `.opencode/commands/**` path                                                                      |
| The regeneration moved no other file                                 | `git status --porcelain --untracked-files=all` lists no `.opencode/package.json` path                                                                                                                                                                    |
| `validate` exits 1 on an unparseable config under `1.16.0`           | Probe replay: a dropped closing quote → `openspec validate --all` exit 1, `Error: Could not parse … Missing closing 'quote`                                                                                                                              |
| The remaining grep fires on both list-shape surfaces                 | Probe replay: `openspec new change <probe>` output matches `must be an array of strings` for an unquoted `rules` item and for an unquoted `operations.apply.guidance` item, with `validate` at exit 0 in both                                            |
| The retired arm's shape is refused before the probe                  | Probe replay: the unparseable config exits 1 on `openspec validate --all`, which the gate runs before `openspec new change`; the healthy config exits 0 with no grep hit                                                                                 |
| No capability spec states the pin                                    | `git grep -n -e "@fission-ai/openspec@" -e "1\.15\.0" -- openspec/specs/` → no match                                                                                                                                                                     |
| No live file states the old pin or stamp                             | `git grep -n -e "openspec@1\.15\.0" -e 'generatedBy: "1\.15\.0"' -- . ':!openspec/changes/archive'` → no match                                                                                                                                           |
| The workflows lint and the tree formats                              | `./gradlew workflowLint` → BUILD SUCCESSFUL; `./gradlew spotlessApply` then `spotlessCheck` → BUILD SUCCESSFUL, run after the last edit to a formatter-owned file                                                                                        |
| The change validates with no delta                                   | `openspec validate --all` → `0 failed`; the item count includes this change directory, so no count is recorded here                                                                                                                                      |

**Dismissals.** `scripts/claim-scan.py` run at `521a93ee` — the base this unit began on, before the branch was
fast-forwarded; a run at the current base also scans the merge since — fires on shape rather than on claimhood, so each
kind carries one reason rather than a list of its candidates: `[path]` marks a file, directory, coordinate or command
mention, which the gate that reads it or a run of the command resolves; `[count]` marks a numeral, which the row or task
line that states it derives; `[absolute]` marks a quantifying word, which a claims row carries when it spans a corpus
and a task imperative does otherwise; and `[cross-ref]` marks a repository path, an issue number, or a package scope,
each resolved by the tracker or lookup it names. A candidate is a claim only where a claims row states it, so the kinds
are not enumerated member by member here.
