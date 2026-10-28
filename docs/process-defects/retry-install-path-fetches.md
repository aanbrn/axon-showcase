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
