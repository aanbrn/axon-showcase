# Process defects — scope-ideas-to-owner-cleanup

### scope-ideas-to-owner-cleanup#1 — Removing a class left a clause about it in a sibling bullet

**Recorded** — 2026-10-29, by the unit's writing session.

**Stage** — implementation.

**Expected** — removing the parked-entry class from `architecture-auditor` means every sentence about that class goes,
including the clauses in neighbouring bullets that name it (the classification block, the unverifiable-condition bullet,
the inverse-item bullet, the frontmatter).

**Observed** — the two sweep bullets and the classification block were removed, but the unverifiable-condition bullet
still read "an inability to verify must not become an expiry or a due-recurrence claim", where `due-recurrence` names
the retired class. It was found by the token sweep (`grep -n "parked\|landed\|due-recurrence"`), not by the edits.

**Evidence** — the sweep returned
`.opencode/agent/architecture-auditor.md:97:  become an expiry or a due-recurrence claim.`, after the class's own
bullets had been deleted; the line was narrowed to "an expiry claim".

**Consequence** — one extra sweep pass and one edit; nothing shipped with the residue.

**Knowledge boundary** — observed: the leftover clause, the sweep that found it, and the fix. Not checked: whether the
same sweep covered the spec delta, which still ships the old class until the archive applies the delta.
