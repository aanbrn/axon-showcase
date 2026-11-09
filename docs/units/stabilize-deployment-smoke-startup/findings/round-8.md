- [coherence] design.md:69-76 — the gate republishes per attempt but passed a single `warmUpShowcaseId` (also
  tasks.md:18-23, proposal.md:17-22, and the load-tests delta), so an earlier attempt's buffered event stays unexcluded
  while its ID is not the one passed, reopening the hollowed check.
- [stale-base] origin/main@0deca079 — the branch sat on `2b4c9a00` behind #872, which rewrote the
  `deployment-operations` pack this unit edits, so the tree and the pack reading were stale.
- [test-control] tasks.md:34-37 — controls A and B run the smoke profile (also tasks.md:42-48), which never builds the
  mid-hold check, so the exclusion on that second check would be unexercised.
