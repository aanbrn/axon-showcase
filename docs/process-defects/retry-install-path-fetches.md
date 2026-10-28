# Process defects — retry-install-path-fetches

### retry-install-path-fetches#1 — A gate run stopped at the new file's formatting

- **Recorded** — 2026-10-28, by the writing session (`ses_e832e4043ffe6ADYTEa13DvV1s`), during the implementation stage.
- **Stage** — implementation.
- **Expected** — a touched file is formatted before the gate that verifies formatting runs, so that gate's verdict
  covers the change rather than a formatting diff the formatter would settle by itself.
- **Observed** — `build-logic/src/main/kotlin/UpdateHelmRepositoriesTask.kt` was written with the field's Javadoc on
  three lines; `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` ran before `spotlessApply`, and
  `:spotlessKotlinCheck` failed on that file.
- **Evidence** — the gate's output named the file and the replaced hunk (`-····/**·…·*/` /
  `+····/**·The·repositories·to·register·and·refresh,·by·name.·*/`); `./gradlew spotlessApply` then left `spotlessCheck`
  green. The `check` run after that one failed for a cause `#2` records as unattributed, and a later run passed.
- **Consequence** — the formatting was fixed from the formatter's own output, and the gate was run again until it
  passed. Nothing reached the repository.
- **Knowledge boundary** — one case observed in this unit. Whether the same ordering costs other units a run was not
  checked, and the convention states the format step relative to reporting the unit done rather than relative to running
  the gate.

### retry-install-path-fetches#2 — A failed gate run's diagnostics were discarded before the run was repeated

- **Recorded** — 2026-10-28, by the writing session (`ses_e832e4043ffe6ADYTEa13DvV1s`), during the implementation stage.
- **Stage** — implementation.
- **Expected** — a gate run's own output is read before it is repeated, so a failure is attributable to what it reported
  rather than to the run that happened to pass afterwards.
- **Observed** — the `check` run following the formatting fix ended `BUILD FAILED in 1m 36s`; the command piped the run
  through `tail -3`, so the failure text was not retained. The next invocation repeated `check` filtered to
  `What went wrong`, matched nothing (that run passed), and the run after it reported `BUILD SUCCESSFUL` from a reused
  configuration-cache entry.
- **Evidence** — the failed run's whole retained output is its last three lines: `BUILD FAILED in 1m 36s`,
  `264 actionable tasks: 101 executed, 33 from cache, 130 up-to-date`, `Configuration cache entry stored.` No later
  artifact carries the failure's task or message.
- **Consequence** — the failure is unattributed: no record names the task that failed or why, and the passing re-run
  does not decide whether this change caused it. The pull-request gate is the verification that remains.
- **Knowledge boundary** — one case observed. The failure's cause was not investigated, and no attempt was made to
  reproduce it.

### retry-install-path-fetches#3 — A local merge of the base was refused for a change directory the base carried

- **Recorded** — 2026-10-28, by the writing session (`ses_e832e4043ffe6ADYTEa13DvV1s`), during the ship stage.
- **Stage** — ship.
- **Expected** — an open pull request whose base advanced is refreshed by `gh pr update-branch`, the procedure's route
  once a pull request is open; this unit's assignment is `none`, and its own staged set carries no path under
  `openspec/changes/`.
- **Observed** — `gh pr update-branch 725` reported `Cannot update PR branch due to conflicts`. The agent then ran a
  local `git merge origin/main`, which conflicted only in `docs/review-rounds.tsv`, and after that resolution was
  staged, `git commit --no-edit` was refused for paths under
  `openspec/changes/archive/2026-10-28-fail-closed-commit-hygiene-reads/`, which the merge brought from the base rather
  than from this unit's edits.
- **Evidence** — the guard's stderr carried one line per staged path under that archive directory:
  `Path staged under a change directory, but the workspace declares none (…)` — the ellipsis standing for the claim file
  and its `records 'none'` clause — followed by the path; the last three named `proposal.md`,
  `specs/showcase/quality/commit-hygiene/spec.md` and `tasks.md`. Before the merge the staged set held only this unit's
  six paths.
- **Consequence** — the merge was aborted and the branch refreshed by `git rebase origin/main` with the same conflict
  resolution re-applied; the branch's single commit was rewritten and force-pushed (`--force-with-lease`), the pull
  request's CI re-ran green, and the merge completed. No content was lost.
- **Knowledge boundary** — one case observed, for a unit whose assignment is `none` and whose base advanced with an
  archived change directory. The guard's rule matches `showcase/quality/commit-hygiene`'s "When the assignment is
  `none`, any path under `openspec/changes/` SHALL fail", so the refusal reproduces the specified behavior rather than a
  defect in it; whether a change-owning unit's merge behaves differently, and whether the refresh procedure intends the
  merge route for this unit shape, was not established.
