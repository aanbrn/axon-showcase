## 1. Agent and command

- [x] 1.1 Add `.opencode/agent/diagrammer.md` pinned to `opencode-go/deepseek-v4-pro` with the semantic-mapping,
      character-width, and preserve-asymmetry contract (already written), and verify the file exists with the model
      front-matter
- [x] 1.2 Add `.opencode/commands/diagram.md` that triggers the subagent and confirms the mapping before rendering
      (already written), and verify it references the correct agent path

## 2. Spec capture

- [x] 2.1 Update the `showcase/quality/agent-skills` delta spec to include `diagrammer` in the per-change subagents
      requirement with its scenario (already written), and verify `openspec validate --changes` passes

## 3. Verification

- [x] 3.1 Run `openspec validate --changes` and confirm the delta is valid
- [x] 3.2 Confirm the `diagrammer` agent is discoverable (`.opencode/agent/diagrammer.md` present with `mode: subagent`
      and the pro model) and the `/diagram` command is present (`.opencode/commands/diagram.md`)
