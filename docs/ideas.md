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

## 2026-09-16

- Cost: time-shift discretionary bulk agent work into the provider's off-peak window — parked; no change yet. Our model
  pins sit on a flat-rate, dollar-metered plan, and the DeepSeek models on it are priced in peak/off-peak tiers, so the
  **same pass consumes half the metered quota off-peak** — nothing changes on the invoice ($10/month flat); the lever
  stretches the quota, which is the binding constraint. The shiftable passes mostly run on the `deepseek-v4-pro` pin
  (the three `/audit-*` agents and `/review-thorough`; the pin's fifth agent, `diagrammer`, is interactive), so it is
  the pro quota the saving lands on. At the time of writing the provider's page gives peak as 01:00–04:00 and
  06:00–10:00 UTC, Mon–Fri, with all other hours including weekends off-peak (~79% of the week), and peak at exactly 2×
  off-peak.
  - Only the unattended work is shiftable: the three `/audit-*` sweeps, `/retrospective`, `/review-thorough`, and long
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

- Fold a lesson discovered while writing a capture into that capture's PR — parked; no change yet. The precise fix for
  the #254 → #255 → #256 chain: the docs-only carve-out did not stop it, because a capture's own subject kept surfacing
  _new_ general rules, so a capture whose subject is a capture should ship in the same PR.
- Merge the parked scheduling/cost ideas into one scheduled unattended-run workflow — parked; no change yet. The three
  zero-touch scheduled variants, the off-peak quota note above, and the upstream-reference watcher (2026-09-14) all want
  the same mechanism: a `schedule:`-triggered OpenCode run that fires issues. One workflow implements all of them,
  time-shifts the `deepseek-v4-pro` quota, and removes the on-demand review-round cost of the audits.
- State once where agent-only tooling lives — parked; no change yet. `scripts/` holds repo tooling whatever the caller
  (the human-documented `setup-idea.sh`, its script-only helper, and the agent-invoked `experience-analysis.sh`), while
  `.opencode/` holds runtime config, agents, commands, and skills. This recurred when the owner asked whether
  `experience-analysis.sh` belonged in `scripts/`; the answer currently lives only in that script's header.
- Pre-commit guard over the staged set — parked; no change yet. Three recurring, review-caught git errors are
  mechanically detectable before a commit: staged files `spotlessCheck` would modify, staged generated artifacts
  (`scripts/__pycache__/*.pyc`), and paths staged and then edited.
- Make the next phase a product phase — parked; no change yet. The first retrospective's recommended direction (see
  `docs/retrospectives/2026-09-16.md`). The tooling is mature enough to be used rather than extended. The highest-value
  parked candidates are the ArchUnit fitness functions (the architecture has decide/describe/review layers but no
  _enforce_ one), narrowing the `query-api` re-export, and web-UI trace propagation.

- The gateway's SSE stream has no idle heartbeat and no event `id` — parked; no change yet.
  `ShowcaseEventStreamConfiguration` buffers with `Sinks.many().replay().limit(100)` and `ShowcaseEventStreamController`
  builds `ServerSentEvent` with `.event("showcase")` but no `.id()` and no periodic comment, while `gateway/live-events`
  requires the stream to "stay connected and deliver events continuously" and nowhere in the chart is a proxy timeout
  set. Open questions: does an idle stream survive the deployed proxy, and should a reconnect resume rather than
  re-receive the buffer? The replay itself is deliberate and relied upon (the web-UI spec requires replayed events not
  to trigger reconciliation), so this concerns the gateway's idleness and `id:` semantics, not the buffer. Surfaced by
  the smoke-run of `make-lesson-capture-consolidate`, whose seeded incident was fictional — hence an idea, not a gotcha.

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
  `dependencyUpdates` / `helmUpdates` / `buildpackUpdates` are each a small task plus a weekly observational workflow
  that opens or updates an issue and mentions the owner when something is actionable. A report would collect the
  references from `AGENTS.md`, `README.md`, and `docs/adr/` — the corpus is all `owner/repo#NNN` plus one non-GitHub id
  (`KAFKA-18281`) — resolve them through the relevant API, and report the ones that closed or went quiet, turning the
  references into a checked corpus rather than claims. Worth building now: the trigger it describes has already fired
  twice.

