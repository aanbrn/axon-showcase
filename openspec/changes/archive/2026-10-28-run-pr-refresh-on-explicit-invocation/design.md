# Design

## Context

This design accompanies `proposal.md`, which states why the refresh must run on the explicit invocation.

- `/sync-branch` was added by `2026-10-27-add-sync-branch-command` with `skip_specs: true`. Its D2 records the hand-off
  as a deliberate addition beside the authorization ("an explicit invocation authorizes the refresh … an open pull
  request is handed to the owner"). Its D3 says no capability describes the command's own contract **and** that "the
  capability that covers a project-authored command's shape (`showcase/quality/agent-skills`) … already govern[s] it" —
  the clause this design has to reconcile, resolved in D5. This change reverses the hand-off deliberately; the archived
  design is not edited.
- `bump-setup-pack-action-2026-10-28#2` is the incident: an explicit invocation produced a comparison and a named route;
  the head stayed `dfe142fb`; after a third owner message `gh pr update-branch` produced `6dd2a768`.
- The README's `/sync-branch` row already advertises "on request, refreshes it"; `AGENTS.md`'s branch-refresh rule and
  the ship stage's BEHIND recovery already name `gh pr update-branch` as the route once a pull request is open. The
  command is the only site that refuses it.
- The lifecycle requirement (`showcase/quality/agent-skills`) states "no commit, push, pull request, archive, or merge
  without that approval", but its subject is a command that **starts a mutating unit that ships**; `/sync-branch`
  refreshes an existing branch and starts none, so the requirement does not bind it. The command's lifecycle section is
  its own text, and `AGENTS.md`'s invariant already permits a push the owner explicitly requests. Step 3 obeys the
  prohibition by handing the update back while the decision point contradicts it; `add-flag-process-defect-command#3` is
  the recorded lesson that a prohibition beside a permission must name the exception rather than leave it to inference.
- `docs/ideas.md` 2026-10-28 parks direction A, which this change implements.

## Goals / Non-Goals

### Goals

- The explicit invocation performs the open pull request's remote base update and reports the head it produced.
- The local branch reaches the same refreshed commit, so the command's comparison and the remote agree.
- The approval boundary is stated identically in the command's decision point and its lifecycle section.

### Non-Goals

- No resolution of a pull request update's conflict, and no force-push, from this command; the local routes keep their
  both-sides conflict resolution.
- No change to `AGENTS.md`'s branch-refresh rule or to the ship stage, which already name the route.
- No general branch-refresh capability spec (D5).

## Decisions

### D1 — The explicit invocation authorizes the remote base update

- What: step 4 runs `gh pr update-branch`, after step 3's stop for a branch holding its own commits beside uncommitted
  work (the root rule has no exception for an open pull request); the decision point and the lifecycle section state the
  authorization.
- Why: the command's advertised purpose is a refresh on request, and the alternative — keeping the hand-off — leaves the
  owner to run the route by hand after asking for it, which is the incident.
- The reversal is named: the archived design chose the hand-off deliberately, so this is a behaviour change, not a
  wording repair.

### D2 — "Refreshed" completes on the local branch too

- What: step 5, after a confirmed remote update, fetches and fast-forwards the local branch to `origin/<branch>`; if it
  cannot fast-forward, report that and leave the local branch where it is.
- Why: the command compares the local branch and promises a refreshed revision. A server-side merge leaves the local
  checkout behind, so a head-only report leaves the two copies disagreeing, and the owner's next `git status` still
  shows the old base.
- Alternative rejected: report the remote head only; it satisfies the pull request and not the command's own comparison.

### D3 — A conflicting update is reported, not resolved

- What: a `gh pr update-branch` failure on conflicts is reported with its reason, and the command stops.
- Why: resolving it locally rewrites the pull request's branch and needs a force-push, which the invocation does not
  authorize; D1 authorizes one update.
- The local rebase and merge routes stay for a branch with no open pull request.

### D4 — The approval boundary is stated at every site that carries it

- What: the command's decision point and its lifecycle section say the same thing: the invocation authorizes this remote
  base update, and the stops on the unit's own work are unchanged.
- Why: `add-flag-process-defect-command#3` — a permission and its prohibition must be readable together.

### D5 — No spec delta is owed

- What: the change carries `skip_specs: true` and no delta artifact; the command's own text states the boundary.
- Why: the mutating-unit lifecycle requirement's subject is a command that starts a unit that ships, and `/sync-branch`
  starts none; `git grep sync-branch` over `openspec/specs/` returns nothing, so no capability describes the command.
  The authorization rests on `AGENTS.md`'s explicit-request invariant, which already permits the push the owner
  requests.
- The archived D3 clause reconciled: "the capability that covers a project-authored command's shape
  (`showcase/quality/agent-skills`) … already govern[s] it" describes the _shape_ the command adopts — the lifecycle
  section it carries — not a SHALL whose antecedent it meets. The requirement's obligation is stated by "a command that
  starts a mutating unit that ships", which `/sync-branch` is not; a recorded rationale cannot widen an antecedent, and
  adopting a template does not make its requirement binding. The command's own text is therefore the site of its
  boundary.
- Reversal: an earlier plan added a `MODIFIED` exception to that requirement, reading D3's "govern" as a finding of
  coverage. That reading was wrong; widening the requirement's subject would impose a new corpus-level obligation on
  every branch-mutating command, which this change does not need. The archived design's `skip_specs: true`
  classification stands for a definition edit the spec does not describe.
- Alternative rejected: `REMOVED` + `ADDED` to widen the subject; it formalizes an unnecessary expansion rather than
  repairing the premise.

### D6 — README row and idea removal

- What: the README's row names the open pull request's branch among the states it refreshes; the `docs/ideas.md` entry
  is removed.
- Why: the docs-refresh convention — the row describes the capability this change alters, and an implemented idea leaves
  the file.

## Claims table

| #   | Claim                                                                       | Source                                                                                          | Reproduced                                                                                                                                                                                                                                   |
| --- | --------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1   | Step 3 hands the route back while the decision point authorizes the refresh | `git show 5a2895b5:.opencode/commands/sync-branch.md \| sed -n '16,17p;30,31p'`                 | "Name it and hand it to the owner rather than running it." / "An explicit invocation authorizes this refresh. It is not approval to commit, push, open a pull request, archive, or merge."                                                   |
| 2   | The archived design records the hand-off deliberately                       | `git show 5a2895b5:openspec/changes/archive/2026-10-27-add-sync-branch-command/design.md`       | D2: "…and adds only what the rule does not say: that an explicit invocation authorizes the refresh, that a call from a diagnosis is read-only, that an open pull request is handed to the owner, and what to report"; D3: `skip_specs: true` |
| 3   | The lifecycle requirement and its five scenarios                            | `git show 5a2895b5:openspec/specs/showcase/quality/agent-skills/spec.md \| sed -n '1330,1367p'` | the requirement text and its five scenarios                                                                                                                                                                                                  |
| 4   | No capability describes the command                                         | `git grep -n "sync-branch" 5a2895b5 -- openspec/specs/`                                         | no hits (exit 1)                                                                                                                                                                                                                             |
| 5   | The README row advertises the refresh                                       | `git show 5a2895b5:README.md \| grep -n "sync-branch"`                                          | line 619: "… and, on request, refreshes it by the rule `AGENTS.md` states, resolving conflicts for both sides"                                                                                                                               |
| 6   | `AGENTS.md` already names the route                                         | `git show 5a2895b5:AGENTS.md \| grep -n "update-branch"`                                        | line 416 ("once a PR is open, `gh pr update-branch` instead"), line 1643, line 2116 (the BEHIND recovery)                                                                                                                                    |
| 7   | The parked direction exists                                                 | `git show 5a2895b5:docs/ideas.md \| grep -n "Run the refresh on an explicit"`                   | line 66, under `## 2026-10-28`, with the approval-boundary rationale                                                                                                                                                                         |
| 8   | The incident's head values                                                  | `git show 5a2895b5:docs/process-defects/bump-setup-pack-action-2026-10-28.md`                   | #2: `dfe142fb` after the invocation, `6dd2a768` after the later run                                                                                                                                                                          |
| 9   | The route merges the base by default and offers `--rebase`                  | `gh pr update-branch --help`                                                                    | "The default behavior is to update with a merge commit … To reconcile … rebasing … `--rebase`"                                                                                                                                               |
| 10  | The command is the only site that hands the route back                      | `git grep -n "hand it to the owner" 5a2895b5 -- .opencode/ AGENTS.md README.md`                 | one hit, `.opencode/commands/sync-branch.md:16`                                                                                                                                                                                              |
| 11  | An explicit request already permits the push the refresh performs           | `git show 5a2895b5:AGENTS.md \| grep -n "Never push to the remote automatically"`               | line 135: "Never push to the remote automatically. Commit locally when asked; run `git push` only when the user explicitly requests it."                                                                                                     |
| 12  | A definition edit the spec does not describe ships with `skip_specs`        | `git show 5a2895b5:AGENTS.md \| grep -n "skip_specs"`                                           | the "A recurring unit type" bullet: "a definition-only edit the spec does not describe (a model-pin bump, `skip_specs: true`) still ships as a change, with no delta"                                                                        |

## Risks

- A command that mutates a remote branch could be read as approval for more. Mitigated by D4's explicit limits, stated
  in the command's decision point and its lifecycle section.
- The update is requested rather than performed by the local process: the head may not move before the command re-reads
  it. Mitigated by re-reading the head a bounded number of times and reporting an unmoved head as "requested but
  unconfirmed", never as refreshed.
- The local fast-forward can fail on diverged history. Mitigated by reporting it and leaving the local branch untouched;
  no reset and no checkout reaches over local work.
