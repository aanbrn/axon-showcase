# Single-source the report contract's per-item budget

## Why

The shared report contract owns every report item's fields — the item's title, its anchor, and one line of evidence —
and five definitions' per-item budgets omitted the evidence line, as did the requirements that spec those auditors, so a
run could report a finding the reader cannot re-check. The first attempt added the evidence line at every site that
enumerates an item's fields; six quick-review rounds then each found another such site, because ~24 requirement sites
and ~9 definition sites each restated the field set and no sweep could prove the set complete. The restatement is the
drift surface. This change single-sources it instead: the contract owns the fields, and every other site references it
by name.

## What Changes

- **Seven definitions** — `.opencode/agent/readme-auditor.md`, `specs-auditor.md`, `agents-auditor.md`,
  `review-thorough.md`, `architecture-auditor.md`, `lesson-capture.md`, and `review-quick.md`: each report contract's
  per-item budget references the shared report contract's per-item budget by name, and the class clauses list only what
  each class adds. The sites that still name a contract-owned field are recorded in `design.md`'s Boundary.
- **Five commands' presentation steps** — `.opencode/commands/audit-readme.md`, `audit-specs.md`, `audit-agents.md`,
  `review-thorough.md`, `audit-architecture.md`: each presents the item as the shared report contract requires rather
  than re-summarizing it.
- **`AGENTS.md`** — the `review-thorough` bullet's parenthetical enumerated two report fields and read as complete; it
  becomes a pointer to the contract.
- **`README.md`** — the two prose sites that state a per-item report format name the evidence line.
- **The delta** — eight `MODIFIED` blocks in `showcase/quality/agent-skills`, each adding one sentence: every item the
  report carries also carries the shared report contract's per-item budget, which that requirement owns, not this one.
  The per-item sentences stay as they are, so the blocks are the requirements plus one pointer each and the scenario
  counts are unchanged.
- **Not changed, with the reason recorded** — `review-thorough`'s requirement already states that a finding "cites
  repository evidence", so it owes no delta; the README's agent-table rows and slash-command rows name what each
  subagent audits, not its report fields; and the capability's `## Purpose` does not describe the report contract.

## Impact

- **Build / tests**: none — `.opencode/` markdown, `AGENTS.md`, `README.md`, and a delta spec; `spotlessCheck` covers
  the markdown.
- **Behavior**: a report item's fields are stated once, in the contract every report already follows; the verdict lines,
  the finding classes, and every auditor's scope are unchanged.
- **Capability**: `showcase/quality/agent-skills` — eight `MODIFIED` requirements, no new capability.

## New Capabilities

None.

## Modified Capabilities

- `showcase/quality/agent-skills` — eight `MODIFIED` requirement blocks, each adding one sentence that delegates the
  per-item field set to the shared report contract: "Per-change quality-gate and analysis subagents are available", "The
  spec corpus is audited for structure and consistency", "Project-owned agent tooling is audited for consistency and
  conciseness", "The architecture is audited for drift from its recorded decisions", "Unexplained design intent is
  surfaced for clarification", "The README is audited for accuracy, design intent, and experience coverage", "AGENTS.md
  entries follow a stated authoring standard", and "Agent-authored technical artifacts follow a structured writing
  standard".
