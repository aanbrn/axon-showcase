---
description: Continue an unfinished unit from its records, establishing the writer handover before any work write
---

Continue the unit this tree holds.

**Trigger** — the owner invokes `/resume-unit`, or asks to continue an unfinished unit, whether or not a pause was
announced.

**Procedure**

1. Load `.opencode/skills/pause-resume-unit/SKILL.md` and follow its resume procedure.

**Repository lifecycle** — this command starts no new unit. Establishing the workspace is the lifecycle the bindings
declare (`docs/contracts/process-bindings.md`). It commits, stashes, pushes, archives, or merges nothing, and stops no
build or container; the bindings declare the review gate that owns the approval stop, and that stop is unchanged.

**Completion criteria** — the skill's resume criteria hold.
