# Decision record — the process components' dependency inventory

Date: 2026-11-02

Status: a bounded exploration. It binds no standing rule; it records what one snapshot of the process components depends
on, and how each dependency's disposition stands.

## The question

The owners of reuse work asked what remains preparable without a second project. Earlier sweeps answered a narrower
question — whether one known literal still appears — and one of them missed a component because a heading reference
wrapped across a line. This inventory therefore starts from the component population and scans each component with a
wrapping-tolerant pattern, then asks of each dependency: what class is it in, who owns it, and is its disposition
recorded?

## The population

Tracked `.opencode/` files at the time of writing: **161**. Excluded, and accounted rather than dropped:

- **100 vendored files** — the `axon4to5-*` migration skills and `ast-grep`, copied from upstream and never edited here.
- **12 generated files** — the `opsx-*` and `openspec-*` commands and skills the OpenSpec CLI writes, **except**
  `opsx-tool-update`, which is project-authored and stays in the population despite its prefix.
- **2 installer-state files** — `.opencode/.gitignore` and `.opencode/package.json`.

Included: **47 components** — 25 commands, 11 agents, 10 project-authored skills, and `opencode.json` as the runtime
configuration. **42** name at least one dependency; **5** name none — the complement of the table's consumers, listed so
the accounting closes: `consult`, `dependency-security-check`, `opencode.json`, `setup-agent-tools` (command), `vision`.
The scan covers six reference forms, because a dependency is not always written as a path:

- an `AGENTS.md` section named in quotes, including a component's `Source:` line, or in prose
  (`### Evidence and search`, the subject table, the formatting convention);
- a script;
- a repository or spec path;
- a spec capability id in its full `showcase/quality/…` form;
- a reference to another `.opencode/` component by path.

Three limits bound the result, and none of them is hidden:

- a bare name is not counted — not a component's name, not an `ADR-NNNN` citation, not a capability's bare form — so an
  invocation or a pointer phrased only that way is outside the inventory;
- the project's own source, build, config, template and policy paths (`showcase-web-ui/…`, `config/…`, `.snyk`,
  `.github/PULL_REQUEST_TEMPLATE.md`, `openspec/config.yaml`) are project substance under ADR-0025, not dependencies of
  the process;
- a `Source:` list wrapped over several lines contributes only what its first line names, so a pack whose provenance
  spans several sections may be under-counted.

The path rows below are the components' process-relevant repository paths; a path outside them falls under one of those
limits, so the exclusion is a stated rule rather than a silent one.

50 distinct dependencies: 15 `AGENTS.md` sections, 10 scripts, 13 repository path rows, 2 spec capability ids, 9
component references, and the guidance destinations, which have no single path. The table gives each one's class under
ADR-0025, its owner, its consumer count, and its disposition.

