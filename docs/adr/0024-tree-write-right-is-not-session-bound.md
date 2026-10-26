# ADR-0024: A tree's write right is not bound to a session

Date: 2026-10-26

Status: Accepted

Revisit when **any** of the following is met — each alone would change this decision:

- **A mechanism binds a write decision to the session** (external coordinate). The surface the running binary loads
  (`.opencode/node_modules/@opencode-ai/plugin/dist/v2/`, both the `promise` and `effect` variants) gains a permission
  or tool hook whose decision names the calling session and its tree — shell writes included — and can deny before
  execution.
- **A probe sees that mechanism act** (internal trigger). A foreign session's write is rejected before any file changes,
  with a control that detects a write undone before the filesystem is compared.

## Context

A tree that holds an active unit should have exactly one owning session — with its delegates — entitled to write in it;
another session should not make a first write there, temporary files included, and cleaning up afterwards does not
satisfy the requirement.

What the repository has today is a discipline, not a prohibition:

- `AGENTS.md`'s workspace rule requires the workspace be established before the first write, and the `Workspace:` record
  lives inside the unit's own artifacts (`tasks.md`, else the hand-off and the pull-request checklist) — so a foreign
  session does not see it unless it opens another unit's change directory.
- git's worktree registry shows a tree's **branch**, not a session; between units the primary sits on `main` and reads
  as free.
- `scripts/commit-hygiene.py --staged` verifies the staged set's mechanics and has no notion of path ownership (the
  Amendment below records what changed).

An episode motivated the question: several units ago a unit wrote its artifacts in the primary checkout before its own
worktree existed and left them behind. It cannot be verified after the fact: the files are gone, and no git record of
them or of their writer survives once they are — so this decision rests on the mechanism, not on that episode.

The surfaces the binaries actually install, read from `.opencode/node_modules/@opencode-ai/plugin` (installed version
**1.19.4**, the v1 line, which the v2 binary rewrites into each `.opencode/`):

- The legacy `Hooks` surface carries `permission.ask` — whose output can be set to `deny` — and
  `tool.execute.before`/`after`, whose input carries the `sessionID`, plus `shell.env`.
- The v2 `PluginContext`, in both of its variants (`dist/v2/promise/` and `dist/v2/effect/`), exposes `options`,
  `agent`, `aisdk`, `catalog`, `command`, `integration`, `plugin`, `reference` and `skill`: no tool and no permission
  domain.
- v2 loads either the `{ id, setup }` (promise) or the `{ id, effect }` (effect) plugin shape, and nothing else: a
  legacy-shaped plugin does not load, and `AGENTS.md` records the error from the retired plugin that hit it.
- The static `permission` config is per project, not per session, and its `bash` rules match the **command**, not a
  path: a shell write cannot be path-scoped, only forbidden wholesale.

## Decision

Do not implement a session-bound write prohibition now, and do not present anything weaker as one.

- For **v2**, the surface the binary loads offers no prohibition: it has no tool or permission hook, and the static
  rules carry no session dimension at all, so no rule can say that _this_ session may not write in _that_ tree.
- For **v1**, a plausible surface exists, but whether it denies the write **before** it happens is unverified — so the
  finding is "not established", never "impossible".
- The property is therefore recorded as an **unmet requirement**, not a scoped limitation.

Rejected alternatives: a claim marker in the tree (it needs voluntary reading and races two sessions); a staged-set
ownership check (it sees the consequence after the write, and name-based attribution breaks on `opencode/…` branches and
on archive moves); a per-session generated config (the surface the running binary loads has no session-creation boundary
— v1's `experimental_workspace.register` configures a tree, not a session — and a static rule still cannot path-scope
shell for a non-owner).

## Consequences

- Local sessions remain governed by the workspace discipline alone. While a foreign write exists, `git status` lists it;
  once it is deleted, no git record of it and none of **which session** made it survives — which is why the requirement
  is recorded rather than assumed.
- The requirement survives here, under this ADR's revisit gate; the parked verification of the worktree rule stays a
  different obligation and is not merged into it.
- Establishing the v1 leg needs a live probe: an isolated v1 install, a plugin that denies, a session, and a control
  that detects a write which is undone before the filesystem is compared. That belongs to a future implementation
  change, which would also need a delta on `showcase/quality/agent-skills` — the capability that owns the gate's
  procedure — rather than only prose.

## Amendment — 2026-10-26

Two statements above no longer hold as written, and this amendment qualifies them without reopening the Decision.

- The Context says `scripts/commit-hygiene.py --staged` "has no notion of path ownership". It now refuses a staged
  change directory that is not the commit's assigned one, read from a per-worktree claim or from a branch named after
  its change (`reject-a-foreign-change-dir-at-commit`).
- The rejected alternative "a staged-set ownership check" stays rejected **as a write prohibition**: the check cannot
  see the write, does not prove who wrote a file, and is defeated by a spoofed assignment, since the branch and the
  claim are mutable declarations. What the change adopts is the consequence check — the accidental cross-tree sweep into
  a commit — and the Decision above therefore stands: the property remains **not ensured**.
- The two reasons the alternative was rejected are answered only in the narrow sense that made this check possible: a
  recorded claim supplies the attribution a branch name cannot, and comparing change identity replaces rename detection.
  Neither makes a write attributable to its author, which is why the revisit gate above is unchanged.
