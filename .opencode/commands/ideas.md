---
description: Append a dated idea note to docs/ideas.md
---

Manage the lightweight ideas scratchpad at `docs/ideas.md`.

**Trigger** — the owner invokes `/ideas`, with or without a note.

**Procedure**

- With a note (text after `/ideas`): append it as a dated bullet under a `## YYYY-MM-DD` section in `docs/ideas.md`,
  creating today's section if it does not exist. Use the current date, and do not hand-wrap the prose — the file is
  formatter-wrapped.
- Without a note: print the current contents of `docs/ideas.md` for review.

**Decision points**

- Do not restructure existing entries; a new idea is appended, not merged into an existing one.
- The file is a scratchpad of emerging ideas, not a backlog of planned work — do not turn an entry into an OpenSpec
  change unless the owner asks.

**Repository lifecycle**

- A new unit starts on its own branch, created from a freshly fetched `origin/main`; a unit continuing an existing
  branch or pull request works on that branch.
- The unit's working tree — the primary checkout when no other unit holds it, otherwise the sibling worktree
  `../axon-showcase-<unit>` — is established and recorded before the first write, with an ownership it cannot establish
  treated as occupied, per `AGENTS.md`'s "Workflow: Change lifecycle" **Workspace** rule.
- Unrelated dirty-worktree changes are preserved.
- The work stays uncommitted until the manual approval pass.
- Nothing is committed, pushed, turned into a pull request, archived, or merged without that explicit approval.
- The full per-unit review gate runs over the result. `AGENTS.md`'s "Workflow: Change lifecycle" and "Workflow: The
  review gate" own the branch, uncommitted-work, approval, and review steps, including the reviewer-session preflight;
  this command points at them rather than restating them.

**Completion criteria** — the note is appended under its dated section, or the file's contents have been printed.
