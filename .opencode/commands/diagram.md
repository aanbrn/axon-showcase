---
description:
  Draw or fix an ASCII or Mermaid diagram with the pro-model diagrammer, then visually review a Mermaid render
---

Draw a new diagram or fix an existing one using the `diagrammer` subagent (pinned to the pro model), in whichever medium
the target uses — ASCII for plain text, Mermaid for a Markdown host that renders it (e.g. GitHub) — so the cheap flash
main agent does not spend its own effort on diagram geometry or on Mermaid's rendering pitfalls.

1. Determine the task: what to draw (a new diagram), which existing diagram to fix (name the file and the issue), and
   the medium (ASCII or Mermaid).
2. Establish the **semantic mapping** — which span/bracket/node starts and ends where, and what each annotation means —
   and confirm it with the user before rendering if it is not already agreed.
3. Invoke the `diagrammer` subagent (`.opencode/agent/diagrammer.md`) with the mapping and any existing diagram text.
4. For **ASCII**, present its rendered diagram and its alignment check; the path ends here.
5. For **Mermaid**, complete the visual-review path before presenting the diagram:
   1. Serve the `mermaid-cli` render `diagrammer` produced — its PNG and SVG — over HTTP from a local static server. The
      browser blocks `file://`, so the artifact must be reachable at an `http://127.0.0.1:<port>/…` URL.
   2. Delegate that artifact to the `vision` subagent with `diagrammer`'s visual-review handoff, so the render is
      inspected rather than only parsed.
   3. When the target is GitHub-rendered Markdown and Playwright can reach the live page, also inspect a live GitHub
      render of the diagram — a local render cannot reveal the `viewscreen` toolbar collision.
   4. Report the local render check and the visual review separately, and state an unavailable live GitHub render as a
      limitation rather than treating the local render as equivalent.
   5. Settle any **hypothesis** the review raises that needs a measurement (e.g. `getComputedTextLength()` on the SVG
      render, or a label's `scrollWidth` against its container) before acting on it — an observation is not a
      measurement.
6. Apply the diagram to the file only with the user's go-ahead.
