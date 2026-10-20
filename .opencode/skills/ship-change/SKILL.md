---
name: ship-change
description:
  Ship an approved unit through push → pull request → CI → archive → merge, and complete a cloud agent's `/oc` pull
  request. Load it before the first `git push`, `gh pr create`, `openspec archive`, or `gh pr merge`.
---

# Ship a change

The authoritative procedure for the two stages `AGENTS.md` triggers. It grants no permission of its own: the invariants
and the review gate in `AGENTS.md` govern, and the owner's approval is the precondition for every guarded step.

## Ship a change (CI → archive → merge)

Source: `AGENTS.md` "Workflow: Ship a change (CI → archive → merge)".

**Trigger** — the user approved the implementation.

1. Push the implementation branch and open a pull request with the code and the active change directory.
2. Wait for the `build` check to be green.
3. Archive the change (move the change directory and sync the main spec) as an additional commit in the _same_ pull
   request.
4. Merge once. The archive — the declaration that a change is done — always follows CI, never precedes it.

### Decision points

- IF the active GitHub user **is** the repository owner or an administrator, THEN merge directly with
  `gh pr merge --squash --delete-branch --admin` once CI is green — do not first attempt a plain merge (the
  `main-require-pr-on-merge` ruleset requires an approving review, `required_approving_review_count: 1`, but the owner
  `aanbrn` is a bypass actor on that ruleset, `bypass_mode: always`) and do not wait on a Copilot review (it never
  approves).
- IF the active user is **not** an administrator (e.g. a team member), THEN `--admin` is meaningless and the normal
  review-required flow applies: request a reviewer and wait for approval before merging.
- IF the pull request's base advanced after CI ran (a `mergeStateStatus` of BEHIND) THEN run `gh pr update-branch` and
  merge once the re-run is green (see the BEHIND gotcha).
- **Docs the change causes while it is in flight** — a capture from its implementation, an idea its own work surfaced, a
  fact it falsified — ship with the change's pull request; a docs refresh it did not cause, or one surfaced only after
  it is done (a merge-time capture), ships as its own docs pull request, per the docs-refresh convention.

### Failure handling

- IF the local branch is not named after the remote branch it must update THEN push with an explicit refspec (see the
  "Fork every new branch" invariant).
- GitHub never merges on an approval — an approval alone leaves the pull request open.

### Completion criteria

CI green, the change archived (change directory moved and main spec synced), and the pull request merged.

## Complete a cloud (`/oc`) agent pull request

Source: `AGENTS.md` "Workflow: Complete a cloud (`/oc`) agent PR".

**Trigger** — a `/oc` (or `/opencode`) timeline comment or inline diff comment created or updated a pull request you are
asked to complete. A `/oc` in a _review body_ (Submit review → Comment/Request changes/Approve) does nothing: the action
does not support `pull_request_review`, so a reviewer who wants to instruct the agent leaves the review and posts the
comment. The root entry point carries the upstream reference and its close-out.

1. The action runs OpenCode on a GitHub-hosted runner; it implements the work on its own `opencode/…` branch, commits,
   and opens the pull request (on an existing pull request the same comment commits to that pull request instead). It
   follows the same rules a local agent does — an OpenSpec change with a change directory, `skip_specs: true` for a pure
   dependency bump, and the change left unarchived, since archiving follows the owner's approval — but it does not
   archive or merge.
2. Verify the self-report against the repository and the run log (a self-report is a claim to verify, like a review
   finding).
3. Check out the agent's branch.

### Decision points

- **An approval comment (`/oc looks good. let's proceed.`) maps to _its_ implementation.** On a **change** pull request
  that is the commit → push → archive sequence: run `openspec archive <change> --yes`, commit and push it there, and
  merge once CI is green (see "Ship a change" above). On a **report** pull request (the `audit` workflow's findings)
  there is no change to archive and no finding to apply — the report is the artifact, so the instruction means **merge
  the report** (it lands under `docs/audits/` as the run's record) and apply nothing from it. Say so explicitly, rather
  than leaving the agent to infer a change it cannot find, or to close a pull request whose file the workflow exists to
  keep.
- IF a pull request was opened from a tracker issue THEN it carries a closing reference to its trigger
  (`Closes #<issue>`), so merging it closes that issue — right for a one-shot work item, wrong for a long-lived tracker
  like the update-check issues, whose workflows look them up with `is:issue is:open` and open a fresh one when none is
  open, so the merge orphans its history and the next weekly run opens a duplicate. Strip the closing keyword before
  merging, or reopen the tracker.

### Failure handling

- The cloud agent **cannot merge** — the action has no merge step — so on a report pull request it must not attempt one;
  the merge is the human's.

### Completion criteria

The pull request is merged: a change pull request archived and merged per "Ship a change", a report pull request merged
with nothing applied, and any tracker preserved.

## How the action derives the pull request

The action uses the agent's final response as the pull request **body** and derives the **title** by asking a model to
summarise that response in under 40 characters (`summarize` → `createPR(base, branch, title, body)` in the action's
`github.handler.ts`), so the title is a _summary of the whole response_, not a line copied from it. Two consequences: a
prompt cannot pin the title by dictating the response's first line, and a response that opens with a mention tends to
summarise into a title about the mention (the `audit` workflow's prompt asked for the owner mention at the start of the
response, and report pull request #333 carried it in the title). Put the mention on a body line and keep the response's
substance about the report, so the generated title names it. When a workflow prompt relies on the action's
pull-request-building behaviour, state the title's derivation rather than assuming a copied line.

## Maintenance

Correct a step or branch here, in place; do not recreate it in `AGENTS.md`. `AGENTS.md` keeps the stage trigger, the
authorization boundaries, and the archive ordering, and names this skill.
