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

## 2026-10-05

- Revisit ADR-0003's Jackson-3 deferral gate — parked; no change yet. The 2026-10-04 architecture audit reports the
  recorded `Revisit when:` condition ("the pinned `org.axonframework` and the Elasticsearch Java client … resolve
  Jackson 3") appears substantially met: `elasticsearch-client-java = 9.5.4` declares
  `tools.jackson.core:jackson-databind`/`jackson-core` as non-optional runtime dependencies, and
  `axon-framework = 4.13.2` now declares `tools.jackson.core:jackson-databind` as an **optional** dependency — Axon 4.13
  gained Jackson-3 support since the ADR (2026-08-18) characterized it as "targets Jackson 2". Open question: is the
  gate met, or does Axon's support being opt-in (not the default) mean it is still not? A decision, not a defect: if
  met, the Jackson-3 backend migration becomes due for re-planning (currently deferred behind Spring Boot 4, ADR-0004).

- Note that the generated `/opsx-sync` is archive-internal, not a standalone command — parked; no change yet. The
  2026-10-04 agents-audit reports that the generated `opsx-sync` command and its `openspec-sync-specs` skill invite a
  standalone "sync delta specs to main specs without archiving" run, which contradicts the load-bearing "Sync the main
  spec only at archive" rule (`AGENTS.md`). The legitimate use is inline within `openspec archive`, and a standalone run
  would write the main spec to describe behavior the code has not been verified against, with `openspec validate` unable
  to flag it. The fix is our usage, not upstream: document in the archive-only rule that `/opsx-sync` is
  archive-internal and must not be run standalone. Parked as one occurrence, routed to the archive-only rule's wording
  when next touched.

- Make the audit workflow's report filename collision-safe — parked; no change yet. The prompt names the report for
  "today's date", so two runs on the same UTC day write the same `docs/audits/<date>.md` and the second silently
  overwrites the first: the 2026-10-04 scheduled run reused the dispatch run's report filename (`#494`), which the
  archived `sync-audit-spec-findings-2026-10-04` change and the scheduled report's own "prior finding" reference cite,
  so the scheduled run's report had to be renamed to `docs/audits/2026-10-04-scheduled.md` (`#500`) to preserve both.
  Routes to `.github/workflows/audit.yml`'s prompt (name a same-day second run distinctly, e.g. by including the run's
  trigger) and the `merge-governance` spec requirement that owns that workflow's report behavior.

## 2026-10-04

- Identify a running service's build at runtime — parked; no change yet. The gateway carries Spring Boot build info
  (`build-info.properties`, added for its OpenAPI `info.version`), but the other four services do not, and no service
  exposes `/actuator/info` (`application.yml` exposes only `health` and `prometheus`). Enabling `buildInfo()` (with
  `build.time` excluded, so `bootBuildInfo` stays cacheable) and exposing `info` across all five would let a running
  deployment report its own version without inspecting image tags — useful when diagnosing which build a pod is running.
  Deliberately not folded into `publish-github-releases`, which only needed it for the gateway's OpenAPI document; the
  images' version is otherwise already single-sourced through their tag.

## 2026-10-03

- Record the Elasticsearch Java client's role on the read side — parked; no change yet. The projection/query services
  and the query client declare `co.elastic.clients:elasticsearch-java` directly (`showcase-projection-service`,
  `showcase-query-service`, and `showcase-query-client` build files), but no Java source imports a `co.elastic.clients`
  type and no ADR records why — ADR-0012 records the OpenSearch-client swap but never mentions it, and no build-file
  comment explains the declaration. The dependency's Jackson-3 transitives are covered by the dependency-security spec
  and the transitive-vulnerability archives, but the structural decision (why the read side depends on Elastic's client
  at all) is recorded nowhere. Surfaced by the widened `architecture-auditor`'s smoke-run; extend ADR-0012 (or ADR-0003)
  with the clause once the reason is established.

## 2026-09-30