- Retire the `NANOS_DATE_PATTERN` workaround once its fix reaches us — parked; no change yet.
  `spring-projects/spring-data-elasticsearch#3334` closed 2026-08-30 (PR #3337, milestone 6.2.0-M2), but we resolve
  spring-data-elasticsearch 5.5.13 on the `spring-data-opensearch` 2.0.7 line (2.0.7 declares 5.5.12; the Spring Boot
  3.5.16 BOM raises it), so the truncation is still live: when a `spring-data-opensearch` release carries 6.2.0-M2, drop
  the custom pattern and the gotcha that guards it. The other closure candidate does not apply — `#1060` leaves our
  `checkBuildEnvironmentConstraints` row untouched (see the entry above). Recorded here rather than in the PR body that
  surfaced it, which no tool reads.

## 2026-09-13

- Query-api → command-api re-export (dependency hygiene) — parked; no change yet. Found by the first
  `/audit-architecture` run (its boundary check): `showcase-query-api` declares `api(project(":showcase-command-api"))`
  although its main source needs only `showcase.identifier.KSUID` and uses no `showcase.command.*` type, so it
  re-exports the whole write-side API to every query-api consumer — a direction the architecture does not sanction. The
  fix is **not** a one-line swap: replacing the dep with `api(project(":showcase-identifier-extension"))` fails
  `showcase-query-client`'s main compile (`ShowcaseQueryClientProperties` imports `org.hibernate.validator.constraints`,
  which it was receiving transitively through command-api), so the modules that leaned on the accidental chain must
  first declare what they actually use. Worth its own change: narrow query-api, add the consumers' explicit deps, and
  let the build prove the graph.

- Architecture fitness functions (ArchUnit) — parked; no change yet. The architecture is _described_ (the README's
  component table and event-flow diagram, `AGENTS.md`'s service/module/port lists) and _reviewed_ per change, but
  nothing _enforces_ it: the version catalog has no ArchUnit and no module carries a dependency-direction or layering
  test, so the intended structure rests on convention and human review. Nothing fails the build if a service starts
  depending on another service (they are meant to talk only via a `-client`, Kafka, or HTTP — services happen not to
  depend on one another today, so the rule would lock in an already-true property), if a service reaches into another's
  internals instead of its `-api`, or if `build-logic`'s convention plugins leak across layers. A small ArchUnit suite
  (in `showcase-test` or its own module) asserting those rules would turn the intended topology into a build failure the
  way `spotlessCheck` and Checkstyle turn style into one — this is the "constrain" layer, the one genuinely absent from
  the project's architecture management (it has decide = ADRs, describe = README/AGENTS.md, review = the review agents
  and the `architecture-auditor`, but no enforcement). It complements the parked _Enforce web UI conventions with
  tooling_ idea (the same intent on the web module via `eslint-plugin-boundaries`). Interaction with the
  `architecture-auditor`: a fitness function _prevents_ boundary drift, the auditor _detects_ it — once a rule is a
  fitness function the auditor should drop that boundary check rather than re-report a property a gate already enforces.
  Deliberately not a full C4/Structurizr description toolchain: enforcement is the missing layer, not more description
  ceremony.

- Reconcile the architecture description across README and AGENTS.md — parked; no change yet. The same architectural
  facts are stated twice — the README's `## Architecture` section and `## Project Structure` tree (component table,
  event-flow diagram, module tree, for humans) and `AGENTS.md` (the service list and key-module list in its Architecture
  section, the HTTP ports in Local Development, for agents) — and they have already drifted (the review that caught
  "four services and a gateway" as a double-count was reading exactly this pair). The human-facing narrative in the
  README and the agent-facing reference in `AGENTS.md` will always differ in _purpose_, so some restatement is
  intentional, but each _fact_ (the component count, the service roles, the module inventory) should have one canonical
  home, with the other naming it rather than restating it. Explore whether the module inventory in particular is worth
  generating from one source (the Gradle module list) vs. stating it once and cross-referencing, and whether the
  README's component table should include the web UI (it lists four components; the tree lists five service/gateway
  directories). This is the other half of the parked _README auditor_ (which cross-checks the README's claims, ports
  included, against `AGENTS.md`): the audit detects divergence, this idea decides which copy is canonical —
  complementary, not parallel.

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

