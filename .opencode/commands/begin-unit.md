---
description: Establish an owner-authorized unit's workspace, record, and claim before its first content write
---

Begin a unit's lifecycle. This command owns the entry point; the Begin skill owns the procedure.

**Lifecycle:** Begin | **Invocation:** owner-invoked, procedure-invoked | **Effect:** unit start | **Owner:**
`.opencode/skills/begin-unit/SKILL.md`.

**Trigger** — the unit lifecycle's Begin stage; an explicit `/begin-unit <unit>` or `/begin-unit --continue <branch>`
invocation also enters it, subject to the same authorization and Workspace rules.

**Procedure**

1. Load `.opencode/skills/begin-unit/SKILL.md` and follow its steps. A missing or unreadable skill stops the command:
   report it rather than reconstructing the procedure.

**Repository lifecycle** — this command starts a unit, so it owes the lifecycle `AGENTS.md`'s "Workflow: Unit lifecycle"
states. Its workspace is the primary checkout when no other unit holds it and a sibling `../axon-showcase-<unit>`
worktree otherwise, established before its first write, with an ownership it cannot establish treated as occupied. A new
unit takes its own branch from a freshly fetched `origin/main`; a unit continuing an existing branch or pull request
works on that branch. Unrelated dirty changes are preserved. The unit begins only on the owner's authorization, and its
proposal passes the owner's manual review before any implementation edit. Work stays uncommitted until the
implementation's approval, apart from a commit the owner explicitly requests and the proposal-stage draft pull request
the lifecycle's procedure documents, and no commit, push, pull request, archive, or merge happens before that approval.
The review gate owns the gate and the bindings declaration locates the declared roles.

**Completion criteria** — the skill's completion criteria hold, and the established stage, pending decision, and next
permitted operation are reported.
