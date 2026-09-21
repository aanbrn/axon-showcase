# AGENTS.md

## Line Length

- Wrap code and text at 120 characters. Markdown (`docs/`, `AGENTS.md`, `README.md`, `SECURITY.md`, `openspec/specs/`,
  active `openspec/changes/*/`, the `.github/` markdown, and the project-authored `.opencode/` markdown) needs no manual
  wrapping — it is formatter-wrapped and gated in `check`, as is the `.opencode/opencode.json` config; Java/Kotlin are
  gated by Spotless too. See the `Formatting` convention for the per-file-type mechanics and the exact scope.

## Project Overview

**axon-showcase** — a CQRS/Event Sourcing reference app using the Axon Framework. Java 21, Spring Boot 3.5.16, Gradle
9.7.1 (Kotlin DSL), monorepo with 19 modules (18 JVM + `showcase-web-ui`).

This repo uses **spec-driven development**: behavior is captured as OpenSpec specs in `openspec/specs/showcase/`
(organized by architectural role: `gateway`, `write-side`, `read-side`, `clients`, `extensions`, `deployment`,
`quality`). Code changes go through the `opsx-*` OpenCode commands / `openspec-*` skills (propose → apply → archive).
Follow these workflows for new work, and treat the captured specs as the behavioral source of truth. A spec describes a
**capability**, not a module one-to-one: the infrastructure modules (`platform`, `build-logic`, `showcase-test`, and the
`helm` parent module) and the shared library modules (`showcase-command-api`, `showcase-query-api`,
`showcase-query-proto`) carry none — the shared APIs are specified through the service and client capabilities that use
them — and a module's name need not equal its spec path — the `showcase-api-gateway` module is specified as
`gateway/rest-api` plus `gateway/live-events`.

## OpenSpec Workflow Agreement

**Never archive a change automatically after apply.** Stop after implementation, report, and let the user review the
changes made and decide when (or whether) to archive.

**Never push to the remote automatically.** Commit locally when asked, but only `git push` when the user explicitly
requests it (e.g., "push" or "commit and push").

