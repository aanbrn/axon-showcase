# Design

## Context

See `proposal.md` — Why. The repo's style gate is Spotless (palantir-java-format) plus Checkstyle, and Checkstyle runs
on every source set, so a Checkstyle rule covers production and test sources alike. The parked idea proposed
`TypeBodyPadding` (`atStartOfBody=true`, `atEndOfBody=false`), but its `haveBlankLineAfterLeftCurly` requires the line
after `{` to be whitespace-only, so it flags every Javadoc-first top-level body (79). No stock module expresses "blank
or comment": `EmptyLineSeparator` checks separation between members, not between `{` and the first member.

## Goals / Non-Goals

**Goals:**

- Reject a first member glued to a type body's opening brace (no blank line and no comment), accept a blank line or a
  comment, and exempt enum bodies, annotation-type bodies, and empty bodies — gated in `check` with no new module or
  dependency.

**Non-Goals:**

- Forcing a blank line where a Javadoc already separates the brace from the member (the dominant style stays valid).
- Reformatting: the gate detects; the fix is manual (palantir has no padding option).
- A custom AST check for the wrapped-declaration case — the chosen pattern already covers it.
- The same-line body form (`class A { int x; }`): the pattern requires a newline after `{`, and the formatter expands
  such a body onto its own lines, so the rule neither flags nor addresses it.

## Decisions

- **Enforce with an anchored `RegexpMultiline`, not `TypeBodyPadding`.** Alternatives: `TypeBodyPadding` (rejected — it
  flags the ~79 Javadoc-first top-level bodies); a custom Checkstyle check (correct but needs a new module/dependency
  and a Checkstyle test harness); an unanchored regex (rejected — the same pattern without the line-start anchor matches
  24 places, 18 of them false positives — the keywords appearing in Javadoc prose, `@DisplayName` string literals, and
  an `@interface` declaration — since `RegexpMultiline` cannot ignore comments/strings); a Spotless step (no such
  option).
- **Anchor the keyword right after the modifiers, and allow the declaration to wrap before its brace.** The match starts
  at a line's modifiers/keyword, so a Javadoc line (which starts with `*`) or a `//` comment does not match, while a
  declaration whose `{` is on a later line still does, stopping at the first `{` (or `;`). The module:

  ```
  <module name="RegexpMultiline">
      <property name="format"
                value="^[ \t]*(?:(?:public|protected|private|static|abstract|final|sealed|non-sealed|strictfp)\s+)*(?:class|interface|record)\s+\w[^{};\n]*(?:\n[^{};\n]*)*?\{[ \t]*\n(?![ \t]*\n)(?![ \t]*[/\*])(?![ \t]*\})"/>
      <property name="message" value="An opening brace in a type body must be followed by a blank line or a comment."/>
  </module>
  ```

  `RegexpMultiline` is a `Checker`-level module, so it sits beside `TreeWalker`, not inside it. The message avoids an
  apostrophe: Checkstyle renders it through `MessageFormat`, which consumes a single `'` (a `body's` renders as
  `bodys`).

- **Exempt enums and annotation types by scope, not a suppression.** The pattern matches only `class|interface|record`,
  so `enum X { FIRST,` and `@interface X { … }` are naturally exempt; an empty body written `{}` has no newline after
  `{`, and the `(?![ \t]*\})` lookahead also exempts a `{\n}` body.
- **Fix the six sites with a blank line or a Javadoc** — the rule accepts either; choose per site during apply (a blank
  line for a small helper field/method, a Javadoc where a comment is more informative).
- **Known-bad / known-good control.** A scratch source with a glued member fails the rule; one with a blank line or a
  comment passes; an enum body, an annotation-type body, and an empty body pass.

## Risks / Trade-offs

- **Regex false positives** (a block comment whose interior line is a type declaration at column 0, or a string
  containing one, followed by a non-blank line): the anchor excludes a Javadoc line and a `//` comment, but not a raw
  block comment's interior, and `RegexpMultiline` cannot ignore comments or strings. Verified against all 176 Java
  files, the pattern matches exactly six places, all real declarations; the residual surface is absent from the corpus
  and is recorded here rather than suppressed.
- **Regex false negative** (a type declaration whose line begins with an annotation, e.g. `@Deprecated class A {` with a
  glued first member): the pattern anchors the keyword right after the modifiers, so an annotation-prefixed declaration
  line is not matched. Palantir puts type annotations on their own line (no such declaration exists in the corpus), so
  it is unreachable in formatted code.
- **A future formatter change to the brace placement**: palantir keeps `{` on the declaration's last line, and the
  pattern also tolerates a `{` on its own line or a wrapped declaration; re-verify the pattern if the formatter pin
  changes.
- **The gate detects but does not fix**: `spotlessApply` will not insert the padding, so a new glued member is caught by
  `check` and fixed by hand. Recorded in the `AGENTS.md` Formatting note.

## Migration Plan

None. Five files gain a blank line or a Javadoc; the gate then holds.
