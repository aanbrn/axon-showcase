---
description:
  Append a dated idea note to docs/ideas.md, print the file, promote one entry, or clean up its oldest batch with the
  owner
---

Manage the lightweight ideas scratchpad at `docs/ideas.md`.

**Trigger** — the owner invokes `/ideas`, with or without a note, `/ideas promote <words>`, `/ideas promote --top`, or
`/ideas cleanup`.

## Append and print

- **With a note** (text after `/ideas`): append it as a dated bullet under a `## YYYY-MM-DD` section, creating today's
  section if it does not exist. Use the current date, and do not hand-wrap the prose — the file is formatter-wrapped.
- **Without a note**: print the current contents of `docs/ideas.md` for review.

## Cleanup mode

1. Take the **oldest dated section after the `Cleanup cursor:` date in `docs/ideas.md`'s header** (the oldest section
   when that line reads `none`), and read its entries in full. If no section is after the cursor, report that every
   section has been covered and ask whether to reset the cursor to `none` — do not re-open a section it already covered.
   **This step ends the turn** — the reset is applied only under the owner's answer, and it writes
   `**Cleanup cursor:** none — no section is covered.`
2. Present the batch: each entry with its own text, and the disposition you propose for it.
3. Ask the owner for one disposition per entry, from the table below. **This step ends the turn** — no disposition is
   applied until the owner answers.
4. Apply the answers and set the header's `**Cleanup cursor:**` line to this section's date once **every** entry in the
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
- **Removing a section's last entry removes its heading too** — the file's header states the rule, and
  `./gradlew verifyIdeasSections` fails the build on an empty dated section.
- A retained entry keeps its original date and needs no re-justification. There is no renewal date and no expiry.
- Do not verify every entry against the repository: investigate a particular entry only when its disposition needs the
  evidence.

## Promote an entry

**Trigger** — `/ideas promote <words>` names an entry by a few of its words; `/ideas promote --top` nominates the next
entry to reconsider.

1. Locate the entry: with `<words>`, match them against the file. When more than one matches, name the candidates and
   ask which; when none matches, report that and stop — never nominate an unrelated entry. With `--top`, apply the
   policy below.
2. Verify the entry's decisive evidence at its owning source before proposing: a recorded reopening condition is checked
   for what it names, with its own qualifiers and compound clauses, and the result is reported beside the entry as met,
   not met, or not verifiable. For `--top` that result decides eligibility (below); for a named entry it informs the
   owner's decision without vetoing it, since the condition is a deferral trigger rather than a prohibition.
3. Present the entry — or the nomination with its reason, its evidence, and what could not be verified — and ask for the
   destination and the confirmation: `issue` (a direction that needs a home now but is not ready to implement, whose
   issue links to the eventual OpenSpec change) or `change` (start the cycle now). **This step ends the turn** — nothing
   is created or tagged until the owner answers.
4. Apply the answer: for an `issue`, create it and tag the entry as the cleanup table's `promote` row states; for a
   `change`, start the propose cycle and leave the entry for that change to remove.

**The `--top` policy** — "the next entry to reconsider", never "the most valuable work":

- Exclude an entry already carrying a promotion tag or named by an active change.
- A recorded reopening condition must be demonstrably met. A condition that cannot be verified stays unknown, and a
  condition verified as not met keeps its entry closed to nomination.
- Nominate only from two sets: entries with an evidenced, satisfied condition first, then entries that record no
  reopening condition at all.
- Tie-break: the oldest capture date, then the entry's opening words. That is an attention order, not a value ranking.
- Show a nomination's dependency, recurrence, or risk evidence beside it as context; never fold those signals into a
  hidden score.
- Present exactly one nomination; step 3 owns the fields it carries.

**Failure handling** — IF verification disproves a `--top` nomination, THEN disclose it and consider the next; IF an
ambiguity could change the selection, THEN abstain and ask one question; IF the owner declines, THEN leave the entry
unchanged and stop, since a rejection is neither `drop` nor a demotion; IF nothing qualifies, THEN say so rather than
nominate.

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

**Completion criteria** — the note is appended under its dated section, the file's contents have been printed, a
promotion's destination and confirmation have been asked and the promotion applied under the owner's answer (or nothing
qualifying is so stated), a cleanup batch's dispositions have been applied under the owner's answer, or the
every-section-covered branch has been reported and its reset awaited.
