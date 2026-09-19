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

## 2026-09-19

- Inject a positive-control task into every check-adding change via `openspec/config.yaml` — parked; no change yet. The
  2026-09-19 retrospective found the same false-signal class recur in the agent's own scratch checks during
  implementation (one traceable, two as the session's account): a rule in `AGENTS.md` is read only when the agent
  happens to recall it, whereas a `config.yaml` rule is injected at `openspec new change` / `openspec instructions` time
  and is hard to miss. The rule would require a change that adds or changes a check to prove it fails on a known-bad
  input and passes on a known-good one, and to read the output of any scratch command used as evidence. Follow the
  config read-path gotcha when adding it: quote any scalar containing `: `, and confirm the CLI consumes the rule before
  relying on it.

## 2026-09-18

- Run the accreted-rules audit on a cadence rather than on demand — parked; no change yet. The capture and the audit are
  opposite forces: this session's capture rounds added rules while its consolidation passes removed or merged roughly as
  much, and each audit ran only because someone asked for it — even though `agents-auditor` now surfaces merge and
  removal candidates as standing findings. A standing trigger — after every Nth capture, say — would make the balance
  self-correcting instead of reactive. The 2026-09-19 retrospective measured the imbalance: `AGENTS.md` grew 1,559 →
  1,835 lines in one window, roughly **+220 net across seventeen captures against −16 net across six consolidations**,
  so a human-triggered prune is too infrequent. Keying the trigger to the `agents-auditor`'s own accreted count (its
  verdict line already reports `<n> accreted`) is the principled choice — accretion is caused by captures, so the prune
  should key to the growth signal, not a bare Nth capture or a wall-clock cadence. Batched into one off-peak
  `schedule:`-triggered run, this also implements the parked off-peak cost idea and the three zero-touch
  scheduled-auditor variants, all of which want the same mechanism (see the "Merge the parked scheduling/cost ideas"
  entry).

## 2026-09-16

- Cost: time-shift discretionary bulk agent work into the provider's off-peak window — parked; no change yet. Our model
  pins sit on a flat-rate, dollar-metered plan, and the DeepSeek models on it are priced in peak/off-peak tiers, so the
  **same pass consumes half the metered quota off-peak** — nothing changes on the invoice ($10/month flat); the lever
  stretches the quota, which is the binding constraint. The shiftable passes mostly run on the `deepseek-v4-pro` pin
  (the four `/audit-*` agents and `/review-thorough`; the pin's sixth agent, `diagrammer`, is interactive), so it is the
  pro quota the saving lands on. At the time of writing the provider's page gives peak as 01:00–04:00 and 06:00–10:00
  UTC, Mon–Fri, with all other hours including weekends off-peak (~79% of the week), and peak at exactly 2× off-peak.
  - Only the unattended work is shiftable: the four `/audit-*` sweeps, `/retrospective`, `/review-thorough`, and long
    capture chains. The interactive loop is paced by the human and cannot be batched, so the saving is concentrated in
    exactly our most token-heavy invocations.
  - Open question for the owner: a `schedule:`-triggered agent run inside the off-peak window (idiomatic here — the
    nightly `e2e` and the weekly update checks already run on schedules), or a mid-session discipline of choosing when
    to fire a bulk pass? This overlaps the three parked zero-touch scheduled auditor variants, so one route likely
    implements both.
  - The window and the rates are provider-owned and mutable, and the window is defined in UTC (so any local restatement
    rots with DST): resolve both at `opencode.ai/docs/go` rather than pinning them in the repo — the dated snapshot
    above is context for this note, not a figure any artifact should carry.
  - Not the biggest lever: model routing (already done) is deterministic and fires every turn, and review-round count
    matters more than time-shifting a single round.

- Merge the parked scheduling/cost ideas into one scheduled unattended-run workflow — parked; no change yet. The three
  zero-touch scheduled variants, the off-peak quota note above, and the upstream-reference watcher (2026-09-14) all want
  the same mechanism: a `schedule:`-triggered OpenCode run that fires issues. One workflow implements all of them,
  time-shifts the `deepseek-v4-pro` quota, and removes the on-demand review-round cost of the audits.
- State once where agent-only tooling lives — parked; no change yet. `scripts/` holds repo tooling whatever the caller
  (the human-documented `setup-idea.sh`, its script-only helper, and the agent-invoked `experience-analysis.sh`), while
  `.opencode/` holds runtime config, agents, commands, and skills. This recurred when the owner asked whether
  `experience-analysis.sh` belonged in `scripts/`; the answer currently lives only in that script's header.
- Pre-commit guard over the staged set — parked; no change yet. Four recurring, review-caught errors are mechanically
  detectable before a commit: staged files `spotlessCheck` would modify, staged generated artifacts
  (`scripts/__pycache__/*.pyc`), paths staged and then edited, and `captured:` marker placement (a marker on a plain
  bullet, or one not at the end of its rule — the 2026-09-19 retrospective found the slip recurring, each time from a
  script deriving item boundaries wrongly rather than from any ambiguity in the rule).
- Make the next phase a product phase — parked; no change yet. The first retrospective's recommended direction (see
  `docs/retrospectives/2026-09-16.md`). The tooling is mature enough to be used rather than extended. The highest-value
  parked candidates are narrowing the `query-api` re-export and web-UI trace propagation (the architecture's missing
  _enforce_ layer now exists — ADR-0010).

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

