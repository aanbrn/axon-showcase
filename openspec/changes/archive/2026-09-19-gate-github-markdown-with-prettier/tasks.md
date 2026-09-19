# Tasks — gate the repository's remaining markdown with Prettier

## 1. Widen the target

- [x] 1.1 Add `.github/**/*.md` and `SECURITY.md` to the `markdown` format target in `build.gradle.kts`. Done — both
      globs added beside `openspec/changes/**/*.md`.
- [x] 1.2 Run `./gradlew spotlessApply` and confirm it touches exactly the files this change expects (the two templates;
      `SECURITY.md` conforms, so it should be unchanged). Done — it modified `.github/ISSUE_TEMPLATE/bug_report.md`,
      `.github/PULL_REQUEST_TEMPLATE.md`, `AGENTS.md`, `README.md` and `build.gradle.kts`, and left `SECURITY.md`
      untouched.
- [x] 1.3 Compare the issue template's YAML frontmatter before and after: the key list and values identical, the `---`
      delimiter count unchanged, only the quote style differing — the equivalence half of the "adding a formatter target
      can rewrite content a consumer outside the build parses" rule. Done — keys
      `['name', 'about', 'title', 'labels', 'assignees']` and their values identical, `---` count 2 → 2, only
      `title: ""` → `title: ''` and the same for `assignees`.

## 2. The spec delta and the prose that follows it

- [x] 2.1 Write the `MODIFIED` delta at
      `openspec/changes/gate-github-markdown-with-prettier/specs/showcase/quality/code-quality/spec.md`, copying the
      main spec's full requirement block and editing its description and "Unformatted markdown fails the build" scenario
      to name `SECURITY.md` and the `.github/` markdown — never hand-writing the block from memory, since a `MODIFIED`
      block replaces the whole requirement. Done — copied from the main spec and edited; 10 of 10 scenarios preserved
      and the main spec left untouched (synced at archive).
- [x] 2.2 Update the scope enumerations that go stale: `AGENTS.md`'s Line Length entry and its Formatting entry, and
      `README.md`'s mention of the markdown scope. Done — all three now name `SECURITY.md` and the `.github/` markdown,
      matching the target.
- [x] 2.3 Remove the implemented idea from `docs/ideas.md`, which names this change's work and its convention-list
      update. Done — the entry and its blank separator are gone; no neighbouring entry disturbed.

## 3. Verification

- [x] 3.1 `./gradlew spotlessCheck` green, and a deliberate edit to one of the newly targeted files (a long line) is
      caught by it — the positive control that the widening actually gates them. Done — a >120-character line appended
      to `.github/PULL_REQUEST_TEMPLATE.md` failed `spotlessCheck` naming that file (and the same control on
      `SECURITY.md` fired too); both files were restored and `spotlessCheck` is green.
- [ ] 3.2 **Deferred**: exercise the consumer that the rewrite reaches — confirm GitHub renders the issue template from
      the branch. This cannot be verified before the merge (GitHub reads templates from the default branch), so it is
      recorded as a deferred verification rather than done: after the merge, open the new-issue picker and confirm the
      "Bug report" template still appears and prefills. The equivalence check in 1.3 is the pre-merge evidence that it
      will; this is its confirmation.
- [x] 3.3 `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` green, and `openspec validate --all` with the change
      validating (a real delta, so not `skip_specs`). Done — `check` green (1m43s) and `check` includes `spotlessCheck`;
      `openspec validate --all` 23/23 with the change's delta accepted.
- [x] 3.4 Confirm no other tracked markdown remains outside the target, applying the target's own `targetExclude` to the
      scan (`git ls-files '*.md'`, then drop `openspec/changes/archive/**`, the generated `opsx-*` commands and
      `openspec-*` skills, and the vendored `axon4to5-*` skills) — so the three files here are the last ungated ones
      rather than the first. Done — the scan reports 688 tracked, 64 in the target, 624 matched by `targetExclude` (the
      544 archived change files, the six generated `opsx-*` commands, and the 74 `.opencode/skills/*` files — the
      vendored `axon4to5-*` skills (63 of them in `axon4to5-migrate-code` alone) and the generated `openspec-*` skills),
      with **0 ungated**. `spotlessMarkdownCheck --rerun-tasks` runs clean over the target set — those 64 tracked files
      plus the active change dir's own markdown, which matches `openspec/changes/**/*.md` on disk.
