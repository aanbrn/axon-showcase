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
3. IF a pull request is open, THEN the route is `gh pr update-branch`. Name it and hand it to the owner rather than
   running it.
4. IF the branch carries its own commits on a clean tree, THEN rebase it against `origin/main`.
5. IF the branch carries its own commits beside uncommitted work, THEN stop and report what has to be resolved first.
6. Otherwise refresh it by `AGENTS.md`'s "Refreshing a branch", following the route the rule gives the state you found —
   including recreating a branch that has no work at all, and stashing first where the update touches a file you
   changed.
7. Resolve a conflict for both sides — never a wholesale side-pick — and then finish the operation the conflict
   interrupted rather than starting it again: `git rebase --continue` after a rebase, or the re-apply of the stash it
   interrupted. Confirm both facts survive in every file both sides touched.
8. Report the incoming revision, the route that ran, and the local work preserved.

**Decision points**

- An explicit invocation authorizes this refresh. It is not approval to commit, push, open a pull request, archive, or
  merge.
- A call reached from a diagnosis or a review round is a read-only check: report the comparison, and refresh only if the
  owner asks.
- IF a conflict cannot be resolved from the evidence in hand, THEN stop with the paths named and ask the owner; do not
  pick a side.
- IF the route was a rebase, THEN say so: the branch's history changed, and the result is not a fast-forward.

**Failure handling** — a refresh that cannot complete is reported with its reason, leaving the tree as it was or the
stash kept with the conflicted paths named. No reset and no checkout reaches over local work.

**Completion criteria** — the owner has the compared state and either "nothing owed", or the refreshed revision with the
route that ran and the local work preserved.

**Repository lifecycle**

- A new unit starts on its own branch, created from a freshly fetched `origin/main`; a unit continuing an existing
  branch or pull request works on that branch.
- The unit's working tree — the primary checkout when no other unit holds it, otherwise the sibling worktree
  `../axon-showcase-<unit>` — is established and recorded before the first write, with an ownership it cannot establish
  treated as occupied, per `AGENTS.md`'s "Workflow: Change lifecycle" **Workspace** rule.
- Unrelated dirty-worktree changes are preserved.
- The work stays uncommitted until the manual approval pass.
- Nothing is committed, pushed, turned into a pull request, archived, or merged without that explicit approval.
- The full per-unit review gate runs over the result. `AGENTS.md`'s "Workflow: Change lifecycle" and "Workflow: The
  review gate" own the branch, uncommitted-work, approval, and review steps, including the reviewer-session preflight;
  this command points at them rather than restating them.
