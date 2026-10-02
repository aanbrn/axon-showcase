---
description: Draw or fix an ASCII or Mermaid diagram with the pro-model diagrammer subagent
---

Draw a new diagram or fix an existing one using the `diagrammer` subagent (pinned to the pro model), in whichever medium
the target uses — ASCII for plain text, Mermaid for a Markdown host that renders it (e.g. GitHub) — so the cheap flash
main agent does not spend its own effort on diagram geometry or on Mermaid's rendering pitfalls.

1. Determine the task: what to draw (a new diagram), which existing diagram to fix (name the file and the issue), and
   the medium (ASCII or Mermaid).
2. Establish the **semantic mapping** — which span/bracket/node starts and ends where, and what each annotation means —
   and confirm it with the user before rendering if it is not already agreed.
3. Invoke the `diagrammer` subagent (`.opencode/agent/diagrammer.md`) with the mapping and any existing diagram text.
4. Present its rendered diagram and its verification (the alignment check for ASCII, the `mermaid-cli` render for
   Mermaid).
5. Apply the diagram to the file only with the user's go-ahead.
