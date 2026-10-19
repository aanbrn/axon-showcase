# Design

## Context

See `proposal.md` — Why. The 2026-10-19 report's two extraction opportunities shipped as archived changes
(`2026-10-19-extract-load-test-commands`, `2026-10-19-extract-ci-narrative`). Both were opportunities, which the
standing reduction unit never applies, so each move was carried out by a dedicated change.

The report's three remaining "safe to load on demand" candidates were verified against their proposed destinations
before this change: none passes the retrieval path. `## Kubernetes Deployment`'s destination (the `helm-chart` spec,
`build.gradle.kts`, the values files) states namespaces, contexts, registry defaults, and packaging, and the mechanism
files carry the release order, the label placement, and the chart's keep-alive value; but no destination states the
data-wipe/re-upgrade warning, the generalized rule that a new chart Deployment must extend the app release's
`installDependsOn` (ADR-0008 records the web UI instance only), the client-label rationale, or the controller-specific
read-timeout guidance. `## Docker Images` carries rules the catalog and the buildpack gotcha do not (the run-image
floating rule, the CNB timestamps, the image names), and its Paketo pins are what `verifyDocumentedPins` reads.
`## Key Environment Variables` describes each variable's meaning, which ADR-0002's four-surface rule does not name. Two
sections are read by gates as well: `verifyInstallCommands` reads the `AGENTS.md` `helm install` block, and
`verifyDocumentedPins` reads the pins in `AGENTS.md` and `README.md`.

## Goals / Non-Goals

**Goals:**

- State the move as a procedure a dedicated change can follow, so the next extraction does not re-derive it.
- Make the gate coupling explicit, since deleting a block a check reads empties the check.
- Keep the procedure short by pointing at the audit's retrieval-path conditions and the growth-bound bullet's trim
  verification rather than restating them.

**Non-Goals:**

- Extracting any of the three remaining candidates: the verification retains all three at root.
- Changing the reduction unit's route: an extraction candidate still goes to a change, and an opportunity is still never
  applied by the unit.

## Decisions

- **The procedure lives in `AGENTS.md`, not the command.** Both extractions came from opportunities, which the command
  never applies, so the dedicated change is the only reader and `AGENTS.md` is what it loads. The command's extraction
  bullet points at the procedure instead of carrying a copy.
- **The material finding is a step, not a note.** A check that reads `AGENTS.md` makes its block extraction-coupled: the
  procedure retargets the check in the same change or keeps the block.
- **Single-source the rest.** The retrieval-path test and candidate conditions stay in `showcase/quality/agent-skills`;
  the removed-term sweep stays in the growth-bound bullet. The procedure states only the move's own order.

## Risks / Trade-offs

- The procedure adds lines to the always-loaded file for a rarely taken path → it replaces the re-derivation each
  dedicated change performs, and the two extractions showed the checks are the risky part; the growth-bound bullet's
  no-growth discipline still applies to future captures, not to a stated procedure.
- The gate list can stale as tasks are added → the procedure says "such as", so a new task makes the step richer, not
  the list wrong.

## Migration Plan

None: guidance prose and a command pointer, taking effect on the next read.

## Claims

Each fact the change states about its evidence, with its source or deriving command. Seeded with
`python3 scripts/claim-scan.py`; its candidates are each resolved into a row or dismissed by kind below the table.

| #   | Claim                                                                                                                                                                                           | Source / deriving command                                                                                                                                                                                                                                                                                                                                                                                                                         |
| --- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1   | The report's two extraction opportunities shipped as archived changes                                                                                                                           | `ls -d openspec/changes/archive/2026-10-19-extract-load-test-commands openspec/changes/archive/2026-10-19-extract-ci-narrative`                                                                                                                                                                                                                                                                                                                   |
| 2   | The `helm-chart` spec states none of release order, `installDependsOn`, persistence, or read timeout                                                                                            | `grep -c "mustInstallAfter\|installDependsOn\|persistence\|read timeout" openspec/specs/showcase/deployment/helm-chart/spec.md` (0)                                                                                                                                                                                                                                                                                                               |
| 3   | `verifyInstallCommands` reads `AGENTS.md` as its input                                                                                                                                          | `grep -n "agentsFile.set" build.gradle.kts` (the task's `layout.projectDirectory.file("AGENTS.md")`)                                                                                                                                                                                                                                                                                                                                              |
| 4   | `verifyDocumentedPins` reads `AGENTS.md` and `README.md`                                                                                                                                        | `grep -n -A6 'tasks.register("verifyDocumentedPins"' build.gradle.kts` (its `docs` inputs)                                                                                                                                                                                                                                                                                                                                                        |
| 5   | `README.md` carries none of the Paketo pin strings                                                                                                                                              | `grep -c "paketo-nginx\|paketo-procfile\|builder-jammy-base" README.md` (0)                                                                                                                                                                                                                                                                                                                                                                       |
| 6   | ADR-0002 names none of the key environment variables                                                                                                                                            | `grep -c "SHOWCASE_CORS_ALLOWED\|DB_PASSWORD\|BPL_DEBUG\|THC_PATH\|JAVA_OPTS" docs/adr/0002-*.md` (0)                                                                                                                                                                                                                                                                                                                                             |
| 7   | `agent-skills` already states the unit's extraction route, which this change leaves unchanged                                                                                                   | `grep -n "An extraction candidate's application is routed to a change" openspec/specs/showcase/quality/agent-skills/spec.md` (1)                                                                                                                                                                                                                                                                                                                  |
| 8   | The parked idea exists and its precondition is the second extraction                                                                                                                            | `git show HEAD:docs/ideas.md \| grep -c "Give the reduction unit an extraction procedure"` (1)                                                                                                                                                                                                                                                                                                                                                    |
| 9   | The report's safe-to-load-on-demand list holds five entries; the first two shipped as opportunities, leaving `## Kubernetes Deployment`, `## Docker Images`, and `## Key Environment Variables` | `sed -n '183,194p' docs/audits/2026-10-19.md` (five entries); `grep -c "Opportunity" docs/audits/2026-10-19.md` (2)                                                                                                                                                                                                                                                                                                                               |
| 10  | The pre-change extraction bullet routes the move to a change and states no move steps                                                                                                           | `git show HEAD:.opencode/commands/apply-audit-reductions.md \| sed -n '30,32p'`                                                                                                                                                                                                                                                                                                                                                                   |
| 11  | The mechanism files carry the order, the label placement, and the keep-alive value, but the four retention rules are stated only in `AGENTS.md`                                                 | `grep -c "mustInstallAfter" build.gradle.kts` (5), `grep -c "axon-showcase-kafka-client" helm/values/axon-showcase/values-local.yaml` (3), `grep -c "keepAliveInterval" helm/chart/src/main/helm/values.yaml` (2); `grep -rln "Re-applying an infrastructure release\|A chart Deployment addition must extend\|belong to the targets that set\|The chart sets no read-timeout annotation" AGENTS.md docs/ openspec/specs/ helm/` (AGENTS.md only) |

**Dismissals** (the scan's candidates resolved to neither a row nor a fact, one reason per kind the scan classifies):
**path candidates** point at a site — a file, task, or workflow the text names — not a claim about the evidence;
**cross-reference candidates** name a repo path or a handoff label rather than asserting a fact; **absolute-quantifier
candidates** are quantifier words inside a rule's or an artifact section's own sentence, not an independent universal
claim about a corpus; and **count candidates** are numerals inside the unit's own writing, not a count of a corpus the
change rests on.
