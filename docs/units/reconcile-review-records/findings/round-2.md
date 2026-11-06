- [missing-task] docs/units/reconcile-review-records/ — the unit holds only `unit.md`: no ledger, no instances files, no
  hand-over and no process-defect record, though `unit.md` states round 1 raised twelve findings.
- [claim-unreproducible] design.md:11,111 — `git ls-tree … | grep advisor` returns sixteen paths because it matches unit
  directory names; only `grep 'findings/advisor-'` returns the four.
- [claim-unreproducible] design.md:121 — the row claims six hits; the stated command returns two, and the other four
  belong to a different pattern.
- [claim-unreproducible] design.md:122 — the records are three and zero, not "three and three": the pin unit's file
  states that no process defect was observed.
- [claim-unreproducible] design.md:112 — `git show <rev>:docs/units/*/review-handover.md` cannot run; a glob is not
  expanded after `<rev>:`.
- [missing-task] tasks.md:48-49 — task 3.2 appends a record into a file that states none was observed, leaving both
  assertions in one file.
- [coherence] design.md:55 — the pin unit's nine entries fold into a proposal-screen section, though its second pass is
  an implementation pass about the shipped tool.
- [contradiction] unit.md:20 — the record's Change set says nothing is written while seven paths are staged.
- [over-claim] design.md:76 — "the single owner of what `findings/` holds" omits the normative sentence in
  `showcase/quality/agent-skills` that also states where a round's instances are written.
- [premise] tasks.md:39-42 — task 2.4 names a review-gate step for a non-reviewer pass that the skill does not have.
- [contradiction] design.md:7 — "one did not hold" while its own table records two parked premises the repository
  contradicts.
