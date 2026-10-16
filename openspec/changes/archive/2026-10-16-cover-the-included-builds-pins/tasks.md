# Tasks

## 1. The check

- [x] 1.1 Add `BuildLogicUpdatesTask` in `build-logic/src/main/kotlin`, in the shape its siblings take
      (`HelmUpdatesTask`, `BuildpackUpdatesTask`, `ToolingUpdatesTask`): a declared alias list, a lookup per alias, and
      a `reportFile`. Keep the lookup and parse helpers with the task (`BuildpackJson`/`ToolingJson` are the precedent),
      not in a separate `*Rules` object.
- [x] 1.2 The lookup reads each alias's `maven-metadata.xml` from the Gradle Plugin Portal — the one surface that
      carries all twelve, since it serves its own published plugins and proxies Maven Central — and takes the newest
      entry the report's own rules accept (1.4). A coordinate whose latest cannot be read is written as an unread lookup
      rather than skipped or treated as current.
- [x] 1.3 Assert the declared list against both files: every `-plugin` alias `build-logic/build.gradle.kts` consumes
      must be declared, and every declared alias must resolve in `gradle/libs.versions.toml`, which owns its coordinate
      and pinned version. The task fails naming any alias that fails either direction — unlisted, declared but no longer
      consumed, or repeated — the property that keeps the coverage from falling behind the way the helm pin did.
