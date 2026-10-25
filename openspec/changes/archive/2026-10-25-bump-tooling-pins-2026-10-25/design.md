# Design

## Context

The two pins belong to one subject because one check reports them: `toolingUpdates` reads the CLI versions pinned in
workflow files, so a bump that left one of its two rows unapplied would report it again on the next run and the unit's
own verification ("the check no longer reports the bumped coordinates") would fail.

The tracker issue is the finding's report, not the authority for which rows exist: the unit's scope comes from
re-running the check, and issue #308 happens to name exactly the two rows it returned this time.

The generated instruction files are regenerated rather than hand-edited: they carry a `generatedBy` stamp that records
the CLI version, and a hand-edit would leave the next `openspec update` producing a diff the repository had not
reviewed.

## Goals / Non-Goals

**Goals:**

- Apply every row `toolingUpdates` reports, each target re-resolved against its own lookup.
- Regenerate the instruction files with the pinned CLI, and state in the proposal what changed in them.

**Non-Goals:**

- Any other pin, dependency or chart: the four subjects the update checks named are separate units.
- The `.opencode/package.json` `@opencode-ai/plugin` pin: OpenCode's startup install rewrote it in this worktree, which
  is installer state rather than a pin this unit bumps (the revert is decision 4).

## Decisions

1. **Both pins in one unit.** They are one report's rows, one verification, and one class of change; splitting them
   would make each unit's check report the other.
2. **Regenerate, do not hand-edit, the instruction files.** The generator owns their content and the `generatedBy`
   stamp; the diff is inspected and described rather than authored.
3. **`skip_specs: true`.** No capability spec states these values; the grep of `openspec/specs/` for the pin coordinates
   is the evidence, and `showcase/quality/merge-governance` specifies the `tooling-updates` workflow's behavior and its
   issue's report, not the versions the pin declares.
4. **Revert the `@opencode-ai/plugin` pin rather than keep it.** `openspec update` ran in a worktree whose OpenCode
   session installs the plugin at startup, which rewrote the tracked pin down (`1.19.4` → `1.19.3`); the file is
   installer state, its own pin is not this unit's subject, and keeping the rewrite would ship an unrelated downgrade.
5. **Regenerate with the pinned version.** The local CLI reports `1.15.0`, equal to the pin CI will install, so the
   stamped regeneration is the one the next `openspec update` under CI's CLI reproduces.
6. **Ship the capture narrowed, not as proposed.** The capture proposed a general `AGENTS.md` step requiring every bump
   to check the parked records it activates. The advisor's review measured the promotion evidence against both branches
   of the gate — two independent occurrences, or one severe verified incident with a clear preventive action — and found
   the threshold unestablished on the available materials. The first branch is unmet because one bump's record is silent
   on the check while the one before it ran the check, so the rule would rest on absence of a record rather than on a
   confirmed recurrence. This unit reads the second as unmet too: a missing check risks a config guard surviving past
   the bump that should retire it, but the periodic architecture audit reads the record at its next pass, so the delay
   is bounded and the incident is not severe. The obligation that does hold is the 2026-09-30 entry's own — it names the
   CLI bump as its check point — so the regeneration command points at that entry, `AGENTS.md` stays unchanged, and the
   pointer retires with the entry.

## Risks / Trade-offs

- **One of the two pins cannot be verified by this pull request's checks.** The Snyk pin takes effect only in a run that
  installs it, so it is dispatched from the branch before the archive; the OpenSpec pin is verified by the `build` gate
  itself, which installs it and validates the corpus.
- **A minor release can still change generated content.** This one did not: the diff is stamp-only across six skills,
  and no `opsx-*.md` command body changed, so the generated inventory is unchanged.

## Verification

- `./gradlew toolingUpdates` after the bump — reports "No tooling updates available." (the check that produced the
  finding no longer lists the bumped coordinates).
- `./gradlew workflowLint` — the four edited workflows lint clean.
- `openspec update` then `git status`/`git diff` — the regenerated set and what changed in it.
- The config-read-path probe `ci.yml` runs, re-exercised against the pinned CLI: a healthy config reads its declared
  `rules` and `operations.*.guidance` with no warning, and each warning pattern still fires on a malformed one — a
  reworded warning would otherwise disable the guard silently while the pull request stayed green.
- The parked config-guard retirement entry (`docs/ideas.md`, 2026-09-30) — its watch condition is checked on the CLI
  bump rather than on the weekly sweep, so this unit checks it: the pinned distribution carries neither
  `inspectProjectConfig` nor the `#1894`/`#1892` fixes, so the probe and the `/opsx-tool-update` re-verification stand
  and the entry stays live.
- `./gradlew spotlessApply`/`spotlessCheck`, `openspec validate --all`, and
  `./gradlew :check -PskipITs -Pcoverage.gate.enabled=false`.

## Claims

