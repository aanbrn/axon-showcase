# Ideas

Short notes to remember emerging development ideas. An idea becomes an OpenSpec change only when acted on — this file is
a scratchpad, not a backlog of planned work. An idea is removed from the list once implemented (captured by a change) or
once explored and decided against (the durable lesson is captured in `AGENTS.md`/an ADR instead); only open,
not-yet-implemented ideas remain.

Changes to this file travel with the change that owns them: an idea's **removal** ships in the implementing change's PR
— it rides that change's branch and commits with its push, like the rest of the work — and a **newly parked** idea the
in-flight change's own work surfaced rides that branch too; only a docs edit the change did _not_ cause, or one surfaced
after it is done (a merge-time capture), ships as its own docs PR (like an `AGENTS.md`/`README.md` refresh PR). When an
idea graduates into a concrete candidate for work, it may be promoted to a GitHub issue that links to the eventual
OpenSpec change. Ideas are grouped into `## YYYY-MM-DD` sections ordered newest-first; each idea goes under a section
dated when it was added (start a new section for a new day rather than appending to the most recent one).

## 2026-10-17

- Check that every recorded class is in the reviewer's canonical list — parked; no change yet. The 2026-10-15
  retrospective's S2, the remaining enabler of the window comparison its baseline names. The label set lives as prose in
  `review-quick`'s class list, and the retrospective found it drifting across the record — which matters because the
  whole convergence signal is label comparability. Add a rules-object check carrying its known-bad and known-good cases
  (the `*RulesTests` shape) asserting that every class in a `docs/review-rounds.tsv` row recorded on or after the
  canonical boundary is in that list, so a genuinely new class is added to the contract before it is recorded. Routes to
  a `code-quality` requirement plus an `agent-skills` delta.

