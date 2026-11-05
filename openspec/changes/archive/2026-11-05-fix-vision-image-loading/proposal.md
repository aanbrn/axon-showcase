# Make the vision subagent obtain pixels before it reports

## Why

The `vision` subagent is the repository's only route to a visual review, and its definition tells it to "load the
artifact yourself with the browser tool" — which reads as "navigate to the URL". In this session the browser returned a
page wrapper and an empty accessibility snapshot for the served PNG, so the subagent saw nothing: it correctly reported
that it had no pixels, and the main agent had to inspect the screenshots itself. The mechanism that actually attaches an
image is the browser's screenshot tool, which the definition never names.

## What Changes

- `.opencode/agent/vision.md`: the loading instruction becomes an ordered procedure of four steps:
  - navigate to the URL the caller names;
  - call the browser's screenshot tool and inspect the image it returns, treating navigation metadata or an
    accessibility snapshot as not being visual evidence;
  - state any scaling or incomplete-frame limitation that affects what was asked;
  - report an explicit "not performed", with the tools' outputs, when the navigation fails or the capture returns no
    image, rather than a clean review of nothing.
- No change to `AGENTS.md` (its URL hand-off rule stays true), to the report's three groups, to the model pin, or to the
  subagent's read-only boundary.

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/agent-skills` — "Diagrams and screenshots are produced by their specialist subagents" gains the
  failed-acquisition outcome: a review that obtains no inspectable image reports that it was not performed, with the
  tools' outputs, under the existing "what the image cannot settle" group. The acquisition steps stay in the definition,
  since the requirement states the outcome and the review's shape.

## Impact

- **Files**: `.opencode/agent/vision.md` and the change's own artifacts.
- **Code, APIs, dependencies**: none.
- **Build and tests**: none; the agent-tooling gate that loads every definition still parses this one.
- **Deployment**: none.
- **Verification**: two short subagent invocations — one URL-only hand-off of a screenshot that exists, and one of a URL
  that returns no image — checking that the first yields a review of the pixels and the second an explicit refusal with
  the tools' outputs.
