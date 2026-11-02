# Review hand-over

## Round 1 — implementation

- **Unit**: `classify-actionlint-required`; **base** `ee7bdc588c1420cf87ee900e33b040e5d9981c25`; **state**: uncommitted
  working tree, staged, no commit; **tree**: `/Users/afanasyev/Development/axon-showcase-classify-actionlint-required`;
  **branch**: `classify-actionlint-required`.
- **Supplied artifacts**: the full diff against the base — `scripts/doctor.sh`, `scripts/test-doctor.sh`,
  `docs/ideas.md` — and the change dir: `proposal.md`, `design.md` (Context, Goals/Non-Goals, five decisions with
  rejected alternatives, Risks, a sixteen-row claims table, per-kind dismissals), `tasks.md` (with the `Workspace:`
  record), and the delta spec.
- **Stage**: implementation. The change is small enough that its planning artifacts and its implementation were written
  in one turn; no separate proposal round ran, so the screen covers both.
- **Packs**: none — the subject is a POSIX-shell probe declaration and a spec delta, not a Gradle decision, JVM code, or
  a deployment surface. The `check-and-test-authoring` skill applies: a check and its control changed.
- **The premise the design rests on**: the doctor's declaration is the one surface that disagrees with the other three.
  `AGENTS.md` and the README already place `actionlint` on the default build-and-test path, `check` runs `workflowLint`
  which refuses without the tool, and the spec's class definition (required = needed to run and verify; optional =
  deployment, security scanning, agentic workflow) excludes it from the optional set.
- **Measured before/after**: under one `PATH` stub providing `java`, `docker`, `python3` and a reachable daemon but no
  `actionlint`, the doctor exited `0` before the change with `Result: every required prerequisite is satisfied.` and
  exits `1` after it with `Result: at least one required prerequisite is unsatisfied (see above).` Under the same stub,
  `./gradlew workflowLint` fails with `actionlint is required to lint GitHub workflows`.
- **Completed checks**: `bash scripts/test-doctor.sh` exits `0` on this machine and under a `PATH` that lacks
  `actionlint`; with the fixture's stub removed the same stripped-`PATH` run exits `1` on
  `fixture leaves required probe 'actionlint' unsatisfied at the floor — the verdict is not Python's alone`, which is
  the failure the fixture now names; `dash -n scripts/doctor.sh` clean;
  `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` BUILD SUCCESSFUL; `spotlessCheck`, `verifyTableRows`,
  `verifyWorkspaceRecords` clean; `openspec validate --all` 27 passed with the change item valid and no warning
  attributed to it; the 120-character recipe over the changed set flags only the claims table's rows and the
  `Workspace:` record, which are one line by construction.
- **Known limitations**: only the default run's exit code moves — `--all` already failed on a missing `actionlint`; the
  check remains a declaration-and-reporting assertion, so the doctor itself is still not part of `check`; and the
  below-floor fixture prepends its stubs to the host `PATH`, so it is not hermetic — a required tool it omits still
  resolves from the host, which the baseline assertion covers for attribution but not for hermeticity.
- **Deadline**: this caller's policy — the foreground invocation's return. A call that cannot complete is recorded as
  `stopped-responding` and replaced from this hand-over, never by an unexplained fresh session.
- **Risk focuses**: (1) whether flipping the class is the right boundary, or whether the gate or the docs are the
  surface that should have moved; (2) whether the fixture stub is the right remedy and whether any other host-dependent
  control now depends on `actionlint`; (3) whether both spec enumerations of the required set were found and no third
  exists; (4) whether the claims rows and the four dismissals are derived rather than asserted.

## Round 2 — implementation, after round 1's four findings

- **Round 1** raised four findings, all verified and applied: two claims rows whose Source command did not reproduce
  their Result (row 6's single-line `grep` missed the repo-state scenario's two-line parenthetical; row 10's range ended
  before the `PATH=` inheritance and the `3.12.0` case), the fixture's attribution invariant asserted only in a comment,
  and a five-space run inside an inline code span in task 5.2 that misquoted the control's message.
- **The fixes**: row 6 now sources `sed -n '96,97p;128,129p'` and states that each parenthetical spans two lines; row 10
  sources `sed -n '250,272p'`; the fixture checks every `expected_required` key reports `satisfied` at the floor, before
  the verdict, so a dropped stub names the probe
  (`fixture leaves required probe 'actionlint' unsatisfied at the floor — the verdict is not Python's alone`, measured
  by removing the stub again); task 5.2 quotes the verbatim single-space fragment.
- **Scope sweep for the added control**: a task (2.3), decision 3's chosen and rejected bullets, and a claims row (17)
  record the assertion.
- **Re-measured**: `bash scripts/test-doctor.sh` exits `0` on this machine and under a `PATH` lacking `actionlint`; with
  the stub removed it exits `1` naming `actionlint`; `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` BUILD
  SUCCESSFUL; `spotlessCheck`, `verifyTableRows`, `verifyWorkspaceRecords` clean; the 120-character recipe flags only
  the claims table's rows and the `Workspace:` record.

## Round 3 — implementation, after round 2's two findings

- **Round 2** raised two findings, both repeats at new instances: row 6's Result quoted the _delta's_ text while its
  Source prints the pre-delta main spec, and task 2.3 carried a five-space run inside an inline span (the continuation
  indent Prettier leaves inside a joined span).
- **The fixes**: row 6 now quotes each parenthetical exactly as the Source prints it and states, beside them, that the
  delta adds `actionlint` to both lists and reaches the main spec at the archive sync; task 2.3's span is shortened to
  `fixture leaves required probe 'actionlint' unsatisfied at the floor`, with the attribution stated in prose.
- **The mechanism sweep the round named**: a scan over the change dir for any inline span carrying a run of two or more
  spaces now reports none, and every verbatim quotation in the artifacts was taken from the Source command's own output.
- **An owner decision recorded here**: the IDE's ShellCheck plugin shows four weak warnings in these two scripts (three
  SC2086 on the intentional `set -- $word` splits, one SC2016 on the literal `` `pack` CLI `` row name), plus two
  spelling typo highlights on `nprobe` and `nvalidate`, which the spell checker reads out of the `\n`-joined literals at
  lines 57 and 214. All of them exist at the base revision, none is introduced here, and the owner chose to leave them
  as they are rather than silence them in this unit.

## Round 4 — implementation, after the advisor consultation

- The technical advisor returned `Agreement: yes` and accepted three residual risks: the fixture's non-hermetic `PATH`
  fallback, the presence-only `actionlint` probe, and the separately parked install-hint work.
- Applied from it: the non-hermetic fixture is now a stated limitation in the design's Risks and in this hand-over, so
  the accepted risk is durable rather than session-only. No behaviour changed.