- Intent questions the widened `/audit-architecture` sweep surfaced — parked; no change yet, awaiting the owner's
  answers. The change that widened the auditor (`widen-architecture-auditor-to-intent-gaps`) ran it once, and the run
  reported these deliberate choices whose rationale is not recorded anywhere as questions for the owner:
  - Why is Axon Framework pinned to 4.x (`config/dependency-updates/major-disabled.properties`)? The entry's comment
    points at `showcase/quality/dependency-management`, which carries no Axon requirement — a dead pointer, and the
    rationale unrecorded, even though the repo vendors the `axon4to5-*` migration skills.
  - Why are the command/event/query/DTO value types Lombok classes rather than records? Fourteen
    `@SuppressWarnings("ClassCanBeRecord")` annotations across the API modules and the gateway encode the choice (on
    value types and a mapper); `AGENTS.md` records the Lombok convention and the `CodeBlock2Expr` suppression
    convention, but never why records are rejected.
  - Why are the legacy OpenSearch high-level REST clients excluded (`showcase-projection-model`,
    `showcase-projection-service`, `showcase-query-service`, `showcase-query-client`) in favour of `opensearch-java`? No
    ADR, spec, or `AGENTS.md` sentence records it — distinct from the parked deprecated low-level
    `RestClientBuilder`/`RestClient` idea.
  - Should ADR-0009's Decision name the `axon-server-connector` exclusion repo-wide? It says "both the gateway and the
    command service", but the exclusion also appears in `showcase-projection-service`, `showcase-query-service`, and the
    two clients' test suites — covered only by implication. Deliberate enumeration, or was the rest incidental?
  - Is `@SuppressWarnings("FutureReturnValueIgnored")` on `ShowcaseRestController`'s list/by-id paths a deliberate
    fire-and-forget cache write or a latent bug? The sweep reported it at low confidence; the rationale is unrecorded.

