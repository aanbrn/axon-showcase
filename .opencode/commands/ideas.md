---
description: Append a dated idea note to docs/ideas.md, print the file, or clean up its oldest batch with the owner
---

Manage the lightweight ideas scratchpad at `docs/ideas.md`.

**Trigger** — the owner invokes `/ideas`, with or without a note, or `/ideas cleanup`.

## Append and print

- **With a note** (text after `/ideas`): append it as a dated bullet under a `## YYYY-MM-DD` section, creating today's
  section if it does not exist. Use the current date, and do not hand-wrap the prose — the file is formatter-wrapped.
- **Without a note**: print the current contents of `docs/ideas.md` for review.

## Cleanup mode

1. Take the **oldest dated section after the `Last cleanup:` date in `docs/ideas.md`'s header** (the oldest section when
   that line reads `none`), and read its entries in full. If no section is after the cursor, report that every section
   has been covered and ask whether to reset the cursor to `none` — do not re-open a section it already covered. **This
   step ends the turn** — the reset is applied only under the owner's answer.
2. Present the batch: each entry with its own text, and the disposition you propose for it.
3. Ask the owner for one disposition per entry, from the table below. **This step ends the turn** — no disposition is
   applied until the owner answers.
4. Apply the answers and set the header's `**Last cleanup:**` line to this section's date once **every** entry in the
   batch has a disposition, then stop: the next invocation starts after the section just covered. An entry the owner
   leaves undisposed leaves that line unchanged, so the next invocation presents the same section again.

| Disposition | Meaning                                   | Effect on the entry                                                                                                               |
| ----------- | ----------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------- |
| `keep`      | it still says something wanted            | stays, with its date; no re-justification needed                                                                                  |
| `narrow`    | part of it landed, or its scope moved     | rewritten to the surviving part, which keeps its date                                                                             |
| `promote`   | it becomes an OpenSpec change or an issue | stays; an issue replaces its tag with `— promoted to issue #NNN; no change yet.`, an OpenSpec change until that change removes it |
| `drop`      | nobody wants it                           | removed, after its durable lesson is routed                                                                                       |

Decision points for a cleanup:

- Do not restructure existing entries; a batch rewrites only the entries the owner disposes.
- The file is a scratchpad of emerging ideas, not a backlog of planned work — do not turn an entry into an OpenSpec
  change unless the owner asks.
- **A `narrow` or `drop` that rests on "this is already done" verifies that claim first** — the change, commit, workflow
  or mechanism the entry describes exists, or a different mechanism now achieves the objective the entry names. A
  reference the entry merely cites does not establish it, and a claim that cannot be verified leaves the entry to the
  owner's judgement rather than to a silent removal.
- **A `narrow` or `drop` routes the durable lesson before the entry leaves** — the header's removal rule owns the
  destinations — and nothing when the entry carried no lesson.
- A retained entry keeps its original date and needs no re-justification. There is no renewal date and no expiry.
- Do not verify every entry against the repository: investigate a particular entry only when its disposition needs the
  evidence.

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

**Completion criteria** — the note is appended under its dated section, the file's contents have been printed, a cleanup
batch's dispositions have been applied under the owner's answer, or the every-section-covered branch has been reported
and its reset awaited.
