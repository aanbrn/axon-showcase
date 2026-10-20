# Proposal

## Why

`AGENTS.md` loads in full into every session, each subagent's included, and now carries 334,308 bytes across 3,441
lines. Two shipping procedures account for 5,675 bytes of that surface while being needed only when a unit is delivered,
so the owner asked whether they can become concise root instructions whose full contracts load at the stage entry. The
pilot measures the trade instead of assuming it: the net saving is 0.65% per fresh session, the on-demand cost is paid
at each complete skill load, and the aggregate benefit depends on F fresh sessions and L complete skill loads — 2,163 ×
F − 6,703 × L — which nobody has measured.

## What Changes

- `AGENTS.md`: replace the bodies of "Workflow: Ship a change (CI → archive → merge)" and "Workflow: Complete a cloud
  (`/oc`) agent PR" with the stage trigger, the semantic governed actions, the authorization boundaries, the archive
  ordering, the report-vs-change outcome, the complete-read instruction, and the missing-destination stop.
- `AGENTS.md`: name the `ship-change` skill in the three capture-workflow destination enumerations, so a
  shipping-procedure correction reaches the skill rather than returning to root.
- `.opencode/skills/ship-change/SKILL.md` (new): carry both procedures' ordered steps, decision branches, failure
  handling, completion criteria, and the opencode action's pull-request derivation, each section with its own `Source:`
  line naming the replaced root heading.
- `.opencode/agent/lesson-capture.md`: add the skill to the four capture-destination paragraphs (five occurrences).
- `openspec/specs/showcase/quality/agent-skills/spec.md`: distinguish a workflow invariant (stays at root) from its
  stage mechanics (may move only behind a complete-read entry point), state the net materiality measure, separate the
  procedure destination from the capability-spec destination for internal control flow, and define global/cross-cutting
  guidance.
- `openspec/specs/showcase/quality/agent-skills/spec.md`: hold the skill as the procedures' authoritative home and route
  a shipping-procedure lesson to it in the capture destination list.
- The action's PR-derivation paragraph stays in the transferred contract for now; any later relocation is independent
  work whose capability owner must be verified first.

## Capabilities

### New Capabilities

None. The behavior lives in the existing agent-skills capability, which already owns the guidance surface.

### Modified Capabilities

- `showcase/quality/agent-skills`: the extraction-candidate conditions gain the invariant-vs-stage-mechanics
  distinction, the complete-read precondition, the net materiality measure, an internal-control-flow rule that keeps
  control flow at root except eligible stage mechanics and sends behavior-changing mechanisms to a capability spec, and
  definitions of global/cross-cutting guidance; a new requirement assigns the two shipping procedures to
  `.opencode/skills/ship-change/` with `AGENTS.md` as the stage entry point; the capture destination list gains the
  skill.

## Impact

- `AGENTS.md` shrinks by 3,383 bytes from the two procedures and grows by 974 bytes from the capture edits (routing and
  lessons), less a 35-byte flag correction: 331,864 bytes, a net 2,444 less in the file.
- The always-loaded surface also gains 281 bytes of metadata (the skill's name and description, and the capture
  subagent's description), for a net 2,163 bytes less per fresh session (0.65% of the file).
- A complete skill load adds 6,703 bytes; a re-entry adds a load only when the procedure is no longer in context, and
  the conservative every-entry reading is stated separately.
- Total maintained guidance for the two procedures grows from 5,675 to 8,996 bytes; including all capture edits it is
  10,182 bytes.
- A new project-owned skill appears under `.opencode/skills/`, inside the markdown formatting target and the
  `agents-auditor`'s scope; `upstreamReferences` still sees `anomalyco/opencode#50247` because that reference stays at
  root.
- `.opencode/agent/lesson-capture.md` gains a third destination for a shipping-procedure lesson; the capture flow itself
  is unchanged.
- The CLI-generated OpenSpec commands and skills, the build, the workflows, and the runtime are untouched.
- The retrieval path was demonstrated only on a simplified 430-byte root in a scratch harness; the complete text still
  needs the implementation-time validation the tasks specify.
