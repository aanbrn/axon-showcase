# Decision record — Markdown linting

A dated record of a bounded exploration and its verdict. It answers whether this repository should gate Markdown with a
linter and which integration each candidate would take, and it keeps the probe evidence so the question is not
re-derived. It is not a retrospective (it spans no window) and not an ADR (it binds no standing rule);
`docs/adr/README.md` states the boundary, and `docs/decisions/2026-10-29-inline-span-whitespace.md` is the sibling
record for one of the defect classes this one leaves manual.

The question arose in a conversation rather than in `docs/ideas.md`. Nothing was parked on it: `docs/ideas.md`,
`openspec/specs/` and `docs/adr/` carried no entry for `markdownlint`, `markdown lint`, `rumdl` or `remark` when the
exploration began (grepped 2026-11-02).

## The question

Markdown is formatted by Prettier through Spotless and checked for table column counts by `verifyTableRows`. No linter
inspects it. Three integrations were probed against the repository's own corpus: is adopting one worth a permanent gate?

## The candidates

| Candidate                 | Form                                                            | Probed                                        |
| ------------------------- | --------------------------------------------------------------- | --------------------------------------------- |
| `rumdl`                   | no Gradle plugin; a custom task over a Rust binary              | 0.3.2 (Homebrew), run over a copy of the tree |
| `markdownlint-cli2`       | the already-applied `com.github.node-gradle.node` (7.1.0) + npm | 0.24.0, Node 24.20.0, Gradle 9.8.2            |
| `com.appmattus.markdown`  | a Markdown-lint plugin, found through its repository            | 0.6.0, last released 2019-11-18               |
| Spotless's own lint steps | built into the pin in use                                       | Spotless 8.10.4 (sources)                     |
| Prettier + `forbidRegex`  | the current formatter plus a lint-only regex step               | Spotless 8.10.4 (sources)                     |

**Scope.** Every count below is split by the repository's own markdown scope — `markdownTarget` minus
`markdownTargetExclude` in `build.gradle.kts`, the set `verifyTableRows` reads. Findings outside it live in
`openspec/changes/archive/**` — the historical record, left as recorded — and in the generated OpenSpec instruction
files, which are the six `opsx-*` commands (the project-authored `opsx-tool-update.md` stays in scope) and the
`openspec-*` skills, plus the vendored skill trees; those files are generated or copied rather than authored here.

**Corpus.** All three tools were run over a tree extracted from the base revision
(`git archive 6258011538161498fb48cf614bbb30454d12e007`), so each count below reproduces at that revision rather than
depending on whatever a working tree held at probe time.

## The probes

### `rumdl` 0.3.2

- At the default 80-column limit: 76 765 findings in 1879 of 1892 files.
- At `MD013.line-length=120`: 3 384 findings, **484 inside scope**.
- Runtime over the corpus at the 120-column override: 0.56–0.65 s.

Its in-scope findings are enumerated beside markdownlint's in the disposition table below.

- `rumdl fmt --diff` would change 467 files, 95 of them in scope, in 998 hunks. None of those hunks is a rewrap,
  counting a hunk as one when both sides are non-empty, their words collapse to the same sequence, and the line count
  changed. The changes are blank-line normalization between list items, trailing blank lines before end of file, the
  addition of a `text` language to five README ASCII-diagram fences, and the MD001 heading rewrite.
- `rumdl check --fix` rewrites `### <unit>#<n>` to `## <unit>#<n>` in `docs/process-defects`, because 74 of the 75
  in-scope MD001 findings are that folder's record headings. That rewrite breaks the record format `AGENTS.md`
  documents, so the rule would have to be configured away rather than fixed.

### `markdownlint-cli2` 0.24.0 through `com.github.node-gradle.node` 7.1.0

- Configuration cache: entry stored, then reused on the second run — compatible with `org.gradle.configuration-cache`.
- At `MD013: {line_length: 120, code_blocks: false, tables: false}`: 3 497 findings, **370 inside scope**, 3127 outside.
- Runtime: 8.9 s for the CLI over the corpus; 21.0 s cold and 10.8 s warm through Gradle.

