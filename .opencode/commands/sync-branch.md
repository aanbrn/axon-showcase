---
description:
  Check the current branch against origin/main and, on the owner's request, refresh it by the rule AGENTS.md states
---

Refresh the current branch when it has fallen behind, by the procedure the repository already states.

**Trigger** — the owner invokes `/sync-branch`.

**Procedure**

1. Fetch, then compare the current branch with `origin/main`: the commits behind and ahead, the working-tree state, and
   whether a pull request is open. Report those facts. IF the fetch fails, THEN report the remote's advance as
   unestablished against the ref present and do not refresh: a comparison against a stale ref is not evidence.
2. IF nothing is owed, THEN say so and stop.
3. IF the branch carries its own commits beside uncommitted work, THEN stop and report what has to be resolved first:
   every route below runs only on a tree whose work is settled.
4. IF a pull request is open, THEN run `gh pr update-branch` on an explicit invocation and report the head it produced,
   or the reason it could not run. Re-read the head a bounded number of times; if it does not move, report the update as
   requested but unconfirmed rather than as done. The default merges the base into the pull request's branch; `--rebase`
   rewrites that history, so it is not the default. A conflicting update is reported with its reason and is not resolved
   here. A call reached from a diagnosis or a review round stays the read-only case the decision points name.
5. After a confirmed update, fetch the branch and fast-forward the local copy to `origin/<branch>`; if it cannot
   fast-forward, report that and leave the local branch where it is. This completes the refresh when a pull request is
   open: steps 6–8 apply only when none is.
6. IF the branch carries its own commits on a clean tree, THEN rebase it against `origin/main`.
7. Otherwise refresh it by `AGENTS.md`'s "Refreshing a branch", following the route the rule gives the state you found —
   including recreating a branch that has no work at all, and stashing first where the update touches a file you
   changed.
8. Resolve a local conflict for both sides — never a wholesale side-pick — and then finish the operation the conflict
   interrupted rather than starting it again: `git rebase --continue` after a rebase, or the re-apply of the stash it
   interrupted. Confirm both facts survive in every file both sides touched.
9. Report the incoming revision, the route that ran, the refreshed head or that the update was requested but
   unconfirmed, and the local work and branch state preserved.

**Decision points**

- An explicit invocation authorizes this refresh, including the remote base update for an open pull request. It is not
  approval to commit or push the unit's own work, open a pull request, archive, or merge.
- A call reached from a diagnosis or a review round is a read-only check: report the comparison, and refresh only if the
  owner asks.
- IF a conflict cannot be resolved from the evidence in hand, THEN stop with the paths named and ask the owner; do not
  pick a side.
- IF the route was a rebase, THEN say so: the branch's history changed, and the result is not a fast-forward.

**Failure handling** — a refresh that cannot complete is reported with its reason, leaving the tree as it was or the
stash kept with the conflicted paths named. No reset and no checkout reaches over local work. A pull request update that
reports conflicts is reported with its reason and not resolved here; a local conflict keeps the paths named and the
stash kept.

**Completion criteria** — the owner has the compared state and one of: "nothing owed"; the pull request update's failure
and its reason; that the update was requested but unconfirmed; the refreshed head with the local branch's state, or why
it could not advance; or, for a local route, the refreshed revision with the route that ran and the local work
preserved.

**Repository lifecycle**

- A new unit starts on its own branch, created from a freshly fetched `origin/main`; a unit continuing an existing
  branch or pull request works on that branch.
- The unit's working tree — the primary checkout when no other unit holds it, otherwise the sibling worktree
  `../axon-showcase-<unit>` — is established and recorded before the first write, with an ownership it cannot establish
  treated as occupied, per `AGENTS.md`'s "Workflow: Change lifecycle" **Workspace** rule.
- Unrelated dirty-worktree changes are preserved.
- The work stays uncommitted until the manual approval pass.
- Nothing is committed, pushed, turned into a pull request, archived, or merged without that explicit approval, apart
  from the remote base update an explicit invocation authorizes.
- The full per-unit review gate runs over the result. `AGENTS.md`'s "Workflow: Change lifecycle" and "Workflow: The
  review gate" own the branch, uncommitted-work, approval, and review steps, including the reviewer-session preflight;
  this command points at them rather than restating them.
