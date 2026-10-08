# Proposal

## Why

A type body whose first member is glued to the opening brace — no blank line and no comment — reads poorly: nothing
separates `{` from the first field or method. The repo's convention is otherwise mixed (a blank line, or a Javadoc
immediately after `{`), and both are fine; only the glued case is not. The parked idea proposed Checkstyle's
`TypeBodyPadding`, but that module requires a _strictly_ blank line and would flag every Javadoc-first body (~79
top-level), so the rule could not be adopted as stated. An anchored `RegexpMultiline` expresses "blank line or comment"
exactly, matching the six real occurrences with no false positives.

## What Changes

- `config/checkstyle/checkstyle.xml`: add a Checker-level `RegexpMultiline` module requiring a class, interface, or
  record body's opening brace to be followed by a blank line or a comment; enum bodies, annotation-type bodies, and
  empty bodies are exempt.
- Fix the six occurrences (add a blank line or a Javadoc to the first member): `ShowcaseApiErrorResolverCT` (two),
  `ShowcaseRestControllerCT`, `ShowcaseProjectorTests`, `ShowcaseQueryClientCT`, and `ShowcaseQueryTransportServiceCT`.
- `AGENTS.md`: a Formatting note recording the convention and its gate.
- `docs/ideas.md`: remove this change's blank-line idea and record the member-separation rule the exploration surfaced
  as a parked follow-up (a dedicated change to propose next).
- A delta spec on `showcase/quality/code-quality`.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/code-quality`: a new requirement that a type body's opening brace is followed by a blank line or a
  comment (the build rejects a glued first member).

## Impact

- **Config**: `config/checkstyle/checkstyle.xml` (one Checker-level module); the Checkstyle gate already runs on every
  source set, so no build wiring changes.
- **Code**: five Java files gain a blank line or a Javadoc after a type body's opening brace (six occurrences).
- **Docs**: `AGENTS.md` Formatting note and the `docs/ideas.md` removal.
- **Build / deployment**: no dependency, image, chart, or CI change.
