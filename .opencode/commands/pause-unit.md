---
description: Put the current unit on pause by writing its resume checkpoint, changing no delivery state
---

**Lifecycle:** cross-cutting | **Invocation:** owner-invoked | **Effect:** operation within an existing unit |
**Owner:** .opencode/skills/pause-resume-unit/SKILL.md

Record the pause of the unit this tree holds.

**Trigger** — the owner invokes `/pause-unit`, or says they are stopping for now.

**Procedure**

1. Load `.opencode/skills/pause-resume-unit/SKILL.md` and follow its pause procedure.

**Repository lifecycle** — this command starts no unit: it creates no branch or tree, commits, stashes, pushes,
archives, or merges nothing, stops no build or container, and leaves the tree assigned to the unit. The bindings
(`docs/contracts/process-bindings.md`) declare the lifecycle and the review gate that own that lifecycle and the
approval stop.

**Completion criteria** — the skill's pause criteria hold, and the turn ends with the pause reported.
