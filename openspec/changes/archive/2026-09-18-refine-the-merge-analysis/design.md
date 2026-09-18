## Decisions

**A merge candidate has two dispositions, and the audit names which applies.** Where the originals' anchors and evidence
all survive in a merged text that says something new, the candidate is a merge. Where the merged text would only restate
a rule the file already carries, the candidate is a deletion of the duplicate — the originals are redundant with the
survivor, and merging them would create a second copy of it. This is the distinction the seeded smoke-run surfaced: the
auditor found the merged text correct in itself and then noticed it duplicated an existing bullet.

**It stays one analysis, not a third class.** A deletion of a duplicate is a removal candidate with a different reason
(the content is already there, rather than the rule governing no decision), so it needs no new class and no new count —
it is still reported as a `remove`. The clause names the disposition so the auditor reaches it rather than stopping at
the merged text.
