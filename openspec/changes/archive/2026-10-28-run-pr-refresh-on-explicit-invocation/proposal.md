# Proposal

## Why

`/sync-branch` exists to refresh a branch on the owner's request, yet its step 3 hands an open pull request's update to
the owner while the same procedure's decision point says an explicit invocation authorizes the refresh. In
`bump-setup-pack-action-2026-10-28` the owner invoked it, received a comparison and a named route, and had to ask twice
more before the head moved: the one case the command exists for is the one it does not perform.

## What Changes

- `.opencode/commands/sync-branch.md` step 4 runs `gh pr update-branch` on an explicit invocation and reports the head
  it produced, or the reason it could not.
- Step 5 fast-forwards the local branch to the refreshed remote head, or reports why it cannot, so "refreshed" names
  both copies.
- The decision point states that the invocation authorizes this remote base update, and that the stops on the unit's own
  work — commit, push of that work, pull request, archive, merge — are unchanged.
- The command's lifecycle section carries the same exception explicitly, so no unqualified prohibition sits beside the
  permission (the shape `add-flag-process-defect-command#3` recorded).
- A server-side update that conflicts is reported with its reason rather than resolved by the command: resolving it
  would rewrite the pull request's branch and need a force-push the invocation does not authorize.
- The completion criteria name the refreshed remote head and the local branch's state.
- `README.md`'s `/sync-branch` row names the open pull request's branch among the states it refreshes.
- `docs/ideas.md` loses its 2026-10-28 entry "Run the refresh on an explicit `/sync-branch` invocation", which this
  change implements.

## Capabilities

### New Capabilities

none

### Modified Capabilities

none

## Impact

- `.opencode/commands/sync-branch.md`, `README.md`, and `docs/ideas.md`.
- No delta spec: no capability describes the command (`git grep sync-branch` over `openspec/specs/` returns nothing),
  and the mutating-unit lifecycle requirement's subject does not reach a command that refreshes an existing branch.
- No JVM code, build script, test, deployment, or API change.
- `/sync-branch`'s behaviour for an open pull request changes; a read-only call from a diagnosis or a review round stays
  as it is.
