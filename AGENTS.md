# AGENTS.md

## Line Length

- Wrap code and text at 120 characters. Markdown needs no manual wrapping — it is formatter-wrapped and gated in
  `check`, as is the `.opencode/opencode.json` config; Java/Kotlin are gated by Spotless too. See the `Formatting`
  convention for the per-file-type mechanics and the exact markdown scope — which paths are gated, and which generated
  or vendored ones are excluded.

## Project Overview

**axon-showcase** — a CQRS/Event Sourcing reference app using the Axon Framework. Java 21, Spring Boot 3.5.16, Gradle
9.8.1 (Kotlin DSL), monorepo with 19 modules (18 JVM + `showcase-web-ui`).

This repo uses **spec-driven development**: behavior is captured as OpenSpec specs in `openspec/specs/showcase/`
(organized by architectural role: `gateway`, `write-side`, `read-side`, `clients`, `extensions`, `deployment`,
`quality`). Code changes go through the `opsx-*` OpenCode commands / `openspec-*` skills (propose → apply → archive).
Follow these workflows for new work, and treat the captured specs as the behavioral source of truth. A spec describes a
**capability**, not a module one-to-one: the infrastructure modules (`platform`, `build-logic`, `showcase-test`, and the
`helm` parent module) and the shared library modules (`showcase-command-api`, `showcase-query-api`,
`showcase-query-proto`) carry none — the shared APIs are specified through the service and client capabilities that use
them — and a module's name need not equal its spec path — the `showcase-api-gateway` module is specified as
`gateway/rest-api`, `gateway/live-events`, and `gateway/client-telemetry`.

## Structured Writing Standard

Every technical artifact the agent authors or edits — this file, an OpenSpec proposal, specification, design, or task
list, an implementation plan, a code review, an agent, command, or skill definition under `.opencode/`, or any other
repository documentation — is written in scannable structure rather than dense prose. The standard governs **how** an
artifact's content is arranged; each artifact still keeps its own required template and sections (an OpenSpec artifact
its schema, an ADR its Status/Context/Decision/Consequences, a gotcha its smallest actionable shape), so it is not one
document outline imposed on all of them. captured: rework-the-review-loop

**The rules:**

- **One independently actionable statement per unit.** Put each requirement, rule, action, condition, or decision in its
  own bullet or numbered step. Do not combine several into one sentence joined by commas, semicolons, or "and".
- **Numbered steps for an ordered procedure; bullets for an unordered set.** Use an ordered list when the order is part
  of the instruction, a bullet list when the items are a set.
- **Explicit conditional branches.** State a decision as its condition and its branch — `IF <condition>, THEN <action>`
  (or a separate decision-table row) — rather than leaving the dependency implied by a compound sentence.
- **Tables for comparisons, mappings, and decision criteria.** When the content is rows of like fields, a table scans
  better than prose or nested bullets.
- **Prose for explanation, not for rules.** Use a paragraph only for continuous explanation (rationale, background,
  narrative); a paragraph MUST NOT carry several independently actionable statements, even when they share a topic.
- **Separate the mandatory from the explanatory.** Keep the normative statement (what MUST/SHALL be done) apart from its
  rationale, background, and examples, so a reader finds the obligation without reading the justification.
- **Labeled fields for complex procedures and diagnostics.** A multi-step procedure or diagnostic guidance uses the
  labeled fields it needs — `Trigger`, `Prerequisites`, `Procedure`, `Decision points`, `Failure handling`,
  `Completion criteria`, `Verification` (the shape the knowledge-authoring standard states) — rather than one
  undifferentiated block.
- **One idea per paragraph.**
- **Preserve precision and normative meaning.** Structure never weakens a claim: keep exact identifiers, paths,
  versions, the strength of a `SHALL`/`MUST`/`SHOULD`/`MAY`, and the necessary context, qualifiers, and caveats. A
  restructure reorders and splits; it never drops or alters meaning.

**Applying it while drafting:**

1. Identify the artifact type and its required structure (its OpenSpec schema, its ADR sections, its report contract).
2. Organize the intended content into that structure's sections and lists before writing prose — the structure is the
   plan, not a reformatting pass afterward.
3. Draft to the shape above:
   - one bullet per requirement, one numbered step per action;
   - a condition stated with its branch, not left implied;
   - observations and hypotheses separated from verified conclusions;
   - rationale beside the rule it explains.
4. Apply the artifact's own schema or template **and** this standard.
5. Where the two require different shapes, the artifact's required sections and semantic distinctions take precedence.

**Before finishing, check the artifact:**

1. Re-read it specifically for a paragraph or bullet that hides several requirements, steps, conditions, or decisions.
2. Restructure those passages — split them, or move them into a list or table — without changing their technical meaning
   or normative strength.
3. Confirm the result still satisfies the artifact's required format.

The `review-quick` and `review-thorough` subagents check an artifact against this standard, and `agents-auditor` reports
repo-authored prose that violates it. This section is the one authoritative definition of the standard; the OpenSpec
configuration and the subagent definitions reference it rather than restating it.

## OpenSpec Workflow Agreement

These rules govern every unit of work that will become a pull request: an OpenSpec change, a docs refresh, a standalone
fix, or a dependency bump. Where a rule says "change", read "unit of work".

### Invariants

- **Never archive a change automatically.** After implementation, stop, report, and let the user review the changes and
  decide when — or whether — to archive.

- **Never push to the remote automatically.** Commit locally when asked; run `git push` only when the user explicitly
  requests it (for example "push" or "commit and push").

- **Keep the planning stage read-only — its delegations too.** A delegation made while a change is explored or planned
  (before implementation) may write only the change's own artifacts under `openspec/changes/<change>/`; its prompt names
  the paths it may touch, and the calling agent verifies the delegate stayed inside them before using its output.

