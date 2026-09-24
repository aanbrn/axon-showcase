# Ideas

Short notes to remember emerging development ideas. An idea becomes an OpenSpec change only when acted on — this file is
a scratchpad, not a backlog of planned work. An idea is removed from the list once implemented (captured by a change) or
once explored and decided against (the durable lesson is captured in `AGENTS.md`/an ADR instead); only open,
not-yet-implemented ideas remain.

Changes to this file travel with the change that owns them: an idea's **removal** ships in the implementing change's PR
— it rides that change's branch and commits with its push, like the rest of the work — while a **newly parked** idea
that is not yet a change is committed as its own docs PR (like `AGENTS.md`/`README.md` refresh PRs). When an idea
graduates into a concrete candidate for work, it may be promoted to a GitHub issue that links to the eventual OpenSpec
change. Ideas are grouped into `## YYYY-MM-DD` sections ordered newest-first; each idea goes under a section dated when
it was added (start a new section for a new day rather than appending to the most recent one).

## 2026-09-24

- Scan the source tree for secrets — parked; no change yet. `dependencySecurityCheck` (Snyk) scans dependencies, not
  source, so a committed token passes every gate; a scanner (gitleaks/trufflehog) needs a workflow and a
  maintainer-owned policy for false positives.
- Bound the size of committed files — parked; no change yet. Nothing stops a large blob from being committed; a check
  would bound it, at the cost of a threshold to tune.
- Enforce line endings via `.gitattributes` — parked; no change yet. The repository has no `.gitattributes`, so Spotless
  normalizes only the files it owns; a `* text=auto eol=lf` (or per-type) policy would make it uniform.
- Pass `-z` to the checker's remaining git path-parsing sites — parked; no change yet. The `git ls-files -s` parse in
  `find_non_executable_scripts` was fixed with `-z`, but `staged_paths` (`git diff --cached --name-only`),
  `find_tracked_ignored` (`git ls-files --cached --ignored`), and `find_staged_then_edited` (`git status --porcelain`)
  still C-quote a non-ASCII path — so `formatter_owned`'s `.endswith(FORMATTER_OWNED)`, which decides whether the guard
  runs the formatter, silently skips it. The AGENTS.md git-path-quoting lesson names the fix.
- Widen the `commit-hygiene` test-coverage requirement to every check — parked; no change yet. The spec's "The guard's
  checks are covered by tests run in the build" is scoped to the guard's classes, while the build checks
  (`verifyCapturedMarkers`, `verifyTrackedIgnoredFiles`, `verifyConflictMarkers`, `verifyExecutableBits`) carry no
  test-coverage clause; widening it needs a REMOVED+ADDED retitle, since a `MODIFIED` block cannot rename a requirement
  header.

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
  would show findings, merge, removal, and accreted counts over time. Thin today (two reports); revisit once several
  accumulate.

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

- Retry the actionlint download in the `build` job — parked; no change yet. `ci.yml`'s "Install actionlint" step fetches
  the tool with `bash <(curl …download-actionlint.bash)` and no retry, so a transient failure of GitHub's release-asset
  download fails the whole merge-gate `build` job. It happened on `main` at `2011915`: the download returned HTTP
  **504** with a 92-byte `text/html` body for actionlint `v1.7.12`, so `gzip: stdin: not in gzip format` and `tar`
  exited 2 — the step passed minutes earlier on `7b4fcef` and the asset itself was intact (`gh api`, 2,353,908 bytes,
  `state: uploaded`), and five consecutive `curl -I` probes reproduced the `504` until it cleared, after which
  re-running the same commit succeeded. The `504` hit the script's **inner** asset download
  (`curl -L "${url}" | tar xvz`, line 125 of `download-actionlint.bash`, which honours no retry flag or env var) — the
  script fetch itself succeeded — so an outer `--retry` on `ci.yml`'s `curl …download-actionlint.bash` would absorb
  nothing; retry the whole step, or download the release asset directly with `curl --retry`. Worth doing because the
  failure is invisible to `workflowLint` and costs a red `main` plus a manual re-run.

- Verify the audit workflow's generated PR title end to end — parked; no change yet. `fix-audit-pr-title` reworded the
  `audit` workflow's prompt so the agent no longer leads its response with the owner mention, because the OpenCode
  GitHub action derives the PR title by summarising the response in under 40 characters. The prompt change's effect on
  the generated title cannot be checked by any gate — no CI job runs the workflow and `workflowLint` reads only the YAML
  — so a `gh workflow run audit.yml` dispatch (or the next scheduled run) is the check: confirm the resulting report
  PR's title names the report rather than the mention.

## 2026-09-16

