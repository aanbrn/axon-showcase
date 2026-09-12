# axon-showcase

A CQRS / event-sourced reference application you can _watch work_ — and a demonstration of an agent-assisted,
spec-driven way to build it.

It is not a toy CRUD app. It is a complete event-sourced system: schedule a showcase and it drives itself through its
lifecycle — starting at the right time, finishing after its duration, streaming every event live to a browser UI that
updates as it happens. And the way the code is written and reviewed is itself a demonstration: behavior is captured in
specs, changes are proposed, applied, reviewed, and archived by an automated agent pipeline. That pipeline also **learns
from itself**: each change's lessons are written back into the agent's instructions, so the next change starts a little
smarter (see [The Self-Learning Loop](#the-self-learning-loop)). It can even bootstrap its own tooling — ask it to set
up the MCP servers and it detects, wires, and installs what it can for you (see
[Tooling MCP Servers](#tooling-mcp-servers)).

## Project Structure

```
axon-showcase/
├── Services and gateway
│   ├── showcase-api-gateway/            # REST entry point (/showcases), SSE live events (/events)
│   ├── showcase-command-service/        # Write side: Axon aggregate, saga, distributed bus
│   ├── showcase-projection-service/     # Consumes Kafka, writes read models to OpenSearch
│   ├── showcase-query-service/          # Read side: queries OpenSearch, Protobuf query API
│   └── showcase-web-ui/                 # Standalone browser UI (React + Vite, Feature-Sliced Design)
├── API and clients
│   ├── showcase-command-api/            # Command-side API interfaces
│   ├── showcase-command-client/         # Reactive command client
│   ├── showcase-query-api/              # Query-side API interfaces
│   ├── showcase-query-client/           # Reactive query client
│   └── showcase-query-proto/            # Protobuf definitions for queries
├── Shared libraries
│   ├── showcase-projection-model/       # Shared query model definitions
│   ├── showcase-identifier-extension/   # KSUID identifier support
│   ├── showcase-mapstruct-extension/    # MapStruct extensions
│   ├── showcase-resilience4j-extension/ # Resilience4j integration
│   └── showcase-test/                   # Shared test utilities
├── Build and deployment
│   ├── build-logic/                     # Gradle convention plugins
│   ├── gradle/                          # Version catalog (libs.versions.toml)
│   ├── platform/                        # Shared BOM and dependency management
│   ├── helm/                            # Helm charts for Kubernetes deployment
│   │   └── chart/                       # Main chart for the application
│   ├── docker-compose.yml               # Local development with Docker Compose
│   └── load-tests/                      # Gatling-based load tests
├── Documentation and process
│   ├── docs/adr/                        # Architecture decision records
│   └── openspec/                        # Spec-driven behavior specs and changes
├── Scripts
│   ├── db.sh                            # Database setup (init / drop / reset)
│   ├── setup-hosts.sh                   # Manage /etc/hosts entries for the local ingress
│   └── scripts/                         # Dev tooling: setup-idea.sh, experience-analysis.sh
```

## The Cool Story

The domain is deliberately simple — a "showcase" is a scheduled, timed event with a lifecycle. The value is in _what the
plumbing does with it_:

```
Scheduled ──(saga deadline: startTime)──► STARTED ──(saga deadline: +duration)──► FINISHED
    │                                                                              │
    └────────────────────────────── REMOVED (any time) ────────────────────────────┘
```

- **The saga runs the show.** When you schedule a showcase, an Axon **saga** (in `ShowcaseSaga`) sets a deadline to
  start it at the scheduled time, then another to finish it after the configured duration. You can schedule a showcase
  and literally watch it auto-start and auto-finish without touching anything.
- **Everything is an event.** Every transition is an Axon domain event, stored in the PostgreSQL event store, published
  to Kafka, projected into OpenSearch, and streamed live to browsers over **SSE** (`/events`).
- **The command side scales.** The command service runs a **distributed command bus** (JGroups). In the local deployment
  its two replicas and the API gateway form one JGroups cluster — peers discover each other through the Kubernetes API
  (KUBE_PING) and commands route across all nodes — so the write side scales out like a real system. Every service can
  be autoscaled (HPA/VPA) and protected with Pod Disruption Budgets.
- **A real browser UI shows it live.** The React UI renders the event timeline, listens to the SSE stream, and
  reconciles against the eventually-consistent read model — so you see the saga's transitions appear live.
- **Resilience is built in.** The command and query clients apply **Resilience4j** circuit breakers, time limiters, and
  retries, and the gateway falls back to a cache when the query side is unavailable.
- **It is observable.** Tempo traces, Prometheus metrics (including web-UI nginx metrics), a dedicated Grafana
  dashboard, health checks, and Gatling load tests are wired in — the same observability a production service needs.
  Observability is available in the **Kubernetes deployment** (Prometheus, Grafana, and Tempo run in the `monitoring`
  namespace); the local docker-compose stack runs without it.
- **Identifiers are KSUIDs** (sortable, collision-resistant), enforced through a custom identifier extension.

## Architecture

The application follows **CQRS (Command Query Responsibility Segregation)** with four components:

| Component              | Role                                                                                        |
| ---------------------- | ------------------------------------------------------------------------------------------- |
| **API Gateway**        | REST entry point (`/showcases`), SSE live events (`/events`)                                |
| **Command Service**    | Write side: Axon aggregate, saga, distributed command bus (JGroups), PostgreSQL event store |
| **Projection Service** | Consumes events from Kafka, writes read models to OpenSearch                                |
| **Query Service**      | Read side: queries OpenSearch, Protobuf query API                                           |

### Event Flow

```
Write: Client → API Gateway → Command Service → (Kafka) → Projection Service → OpenSearch
                              └─ Axon event store (PostgreSQL)                     │
Read:  Client → API Gateway → Query Service → OpenSearch ◄─────────────────────────┘
SSE:   Command Service → (Kafka) → Gateway /events → Client
```

A scheduled showcase flows through the whole pipeline: the command service stores the event and publishes it, the
projection service builds the read model, the query service serves it, and the gateway streams the event live to any
subscribed browser — all from one `POST /showcases`.

## Technologies

### In the System

- **Java 21** and **Spring Boot 3.5.16** (a Spring Boot 4 migration is deferred — see `docs/adr/0004`)
- **Axon Framework** — aggregates, sagas and deadlines, command/query buses, distributed command bus via **JGroups**
- **PostgreSQL** — the Axon event store
- **Apache Kafka** — event streaming between services
- **OpenSearch** — the read-side projection store
- **React + Vite + TypeScript** — web UI (TanStack Query, Redux Toolkit, React Hook Form + Zod, Vitest, Playwright),
  Feature-Sliced Design
- **KSUID** identifiers, **MapStruct** mapping, **Resilience4j** resilience, **Protobuf** inter-service queries
- **Helm** + **Kubernetes** — deployment (HPA/VPA/PDB, network policies, ingress)
- **Prometheus / Grafana / Tempo** — metrics, a custom observability dashboard, and distributed tracing (in the
  Kubernetes deployment)
- **Gatling** — load tests

### In the Process

- **Gradle** (Kotlin DSL) with **build-logic convention plugins** and a version catalog
- **Spotless** — palantir-java-format for Java, ktfmt for Kotlin/Gradle DSL, Prettier for markdown
- **Checkstyle, SpotBugs, ErrorProne (NullAway + JSpecify), JaCoCo coverage gate** — all in `check`, no IDE required
- **actionlint** — lints the GitHub Actions workflows
- **Snyk** — dependency security scanning
- **OpenSpec** — spec-driven behavior capture (`propose → apply → archive`)
- **OpenCode** — the agentic coding tool driving the process: slash-commands, spec-aware subagents, a self-learning
  lesson-capture loop, and on-request setup of its own tooling ([Tooling MCP Servers](#tooling-mcp-servers))
- **GitHub Actions** — CI, e2e, dependency updates, helm updates, security scans

## Development Workflow

> Just want to run the project? Skip to [Getting Started](#getting-started).

### Spec-Driven Development

This repository is built spec-first. Behavior is captured as OpenSpec specs under `openspec/specs/` (the behavioral
source of truth) and changes are planned under `openspec/changes/` using the propose → apply → archive workflow (via the
`/opsx-*` OpenCode commands / `openspec-*` skills). `AGENTS.md` records the development workflow and conventions — read
it before contributing.

Every change flows through the same loop:

```
                           ┌─────────────────────────────── human approves ───────────────────────────────┐
                           │                                                                              │
  Idea  ──►  Explore  ──►  Propose  ──►  Review  ──►  Apply  ──►  Review  ──►  PR  ──►  Archive  ──►  Merge
                           │ (spec delta)         (code+tests)            (CI green)          │
                           │         (auto+manual)            (auto+manual)                   │
                           │         (optional PR)                      (mandatory PR)        │
                           │                                                                  │
                           └───────────────────── delta spec → main spec ─────────────────────┘
```

An idea becomes a **proposal** (what and why), then a **spec delta** (the new behavior, scoped to a capability), a
**design** (how), and **tasks**. Applying writes the code and tests. Each phase ends in **two reviews**: an
**auto-review** (the `review-quick` subagent, repeated until it finds nothing new) and your **manual review**. The
change's single **PR** may be opened as a draft after the proposal review (to share the plan) and is opened for real
after the implementation review — it's the same PR, on which CI runs. Once CI is green and you approve, the change is
**archived** — the change dir moves to `openspec/changes/archive/` and its delta spec is folded into the main spec — as
a commit in the same PR, then merged. So the main spec always describes behavior the code has been verified against.

A vague idea is usually sharpened first in **explore mode** (`/opsx-explore`) — a thinking stance that investigates the
codebase and clarifies what to build, without writing code. Only when the idea is concrete enough does it become a
proposal.

The loop is **iterative, not a strict pipeline** — you can step back and rework at any point. A question in review can
send you back to the proposal, a realization mid-implementation to the spec delta; the agent revises the affected
artifacts (`/opsx-update` reworks a change's planning artifacts) and re-verifies. The loop repeats until the change is
right — the archive is a snapshot of a settled state, not the end of a one-way flow.

The specs are organized by architectural role (`gateway`, `write-side`, `read-side`, `clients`, `extensions`,
`deployment`, `quality`) — 22 capability specs covering everything from the REST API and the event pipeline to the
identifier extension and the dependency-management policy. Every implemented change is archived under
`openspec/changes/archive/` (120+ and counting), so the spec structure itself tells the project's history: the main spec
is always in sync with behavior the code has been verified against, and a change's delta spec shows what a specific
feature introduced.

Emerging ideas are parked in `docs/ideas.md` — a lightweight, date-grouped scratchpad (added to via `/ideas`, removed
once implemented or decided against) rather than a backlog of planned work. A parked idea is the usual starting point
for an explore session that decides whether it deserves a proposal.

An idea isn't the only entry point, though: a session can start just as well from a reported issue, a failing CI job, or
any other observation — you describe it in a prompt; the agent investigates (reading the issue or the failing check
through the GitHub MCP) and proposes from there. The loop is identical whatever kicked it off; `docs/ideas.md` holds
un-acted ideas — it is not a required gate.

Cross-cutting architecture decisions and their rationale are recorded as Architecture Decision Records under
`docs/adr/`. OpenSpec captures what the system does and how a change is planned; ADRs capture why the system is shaped
the way it is.

### The Agentic Process

This repository is developed through a **spec-first, agent-assisted workflow** powered by
[OpenCode](https://opencode.ai) — an AI coding agent you drive interactively from its **TUI** (terminal) or **Desktop**
app. You describe what you want in plain language, and the agent does the work: it proposes a plan, writes the code,
runs the gates, reviews itself, and opens the PR. You steer and approve; the agent implements.

The repo's `.opencode/` config pins its agents to `opencode-go/*` models, available through an **OpenCode Go**
subscription — install OpenCode (see [Prerequisites](#prerequisites)), subscribe, and `/connect` to it to follow along
with the worked scenarios.

The OpenCode agents under `.opencode/agent/` form a layered quality pipeline:

| Agent                 | Role                                                                                    |
| --------------------- | --------------------------------------------------------------------------------------- |
| `experience-analyzer` | Periodic retrospectives + improvement suggestions (system & process) — `/retrospective` |
| `lesson-capture`      | Captures gotchas/conventions into AGENTS.md after every change (automatic)              |
| `review-quick`        | Fast review after proposal & implementation, repeated until clean (automatic)           |
| `review-thorough`     | Deep on-demand review (drift, correctness, architecture) — `/review-thorough`           |
| `vision`              | Reads screenshots for the text-only main agent                                          |
| `diagrammer`          | Draws/fixes ASCII diagrams with the pro model — `/diagram`                              |

#### What the Agent Automates

- **Proposing and applying changes**: the `opsx-*` commands scaffold a change (proposal, design, tasks, spec delta),
  implement it, and prepare it for review.
- **Code review**: every change is auto-reviewed after its proposal and after its implementation; the quick-review loop
  repeats until it finds nothing new. A deep `/review-thorough` pass is available on demand.
- **Lesson capture**: after each change, `lesson-capture` proposes AGENTS.md gotchas and conventions — so mistakes are
  recorded systematically instead of relying on memory.
- **Retrospectives**: `/retrospective` gathers the last week of PRs and changes and produces a sprint retrospective with
  improvement suggestions.
- **Formatting, gates, CI, PRs**: formatting and quality gates run in the build; the agent opens PRs, watches CI, and
  merges them once green.

#### What the Human Decides

Developing on this repo is mostly a **prompting exercise**: you tell the agent what to build, and it plans, codes,
tests, reviews, and ships. You almost never edit files by hand — the manual work is deciding and approving:

- **Scope and direction**: what to build, and what an idea becomes.
- **Approval at each step**: the agent proposes; you approve the proposal, the implementation, and the merge.
- **Archiving**: a change is archived only after CI is green and you approve.
- **Applying suggestions**: retrospective improvements and lesson captures are proposed by the agent and applied by your
  judgment.
- **Merging**: on this repo, the owner merges PRs directly (admin) once CI is green; a non-admin follows the normal
  review-required flow.

#### A Worked Scenario

Here is what implementing a feature actually looks like — say you want showcases to support a custom title color. You
sit in the OpenCode TUI (or Desktop) and type:

```
Add a custom title color to showcases. The API should accept an optional color in the create request, the command
service should validate it, and the UI should render the title in that color.
```

That one prompt starts the whole loop. The agent:

1. **Proposes** (`/opsx-propose`): scaffolds the change under `openspec/changes/` — a proposal, design, tasks, and a
   spec delta that states the new behavior — then runs a quick self-review of the plan and shows it to you.
2. **You approve the proposal**: reply "looks good", and the agent starts implementing.
3. **Implements** (`/opsx-apply`): follows the design's tasks — the new API field, the validation, the UI color — writes
   the code and the tests, and runs the gates (`spotlessApply`, the module's `check`).
4. **Auto-reviews**: after the implementation it runs the quick-review again, fixes anything it finds, and reports back
   what changed and what is verified.
5. **You review the diff**: you look at the actual changes and ask for tweaks ("also validate the hex format"). The
   agent applies them — and if a tweak changes the intended behavior, the spec delta is reworked to match, not just the
   code.
6. **Ships**: on your go-ahead it pushes a branch and opens a PR and watches CI until green.
7. **You approve archiving** (`/opsx-archive`): the change dir moves to the archive and the main spec is updated to
   match — as a commit in the same PR — then it merges. The feature is now part of the source of truth. On this repo the
   owner's merge is direct (admin); the agent only merges with your approval.

Throughout, the only manual work was the initial prompt and a few approvals. The agent wrote the plan, the code, the
tests, and the PR; you steered.

#### The Self-Learning Loop

The process is designed to **learn from itself** — and that is the mechanic, not a metaphor:

- **`AGENTS.md` is the agent's persistent memory.** It is loaded as instructions at the start of every session, so a
  lesson written there does not merely document the past — it changes how the agent behaves on the next change.
- **Every change closes the loop.** The `lesson-capture` subagent runs not only after a change's implementation but
  again after every merge, so lessons that only surface once a change is live still get captured; the ones you accept
  land in `AGENTS.md`.
- **Mistakes compound into rules.** The repo's strictest conventions were captured this way — archive a change in the
  same PR, interrogate the premise before moving existing configuration, never `reset --hard` a branch carrying
  uncommitted work — rules that exist because a real run got them wrong once and now steer every future run.
- **Periodic retrospectives zoom out.** `/retrospective` turns a week of merged PRs, archived changes, and accumulated
  gotchas into a sprint retrospective whose suggestions are classified `process` (→ `AGENTS.md`) or `system` (→ an idea
  or a proposal), so both the process and the system keep improving.

A mistake made once becomes a rule the agent follows thereafter — the process gets a little better with every change.

#### Tooling MCP Servers

Part of the agent's reach comes from **MCP servers** — and the easiest way to set them up is to **ask the agent**: just
tell it to set up the tooling (or run `/setup-agent-tools`). It detects what you already have, wires your global config,
and installs the `gh-mcp` extension, handing back only what it can't do for you — like `gh auth login`, or installing
`gh` itself. Prefer this over wiring them by hand, which is fiddly and easy to get wrong.

The one that matters is **GitHub** (the agent reads PRs, issues, and CI checks); **Playwright** is already configured in
the project, so there's nothing to set up.

By hand, the auth-bound server (GitHub) goes under the top-level `mcp` object in your **global** config
(`~/.config/opencode/opencode.jsonc`), not the project config — a project entry can't use your credentials:

- **Playwright** (project) — the agent's browser: it drives the running web UI and captures screenshots for the `vision`
  subagent. It needs only Node/`npx` (no credentials).
- **GitHub** — read PRs, issues, and CI checks. Run `gh auth login` (the server reuses your `gh` credentials), install
  the `gh-mcp` extension (`gh extension install shuymn/gh-mcp`), then add
  `"github": { "type": "local", "command": ["gh", "mcp"], "enabled": true }`.

MCP config is read at startup, so restart OpenCode after adding one.

### Slash Commands

| Command                      | What it does                                                                            |
| ---------------------------- | --------------------------------------------------------------------------------------- |
| `/opsx-propose`              | Scaffolds a new change: proposal, design, tasks, and spec delta                         |
| `/opsx-apply`                | Implements the change's tasks                                                           |
| `/opsx-archive`              | Archives a completed change and syncs the main spec                                     |
| `/opsx-sync`                 | Syncs a change's delta spec to the main spec without archiving                          |
| `/opsx-update`               | Revises a change's planning artifacts                                                   |
| `/opsx-explore`              | Explores an idea before proposing it                                                    |
| `/setup-agent-tools`         | Sets up the tooling MCP servers (GitHub core, plus optional extras)                     |
| `/setup-idea`                | Sets up the project's IntelliJ configuration (settings + formatters incl. web Prettier) |
| `/review-thorough`           | Deep on-demand review of a change                                                       |
| `/diagram`                   | Draws or fixes an ASCII diagram with the pro-model diagrammer                           |
| `/retrospective`             | Weekly retrospective with improvement suggestions                                       |
| `/dependency-updates`        | Runs and summarizes the dependency update report                                        |
| `/gradle-update`             | Updates the Gradle wrapper to the latest stable                                         |
| `/dependency-security-check` | Runs the Snyk dependency security scan                                                  |
| `/opsx-tool-update`          | Regenerates the OpenSpec command/skill files after an openspec CLI release              |
| `/ideas`                     | Lists and manages `docs/ideas.md`                                                       |

## Getting Started

### Prerequisites

Most verification runs entirely in the Gradle build, so the tool list is small. Gradle itself is not on it — the wrapper
pins Gradle 9.7.1 and downloads it on first use.

| Tool                   | Needed for                                                   | Install (macOS)                                                                   |
| ---------------------- | ------------------------------------------------------------ | --------------------------------------------------------------------------------- |
| **Java 21+**           | Building and running everything                              | `brew install --cask temurin@21`, or SDKMAN                                       |
| **Docker & Compose**   | Infrastructure (PostgreSQL, Kafka, OpenSearch) and the stack | Docker Desktop, or `brew install colima docker`                                   |
| **actionlint**         | The workflow-lint gate in `check`                            | `brew install actionlint`                                                         |
| **`pack` CLI**         | Building the web-UI image                                    | `brew install buildpacks/tap/pack`                                                |
| **Helm 4.x**           | Kubernetes deployment                                        | `brew install helm`                                                               |
| **Kubernetes cluster** | The `helmInstallToLocal` target                              | kind, minikube, or colima with k3s                                                |
| **OpenCode**           | The agentic development workflow (TUI or Desktop)            | `brew install opencode` / `brew install opencode-desktop`, or https://opencode.ai |
| **Snyk CLI**           | `dependencySecurityCheck`                                    | `brew install snyk/tap/snyk`                                                      |
| **Python 3**           | `scripts/setup-idea.sh`                                      | Ships with macOS Command Line Tools                                               |

Only **Java and Docker** are required to run the application. actionlint is needed for the full `check`; `pack` only
when building the web-UI image; Helm, a cluster, and Snyk are only for deployment and security scanning; OpenCode is
only for the agentic development workflow.

### Get the Sources

```bash
git clone https://github.com/aanbrn/axon-showcase.git
cd axon-showcase
```

### Build the Project

```bash
./gradlew build
```

`build` compiles everything, runs the quality gates, and runs the test suite. Without Docker, use the fast gate —
`./gradlew check -PskipITs` skips the Testcontainers integration tests.

### Run the Stack with Docker

```bash
./gradlew composeBuildAndUp
```

This is the fastest way to see the whole system work: it builds all five service images and starts the complete stack —
PostgreSQL, Kafka, OpenSearch, the four services, and the web UI. The Gradle compose tasks set `PROJECT_VERSION` and the
image tags automatically, so no environment variables are needed.

Open http://localhost:8084 for the web UI, or http://localhost:8080 for the API. Stop everything with
`./gradlew composeDown`.

Other stack tasks:

```bash
./gradlew composeUp                # start the stack (images must already be built)
./gradlew composeStop              # stop the stack without removing it
./gradlew composeRestart           # restart the stack
./gradlew composeBuildAndRestart   # rebuild the images, then restart
```

### Develop from Source

The compose stack runs the application as pre-built containers. To develop a service with hot reload, run it from source
with `bootRun` while the infrastructure stays in Docker:

```bash
./gradlew :showcase-api-gateway:bootRun        # :8080
./gradlew :showcase-command-service:bootRun    # :8081
./gradlew :showcase-projection-service:bootRun # :8082
./gradlew :showcase-query-service:bootRun      # :8083
```

Each service runs on its own HTTP port. The web UI runs on the Vite dev server (hot reload, proxies `/showcases` and
`/events` to the gateway on `:8080`):

```bash
./gradlew :showcase-web-ui:viteDev
```

Open http://localhost:5173.

#### Database Scripts

The command service stores events in PostgreSQL. When running it standalone against a native local PostgreSQL (instead
of the Docker stack), initialize the event store first:

```bash
./db.sh init    # initialize the event store (idempotent)
./db.sh drop    # drop the event store database
./db.sh reset   # drop and recreate the event store database
```

### Play with the Application

The web UI is the easiest way in: create a showcase, then watch the saga auto-start and auto-finish it — every
transition appears live in the event timeline. The same flow works over the API:

```bash
# Watch events stream in real time (SSE)
curl -N http://localhost:8080/events

# Schedule a showcase — the saga starts it at startTime and finishes it after the duration.
# Use a future startTime (e.g. a few minutes from now) so you can watch the saga auto-start it.
curl -X POST http://localhost:8080/showcases \
  -H "Content-Type: application/json" \
  -d '{
    "title": "My Showcase",
    "startTime": "<future ISO-8601 timestamp, e.g. 2026-10-01T10:00:00Z>",
    "duration": "PT5M30S"
  }'

# Drive it through its lifecycle
curl -X PUT http://localhost:8080/showcases/{showcaseId}/start
curl -X PUT http://localhost:8080/showcases/{showcaseId}/finish
curl -X DELETE http://localhost:8080/showcases/{showcaseId}

# Browse
curl "http://localhost:8080/showcases?title=My&status=SCHEDULED&size=10"
curl http://localhost:8080/showcases/{showcaseId}
```

The query service also exposes two Protobuf endpoints (`/query` and `/streaming-query`, `application/x-protobuf`),
consumed by the query-client (`showcase-query-client`) for inter-service communication — the gateway queries the query
service through that client, which in turn queries OpenSearch.

## Development Practices

### Testing

Tests are organized into four tiers, run in order:

| Tier        | Command                               | Notes                         |
| ----------- | ------------------------------------- | ----------------------------- |
| Unit        | `./gradlew :<module>:test`            | isolated, no Spring context   |
| Component   | `./gradlew :<module>:componentTest`   | real in-process collaborators |
| Integration | `./gradlew :<module>:integrationTest` | Testcontainers (needs Docker) |
| End-to-end  | `./gradlew :<module>:e2eTest`         | real deployed service + infra |

The gateway e2e boots the full four-service pipeline with Testcontainers and verifies cross-service propagation. The web
UI e2e (`./gradlew :showcase-web-ui:e2eTest`) boots the same pipeline via docker compose, serves the built UI with Vite
preview, and drives it with Playwright — creating a showcase, starting it, observing a saga-triggered transition over
SSE, live events appending to the timeline, and a duplicate title surfacing the gateway error.

### Quality Gates

Run the full check for a module — compile, spotless, checkstyle, spotbugs, errorprone, test, componentTest,
integrationTest — with `./gradlew :<module>:check` (add `-PskipITs` to drop integration for a Docker-free check;
`e2eTest` is a separate opt-in task). All quality gates run in the Gradle build, so no IDE is required to verify a
change. An IDE (e.g. IntelliJ IDEA) is an optional convenience for interactive editing, debugging, and inspection.

### Formatting and IDE Setup

Formatting is enforced by Spotless — palantir-java-format for Java, ktfmt for Gradle Kotlin DSL (`*.gradle.kts`) and
build-logic Kotlin (`build-logic/src/**/*.kt`), Prettier for markdown (`docs/`, `AGENTS.md`, `README.md`,
`openspec/specs/`): `./gradlew spotlessApply` formats, `spotlessCheck` verifies, and the build never depends on an IDE.
For the **web module**, `./gradlew :showcase-web-ui:npmFormat` applies Prettier and `:showcase-web-ui:npmFormatCheck`
(run by `check`) verifies it. IntelliJ's built-in formatter uses its own code style and would reformat files
differently, so configure the IDE to stay in sync:

- The repo's IntelliJ config is **not versioned** — `.idea/` is git-ignored. Run the setup script any time the
  configuration drifts: it merges the committed settings from `config/idea/` into `.idea/` (restoring the
  palantir-java-format and ktfmt settings and the test-tier naming inspection, leaving IntelliJ-managed content
  untouched) and installs the **palantir-java-format** and **ktfmt** plugins. The plugin install needs IntelliJ closed
  (it warns and skips that step otherwise); the configuration merge does not, so a re-run repairs the config even with
  the IDE open:

  ```bash
  ./scripts/setup-idea.sh
  ```

  IntelliJ reads these settings at startup, so apply the merged config with **File → _Reload All from Disk_** (or
  restart the IDE) — the test-tier naming inspection, in particular, only takes effect then. You can also ask the agent
  to do it (`/setup-idea`), which additionally handles quitting a running IDE for the plugin install.

  The ktfmt template uses the plugin's **Custom** style to reproduce ktfmt's kotlinlang style at 120 columns with
  unused-import removal (the plugin's `Kotlinlang` mode hard-codes ktfmt's 100-column default and ignores the
  line-length option). Where a plugin is disabled by default (palantir only auto-enables with the
  `com.palantir.java-format` Gradle plugin, which this project does not use), enable it via **Settings → Other Settings
  → palantir-java-format Settings**. When enabled it replaces `Reformat Code` (`Ctrl+Alt+L`) with the palantir
  formatter.

- The plugin only replaces `Reformat Code`; **import order is a separate mechanism** — IntelliJ's `Optimize Imports`
  (`Ctrl+Alt+O`) is governed by `.editorconfig`, which the repo ships with the palantir layout
  (`ij_java_imports_layout = $*,|,*` — _import static all other imports_, blank line, _import all other imports_) so
  `Optimize Imports` matches `spotlessApply` automatically. If the layout is not picked up, set it manually: **Settings
  → Editor → Code Style → Java → Imports**, _Import Layout_ panel → clear the rows and add: _import static all other
  imports_, blank line, _import all other imports_ (single imports, no wildcards).
- With the plugins enabled and the `.editorconfig` import layout in effect, IntelliJ's `Reformat Code` and
  `Optimize Imports` produce spotless-compatible output, so the automatic reformat triggers are safe to keep on:
  **Actions on Save** → _Reformat code_ / _Optimize imports_, and **Auto Import** → _Optimize imports on the fly_. If
  the plugin is not active on a machine, disable those triggers instead to avoid drift; `spotlessCheck` in `check` is
  the backstop either way.
- For the **web module** (`showcase-web-ui`), the same setup enables IntelliJ's built-in **Prettier** integration —
  Automatic configuration, run on reformat and on save — so `Reformat Code` formats TS/TSX, CSS, and HTML with the
  project's `prettier` and `.prettierrc`, matching the `prettier --check` gate; the Prettier scope is extended
  (`myFilesPattern`) because IDEA's default covers neither CSS nor HTML, and the JS/TS indentation comes from
  `.editorconfig`. IDEA's **Optimize Imports** is safe here too — it only removes unused/duplicate imports and reorders
  them, and no gate enforces import order. No plugin install is needed — IntelliJ bundles JavaScript/TypeScript and its
  Prettier integration is free from IntelliJ IDEA 2026.1 (on the earlier unified 2025.3–2026.0 it needs the Ultimate
  subscription).
- When in doubt, format with `./gradlew spotlessApply` (JVM and markdown) or `./gradlew :showcase-web-ui:npmFormat` (the
  web module) — the build owns formatting either way.

## Deployment and Operations

### Kubernetes Deployment

Use the bundled Helm release — it builds all five images and deploys the monitoring stack, infrastructure, and the
application to your local cluster in dependency order:

```bash
./gradlew helmInstallToLocal
```

The `local` release target deploys to your local cluster: it uses the `helm.local.kubeContext` Gradle property when set
(in `~/.gradle/gradle.properties` or via `-P`), otherwise your current kube context. Set `helm.local.kubeContext` only
if you have multiple kube contexts and need to pin the local one — e.g. macOS colima users may add
`helm.local.kubeContext=colima`.

Custom values can be placed in `helm/values/axon-showcase/values-local.yaml`. Per-release install/uninstall tasks follow
`helmInstall<Release>ToLocal` / `helmUninstall<Release>FromLocal` (e.g. `helmInstallKpsToLocal`), for installing or
verifying a single chart without building images.

#### Access the Deployed System

The deployment exposes the API gateway and the web UI through the cluster's ingress controller at the hostnames
`axon-showcase-api` and `axon-showcase-ui`. To reach them by hostname instead of a `Host:`-header curl workaround,
manage the `/etc/hosts` entries once per cluster (requires sudo):

```bash
./setup-hosts.sh setup
```

The script detects the ingress controller's LoadBalancer address generically against your current kube context (colima
with Traefik, kind/minikube with ingress-nginx, and similar). After it runs, open http://axon-showcase-ui in the browser
for the web UI and use http://axon-showcase-api for the API (e.g. `curl http://axon-showcase-api/showcases`). The
address can change on cluster restart — re-run `./setup-hosts.sh setup` to refresh, or `./setup-hosts.sh remove` to
clean up.

### Continuous Integration

`.github/workflows/ci.yml` gates every pull request and push to `main` with a single `build` check. Pull requests run
the Docker-free fast gate (`check -PskipITs` with the coverage gate disabled), while pushes to `main` run the full gate
(`check` with integration tests and coverage); both run `openspec validate --all`. The Gradle cache is restored across
runs via `gradle/actions/setup-gradle`. The `main-required-checks` ruleset requires the `build` check for all merges
into `main`, with no bypass actors.

`.github/workflows/e2e.yml` runs the end-to-end suites (`:showcase-api-gateway:e2eTest`, which builds all four service
images and boots the full pipeline, and `:showcase-web-ui:e2eTest`, which drives the browser against the same pipeline
with Playwright) on a nightly schedule and via `workflow_dispatch` — observational, never a merge gate, no secrets.

`.github/workflows/snyk.yml` runs the dependency security scan (`dependencySecurityCheck`, all sub-projects with the
root `.snyk` policy) on a weekly schedule and via `workflow_dispatch`, authenticated with the `SNYK_TOKEN` secret —
observational, never a merge gate.

`.github/workflows/dependency-updates.yml` runs the Gradle dependency update report (`dependencyUpdates`) on a weekly
schedule and via `workflow_dispatch`, opening or updating the "Dependency updates" issue with only the actionable
sections of the report (stable catalog updates + Gradle wrapper status) using the `GITHUB_TOKEN` (`issues: write`). When
there are actionable updates it posts a comment mentioning the repository owner (so they are notified); runs with no
updates update the issue silently — observational, never a merge gate.

`.github/workflows/helm-updates.yml` runs the Helm update check (`helmUpdates`) on a weekly schedule and via
`workflow_dispatch`, opening or updating the "Helm updates" issue with the actionable coordinates (the Helm CLI and
pinned chart versions that have a newer version) — observational, never a merge gate.

`.github/workflows/buildpack-updates.yml` runs the Paketo buildpack update check (`buildpackUpdates`) on a weekly
schedule and via `workflow_dispatch`, opening or updating the "Buildpack updates" issue with the pinned Paketo builder
and buildpack coordinates that have a newer version — observational, never a merge gate.

### Dependency Updates and Security

```bash
./gradlew dependencyUpdates            # report available dependency updates
./gradlew dependencySecurityCheck      # Snyk dependency security scan (needs Snyk CLI, not part of check)
./gradlew helmUpdates                  # report available Helm chart updates
./gradlew buildpackUpdates             # report available Paketo builder/buildpack updates
./gradlew verifyInfraImageVersions     # verify infra image tags match their pinned charts
./gradlew workflowLint                 # lint the GitHub Actions workflows with actionlint
```

`dependencyUpdates` reports newer versions of dependencies whose version is declared with an exact `version.ref` in the
version catalog (`gradle/libs.versions.toml`); BOM-inherited versions are not reported. Major updates can be suppressed
per coordinate or group prefix in `config/dependency-updates/major-disabled.properties` — minor and patch updates for
those coordinates are still reported. The suppression rationale for each coordinate is recorded in the
`showcase/quality/dependency-management` spec. See ADR-0004 for the deferred Spring Boot 4 migration context.

The `/dependency-updates` OpenCode command runs this report and summarizes the available updates; the `/gradle-update`
command updates the Gradle wrapper to the latest stable version when one is available, and the `/opsx-tool-update`
command regenerates the OpenSpec command/skill instruction files after a new `openspec` CLI release.

Note that the report can also surface spurious rows caused by build-environment constraints: build tooling such as
SpotBugs publishes module constraints that `checkBuildEnvironmentConstraints` reads and reports as the "current
version". For example, a `log4j-core [2.17.1 -> 2.26.1]` row appears even though `log4j-core` resolves to `2.26.1`
everywhere — `2.17.1` is the floor of an external Log4Shell guard published by `spotbugs-annotations`. These rows are a
known `gradle-versions-plugin` limitation, not real updates (see upstream ben-manes/gradle-versions-plugin#755); see
ADR-0007 for the evidence trail.

For calendar-versioned coordinates (leading segment is a 4-digit year, e.g. Spring `YYYY.MINOR.MICRO` such as
`reactor-bom 2025.0.7`), a change in the `YYYY.TRAIN` pair (the first two version segments) is treated as a major update
— matching Spring's release-train definition where `2025.0` and `2025.1` are distinct trains — while a change only in
the service-release (third) segment within the same train is a minor/patch update. Semver coordinates keep the
leading-integer major comparison.

### Load Testing

```bash
./gradlew :load-tests:test
```

Gatling-based load tests.

### Observability

Observability is part of the **Kubernetes deployment** — `./gradlew helmInstallToLocal` installs Prometheus, Grafana,
and Tempo into the `monitoring` namespace alongside the application. The local docker-compose stack does not run it.

- **Metrics**: each service exports Prometheus metrics (HTTP throughput/latency/failure, Axon command bus, event store,
  saga, deadlines, projection lag, cache hits, query performance); the web UI exports nginx `stub_status` via a sidecar.
  ServiceMonitors are wired for all of them.
- **Grafana**: a custom **Axon Showcase** dashboard is provisioned automatically (31 panels across 5 sections covering
  every service and the Axon internals), and Grafana is preconfigured with a Tempo data source. The default login is
  `admin` with the password from the chart's generated secret.
- **Tracing**: services export **OpenTelemetry** traces to **Grafana Tempo** (`tempo.monitoring`), viewable in Grafana's
  Explore.

Grafana is reached by port-forwarding to its service:

```bash
kubectl port-forward -n monitoring svc/kps-grafana 3000:80
```

Then open http://localhost:3000. Traces are available in the Tempo data source under Grafana → Explore.

## License

This project is a reference application licensed under the [MIT License](LICENSE) — free to use, copy, and adapt for
learning and as a starting point for derived projects.

## Author

**Alexey Afanasyev** — [GitHub](https://github.com/aanbrn)
