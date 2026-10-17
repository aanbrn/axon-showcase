# Design

## Context

See `proposal.md` — Why. The repository already states one authoring standard — the knowledge-authoring standard
`AGENTS.md` entries follow, owned by the `agent-skills` capability
(`openspec/specs/showcase/quality/agent-skills/spec.md`, "AGENTS.md entries follow a stated authoring standard") — but
it governs **what** belongs in `AGENTS.md` and its entry representations, not **how** an artifact's prose is structured.
Writing-shape guidance is otherwise scattered: `AGENTS.md` carries a wrapping rule (`## Line Length`) and, in
`Conventions`, a `Javadoc` rule and a `Formatting` rule, each a local instruction rather than a general prose-structure
standard.

Where an instruction the agent writes artifacts from lives matters:

- `AGENTS.md` is loaded on every invocation, so it is the one surface active **while the main agent drafts** any
  artifact.
- `openspec/config.yaml`'s `rules` and `operations.*.guidance` are project-authored (unlike the OpenSpec CLI's schema
  templates and the generated `opsx-*`/`openspec-*` files, which `openspec update` overwrites) and are injected to the
  agent as per-artifact constraints — `.opencode/commands/opsx-propose.md` reads and applies them at artifact creation.
- The review subagents (`.opencode/agent/review-quick.md`, `review-thorough.md`) and the tooling auditor
  (`agents-auditor.md`) are where a conformance check is enforced after drafting.

## Goals / Non-Goals

**Goals:**

- State the standard once, in the always-loaded authoritative project instruction (`AGENTS.md`), so it governs drafting.
- Reference it from the OpenSpec artifact constraints and the review/audit subagents rather than copying its contents.
- Capture the outcome in the capability that owns agent tooling, without weakening the existing authoring standard.

**Non-Goals:**

- Rewriting existing artifacts into the standard (the user's scope excludes an indiscriminate rewrite); the standard
  applies to artifacts as they are created or substantially edited.
- A build/CI gate: prose structure is a judgment call, not a lint at reasonable cost.
- Editing the generator-owned OpenSpec CLI templates or the generated `opsx-*`/`openspec-*` files.

## Decisions

**The authoritative home is `AGENTS.md`, not the `agent-skills` spec.** The knowledge-authoring standard lives in the
spec because it governs only `AGENTS.md` entries, read by two specific subagents; the structured writing standard
governs **every** artifact the main agent drafts, so it must be on the always-loaded surface, or it cannot influence the
initial output the way the standard requires. Rejected: a spec-only home, and a standalone `docs/` reference (the
always-loaded file is what drafting needs).

**The spec carries the outcome, not the standard's content.** The new `agent-skills` requirement states that
agent-authored artifacts follow the standard, that it is applied while drafting, and that the review/audit subagents
check it — and points at `AGENTS.md`'s section for the standard itself, so there is one authoritative definition.
Rejected: restating the principles in the spec (duplication, which the standard forbids) and a second requirement for
enforcement (one subject split across two permanent entries).

**One ADDED requirement, no MODIFIED block.** The existing "Per-change quality-gate and analysis subagents are
available" requirement states each subagent is "described by the scenarios below or by its own requirement in this
capability", so a new requirement can describe the writing-standard check on the review and audit subagents without
rewriting that requirement's scenario. This mirrors
`openspec/changes/archive/2026-10-17-record-agents-md-authoring-standard`, which kept the audit requirement's `at least`
clause and added a separate requirement rather than a `MODIFIED` one.

**The standard speaks the existing slot vocabulary and preserves artifact formats.** Its labeled fields are the
procedure slots the knowledge-authoring standard already names (`Trigger`, `Prerequisites`, `Procedure`,
`Decision points`, `Failure handling`, `Completion criteria`, `Verification`), so the two standards do not contradict,
and it states explicitly that each artifact keeps its own required template and semantic distinctions — it shapes
content within them, not a single outline for all.

**The writing-standard violation gets its own finding class.** `review-quick`'s canonical class list is the only home
for a finding's class, and a writing-standard violation fit none of the existing classes (a prose-structure defect is
neither `formatting`, which routes to the formatter, nor `wording`), so the change adds `structured-writing` to that
list — the contract already directs the calling agent to add a class in the same change when a finding fits none. No
spec edit accompanies it: the shared report-contract requirement is class-agnostic, deferring to "the canonical list the
reviewer's contract carries".

## Risks / Trade-offs

- **Two standards could be read as competing** → the writing standard governs prose structure; the knowledge-authoring
  standard governs `AGENTS.md` content and its entry representations. The new standard says so, and reuses the
  knowledge-authoring standard's procedure slots rather than defining new ones.