- [x] 1.4 Reuse the report's _deciding_ rule, not just its parsers: call `DependencyUpdateRules.shouldReject` per
      candidate with the catalog's pin as `currentVersion` and the catalog map, the suppression set from
      `disabledEntries`, and the held lines from `holdBackEntries` — the same composition the convention's
      `rejectVersionIf` makes. That is what keeps a milestone (`kotlin-gradle-plugin`'s `2.5.0-Beta1`), a deferred major
      (`org.springframework` covering `spring-boot-gradle-plugin`), and a held-back line out of the rows, and what stops
      the check re-deriving a looser match of its own.
- [x] 1.5 Unit-test the helpers: the metadata URL for a known coordinate, the parse against a **real portal metadata
      body** (not a hand-written string), the actionable/current/unread decisions, the stability and suppression
      decisions, and 1.3's three mismatch directions (unlisted, declared but not consumed, repeated).

## 2. The report

- [x] 2.1 Register the task in the root `build.gradle.kts` beside its siblings, with `reportFile` under
      `build/build-logic-updates/report.txt`, and wire the two files 1.3 asserts against plus the suppression files.
- [x] 2.2 Confirm the report's shape matches what the workflow's extraction keys on, and that a run with every pin
      current or suppressed writes a report with no actionable rows.

## 3. The workflow carries it into the same issue

- [x] 3.1 `.github/workflows/dependency-updates.yml` runs the check alongside the existing reports, with the same
      failure handling.
- [x] 3.2 The issue-body step includes its actionable rows as their own section, and their presence notifies the owner
      exactly as catalog updates do — no second issue, job, or comment path.
- [x] 3.3 Keep the "no stable updates" path true: a check with no actionable rows must not on its own make the issue
      claim updates.

## 4. Verification

- [x] 4.1 Re-run the claims-table rows with their sources and confirm each reproduces.
- [x] 4.2 **Both control directions.** Pin one coordinate deliberately behind (an older `helm-plugin` or `lombok-plugin`
      in the catalog) and confirm the check reports it as actionable; then confirm every current pin reports nothing.
      Read each row from _that run's_ output, not a file left by an earlier one.
- [x] 4.3 **The filter controls.** Confirm the rows exclude what the report excludes: a coordinate whose portal latest
      is a pre-release (the three the Context names) yields no row, and `spring-boot-gradle-plugin` yields none while
      `org.springframework` is a suppressed major.
- [x] 4.4 Exercise the lookup's failure path: make one declared coordinate unanswerable and confirm it is reported as
      unread rather than as current, then restore it.
- [x] 4.5 Dispatch the `dependency-updates` workflow from the change's branch
      (`gh workflow run     dependency-updates.yml --ref <branch>`); a workflow edit's own run is its verification,
      since its schedule alone would only test it next week. **Run 38849159717 is green** — the new step ran, and issue
      #13's body now carries the "**Build-logic pins**" section with the five rows above the other sections intact.
- [x] 4.6 Run `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`, `spotlessApply`/`spotlessCheck`,
- [x] 4.7 Verify the `dependency-management` delta's requirements against the running task: each scenario's outcome
      reproduced (a pin behind a newer stable release reported; a pre-release and a deferred major not; an unread lookup
      named; a mismatch failing the task; `check` not running it). `openspec validate --all`, and the manual
      120-character check over the changed set.

## 5. Docs the change owns

- [x] 5.1 Sweep **every** live statement of which surface the update checks cover — the class this change corrects:
      `AGENTS.md`'s per-check inventory, its `dependency-updates.yml` entry in "What each covers", the CI section's
      sentence naming the commands that workflow runs, and the check↔surface list whose sentence names what
      `dependencyUpdates`/`dependency-updates.yml` cover (the belief this change falsifies); `README.md`'s block listing
      every report command, its slash-command table row (which names the command rather than the task, so only a grep
      for `/dependency-updates` finds it) and its suppression-coverage sentence; and
      `.opencode/commands/dependency-updates.md`, which tells an agent to run two reports. State the pins' checker in
      each, and keep the hand-audit sentence true for what remains (`[versions]`-only entries).
- [x] 5.2 `docs/ideas.md` — remove the parked entry this change implements.
- [x] 5.3 `openspec/config.yaml` — sweep its `context:` block and rules for a statement of the checks' coverage, and
      record that it carries none if that is the reading.
- [x] 5.4 Check ADR-0015's Consequences sentence about the hand-audit remainder against the new state, and record the
      reading (the ADR itself is not edited).
- [x] 5.5 Record each touched capability's `## Purpose` outcome: `merge-governance`'s names "the update checks" as a
      category, so it needs no change, while `dependency-management`'s enumerates the two reports its requirements cover
      — the Gradle report and the web UI's — so the third report's arrival owes a Purpose refresh in the archive commit
      (a delta cannot carry a `## Purpose`).

## 6. The change's own records

- [x] 6.1 Record the pre-write derivation pass's output — the claims table in `design.md` — and re-derive each row
      against its source before the first review round. This change's first plan was re-derived because a probe showed
      the included build cannot run the main report, so the pass names mechanism claims after running the tool.
- [x] 6.2 Append each `review-quick` round's row to `docs/review-rounds.tsv` when that round's report arrives.
- [x] 6.3 Run the per-unit `lesson-capture` and record the applied net `AGENTS.md` delta: two rules applied (the
      filter-before-select clause in the lookup-failure sub-bullet, and the machine-asserted-list rewrite of the
      `toolingUpdates` bullet) and no retirements; the change's whole `AGENTS.md` diff is **+13 lines** net
      (`git diff --numstat`), the sweep and the record's edits included. The capture reported five of the unit's
      incidents as already covered by existing rules (the pre-write mechanism rule, the run-scoped-output rule, the
      scratch-script-input rule, the programmatic-edit rule, the control rules), routed the `isNullOrEmpty` inspection
      to the existing optional-IDE-inspections convention, and found `README.md`'s `/dependency-updates` row stale —
      fixed in this change.
- [x] 6.4 Request the user's manual review pass, and archive only after it, the CI run, and the change's own PR: the
      pass was requested and given, PR #594's `build` check is green (8m28s), and the archive commit follows on the same
      PR.

## Implementation record

What the implementation settled beyond the plan, and the readings the docs tasks called for:

- **The declared list is aliases, not coordinates.** The check declares the twelve catalog aliases and reads each one's
  coordinate and pinned version from the catalog (`DependencyUpdateRules.catalogLibraries`, added here), so neither fact
  is duplicated; 1.3's assertion is the alias set against `build-logic`'s consumed `-plugin` aliases.
- **No Maven Central fallback.** The portal alone carries all twelve (it proxies Central), so a second lookup path would
  be an unverified branch; the design's D2 records that.
- **The row decision is a helper** (`BuildLogicPins.rowFor`), so the unread and no-update branches have committed
  controls; 4.4's end-to-end perturbation is not achievable through the catalog (an unresolvable coordinate fails the
  root build's own configuration), which is why the branch's control is the test.
- **4.2/4.3 controls (run, then reverted).** Pinning `spotless-plugin` back to `8.10.3` produced its row; pinning
  `kotlin` to `2.4.20` produced `2.4.20 -> 2.4.21` and not the `2.5.0-Beta1` the portal reports as its `release`;
  `spring-boot-plugin` produces no row while the metadata carries the stable `4.1.1` above it, and the test asserts both
  directions of that. Each was read from that run's own report — one earlier control read a stale file after a failed
  run, which the report's mtime exposed.
- **A delta was added at implementation time.** `dependency-management`'s requirements are written per report (one for
  the Gradle report, one for the web UI's), so the new check's reporting contract is a third requirement set there —
  three requirements, each one behavior, with their scenarios (its scope, its filtering, its coverage assertion) —
  recorded as design D7. The capture's verification surfaced it; the plan had scoped the delta to `merge-governance`
  alone.
- **5.1's sweep sites** are updated: `AGENTS.md`'s Build & Test block, its workflow paragraph, its "What each covers"
  entry, its catalog-ownership bullet and its check↔surface list; `README.md`'s workflow description, its report-command
  block and its coverage paragraph; and `.opencode/commands/dependency-updates.md`.
- **5.3 reading:** `openspec/config.yaml` carries no statement of the checks' coverage, so it needed no edit.
- **5.4 reading:** ADR-0015's Consequences already state the duty this implements ("checking which (if any) update
  checker covers it … a hand-audit remainder") and stay true — the remainder excludes these pins — so the ADR is not
  edited.
- **5.5 reading:** `merge-governance`'s Purpose names "the update checks" as a category rather than listing them, so it
  needs no change; `dependency-management`'s enumerates the two reports its requirements cover, so the third report owes
  a Purpose refresh in the archive commit (a delta cannot carry a `## Purpose`).