**Create the change's branch at propose — and leave the work uncommitted until a commit is forced or the user has
approved it.** As soon as a change is proposed, put its artifacts on their own branch (named after the change) and
commit nothing: the proposal, the review findings, and the implementation all stay in the working tree, so the user's
review pass runs against the visible `git status`/`git diff`. Exactly two things force a commit on their own — **a
push** and **a branch switch that would carry unfinished changes — tracked or untracked —** onto the other branch
(commit, or `git stash`); an untracked unit survives a reset but rides a `git switch` onto whichever branch you land on,
where a directory-scoped `git add` can stage it into the wrong unit's commit (commit each unit on its own branch before
starting the next) — besides a commit the user explicitly asks for, and no local commit before the user's approval pass.
A push is the moment a change commits: the branch's first push carries whatever is ready then — proposal and
implementation together on the default path, the proposal alone if a proposal-stage draft PR is opened (see the README)
— and the archive adds a commit before the final push. Review findings are therefore edited in the working tree, never
committed as an "Address review findings" commit, and nothing is committed while a review loop runs. Until the first
commit the change dir is untracked, which is safe against the hazards the gotchas name — an untracked change dir
survives `git reset --hard` and `git checkout --` (verified) — `git clean -fd` is the one loss vector, so never run it
on a branch holding unfinished work. The change dir and all subsequent work live on that branch; rejecting a proposal is
a branch delete, never a `main` cleanup. A branch that has been committed (so it can fall behind `main`) is refreshed
from `origin/main` — recreate it when it holds no work, otherwise rebase it — rather than continued on stale; once a PR
is open, the mechanism is `gh pr update-branch` instead (see the BEHIND gotcha). This covers the whole unit, not just
code: a docs refresh or a standalone fix stays uncommitted too, so the quick and manual reviews run against the visible
working-tree diff, and the change's `docs/ideas.md` removal rides the branch like the rest. Committing early and then
adding one "Address quick-review findings" commit per review round produced 11 commits for a single change (squashed
before delivery) — the discipline above removes that failure mode by construction, leaving nothing to squash. captured:
capture-reverted-sweep-lessons (#283)

**Fork branches from `main` only.** Every new branch — a change branch, a standalone fix, or a capture's docs change —
is created from `origin/main` (fetch first), never from another work branch. Branching from a work branch silently
carries its commits into the new PR (a fix PR ended up shipping a change's commit history); recover by rebasing
`--onto origin/main` and force-pushing, then verify the PR's changed-file set is the intended one. Create the branch
with `--no-track` (`git switch -c <name> --no-track origin/main`), or push its first time with
`git push -u origin <branch>`: a plain `git checkout -b <name> origin/main` silently makes `origin/main` the new
branch's upstream (`branch.autoSetupMerge`), so a later bare `git push` refuses with the confusing "The upstream branch
of your current branch does not match the name of your current branch" (fix with `git push -u origin <branch>`, which
repoints it, or `git branch --unset-upstream`). When the local branch is not named after the remote branch it must
update — completing an agent's `opencode/…` PR from a differently-named checkout — push with an explicit refspec
(`git push origin HEAD:<remote-branch>`); the `git push -u origin <branch>` remedy above would open a second branch and
leave the PR without the commit.

**Auto-review the change before asking for a manual review.** After finishing a change's **proposal** (planning
artifacts) and again after finishing its **implementation**, run a quick review of the work (the `review-quick`
subagent) against the change's planning artifacts — for the proposal, the proposal/design/tasks/spec-delta coherence and
repo fit; for the implementation, the tasks and delta spec — and repeat it until it reports no new observations. Fix
everything the quick review finds, re-run it, and stop only when it comes back clean — only then ask the user for a
manual review pass. A clean quick review is a precondition for asking for the manual review, **not** a substitute for it
— it means _ask the user now_, not _the implementation is approved_. Never commit, push, open a PR, archive, or merge on
the strength of a clean `review-quick` alone; the `rework-idea-setup` session reached a merged PR (#152) within minutes,
without ever requesting the manual pass. The commit → push → PR → CI → archive sequence starts only after the user
approves the implementation — the "Run CI before archiving" convention does not authorize committing earlier. An
unanswered approval request is not an approval: a reply that does not address it — the user asks about something else,
or the thread moves on — leaves the request outstanding, so re-ask explicitly before committing, pushing, opening the
PR, archiving, or merging, and do not read a tangential reply as clearance. Work done while awaiting the pass must stay
in the working tree until it is given; as the owner recounted afterwards, a clean quick review asked for the manual
pass, the reply asked about lesson capture instead, and the capture was folded into the same branch and the work
continued — nothing was committed before the repeated request was answered, but the request had been missed. A review
loop that keeps finding the **same class** of observation round after round is not converging — each fix is treating a
symptom of a root cause that is still there, and the next round will find another instance. Stop and re-derive the root
cause, or abandon the unit; do not layer another special case. A revert after a non-converging loop is a legitimate
outcome — record why in the change dir so the decision is not re-litigated. captured: retro-mark-captured-rules

**The review gate is not OpenSpec-specific.** Run the same quick-review-then-manual-review sequence for every unit of
work that will become a PR — a docs refresh, a standalone fix, a dependency bump — not only an OpenSpec change. There is
no proposal/implementation/archive vocabulary for those, so map the rule onto what exists: run `review-quick` over the
diff against the repo (and the change dir when one exists), fix the findings, re-run until clean, then ask the user for
the manual pass — all _before_ committing, pushing, or opening the PR. Docs refreshes shipped as their own PR and
standalone fixes describe _where the work ships_, not a waiver (several docs-refresh PRs were pushed branch → commit →
push → PR with neither review, until the user rejected the tool call and asked "Why again you commit and push without
any quick or manual review?").

**The review gate extends to an outward-facing artifact — most of all an upstream issue or comment.** Anything published
outside the repository is reviewed before it is posted, the same as a diff: draft it, run it past `review-quick`, and
fix the findings before publishing. A false claim written into the repository is correctable in a follow-up commit; a
post to a public tracker is not. A comment drafted for `anomalyco/opencode#48100` was reviewed this way, and the review
caught a wrong premise about permission-pattern expansion before it went public.

**An owner's shorthand that appears to skip or merge a process step is not a waiver — name the conflict and ask for
confirmation before acting on it.** The documented routing stands until the owner explicitly changes it: "let's park and
capture" was read as licence to bundle a newly parked idea and its lesson capture into one docs PR (the docs-refresh
convention routes a newly parked idea to its own PR) — a reading the owner rejected, asking instead that a process
violation be surfaced for confirmation ("if you detect my violation of the process, remind me and ask for
confirmation"). Do not self-authorize an exception and do not write one into a PR body; when an instruction reads as
combining units the workflow separates, state the routing it would break and wait for an explicit answer — as with an
unanswered approval request, an ambiguous instruction is not clearance.

**A review finding is a claim to verify, not an instruction to apply.** The review gate catches errors, but its
corrections are themselves claims: reproduce each against the mechanism or artifact before adopting it, and when a
correction cannot be substantiated, remove the claim rather than assert it either way — a refutation confirmed against
the authoritative policy may be adopted; one that cannot be reproduced is dropped, not written in either direction.

**Report an upstream gap we identify, not only the workaround around it.** A dependency or tool we rely on is worth
improving: when a gap is identified — a spurious update row, a deprecation with no fix, a limitation that forces a
workaround — report it upstream with a reproduction and the evidence, and keep the reference where the constraint bites,
with the close-out clause for when it lands. The workaround is ours to keep; the fix belongs where the defect is, and an
upstream project improves only if its users say what is wrong. Post it through the review gate like any outward-facing
artifact. Resolve the tool's owning repository from its own metadata before searching or filing
(`npm view <pkg> repository.url`, the `homepage` field, or the CLI's docs) — a tool is not necessarily hosted where it
is configured: the `openspec` CLI is `@fission-ai/openspec` on npm, tracked at `Fission-AI/OpenSpec`, not the repo it
configures. Confirm a candidate duplicate by reading its body, not its title — `Fission-AI/OpenSpec#1322` reads like the
config-rules defect but concerns rules keys valid for another schema, a different bug; a matching title is a lead, not a
verdict. Name the close-out's exact retiring mechanism, not the broader ask it is one option under: a clause keyed on a
wider condition (the config-rules issue's own close-out is at the read-path gotcha) can retire a guard on a change that
cannot replace it.

**Interrogate the premise before designing a change — moving, copying, or removing existing state, or proposing a
mechanism the repo may already have.** Establish _why the current state exists_, whether it is deliberate, and whether
the mechanism already exists before designing _how_ to add or change it — a change that relocates configuration already
in place, or re-derives a parked design, can be the best-executed version of the wrong idea. The
`remove-redis-client-label` detour designed a chart-default for the `*-client` pod labels (hardcode them in the
templates) and ran a full propose→apply→verify cycle, including a live `helmInstallToLocal`, before the user's question
("the label name depends on the release name — does this still make sense?") revealed the labels are intentionally
local-target values that belong in `values-local.yaml`. The design weighed implementation alternatives but never
questioned the premise. Verify the current state's rationale against the repo — grep for consumers, read the values and
their comments, check `git log` for the introducing change — and record it in the design's Context; treat "this looks
redundant" as a hypothesis to verify, not a justification to remove. Before proposing a mechanism (an auditor, a check,
a workflow, a scan), grep `docs/ideas.md` for a parked design and the spec corpus and archived changes for an existing
requirement — a parked idea or a spec'd capability is the design to adopt, not re-derive; `add-readme-auditor`'s
planning proposed widening `agents-auditor` past the parked `readme-auditor` idea the repo already had, caught by the
owner rather than a gate. captured: add-readme-auditor (#315)

**An authority rule names the source of truth, not the winning value — resolve a value disagreement from the repo's own
prior reconciliation.** ADR-0002 makes the Java `@ConfigurationProperties` the surface that owns a property's default,
not the value to prefer once the surfaces have drifted; a change that read it as "the Java value always wins" planned to
lower the deployed `showcaseCache` bound to the field's lagging value, until review showed
`align-gateway-cache-defaults` had resolved the identical drift by picking the operationally-intended value and raising
the Java default instead. When reconciling a value stated on several surfaces, search the archives for the prior change
that reconciled the same class of drift and follow its direction — and read the introducing commit's full diff before
treating an edit as incidental (the `git log` check above).

**Capture lessons once, after a change's implementation, and detect at the merge.** Once a change's implementation quick
review is clean, run the `lesson-capture` subagent (giving it the diff, review findings, the change dir when one exists,
and a short note on what went wrong or was learned) to propose AGENTS.md additions — gotchas and conventions worth
recording. A merge runs no capture: at the merge, read what the merge alone affected — its non-diff effects, such as an
agent PR closing its own tracker — and report any candidate lesson it leaves with the bullet it would extend, then ask
the user for explicit confirmation before running one. One capture per unit, never a chain. Apply the proposals the main
agent judges durable, then ship them as a docs PR (per the docs-refresh convention) alongside or after the change.
Process mistakes that leave no diff trace (e.g. a git command that discarded work) are the most valuable thing to
capture — this is what makes the capture systematic instead of memory-dependent. For a docs-only merge that fixes stale
facts or removes duplication, the fix is the lesson — do not re-capture it as a new gotcha; capture only what the merge
left unaddressed. Read that as barring a gotcha that _restates the fix_, not a general rule the fix exemplifies: a
durable rule absent from `main` is one of the things the merge left unaddressed. Before rejecting a captured rule as a
re-capture, check `main`'s own text (`git show origin/main:AGENTS.md`) and reject only a rule that restates the fix
itself. Every proposal names the existing bullet it extends, or states that no bullet covers the lesson — a new rule
merges into or replaces one rather than accreting. Every proposal also names **the decision its rule governs**, and
states whether a future change would plausibly hit it and whether the cost of not knowing it is material — **a proposal
that governs no decision is trivia, not a rule, and is not proposed** (the `disable-axoniq-console-message` sentence
this repo deleted stated a fact governing no decision). When the addition restates a remedy the target bullet already
carries, read the duplication as the target rule's wording being the gap rather than a missing mode: re-read the rule
the incident should have caught by and ask whether one word excludes it — a drafted third `git add <dir>` mode restated
the staged-set inspection that bullet already states, while the real gap was the commit-discipline clause saying
"tracked" where the change dir is deliberately left untracked; the owner's "would following the existing discipline
already have prevented this?" is the test, verified against the text before a bullet is added. Each captured rule also
carries its origin — at the end of the rule it records — in a `captured: <change>` marker — the change at an
implementation capture, the change and its PR when a merge-time detection found the lesson — so a rule's provenance is
readable without git and survives a reflow: grep the `captured:` token, which no reflow splits even when the change name
wraps to the next line. On a bullet the capture merged into rather than authored, the end-of-bullet marker records only
the latest captured contribution, not the bullet's total origin — the pre-existing text stays recoverable from
`git blame` / `git log -S`. Do not skip the subagent or conclude "nothing to capture" on your own judgment — the
subagent is the arbiter, and an initial "nothing to capture" verdict is a hypothesis: a merge that closed a change was
once skipped on exactly such an assumption and the forgotten-archive and premise-interrogation lessons went uncaptured
until the user pushed back twice. When the user asks "is there anything else to capture?", treat it as a prompt to run
the subagent again over the events — not as a request to justify the previous pass. A docs-fix merge has nothing further
to capture only if the subagent actually reviewed it and said so — or if the merge-time detection above found no
candidate. captured: capture-untracked-follows-switch (#284)

**A capture verifies the live state the merge left, not only the diff — and corrects a defect it finds there, not merely
records it.** The merge-time detection is the pass that can read the merge's non-diff effects: an agent PR's closing
reference had already closed the tracker it was triggered from (see the action-flow paragraph below), a defect no gate
reads — and the earlier capture that recorded the stale-target rule had quoted that issue's body without noticing its
closure. When the verification finds shipped work broken, fix the live instance (an out-of-band corrective action, such
as reopening the issue) alongside the recorded rule; documenting the hazard alone leaves the defect live.

**A capture's output is itself a change-sized unit — start it on its own branch, not in `main`'s working tree.**
Applying the subagent's proposals is the propose-like moment for the docs change they become: fork from the just-merged
`main` as soon as you begin, so `main` never carries an in-progress diff and the work is isolated to its own branch. The
capture after the upstream-report PR (#212) was applied directly on `main` and sat there as an uncommitted two-file diff
until a review pass flagged it, and the branch was created only then; the leave-work-uncommitted rule presumes a branch
— an uncommitted capture belongs on its branch, not on `main`.

**Sync the main spec only at archive.** Apply edits to code and the change dir's _delta_ spec — never the main spec
under `openspec/specs/`. The main spec is updated exclusively when the change is archived (delta → main), so the source
of truth never describes behavior the code hasn't yet been verified against. `skip_specs` is only for a change no
capability spec covers — grep `openspec/specs/` for the file, config key, or behavior you are changing before declaring
it. `gate-github-markdown-with-prettier` first declared `skip_specs` for adding `.github/**/*.md` and `SECURITY.md` to
the Spotless target, but `showcase/quality/code-quality` already specified the markdown scope, so it owed (and shipped)
a `MODIFIED` delta like the analogous `bring-opencode-under-spotless`; `openspec validate` cannot flag a missing delta,
so the misclassification would have left the spec silently stale. captured: gate-github-markdown-with-prettier

The grep needs the behavior's name as well as its file: a requirement may name no file at all, and
`.github/workflows/audit.yml` appears in none — yet `merge-governance`'s audit requirement owns that workflow's
report-body behavior, so a prompt edit there owes a `MODIFIED` delta that `openspec validate` cannot flag as missing.
captured: notify-owner-from-the-audit-report (#327)

A change's delta spec path mirrors the main spec's: the delta for `showcase/clients/web-ui` is
`openspec/changes/<change>/specs/showcase/clients/web-ui/spec.md`. Omitting the `showcase/` segment does not collapse to
the same capability — OpenSpec reads `clients/web-ui` and `showcase/clients/web-ui` as two capability ids — so the delta
targets a different spec, and `openspec validate` does not tie it back. `restructure-spec-corpus-findings` first wrote
two deltas at `specs/clients/…` and `specs/gateway/…` and had to move them under `specs/showcase/…`. captured:
restructure-spec-corpus-findings (#332)

**A delta spec cannot rename a main-spec requirement header.** A `MODIFIED` requirement in a change's delta spec is
matched to the main spec by its `### Requirement:` header, so the header must be verbatim-identical to the one it
modifies — only the description/body can change. Retitling a requirement while rewording it (e.g. renaming "Vendored
agent skills are available to agents" while narrowing it) fails `openspec validate --changes` with "Archive would refuse
this delta: MODIFIED failed for header ... not found" and must be reverted. To genuinely retitle a requirement,
delete-and-add it instead of renaming the MODIFIED header: a `## REMOVED Requirements` block naming the old
`### Requirement:` header with a `**Reason**:` (what supersedes it) and a `**Migration**:` (how behavior carries over),
plus a `## ADDED Requirements` block carrying the new header with its full description and every retained scenario
(`widen-agents-auditor-to-tooling` was the first in-repo use of this route). The schema _instructs_ a `Reason` and a
`Migration` on a REMOVED block but does not validate them, and the ADDED block is the only place the old scenarios now
live — `openspec validate` checks scenario preservation for a MODIFIED block, not across a REMOVED/ADDED pair, so carry
the full set by hand.

**Prefer a `MODIFIED` block over an `ADDED` one when the new behavior refines a requirement the spec already holds — add
a new requirement only for behavior no existing requirement covers.** A clause folded into an existing requirement costs
one delta; a new requirement costs a permanent corpus entry, and the corpus accretes the way `AGENTS.md` does
(`make-lesson-capture-consolidate` put its obligation into the scenario that already described the subagent, while a
genuinely new subject like `concise-agent-reports`' shared report contract is still `ADDED`).

**A `MODIFIED` requirement block replaces the whole requirement — the delta must carry every existing scenario the main
spec still has, not just the new ones.** `openspec validate --changes` fails with "MODIFIED ... omits scenario(s) the
current spec still has" when a delta drops an existing scenario (the first `helm-install-builds-webui-image` delta wrote
only the new web UI scenario, omitting "The deployed UI can call the gateway" and "The UI origin is configurable"). The
safe recipe: copy the current spec's full requirement block (description + all scenarios) into the delta, then edit it —
never hand-write a MODIFIED block from memory.

A `MODIFIED` block names one `### Requirement:` header, so folding a second requirement into the first does not retire
that second requirement: the delta also needs a `REMOVED` block for it, or the main spec keeps both the merged
requirement and the orphaned details requirement. `restructure-spec-corpus-findings` folded three `… details`
requirements into their cores as `MODIFIED` + `REMOVED` pairs, each `MODIFIED` carrying the core's scenarios plus the
details' so none was lost. captured: restructure-spec-corpus-findings (#332)

**A spec rename/move (`git mv`) does not update the spec's internal `#` title, and nothing validates the title against
the capability path.** The first line of `openspec/specs/.../spec.md` must be edited separately to match the new path —
`openspec validate` never checks it, so a stale header is silent drift that passes CI. The 2026-08-14 role-group
restructure git-mv'd most spec files but left their `#` headers at the old `showcase/<capability>` paths; #105 later
fixed the eight leftover headers (helm-chart, the three extensions, load-tests, and the command/projection/query
services). Two specs long carried non-path titles that predate the restructure (`# Ide Config Specification` under
`showcase/quality/ide-config/` and `# Infra Image Versions Specification` under
`showcase/quality/infra-image-versions/`) — a different drift, same lesson — until `apply-specs-audit-findings` fixed
them. On any spec move or capability rename, fix the first line in the same change — the title does not follow the file.
A `#` title is the one spec edit made directly _before_ archive, rather than through a delta (the Purpose refresh is the
other non-delta edit, but it waits for the archive commit): it is not a requirement, so no delta block carries it, and
it may be edited directly (as `fix-stale-spec-headers` did as a `skip_specs` change, and `apply-specs-audit-findings`
alongside four real deltas) — an exception to "sync the main spec only at archive". Record it as its own task, so the
mixed route is explicit rather than looking like an un-routed main-spec edit.

**A delta cannot carry a `## Purpose` for an existing capability — refresh the main spec's Purpose in the archive commit
and record it as a task.** `openspec archive` (and the `openspec-sync-specs` workflow) treats the main spec's Purpose as
authoritative and leaves it alone; a delta `## Purpose` only seeds a capability whose spec does not exist yet and is
otherwise ignored. A change that alters a capability's scope — e.g. adding `zstd-jni` to the constrained transitives in
`showcase/quality/dependency-security` — therefore leaves the Purpose stale, and `openspec validate` still passes
because the Purpose is not validated against the change. The repo rule forbids editing the main spec before archive, so
record an explicit task (as `address-new-snyk-findings` did in task 3.2) and apply the Purpose edit in the archive
commit; do not assume the sync workflow covers it. A standalone Purpose refresh is a legitimate `skip_specs` change of
its own — not only a fold-in to the next change that touches the capability — and its implementation commit then carries
**no** spec edit by design, the only real change being the archive commit's: **state the deferral in the report**, since
the diff reads as empty of the change's substance (the owner asked "why do I see no touched files except ideas.md?"),
rather than moving the edit earlier to give it some.

A capability owning a delta is not the trigger for a Purpose refresh — read the Purpose and refresh it only where the
change falsifies its own text. Requirements moving _out_ of a capability can leave its Purpose untouched when the
Purpose never described them: `restructure-spec-corpus-findings` moved the two Helm requirements out of
`merge-governance` and refreshed only `helm-chart`'s Purpose, because `merge-governance`'s — which describes branch
protection and CI gates — is unchanged by requirements it never described. captured: restructure-spec-corpus-findings
(#332)

**`openspec validate` checks a change's delta specs, not its `proposal.md` — proposal-schema defects surface only at
archive, and non-blockingly.** For a change, `openspec validate --all`/`--changes` runs only the delta-spec validator;
the proposal-level schema check is invoked exclusively by `openspec archive` (on its human, validated path — not with
`--json` or `--no-validate`), which prints its findings under "Proposal warnings in proposal.md (non-blocking)" and
never blocks on them. A proposal whose `## Why` section exceeds the schema's 1,000-character cap — or falls under its
50-character floor — therefore validates clean through CI and only warns at archive
(`fix-agent-tooling-audit-findings`'s own proposal tripped the cap this way). Keep the Why section within 50–1,000
characters and move a longer rationale into the design or the What Changes body; do not treat a green
`openspec validate --all` as proof the proposal is well-formed.

**Run CI before archiving; one PR per change.** Push the implementation branch and open a PR with the code and the
active change dir. After the `build` check is green and the user approves, archive the change (move the change dir and
sync the main spec) as an additional commit in the _same_ PR, then merge once. The archive — the declaration that a
change is done — always follows CI, never precedes it. (Docs refresh that reflects a completed change —
`AGENTS.md`/`README.md`/`docs/ideas.md` updates and captured lessons — ships as its own separate docs PR; docs that ARE
the change ship with the change's PR, per the docs-refresh convention.)

**The `opencode` GitHub Action authors a PR from an `/oc` comment — a local agent or human completes it.** Commenting
`/oc …` (or `/opencode …`) on an issue runs OpenCode on a GitHub-hosted runner; it implements the work on its own
`opencode/…` branch, commits, and opens the PR (on an existing PR the same comment commits to that PR instead). The
trigger must be a **timeline comment** or an **inline diff comment**: a `/oc` in a _review body_ (Submit review →
Comment/Request changes/Approve) does nothing, because the action does not support `pull_request_review` (filed upstream
as `anomalyco/opencode#50247` (retires when the action gains that trigger)) — so a reviewer who wants to instruct the
agent leaves the review and then posts the comment. It follows the same rules a local agent does — an OpenSpec change
with a change dir, `skip_specs: true` for a pure dependency bump, and the change left unarchived, since archiving
follows the owner's approval — but it does not archive or merge. An approval comment (`/oc looks good. let's proceed.`)
therefore maps to _its_ implementation: on a **change** PR that is the commit → push → archive sequence, while on a
**report** PR (the `audit` workflow's findings) there is no change to archive and no finding to apply — the report is
the artifact, so the instruction means **merge the report** (it lands under `docs/audits/` as the run's record) and
apply nothing from it. Say so explicitly, rather than leaving the agent to infer a change it cannot find, or to close a
PR whose file the workflow exists to keep. The cloud agent **cannot merge** — the action has no merge step — so on a
report PR it must not attempt one; the merge is the human's. Treat its PR like any other: verify the self-report against
the repository and the run log (a self-report is a claim to verify, like a review finding), then check out the agent's
branch, run `openspec archive <change>`, commit and push it there, and merge once CI is green (the one-PR-per-change
sequence above). GitHub never merges on an approval — an approval alone leaves the PR open. A PR the action opened from
an issue carries a closing reference to its trigger (`Closes #<issue>`), so merging it closes that issue — right for a
one-shot work item, wrong for a long-lived tracker like the update-check issues, whose workflows look them up with
`is:issue is:open` and open a fresh one when none is open, so the merge orphans its history and the next weekly run
opens a duplicate. Strip the closing keyword from an agent PR triggered from a tracker before merging, or reopen the
tracker.

**Merging PRs: the `--admin` flag is for admin users only.** The `main-require-pr-on-merge` ruleset requires an
approving review (`required_approving_review_count: 1`), but the repo owner (`aanbrn`) is a bypass actor on that ruleset
(`bypass_mode: always`). When the active GitHub user **is** the repo owner/admin, merge directly with
`gh pr merge --squash --delete-branch --admin` once CI is green — do not first attempt a plain merge (it will be
rejected by the ruleset) and do not wait on a Copilot review (it never approves). When the active user is **not** an
admin (e.g. a team member), `--admin` is meaningless and the normal review-required flow applies: request a reviewer and
wait for approval before merging.

## Prerequisites

- Java 21+
- Docker & Docker Compose (for local dev / integration tests)
- Gradle wrapper included (use `./gradlew`)
- Helm 4.x + Kubernetes cluster (for deployment)
- Snyk CLI (for `./gradlew dependencySecurityCheck`)
- actionlint (for `./gradlew workflowLint`, part of `check`; see https://github.com/rhysd/actionlint — brew,
  `go install`, or a release binary)
- `pack` CLI (for the web UI `dockerBuildImage` image build; see
  https://buildpacks.io/docs/for-platform-operators/how-to/integrate-ci/pack/, e.g. `brew install buildpacks/tap/pack`
  on macOS)
- Python 3 (for `./scripts/setup-idea.sh`'s IDE-settings merge; macOS ships it via Command Line Tools)

## Build & Test

```bash
# Full build (all modules)
./gradlew build

# Build a single service (also produces Docker image via bootBuildImage)
./gradlew :showcase-command-service:bootBuildImage

# Run tests for a single module
./gradlew :showcase-command-service:test

# Run component tests (faster, no containers)
./gradlew :showcase-command-service:componentTest

# Run integration tests (requires Docker, spins up Testcontainers)
./gradlew :showcase-command-service:integrationTest

# Run end-to-end tests (separate opt-in task; boots services + infra via Testcontainers, builds service images)
./gradlew :showcase-api-gateway:e2eTest

# Run web UI end-to-end tests (separate opt-in task; builds service images + boots infra via docker compose,
# serves the built UI via vite preview, drives it with Playwright, then tears the stack down)
./gradlew :showcase-web-ui:e2eTest

# Check runs: compile → spotless/checkstyle/spotbugs/errorprone → test → componentTest → integrationTest,
# plus workflowLint (actionlint), verifyInfraImageVersions, and verifyModuleDependencies
# (a Docker-free check is -PskipITs -Pcoverage.gate.enabled=false — see the coverage-gate gotcha; e2e is never part of
# check)
./gradlew :showcase-command-service:check

# Load tests (Gatling)
./gradlew :load-tests:test

# Dependency security scan (Snyk; requires the Snyk CLI on PATH, not part of check)
./gradlew dependencySecurityCheck
# The scan passes --policy-path=.snyk (the root Snyk policy). The currently-suppressed findings, their expiry
# rationale, and the rate-limit nuance live in `.snyk` (its header comment and each ignore's `reason`) and the
# /dependency-security-check command — see those rather than restating them here.

# Dependency update report (only catalog-owned coordinates; majors suppressed for groups in
# config/dependency-updates/major-disabled.properties)
./gradlew dependencyUpdates

# Frontend (showcase-web-ui): install, lint, format-check, tests (verification; the production bundle
# is built by `build`/`assemble`, like the JVM modules' bootJar)
./gradlew :showcase-web-ui:check
```

The `/dependency-updates` OpenCode command runs this task and summarizes the available updates; the `/gradle-update`
command updates the Gradle wrapper to the latest stable version when one is available, and the `/opsx-tool-update`
command regenerates the OpenSpec command/skill instruction files after a new `openspec` CLI release.

Build-environment constraints can surface as spurious "current version" rows in the report: build tooling such as
SpotBugs publishes module constraints that `checkBuildEnvironmentConstraints` reads and reports as the current version.
For example, a `log4j-core [2.17.1 -> 2.26.1]` row appears even though `log4j-core` resolves to `2.26.1` everywhere —
`2.17.1` is the floor of an external Log4Shell guard published by `spotbugs-annotations`. These rows are a known
`gradle-versions-plugin` limitation, not real updates (see upstream ben-manes/gradle-versions-plugin#755); do not chase
them (see ADR-0007). When documenting such an external constraint, name the coordinate and the mechanism, not its
version: that coordinate is not catalog-owned and its resolved version is not verifiable from the repository, so a
pinned version rots on the next SpotBugs bump.

Major-blocking entries in `config/dependency-updates/major-disabled.properties` carry a pointer comment naming the
coordinate and its rationale; the authoritative reasoning for each suppressed coordinate lives in the
`showcase/quality/dependency-management` spec.

The Helm update check has its own suppression file, `config/helm-updates/major-disabled.properties`: major bumps of the
bitnami infra charts (postgres, kafka, opensearch) are suppressed there because a major chart ships a new preconfigured
`image.tag` that diverges from the test-surface `*-image-tag` pins (docker-compose/Testcontainers) — a coordinated
migration, not an automatic update. The observability charts (kps, tempo) carry no `*-image-tag`, so their major bumps
surface as actionable; verify them with a live install + smoke test (pods ready, Prometheus targets up, Grafana
datasources wired), since `verifyInfraImageVersions` covers only the bitnami infra charts.

For calendar-versioned coordinates (leading segment is a 4-digit year, e.g. Spring `YYYY.MINOR.MICRO` such as
`reactor-bom 2025.0.7`), the report treats a change in the `YYYY.TRAIN` pair (the first two version segments) as a major
update — matching Spring's release-train definition where `2025.0` and `2025.1` are distinct trains — while a change
only in the service-release (third) segment within the same train is a minor/patch update. Semver coordinates keep the
leading-integer major comparison.

**Test suite order matters:** `test` → `componentTest` → `integrationTest` → `e2eTest`. `check` runs the first three by
default (`-PskipITs` drops integration for Docker-free runs — add `-Pcoverage.gate.enabled=false` to keep the coverage
gate green, see Gotchas); `e2eTest` is a separate opt-in task (two suites — the gateway's and the web UI's; see Test
tiers).

**Test tiers** — a test's tier is decided by its collaborators (what is real vs. faked), not by how long it takes to
run:

- **Unit** (`src/test/java`, suffix `Tests`): the subject under test is isolated — its collaborators are mocks/fakes, no
  Spring context. Verifies single-class logic (e.g. `KsuidIdentifierFactoryTests`).
- **Component** (`src/componentTest/java`, suffix `CT`): the subject is composed with real, in-process collaborators —
  real serializers, Axon `AggregateTestFixture`/`SagaTestFixture`, or a Spring context with WireMock — but external
  infrastructure is never started. Verifies a component behaves correctly against its real neighbors (e.g.
  `QueryMessageRequestMapperCT`, `ShowcaseAggregateCT`, `ShowcaseQueryClientCT`).
- **Integration** (`src/integrationTest/java`, suffix `IT`): real external infrastructure via Testcontainers
  (PostgreSQL, Kafka, OpenSearch). Verifies services against the real things they talk to.
- **End-to-end** (`src/e2eTest/java`, suffix `E2E`): a real deployed system is booted and exercised against all-real
  collaborators, transport-independent — HTTP for the gateway/query-service, the distributed command bus (JGroups) for
  the command-service. The gateway e2e boots the full four-service pipeline and verifies cross-service propagation over
  the full command → Kafka → projection → query pipeline (e.g. `ShowcaseApiGatewayE2E`). The web UI e2e
  (`showcase-web-ui/e2e`, Playwright) boots the same pipeline via docker compose and drives the browser against it:
  create → appears, start → STARTED, saga auto-start reflected over SSE, live events appended to the timeline, and a
  duplicate title surfacing the gateway validation error.

**DB scripts** — before running the command-service standalone (outside Docker), ensure the PostgreSQL event store is
initialized:

```bash
./db.sh init   # creates user `showcase` and database `showcase-events` if absent
./db.sh reset  # drops the database and recreates it
```

## Continuous Integration

`.github/workflows/ci.yml` runs a single `build` job on every pull request and every push to `main`:

- **Pull requests** run the Docker-free fast gate: `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` plus
  `openspec validate --all` and a probe that the OpenSpec config's rule sets are readable by the CLI — the coverage gate
  is disabled because the 0.80 baseline is calibrated on integration-test coverage, which PRs skip by design.
- **Pushes to `main`** run the full gate: `./gradlew check` (with integration tests and the coverage gate) plus the same
  OpenSpec validation and config probe as the pull-request path.
- **A check belongs in the pull-request gate only when the change that trips it can remediate it.** Drift in state no
  pull request causes would fail every unrelated PR, so it belongs in the observational scheduled pattern instead (the
  four update-check workflows, and the out-of-repository surfaces the Docs-refresh bullet names), never `build`.

The `check` task also runs `workflowLint`, which lints the GitHub Actions workflows with actionlint (installed on the
runner via the official download script; see the Prerequisites), and `verifyModuleDependencies`, which enforces the
module dependency graph ADR-0010 records (`check` also runs `build-logic`'s tests, since that is an included build).

The job uses `gradle/actions/setup-gradle` to restore the Gradle User Home (dependencies, wrapper, and local build
cache) across runs — it never caches workspace `build/` directories, since stale `jacoco` exec data would corrupt the
coverage gate. The `.github/workflows/ci.yml` and `.github/workflows/e2e.yml` workflows additionally extend
`gradle-home-cache-includes` with `nodejs` (the node-gradle plugin's Node download in `~/.gradle/nodejs`) and add an
`actions/cache` step for the npm package cache (`~/.npm`, keyed on `showcase-web-ui/package-lock.json`), so the web UI
build does not re-download the Node runtime or the dependency tree on every run. The `main-required-checks` branch
ruleset requires the `build` check for every merge into `main`, with no bypass actors.

**A build-file or dependency change costs a one-time full rebuild (~9 min vs ~1 min warm).** `setup-gradle` partitions
its caches by a hash of the build/dependency configuration (log keys like `gradle-home-v2|Linux-X64|build[<hash>]` and
`gradle-build-cache-v2-<hash>`), and Gradle build-cache entries are keyed on task inputs — so changing
`libs.versions.toml` or a `build.gradle.kts` invalidates the compile/test/static-analysis entries for every module. PR
runs are `cache-read-only: true` (they restore from `main` but never write), so a PR cannot re-warm the cache itself and
waits for the next push-to-main run; a docs or tiny PR that branches off the updated `main` and builds before that
re-warm lands pays the full rebuild once (the docs-only #138 raced #137's 10-minute re-warm and took ~9 min instead of
~1). It self-heals as soon as `main` re-warms — nothing to fix.

`.github/workflows/e2e.yml` runs the heavy end-to-end suites (`:showcase-api-gateway:e2eTest`, which builds all four
service images and boots the full pipeline, and `:showcase-web-ui:e2eTest`, which drives the browser against the same
pipeline with Playwright) on a nightly schedule and via `workflow_dispatch`. It installs the `pack` CLI explicitly
(`buildpacks/github-actions/setup-pack`, pinned to the same version as local development — the GitHub runner image does
not guarantee it), and uses `actions/cache@v6` for the npm cache. It is observational — never a merge gate, no secrets,
and it shares the same `gradle/actions/setup-gradle` caching rules as `.github/workflows/ci.yml`.

`.github/workflows/snyk.yml` runs the credentialed dependency security scan (`./gradlew dependencySecurityCheck`, all
sub-projects with the root `.snyk` policy) on a weekly schedule and via `workflow_dispatch`, authenticated with the
`SNYK_TOKEN` secret. It is observational — never a merge gate.

The four update-check workflows — `.github/workflows/dependency-updates.yml`, `.github/workflows/helm-updates.yml`,
`.github/workflows/buildpack-updates.yml`, and `.github/workflows/tooling-updates.yml` — each run a Gradle report on a
weekly schedule and via `workflow_dispatch`, open or update their tracker issue from that report's file with the
`GITHUB_TOKEN` (`issues: write`), post a comment mentioning the repository owner when there are actionable updates (so
they are notified), and update the issue silently when there are none. They are observational — never a merge gate.

An added or edited workflow among these — or any other `workflow_dispatch`-enabled scheduled workflow — gets its first
real run by dispatch, not by waiting for its schedule: GitHub only exposes `workflow_dispatch` once the file exists on
the default branch, so after it lands on `main` run `gh workflow run <file>` (no CI job exercises it, and `workflowLint`
checks only the YAML) to exercise the workflow end to end — for an update check that is its report path, jq filter and
tracker-issue lookup. captured: bump-snyk-cli-pin

A dispatch verification cannot live as a task in the change dir: `openspec/changes/archive/` is invisible and no gate
reads it, so an unchecked dispatch task is silently lost — both `fix-audit-workflow-report-formatting` and
`notify-owner-from-the-audit-report` deferred their dispatch and archived it unchecked. Run `gh workflow run <file>` as
part of the merge, or park the follow-up in `docs/ideas.md` and name it in the change's report. captured:
notify-owner-from-the-audit-report (#327)

`.github/workflows/audit.yml` runs the three repository audits (agent tooling, spec corpus, architecture) on a weekly
schedule and via `workflow_dispatch`, through the OpenCode GitHub action's scheduled path (a `prompt` input, OIDC auth,
`contents: write` + `pull-requests: write`), and opens a pull request with their findings, mentioning the repository
owner so the report is not left unread — or commits nothing when they report nothing. It is observational — never a
merge gate.

**What each covers:**

- `dependency-updates.yml` — `./gradlew dependencyUpdates`; the actionable sections of
  `build/dependencyUpdates/report.txt` (stable catalog updates + the Gradle wrapper status), in the "Dependency updates"
  issue.
- `helm-updates.yml` — `./gradlew helmUpdates`; the actionable coordinates from `build/helm-updates/report.txt` (the
  Helm CLI and pinned chart versions that have a newer version), in the "Helm updates" issue.
- `buildpack-updates.yml` — `./gradlew buildpackUpdates`; the pinned builder and buildpack coordinates from
  `build/buildpack-updates/report.txt` that have a newer version, in the "Buildpack updates" issue.
- `tooling-updates.yml` — `./gradlew toolingUpdates`; the actionable lines from `build/tooling-updates/report.txt` (the
  tool versions pinned in workflow files — the OpenSpec, Snyk and `pack` CLIs), in the "Tooling updates" issue.

`.github/dependabot.yml` keeps the GitHub Actions versions current (weekly `github-actions` updates), so an action whose
major bump targets a newer Node runtime (e.g. the Node 20 → Node 24 migration) surfaces as a reviewable PR instead of a
silent CI deprecation warning. The `opencode` workflow's `anomalyco/opencode/github@latest` and the Snyk workflow's
`snyk/actions/setup@master` are deliberate floating refs that Dependabot does not manage. The `opencode` workflow's runs
also log a benign `Cache reservation failed: cache write denied` warning — the action's own `actions/cache@v4` step
cannot save on the comment triggers (`issue_comment`, `pull_request_review_comment`), low-trust events GitHub gives
read-only cache access — while the run itself succeeds; do not chase it. Reported upstream as
`anomalyco/opencode#49127`; retires when the action's cache step skips cleanly (or drops to restore-only).

**An update-check issue's named target version can be stale by the time it is actioned — re-resolve the latest before
bumping.** The update-check issues refresh weekly, so the upstream can publish again in between: the "Helm updates"
issue named a chart version the upstream had already superseded by the time the bump was actioned. Confirm the target
with the tool's own lookup (`helm search repo <chart>`, `gh api repos/<org>/<repo>/releases/latest`,
`npm view <pkg> version`) and bump to the resolved latest.

## Architecture

CQRS with four services and a web UI:

- **showcase-command-service** — write side, publishes events to Kafka, uses PostgreSQL event store
- **showcase-projection-service** — consumes Kafka events, writes projections to OpenSearch
- **showcase-query-service** — read side, queries OpenSearch
- **showcase-api-gateway** — REST entry point (`/showcases`), routes to command/query services; also exposes the live
  event stream over SSE (`/events`) and applies CORS for the web UI origin
- **showcase-web-ui** — standalone browser UI (React + Vite, Feature-Sliced Design) that browses and drives showcases
  through the gateway and renders the live event timeline; deployed as its own nginx container image (see Docker Images)
  with the API base URL configured via `SHOWCASE_API_BASE_URL`

Key modules (libraries, not services):

- `showcase-command-api` / `showcase-query-api` — API interfaces; their testFixtures are used by clients and services
- `showcase-command-client` / `showcase-query-client` — reactive clients for remote services
- `showcase-query-proto` — Protobuf definitions for query side
- `showcase-projection-model` — shared query model definitions
- `showcase-test` — shared test utilities
- `showcase-identifier-extension` — KSUID identifier support
- `showcase-mapstruct-extension` — MapStruct extensions
- `showcase-resilience4j-extension` — Resilience4j integration
- `platform` — Java platform BOM for dependency version management
- `build-logic` — Gradle convention plugins
- `helm` — Gradle module for Helm releases; contains sub-module `helm:chart`
- `load-tests` — Gatling-based load tests

## Conventions

- **Lombok**: use Lombok where possible (e.g., `@RequiredArgsConstructor`, `@Data`, `@Builder`, `@Value`) instead of
  writing boilerplate manually; `addNullAnnotations = jspecify`; copyable annotations include `@Qualifier` and `@Value`.
  The command/event/query/DTO value types are Lombok `@Value` classes carrying `@Builder` (`toBuilder = true` on the
  three types whose variants tests derive — `ScheduleShowcaseCommand`, `Showcase`, `ShowcaseEntity`) rather than Java
  records, and the builder is the reason: the aggregate and saga construct events and commands through `builder()`
  (`ShowcaseScheduledEvent.builder()`, `StartShowcaseCommand.builder()`), and tests derive variants via `toBuilder()` —
  a record's canonical constructor cannot express that. Fourteen `@SuppressWarnings("ClassCanBeRecord")` annotations
  encode the choice; delete one only if the type genuinely needs no builder. Whether records would be faster is
  **untested** — the suppression records intent, not a measured result, so do not read it as a performance claim.
- **MapStruct**: default component model is `spring` (`-Amapstruct.defaultComponentModel=spring`)
- **ErrorProne**: NullAway on `showcase.*` packages in production code; disabled in `TestJava` tasks
- **Checkstyle**: style gate wired into `check` via `code-check-conventions.gradle.kts`; ruleset at
  `config/checkstyle/checkstyle.xml`, generated sources excluded via `config/checkstyle/suppressions.xml`
- **SpotBugs**: finds bugs with findsecbugs and fbContrib plugins; uses `spotbugs-include.xml` and
  `spotbugs-exclude.xml` filters in `config/spotbugs/` if present (see `code-check-conventions.gradle.kts`)
- **LZ4 relocation**: root build forces `org.lz4:lz4-java` substitution (see `build.gradle.kts`)
- **Build-tool versions live in the version catalog**: a `build-logic` convention plugin never hard-codes a tool version
  — it declares it in `gradle/libs.versions.toml` and reads it via `val libs = the<LibrariesForLibs>()` /
  `libs.versions.<name>.get()` (runtime `node`/`java`, plugin `toolVersion` `checkstyle`/`spotbugs`/`jacoco`, generator
  artifacts, buildpack ids, image tags). A version that is not a `group:name` dependency (a buildpack id, a
  builder/run-image tag, `node`) is a `[versions]`-only entry with no `[libraries]` module. Catalog ownership is
  single-sourcing, not update tracking: a bare `[versions]` entry is not resolved as a dependency, so
  `dependencyUpdates` ignores it — but `helmUpdates` reads the Helm CLI and chart pins, `buildpackUpdates` the Paketo
  builder and buildpack pins, and `toolingUpdates` the CLI versions pinned in workflow files, so only the entries none
  of them reads must be audited by hand.
- **All JavaCompile tasks** add `-parameters` flag
- **Test display names**: every test class and every `@Test`/`@ParameterizedTest` method (plus `@Nested` groups) carries
  a static-sentence `@DisplayName` (e.g., `@DisplayName("Showcase aggregate component tests")`,
  `@DisplayName("Finishing a showcase with a valid command succeeds")`). Do not use `{0}`-style placeholders — named
  `argumentSet("...", ...)` invocations already render their own detail. A display name that enumerates several cases
  must have the body assert every one of them — an enumerated name with one asserted case is an unverified claim. A
  contract-module test named "may not depend on a client or a service application" asserted only the client case, so the
  rule's second clause was dead and survived repeated review passes; IntelliJ's always-false warning caught it, not a
  test or a review. A test must also assert a decision the production code encodes, not a language operator or a case
  another test already covers: a helper that was only `name in set` (the real suppression policy lives at the call site)
  and a `BuildpackUpdatesTaskTests` case that duplicated a `VersionsTests` case verbatim both passed while verifying
  nothing — a reviewer caught both, and they were deleted rather than kept for the count. When a rule is a one-line
  wrapper of a language construct, test the caller's decision; before adding a case, check no existing test asserts it.
  A test written to guard a refactor must fail against the pre-refactor code — run it before the change (or with the old
  path temporarily restored) and confirm it goes red, since a test green on both sides verifies nothing and is deleted
  rather than kept for the count. The `ShowcaseRestController` cache-fallback refactor shipped under the existing 76
  scenario tests for this reason. captured: resolve-recorded-intent-questions (#334)

The same plugin, `.opencode/plugin/grant-cli-config-dirs.ts`, also grants the **`gh` CLI's config directory**
(`$GH_CONFIG_DIR`, else `$XDG_CONFIG_HOME/gh`, else `~/.config/gh` — resolved the way `gh` resolves it, so
`/home/runner/.config/gh` on a runner and a home path locally both match): the agent invokes `gh`, which consults that
directory, and a missing grant hangs the run the same way (an `/oc` run was found stuck on `/home/runner/.config/gh/*`).

## Docker Images

Each boot service builds a Docker image:

- `aanbrn/axon-showcase-command-service:${project.version}`
- `aanbrn/axon-showcase-api-gateway:${project.version}`
- `aanbrn/axon-showcase-query-service:${project.version}`
- `aanbrn/axon-showcase-projection-service:${project.version}`
- `aanbrn/axon-showcase-web-ui:${project.version}` (static nginx serving the built frontend)

Image names are set in each service's `bootBuildImage` task configuration. To build for a non-default platform (e.g.,
ARM64 host), pass `-PimagePlatform=linux/amd64` (or `--imagePlatform=linux/amd64`), which Gradle maps to the
`bootBuildImage`/`dockerBuildImage` task's `imagePlatform` `@Option`.

The web UI image is built differently: `frontend-conventions` registers a generic `dockerBuildImage` task (typed as
`PackBuildImageTask`) that runs the `pack` CLI with the **version-pinned** Paketo NGINX + Procfile buildpacks
(`paketo-buildpacks/nginx@1.2.0`, `paketo-buildpacks/procfile@5.15.0`; the versions are catalog-owned as `paketo-nginx`
and `paketo-procfile`) over `build/dist` (the `pack` CLI is a build prerequisite like Helm/Snyk). The pins are explicit
because an unversioned buildpack reference becomes ambiguous — `pack` fails with "multiple versions … must specify an
explicit version" — once the builder bundles two versions of a buildpack (the intermittent `e2e`/`helmInstallToLocal`
failure). The builder itself is also pinned (`builder-jammy-base:0.4.642`, catalog-owned as `paketo-builder-jammy-base`)
rather than floating, and the `buildpackUpdates` task / `buildpack-updates` workflow reports newer builder and buildpack
versions — no other update check covers Paketo. The NGINX buildpack is **held back at 1.2.0** because `1.2.1` does not
work on the primary development machine (arm64) — the failure signatures are the ones the host-state and buildpack-pin
gotchas below document, tracked upstream as `paketo-buildpacks/nginx#1340`. The identical build with `1.2.0` succeeds
and serves, and x86 CI builds `1.2.1` fine, so the cause is unsettled: this holds a machine working, it does not report
a repo-wide defect. Close-out: re-test the arm64 build after a container-runtime change (colima, Rosetta or `pack`) or
when the tracked upstream issue is resolved, and re-take the bump if it passes — until then `buildpackUpdates` keeps
naming it. Unlike the builder, the run image (`paketobuildpacks/run-jammy-base:latest`) is deliberately left floating so
base-OS security patches keep flowing — do not "complete" the pin by freezing it. The image serves the bundle via nginx
on `8080` and exposes nginx `stub_status` metrics on `9090` (`BP_NGINX_STUB_STATUS_PORT`); in the Helm deployment, a
gated `nginx-prometheus-exporter` sidecar (`webUi.metricsExporter`, on by default when observability metrics export and
the web UI ServiceMonitor are enabled) converts stub_status to Prometheus `/metrics` on port `9113`, which the Service
`http-metrics` port and ServiceMonitor scrape. A `PackBuildImageTask` convention defaults the image name to
`${project.name}:${project.version}`, which the web UI module overrides with the deployable
`aanbrn/axon-showcase-web-ui:${project.version}` in `showcase-web-ui/build.gradle.kts`. The UI's API base URL is
configured at runtime via the `SHOWCASE_API_BASE_URL` env var — **no baked default** (the browser needs the
externally-visible gateway URL, which only the deployment knows; compose sets `http://localhost:8080`, the Helm chart
uses `webUi.apiBaseUrl` with an empty default) — which a `start.sh` renders into `/workspace/config.js` at container
start (failing fast if the env var is unset/empty) — no ConfigMap or volume mount. The `dockerBuildImage` run prints two
informational warnings from the toolchain, not defects: "Exporting to docker daemon (building without --publish) and
daemon uses containerd storage" (pack exports to the local daemon's containerd store, losing the fast publish path) and
"deprecated usage of stack" (an upstream Paketo buildpack still declares the deprecated `stacks` key instead of
`targets`). Neither is actionable in the build — ignore them.

Similarly, a CNB-built image's timestamps are not host state: Cloud Native Buildpacks stamp buildpack layers with a
fixed past date — they list as `Jan 1 1980` — so builds are reproducible and layer caching stays stable, and this build
passes no `--creation-time`, so the image's creation date reads correspondingly old. Do not read those timestamps, or
the image's age, as evidence about when it was built, which host built it, or whether an image store was re-imported.

## Kubernetes Deployment

```bash
# Deploy to local cluster (must be ordered)
helm install kps prometheus-community/kube-prometheus-stack --version 91.4.0 \
  --namespace monitoring --create-namespace --wait
helm install tempo grafana/tempo --version 1.24.4 --namespace monitoring --create-namespace --wait
helm install axon-showcase-db-events bitnami/postgresql --version 16.7.27 \
  --namespace axon-showcase --create-namespace --wait
helm install axon-showcase-kafka bitnami/kafka --version 31.5.0 \
  --namespace axon-showcase --create-namespace --wait
helm install axon-showcase-os-views bitnami/opensearch --version 2.0.10 \
  --namespace axon-showcase --create-namespace --wait
helm install axon-showcase ./helm/chart --namespace axon-showcase --create-namespace --wait

# Or use Gradle Helm plugin (builds images, then installs all releases to the local target)
./gradlew helmInstallToLocal
```

Per-release install/uninstall tasks follow `helmInstall<Release>To<Target>` / `helmUninstall<Release>From<Target>`, e.g.
`helmInstallKpsToLocal` and `helmUninstallKpsFromLocal` (to install/verify a single chart without building images — the
app-release install task additionally depends on the four `bootBuildImage` tasks and the web UI `dockerBuildImage`).
Uninstalling leaves `createNamespace` namespaces (`monitoring`, `axon-showcase`) behind; remove them with
`kubectl delete namespace` afterwards.

**A chart Deployment addition must extend the app release's `installDependsOn` in `build.gradle.kts`.** The list must
enumerate an image-build task for every image the chart's Deployments reference — `ship-web-ui-as-deployable` added the
web UI Deployment without adding its image build, so `helmInstallToLocal` deployed a web UI pod with an image that was
never built. Note the web UI image is built by `:showcase-web-ui:dockerBuildImage` (a pack-based task, not a
`bootBuildImage`); verify the graph with `./gradlew helmInstallToLocal --dry-run` and confirm every chart Deployment's
image has a build task in it.

**Helm release order**: kps → tempo → db-events/kafka/os-views → axon-showcase, declared by `mustInstallAfter`/
`mustUninstallAfter` in `build.gradle.kts`. Uninstall in reverse.

**Helm release namespaces**: declared in `build.gradle.kts` — the observability releases (kps, tempo) deploy into the
`monitoring` namespace, and the application and infrastructure releases (db-events, kafka, os-views, axon-showcase)
deploy into a dedicated `axon-showcase` namespace (created on install). The local deployment does not depend on the kube
context's current namespace or a `helm.namespace` gradle property.

**Helm release target kube contexts**: each release target declares the kube context it deploys to in
`build.gradle.kts`. The `local` target resolves its context per-machine from the `helm.local.kubeContext` Gradle
property (set in `~/.gradle/gradle.properties` or via `-P`), falling back to the developer's current kube context when
unset — so macOS (colima) and Linux (kind/minikube) contributors each deploy to their own local cluster without a
hard-coded context name in the repo. A remote target (e.g. a future staging) would declare a shared, fixed context in
the build. Do not hard-code a machine-specific local context name (like `colima`) in the versioned build.

**Chart validation**: the chart is linted as part of packaging (`helmPackageMainChart` → `helmLintMainChart`). Lint runs
strict (warnings are errors) and lints the Bitnami `common` subchart, rendering two extra value sets:

```bash
./gradlew :helm:chart:helmLintMainChartFull :helm:chart:helmLintMainChartMinimal
```

Value files live in `helm/chart/src/test/helm/` (`helm/chart/src/test/helm/helm-lint-full.yaml` enables all optional
features, `helm/chart/src/test/helm/helm-lint-minimal.yaml` disables the default-on ones).

Custom values can be placed in `helm/values/<release-name>/values-local.yaml`.

**The `*-client` pod labels in `helm/values/axon-showcase/values-local.yaml` are a bitnami-netpol × local-target
artifact — keep them in the local values, never in the chart.** The labels (`axon-showcase-kafka-client`,
`axon-showcase-db-events-client`, `axon-showcase-os-views-client`) exist only because the local target sets
`allowExternal: false` on the bitnami infra charts (their netpols admit pods carrying the `<release>-client` label), and
their values derive from the local target's release names (declared in `build.gradle.kts`). Moving them into the
reusable app chart would couple it to (a) the bitnami netpol convention, (b) the local release names, and (c) a
netpol-strictness decision the chart cannot observe — a deployment that leaves the bitnami netpols open would carry
labels nothing consumes. A `*-client` label with no matching infra release is dead weight (the removed
`axon-showcase-redis-client` was a leftover from an earlier design — nothing in the stack or code referenced redis).
This was explored as a chart-default change and reverted; the labels belong co-located with the netpol restrictions that
require them.

The local values expose the API gateway and web UI via ingress at the hostnames `axon-showcase-api` and
`axon-showcase-ui` respectively. To reach them by hostname (instead of a `Host:`-header curl workaround), run
`./setup-hosts.sh setup`, which detects the local cluster's ingress-controller LoadBalancer address generically (against
the current kube context, so it works on colima + Traefik, kind/minikube + ingress-nginx, etc.) and manages the
`/etc/hosts` entries (`./setup-hosts.sh remove` to clean up; re-run `setup` if the address changes on cluster restart).

**The live event stream's continuity depends on the ingress read timeout.** The gateway emits an SSE keep-alive at
`apiGateway.events.keepAliveInterval` (default `PT15S`), so a quiet period still carries bytes; keep that interval below
the ingress controller's read timeout. The chart sets no read-timeout annotation, because the annotation name is
controller-specific (Traefik on colima, ingress-nginx on kind/minikube) — a deployment that needs one sets it through
the existing `apiGateway.annotations` value.

## Local Development

```bash
# Infrastructure
docker compose up -d

# Run services individually (each on separate port)
./gradlew :showcase-api-gateway:bootRun        # :8080
./gradlew :showcase-command-service:bootRun     # :8081
./gradlew :showcase-query-service:bootRun       # :8083
./gradlew :showcase-projection-service:bootRun  # :8082

# Web UI (Vite dev server, proxies /showcases and /events to :8080)
./gradlew :showcase-web-ui:viteDev              # :5173
```

Docker Compose (`docker-compose.yml`) starts all infrastructure **and** the Java services (using pre-built Docker images
`aanbrn/axon-showcase-*:${PROJECT_VERSION}`). Build the images first (`./gradlew bootBuildImage` for the JVM services,
`./gradlew :showcase-web-ui:dockerBuildImage` for the UI); `PROJECT_VERSION` resolves the image tags and must equal the
version the images were built with — the Gradle compose tasks set it automatically, a raw `docker compose up -d` needs
it set explicitly. To run services from source instead, use `bootRun` as shown above. The compose stack also runs the
deployed web UI at `http://localhost:8084` (its image is the nginx-built
`aanbrn/axon-showcase-web-ui:${PROJECT_VERSION}`); the Vite dev server (`viteDev`) remains the hot-reload alternative
for UI development.

**Ports:** the HTTP ports (`server.port` in each service's `application.yml`) are the API Gateway `8080`, Command
Service `8081`, Query Service `8083`, Projection Service `8082`. In `docker-compose.yml`, the published `8000`–`8003`
mappings are **JVM debug ports** (`BPL_DEBUG_PORT`), not the services' HTTP ports — only the API Gateway publishes its
HTTP port (`8080`); the other services' HTTP ports are reachable only via the Docker network or `bootRun`. The web UI is
published on `8084` (its container nginx port is `8080`; `stub_status` metrics on `9090`).

The `docker-conventions` plugin adds `compose*` Gradle tasks that wrap Docker Compose and set `PROJECT_VERSION` + image
versions automatically (also `composeBuildAndUp`, `composeBuildAndRestart`): `./gradlew composeUp`,
`./gradlew composeDown` address the whole project, and the four service modules carry the same tasks scoped to their own
compose service — `./gradlew :showcase-api-gateway:composeUp` brings up `api-gateway` with its Compose dependency graph,
and `./gradlew :showcase-api-gateway:composeDown` stops and removes just that container. A compose task runs only when
it is explicitly requested on the command line (standalone `./gradlew composeUp` — a leading `:` from the IDE, e.g.
`:composeUp`, is tolerated) or when a scheduled task needs it as a dependency or finalizer — the web UI `e2eTest` boots
the stack via `composeBuildAndUp` and tears it down via `composeDown`. Broad builds that do not schedule a compose task
never start/stop containers as a side effect. `composeBuildAndUp` starts the stack with `docker compose up -d` (no
`--wait` — a one-shot `kafka-init` container would trip `--wait`'s health check), so the web UI e2e suite polls the
gateway's health endpoint itself to avoid racing gateway startup.

**Infra image versions are single-sourced** in `gradle/libs.versions.toml`: `*-image-tag` coordinates
(`postgres-image-tag`, `kafka-image-tag`, `opensearch-image-tag`) for the official Docker Hub images used by
docker-compose and the Testcontainers IT/e2e suites (`postgres`, `apache/kafka`, `opensearchproject/opensearch`;
postgres omits a trailing `.0`), and pinned `bitnami-*` chart versions (`bitnami-postgresql`, `bitnami-kafka`,
`bitnami-opensearch`) for the Helm deployment. Each chart ships its own preconfigured `image.tag`, which the Helm charts
deploy as-is — no `image.tag` override in build logic or values files. To bump an infra component, update its
`*-image-tag` and/or its `bitnami-*` chart version together. The `verifyInfraImageVersions` task (part of `check`)
derives its checks from the actual `helm.releases` container, resolves each pinned chart's preconfigured `image.tag` via
the Helm CLI (`helm show values bitnami/<chart> --version <pinned>`, using the plugin-managed client; the task adds and
updates the bitnami chart repository itself), fails the build if its app version drifts from the `*-image-tag` after
truncating the chart app version to the official tag's numeric segment count (so `17.6` matches a chart app version
`17.6.0` at minor granularity, while `3.9.0` requires an exact chart app version match), rejects any official tag with
fewer than two numeric segments as a floating reference (e.g. `17`, which Docker Hub re-points to the latest 17.x), and
fails if any infra values file (`helm/values/*/values*.yaml`) pins `image.tag` — so the repo cannot reintroduce a
separate override. The task is build-cacheable on its inputs (the pinned coordinates and values files): since a pinned
chart version's preconfigured `image.tag` is immutable, unchanged inputs restore the verification from the Gradle build
cache and skip the Helm resolution entirely. Deriving the checks from the configured releases means renaming an infra
release retargets its check and removing one drops it. External `image.tag` overrides at deploy time (e.g. `--set` in a
release pipeline) are outside this in-repo gate.

**Every Helm chart coordinate in the version catalog is a concrete version** — never a floating major-line pin such as
`77.x.x`. This covers the observability charts (`prometheus-community-stack`, `grafana-tempo`) and the `common` subchart
dependency as well as the `bitnami-*` infra charts, so the Helm deployment is reproducible at a reviewable version. Bump
a chart by updating its concrete coordinate (e.g. `77.14.0` → `77.15.0`), and verify the bump with a live install +
smoke test (`helmInstall<Release>ToLocal`: pods ready, Prometheus targets up, Grafana datasources wired) —
`verifyInfraImageVersions` gates only the bitnami image-tag drift, not that the bumped chart deploys correctly.

**Kafka 3.9.0 Testcontainers note**: Kafka 3.9.0 has a validation bug (KAFKA-18281) that rejects Testcontainers' default
listener config (`0.0.0.0` binds). The `KafkaContainer` usages in the IT/e2e suites override `KAFKA_LISTENERS` to
`PLAINTEXT://:9092,BROKER://:9093,CONTROLLER://:9094` (empty hosts make the listeners implicit) so 3.9.0 starts; keep
that override when bumping the Kafka image tag.

## Key Environment Variables

- `SHOWCASE_CORS_ALLOWED_ORIGINS` — comma-separated browser origins allowed cross-origin access to the gateway. The
  container image defaults to empty (fail-closed — deployments must allow their UI origin explicitly); docker-compose
  sets it to the local dev + preview origins, and the Helm chart exposes it as `apiGateway.cors.allowedOrigins`.
- `DB_PASSWORD=showcase` — PostgreSQL password for command-service
- `BPL_DEBUG_ENABLED=true` / `BPL_DEBUG_PORT=8000-8003` — JVM debug (JDWP) agent ports (`8000`–`8003` are the published
  debug ports in `docker-compose.yml`; see Local Development)
- `THC_PATH=/actuator/health` — health check path
- `JAVA_OPTS=-XX:MaxDirectMemorySize=128m -XX:MaxGCPauseMillis=20` — container JVM tuning

## Gotchas

- **`git stash pop` can leave conflict markers after a rebase.** The commit-only-at-push workflow leaves the change
  uncommitted and stashes it when a rebase needs a clean tree; if a docs file (e.g. `docs/ideas.md`) advances on `main`
  between the stash and the rebase, popping the stash after the rebase can leave `<<<<<<<` conflict markers in the
  working tree (the stash carries the pre-rebase copy). This surfaced when the ideas-dates fix (PR #64) merged
  mid-rebase. Resolve by reconciling the file — take `main`'s copy and re-apply the change's idea removal — rather than
  resolving the markers by hand; a change branch carries its own `docs/ideas.md` removal (see the docs-refresh
  convention), so it can no longer simply be reset to `origin/main`. Under the commit-only-at-push rule the branch has
  no commits to rebase before its first push, so the stash path arises only after a commit; refresh a pushed branch with
  `gh pr update-branch` rather than a local stash-rebase.
- **`git reset --hard` on a branch with uncommitted work discards tracked-file edits and deletes branch-added files.** A
  change branch holds the work uncommitted (per the workflow); a `git reset --hard origin/main` to "rebase" the branch
  reverts every tracked-file modification (`build.gradle.kts`, workflows, docs) **and deletes the files a commit on that
  branch added** — the change dir included, once it has been committed at a push or a branch-switch checkpoint —
  silently losing the implementation's edits, and the proposal too if it was committed. A still-untracked change dir
  survives (verified): under the commit-only-at-push rule the whole change is untracked until the first commit, so a
  pre-push reset loses only tracked-file edits. This bit the actionlint change when rebasing onto a main that had
  advanced. Never `reset --hard` a branch carrying uncommitted work: with no local commits, `git reset --soft`/`--mixed`
  to `origin/main` keeps the working tree; with local commits, `git rebase` (or stash → rebase → stash pop, per the
  stash-pop gotcha above) is the way. Verify `git status` after to confirm the diff survived. The same class of mistake
  occurs outside a rebase: `git checkout -- <file>` (or `git restore <file>`) reverts just that file to `HEAD`,
  discarding its uncommitted edits — a README change was lost this way to a `git checkout -- README.md` run inside an
  unrelated verification step (recovered only from a backup). While a work branch holds uncommitted edits, inspect
  committed content with `git diff`/`git show HEAD:<file>` rather than `checkout --`/`restore`/`reset`.
- **`openspec archive` relocates the change dir with a filesystem move, not `git mv` — nothing is staged, so stage both
  sides — and the archived tree is outside Spotless, so a post-move `spotlessApply` will not normalize it.** The tool
  moves the directory (`fs.rename`/`mv`), so git reports the old path deleted and the new path untracked:
  `git add -A openspec/changes` stages the move (a commit records the unchanged files as renames, the edit-carrying one
  as delete+add), whereas staging only the archive directory commits the addition while the original stays tracked — a
  **duplicated** change dir. Format the change-dir markdown **before** the archive (`build.gradle.kts` excludes
  `openspec/changes/archive/**` from the markdown target), and confirm the status before committing. A hand-run `git mv`
  is a different trap: it stages the **already-committed** content, so a file carrying an uncommitted edit (typically
  the `tasks.md` tick) shows `RM`, and a plain `git commit` records the pre-edit content.
- **The actionlint download script takes positional arguments (`version dir`), not `--dir`, and the target dir must
  already exist.** When installing actionlint in CI with `bash <(curl .../scripts/download-actionlint.bash)`, pass
  `latest "$RUNNER_TEMP/actionlint"` and `mkdir -p` the dir first — a `--dir` flag is rejected as an invalid version
  (the script exits 1 with its usage).
- **`gh pr list` does not support a `--since` flag, and a `gh <x> list` command silently truncates at its default
  `--limit`.** Filter merged PRs by window with the search qualifier
  `gh pr list --state merged --search "merged:>=<YYYY-MM-DD>"` (an unknown flag like `--since` is rejected outright, and
  there is no date variant of `--merged`). This bit the `/retrospective` gather script
  (`scripts/experience-analysis.sh`), whose first version used `--since`; the proposal/tasks that described
  `--merged --since <window>` were corrected to the search form during implementation. A list command returns only its
  default page with no warning when more exists (`--limit` defaults to 30 for `pr` and `issue`, 20 for `run`), so the
  same script returned 30 rows for a window holding 249 merges until it gained an explicit `--limit 1000`. Any future
  "what shipped since X" automation must use the `--search "merged:>=..."` form **and** an explicit `--limit` sized to
  the corpus. The same truncation is self-inflicted by an explicit slice: `head -1` on the `buildpackUpdates` report hid
  the second of its two lines, and the tasks file cited the first as though it were the whole report. Cite a command's
  full captured output — read the generated report file rather than a piped `head`/`tail` — before recording it as
  evidence. captured: test-build-logic-rules-and-unify-version-comparison (#306)
- **`gh pr create --body` with Markdown can fail under zsh with `no matches found`** — an inline body containing
  `**bold**` (or other shell metacharacters/newlines) is subject to zsh's `nomatch` glob error
  (`zsh: no matches found: **...`). Write the body to a file and use `--body-file <file>` instead; it also sidesteps
  quoting and embedded-newline problems. Prefer `--body-file` for any multi-line PR or issue body.
- **Relative `date` arithmetic is not portable across macOS and Linux**: BSD `date` (macOS) uses `-v-7d`, GNU `date`
  (Linux, incl. CI) rejects it and needs `--date='7 days ago'`. A cross-platform script computing a relative date must
  probe first (`date -v-7d >/dev/null 2>&1 && … || date --date='…'`), as `scripts/experience-analysis.sh` does for its
  7-days-ago default — don't hard-code one platform's form.

- **Deployment investigations must include the Helm chart and values files.** When diagnosing a deployment issue, the
  chart (`helm/chart/src/main/helm/`) and its values (`helm/values/*/values-*.yaml`) are always in scope alongside the
  live cluster — the deployed resources are generated output of the chart, so a wrong live resource usually means a
  wrong source (or a stale apply), not a standalone "cluster problem." The chart also introduces k8s-specific concerns
  (namespaces, NetworkPolicies, KUBE_PING discovery) that do not exist under `bootRun`/`docker-compose`, so a bug can be
  invisible locally and only surface once deployed. Recent deployment bugs were both chart bugs, not code bugs: the
  `kubernetes` EndpointSlice `lookup` namespace in the network policies (`fix-kube-ping-api-egress`) and the missing
  release-namespace declarations (`declare-axon-showcase-namespace`). Prefer rendering the chart locally with
  `helm template` to inspect what the source produces before (or alongside) inspecting live resources.
- **Checking CI status**: don't poll a PR build with an idle `sleep` loop — use `gh run watch <run-id> --exit-status`
  (or `gh pr checks <pr> --watch`), which blocks until the check finishes and exits non-zero on failure. When the run id
  isn't known, fetch it once via the GitHub MCP `pull_request_read` / `get_check_runs` (or `gh run list`), then
  `gh run watch` it — one blocking call, no manual polling. A docs/build change's `build` check typically completes in
  about a minute; check once shortly after pushing, then confirm green before archiving/merging. Idle sleep loops only
  waste time and add no information.
- **A PR whose base advanced after its CI ran fails to merge with "Required status check 'build' is expected" and
  `mergeStateStatus` BEHIND.** When two PRs merge close together (the norm here: a code PR followed by its docs PR), the
  second PR's branch is behind the new `main`; the `main-required-checks` ruleset demands the `build` check on the
  latest base, so the merge is blocked even though the branch's own CI is green. The error message does not say "update
  your branch" — BEHIND is the tell. Fix: `gh pr update-branch` (or `gh pr merge --update-branch`), which re-runs CI
  against the updated base; merge only once that check is green.
- **Exec tasks (`docker`, `pack`, `snyk`) fail in IDEA on macOS**: an IDEA launched from Finder/Dock (or a stale Gradle
  daemon) gives the Gradle daemon a minimal PATH (`/usr/bin:/bin:/usr/sbin:/sbin`) without `/opt/homebrew/bin`, so
  bare-name execs ("command 'docker' not found") fail even though the tools are installed. Root cause: Gradle applies
  the client's environment to the daemon (`System.getenv("PATH")` is then the full shell PATH), but the JVM caches PATH
  for native process spawning at daemon start and ignores later changes — so execs that resolve a bare command name via
  the JVM's cached PATH fail intermittently depending on which client spawned the daemon. The build resolves this by
  resolving the tool to its absolute path from `System.getenv("PATH")` (which is the real shell PATH) and passing that
  in the `commandLine` (`dockerCli()` in `docker-conventions`, `packCli()` in `PackBuildImageTask`, `snykExecutable()`
  in `dependency-security-conventions`), bypassing the JVM's cached PATH entirely. Do not revert to bare command names;
  do not prepend tool dirs to PATH (the daemon JVM won't honor it). Launching IDEA from a terminal still helps avoid
  stale minimal-PATH daemons in the first place.
- **macOS local-network privacy can leave a Warp-spawned shell unable to reach the local cluster.** Reaching a
  local-network address is a per-app privilege macOS tracks by code-signing identity (Apple `TN3179`), and a third-party
  terminal's child processes can be denied it while the internet works: the LAN and colima's vmnet subnet
  (`192.168.64.0/24`, the address the kubeconfig points at) fail with `EHOSTUNREACH`, which reads as a cluster outage,
  and neither a `colima restart` nor a `colima delete` + recreate changes it, because the fault is the host's consent
  layer rather than the VM or k3s. Check the terminal app first: Warp can be _listed_ under System Settings → Privacy &
  Security → Local Network with its toggle **off**, and switching it on restores access immediately
  (`warpdotdev/warp#6320`); quit and reopen the app if its existing children keep the denied state. Fallbacks: run the
  command from **Terminal.app** or under `sudo` (macOS auto-allows command-line tools run from Terminal or over SSH, and
  any program running as root), or point the kubeconfig at colima's loopback forward (`https://127.0.0.1:<port>`, the
  port changing per start), which is not a local-network operation at all. A/B against Terminal.app to confirm the
  class.
- **A containerized image build that fails in varying ways — including on the unmodified baseline — is host state until
  proven otherwise; check the container runtime's amd64 emulation before touching a pin.** A Paketo `dockerBuildImage`
  failure whose signature shifts between runs (a missing `io.buildpacks.buildpackage.metadata` label, an analyzer
  `panic: could not parse '0.12' as version`, `flate: closed writer`, a Go GC fault) and that reproduces on the pre-bump
  baseline points at the host, not the change: colima's template enables Rosetta, a macOS upgrade removed the Rosetta
  runtime, colima silently fell back to QEMU for linux/amd64 containers, and the amd64 buildpack lifecycle crashes under
  QEMU. With Rosetta reinstalled the same pins build — the identical nginx digest that reported "could not find label"
  is the digest the successful build used — but a host explanation exonerates no pin: a surface that fails one run and
  passes the next supports no causal claim in either direction, so name the failures that are host state and leave a
  pin's fault an open question. `hold-back-nginx-buildpack` held `paketo-nginx` back at `1.2.0` because that episode's
  arm64 architecture symptom survived every host reset and is tracked as `paketo-buildpacks/nginx#1340`. A pin decides
  which image `pack` pulls, so a host failure mimics a pin defect exactly: confirm the runtime's configured emulation
  (Rosetta versus its QEMU fallback) and whether the baseline fails too before changing a version.
- **A buildpack pin is verified by running the image, not by a green build or a metadata label — a release can install a
  binary of the wrong architecture and still build cleanly.** `bump-paketo-builder-and-buildpacks` ticked its test plan
  on `dockerBuildImage` succeeding and on reading the built image's `io.buildpacks.build.metadata` label back (which
  named `paketo-buildpacks/nginx 1.2.1`), and the pin it shipped exits 127 on the development machine: the image is
  amd64 but its `nginx` is AArch64, while the build log says `Installing Nginx Server 1.31.5` on both architectures. Run
  the image after a buildpack or builder bump — `docker run`, `GET /`, check the binary's architecture — and be explicit
  about the platform when reasoning about a multi-arch artifact: the buildpack images are multi-arch, so a bare
  `docker pull` on an arm64 host resolves the host's own slice and cannot evidence a claim about the amd64 one (the
  image this pipeline produces is amd64-only, so a bare run of it is not the risk).
- IntelliJ's built-in formatter (its `Default` code style) disagrees with the Spotless format (palantir for Java, ktfmt
  for `.gradle.kts`), so the auto-reformat triggers (**Actions on Save → Reformat code / Optimize imports**, **Auto
  Import → Optimize imports on the fly**) only cause drift if the **palantir-java-format**/**ktfmt** plugins (JVM) or
  the built-in **Prettier** (web module) are not active. The repo's IntelliJ config is **not versioned** — `.idea/` is
  git-ignored entirely. Run `./scripts/setup-idea.sh` any time to reconcile: it **merges** the committed settings
  (`config/idea/*.xml` and the test-tier naming inspection) into `.idea/`, replacing only our components and preserving
  IntelliJ-managed content, so a re-run repairs a configuration that has drifted (e.g. it was never applied cleanly, or
  IntelliJ overwrote it); it then installs the palantir-java-format and ktfmt plugins when IntelliJ is closed. The
  configuration merge needs neither the launcher nor a closed IDE — only the plugin install does. IntelliJ reads the
  merged files at startup (or on **File → Reload All from Disk**), so the settings take effect only after a reload or
  restart. The `/setup-idea` agent command wraps the script (and negotiates quitting a running IDE for the plugin
  install). For the web module it also enables IntelliJ's built-in **Prettier** (via `config/idea/prettier.xml`; free
  from IntelliJ IDEA 2026.1, Ultimate on the earlier unified 2025.3–2026.0) and extends its file-type scope
  (`myFilesPattern`) to CSS and HTML, which the Prettier default omits, so `Reformat Code` matches the
  `prettier --check` gate, with the JS/TS indentation from `.editorconfig`. IDEA's Optimize Imports is gate-neutral for
  the web module (it only removes unused/duplicate imports and reorders them, and no gate enforces import order). The
  ktfmt config uses the plugin's **Custom** style configured to reproduce ktfmt's kotlinlang style at 120 columns with
  unused-import removal, because the plugin's `Kotlinlang` mode hard-codes ktfmt's 100-column default and ignores the
  line-length option (see README → Local Development → IntelliJ IDEA Setup).
- **Verify an IDE formatting integration by actually reformatting — the right action and the full gate scope, neither of
  which config review can see.** Two failure modes: a Prettier scope narrower than the gate (the web module's
  `myFilesPattern` omitted CSS/HTML until extended) makes `Reformat Code` fall back to IDEA's built-in formatter and
  break `prettier --check` — found only when the module was actually reformatted; and a script that called
  `CodeStyleManager.reformat` silently did nothing (it runs IDEA's own formatter, not the external one). So enumerate
  every extension the gate covers and reformat one file of each type before believing the setup; make a file
  deliberately non-conforming, invoke the real `ReformatCode` action in the live IDE (`ActionManager` +
  `AnActionEvent`), then check `prettier --check`/`git diff`. Verify a Gradle formatting task the same way — tamper a
  file, run the task, and confirm the tree returns clean (`--dry-run` only proves the task is scheduled, not that it
  formats). Also check `git status` after any IDE action — Optimize Imports ran project-wide and touched unrelated files
  (noise, not a gate failure) when auditing only the web module.
- **Adding a file type or path to a formatter target can rewrite content a consumer outside the build parses — verify
  equivalence and exercise that consumer.** Bringing the `.opencode/` markdown under Spotless restyled every agent
  definition's frontmatter (`description: <first line>` became `description:` plus an indented block), and Prettier also
  normalized `*emphasis*` to `_emphasis_` and added missing trailing newlines; the consumer is the OpenCode agent
  loader, not Gradle, so a passing `spotlessCheck` said nothing about the files still loading. After widening a
  formatter target, diff the actual normalizations — do not describe the pass as "purely rewrapping" before you have —
  and confirm the rewritten files parse to the same values and the consuming tool still works (for an agent or skill,
  after an OpenCode reload).

- **A check is evidence only once it has been shown to fail — a clean run, an empty result, or an unmoved control proves
  nothing until the check hits a known positive.** Recurring incidents share this root, each with its own mode to guard
  against:

  - **A glob or filter that matches nothing is vacuous, not clean.** An audit command told the agent to run "a manual
    120-character check for `.opencode/*.md`" — a glob matching **no file**, since the markdown lives in
    `.opencode/agent/` and `.opencode/commands/`; it shipped that way because no gate reads a glob written in prose. The
    first fix over-corrected to `.opencode/**/*.md`, which also matches `node_modules/`, the generator-written `opsx-*`
    commands and `openspec-*` skills, and the vendored `axon4to5-*` skills — so "clean" was unachievable. Expand a glob
    once (`ls <glob>`) before trusting it: a vacuous match fails silently and a recursive one over-matches generated or
    vendored files. The same failure comes from a name pattern that truncates: a module-inventory diff written
    `showcase-[a-z-]+` dropped the digit in `showcase-resilience4j-extension`, so the module read as _absent_ and the
    audit nearly opened a change to "restore" an entry that was present all along — a pattern that silently
    under-matches reports a false absence, which is worse than a false positive because it manufactures a defect. A glob
    can also under-match across tools: the build's `**` matches zero or more directory levels, while a plain
    `git ls-files '<dir>/**/*.md'` requires at least one and silently omits a file directly under `<dir>`
    (`.github/PULL_REQUEST_TEMPLATE.md`) — reproduced, 1 file vs 2 — because git treats `**` specially only under
    `:(glob)`. Reproduce a target's file set with `:(glob)` magic or a Gradle-native listing, and prove the scan returns
    a known file from each directory level before trusting its total. captured: reconcile-readme-agents-architecture
  - **A search that under-matches returns a plausible empty or partial result.** A grep for `permissions:` matched one
    job-level block while the six top-level ones went unreported — a real result that looked complete, so nothing flags
    it without a completeness check (unlike a vacuous glob, which fails silently); match the key at any indentation
    (`^[[:space:]]*<key>:`, or strip leading whitespace) and count the hits. The same root hides a wrapped prose phrase:
    a grep for a multi-word phrase that `spotlessApply` split across a line break returns zero though the content is
    present, and that zero is not evidence the edit is absent — three such false negatives in one session led to a tool
    being wrongly accused of reverting an edit (a Purpose refresh read as regenerated by `openspec archive`, when it had
    survived and only a token grep showed so). Grep a token the formatter cannot break (`accretion`, `captured:`) or
    flatten whitespace before matching, and confirm the search can hit a known positive before trusting an empty or
    partial result. captured: make-captured-rules-traceable (#279)
  - **A lookup that yields no readable version reads as current.** `buildpackUpdates` (like `helmUpdates`) maps a failed
    lookup to "no update", so a wrongly-built URL or renamed repository reads as current — and so does a parse pattern
    that cannot match the provider's real body. Verify a new or changed check by temporarily pinning a known-older
    version, confirming the report shows `<name>: <old> -> <latest>`, then reverting the pin (the `buildpackUpdates`
    lookup was proved this way with `0.1.0`) — and unit-test the parse pattern against a **real provider response
    body**, not a hand-written string: the GitHub releases API pretty-prints (its `tag_name` key has a space after the
    colon) while npm compacts, so the no-space pattern never matched and left `HelmUpdatesTask`'s Helm CLI silently
    uncompared (its pin happened to equal the live release, which is why nothing surfaced). A sibling task that predates
    your change is not exempt from that positive control — grep the other update tasks for the same pattern. A
    GitHub-backed lookup also sends `GH_TOKEN`/`GITHUB_TOKEN` when CI provides one (attached to GitHub requests only,
    never to the npm one): an anonymous lookup is rate limited, and the throttled body lands exactly where "current"
    does. captured: unify-tooling-currency-checks (#304)
  - **A control must perturb the surface the check actually reads.** `reconcile-showcase-cache-default`'s control
    perturbs the yml placeholder because `applicationYmlPlaceholdersBindDocumentedDefaults` boots `application.yml` and
    never binds the Java field — reverting the field instead would have proved nothing, since that test passes
    regardless. Match the control's injection point to the test's binding source, confirm the assertion fails (a control
    that runs without failing has not exercised the check), and prove the control's own setup actually perturbed its
    target — assert the anchor occurs exactly once, or diff the surface before and after — before reading its outcome at
    all. A tool that constructs its own input can manufacture the anomaly it appears to detect: the config-rules control
    unquoted a `config.yaml` rule item that was already unquoted _and_ carried no `: ` (select a rule item that carries
    `: ` — the shape whose unquoted parsing breaks; resolve it in `config.yaml`, not from recall), so its edit no-opped
    and the silence was misread as a defect in the guard; a throwaway `python` `replace` left an unterminated quote
    whose whole-file `could not parse … Missing closing 'quote` warning was briefly read as a wording defect — the
    wording was faithful, though it did expose a real gap (a malformed edit yields `could not parse … ignoring it.`,
    which the CI probe's grep did not match, so an unparseable config passed the job (exit 0)), since widened to catch
    both. When a scratch script's result surprises you, print or diff the input it actually produced before drawing a
    conclusion from it — a before/after diff proves the edit _landed_, not that it was the _intended_ one.
  - **A check whose input set depends on which task graph ran is not a check.** `verifyModuleDependencies` inspected 36
    edges standalone and 47 under `check`, the 11-edge difference being exactly the project dependencies declared inside
    `testing { suites { … } }` blocks: a suite's configurations hold their dependencies only once its test tasks are
    realized, which `check`'s own `dependsOn(testing.suites…)` caused incidentally. Four fixes failed (eager suite
    realization, `evaluationDependsOn`, collecting suite edges where the `testing` accessor is in scope, reading every
    configuration) before the scope was corrected: the invariant protects what ships and a suite's edges reach no
    artifact, so the walk reads only the production source sets — `main` and `testFixtures`, declared directly in build
    scripts and thus eager. Have the report enumerate every inspected item, not just its count — the "Inspected edges"
    list is what made the two invocations diffable and named the missing 11 as a set; run a new verification standalone
    and through `check` and diff its report.
  - **A verdict echo is not a check.** While fixing `scripts/experience-analysis.sh`, the 120-character recipe printed
    the offending line and the next command echoed "(script 120 clean)" regardless — the log carried the defect and the
    summary contradicted it. Let the exit status carry the verdict
    (`test -z "$(perl -CSD -lne 'print if length > 120' <file>)"` is non-zero when a line is over the limit) or read the
    output before writing the sentence; never emit a canned "clean" you did not derive from that run. That idiom still
    fails open — `perl` exits 0 on a missing file, so `test -z` reports clean on a typo'd path — which is why the check
    must be seen to hit a known positive before its clean run means anything.
  - **A clean run from the wrong resolution context — a directory, or a point in the lifecycle — is not a
    reproduction.** TypeScript's automatic inclusion of `@types` packages walks up from the tsconfig's directory — or
    the current working directory when no tsconfig is used — not from the directory of the file being checked, so a
    check can pass from inside the package while failing from the root its consumer resolves from (here, the editor's
    project root). A `review-quick` round proposed the simpler fix ("add `@types/node`; a tsconfig isn't needed"), which
    held only in the passing directory. Run a reproduction from the context that fails — the directory whose config the
    failing tool reads, not the artifact's own directory — and treat a different error as its own signal. captured:
    fix-tmpdir-plugin-types (#299) The context is temporal as well as spatial: a plugin hook or a config read runs at
    process init, so a standalone execution proves what the artifact emits but never that its inputs exist by then. The
    `.opencode/plugin/grant-cli-config-dirs.ts` grant was proved with a `bun` run (every grant emitted), yet in the
    cloud run the agent installed `openspec` mid-run, after init, so the resolved-path grant could not exist and the run
    hung. Exercise the artifact at the lifecycle point it runs in (check the workflow's step order against the
    artifact's run point), not only its own invocation. captured: grant-openspec-global-access-in-cloud (#325)

- **Run `spotlessApply` after the _final_ write to a Spotless-owned file — ticking a checklist task is an edit too.** A
  `tasks.md` task was ticked ("`spotlessCheck` passes") _after_ the last `spotlessApply`; the re-wrapped prose broke
  Prettier, so the claimed gate actually failed and only the quick review caught it. After any last edit to a
  Spotless-owned file (a TODO tick, a status note, `AGENTS.md` itself) re-run `spotlessApply` and `spotlessCheck` before
  reporting the gate green — do not trust a check that ran before the final edit.
- **palantir-java-format does not manage imports**: since 2.47.0 the plugin only takes over **Reformat Code**, and
  `Optimize Imports` is always run by IDEA's native optimizer, governed by `.editorconfig` (the import layout
  `ij_java_imports_layout = $*,|,*` and `ij_java_use_single_class_imports=true` with the two on-demand counts at `999`).
  This is what stops IDEA collapsing to wildcard imports; without it, `spotlessApply` cannot auto-expand a wildcard
  (palantir never touches imports), so a wildcard must be expanded by hand or via Optimize Imports. The build gate is
  Spotless `forbidWildcardImports()` (fails on any `import x.*;`). A change to the code-style scheme requires an IDEA
  restart to take effect.
- E2E tests for `showcase-api-gateway` depend on Docker images of all other services being built (`bootBuildImage`). Run
  those first.
- The `io.github.build-extensions-oss.helm` / `io.github.build-extensions-oss.helm-releases` gradle-helm-plugin tasks
  are not configuration-cache compatible — do not enable `org.gradle.configuration-cache=true` (verify with
  `--configuration-cache` before adding it).
- The gradle-helm-plugin 3.1.2 calls the deprecated `Project.getProperties()` (a `--warning-mode all` deprecation that
  becomes a hard error in Gradle 10). Tracked upstream as build-extensions-oss/gradle-helm-plugin#145; bump the plugin
  when a fix is released.

- NullAway is strict on `showcase.*` packages — ensure proper `@Nullable`/`@NonNull` annotations from `jspecify`.
- Jackson 3 artifacts (`tools.jackson.core:*`) are present on the query-service and projection-service runtime
  classpaths transitively via `co.elastic.clients:elasticsearch-java`, constrained by the platform's `jackson3-bom`
  (kept current on minor versions). This is dependency hygiene, not the deferred Jackson 3 backend migration — Jackson 2
  remains the serialization backend in application code (see ADR-0003).
- Spring Data Elasticsearch's `DateFormat.strict_date_optional_time_nanos` maps to a **microsecond** Java pattern
  (`SSSSSS`, not 9 digits) despite its name — see upstream spring-data-elasticsearch#3334. `ShowcaseEntity` uses a
  custom `NANOS_DATE_PATTERN` (`yyyy-MM-dd['T'HH:mm:ss.SSSSSSSSSXXX]`) with `format = {}` instead; do not "simplify" it
  back to the built-in enum until the fix (PR #3337, 6.2.0-M2) reaches us through `spring-data-opensearch`. The
  truncation is invisible on macOS (microsecond clocks) and surfaces only on nanosecond clocks (Linux CI).
- Custom Gradle test suites (`componentTest`, `integrationTest`, `e2eTest`) do not inherit the project's
  `implementation`-only dependencies — each suite re-declares what it needs (client component suites duplicate
  axon/opensearch/wiremock/resilience4j deps, and `showcase-query-proto` must be listed explicitly). A suite can be
  referenced in `shouldRunAfter(...)` only when bound as a `val` (e.g.
  `val integrationTest = suites.register<JvmTestSuite>("integrationTest")`).
- **A `project(...)` dependency a module does not use can still be load-bearing for a consumer — narrowing it can break
  a downstream `compileJava`.** `showcase-query-api` declares `api(project(":showcase-command-api"))` only to reach
  `showcase.identifier.KSUID` (its main source uses no `showcase.command.*` type), so the architecture auditor flagged
  the re-export as an unsanctioned direction — but replacing it with `api(project(":showcase-identifier-extension"))`
  breaks `:showcase-query-client:compileJava`: `ShowcaseQueryClientProperties` imports
  `org.hibernate.validator.constraints.URL`, which it receives transitively through `command-api`'s
  `api(libs.hibernate.validator)`. Before removing or narrowing a `project(...)` dependency, grep the consumers' sources
  for what they actually import and compile the affected modules; an unused direct dependency may be carrying the
  transitive one a consumer's main source needs.
- `@Nested` test classes are incompatible with Spring Boot slice tests (`@WebFluxTest`/`@WebMvcTest`): nested classes
  load the full application context instead of the slice and fail on infrastructure beans (e.g. the gateway's JGroups
  `DistributedCommandBusProperties`). Keep slice-test classes flat (see `ShowcaseRestControllerCT`).
- Testcontainers 2.0.5 moved `PostgreSQLContainer` from `org.testcontainers.containers` (now a deprecated shim) to the
  non-generic `org.testcontainers.postgresql.PostgreSQLContainer` — use the new import without the `<?>`/`<>` type
  arguments.
- **Programmatic edits to a structured file — markdown or source — can silently drop or misplace structure; verify at
  the granularity you edited.** Reordering the README with a Python boundary script dropped the entire "Getting Started"
  section: the move used `lines[ends['Development Workflow']:]`, which starts at the _next_ section and skips the block
  sitting between the two, and only a follow-up grep for `## Getting Started` caught the loss. After any script-driven
  move/rewrite of a markdown file, grep for each expected heading (or diff the heading list before/after) before
  reporting done; for a docs reorder, prefer the edit tool over a hand-rolled reordering script. The same class bites at
  item granularity: the `retro-mark-captured-rules` provenance sweep derived each rule's line range with a boundary
  heuristic that treated a top-level plain `- Text` bullet directly after a bold-lead rule as the rule's continuation.
  Three such adjacencies exist in `AGENTS.md` (`A buildpack pin is verified…`,
  `palantir-java-format does not manage imports`, `A project(...) dependency…`), and the second rule's range ran through
  the eight plain bullets behind it — so both the `git log -L` range an origin was read from and the line the
  `captured:` marker was appended to belonged to the wrong item, and the marker landed on the following bullet. Derive
  an item's end from the list's own structure (a wrapped continuation is indented; a `- ` at column zero starts a new
  item) and have any script that ranges over or appends to items assert that boundary itself — no marker on a plain
  bullet, every marker at the end of the rule it names — before trusting the result. captured: retro-mark-captured-rules
  The same range-boundary hazard bites source: removing three tasks' private comparators with a range running from the
  first deleted member to the end of the file took each class's **closing brace** (the members were last in the body),
  failing three compiles, and orphaned a KDoc above the deleted region — which compiled clean and only a reviewer saw.
  Derive a deletion's end from the member boundaries, and after removing members confirm each surviving KDoc still
  attaches to a declaration: a compile catches the lost brace, never the orphan. captured:
  test-build-logic-rules-and-unify-version-comparison (#306) A formatter-owned file is re-wrapped, so a
  whitespace-tolerant substitution — a normalization or `\s*`-style pattern — silently matches the wrong span or
  rebuilds it unformatted. The edit no-opped or mangled several times across one arc: a workflow step inserted at the
  wrong YAML indentation (a parse error), a README bullet reported as exactly one match that never landed, a `body=`
  line left 198 characters long, and a prompt line replaced at the wrong indentation. Locate the target by line index in
  the content you just read — or match one full line verbatim including its indentation — and re-read the result.
  captured: notify-owner-from-the-audit-report (#327)
- **A write to an already-occupied path replaces the file silently: check the path before creating a "new" file.**
  `ShowcaseEventStreamControllerTests.java` was assumed new, but it dated from #43 and held two tests (event wrapping;
  REMOVED-type preservation), and the whole-file write destroyed both. Before writing a file you believe is new, check
  the path — `test -e <path>` (or a read) proves it exists, `git ls-files <path>` whether it is tracked — and when it
  exists, merge the addition into it rather than re-creating the class from its new-test shape. The loss happens at
  write time, so the `git add <dir>` staging gotcha does not cover it. captured: keep-sse-stream-alive (#301)
- **Keep em-dashes paired when inserting a clause into a sentence that already uses them.** A rework that adds an aside
  to a dashed sentence can leave an odd number of dashes, so the reader cannot tell which dash opens the outer clause
  and which closes it — the trailing clause's pairing is ambiguous. Count the dashes after the edit (an odd count is the
  defect) and prefer parentheses for the inserted aside; Prettier reflows prose but never balances delimiters, so no
  gate catches an unpaired dash — proofread it as content. captured: capture-untracked-follows-switch (#284)

  The same class covers a paragraph boundary: a blank line lost in an edit fuses two paragraphs (an added one splits a
  sentence), and Prettier reflows the result without complaint — the `audit.yml` and `buildpack-updates.yml` paragraphs
  paragraphs were fused in the README's CI section and rode `main` through `spotlessCheck` for several changes
  (`5fc1a09` introduced the fusion) until a drive-by fix separated them — so proofread blank lines as content too.
  captured: notify-owner-from-the-audit-report (#327)

- **Align ASCII/Unicode diagram comments by character width, not byte length.** In the README's project-structure tree,
  `awk`/`length()` counts UTF-8 box-drawing characters (`│`, `├`, `─`) as multiple bytes, so byte columns ≠ visual
  columns and the `#` comments end up misaligned. Measure with a decoded string (`len(line[:idx]) + 1` in Python) and
  align every comment to the longest entry (the tree's target column 42 is set by `showcase-resilience4j-extension/`).
- **A diagram's geometry can encode semantics — do not normalize a deliberate asymmetry as a rendering defect.** The
  README's OpenSpec-flow diagram has two brackets with different right edges on purpose: `human approves` spans
  Propose→Merge, while `delta spec → main spec` ends at Archive (where the delta folds into main, one node before
  Merge). A cleanup pass that aligned the body pipes to the full width flattened that distinction and had to be reverted
  by the user. When a diagram (or any doc) has been hand-edited, treat an asymmetry as intentional until you verify what
  each element is meant to start and end at — ask rather than "fixing" it, and never regenerate over a human edit
  without diffing against it.
- **Verify documented infrastructure/deployment numbers against the config files, not memory.** The README rewrite
  claimed "the API gateway's two replicas" (only `commandService.replicaCount` is 2 in
  `helm/values/axon-showcase/values-local.yaml`; the gateway defaults to 1), "36 panels" (36 is the raw top-level count
  — 5 are empty row separators, 31 are real panels), and "four services and a gateway" (double-counting a table that
  lists four components). The quick review against repo files caught all three. Before writing a replica count,
  panel/section count, or diagram count into a doc, read the source (`helm/values/*/values-*.yaml`, the dashboard JSON,
  the component table) and cite the real number. A two-way split of an enumerated set can also sum to its total and
  still be wrong when its boundary is hand-drawn: a scan of the markdown target's completeness re-listed exclusions from
  memory and counted the vendored `axon4to5-*` and generated `openspec-*` skill files as gated, while the target's own
  `target(...)` / `targetExclude(...)` entries put them on the excluded side (the project-authored skills stay gated) —
  and _both_ splits summed to the same total, so the arithmetic check cannot catch it. For a gated-vs-excluded split, or
  any partition of an enumerated set, derive each side from the config the tool itself reads (`build.gradle.kts`'s
  `target`/`targetExclude`), not a hand-written complement, and never write the resulting totals into a durable artifact
  — they move with every change the corpus gains. captured: gate-github-markdown-with-prettier Name a count's referent
  and check its arithmetic against the total it belongs to: an early draft of the `make-captured-rules-traceable` design
  wrote "46 of them capture-class, and those 65 are concentrated in Gotchas (41) and Conventions (21)" — 41 + 21 = 62,
  so the split could not belong to the 46; it was the 65-bullet set the same sentence also named, and the mismatch was
  invisible until the review did the addition. Distinguish a point-in-time count from a durable one: an exact count of a
  growing corpus (capability specs, requirements, archived changes) belongs in a change's own artifacts, where a stale
  snapshot does no harm, and never in a durable artifact (a subagent definition loaded on every invocation, `AGENTS.md`,
  the README, a command file, a main spec) — where it drifts on every archive and becomes exactly the drift the
  `specs-auditor` exists to catch. The `add-specs-auditor-agent` proposal said the corpus held "157 requirements"; the
  change's own delta made the main spec hold 158, and the README still stated a hard "22 capability specs". Describe the
  shape instead of freezing a tally — the `specs-auditor` definition now says "a corpus … that grows with every archived
  change", and the README's "120+ and counting" is the pattern to follow. A process count — review rounds, elapsed time,
  effort — is not a durable fact either, though for a different reason: no reader can verify it from the repository at
  all, so describe it qualitatively ("repeated review rounds"), not as a precise number. That targets _human_ process
  narrative — effort, review rounds, how long a session ran — which no machine-measured evidence records; a
  _machine-measured_ figure is a different class, since the CI run log is its evidence, so an order-of-magnitude build
  cost (the one-time-full-rebuild vs warm CI timings above) is not a process count and need not be made qualitative. A
  named example or mechanism inside a convention is itself a claim, not decoration: the `@DirtiesContext` rule said keep
  it on "the gateway e2e test, which pulls in JGroups", but the e2e suite drives containers and never boots JGroups in
  the test JVM — a false example that surfaced only when ADR-0009 had to restate the same rule. When a second artifact
  restates an existing fact, diff the two against the code rather than copying the prose. captured:
  make-captured-rules-traceable (#279)

- **A doc-consistency sweep is scoped by the convention, not by the review's findings list — and a claim about the code
  is verified against the code.** The `fix-javadoc-consistency` change introduced a `@param elasticsearchConverter`
  reading "OpenSearch results to entities" for a converter that maps entities → OpenSearch (the field Javadoc and its
  `mapObject(ShowcaseEntity…)` call sites say so); the implementation quick review caught it. For converters/mappers
  either direction reads plausibly, so read the field's Javadoc and one call site before writing the `@param`/`@return`.
  The same change also had to add a field Javadoc the review never enumerated (`ShowcaseProjector.METER_NAME_PREFIX`,
  caught by the user): a consistency sweep must audit every class/method/field in the touched classes, not only the
  review's findings list. The same holds for a spec-corpus fix: an audit's finding list scopes the fix, not the pattern
  the finding is an instance of — `apply-specs-audit-findings` normalized the stray-space coordinates in the
  `spring-data-opensearch` requirement but left the identical `org.springdoc: springdoc-openapi-starter-webflux-ui`
  spelling lower in the same file, deferred as "a future audit" when a grep would have caught it (it took a follow-up
  change, `fix-springdoc-coordinate-spelling`, to sweep it). When a finding is an instance of a convention
  (coordinate/identifier spelling, `MUST` vs `SHALL`, a stale enumeration), grep the artifact or corpus and fix every
  instance in the same change. The same sweep applies to a corrected fact, not only a convention: after a review
  corrects a direction, value, or magnitude, grep the artifact for that claim and fix every repetition — a
  `reconcile-showcase-cache-default` review corrected the reversed direction in the design's Context, and the same
  claim's residue in the Decisions section (plus a "tenfold" that was a hundredfold) was caught only in the next pass. A
  **replaced** rule sweeps one degree harder still: its restatements are paraphrases rather than repetitions and live
  beyond the artifact — in other gotchas and conventions, and in file headers — so grep every doc that describes the
  workflow for the _concept_, not the old wording: replacing the commit discipline left four restatements (two gotchas,
  the docs-refresh convention, and `docs/ideas.md`'s header). An abandoned approach needs the same sweep: reverting an
  experiment's code line does not remove the comment or the task/design prose that describes it, so grep the artifact
  for the approach's name before calling the revert done. The sweep spans live copies only: `openspec/changes/archive/`
  is the historical record, left as recorded — the Spotless target already excludes it — so a corrected command or a
  renamed symbol found there stays as recorded rather than being "fixed".
- **A configuration-default change names every test assertion that pins the value, on each surface it is declared.** The
  `reconcile-showcase-cache-default` plan initially missed that `allPropertiesHaveDocumentedDefaults` asserts the Java
  field (so the change would fail it) and that the yml-wiring test's missing `showcaseCache` assertion was the gap which
  let the drift pass. Grep the test sources for the field accessor when changing a default, and give each pinning
  assertion its own task — the Java-defaults test and the yml-wiring test each bind a different surface. A variable
  carried in a service's `bootBuildImage` `BPE_DEFAULT_*` map has one surface more: the image's launch-environment
  default (`paketo-buildpacks/environment-variables`), which a deployment's env var overrides but which in turn
  overrides the yml fallback.
- **Doc claims must match their source and their strength — quote verbatim or paraphrase explicitly, and reserve
  "enforced" for a real gate.** The self-learning README section described `AGENTS.md` rules in quotes;
  `/review-thorough` caught a reworded rule rendered as a verbatim quote, an "enforced" that no gate backs, and an
  overstated process claim. When a doc quotes a rule/spec/comment, copy the exact text (or drop the quote marks and
  describe it), and check the mechanism before using words like "enforced", "always", "never", or "every" — against the
  variants other docs define, too, since an unconditional rule must hold on every documented path, not only the default
  one (the README's proposal-stage draft PR is such a variant). One of those paths is the rule's own text: state a
  formal criterion — a count, a delimiter balance, a uniqueness test — with its scope where the criterion is stated, not
  only in the rule's opening sentence, so a literal reading cannot apply it to the rule itself (the
  `Keep em-dashes paired…` gotcha's odd-count check is scoped by its first sentence to a sentence that already uses
  paired dashes; read per-sentence, it would flag its own lone-aside sentences). Check the claim's grammatical subject
  too, not only its wording — a pronoun can bind a claim to the wrong actor: a README draft closed with "… and a
  practice of reporting gaps in the libraries and tools _it_ depends on", which bound the project's upstream-report
  practice to OpenCode (the bullet's subject) rather than to the project; naming the subject fixed it. Check a
  qualifier's scope too: a condition or exception introduced for one variant must not swallow the rule's primary mandate
  — strip the qualifiers and confirm the imperative verb still governs the default case (the `reshape-the-capture-loop`
  rewrite of the capture trigger lost its lead's main verb and folded the implementation capture under the merge
  condition; the PR's test plan records the review catching it). Check a recorded _mechanism_ against every measurement
  the change made, not only against other docs: name the evidence points the explanation must cover and confirm the
  wording holds for each, because a generalization one of your own runs contradicts is wrong however well it reads. The
  blocking-execution bullet must cover both of its evidence points — the gateway's not-found test passing (its
  `fetchById` is stubbed, so the handler maps the error directly) and the query-service's failing (`404` → `503`,
  because its error comes off the dispatched query bus) — since a wording covering only one contradicts the other.
  captured: record-blocking-execution-rationale (#335)

- **A comparison between two runs or files that differ in more than one dimension cannot attribute the difference to
  either — isolate the variable before naming a cause.** Comparing `ci.yml`'s `Cache mode: write` with the `opencode`
  workflow's read-only run attributed the difference to the latter's `permissions:` block, but the two differ in two
  ways — the trigger (`push`/`pull_request` vs the comment triggers) and the blocks' contents (`ci.yml`: a top-level
  `contents: read`; `opencode.yml`: a job-level `id-token: write` plus reads) — and a grep anchored to an indented
  `permissions:` had hidden `ci.yml`'s top-level one. GitHub's dependency-caching reference names the trigger, so a fix
  built on the misattribution (granting `actions: write`) would have widened the token for nothing. Read the
  authoritative policy for the mechanism you are hypothesising, or vary only that dimension; a fix that enlarges a
  privilege to explain a behavior is a signal the cause is still undiagnosed.
- **A reproduction in an outward-facing artifact is itself part of the claim — write it so a reader reruns it to the
  same output, and rerun the exact sequence before posting.** State the tool version and the starting state, and record
  the commands in the order they ran: an order-dependent transcript can self-contradict (a `Fission-AI/OpenSpec#1892`
  draft showed `openspec validate --all` exiting 0, but the delta-less change its own `new change` had created makes
  `validate` exit 1), and a precondition left implicit is not reproducible elsewhere (in a directory with no planning
  shape the same malformed config hard-errors with exit 1 instead of warning). Re-check every derived detail — line
  numbers, exit codes — against the actual output, and capture an exit code directly, not through a pipe (`cmd | jq`
  reports `jq`'s status, not the CLI's). The `#1892` draft took repeated review passes, each catching a different
  mis-reproduction, because the review gate reads the artifact while no gate reruns its transcript. This is the drafting
  counterpart to "report it upstream with a reproduction and the evidence"; the tool-behavior claim itself is verified
  per the `--help`/read-path rules.
- **When documenting agent tooling (MCP servers, skills, subagents), read the artifact's own definition — not the config
  entry or the `docs/ideas.md` note that mentions it.** The README "Tooling MCP Servers" section described a
  since-removed `codefmt` skill as running IDE _inspections_ (it runs the formatter), said the Playwright MCP "drives
  the web-UI e2e" (the test framework runs the e2e; the MCP is only the agent's browser), and listed
  `runInspectionsDirectly` as an MCP tool (it is an IntelliJ helper called inside `steroid_execute_code`) — every claim
  sourced from the config entry or the parked idea note rather than the tool itself. A referencing entry summarizes; it
  does not specify. Read `.opencode/skills/*/SKILL.md`, `.opencode/agent/*.md`, and the server's exposed tool list
  before describing what each does, and treat an idea note's prose as a lead, not a spec.
- **A CLI's `--help` is not a capability list — absence of a flag is not evidence the capability is missing.** The
  `setup-agent-tools` design originally asserted `opencode mcp add` was interactive "with no `--command` flag for a
  local server, so it cannot be driven by the agent"; in fact `opencode mcp add <name> -- <command…>` is
  non-interactive, writes the global config, and preserves JSONC comments — the `-- <command>` form is simply not shown
  in `opencode mcp add --help` (which shows only the MCP-server flags `--url`, `--env`, `--header`). A quick review
  caught the false premise. Before designing around a limitation ("this can't be automated"), verify it by trying the
  command or reading its source/docs — do not infer impossibility from a help screen. The same holds for a capability
  the docs describe only _partially_: the permissions docs name `~`/`$HOME` pattern expansion, and an `AGENTS.md` bullet
  concluded `{env:VAR}` was unsupported — it is not, and the scratch-files convention records the substitution plus the
  `{env:TMPDIR}` trailing-separator caveat. That false limitation survived the review gate and was only caught by
  reading the source, because a tool's behavior is not repo-evidenced and no in-repo gate can check it. Treat a doc's
  account of a feature as a floor, not a boundary, and verify a tool-behavior claim against the source/CLI before
  writing it into a durable artifact. Confirm too that the file you read is the code path that runs: a package can hold
  a mock or test harness whose name matches the entry point (`github/index.ts` is a local dev/test entry; the shipped
  handler is `github.handler.ts`), and a matching filename or path is not evidence you read the implementation. The same
  probe-first rule covers a compiler or build-tool semantics claim: a design drafted the premise that a precompiled
  `.gradle.kts` cannot see an `internal` declaration, and a scratch `kotlin-dsl` build disproved it — the
  `internal object` compiles from the script, and only `private` fails — so the shared helper stayed `internal` rather
  than being widened to `public`. Verify a visibility or build-semantics claim with a minimal scratch build before it
  constrains a design. captured: test-build-logic-rules-and-unify-version-comparison (#306)
- **A CLI warning dismissed as noise can report a live defect — a config a tool consumes is unverified until its own
  read path is probed, and a warning no gate reads is not a check.** `openspec/config.yaml` declared per-artifact rules
  for four artifacts, but two items contained an unquoted `: `, so YAML parsed them as mappings, the lists stopped being
  arrays of strings, and the CLI ignored those two artifacts' rules — warning on stderr ("Rules for 'proposal' must be
  an array of strings, ignoring this artifact's rules") wherever the rules are read (`openspec new change`,
  `openspec instructions`), which read as noise for as long as the config existed; `openspec validate --all` emits no
  warning at all, which is why CI missed it. The dropped `proposal` rules included
  `Declare "New Capabilities" / "Modified Capabilities" using existing capability names` — the rule the review loop kept
  catching missing. Quote any YAML scalar containing `: `; the CI `build` job now probes for that warning and the
  whole-file `could not parse` one (a malformed scalar) and fails on either, and `/opsx-tool-update` re-verifies both
  with a positive control. The general rule: a config can look well-formed and pass the tool's own validation while the
  tool silently ignores part of it, and from outside a valid config and an ignored one are indistinguishable — the only
  signal is a warning on stderr that no gate reads. Do not lint the shape with a second parser of your own (that encodes
  an assumption about a contract the tool owns); probe the consumer's own read path, and fail a gate on the tool's own
  warning. The same class covers the change's own `.openspec.yaml`, quieter still: OpenSpec's change-metadata schema is
  not strict, so an unrecognized key is silently stripped with no warning at all — a `skip_design: true` marker (an
  inherited agent habit; a dozen archived changes carry it) does nothing, and `openspec status` still reports `design`
  incomplete and points at `openspec instructions design`. There is no artifact-skip key beyond `skip_specs`: skip
  `design.md` by simply not writing it, never by adding a key. captured: bump-snyk-cli-pin Upstream, the reports are
  `Fission-AI/OpenSpec#1891` (an unquoted `: ` in a rules item) and `Fission-AI/OpenSpec#1892` (an unparseable config);
  if `validate` gains a config check that fails (an ask in each), the CI probe and the `/opsx-tool-update`
  re-verification become redundant and can go.
- **An upstream issue reference is a status claim, not a citation — resolve it, and treat a closure as a trigger to
  check rather than an answer.** A note saying an issue is "tracked upstream" asserts something no gate reads and that
  changes without the repository moving: when the upstream-reference report was parked, review found two of four
  references already closed — `ben-manes/gradle-versions-plugin#755` (2026-08-06, PR #1060) and
  `spring-projects/spring-data-elasticsearch#3334` (2026-08-30, PR #3337) — while both constraint notes still read as
  open. Resolve an `owner/repo#NNN` through the tracker before writing or editing such a reference, and when it has
  closed, read the fixing PR's own diff and the release it shipped in against our pin before treating the note as
  retirable — the issue's premise and its closure are not enough. `#755` closed through a change to platform-sourced
  constraints (`satisfiesDeclaredBound`, "nothing changes by default"), so our spurious row from
  `checkBuildEnvironmentConstraints` remains even though the fix shipped in 0.60.0, so any pin since then already
  contains it. An upstream reference must also document the symptom it is cited for — read the body against the symptom,
  not the title or the release. The first `hold-back-nginx-buildpack` draft blamed an arm64 build-extraction failure on
  `paketo-buildpacks/nginx#1340`, whose body documents only the runtime symptom (an AArch64 `nginx` in an amd64 image)
  and states that the `io.buildpacks.buildpackage.metadata` label is present on both slices; the draft also contradicted
  the host-state gotcha's account of that same signature, and review caught both. When a passage already explains a
  signature, reconcile with it rather than writing a second explanation beside it. This is the write-time check for the
  reference you are touching; the periodic corpus sweep is parked in `docs/ideas.md`.
- **Work a change surfaces is parked durably — a PR body is not a record.** When a docs change records an external state
  change that implies work, park that work in `docs/ideas.md` (or record it as a task) in the same change: a PR body is
  squashed and no tool reads it, so work named only there is lost.
- **A change merged without its archive is incomplete — do not merge the implementation PR and defer the archive.** The
  "one PR per change" rule puts the archive commit in the _same_ PR before merge; a change whose PR merged but whose
  change dir was never archived is easy to forget (the `remove-redis-client-label` change was merged and sat unarchived
  until the user pointed it out). When the merge completes, verify the change dir is archived; if archiving post-merge,
  put it on its own branch and PR — never commit it to local `main` and `git push origin main`, which the branch ruleset
  rejects and forces a branch-then-reset dance.
- **A "the only X" claim in `AGENTS.md` goes stale the moment a change adds a second X — fix it in that change.** The
  web UI's Playwright e2e suite was added while `AGENTS.md` still said "the only e2e suite is `showcase-api-gateway`'s",
  and the stale claim survived until a later cleanup pass (PR #116). When a change adds a second instance of anything
  the docs call unique (a second e2e suite, image, or workflow), grep `AGENTS.md` for `only`/`sole`/`never` claims about
  the first and update them; when editing a section, re-verify such claims against the repo instead of trusting the
  prose. The same applies in the negative: when a change makes a previously-true "nothing covers X" / "A and B ignore X"
  statement false, grep `AGENTS.md`/`README.md` for the capability's absence claims and fix them in the same change —
  including one added by a recent change (the `buildpackUpdates` check in #149 falsified the "`dependencyUpdates` and
  `helmUpdates` ignore bare `[versions]` entries" sentence #148 had added one change earlier). The same goes for a
  **code-symbol rename/removal**: docs cite class, test, and config names as examples and nothing resolves them, so grep
  `AGENTS.md`/`README.md` for the old name in the same change — the first audit found `ShowcaseApiController*`
  references that a rename had left behind. The sweep covers non-doc artifacts too: recording that a component is
  deliberately _not_ used (ADR-0009's "without Axon Server") must grep the whole repo for its name, because config,
  values, and template comments carry claims no auditor reads (`helm/chart/src/main/helm/values.yaml` still called the
  `db-scheduler` settings "Axon Server scheduler settings").
- **A buildpack's CNB id is not its Docker Hub repository — a registry lookup must target the repository, not the id.**
  The buildpacks are passed to `pack` as `paketo-buildpacks/nginx` (hyphen), but their Docker Hub repositories are
  `paketobuildpacks/nginx` (no hyphen); querying the tags API with the CNB id 404s, so `BuildpackUpdatesTask`'s check
  model carries `repository` separately from the display `name`. When adding a buildpack to the check, use its Docker
  Hub repository. The same repositories also publish alias tags (`1.2`, `5.15`) alongside the full semver (`1.2.1`),
  which is why the buildpack check has two version operations rather than one: `Versions.isNewer` — the shared
  comparator all three update checks use — compares numerically with zero padding, so the two spellings of one release
  are equal and neither reports the other as an update, while `Versions.highest`, which only this check needs, keeps a
  longer-spelling tiebreak so the _reported_ tag is the canonical form rather than the alias.
- **A workflow tool pin belongs in the `toolingUpdates` check's declared list — it is the only thing that detects a
  release.** `dependencyUpdates` / `dependency-updates.yml` cover Gradle catalog coordinates, `helmUpdates` /
  `helm-updates.yml` the Helm CLI and pinned charts, `buildpackUpdates` / `buildpack-updates.yml` the Paketo builder and
  buildpacks, `toolingUpdates` / `tooling-updates.yml` the versions pinned in workflow files (the OpenSpec, Snyk and
  `pack` CLIs), and Dependabot covers `uses:` action refs. Add a pin to that check's declared list when you add it to a
  workflow — its patterns are asserted to match exactly once, so a renamed input fails the task rather than reading as
  current — and note that extending the list one pin at a time is how the OpenSpec pin, then the `pack` CLI, was each
  missed in turn while the check did not exist. The `/opsx-tool-update` command regenerates the instruction files after
  a release but does not detect one. (`java-version: '21'` and the opencode workflow's `model` input are deliberate
  pins, not tooling currency — the model pin has its own multi-file bump sweep, see the OpenCode model-pin gotcha.) A
  Snyk or pack bump cannot be verified locally: `workflowLint` (actionlint) proves only that the YAML lints, not that
  the version tag is installable — the credentialed weekly run (or a local `dependencySecurityCheck` with `SNYK_TOKEN`)
  is the first real execution. `workflowLint` also cannot see inside a quoted `gh api --jq` program — actionlint parses
  the YAML and the shell, not the jq — so a malformed copied filter passes `check` and fails only on the scheduled run;
  verify a new or edited update workflow by diffing it against the sibling it copies (a `tooling-updates.yml` jq filter
  was missing a closing parenthesis, caught by that diff and by nothing in `check`). The same skew bites a guard keyed
  off a tool's output: it must be verified against the version CI pins, not only the locally-installed one, since the
  pinned CLI is what the gate actually runs and the output text it matches on may differ there. The same caution applies
  to a proposed _fix_ attributed to a dependency bump: verify it exists in a released version, not only on the project's
  default branch — a bump claimed to make a failure skip cleanly held on `actions/cache`'s `main` but in no release
  (latest `v6.1.0`). captured: unify-tooling-currency-checks (#304)
- **`git add <dir>` / `git add -A` can sweep untracked generated artifacts into the commit — inspect the staged set
  first.** A tool that emits files beside sources (a Python script's `scripts/__pycache__/*.pyc`, a test/build run's
  output) leaves them untracked; a directory-wide `git add` stages them silently, so the commit carries files the change
  never intended. Stage explicit paths, add a `.gitignore` entry for the artifact, and check
  `git diff --cached --name-only` (or `git status`) before committing — the `rework-idea-setup` commit swept in a
  `scripts/__pycache__/*.pyc` that had to be removed and ignored. Staging is a snapshot, not a view of the working tree:
  an edit made after the last `git add` sits unstaged, so `git diff --cached` can look complete while the commit ships
  the older copy — check `git status` for a path listed under both staged and unstaged changes (or `git diff` for
  unstaged edits) before committing, since a review caught the staged set missing corrections made after the last
  `git add`, which would have shipped half the fix.
- **Never chain an edit to a commit without reading the edit's result — gate the commit on a content check, not on the
  edit command's exit status.** While implementing the `concise-agent-reports` change, an anchor assertion in the edit
  script failed (Spotless had re-wrapped the text), so the edit no-opped — and the next command in the shell sequence
  committed anyway, an unfixed state caught only by reading `git show --stat`, not the commit message or the exit codes
  (an assertion that fails does not by itself stop a following command unless the chain is gated). Inspect the staged
  set before committing — the `git add <dir>` gotcha above owns that files check — and read the diff's _content_, not
  just its file list, since a names-only check cannot see an edit that no-opped. An anchor read before a formatter run
  is stale the moment the formatter rewrites the file (`*italic*` → `_italic_`, rewrapping) — take the anchor from a
  read that follows `spotlessApply`, or re-read the file before reusing a saved one. The same change also showed why a
  multi-file change belongs in the apply workflow, not an ad-hoc edit script: hand-editing the six agent definitions
  with `python` `replace` scripts left five of six files edited while `architecture-auditor.md` was untouched and still
  reported edited, and duplicated a line in `lesson-capture.md` — the script's "edited" line is no per-file evidence,
  while `openspec-apply-change`'s per-task edits make a skipped file visible. A script that batches its edits and writes
  once is a third mode: an assert that aborts on a later edit discards every earlier edit in the same script, because
  none reached the single write — a `tasks.md` script applied 11 checkbox ticks and two rewording replacements, hit a
  stale anchor on the last and `sys.exit(1)`'d before its one `write_text`, and all 11 ticks were lost (only a later
  review caught the file still unticked). Write each edit as it succeeds (or split the batch into per-edit scripts) and
  verify the batch's full result afterward, rather than trusting a script that writes once at the end. captured:
  make-captured-rules-traceable (#279)
- **IntelliJ settings-XML component names are exact and easy to transpose — take them verbatim from an IDE-written file,
  not the intuitive name.** `scripts/ensure-idea-settings.py` writes the inspection-profile skeleton with
  `<component name="InspectionProjectProfileManager">`; the script it replaced had it transposed as
  `ProjectInspectionProfileManager`, which raises no error — IntelliJ simply ignores the profile, and only the
  fresh-clone path (no `.idea/inspectionProfiles/Project_Default.xml` yet) exposes it. When adding or editing a settings
  skeleton, copy each `<component>`/inspection name from a real IntelliJ-written file. The same applies at field level:
  a plugin's config field serializes under its `@OptionTag` name (Prettier's `filesPattern` is written as
  `myFilesPattern`), so derive each `<option>`/attribute from an IDE-written file or the bundled plugin jar
  (`…/plugins/<plugin>/lib/*.jar`), not from the field name or the UI label.
- **`./gradlew check -PskipITs` alone fails the coverage gate — the Docker-free check also passes
  `-Pcoverage.gate.enabled=false`.** `-PskipITs` excludes `integrationTest` from the jacoco exec data, so
  `jacocoTestCoverageVerification` verifies against a figure below the 0.80 baseline and fails; the gate is added to
  `check` only when `coverage.gate.enabled` is not `false` (`code-coverage-conventions.gradle.kts`). The PR CI gate is
  exactly `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` (see Continuous Integration) — run that for a local
  Docker-free check, not the bare `-PskipITs` form.
- **A subagent added mid-session is not registered until OpenCode reloads its agent list.** Creating
  `.opencode/agent/<name>.md` (plus its command) does not make the agent reachable in the running session — invoking it
  via the Task tool fails with `Unknown agent type: <name>`; the `add-agents-auditor-agent` smoke-run was blocked until
  OpenCode was restarted. A presence check (file exists, `mode: subagent`, model pin set) passes while the session still
  cannot see it. Restart OpenCode (or start a new session) before smoke-testing a newly added or renamed subagent; the
  same config-read-at-startup rule makes a changed `model` pin apply only to new sessions. Treat that smoke-run as a
  precondition for **archiving**, not a task to defer across the merge: it is the only verification that exercises the
  subagent (CI runs none), and an unchecked task inside `openspec/changes/archive/` is invisible — the archive tree is
  outside Spotless and no gate surfaces it — so the check is silently lost (`add-architecture-auditor-agent` archived
  with its `/audit-architecture` smoke-run unchecked for exactly this reason). Seed the smoke-run with positive controls
  — a known-missing rationale it **must** report and a known-explained surface (a spec requirement, an ADR, an
  `AGENTS.md` convention) it must **not**: a run that reports everything or nothing has not exercised the suppression
  rule. **A smoke-run verifies the subagent's behavior, not the facts of its output — the seed is synthetic, so verify
  both separately:** check the seed's premise against the repository before reading the run (a seeded incident the repo
  does not exhibit leaves the targeted branch unexercised — the subject working, not a failed test — so re-seed and
  record which branch the run exercised), and never promote a proposal resting on the seed's invented details (the
  repo-evidence rule's "a run whose output you have" counts a smoke-run for the subject's behavior, not for a claim
  about the code, so re-verify the proposal's factual basis first — `make-lesson-capture-consolidate`'s smoke-run
  proposed a rule resting on a fabricated incident the repository never evidenced). A run against a real artifact also
  surfaces genuine defects beyond the seed: verify each against the repository and fix those the user approves in the
  introducing change, since the archived run's note is never swept again — the `add-readme-auditor` smoke-run reported
  three real README defects beyond its seed (a missing `-Pcoverage.gate.enabled=false` at two sites, a merge-narrative
  contradiction, and a stray table cell), all fixed in that change. captured: add-readme-auditor (#315) When the owner
  deliberately decouples a follow-up task from the merge, reword the task to record what is deferred, where it went, and
  the owner's chosen order, then **tick it** — an unchecked box inside `archive/` is invisible and no gate reads it.
  That is not the rule above: the smoke-run itself is never deferred, only a task the owner explicitly decouples.
- **Prove a permission rule is applied by reading the OpenCode log, not by the absence of a prompt.** OpenCode records
  every evaluation in `~/.local/share/opencode/log/opencode.log`, in a line carrying
  `message=evaluated permission=<key>`, `action.pattern=<resolved rule>` and `action.action=<action>` (the field order
  varies by entry) — `action.pattern` is the rule that actually matched. A plugin-supplied rule exists in no config
  file, so that line is the proof it _matched a call_ (the `$TMPDIR/opencode` grant was verified this way); a prompt
  that does not appear does not say which pattern allowed the call. For the static half,
  `OPENCODE_CONFIG_CONTENT='<json>' opencode debug config` prints the merged config without a restart — proving
  `{env:VAR}` substitution (`{env:FOO}/**` with `FOO=/x` prints `/x/**`) and showing a plugin-injected rule (under
  `permission.external_directory`, with `plugin_origins` naming the plugin). `debug config` proves a rule reached the
  merged config; the log line proves it matched.
- **`external_directory` and `permission.bash` are separate permission keys.** `external_directory` governs the file
  tools (`read`/`edit`/`write`/`glob`/`grep`) and path-taking commands, while a script's own out-of-tree writes run
  under `permission.bash` — so removing the blanket `/tmp/**` allow from `external_directory` leaves a bash-script write
  such as `setup-hosts.sh`'s unaffected, and a temp-root grant should not be re-added there for it.

- **Caffeine's `AsyncCache.getIfPresent` returns `null` for a future that completed exceptionally — a failed cached
  future is indistinguishable from a cache miss.** A cached `CompletableFuture` that failed is not surfaced:
  `getIfPresent` yields `null`, so the `if (present) … else …` shape takes the miss branch and no callback runs. Two
  consequences for a future-callback style (`future.thenAccept(…)`): the callback only ever executes on success, and the
  returned future ErrorProne's `FutureReturnValueIgnored` flags can never leave a sink hanging here. So a
  `@SuppressWarnings( "FutureReturnValueIgnored")` on such a method is **redundant**, not a documented decision or a
  hidden bug — verified against Caffeine 3.2.4 (`getIfPresent` of an exceptionally-completed future returns `null`).
  Prefer `Mono.fromFuture(…)` over a raw sink-plus-`thenAccept`: Reactor observes the future (no dangling return, no
  suppression needed) and the empty-vs-failed distinction stays with the cache's own contract.
  `ShowcaseRestController`'s fallback paths were refactored this way and the two method-level suppressions deleted.
