# Proposal

## Why

The agent writes dense prose: one paragraph or bullet often carries several independent requirements, ordered steps, and
conditions at once, across `AGENTS.md`, OpenSpec proposals, specifications, designs, task lists, and reviews. No
instruction states how an artifact's prose should be structured — the guidance is scattered (a wrapping rule here, a
Javadoc rule there) and says nothing about bullets-per-requirement, numbered procedures, explicit conditions, tables, or
separating the mandatory from its rationale. The result is documents that are hard to act on and that a reviewer must
unpack. Stating one reusable standard, always loaded so it governs drafting, and referencing it from the OpenSpec
authoring rules and the review subagents, makes the writing discipline explicit and enforceable.

## What Changes

- **`AGENTS.md`** — a new "Structured Writing Standard" section is the standard's one authoritative definition: the
  shape every artifact's content takes, how it is applied while drafting, and how it is checked before finishing. The
  other artifacts reference it rather than restating it.
- **`openspec/config.yaml`** — each artifact's `rules` (proposal, specs, design, tasks) and the `apply` operation
  guidance reference the standard, so it reaches the artifact-creation path.
- **`.opencode/agent/review-quick.md`** and **`.opencode/agent/review-thorough.md`** — the review checks include
  conformance to the standard; `review-quick`'s canonical finding-class list gains `structured-writing` (a violation
  that fit no existing class).
- **`.opencode/agent/agents-auditor.md`** — its consistency axis reports repo-authored prose that violates the standard.
- **`showcase/quality/agent-skills`** — a new requirement states the outcome: agent-authored technical artifacts follow
  the standard, applied while drafting and checked on review and audit.
- **`README.md`** — the Self-Learning Loop's enumeration of the audit's consistency sub-checks names the
  writing-standard check.

Every reference names the `AGENTS.md` section rather than restating the standard, so there is one authoritative
definition.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/agent-skills`: adds the "Agent-authored technical artifacts follow a structured writing standard"
  requirement — the standard lives in `AGENTS.md`, and the requirement states the outcome plus the subagents that apply
  and check it.

## Impact

- Future artifacts are authored in scannable structure rather than dense prose; no product, build, or deployment change.
- **Build / tests / services**: none — instruction and documentation edits (one `AGENTS.md` section,
  `openspec/config.yaml` rules, three subagent definitions, one requirement, `README.md` prose).
- **Verification**: `openspec validate --all`, `spotlessApply`/`spotlessCheck`, the 120-character check, and a probe
  that `openspec instructions` carries the rules into the artifact-creation path for each artifact type.
