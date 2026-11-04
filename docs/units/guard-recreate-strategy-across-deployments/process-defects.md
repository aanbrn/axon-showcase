# Process defects — guard-recreate-strategy-across-deployments

Process defects observed while working this unit. Recorded under `AGENTS.md`'s "Workflow: Record process defects".

### guard-recreate-strategy-across-deployments#1 — A `check` task and a `check`-dependency premise carried no `gradle-build` pack

- **Recorded** — 2026-11-04, by the writing session, on round 1's missing-input stop.
- **Stage** — proposal.
- **Expected** — a change that registers a `check` task and rests a premise on what `check` already depends on is
  classified against `gradle-build`'s row ("changing a Gradle build decision", the `check` gates) and names the pack in
  its hand-over.
- **Observed** — the hand-over named `deployment-operations` only; the round-1 reviewer stopped on the missing
  `gradle-build`, pointing at task 2.3, design decision 3 (the claim that `check` already runs the chart packaging) and
  the `./gradlew check` completion gate.
- **Evidence** — round 1's report ("Missing pack — `gradle-build`"); the hand-over's `Packs: deployment-operations`
  line; the change's `tasks.md` build task and its `design.md` premise.
- **Consequence** — one stopped invocation and one re-run with both packs. This repeats
  `reject-ignored-openspec-delta-sections#2`, one unit earlier and for the same reason: the classification followed the
  change's subject (a chart, a CLI guard) and missed the build surface the change also edits.
- **Knowledge boundary** — observed this unit's stop and the earlier unit's record. Did not check whether other units in
  the session edited `build.gradle.kts` without the pack.

### guard-recreate-strategy-across-deployments#2 — An edit script reported success while one of its replacements did not apply

- **Recorded** — 2026-11-04, by the writing session, on round 4's report.
- **Stage** — proposal.
- **Expected** — a scripted replacement asserts each anchor's occurrence count and fails loudly, and the result is read
  back before a hand-over claims it landed.
- **Observed** — the round-3 fix script replaced four strings in `tasks.md`; the task-1.1 anchor no longer matched after
  the formatter's reflow, but the other three replacements applied and the script printed its success line, so the
  doc-comment requirement never reached the task. The round-4 hand-over then stated that 1.1 required it.
- **Evidence** — round 4's `dropped-content` finding; the hand-over's round-4 summary; `tasks.md`'s 1.1 as it stood.
- **Consequence** — one extra proposal round; the anchor was re-applied with the edit tool and read back.
- **Knowledge boundary** — observed this unit's script and its output. Did not check whether earlier scripted edits in
  the session had a silent no-op that a later read did not catch.

### guard-recreate-strategy-across-deployments#3 — A claims row's command could not produce its own result

- **Recorded** — 2026-11-04, by the writing session, on round 6's report.
- **Stage** — proposal.
- **Expected** — a claim's source is a command whose output contains what the row asserts, copied from a run.
- **Observed** — the `HelmTemplate` row cited `unzip -p … '*.kt' | grep -n …`, which concatenates every source member
  before numbering, so it prints neither filenames nor the per-file line numbers the result names. The row was patched
  twice (rounds 5 and 6) before the source was re-derived from an extraction plus `grep -rn`.
- **Evidence** — rounds 5 and 6's `claim-unreproducible` findings; the row's final command and its output
  (`AbstractHelmInstallationCommandTask.kt:54`, `HelmTemplate.kt:95`, `HelmValueOptions.kt:25`).
- **Consequence** — three rounds on one row; the fact itself was true throughout, and only the evidence lagged.
- **Knowledge boundary** — observed this row's rounds. Did not audit the other rows of this change's table for a source
  that prints a different shape from its result.

### guard-recreate-strategy-across-deployments#4 — A reported replacement did not land, and the hand-over repeated the claim

- **Recorded** — 2026-11-04, by the writing session, on round 10's stated limitation.
- **Stage** — proposal.
- **Expected** — a replacement whose anchor is a wrapped span is verified against the file after the write, and a
  hand-over states only what the artifacts show.
- **Observed** — the script that qualified the delta "dropped the pointer into `docs/ideas.md`", but its anchor had been
  reflowed, the replacement no-opped, and the hand-over reported the drop. Round 10 read the clause back and named the
  discrepancy.
- **Evidence** — round 10's limitation paragraph; `specs/showcase/deployment/helm-chart/spec.md:54` before the
  correction; the corrected line afterwards.
- **Consequence** — one inaccurate hand-over and one corrective edit; the normative scope was unaffected, and the clause
  was then removed deliberately.
- **Knowledge boundary** — observed this unit's scripts and hand-overs. Did not check whether the same missed-anchor
  pattern affected the design's two other qualifications of the same sentence.