- **Fork every new branch from `origin/main` (fetch first), never from another work branch.** This applies to a change
  branch, a standalone fix, and a docs PR alike. Branching from a work branch silently carries its commits into the new
  PR (a fix PR ended up shipping a change's commit history); recover by rebasing `--onto origin/main` and force-pushing,
  then verify the PR's changed-file set is the intended one. Create the branch with `--no-track`
  (`git switch -c <name> --no-track origin/main`; `git worktree add -b <name> <path> origin/main` takes the same flag),
  or push its first time with `git push -u origin <branch>`. A plain `git checkout -b <name> origin/main` silently makes
  `origin/main` the new branch's upstream (`branch.autoSetupMerge`), so a later bare `git push` refuses with "The
  upstream branch of your current branch does not match the name of your current branch" — fix it with
  `git push -u origin <branch>`, which repoints it, or `git branch --unset-upstream`. When the local branch is not named
  after the remote branch it must update — completing an agent's `opencode/…` PR from a differently-named checkout —
  push with an explicit refspec (`git push origin HEAD:<remote-branch>`); the `git push -u origin <branch>` remedy would
  open a second branch and leave the PR without the commit. captured: park-git-worktree-idea

- **Sync the main spec only at archive.** Edit the code and the change dir's _delta_ spec; never edit the main spec
  under `openspec/specs/`. The main spec is updated exclusively when the change is archived (delta → main), so the
  source of truth never describes behavior the code has not yet been verified against. The generated `/opsx-sync`
  command (and its `openspec-sync-specs` skill) is archive-internal: use it only inline within `openspec archive`, never
  standalone, since a standalone run writes the main spec ahead of verified behavior and `openspec validate` cannot flag
  it. `skip_specs` is only for a change no capability spec covers — grep `openspec/specs/` for the file, config key, or
  behavior you are changing before declaring it (see "Workflow: Author a spec delta and archive").
  `gate-github-markdown-with-prettier` first declared `skip_specs` for adding `.github/**/*.md` and `SECURITY.md` to the
  Spotless target, but `showcase/quality/code-quality` already specified the markdown scope, so it owed (and shipped) a
  `MODIFIED` delta like the analogous `bring-opencode-under-spotless`; `openspec validate` cannot flag a missing delta,
  so the misclassification would have left the spec silently stale. captured: gate-github-markdown-with-prettier

- **The review gate covers every unit of work and every outward-facing artifact.** Run the same
  quick-review-then-manual-review sequence for a docs refresh, a standalone fix, or a dependency bump as for an OpenSpec
  change. There is no proposal/implementation/archive vocabulary for those, so map the rule onto what exists: run
  `review-quick` over the diff against the repo (and the change dir when one exists), fix the findings, re-run until
  clean, route the deep review, then ask the user for the manual pass — all _before_ committing, pushing, or opening the
  PR. Docs refreshes shipped as their own PR and standalone fixes describe _where the work ships_, not a waiver (several
  docs-refresh PRs were pushed branch → commit → push → PR with neither review, until the user rejected the tool call
  and asked "Why again you commit and push without any quick or manual review?"). Anything published outside the
  repository — most of all an upstream issue or comment — is reviewed the same as a diff: draft it, run it past
  `review-quick`, and fix the findings before publishing. A false claim written into the repository is correctable in a
  follow-up commit; a post to a public tracker is not. A comment drafted for `anomalyco/opencode#48100` was reviewed
  this way, and the review caught a wrong premise about permission-pattern expansion before it went public.

- **An owner's shorthand that appears to skip or merge a process step is not a waiver — name the conflict and ask for
  confirmation before acting on it.** The documented routing stands until the owner explicitly changes it: the shorthand
  "let's park and capture" was once read as clearance to bundle a parked idea and its capture into one PR without
  asking, and the owner rejected the unilateral reading — the correction is to surface the suspected conflict and wait,
  not to decide it ("if you detect my violation of the process, remind me and ask for confirmation"). Do not
  self-authorize an exception and do not write one into a PR body; when an instruction reads as merging units the
  routing keeps apart — bundling a docs edit the change did _not_ cause, or skipping a gate — state the routing it would
  break and wait for an explicit answer. As with an unanswered approval request, an ambiguous instruction is not
  clearance.

- **Treat a review finding as a claim to verify, not an instruction to apply.** Reproduce each correction against the
  mechanism or artifact before adopting it; when a correction cannot be substantiated, remove the claim rather than
  assert it either way — a refutation confirmed against the authoritative policy may be adopted, one that cannot be
  reproduced is dropped, not written in either direction. A correction that **deletes** an existing statement has no
  downstream reader to catch it, so reproduce it at the source the statement names before deleting — a POM's
  `<exclusion>` searched as Gradle's `<exclude>` returned 0 hits, read as "no exclusions", and removed a true one.
  captured: record-remaining-architecture-advisories

- **Report an upstream gap we identify, not only the workaround around it.** When a gap is identified — a spurious
  update row, a deprecation with no fix, a limitation that forces a workaround — report it upstream with a reproduction
  and the evidence, and keep the reference where the constraint bites and in a file the closure report scans
  (`upstreamReferences` reads `AGENTS.md`, `README.md`, `docs/ideas.md`, and `docs/adr/**`), so its `retires when` can
  fire (see the upstream-reference bullet), with the close-out clause for when it lands. The workaround is ours to keep;
  the fix belongs where the defect is, and an upstream project improves only if its users say what is wrong. Post it
  through the review gate like any outward-facing artifact. Resolve the tool's owning repository from its own metadata
  before searching or filing (`npm view <pkg> repository.url`, the `homepage` field, or the CLI's docs) — a tool is not
  necessarily hosted where it is configured: the `openspec` CLI is `@fission-ai/openspec` on npm, tracked at
  `Fission-AI/OpenSpec`, not the repo it configures. When it is an **error string** you are triaging, the owner is the
  project that emits it, not the project whose issue text matches it: follow the string to its source (the package or
  file the message names — `failed to fetch base layers` is `imgutil`'s `local/local.go`, and its issue
  `buildpacks/pack#2527` carries the exact line) and file there, naming a downstream consumer's matching issue as the
  thread rather than filing on it — a comment posted to `spring-projects/spring-boot#49251` first was noise on a
  consumer's issue and had to be deleted. Confirm a candidate duplicate by reading its body, not its title —
  `Fission-AI/OpenSpec#1322` reads like the config-rules defect but concerns rules keys valid for another schema, a
  different bug; a matching title is a lead, not a verdict. Name the close-out's exact retiring mechanism, not the
  broader ask it is one option under: a clause keyed on a wider condition (the config-rules issue's own close-out is at
  the read-path gotcha) can retire a guard on a change that cannot replace it. captured:
  pin-the-smoke-docker-to-overlay2

- **Interrogate the premise before designing a change — moving, copying, or removing existing state, or proposing a
  mechanism the repo may already have.** Before designing _how_, establish _why the current state exists_, whether it is
  deliberate, and whether the mechanism already exists. Grep `docs/ideas.md` for a parked design, the spec corpus and
  archived changes for an existing requirement **or a recorded decision about it — including a `skip_specs` decision not
  to spec it and a recent change's Non-Goal**, and `docs/adr/` for an Accepted decision the design would implement or
  contradict — an ADR's Decision and a spec requirement are both normative, so a relaxation past either is a premise
  error, not a design choice: a requirement the design needs relaxed is amended in the delta and the design names the
  amendment, never re-read as permitting what its text does not state (an ADR is superseded, not edited) — and the
  artifact you are editing at `HEAD`, `AGENTS.md` included, since a sibling change merged earlier the same session may
  already have added it. A parked idea, a spec'd capability, or an Accepted ADR is the design to adopt, not re-derive; a
  false absence claim runs a whole propose cycle on the wrong premise and duplicates what exists (`add-readme-auditor`'s
  planning proposed widening `agents-auditor` past the parked `readme-auditor` idea the repo already had, caught by the
  owner rather than a gate; an `enforce-web-ui-import-boundaries` design proposed two relaxations of the one-way import
  rule ADR-0013's Accepted Decision already forbade, caught by a reviewer). Verify the current state's rationale against
  the repo — grep for consumers, read the values and their comments, check `git log` for the introducing change — and
  record it in the design's Context; treat "this looks redundant" as a hypothesis to verify, not a justification to
  remove, and a falsified example narrows a rule rather than deleting its remedy (the rule governs a class, the example
  is evidence — `fix-timeout-load-flakes` restored a load-timing remedy #584 had deleted when the controller slice's 30s
  raise failed). A third-party capability claim is established from the dependency's own sources (its documented DSL,
  its jar, the task's resolved configuration read back by a scratch init script), never from one interface cargo-culted
  into a search — grepping the Helm plugin's `HelmInstallationOptions` missed the `remoteTimeout` it inherits from
  `ConfigurableHelmServerOperationOptions`, and a whole change was designed around "the plugin exposes no install
  timeout" until its docs showed `helm.remoteTimeout` → `--timeout`, while the blanket `extraArgs` that _was_ read is
  the worse tool (it reaches every command, `helm repo search` included) — and a setting a build script does add is read
  back from the task that resolves it, not asserted from the declaring script. A shared helper's **precedence** is part
  of that claim too: read its own template, since a non-empty global can override every per-component value (defaulting
  the Bitnami `common.images.image` global would have misrouted the Docker-Hub metrics-exporter sidecar). A claim about
  a dependency's mechanism — what a coordinate pulls, what a jar contains, what a POM excludes — is derived from the
  resolved graph or the artifact's own contents, never hand-enumerated from memory: a remembered inventory standing in
  for an absence claim is wrong on the members beside the ones you listed. A cost, effort, or performance claim is a
  hypothesis of the same kind and has no source in the repo but a deliberate measurement, so probe it before it drives a
  proceed-or-skip decision: the parked `git worktree` idea carried an assumed build cost behind a conclusion that
  worktrees were not worth it for a docs-only or single-file fix, and a throwaway probe (a detached worktree from
  `main`, shared caches warm) measured the first build at a one-time ~1 minute (its Docker-free `check` ran 78s) and
  ~628M per tree, partly falsifying the premise. A cost the repository cannot yet measure is a model, not a measurement:
  state each operand as measured or assumed, derive only what the measured ones determine, and leave the aggregate
  unestablished rather than computing it from a proxy (a review-round tally for fresh sessions). Before treating a spec
  change as owed, read the spec's **existing** wording for the behavior the code implements — the contract may already
  state it, in which case the fix is code→spec alignment, not a spec rewrite (`#455`'s `scripts/doctor.sh` classified
  the repo-state checks as `required` while `showcase/quality/toolchain-check` already carried the exit contract
  ("unsatisfied prerequisites for the **default build-and-test path**") and only ever had the hooks _reported_, so the
  defect was a classification chosen in the code and never written into the spec). The worked case is the
  `remove-redis-client-label` detour — a full propose→apply→verify cycle for a chart-default its labels must not have,
  with the design weighing implementation alternatives but never questioning the premise; the premise, and why it
  collapses, are in the `*-client` pod-labels bullet below. captured: publish-helm-chart captured:
  record-remaining-architecture-advisories captured: fix-timeout-load-flakes captured: record-worktree-cost-measurement
  captured: spec-cache-fallback-failed-fetch-contract captured: enforce-web-ui-import-boundaries captured:
  classify-repo-state-checks captured: rework-readme-lifecycle-diagram captured:
  load-stage-workflow-instructions-on-demand

- **An authority rule names the source of truth, not the winning value — resolve a value disagreement from the repo's
  own prior reconciliation.** ADR-0002 makes the Java `@ConfigurationProperties` the surface that owns a property's
  default, not the value to prefer once the surfaces have drifted; a change that read it as "the Java value always wins"
  planned to lower the deployed `showcaseCache` bound to the field's lagging value, until review showed
  `align-gateway-cache-defaults` had resolved the identical drift by picking the operationally-intended value and
  raising the Java default instead. When reconciling a value stated on several surfaces, search the archives for the
  prior change that reconciled the same class of drift and follow its direction — and read the introducing commit's full
  diff before treating an edit as incidental (the `git log` check above).

### Workflow: Change lifecycle

**Trigger** — you are starting, implementing, or shipping any unit of work that will become a PR.

**Procedure**

1. Propose the change: create its artifacts (or the unit's edits) and put them on a branch named after the change,
   created per the "Fork every new branch" invariant. Commit nothing.
2. Leave the work uncommitted. The proposal, the review findings, and the implementation all stay in the working tree,
   so the user's review pass runs against the visible `git status`/`git diff`; the change's `docs/ideas.md` removal
   rides the branch like the rest.
3. Run the review gate (see "Workflow: The review gate") and fix every finding until it is clean.
4. Ask the user for the manual review pass. Do not commit, push, open a PR, archive, or merge before the user approves
   the implementation — except a commit the user explicitly asks for.
5. After the user approves: run CI, archive, and merge per "Workflow: Ship a change". The change dir and all subsequent
   work live on that branch.

**Decision points**

- IF a commit is forced — a push is requested, or a `git switch` would carry unfinished changes (tracked or untracked)
  onto another branch — THEN commit (or `git stash`) that unit before switching. An untracked unit survives a reset but
  rides a `git switch` onto whichever branch you land on, where a directory-scoped `git add` can stage it into the wrong
  unit's commit, so commit each unit on its own branch before starting the next.
- IF you open a proposal-stage draft PR (see the README) THEN the branch's first push carries the proposal alone; on the
  default path it carries proposal and implementation together. A push is the moment a change commits; the archive adds
  a commit before the final push.
- IF the user's reply does not address the approval request — they ask about something else, or the thread moves on —
  THEN the request is still outstanding: re-ask explicitly before committing, pushing, opening the PR, archiving, or
  merging, and never read a tangential reply as clearance.
- IF the reply moves the thread to the next unit THEN re-ask before starting that unit; the new branch forked from
  `main` excludes the unmerged one, so its edits read pre-change there (expected, not a revert).
- **No in-flight branch:** IF a unit's work begins with no in-flight branch (a merge-time capture, a docs unit applying
  audit findings, a standalone fix) THEN it is itself a change-sized unit: start it on its own branch, forked from the
  just-merged `main`, as soon as you begin, so `main` never carries an in-progress diff. The capture after the
  upstream-report PR (#212) was applied directly on `main` and sat there as an uncommitted two-file diff until a review
  pass flagged it, and the branch was created only then; a docs unit applying audit findings repeated the sequence,
  branching only when the review flagged it. The leave-work-uncommitted rule presumes a branch — an uncommitted unit
  belongs on its branch, not on `main`. captured: apply-2026-09-28-audit-docs-findings

**Failure handling**

- Work done while awaiting the approval pass stays in the working tree until it is given; as the owner recounted
  afterwards, a clean quick review asked for the manual pass, the reply asked about lesson capture instead, and the
  capture was folded into the same branch and the work continued — nothing was committed before the repeated request was
  answered, but the request had been missed.
- **Rejecting a proposal is a branch delete, never a `main` cleanup.** A branch parked because its premise collapsed is
  neither a rejection nor a merge: deleting it loses work the premise did not touch, and merging it ships a fix for a
  failure that is not there. Record what survives beside why the premise died — the code can outlive the rationale — and
  answer a later "does this still make sense?" by re-deriving the artifacts to that surviving capability rather than
  re-litigating the dead premise. captured: route-the-smoke-through-the-ingress
- Until the first commit the change dir is untracked, which is safe against `git reset --hard` and `git checkout --`
  (verified); `git clean -fd` is the one loss vector, so never run it on a branch holding unfinished work.
- **Refreshing a committed branch:** a branch that has been committed (so it can fall behind `main`) is refreshed from
  `origin/main` — recreate it when it holds no work, otherwise rebase it — rather than continued on stale; once a PR is
  open, the mechanism is `gh pr update-branch` instead (see the BEHIND gotcha). Resolve that rebase for both sides
  rather than picking one: a conflict marks where the two sides overlap textually, so a wholesale side-pick drops the
  other's fact, and a file both sides changed in separate hunks merges silently and needs the same check — verify every
  file both sides touched still carries both facts after the rebase. captured: route-the-smoke-through-the-ingress

**Completion criteria** — the user has approved the implementation and the change is committed, pushed, and archived per
"Workflow: Ship a change". Review findings are edited in the working tree, never committed as an "Address review
findings" commit, and nothing is committed while a review loop runs. Committing early and then adding one "Address
quick-review findings" commit per review round produced 11 commits for a single change (squashed before delivery) — the
discipline above removes that failure mode by construction, leaving nothing to squash. captured:
capture-reverted-sweep-lessons (#283)

### Workflow: The review gate

**Trigger** — you have finished a change's proposal (planning artifacts) or its implementation, or any non-OpenSpec unit
(a docs refresh, a standalone fix, a dependency bump), and before you commit, push, open a PR, archive, or merge.

**Procedure**

1. Run the pre-write derivation pass over the planning artifacts, deriving each of the five claim classes the loop keeps
   catching from its source: read every run-log, issue-state, or external-artifact claim from the artifact now; re-grep
   every count — and every universal claim (`every`/`all`/`each`) over a corpus or an enumerated list — against its set,
   naming the instances it covers where the class has no greppable predicate, and splitting a claim whose list carries
   an exception — the members that carry the attribute, and the excepted item with its own coverage — rather than
   qualifying the exception, since the quantifier asserts the attribute of it (a count of prior changes reproduces as
   different totals by criterion, so state the instances or the criterion, never the bare number); captured:
   extract-load-test-commands; single-source a claim, a rule set, or a record stated in more than one artifact; derive
   every mechanism or task-check claim from the tool or the graph, deleting a term or mechanism name whose grep finds no
   referent in the repository (a coinage lifted from an external prompt; a design carried three, each stripped by a
   review round); and, for a change whose facts are its own measurement, computation, or process record, derive each
   figure from the mechanism that produces it — the window or instant it spans and the aggregation it applies, or for a
   record figure the command that emits it (a review round's row from its report's record line, the capture record's net
   delta from `git diff --numstat AGENTS.md` after `spotlessApply`, per-hunk `git diff -U0` counts when other edits
   share the file) — re-deriving it after that mechanism's last edit, since a figure measured before the final tuning,
   or written from what the edits were meant to add, is the claim the next round reports unreproducible
   (`verify-unique-review-rounds` recorded a row clean before its round reported and stated a net `AGENTS.md` delta of
   `+2` where the diff measured `+5`; the measurement-attribution gotcha carries the series rule). captured:
   seed-claims-from-the-diff captured: record-agents-md-authoring-standard captured: sharpen-experience-analysis
   captured: sharpen-review-quick
2. Record the pass. Every unit carries it as a task — a task in a change's `tasks.md`, or a line in the pull-request
   checklist for a unit with no change dir — so a missing box is a skip, not a pass that found nothing. Its output is
   the change's **claims table** — each fact the change states about its evidence (the prior artifacts it rests on) with
   its source or the command that derives it, re-run so it reproduces the figure the row records, since a figure an
   earlier, broader scan produced beside a narrower command is the claim the next round reports unreproducible, its rows
   seeded from the recurring classes `docs/review-rounds.tsv` records — in `design.md`, or in `tasks.md` for a unit that
   writes no `design.md`, or on the pull-request checklist line for a unit with no change dir. Seed it with
   `scripts/claim-scan.py` (via `/seed-claims`), whose candidates for the unit's diff are each resolved into a row or
   recorded as a dismissal beside the table — one reason per kind, never an enumerating clause, governing a row's source
   as well as a dismissal: a source that is a command's output states the reason by class, never a list of the output's
   lines, since a list of a kind's candidates or of an output's lines is incomplete by construction and the artifact
   stating it adds candidates to the scanned set — a lead list, never a gate, and it seeds the table rather than
   replacing the derivation (`showcase/quality/agent-skills` owns the behaviour). A dismissal's reason is itself a claim
   about the repository: verify it against the diff or the source it names before recording it, since a dismissal is
   recorded, not checked (`sharpen-architecture-audit` dismissed the README table rows as carrying only a formatter
   reflow, and the diff held no such change). A row about an artifact the change itself edits anchors its source to that
   artifact's pre-change state (`git show HEAD:<path>`) — its embedded commands included, since a script that opens the
   working-tree file reports the implemented result while the row reads as anchored
   (`git show <rev>:<path> | python3 …`): it stops reproducing once the change lands. A site named for a later sweep is
   quoted by its text or token, never by a line number — a concurrent merge moves it, so the anchor is stale as
   committed (`AGENTS.md:NNNN` anchors were two lines out here). A task's `**Done**` note or a claims-table entry that
   must report a figure states a relative result — the difference between two runs, which the corpus's growth leaves
   fixed — the command's outcome, or a text-quoted site, never an absolute count its own writes move (its appended
   ledger rows, the candidates its own artifact adds to the scan), so re-derive the table after the unit's last append
   to its own record, which adds one. captured: widen-architecture-auditor-to-stale-ideas captured:
   normalize-the-review-round-digest captured: capture-authoring-system-gaps captured: sharpen-architecture-audit
   captured: extract-ci-narrative captured: convert-remaining-gotchas-rules
3. Run the reviewer-session preflight, then `review-quick`:
   - **Preflight** — before each invocation, the re-runs in step 4 included: create the reviewer's session on the unit's
     first round with a fresh invocation and retain the identifier it returns; on every later round pass that saved
     identifier as the invocation's session-continuation parameter, never a fresh invocation relabelled as a
     continuation; compare the identifier the invocation returns with the one you saved, and stop the round reporting a
     session-continuity failure if they differ; start a replacement only after the invocation you are replacing has
     produced one of the recovery cases the rule names, recording the concrete reason and both identifiers — the failed
     session and its replacement's — before you replace; and bound each invocation with a deadline your policy supplies,
     recording the invocation's hand-over **before** you wait on it. A reviewer invocation cannot be cancelled and
     reports nothing while it runs, so a stall is recovered from that record rather than from the stalled session. The
     rule, its recovery cases, the record's contract, the deadline's source, and the hand-over's fields are
     `showcase/quality/agent-skills`'s and `docs/review-rounds.tsv`'s, not restated here.
   - **Run** — `review-quick` against the change's planning artifacts: for the proposal round, the
     proposal/design/tasks/spec-delta coherence and repo fit; for the implementation round, the tasks and delta spec.
     For a capture, supply the existing rules each proposal names (or its statement that none covers it), the round rows
     and the change record, and the `lesson-capture` definition where its gate is checked.
   - Append each round's row with the session fields `docs/review-rounds.tsv` declares — the identifier, the transition,
     and a replacement's reason — supplying the identifier the invocation returned, never an invented one; an omission
     fails `verifyReviewSessionContinuity`. captured: track-review-session-continuity
4. Fix everything it finds; re-run. Repeat until it reports no new observations.
5. Route the deep review, once, from the completed implementation, the diff, the applicable artifacts, and the existing
   review evidence: derive the floor with `python3 scripts/review-routing.py`, passing `--change <name>` when the unit
   has a change dir and omitting it for a unit that has none; decide at or above it (`REQUIRED`, `RECOMMENDED`, or
   `NOT_NEEDED`), and record the outcome and its signals on the change's review task in `tasks.md` (or on the
   pull-request checklist line for a unit with no change dir).
   - A unit with no change dir omits `--change <name>`; naming a directory that does not exist fails the derivation,
     which the rule below reads as `REQUIRED`, so the omission is not a lower floor. What the flag supplies is the
     tool's own contract — its docstring and its `--change` help — not a rule restated here.
   - A `REQUIRED` outcome runs before completion; a `RECOMMENDED` one needs an explicit recorded resolution; a
     `NOT_NEEDED` one creates no deep-review session, prepares no hand-over, and spends no expensive-model invocation.
   - A missing, malformed, or failed derivation is `REQUIRED` — never a silent skip.
   - The outcomes and the procedure are specified in `showcase/quality/agent-skills` and `/review-thorough`.
6. Only then ask the user for the manual review pass.

No semantic self-review precedes the loop — a re-read shares the blind spot that produced the claim — so the author owes
only the pre-write pass above and the mechanical pre-flight (the formatter and the touched module's gates after the
final edit). captured: decide-against-pre-review

**Decision points**

- **Pass each prior finding's instance, not only its class:** IF you are re-running the review after a fix THEN pass
  each prior finding's **instance** (`file:line` and the claim) from the prior round's report, so the subagent can
  report an unfixed prior instance, or a new instance of a class a prior round already raised, rather than classifying
  each round in isolation. The instances live only in that report, not the class-only record line, so a loop whose
  **caller** is about to lose its context — a new session — writes them to a handover before it is lost and resumes by
  running the round, the handover written from the artifact (each `file:line` and its text), never from what the edit
  was meant to do: its claims are the successor's premise, and one that does not reproduce costs the round it was meant
  to save. captured: record-agents-md-authoring-standard
- **Continue the reviewer's session** — step 3's preflight is what makes that happen, and each round must not lean on
  the reviewer's memory in place of the hand-off, which is the caller's record. The caller's own re-check of the
  handover is a recorded deviation, not a passed round (a resumed round found defects the handover's self-review had
  passed). captured: retrospective-2026-10-15
- **A finding that adds behavior is a scope change, not a code fix:** applying it to the code and a test alone leaves
  the change's planning artifacts — proposal, design, tasks, and delta spec — describing the old behavior, and the next
  round reports the residue as spec-/task-drift. Sweep every one of them, not only the file the finding names, as part
  of applying the finding (and the docs, per the docs-refresh convention). A **framing or coherence** correction takes
  the same sweep even though it adds no behavior: the artifacts the change states the model across — planning and
  shipped alike — state one model of the subject, so correcting the model in the artifacts the finding names leaves a
  sibling — often the shipped copy, not only `proposal.md` — stating the old framing, which the next round flags as
  coherence drift. Re-read every artifact that states the corrected claim, `proposal.md` and the shipped copies included
  — and the section _inside_ an artifact, such as a delta requirement's description beside its scenarios — before
  re-running the review; `openspec validate` checks a scenario's presence, never that it still agrees with the
  description. captured: record-baseline-operating-point captured: widen-architecture-auditor-to-revisit-triggers
  captured: add-apply-audit-reductions-command captured: record-agents-md-authoring-standard
- **Single-source a claim, a rule set, or a record one source owns** — a predicate the requirement owns (a threshold,
  band, or formula); a scope the owning definition states via its class names; a standard or rule set whose rules are
  never enumerated at a referencing site (including the change's own planning artifacts); a field set one artifact owns
  (a report's per-item format — the shared report contract's per-item budget); or a record another artifact owns (a
  review round's state is `docs/review-rounds.tsv`'s, not a task's): state it once there and repeat it identically
  (referencing it by name where the site can), since a restated copy re-anchors on its own (a README copy made the
  matching band a tenth of the _measured_ rate where the requirement and code use the _recorded_ one) and the sweep has
  no canonical wording to sweep to. An enumeration repeated at many sites cannot be proven complete by any grep, read,
  or sweep — the restatement is the drift surface — so a loop that keeps finding another site of one set
  (`incomplete-enumeration` round after round) single-sources it rather than sweeping, deriving the sites from the
  owner's own scope (its bearers crossed with its item classes); a field-list grep is a lead, never a completeness gate.
  The boundary deciding whether a site states an item or points at it is the imperative-versus-procedure split: an
  imperative a reader must act on before the step that would touch it — a warning, a safety condition, a lifecycle
  requirement, a confirmation — is stated at every site where that step appears, so N definitions carrying the same four
  imperatives is this rule working rather than the restatement it warns against, and a sweep over such a set corrects
  all N (correcting the one a finding named and leaving its siblings is the `incomplete-enumeration` recurrence); only
  the owner's procedure steps, field set, or record are pointed at, since those are what a copy drifts from.
  Over-statement is the mirror defect: a site that restates a procedure, a skill's steps, or a script's semantics
  instead of naming the owner has taken the copy this rule forbids. captured: establish-structured-writing-standard
  captured: require-evidence-in-report-contracts captured: harden-command-definitions
- **A clean quick review is a precondition for asking for the manual review, not a substitute for it** — it means _ask
  the user now_, not _the implementation is approved_. Never commit, push, open a PR, archive, or merge on the strength
  of a clean `review-quick` alone; the `rework-idea-setup` session reached a merged PR (#152) within minutes, without
  ever requesting the manual pass. The commit → push → PR → CI → archive sequence starts only after the user approves
  the implementation — the "Run CI before archiving" convention does not authorize committing earlier. captured:
  bump-snyk-cli-2026-10-12 captured: require-check-controls-as-tests
- IF the review loop keeps finding the **same class** of observation round after round, it is not converging — each fix
  is treating a symptom of a root cause that is still there. Stop, re-derive the root cause by the shapes under "Failure
  handling", and do not layer another special case.
- The `review-quick` subagent's contract and the pre-write pass record are specified in `showcase/quality/agent-skills`
  — act on its verdict line rather than on your own recollection of the thread.

**Failure handling — re-deriving a non-converging loop**

- **A claim about another artifact or about the change's own mechanism** that under-describes or contradicts it (a
  surface list, an artifact set, a list of touched files or test sites, a count, a unit, a decision's numbers, **what a
  command yields or which inputs it can receive**) was written from memory rather than derived from the surface it
  summarizes: re-derive it by grepping the corpus, the archive, the change's sibling artifacts, and the diff **— or, for
  a mechanism claim, by running the tool** — or, when the change's mechanism does not need the claim, **delete it** and
  shrink the change to its mechanism (the `derive-the-change-claims` proposal loop ran 14 rounds until that rewrite) —
  or abandon the unit. captured: single-source-the-scheduled-audit-mechanism captured: seed-claims-from-the-diff
- **A passage the change was single-sourcing:** each round finds another residue of the duplicate the pointer should
  have removed because the edit trimmed the sub-clause that round named; replace the whole duplicated unit in one edit
  and derive what stays at the pointing site from that site's own subject, not from the finding. A description of a
  sibling artifact's contents — a source list, a delta description, a list of the change's own edits — is the same shape
  with a moving subject: it is stale the moment that artifact changes again, so delete the description and keep the
  pointer rather than updating it.
- **A class the loop reports round after round whose instances share a confirmed cause:** rewrite the affected section
  once from its intended content — correct verified errors, remove verified duplication, preserve every valid
  requirement, exception, warning, and identifier — and verify with the growth-bound bullet's trim sweep. Do not infer
  the shared cause from the class label: a class whose instances have different causes takes the cause-specific shape
  instead. captured: add-extraction-procedure
- **A rule's own boundary wording — or a claim's supporting reason:** a discriminator, or a reason a decision rests on,
  that reads plausibly but excludes (or no longer supports) the case it was written for must be re-derived from the
  mechanism that distinguishes the cases — not merely re-worded — and tested against that motivating case before its
  wording is swept across the artifact set. A review that falsifies a claim's reason falsifies the conclusion it
  carries: re-derive the conclusion rather than substituting a truer reason (`sharpen-architecture-audit` swapped the
  reason behind `No ## Purpose refresh is owed`, left the conclusion, and the conclusion did not reproduce). captured:
  relax-diagram-span-rule captured: sharpen-architecture-audit
- **The change's own self-records (the claims table, the task `Done` notes, the dismissals):** a recurring
  `claim-unreproducible` means they were drafted from what the edits were meant to do, not from the source they name —
  so re-derive each **clause** from that source once the edits land, writing it from the named command's output and
  deleting a clause you cannot so source, and delete a claim whose described edit has no referent. When the repeated
  class is their completeness (a stated fact they omit, a source that does not reproduce), adding the omitted row
  re-triggers — the rows track the facts the artifacts state — so delete the unneeded fact rather than add the row. A
  dismissal's completeness finding is re-derived rather than repaired by deletion: re-derive it from the scan's whole
  candidate list grouped by kind, in one edit — appending the candidate a round named is the enumerating clause the
  claims-table step forbids, and the clause's own growth is the incompleteness the round reports. captured:
  dont-report-what-a-gate-covers captured: make-reviews-predictable-and-recoverable captured:
  apply-2026-10-19-audit-findings captured: sharpen-architecture-audit captured: track-review-session-continuity
- **A revert after a non-converging loop is a legitimate outcome** — record why in the change dir so the decision is not
  re-litigated. captured: propagate-ui-trace-context (#362) captured: realistic-load-test-profiles captured:
  check-load-test-drift captured: check-unique-cron-schedules (#436) captured: derive-retrospective-facts captured:
  derive-the-change-claims

**Completion criteria** — the quick review reports no new observations (the loop is clean), the pre-write pass and
claims table are recorded as required, the deep-review routing decision is recorded with its outcome, any `REQUIRED`
deep review has completed, and the manual review pass has been requested. A clean quick review means _ask the user now_.

### Workflow: Capture lessons

**Trigger** — once a unit's implementation quick review is clean and the skip condition below does not hold, before you
ask for the manual review pass; and at a merge, where the detection below always runs.

**Procedure**

1. Decide whether the skip condition below holds — it is the only case that may go without a run — and skip the capture
   when it does, recording the reason with the capture record (step 3).
2. Run the `lesson-capture` subagent with the diff, the review findings and round rows, the change dir when one exists,
   the existing guidance a candidate would extend, and a short note on what went wrong or was learned. It proposes
   additions to the artifact that owns each lesson — an `AGENTS.md` gotcha or convention, a check/test authoring mode in
   the `check-and-test-authoring` skill, or a shipping-procedure correction in the `ship-change` skill — the rules the
   unit makes obsolete or redundant as retirement or replacement candidates, and any authoring-system recommendation,
   whose fix belongs in a mechanism producing artifacts rather than in this file.
3. Apply the proposals the main agent judges durable, and record the outcome on the capture record — the applied net
   `AGENTS.md` delta, or the skip and its reason. An implementation capture is caused by the unit, so it rides the
   unit's branch and ships in its PR.
4. A merge runs no capture: at the merge, read what the merge alone affected — its non-diff effects, such as an agent PR
   closing its own tracker — and report any candidate lesson it leaves with the rule it would extend (an `AGENTS.md`
   bullet, a mode in `check-and-test-authoring`, or a `ship-change` procedure), then ask the user for explicit
   confirmation before running one. Apply that merge-time capture as its own docs PR (it has no in-flight branch to
   ride).

**Decision points**

- **The capture is owed unless the skip condition holds in full**, and that condition is the only case that may go
  without a run: no review round whose recorded classes go beyond the local-implementation set (`wording`, `formatting`,
  `redundant-code`, `dropped-content`); no finding recorded as a compliance miss of an existing rule; a diff touching no
  authoring surface (`.opencode/`, `AGENTS.md`, `openspec/specs/`, `openspec/config.yaml`, a check, or a workflow); and
  no problem the unit hit already recorded in a `captured:` rule or `docs/ideas.md`. Each clause reads a surface the
  unit already produced — the round rows in `docs/review-rounds.tsv`, the diff, the two corpora — so the default is to
  run it, and the skip is a derived, recorded decision rather than a judgment about whether the work felt instructive. A
  recurrence fails the conjunction deliberately: a problem the repo already records is an instruction-following failure,
  a reason to question the rule's wording rather than to skip.
- **Every proposal names the existing rule it extends** — an `AGENTS.md` bullet, a check/test authoring mode already in
  the `check-and-test-authoring` skill, or a `ship-change` procedure — or states that none covers the lesson, so a new
  rule merges into or replaces one rather than accreting.
- **Every proposal names the decision its rule governs**, and states whether a future change would plausibly hit it and
  whether the cost of not knowing it is material — a proposal that governs no decision is trivia, not a rule, and is not
  proposed (the `disable-axoniq-console-message` sentence this repo deleted stated a fact governing no decision).
- **A proposal passes the promotion gate** before it is proposed — true, actionable, not automatable as a lint/test/CI
  check at reasonable cost, material, general enough for a class of future tasks, and high-confidence with a known
  scope, each criterion defined, with the gate's evidence threshold, in the `agent-skills` spec and `lesson-capture.md`.
  A rule that fails the gate is routed to a check, a spec, an ADR, or the change dir rather than the always-loaded file.
- **A proposal also states its decision difference**, and an addition the existing guidance already determines is
  `already covered` rather than an addition — the test, its evidence rule, and its dispositions are
  `lesson-capture.md`'s and the `agent-skills` spec's, not restated here.
- **A lesson sourced from untrusted content** — a web page, an issue or PR comment, tool output, or a file the unit did
  not author — is verified against the repository before it is proposed, never promoted on the source's word, so an
  injected instruction cannot become a durable rule.
- **IF the addition restates a remedy the target rule already carries** THEN read the duplication as the target rule's
  wording being the gap rather than a missing mode: re-read the rule the incident should have caught by and ask whether
  one word excludes it — a drafted third `git add <dir>` mode restated the staged-set inspection that bullet already
  states, while the real gap was the commit-discipline clause saying "tracked" where the change dir is deliberately left
  untracked; the owner's "would following the existing discipline already have prevented this?" is the test, verified
  against the text before a bullet is added. This branch is where the decision-difference test meets a restated
  _remedy_; the test itself, and the dispositions it names, are `lesson-capture.md`'s.
- **IF a docs-only merge fixes stale facts or removes duplication** THEN the fix is the lesson — do not re-capture it as
  a new gotcha; capture only what the merge left unaddressed. That bars a gotcha that _restates the fix_, not a general
  rule the fix exemplifies: a durable rule absent from `main` is one of the things the merge left unaddressed. Before
  rejecting a captured rule as a re-capture, check `main`'s own text (`git show origin/main:AGENTS.md`) and reject only
  a rule that restates the fix itself.
- **IF the user asks "is there anything else to capture?"** THEN treat it as a prompt to run the subagent again over the
  events, not as a request to justify the previous pass.

**Failure handling**

- **Do not skip the run outside the skip condition, and never conclude "nothing to capture" in its place.** The subagent
  is the arbiter of what a unit taught, and a skip leaves no verdict: `make-the-capture-visible` recorded three skipped
  captures that surfaced only at the merge, and the class they lost — a process mistake that leaves no diff trace — is
  the one no skip condition can see, which is why the skip is a closed conjunction over surfaces the unit already
  produced rather than a judgment, and why the merge-time read re-verifies it. A docs-fix merge has nothing further to
  capture only if the subagent actually reviewed it and said so — or if the merge-time detection above found no
  candidate. captured: add-pre-commit-staged-set-guard (#372) captured: re-measure-load-test-baseline (#429) captured:
  make-the-capture-selective
- **A capture verifies the live state the merge left, not only the diff — and corrects a defect it finds there, not
  merely records it.** The merge-time detection is the pass that can read the merge's non-diff effects: an agent PR's
  closing reference had already closed the tracker it was triggered from (see "Workflow: Complete a cloud (`/oc`) agent
  PR" below), a defect no gate reads — and the earlier capture that recorded the stale-target rule had quoted that
  issue's body without noticing its closure. When the verification finds shipped work broken, fix the live instance (an
  out-of-band corrective action, such as reopening the issue) alongside the recorded rule; documenting the hazard alone
  leaves the defect live.

**Completion criteria** — the capture ran, or was skipped under the skip condition with its reason recorded, and its
durable proposals are applied. Every unit carries a capture record — a task in `tasks.md`, or a line in the pull-request
checklist for a unit with no change dir — carrying the applied net delta or the skip and its reason, so neither an unrun
capture nor an unrecorded skip reads as a run that found nothing, and the merge-time read re-verifies a recorded skip
against the merge's own evidence. A unit whose review loop runs rounds carries its per-round record rows in
`docs/review-rounds.tsv` the same way — the append is the calling agent's per-round act, made when that round's report
arrives rather than while fixing the next round's findings, so a deferred append lands late or never
(`make-the-p99-verdict-confirm-first` appended rows a round late until the owner caught one) and leaving it to the
spec's record line runs the rounds unrecorded (`bound-the-review-loop` ran seven unrecorded until a reviewer caught it).
captured: bound-the-review-loop captured: make-the-p99-verdict-confirm-first

**How a captured rule is recorded**

- **Author each entry to the stated standard.** The knowledge-authoring standard the `agent-skills` spec states governs
  every entry — the representation it takes, a procedure's and a gotcha's shape, the evidence its claims need, and the
  validation before finishing — and the `agents-auditor` reconciles entries against the same standard.
- **At most one capture per unit, never a chain.** Process mistakes that leave no diff trace (e.g. a git command that
  discarded work) are the most valuable thing to capture — this is what makes the capture systematic instead of
  memory-dependent.
- **Each captured rule carries its origin in a `captured: <unit>` marker** — the unit at an implementation capture, the
  unit and its PR when a merge-time detection found the lesson — so a rule's provenance is readable without git and
  survives a reflow: grep the `captured:` token, which no reflow splits even when the captured name wraps to the next
  line. Place the marker at the end of the rule it records; a pre-commit guard enforces that part in `AGENTS.md` and in
  the project-authored agent and skill definitions (`.opencode/agent/*.md`, `.opencode/skills/*/SKILL.md`) — a marker
  lies inside a rule block, never on a plain bullet. On a rule the capture merged into rather than authored, the
  end-of-rule marker records only the latest captured contribution, not the rule's total origin — the pre-existing text
  stays recoverable from `git blame` / `git log -S`.
- **AGENTS.md's growth is bounded, but the bound is a discipline rather than a hard cap:** applying a capture should
  leave the file no larger than it was (the rule and its rationale are in the `agent-skills` spec), and the applying
  agent records the applied net delta on the capture record. The cheapest way to keep it flat: when a bullet's rationale
  is normative in a spec, **keep only what a reader needs to act and point at the spec — a pointer, not a condensed
  copy** (the spec records why). The same routing covers a rule's subject the specs describe **only as an outcome**: the
  mechanism belongs in the capability spec **only when changing it would change a scenario's outcome — verify that
  against the code before moving it** — and a mechanism whose alternatives yield the same result stays in `AGENTS.md` as
  internal control flow. A pointer must also carry every **imperative or target-less fact** the old text held — a
  warning the reader must still act on is a rule, not rationale, and is lost rather than condensed when the pointer
  drops it — and **must not claim more than its target holds**: a spec holds an outcome, not the identifiers,
  declarations, or gate conditions implementing it, nor a rationale it never states. Verify a trim — or any block or
  span replace in an artifact — by sweeping the removed text's distinctive tokens against the trimmed text, the
  pointer's target where one applies, and the rest of the artifact set: a replace leaves the old text's tail behind as a
  fragment that fuses onto its replacement and reads as authored, which a self-re-read does not find —
  `bump-tooling-pins-2026-10-16` spent four review rounds on such survivors, and `trim-agents-md-rule-restatements`'
  five trims dropped six such facts, restored only by review. captured: bump-tooling-pins-2026-10-16
- **Durability governs where the _rule_ lives, not whether every sentence about it does.** Because the corpus is
  evidence-anchored incident memory, the control is the periodic `/audit-agents` pass — whose verdict already reports
  the accreted-rule count — not mass deletion to hit a number.

### Workflow: Author a spec delta and archive

**Trigger** — the change modifies behavior a capability spec covers, or you are declaring a change done (archive).

**Procedure**

1. Decide whether a delta is owed. Grep `openspec/specs/` for the behavior's **name as well as its file** — a
   requirement may name no file at all (`.github/workflows/audit.yml` appears in none, yet `merge-governance`'s audit
   requirement owns that workflow's report-body behavior, so a prompt edit there owes a `MODIFIED` delta that
   `openspec validate` cannot flag as missing). An outcome's owning capability is not the section's heading or a
   report's proposed destination: one section can span several capabilities (the CI section's workflows are owned by
   `merge-governance` _and_ `releases`), so grep each outcome's own subject across the corpus before adding a
   requirement for it. captured: notify-owner-from-the-audit-report (#327) captured: extract-ci-narrative
2. Write the delta at a path that mirrors the main spec's: the delta for `showcase/clients/web-ui` is
   `openspec/changes/<change>/specs/showcase/clients/web-ui/spec.md`. Omitting the `showcase/` segment does not collapse
   to the same capability — OpenSpec reads `clients/web-ui` and `showcase/clients/web-ui` as two capability ids — so the
   delta targets a different spec, and `openspec validate` does not tie it back (`restructure-spec-corpus-findings`
   first wrote two deltas at `specs/clients/…` and `specs/gateway/…` and had to move them under `specs/showcase/…`).
   captured: restructure-spec-corpus-findings (#332)
3. **Prefer a `MODIFIED` block over an `ADDED` one when the new behavior refines a requirement the spec already holds**
   — add a new requirement only for behavior no existing requirement covers. A clause folded into an existing
   requirement costs one delta; a new requirement costs a permanent corpus entry, and the corpus accretes the way
   `AGENTS.md` does (`make-lesson-capture-consolidate` put its obligation into the scenario that already described the
   subagent, while a genuinely new subject like `concise-agent-reports`' shared report contract is still `ADDED`).
4. **A `MODIFIED` requirement block replaces the whole requirement:** copy the current spec's full requirement block
   (description + all scenarios) into the delta, then edit it, preserving the existing scenarios' relative order — never
   hand-write a MODIFIED block from memory. A scenario cannot be renamed.

**Decision points**

- **To retitle a requirement, use `REMOVED` + `ADDED`, not a renamed `MODIFIED` header.** A `MODIFIED` requirement is
  matched to the main spec by its verbatim `### Requirement:` header — only the description/body can change — so
  retitling it while rewording it (e.g. renaming "Vendored agent skills are available to agents" while narrowing it)
  fails `openspec validate --changes` with "Archive would refuse this delta: MODIFIED failed for header ... not found"
  and must be reverted. Write a `## REMOVED Requirements` block naming the old header with a `**Reason**:` (what
  supersedes it) and a `**Migration**:` (how behavior carries over), plus a `## ADDED Requirements` block carrying the
  new header with its full description and every retained scenario (`widen-agents-auditor-to-tooling` was the first
  in-repo use of this route). The schema _instructs_ a `Reason` and a `Migration` on a REMOVED block but does not
  validate them, and the ADDED block is the only place the old scenarios now live — `openspec validate` checks scenario
  preservation for a MODIFIED block, not across a REMOVED/ADDED pair, so carry the full set by hand.
- **IF folding a second requirement into the first** THEN the delta also needs a `REMOVED` block for that second
  requirement, or the main spec keeps both the merged requirement and the orphaned details requirement.
  `restructure-spec-corpus-findings` folded three `… details` requirements into their cores as `MODIFIED` + `REMOVED`
  pairs, each `MODIFIED` carrying the core's scenarios plus the details' so none was lost. captured:
  restructure-spec-corpus-findings (#332)
- **To change a scenario's subject** keep the existing scenario (narrowing its body) and add a sibling — a `MODIFIED`
  block may add a scenario, never rename one.
- **A capability spec covers a human-facing artifact's outcome** — that it exists and what it is for — not its
  presentation content (an inventory of its parts, their layout, or their count, which the documented-numbers gotcha
  owns): `add-web-ui-dashboard-panels` added dashboard panels and owed no delta (the requirement's outcome was
  unchanged), while `capture-axon-showcase-dashboard-in-spec` added the dashboard's existence and purpose to
  `showcase/deployment/helm-chart` and no panel inventory. captured: capture-axon-showcase-dashboard-in-spec
- **A spec rename/move (`git mv`) does not update the spec's internal `#` title, and nothing validates the title against
  the capability path.** Edit the first line of `openspec/specs/.../spec.md` separately to match the new path in the
  same change — `openspec validate` never checks it, so a stale header is silent drift that passes CI (the 2026-08-14
  role-group restructure git-mv'd most spec files but left their `#` headers at the old `showcase/<capability>` paths;
  #105 later fixed the eight leftover headers (helm-chart, the three extensions, load-tests, and the
  command/projection/query services). Two specs long carried non-path titles that predate the restructure
  (`# Ide Config Specification` under `showcase/quality/ide-config/` and `# Infra Image Versions Specification` under
  `showcase/quality/infra-image-versions/`) — a different drift, same lesson — until `apply-specs-audit-findings` fixed
  them). A `#` title is the one spec edit made directly _before_ archive, rather than through a delta (the Purpose
  refresh is the other non-delta edit, but it waits for the archive commit): it is not a requirement, so no delta block
  carries it, and it may be edited directly (as `fix-stale-spec-headers` did as a `skip_specs` change, and
  `apply-specs-audit-findings` alongside four real deltas) — an exception to "sync the main spec only at archive".
  Record it as its own task, so the mixed route is explicit rather than looking like an un-routed main-spec edit.
- **A delta cannot carry a `## Purpose` for an existing capability.** `openspec archive` (and the `openspec-sync-specs`
  workflow) treats the main spec's Purpose as authoritative and leaves it alone; a delta `## Purpose` only seeds a
  capability whose spec does not exist yet and is otherwise ignored. IF the change alters a capability's scope (e.g.
  adding `zstd-jni` to the constrained transitives in `showcase/quality/dependency-security`) THEN record an explicit
  task (as `address-new-snyk-findings` did in task 3.2) and apply the Purpose edit in the archive commit — whether a
  refresh is owed is the "not the trigger" bullet's determination. captured: sharpen-architecture-audit
  `openspec validate` still passes because the Purpose is not validated against the change. A standalone Purpose refresh
  is a legitimate `skip_specs` change of its own — not only a fold-in to the next change that touches the capability —
  and its implementation commit then carries **no** spec edit by design, the only real change being the archive
  commit's: **state the deferral in the report**, since the diff reads as empty of the change's substance (the owner
  asked "why do I see no touched files except ideas.md?").
- **A capability owning a delta is not the trigger for a Purpose refresh** — read the Purpose and refresh it only where
  the change falsifies its own text, a **widening** of a class the Purpose names counting as falsifying it even where
  only the class's granularity is refined. Requirements moving _out_ of a capability can leave its Purpose untouched
  when the Purpose never described them: `restructure-spec-corpus-findings` moved the two Helm requirements out of
  `merge-governance` and refreshed only `helm-chart`'s Purpose, because `merge-governance`'s — which describes branch
  protection and CI gates — is unchanged by requirements it never described. captured: restructure-spec-corpus-findings
  (#332) captured: sharpen-architecture-audit

**Failure handling**

- **A dropped or renamed scenario fails the delta.** `openspec validate --changes` fails with "MODIFIED ... omits
  scenario(s) the current spec still has" both when a delta drops an existing scenario (the first
  `helm-install-builds-webui-image` delta wrote only the new web UI scenario, omitting "The deployed UI can call the
  gateway" and "The UI origin is configurable") and when it renames one (renaming the ASCII scenario in
  `widen-diagrammer-to-mermaid` read as omitting it and adding another) — but not when one is reordered, since
  `openspec archive` writes the delta's order into the main spec and a reorder is silent drift. captured:
  name-review-finding-classes (#367) captured: widen-diagrammer-to-mermaid
- **A proposal-schema defect surfaces only at archive.** `openspec validate` checks a change's delta specs, not its
  `proposal.md`; the proposal-level schema check runs only at `openspec archive` (on its human, validated path — not
  with `--json` or `--no-validate`), prints its findings under "Proposal warnings in proposal.md (non-blocking)", and
  never blocks on them. Keep the `## Why` section within 50–1,000 characters and move a longer rationale into the design
  or the What Changes body; do not treat a green `openspec validate --all` as proof the proposal is well-formed
  (`fix-agent-tooling-audit-findings`'s own proposal tripped the cap this way).

**Completion criteria** — `openspec validate --all` passes; the delta is at the mirrored path; and, at archive,
`openspec archive <change> --yes` completes with both the change-dir move and the main-spec sync staged (see the archive
gotcha).

### Workflow: Extract an `AGENTS.md` section

**Trigger** — an audit's extraction candidate or opportunity is approved, and a dedicated change will move the section
to its destination.

**Procedure**

1. Verify the retrieval path: the destination carries the section's complete contract, or — for a destination that does
   not yet exist — the retained pointer can name the trigger and the destination the change will create, using the
   audit's conditions (`showcase/quality/agent-skills`) and the growth-bound bullet's trim verification.
2. Check the build for a gate that reads `AGENTS.md` as its input, such as `verifyInstallCommands`,
   `verifyDocumentedPins`, and `verifyCapturedMarkers`.
3. Create the destination if it does not exist — a new spec or ADR.
4. Land the content in the destination before the root text is deleted.
5. Replace the section with the retained pointer: the destination, the trigger, and every imperative, warning, or
   identifier the destination does not carry.
6. Retarget each live cross-reference to the section at its new home.
7. Re-run the retrieval path from the trigger.
8. Re-measure the section.

**Decision points**

- IF an existing destination does not carry the section's complete contract, THEN retain the source at root.
- **IF the section's rules answer to different triggers, THEN split the source unit:** move the rules whose trigger
  fires before the decision each governs, and keep the rest at root under their own heading instead of broadening the
  trigger to reach them — one that fires on most turns reloads the body more often than the root text it replaced.
  `reduce-agents-md-loaded-surface`'s first design would have put five search, audit, review, and debugging rules behind
  a check-authoring trigger; the owner caught it, not the review loop. captured: reduce-agents-md-loaded-surface
- **IF a gate reads the block, or reads the file for a property of the content being moved** (the `captured:` markers
  `verifyCapturedMarkers` scans), **THEN retarget the gate to the new destination in the same change**, with a case that
  fails on a misplaced item there, or keep each item the scan would stop seeing: a block-scoped check is emptied by the
  move, while a file-scoped scan keeps passing over what it no longer sees. An item nothing live depends on may leave,
  with its reason recorded. `reduce-agents-md-loaded-surface` moved 19 markers out of `AGENTS.md` and left that scan
  green until the deep review caught it. captured: reduce-agents-md-loaded-surface captured:
  separate-agents-md-operational-from-evidence
- **IF the new destination should own future captures, retarget the capture's destination list in the same change** —
  `.opencode/agent/lesson-capture.md` and the `agent-skills` capture requirement — or the next capture proposes the rule
  back at root. captured: reduce-agents-md-loaded-surface
- IF a removed term has no surviving home, THEN return it to root or reject the move.

**Completion criteria** — the destination carries the contract, the retained pointer resolves from the trigger, and no
removed identifier survives only in the deleted prose.

### Workflow: Ship a change (CI → archive → merge)

**Trigger** — any request that moves a unit towards delivery: a push, a pull request, an archive, or a merge, however it
is worded ("push this branch", "commit and push", "archive this change", "merge this PR", "CI is green; finish it").

**Load** — read `.opencode/skills/ship-change/SKILL.md` (the `ship-change` skill) in full before the governed decision:
before deciding to publish commits to the remote (`git push`, or an API or connector equivalent), to create a pull
request, to archive the change (`openspec archive` or moving its directory), or to merge (`gh pr merge`, the GitHub UI,
or auto-merge). The skill owns the ordered steps, the merge-authority mechanics, the BEHIND recovery, the refspec
remedy, and the archive step's placement.

**Boundary**

- Loading grants no permission; the invariants above govern push and archive authorization.
- A request that does not establish the owner's approval is not clearance.
- Merge only after the `build` check is green and the owner has approved.
- A user who is not a repository administrator requests a reviewer rather than merging.

**Ordering** — the archive is a commit in the **same** pull request, after the `build` check is green and before the
merge.

**Failure handling** — IF the skill is missing or unreadable, THEN stop and report it; never ship from memory. A push
that needs an explicit refspec is still a publication and still loads the skill; the "Fork every new branch" invariant
owns the refspec itself.

### Workflow: Complete a cloud (`/oc`) agent PR

**Trigger** — a `/oc` or `/opencode` comment created or updated a PR you are asked to complete, or a request to continue
or finish an agent-opened PR. A `/oc` in a review body does nothing (`anomalyco/opencode#50247`, retires when the action
gains that trigger).

**Load** — read `.opencode/skills/ship-change/SKILL.md` in full before the governed decision in this stage: deciding to
publish the archive commit, archive, or merge.

**Outcome**

- A **change** PR is archived and then merged.
- A **report** PR is merged with nothing applied.
- The cloud agent cannot merge — the merge is the human's.

**Failure handling** — IF the skill is missing or unreadable, THEN stop and report it.

## Prerequisites

- **Toolchain**: `./scripts/doctor.sh` (or `/check-tooling`) probes every prerequisite — presence, version floors, and
  repo state — and prints the platform-appropriate install command for anything missing.
- **The README owns the documented list and the install columns**; the doctor owns the probe, and `testDoctorCharter`
  keeps the two in step.
- **Only Java and Docker with Compose are needed for the default build-and-test path.** The optional tools gate optional
  paths: `Helm`/`kubectl` for deployment, `Snyk` for `dependencySecurityCheck`, `pack` for the web UI image,
  `actionlint` for `workflowLint` (part of `check`), `Python 3` for the `scripts/*.py` tools.
- **Git hooks**: run `./scripts/install-git-hooks.sh` once per clone to activate the pre-commit guard (the checks the
  `git add <dir>` gotcha enumerates); bypass a deliberate exception with `git commit --no-verify`.
- **The doctor is not part of `check`**: the host is not a build input, so gating it would fail pull requests for causes
  they cannot remediate. The check asserts the declaration and the reporting instead, and ignores that exit code.

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
# plus the chart packaging, the workflow lint, and the repository's verification tasks — the `check` block in
# `build.gradle.kts` enumerates them.
# (a Docker-free check is -PskipITs -Pcoverage.gate.enabled=false — see the coverage-gate gotcha; e2e is never part of
# check)
./gradlew :showcase-command-service:check

# Load tests (Gatling; opt-in, targets the local Helm cluster via its gateway ingress hostname)
./gradlew :load-tests:gatlingRun -Pprofile=smoke
# `gatlingRun` runs the simulation and `:load-tests:test` starts nothing; the property set (each passed as `-P<name>`),
# the profile set and their assertions, the threshold derivation and its fallback values, and the baseline mechanics are
# `showcase/quality/load-tests`'s. The measurement run is `./scripts/load-test-baseline.sh`, whose docstring states the
# run's tolerance and refresh variables and the floors they are compared against. The calibration deliberately ramps
# past the knee and the resource sampling follows the current kube context (`kubectl top pods -A`), so point a run at an
# environment you own.

# Trend the dated records as a chronological series
./gradlew :load-tests:baselineTrend
# reads docs/load-tests/*.md by default (-Precords=<dir> overrides it); the record's fields are the load-tests spec's,
# and a per-request series across recorded runs comes from git
# (git show <commit>:load-tests/src/gatling/resources/baseline.properties).

# Dependency security scan (Snyk; requires the Snyk CLI on PATH, not part of check)
./gradlew dependencySecurityCheck
# The scan passes --policy-path=.snyk (the root Snyk policy). The currently-suppressed findings, their expiry
# rationale live in `.snyk` (its header comment and each ignore's `reason`) — see those rather than restating them here.
# A Snyk plan's quota and reset schedule are the provider's to state, so the command reads them from the provider at the
# time it matters rather than pinning them. A version-pinned ignore goes dead
# silently once a constraint or bump moves the coordinate past its pinned version — unlike the web UI's
# `npm-audit-ignores.json`, which `npmAudit` fails on when an entry matches no finding, the `.snyk` policy has no
# staleness guard — so sweep the ignore and its rationale comment in the change that moves past it.
# Web UI npm vulnerability audit (fails on high-severity findings; not part of check)
./gradlew :showcase-web-ui:npmAudit

# Dependency update report (only catalog-owned coordinates; majors suppressed per
# config/dependency-updates/major-disabled.txt and same-major updates held back per
# config/dependency-updates/hold-back.txt)
./gradlew dependencyUpdates
# Web UI npm update report (not part of check)
./gradlew :showcase-web-ui:npmOutdated
# Build-logic plugin pin update report — the pins only the included build resolves (not part of check)
./gradlew buildLogicUpdates

# Frontend (showcase-web-ui): install, lint, format-check, type-check, tests (verification; the production bundle
# is built by `build`/`assemble`, like the JVM modules' bootJar)
./gradlew :showcase-web-ui:check
```

The `/dependency-updates` OpenCode command runs these reports and summarizes the available updates; the `/gradle-update`
command updates the Gradle wrapper to the latest stable version when one is available, and the `/opsx-tool-update`
command regenerates the OpenSpec command/skill instruction files after a new `openspec` CLI release.

Build-environment constraints can surface as spurious "current version" rows in the report: the build tooling itself
publishes module constraints that `checkBuildEnvironmentConstraints` reads and reports as the current version. For
example, `dependencyUpdates` reported `log4j-core [2.17.1 -> 2.26.1]` although `log4j-core` resolves to `2.26.1`
everywhere — `2.17.1` is the floor of the Log4Shell ban **Gradle itself** applies to every classpath it builds
(`Log4jBannedVersion` in the distribution's `gradle-logging` jar). Such a row is not a real update (upstream
`ben-manes/gradle-versions-plugin#755`), and ADR-0021 filters it in the report's own rules: the plugin consults
`rejectVersionIf` for this path, so a candidate no newer than the version the catalog declares is rejected **when the
plugin's reported current differs from that version**. Keep both halves of that condition — the plugin treats a
coordinate whose every candidate is rejected as unresolved, so a rule that also rejects an up-to-date coordinate's own
version moves every catalog-owned entry out of the report's up-to-date section. When documenting such an external
constraint, name the coordinate and the mechanism, not the constraint's version: that version is the _tool's_ guard, not
a version this repository pins, so a pinned version rots on the next tool bump.

Major-blocking entries in `config/dependency-updates/major-disabled.txt` and held-back entries in
`config/dependency-updates/hold-back.txt` carry a pointer comment naming the coordinate and its rationale; the
authoritative reasoning for each suppressed or held-back coordinate lives in the
`showcase/quality/dependency-management` spec. Both files are line lists parsed on the first `=`, so a `group:module`
coordinate keeps its colon (see the config-read-path gotcha for the `Properties` mis-parse this avoids).

The Helm update check has its own suppression file, `config/helm-updates/major-disabled.txt`: major bumps of the bitnami
infra charts (postgres, kafka, opensearch) are suppressed there because a major chart ships a new preconfigured
`image.tag` that diverges from the test-surface `*-image-tag` pins (docker-compose/Testcontainers) — a coordinated
migration, not an automatic update. The observability charts (kps, tempo) carry no `*-image-tag`, so their major bumps
surface as actionable; verify them with a live install + smoke test (pods ready, Prometheus targets up, Grafana
datasources wired), since `verifyInfraImageVersions` covers only the bitnami infra charts.

The web UI's npm report has a third suppression list, `config/web-ui-updates/major-disabled.txt`: its listed packages'
major-only rows are filtered out of `npmOutdated` (`NpmOutdatedRules`), while their same-major updates stay reported —
`npm outdated` has no `rejectVersionIf` equivalent, so the filter runs on the report's text.

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
  real serializers, Axon `AggregateTestFixture`/`SagaTestFixture`, or a Spring context with a stubbed remote call — but
  external infrastructure is never started. Verifies a component behaves correctly against its real neighbors (e.g.
  `QueryMessageMapperCT`, `ShowcaseAggregateCT`, `ShowcaseQueryClientCT`).
- **Integration** (`src/integrationTest/java`, suffix `IT`): real external infrastructure via Testcontainers
  (PostgreSQL, Kafka, OpenSearch). Verifies services against the real things they talk to. A dependency bump of a
  runtime-path library is verified **here**: a same-major release can be binary-incompatible with another pinned library
  while compilation, the unit/component tests, and the Docker-free check all pass — `opensearch-java` `3.9.0`→`3.10.0`
  changed `Hit.matchedQueries()`'s return type, which `spring-data-opensearch` 2.x's `DocumentAdapters.from` calls — so
  run the full `check` with integration tests before reporting such a bump done. captured: bump-dependencies-2026-09-25
- **End-to-end** (`src/e2eTest/java`, suffix `E2E`): a real deployed system is booted and exercised against all-real
  collaborators, transport-independent — HTTP for the gateway's client-facing API, gRPC for the gateway → query-service
  read path, the distributed command bus (JGroups) for the command-service. The gateway e2e boots the full four-service
  pipeline and verifies cross-service propagation over the full command → Kafka → projection → query pipeline (e.g.
  `ShowcaseApiGatewayE2E`). The web UI e2e (`showcase-web-ui/e2e`, Playwright) boots the same pipeline via docker
  compose and drives the browser against it: create → appears, start → STARTED, saga auto-start reflected over SSE, live
  events appended to the timeline, and a duplicate title surfacing the gateway validation error.

**DB scripts** — before running the command-service standalone (outside Docker), ensure the PostgreSQL event store is
initialized:

```bash
./db.sh init   # creates user `showcase` and database `showcase-events` if absent
./db.sh reset  # drops the database and recreates it
```

## Continuous Integration

The CI gates and the observational scheduled workflows — what each runs, on which trigger, and what it reports — are
specified in `showcase/quality/merge-governance`, and the release workflow's in `showcase/quality/releases`; each
workflow's YAML is its mechanism's authoritative configuration. This section keeps the rules that bind a workflow edit
or a CI-failure diagnosis.

- **A check belongs in the pull-request gate only when the change that trips it can remediate it.** Drift in state no
  pull request causes would fail every unrelated PR, so it belongs in the observational scheduled pattern instead (and
  the out-of-repository surfaces the Docs-refresh bullet names), never `build`. Its complement is a gate-worthy step
  whose own fetch can fail transiently (such as the actionlint install or the Helm chart-repository setup): it stays in
  the gate and is made resilient instead, with a bounded retry (a few attempts and a short pause) that leaves each
  failure visible and still fails once the attempts are exhausted — never toleration (`tolerateFailures`,
  `continue-on-error`), which makes the check pass falsely, or the `check -x <task>` interim, which verifies the gate
  with a member removed. Each instance's outcome is in its capability requirement. captured:
  retry-verify-infra-image-versions-2026-10-16

**A build-file or dependency change costs a one-time full rebuild (~9 min vs ~1 min warm).** The cache configuration —
which Gradle caches are restored, the `nodejs` include the web UI workflows add, and the npm cache step — is the
workflow YAML's: the caches are partitioned by a hash of the build/dependency configuration and Gradle build-cache
entries are keyed on task inputs, and PR runs are `cache-read-only` and re-warm on the next push-to-main run. The setup
never restores workspace `build/` directories, since stale `jacoco` exec data would corrupt the coverage gate.

**On the 4-vCPU runner a cold build's concurrent load can starve a time-sensitive test past its framework-default
timeout.** `grpc-query-transport` (#514), which edited the `protobuf`/`code-check` conventions, saw
`ShowcaseRestControllerCT` fail 38 of its 76 cases at the default 5s `WebTestClient` timeout, while the same suite
passed locally with `--rerun`, the runner's `--max-workers=4`, full CPU saturation, and the four component-test tasks
concurrent. A suite whose subject the change did not change, a stable failure count, and one framework-default timeout
at the same call point point to load timing rather than a code defect: a test whose own cost is a one-time load — the
web UI's `eslint-rules.test.ts` pays its ESLint flat-config load inside the first case, measured at ~0.9s locally — is
given a budget that fits it rather than changing what the test covers. The controller slice's 30s raise did not hold: a
Docker-free PR gate failed the same way at 30s on 2026-10-15, so its mechanism stays an open question — `docs/ideas.md`
(2026-10-06) records it, the slice now autodetects a thread-dump extension for the next occurrence, but the test-timing
assumption the cold build exposes needs a real fix. An intermittent CI-only failure that does not reproduce locally owes
that diagnostic in place of another speculative mitigation — its output must reach the stream the runner prints
(`testLogging { showStandardStreams = true }`; verified: without it the dump is absent from the console) — and a clean
local probe of a candidate cause leaves it **unconfirmed, not refuted**. captured: grpc-query-transport (#514) captured:
fix-main-ci-regressions captured: diagnose-rest-controller-ct-hang captured: fix-timeout-load-flakes

**The workflows track `ubuntu-latest` deliberately; validate a newer image before the label moves.** GitHub migrates the
label to Ubuntu 26.04 over a rollout from 2026-10-19 to 2026-11-19 (`actions/runner-images#14748`), whose impact is the
image's system libraries, package versions, and prebuilt binaries — the surface the smoke's `kind`/`kubectl`/
`docker`/`sudo` steps touch. Pinning an explicit image would cost a `MODIFIED` delta on each of `merge-governance`'s
nine `ubuntu-latest` clauses plus a bump process nothing tracks, so the label is kept and validated instead: the smoke's
`runner` input dispatches the job onto a newer image
(`gh workflow run deployment-smoke.yml --ref <branch> -f runner=ubuntu-26.04`), and its first step prints `runner.os`,
the requested label, and `/etc/os-release`'s name and version — so a failure during the rollout window is attributable
to the image rather than to the change under test. **That check has already earned its keep:** the 26.04 dispatch failed
at `:showcase-web-ui:dockerBuildImage` (the `pack`-built Paketo image), reproducibly, while the four JVM images built —
attributed to the Docker 28 → 29 difference (the store alone is not the trigger — see the buildpack note), and addressed
by the smoke's own `overlay2` step, which a re-dispatch on `ubuntu-26.04` is what confirms.

The merge gate's `check` never builds the web UI bundle (`vite build` lives in `build`/`assemble`), so a change that
alters the built bundle or its runtime — a React/Vite major — must run `./gradlew :showcase-web-ui:e2eTest` deliberately
before it is reported done. captured: migrate-web-ui-frontend-majors

**Workflow: Add or edit a workflow**

**Trigger** — you add a `workflow_dispatch`-enabled scheduled workflow, or edit an existing one.

**Procedure**

1. Verify it by dispatch, not by its schedule (no CI job exercises a workflow body, and `workflowLint` checks only the
   YAML). GitHub exposes `workflow_dispatch` only once the file is on the default branch, so a newly added workflow is
   dispatched from `main` (`gh workflow run <file>`), while an edit to one already on the default branch is dispatched
   from the change's own pushed branch (`gh workflow run <file> --ref <branch>`) — for an update check, exercising its
   report path, jq filter, and tracker-issue lookup end to end.
2. For the `release` workflow — the one added workflow whose _only_ trigger is `workflow_dispatch` — accept that its
   tag-and-release path first runs after merge, and exercise its build-and-push path pre-merge through a `dry_run` input
   or a `publish_tag` backfill, each of which bypasses the `main`-ref guard and creates no tag or release (the bypass a
   ref-guarded dispatch workflow needs, since its own guard otherwise blocks the pre-merge dispatch this rule requires).
   Exercise its failure paths first, with dispatches that create nothing. captured: publish-images-to-ghcr

**Decision points**

- **A schedule-only edit is the exception:** a dispatch runs the workflow body, not the scheduler, so the new `cron`'s
  first fire is its verification — never tick a schedule change off on a dispatch.
- **Verify the workflow's own run before blaming the change:** GitHub's `schedule` trigger is best-effort — a run can
  slip by hours or be dropped under load, and no `cron` value makes it punctual — so an earlier slot is margin against a
  delay, never a guarantee.
- **Schedule placement:** put each workflow's runtime into the repo owner's small hours (UTC+7) — early Sunday morning
  for the weekly runs, so the results are waiting at the start of the owner's working week — and off the
  `:00`/`:10`/`:20`/`:30`/`:40`/`:50` minutes GitHub documents as its high-load slots; the metered pass's placement is
  the model-pin bullet's off-peak call. captured: bump-snyk-cli-pin captured: schedule-jobs-into-the-owners-night
  captured: reschedule-weekly-workflows-earlier

**Failure handling** — a dispatch that fails still exercises the workflow body: read its run log
(`gh run view <run-id> --log-failed`), fix the workflow, and re-dispatch.

**Completion criteria** — the workflow's body has run end to end: a dispatch for an added or edited workflow, or its
first scheduled fire for a schedule-only edit (a schedule change is never ticked off on a dispatch).

A verification the local environment cannot run cannot live as a task in the change dir: `openspec/changes/archive/` is
invisible and no gate reads it, so an unchecked task is silently lost — a dispatch
(`fix-audit-workflow-report-formatting` and `notify-owner-from-the-audit-report` deferred theirs and archived them
unchecked) and a cluster-gated live check alike. Run the check as part of the merge (for a dispatch,
`gh workflow run <file> --ref <branch>` against the pushed branch, so it completes inside the change's PR before the
archive commit — only a workflow not yet on the default branch must land on `main` first), and tick its task in the
archive commit's own edit of the archived `tasks.md`, or in a commit before the archive. The change dir's `tasks.md` is
committed before the verification runs, so the tick is a second edit. `openspec archive --yes` continues past an
incomplete task with a warning line rather than failing, so that line is the only signal the tick is missing. Or park
the follow-up in `docs/ideas.md` and name it in the change's report. captured: bump-snyk-cli-2026-09-29 captured:
fix-upstream-references-config-cache (#634) A workflow whose run is the only verification must also leave the evidence a
failure needs: it deletes its own environment — the deployment smoke's throwaway cluster goes even when a step fails —
so a failed run must print the deployed signal (the pods with their restart counts, the recent events, each pod's log
tail, and the previous log for a restarted container) in a step that runs before teardown, or the failure names no
cause. Scope that evidence to every namespace a load-bearing component occupies, not just the application's: the smoke's
load travels through the `ingress-nginx` controller, so a diagnostics step reading only `axon-showcase` leaves the hop
that carries it unexamined — the step prints the `ingress-nginx` pods, events, and controller logs alongside the
application's. A component that changes namespace takes the failure diagnostics with it. captured:
route-the-smoke-through-the-ingress captured: fix-the-deployment-smoke-503s captured: notify-owner-from-the-audit-report
(#327) captured: expose-grafana-by-hostname

`.github/dependabot.yml` keeps the GitHub Actions versions current (weekly `github-actions` updates), so a major bump
that targets a newer Node runtime surfaces as a reviewable PR instead of a silent CI deprecation warning. **The owner
notification for those PRs comes from `.github/CODEOWNERS`, never a `reviewers` key in `dependabot.yml`** — GitHub
retired that key and silently ignores it, so keep the notification path in `CODEOWNERS`; the gotcha below carries the
probe with its base-branch and author caveats. The `opencode` workflow's `anomalyco/opencode/github@latest` and the Snyk
workflow's `snyk/actions/setup@master` are deliberate floating refs Dependabot does not manage. The `opencode` workflow
also logs a benign `Cache reservation failed: cache write denied` warning from the action's own cache step on the
comment triggers; the run succeeds — do not chase it. Reported upstream as `anomalyco/opencode#49127`; retires when the
action's cache step skips cleanly (or drops to restore-only).

**Workflow: Action an update-check issue**

**Trigger** — an update-check issue (dependency, Helm, buildpack, or tooling) names a target version to bump.

**Procedure**

1. Re-resolve the latest before bumping — the update-check issues refresh weekly, so the upstream can publish again in
   between (the "Helm updates" issue once named a chart version the upstream had already superseded by the time the bump
   was actioned). Confirm the target with the tool's own lookup (`helm search repo <chart>`,
   `gh api repos/<org>/<repo>/releases/latest`, `npm view <pkg> version`); for Helm, `helm search repo` reads the
   **local** index, not the upstream, so refresh it first (`helm repo update <repo>`) or re-resolve from
   `./gradlew helmUpdates`, which runs `repo add`/`repo update` before its search (a hand-run search named `92.3.0` as
   newest where the check's fresh run reported `92.9.0`; the prior kps bump hit the same stale reading, `91.9.0` vs
   `92.3.0`). captured: bump-helm-and-kps-2026-10-16
2. Bump to the resolved latest.
3. Re-run the check that produced the finding (`./gradlew dependencyUpdates` / `helmUpdates` / `buildpackUpdates` /
   `toolingUpdates`) and confirm the bumped coordinates no longer appear in its report — otherwise the finding's
   clearance is a claim with no run behind it.

**Completion criteria** — the resolved latest is pinned and the check that produced the finding no longer reports the
bumped coordinates. captured: bump-buildpacks-2026-09-29

## Architecture

CQRS with four services and a web UI:

- **showcase-command-service** — write side, publishes events to Kafka, uses PostgreSQL event store
- **showcase-projection-service** — consumes Kafka events, writes projections to OpenSearch
- **showcase-query-service** — read side, queries OpenSearch, serves the generic gRPC query transport (`Dispatch`)
- **showcase-api-gateway** — REST entry point (`/showcases`), routes to command/query services; also exposes the live
  event stream over SSE (`/events`), ingests client telemetry (`/telemetry`), and applies CORS for the web UI origin
- **showcase-web-ui** — standalone browser UI (React + Vite, Feature-Sliced Design) that browses and drives showcases
  through the gateway and renders the live event timeline; deployed as its own nginx container image (see Docker Images)
  with the API base URL configured via `SHOWCASE_API_BASE_URL`

Key modules (libraries, not services):

- `showcase-command-api` / `showcase-query-api` — API interfaces; their testFixtures are used by clients and services
- `showcase-command-client` / `showcase-query-client` — reactive clients for remote services
- `showcase-query-proto` — Protobuf + gRPC definitions for the query side
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
  a record's canonical constructor cannot express that. The value classes carry `@SuppressWarnings("ClassCanBeRecord")`
  to encode the choice; delete one only if the type genuinely needs no builder, and omit it where the class is already
  not record-convertible (an initialized `@Builder.Default` field is one such case — IntelliJ flags the suppression as
  redundant there). A `@Builder.Default` on a `@Jacksonized` value class guards only an omitted JSON key: an explicit
  `null` sets the field to `null` (`@Size` ignores a `null`), so a builder-defaulted collection also carries `@NotNull`
  or the request NPEs to a 500; a collection that should instead _tolerate_ an omitted or `null` list uses
  `@Singular(ignoreNullCollections = true)`, which makes both empty (as `FetchShowcaseListQuery` does). Whether records
  would be faster is **untested** — the suppression records intent, not a measured result, so do not read it as a
  performance claim. Declare local variables with Lombok's `val` when the initializer infers the type and the local is
  not reassigned, and with the language's `var` when it is reassigned (Lombok's `var` cannot be imported on Java 10+ —
  the compiler rejects `import lombok.var;` as a restricted type); keep an explicit type where inference is impossible
  or would change the type — fields, method parameters and returns, a diamond initializer with no target type
  (`new TreeMap<>()`), a declaration with no initializer or more than one declarator, a
  `null`/lambda/method-reference/array initializer, a declared boxed type the initializer would unbox, or a declared
  primitive wider than the initializer. captured: add-web-ui-rum-observability
- **MapStruct**: default component model is `spring` (`-Amapstruct.defaultComponentModel=spring`)
- **ErrorProne**: NullAway on `showcase.*` packages; disabled in `TestJava` tasks. Every package in a NullAway-compiled
  source set (`main`, `testFixtures`, and `load-tests`' `gatling`) carries a `package-info.java` with `@NullMarked`
  (`org.jspecify.annotations.NullMarked`), so the nullness contract is declared for the whole package.
  `UnrecognisedJavadocTag` is suppressed where it fires (`@SuppressWarnings("UnrecognisedJavadocTag")` on the
  Lombok-processed class): it is a false positive from Lombok's copy of a field Javadoc carrying an inline tag, so the
  Javadoc's wording is not the defect — only removing the tag or suppressing the check clears it. Tracked upstream as
  `google/error-prone#5855` (open, the pinned 2.50.0); retires when an ErrorProne bump past the fix lands. captured:
  suppress-lombok-javadoc-false-positive
- **Checkstyle**: style gate wired into `check` via `code-check-conventions.gradle.kts`; ruleset at
  `config/checkstyle/checkstyle.xml`, generated sources excluded via `config/checkstyle/suppressions.xml`
- **SpotBugs**: finds bugs with findsecbugs and fbContrib plugins; uses `spotbugs-include.xml` and
  `spotbugs-exclude.xml` filters in `config/spotbugs/` if present (see `code-check-conventions.gradle.kts`)
- **LZ4 relocation**: root build forces `org.lz4:lz4-java` substitution (an unmaintained, CVE-affected line; see
  ADR-0020 and `build.gradle.kts`)
- **Build-tool versions live in the version catalog**: a `build-logic` convention plugin never hard-codes a tool version
  — it declares it in `gradle/libs.versions.toml` and reads it via `val libs = the<LibrariesForLibs>()` /
  `libs.versions.<name>.get()` (runtime `node`/`java`, plugin `toolVersion` `checkstyle`/`spotbugs`/`jacoco`, the
  ErrorProne/NullAway and protobuf artifacts, buildpack ids, image tags); see ADR-0015 for the rationale and the
  rejected alternatives. A version that is not a `group:name` dependency (a buildpack id, a builder tag, `node`) is a
  `[versions]`-only entry with no `[libraries]` module. Catalog ownership is single-sourcing, not update tracking: a
  bare `[versions]` entry is not resolved as a dependency, so `dependencyUpdates` ignores it — but `helmUpdates` reads
  the Helm CLI and chart pins, `buildpackUpdates` the Paketo builder and buildpack pins, `toolingUpdates` the CLI
  versions pinned in workflow files, and `buildLogicUpdates` the plugin pins the included `build-logic` build resolves
  (which the main build's own report cannot see), so only the entries none of them reads must be audited by hand.
- **A `build-logic` convention plugin's effect is verified through a consuming module, not unit-tested.** `build-logic`
  has no `gradleTestKit()`, so its `src/test` holds unit tests of plain helper objects (`Versions`, the `*Rules`
  classes) and a plugin's Gradle DSL — e.g. `spring-boot-conventions`' `springBoot { buildInfo { … } }` — cannot be
  exercised there. Assert the effect in a module that applies the plugin (the four JVM services' ITs assert the baked
  `BuildProperties`) rather than writing a test that cannot reach it. captured: expose-service-build-info
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
  scenario tests for this reason. An assertion whose shape must change — a matcher can no longer match exactly because
  the value it compares gained a member — must keep every subject the old matcher pinned: rewriting
  `expect(fetchMock).toHaveBeenCalledWith('<url>', { method: 'PUT' })` into field reads because the added `traceparent`
  header broke the exact match dropped the call's URL check at every rewritten site, and only the quick review caught
  it; assert the URL alongside the new field (`fetchMock.mock.calls[0][0]`), since a silently dropped subject leaves a
  test that still passes. A refactor can silently stop a test from exercising its subject: a function moved into a new
  module needs its tests moved with it (deleting the old test file left a re-created four-case event→predicate mapping
  with two cases covered), and a negative test driving a captured mock callback must assert the callback was captured —
  `callback?.()` no-ops when the wiring is absent, so the assertion passes vacuously. An error-translation chain must be
  tested by driving the whole method, not only its branches: `ShowcaseQueryTransportService.translate` re-mapped its own
  `INVALID_ARGUMENT` output to `INTERNAL` because the default arm caught the `StatusRuntimeException` the chain had just
  produced, and the per-branch cases were all green — only the component test exercising `dispatch` end to end saw it.
  captured: propagate-ui-trace-context (#362) captured: rethink-web-ui-reconciliation captured: grpc-query-transport
- **Spring bean mocks in tests**: use `@MockitoBean` (from `org.springframework.test.context.bean.override.mockito`),
  not the deprecated-for-removal `@MockBean` (`org.springframework.boot.test.mock.mockito`), which has been deprecated
  since Spring Boot 3.4
- **Test tier placement**: a test's tier is decided by its collaborators (see Test tiers). Verify the application's bean
  wiring (`@SpringBootApplication` config) at the **integration** tier via a real context boot — do not write component
  tests that mock the app's own collaborators. Component tests compose real in-process collaborators (e.g. a real
  mapper) with only external infrastructure faked. A test that subscribes to a stream which never completes (an SSE
  endpoint's `Flux`) is a **unit** test with a bounded subscription (`take(1)`, `blockFirst(timeout)`) — inside a
  `@WebFluxTest`/slice test the open exchange leaks and breaks unrelated cases in the same run (the keep-alive slice
  version failed 15 of 76 `ShowcaseRestControllerCT` cases; all 108 passed without it). captured: keep-sse-stream-alive
  (#301)
- **Nested test groups for resilience features**: a `@Nested` class that groups Resilience4j scenarios is named
  `<Feature>Behavior` (e.g., `TimeLimiterBehavior`, `RetryBehavior`, `CircuitBreakerBehavior`), both for uniformity and
  to avoid shadowing the library's `CircuitBreaker` type
- **BlockHound jvmArgs**: only suites whose tests call `BlockHound.install()` need
  `-XX:+AllowRedefinitionToAddDeleteMethods` and `-XX:+EnableDynamicAgentLoading` (e.g. the query-client `componentTest`
  and the gateway `e2eTest` suites); leave them off suites that don't (e.g. a `componentTest` with only an
  `ApplicationContextRunner` test)
- **The WebFlux blocking-execution routing in the gateway is load-bearing — do not remove it as a BlockHound band-aid.**
  Every controller method routes to the bounded-elastic scheduler; removing it reddens the error and validation paths
  (the gateway's invalid payloads become `500` instead of `400`) with zero BlockHound hits either way. The rules and the
  measured evidence live in the spec that owns the outcome — `gateway/rest-api` (`Command error translation`,
  `Query error translation`) — and in `design.md` of the change that moved them there
  (`extract-specd-rationale-from-agents-md`). Do not read the `@WebFluxTest` component-scan the routing forces as
  removable complexity. The query service carried the same routing until its HTTP controller was replaced by the generic
  gRPC transport (`grpc-query-transport`), which is why its error and validation outcomes are gRPC statuses now rather
  than HTTP problem details.
- **Asserting log output**: use `OutputCaptureExtension` (`CapturedOutput`) when the code under test runs **in the test
  JVM** (e.g. `ShowcaseProjectorIT`'s projector logging). It cannot capture a separate process's output — to assert a
  **containerized** service's logs (the code-under-test runs in a different JVM), collect them via `withLogConsumer`
  into a `static StringBuilder` and poll it, as the command-client e2e did before the suite was consolidated (see
  `69f2811`). When an integration test fails with an opaque timeout, read the service-side log the runner captured into
  the test report's XML (`system-out`) before diagnosing the assertion — a 5s `TimeoutException` on a `WebTestClient`
  exchange hid the real `NoSuchMethodError`. captured: bump-dependencies-2026-09-25
- **`@DirtiesContext`**: add it only where a full-context boot leaks global JVM state — JGroups (ports and system
  properties) and JCache (a JVM-global cache manager). Contexts that are safely cacheable don't need it: service slices,
  and `@Nested` classes with distinct `@ActiveProfiles` (which already get separate cached contexts). Keep it on the
  gateway/command-service full-context ITs (each boots a JGroups-enabled service); drop it elsewhere
- **Code coverage**: JVM modules opt in via `code-coverage-conventions`. Coverage is measured per module with
  `jacocoTestReport` (unit + component + integration exec data) and aggregated with the root `jacocoRootReport`. The
  `jacocoTestCoverageVerification` gate is wired into `check` at the baseline in
  `config/jacoco/coverage-baseline.properties` and requires Docker (integration tests). A module can extend the
  generated-class excludes via `coverage.generatedClassExcludes`. The **web UI's** coverage is a separate Vitest/V8
  gate: `npmTestCoverage` (in the frontend `check`) enforces the statement minimum in
  `config/web-ui-coverage/coverage-baseline.properties` (fraction form, scaled to Vitest's percentage thresholds)
- **Architecture Decision Records**: record cross-cutting decisions as numbered ADRs (architecture and supply-chain
  alike, such as a build's third-party artifact source) under `docs/adr/` (Nygard format —
  Status/Context/Decision/Consequences). OpenSpec captures behavior and change plans; ADRs capture the _why_ behind
  structural choices. Capture a decision as an ADR when it is made, not after the fact. A decision that only surfaces
  after the fact (an auditor or review finds it unrecorded) is dated to the day the decision was made — derived from git
  history (the shipping commit), never from the record's own `Date:` field or a reviewing audit's suggested date, which
  transpose a retrospective record's shipped, recorded, and amended dates — with a Context line stating it was recorded
  retrospectively and when; if the decision predates the ADR practice and no date can be established, date the recording
  and say so (the first architecture audit produced two such ADRs, dated the two ways). A retrospective ADR (or the note
  for any other unrecorded cross-cutting declaration whose rationale the repository does not state) that cannot state
  _why_ the decision was made should ask the project owner before recording the rationale as unrecorded — the
  repository's silence is not evidence the rationale does not exist, and a missing _why_ is a question for the human,
  not a permanent gap to write down (ADR-0009 declared its no-Axon-Server rationale "not recorded anywhere in the
  repository" until asking the owner recovered it: avoiding Axon Server's commercial licensing). captured:
  apply-2026-09-28-audit-docs-findings captured: remove-unrecorded-redgate-repository A deferral's `Revisit when:` gate
  names each clause's artifact at that artifact's **current** coordinate and labels its type (external coordinate vs
  internal trigger); states the qualifier that narrows the condition in the gate's own line (a _maintained_ release, not
  any); and a compound gate is a conjunction, met only when every clause is, so a check names the clauses still open. A
  check reads the gate literally: a renamed coordinate (`axon-bom` → `axon-framework-bom`) resolves to a frozen artifact
  that never fires, and an implicit qualifier reads as met by a release the owner's intent excludes. captured:
  correct-adr-0011-axon5-gate captured: correct-adr-0017-spring-grpc-gate
- **Docs refresh on change**: on every change, verify whether `AGENTS.md`, `README.md`, and `docs/adr/` need to be
  refreshed to reflect the new state (commands, config, conventions, gotchas) — including an ADR whose Consequences name
  a follow-on this change lands, or whose Decision it alters (ADR-0006 called scheduled Snyk monitoring a follow-on
  concern for weeks after the Snyk workflow landed, until the first architecture audit caught it). A **version bump** (a
  dependency, tool, or wrapper version) is the mechanical case: it makes stale every live doc that names the bumped
  version — including a coordinate version an ADR or idea quotes as the catalog's current value — so grep the old string
  across `AGENTS.md`, `README.md`, `openspec/config.yaml`, `docs/adr/`, and `docs/ideas.md` and fix each, since no gate
  reads any of them. captured: bump-dependencies-2026-10-12 Update them before reporting the change done; also remove
  the change's idea from `docs/ideas.md` in this PR, not as a separate docs PR. An idea is removed once implemented
  (captured by a change) or once explored and decided against (the durable lesson is captured in `AGENTS.md`/an ADR
  instead); only open ideas remain (see the file's header). Promotion to a GitHub issue is a **link, not a removal** —
  annotate the idea with the issue number when you promote it, since the file's header names only the issue's link to
  the change, not the scratchpad's back-link to the issue, so the two drift apart — and prefer promoting a unit's idea
  at that unit's merge: while its PR is open, citing an artifact the unit ships is a contingent claim that must say so
  (`open as #NNN; not yet merged`) and the clause drops at the merge — the merge-time read owns that drop, and where the
  claim was already published outside the repository it means editing the published artifact, not only omitting it from
  new prose — because the issue is a permanent record outside the repository and cannot cite what the default branch
  does not have. Give every parked entry a trailing status tag stating its disposition (`— parked; no change yet.` is
  the common form; a promoted or explored-and-set-aside idea says so instead: the promotion _replaces_ the parked tag,
  `— promoted to issue #NNN; no change yet.`, never a clause appended past the tag's sentence-final period), since the
  header fixes the sections' order and dating but not the tag, and the tag is what marks the entry as a still-unowned
  idea rather than one already routed to work. Also sweep the un-audited docs for references to the thing this change
  shipped — `docs/ideas.md`, and a retrospective's suggestion rows and prose, which the change marks with the
  suggestion's disposition (**Applied** where it applies it; its routing where it routes it: parked in `docs/ideas.md`,
  a named change, an issue; or closed where it decides against it) rather than removes. An open idea that still calls it
  "the proposed X" is itself a stale claim: update its prose in the same change, including any enumeration or count it
  carries ("two others remain open: A, B") that the change's new instance makes wrong. No auditor **owns** either file
  (each auditor's bullet states its own scope; the architecture-auditor's sweep reaches `docs/ideas.md` for a parked
  entry's staleness). For every docs edit the boundary is **causation _while the change is in flight_**: docs that ARE
  the change (new agent/command/skill documentation, README rows describing a new capability, the change's idea
  removal), and any edit the change causes that exists while it can still ride a branch — a lesson from its
  implementation (the capture), an idea its own sweep surfaced, a fact it falsified — ship with the change's PR, while
  an edit the change did not cause (a stale fact it merely passed by, a standalone reword, a refresh of an
  already-completed change) and one surfaced only after the change is done (a merge-time capture, caused by the change
  but with no branch left to ride) ships as its own docs PR, forked from `main`, batched into one docs PR rather than
  one per item. Decide the owner before committing — a docs PR forked from `main` cannot carry an edit committed on a
  change branch, so committing it there first for a clean tree silently leaves it out of the docs PR and `main`
  unchanged — and verify the fix against the merged PR's diff rather than the PR description, which can claim a change
  the diff does not contain. A parked-idea docs PR owes the refresh too: fold any durable fact the idea reveals into the
  relevant `AGENTS.md`/`README.md` section (e.g. add a newly surfaced manual pin to an existing enumeration) —
  `docs/ideas.md` is a prunable scratchpad, so a fact left only there is lost once the idea is implemented or dropped.
  The un-gated copies of what this file and the specs state take the same sweep, refreshed in the change that moves one:
  the `openspec/config.yaml` `context:` block (the runtime/Spring/Gradle versions, module count, service list, and
  Docker image names that OpenSpec shows the AI when creating artifacts), that file's per-artifact `rules` (which
  restate obligations stated here and in the specs, injected per artifact alone — `projectConfig.rules[artifactId]` — so
  a clause several artifacts need lives in each list rather than in a shared one), and the repository's GitHub
  description and topics. What is un-gated is their content and currency: `ci.yml`'s probe fails on a malformed item —
  the list's shape, since a bad one silently drops the whole artifact's rule set — but not on whether a rule's wording
  still matches `AGENTS.md` and the spec, so refresh a rule whose obligation moves even though the gate stays green, and
  `openspec validate` never checks the rest, so it drifts silently. A **removal** counts too: when a sweep deletes an
  entry as non-durable, check these copies for the same sentence — the fact has not moved, so the move rule does not
  fire (`disable-axoniq-console-message`'s sentence outlived #292's removal from `AGENTS.md` by four PRs in
  `config.yaml`, until the audit noticed). No auditor owns a surface outside the repository. A file can also _depend_ on
  such a surface rather than describe one: `SECURITY.md`'s private-reporting path is a dead end unless private
  vulnerability reporting is enabled. Enable the setting as part of the change that ships the instruction — a repository
  setting leaves no diff, so a diff-only review cannot see it — and name the enabling in the change's report. captured:
  park-retro-marking-idea (#281) captured: re-measure-load-test-baseline (#429) captured: mark-s3-applied captured:
  attribute-load-test-tail (#570) captured: park-review-loop-follow-ups captured: fix-openspec-config-rules
- **"OpenCode" is capitalized in prose; lowercase `opencode` is only the CLI command, `.opencode/` paths, the
  `opencode.json`/`opencode.jsonc` config filenames, `.github/workflows/opencode.yml`, and the `anomalyco/opencode` repo
  path.** Keep the distinction when editing docs — the lowercase form names a command or path, not the product; the
  README already follows this.
- **README design intent**: the README is a human-facing showcase and onboarding guide, not a reference dump. Preserve
  its intended shape on every edit: section order (intro → Project Structure → Cool Story → Architecture → Technologies
  → Development Workflow → Getting Started → Development Practices → Deployment and Operations → License/Author);
  "Getting Started" is a step-by-step path (tools → sources → build → run → play); prefer Gradle tasks over raw
  `docker compose`/`helm install` commands (they need env vars the Gradle tasks set automatically); use one CLI for API
  examples (curl) — do not add parallel httpie examples; the development narrative is a prompting exercise (the agent
  implements, the human approves); observability is Kubernetes-deployment-only via Helm (custom Axon Showcase Grafana
  dashboard, not in the local compose stack); slash commands render as a table; a worked scenario shows the interactive
  loop. Keep the two files' division of labour as it stands rather than extracting one into the other or into a task
  runner: `AGENTS.md` carries the agent's full command surface and its environment/port reference, while the README
  carries the human onboarding path. The shared commands are too short and too thinly repeated to alias, and copying the
  environment or port reference into the README would give it a second un-gated copy of facts the Java
  `@ConfigurationProperties`, the yml placeholders, the chart values, and the `BPE_DEFAULT_*` map (ADR-0002's four
  surfaces) already own.
- **Surface human-visible capabilities in the README on every change**: while working on a change, actively look for
  behavior a person can _see or experience_ — a cool story moment, a watcher's flow, a demo-able feature, an access
  path, a dashboard — and make sure it is mentioned in the README before the change is reported done (the docs-refresh
  and README-design-intent conventions cover _how_ it is presented; this is the _what_ to look for). Prefer
  experience-oriented framing ("watch the saga auto-start it") over plumbing descriptions. If a feature is deliberately
  not surfaced, note the omission rather than leaving it silent. Examples that were nearly missed: how to reach the
  deployed system (the `setup-hosts.sh` hostnames) and the observability access path (the Grafana hostname).
- **No comments** in source code (per project convention). The sole exception is the `// SPDX-License-Identifier: MIT`
  header, enforced by Spotless on every Java file and by `eslint-plugin-header` (`@tony.ganchev/eslint-plugin-header` in
  the flat `showcase-web-ui/eslint.config.js`, since the original plugin is unmaintained and does not support ESLint
  9/10) on every `showcase-web-ui` source file (the project is MIT licensed; see the LICENSE file)
- **Javadoc**: classes, methods, and fields carry a Javadoc comment describing their purpose (see
  `ShowcaseApiErrorResolver`, `ShowcaseRestController`); where a `@Nullable` field's `null` encodes a domain state
  rather than "unknown", state what `null` means and why a live instance cannot be `null` (`ShowcaseSaga.showcaseStatus`
  is `null` only once the showcase is removed); wrap at 120 characters. The `showcase-web-ui` uses JSDoc the same way:
  exported components, hooks, and helpers carry a `/** ... */` comment describing their purpose (e.g. `ShowcasesPage`,
  `contextualTime`, `useShowcaseReconciliation`); both wrap at 120 characters. The rule covers every declaration in a
  file you touch, not only the one a review named: adding a class to an existing `build-logic` or Java file puts the
  enclosing type and its private helpers in scope too, so sweep each touched file's declarations in the same pass.
  captured: check-agents-install-commands captured: document-saga-status-removed-marker
- **Frontend (`showcase-web-ui`)**: organized per Feature-Sliced Design (`app`/`pages`/`widgets`/`features`/`entities`/
  `shared`, importing only downward and only through a slice's public API — a sibling-slice import needs a declared `@x`
  cross-import API; `eslint-plugin-boundaries` enforces the direction and the public-API rule in the lint gate; `@/`
  alias → `src/`). The module's naming conventions are enforced by the same lint gate: components and types
  `StrictPascalCase`, hooks `use*` (via the React-hooks check), helpers and parameters `strictCamelCase` (an unused
  parameter may be `_`), module constants `UPPER_CASE`, and unit tests `*.test.ts(x)` — a `src` `*.spec.ts` is not
  collected by Vitest (the Playwright `e2e/` suite keeps its own suffix). Server state via TanStack Query; client state
  via Redux Toolkit slices owned by the entity slices and composed into the store in `app`; forms via React Hook Form +
  Zod. Format with Prettier (`format:check` gated in `check`; apply with `./gradlew :showcase-web-ui:npmFormat`); lint
  with ESLint 10 via the flat `showcase-web-ui/eslint.config.js`. Stub a browser global constructor with a `function`
  implementation (`vi.fn(function () { return fake; })`) — under Vitest 5 the arrow form `vi.fn(() => fake)` is not
  constructable and throws. The live event stream has exactly one subscription (the page's `connectEventStream`);
  another consumer reads the events the `showcase-event` slice already records, through its `@x/showcase` cross-import
  API, rather than opening a second `EventSource` — a second connection re-receives the gateway's replay buffer and
  double-delivers events. captured: migrate-web-ui-frontend-majors captured: rethink-web-ui-reconciliation
- **Avoid redundancy**: don't write redundant code — e.g. redundant `throws` clauses on test methods, explicit type
  arguments that diamond inference or target typing resolve, or repeated boilerplate that Lombok covers. Use the
  simplest construct that compiles and stays readable. The same applies to prose: when a bullet needs a set another
  `AGENTS.md` bullet already enumerates, cross-reference that bullet instead of re-listing it — a copied enumeration is
  a second copy that drifts. Merging guidance entries, near-duplicate or complementary, is the same trade in reverse:
  merge the repetition, not the evidence.
  1. Enumerate the concrete anchors each entry carries (an exact warning string, an exit code, a parenthetical
     qualifier, an upstream issue link).
  2. Confirm each still appears attached to the entry that carried it — a shared arrow, preposition, or target list
     after an `or` applies one entry's operand to the other, widening it while every anchor still appears.
  3. Resolve each unit of the merged text back to the entry it came from; reading the entries into the merged text finds
     a dropped obligation and never an added one.
  4. Grep the shorthands that named a bullet a merge retitles so they can be repointed.

  (Whether an already-restated fact is still accurate is a separate check: diff it against the code — see the
  documented-numbers gotcha.) captured: consolidate-docs-update-obligations

- **Formatting**: format Java sources, Gradle Kotlin DSL (`*.gradle.kts`), and build-logic Kotlin
  (`build-logic/src/**/*.kt`) files with `./gradlew spotlessApply` (Spotless: palantir-java-format for Java, ktfmt for
  `.gradle.kts` and build-logic `.kt`, both fixed 120 columns) — the canonical format step, enforced by `spotlessCheck`
  in `check` with no IDE required. After each edit, run `spotlessApply` before reporting the change done; the IntelliJ
  formatter is no longer canonical, and import order is owned by the formatter. `palantirJavaFormat()` and `ktfmt()`
  take no version — they ship with the `spotless-plugin` pin — so a plugin bump can reflow sources tree-wide with no
  config change (`8.10.2`→`8.10.3` reflowed 13 Java test sources); after bumping it, run `spotlessApply` and account for
  the reflowed files in the change rather than declaring "no source change". captured: bump-dependencies-2026-09-29
  - A type body's opening brace is followed by a blank line or a comment (Checkstyle's `RegexpMultiline` gate; enum,
    annotation-type, and empty bodies are exempt): palantir preserves a blank after `{` and removes the one before `}`,
    and will not insert the padding, so a new glued member is fixed by hand. A Checkstyle `message` is a `MessageFormat`
    pattern — a lone `'` is consumed and an unbalanced `{` throws at violation time — so author it without either.
    captured: enforce-type-body-padding
  - A blank line separates consecutive members of a type body (fields included), gated by Checkstyle's
    `EmptyLineSeparator` with member tokens. The exemption is directional — the module checks the gap after a token
    before its next sibling, so no blank line is required _after_ an enum's constant, an annotation type's declaration,
    or an annotation type's member, while the gap before one is still checked. captured: enforce-member-separation
  - The 120-character wrapping convention still applies manually to content the formatter does not touch (YAML; the
    repo's Python scripts (`scripts/*.py`), which Spotless does not own; the `docs/review-rounds.tsv` record rows, one
    line by contract and therefore unwrappable; Javadoc and JSDoc prose, which the formatters do not reflow —
    a >120-character comment line passes the formatter gate and only the manual check catches it); markdown is formatted
    by the root Spotless `markdown` format (Prettier, `printWidth: 120` with `proseWrap: "always"` — a preference, not a
    hard limit: backtick-dense lines can still exceed 120, the accepted trade-off of automating markdown wrapping). A
    file the markdown format does not converge on is **skipped with a zero exit**
    (`Skipping '<path>' because it does not converge`), so a green `spotlessCheck` is not evidence for that file — treat
    a skip as a failure for it, not a pass (reported upstream as `diffplug/spotless#3143`; retires when a skip fails the
    check — the alignment removes this repository's known trigger for such a skip, but the gap remains upstream).
    captured: make-the-p99-verdict-confirm-first The markdown scope is `docs/`, `AGENTS.md`, `README.md`,
    `openspec/specs/`, active `openspec/changes/*/`, `SECURITY.md`, the `.github/` markdown, and the project-authored
    `.opencode/` markdown — the generated `opsx-*`/`openspec-*` files and the vendored `axon4to5-*` skills are excluded,
    while the project-authored `opsx-tool-update.md` stays in scope despite the shared prefix — and
    `.opencode/opencode.json` has its own `json` format. A target's generated-file exclusions must track what the
    generator writes — `/opsx-tool-update` checks the list when the generated inventory changes, since a newly generated
    `opsx-*` command would otherwise be reformatted by `spotlessApply` and then overwritten by the next
    `openspec update`. Verify with a character count (`perl -CSD -lne 'print if length > 120'`), not
    `awk 'length > 120'` — `awk` counts bytes and false-flags a ≤120-character line containing non-ASCII (the `→` arrow
    tripped this three times); the `-l` chomps the trailing newline `-ne` would otherwise count, so an
    exactly-120-character line is not false-flagged. Verify a verification command on a boundary case before recording
    it — the first recipe omitted `-l` and false-flagged every exactly-120-character line. Apply it over the change's
    whole changed set, derived with `git status --porcelain --untracked-files=all` — `git diff --name-only` omits the
    untracked change dir the workflow leaves in place, so the change's own planning artifacts go unchecked — not only
    the files a finding named: a per-file scope leaves the rest unverified and the review re-finds them. captured:
    add-web-ui-rum-observability Formatters cannot reflow string literals (e.g. an error message in Kotlin/Gradle), so
    wrap an over-long string with concatenation (`"part1 " + "part2"`) — the formatter preserves it. Write markdown as
    natural prose and let `spotlessApply` (Prettier) wrap it — do not hand-wrap lines at 120; the formatter owns the
    wrapping and reflows on every run. A bare `$` in prose (outside inline code) is parsed as inline math and blocks
    that reflow — the paragraph silently keeps its original ragged wrapping while `spotlessCheck` still passes; escape
    it as `\$` (which renders as `$`); a bare `|` in a markdown table row splits the cell, so escape it as `\|` —
    Prettier neither escapes nor flags it, and an inline code span is no protection. The formatter also leaves the
    interior of an inline code span untouched — it wraps prose around the span but never rewrites the code text it
    contains — so a defect inside one (a whitespace run) passes `spotlessCheck` and the manual 120-character check
    alike, neither of which has a rule that detects it: proofread inline-code content as content, not as something the
    gate will fix. Never author an inline code span across a source line break — Prettier's reflow joins the lines and
    leaves the continuation line's indentation as extra spaces inside the span (a `paketo-buildpacks/procfile` split
    from its `5.15.0` came out as `paketo-buildpacks/procfile     5.15.0`): keep a span on one source line and let the
    reflow move the whole span. Fenced blocks are a different story — Prettier applies embedded formatting inside a
    fence whose info string names a language it supports (`json`, `yaml`, `markdown`), so that content is gated, while
    an unsupported one (`bash`, `java`, `mermaid`) is not. captured: fix-gateway-cors-allowed-headers (#359) captured:
    optimize-agents-auditor-for-instruction-budget
  - For assertion lambdas inside `argumentSet(...)` parameterized sources, prefer a block lambda body (`(x) -> { ... }`)
    so the formatter indents the statements normally instead of deep-aligning one long expression. The resulting
    "Statement lambda can be replaced with expression lambda" inspection is suppressed with
    `@SuppressWarnings("CodeBlock2Expr")` on the source method (the correct token — not `StatementLambdaInspection`).
  - Line endings are normalized by the committed `.gitattributes` (`* text=auto eol=lf`, with `*.bat text eol=crlf` for
    `gradlew.bat`), so a text file is stored as LF in the index regardless of a machine's `core.autocrlf` and the batch
    file is checked out as CRLF. This is git-enforced, not a Spotless rule: it covers the files Spotless does not own.
    `eol=crlf` governs the checkout only — the stored blob is LF like every other text file; read the two axes with
    `git ls-files --eol` (`i/` is the index, `w/` the checkout), and note `git check-attr` reports `text: set` for the
    explicit `*.bat` rule but `text: auto` for every path the `* text=auto` catch-all matches. captured:
    enforce-line-endings.
- **IDE inspections (optional)**: the build gates are the canonical verification — after each edit, run
  `./gradlew spotlessApply` and the touched module's quality gates (`compileJava`/`check`); no IDE is required. If the
  IDE is available, you may additionally run its inspections on the touched files (through the Steroid MCP
  `steroid_execute_code`, via its `runInspectionsDirectly` helper) and fix warnings, but this is not required and never
  a gate. Prefer assertions like `assertThat(x).isNotNull()` over `Objects.requireNonNull(x)` when guarding nullable
  values in tests, since the IDE recognizes them for dataflow. The null-check case is the mirror:
  `assertThat(frame.data()).isNull()` trips `DataFlowIssue` twice — "The call to 'isNull' always fails with an
  exception" and "Argument 'frame.data()' might be null" — and the intermediate
  `assertThat(frame).extracting(ServerSentEvent::data).isNull()` still warns ("Function may return null, but it's not
  allowed here"); assert through the holder instead — `assertThat(frame).matches(f -> f.data() == null)` — which is
  clean. When an IDE inspection and a checkstyle rule demand opposite shapes, the checkstyle gate decides:
  `UnusedLocalVariable` fails a `switch` arm whose pattern variable is unused (`case TimeoutException ex ->` with `ex`
  unread), while `IfCanBeSwitch` warns on the `if (x instanceof …)` chain that avoids the binding — give every binding a
  use (the timeout arm logs it) rather than contorting the code for an advisory inspection. captured:
  keep-sse-stream-alive (#301) captured: grpc-query-transport
- **Diagrams are delegated.** The main agent runs on the cheap flash model and is weak at diagram work — ASCII geometry
  and Mermaid's rendering pitfalls each repeatedly cost effort and review cycles. Every creation, alignment, or fix goes
  through the `/diagram` command and its `diagrammer` subagent, a comment or label tweak included.
- **The `diagrammer` establishes the mapping and applies its own rules.** It derives the semantic mapping, applies the
  geometry and rendering rules recorded in its definition, verifies its output, and returns the source plus a
  visual-review handoff.
- **Confirm a diagram's semantic mapping with the user before iterating its geometry.** A diagram is a rendering of a
  fixed mapping — which span starts where and ends where; once the mapping is agreed, alignment is mechanical. State the
  intended mapping (each span → its end node) in the change's report and get it confirmed before re-rendering; keep
  review effort proportional to a presentational artifact instead of iterating its geometry through the review agents.
- **A `mermaid` fence is un-gated by Spotless, so a render must be inspected before the diagram ships.** Render it to an
  image and delegate the image to the `vision` subagent with the diagrammer's handoff; a local render proves only that
  the source parses and lays out, never the host renderer's layout or toolbar clearance. captured:
  rework-readme-lifecycle-diagram
- **Visual review goes to the `vision` subagent, which loads only an `http(s)://` URL.** When a screenshot, image, or
  rendered diagram needs inspecting, delegate it — the browser blocks `file://`, so serve a local artifact over HTTP and
  pass its URL.
- **A visual claim that needs a measurement is a hypothesis.** Settle it before acting: it reported a diagram label
  clipped across repeated renders while a measurement found no overflow, so measure the text against its box
  (`getComputedTextLength()` for SVG text; a label `div`'s `scrollWidth` vs its `foreignObject` width).
- **Never normalize a deliberate asymmetry, and never regenerate over a human edit without diffing against it.** When a
  diagram or any doc has been hand-edited, establish what each element starts and ends at before "fixing" it; if the
  meaning is unclear, leave the edit and ask.
- **Experience-analyzer subagent for retrospectives and improvements**: the `experience-analyzer` subagent
  (`.opencode/agent/experience-analyzer.md`) aggregates recent experience across many changes — above the per-unit
  `review-quick`/`lesson-capture` agents. Trigger it with the `/retrospective` OpenCode command (or run it manually):
  the command gathers the digest with `./scripts/experience-analysis.sh [since]` (merged PRs, git log, archived changes,
  AGENTS.md gotchas, docs/ideas.md, and a derived-facts block carrying the window's countable claims already resolved),
  then the subagent returns a retrospective (shipped PRs by theme, lessons, went-well/went-wrong, each significant
  failure's root cause and earliest prevention stage, and the previous retrospective's recommendations evaluated) and
  improvement suggestions classified as `system` (→ docs/ideas.md or a proposal) or `process` (→ AGENTS.md or a subagent
  definition), each stating the pattern it targets and how its outcome would be verified, which the main agent verifies
  and applies. Retrospectives land in `docs/retrospectives/<date>.md` as a docs change. It is deliberately **not** in
  the scheduled `audit` workflow — unlike the three audits, a retrospective is a judgment about a _period_ whose richest
  input is session-only, so it stays on demand (the `agent-skills` spec records why).
- **Agents-auditor subagent for agent-tooling maintenance**: the `agents-auditor` subagent
  (`.opencode/agent/agents-auditor.md`) audits the project-owned agent tooling — `AGENTS.md` and the project-authored
  `.opencode/` files — for the drift an accretion-only set produces: entries contradicted elsewhere, stale enumerations,
  dead cross-references, near-duplicate gotchas, `AGENTS.md` entries that violate the stated authoring standard, and a
  recurring structural defect it traces to the mechanism that produces the artifact. It also measures the always-loaded
  instruction budget — `AGENTS.md`'s size, the effective bound with its source, the growth since the newest report under
  `docs/audits/`, and the extraction candidates with their retrieval-path results (the ledger the
  `showcase/quality/agent-skills` requirement specifies) — so guidance can be moved out of `AGENTS.md` into a
  destination that loads on demand, and where no such destination exists yet it reports an extraction **opportunity** as
  a design recommendation rather than a candidate. Its finding classes (merge, removal, route, and extraction
  candidates, the advisory class for excluded files, the accreted meta rules with their origins, and the
  authoring-system gap class for a recurring defect's mechanism) and the shared report contract are specified in
  `showcase/quality/agent-skills` — see there rather than here. The boundary is a **provenance partition**: it never
  _fixes_ what the repo does not author (the generated OpenSpec instruction files and the vendored `axon4to5-*` skills),
  though it still _reads_ them and reports an advisory item where one contradicts how the repo uses it — a
  project-authored file that shares a generated prefix, like `opsx-tool-update`, stays in scope — a boundary drawn by
  provenance, not a filename pattern, which over-captures (the same holds for any audit, ignore, or lint scope). Trigger
  it with the `/audit-agents` command; the main agent applies the approved findings under the review gate, whose
  delivery routes — each unit scoped to its own artifacts and diff, split by fix type rather than run through one unit —
  are the `showcase/quality/agent-skills` requirement's. The scheduled variant runs unattended in the `audit` workflow;
  the audit itself is on demand. A report's reduction candidates are drained by the standing owner-gated unit
  `/apply-audit-reductions` (specified in `showcase/quality/agent-skills`), run per report rather than left to age; the
  report's other findings go through the normal change workflow. captured: record-agents-md-authoring-standard
- **Specs-auditor subagent for spec-corpus maintenance**: the `specs-auditor` subagent
  (`.opencode/agent/specs-auditor.md`) audits `openspec/specs/` as a corpus for the cross-spec structural consistency
  `openspec validate` does not gate — title ↔ capability-path match, Purpose ↔ requirements fit, requirement
  conventions, cross-spec duplication, and dead cross-references (its classes and report contract are in the
  `agent-skills` spec). It reports a dead reference only once it is verified absent or obsolete — never for a
  false-positive class such as a generated class, an external tool's command, or a conceptual reference — and reports a
  duplication only where the obligations are substantially identical or their overlap is materially ambiguous, leaving a
  related-but-distinct requirement and an intentional repetition alone. It deliberately does **not** check behavior
  against the code (the change workflow's review loop and the archive-time sync own that), audit the corpus's capability
  decomposition (the `architecture-auditor` owns that), re-report an issue an existing gate enforces, or audit a
  proposed change's internal quality. Trigger it with the `/audit-specs` command. The main agent applies approved
  findings through the delivery routes the `showcase/quality/agent-skills` requirement states. The scheduled variant
  runs unattended in the `audit` workflow.
- **Architecture-auditor subagent for design drift, unrecorded intent, and a record whose premise is due**: the
  `architecture-auditor` subagent (`.opencode/agent/architecture-auditor.md`) audits `docs/adr/` plus the architectural
  surface (the service boundaries, the module dependency graph, and the spec corpus's capability decomposition) for
  drift from its recorded decisions, and reports **where clarification of intent is missing** — a deliberate choice or
  absence whose rationale is not recorded — and **a record whose recorded premise appears met** — a deferral's
  `Revisit when:` condition, or a parked `docs/ideas.md` entry whose work has landed, fully or partly, or whose
  objective a different mechanism achieves (its finding classes, its advisory section, and the report contract are in
  the `agent-skills` spec). It sweeps the surfaces a rationale must exist for (dependency `exclude(...)` declarations,
  the major-version-suppressed, held-back, and web-UI-npm-suppressed coordinates, the suppression annotations and
  retained deprecated APIs, the deferrals and band-aids recorded in ADRs or `docs/ideas.md`, and a record whose premise
  is due — a deferred ADR's `Revisit when:` condition, or the work a parked entry describes — held against the
  repository where it names a checkable fact) and searches the repository for a rationale before reporting each item —
  an item whose rationale is already recorded is not reported. The **deferred-and-parked-record** item is the inverse —
  its record exists, so it is reported when that record's premise appears met, a different question from whether the
  rationale exists. It establishes the authoritative record before asserting a violation (a two-record contradiction, a
  stale supersession, or a deliberate exception is an advisory, not a finding). It deliberately does **not** check
  behavior against the code (the review loop and archive-time sync own that), the corpus's internal structure
  (`specs-auditor` owns that), or any property an existing gate enforces. It classifies each `Revisit when:` condition
  before reporting it, and an unverifiable condition is no expiry claim. Trigger it with the `/audit-architecture`
  command. The main agent applies the approved findings under the review gate: the route each finding takes is the
  `showcase/quality/agent-skills` requirement's, with one addition it does not carry: a correction that becomes its own
  change is parked as an idea until then; do not force the suggested correction into the audit-fix PR (the first audit's
  `query-api` boundary finding was verified drift, yet narrowing the dependency broke
  `:showcase-query-client:compileJava` — since landed as `narrow-query-api-dependency`). An advisory item needs the
  user's decision first. The scheduled variant runs unattended in the `audit` workflow.
- **Readme-auditor subagent for the human-facing README**: the `readme-auditor` subagent
  (`.opencode/agent/readme-auditor.md`) audits `README.md` — the repository's human-facing showcase and onboarding
  guide, whose content no gate checks — on three axes: accuracy against the repository, design-intent fidelity (the
  README convention), and coverage of every human-visible capability (the axes and the report contract are in the
  `agent-skills` spec; a clean audit is a valid one-line result). Its scope is `README.md` only — the other documents
  have their own owners — and it is justified as a distinct artifact and audience (humans, not agents). Trigger it with
  the `/audit-readme` command. It is the one audit **not** in the scheduled `audit` workflow — deliberately, since the
  schedule counters the accretion the capture loop produces while the README audit verifies a human-facing document
  against the repository.
- **Justify a new auditor by a distinct artifact/property, not by symmetry — widen an existing one when its artifacts
  are coupled.** A new auditor earns its place only when its artifact or property has drift no existing auditor can see;
  if the drift is visible only across artifacts an existing auditor already holds, widen that auditor instead:
  `specs-auditor` is separate because `openspec/specs/` is a distinct corpus with its own gate (`openspec validate`),
  while a subagent is described across its definition, an `AGENTS.md` bullet, the README, and the `agent-skills` spec,
  so widening `agents-auditor` catches the cross-artifact half a single-artifact auditor would miss. Keep an auditor's
  **fix scope** and its **comparison span** distinct — a compared copy outside the scope is expected, with its fix
  routed to its owner (a subagent is also described in the README's table and prose, fixed by the change's docs sweep).
  Before adding an auditor, name the artifact's drift and which existing auditor cannot see it — if one can, widen
  rather than add.
- **Thorough-review subagent for deep passes**: the `review-thorough` subagent (`.opencode/agent/review-thorough.md`)
  does a deep review of a change against its proposal, delta specs, design, tasks, and the implementation diff — drift,
  correctness, architecture, and conventions — proportionately to the risk the review gate's routing decision names. It
  is reached by that decision (a `REQUIRED` outcome, or a `RECOMMENDED` one the owner resolves to run) or by the
  `/review-thorough` command, whose procedure carries the session, the hand-over, the pass budget, and the finding
  statuses (`showcase/quality/agent-skills` owns the outcomes); a manual invocation is an explicit run, never a
  substitute for a `REQUIRED` outcome. Findings come back grouped by severity, in the shared report contract
  (`showcase/quality/agent-skills` owns it); the main agent applies fixes.
- **A subagent is only invocable through a trigger, not its documentation**: documenting an `.opencode/agent/*.md`
  subagent in AGENTS.md does not make it reachable — ship a `.opencode/commands/*.md` command (e.g. the `/retrospective`
  trigger for `experience-analyzer`) alongside the agent definition. The experience-analyzer agent existed as
  documentation first and was only usable once the user pointed out it had no trigger and the command was added.
- **A recurring unit type — a subagent/command change, a version bump — derives its change dir from the last analogous
  archived change as a **floor** rather than re-deriving it, unioned with the tasks the rules mandate now (the pre-write
  pass's record and claims table first): the artifact set for the former — the union of the sites every artifact-bearing
  task in that change names, not the list one task carries — and for a bump the task set, each task's result record, and
  the spec-position rationale.** **A definition edit that changes spec'd behavior is a change, not a docs edit** — the
  `agent-skills` spec describes that definition's behavior, so such an edit owes that spec's delta and never rides a
  docs, audit-fix, or retrospective PR; a definition-only edit the spec does not describe (a model-pin bump,
  `skip_specs: true`) still ships as a change, with no delta. Beyond the descriptors the auditor-justification bullet
  names, a change also touches the definition's frontmatter `description`; its **own report-contract verdict line** (a
  report that gains or changes an output section must name it there — the shared contract makes an auditor's first line
  state a count, so a pre-widening `<n> findings` line is stale the moment an advisory section exists, becoming
  `<n> findings, <n> advisory`); the shared report-contract requirement's own bearer list, which enumerates the
  report-producing subagents and goes stale when one is added, so it needs a `MODIFIED` delta; the trigger command
  (including its step-1 read-list); the README's **agent table row** and **slash-command table row** and every **prose**
  bullet describing it (the Spec-Driven Development, Agentic Process, and Self-Learning Loop sentences) whenever what it
  reports changes — the tables and the prose bullets are separate copies; a task for the capability `## Purpose` refresh
  (a delta cannot carry a Purpose); and the proposal's `### New Capabilities`/ `### Modified Capabilities` subsections
  ("none" where empty). Keep any enumerated list (the swept surfaces, the finding classes) in the two sites that own it
  — the definition and the delta — and make every other site that states it, the definition's own class paragraph,
  method bullets, and verdict line (separate edit passes) included, illustrative (`such as`) or a pointer, never a copy
  — the single-sourcing rule under "Workflow: The review gate" owns the drift signal (a method bullet routed an
  out-of-scope mechanism to the advisory class while the class paragraph carried the three-branch routing). The
  `widen-architecture-auditor-to-intent-gaps` proposal took repeated `review-quick` rounds because each round surfaced
  one of these that a prior analogous change had covered — read the archived analogous change and grep for the
  artifact's **name and its trigger command** before hand-writing the set, since a site may name only one (the agent
  table row names the agent; the prose bullets and the slash-command row name its command), and for a set the change
  enumerates the sweep rule's member grep is the sweep — and point the proposal's site list at the tasks that name those
  sites rather than hand-writing it, since a hand-written enumeration keeps under-matching the union it derives from.
  captured: add-readme-auditor (#315) captured: bump-kps-chart-2026-10-12 captured: record-review-rounds captured:
  bound-the-review-loop captured: bump-dependencies-2026-10-16 captured: widen-audit-agents-to-authoring-gaps captured:
  sharpen-experience-analysis captured: sharpen-specs-audit
- **An OpenCode model pin is resolved before it is set, and a bump is a multi-file sweep — grep for the old model id,
  and keep the vision pin out of scope.** A pin naming a provider that does not serve the model fails the agent at every
  invocation (`Model unavailable`) with no gate reading it, so resolve it first
  (`opencode models | grep '<provider>/<model>'`) — the `vision` pin read `opencode-go/deepseek-v4-flash-vision-exp`
  from its 2026-09-04 introduction, but that model is **retired** — its name is aliased to the latest DeepSeek Flash —
  and its endpoint answered `Endpoint is unavailable`, so the agent could not run until the pin moved to a model that
  reads images. The cheap flash model (`opencode-go/deepseek-v4.1-flash`) is pinned across several places:
  `.opencode/opencode.json` (`model` and `small_model` — two keys), the flash-pinned subagent frontmatter
  (`.opencode/agent/review-quick.md`, `lesson-capture.md`, `experience-analyzer.md`), the
  `.github/workflows/opencode.yml` `model` input, and the `AGENTS.md` agent gotchas that name the model id (docs that
  ARE the change — update them in the same change). When bumping, grep for the old id across `.opencode/`,
  `.github/workflows/`, and `AGENTS.md` (the `README.md` names the generic `opencode-go/*` form and the vision pin's
  model id, so a vision-pin bump edits it too, while a flash-model bump does not), and exclude the vision agent's pin
  (`opencode-go/claude-haiku-5-5`): the vision model is a separate multimodal model that is not part of the flash/pro
  line being bumped, so a naive sweep that flags it as stale would wrongly "fix" a deliberate asymmetry. Note the config
  `model` key is a default for **new** sessions, not a live override: OpenCode persists the last-used model in
  `~/.local/state/opencode/model.json` (its `recent` list), so a restarted TUI that restores a session keeps that
  session's model and still shows the old one until you switch manually or start a new session — a correct config pin
  does not by itself make the running agent use the new model. The pins also sit behind a **flat-rate, dollar-metered**
  plan (OpenCode Go), so a per-model quota is consumed at the model's own rate rather than by request count, and the
  DeepSeek models carry peak/off-peak rate tiers, so the quota a pass consumes depends on when it runs. That is a
  property of the plan, not of the pin: resolve the current tiers and window at the provider (`opencode.ai/docs/go`)
  instead of pinning them here. It is also a **lever on the quota**: the non-interactive passes — the four audits,
  `/review-thorough`, `/retrospective`, and long capture chains — can be time-shifted into the off-peak window for the
  same metered spend; a scheduled pass's timing is encoded in its workflow's `cron`, so check a schedule's slot against
  the current window (rather than asserting a UTC figure, which rots) when adding or moving one. Check the slot's margin
  to the next peak block against the pass's worst-case run: the window can be split into blocks, so a gap slot is
  off-peak only until an overrun reaches one, and the metered pass belongs in the widest contiguous stretch rather than
  a gap. A schedule alone does not make a workflow shiftable — most scheduled workflows here invoke no model at all, so
  check that the workflow itself spends metered quota before reasoning about its slot. The interactive loop is paced by
  the human and cannot be shifted. captured: capture-offpeak-cost-lever (#369) captured:
  schedule-jobs-into-the-owners-night captured: structure-visual-review
- **Vendored agent skills**: the three `axon4to5-*` skills under `.opencode/skills/` are vendored from the
  `AxonIQ/agent-skills` repository, plugin `axoniq-migration` version 0.2.2 (Apache-2.0), copied verbatim from
  `plugins/axoniq-migration/skills/`. To refresh, re-copy the skill directories from that upstream tree at the desired
  plugin version and update the recorded version here and in the `showcase/quality/agent-skills` spec — a deliberate,
  reviewed change, not silent drift.
- **Tooling lives in a stated place, in four kinds of location — state it here, not per script.** Repository-wide
  tooling goes in `scripts/`; a module's own tooling stays beside that module
  (`showcase-web-ui/scripts/outdated-report.sh`, `showcase-web-ui/scripts/audit-report.sh`, `showcase-web-ui/start.sh`);
  the repository root carries only the generated `gradlew` and the human-facing setup entry points `db.sh` /
  `setup-hosts.sh`; and `.opencode/` holds the OpenCode-runtime surface — agents, commands, skills — with **no
  executable**, so a command may trigger a `scripts/` tool but never hosts its implementation. A `scripts/` tool an
  agent drives gets its own command trigger, and the tool keeps the implementation (`doctor.sh` ↔ `/check-tooling`,
  `setup-idea.sh` ↔ `/setup-idea`, `experience-analysis.sh` ↔ `/retrospective`, `claim-scan.py` ↔ `/seed-claims`); a
  tool with no agent caller stays untriggered (`install-git-hooks.sh`, `load-test-baseline.sh`, `review-rounds.py`), and
  `commit-hygiene.py` is driven by the tracked `scripts/git-hooks/pre-commit` and by the Gradle `verify*` tasks, and
  `review-routing.py` by the review gate's own procedure and by its `testReviewRouting` task. The generated OpenSpec
  command/skill files and the vendored `axon4to5-*` skills are not the repository's to place. Derive any claim about
  placement from the complete set (`git ls-files -s | awk '$1=="100755"'`), not from the location in hand. captured:
  state-tooling-placement-rule (#458)
- **Tooling-setup skill and command**: `.opencode/skills/setup-agent-tools/` and
  `.opencode/commands/setup-agent-tools.md` (project-local, **not** one of the vendored `axon4to5-*` skills) let a
  contributor ask the agent to wire the per-user MCP servers — the GitHub MCP (core) and, for IntelliJ IDEA users only,
  the Steroid MCP — into the flat `mcp` map of their global `~/.config/opencode/opencode.jsonc` (not the `mcp.servers`
  nesting: `opencode mcp add` needs `--global`, and Gotchas carries the shape rationale and the upstream reference).
  Playwright is project-configured. The README deliberately documents only GitHub (and the project-configured
  Playwright): Steroid is optional, IDEA-only, and nothing in the repo requires it (formatting is Spotless), so it is
  surfaced on demand via `/setup-agent-tools` rather than advertised — do not re-add it to the README's server list.
- **Agent scratch files go in `$TMPDIR/opencode`, which OpenCode v2 allows by default.** v2's managed temporary
  directory _is_ `<tmpdir>/opencode` (`opencode debug paths`), and its permissions docs say v2 allows
  `external_directory` access to that directory, so no rule is needed; put PR-body files and similar there — including a
  subagent's own probe scratch, which should be created under `$TMPDIR/opencode` rather than `/tmp`, so it needs no
  grant. Scope any temp grant to a named scratch subdirectory — never the whole OS temp root (`/tmp`, i.e. macOS's
  `/private/tmp`), which would grant every application's temporary files; that is why `/private/tmp` is not in the
  config. The other external paths an unattended run needs are granted declaratively in `.opencode/opencode.json`'s
  `permission` rules. The **`gh` CLI's config directory** is granted as `$HOME/.config/gh/*`: the agent invokes `gh`,
  which consults that directory outside the workspace, and a missing grant hangs an unattended run (see the
  unattended-`ask` gotcha); v2 expands `~`/`$HOME` and substitutes `{env:VAR}` in a pattern, but an unset variable
  substitutes to an empty string (a pattern matching every path), so the config grants the `$HOME` default and a run
  that sets `$GH_CONFIG_DIR` or `$XDG_CONFIG_HOME` elsewhere still prompts. The **globally-installed `openspec`
  package** is granted as `*/@fission-ai/openspec/*` — the CLI reads its own schema templates from an
  outside-the-workspace package directory, and an unattended cloud run cannot answer the prompt it would otherwise raise
  (a `/oc` run was found stuck on exactly
  `/usr/local/lib/node_modules/@fission-ai/openspec/schemas/spec-driven/templates/*`); the wildcard covers the
  per-machine prefix, so nothing is resolved at init. Only reading that package tree outside the workspace exercises the
  `openspec` grant, and no CI job does — the `build` gate's OpenCode probe loads the configuration and the
  agent/command/skill definitions (asserting each resolves with its metadata) and never invokes `openspec`, and
  `workflowLint` checks only the YAML — so a green `check` does not validate it. This replaced a v1 plugin
  (`.opencode/plugin/grant-cli-config-dirs.ts`) whose `config` hook injected the grants: OpenCode v2 changed the plugin
  contract and its typed plugin API has no permission hook, so the plugin no longer loaded and was retired. A tool's
  major-version migration has to treat each v1 surface separately: v2's normalizer auto-mapped the config's
  `permission.bash` onto v2's `shell`, which did not vouch for that sibling plugin, whose load failed with
  `Plugin must export a default definition with an id and an effect or setup function`. v2 pre-approves the
  machine-specific `<tmpdir>/opencode`, which is enough for this repo; `anomalyco/opencode#48100` asks for a portable
  default and remains open upstream. `.opencode/package.json` (tracked) holds the dependencies OpenCode installs at
  startup — the `@opencode-ai/plugin` package stays because the v2 binary installs it into each `.opencode/`, rewriting
  the tracked pin to its own version, so a cloud run's PR can carry the bump (report PR #543 carried
  `1.18.34`→`1.19.0`): read it as the startup install, not a deliberate dependency change, and decide whether to keep or
  revert it. captured: capture-cloud-plugin-pin-bump (#545) `.opencode/.gitignore` keeps only `node_modules` and the
  lockfiles out of the repo. The config is written in the **V1 shape** — the `permission` block, the flat `mcp` map, and
  `small_model` — which v2 lowers into its own `permissions` array and `mcp.servers` envelope rather than the v2
  spellings: the GitHub action the cloud workflows run installs `releases/latest` — the v1 line — and a v2-only
  `permissions` array makes v1 exit at startup (`V2 permissions are not supported by OpenCode V1`), which silently broke
  both cloud workflows until a dispatch exposed it. The same applies to a contributor's **global** config: the ACP agent
  IntelliJ IDEA's AI launches is a 1.x binary, and it exits on `permissions` (`Unrecognized key: permissions`) and on an
  `mcp.servers` key (`Missing key mcp.servers.enabled`) — so the global config is kept in the flat `mcp` shape too, and
  the setup guidance leads there. `opencode mcp add` writes that nesting upstream (`anomalyco/opencode#49904`); the
  guidance retires its caveat when the command emits the flat shape. Keep this file loadable by both majors; the local
  workflow still runs v2, per the README's row. Verify a behavioural change to it — a new grant, model, or server — by
  dispatching the cloud workflow that loads it (`gh workflow run audit.yml`): the `build` gate probes the config's load
  under the action's v1 binary when a pull request changes it, but only a dispatch (or the next scheduled run) proves
  the workflow starts. captured: migrate-opencode-config-to-v2 captured: retire-opencode-permission-plugin (#388)
  captured: fix-cloud-agent-config-for-v1 (#438)

## Docker Images

Each boot service builds a Docker image:

- `aanbrn/axon-showcase-command-service:${project.version}`
- `aanbrn/axon-showcase-api-gateway:${project.version}`
- `aanbrn/axon-showcase-query-service:${project.version}`
- `aanbrn/axon-showcase-projection-service:${project.version}`
- `aanbrn/axon-showcase-web-ui:${project.version}` (static nginx serving the built frontend)

On a release, `.github/workflows/release.yml` publishes the five images to the GitHub Container Registry
(`ghcr.io/<owner>/axon-showcase-*`, tagged `<version>` and `latest`, `linux/amd64`) — see ADR-0018 — and the Helm chart
to `oci://ghcr.io/<owner>/charts/axon-showcase` — see ADR-0019 — while a `publish_tag` dispatch backfills an existing
release's `<version>` image (no `latest`, no chart), and the local and `ci` targets keep building the images into the
local daemon. Each service's `image.registry` defaults to `ghcr.io`; the local and `ci` values set it empty, and the
global registry stays empty so the metrics-exporter sidecar keeps Docker Hub.

Image names are set in each service's `bootBuildImage` task configuration. To build for a non-default platform (e.g.,
ARM64 host), pass `-PimagePlatform=linux/amd64` (or `--imagePlatform=linux/amd64`), which Gradle maps to the
`bootBuildImage`/`dockerBuildImage` task's `imagePlatform` `@Option`.

The web UI image is built differently: `frontend-conventions` registers a generic `dockerBuildImage` task (typed as
`PackBuildImageTask`) that runs the `pack` CLI with the **version-pinned** Paketo NGINX + Procfile buildpacks
(`paketo-buildpacks/nginx@1.3.0`, `paketo-buildpacks/procfile@5.15.4`; the versions are catalog-owned as `paketo-nginx`
and `paketo-procfile`) over `build/dist` (the `pack` CLI is a build prerequisite like Helm/Snyk). The pins are explicit
because an unversioned buildpack reference becomes ambiguous — `pack` fails with "multiple versions … must specify an
explicit version" — once the builder bundles two versions of a buildpack (the intermittent `e2e`/`helmInstallToLocal`
failure). The builder itself is also pinned (`builder-jammy-base:0.4.662`, catalog-owned as `paketo-builder-jammy-base`)
rather than floating, and the `buildpackUpdates` task / `buildpack-updates` workflow reports newer builder and buildpack
versions — no other update check covers Paketo. **The builder and the buildpacks it bundles must be bumped together:** a
`paketo-nginx` pin that is _absent_ from the builder (e.g. holding a buildpack one line behind the builder) makes `pack`
add it from the registry and resolve the added buildpackage to an **arm64** slice even though the builder is published
`linux/amd64` only — the mismatched pair fails with `exec: nginx: not found` where the matched pair serves a proper
x86-64 nginx; the full mechanics and the `address-2026-09-21-update-checks` A/B are in the buildpack-pin gotcha below.
An earlier record held `paketo-nginx` back at `1.2.0` because `1.2.1` "does not work" —
`address-2026-09-21-update-checks` showed that reading was imprecise and took both bumps together. Reported upstream as
a comment on `paketo-buildpacks/nginx#1340` (2026-09-22): whether the added buildpackage should resolve to the target
platform's slice is open upstream. Close-out: re-take a mismatched pairing only once that upstream resolution lands
(then re-test the built image), or if `pack` changes how it resolves an added buildpackage's slice — the rule above
(bump the builder and its bundled buildpacks together) stays the default either way. Unlike the builder, the run image
(`paketobuildpacks/run-jammy-base:latest`) is deliberately left floating so base-OS security — do not "complete" the pin
by freezing it. The image serves the bundle via nginx on `8080` and exposes nginx `stub_status` metrics on `9090`
(`BP_NGINX_STUB_STATUS_PORT`); the Helm deployment's gated `nginx-prometheus-exporter` sidecar (`webUi.metricsExporter`,
on by default when observability metrics export and the web UI ServiceMonitor are enabled) converts stub_status to
Prometheus `/metrics` on port `9113`, which the Service's `http-metrics` port and the ServiceMonitor scrape — the chain
is specified in the `deployment/web-ui` spec. A `PackBuildImageTask` convention defaults the image name to
`${project.name}:${project.version}`, which the web UI module overrides with the deployable
`aanbrn/axon-showcase-web-ui:${project.version}` in `showcase-web-ui/build.gradle.kts`. **A Docker 29 daemon's store is
not by itself the trigger for this build's failure — do not read the driver as the cause:** the failure
(`failed to fetch base layers: open /tmp/imgutil.local.image.*: no such file or directory`) reproduced on the GitHub
runner's Docker 29.4.2 (`ubuntu-26.04`) while **the same build succeeds on Docker 29.5.2 with the containerd `overlayfs`
snapshotter locally** (`io.containerd.snapshotter.v1`), so the driver alone does not explain it and the runner's Docker
setup is part of the trigger. The `daemon.json` `{"storage-driver":"overlay2"}` step is a documented workaround the
runner needs — the deployment smoke sets it for that reason, and that step is retired when `buildpacks/pack#2527` (the
owning venue; `spring-projects/spring-boot#49251` is the downstream thread) fixes the store. `SHOWCASE_API_BASE_URL` —
**no baked default** (compose sets `http://localhost:8080`; the chart's `webUi.apiBaseUrl` defaults to empty) — has no
ConfigMap or volume mount. The runtime contract (the `start.sh` render into `/workspace/config.js` and the fail-fast on
an unset value) is specified in the `deployment/web-ui` spec. The `dockerBuildImage` run prints two informational
warnings from the toolchain, not defects: "Exporting to docker daemon (building without --publish) and daemon uses
containerd storage" (pack exports to the local daemon's containerd store, losing the fast publish path) and "deprecated
usage of stack" (an upstream Paketo buildpack still declares the deprecated `stacks` key instead of `targets`). Neither
is actionable in the build — ignore them. (The first warning names the store the runner's Docker 29 reports, the context
the constraint above is scoped to — so it is a signal about the daemon, not a defect to chase in the repo.)

Similarly, a CNB-built image's timestamps are not host state: Cloud Native Buildpacks stamp buildpack layers with a
fixed past date — they list as `Jan 1 1980` — so builds are reproducible and layer caching stays stable, and this build
passes no `--creation-time`, so the image's creation date reads correspondingly old. Do not read those timestamps, or
the image's age, as evidence about when it was built, which host built it, or whether an image store was re-imported.

## Kubernetes Deployment

```bash
# Deploy to local cluster (must be ordered)
helm install kps prometheus-community/kube-prometheus-stack --version 92.9.0 \
  --namespace monitoring --create-namespace --wait
helm install tempo grafana/tempo --version 1.24.4 --namespace monitoring --create-namespace --wait
helm install axon-showcase-db-events bitnami/postgresql --version 16.7.27 \
  --namespace axon-showcase --create-namespace --wait
helm install axon-showcase-kafka bitnami/kafka --version 31.5.0 \
  --namespace axon-showcase --create-namespace --wait
helm install axon-showcase-os-views bitnami/opensearch --version 2.0.10 \
  --namespace axon-showcase --create-namespace --wait
helm install axon-showcase helm/chart/build/helm/charts/axon-showcase \
  -f helm/values/axon-showcase/values-local.yaml \
  --namespace axon-showcase --create-namespace --wait

# Or use Gradle Helm plugin (builds images, then installs all releases to the local target)
./gradlew helmInstallToLocal
```

Per-release install/uninstall tasks follow `helmInstall<Release>To<Target>` / `helmUninstall<Release>From<Target>`, e.g.
`helmInstallKpsToLocal` and `helmUninstallKpsFromLocal` (to install/verify a single chart without building images — the
app-release install task additionally depends on the four `bootBuildImage` tasks and the web UI `dockerBuildImage`).
Uninstalling leaves `createNamespace` namespaces (`monitoring`, `axon-showcase`) behind; remove them with
`kubectl delete namespace` afterwards.

**Re-applying an infrastructure release's local values wipes its data and leaves the app unusable until the app release
is upgraded too.** Every `helm/values/axon-showcase-*/values-local.yaml` sets `persistence.enabled: false`, so the
values change restarts PostgreSQL/Kafka/OpenSearch against an empty store, and the app chart's schema-creating hooks
(`db-migration`, `index-initialization`, both `pre-install,pre-upgrade`) run only on an app-release upgrade — follow the
infra upgrades with `helmInstallAxonShowcaseToLocal`, or the gateway answers 503 against the missing schema/index.
Verify a resource sizing change under load (`./scripts/load-test-baseline.sh`: 0 failed, no `OOMKilled`/restart spikes),
since an idle install cannot OOM a limit trimmed toward the working set. captured: right-size-infra-resources

**A chart Deployment addition must extend the app release's `installDependsOn` in `build.gradle.kts`.** The list must
enumerate an image-build task for every image the chart's Deployments reference — `ship-web-ui-as-deployable` added the
web UI Deployment without adding its image build, so `helmInstallToLocal` deployed a web UI pod with an image that was
never built. Note the web UI image is built by `:showcase-web-ui:dockerBuildImage` (a pack-based task, not a
`bootBuildImage`); verify the graph with `./gradlew helmInstallToLocal --dry-run` and confirm every chart Deployment's
image has a build task in it. The local and `ci` values set the chart's image registry empty (see the Docker Images note
above), so those targets use the locally-built daemon images, and a `kind` cluster's nodes cannot see the host daemon's
images, so a runner or bare-kind install must build them and `kind load` each before `helmInstallToLocal` — load by
repository rather than pinning the image version, and pass `--name <cluster>` to `kind load`, since kind's CLI defaults
to a cluster named `kind`: a bare `kind load docker-image` against the smoke's `axon-showcase-smoke` failed its first
dispatch with `ERROR: no nodes found for cluster "kind"`. captured: add-deployment-smoke captured:
trim-deployment-smoke-install

**Helm release order**: kps → tempo → db-events/kafka/os-views → axon-showcase, declared by `mustInstallAfter`/
`mustUninstallAfter` in `build.gradle.kts`. Uninstall in reverse. Because the app release also depends on the five image
builds, `helmInstallToLocal` is whole-stack — it builds all five images and installs all six of the releases a person
runs locally (the ingress controller is `ci`-only). The `ci` target is the deliberate exception for the deployment
smoke: it selects the application, infrastructure, and ingress releases (no observability), points at the runner's fixed
kube context, and the releases it installs carry the trimmed `values-ci.yaml` files the smoke needs to fit its runner
(`merge-governance`'s deployment-smoke requirement); the local path stays the one the docs give a person. Fitting a
runner is a **requests** concern — the scheduler places on requests — so a trim lowers the requests and leaves the
memory **limits** alone: a JVM's heap is a fraction of its limit, and cutting the limit toward the runner starves the
heap under load. captured: add-deployment-smoke captured: fix-the-deployment-smoke-503s

**Helm release namespaces**: declared in `build.gradle.kts` — the observability releases (kps, tempo) deploy into the
`monitoring` namespace, the ingress controller (`ingress-nginx`, selected only by `ci`) into its own `ingress-nginx`
namespace, and the application and infrastructure releases (db-events, kafka, os-views, axon-showcase) deploy into a
dedicated `axon-showcase` namespace (created on install). The local deployment does not depend on the kube context's
current namespace or a `helm.namespace` gradle property.

**Helm release target kube contexts**: each release target declares the kube context it deploys to in
`build.gradle.kts`. The `local` target resolves its context per-machine from the `helm.local.kubeContext` Gradle
property (set in `~/.gradle/gradle.properties` or via `-P`), falling back to the developer's current kube context when
unset — so macOS (colima) and Linux (kind/minikube) contributors each deploy to their own local cluster without a
hard-coded context name in the repo. The `ci` target declares the fixed `kind-axon-showcase-smoke` context that the
deployment smoke's throwaway cluster is created under — a comment on each side names the other as the coupling's other
half. Do not hard-code a machine-specific local context name (like `colima`) in the versioned build.

**Chart validation**: the chart is linted as part of packaging (`helmPackageMainChart` → `helmLintMainChart`), and
`check` depends on that packaging — the members the `helm-chart` requirement names — so a chart that cannot bundle its
declared dependency fails the pull request rather than only in the nightly deployment smoke's install. The Bitnami
`common` dependency is fetched by the plugin's `helmUpdateMainChartDependencies` task — which its own docs equate with
`helm dependency update` — and that task's `onlyIf` resolves false when the configuration cache stores its entry before
the chart sources are collected, i.e. on clean checkouts and in CI, so `helm/chart/build.gradle.kts` replaces the gate
with `setOnlyIf { true }`: without it nothing fetches on a clean checkout, the strict lint fails with "chart directory
is missing these dependencies: common", and the deployment smoke fails at the install with "found in Chart.yaml, but
missing in charts/ directory: common". Measured on one commit with only the cache toggled: the task reports SKIPPED with
the cache on and fetches with `--no-configuration-cache`. The upstream gap is parked in `docs/ideas.md`. Lint runs
strict (warnings are errors) and lints the Bitnami `common` subchart, rendering two extra value sets:

```bash
./gradlew :helm:chart:helmLintMainChartFull :helm:chart:helmLintMainChartMinimal
```

Value files live in `helm/chart/src/test/helm/` (`helm/chart/src/test/helm/helm-lint-full.yaml` enables all optional
features, `helm/chart/src/test/helm/helm-lint-minimal.yaml` disables the default-on ones).

Custom values can be placed in `helm/values/<release-name>/values-<target>.yaml` — the plugin applies a release's file
only to the target it names, so `values-local.yaml` is the local target's and `values-ci.yaml` the deployment smoke's. A
target that installs the app release must set `webUi.apiBaseUrl` in its own file: the chart defaults it to empty and the
web UI image fails fast on that (see the Docker Images note), so the UI pod crash-loops and the release's `wait = true`
turns it into an install timeout — a target inherits nothing from another target's values. captured:
trim-deployment-smoke-install

**The `*-client` pod labels belong to the targets that set `allowExternal: false` — keep them in those targets' values,
never in the chart.** They sit in `helm/values/axon-showcase/values-local.yaml` for the local target and
`helm/values/axon-showcase/values-ci.yaml` for the deployment smoke's. The labels (`axon-showcase-kafka-client`,
`axon-showcase-db-events-client`, `axon-showcase-os-views-client`) exist only because those targets set
`allowExternal: false` on the bitnami infra charts (their netpols admit pods carrying the `<release>-client` label), and
their values derive from the release names (declared in `build.gradle.kts`). Moving them into the reusable app chart
would couple it to (a) the bitnami netpol convention, (b) the release names, and (c) a netpol-strictness decision the
chart cannot observe — a deployment that leaves the bitnami netpols open would carry labels nothing consumes. A
`*-client` label with no matching infra release is dead weight (the removed `axon-showcase-redis-client` was a leftover
from an earlier design — nothing in the stack or code referenced redis). This was explored as a chart-default change and
reverted; the labels belong co-located with the netpol restrictions that require them.

The local values expose the API gateway, the web UI, and Grafana via ingress at the hostnames `axon-showcase-api`,
`axon-showcase-ui`, and `axon-showcase-grafana`. To reach them by hostname (instead of a `Host:`-header curl
workaround), run `./setup-hosts.sh setup`, which detects the local cluster's ingress-controller LoadBalancer address
generically (against the current kube context, so it works on colima + Traefik, kind/minikube + ingress-nginx, etc.) and
manages the `/etc/hosts` entries (`./setup-hosts.sh remove` to clean up; re-run `setup` if the address changes on
cluster restart).

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
./gradlew :showcase-query-service:bootRun       # :8083 (gRPC :9090)
./gradlew :showcase-projection-service:bootRun  # :8082

# Web UI (Vite dev server, proxies /showcases, /events, and /telemetry to :8080)
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
Service `8081`, Query Service `8083`, Projection Service `8082`. The query service **additionally** serves the generic
gRPC query transport on `9090` (`grpc.server.port`); its `server.port` (`8083`) now serves only the actuator, since the
query service has no HTTP API. In `docker-compose.yml`, the published `8000`–`8003` mappings are **JVM debug ports**
(`BPL_DEBUG_PORT`), not the services' HTTP ports — only the API Gateway publishes its HTTP port (`8080`); the other
services' HTTP ports are reachable only via the Docker network or `bootRun`. The web UI is published on `8084` (its
container nginx port is `8080`; `stub_status` metrics on `9090`).

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
`*-image-tag` and/or its `bitnami-*` chart version together. The `verifyInstallCommands` task (also part of `check`)
verifies the manual `helm install … --version` commands in this file's Kubernetes Deployment section against the catalog
chart pins, so a chart bump cannot leave the documented command stale. The `verifyDocumentedPins` task (also part of
`check`) verifies the catalog's Paketo pins — the buildpack builder and the buildpacks — wherever `AGENTS.md` or
`README.md` states them, so a buildpack bump cannot leave a documented pin stale. The `verifyInfraImageVersions` task
(part of `check`) derives its checks from the actual `helm.releases` container, resolves each pinned chart's
preconfigured `image.tag` via the Helm CLI (`helm show values bitnami/<chart> --version <pinned>`, using the
plugin-managed client; the task adds and updates the declared chart repositories itself), fails the build if its app
version drifts from the `*-image-tag` after truncating the chart app version to the official tag's numeric segment count
(so `17.6` matches a chart app version `17.6.0` at minor granularity, while `3.9.0` requires an exact chart app version
match), rejects any official tag with fewer than two numeric segments as a floating reference (e.g. `17`, which Docker
Hub re-points to the latest 17.x), and fails if any infra values file (`helm/values/*/values*.yaml`) pins `image.tag` —
so the repo cannot reintroduce a separate override. The task is build-cacheable on its inputs (the pinned coordinates
and values files): since a pinned chart version's preconfigured `image.tag` is immutable, unchanged inputs restore the
verification from the Gradle build cache and skip the Helm resolution entirely. Deriving the checks from the configured
releases means renaming an infra release retargets its check and removing one drops it. External `image.tag` overrides
at deploy time (e.g. `--set` in a release pipeline) are outside this in-repo gate.

**Every Helm chart coordinate in the version catalog is a concrete version** — never a floating major-line pin such as
`77.x.x`. This covers the observability charts (`prometheus-community-stack`, `grafana-tempo`) and the `common` subchart
dependency as well as the `bitnami-*` infra charts, so the Helm deployment is reproducible at a reviewable version. Bump
a chart by updating its concrete coordinate (e.g. `77.14.0` → `77.15.0`), and verify the bump with a live install +
smoke test (`helmInstall<Release>ToLocal`: pods ready, Prometheus targets up, Grafana datasources wired) —
`verifyInfraImageVersions` gates only the bitnami image-tag drift, not that the bumped chart deploys correctly. The
smoke can run on a throwaway `kind` cluster in a GitHub runner — `bump-kps-chart-91` did exactly that
(`helmInstallKpsToLocal` against a runner-hosted `kind`, cluster deleted after) — so a bump is verifiable end to end in
CI without a local cluster.

**Kafka 3.9.0 Testcontainers note**: Kafka 3.9.0 has a validation bug (KAFKA-18281) that rejects Testcontainers' default
listener config (`0.0.0.0` binds). The `KafkaContainer` usages in the IT/e2e suites override `KAFKA_LISTENERS` to
`PLAINTEXT://:9092,BROKER://:9093,CONTROLLER://:9094` (empty hosts make the listeners implicit) so 3.9.0 starts; keep
that override when bumping the Kafka image tag.

## Key Environment Variables

- `SHOWCASE_CORS_ALLOWED_ORIGINS` — comma-separated browser origins allowed cross-origin access to the gateway. The
  container image defaults to empty (fail-closed — deployments must allow their UI origin explicitly); docker-compose
  sets it to the local dev + preview origins, and the Helm chart exposes it as `apiGateway.cors.allowedOrigins`.
- `SHOWCASE_CORS_ALLOWED_HEADERS` — comma-separated request headers the UI may send cross-origin. Defaults to the
  headers the UI sends (`Content-Type`, `Idempotency-Key`, `traceparent`) on every surface — the Java field, the yml
  placeholder, the image's `BPE_DEFAULT_*` map, and the chart value (`apiGateway.cors.allowedHeaders`) — so it behaves
  the same in a deployment and under `bootRun`.
- `DB_PASSWORD=showcase` — PostgreSQL password for command-service
- `BPL_DEBUG_ENABLED=true` / `BPL_DEBUG_PORT=8000-8003` — JVM debug (JDWP) agent ports (`8000`–`8003` are the published
  debug ports in `docker-compose.yml`; see Local Development)
- `THC_PATH=/actuator/health` — health check path
- `JAVA_OPTS=-XX:MaxDirectMemorySize=128m -XX:MaxGCPauseMillis=20` — container JVM tuning

## Gotchas

### Git & repository workflow

- **`git stash pop` can leave conflict markers after a rebase.** The commit-only-at-push workflow leaves the change
  uncommitted and stashes it when a rebase needs a clean tree; if a docs file (e.g. `docs/ideas.md`) advances on `main`
  between the stash and the rebase, popping the stash after the rebase can leave `<<<<<<<` conflict markers in the
  working tree (the stash carries the pre-rebase copy). This surfaced when the ideas-dates fix (PR #64) merged
  mid-rebase. Resolve by reconciling the file — take `main`'s copy and re-apply the change's idea removal — rather than
  resolving the markers by hand; a change branch carries its own `docs/ideas.md` removal (see the docs-refresh
  convention), so it can no longer simply be reset to `origin/main`. Under the commit-only-at-push rule the branch has
  no commits to rebase before its first push, so the stash path arises only after a commit; refresh a pushed branch with
  `gh pr update-branch` rather than a local stash-rebase.

The same stale-stash hazard has a second, quieter failure mode: a stash taken while the working tree holds a **stale
copy** — a pre-pull version of a file whose `HEAD` already carries the pull — records a diff that _deletes_ the pull's
lines, and popping it back onto that same `HEAD` re-applies the deletion **cleanly, with no conflict**, silently
reverting the pull. (Pop the other way — stash first, then `HEAD` advances — and git runs a 3-way merge, so the pull's
lines survive or raise a conflict instead; the silent case needs the staleness to exist _at stash time_. Reproduced both
ways.) Moving a capture started in `main`'s working tree onto its own branch this way left `AGENTS.md` ~380 lines short,
and only the staged diff's size (`+31/-409`, PR #336, closed unmerged) revealed it — a content scan missed it entirely.
So check a staged diff's insertions/deletions ratio (`git diff --cached --numstat`) before committing, and treat a large
deletion count on a file the change only appends to as a truncation, not a tidy-up. The workspace's own capture-branch
rule exists to keep this from arising at all: start the capture on its branch, not in `main`'s working tree. captured:
capture-stash-stale-copy

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
  **duplicated** change dir. The same command also writes the delta → main sync into `openspec/specs/**` — a second,
  separate staged output, so `git add -A openspec/changes` leaves the spec edits unstaged and a commit of the move alone
  ships the archive without its sync (a change arrived on `main` exactly that way, the sync recovered as a follow-up
  PR); the pre-commit guard now refuses exactly that commit. Stage `openspec/specs` too and inspect the staged set
  (`git diff --cached --name-only` against `git status` — an ` M openspec/specs/...` line is unstaged) before
  committing; format the change-dir markdown **before** the archive (`build.gradle.kts` excludes
  `openspec/changes/archive/**` from the markdown target). A hand-run `git mv` is a different trap: it stages the
  **already-committed** content, so a file carrying an uncommitted edit (typically the `tasks.md` tick) shows `RM`, and
  a plain `git commit` records the pre-edit content. `openspec archive` also prompts for confirmation when it would
  update specs, and a non-interactive agent shell cannot answer (`no answer could be read from stdin`) — run it as
  `openspec archive <change> --yes`; `--skip-specs` (which would skip the sync) and `--no-validate` (which would skip
  the validation) are not the remedy. captured: sync-audit-spec-findings-2026-10-04 (#497) captured:
  cover-the-included-builds-pins (#594)

- **Programmatic edits to a structured file — markdown or source — can silently drop or misplace structure; verify at
  the granularity you edited.** Reordering the README with a Python boundary script dropped the entire "Getting Started"
  section: the move used `lines[ends['Development Workflow']:]`, which starts at the _next_ section and skips the block
  sitting between the two, and only a follow-up grep for `## Getting Started` caught the loss. After any script-driven
  move/rewrite of a markdown file, grep for each expected heading (or diff the heading list before/after) before
  reporting done; for a docs reorder, prefer the edit tool over a hand-rolled reordering script. The same class bites at
  the edit tool's anchor: an edit anchored on a neighbouring item's line is applied at the neighbour — the tool's
  uniqueness check is satisfied by any unique line — so the replacement is written there while the item meant to change
  keeps its old text; anchor on the text being changed. Token and table granularity bite the same way: a file-wide
  substring replacement rewrites every occurrence that legitimately shares the value, so scope it to the block that owns
  the value or assert its exact intended match count first, and a row keyed on its table's column (pipe) count lands in
  any sibling table sharing that count, so key it on its identifying cell and re-check every edited table's column
  count. The same class bites at item granularity: the `retro-mark-captured-rules` provenance sweep derived each rule's
  line range with a boundary heuristic that treated a top-level plain `- Text` bullet directly after a bold-lead rule as
  the rule's continuation. At the time, three such adjacencies existed in `AGENTS.md` (`A buildpack pin is verified…`,
  `palantir-java-format does not manage imports`, `A dependency a module does not use…`), and the second rule's range
  ran through the eight plain bullets behind it — so both the `git log -L` range an origin was read from and the line
  the `captured:` marker was appended to belonged to the wrong item, and the marker landed on the following bullet.
  Derive an item's end from the list's own structure (a wrapped continuation is indented; a `- ` at column zero starts a
  new item, but a heading at column zero ends the list — a search for the next `- ` otherwise crosses the section
  boundary) and have any script that ranges over or appends to items assert that boundary itself — the marker-placement
  rule above states what the pre-commit guard enforces — before trusting the result. captured: retro-mark-captured-rules
  captured: add-a-fresh-session-trigger captured: optimize-agents-auditor-for-instruction-budget captured:
  convert-remaining-gotchas-rules

- **The same range-boundary hazard bites source:** removing three tasks' private comparators with a range running from
  the first deleted member to the end of the file took each class's **closing brace** (the members were last in the
  body), failing three compiles, and orphaned a KDoc above the deleted region — which compiled clean and only a reviewer
  saw. Derive a deletion's end from the member boundaries, and after removing members confirm each surviving KDoc still
  attaches to a declaration: a compile catches the lost brace, never the orphan. captured:
  test-build-logic-rules-and-unify-version-comparison (#306)

- **A formatter-owned file is re-wrapped, so a whitespace-tolerant substitution — a normalization or `\s*`-style pattern
  — silently matches the wrong span or rebuilds it unformatted.** The edit no-opped or mangled several times across one
  arc: a workflow step inserted at the wrong YAML indentation (a parse error), a README bullet reported as exactly one
  match that never landed, a `body=` line left 198 characters long, and a prompt line replaced at the wrong indentation.
  Locate the target by line index in the content you just read — or match one full line verbatim including its
  indentation — and re-read the result. captured: notify-owner-from-the-audit-report (#327)

- **An AST codemod is a further mode: re-printing the parsed tree (`unit.toString()`) re-serializes the whole file** —
  stripping blank lines and reflowing untouched code — so edit the original text at the AST-located range (or use
  `LexicalPreservingPrinter`), and bound the replacement by the next token's start, since JavaParser's `Range.end` is
  inclusive (a range ending on the type's last character leaves it, `LogFileData` → `vala`). Derive a skip from the
  transformation's actual constraint, not a blanket proxy: only `++`/`--` and assignment reassign a variable (`!x` is a
  `UnaryExpr` but does not), and a declaration annotation (`@SuppressWarnings("unchecked")`) need not force a skip when
  a text edit preserves it. Verify the codemod's decisions rather than trusting the compile — a `var` emitted where
  `val` was intended compiles, so a green build does not show the sweep conformed. captured: adopt-lombok-val-convention

- **An edit to wrapped prose can mangle the clause around the insertion when its replacement reflows into the
  neighbouring text, and patching the damage with further span edits re-mangles it each round; replace the whole unit in
  one edit, re-derived from the site's own subject, and re-read the sentence.** captured: verify-unique-review-rounds

- **A write to an already-occupied path replaces the file silently: check the path before creating a "new" file.**
  Before writing a file you believe is new, check the path — `test -e <path>` (or a read) proves it exists,
  `git ls-files <path>` whether it is tracked — and when it exists, merge the addition into it rather than re-creating
  the file from its new-test shape. The loss happens at write time, so the `git add <dir>` staging gotcha does not cover
  it. captured: keep-sse-stream-alive (#301)

  evidence: git-and-repository-workflow

- **Work a change surfaces is parked durably — a PR body is not a record.** WHEN a change records an external state
  change that implies work, or names a companion change it will not deliver, THEN park that work in `docs/ideas.md`
  under a dated `## YYYY-MM-DD` section with its trailing status tag (the tag's wording and disposition are the
  docs-refresh convention's), or record it as a task in the change. A PR body is squashed and a change dir is archived,
  and no tool reads either, so work named only there is lost. Verify by grepping `docs/ideas.md` for the parked text
  before reporting the change done. captured: cut-review-loop-rounds

- **A change merged without its archive is incomplete — do not merge the implementation PR and defer the archive.** The
  "one PR per change" rule puts the archive commit in the _same_ PR before merge. WHEN the merge completes, verify the
  change dir is archived — the live dir is gone (`test ! -d openspec/changes/<change>`) and the dated archive exists
  (`git ls-files 'openspec/changes/archive/**' | grep <change>`). If it is not archived, fix it on its own branch and PR
  — never commit it to local `main` and `git push origin main`, which the branch ruleset rejects and forces a
  branch-then-reset dance.

  evidence: git-and-repository-workflow

- **`git add <dir>` / `git add -A` can sweep untracked generated artifacts into the commit — inspect the staged set
  first.** A tool that emits files beside sources (a Python script's `scripts/__pycache__/*.pyc`, a test/build run's
  output) leaves them untracked; a directory-wide `git add` stages them silently, so the commit carries files the change
  never intended. Stage explicit paths, add a `.gitignore` entry for the artifact, and check
  `git diff --cached --name-only` (or `git status`) before committing. Staging is a snapshot, not a view of the working
  tree: an edit made after the last `git add` sits unstaged, so `git diff --cached` can look complete while the commit
  ships the older copy — check `git status` for a path listed under both staged and unstaged changes (or `git diff` for
  unstaged edits) before committing. A pre-commit guard (`scripts/git-hooks/pre-commit`, activated by
  `scripts/install-git-hooks.sh`) enforces this inspection mechanically: it refuses a commit whose staged set fails the
  formatter check, force-stages a generated artifact, stages a path and then edits it, carries a merge conflict marker,
  misplaces a `captured:` marker, or stages an `openspec/changes/archive/**` change dir while leaving its
  `openspec/specs/**` sync unstaged.

  evidence: git-and-repository-workflow

- **Never chain an edit to a commit — or trust an edit script's own report — without reading the edit's result: gate the
  commit on a content check, not on the edit command's exit status.** While implementing the `concise-agent-reports`
  change, an anchor assertion in the edit script failed (Spotless had re-wrapped the text), so the edit no-opped — and
  the next command in the shell sequence committed anyway, an unfixed state caught only by reading `git show --stat`,
  not the commit message or the exit codes (an assertion that fails does not by itself stop a following command unless
  the chain is gated). Inspect the staged set before committing — the `git add <dir>` gotcha above owns that files check
  — and read the diff's _content_, not just its file list, since a names-only check cannot see an edit that no-opped. An
  anchor read before a formatter run is stale the moment the formatter rewrites the file (`*italic*` → `_italic_`,
  rewrapping) — take the anchor from a read that follows `spotlessApply`, or re-read the file before reusing a saved
  one; make a scripted replacement fail loud on a stale anchor — assert the anchor's occurrence count before replacing,
  since `str.replace` returns the input unchanged when it does not match. The same change also showed why a multi-file
  change belongs in the apply workflow, not an ad-hoc edit script: hand-editing the six agent definitions with `python`
  `replace` scripts left five of six files edited while `architecture-auditor.md` was untouched and still reported
  edited, and duplicated a line in `lesson-capture.md` — the script's "edited" line is no per-file evidence, while
  `openspec-apply-change`'s per-task edits make a skipped file visible. A script that batches its edits and writes once
  is a third mode: an assert that aborts on a later edit discards every earlier edit in the same script, because none
  reached the single write — a `tasks.md` script applied 11 checkbox ticks and two rewording replacements, hit a stale
  anchor on the last and `sys.exit(1)`'d before its one `write_text`, and all 11 ticks were lost (only a later review
  caught the file still unticked). Write each edit as it succeeds (or split the batch into per-edit scripts) and verify
  the batch's full result afterward, rather than trusting a script that writes once at the end. captured:
  make-captured-rules-traceable (#279) captured: track-review-session-continuity

### Build, Gradle & dependency management

- **A build property's name is shared across the module's tasks — grep the plugin's existing `gradleProperty(...)`
  consumers before forwarding a new `-P<name>`.** `load-tests/build.gradle.kts` forwards its whitelisted properties into
  the Gatling task's `systemProperties`, but the same module's `kneeFinder` task reads `-Pknee` as its **output-file
  path** — so the knee-rate property had to be `kneeRate`, since a reused `knee` would forward a path where the
  simulation parses an integer. A JavaExec task runs with the module's project directory as its working directory, so a
  relative `-Plog`/`-Precords` resolves under `load-tests/`, not the repository root — hand-run one with the absolute
  path the wrapper passes (`$ROOT/...`). captured: check-load-test-drift

- **The configuration cache is enabled (`org.gradle.configuration-cache=true`); a task action must not reference
  anything belonging to the Gradle script** — `project`, `gradle`, `rootProject`, `providers`, a script-level
  `val`/`fun`, the script's own `this` — because serializing the captured **script object** fails the store; compute
  what the action needs into a `val` local to the configuration lambda (a class/object reference is safe). It needed the
  gradle-helm-plugin at **3.2.0** (its tasks captured a `Task`/`Project` through 3.1.2 —
  build-extensions-oss/gradle-helm-plugin#140, closed), that hoist at each task action that had it (the workflow-lint,
  dependency-security, frontend npm, load-tests, and compose tasks — whose `onlyIf` also read `gradle.startParameter`
  and a `rootProject.extra` handshake, now `composeScheduleState`), and `notCompatibleWithConfigurationCache` on
  `:load-tests:gatlingRecorder`, whose third-party task type holds a `Configuration`/`SourceSet` no hoist can fix (the
  plugin repository disables issues and discussions, so an upstream fix must arrive as a PR). Sweep such a change by
  **executing** every task a workflow invokes, not by realizing the graph: `./gradlew tasks --all`'s set never runs a
  task action, so a task no `check` graph requests — `upstreamReferences`, invoked only by its own workflow — escapes
  every sweep the build itself can run, and its store defect surfaces only in CI, on a schedule, where no gate sees it
  (#581 missed it for a week). Run each such task with the cache enabled and require a stored entry, then a reused one.
  A plugin task type is one failure shape (it cannot store); a third-party task whose `onlyIf` reads build state is
  another: it stores cleanly and then silently skips, because the cache resolves the predicate against the state
  captured at store time. Compare each such task's _outcome_ (ran / skipped) with and without the cache on a clean
  checkout, not only that the graph stores; and neutralize such a gate with `setOnlyIf { true }`, since a bare
  `onlyIf { true }` only appends a spec that is ANDed with the plugin's and leaves it in force (the helm plugin's
  chart-dependency skip is the worked case — see the Chart validation bullet above). captured:
  fix-upstream-references-config-cache

- **IF a Gradle run under `--warning-mode all` emits a gradle-helm-plugin deprecation or a `Configuration.visible`
  warning, THEN treat it as upstream and leave it: there is no repo-side fix.** gradle-helm-plugin 3.2.0 still calls the
  deprecated `Project.getProperties()` (a hard error in Gradle 10); tracked upstream as
  build-extensions-oss/gradle-helm-plugin#145, so bump the plugin when a fixed release lands. The
  `Configuration.visible` deprecation (`setVisible(boolean)`, removed in Gradle 11) is emitted by several third-party
  plugins (helm, errorprone, SpotBugs, Spring Boot, protobuf) and is likewise plugin-side. Do not add a local workaround
  for either.

- **A task whose verdict derives from wall-clock time or external state must not declare an output Gradle can re-use — a
  cached UP-TO-DATE run serves a stale verdict.** `npmAudit` first declared `outputs.file(...)`, so a re-run was
  UP-TO-DATE and its `doLast` verdict — including the `LocalDate.now()` suppression-expiry check — was never re-derived:
  a cached pass could outlive a suppression's expiry. Drop the `outputs.file` declaration (matching the sibling
  `npmOutdated` task) so the verdict re-runs every invocation; declare `inputs` (the suppression file) without declaring
  an output the task does not produce as an artifact. Any verdict task whose result changes without its inputs changing
  — a date check, a live registry query, a remote state read — takes the same treatment. Verify by running the task
  twice with nothing changed: the second run must re-execute, not report UP-TO-DATE. captured:
  suppress-braces-npm-advisory

- **A fixable npm advisory in a transitive that a dev dependency pins exactly is forced with an `overrides` entry — not
  npm's suggested fix.** npm's offered fix is a _breaking_ downgrade of the pinning package (`eslint-plugin-boundaries`
  7.2.0 → 5.4.0, breaking the FSD boundary lint), because the plugin and `@boundaries/elements` pin
  `handlebars: "4.7.9"` exactly; `overrides` forces the patched `4.7.10` under them instead. A suppression
  (`npm-audit-ignores.json`) is for an advisory with no fix — the `dependency-security` spec owns that policy — and
  unlike one the override carries no expiry, so park its re-check in `docs/ideas.md` and derive the retrigger from the
  resolved graph: every package that pins the vulnerable version must bump before it is dropped. Verify the override
  took effect with `npm ls <pkg>` (the patched version resolves). captured: fix-dependency-security-findings

- **A build tool's own vulnerable transitive does not reach the platform BOM, and a tool configuration that rejects a
  platform project is aligned with a dependency constraint instead.** The platform's `constraints` reach the
  compile/runtime classpaths, not Gradle's tool configurations: `errorprone(platform(project(":platform")))` works (its
  `annotationProcessor` classpath extends it), while `checkstyle(platform(project(":platform")))` drops the checkstyle
  tool and fails `checkstyleMain` with `ClassNotFoundException: CheckstyleAntTask` — constrain that one with
  `constraints { checkstyle(libs.guava) }`. A scan names the coordinate, not the configuration carrying it, so derive
  the owning configuration from the graph (`./gradlew :<module>:dependencies --configuration <config>`) before choosing
  the fix. A plain `compileJava` never exercises the tool configuration — only the tool task does — so verify the fix by
  running the tool task (e.g. `./gradlew :<module>:checkstyleMain`), not by compiling. captured:
  fix-new-security-advisories

- **Custom Gradle test suites (`componentTest`, `integrationTest`, `e2eTest`) do not inherit the project's
  `implementation`-only dependencies on their compile classpath** — the default `test` suite does, since its classpath
  extends `main`'s, while a custom suite receives them only at runtime through its `project()` dependency — so each
  suite re-declares what it needs to compile (client component suites duplicate axon/opensearch/resilience4j deps,
  `showcase-query-proto` must be listed explicitly, and a suite compiling against a `@Mapper`-annotated main class needs
  `libs.mapstruct`, or javac warns that the annotation's `ReportingPolicy` constant is unknown). A suite can be
  referenced in `shouldRunAfter(...)` only when bound as a `val` (e.g.
  `val integrationTest = suites.register<JvmTestSuite>("integrationTest")`). Source sets are siblings — `src/test/java`
  cannot see a type declared in `src/gatling/java`, while both see `main` — so a pure-logic type a unit test and a
  custom source set both need belongs in the module's `main` source set. Verify a suite's classpath by compiling its
  source set, which surfaces a missing dependency directly. captured: check-load-test-drift captured:
  fix-mapstruct-suite-warning

- **A dependency a module does not use can still be load-bearing — narrowing a `project(...)` dependency can break a
  downstream `compileJava`, and removing a starter drops the modules it transitively provided.** Before removing or
  narrowing a dependency, grep the consumers' sources for what they actually import and compile the affected modules; an
  unused direct dependency may be carrying the transitive one a consumer's main source needs. Worked case:
  `showcase-query-api` held `api(project(":showcase-command-api"))` only for `showcase.identifier.KSUID`; narrowing it
  to `api(project(":showcase-identifier-extension"))` (`narrow-query-api-dependency`) broke `:showcase-query-client`'s
  main compile, because `ShowcaseQueryClientProperties` imports `org.hibernate.validator.constraints.URL` and had been
  receiving it through `command-api`'s `api(libs.hibernate.validator)` — that consumer now declares it. The same holds
  for an external starter: removing `spring-boot-starter-webflux` from `showcase-query-client` silently removed the
  `jackson-datatype-jsr310` it had provided, and `JacksonSerializer.defaultSerializer()` failed at test-class init with
  `NoClassDefFoundError: JavaTimeModule` — name every module a removed starter was providing, declare it explicitly
  (`jackson-databind`, `jackson-datatype-jsr310`), and run the module's tests, not only `compileJava`. captured:
  grpc-query-transport

- **`./gradlew check -PskipITs` alone fails the coverage gate — the Docker-free check also passes
  `-Pcoverage.gate.enabled=false`.** `-PskipITs` excludes `integrationTest` from the jacoco exec data, so
  `jacocoTestCoverageVerification` verifies against a figure below the 0.80 baseline and fails; the gate is added to
  `check` only when `coverage.gate.enabled` is not `false` (`code-coverage-conventions.gradle.kts`). The PR CI gate is
  exactly `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` (see Continuous Integration) — run that for a local
  Docker-free check, not the bare `-PskipITs` form. That form disables the coverage gate exactly as the PR gate does, so
  it cannot catch a coverage regression: when a change adds production code to a gated module, run that module's `check`
  with the gate enabled before merging — Docker-free with `-PskipITs` for a module with no integration tests
  (`./gradlew :showcase-query-proto:check -PskipITs`), or the full `./gradlew check` — since the drop otherwise merges
  green and reddens the push-to-main full `check` (`grpc-query-transport` (#514) added
  `QueryMessageMapper.messageToResponse`/`payloadFromResponse` with no module test, `showcase-query-proto` fell to 0.60
  against 0.80, and every full `check` since failed until a component test covered them). The same Docker-free form also
  drops the `integrationTest` and `e2eTest` classpaths (`check` never includes `e2eTest`), so a resolution-affecting
  change — a dependency, repository, or plugin declaration, or a BOM — compiles them explicitly
  (`./gradlew compileIntegrationTestJava compileE2eTestJava --refresh-dependencies`) and names the tiers its resolution
  claim covers, rather than resting that claim on the Docker-free check. captured: fix-main-ci-regressions captured:
  remove-unrecorded-redgate-repository

- **Bumping the Gradle wrapper needs two `wrapper` runs with `--distribution-type all`.** The first run only rewrites
  `distributionUrl`; the second, executing under the new version, regenerates `gradle-wrapper.jar`/`gradlew`/
  `gradlew.bat`. A single run leaves the jar and scripts at the old version, and a run without the flag flips the repo's
  `-all` distribution to `-bin` — both silently. `./gradlew --version` cannot see either (it reports the version from
  `distributionUrl`), so verify by diffing all four wrapper files (`gradle/wrapper/gradle-wrapper.properties`,
  `gradle/wrapper/gradle-wrapper.jar`, `gradlew`, `gradlew.bat`) and confirming `distributionUrl` still ends `-all.zip`;
  if only `gradle-wrapper.properties` changed, the bump is incomplete. Keep `/gradle-update`'s two-step invocation.
  captured: bump-gradle-and-web-ui-dependencies

### Evidence and search

- **A glob or filter that matches nothing is vacuous, not clean.** An audit command told the agent to run "a manual
  120-character check for `.opencode/*.md`" — a glob matching **no file**, since the markdown lives in
  `.opencode/agent/` and `.opencode/commands/`; it shipped that way because no gate reads a glob written in prose. The
  first fix over-corrected to `.opencode/**/*.md`, which also matches `node_modules/`, the generator-written `opsx-*`
  commands and `openspec-*` skills, and the vendored `axon4to5-*` skills — so "clean" was unachievable. Expand a glob
  once (`ls <glob>`) before trusting it: a vacuous match fails silently and a recursive one over-matches generated or
  vendored files. The same failure comes from a name pattern that truncates: a module-inventory diff written
  `showcase-[a-z-]+` dropped the digit in `showcase-resilience4j-extension`, so the module read as _absent_ and the
  audit nearly opened a change to "restore" an entry that was present all along — a pattern that silently under-matches
  reports a false absence, which is worse than a false positive because it manufactures a defect. A glob can also
  under-match across tools: the build's `**` matches zero or more directory levels, while a plain
  `git ls-files '<dir>/**/*.md'` requires at least one and silently omits a file directly under `<dir>`
  (`.github/PULL_REQUEST_TEMPLATE.md`) — reproduced, 1 file vs 2 — because git treats `**` specially only under
  `:(glob)`. Reproduce a target's file set with `:(glob)` magic or a Gradle-native listing, and prove the scan returns a
  known file from each directory level before trusting its total. captured: reconcile-readme-agents-architecture

- **A search that under-matches returns a plausible empty or partial result.** A grep for `permissions:` matched one
  job-level block while the six top-level ones went unreported — a real result that looked complete, so nothing flags it
  without a completeness check (unlike a vacuous glob, which fails silently); match the key at any indentation
  (`^[[:space:]]*<key>:`, or strip leading whitespace) and count the hits. The same root hides a wrapped prose phrase: a
  grep for a multi-word phrase that `spotlessApply` split across a line break returns zero though the content is
  present, and that zero is not evidence the edit is absent — three such false negatives in one session led to a tool
  being wrongly accused of reverting an edit (a Purpose refresh read as regenerated by `openspec archive`, when it had
  survived and only a token grep showed so). Grep a token the formatter cannot break (`accretion`, `captured:`) or
  flatten whitespace before matching, and confirm the search can hit a known positive before trusting an empty or
  partial result. A check matching a subject's output for a name must also anchor to the shape the subject emits, not
  the bare name: an unanchored ` java` match false-passed on a detail line (`Note:  java  …`) though `java` was never
  reported — anchor to the report line's own form (`^  <key> `) and prove the pattern against the subject's real output.
  A parsed command's output is a third under-match source: `git ls-files -s` C-quotes any path holding non-ASCII bytes
  (`"caf\303\251.sh"`, `core.quotePath` defaulting on), so a `.endswith(".sh")` filter silently skips it and the check
  reports a clean set it never examined — pass `-z` to any git command whose paths you parse (NUL-delimited and
  unquoted) and keep a positive-control test with a non-ASCII path. Deriving the record shape is not enough: a `-z` fix
  that must skip a field git emits _beside_ the path (a rename's old name) placed the skip **inside** the branch that
  tests the record, so the neighbour state that never enters it — a clean rename, `R ` with no worktree edit — leaked
  the field and yielded a bogus path (`['txt']` from `git mv RM.txt New.txt`), which would have blocked a legitimate
  commit. Put a field-skip **before** the branch test, and derive the fixture from the state space (a clean rename and
  an edited one are two cases), not from the one case the design's scratch fixture happened to model — a test mirroring
  only the case you were thinking about passes against code that is wrong for its neighbour. A fourth under-match source
  is the source's own spelling of the token: a version-catalog alias reaches build scripts only as its accessor
  (`hibernate-validator` → `libs.hibernate.validator`, dots for dashes), so a coordinate grep exits 1 while several
  modules reference it, and no positive instance of the searched spelling exists to anchor a control — search the
  accessor form too. captured: address-snyk-advisories-2026-10-16 captured: make-captured-rules-traceable (#279)
  captured: add-conflict-marker-and-executable-bit-checks captured: quote-safe-git-path-parsing captured:
  declare-probes-with-shape-and-behavioural-checks

- **A derivation whose failure maps onto the benign outcome reads as a clean result — never map its failure there.**
  `buildpackUpdates` (like `helmUpdates`) maps a failed lookup to "no update", so a wrongly-built URL or renamed
  repository reads as current — and so does a parse pattern that cannot match the provider's real body. Verify a new or
  changed check by temporarily pinning a known-older version, confirming the report shows `<name>: <old> -> <latest>`,
  then reverting the pin (the `buildpackUpdates` lookup was proved this way with `0.1.0`) — and unit-test the parse
  pattern against a **real provider response body**, not a hand-written string: the GitHub releases API pretty-prints
  (its `tag_name` key has a space after the colon) while npm compacts, so the no-space pattern never matched and left
  `HelmUpdatesTask`'s Helm CLI silently uncompared (its pin happened to equal the live release, which is why nothing
  surfaced). The same silence hides in a workflow's report **extraction**: `dependency-updates.yml`'s `awk` opened on
  `The following dependencies have newer versions:` — a header the pinned `gradle-versions-plugin` never emits (its
  reporter labels the section from the task's `revision` strategy, which the convention had left at the `milestone`
  default) — so the issue's catalog section was always empty and 28 stable updates went unreported, reading exactly like
  "no updates". Ground the extraction pattern in the pinned tool's emitted output (its reporter, or a real report) and
  re-ground it whenever the tool or its report configuration (the task's `revision`, say) changes; the sibling-diff
  catches a diverged copy (see the `toolingUpdates` bullet), while the positive control above proves the pattern matches
  the tool's output. A sibling task that predates your change is not exempt from that positive control — grep the other
  update tasks for the same pattern. A GitHub-backed lookup also sends `GH_TOKEN`/`GITHUB_TOKEN` when CI provides one
  (attached to GitHub requests only, never to the npm one): an anonymous lookup is rate limited, and the throttled body
  lands exactly where "current" does. Filter the candidates before selecting the newest: a check that takes the highest
  release first reports nothing when its filter rejects that release, hiding the acceptable one below it
  (`BuildLogicPins.latestAccepted` filters with `shouldReject` before `Versions.highest`, so a pin at `3.5.15` still
  reports `3.5.16` while `4.1.1` is a deferred major). captured: unify-tooling-currency-checks (#304) captured:
  fix-dependency-updates-catalog-extraction captured: cover-the-included-builds-pins

- **A verdict echo is not a check.** While fixing `scripts/experience-analysis.sh`, the 120-character check printed the
  offending line and the next command echoed "(script 120 clean)" regardless — the log carried the defect and the
  summary contradicted it. Let the exit status carry the verdict — wrap the `Formatting` convention's recipe so a line
  over the limit is non-zero — or read the output before writing the sentence; never emit a canned "clean" you did not
  derive from that run. The wrapper still fails open on a missing file, which is why the check must be seen to hit a
  known positive before its clean run means anything.

- **A clean run from the wrong resolution context — a directory, a point in the lifecycle, or a stand-in for the
  artifact — is not a reproduction.** TypeScript's automatic inclusion of `@types` packages walks up from the tsconfig's
  directory (the then-`.opencode/tsconfig.json`, retired with the plugin below) — or the current working directory when
  no tsconfig is used — not from the directory of the file being checked, so a check can pass from inside the package
  while failing from the root its consumer resolves from (here, the editor's project root). A `review-quick` round
  proposed the simpler fix ("add `@types/node`; a tsconfig isn't needed"), which held only in the passing directory. Run
  a reproduction from the context that fails — the directory whose config the failing tool reads, not the artifact's own
  directory — and treat a different error as its own signal. The binary is part of that context: a control that installs
  one major and then invokes it by a bare command name runs whatever the host has instead — the host's V2 `opencode`
  accepts the V2-only key the V1 probe rejects (exit 0), so the control would prove the opposite — so run the installed
  binary by absolute path, isolate its `HOME` so it does not read the host's global config, and assert its version.
  captured: probe-opencode-config-read-path captured: fix-tmpdir-plugin-types (#299) The context is temporal as well as
  spatial: a plugin hook or a config read runs at process init, so a standalone execution proves what the artifact emits
  but never that its inputs exist by then. The now-retired `.opencode/plugin/grant-cli-config-dirs.ts` grant was proved
  with a `bun` run (every grant emitted), yet in the cloud run the agent installed `openspec` mid-run, after init, so
  the resolved-path grant could not exist and the run hung — the config wildcard that replaced it removes the init-time
  dependency. Exercise the artifact at the lifecycle point it runs in (check the workflow's step order against the
  artifact's run point), not only its own invocation. The context is also the retained state: clearing a module's
  `build/` is not the clean-checkout condition, because a tool's own cache and the project's `.gradle/` survive it —
  `rm -rf helm/chart/build` alone still let the chart-dependency task run, and only clearing the plugin's cache under
  `.gradle/` reproduced the skip CI saw — so clear the caches outside `build/` before concluding a failure does not
  reproduce. The context is also the input's own content: a probe against a stand-in evidences the stand-in, so state
  the fixture's measured size beside the claim and defer the untested remainder to a task. captured:
  fix-chart-dependency-fetch captured: grant-openspec-global-access-in-cloud (#325) captured:
  load-stage-workflow-instructions-on-demand

### Writing checks and tests that can actually fail

- **A check is evidence only once it has been shown to fail.** A clean run, an empty result, or an unmoved control
  proves nothing until the check hits a known positive.
- **A gate's control is a committed test, not a task.** A gate whose logic lives in a rules object carries its known-bad
  and known-good cases in that object's `*RulesTests`; an inline gate carries them in its consuming module, since a
  checklist task asserting the control ran can be ticked without it running.
- **Before you write or change a test, a check, or a gate, load the `check-and-test-authoring` skill.** It records each
  way such a check passes without verifying anything — an unreachable bad state, a lenient parser, a control that misses
  the surface the check reads, an input set narrower than what it reads — with the confirmation and incident evidence
  for each. Loading it at that trigger is mandatory, not reference.

### Testing, measurement & load verification

- **A derived measurement may report only what it measured — pin its unit to a countable event, never let a fallback be
  indistinguishable from the real thing, and never let a check's failure gate nothing: label a reporting fallback as
  unmeasured, fail an unrecognized input rather than substituting a valid default, and assert the name a check's failure
  is logged under.** A configured rate's unit is a countable event, and the derivation must count exactly that event: a
  `rate` the delta spec first defined in requests/s was injected as users/s while each read user issued two requests, so
  the knee and the operating point were computed and fed back in the wrong unit — fixed by defining one unit as a read
  iteration or a write-lifecycle completion and counting `FetchShowcases` + `RemoveShowcase` in `KneeFinder`. A fallback
  must be distinguishable from a measured value: `KneeFinder` falls back to the calibration ceiling when no sustained
  departure is found, and the report once named that "below the knee" — carry an explicit `measured` flag and its own
  label, so a report never presents a default as a measurement. The input side is the same rule reversed: a `default`
  that maps an unrecognized value onto a valid one makes a mistyped input pass as a real run (`ShowcaseSimulation`'s
  profile switch fell back to `smoke`, so a mistyped `-Pprofile` read as a pass), so fail on the unknown value, naming
  it — except a dispatch in a static initializer, where a throw surfaces as `ExceptionInInitializerError` rather than
  the named exception. A deterministic fail-fast is also the cheap negative control an error-path verification needs:
  `PROFILE=bogus` fails in seconds and attributes the exit to the profile alone, where a failing supported profile
  needed a 30–40-minute asserting run. A Gatling check's outcome is a stats row named after the **check**, not its
  request (`logResponse(session, currentCheck.name, …)` records `showcaseEvent`, not `ShowcaseEvents`), so a
  per-request-name assertion never sees a check's `KO`: a stream that dies after its first event passes silently until
  the check's own name is asserted (`details("<checkName>").failedRequests().count().is(0L)`). `global()` does aggregate
  the check's row (a stalled `smoke` run failed `Global: count of failed events … actual : 1.0`); a name with no stats
  cannot be asserted at all — `ratio: 1` drops the write names and the per-name assertions fail with
  `Could not find stats matching assertion path`, so a read-only verification runs under an assertion-free profile
  (`calibrate`). Perturb the metric the assertion reads, not the check's condition. The same unit discipline applies at
  a **library's own API boundary**: verify the unit that API expects before feeding a value into it, and scale at the
  boundary — Vitest's coverage `thresholds` are **percentages (0–100)**, compared against `summary.pct` with
  `coverage < threshold`, so the web-UI gate keeps its baseline in the JVM's **fraction** form (`0.80`) and multiplies
  by 100 in `vite.config.ts`; a fraction passed straight through is always below any real coverage and the gate silently
  never fires. captured: rework-load-tests captured: close-load-test-knee-loop captured: tighten-load-test-streams
  captured: web-ui-coverage-gate

- **To attribute a runtime-cost effect to your change, measure a before/after pair on the same environment — a single
  run against the committed reference conflates every merge since that reference — and read the timer of the operation
  under test, not an upstream queue metric.** The `refresh-projection-writes` measurement toggled `Refresh.True` /
  `Refresh.False` on the projector's bulk, rebuilt and redeployed, and ran `./scripts/load-test-baseline.sh` once each:
  the read/write and `showcaseProjector.projectionLag` figures were flat while the projector's bulk-write latency rose
  4× (`showcaseProjector_successTimer_seconds`, mean 4.4 → 18.4 ms). `projectionLag` is recorded at Kafka ingest, before
  the write, so it cannot show write cost. Window-scope a retained series from **counter or histogram deltas over the
  measurement's own window** (`histogram_quantile` over `_bucket` for a percentile) — never from a `*_max` **gauge**,
  which a Micrometer `TimeWindowMax` decays over its own ring buffer (configured here by `METRICS_EXPIRY_ALL`), so its
  value can be a pre-window excursion and `increase()`/`rate()` over it is invalid; a bucket's `le` is the cumulative
  _below_ count, and `histogram_quantile` needs `le` series (on an in-flight gauge it returns nonsense). Take the window
  from the measurement itself — the profile's own injection span, not a reference's `recordedAt`. captured:
  refresh-projection-writes captured: attribute-load-test-tail

- **Do not write a Gatling stream's loop or retry condition as an EL string — use the typed session predicate; an EL
  condition can crash on the substituted value and KO the whole stream.** The write stream's
  `doWhileDuring("#{queryStatus} != 200", …)` crashed under a sustained run —
  `Condition evaluation crashed: Can't parse '200 != 200' into boolean … For input string: "200 != 200"` — KO-ing 194 of
  22,463 polls and tripping the failure assertions, while a committed record proved it timing-dependent (232,080 polls,
  0 failed, at 385 rps). Use `doWhileDuring(Function<Session, Boolean>, …)` (the shared `differs(attribute, expected)`);
  the source reads correct either way, so only a sustained run exposes it. captured: anchor-the-load-test-baseline

- **The gateway e2e suite needs the four service images present in the local Docker daemon.** The Gradle
  `:showcase-api-gateway:e2eTest` task declares the four `bootBuildImage` tasks (gateway, command, projection, query) as
  `dependsOn`, so running it through Gradle builds them. An IDE JUnit run does not fire `dependsOn`, so before running
  the `ShowcaseApiGatewayE2E` class directly, build them:
  `./gradlew bootBuildImage :showcase-command-service:bootBuildImage :showcase-projection-service:bootBuildImage :showcase-query-service:bootBuildImage`.
  Verify the images exist for the version declared in `gradle.properties`
  (`docker images aanbrn/axon-showcase-command-service` lists the local tags); a missing image fails the suite at
  container start.

- **`@Nested` test classes are incompatible with Spring Boot slice tests (`@WebFluxTest`/`@WebMvcTest`).** Nested
  classes load the full application context instead of the slice and fail on infrastructure beans (e.g. the gateway's
  JGroups `DistributedCommandBusProperties`). Keep slice-test classes flat (see `ShowcaseRestControllerCT`).

- **Testcontainers 2.0.5 moved `PostgreSQLContainer` from `org.testcontainers.containers` (now a deprecated shim) to the
  non-generic `org.testcontainers.postgresql.PostgreSQLContainer`.** Use the new import without the `<?>`/`<>` type
  arguments. The old import still compiles through the shim, so a green build does not confirm the migration — check the
  import.

### Deployment, Helm & Kubernetes

- **WHEN diagnosing a deployment issue, read the Helm chart and values files, not the live cluster alone.** The deployed
  resources are generated output of the chart (`helm/chart/src/main/helm/`; values `helm/values/*/values-*.yaml`), so a
  wrong live resource usually means a wrong source (or a stale apply), not a standalone "cluster problem." The chart
  also introduces k8s-specific concerns (namespaces, NetworkPolicies, KUBE_PING discovery) that do not exist under
  `bootRun`/`docker-compose`, so a bug can be invisible locally and only surface once deployed. Render what the source
  produces before (or alongside) inspecting live resources: `helm template` against the **packaged** chart at
  `helm/chart/build/helm/charts/axon-showcase` — the `helm/chart` module dir has no `Chart.yaml`, and the source at
  `helm/chart/src/main/helm` carries Gradle-filtered placeholders that `helm template` rejects. Two recent deployment
  bugs were chart bugs, not code bugs: the `kubernetes` EndpointSlice `lookup` namespace in the network policies
  (`fix-kube-ping-api-egress`) and the missing release-namespace declarations (`declare-axon-showcase-namespace`). **A
  deployment-only behavior is verified against the deployed signal, not the config** — a green `check` and a correct
  chart value prove the configuration, not the behavior. Observability is Kubernetes-only here, so prove a trace by
  reading what the deployed system emitted: the `traceparent` the browser really sent (`browser_network_request` with
  `part: request-headers`) and the exported trace from Tempo's API (`kubectl port-forward -n monitoring svc/tempo 3200`,
  `GET /api/traces/<trace-id>`, with the root server span parented at the browser's id). **`./setup-hosts.sh setup`
  cannot complete under an agent shell** (no TTY to answer `sudo`), so it stops at the `/etc/hosts` write. It prints the
  ingress LoadBalancer address before that: verify a hostname with a `Host:`-header curl against that address
  (`curl -H "Host: axon-showcase-grafana" http://<address>/api/health`) and hand the hosts write to the owner; record
  which half ran rather than ticking a task on the script's assumed completion. captured: propagate-ui-trace-context
  (#362) captured: expose-grafana-by-hostname

- **A containerized image build that fails in varying ways is host state until proven otherwise — check the container
  runtime's amd64 emulation before touching a pin.** Paketo `dockerBuildImage` failures with shifting signatures (a
  missing `io.buildpacks.buildpackage.metadata` label, an analyzer `panic: could not parse '0.12' as version`,
  `flate: closed writer`, a Go GC fault) point at the host, not the change. A pin decides which image `pack` pulls, so a
  host failure mimics a pin defect exactly; establish both before changing a version:
  1. Does the unmodified baseline fail the same way? Reproduce with the pre-bump pins (or on `main`); if it does, the
     change is not the cause.
  2. Is linux/amd64 emulation working? The build runs the amd64 buildpack lifecycle, which crashes under QEMU. Check the
     VM type and architecture (`colima status`, `colima list`) and the profile's Rosetta flag
     (`grep '^rosetta:' ~/.colima/default/colima.yaml`), then confirm it actually functions — a config can be stale —
     with `docker run --rm --platform linux/amd64 alpine uname -m` (must print `x86_64`). Only when the baseline passes
     and amd64 emulation works is the pin implicated. The worked case: colima's template enabled Rosetta, a macOS
     upgrade removed the Rosetta runtime, colima silently fell back to QEMU, and the amd64 lifecycle crashed; with
     Rosetta reinstalled the same pins built (the identical nginx digest that reported "could not find label" is the
     digest the successful build used). A surface that fails one run and passes the next supports no causal claim in
     either direction, so name the failures that are host state and leave a pin's fault an open question.
     `hold-back-nginx-buildpack` held `paketo-nginx` back at `1.2.0` because that episode's arm64 architecture symptom
     survived every host reset and is tracked as `paketo-buildpacks/nginx#1340`.

- **A buildpack pin is verified by running the image, not by a green build or a metadata label — a release can install a
  binary of the wrong architecture and still build cleanly.** `bump-paketo-builder-and-buildpacks` ticked its test plan
  on `dockerBuildImage` succeeding and on reading the built image's `io.buildpacks.build.metadata` label back (which
  named `paketo-buildpacks/nginx 1.2.1`), and the pin it shipped exits 127 on the development machine: the image is
  amd64 but its `nginx` is AArch64, while the build log says `Installing Nginx Server 1.31.5` on both architectures. Run
  the image after a buildpack or builder bump — `docker run`, `GET /`, and read the binary's architecture **from the
  buildpack's layer** (`/layers/paketo-buildpacks_nginx/nginx/sbin/nginx`), not the runtime `PATH` (a
  `file $(which nginx)` probe returns not-found there), decoding the ELF `e_machine` (`3e 00` = x86-64, `b7 00` =
  AArch64) — and be explicit about the platform when reasoning about a multi-arch artifact: the buildpack images are
  multi-arch, so a bare `docker pull` on an arm64 host resolves the host's own slice and cannot evidence a claim about
  the amd64 one (the image this pipeline produces is amd64-only, so a bare run of it is not the risk). The metadata
  label is doubly misleading: it names the _buildpack version_ the build used (correctly the pinned version) while the
  wrong-architecture binary comes from the resolved **buildpackage slice**, so a label reading `1.2.0` does not clear a
  `1.2.0` pin — the `address-2026-09-21-update-checks` A/B found the pin build cleanly and exit 127 when `pack` added it
  from the registry as an arm64 slice (`sha256:0d6fedc4…`), because the builder bundled a _different_ nginx version,
  while the matched pair (pin = the builder's bundled version) ran. **Establish that pair before choosing the pin, not
  after:** on a builder or buildpack bump, inspect the new builder's bundled versions
  (`pack builder inspect paketobuildpacks/builder-jammy-base:<version>`) and pick the buildpack pins from what it lists
  — a builder that no longer bundles the pinned version makes `pack` add it from the registry as an arm64 slice (the
  mismatch above). captured: bump-buildpacks-2026-10-05 captured: bump-buildpacks-2026-10-12

- **A buildpack's CNB id is not its Docker Hub repository — a registry lookup must target the repository, not the id.**
  The buildpacks are passed to `pack` as `paketo-buildpacks/nginx` (hyphen), but their Docker Hub repositories are
  `paketobuildpacks/nginx` (no hyphen); querying the tags API with the CNB id 404s, so `BuildpackUpdatesTask`'s check
  model carries `repository` separately from the display `name`. When adding a buildpack to the check, use its Docker
  Hub repository. Verify a new entry as in _A derivation whose failure maps onto the benign outcome reads as a clean
  result_. The same repositories also publish alias tags (`1.3`, `5.15`) alongside the full semver (`1.3.0`), which is
  why the buildpack check has two version operations rather than one: `Versions.isNewer` — the shared comparator all
  three update checks use — compares numerically with zero padding, so the two spellings of one release are equal and
  neither reports the other as an update, while `Versions.highest`, which only this check needs, keeps a longer-spelling
  tiebreak so the _reported_ tag is the canonical form rather than the alias.

### CI & GitHub Actions

- **The actionlint download script takes positional arguments (`version dir`), not `--dir`, and the target dir must
  already exist.** When installing actionlint in CI with `bash <(curl .../scripts/download-actionlint.bash)`, pass
  `latest "$RUNNER_TEMP/actionlint"` and `mkdir -p` the dir first — a `--dir` flag is rejected as an invalid version
  (the script exits 1 with its usage).

- **A changed-file check in a workflow step needs a two-dot diff, and a path-gated step must list its own definition.**
  `actions/checkout`'s default `fetch-depth: 1` leaves the pull-request head a grafted root with no merge base, so a
  three-dot `git diff --name-only FETCH_HEAD...HEAD -- <paths>` exits 128 (`fatal: FETCH_HEAD...HEAD: no merge base`;
  reproduced against a depth-1 clone) while the two-dot `git diff --name-only FETCH_HEAD HEAD -- <paths>` form works; it
  can list a file the base changed past the merge base (which merely runs the step), never fail. And a step gated on
  changed paths must list its own definition file among them, or the pull request that edits the step skips it.
  captured: probe-opencode-config-read-path

- **`gh pr list` does not support a `--since` flag; a `gh <x> list` command silently truncates at its default `--limit`;
  and a windowed git query is not the PR window.** Filter merged PRs by window with the search qualifier
  `gh pr list --state merged --search "merged:>=<YYYY-MM-DD>"` (an unknown flag like `--since` is rejected outright, and
  there is no date variant of `--merged`). This bit the `/retrospective` gather script
  (`scripts/experience-analysis.sh`), whose first version used `--since`; the proposal/tasks that described
  `--merged --since <window>` were corrected to the search form during implementation. A list command returns only its
  default page with no warning when more exists (`--limit` defaults to 30 for `pr` and `issue`, 20 for `run`), so the
  same script returned 30 rows for a window holding 249 merges until it gained an explicit `--limit 1000`. Any future
  "what shipped since X" automation must use the `--search "merged:>=..."` form **and** an explicit `--limit` sized to
  the corpus. Scope the window's git side with a time-explicit `since` (`--since="${SINCE}T00:00:00"`) — a bare
  date-only `--since` excludes commits before the wall-clock time of day on that date, so the count varies run to run
  (verified: 48 commits bare vs 65 at `T00:00:00`) — and never read a git window's start as the PR window's start: a
  squash-merged commit's date can differ from its PR's merge date (verified: `#494` merged 2026-10-04T22:25Z, committed
  2026-10-05), so a baseline at the raw git window's start is a PR late (`derive-retrospective-facts` first read 2672
  lines where the window's start is 2693). The same truncation is self-inflicted by an explicit slice: `head -1` on the
  `buildpackUpdates` report hid the second of its two lines, and the tasks file cited the first as though it were the
  whole report. Cite a command's full captured output before recording it as evidence or scoping a change from it — read
  the whole report file or CI run log, and take the flagged set from the tool's own findings list, not a `head`/`tail`
  slice, a `grep` filter, or a derived/narrative section of the output. A filtered read of the 2026-09-24 Snyk run's
  `--log-failed` output showed only four of the seven Jackson advisories, so the change was scoped Jackson 2-only and
  would have left the three Jackson 3 ones (fix `3.2.3`) unaddressed; the full log named both lines. captured:
  test-build-logic-rules-and-unify-version-comparison (#306) captured: bump-jackson-boms-for-snyk-findings captured:
  derive-retrospective-facts Confirm the file is the run's output: a build directory persists a tool's previous report,
  and `dependencyUpdates` writes per-project `partials/*.json` as each resolves and `report.txt` afterwards, so a run
  that did not regenerate leaves a stale aggregate — reading a stale `report.txt` twice made
  `align-dependency-report-version-authority` abandon a working change on the false conclusion that the plugin's rule
  could not reach the row. Cite the run-scoped output, or confirm the summary is newer than the run. captured:
  align-dependency-report-version-authority

  A slice of a list of _objects_ selects a subject, not only a shorter list: `head -1` on a `docker images` list picked
  another tag's stale build, whose `io.buildpacks.build.metadata` label named procfile `5.15.2` where the tag the run
  produced (`aanbrn/axon-showcase-web-ui:0.2.0-SNAPSHOT`) named the pin `5.15.4` — one repository's tags each carry
  their own build's metadata — and the false discrepancy reached the task record and returned as a review finding
  against a true `AGENTS.md` rule. Name a built image by the tag the declared version gives
  (`aanbrn/axon-showcase-web-ui:${project.version}`), never by list position. captured: bump-buildpacks-2026-10-16

- **`gh pr create --body` with Markdown can fail under zsh with `no matches found`** — an inline body containing
  `**bold**` (or other shell metacharacters/newlines) is subject to zsh's `nomatch` glob error
  (`zsh: no matches found: **...`). Write the body to a file and use `--body-file <file>` instead; it also sidesteps
  quoting and embedded-newline problems. Prefer `--body-file` for any multi-line PR or issue body. A body supplied this
  way also bypasses the repository's pull-request template (`--template` is rejected alongside `--body`/`--body-file`),
  so a line added to `.github/PULL_REQUEST_TEMPLATE.md` reaches an agent-opened PR only if the body file carries it —
  write the template's sections and any new line into the body. captured: make-the-capture-visible

- **An exit-code probe's status is inverted from the question it answers — run it inside an `if`, never bare under a
  step shell's `set -e`.** `git ls-remote --tags --exit-code origin "refs/tags/v<version>"` exits `0` when the ref
  **exists** and `2` when it does not, so a bare invocation under `set -euo pipefail` aborts the step on a **free** tag
  and continues on an **existing** one — the guard that should reject a taken tag passes it and rejects the good case.
  The release workflow writes `if git ls-remote --tags --exit-code … ; then echo exists; exit 1; fi`, which suppresses
  `set -e` for the probe and inverts the status to the intent. Before gating a workflow step on a command's exit status,
  probe that command's actual codes on both the hit and the miss (a taken ref and a free one) and place it in an `if`; a
  bare status-checking command in a `set -e` step is the inversion bug. captured: publish-github-releases

- **Checking CI status**: don't poll a PR build with an idle `sleep` loop — use `gh run watch <run-id> --exit-status`
  (or `gh pr checks <pr> --watch`), which blocks until the check finishes and exits non-zero on failure. When the run id
  isn't known, fetch it once via the GitHub MCP `pull_request_read` / `get_check_runs` (or `gh run list`), then
  `gh run watch` it — one blocking call, no manual polling. A docs/build change's `build` check typically completes in
  about a minute; check once shortly after pushing, then confirm green before archiving/merging. Verify the run itself
  (`gh run view <run-id> --json conclusion`), never a pipeline's exit code — `gh run watch … | tail -6; echo $?` reports
  `tail`'s, so a failed run reads as green. Idle sleep loops only waste time and add no information. captured:
  fix-timeout-load-flakes

- **A PR whose base advanced after its CI ran fails to merge with "Required status check 'build' is expected" and
  `mergeStateStatus` BEHIND.** When two PRs merge close together (a code PR followed by a docs PR), the second PR's
  branch is behind the new `main`; the `main-required-checks` ruleset demands the `build` check on the latest base, so
  the merge is blocked even though the branch's own CI is green. The error message does not say "update your branch" —
  BEHIND is the tell. Fix: `gh pr update-branch`, which re-runs CI against the updated base; merge only once that check
  is green.

- **A workflow tool pin belongs in the `toolingUpdates` check's declared list — it is the only thing that detects a
  release.** `dependencyUpdates` / `dependency-updates.yml` cover Gradle catalog coordinates (and, through the same
  workflow, the web UI's npm dependencies), `buildLogicUpdates` / `dependency-updates.yml` the Gradle plugin pins the
  included `build-logic` build resolves — invisible to the main build's report, and reached by the check's declared list
  of the aliases that build consumes — `helmUpdates` / `helm-updates.yml` the Helm CLI and pinned charts,
  `buildpackUpdates` / `buildpack-updates.yml` the Paketo builder and buildpacks, `toolingUpdates` /
  `tooling-updates.yml` the versions pinned in workflow files (the OpenSpec, Snyk, `pack`, and gitleaks CLIs), and
  Dependabot covers `uses:` action refs. Add a pin to that check's declared list when you add it to a workflow, and
  machine-assert the list's members against the file that declares them rather than trusting the list to be maintained
  by hand — a pattern is asserted to match exactly once (a renamed input fails the task rather than reading as current),
  while `buildLogicUpdates` asserts the same set-equality against `build-logic`'s own script (specified in
  `showcase/quality/dependency-management`) — since extending a list by hand one pin at a time is how the OpenSpec pin,
  then the `pack` CLI, was each missed in turn while the check did not exist. captured: cover-the-included-builds-pins A
  tool pinned in more than one workflow file takes one check entry per file: a check reads exactly one `workflowFile`,
  which must also be registered in `pinFiles`, and a file no entry names has its pin silently unchecked (the smoke's
  `pack` pin sat unlisted until it gained its own `pack-cli (deployment-smoke.yml)` entry) — name each entry per file
  (`pack-cli (e2e.yml)`, `pack-cli (release.yml)`, `pack-cli (deployment-smoke.yml)`), since the report names the check,
  not its file. captured: publish-images-to-ghcr The `/opsx-tool-update` command regenerates the instruction files after
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

### Formatting & IDE integration

- **IntelliJ's built-in formatter (its `Default` code style) disagrees with the Spotless format (palantir for Java,
  ktfmt for `.gradle.kts`).** The auto-reformat triggers (**Actions on Save → Reformat code / Optimize imports**, **Auto
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

- **Run `spotlessApply` after the _final_ write to a Spotless-owned file — ticking a checklist task is an edit too.** A
  `tasks.md` task was ticked ("`spotlessCheck` passes") _after_ the last `spotlessApply`; the re-wrapped prose broke
  Prettier, so the claimed gate actually failed and only the quick review caught it. The tick that claims a gate green
  is the edit that falsifies it — so never tick a gate task as the last act: tick it, then re-run `spotlessApply` and
  `spotlessCheck` before the file is committed. After any last edit to a Spotless-owned file (a TODO tick, a status
  note, `AGENTS.md` itself) re-run `spotlessApply` and `spotlessCheck` before reporting the gate green — do not trust a
  check that ran before the final edit. captured: suppress-web-ui-npm-majors

- **palantir-java-format does not manage imports**: since 2.47.0 the plugin only takes over **Reformat Code**, and
  `Optimize Imports` is always run by IDEA's native optimizer, governed by `.editorconfig` (the import layout
  `ij_java_imports_layout = $*,|,*` and `ij_java_use_single_class_imports=true` with the two on-demand counts at `999`).
  This is what stops IDEA collapsing to wildcard imports; without it, `spotlessApply` cannot auto-expand a wildcard
  (palantir never touches imports), so a wildcard must be expanded by hand or via Optimize Imports. The build gate is
  Spotless `forbidWildcardImports()` (fails on any `import x.*;`). A change to the code-style scheme requires an IDEA
  restart to take effect.

- **IntelliJ settings-XML component names are exact and easy to transpose — take them verbatim from an IDE-written file,
  not the intuitive name.** `scripts/ensure-idea-settings.py` writes the inspection-profile skeleton with
  `<component name="InspectionProjectProfileManager">`; the script it replaced had it transposed as
  `ProjectInspectionProfileManager`, which raises no error — IntelliJ simply ignores the profile, and only the
  fresh-clone path (no `.idea/inspectionProfiles/Project_Default.xml` yet) exposes it. When adding or editing a settings
  skeleton, copy each `<component>`/inspection name from a real IntelliJ-written file. The same applies at field level:
  a plugin's config field serializes under its `@OptionTag` name (Prettier's `filesPattern` is written as
  `myFilesPattern`), so derive each `<option>`/attribute from an IDE-written file or the bundled plugin jar
  (`…/plugins/<plugin>/lib/*.jar`), not from the field name or the UI label.

### Runtime code, libraries & Spring

- **A controller-local `@ExceptionHandler(HandlerMethodValidationException.class)` fires only when method validation is
  active for that controller — put a constraint (`@NotNull`) directly on the method parameter, not only `@Valid`.**
  Without it a body failure arrives as a `WebExchangeBindException`, so the shared `ShowcaseApiErrorResolver`'s
  `bodyErrors` contract is silently lost (`ShowcaseRestController` gets activation incidentally from the `@Min`/`@Max`
  on its page-size parameter). A component test that posts a malformed body and asserts `bodyErrors` covers it.
  captured: add-web-ui-rum-observability

- **A new gateway controller package or browser-called endpoint is silent until registered in two un-gated lists.** Add
  the package to `springdoc.packages-to-scan` (else the endpoint is absent from `/v3/api-docs`) and, for a path the
  browser calls, add it to the Vite proxy in `showcase-web-ui/vite.config.ts` (else it never reaches `viteDev` or the
  e2e `vite preview`); widen the live endpoint enumerations per the docs-refresh convention too. captured:
  add-web-ui-rum-observability

- **NullAway is strict on `showcase.*` packages — ensure proper `@Nullable`/`@NonNull` annotations from `jspecify`.** A
  missing or wrong annotation fails the module's `compileJava`.

- **Jackson 3 artifacts (`tools.jackson.core:*`) on the query-service and projection-service runtime classpaths are
  dependency hygiene, not the deferred Jackson 3 backend migration.** IF you see `tools.jackson.core:*` on those
  classpaths, THEN do not treat it as a migration to complete and do not switch application code to Jackson 3 — Jackson
  2 remains the serialization backend. The why (including the `jackson3-bom` constraint) lives in ADR-0003.

- **Spring Data Elasticsearch's `DateFormat.strict_date_optional_time_nanos` truncates to microseconds despite its
  name.** The nanosecond pattern and the reason live in the `read-side/projection-model` spec. `ShowcaseEntity` declares
  the custom `NANOS_DATE_PATTERN` (`yyyy-MM-dd['T'HH:mm:ss.SSSSSSSSSXXX]`) with `format = {}`; do not "simplify" it back
  to the built-in enum until the fix (PR #3337, 6.2.0-M2) reaches us through `spring-data-opensearch`; the truncation is
  invisible on macOS (microsecond clocks) and surfaces only on nanosecond clocks (Linux CI).

- **Extracting a component that exists in a sibling service means mirroring the sibling's full wiring, not the shape you
  copied.** The gateway's Kafka converter was lifted from the projection service without its Axon `upcasterChain`, and
  its event receiver from `ShowcaseProjector` without the `ObservationRegistry` — both silent until review, and no gate
  catches either. Grep the sibling for every configuration call it makes on the same component and carry each across: a
  missing upcaster chain deserializes events un-upcast and a missing registry drops their traces. captured:
  read-through-gateway-caches

- **A generic getter's inferred type can bind an overloaded call to the wrong overload — cast to the erased type, and
  verify the binding, not the source.** `Session.get` is generic (`<T> T get(String)`), so
  `String.valueOf(session.get(attr))` inferred `T = char[]` (the most specific applicable overload) and called
  `String.valueOf(char[])`; the source reads correct and only the run failed
  (`ClassCastException: class java.lang.Integer cannot be cast to class [C`). Write
  `String.valueOf((Object) session.get(attr))` — `javap` then shows `String.valueOf:(Ljava/lang/Object;)` — or bind the
  value to an `Object` local. A compile-valid call proves only that some overload matched; confirm the binding with
  `javap -c` or a run. captured: anchor-the-load-test-baseline

- **Caffeine's `AsyncCache` trips a Reactor bridge on two contracts — a non-null value type and an empty source — and
  its access expiry never bounds a hot entry.** `getIfPresent` returns `null` for a future that completed exceptionally,
  so a failed cached future is indistinguishable from a miss: the `if (present) … else …` shape takes the miss branch, a
  `future.thenAccept(…)` callback only runs on success, and the future ErrorProne's `FutureReturnValueIgnored` flags can
  never leave a sink hanging — so a `@SuppressWarnings("FutureReturnValueIgnored")` on such a method is **redundant**,
  not a hidden bug (verified against Caffeine 3.3.0). Prefer `Mono.fromFuture(…)` over a raw sink-plus-`thenAccept`:
  Reactor observes the future and the empty-vs-failed distinction stays with the cache's own contract. The reverse
  bridge — a `Mono` fed into `AsyncCache.get`'s mapping callback — needs a helper: `Mono.toFuture()` is typed
  `CompletableFuture<@Nullable T>`, and a fresh future completed from
  `source.subscribe(future::complete, future::completeExceptionally)` never completes on an empty source, so guard it
  with `switchIfEmpty(Mono.error(…))` or that entry wedges forever. The callback runs on the calling thread, so a
  subscription created inside it does not inherit the caller's Reactor context — carry it with
  `Mono.deferContextual(ctx -> … contextWrite(ctx) …)` or the trace link breaks. A cache's freshness bound is
  `expiresAfterWrite`: every read resets an access expiry, so `expiresAfterAccess` never bounds a hot entry. captured:
  read-through-gateway-caches

### Agent tooling, OpenCode & permissions

- **Before describing an MCP server, skill, or subagent in a durable doc, read that artifact's own definition — not a
  config entry, a `docs/ideas.md` note, or another doc that merely mentions it.** A referencing entry summarizes; it
  does not specify. Read `.opencode/skills/*/SKILL.md`, `.opencode/agent/*.md`, and the server's exposed tool list, and
  treat an idea note's prose as a lead, not a spec. The README's "Tooling MCP Servers" section once called the
  since-removed `codefmt` skill an IDE-inspection runner (it runs the formatter), said the Playwright MCP "drives the
  web-UI e2e" (the test framework runs it; the MCP is only the agent's browser), and listed `runInspectionsDirectly` as
  an MCP tool (it is an IntelliJ helper called inside `steroid_execute_code`) — all sourced from the config entry or the
  parked idea note rather than the tool.

- **A CLI's `--help` and its docs are not its contract — a missing flag, an asserted exit code, an assumed output
  stream, and a flag's default each have to be probed before a design keys on them.** The `setup-agent-tools` design
  originally asserted `opencode mcp add` was interactive "with no `--command` flag for a local server, so it cannot be
  driven by the agent"; in fact `opencode mcp add <name> -- <command…>` is non-interactive and preserves JSONC comments.
  A quick review caught the false premise — and the correction held only for the CLI of its day: the same probe
  concluded it "writes the global config", which 2.0.21 does not (it defaults to the **project** config; `--global`
  writes the global one), and its `--help` does list the `-- <command>` form. A probe can also write into the
  repository: `opencode mcp add` without `--global` dropped a server into this repo's project config — run a probe whose
  default target is the repo from a scratch working directory (a scratch `HOME` can hang a CLI that starts a background
  daemon) and `git status` afterwards. Before designing around a limitation ("this can't be automated"), verify it by
  trying the command or reading its source/docs — do not infer impossibility from a help screen. The same holds for a
  capability the docs describe only _partially_: the permissions docs name `~`/`$HOME` pattern expansion, and an
  `AGENTS.md` bullet wrongly concluded `{env:VAR}` was unsupported. That false limitation survived the review gate and
  was only caught by reading the source, because a tool's behavior is not repo-evidenced and no in-repo gate can check
  it. Reproduce the _condition_, not the message: a tool error read from a log names only what it rejected in that one
  invocation, so a limitation derived from it is about the input, not the tool. `actionlint 1.7.12` rejects
  `runs-on: ${{ inputs.runner }}` only when the input is **undeclared**
  (`property "runner" is not defined in object type {}`); declare the input and the bare reference passes (both
  directions reproduced) — so the fallback that message seemed to mandate was a _runtime_ need (the scheduled trigger
  leaves the input empty), and the false cause nearly discarded the real one. Before writing "tool X rejects Y" into a
  design or a comment, run X on the minimal input that does and on one that does not, and quote the discriminating
  condition rather than the message. captured: validate-the-workflows-on-ubuntu-2604

- **BEFORE writing a tool-, library-, or framework-behavior claim into a durable artifact, verify it against the source
  or CLI — a doc's account of a feature is a floor, not a boundary.** A framework doc's snippet can be written for
  another version and can name a mechanism that does not apply to the pinned one: Spring Boot's `buildInfo()` doc
  offered `time.set(null)`, a no-op on the pinned 3.5.16 (which needs `excludes`), and `build-info.properties` is
  exposed as a `BuildProperties` **bean**, not a Spring `Environment` property source, so a
  `@Info(version = "${build.version}")` placeholder would not resolve and would serve the literal. Check the mechanism
  against the pinned version's own classes (here `ProjectInfoAutoConfiguration.buildProperties` is a plain `@Bean`) or
  an empty run before designing around the doc's form. captured: publish-github-releases

- **Before treating a file as the implementation, confirm it is the code path that runs.** A matching filename or path
  is not evidence: a package can hold a mock or test harness whose name matches the entry point (`github/index.ts` is a
  local dev/test entry; the shipped handler is `github.handler.ts`). Trace the entry point — the `main`/handler
  registration, or the `uses:` target — to the file that actually runs before describing it. The same applies to a
  `uses:` step's side effects: read the action's README/docs before writing what a step touches, and settle both
  directions there — a design asserted "the only step that touches Docker is `setup-gradle`'s cache service" without
  reading the action, whose own README never mentions Docker, and the counter-claim that it _does_ use Docker, reviewed
  and rejected, was equally unverified. captured: pin-the-smoke-docker-to-overlay2

- **A compiler or build-tool semantics claim needs a minimal scratch build before it constrains a design.** A design
  drafted the premise that a precompiled `.gradle.kts` cannot see an `internal` declaration, and a scratch `kotlin-dsl`
  build disproved it — the `internal object` compiles from the script, and only `private` fails — so the shared helper
  stayed `internal` rather than being widened to `public`. Verify a visibility or build-semantics claim with a minimal
  scratch build before it constrains a design. A `build-logic` object is also unreachable from a root script's
  _configuration_ path for a second reason: `settings.gradle.kts` `includeBuild`s it, so its classes are not on the root
  script's buildscript classpath — a probe saw `ClassNotFoundException: HelmUpdateRules` even from an init script, while
  the same object is in scope inside a task _action_. Keep the rule in the `build-logic` task that applies it — pass the
  input file as an `@InputFile` property, `@Optional` when it may be absent — and let the root script only point the
  task at it, rather than loading it inline from `build.gradle.kts`. captured: rework-the-helm-suppression-list

- **A tool's own contract is not the one you remember — and a bump that touches the production bundle needs the bundle
  built, not only `check`.** The web UI's npm checks asserted npm's contract and took repeated review rounds to correct:
  `npm outdated` exits `1` both when updates exist and on error (the report's content, not the exit code alone, is the
  discriminator), npm warns to stderr on clean runs (so stderr is not the error signal), and `npm audit`'s default
  `--audit-level` resolves to `low`, not "moderate". `npm update` reconciles the whole dependency graph rather than the
  packages you asked for: an in-range direct bump moved its eslint cache stack (`file-entry-cache`, `flat-cache`,
  `keyv`) across majors, and npm 11 filled `license` fields throughout the lockfile — so describe the lockfile diff by
  what moved (not as "patches/minors") and run the frontend `check`. The frontend `check` never builds the production
  bundle — `vite build` is reached only by `build`/`assemble`/`e2eTest` (`check` depends on
  lint/format-check/type-check/Vitest) — so a bump that touches a bundle-path package, `vite` most of all, must also run
  `./gradlew :showcase-web-ui:build` and confirm the bundle emits (a `vite` patch bump can break it while `check` stays
  green). captured: test-build-logic-rules-and-unify-version-comparison (#306) captured: monitor-web-ui-npm-dependencies
  (#395) captured: bump-gradle-and-web-ui-dependencies captured: bump-web-ui-npm-2026-10-05

- **A dependency's last usage takes the same sweep.** WHEN you remove a dependency's last usage, in the same change
  remove its catalog entry and grep the docs, build files, and code for its name — both the coordinate and the catalog
  accessor form (`wiremock-spring-boot` and `libs.wiremock.spring.boot`) — and verify with a repo-wide grep before
  reporting done. After WireMock was removed from its last consumer, its dead `wiremock-spring-boot` catalog entry and
  two `AGENTS.md` examples naming it had to follow, since an unused catalog entry is resolved by no configuration and no
  update check reports it. captured: grpc-query-transport

- **A CLI warning dismissed as noise can report a live defect — a config is unverified until its own read path is
  probed, whoever consumes it (a tool, a hosted service, or the repository's own build), and a warning no gate reads is
  not a check.** `openspec/config.yaml` declared per-artifact rules for four artifacts, but two items contained an
  unquoted `: `, so YAML parsed them as mappings, the lists stopped being arrays of strings, and the CLI ignored those
  two artifacts' rules — warning on stderr ("Rules for 'proposal' must be an array of strings, ignoring this artifact's
  rules") wherever the rules are read (`openspec new change`, `openspec instructions`), which read as noise for as long
  as the config existed; `openspec validate --all` emits no warning at all, which is why CI missed it. The dropped
  `proposal` rules included `Declare "New Capabilities" / "Modified Capabilities" using existing capability names` — the
  rule the review loop kept catching missing. Quote any YAML scalar containing `: ` — the scope is any declarative
  artifact, a project-defined definition file as much as a config: an unquoted `: ` in a `.opencode/agent/*.md`
  frontmatter's `description` made YAML read the value as a mapping, the parse failed, and the agent silently did not
  load (`/audit-readme` failed with `Agent readme-auditor cannot run as a subagent`) — the CI `build` gate now probes
  that every agent/command/skill definition resolves with its metadata intact (its definition-inventory assertion), and
  a reload does not fix it, unlike the not-registered `Unknown agent type` failure. captured:
  fix-readme-auditor-frontmatter (#477)

- **A config can look well-formed and pass the consumer's validation while the consumer silently ignores or reinterprets
  part of it — from outside, a valid config and an ignored one are indistinguishable.** The CI `build` job now probes
  for the list-shape warning on any declared list surface and the whole-file `could not parse` one (a malformed scalar)
  and fails on either, and `/opsx-tool-update` re-verifies both with a positive control. The signal may be a warning on
  stderr that no gate reads, **or none at all**: `java.util.Properties` splits a key at its first `:`, so the dependency
  list's `group:module` entries collapsed to group prefixes — a dead exact-coordinate branch and a whole-group
  over-suppression. For a config **a tool owns**, do not substitute a parser of your own (it assumes a contract the tool
  owns): probe the consumer's read path and fail a gate on its warning. For a format **the repository defines**, own the
  parse and unit-test it — a repository-owned line parser (`DependencyUpdateRulesTests`) beats escaping a library's
  delimiters. captured: hold-back-minor-dependency-updates

- **A load that either succeeds or throws reports through its exit status, not from a message grepped out of its
  output.** `opencode debug config` prints the whole resolved configuration, including every agent's full prompt, so a
  grep can match text the config merely embeds; a loadable config exits 0 and a V2-only `permissions` key exits 1. Its
  blind spot is the definition that loads but is silently degraded — an agent's or command's fields dropped, a skill
  absent from the inventory, all with exit 0 — which is why the CI `build` gate additionally asserts every definition's
  resolved metadata, not the exit alone. captured: probe-opencode-config-read-path

- **Keep a config guard's list-shape pattern generic (`must be an array of strings`), not one surface's enumerated
  wording.** It grew from `ignoring this artifact's rules` to `ignoring this artifact's rules|could not parse`, then to
  the generic phrase, which is what covered every surface — so a malformed `rules` or `operations.*.guidance` item drops
  no config list silently. The same class covers the change's own `.openspec.yaml`, quieter still: OpenSpec's
  change-metadata schema is not strict, so an unrecognized key is silently stripped with no warning at all — a
  `skip_design: true` marker (an inherited agent habit; a dozen archived changes carry it) does nothing, and
  `openspec status` still reports `design` incomplete and points at `openspec instructions design`. There is no
  artifact-skip key beyond `skip_specs`: skip `design.md` by simply not writing it, never by adding a key. captured:
  bump-snyk-cli-pin

- **Track the upstream reports that would retire this project's config guards.** Upstream, the reports are
  `Fission-AI/OpenSpec#1891` (an unquoted `: ` in a rules item; closed 2026-09-29, but its fix
  `Fission-AI/OpenSpec#1894` is still unmerged and in no release — no released CLI has `inspectProjectConfig`, so the
  defect is live here) and `Fission-AI/OpenSpec#1892` (an unparseable config; still open); if `validate` gains a config
  check that fails (an ask in each), the CI probe and the `/opsx-tool-update` re-verification become redundant and can
  go. captured: inject-positive-control-task-rule (#375) captured: widen-config-probe-to-guidance captured:
  record-openspec-1891-closure

- **A hosted service is a third consumer class, verified by the service itself — its own check where it has one, else
  the event it produces.** A retired key can be accepted and ignored with no local signal — `dependabot.yml`'s
  `reviewers` was — so probe the service's read path (`gh api "repos/<owner>/<repo>/codeowners/errors?ref=<branch>"`
  returns `{"errors":[]}` for a valid file and 404s for a ref without one) or observe the event it produces (a review
  request on a real pull request, which begins only once `CODEOWNERS` is on that pull request's base branch, since
  GitHub reads the file from there and skips drafts) — GitHub refuses a review request from the pull request's author
  (`Review cannot be requested from pull request author`, HTTP 422), so probe with a pull request you did not author.
  The general mode: an exclusion can sit on the actor's side rather than the input's (the positive-control rule above),
  where no input choice avoids it. GitHub refuses the request when the requested reviewer is the pull request's own
  author, so the remedy keeps the identity and changes the object — `probe with a pull request you did not author` — and
  a probe run without that change is uninformative by construction (three self-authored probes, differing only in the
  probe's shape, each read `reviewRequests: []`). Confirm that the observable can exist for the actor before reading a
  negative as evidence. captured: fix-dependabot-review-notification captured: record-codeowners-author-rule

- **A subagent added or edited mid-session is not registered until OpenCode reloads its agent list.** Creating
  `.opencode/agent/<name>.md` (plus its command) does not make the agent reachable in the running session — invoking it
  via the Task tool fails with `Unknown agent type: <name>`; the `add-agents-auditor-agent` smoke-run was blocked until
  OpenCode was restarted. A presence check (file exists, `mode: subagent`, model pin set) passes while the session still
  cannot see it. Restart OpenCode (or start a new session) before smoke-testing a newly added, renamed, or **edited**
  subagent — a changed definition body, like a changed `model` pin, is served from the pre-change copy until then, so an
  in-session smoke-run exercises the old instructions, and a run echoing the new text is no proof of pickup (the
  subagent can read its own definition from disk, so the registered body is the evidence). captured:
  name-review-finding-classes (#367) Treat that smoke-run as a precondition for **archiving**, not a task to defer
  across the merge: it is the only verification that exercises the subagent (CI runs none), and an unchecked task inside
  `openspec/changes/archive/` is invisible — the archive tree is outside Spotless and no gate surfaces it — so the check
  is silently lost (`add-architecture-auditor-agent` archived with its `/audit-architecture` smoke-run unchecked for
  exactly this reason). Seed the smoke-run with positive controls — a known-missing rationale it **must** report and a
  known-explained surface (a spec requirement, an ADR, an `AGENTS.md` convention) it must **not**: a run that reports
  everything or nothing has not exercised the suppression rule. **A smoke-run verifies the subagent's behavior, not the
  facts of its output — the seed is synthetic, so verify both separately:** check the seed's premise against the
  repository before reading the run (a seeded incident the repo does not exhibit leaves the targeted branch unexercised
  — the subject working, not a failed test — so re-seed and record which branch the run exercised), and never promote a
  proposal resting on the seed's invented details (the repo-evidence rule's "a run whose output you have" counts a
  smoke-run for the subject's behavior, not for a claim about the code, so re-verify the proposal's factual basis first
  — `make-lesson-capture-consolidate`'s smoke-run proposed a rule resting on a fabricated incident the repository never
  evidenced). A run against a real artifact also surfaces genuine defects beyond the seed: verify each against the
  repository and fix those the user approves in the introducing change, since the archived run's note is never swept
  again — the `add-readme-auditor` smoke-run reported three real README defects beyond its seed (a missing
  `-Pcoverage.gate.enabled=false` at two sites, a merge-narrative contradiction, and a stray table cell), all fixed in
  that change. captured: add-readme-auditor (#315) When the owner deliberately decouples a follow-up task from the
  merge, reword the task to record what is deferred, where it went, and the owner's chosen order, then **tick it** — an
  unchecked box inside `archive/` is invisible and no gate reads it. That is not the rule above: the smoke-run itself is
  never deferred, only a task the owner explicitly decouples.

- **Prove a permission rule reached the configuration with `opencode debug config`; the v1 log line that proved it
  _matched_ a call is gone under v2.** v1 recorded every evaluation in `~/.local/share/opencode/log/opencode.log` as
  `message=evaluated permission=<key>`, `action.pattern=<resolved rule>` and `action.action=<action>` (the field order
  varies by entry), and `action.pattern` was the rule that actually matched; a prompt that does not appear does not say
  which pattern allowed the call. v2 emits no such line — none appears after the 2026-09-24 migration — so matching is
  no longer directly observable, and the static half is the reachable proof: `opencode debug config` lists the
  configuration sources and each one's normalized document. The log also records your own commands, as
  `message="spawning process"` lines, so a search for a token matches its own invocation — a
  `grep 'evaluated permission'` over the log returns its own command lines as hits; exclude them
  (`grep -v 'spawning process'`) before reading a count as evidence. Its `info` is the **normalized** document, not a
  literal input: the normalizer materializes defaults and lifts a flat `mcp.<name>` entry into `mcp.servers.<name>`, so
  a migration verified against `debug config` is behaviourally equivalent, never literally identical — name the delta it
  accepts (2.0.21: no `disabled` key materialized, credential values masked). A source's `info` can carry credentials —
  the v2.0.15 build rendered the global config's MCP `environment` value (a `GH_TOKEN`) verbatim — so scope a capture to
  the project document or redact before quoting its output into a report, a PR body, a change dir, or any other
  artifact; re-check the masking on each v2 bump. The same redaction binds any diagnostic capture:
  `./gradlew --info`/`--debug` logs each exec'd process's full environment (`with environment: {…}`, values included),
  putting `SNYK_TOKEN`, `GH_TOKEN`, and `NVD_API_KEY` into the output — never persist a verbose log into an artifact;
  read it with a targeted `grep`. captured: bump-dependencies-2026-10-12

- **`external_directory` and the `shell` permission are separate actions.** `external_directory` governs the file tools
  (`read`/`edit`/`write`/`glob`/`grep`) and path-taking commands, while a script's own out-of-tree writes run under the
  `shell` action (`permission.bash` in v1) — so removing the blanket `/tmp/**` allow from `external_directory` leaves a
  bash-script write such as `setup-hosts.sh`'s unaffected, and a temp-root grant should not be re-added there for it.

- **An unanswered `ask` hangs an unattended run — anchor a `bash`/`shell` permission pattern to the parsed command, and
  bound the job.** OpenCode emits one pattern per parsed `command` node, so a whole-command-string guard
  (`"*git push*"`) also matches a read-only command that merely quotes the phrase (`grep … "git push"`); anchor it
  (`"git push *": "ask"`). A **saved "always" allow overrides the rule**: the engine appends the saved rules after the
  config rules and takes the last match (`findLast`, `packages/core/src/permission.ts`), and v2 persists "always" per
  project in the DB's `permission` table though the docs call it session-only — so a guard that never prompts is a saved
  row until proven otherwise; check or clear it before changing the rule (saved rows are per-project in that table).
  Tracked upstream as `anomalyco/opencode#48456` (open; `#50600` asks for session-scoped or resettable approvals);
  retires when a saved allow stops overriding a configured `ask`. The action's handler denies only the `question`
  permission, so resolve each binary's ask-defaults — the cloud (v1) binary asks for `read` `.env`,
  `external_directory`, and `doom_loop`, the local (v2) binary for `read` `.env` only — to `allow`/`deny` in the config
  the action uses, and bound each unattended job with `timeout-minutes` — a scheduled `audit` hung 2h13m on a `bash`
  rule, and `/oc` runs were cancelled at 42m and 17m on `external_directory` asks. Deny only a default whose internal
  allows the config can re-express, since a config rule is appended after the binary's defaults and overrides them:
  `external_directory`'s `*` cannot be denied (it would override v1's scratch/skill/reference allows, which `expand()` —
  only `~`/`$HOME` — cannot re-express), while denying `*.env.*` needs `*.env.example` re-allowed. Reported upstream as
  `anomalyco/opencode#54477` (declined for v1 — `wont_fix`); retires when the handler resolves a non-`question` `ask`
  (or fails the run on an unanswerable prompt). captured: fix-unattended-permission-hang captured:
  harden-cloud-runs-against-ask-hangs captured: record-saved-allow-override

### Documentation & durable claims

- **Keep em-dashes paired when inserting a clause into a sentence that already uses them.** A rework that adds an aside
  to a dashed sentence can leave an odd number of dashes, so the reader cannot tell which dash opens the outer clause
  and which closes it — the trailing clause's pairing is ambiguous. Count the dashes after the edit (an odd count is the
  defect) and prefer parentheses for the inserted aside; Prettier reflows prose but never balances delimiters, so no
  gate catches an unpaired dash — proofread it as content. captured: capture-untracked-follows-switch (#284)

  The same class covers a paragraph boundary: a blank line lost in an edit fuses two paragraphs (an added one splits a
  sentence), and Prettier reflows the result without complaint — so proofread blank lines as content too. captured:
  notify-owner-from-the-audit-report (#327)

  evidence: documentation-and-durable-claims

- **Verify documented infrastructure/deployment numbers against the config files, not memory.** Before writing a replica
  count, panel/section count, or diagram count into a doc, read the source (`helm/values/*/values-*.yaml`, the dashboard
  JSON, the component table — or, for an upstream chart's shipped default or a container the values do not override, the
  pinned chart via `helm show values <chart> --version <pinned>`) and cite the real number. The same holds for _which
  check_ a documented figure belongs to: two mechanisms can bound the same metric at the same order of magnitude, so a
  boundary's owner cannot be inferred from the figure's size — the 2026-09-28 refresh left the below-knee profiles'
  floor-dominated thresholds unchanged (`5 x 27 ms = 135 ms` is under their 200 ms p99 floor), while the ~1.5x boundary
  an annotation named was the drift check's own tolerance. Read the mechanism's definition and name the one the boundary
  belongs to. captured: re-measure-load-test-baseline (#429)

  evidence: documentation-and-durable-claims

- **A two-way split of an enumerated set can also sum to its total and still be wrong when its boundary is hand-drawn:**
  for a gated-versus-excluded split, or any enumeration or partition of a set the repository declares — the root `check`
  task's members, a CLI's modes, the tracked executables (`git ls-files -s | awk '$1=="100755"'`) — derive the members
  from that declaration (`build.gradle.kts`'s `check` block, its `target`/`targetExclude` entries, the `git ls-files`
  query), never from intent or a hand-written complement, and update every doc that enumerates the set when a change
  adds a member. A claim about what a gate, task, or check _runs_ is the same class: derive it from that tool's own
  definition — its task registration and `commandLine` — not from recall, and a claim about what a chart renders
  likewise from the selector the template emits rather than its comments, which can contradict it. Never write the
  resulting totals into a durable artifact — they move with every change the corpus gains. captured:
  reconcile-netpol-spec-peer-wording

  evidence: documentation-and-durable-claims

- **`workflowLint` runs `actionlint` alone** (the repository installs and documents no `shellcheck`; actionlint invokes
  it only if it happens to be on `PATH`). **A list naming a tool or check set must not read as complete while naming a
  subset, and a claim patched at the instance is still wrong one level down:** derive the claim from the complete set,
  and either enumerate a set in full or name the location only. Name a count's referent too, and check its arithmetic
  against the total it belongs to.

  evidence: documentation-and-durable-claims

- **Distinguish a point-in-time count from a durable one:** an exact count of a growing corpus (capability specs,
  requirements, archived changes) belongs in a change's own artifacts — except when the counted artifact is the change's
  own and grows while it is in flight (its review-round record, its appended rows), where a checked task certifying "the
  first N rows" is falsified by the next row, so state the mechanism with no count — unless the count is the artifact's
  evidence (a claims-table row counting a live corpus), where the deriving command names the boundary that stabilizes
  it, excluding the unit's own rows (`$2 != "<unit>"`), or the figure moves every time the unit appends one. Never put a
  corpus count in a durable artifact (a subagent definition, `AGENTS.md`, the README, a command file, a main spec): it
  drifts on every archive, exactly the drift the `specs-auditor` exists to catch, so describe the shape instead of
  freezing a tally. A process count — elapsed time, effort — is not durable either, since no reader can verify it from
  the repository, so describe it qualitatively; review rounds are the exception, cited from `docs/review-rounds.tsv`
  (aggregated by `scripts/review-rounds.py`) rather than from a session-only number. A _machine-measured_ figure is a
  different class, evidenced by its CI run log, so an order-of-magnitude build cost is not a process count and need not
  be made qualitative.

  evidence: documentation-and-durable-claims

- **A named example or mechanism inside a convention is itself a claim, not decoration:** the `@DirtiesContext` rule
  said keep it on "the gateway e2e test, which pulls in JGroups", but the e2e suite drives containers and never boots
  JGroups in the test JVM — a false example that surfaced only when ADR-0009 had to restate the same rule. When a second
  artifact restates an existing fact, diff the two against the code rather than copying the prose. captured:
  make-captured-rules-traceable (#279) captured: verify-tracked-ignored-files captured: state-tooling-placement-rule
  (#458) captured: record-review-rounds captured: derive-the-change-claims

- **A doc-consistency sweep is scoped by the convention or the claim, not by the review's findings list — and a claim
  about the code is verified against the code.** Derive the sites by grepping the artifact or corpus, never from the
  plan's list, and sweep the pair for each kind of change the unit made:

  - a convention (a coordinate or identifier spelling, `MUST` vs `SHALL`, an enumeration) → every instance in the
    artifact or corpus;
  - a corrected fact (a direction, a value, a magnitude) → every repetition, plus the statements that depend on it
    without repeating it (a caveat, a rationale, a concluding sentence);
  - a rule the change authored or replaced → its paraphrases by the rule's **subject**, in every document that describes
    the workflow: `AGENTS.md`, file headers (e.g. `docs/ideas.md`'s), the `.opencode/` definitions, the specs, and the
    change's own artifacts;
  - an audit finding → the pattern the finding is an instance of, not the finding alone;
  - a widened or narrowed check → its describers, including a worked example in a definition;
  - a javadoc or field-doc pass → every declaration in the touched classes, not only the ones a review named;
  - a set that gained a member, or an enumeration of one → every live enumeration of the set, since a plain enumeration
    is the case a name-grep cannot see: grep its distinctive members across `.opencode/`, `openspec/specs/`,
    `AGENTS.md`, `README.md`, `docs/ideas.md`, and `scripts/` before writing the addition — a tool's module docstring
    and `--help` text are live enumerations too, and the change's own planning copies can stay identical while every
    live site goes stale — and when the set is an auditor's, sweep its definition, its trigger command, `AGENTS.md`'s
    summary of it, and the spec requirement that owns the sweep, which is a main spec and so needs a `MODIFIED` delta
    rather than a docs edit;
  - a code symbol or file renamed or removed → the whole repository for the old name, config, values, and template
    comments included, since docs cite these names as examples and nothing resolves them;
  - a second instance of something the docs call unique (an `only`/`sole`/`never` claim) → `AGENTS.md`, for the claim
    about the first instance; fix it in the same change, and re-verify such a claim against the repository whenever you
    edit its section rather than trusting the prose;
  - a capability the change makes no longer absent (`nothing covers X`, `A and B ignore X`) → `AGENTS.md` and
    `README.md`, including a claim a recent change added;
  - a component you record as deliberately _not_ used → the whole repository for its name.

  Two directions read plausibly for a converter or mapper, so read the field's Javadoc and one call site before writing
  its `@param`/`@return`. captured: bump-tooling-pins-2026-10-16 captured: make-the-capture-selective captured:
  monitor-web-ui-npm-dependencies (#395) captured: verify-unique-review-rounds

  evidence: documentation-and-durable-claims

- **An abandoned approach needs the same sweep:** reverting an experiment's code line does not remove the comment or the
  task/design prose that describes it, so grep the artifact for the approach's name before calling the revert done. The
  sweep spans live copies whose sentence makes a present-tense claim: `openspec/changes/archive/` is the historical
  record, left as recorded — the Spotless target already excludes it — so a corrected command or a renamed symbol found
  there stays as recorded rather than being "fixed". A live artifact is no different when its sentence cites the figure
  as a historical incident: `.opencode/agent/readme-auditor.md`'s "a '36 panels' that counted row separators" names the
  README defect the auditor exists to catch, not the dashboard's current structure, so it stays too — and the change
  records why the match is deliberately left rather than silently skipping it. captured: right-size-infra-resources
  captured: add-web-ui-dashboard-panels captured: rework-readme-diagrams-for-github-mermaid

- **A configuration-default change names every test assertion that pins the value and every doc that describes it, on
  each surface it is declared.** Grep the test sources for the field accessor when changing a default, and give each
  pinning assertion its own task — the Java-defaults test and the yml-wiring test each bind a different surface. A
  variable carried in a service's `bootBuildImage` `BPE_DEFAULT_*` map has one surface more: the image's
  launch-environment default (`paketo-buildpacks/environment-variables`), which a deployment's env var overrides but
  which in turn overrides the yml fallback. A new property the image carries joins that map too, its value decided from
  the map's closest sibling there — not as a diff against the Java default — because the map is the image's declared
  deployment environment. Documents that describe the value are surfaces too: grep the docs for it and correct every
  description — a doc's sites come from that grep, not from the plan's list — but state the values a **historical
  record** (a baseline, a retro, an audit) ran under in the past tense rather than rewriting it to the new ones, since
  its measured data is what it records. A service URL's yml fallback is overridden by the container path
  (`BPE_DEFAULT_*`; in-container every service listens on 8080), so it is exercised only under host `bootRun` and must
  equal the target service's local `server.port` — a component test asserting the placeholder literally pins whatever
  value is there, not the right one. captured: fix-gateway-cors-allowed-headers (#359) captured:
  right-size-chart-resources captured: fix-gateway-query-service-url captured: guard-archive-spec-sync

  evidence: documentation-and-durable-claims

- **Doc claims must match their source and their strength — quote verbatim or paraphrase explicitly, and reserve
  "enforced" for a real gate.** When a doc quotes a rule, spec, or comment, copy the exact text (or drop the quote marks
  and describe it), and check the mechanism before using words like "enforced", "always", "never", or "every" — against
  the variants other docs define too, since an unconditional rule must hold on every documented path, not only the
  default one. One of those paths is the rule's own text: state a formal criterion — a count, a delimiter balance, a
  uniqueness test — with its scope where the criterion is stated, not only in the rule's opening sentence, so a literal
  reading cannot apply it to the rule itself (the `Keep em-dashes paired…` rule scopes its odd-count check to a sentence
  that already uses paired dashes). Check the claim's grammatical subject too, not only its wording, since a pronoun can
  bind a claim to the wrong actor; and check a qualifier's scope, because a condition or exception introduced for one
  variant must not swallow the rule's primary mandate — strip the qualifiers and confirm the imperative verb still
  governs the default case. Check a recorded _mechanism_ against every measurement the change made, naming the evidence
  points the explanation must cover and confirming the wording holds for each, because a generalization one of your own
  runs contradicts is wrong however well it reads. captured: record-blocking-execution-rationale (#335) captured:
  grpc-query-transport

  evidence: documentation-and-durable-claims

- **A durable artifact may assert only what the repository can evidence — a history that lives only in the conversation
  is not repo history.** Before writing a historical or behavioral claim into `AGENTS.md`, `README.md`, a subagent
  definition, or a main spec, find its evidence — a config file, a log line, a commit, or a run whose output you have.
  Check a cited source's provenance too, not only its existence: a clause written _for_ an event cannot evidence a
  _prior_ occurrence of it, so before a recurrence claim take a source that predates the event and was not produced by
  it (`git blame`/`git log -S` the clause, or grep the rule's `captured:` token to find its introducing commit). A
  commit cited as evidence is itself a claim: read what it did (`git show --stat`) and whether the path existed at
  `<commit>^` before attributing lines or a reflow to it. If the only source is the conversation, omit it or label it as
  the owner's account; for an outcome you did not observe, state the mechanism ("a bash-script write runs under the
  `shell` permission") rather than the observation ("it did not prompt"). Point-in-time narrative belongs in a dated
  record — the change archive, a retrospective, an audit report, a load-test record, or a rule's
  `docs/guidance-evidence/` entry — never in the guidance itself. captured: store-2026-10-05-retrospective

  evidence: documentation-and-durable-claims

- **A claim about a surface outside the repository — a registry's contents, a repository setting, a live URL — has no
  config file or commit behind it, so its evidence is a query whose output you have:** run it before writing the claim,
  and re-run it at review, because such a surface can change between the two and no gate reads it. Verify a URL by
  requesting it, and treat the review, not a gate, as the check. An evidence statement for a task that _touches_ such a
  surface has the same limit in the other direction: do not pin a value the surface produced as the evidence the task
  was done, since the rows of a live lookup vary run to run. captured: rework-the-helm-suppression-list

  evidence: documentation-and-durable-claims

- **When a script generates many claims at once, the method that derived them is not their evidence:** each generated
  item needs its own control, applied per item rather than as a spot check, because a heuristic that is right on nine of
  ten items writes its one error silently into the durable file and a hedge tag (`approximate`, "uncertain") does not
  repair it. captured: retro-mark-captured-rules

  evidence: documentation-and-durable-claims

- **A comparison between two runs or files that differ in more than one dimension cannot attribute the difference to
  either — isolate the variable before naming a cause.** Read the authoritative policy for the mechanism you are
  hypothesising, or vary only that dimension; a fix that enlarges a privilege to explain a behavior is a signal the
  cause is still undiagnosed. The same rule binds a single experiment that varies two dimensions at once, and a
  disagreement between two runs that differ in an uncontrolled dimension refutes neither — compare the inputs before
  concluding either wrong. It extends to a single observation: a token used to evidence which route supplies a surface
  must reach it only by that route — read the route the graph prints
  (`./gradlew :<module>:dependencies --configuration <suite>CompileClasspath`, whose tree names each route's parent), or
  pick a token with one route. captured: guard-agent-loader-read-path captured: fix-mapstruct-suite-warning

  evidence: documentation-and-durable-claims

- **A reproduction in an outward-facing artifact is itself part of the claim — write it so a reader reruns it to the
  same output, and rerun the exact sequence before posting.** State the tool version and the starting state, and record
  the commands in the order they ran, since an order-dependent transcript can self-contradict and a precondition left
  implicit is not reproducible elsewhere. Re-check every derived detail — line numbers, exit codes — against the actual
  output, and capture an exit code directly, not through a pipe (`cmd | jq` reports `jq`'s status, not the CLI's). This
  is the drafting counterpart to "report it upstream with a reproduction and the evidence"; the tool-behavior claim
  itself is verified per the `--help`/read-path rules.

  evidence: documentation-and-durable-claims

- **An upstream issue reference is a status claim, not a citation — resolve it, and treat a closure as a trigger to
  check rather than an answer.** Resolve an `owner/repo#NNN` through the tracker before writing or editing such a
  reference. When it has closed, read the closing comment for its disposition (a duplicate's live thread is elsewhere,
  so the closed issue is not its authority) and read the fixing PR's own diff and the release it shipped in against our
  pin before treating the note as retirable. The note itself is a claim: re-verify its recorded reason, not only the
  open/closed state. An upstream reference must state the claim it is cited for and sit on the clause it supports; read
  the surface that carries the claim — the body for what the issue reports, the closing comment for its disposition —
  never the title or the release alone. A close-out reference must live in a file `upstreamReferences` scans
  (`AGENTS.md`, `README.md`, `docs/ideas.md`, `docs/adr/**`); cited only in a skill, command, subagent definition, or
  code comment it is invisible to the report that would fire its `retires when`. When a passage already explains a
  signature, reconcile with it rather than writing a second explanation beside it. A planned task that posts to an
  external tracker depends on that venue's mutable state: check the venue is writable before writing the posting task —
  a repository can disable issues and discussions outright, and a thread can be locked — and when the issue is closed or
  locked, cite the fixing PR the close-out keys on. The periodic corpus sweep is the `upstream-references.yml` report
  (`showcase/quality/merge-governance` specifies it with the other observational checks). captured:
  suppress-braces-npm-advisory captured: refresh-upstream-references-2026-10-05 captured: record-gatling-report-block
  captured: refresh-upstream-references-2026-10-19

  evidence: documentation-and-durable-claims

### Environment & host toolchain

- **Relative `date` arithmetic is not portable across macOS and Linux**: BSD `date` (macOS) uses `-v-7d`, GNU `date`
  (Linux, incl. CI) rejects it and needs `--date='7 days ago'`. A cross-platform script computing a relative date must
  probe first (`date -v-7d >/dev/null 2>&1 && … || date --date='…'`), as `scripts/experience-analysis.sh` does for its
  7-days-ago default — don't hard-code one platform's form.

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

- **A repo Python script runs under whichever `python3` PATH resolves, not the documented prerequisite — verify a new
  script under the system interpreter.** The documented Python is macOS's system `/usr/bin/python3` (3.9), where PEP 604
  `X | Y` unions raise `TypeError` at import, while the newer `python3` on a contributor's PATH (and in CI) accepts them
  — so the incompatibility passes every gate, none of which pins the interpreter. Keep repo Python stdlib-only and
  compatible with the system interpreter (`typing.Optional`, not `str | None`), and run `/usr/bin/python3 <script>`
  before reporting it done. captured: add-pre-commit-staged-set-guard (#372)

- **macOS local-network privacy can leave a Warp-spawned shell unable to reach the local cluster.** Reaching a
  local-network address is a per-app privilege macOS tracks by code-signing identity (Apple `TN3179`), and a third-party
  terminal's child processes can be denied it while the internet works: the LAN and colima's vmnet subnet
  (`192.168.64.0/24`, the address the kubeconfig points at) fail with `EHOSTUNREACH`, which reads as a cluster outage,
  and neither a `colima restart` nor a `colima delete` + recreate changes it, because the fault is the host's consent
  layer rather than the VM or k3s. Check the terminal app first: Warp can be _listed_ under System Settings → Privacy &
  Security → Local Network with its toggle **off**, and switching it on restores access immediately; quit and reopen the
  app if its existing children keep the denied state. `warpdotdev/warp#6320` documents the symptom (the same command
  succeeds from Terminal.app) but was closed as a duplicate of the canonical, still-open `warpdotdev/warp#5550`, whose
  thread shows the toggle already enabled for reporters who still failed: this is the first check, not the only one.
  Fallbacks: run the command from **Terminal.app** or under `sudo` (macOS auto-allows command-line tools run from
  Terminal or over SSH, and any program running as root), or point the kubeconfig at colima's loopback forward
  (`https://127.0.0.1:<port>`, the port changing per start), which is not a local-network operation at all. A/B against
  Terminal.app to confirm the class.