- README auditor — parked; no change yet. The ~690-line README is human-facing, and its content — unlike its markdown
  formatting, which Spotless gates — has no check, so several gotchas are README errors caught reactively (the "two
  replicas" / "36 panels" / "four services and a gateway" miscounts, the diagram asymmetry, the "22 capability specs"
  tally); two documented conventions — README design intent and Surface human-visible capabilities — have no
  enforcement. A `readme-auditor` subagent (pro model, `/audit-readme`) would check three verifiable axes:
  **accuracy/consistency** (every claim — commands, ports, versions, image and task names, links, the OpenSpec-flow
  diagram's semantics — matches the repo, cross-checked against `AGENTS.md` and the spec corpus); **design-intent
  fidelity** (the README convention: section order, the step-by-step Getting Started path, Gradle tasks over raw
  commands, curl-only, the prompting-exercise narrative); and **coverage / experience surfacing** (the Cool Story and
  every human-visible capability — the saga auto-start, the live SSE timeline, the `setup-hosts` hostnames, the Grafana
  access path — cross-checked against what the system does). Deliberately **not** an "attractiveness" judge: subjective
  quality — prose and structure beyond the documented shape (redundancy, jargon, flow) — belongs in an advisory section
  for the user's judgment, never as a defect, since the README is hand-curated by design ("preserve its intended shape
  on every edit"). Scope: `README.md` only (ADRs, retrospectives, and the other docs are out). Justified by a distinct
  artifact and audience (humans, not agents), following the same auditor pattern.

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

## 2026-09-11

- Snyk CLI update check — parked; no change yet. The `snyk-version` pin in `.github/workflows/snyk.yml` is outside every
  update-check workflow (`dependencyUpdates` covers Gradle coordinates, `helmUpdates` covers the Helm CLI and charts,
  and Dependabot manages action refs but not the `snyk-version` input), so it goes stale silently and can only be
  confirmed by hand against `gh api repos/snyk/cli/releases/latest`. Mirror the `helmUpdates` pattern: a task/workflow
  that queries `snyk/cli` releases and opens or updates an issue when the pin lags. A local bump cannot be fully
  verified anyway (actionlint only lints the YAML; the credentialed weekly run is the first real execution), so the
  check is worth automating rather than relying on manual audits.

- OpenSpec CLI update check — parked; no change yet. The CLI is pinned in CI as
  `npm install --global @fission-ai/openspec@<version>` (`.github/workflows/ci.yml`), so releases go stale silently
  because the pin sits outside every update check (`dependencyUpdates` covers Gradle catalog coordinates, `helmUpdates`
  the Helm CLI and charts, `buildpackUpdates` the Paketo builder and buildpacks, and Dependabot only action refs). Add
  an `openspecUpdates` Gradle task mirroring `buildpackUpdates` — query the npm registry for `@fission-ai/openspec`'s
  latest version, compare with the pinned one, report — plus a weekly `openspec-updates.yml` workflow opening or
  updating an issue. It pairs with the existing `/opsx-tool-update` command, which can act on a release but does not
  detect one; decide where the pin is single-sourced (it lives in the workflow today — the `snyk-version` case above has
  the same shape).

- Tooling-currency checks: unify the mechanism and cover the `pack` pin — parked; no change yet. The repo has three
  update checks of near-identical shape (`dependencyUpdates` via the gradle-versions plugin; `helmUpdates` and
  `buildpackUpdates` as `build-logic` tasks feeding a workflow that opens or updates an issue), two parked ideas for two
  more (the Snyk and OpenSpec CLI pins above, each proposing the same task-plus-workflow shape), and one more uncovered
  pin with no idea yet — `pack-version` in `.github/workflows/e2e.yml` (`buildpackUpdates` covers the Paketo builder and
  buildpacks, not the `pack` CLI). Rather than adding a fourth and fifth near-duplicate task, consider one parameterized
  mechanism: a declared list of pinned tools with their current-version source (Gradle catalog, Helm CLI/charts, Docker
  Hub, npm, GitHub releases) driving a single report and update workflow — shrinking the AGENTS.md manual-pin audit list
  to the tools it cannot reach.

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

- Fix the per-service compose tasks' "no such service" failure — parked; no change yet. The `docker-conventions`
  per-service `compose*` tasks pass `project.name` (e.g. `showcase-api-gateway`) as the compose service argument, but
  the `docker-compose.yml` services are named differently (`api-gateway`, `command-service`, `query-service`,
  `projection-service`, `web-ui`, `db-events`, ...), so `./gradlew :showcase-api-gateway:composeStop` fails with
  `no such service`. Fix: map each module to its compose service name (or drop the service argument and rely on the
  compose project scope).

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
