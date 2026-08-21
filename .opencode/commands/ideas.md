---
description: Append a dated idea note to docs/ideas.md
---

Manage the lightweight ideas scratchpad at `docs/ideas.md`.

- When the user provides a note (text after `/ideas`), append it as a dated bullet under a `## YYYY-MM-DD` section in
  `docs/ideas.md`, creating the section for today if it does not exist. Use the current date and keep the line within
  120 characters (wrap as needed). Do not restructure existing entries.
- When called without a note, print the current contents of `docs/ideas.md` for review.

The file is a scratchpad of emerging ideas, not a backlog of planned work — do not turn entries into OpenSpec changes
unless the user asks.