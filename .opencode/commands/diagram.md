---
description:
  Draw or fix an ASCII or Mermaid diagram with the pro-model diagrammer, then visually review a Mermaid render
---

Draw a new diagram or fix an existing one using the `diagrammer` subagent (pinned to the pro model), in whichever medium
the target uses — ASCII for plain text, Mermaid for a Markdown host that renders it (e.g. GitHub) — so the cheap flash
main agent does not spend its own effort on diagram geometry or on Mermaid's rendering pitfalls.

**Trigger** — the owner invokes `/diagram`, naming what to draw or which diagram to fix and why.

**Prerequisites** — the target file and the medium. The `diagrammer` subagent owns the drawing pitfalls, the semantic
mapping procedure, and its visual-review handoff.

**Procedure**

1. Determine the task: what to draw (a new diagram), which existing diagram to fix (the file and the issue), and the
   medium (ASCII or Mermaid).
2. Establish the **semantic mapping** — which span, bracket, or node starts and ends where, and what each annotation
   means — and confirm it with the owner before rendering if it is not already agreed.
3. Invoke the `diagrammer` subagent (`.opencode/agent/diagrammer.md`) with the mapping and any existing diagram text.
4. For **ASCII**: present its rendered diagram and its alignment check. The path ends here.
5. For **Mermaid**: complete the visual-review path before presenting the diagram.
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
6. Apply the diagram to the file only with the owner's go-ahead.

**Decision points**

- IF the semantic mapping is not agreed, THEN confirm it before rendering rather than iterating the geometry through
  review rounds.
- IF the review raises a claim that needs a measurement, THEN measure it before acting — do not treat the observation as
  a finding.
- IF no live GitHub render is reachable, THEN say so as a limitation; a local render is not equivalent.

**Failure handling** — a render that cannot be served, or a review that cannot run, is reported as an unavailable step
rather than presented as a passed check.

**Verification** — `diagrammer`'s local parse and layout check, and the `vision` review's structured report, reported
separately.

**Completion criteria** — the owner has the diagram, the local check, and the visual review, and has approved applying
it to the file.

**Repository lifecycle**

- The unit starts on its own branch, created from a freshly fetched `origin/main`.
- Unrelated dirty-worktree changes are preserved.
- The work stays uncommitted until the manual approval pass.
- Nothing is committed, pushed, turned into a pull request, archived, or merged without that explicit approval.
- The full per-unit review gate runs over the result. `AGENTS.md`'s "Workflow: Change lifecycle" and "Workflow: The
  review gate" own the branch, uncommitted-work, approval, and review steps, including the reviewer-session preflight;
  this command points at them rather than restating them.
