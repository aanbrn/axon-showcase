---
description: Put the current unit on pause by writing its resume checkpoint, changing no delivery state
---

Record the pause of the unit this tree holds.

**Trigger** — the owner invokes `/pause-unit`, or says they are stopping for now.

**Procedure**

1. Load `.opencode/skills/pause-resume-unit/SKILL.md` and follow its pause procedure.

**Repository lifecycle** — this command starts no unit: it creates no branch or tree, commits, stashes, pushes,
archives, or merges nothing, stops no build or container, and leaves the tree assigned to the unit. `AGENTS.md`'s
"Workflow: Change lifecycle" owns that lifecycle and its "Workflow: The review gate" the approval stop.

**Completion criteria** — the skill's pause criteria hold, and the turn ends with the pause reported.
