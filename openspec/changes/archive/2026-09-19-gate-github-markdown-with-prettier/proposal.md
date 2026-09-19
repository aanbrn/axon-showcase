# Gate the repository's remaining markdown with Prettier

## Why

The Spotless `markdown` target enumerates the project-authored markdown — `docs/**`, `AGENTS.md`, `README.md`,
`openspec/specs/**`, `openspec/changes/**` and the three `.opencode/` globs — but nothing under `.github/`, and not the
root `SECURITY.md`. Three tracked files therefore sit outside the gate: `.github/ISSUE_TEMPLATE/bug_report.md` and
`.github/PULL_REQUEST_TEMPLATE.md` (both added in #266/#267) and `SECURITY.md`. Two of them would be reformatted by the
very configuration the gate applies — the issue template's frontmatter uses double quotes where the config sets
`singleQuote`, and the PR template has one prose line past the wrap width — so the drift is real and invisible to
`spotlessCheck` today.

## What Changes

- **`build.gradle.kts`** — add `.github/**/*.md` and `SECURITY.md` to the `markdown` format target.
- **`.github/ISSUE_TEMPLATE/bug_report.md`**, **`.github/PULL_REQUEST_TEMPLATE.md`** — reformatted by `spotlessApply`
  (frontmatter quotes to single, one prose line rewrapped). `SECURITY.md` already conforms, so it enters the gate
  unchanged.
- **`openspec/changes/gate-github-markdown-with-prettier/specs/showcase/quality/code-quality/spec.md`** — a `MODIFIED`
  delta extending the markdown scope in the "Source formatting is enforced by the build" requirement (its description
  and its "Unformatted markdown fails the build" scenario) to name `SECURITY.md` and the `.github/` markdown.
- **`AGENTS.md`** (its Line Length and Formatting entries), **`README.md`**, and the **`code-quality` spec's own
  enumeration** — the scope lists that become stale the moment this lands.
- **`docs/ideas.md`** — remove the parked idea this implements ("The markdown formatter target omits the root community
  files … add them to the target and to the convention's scope list").

## Impact

- **Build**: `spotlessCheck` (and the `check` task that depends on it) now covers three more files, so a future edit to
  any of them is gated. No new formatter or dependency — the existing Prettier 3.9.6 configuration applies.
- **Tests**: none.
- **Consumers outside the build**: the two templated files are parsed by GitHub rather than by Gradle. The issue
  template's YAML frontmatter is the one piece GitHub interprets structurally, so the formatted output was compared
  against the original before adopting: the keys and their values are identical, the `---` delimiter count is unchanged,
  and only the quote style differs (`title: ""` → `title: ''`), which YAML reads as the same string. The PR template and
  `SECURITY.md` are rendered as prose and have no frontmatter.
- **Deployment**: none.

## New Capabilities

None.

## Modified Capabilities

- `showcase/quality/code-quality` — the "Source formatting is enforced by the build" requirement's markdown scope gains
  the `.github/` markdown and `SECURITY.md`.
