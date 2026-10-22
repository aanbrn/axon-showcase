# Design

## Context

The three pins belong to one subject because one check reports them: `toolingUpdates` reads the CLI versions pinned in
workflow files, so a bump that left two of its three rows unapplied would report them again on the next run and the
unit's own verification ("the check no longer reports the bumped coordinates") would fail.

The tracker issue is not the authority for which rows exist. Issue #308 listed only the OpenSpec row because its report
predates the Snyk and `pack` releases, so the unit's scope comes from re-running the check rather than from the issue's
body — the rule that a target is re-resolved before it is bumped.

The generated instruction files are regenerated rather than hand-edited: they carry a `generatedBy` stamp that records
the CLI version, and a hand-edit would leave the next `openspec update` producing a diff the repository had not
reviewed.

## Goals / Non-Goals

**Goals:**

- Apply every row `toolingUpdates` reports, each target re-resolved against its own lookup.
- Regenerate the instruction files with the pinned CLI, and state in the proposal what changed in them.

**Non-Goals:**

- Any other pin, dependency or chart: the four subjects the update checks named are separate units.
- The `@hookform/resolvers` 6.0.0 major the web UI's npm report now shows: a major bump is a decision, not an
  update-check row.

## Decisions

1. **All three pins in one unit.** They are one report's rows, one verification, and one class of change; splitting them
   would make each unit's check report the other two.
2. **Regenerate, do not hand-edit, the instruction files.** The generator owns their content and the `generatedBy`
   stamp; the diff is inspected and described rather than authored.
3. **`skip_specs: true`.** No capability spec states these values; the grep of `openspec/specs/` for the pin coordinates
   is the evidence, and the specs that name the files (the agent-skills spec's generated-file exclusions, the
   code-quality spec's markdown scope) state what a formatter does with them, not what they contain.
4. **The regenerated files' split is stated once**, in the proposal's What Changes; this design cites it rather than
   repeating the per-file list, and the claims row below carries its derivation.

## Risks / Trade-offs

- **Two of the three pins cannot be verified by this pull request's checks.** The Snyk and `pack` pins take effect only
  in a run that installs them. The Snyk pin is dispatched from the branch before the archive; the `pack` pin is verified
  by the next image build, and that follow-up is parked in `docs/ideas.md` with its revisit condition rather than left
  as a silent gap — dispatching a whole-cluster smoke to prove one CLI tag installs is not a proportionate pre-merge
  cost.
- **A patch release can still change generated content.** It did: the proposal's split records which regenerated files
  carry a template change, so the diff was read rather than assumed to be stamps only.

## Verification

- `./gradlew toolingUpdates` after the bump — reports "No tooling updates available." (the check that produced the
  finding no longer lists the bumped coordinates).
- `./gradlew workflowLint` — the seven edited workflows lint clean.
- `npx @fission-ai/openspec@1.14.3 update` then `git status`/`git diff` — the regenerated set and what changed in it.
- The config-read-path probe `ci.yml` runs, re-exercised against the pinned CLI: a healthy config reads its declared
  `rules` and `operations.*.guidance` with no warning, and each warning pattern still fires on a malformed one — a
  reworded warning would otherwise disable the guard silently while the pull request stayed green.
- `./gradlew spotlessApply`/`spotlessCheck`, `openspec validate --all`, and
  `./gradlew :check -PskipITs -Pcoverage.gate.enabled=false`.

## Claims

| Claim                                                      | Source                                                                                                                                                                                                                                                                                                                                                                                                                                              |
| ---------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| The three pins and their targets                           | `./gradlew toolingUpdates` before the bump; each target re-resolved by `npm view @fission-ai/openspec version`, `gh api repos/snyk/cli/releases/latest --jq .tag_name`, `gh api repos/buildpacks/pack/releases/latest --jq .tag_name`                                                                                                                                                                                                               |
| The check reports nothing after the bump                   | `./gradlew toolingUpdates` after the bump → "No tooling updates available."                                                                                                                                                                                                                                                                                                                                                                         |
| Each pin's homes                                           | `grep -rn "@fission-ai/openspec@" .github/workflows/*.yml` (3 files), `grep -rn -e "snyk-version:" -e "pack-version:" .github/workflows/*.yml` (1 + 3 files)                                                                                                                                                                                                                                                                                        |
| Eight generated files change; four carry a template change | `git status --porcelain` after the update; per-file hunks from `git diff -- .opencode/` — the four with a non-stamp hunk are both `opsx-*.md` commands (which carry no `generatedBy` field at all) and `openspec-{propose,explore}/SKILL.md`                                                                                                                                                                                                        |
| No capability spec states the pins                         | `grep -rn -e "openspec@" -e "snyk-version" -e "pack-version" openspec/specs/` → no match                                                                                                                                                                                                                                                                                                                                                            |
| The tracker named only the OpenSpec row                    | Issue #308's body, last updated 2026-10-17                                                                                                                                                                                                                                                                                                                                                                                                          |
| No `build.gradle.kts` change is needed                     | `grep -n -e "openspec-cli" -e "snyk-cli" -e "pack-cli" build.gradle.kts` → seven per-file entries; `pinFiles` lists exactly the seven edited workflows, so no pin sits in an unread file                                                                                                                                                                                                                                                            |
| No live document states the old pins                       | `git grep -n -e 1.14.2 -e v1.1308.1 -e 0.40.9 -- . ':!openspec/changes/archive'` → exactly the two `docs/audits/2026-10-19.md` lines and nothing else, so no live document states them                                                                                                                                                                                                                                                              |
| The Snyk and `pack` pins need a run that installs them     | The steps install them (`snyk/actions/setup` in `dependency-security.yml`, `setup-pack` in the three image workflows) and no local task does, so `workflowLint` cannot clear either — the reason 3.6 dispatches and 3.7 names a follow-up                                                                                                                                                                                                           |
| The pinned CLI still emits the warning text `ci.yml` greps | `openspec new change <probe>` then `openspec instructions proposal --change <probe> --json` with each malformation applied to `openspec/config.yaml`: a healthy config warns nothing; an unquoted item in `rules` and the same in `operations.apply.guidance` each yield "must be an array of strings"; a dropped closing quote yields "could not parse … Missing closing 'quote". The probe is removed and the config restored to a zero-line diff |

**Dismissals.** `scripts/claim-scan.py --base origin/main` returns candidates in four kinds, and the scanner fires on
shape rather than on claimhood: `[path]` on file and directory mentions and on command invocations, `[count]` on
numbers, `[absolute]` on words like `only`/`every`/`never`, and `[cross-ref]` on repository paths, `#NNN` references and
the bare `AGENTS.md`/`README.md`/`SECURITY.md` filenames. A candidate is a claim only where a claims row states it; the
rest are pointers a reader resolves against the repository — a path by the gate that reads it, a command by running it,
a number by the row that derives it — so the kinds are not enumerated member by member here.
