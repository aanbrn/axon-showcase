## Context

`specs-auditor` reported inconsistent coordinate spelling in `showcase/quality/dependency-management`. The follow-up
change `apply-specs-audit-findings` fixed only the requirement the finding named (the OpenSearch coordinates) and left
the identical stray-space pattern in the sibling `springdoc` requirement in the same file, deferred as "a future audit".
A grep for the pattern is immediate, so this change applies the convention sweep the deferral should have been.

## Goals / Non-Goals

**Goals:**

- Normalize the remaining stray-space coordinate mentions (`org.springdoc: springdoc-...`) to `group:artifact`.

**Non-Goals:**

- Re-running the audit or widening its scope. This is the single sibling instance of a pattern already being corrected.
- Any requirement-text change beyond the coordinate spelling.

## Decisions

- **A `MODIFIED` delta, carrying all scenarios.** The coordinate lives in the requirement body and its two scenarios, so
  the delta copies the current requirement verbatim and edits only the three spellings. `openspec validate --changes`
  confirms no scenario was dropped.
- **Small and separate.** It is a one-convention correction; folding it into a docs PR would misroute a spec edit (a
  spec edit is a change), and reopening the archived `apply-specs-audit-findings` is not possible — so it is its own
  change.

## Risks / Trade-offs

- **Minimal.** The change is spelling-only; the coordinate's meaning is unchanged and no behavior is described
  differently.
