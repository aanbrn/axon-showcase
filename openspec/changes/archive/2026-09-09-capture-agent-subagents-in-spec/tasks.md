## 1. Capture the per-change subagents in the spec

- [x] 1.1 Add a "Per-change quality-gate and analysis subagents are available" requirement to the `agent-skills` spec
      delta with four scenarios (`review-quick`, `review-thorough`, `lesson-capture`, `vision`), each phrased from the
      actual agent file and AGENTS.md behavior.
- [x] 1.2 Trim the local-subagents sentence from the vendored-skills requirement in the delta (the new requirement owns
      it), keeping the header name unchanged.
- [x] 1.3 Confirm the delta matches the main spec structure (MODIFIED vendored-skills requirement + ADDED per-change
      subagents requirement).

## 2. Verify

- [x] 2.1 Run `openspec validate --all` and `openspec validate --changes`.
- [x] 2.2 Run `./gradlew spotlessApply` / `spotlessCheck` (change docs are in the markdown target).
- [x] 2.3 Confirm no idea removal is needed in `docs/ideas.md` (none was parked for this).
