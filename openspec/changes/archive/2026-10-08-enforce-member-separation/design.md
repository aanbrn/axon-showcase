# Design

## Context

See `proposal.md` — Why. Checkstyle's `EmptyLineSeparator` is a `TreeWalker` module that requires an empty line between
a token and its next sibling; it has `tokens` (which member kinds to check), `allowNoEmptyLineBetweenFields` (whether
two adjacent fields may omit it; default `false`), and `allowMultipleEmptyLines` (default `true`). Its default `tokens`
include `PACKAGE_DEF`, `IMPORT`, and `MODULE_IMPORT`, whose gap check would flag the SPDX-header-to-`package` gap
present in every file. It never checks the gap after a type body's opening brace — that is the sibling `RegexpMultiline`
rule's subject (`enforce-type-body-padding`), so the two compose without overlap.

## Goals / Non-Goals

**Goals:**

- Require a blank line between consecutive members of a type body, fields included, gated in `check` with no new module
  or dependency; fix the 14 grouped fields the rule flags.

**Non-Goals:**

- The gap after a type body's opening brace (the padding rule) and any whitespace inside method bodies.
- Collapsing multiple consecutive blank lines (`allowMultipleEmptyLines` stays at its default `true`; the repo has no
  runs of two or more blanks, and forbidding them is a separate rule).
- Reformatting automatically — the gate detects; the fix is manual.

## Decisions

- **Enforce with `EmptyLineSeparator` scoped to member tokens, not its defaults.** The default `tokens` include
  `PACKAGE_DEF`/`IMPORT`/`MODULE_IMPORT`, which would flag the license-header-to-`package` gap in every file. The
  module:

  ```
  <module name="EmptyLineSeparator">
      <property name="tokens"
                value="VARIABLE_DEF,METHOD_DEF,CTOR_DEF,CLASS_DEF,INTERFACE_DEF,ENUM_DEF,RECORD_DEF,
                       STATIC_INIT,INSTANCE_INIT,COMPACT_CTOR_DEF"/>
      <property name="allowNoEmptyLineBetweenFields" value="false"/>
  </module>
  ```

  The value wraps across lines because XML attribute-value normalization folds the newline to a space, which
  Checkstyle's token parser ignores. `EmptyLineSeparator` checks the gap after a token before its next sibling, so the
  exemption is directional: it has no token for an annotation type declaration (`ANNOTATION_DEF`), an enum constant
  (`ENUM_CONSTANT_DEF`), or an annotation member (`ANNOTATION_FIELD_DEF`), so no blank line is required _after_ those
  kinds — while the gap _before_ them is still checked via the preceding member's token. That matches the spec's
  directional exemption.

  Alternatives: the default token set (rejected — the package gap); a methods-only scope
  (`allowNoEmptyLineBetweenFields=true`, rejected — it would leave the 14 grouped fields grouped); a custom regex
  (unnecessary — the stock module expresses the rule exactly).

- **Include fields** (`allowNoEmptyLineBetweenFields=false`, the module default, set explicitly), the decision taken
  when this follow-up was parked: it forces the 14 grouped-field blocks apart, standardizing on the spacing every other
  field already uses.

- **Leave `allowMultipleEmptyLines` at its default.** The repo has no runs of two or more blank lines, so the rule has
  nothing to fix there, and forbidding them is a different behavior from requiring separation.

## Risks / Trade-offs

- **All 14 fixes are in test sources** (`test`, `componentTest`, `e2eTest`); no production file changes, and no
  semantics change — only a blank line is inserted.
- **Adjacent fields will fail the gate** from now on, including related-constant groups. That is the intended
  strictness; each field already carries its own Javadoc elsewhere in the codebase, so the blank line matches the
  convention.
- **Overlap with the padding rule is impossible**: the padding rule checks the first member's gap after `{`, and
  `EmptyLineSeparator` checks member-to-member gaps.

## Migration Plan

None. Eight files gain blank lines; the gate then holds.