| Claim                                                                | Source                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| -------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| The two pins and their targets                                       | `./gradlew toolingUpdates` at `c149149d`; each target re-resolved by `npm view @fission-ai/openspec version` → `1.15.0` and `gh api repos/snyk/cli/releases/latest --jq .tag_name` → `v1.1308.3`                                                                                                                                                                                                                                              |
| The check reports nothing after the bump                             | `./gradlew toolingUpdates` after the bump → "Tooling updates:\nNo tooling updates available."                                                                                                                                                                                                                                                                                                                                                 |
| Each pin's homes                                                     | `grep -rn "@fission-ai/openspec@" .github/workflows/*.yml` → `ci.yml`, `opencode.yml`, `audit.yml`; `grep -rn "snyk-version:" .github/workflows/*.yml` → `dependency-security.yml`; the `toolingUpdates` registration declares one entry per file for each pin                                                                                                                                                                                |
| Six generated files change, each stamp-only; no command body changes | `git status --porcelain --untracked-files=all` after `openspec update`; `git diff -- .opencode/` → one `generatedBy: "1.14.3"` → `"1.15.0"` line per skill, and no `.opencode/commands/**` path in the diff                                                                                                                                                                                                                                   |
| No capability spec states the pins                                   | `grep -rn -e "openspec@" -e "snyk-version" -e "1\.1308" -e "1\.15\.0" -e "1\.14\.3" openspec/specs/` → no match                                                                                                                                                                                                                                                                                                                               |
| The tracker names exactly these rows                                 | Issue #308's body at its `2026-10-24T21:08:44Z` update                                                                                                                                                                                                                                                                                                                                                                                        |
| No `build.gradle.kts` change is needed                               | The `toolingUpdates` registration in `build.gradle.kts` already declares a `ToolingUpdateCheck` per pin per file, and `pinFiles` registers every workflow it reads                                                                                                                                                                                                                                                                            |
| No live document states the old pins                                 | `git grep -n -e "1\.14\.3" -e "v1\.1308\.2" -- . ':!openspec/changes/archive'` → no match                                                                                                                                                                                                                                                                                                                                                     |
| The Snyk pin needs a run that installs it                            | The step installs it (`snyk/actions/setup` with `snyk-version` in `dependency-security.yml`) and no local task does, so `workflowLint` cannot clear it — the reason the dispatch is a task                                                                                                                                                                                                                                                    |
| The pinned CLI still emits the warning text `ci.yml` greps           | `openspec new change <probe>` with each malformation applied to `openspec/config.yaml`: healthy → no warning, `rules` 9 items and `operationGuidance` 6 items via `openspec instructions`; an unquoted `rules` item containing `: ` and the same in `operations.apply.guidance` each → "must be an array of strings"; a dropped closing quote → "could not parse". The config was restored to a zero-line diff and the probe changes removed  |
| The final diff carries no `.opencode/package.json` change            | `git restore .opencode/package.json` after the rewrite; `git status --porcelain --untracked-files=all` lists no such path                                                                                                                                                                                                                                                                                                                     |
| The parked config-guard entry's retirement condition is still unmet  | `grep -rl inspectProjectConfig <1.15.0 dist>` → no match, with a positive control on the same tree (`grep -rl "array of strings"`, whose hits include the guard's own `dist/core/project-config.js`); `gh pr view 1894 -R Fission-AI/OpenSpec` → OPEN, `gh issue view 1892 -R Fission-AI/OpenSpec` → OPEN, and its latest release is `v1.15.0`                                                                                                |
| The narrowing rests on one recorded check and one silent record      | `grep -l 1894 openspec/changes/archive/2026-10-16-bump-tooling-pins-2026-10-16/*.md` → that change's `design.md` names `pulls/1894` open with the watch condition unmet; `grep -rn -e 1894 -e 1891 -e inspectProjectConfig -e 'config.guard' openspec/changes/archive/2026-10-22-bump-tooling-pins-2026-10-22/` → exit 1 and that change's capture record states no `AGENTS.md` proposal, which evidences silence rather than a skipped check |
| The workflows lint clean                                             | `./gradlew workflowLint` → BUILD SUCCESSFUL                                                                                                                                                                                                                                                                                                                                                                                                   |
| The change validates with no delta                                   | `openspec validate bump-tooling-pins-2026-10-25 --type change` → valid, `skip_specs` accepted                                                                                                                                                                                                                                                                                                                                                 |

**Dismissals.** `scripts/claim-scan.py --base c149149d` fires on shape rather than on claimhood, so each kind carries
one reason rather than a list of its candidates: `[path]` marks a file, directory or command mention, which the gate
that reads it or a run of the command resolves; `[count]` marks a numeral, which the row or task line that states it
derives; `[absolute]` marks a quantifying word, which a claims row carries when it spans a corpus and a task imperative
does otherwise; and `[cross-ref]` marks a repository path, an issue number, or a package scope, each resolved by the
tracker or lookup it names. A candidate is a claim only where a claims row states it, so the kinds are not enumerated
member by member here.
