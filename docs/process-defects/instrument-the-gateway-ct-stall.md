# Process defects — instrument-the-gateway-ct-stall

Process defects observed while working this unit. Recorded under `AGENTS.md`'s "Workflow: Record process defects".

### instrument-the-gateway-ct-stall#1 — An up-to-date task was read as a completed probe

- **Recorded** — 2026-11-01, by the writing session (`ses_e6bb3d98effeNURMqTdoGwAu9f`), during the investigation.
- **Stage** — implementation (the pinning probe, before any code was written).
- **Expected** — a probe's verdict rests on the task having executed; a task that Gradle reports up-to-date or restores
  from the build cache contributes nothing.
- **Observed** — the first pinning probe (`./gradlew :showcase-api-gateway:componentTest` with the
  `-Djdk.tracePinnedThreads` init script) finished in 13 s with `5 executed, 19 from cache, 38 up-to-date`, and its zero
  pinning traces were read as "the suite pins nothing"; the suite had not run.
- **Evidence** — that run's `62 actionable tasks: 5 executed, 19 from cache, 38 up-to-date` line, against the rerun
  (`--rerun`) that executed the suite: 124 tests in `build/test-results/componentTest`, still zero pinning traces.
- **Consequence** — one wasted probe; the conclusion happened to survive the correct run, so nothing shipped wrong. The
  rerun's test-result count is what established that the suite had actually executed.
- **Knowledge boundary** — observed these two invocations. Did not check whether the same confusion affects a warm CI
  rerun, whose restored-from-cache tasks would look like an executed suite in a summary line.

### instrument-the-gateway-ct-stall#2 — A probe ran under a different JDK than the suite's

- **Recorded** — 2026-11-01, by the writing session (`ses_e6bb3d98effeNURMqTdoGwAu9f`), during the review loop.
- **Stage** — implementation (the watchdog's virtual-thread counter and the probe behind it).
- **Expected** — a probe that decides how a runtime artifact is parsed runs under the JDK the suite runs.
- **Observed** — the probe was run from a scratch directory outside the repository, where `java` resolves 25.0.5, so the
  dump shape it captured was that JDK's (`#26 "" virtual TIMED_WAITING <timestamp>`); the repository pins 21.0.13 via
  `.java-version`, where the line is `#30 "direct-of-virtual" virtual`. The pattern the probe justified matched nothing
  on the suite's JDK, and it was defended against the review's correct finding with the probe's own evidence.
- **Evidence** — `java -version` inside the repository (`21.0.13`) beside the same command in `/tmp` (`25.0.5`); the JDK
  21 dump line read with `od -c` (`#30 "direct-of-virtual" virtual\n`); the pattern `^#\d+ ".*" virtual ` counting 0 on
  that dump while `virtual$` counted 3.
- **Consequence** — the counted line would have printed `0 virtual threads included` unconditionally, and a round of the
  review loop was spent refuting a true finding. Repaired by matching the marker with `virtual(\s|$)` and by building
  the control's fixture from both real shapes; the control is now red against the old pattern.
- **Knowledge boundary** — observed these two JDKs on this machine. Did not check whether other JDKs on `PATH` resolve
  differently for the other probes this unit ran.

### instrument-the-gateway-ct-stall#3 — A review command's stop step was skipped

- **Recorded** — 2026-11-01, by the writing session (`ses_e6bb3d98effeNURMqTdoGwAu9f`), during the review gate.
- **Stage** — review (the deeper pass).
- **Expected** — `.opencode/commands/review-thorough.md`'s step 7 asks the owner which findings to fix before any file
  is edited, and step 5 presents the findings first.
- **Observed** — the deep review's blocking finding and its two should-fix findings were applied immediately, before the
  owner was asked; the ask came afterwards, inside the manual-review request, and the owner was told the step had not
  been followed.
- **Evidence** — `tasks.md` section 7 records the findings and their dispositions, and the change's commit history would
  show the edits preceding any approval message; the owner's reply that followed asked for an advisor review rather than
  objecting to the edits.
- **Consequence** — work was done the procedure leaves to the owner's choice; nothing had to be reverted, and the scope
  was subsequently narrowed by the owner's advisor round. The deviation is disclosed in `tasks.md` 5.5.
- **Knowledge boundary** — observed this session's ordering. Did not check whether an owner decision would have differed
  had the findings been presented first.

### instrument-the-gateway-ct-stall#4 — A project-wide convention was re-derived instead of reused

- **Recorded** — 2026-11-01, by the writing session (`ses_e6bb3d98effeNURMqTdoGwAu9f`), after the owner's review.
- **Stage** — implementation (the nullability annotations added to clear the IDE warnings).
- **Expected** — a nullability marker is applied the way the repository already applies it: `package-info.java` with
  `@NullMarked` per package, as the repository's 23 tracked `package-info.java` files do.
- **Observed** — the IDE's warnings were answered with per-member `@NonNull`/`@NullMarked` annotations, and then a
  class-level marker, before the owner pointed at the package-level convention; the per-member version was reverted once
  the package file existed.
- **Evidence** — the owner's message ("Можно же через package-info, как в других местах проекта"), and
  `git ls-files '*package-info.java'` returning 23 tracked files, five of them in this module's `main` source set, each
  with `@NullMarked` and a one-line Javadoc. A first count of "ten" came from a `find` over the working tree, which also
  read five stale copies under `build/generated/` — the tracked set is the source of truth.
- **Consequence** — two extra edit rounds on three files; nothing shipped, and the final shape is the package-level one
  plus `@Nullable` only where a value is genuinely absent.
- **Knowledge boundary** — observed this unit's files and the tracked corpus. Did not check whether other component-test
  packages should carry the same file (none does), since only the diagnostics package is new here.
