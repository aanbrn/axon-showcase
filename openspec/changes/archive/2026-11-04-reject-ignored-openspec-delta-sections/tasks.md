# Tasks — reject OpenSpec content the CLI would drop at archive

The unit's record is `docs/units/reject-ignored-openspec-delta-sections/unit.md`.

## 0. The pre-write pass

- [x] 0.1 The review gate's pre-write derivation pass ran, seeded with `scripts/claim-scan.py`; each candidate is
      resolved into the claims table below or dismissed beside it. Dismissals, one reason per kind: `absolute` — the
      flagged words are this proposal's and the design's own statements, each backed by a row; `count` — the figures are
      the table's rows; `path` — the paths are this change's artifacts and the files it edits.

## 1. The probe that settles the premise

- [x] 1.1 On the pinned CLI (1.16.0), a delta whose only section is `## DEPRECATED Requirements` makes
      `openspec validate --all` exit 1 with `No delta sections found`, and `openspec archive --yes` abort with
      `Delta parsing found no operations … No files were changed.`
- [x] 1.2 The same malformed section beside one valid `## ADDED Requirements` makes `openspec validate --all` exit 0
      with both warnings, and `openspec archive --yes` exit 0 while the malformed requirement is dropped (its marker
      absent from the main spec afterwards).
- [x] 1.3 An unowned prose paragraph beside a valid `## ADDED Requirements` makes `validate --all` exit 0 with
      `Text above the first "## " section is not carried into the main spec by archive`, and `archive --yes` drops it.
      The enforced class is content the CLI would drop, not only a malformed section.

## 2. The guard

- [x] 2.1 `scripts/verify-openspec-delta-guard.py`: `--validate-output <file>` fails on a line matching "not a delta
      section" or "not carried into the main spec by archive", printing the matched line, and exits 0 otherwise;
      `--probe` builds four fixtures under a temporary directory (the three loss-producing shapes and a fully valid
      control), runs the pinned CLI, requires the valid control to validate and archive cleanly, counts a non-zero exit
      as the upstream fix only when the CLI's rejection diagnostic is present, notes retirement only when both loss
      shapes are rejected, and exits 3 with a stated reason when the CLI is absent.
- [x] 2.2 `scripts/test-verify-openspec-delta-guard.py`: the predicate over recorded outputs — both drop shapes, the
      corpus's long-requirement warning, a clean run — and the fixtures' content; exits non-zero on any mismatch.
- [x] 2.3 `build.gradle.kts`: register `testOpenSpecDeltaGuard` as a `verification` `Exec` task over the script and its
      test, in the shape `testCommitHygiene` uses, and add it to `check`'s `dependsOn` list.
- [x] 2.4 `.github/workflows/ci.yml`, the "Run quality gates" step: capture `openspec validate --all` to
      `$RUNNER_TEMP/openspec-validate.log`, exit with the captured output printed when validation itself fails, then run
      the script's `--validate-output` and `--probe` — placed before the configuration probe so the failure names the
      validation output.

## 3. The specification

- [x] 3.1 The change's delta: `MODIFIED` blocks on "Pull requests run the fast quality gate" and "Pushes to main run the
      full quality gate", each adding the verification that the validation output carries no content the CLI would drop
      at archive, with a scenario naming both shapes.

## 4. Report the gap upstream and record its retirement

- [x] 4.1 Draft the reproduction — the mixed fixture and the prose fixture, each with `validate --all`'s exit 0 and the
      content absent after `archive --yes` — run it past `review-quick` as an outward-facing artifact, and file it at
      `Fission-AI/OpenSpec` (`openspec` is published from that repository as `@fission-ai/openspec`). Check the venue
      accepts issues before posting.
- [x] 4.2 Record the issue reference **as `Fission-AI/OpenSpec#<n>`** — the form the report extracts, not a URL or a
      Markdown link — with its `retires when` condition, in `AGENTS.md`'s Continuous Integration section, the durable
      file the `upstreamReferences` report scans; the guard is retired once the pinned CLI rejects both shapes itself,
      which the probe reports.

## 5. Withdraw the parked entry

- [x] 5.1 `docs/ideas.md`, the `## 2026-10-18` section: remove only the "Widen `ci.yml`'s OpenSpec probe to fail on the
      CLI's 'is not a delta section' warning" bullet. The heading stays, because the section's sibling entry ("Assert
      the config's cross-artifact clause parity in a committed check") remains parked.

## 6. Verify

- [x] 6.1 `openspec validate --all` passes; `./gradlew spotlessApply` and `spotlessCheck` run after the last edit;
      `workflowLint` passes; the new test task runs in `check`.
- [x] 6.2 The change's own `build` run exercises the new step on the good shape, and `--probe` passes where the CLI is
      installed. Observed in pull request #840's `build` run (2m49s, green) with the new step's log carrying the
      validate summary.
- [x] 6.3 The review gate runs over the change.

## Claims table

Each source is read at the unit's base revision `1adfc2ce`, so the rows keep reproducing after this change edits the
file they name.

| Claim                                                                                     | Source or command                                                                                             | Result                                                                                                                                                   |
| ----------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------- |
| The pinned CLI is 1.16.0 in every workflow that installs it                               | `git grep -n "@fission-ai/openspec@" 1adfc2ce -- .github/workflows`                                           | `3` hits — `audit.yml`, `ci.yml`, `opencode.yml` — each `@1.16.0`                                                                                        |
| The gate runs `openspec validate --all` as a bare line today                              | `git show 1adfc2ce:.github/workflows/ci.yml \| grep -n "openspec validate --all"`                             | `104` (the gate) and `109` (a comment inside the config probe)                                                                                           |
| The corpus emits long-requirement warnings on every run, so a generic match would misfire | `openspec validate --all \| grep -c "very long (>500 characters)"`                                            | `91` warnings across the corpus, and no ignored-delta warning                                                                                            |
| A malformed-only delta already fails validation                                           | the probe: a throwaway copy of `openspec/`, a change whose only delta section is `## DEPRECATED Requirements` | `validate --all` exit `1`, `No delta sections found`; `archive --yes` exit `1`, `Aborted. No files were changed.`                                        |
| A mixed delta passes validation and loses the malformed requirement at archive            | the same probe with one valid `## ADDED Requirements` beside it                                               | `validate --all` exit `0` with both warnings; `archive --yes` exit `0`, `+ 1 added`, the marker count `0` in the main spec afterwards                    |
| Unowned prose is dropped the same way                                                     | the same probe with a prose paragraph beside a valid section                                                  | `validate --all` exit `0`, `Text above the first "## " section is not carried into the main spec by archive`; `archive --yes` exit `0`, the prose absent |
| The `## 2026-10-18` section holds a sibling entry, so the heading stays                   | `git show 1adfc2ce:docs/ideas.md \| sed -n '/^## 2026-10-18/,/^## /p' \| grep -c '^- '`                       | `2` entries                                                                                                                                              |
| `openspec` is published from `Fission-AI/OpenSpec`                                        | `npm view @fission-ai/openspec repository.url`                                                                | `git+https://github.com/Fission-AI/OpenSpec.git`                                                                                                         |
| A bare capture under `bash -e` swallows a failing command's output                        | `bash -ec 'x=$(false 2>&1); echo "$x"; echo after'`                                                           | prints nothing and exits 1, so the step captures with `if ! ... \| tee` to keep the diagnostic                                                           |