- Retire the OpenSpec config probe and the `/opsx-tool-update` re-verification once `validate` checks the config —
  parked; no change yet. `Fission-AI/OpenSpec#1891` closed 2026-09-29, but its fix `Fission-AI/OpenSpec#1894` is still
  unmerged and in no release (the latest CLI, our pinned `1.14.0`, has no `inspectProjectConfig`), so the `ci.yml` probe
  and the `/opsx-tool-update` re-verification remain the only guards against a config the CLI silently drops. The watch
  condition is a release carrying `#1894` (and `#1892`'s unparseable-config half) — re-check when `@fission-ai/openspec`
  publishes past `1.14.0`, not on the next weekly sweep. The `#1891` status is recorded inline in `AGENTS.md`'s
  config-read-path gotcha.

## 2026-09-28

- Schedule the load-test drift check in CI — parked; no change yet. The `check-load-test-drift` change adds the
  comparison but keeps it where the reference lives (a developer's cluster). A scheduled run needs three things it does
  not have: an environment identity that is not the base URL (the reference's name and its `target` guard key on the URL
  host, so a CI run at `http://axon-showcase-api` — what the deployment smoke already uses — would compare against, or
  refresh, the committed local reference), a runner-specific reference with a tolerance calibrated there, and a stable
  environment to attribute a delta to the code at all — a shared runner's timings are host state, the same reason the
  smoke runs only the `smoke` profile and records no performance numbers. Observational only if ever done, never a merge
  gate.

- Explain the load tests' accepted tail degradation — promoted to issue #430; no change yet. The 2026-09-28 re-measure
  accepted a **p99** regression into the committed reference (`FetchShowcases` 15 → 24 ms, `ScheduleShowcase` 15 → 27,
  `PollShowcase` 12 → 24, `RemoveShowcase` 13 → 24) while mean and p95 moved by ~1 ms. The profiles' own thresholds do
  not move with the tail (5x these values still lands under their 200 ms p99 floor), so only the drift check tracks it:
  a further p99 rise of up to ~1.5x them (the tolerance, floored) passes it. Whether that tail is a real degradation of
  the pipeline (gateway or OpenSearch tail latency under this cluster's state) or an artifact of the environment the
  measurement ran in is unestablished: the reference was refreshed to match reality, not to resolve it.

## 2026-09-24

- Widen the `commit-hygiene` test-coverage requirement to every check — parked; no change yet. The spec's "The guard's
  checks are covered by tests run in the build" is scoped to the guard's classes, while the build checks
  (`verifyCapturedMarkers`, `verifyTrackedIgnoredFiles`, `verifyConflictMarkers`, `verifyExecutableBits`,
  `verifyUniqueCronSchedules`, `verifyLargeFiles`) carry no test-coverage clause; widening it needs a REMOVED+ADDED
  retitle, since a `MODIFIED` block cannot rename a requirement header.

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

- Make the next phase a product phase — parked; no change yet. The first retrospective's recommended direction (see
  `docs/retrospectives/2026-09-16.md`). The tooling is mature enough to be used rather than extended: the architecture's
  missing _enforce_ layer now exists (ADR-0010), and the highest-value candidate it named — web-UI trace propagation —
  has since shipped.

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
  spring-data-elasticsearch 5.5.13 on the `spring-data-opensearch` 2.0.8 line (which declares 5.5.13 directly), so the
  truncation is still live. The condition to watch is therefore a release _containing the fix_, not a line or a
  milestone: `spring-data-opensearch` 3.x is the line that would carry it (the latest, 3.1.4, still ships 6.1.1), but
  3.x targets Spring Boot 4 and `spring-data-elasticsearch` 6.x, so retiring the workaround rides the deferred Spring
  Boot 4 migration (ADR-0004) — re-check when that migration lands, not on a chart or patch bump. The other closure
  candidate does not apply — `#1060` leaves our `checkBuildEnvironmentConstraints` row untouched (the `#755` verdict is
  recorded in the upstream-reference bullet in `AGENTS.md` and ADR-0007). Recorded here rather than in the PR body that
  surfaced it, which no tool reads.

## 2026-09-07

- Client-side (RUM) observability for the web UI — parked; no change yet. The deployable-UI change adds only server-side
  nginx metrics (stub_status + ServiceMonitor); the UI's user-facing experience is still unobserved, so a separate UI
  change would add web-vitals + JS-error reporting (e.g. Grafana Faro or a push-to-gateway metrics endpoint).

- Rethink reconciliation in the web UI — parked; no change yet. `ShowcasesPage` reconciles local writes and
  saga-triggered events against the eventually-consistent read model by waiting on the projected state per event
  (`waitForEvent`/`waitForReadModel`, with a connect-time filter). This works but couples the page to polling; a
  redesign could subscribe the read model itself to the event stream (server-side projection push) or refetch on event
  with a single debounced invalidation instead of one wait per event.

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
