# Ideas

Short notes to remember emerging development ideas. An idea becomes an OpenSpec change only when acted on — this file
is a scratchpad, not a backlog of planned work. An idea is removed from the list once implemented (captured by a
change); only open, not-yet-implemented ideas remain.

## 2026-09-04

- Root Prettier for markdown — parked (option A from the formatting discussion); no change yet. Automate markdown
  formatting the way Java (Spotless) and the web UI (Prettier) already are: add a root Prettier config with
  `proseWrap: "always"` and `printWidth: 120` (not the Prettier default `preserve`), plus a `format` / `format:check`
  pair wired into CI. This ends the manual 120-char wrapping convention for `docs/`, `AGENTS.md`, `README.md`, and
  OpenSpec specs. Cost to accept: a one-time reflow of existing markdown on first run (may reflow long inline code,
  tables, and `→`/`—` sequences), and a separate decision on whether YAML gets included in the same Prettier run.

- Dependency updates for the web UI — parked; explore whether it's possible to check frontend dependency updates the
  way the JVM modules do (`dependencyUpdates`), and whether we can also scan the web UI for dependency
  vulnerabilities. The `dependency-updates` machinery currently only covers catalog-owned Gradle coordinates; the web
  UI's npm dependencies (`package.json` / `package-lock.json`) are outside it. To explore: `npm outdated` /
  `npm audit` (and `npm audit --omit=dev`) as analogues of `dependencyUpdates` / `dependencySecurityCheck`, wired as
  Gradle tasks or npm scripts and reported like the existing update/security issues; whether the observability chart
  has no `*-image-tag` for the UI (it would be an nginx image once the UI is a dedicated deployable — see that idea),
  so a UI image-tag bump would be a manual coordinate; and whether Snyk can also scan `package-lock.json` (the
  existing `dependencySecurityCheck` uses the Snyk CLI with the root `.snyk` policy).

- Document the IDE-tooling MCP setup in the README — parked; recommended resolution for the MCP-config question. The
  repo uses several MCPs but only Playwright is in the project config (`.opencode/opencode.json`); the rest live in
  the user's global `opencode.jsonc` and are mostly undocumented: `github` (PRs, CI check status, `gh`), `steroid`
  (IntelliJ/IDEA tooling — `steroid_execute_code` / `runInspectionsDirectly`, used by the `codefmt` flow), and
  `amplicode` (Spring/Amplicode plugin tools). Contributors can't discover these exist or how to enable them. The
  split is correct (auth/IDE-bound tooling belongs in global config — copying it into the project would not enable it
  without the contributor's own setup), so the fix is documentation, not config duplication: a README note covering
  (1) the global GitHub MCP setup (`gh auth login` + the `gh mcp` entry), (2) Steroid/Amplicode for IDEA-based work
  and which of them help with repo-specific problems (Steroid for the Spotless/`codefmt` inspection flow, Amplicode
  for Spring/Axon work), and (3) that Playwright is project-configured for the web-UI e2e.

- Ship the web UI as a dedicated deployable unit — parked; no change yet. Today `showcase-web-ui` is a build-only
  module (`npmBuild` → `build/dist`): no Docker image is built and nothing serves it in deployment — it runs only via
  the Vite dev server (`viteDev`) or preview, so the Docker/Helm stack has no UI. The UI is a standalone deployable
  (not served by the API gateway — decided): (1) build a dedicated UI image (an nginx image serving `build/dist`),
  (2) add it to the Helm chart as its own deployment/service (not under the gateway) including its values and the
  CORS origin it needs, and (3) wire the boot image + chart values so the UI is reachable in the local and deployed
  stacks.

- Enforce web UI conventions with tooling — parked; do as its own change after `add-web-ui` is merged. Prettier is a
  formatter, not a style linter: it gates formatting (width, quotes, semicolons) but not *conventions*. ESLint
  (correctness) and tsc (types) gate their slices, but two convention areas are currently human-review/AGENTS.md-only:
  (1) **FSD import-direction rules** (slices import only downward; `app/` → `pages/` → `widgets/` → `features/` →
  `entities/` → `shared/`) — enforce with `eslint-plugin-boundaries`, with public-API boundaries per slice; and
  (2) **naming conventions** (e.g. `use*` hooks, `*.test.ts(x)`/`*.spec.ts` suffixes) — via ESLint rules or
  `eslint-plugin-import` naming. Keep it out of the current change to keep the review focused; verify existing code
  conforms (it was built cleanly) and let CI gate it from then on.

## 2026-09-03

- Extend `scripts/setup-idea.sh` for the web module and audit what's stale there. Today the script only covers the
  Java/Gradle side: it installs the `palantir-java-format` + `ktfmt` plugins, copies `config/idea/*.xml` (Java code
  style, ktfmt, codeStyleConfig), and upserts the test-tier naming inspection. It is now incomplete for
  `showcase-web-ui` and should be extended (and its assumptions re-checked): (1) explore whether there is a Prettier
  plugin for IDEA (and if so, install it) so `Reformat Code` matches the web module's `prettier --check` gate — note
  IDEA ships built-in Prettier support that may need enabling rather than a separate plugin; add it into the setup
  script's plugin installs and add a 2-space JS/TS code-style scheme — IntelliJ's default is 4 spaces, which disagrees
  with the web module's `prettier --check` gate (`.prettierrc` uses `tabWidth: 2`), the same "IDE vs build gate" drift
  the script already fixes for Spotless; (2) the JS/TS code style must not collapse to wildcard imports (the Java
  palantir layout sets
  `ij_java_imports_layout`; the TS side needs the equivalent single-import + on-demand-count preference so Prettier
  never has to expand a wildcard by hand); (3) the inspection-profile upsert is Java-only — the web module's Vitest
  naming (`.test.ts(x)`) has no IDEA inspection yet; (4) audit the script for other stale bits, e.g. whether it
  should also configure the Node plugin / `@/` alias awareness (tsconfig paths are picked up automatically, but the
  npm tasks and 2-space scheme are not), and whether the "IDE must be closed" abort + installPlugins flow still holds
  on current IDEA builds. **Also investigate a durability problem: on reimport of the Gradle project, IDEA regenerates
  the `.idea` directory completely and overwrites the files `setup-idea.sh` added (the committed `config/idea/*.xml`
  copies and the inspection-profile upsert), so the setup is not idempotent across Gradle reimports. Find a way to
  make it survive — e.g. store the config so IDEA's reimport preserves it (shared workspace XML / `*.idea` git-tracked
  scheme files, a `.idea/codeStyles` scheme referenced by name rather than an inline copy, or a re-import hook / Gradle
  task that re-applies the setup), and verify the flow after a fresh Gradle reimport. **Also fix the running-IDE
  ordering: the script aborts before `ensure_project_config` when the IDE is running, so a running IDE blocks the
  config-file install too — even though copying `config/idea/*.xml` and upserting the inspection profile do not need
  the IDE closed (only `installPlugins` does). Split the flow so `ensure_project_config` runs regardless, and only the
  plugin install requires the closed IDE (e.g. warn-and-skip plugin install when running, or re-order to apply config
  first then install plugins).