In-scope findings track the `rumdl` run closely. Two rules are structural differences — MD076 (+155, `rumdl` only) and
MD024 (−33, markdownlint only) — and two counts differ — MD038 (−9) and MD036 (+1) — netting 114 more in-scope findings
for `rumdl`.

Two cross-checks bear on the gates already in place:

- MD060 (table column count) has **0 in-scope findings**, so `verifyTableRows` covers that surface.
- MD031, MD032, MD047, MD022, MD007 and MD029 have **0 in-scope findings**, so Prettier's output is already conformant
  where the two tools could overlap.

### In-scope findings and their disposition

The table counts only the rules that fired inside scope; the outside-scope files are the ones the Scope paragraph names,
and their findings are not enumerated here. A `—` means that tool has no such rule.

| Rule                                                              | `rumdl` | `markdownlint-cli2` | Disposition                                                                                                                            |
| ----------------------------------------------------------------- | ------- | ------------------- | -------------------------------------------------------------------------------------------------------------------------------------- |
| MD076 blank line between list items                               | 155     | —                   | deliberate — long bullets separated by blank lines                                                                                     |
| MD036 emphasis used instead of a heading                          | 120     | 119                 | deliberate — bold single-line paragraphs used as pseudo-headings in the command files, the audits, the root docs and three skills      |
| MD001 heading level jump — `docs/process-defects` record headings | 74      | 74                  | deliberate — the record format `AGENTS.md` documents                                                                                   |
| MD001 heading level jump — elsewhere                              | 1       | 1                   | unclassified — one `docs/audits` file                                                                                                  |
| MD041 first line should be a level 1 heading                      | 35      | 35                  | deliberate — agent and command definitions open with prose after their frontmatter, and the pull-request template opens with a comment |
| MD024 duplicate heading                                           | —       | 33                  | unclassified                                                                                                                           |
| MD034 bare URL                                                    | 32      | 32                  | unclassified — bare URLs that GitHub autolinks                                                                                         |
| MD013 line length                                                 | 19      | 19                  | deliberate — the 120-column limit is a preference, not a hard limit, and every instance is a `docs/process-defects` record line        |
| MD038 spaces inside a code span                                   | 19      | 28                  | unclassified                                                                                                                           |
| MD040 fenced block without a language                             | 15      | 15                  | unclassified — five of them are the README's ASCII-diagram fences                                                                      |
| MD033 inline HTML                                                 | 7       | 7                   | deliberate — the README's `<details>` and `<summary>` blocks, and one quoted `<dir>` line in an audit                                  |
| MD025 multiple top-level headings                                 | 3       | 3                   | unclassified                                                                                                                           |
| MD026 trailing punctuation in a heading                           | 2       | 2                   | unclassified — both instances are heading-like lines quoted in `docs/audits/2026-10-19.md`                                             |
| MD018 no space after the hash                                     | 2       | 2                   | unclassified — both instances are the heading-like retrospective lines                                                                 |

Each `deliberate` row is a construct one of the repository's conventions requires or accepts, so a rule reporting one
must be configured away rather than fixed. The `unclassified` rows are the open question this record leaves: no probe
established whether their instances are defects.

### `com.appmattus.markdown` 0.6.0

- It applies and runs on Gradle 9.8.2 in a trivial project.
- On this corpus it crashed twice — `NoMultipleSpaceBlockquoteRule` (a `Heading`-to-`Paragraph` cast failure) and then
  `ListIndentRule` (`IllegalStateException`) — and produced a report only with both rules disabled.
- It analysed 1762 of 1892 files, taking 4 min 57 s, and it **breaks the configuration cache**
  (`Configuration cache problems found`, build failed).
- With both crashing rules disabled it reported 77 658 line-length findings at its default 80 columns, of which 4 688
  would remain at 120, plus 4 346 findings from the other rules, including 578 from its dictionary-based
  `ProperNamesRule`.

### Spotless 8.10.4

Read from the plugin and library sources of the pin in use:

- The Markdown steps are formatters only (`FlexmarkStep`, `FreshMarkStep`, plus the generic Prettier step). The package
  `com.diffplug.spotless.markdown` contains no lint code, and the sources mention neither `markdownlint`, `remark` nor
  `rumdl`.
