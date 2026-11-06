---
description:
  Continue an unfinished unit from its records — by name, or by the paused units it finds — establishing the writer
  handover before any work write
---

**Lifecycle:** cross-cutting | **Invocation:** owner-invoked | **Effect:** operation within an existing unit |
**Owner:** .opencode/skills/pause-resume-unit/SKILL.md

Continue an unfinished unit.

**Trigger** — the owner invokes `/resume-unit`, with or without a unit name, or asks to continue an unfinished unit,
whether or not a pause was announced.

**Procedure**

1. Load `.opencode/skills/pause-resume-unit/SKILL.md` and follow its resume procedure.
2. With a name, resolve that unit before touching the tree: run `python3 scripts/paused-units.py --unit <unit>` and
   require one `assignment`, from the record whose `path` and `branch` name that tree; in this mode `selectable` means
   the unit is uniquely resolved, and `candidates` stays empty for a unit that is not paused.
3. Without a name, run `python3 scripts/paused-units.py` and read its document: report any `unknown` entry, an
   incomplete result, or a `selectable` flag that is false, and continue nothing until the set is established; otherwise
   one paused unit continues, several are presented for the owner's choice and the turn ends, and none is reported and
   the turn ends.

**Repository lifecycle** — this command starts no new unit. Establishing the workspace is the lifecycle the bindings
declare (`docs/contracts/process-bindings.md`). It commits, stashes, pushes, archives, or merges nothing, and stops no
build or container; the bindings declare the review gate that owns the approval stop, and that stop is unchanged. A
session in another tree moves there only after the unit is resolved and verified.

**Completion criteria** — the skill's resume criteria hold.
