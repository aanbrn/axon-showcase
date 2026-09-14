## 1. Extend the format target

- [x] 1.1 Add the project-authored `.opencode/` markdown to the root Spotless markdown target — the agent definitions,
      the project-authored commands, and the project skills — and exclude the six generated `opsx-*` commands (keeping
      the project-authored `opsx-tool-update.md` in scope), the generated `openspec-*` skills, and the vendored
      `axon4to5-*` skills (the target names only the project-authored directories, so `node_modules/` is never reached);
      verify with `./gradlew spotlessApply` and `git diff --name-only` that only project-authored files change
- [x] 1.2 Add a `json` format block scoped to `.opencode/opencode.json` with `printWidth: 120` (Prettier's default of 80
      would explode the `command` array), and verify `spotlessApply` reformats it
- [x] 1.3 Verify the generated `opsx-*`/`openspec-*` files and the vendored `axon4to5-*` skills are byte-identical after
      the format run (`git diff` reports no change for them; a backtick-dense line may legitimately stay over 120, per
      the documented trade-off, so the check is byte-identity and a visual pass, not a hard line-length assertion)

## 2. Reformat

- [x] 2.1 Run `./gradlew spotlessApply`, then confirm `./gradlew spotlessCheck` passes (the format is idempotent)

## 3. Remove or re-scope the superseded instructions

- [x] 3.1 Drop the manual `.opencode/` 120-character instruction from `.opencode/commands/audit-architecture.md` and
      `.opencode/commands/audit-agents.md`, and add the `.opencode/` files those audits edit to their step-4
      "formatter-wrapped files" enumeration; the general "a glob written into an instruction file is unchecked" gotcha
      stays, though its text is corrected in task 3.2
- [x] 3.2 Update the `AGENTS.md` **Line Length** bullet (its markdown scope enumeration is what goes stale) and the
      `Formatting` convention to name the new `.opencode/` scope and which files are not reformatted, and correct the
      glob gotcha — its mid-bullet "(`.opencode/` is outside Spotless)" parenthetical is now false, and its closing
      sentence prescribes the very check task 3.1 deletes; verify with `./gradlew spotlessApply` plus `git diff` that
      only the intended prose changed
- [x] 3.3 Re-scope the line-length instructions in the two review agents — the 120-character run in
      `.opencode/agent/review-quick.md` and the "120-column" convention item in `.opencode/agent/review-thorough.md` —
      to the changed files the formatter does not cover (e.g. YAML), since everything else they check is now gated and a
      hand-rolled check can false-flag a backtick-dense line
- [x] 3.4 Extend `.opencode/commands/opsx-tool-update.md` to verify, after regenerating the OpenSpec instruction files,
      that the format target's generated-file exclusions still match what the generator wrote
- [x] 3.5 Update `README.md`'s `Formatting` section, whose Prettier markdown scope enumeration would go stale, to name
      the project-authored `.opencode/` files and the config JSON
- [x] 3.6 Drop `/ideas`'s "wrap within 120 characters" instruction for `docs/ideas.md`, which is formatter-wrapped

## 4. Verification

- [x] 4.1 Verify every reformatted file's frontmatter still parses to the same values (compare the parsed `description`
      before and after) and smoke-test a subagent end-to-end after an OpenCode reload — confirming it loads and responds
      — so the restyled frontmatter is proven against the loader; do not archive while this is unchecked
- [x] 4.2 Run `./gradlew spotlessApply` after the final edit (tasks 3.3–3.6 edit files now inside the markdown target),
      then the PR gate `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`, and confirm `spotlessCheck` and
      `openspec validate --all` are clean
- [x] 4.3 Record the `showcase/quality/code-quality` `## Purpose` check for the archive commit — its Purpose is
      scope-agnostic ("enforces the project's code style … through the build"), so it still fits the widened scope and
      needs no edit; recorded as an explicit task because a delta cannot carry a Purpose
