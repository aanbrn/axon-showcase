---
description: Draw or fix an ASCII diagram with the pro-model diagrammer subagent
---

Draw a new ASCII diagram or fix a misaligned one using the `diagrammer` subagent (pinned to the pro model), so the
cheap flash main agent does not spend effort on ASCII geometry.

1. Determine the task: what to draw (a new diagram) or which existing diagram to fix (name the file and the issue).
2. Establish the **semantic mapping** — which span/bracket/node starts and ends where, and what each annotation means —
   and confirm it with the user before rendering if it is not already agreed.
3. Invoke the `diagrammer` subagent (`.opencode/agent/diagrammer.md`) with the mapping and any existing diagram text.
4. Present its rendered diagram and alignment verification.
5. Apply the diagram to the file only with the user's go-ahead.