| Dependency                                                    | Class                    | Owner                                                  | Consumers | Disposition                                   |
| ------------------------------------------------------------- | ------------------------ | ------------------------------------------------------ | --------: | --------------------------------------------- |
| `docs/contracts/process-bindings.md`                          | supporting material      | the declaration, decided by ADR-0026                   |        21 | declared — the read route                     |
| `docs/contracts/shared-report.md`                             | supporting material      | ADR-0025 §4                                            |         8 | declared support                              |
| `docs/contracts/knowledge-authoring.md`                       | supporting material      | ADR-0025 §4                                            |         2 | declared support                              |
| `docs/contracts/` (the layer)                                 | supporting material      | ADR-0025 §3–4                                          |         1 | declared support                              |
| `AGENTS.md`, "## Conventions"                                 | explicit project binding | the conventions section                                |         3 | explicit by decision                          |
| `AGENTS.md`, "## Docker Images"                               | explicit project binding | the deployment guidance                                |         1 | explicit by decision                          |
| `AGENTS.md`, "## Kubernetes Deployment"                       | explicit project binding | the deployment guidance                                |         1 | explicit by decision                          |
| `AGENTS.md`, "## Local Development"                           | explicit project binding | the deployment guidance                                |         1 | explicit by decision                          |
| `AGENTS.md`, "Workflow: The review gate"                      | explicit project binding | the `review-gate` role                                 |         1 | declared                                      |
| `AGENTS.md`, "### Workflow: Record process defects"           | explicit project binding | the process-defect record's workflow                   |         1 | declared — the `process-defect-records` owner |
| `AGENTS.md`, the formatting convention                        | explicit project binding | the formatting rule                                    |         2 | explicit by decision                          |
| `AGENTS.md`, "### Evidence and search"                        | explicit project binding | the evidence rule                                      |         2 | explicit by decision — a destination          |
| `AGENTS.md`, the subject table                                | explicit project binding | the packs' classification and trigger                  |         6 | explicit by decision                          |
| the guidance destinations                                     | explicit project binding | `AGENTS.md`, the subject packs, three procedure skills |         1 | explicit by decision — the destination list   |
| `AGENTS.md`, "Workflow: Author a spec delta and archive"      | explicit project binding | the spec-delta workflow                                |         3 | settled: declared by `adopt-spec-delta-role`  |
| `AGENTS.md`, "Refreshing a branch"                            | explicit project binding | the branch-refresh rule                                |         1 | unresolved — no transfer record               |
| `AGENTS.md`, "Workflow: Ship a change (CI → archive → merge)" | explicit project binding | the shipping procedure                                 |         1 | unresolved — no transfer record               |
| `AGENTS.md`, "Workflow: Complete a cloud (`/oc`) agent PR"    | explicit project binding | the cloud-PR workflow                                  |         1 | unresolved — no transfer record               |
| `AGENTS.md`, "Workflow: Generalize process defects"           | explicit project binding | the generalization procedure                           |         1 | unresolved — no transfer record               |
| `AGENTS.md`, "Workflow: Extract an `AGENTS.md` section"       | explicit project binding | the extraction procedure                               |         1 | unresolved — no transfer record               |
| `docs/ideas.md`                                               | explicit project binding | the ideas file's own header                            |         9 | recorded: an input, not a destination         |
| `README.md`                                                   | explicit project binding | the README audit's subject                             |         2 | subject                                       |
| `docs/adr/`                                                   | explicit project binding | the ADR corpus; the architecture audit                 |         2 | subject                                       |
| `docs/audits/`                                                | explicit project binding | the audit reports' location                            |         3 | subject                                       |
| `docs/retrospectives/<date>.md`                               | explicit project binding | the retrospective's layout                             |         1 | subject                                       |
| `docs/guidance-evidence/`                                     | explicit project binding | the guidance-evidence records                          |         1 | incompatibility — a target without it         |
| `docs/README`                                                 | explicit project binding | none — the repository has no such path                 |         1 | dangling — `diagrammer`'s description         |
| `openspec/specs/.../spec.md` (the corpus locator)             | explicit project binding | the spec corpus; `specs-auditor`                       |         1 | subject                                       |
| `openspec/specs/showcase/` (the decomposition)                | explicit project binding | the corpus; `architecture-auditor`                     |         1 | subject                                       |
| the capability `showcase/quality/agent-skills`                | supporting material      | the capability spec                                    |         2 | re-owned: a target supplies its own           |
| the capability `showcase/quality/dependency-management`       | supporting material      | the capability spec                                    |         2 | re-owned: a target supplies its own           |
| `scripts/claim-scan.py`                                       | explicit project binding | `/seed-claims`, the review gate                        |         2 | tooling                                       |
| `scripts/doctor.sh`                                           | explicit project binding | `/check-tooling`, `check-and-test-authoring`           |         2 | tooling                                       |
| `scripts/experience-analysis.sh`                              | explicit project binding | `/retrospective`, `experience-analyzer`                |         2 | tooling — embeds the records directory        |
| `scripts/new-worktree.sh`                                     | explicit project binding | `/new-worktree`                                        |         2 | tooling                                       |
| `scripts/setup-idea.sh`                                       | explicit project binding | `/setup-idea`                                          |         2 | tooling                                       |
| `scripts/commit-hygiene.py`                                   | explicit project binding | the review gate; the pre-commit guard                  |         1 | tooling                                       |
| `scripts/load-test-baseline.sh`                               | explicit project binding | the `load-tests` capability                            |         1 | tooling                                       |
| `scripts/reconnect-mcp.py`                                    | explicit project binding | `/reconnect-mcp`                                       |         1 | tooling                                       |
| `scripts/review-routing.py`                                   | explicit project binding | the review gate                                        |         1 | tooling                                       |
| `scripts/test-doctor.sh`                                      | explicit project binding | `check-and-test-authoring`                             |         1 | tooling                                       |
| `.opencode/skills/pause-resume-unit/SKILL.md`                 | explicit project binding | its own slice                                          |         3 | travels with the component set                |
| `.opencode/opencode.json`                                     | explicit project binding | the runtime configuration                              |         2 | travels with the component set                |
| `.opencode/agent/agents-auditor.md`                           | explicit project binding | the audit's reviewer contract                          |         1 | travels with the component set                |
| `.opencode/agent/architecture-auditor.md`                     | explicit project binding | the audit's reviewer contract                          |         1 | travels with the component set                |
| `.opencode/agent/diagrammer.md`                               | explicit project binding | the diagram subagent                                   |         1 | travels with the component set                |
| `.opencode/agent/experience-analyzer.md`                      | explicit project binding | the retrospective's analyzer                           |         1 | travels with the component set                |
| `.opencode/agent/readme-auditor.md`                           | explicit project binding | the audit's reviewer contract                          |         1 | travels with the component set                |
| `.opencode/agent/review-thorough.md`                          | explicit project binding | the review's reviewer contract                         |         1 | travels with the component set                |
| `.opencode/agent/specs-auditor.md`                            | explicit project binding | the audit's reviewer contract                          |         1 | travels with the component set                |

