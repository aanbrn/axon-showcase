## Why

The OpenSpec specs under `openspec/specs/` are the behavioral source of truth — a corpus of capability specs and
requirements that grows with every archived change (currently 22 specs holding 157 requirements). `openspec validate`
gates their _well-formedness_ (delta blocks, requirement/scenario shape) and the archive flow keeps the main spec in
sync with each change's delta, but nothing audits the corpus for **structural consistency**: whether each spec's `#`
title matches its capability path, whether a Purpose describes the requirements the spec now holds, whether requirement
headers follow the repo's conventions, whether requirements are duplicated or near-duplicated across specs, and whether
specs reference symbols/files/config that have since moved. Drift of this kind passes `openspec validate` silently — the
two non-path `#` titles (`ide-config`, `infra-image-versions`) are a live example — exactly as `AGENTS.md` drifted
before the `agents-auditor` subagent. This is a different artifact and a different property (structure, not behavior vs.
code) from the per-change review the workflow already runs, so it warrants its own auditor rather than widening
`agents-auditor`.

## What Changes

- Add a `specs-auditor` subagent (`.opencode/agent/specs-auditor.md`) pinned to `opencode-go/deepseek-v4-pro` that
  audits the `openspec/specs/` corpus for **structure and consistency**: title ↔ capability-path match; Purpose ↔
  requirements fit; requirement-header conventions (a declarative noun phrase, with SHALL bodies and WHEN/THEN
  scenarios); requirements duplicated or near-duplicated across specs (flagged for judgment — a reused header may be two
  genuinely different capabilities, as the two clients' `Business error translation` and the two extensions'
  `Module dependency exposure` are); and dead cross-references to renamed classes/files/config the specs cite. It
  verifies each finding against the repository, reports findings by severity with a location and a suggested rewrite,
  and does not edit files. It SHALL NOT audit behavior-vs-code drift (the change workflow's review loop and the
  archive-time sync own that).
- Add an `/audit-specs` command (`.opencode/commands/audit-specs.md`) as the trigger.
- Capture the new subagent in the `showcase/quality/agent-skills` spec.
- Record the convention in `AGENTS.md` (subagent section), add the `specs-auditor` row to the README agent table plus
  the `/audit-specs` row to the slash-command table, and add a sentence to the README's Spec-Driven Development section
  surfacing the audit as the spec corpus's maintenance.

## Capabilities

### New Capabilities

- none

### Modified Capabilities

- `showcase/quality/agent-skills`: adds a requirement that a `specs-auditor` subagent audits the `openspec/specs/`
  corpus for structural consistency, verifies its findings against the repository, and reports them without editing
  files.

## Impact

- `.opencode/agent/specs-auditor.md` — new subagent definition.
- `.opencode/commands/audit-specs.md` — new command trigger.
- `openspec/specs/showcase/quality/agent-skills/spec.md` — added requirement (delta spec in this change).
- `AGENTS.md` (subagent convention bullet) and `README.md` (agent-table row, slash-command row, and a Spec-Driven
  Development sentence) — docs that are the change, shipped in this change's PR.
- `docs/ideas.md` — an entry parking the scheduled-workflow variant this change defers (the entry ships as its own docs
  PR, not in this change's PR).