- **A definition edit is served from the pre-change copy until a reload or a new session** → the smoke-run of each
  edited definition is a precondition for the archive, per `AGENTS.md`'s subagent-reload gotcha (a task, not run at
  archive time here).
- **Conformance is judgment, not a gate** → the reviewer/auditor reports it as a finding with the restructure for the
  owner; a false positive costs a review line, not a broken build.

## Migration Plan

None: `AGENTS.md` and `README.md` edits, three subagent definitions, `openspec/config.yaml` rules, and one spec
requirement, taking effect on the next artifact authored or reviewed. Existing artifacts are not mass-rewritten.

## Claims table

| Claim                                                                      | Source                                                                                                                                                                                                                                |
| -------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| No structured-writing standard exists today                                | `grep -rIl -e "Structured Writing Standard" -e "one bullet per" -e "numbered procedure"` over the repo (excluding `build/`, `node_modules/`, `.git/`, `openspec/changes/archive/`) returns only this change's own files               |
| Writing-shape guidance is scattered, not a standard                        | `git show HEAD:AGENTS.md` — `## Line Length` (a wrapping rule) and the `Conventions` bullets `Javadoc`, `Formatting`, and `Avoid redundancy`, each a local instruction                                                                |
| `openspec/config.yaml` is project-authored artifact-creation input         | `openspec/config.yaml` (`rules`, `operations.*.guidance`), read by `.opencode/commands/opsx-propose.md` ("`rules` … constraints for you", "Apply `context` and `rules` as constraints")                                               |
| The CLI templates and the `opsx-*`/`openspec-*` files are generator-owned  | `.opencode/skills/openspec-propose/SKILL.md` frontmatter `generatedBy: "1.14.2"`; the templates live in the installed package `@fission-ai/openspec/schemas/spec-driven/templates/`; `agents-auditor.md` excludes them from fix scope |
| `review-quick`/`review-thorough` are the repository's review subagents     | `.opencode/agent/review-quick.md`, `.opencode/agent/review-thorough.md`; `agent-skills` "Per-change quality-gate and analysis subagents are available"                                                                                |
| The `agent-skills` capability owns agent tooling and an authoring standard | `git show HEAD:openspec/specs/showcase/quality/agent-skills/spec.md` — `## Purpose` and "AGENTS.md entries follow a stated authoring standard"                                                                                        |
| The README enumerates the audit's consistency sub-checks                   | `git show HEAD:README.md` — the Self-Learning Loop bullet naming "`AGENTS.md` entries that violate the stated authoring standard"                                                                                                     |
| A new requirement need not MODIFY the existing review scenario             | `agent-skills` — "each described by the scenarios below or by its own requirement in this capability"                                                                                                                                 |
| The design can add one ADDED requirement without a MODIFIED block          | `openspec/changes/archive/2026-10-17-record-agents-md-authoring-standard/specs/showcase/quality/agent-skills/spec.md` — a single `## ADDED Requirements` block; the audit requirement carries "SHALL cover, at least:"                |
| `AGENTS.md` is loaded on every invocation (the drafting-time surface)      | `git show HEAD:README.md` — "`AGENTS.md` is the agent's persistent memory. It is loaded as instructions at the start of every session"                                                                                                |
| The report-contract requirement is class-agnostic                          | `git show HEAD:openspec/specs/showcase/quality/agent-skills/spec.md` — it defers to "the canonical list the reviewer's contract carries"                                                                                              |
| `review-quick`'s contract carries the canonical finding-class list         | `git show HEAD:.opencode/agent/review-quick.md` — the class list and "the calling agent adds a class here, in the same change"                                                                                                        |
| The existing authoring standard governs content, not prose shape           | `git show HEAD:openspec/specs/showcase/quality/agent-skills/spec.md` — "AGENTS.md entries follow a stated authoring standard": representation and entry shape, not artifact prose structure                                           |
| The knowledge-authoring standard already names the procedure slots         | `git show HEAD:openspec/specs/showcase/quality/agent-skills/spec.md` — the same requirement lists Trigger, Prerequisites, Procedure, Decision points, Failure handling, Completion criteria, Verification                             |
| A definition edit is served from the pre-change copy until a reload        | `git show HEAD:AGENTS.md` — the subagent-reload gotcha ("a changed definition body … is served from the pre-change copy until then")                                                                                                  |

**Dismissals** (scan candidates resolved to neither a row nor a fact, grouped by kind): absolute quantifiers in the
standard's text (`every`, `never`, `only`, `must not`) are modal wordings, not evidence claims; the `120`-character
limit is a formatting constant, not a repository fact; this unit's own appended `docs/review-rounds.tsv` rows are not
facts about the repository; backticked tokens (`AGENTS.md`, `openspec/config.yaml`, `review-quick`, `SHALL`,
`IF`/`THEN`) point at the rows above or name the artifacts the change edits.