- Spotless models lints (`Lint`, `LintState`, `suppressLintsFor`; every lint is an error).
  `forbidRegex(name, regex, detail)` produces per-line lints whose rule code is the matched text.
- `custom(name, FormatterFunc)` can emit real lints by overriding `FormatterFunc.lint`, but a custom step makes the task
  never up to date unless `bumpThisNumberIfACustomStepChanges(n)` is maintained by hand, and `suppressLintsFor.path`
  compares a literal path (`path.equals(relativePath)`), so a directory-scoped suppression is not expressible.

## What the probes did not establish

- **Whether the residual findings are unwanted.** The disposition table separates the classes a convention requires or
  accepts from the residue, and no probe established that the residue's instances are defects. Two instances read as
  false positives: `docs/retrospectives/2026-10-13.md:4` and `docs/retrospectives/2026-10-05.md:6` begin with PR
  references (`#543`, `#440`), which the heading rules read as malformed headings. Five README fences are ASCII diagrams
  whose rendering a language tag would not change.
- **Whether either linter covers the one defect class `AGENTS.md` records as gate-less.** Measured directly on a
  two-line fixture, both flag a one-sided space inside a span and neither flags an internal run: the documented
  `paketo-buildpacks/procfile     5.15.0` example passes silently. Coverage of that class is therefore not a reason to
  adopt either tool.
- **What the integration would cost in the repository's own terms.** Two costs first assumed were corrected: the build
  downloads Node itself (`frontend-conventions.gradle.kts`), so `scripts/doctor.sh` owes no probe; and the exclusion set
  can be derived from `markdownTarget`/`markdownTargetExclude` the way `markdownTableFiles` derives it
  (`build.gradle.kts:699-703`), rather than restated.

## The decision

Adopt **no** Markdown linter.

- Prettier stays the only owner of Markdown form.
- `verifyTableRows` stays the only structural check.
- The classes the disposition table marks deliberate stay as they are.
- The unclassified residue stays a reviewer's judgment.
- The gate-less inline-span class keeps the `Formatting` convention's proofreading instruction, joint with this record's
  sibling at `docs/decisions/2026-10-29-inline-span-whitespace.md`.

Three facts carry the verdict:

1. **A warning count is not a benefit.** Neither probe established a defect that escapes the current gates, recurs, and
   matters once rendered. Most in-scope findings fall in classes one of the repository's conventions requires or
   accepts, and the rest are unclassified.
2. **Neither tested linter flags the one defect class `AGENTS.md` records as gate-less.** Measured on a fixture, both
   linters flag a one-sided space inside a span and neither flags an internal run, so detection of that class is not a
   reason to adopt either. Detection is not the barrier in any case: `forbidRegex` can flag the run, and this record
   rejects it for failing to separate a defect from literal evidence.
3. **The integration cost is permanent.** A linter adds a pin to watch and a second npm or binary surface. The exclusion
   set is not part of that cost: it is derivable from `markdownTarget`/`markdownTargetExclude` the way
   `markdownTableFiles` derives it (`build.gradle.kts:699-703`).

The alternative considered and rejected is `forbidRegex` for two or three rules: no concrete, context-independent
pattern was established whose findings would all be defects rather than quoted content.

## Reopening condition

A later attempt may reopen this decision, and it needs **all three** of the following — a conjunction, not any one of
them:

1. **A defect.** A reproducible case that materially damages a rendered document, escapes `spotlessCheck`,
   `verifyTableRows` and the manual 120-character check, and recurs — evidenced by the span itself and by the prior
   occurrences, not by a rule count.
2. **A configuration that separates it.** A rule set that catches the defect, leaves every row the disposition table
   marks deliberate clean — including MD001 for the `docs/process-defects` record headings, though not for its one
   instance elsewhere — and leaves its remaining findings classified — unwanted where they are defects, accepted where a
   convention or literal evidence requires them — rather than unclassified. A currently unclassified row is where a real
   defect is likeliest to sit, so the clause asks for the classification instead of an empty row.
3. **An integration that fits the build.** One that stays configuration-cache compatible and adds no host prerequisite,
   which the `markdownlint-cli2` route already satisfies and the plugin route does not.

A reopen that fails clause 2 is the failure this record exists to prevent: it trades an accepted style for a permanent
exclusion list.
