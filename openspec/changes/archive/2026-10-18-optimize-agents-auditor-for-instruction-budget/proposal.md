# Proposal

## Why

`AGENTS.md` is the repository's always-loaded instruction surface: 357 KB and 3,622 lines before this change, injected
in full into every session — the main agent's and each subagent's — with no size cap, and it grew 25% in the seven days
since the 2026-10-11 audit recorded 2,899 lines. The `agents-auditor` reconciles that file's consistency and conciseness
but never measures it: no size, no growth, no cost for a rule that stays always-loaded, and no disposition for moving a
rule to a destination that loads only when its task triggers. Its reduction candidates name a rewrite, not the decision
a rule changes or what keeping it loaded costs. So the one lever that bounds the file — relocating guidance out of the
always-loaded prefix — has no analysis behind it, and the bound stays a discipline nothing measures.

## What Changes

- **`showcase/quality/agent-skills`** — the audit requirement gains an **instruction-budget ledger**, a
  root-guidance-scoping rule, and an **extraction candidate** disposition with its conditions, destination mapping,
  control-flow caveat, and retrieval-path test (three body paragraphs and eight scenarios), plus an **extraction
  opportunity** class for a source unit whose destination does not yet exist — and which therefore cannot yet pass that
  test (a body paragraph and six scenarios); the reduction-candidate reporting rule gains the seven fields each
  candidate must name; the reduction unit's requirement names `extraction` among the candidates it applies and never
  applies an opportunity (one body edit and two scenarios).
- **`.opencode/agent/agents-auditor.md`** — the frontmatter `description`, the conciseness axis's standing analyses (the
  new disposition), the candidate field table, the extraction conditions and plan table, the destination-type table, the
  prioritization order, the **extraction-opportunity** class with its classification workflow and its field table, and
  the report contract's `Instruction-budget ledger` section with its opportunities subsection.
- **`.opencode/commands/audit-agents.md`** — its `description`, its step-1 read-list, and its step-2 report list.
- **`.opencode/commands/apply-audit-reductions.md`** — the candidates it applies now include an extraction, whose
  application is a content-move routed to the change workflow.
- **`AGENTS.md`** — the `agents-auditor` bullet names the budget pass, the extraction disposition, and the extraction
  opportunity.
- **`README.md`** — the audit prose, the agent table row, and the slash-command table row.
- **`docs/ideas.md`** — the "Trend the audit counts across reports" entry now names the size ledger as a trendable
  series; and the reduction unit's `extraction` gap, if any remains, is parked.
- **`openspec/config.yaml`** — its `rules.tasks` gains the member-grep obligation the capture's authoring-system
  recommendation names, so a sweep task states the set's members to grep rather than a phrase one enumeration uses.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/agent-skills`: the audit requirement gains the instruction-budget ledger, the root-guidance rule,
  the extraction disposition and its conditions, and the reduction candidates' required fields; the reduction-unit
  requirement gains `extraction` among the candidates it applies.

## Impact

- A future audit measures `AGENTS.md`'s always-loaded size, reports what a rule costs by staying loaded, and can propose
  relocating a rule to a destination that loads on demand — with the decision it preserves and the retrieval path
  verified. Where no destination exists yet, it reports an **extraction opportunity** instead: a design recommendation
  the owner carries out in a dedicated change, never applied by the reduction unit and never counted as a finding. The
  candidate's own safety conditions are unchanged. Nothing about the product, its build, or its deployment changes.
- **Build / tests / services**: no code, build, dependency, or deployment change — three `.opencode/` files (one agent
  definition and two commands), `AGENTS.md`, `README.md`, `docs/ideas.md`, `openspec/config.yaml` (one `rules.tasks`
  entry), and the spec delta. `AGENTS.md`'s prose is read by `verifyInstallCommands`, `verifyDocumentedPins`, and
  `verifyCapturedMarkers`, so those gates re-run.
- **Verification**: `openspec validate --all` and `spotlessApply`/`spotlessCheck` for the artifacts; a definition's
  behavior is exercised only by a run, which needs the edited definition picked up (a reload or a new session), so a
  smoke-run with controls gates the archive. The pass is a measurement over the live file, so its own report is the
  evidence for its figures.
- **Out of scope, recorded**: applying any reported reduction is a separate owner-approved unit; this change ships the
  auditor's ability to propose them, not the reductions.