## 2026-09-02

- Remove the gateway's blocking-execution routing — parked; keep for now. `ShowcaseBlockingExecutionConfigurer`
  (`configureBlockingExecution(__ -> true)`) routes every controller method to a blocking scheduler. It was added in
  `fadc7bc` ("fixed blocking issues using reactor blockhound") as a global workaround, but the controller is fully
  reactive (the only blocking call, `IdentifierFactory.generateIdentifier()`, is already offloaded via
  `subscribeOn(boundedElastic)`). It's a coarse band-aid, not the right fix — the real work is rooting out whatever
  still trips BlockHound in the error/validation path and offloading it surgically, then dropping the global routing
  (and the `@WebFluxTest` configurer-discovery complexity it forces). Split off CORS into `ShowcaseApiConfigurer`.

## 2026-09-01

- Managed-k8s staging for free or cheaply — explored, parked (no change yet). Goal: a managed Kubernetes staging env
  for the chart, ideally free, otherwise as cheap as reasonable. Findings: (1) the two real free-control-plane paths
  are AKS Free (control plane free, but you pay for nodes — not $0 ongoing) and OKE (control plane free + 2 Always
  Free ARM nodes = 12 GB, genuinely $0); (2) Oracle halved the Always Free Ampere A1 to 2 OCPU / 12 GB in June 2026,
  so the full stack (~11-12 GiB with the `kps` + `tempo` observability stack, 5 services, Kafka, OpenSearch,
  Postgres) does **not** fit OKE's free budget with observability enabled — it fits only if staging trims
  observability/single-replica/smaller OpenSearch, and images must be ARM (`-PimagePlatform=linux/arm64` already
  supported); (3) there is no free managed k8s that runs the full chart as-is, always-on, at $0 — every free path
  needs either trimming (OKE), paying for nodes (AKS, ~$40-80/mo), or accepting ephemerality. Reference:
  `nce/oci-free-cloud-k8s` runs OKE free but on a leaner stack. Open decision: how faithful staging must be to the
  observability stack (the real decider between OKE-free and AKS-paid), and whether always-on vs on-demand node-pool
  stop/start changes the budget.
- Web-based UI — build a beautiful web-based UI for the showcase so the CQRS/Event-Sourcing pipeline can be
  interacted with and demonstrated visually rather than only via the REST API / `curl`. Parked; no change yet. Open
  questions for later: single-page app served by the API gateway, what it interacts with (create/browse showcases,
  event timeline), and how it fits the existing read/write-side separation.