- Guard against unticked tasks in an archived change dir — parked; no change yet. The 2026-10-15 retrospective's S4,
  proposed at lower confidence. Archived change dirs carry unticked review-gate boxes (the count and the window-proper
  instances are the retrospective's), and an unticked box inside `openspec/changes/archive/` is invisible — no gate
  reads one. A `check` member or a pre-commit arm would refuse to stage an archived change dir still carrying an
  unticked box, exempting a task the owner deliberately decoupled from its merge; that exemption is the fiddly part, so
  the guard is worth taking only if it reads as cheaper than the silence.

- Make the chart-packaging gate's fetches resilient — parked; no change yet. `check` now runs the chart's packaging, so
  the plugin's repository setup (`helmAdd*Repository`, `helmUpdateRepositories`) and its dependency fetch
  (`helmUpdateMainChartDependencies`) run on every pull request. The gate-worthy-step rule wants such a fetch made
  resilient with a bounded retry, but these are the plugin's own task types, so no repo-side wrapper can retry another
  task's action; `verifyInfraImageVersions`'s `AbstractHelmRepositoriesTask` + `BoundedRetry` is the model for a
  repo-authored fetch. Verify whether the plugin can configure a retry before acting — if it cannot, the gap is an
  upstream report on `build-extensions-oss/gradle-helm-plugin` (its issues are enabled), and the remedy is a
  repo-authored fetch task.

## 2026-10-16

- Report the plugin's cache-dependent chart-dependency skip upstream — parked; no change yet. The
  `helmUpdateMainChartDependencies` task (which the plugin's docs equate with `helm dependency update`) silently skips
  when the **configuration cache** (#581) stores its entry before the chart sources are collected — a clean checkout or
  CI: its `onlyIf` resolves false there, so nothing fetches the chart's external Bitnami `common` dependency,
  `helm/chart`'s packaging produces a chart with no `charts/`, and both the strict lint and the deployed install fail.
  Measured on one commit with only the cache toggled: SKIPPED with the cache on, fetches with
  `--no-configuration-cache`. The repo's workaround is `setOnlyIf { true }` in `helm/chart/build.gradle.kts`;
  `build-extensions-oss/gradle-helm-plugin` has issues enabled (`has_issues: true`, `has_discussions: false`), so a fix
  belongs there as an issue or a pull request and would retire the workaround.

## 2026-10-15

- Spec the owner-notification path for a bot-authored pull request — parked; no change yet. `merge-governance` owns the
  rulesets, the CI gates and the scheduled workflows, but nothing states how the owner learns that a Dependabot PR is
  open — and that path was dead on arrival: GitHub retired `dependabot.yml`'s `reviewers` option on 2025-05-20 and `#91`
  added the key anyway sixteen months later, so no review request (and no notification) was ever raised until `#586`
  replaced it with `.github/CODEOWNERS`. A requirement would state the outcome — a pull request the code owner did not
  author requests their review — while leaving the mechanism free to change, which is what the repo's own experience
  says it will do.
- Park the review loop's rejected levers, and the comparison its baseline enables — parked; no change yet. Four levers
  the owner's proposal for optimizing the iterative review loop asked for are **contradicted by the repository's own
  record**, so they are not to be re-derived the next time the question is asked: a **persistent reviewer session as a
  substitute for the per-round instance hand-off** (the loop's measured cost is re-raised claims about the change's own
  artifacts rather than re-exploration, per the retrospective's baseline, so reusing a session buys little; and the
  class-exhaustion clause requires searching the artifact anew each round, so a session's prior context must not stand
  in for that search). The session itself is the caller's to reuse: the 2026-10-16 runs reused one across rounds to good
  effect, and the day's one fresh-session loop was fresh only because its sessions had expired; **diff-scoped
  follow-ups** (an unapplied fix produces no diff, so a diff-only round can report a false clean); a **fixed iteration
  cap** (`bound-the-review-loop` explicitly rejected one, since a count would stop a productive loop); and **token,
  elapsed-time or session-count telemetry** (a process count is not a verifiable repository fact, per the
  documented-numbers rule). The one change the record did support — the canonical class list — has since landed. The
  companion measurement: `docs/retrospectives/2026-10-15.md` records the review loop's baseline — the figures and the
  boundary that makes them quotable live there rather than here, so the two cannot drift apart — to be compared against
  the first window whose rounds are both post-lever and canonically labelled. That comparison is what should decide any
  further review-loop change, not a fresh proposal.

- Measure whether the review loop's non-convergence is model-bounded — parked; no change yet. The loop's reviewer shares
  the main agent's model (the model-pin gotcha), so it cannot find what that model cannot see — plausibly why
  `contradiction`, `over-claim`, and `premise` survive it. The decisive cheap experiment is to run one unit through both
  `review-quick` (flash) and `review-thorough` (pro) and diff the _classes_ each finds, which would show whether those
  classes should escalate to the pro pass.

- Re-check the Gatling plugin's configuration-cache gap — parked; no change yet. The `gatlingRecorder` task (the
  plugin's `GatlingRecorderTask`, the pinned Gatling Gradle plugin's own task) holds a `Configuration` and a
  `SourceSet`, which the configuration cache cannot serialize, so `load-tests/build.gradle.kts` marks it
  `notCompatibleWithConfigurationCache` — a graph containing it runs cache-less. The plugin's own "support gradle
  configuration cache" work (#131, a merged PR) left this task uncovered, and the plugin repository's own tracker and
  discussions are disabled (`has_issues: false`, `has_discussions: false`), so it carries no report thread and no PR
  addressing it to cite; a fix reaches it only through a pull request. Re-check with
  `./gradlew :load-tests:gatlingRecorder --configuration-cache`, and retrigger on a release whose recorder task no
  longer holds those types.

- Widen the reviewers' 120-character check to every changed file — parked; no change yet. `review-quick.md` and
  `review-thorough.md` both scope it to the files the formatter does not cover (YAML, and so on), treating everything
  else as formatter-gated, which excludes Java — yet the Formatting convention records that Javadoc and JSDoc prose is
  not reflowed and only the manual check catches it, and two 122-character Javadoc lines passed `spotlessCheck` until an
  explicit scan found them. Name Javadoc prose in both definitions, and have the recipe iterate the changed set (split
  correctly — an unquoted stored value does not word-split in zsh).

## 2026-10-12

- Report Gradle's `--info` environment dump upstream — parked; no change yet. `--info`/`--debug` logs each exec'd
  process's full environment (values included), so a diagnostic run puts `SNYK_TOKEN`/`GH_TOKEN`/`NVD_API_KEY`-style
  values into captured output; the owning repository is `gradle/gradle`, and the reproduction is
  `./gradlew --info :helm:chart:helmLintMainChart` (7 `with environment:` lines). The durable rule is in `AGENTS.md`;
  this entry tracks the upstream report.

- Correct the `architecture-auditor`'s deferral check for renamed and compound ADR gates — parked; no change yet. Its
  definition resolves the coordinates the `Revisit when:` line names, and the `agent-skills` scenario frames the check
  as an external coordinate gate whose pinned coordinate published a newer major — so a gate whose coordinate upstream
  renamed reads as permanently unmet (it resolves a frozen artifact), and a compound gate can read as met on one axis
  while a sibling clause is open. The 2026-10-11 advisory read ADR-0011's gate that way. Routes to
  `.opencode/agent/architecture-auditor.md` and the scenario, which owes that capability's delta.

## 2026-10-11

- Re-check the web UI's `handlebars` override when the lint plugin bumps it — parked; no change yet.
  `showcase-web-ui/package.json` forces `handlebars` to `4.7.10` via `overrides`, because
  `eslint-plugin-boundaries@7.2.0` and `@boundaries/elements@3.1.1` pin `handlebars: "4.7.9"` exactly while npm's only
  offered fix is a breaking downgrade of the plugin. Unlike the `npm-audit-ignores.json` suppressions beside it, the
  override carries no expiry, so it can silently hold `handlebars` back; re-check when no package in the tree still
  requests `handlebars 4.7.9` — both `eslint-plugin-boundaries` and `@boundaries/elements` pin it exactly, so both must
  bump before the override is dropped, or the vulnerable copy returns through the one that has not.

## 2026-10-10

- Adopt `git worktree` for the per-change branch workflow — parked; no change yet. The workflow keeps a change
  uncommitted until a push forces a commit and forbids carrying unfinished work (tracked or untracked) across a branch
  switch, so a worktree per change would remove the stash/switch seam entirely: no `git stash pop` conflict-marker
  hazard, and `main`'s working tree stays free of in-progress diffs. It fits this repo well because the git hooks are
  shared — `core.hooksPath = scripts/git-hooks` lives in the common `.git/config`, so the pre-commit guard runs in every
  worktree with no per-worktree setup. Costs: the per-tree disk, measured (2026-10-12, a throwaway worktree from `main`
  with the shared caches warm) at ~628M after a fresh worktree's first Docker-free `check` — dominated by `.gradle`
  (129M), `showcase-web-ui/node_modules` (225M), and the per-module `build/` directories, over a 12M checkout — and the
  IDE, since `.idea/` is git-ignored so each worktree needs `./scripts/setup-idea.sh` (or one tree stays the IDE tree).
  The first build is a one-time ~1 minute, not a cold rebuild: the shared `~/.gradle`/`~/.npm` caches and the Gradle
  build cache restored 251 of 374 tasks, so that Docker-free gate ran in 78 seconds against ~16 seconds warm on the same
  machine — not the re-resolve and re-install the entry first assumed. A harness note too: the OpenCode session's cwd is
  per-session, so making a worktree the primary directory needs a session move. Suggested shape: primary clone on
  `main`, `git worktree add --no-track -b <change> ../axon-showcase-<change> origin/main` per change (`--no-track`, or a
  first push with `-u origin <branch>`, avoids the silent `origin/main` upstream), `git worktree remove` after merge.
  The remaining trade is the ~628M/worktree and the IDE setup rather than build time, so the choice turns on whether the
  isolation is wanted for the change at hand.

- Confirm the vendored migration skills' model pins resolve — parked; no change yet. The vendored `axon4to5-*` skills
  carry frontmatter model pins (`axon4to5-isolatedtest` → `haiku`, `axon4to5-openrewrite` → `sonnet`, Anthropic aliases)
  while every project-authored model pin is `opencode-go/deepseek-*`. Surfaced by the 2026-10-10 `agents-auditor`
  smoke-run as a low-confidence advisory: the harm is a hypothesis until it is established whether OpenCode applies (or
  how it resolves) a skill's frontmatter `model` — a triggered skill may ignore it. Confirm that before acting; if it
  applies, the fix is a re-vendor decision (the skills are copied verbatim from `AxonIQ/agent-skills`), not a local
  edit.

## 2026-10-08

- Gate the `@NullMarked` package-info convention — parked; no change yet. NullAway 0.14.2 (the pinned version) ships the
  `RequireExplicitNullMarking` Error Prone checker, which reports a top-level class unless it or its package is
  explicitly `@NullMarked`/`@NullUnmarked`, so enabling it (`-Xep:RequireExplicitNullMarking:ERROR`) would enforce the
  convention mechanically. It needs its own change: it is a separate Error Prone check from `NullAway`, so the existing
  `disable("NullAway")` on `*TestJava` tasks does not disable it — enablement must be scoped to the non-`TestJava`
  branch (or the test packages marked), which is a real investigation.

## 2026-10-06

- Explain or eliminate the gateway controller CT's load-sensitive hang — parked; diagnostics shipped, mechanism open.
  `ShowcaseRestControllerCT` fails with a `TimeoutException` at the `WebTestClient` `.exchange()`: 38 of its 76 cases at
  the default 5s timeout, and the same shape at the 30s the slice now configures — a Docker-free PR gate failed that way
  on 2026-10-15, so the raise no longer holds there. The mechanism is unproven and does not reproduce locally: shrinking
  the JVM-wide `Schedulers.boundedElastic()` pool to 4 and running the suite six times all passed, which leaves
  boundedElastic starvation unconfirmed rather than refuted — a local pass is no evidence against a CI-only cause. The
  failed CI attempt ran the suite concurrently with the web UI's `check` and spotbugs on a 4-vCPU runner (the #514
  starvation shape), and the suite's CT classes share one JVM (the #301 interference shape); JUnit kept dispatching
  distinct tests, each new one timing out on a 30s cadence for four minutes, so the stall is in the request path's
  threads rather than the JVM. The diagnostics this entry asked for are shipped: JUnit's extension autodetection
  registers `ThreadDumpOnFailure`, which dumps every thread's stack trace to the test output on a test failure — with
  the suite's `showStandardStreams`, which prints its standard output and error, a deliberate cost to remove with the
  extension once the mechanism is found.

## 2026-10-05

- Guarantee the weekly reports against a dropped or delayed `schedule` — parked; no change yet. The `schedule:` trigger
  is best-effort (delayed or dropped under load — a recent run slipped ~3 hours), and no `cron` value makes it punctual,
  so `reschedule-weekly-workflows-earlier` only widens the margin. A dispatch-fallback would turn "likely" into
  "present-or-retriggered": one scheduled watchdog that, after the expected window, checks via `gh` whether each weekly
  workflow produced its artifact for the current cycle (the audit's dated report/PR, each update check's tracker issue)
  and calls `gh workflow run <file>` for any that is absent — idempotent, so a healthy week is a no-op. Needs
  `workflow: write` + `issues: read`, and owes a `merge-governance` spec delta (a new scheduled workflow). Deferred
  while the reschedule's added margin is judged sufficient; its own change when the guarantee is wanted.

- Revisit ADR-0003's Jackson-3 deferral gate — parked; no change yet. The 2026-10-04 architecture audit reports the
  recorded `Revisit when:` condition ("the pinned `org.axonframework` and the Elasticsearch Java client … resolve
  Jackson 3") appears substantially met: `elasticsearch-client-java = 9.5.5` declares
  `tools.jackson.core:jackson-databind`/`jackson-core` as non-optional runtime dependencies, and
  `axon-framework = 4.13.2` now declares `tools.jackson.core:jackson-databind` as an **optional** dependency — Axon 4.13
  gained Jackson-3 support since the ADR (2026-08-18) characterized it as "targets Jackson 2". Open question: is the
  gate met, or does Axon's support being opt-in (not the default) mean it is still not? A decision, not a defect: if
  met, the Jackson-3 backend migration becomes due for re-planning (currently deferred behind Spring Boot 4, ADR-0004).

- Make the audit workflow's report filename collision-safe — parked; no change yet. The prompt names the report for
  "today's date", so two runs on the same UTC day write the same `docs/audits/<date>.md` and the second silently
  overwrites the first: the 2026-10-04 scheduled run reused the dispatch run's report filename (`#494`), which the
  archived `sync-audit-spec-findings-2026-10-04` change and the scheduled report's own "prior finding" reference cite,
  so the scheduled run's report had to be renamed to `docs/audits/2026-10-04-scheduled.md` (`#500`) to preserve both.
  Routes to `.github/workflows/audit.yml`'s prompt (name a same-day second run distinctly, e.g. by including the run's
  trigger) and the `merge-governance` spec requirement that owns that workflow's report behavior.

## 2026-09-30

- Retire the OpenSpec config probe and the `/opsx-tool-update` re-verification once `validate` checks the config —
  parked; no change yet. `Fission-AI/OpenSpec#1891` closed 2026-09-29, but its fix `Fission-AI/OpenSpec#1894` is still
  unmerged and in no release (no released CLI has `inspectProjectConfig`), so the `ci.yml` probe and the
  `/opsx-tool-update` re-verification remain the only guards against a config the CLI silently drops. The watch
  condition is a release carrying `#1894` (and `#1892`'s unparseable-config half), checked when the CLI is bumped rather
  than on the next weekly sweep. The `#1891` status is recorded inline in `AGENTS.md`'s config-read-path gotcha.

## 2026-09-28

- Schedule the load-test drift check in CI — parked; no change yet. The `check-load-test-drift` change adds the
  comparison but keeps it where the reference lives (a developer's cluster). A scheduled run needs three things it does
  not have: an environment identity that is not the base URL (the reference's name and its `target` guard key on the URL
  host, so a CI run at `http://axon-showcase-api` — what the deployment smoke already uses — would compare against, or
  refresh, the committed local reference), a runner-specific reference with a tolerance calibrated there, and a stable
  environment to attribute a delta to the code at all — a shared runner's timings are host state, the same reason the
  smoke runs only the `smoke` profile and records no performance numbers. Observational only if ever done, never a merge
  gate.

## 2026-09-24

- Widen the `commit-hygiene` test-coverage requirement to every check — parked; no change yet. The spec's "The guard's
  checks are covered by tests run in the build" is scoped to the guard's classes, while the build checks
  (`verifyCapturedMarkers`, `verifyTrackedIgnoredFiles`, `verifyConflictMarkers`, `verifyExecutableBits`,
  `verifyUniqueCronSchedules`, `verifyUniqueReviewRounds`, `verifyLargeFiles`) carry no test-coverage clause; widening
  it needs a REMOVED+ADDED retitle, since a `MODIFIED` block cannot rename a requirement header.

- Carry findings forward across audit reports — parked; no change yet. The reports under `docs/audits/` are write-only:
  a finding recurs run after run with nothing to say it is the same unapplied item, and the recorded smoke-run lesson is
  that "the archived run's note is never swept again". Have the verdict name which items also appear in the newest prior
  report and remain unapplied, so the report ages into a backlog. Routes to the `agents-auditor` definition, its report
  contract, and the `/audit-agents` command's read-list.

- Sharpen route-candidate verification the way merge candidates were sharpened — parked; no change yet. Merge candidates
  got the target-verification rule (#366) only after a false delegation dropped identifiers; route candidates (added
  2026-09-24) carry the lighter "confirm the mechanism exists and covers the rule's subject". A mechanism can enforce a
  _sibling_ case rather than the rule's subject (Spotless enforces the SPDX header but not "no comments"; the pre-commit
  hook enforces formatting, not the timing a rule advises), so the candidate should verify full coverage and keep
  whatever the mechanism does not cover. Parked: no incident yet — a false route is the trigger to add the clause, as a
  false delegation was for merge candidates.

- Trend the audit counts across reports — parked; no change yet. The capture rule leans on the verdict's accreted-rule
  count as the growth control, but nothing reads it back, so the only quantitative signal that consolidation is winning
  goes uncollected. A small report — or a `/retrospective` input — extracting the verdict lines from `docs/audits/*.md`
  would show findings, merge, removal, and accreted counts over time. Five reports now exist, and one definition now
  governs the accreted count, so reports from here on are comparable and the trend is buildable.

- Record the lesson-capture's rejected proposals — parked; no change yet. A "nothing durable" verdict is a judgment that
  vanishes (the `widen-auditor-to-route-candidates` capture's, for instance); recording each rejected proposal with the
  gate criterion it failed would let the retrospective surface a recurring blind spot or a recurring false positive.
  Routes to the `lesson-capture` definition plus the retrospective's digest, and needs a durable home, since the
  capture's output is currently not persisted.

- Name a durable home for a smoke-run's beyond-seed findings — parked; no change yet. The smoke-run convention says to
  fix the findings the user approves in the introducing change, but not where an unapproved one goes: in
  `widen-auditor-to-route-candidates` the second surfaced item landed only in the archive-bound `tasks.md`, which no
  sweep reads. One line in the convention — fix it, or park it in `docs/ideas.md` — closes it. Parked: one occurrence so
  far, below the capture gate.

- Explain an unexplained `.opencode/opencode.json` truncation — parked; no change yet. In
  `retire-opencode-permission-plugin` (#388) the file was truncated in the working tree between the edit and the commit,
  so only `{ "$schema": … }` shipped — losing the config the change added plus `model`, `small_model`, and the
  Playwright MCP (restored in #389). The cause is unknown: running `opencode debug config` leaves the file unchanged and
  it has held on `main` since the restore, so something in the session (a subagent probe, or a config write) rewrote it.
  Watch for a recurrence — a silent truncation strips any config-based grant, and the staged-diff-size rule in
  `AGENTS.md` names the tell. If it recurs, find the writer before trusting the file as a durable surface.

## 2026-09-22

- Check the ADR `Status:` vocabulary mechanically — parked; no change yet. `docs/adr/README.md` enumerates the
  vocabulary as `Proposed | Accepted | Superseded by ADR-NNNN`, and `narrow-query-api-dependency` invented
  `Status: Accepted (amended …)` — caught by review and reverted. The `architecture-auditor` already reports "an ADR
  `Status` that is stale", so widening it to the vocabulary (or a cheap grep in a docs check) would catch the deviation
  where the prose rule cannot be relied on. Parked: one non-severe occurrence, and it is tooling — which the
  retrospectives' own "use the tooling rather than extend it" direction argues against until it recurs.

- Make a capture confirm the rule excludes the incident that produced it — parked; no change yet. The gate requires "one
  severe verified incident with a clear preventive action" but never tests that the rule would have _caught_ that
  incident: the `extract-specd-rationale-from-agents-md` outcome-only clause passed the gate and, about an hour later
  (`sharpen-spec-worthiness-precondition`), licensed an over-move because it did not exclude the internal-control-flow
  case — that change had to add the precondition. The fix is a capture-procedure step: state the incident and confirm
  the rule excludes it, re-reading the incident to ask "would this rule have caught it?" It routes to the
  `lesson-capture` definition rather than `AGENTS.md`. Parked rather than proposed: one incident so far and the fix is
  subtle, so a second occurrence or the next `/retrospective` should confirm the class first.

- Refresh the `gateway/rest-api` Purpose to name the CORS capability — parked; no change yet. The 2026-09-21 specs-audit
  (`docs/audits/2026-09-21.md`) reported that the Purpose (`openspec/specs/showcase/gateway/rest-api/spec.md`) covers
  the request/response contract but never mentions cross-origin access, which the spec holds as its own fail-closed
  requirement, and suggested extending the closing clause with "…and configurable cross-origin access (CORS) for the
  standalone web UI." It called it Purpose-fit drift rather than a hard defect, and the finding was neither applied nor
  parked when the audit-fix change landed. A standalone `skip_specs` Purpose refresh: a delta cannot carry a Purpose, so
  the edit lands in the archive commit and the implementing PR's diff would show no spec change — state the deferral in
  the report.

## 2026-09-21

- Verify the audit workflow's generated PR title end to end — parked; no change yet. `fix-audit-pr-title` reworded the
  `audit` workflow's prompt so the agent no longer leads its response with the owner mention, because the OpenCode
  GitHub action derives the PR title by summarising the response in under 40 characters. The prompt change's effect on
  the generated title cannot be checked by any gate — no CI job runs the workflow and `workflowLint` reads only the YAML
  — so a `gh workflow run audit.yml` dispatch (or the next scheduled run) is the check: confirm the resulting report
  PR's title names the report rather than the mention.

## 2026-09-16

- Make the next phase a product phase — parked; no change yet. The first retrospective's recommended direction (see
  `docs/retrospectives/2026-09-16.md`). The tooling is mature enough to be used rather than extended: the architecture's
  missing _enforce_ layer now exists (ADR-0010), and the highest-value candidate it named — web-UI trace propagation —
  has since shipped.

- GitHub Discussions are disabled — parked; no change yet. Enabling them would give the project a second support surface
  beside issues, but it needs moderation, so it is a decision rather than a toggle.
- No project homepage — parked; no change yet. The README _is_ the documentation, so the `homepage` field stays empty
  until there is a site (or a GitHub Pages rendering of the README) to point at.
- No social preview image — parked; no change yet. It is the card shown when the repository is shared, it needs a
  design, and it is a repository-settings upload rather than a file in the tree.
- No Code of Conduct — parked; no change yet. Adopting one is a commitment with enforcement expectations, so it is the
  owner's decision rather than a file to drop in; the MIT license and the README already state the project's posture.

- The GitHub description and topics are un-gated — parked; no change yet. Nothing reads them, and the description sat
  unset until a human noticed. A pull-request check could read the live values and fail on drift, but no pull request
  causes or can remediate that drift, so every unrelated PR would carry the failure. Gating them means the repository's
  observational pattern instead — single-source the expected description and topics in-repo (a small
  `config/github-metadata.*`, or the `openspec/config.yaml` context block that already carries a near-copy of the same
  facts), then a scheduled workflow that compares the live values via the API and opens or updates an issue on drift,
  like the four update-check workflows. Worth it only if the added workflow and the fourth copy of the stack facts are
  judged cheaper than the silence.

## 2026-09-14

- Retire the `NANOS_DATE_PATTERN` workaround once its fix reaches us — parked; no change yet.
  `spring-projects/spring-data-elasticsearch#3334` closed 2026-08-30 (PR #3337, milestone 6.2.0-M2), and the fix is now
  published — but only on lines above ours: `6.2.0-M2` (2026-09-24) carries it, while we resolve
  spring-data-elasticsearch 5.5.13 on the `spring-data-opensearch` 2.0.8 line (which declares 5.5.13 directly), so the
  truncation is still live. The condition to watch is therefore a release _containing the fix on our line_, not a line
  or a milestone: `spring-data-opensearch` 3.x is the line that would carry it (the latest, 3.1.4, still ships 6.1.1),
  but 3.x targets Spring Boot 4 and `spring-data-elasticsearch` 6.x, so retiring the workaround rides the deferred
  Spring Boot 4 migration (ADR-0004) — re-check when that migration lands, not on a chart or patch bump. The other
  closure candidate does not apply — `#1060` leaves our `checkBuildEnvironmentConstraints` row untouched (the `#755`
  verdict is recorded in the upstream-reference bullet in `AGENTS.md` and ADR-0007). Recorded here rather than in the PR
  body that surfaced it, which no tool reads.

## 2026-09-07

- Migrate off the deprecated OpenSearch low-level REST client — parked; no change yet.
  `org.opensearch.client.RestClientBuilder` (and the `RestClient` it builds) is `@Deprecated`, to be removed in future
  releases in favor of the official OpenSearch Java Client. The projection service's
  `openSearchRestClientBuilderCustomizer` bean (`ShowcaseProjectionApplication`) surfaces a `[deprecation]` compile
  warning because Spring Data OpenSearch's `RestClientBuilderCustomizer` contract forces touching the deprecated type to
  configure connection pooling / idle eviction. When Spring Data OpenSearch updates its customizer to the newer client
  (or we migrate the projection/query services to the OpenSearch Java Client transport directly), the warning resolves;
  track so it does not become a hard break when the low-level client is removed.

## 2026-09-01

- Managed-k8s staging for free or cheaply — explored, parked (no change yet). Goal: a managed Kubernetes staging env for
  the chart, ideally free, otherwise as cheap as reasonable. Findings: (1) the two real free-control-plane paths are AKS
  Free (control plane free, but you pay for nodes — not \$0 ongoing) and OKE (control plane free + 2 Always Free ARM
  nodes = 12 GB, genuinely \$0); (2) Oracle halved the Always Free Ampere A1 to 2 OCPU / 12 GB in June 2026, so the full
  stack (~11-12 GiB with the `kps` + `tempo` observability stack, 5 services, Kafka, OpenSearch, Postgres) does **not**
  fit OKE's free budget with observability enabled — it fits only if staging trims observability/single-replica/smaller
  OpenSearch, and images must be ARM (`-PimagePlatform=linux/arm64` already supported); (3) there is no free managed k8s
  that runs the full chart as-is, always-on, at \$0 — every free path needs either trimming (OKE), paying for nodes
  (AKS, ~\$40-80/mo), or accepting ephemerality. Reference: `nce/oci-free-cloud-k8s` runs OKE free but on a leaner
  stack. Open decision: how faithful staging must be to the observability stack (the real decider between OKE-free and
  AKS-paid), and whether always-on vs on-demand node-pool stop/start changes the budget.