- An upstream-reference report — parked; no change yet. Several durable-artifact notes point at upstream issues we are
  waiting on, and the first closures have already gone unnoticed: `ben-manes/gradle-versions-plugin#755` closed
  2026-08-06 (PR #1060) and `spring-projects/spring-data-elasticsearch#3334` closed 2026-08-30 (PR #3337) — at park time
  both constraint notes still read as open, and no gate reads them. Neither closure retires its note by itself — `#1060`
  covers platform-sourced constraints and changes nothing for our `checkBuildEnvironmentConstraints` row, and the
  `#3334` fix reaches us only through a future `spring-data-opensearch` (that retirement is parked separately below) —
  which is the point: a closure is a trigger to check, not an answer. Five others remain open —
  `build-extensions-oss/gradle-helm-plugin#145`, `anomalyco/opencode#48100`, `anomalyco/opencode#49127` (the action's
  cache step), `Fission-AI/OpenSpec#1891` (the unquoted `: ` class) and `Fission-AI/OpenSpec#1892` (the
  unparseable-config class) — each now carrying an inline close-out. The repository already has the shape for a watcher:
  `dependencyUpdates` / `helmUpdates` / `buildpackUpdates` / `toolingUpdates` are each a small task plus a weekly
  observational workflow that opens or updates an issue and mentions the owner when something is actionable. A report
  would collect the references from `AGENTS.md`, `README.md`, `docs/adr/`, and **`docs/ideas.md`** — the corpus is all
  `owner/repo#NNN` plus one non-GitHub id (`KAFKA-18281`) — resolve them through the relevant API, and report the ones
  that closed or went quiet, turning the references into a checked corpus rather than claims. `docs/ideas.md` belongs in
  the scope though it reads as a scratchpad: it carries more `owner/repo#NNN` references than `README.md` and
  `docs/adr/` combined, including the two closures this entry records, so a report that skipped it would leave those
  references untracked. Worth building now: the trigger it describes has already fired twice.

- Retire the `NANOS_DATE_PATTERN` workaround once its fix reaches us — parked; no change yet.
  `spring-projects/spring-data-elasticsearch#3334` closed 2026-08-30 (PR #3337, milestone 6.2.0-M2), but we resolve
  spring-data-elasticsearch 5.5.13 on the `spring-data-opensearch` 2.0.7 line (2.0.7 declares 5.5.12; the Spring Boot
  3.5.16 BOM raises it), so the truncation is still live: when a `spring-data-opensearch` release carries 6.2.0-M2 — the
  retirement the upstream-reference report's `#3334` finding points at — drop the custom pattern and the gotcha that
  guards it. The other closure candidate does not apply — `#1060` leaves our `checkBuildEnvironmentConstraints` row
  untouched (see the entry above). Recorded here rather than in the PR body that surfaced it, which no tool reads.

## 2026-09-13

- Query-api → command-api re-export (dependency hygiene) — parked; no change yet. Found by the first
  `/audit-architecture` run (its boundary check): `showcase-query-api` declares `api(project(":showcase-command-api"))`
  although its main source needs only `showcase.identifier.KSUID` and uses no `showcase.command.*` type, so it
  re-exports the whole write-side API to every query-api consumer. ADR-0010 sanctions that direction for now, since the
  consumers lean on the transitive `hibernate-validator` it carries — which is what this idea removes. The fix is
  **not** a one-line swap: replacing the dep with `api(project(":showcase-identifier-extension"))` fails
  `showcase-query-client`'s main compile (`ShowcaseQueryClientProperties` imports `org.hibernate.validator.constraints`,
  which it was receiving transitively through command-api), so the modules that leaned on the accidental chain must
  first declare what they actually use. Worth its own change: narrow query-api, add the consumers' explicit deps, and
  let the build prove the graph.

- ADR revisit triggers for time-bounded decisions — parked; no change yet. ADR-0003 and ADR-0004 are explicit deferrals
  whose entire point is to be revisited when a stated condition is met: ADR-0003 (retain Jackson 2; adopt Jackson 3 only
  once Axon and the OpenSearch client support it — an external gate) and ADR-0004 (defer Spring Boot 4; reopen when
  there is capacity — an internal one). Both _state_ their condition in prose in their Decision, but nothing _surfaces_
  it: the ADR template has no `Revisit when:` field, no check watches for the condition, and `Status` only records a
  replacement after the fact — so a deferral silently becomes permanent until someone remembers it. Add a
  `Revisit when:` line to the template (and to the two existing deferrals), and decide how a due trigger reaches a
  human: the `architecture-auditor` checks `Status` integrity and a Decision contradicted by the code, but not whether a
  deferred decision's condition has since been met, so either extend it to flag a deferred ADR whose condition looks
  met, or list such ADRs in a small report alongside the dependency-update checks. Distinct from status drift: the
  decision still holds, its premise may not.

- Scheduled architecture audit — parked; no change yet. The `architecture-auditor` audits the architecture on demand; a
  zero-touch periodic variant would run it unattended, the same way the scheduled AGENTS.md and spec-corpus audits are
  parked. The mechanism is identical (a weekly `.github/workflows/` run of the OpenCode GitHub action with an
  `on: schedule` `prompt`), so all the scheduled audits could share one workflow — audit every artifact and open or
  update a single findings issue. Same permissions and caveats as the AGENTS.md entry (`2026-09-12`): no user context to
  permission-check, so every write must be granted explicitly, and the scheduled `prompt` path needs confirming before
  relying on it.

## 2026-09-12

- Scheduled spec-corpus audit — parked; no change yet. The `specs-auditor` subagent audits `openspec/specs/` on demand;
  a zero-touch periodic variant would run it unattended, the same way the scheduled AGENTS.md audit below is parked. The
  mechanism is identical (a weekly `.github/workflows/` run of the OpenCode GitHub action with an `on: schedule`
  `prompt`), so the scheduled audits could share one workflow when the first is built — audit every artifact and open or
  update a single findings issue. Same permissions and caveats as the AGENTS.md entry below.

- Scheduled AGENTS.md audit — parked; no change yet. The `agents-auditor` subagent audits `AGENTS.md` and the
  project-owned `.opencode/` files on demand; a zero-touch periodic variant would run it unattended. The OpenCode GitHub
  action supports `on: schedule`, which — unlike a comment trigger — has no comment to read, so it requires a `prompt`
  input (see its docs' "Schedule Example"). A weekly `.github/workflows/agents-audit.yml` (`schedule:` +
  `workflow_dispatch:`) could run `anomalyco/opencode/github@latest` with the existing `OPENCODE_API_KEY` secret and a
  prompt to audit `AGENTS.md` and open or update an issue with the findings — mirroring `dependency-updates.yml`. It
  needs `id-token: write` (the action authenticates to the OpenCode GitHub App via OIDC, as `opencode.yml` does),
  `contents: read` for the checkout, and `issues: write` to post the findings; per the docs, a scheduled run has no user
  context to permission-check, so every write it performs must be granted explicitly. Confirm the scheduled `prompt`
  path works before relying on it.

## 2026-09-10

- Rethink or rewrite the load tests — parked; no change yet. The current Gatling setup (`load-tests/src/gatling/java`,
  `ShowcaseSimulation`) is a single probabilistic scenario exercising the API gateway (list, then schedule/start/
  finish/remove with decreasing probability) with per-profile pass assertions — it predates the distributed command bus,
  the web UI, and the current architecture and has not kept pace with the system it tests. Revisit the scenario mix
  (include the query service / Protobuf paths, the SSE stream, the web UI), the injection profiles and pass assertions,
  whether load tests should run against the compose stack or the Helm deployment, and how results feed the
  requests/limits baselines (see the resource-sizing idea below).

- Grafana ingress + hostname instead of port-forward — parked; no change yet. The README documents observability access
  via `kubectl port-forward -n monitoring svc/kps-grafana 3000:80`, which is inconvenient. Add an ingress for the
  Grafana service (e.g. hostname `axon-showcase-grafana`) plus a `setup-hosts.sh` entry, mirroring the app's
  `axon-showcase-api`/`axon-showcase-ui` hostnames, so Grafana — and the Tempo data source inside it — is reachable by
  hostname with no port-forward. Check whether the kube-prometheus-stack chart exposes `grafana.ingress` to enable, and
  whether `setup-hosts.sh`'s LoadBalancer-address detection needs a monitoring-namespace case.

## 2026-09-07

- Measure code coverage for the web UI — parked; no change yet. The JVM modules have a JaCoCo coverage gate
  (`jacocoTestCoverageVerification`, baseline in `config/jacoco/coverage-baseline.properties`), but `showcase-web-ui`
  (Vitest) has no coverage measurement. Explore wiring Vitest's built-in `--coverage` (via `@vitest/coverage-v8`) into
  the frontend `check`, and whether a coverage gate (threshold) makes sense for the UI or just a reporting step.

- Client-side (RUM) observability for the web UI — parked; no change yet. The deployable-UI change adds only server-side
  nginx metrics (stub_status + ServiceMonitor); the UI's user-facing experience is still unobserved. A separate UI
  change would add: (1) web-vitals + JS-error reporting (e.g. Grafana Faro or a push-to-gateway metrics endpoint), and
  (2) W3C Trace Context propagation (`traceparent` header) on API calls so the browser's requests join the existing
  Tempo traces from the gateway onward — the highest-leverage piece, since the pipeline already traces gateway →
  command/query. Consider whether the gateway CORS needs to allow the trace header.

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

## 2026-09-02

- Remove the gateway's blocking-execution routing — parked; keep for now. `ShowcaseBlockingExecutionConfigurer`
  (`configureBlockingExecution(__ -> true)`) routes every controller method to a blocking scheduler. It was added in
  `fadc7bc` ("fixed blocking issues using reactor blockhound") as a global workaround, but the controller is fully
  reactive (the only blocking call, `IdentifierFactory.generateIdentifier()`, is already offloaded via
  `subscribeOn(boundedElastic)`). It's a coarse band-aid, not the right fix — the real work is rooting out whatever
  still trips BlockHound in the error/validation path and offloading it surgically, then dropping the global routing
  (and the `@WebFluxTest` configurer-discovery complexity it forces). Split off CORS into `ShowcaseApiConfigurer`.

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
