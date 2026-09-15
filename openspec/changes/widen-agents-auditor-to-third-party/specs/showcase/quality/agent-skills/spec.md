## MODIFIED Requirements

### Requirement: Project-owned agent tooling is audited for consistency and conciseness

The repository SHALL provide an `agents-auditor` agent subagent that audits the project-owned agent tooling —
`AGENTS.md` and the project-authored files under `.opencode/agent/`, `.opencode/commands/`, and `.opencode/skills/` —
for **consistency** and **conciseness**, and reports its findings without editing files. The audit SHALL exclude files
the repository does not author: the generator-owned OpenSpec instruction files (the commands and skills written by
`openspec update`) and the vendored `axon4to5-*` skills (copied verbatim from the upstream AxonIQ repository); a
project-authored file that shares a generated file's name prefix (e.g. the `opsx-tool-update` command) SHALL remain in
scope. The audit SHALL cover, at least: contradictions between entries or files, stale claims and enumerations the
repository has outgrown, dead cross-references, and drift from the code/workflows/specs the prose describes
(consistency); and duplicated or near-duplicate entries, one-off trivia, over-long or over-specific entries, and
misplaced entries (conciseness). Each finding SHALL be verified against the repository rather than inferred from the
prose alone, and SHALL be reported with its location and a concrete suggested rewrite. The subagent SHALL propose its
findings without modifying files; the main agent verifies and applies those the user approves.

The audit SHALL additionally report, as an **advisory** class kept separate from its fix findings, third-party
inconsistency: a file the audit excludes (a generated `openspec-*` instruction file, or a vendored `axon4to5-*` skill)
that contradicts how this repository uses it. The class SHALL be bounded by a harm test — reported only where the
contradiction would mislead a workflow driven by the file, or instruct a pattern the repository's code or conventions
contradict, never a mere textual difference from the repository's own prose. Each item SHALL name the harm and the
decision it invites (report it upstream, re-vendor at a newer version, or change the repository's usage) and SHALL NOT
propose a local edit to the excluded file, which the exclusion rule forbids.

#### Scenario: An agent-tooling audit is produced

- **WHEN** the main agent invokes the `agents-auditor` subagent (e.g. via the `/audit-agents` command)
- **THEN** it returns findings grouped by severity, covering consistency (contradictions, stale claims, dead
  cross-references, drift) and conciseness (duplication, trivia, length, placement) across `AGENTS.md` and the
  project-authored `.opencode/` files, each with a location and a suggested rewrite

#### Scenario: Generated and vendored files are excluded

- **WHEN** the `agents-auditor` subagent selects what to audit
- **THEN** it excludes the OpenSpec instruction files written by `openspec update` and the vendored `axon4to5-*` skills,
  whose correct state is defined by their generator or upstream rather than by this repository — while a
  project-authored file with a similar name (the `opsx-tool-update` command) stays in scope

#### Scenario: Findings are verified against the repository

- **WHEN** the `agents-auditor` subagent flags a claim, enumeration, or cross-reference
- **THEN** it checks the claim against the repository (the referenced files, workflows, or specs) before reporting it,
  so a deliberate or still-true entry is not reported as stale

#### Scenario: The auditor does not edit files itself

- **WHEN** the `agents-auditor` subagent runs
- **THEN** it returns proposed findings and rewrites without modifying files; the main agent verifies and applies those
  the user approves

#### Scenario: An excluded file inconsistent with our usage is reported as advisory

- **WHEN** an excluded file (a generated instruction file or a vendored skill) contradicts how this repository uses it —
  for example a vendored migration skill instructing a pattern the codebase no longer follows
- **THEN** the audit reports it in a separate advisory class, naming the harm and the decision it invites (report
  upstream, re-vendor, or change our usage), and never proposes a local edit to that file

#### Scenario: A mere textual difference is not reported

- **WHEN** an excluded file differs from the repository's own prose but would not mislead a workflow or instruct a
  contradicted pattern
- **THEN** the audit does not report it, so the advisory class stays limited to contradictions that would change what a
  reader does
