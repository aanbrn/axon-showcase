# Tasks

## 1. The command's remote refresh

- [x] 1.1 Rewrite step 4 of `.opencode/commands/sync-branch.md`: on an explicit invocation, run `gh pr update-branch`,
      re-read the head a bounded number of times, report the head it produced or "requested but unconfirmed" when it
      does not move, report the reason it could not run, and keep the read-only case for a diagnosis or a review round.
- [x] 1.2 Add the local completion as step 5: after a confirmed update, fetch and fast-forward the local branch to
      `origin/<branch>`, and report why when it cannot.
- [x] 1.3 Replace the decision point's current sentence with the qualified stops: the invocation authorizes this remote
      base update, and it is not approval to commit or push the unit's own work, open a pull request, archive, or merge.
- [x] 1.4 Carry the same exception in the command's lifecycle section, so no unqualified prohibition sits beside the
      permission.
- [x] 1.5 Extend the failure handling: a conflicting update is reported with its reason and not resolved by the command.
- [x] 1.6 Name the refreshed remote head (or "requested but unconfirmed") and the local branch's state in the completion
      criteria.
- [x] 1.7 State that step 5 completes the refresh on an open pull request, so steps 6–8 (the local rebase and the rule's
      other routes) apply only when none is open.

## 2. The human-facing record

- [x] 2.1 Add the open pull request's branch to the README's `/sync-branch` row.
- [x] 2.2 Remove the 2026-10-28 `docs/ideas.md` entry "Run the refresh on an explicit `/sync-branch` invocation", which
      this change implements.

## 3. Verification

- [x] 3.1 Run the pre-write derivation pass and record its claims table (`design.md`, twelve rows, each with its
      command).
- [x] 3.2 Run `openspec validate --all`, `./gradlew spotlessApply`, and the Docker-free `./gradlew check`, and record
      the results.
- [x] 3.3 Write the unit's process-defect record.
- [x] 3.4 Run the review gate: the quick loop and the routing decision (the owner resolved the `RECOMMENDED` outcome to
      skip the deep review in favour of the independent consultation), then the manual approval.
