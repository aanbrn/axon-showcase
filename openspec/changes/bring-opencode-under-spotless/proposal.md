## Why

`.opencode/` is the last markdown corpus outside the formatter gate, and its only check is a hand-rolled 120-character
command embedded in two agent commands and the `review-quick` agent — a check that has already failed silently (PR #199
fixed a vacuous `.opencode/*.md` glob that matched no file). Bringing the project-authored files under the same Prettier
gate the rest of the markdown uses deletes that whole class of bug.

## What Changes

- Extend the root Spotless markdown target to the **project-authored** `.opencode/` markdown: the agent definitions, the
  project-authored commands, and the project skills.
- Exclude the files the repository does not author — the generated `opsx-*` commands (except the project-authored
  `opsx-tool-update.md`), the generated `openspec-*` skills, and the vendored `axon4to5-*` skills (which the vendoring
  convention requires stay verbatim); the target names only the project-authored directories, so `node_modules/` is
  never reached.
- Add a `json` format target for the project-owned `.opencode/opencode.json`.
- Reformat the project-authored markdown and the config once (pure rewrapping plus Prettier's frontmatter style, a
  missing trailing newline, and its `_emphasis_` normalization).
- Drop the manual `.opencode/` 120-character instruction from `/audit-architecture` and `/audit-agents` — the gate
  covers it now — while keeping the general "a glob written into an instruction file is unchecked" lesson.
- Re-scope the two review agents' line-length instructions — `review-quick`'s 120-character run and `review-thorough`'s
  "120-column" convention item — to the changed files the formatter does not cover (e.g. YAML), since everything else
  they check is now gated.
- Drop `/ideas`'s manual "wrap within 120 characters" instruction for `docs/ideas.md`, which is formatter-wrapped.
- Extend the two audit commands' step-4 "formatter-wrapped files" enumeration with the `.opencode/` files they edit, and
  update the scope enumerations that go stale in `AGENTS.md` and `README.md`.
- Hook `/opsx-tool-update` (which regenerates the OpenSpec instruction files) to keep the generated-file exclusions in
  sync with what the generator writes.

## Capabilities

### New Capabilities

- none

### Modified Capabilities

- `showcase/quality/code-quality`: the "Source formatting is enforced by the build" requirement's scope extends to the
  project-authored `.opencode/` markdown and to the project-owned `.opencode/` configuration JSON, and states which
  `.opencode/` files are deliberately not reformatted.

## Impact

- `build.gradle.kts` — the markdown target's scope and exclusions, plus a `json` target.
- The project-authored `.opencode/` markdown and `.opencode/opencode.json` — reformatted once.
- `.opencode/commands/audit-architecture.md`, `.opencode/commands/audit-agents.md`, `.opencode/commands/ideas.md`,
  `.opencode/commands/opsx-tool-update.md`, `.opencode/agent/review-quick.md`, `.opencode/agent/review-thorough.md`,
  `AGENTS.md`, and `README.md` — the manual line-length instructions the gate supersedes, and the scope enumerations
  that go stale.
- The generated `opsx-*`/`openspec-*` files and the vendored `axon4to5-*` skills stay byte-identical.
- `openspec/specs/showcase/quality/code-quality/spec.md` — synced at archive.