## What the inventory establishes

- **The earlier sweeps were not population-complete.** Six `AGENTS.md` sections beyond the two declared ones are named
  by seven components across eight references — the same class as the sections the heading sweep adopted, and invisible
  to a search for those two names. One of the six is now declared; the bullet records the finding, not the count.
- **Five section rows remain unresolved for one reason:** nothing exercises them. A counterfactual run or a real target
  would settle each; one has been settled that way since this inventory was written — the spec-delta section, by
  `adopt-spec-delta-role`'s renaming counterfactual — which is the reopening condition working as recorded.
- **Four dispositions are already recorded** and are not reopened here: the evidence section and the guidance
  destinations are destinations by decision, the ideas file is a recurrence input, and `docs/guidance-evidence/` is an
  incompatibility.
- **One path is dangling:** `diagrammer`'s description points at `docs/README`, which this repository does not have.
- **Nine component references travel with the set:** a target copying the components keeps them unchanged, so they need
  no adaptation even though they are dependencies.

## What it does not establish

It does not establish runtime portability, transfer cost, or that no dependency remains unclassified. The table records
the dependencies its stated forms find under its stated limits: the 47-component population says which files were read,
not that the forms recognise every dependency inside them, and the counts describe the scan's output rather than
validate its coverage. The consumer counts are per dependency; the per-component membership is not reproduced. It adopts
no role, amends no policy, and adds no check.

## Reopening condition

Reopen this inventory when any one of the following holds:

- **The component population changes** — a component is added, removed, or renamed, so the 47-component accounting is
  stale (internal trigger: the `.opencode/` tree).
- **A transfer or counterfactual record settles an unresolved row** — that record replaces the row's disposition
  (external or internal trigger: any such record naming the dependency).
- **A dependency appears that no row classifies** — the table is then incomplete and is re-derived (internal trigger: a
  component's own text).