- State once where agent-only tooling lives — parked; no change yet. `scripts/` holds repo tooling whatever the caller
  (the human-documented `setup-idea.sh`, its script-only helper, and the agent-invoked `experience-analysis.sh`), while
  `.opencode/` holds runtime config, agents, commands, and skills. This recurred when the owner asked whether
  `experience-analysis.sh` belonged in `scripts/`; the answer currently lives only in that script's header.
- Make the next phase a product phase — parked; no change yet. The first retrospective's recommended direction (see
  `docs/retrospectives/2026-09-16.md`). The tooling is mature enough to be used rather than extended: the architecture's
  missing _enforce_ layer now exists (ADR-0010), and the highest-value candidate it named — web-UI trace propagation —
  has since shipped.

- Publish a first GitHub release and keep tagging — parked; no change yet. The repo has 250+ merged PRs, 150+ archived
  changes, and versioned service images, but zero releases and zero git tags, so there is no "what shipped, when"
  surface for a visitor. Decide the version (the images already carry `${project.version}`), what a release notes, and
  whether it is cut per change, per milestone, or on a cadence.

- GitHub Discussions are disabled — parked; no change yet. Enabling them would give the project a second support surface
  beside issues, but it needs moderation, so it is a decision rather than a toggle.
- Nothing is published to a container registry — parked; no change yet. The image tasks (`bootBuildImage` for the four
  JVM services, `dockerBuildImage` for the web UI) build to the local daemon only, so the `aanbrn/axon-showcase-*` names
  documented in `AGENTS.md` are a convention the images never leave: nothing pushes them, and the `aanbrn` Docker Hub
  namespace is empty today (the owner deleted the repositories an earlier manual attempt had created). The decision is
  whether to publish at all, and where — GHCR (discoverable from the repository) and/or Docker Hub — which makes this a
  pipeline change plus a registry choice.
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
  `spring-projects/spring-data-elasticsearch#3334` closed 2026-08-30 (PR #3337, milestone 6.2.0-M2), but the fix is in
  **no published release**: the newest artifacts are `6.2.0-M1`, `6.1.1` and `6.0.7`, all published 2026-08-20, ten days
  _before_ the fix merged — so `M1` predates it and only the unreleased `6.2.0-M2` carries it. We resolve
  spring-data-elasticsearch 5.5.13 on the `spring-data-opensearch` 2.0.7 line (2.0.7 declares 5.5.12; the Spring Boot
  3.5.16 BOM raises it), so the truncation is still live. The condition to watch is therefore a release _containing the
  fix_, not a line or a milestone: `spring-data-opensearch` 3.x is the line that would carry it (the latest, 3.1.4,
  still ships 6.1.1), but 3.x targets Spring Boot 4 and `spring-data-elasticsearch` 6.x, so retiring the workaround
  rides the deferred Spring Boot 4 migration (ADR-0004) — re-check when that migration lands, not on a chart or patch
  bump. The other closure candidate does not apply — `#1060` leaves our `checkBuildEnvironmentConstraints` row untouched
  (the `#755` verdict is recorded in the upstream-reference bullet in `AGENTS.md` and ADR-0007). Recorded here rather
  than in the PR body that surfaced it, which no tool reads.

## 2026-09-13

- ADR revisit triggers for time-bounded decisions — parked; no change yet. ADR-0003, ADR-0004, and ADR-0011 are explicit
  deferrals whose entire point is to be revisited when a stated condition is met: ADR-0003 (retain Jackson 2; adopt
  Jackson 3 only once Axon and the OpenSearch client support it — an external gate) ADR-0004 (defer Spring Boot 4;
  reopen when there is capacity — an internal one), and ADR-0011 (defer Axon Framework 5 until its dependency surface
  ships 5.x — an external gate). Both _state_ their condition in prose in their Decision, but nothing _surfaces_ it: the
  ADR template has no `Revisit when:` field, no check watches for the condition, and `Status` only records a replacement
  after the fact — so a deferral silently becomes permanent until someone remembers it. Add a `Revisit when:` line to
  the template (and to the existing deferrals), and decide how a due trigger reaches a human: the `architecture-auditor`
  checks `Status` integrity and a Decision contradicted by the code, but not whether a deferred decision's condition has
  since been met, so either extend it to flag a deferred ADR whose condition looks met, or list such ADRs in a small
  report alongside the dependency-update checks. Distinct from status drift: the decision still holds, its premise may
  not.

## 2026-09-10

- Rethink or rewrite the load tests — parked; no change yet. The current Gatling setup (`load-tests/src/gatling/java`,
  `ShowcaseSimulation`) is a single probabilistic scenario exercising the API gateway (list, then schedule/start/
  finish/remove with decreasing probability) with per-profile pass assertions — it predates the distributed command bus,
  the web UI, and the current architecture and has not kept pace with the system it tests. Revisit the scenario mix
  (include the query service / Protobuf paths, the SSE stream, the web UI), the injection profiles and pass assertions,
  whether load tests should run against the compose stack or the Helm deployment, and how results feed the
  requests/limits baselines (see the resource-sizing idea below).

## 2026-09-07

- Measure code coverage for the web UI — parked; no change yet. The JVM modules have a JaCoCo coverage gate
  (`jacocoTestCoverageVerification`, baseline in `config/jacoco/coverage-baseline.properties`), but `showcase-web-ui`
  (Vitest) has no coverage measurement. Explore wiring Vitest's built-in `--coverage` (via `@vitest/coverage-v8`) into
  the frontend `check`, and whether a coverage gate (threshold) makes sense for the UI or just a reporting step.

- Client-side (RUM) observability for the web UI — parked; no change yet. The deployable-UI change adds only server-side
  nginx metrics (stub_status + ServiceMonitor); the UI's user-facing experience is still unobserved, so a separate UI
  change would add web-vitals + JS-error reporting (e.g. Grafana Faro or a push-to-gateway metrics endpoint).

- Rethink reconciliation in the web UI — parked; no change yet. `ShowcasesPage` reconciles local writes and
  saga-triggered events against the eventually-consistent read model by waiting on the projected state per event
  (`waitForEvent`/`waitForReadModel`, with a connect-time filter). This works but couples the page to polling; a
  redesign could subscribe the read model itself to the event stream (server-side projection push) or refetch on event
  with a single debounced invalidation instead of one wait per event.

- Rethink resource requests/limits in the Helm chart and the local target — parked; no change yet. The chart's default
  resources are uneven and largely unvalidated: the JVM services default to `requests: 1.0 CPU / 0.5Gi` with
  `limits: 3.0 CPU / 1Gi` (generous, speculative), while the web UI uses `requests: 100m / 64Mi` with
  `limits: memory 128Mi` (no CPU limit), and the local target (`values-local.yaml`) overrides no resources at all.
  Revisit with measured baselines (e.g. from the Gatling load tests / kube-state-metrics) so requests/limits reflect
  real usage, decide on CPU limits (the JVM services set them; the web UI does not), and align defaults across services
  for consistency.

- Migrate off the deprecated OpenSearch low-level REST client — parked; no change yet.
  `org.opensearch.client.RestClientBuilder` (and the `RestClient` it builds) is `@Deprecated`, to be removed in future
  releases in favor of the official OpenSearch Java Client. The projection service's
  `openSearchRestClientBuilderCustomizer` bean (`ShowcaseProjectionApplication`) surfaces a `[deprecation]` compile
  warning because Spring Data OpenSearch's `RestClientBuilderCustomizer` contract forces touching the deprecated type to
  configure connection pooling / idle eviction. When Spring Data OpenSearch updates its customizer to the newer client
  (or we migrate the projection/query services to the OpenSearch Java Client transport directly), the warning resolves;
  track so it does not become a hard break when the low-level client is removed.

## 2026-09-04

- Dependency updates for the web UI — parked; explore whether it's possible to check frontend dependency updates the way
  the JVM modules do (`dependencyUpdates`), and whether we can also scan the web UI for dependency vulnerabilities. The
  `dependency-updates` machinery currently only covers catalog-owned Gradle coordinates; the web UI's npm dependencies
  (`package.json` / `package-lock.json`) are outside it. To explore: `npm outdated` / `npm audit` (and
  `npm audit --omit=dev`) as analogues of `dependencyUpdates` / `dependencySecurityCheck`, wired as Gradle tasks or npm
  scripts and reported like the existing update/security issues; whether the observability chart has no `*-image-tag`
  for the UI (it is now a dedicated nginx image serving the built frontend), so a UI image-tag bump would be a manual
  coordinate; and whether Snyk can also scan `package-lock.json` (the existing `dependencySecurityCheck` uses the Snyk
  CLI with the root `.snyk` policy).

- Enforce web UI conventions with tooling — parked; do as its own change after `add-web-ui` is merged. Prettier is a
  formatter, not a style linter: it gates formatting (width, quotes, semicolons) but not _conventions_. ESLint
  (correctness) and tsc (types) gate their slices, but two convention areas are currently human-review/AGENTS.md-only:
  (1) **FSD import-direction rules** (slices import only downward; `app/` → `pages/` → `widgets/` → `features/` →
  `entities/` → `shared/`) — enforce with `eslint-plugin-boundaries`, with public-API boundaries per slice; and (2)
  **naming conventions** (e.g. `use*` hooks, `*.test.ts(x)`/`*.spec.ts` suffixes) — via ESLint rules or
  `eslint-plugin-import` naming. Keep it out of the current change to keep the review focused; verify existing code
  conforms (it was built cleanly) and let CI gate it from then on.

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
