# Design

## Context

See `proposal.md` — Why. `.opencode/opencode.json` carries a `bash` permission rule `"*git push*": "ask"`, broadened
from `"git push*"` by `#277` ("Catch a chained git push in the permission guard"): at that time OpenCode evaluated the
rule against the whole command string, so an anchored pattern missed a push batched after another command. The version
the cloud action installs (the latest v1 release — `v1.18.35` when the run hung) instead parses the command with
tree-sitter-bash and emits one pattern per parsed `command` node (`packages/opencode/src/tool/shell.ts`:
`commands(root)` + `source(node)`), so each chained command is matched on its own text — `#277`'s premise no longer
holds. The action's shipped handler (`packages/opencode/src/cli/cmd/github.handler.ts`, run as `opencode github run`)
creates the session with a permission override that denies `question` only, and subscribes to no permission event, so an
`ask` it raises for any other permission (a tool permission, `external_directory`, `doom_loop`) is never resolved and
the run blocks. `audit.yml`'s job carries no `timeout-minutes`, so GitHub's 360-minute default is the only bound.

## Goals / Non-Goals

**Goals:**

- Remove the false positive so an unattended run does not block on a read-only command that quotes the phrase.
- Keep the chained-push guard `#277` added.
- Bound the audit job so any residual unanswered prompt fails fast and leaves a downloadable log.

**Non-Goals:**

- Making the agent unable to push: the interactive `ask` stays, since the workflow relies on the owner approving a push
  ("commit and push").
- Adding a timeout to the `/oc` workflow: a human triggers and watches it, and its legitimate duration is less
  predictable.
- Changing the audit workflow's triggers, permissions, or report.

## Decisions

**Anchor the guard to the parsed command (`"git push *"`), not the substring.** The parser emits one pattern per command
node, so the anchored form still matches a `git push` that is bare, carries arguments, or is chained (`a && git push`,
`a | git push`, newline-separated), while a read-only command that quotes the phrase (`grep … "git push" …`) no longer
matches. `"git push *"` (with the space) is preferred over `"git push*"`: `Wildcard.match` turns a trailing ` .*` into
`( .*)?`, so the spaced form matches a bare `git push` and `git push <args>` without matching a stray `git pushx`.
_Alternatives rejected:_ keeping `"*git push*"` and adding `"grep *": "allow"` after it (whack-a-mole — any read-only
command that quotes the phrase still false-positives); `"*git push*": "deny"` (removes the interactive `ask` the
workflow relies on, and turns a false-positive read-only command into a blocked one).

**Bound the audit job (60 min), not `/oc`.** The audit is unattended — no one is watching it — so it needs the backstop;
60 is more than twice its ~29-minute observed maximum. `/oc` is triggered and watched by a human, and a legitimate run's
duration is less predictable, so no timeout is imposed there. Its two cancels were `external_directory` asks
(`/home/runner/.config/gh/*` and the global `@fission-ai/openspec` templates), already granted in
`.opencode/opencode.json`, so the anchoring does not address them; a new `ask` surface would still hang `/oc` until the
human notices (observed at 42m and 17m) — accepted, since a human is present.

**Record the lesson and report the upstream gap.** The substring false positive and its unattended consequence are a
durable lesson for `AGENTS.md`; the handler's resolution of the `question` permission only — and no other permission —
is the gap that turns an `ask` into an indefinite hang, reported upstream with a close-out clause in a file
`upstreamReferences` scans. The venue is derived at posting time from the tracker's search and the closest reports'
cross-references, not a fixed list, since the tracker's state is mutable (the task names the current starting points); a
duplicate report is noise.

## Risks / Trade-offs

- A genuine `git push` in an unattended run still asks and would block until the timeout → the timeout bounds it, and
  the audit prompt never asks the agent to push (the action performs the push itself, outside the permission system).
- The anchored form narrows coverage: `sudo git push` and `VAR=x git push` contain the literal and the old `*git push*`
  matched them, so anchoring drops them, and `#277`'s own residuals (`git -C <dir> push`, a double-spaced or
  backslash-continued push, a push inside a script) were never matched → accepted; the agent is instructed not to push,
  and the timeout bounds a genuine push.
- The owner's global `~/.config/opencode/opencode.jsonc` carries the same `"*git push*"` rule, outside the repository's
  control, so a local session still over-prompts when a read-only command quotes the phrase → the owner updates it
  separately (as `#277` did); the cloud runs read only the project config, so they are unaffected.
- The parser behavior is a property of the installed version → the evidence is the v1 source at the tag the run
  installed (`v1.18.35`) and the run's own log (one evaluated pattern per command node).

## Migration Plan

None: a config line and a workflow bound, both taking effect on the next run (the action installs the latest release and
reads the config at session start).
