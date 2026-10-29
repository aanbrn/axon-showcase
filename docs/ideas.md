# Ideas

Short notes to remember emerging development ideas. An idea becomes an OpenSpec change only when acted on — this file is
a scratchpad, not a backlog of planned work. An idea is removed from the list once implemented (captured by a change) or
once explored and decided against (the durable lesson is captured in `AGENTS.md`, an ADR, or a dated retrospective where
the decision is a program direction with a revisit condition rather than a rule an agent applies); only open,
not-yet-implemented ideas remain.

Changes to this file travel with the change that owns them: an idea's **removal** ships in the implementing change's PR
— it rides that change's branch and commits with its push, like the rest of the work — and a **newly parked** idea the
in-flight change's own work surfaced rides that branch too; only a docs edit the change did _not_ cause, or one surfaced
after it is done (a defect a merge appends to the merged unit's record), ships as its own docs PR (like an
`AGENTS.md`/`README.md` refresh PR). When an idea graduates into a concrete candidate for work, it may be promoted to a
GitHub issue that links to the eventual OpenSpec change. Ideas are grouped into `## YYYY-MM-DD` sections ordered
newest-first; each idea goes under a section dated when it was added (start a new section for a new day rather than
appending to the most recent one). Search the file for the idea before adding it: the same work already parked — however
differently worded — is extended in place in its existing section, since the day-section rule otherwise files a re-park
as a second entry and splits one idea's detail across two sections.

## 2026-10-29

- README density pass — parked; no change yet. Development Workflow runs roughly twice the length of Getting Started,
  and several mechanism descriptions (the auditors, the instruction budget, the quality gates) restate what their owners
  document; trimming them to pointers is its own pass, deliberately outside the promises-and-routes unit.
- First-run resource passport — parked; no change yet. No source in the repository states the machine budget a first
  Compose or Helm run needs, the builds included; the load-test baselines measure a deployed cluster, not a minimum.
  Measure it before the README promises RAM or startup time.

## 2026-10-28

- Give the review gate's §5 a post-refresh return branch — parked; no change yet. The unit's `#1` process-defect record
  describes a refresh after the closing quick round whose hand-resolved conflicts were not re-reviewed before the manual
  pass was requested again. The requirement exists in `/sync-branch`'s lifecycle ("The full per-unit review gate runs
  over the result"), but §5 — the boundary the agent follows when asking — is silent: it permits refresh, re-stage and
  ask. Add the return branch to §5 (after a refresh that changed the reviewed tree, run the closing quick round again),
  or point §5 at the lifecycle line, without restating the sync procedure.

- Evaluate Gradle's official `gradle-best-practices` skill against this build — parked; no change yet. `gradle-skills`
  ships `gradle-wrapper-upgrade` and `gradle-best-practices`. `gradle-best-practices` audits a build against a bundled
  copy of the official best practices, in an Audit mode that reports and an Apply mode that edits. Of the two skills,
  only `gradle-best-practices` is in question here: `/gradle-update` with the `gradle-build` pack's wrapper rule already
  carries the wrapper skill's two-`wrapper`-runs procedure. Its value here is unmeasured, and this build deliberately
  diverges from generic advice in places that the audit may flag: upstream `gradle-helm-plugin` warnings left unfixed,
  the custom test suites' re-declared dependencies, and the coverage gate's `-Pcoverage.gate.enabled=false`. Read a
  published tag rather than the announcement: the announcement promised a fix for wording that could let an unattended
  audit apply structural fixes, and at `gradle-best-practices-v1.0.1` an unattended run still applies structural fixes —
  its "Never block on a question" rule has the run record the plan and then execute it, and its Audit mode applies fixes
  without asking once no reply can come. Do not run the skill's Audit or Apply mode unattended. Vendoring is one option
  rather than a requirement: it would make `gradle-best-practices` another vendored skill set, owing the provenance
  record, the declaration sites the `Vendored agent skills` bullet and the `2026-10-22` single-sourcing entry own, and a
  `MODIFIED` delta on `showcase/quality/agent-skills`, whose requirement names the vendored sets. Revisit when: a
  published `gradle-best-practices` tag's `SKILL.md` leaves an unattended Audit run report-only — it applies no fixes,
  structural or otherwise, and does not proceed past a confirmation it could not obtain (external coordinate — a
  per-skill released tag such as `gradle-best-practices-v1.0.1`, not a default-branch commit; the skills carry their own
  tags beside the repository's releases). Meeting it reopens the evaluation; it authorizes no run and no adoption.

- Pin the wrapper's distribution checksum and give the bump a rollback — parked; no change yet. Gradle's
  `gradle-wrapper-upgrade` reads the release's SHA-256 from `services.gradle.org` and restores the wrapper when the
  build no longer configures. `/gradle-update` and the `gradle-build` pack's wrapper rule carry neither, as the pack
  records; whether either is worth adopting here is unmeasured. Surfaced while scoping the entry above.

- Expose the session's remaining context budget to the agent — parked; no change yet. `anomalyco/opencode#59531` (this
  repository's report: an agent concluded its budget was exhausted well inside the model's window) closed `not_planned`
  on 2026-10-27, because newer models get confused by such a figure; the maintainer closed with "if you want it you
  should write a plugin for it". The SDK reports a session's token totals and cost but no remaining budget, and whether
  a remainder can be derived from those totals is unverified. On the surface the running binary loads, feasibility is
  not established either: the v2 `PluginContext` names no tool and no per-turn message hook (ADR-0024 lists its
  domains), leaving a static `agent.transform` prompt — the caching-breaking form the issue's own discussion flagged —
  and an `aisdk.language` model wrapper whose ability to append a reminder to the latest message is untested. Any
  implementation must be measured against the maintainer's caution rather than assumed helpful. Revisit when either of
  the following is met, each alone reopening it: (external coordinate) a released OpenCode version's v2 plugin surface
  gains a tool-registration or per-turn message hook; (internal trigger) a probe shows an `aisdk.language` wrapper
  appending a reminder to the latest user message while the earlier prefix stays cacheable.

## 2026-10-27

- Fix the gradle-helm-plugin's `failIfExists` repository-add option — parked; reported upstream at
  `build-extensions-oss/gradle-helm-plugin#238`, open as `build-extensions-oss/gradle-helm-plugin#241`; not yet merged.
  The option emits `helm repo add --no-update`; the pinned Helm 4.3.1 rejects that flag (`unknown flag: --no-update`),
  and Helm 3 declares it `Ignored.`, so the option cannot work with any client the plugin supports. The proposal reads
  the registered repositories and fails the task instead. The build does not set the option.

- Read a parked idea's own `Revisit when:` condition, not only whether its work landed — parked; no change yet.
  `architecture-auditor` classifies a `Revisit when:` condition for a deferred ADR, but for a `docs/ideas.md` entry it
  tests whether the work has landed, only partly landed, or is achieved by a different mechanism — not the entry's own
  recorded condition — and the parked entries carrying one (any entry stating one, in whatever form; the vendored-skill
  set's under `## 2026-10-22`, the retrospective-evidence one's under `## 2026-10-23`, and the pull-request title
  check's under `## 2026-10-25` are examples) are swept by nothing. Add a sibling branch — "a parked entry whose own
  recorded condition appears met" — classified by the same four-way rule as an ADR condition and not reported when the
  condition names no checkable repository fact, with a `MODIFIED` delta on the `agent-skills` requirement, one clause in
  `AGENTS.md`'s auditor summary, and the capture bullet's own closing reason — "no other reader tests a parked entry's
  condition" — which this branch would falsify. The event-time half is in this unit's capture (an incident that meets a
  parked entry's condition reopens it); this is the periodic sweep. Surfaced by the
  `annotate-the-reviewer-wrapper-recurrence` capture.

- Make `--markers` require that a marker ends its rule, not only that it sits inside one — parked; no change yet.
  `commit-hygiene.py`'s `_marker_is_in_rule_block` accepts a `captured:` marker anywhere within a bold-lead block, so a
  marker inserted mid-rule passes green while reading as part of the sentence around it; this unit introduced one that
  way and its round 2 caught it, not the gate. The check could require the marker chain to be the last text before the
  rule's next sibling, or that no prose follows a marker inside the block. Surfaced by the
  `annotate-the-reviewer-wrapper-recurrence` unit's round 2.

- Read a changed procedure's narrative, not only its name — parked; no change yet. When `#706` added the branch-refresh
  step and `#707` added the base check, the README's manual-review paragraph kept saying only that the agent stages the
  new files, and a name search for `origin/main` hit the `/sync-branch` table row and read as coverage. A candidate rule
  would say that a procedure the change alters takes its narrative with it, because a name search is answered by any
  mention while the passage still states the old behaviour. It failed the capture promotion gate on evidence: one gap
  that survived two merges is one instance, not two independent ones, and no severity is shown. Promote it with a second
  independent occurrence — another change that altered a procedure without revisiting the passage describing it — or
  with a demonstrated cost. **Reopened 2026-10-27**: its condition is met. `separate-lesson-capture-from-implementation`
  replaced the per-unit capture with recording plus an owner-triggered generalization and rewrote the README's loop
  bullet, while the introduction that forwards to that section still said each unit's lessons are written back — found
  by a consultation, not by the unit's own README sweep. No rule is proposed here; the entry stays parked for the owner.

- Verify a fix against its source, not only against its finding — parked; no change yet. A fix can satisfy its finding
  and still narrow or broaden the claim past its source: this unit's round-1 fix added "only", which met the finding and
  excluded the procedure's other stops. A candidate rule would add that second check to the review gate's fix step. Two
  obstacles stand: it failed the promotion gate on evidence — one round of one unit, and the loop itself caught the
  over-correction — and it contradicts the gate's own "No semantic self-review precedes the loop", which has the author
  owe only the pre-write pass and the mechanical pre-flight. Promote it with a second independent occurrence and wording
  that reconciles it with that rule, or route it to the reviewer's side, where re-deriving from the source already
  lives.
- Record that a `PATH` interception sees only the calls resolving through it — parked; no change yet. A wrapper placed
  first on `PATH` records the calls that resolve through it; a tool that launches its own child by an internal exec-path
  escapes it, so a record evidences the calls and never the effect. A `git` wrapper was designed to capture a
  `post-merge` hook's subcommand and a probe showed git puts its own exec-path ahead of `PATH` for hooks, so the
  assertion could never fire. Run the fixture once and read the record for a known instance before asserting through it,
  and assert the outcome for an effect produced outside the wrapper's path. The evidence came from the
  `add-sync-branch-command` script draft, whose suite was dropped; the lesson is independent of it. A
  `check-and-test-authoring` mode is where it belongs.

- Record that an assertion the sibling outcomes share cannot see which one ran — parked; no change yet. When a subject
  emits one of a closed set of outcome tokens, assert the token that distinguishes the outcome: a failed fetch had to
  report `unknown` where a skipped fetch reports `unconfirmed`, and the case asserted `unconfirmed` — the word both
  carry — so the wrong state could ship with the suite green. Read the outcome set off the rule that defines it, pin one
  case per member on its own token, and assert a sibling's token absent where the set is closed. Evidence from the same
  dropped suite; a `check-and-test-authoring` mode is where it belongs.

- Correct `pause-resume-unit`'s `Stage` field so it stops inviting the ledger's state — parked; no change yet. The field
  says "Where the unit is: the proposal, a review round, the implementation, …", so a writer naturally renders the
  review stage as a narrative — how many rounds, whether the loop is clean — which is exactly the copy `AGENTS.md`'s
  single-sourcing invariant forbids (a review round's state is `docs/review-rounds.tsv`'s). Three drifts in one
  checkpoint (`add-sync-branch-command`'s) came from that pressure, each fixed by pointing at the ledger. The correction
  belongs in the skill's field set and a decision point (state only what the artifact owns; point at the ledger), not in
  `AGENTS.md`, whose invariant already states the obligation. A definition edit of that skill owes its own change.
- Edit the audit workflow's prompt if the general language rule is ever observed failing on that path — parked; no
  change yet. `unify-agent-language-policy` leaves `.github/workflows/audit.yml` unedited: its prompt already states
  that the final response becomes the pull-request body, so the durable-artifact rule makes that body English, while a
  separate clause would owe a `merge-governance` delta and a full three-audit cloud dispatch. It reopens when an audit
  pull-request body is observed in a language other than English, or a review records one on that path; the fix is then
  the prompt clause, its `merge-governance` delta, and a dispatch from the pushed branch.

- Investigate runtime reply-language compliance if mixing persists — parked; no change yet. #713 widened the `AGENTS.md`
  convention and the `agent-skills` scenarios so owner-visible narration and a presented report's prose follow the
  owner's request language, while its design records that no in-repo check can observe how a model phrases a reply and
  ends "the next step is runtime compliance, not more wording"; this entry is that next step's live record, since the
  archived design is not read. It reopens when an owner-visible message produced with the widened convention in effect
  violates it — not when it merely carries an untranslated identifier or quotation — and retains the triggering request,
  the offending excerpt, and the evidence identifying the applicable instruction state. The direction is to investigate
  compliance before proposing another wording expansion; no enforcement mechanism is selected.

- Give claims-table rows an executable verification mode — parked; no change yet. Of the units this pass analyzed, four
  carry 17 `claim-unreproducible` rounds between them (`extend-owner-reply-language-scope` 3,
  `hand-over-unfinished-work` 4, `record-the-settle-step-defect` 2, `separate-lesson-capture-from-implementation` 8),
  with different causes (a line range, a case-sensitive `grep`, an unsourced figure, a path set that excludes the
  subject), and the cause-specific prose rules already exist while each new cause produced another defect: the row's
  command and expected result are prose, so nothing executes a row. The shape to build is an opt-in stdlib Python
  `--verify` mode in `scripts/claim-scan.py`, its advisory default preserved, running each row's command at the revision
  the row names and exiting non-zero on a mismatch, with no execution in the default `check`. A bounded pilot over these
  incident commands comes first, because it must establish the machine-checkable subset (some rows rest on sources or
  transient observations) and the execution-context, safe-operations, revision-semantics, timeout and comparable-result
  contracts that a command/result pair does not supply. Surfaced by `generalize-process-defects-2026-10-27`.

## 2026-10-26

- Freeze a unit's resume checkpoint at the resume — parked; no change yet. `.opencode/skills/pause-resume-unit/SKILL.md`
  defines the checkpoint's field set but never says it stops being maintained once the unit resumes, so a checkpoint
  written for a pause stays live afterwards, restates state that `docs/review-rounds.tsv` and the task list own, and
  drifts: `correct-subagent-reload-rule`'s drifted twice before it was frozen as a dated snapshot pointing at the live
  records. The contract should say that resuming is the act that freezes it, and that each field names the artifact
  owning its state rather than restating it. Surfaced by that unit's capture.

- Gate the guidance-evidence pairing instead of leaving it to the audit — parked; no change yet. A rule can lose its
  `evidence:` token, or a record section its rule, with no gate failing: `verifyCapturedMarkers` enforces marker
  placement, while the three pairing scenarios live only in `agents-auditor`, which runs on demand or on the schedule. A
  `verifyGuidanceEvidence` rules task on that pattern would assert, per `docs/guidance-evidence/` section, that the rule
  whose bold lead matches its `Rule:` paragraph carries `evidence: <record>`, and that every such token names an
  existing record — with a `MODIFIED` delta on `showcase/quality/agent-skills`, since the pairing finding is currently
  the auditor's alone. Surfaced by the `capture-colima-local-network` capture, where the token was dropped to keep
  `AGENTS.md` flat and only a review round caught it.
- Extract the smoke-run obligations from `AGENTS.md` into their owner — parked; no change yet. The obligations are one
  bullet each after `correct-subagent-reload-rule` split them in place (its `design.md` Decision 5), so what remains
  here is the move, not the split. They belong with the review gate that runs the smoke-run —
  `.opencode/skills/review-gate/SKILL.md` or a `showcase/quality/agent-skills` requirement — so the move owes a spec
  delta, a destination carrying the complete contract, and a retarget of the `captured:` markers those obligations
  carry.

- Pin the thread-dump helper's fresh-path behaviour with a control — parked; no change yet. `ThreadDumpWatchdog` names
  each snapshot's file inside a fresh `Files.createTempDirectory` because `jcmd Thread.dump_to_file` refuses to write
  over an existing path: the repeat call prints `… exists, use -overwrite to overwrite` and exits **0**, leaving the
  previous dump in place. The helper discards the child's output and treats exit `0` as a complete snapshot, so a
  regression to a fixed path would serve a stale dump marked complete, silently. Nothing pins the naming —
  `ThreadDumpRegistrationCT` asserts the service-path registration and autodetection only. The control drives two
  consecutive snapshots and asserts each is a fresh, complete render. Surfaced by the 2026-10-25 capture, which routed
  this hazard to a check rather than to a rule in prose.

- Fix the definition's stale skill enumeration — parked; no change yet. `.opencode/agent/lesson-capture.md` says "Do NOT
  edit `AGENTS.md`, **either skill**, or a subject pack" while it names three skills (`check-and-test-authoring`,
  `ship-change`, `review-gate`), so the count predates one of them. The correction is a word; it surfaced in this
  capture's review as a stale fact the change passed by rather than caused, so its correction belongs to a docs change
  of its own.

- Check that a unit records its `Workspace:` line — parked; no change yet. `AGENTS.md` requires the record "before its
  first write", and it lives "with the unit's own record: its `tasks.md` when it has a change dir, otherwise its
  hand-off or report and then the pull-request checklist" — but no check enforces it: the commit guard reads the claim,
  and the record's other reader is the `pause-resume-unit` procedure, which reads it, confirms the tree against it, and
  stops on a disagreement — procedurally, with no gate behind it. The `reject-a-foreign-change-dir-at-commit` unit's
  archived `tasks.md` carries no `Workspace:` line at all, and this note's own unit recorded one only when a review
  round asked for it. The claim the commit guard reads is the record's machine-readable sibling, so a
  `verifyWorkspaceRecord` rules task — or a `commit-hygiene.py` check beside the claim's — could assert that a change
  unit's `tasks.md` carries the line, that it names the writing session rather than the reviewer's, and that its `path`
  and `branch` agree with the tree the claim sits in. A unit with no change dir keeps the record in its hand-off or
  report and then its pull request, which the current offline guard does not fetch, so that home stays with the review
  unless the record moves to a file a gate reads. Surfaced by the `clarify-the-refresh-recovery-route` unit's review.

- Probe whether v1's `permission.ask` denies a write before it happens — parked; no change yet. ADR-0024 records that
  the property "one owning session per tree" is not ensured: the v2 plugin surface has no tool or permission hook, the
  static config carries no session dimension, and the v1 legacy `Hooks` — `permission.ask` (output settable to `deny`)
  and `tool.execute.before` with `sessionID` — are plausible but unverified. Establishing that leg needs an isolated v1
  install, a plugin that denies, a session, and a control that detects a write undone before the filesystem is compared;
  only then can a change touch the gate's procedure. This entry closes when both `.github/workflows/opencode.yml` and
  `.github/workflows/audit.yml` leave the v1 line, which drops the legacy `Hooks` surface that carries `permission.ask`.

- Report OpenCode's missing MCP reconnect upstream — parked; no change yet. On 2026-10-26 a laptop wake left the network
  down while the Playwright server started: `npx` could not reach the registry, the connect stalled 346 s, and the
  server stayed `failed` for that workspace for ~2.5 h across new sessions, with no automatic retry.
  `POST /api/experimental/mcp/{server}/connect` repaired it in 1.4 s, and `opencode mcp` (2.1.4) exposes no connect
  subcommand, so the reproducer and measurements are the ones recorded in the `reconnect-failed-mcp` change and its
  `scripts/reconnect-mcp.py`. The report asks for bounded automatic recovery or a documented retry policy, plus the CLI
  surface. It retires when a released version retries a failed connect on its own (or exposes the operation in the CLI),
  not when an issue closes.

- Settle the README's "MCP config is read at startup, so restart OpenCode after adding one" against OpenCode 2.x's
  reload behaviour — parked; no change yet. The `opencode` skill describes configuration watching and a `/reload`, while
  `README.md`'s Tooling MCP Servers section (with `.opencode/commands/setup-agent-tools.md` and its skill) tells a
  contributor to restart. Neither side is verified here, and `reconnect-failed-mcp` deliberately narrowed its own
  wording to avoid asserting either (a configuration change is "a different condition" it points at the README for)
  rather than propagate an unverified mechanism. Settling it: on the installed 2.1.4 build (`opencode --version`), add a
  throwaway server entry, run `/reload`, and check whether `opencode mcp list` or the session's tools show it; if a
  reload suffices, the three sites are one docs PR. It retires when those sites state the behaviour the probe shows.

- Add the marker's removal to the definition-edit probe rule — parked; no change yet. `AGENTS.md`'s rule (refreshed by
  `correct-subagent-reload-rule`) saves a marker line in a definition and requires it in the reply to prove the
  registered body was applied, but names no removal step: a run that follows the four steps literally leaves probe text
  in a shipped definition, while removing it without a further check leaves the evidence attesting to a body the
  repository does not ship. The rule should end with the marker removed and a final probe requiring that no marker is
  defined, which is what confirms the marker-free body is the registered one. Surfaced by `unify-agent-language-policy`,
  whose advisor-definition edit applies the rule.

## 2026-10-25

- Decide the dead `handlebars` constraint on `:platform` — parked; no change yet. `platform/build.gradle.kts` declares
  `api(libs.handlebars)` in its `constraints` block, the catalog pins `com.github.jknack:handlebars`, and no main-build
  JVM subproject resolves the coordinate: `dependencies` across those sixteen (the four services and twelve others;
  `build-logic` and `helm` excluded) names it nowhere, so the constraint and its catalog entry are its only two
  references (`git log -S` shows one "dependencies updated" commit and no rationale). The npm graph's own `handlebars`
  is a different package, not this one. Surfaced by the 2026-10-25 dependency bump, which bumped it with the rest rather
  than deciding its removal — a constraint's rationale is the owner's to state, and the report would otherwise keep
  naming a coordinate nothing consumes.

- Revisit a pull-request title check when merged titles drift from the standard — parked; no change yet.
  `standardize-pr-conventions` wrote the title and body standard into `.github/PULL_REQUEST_TEMPLATE.md` with no gate,
  so a deviation reaches `main` unless a review catches it. The check is cheap — a `pull_request`-triggered step reads
  the title from the event payload — and its justification is drift rather than the rule's absence: add it when merged
  titles repeatedly leave the shape, where the pull-request list, the squash subject, and the generated release notes
  are the surfaces that would show it.
- Assert per platform that the doctor's install hint agrees with the README's Prerequisites column: the spec already
  requires the agreement and `test-doctor.sh` asserts only that a hint exists, so the check plus the wording
  reconciliation across the probe set — parked; no change yet.
- Classify `actionlint` as a required probe in the doctor, with its required-set assertion and the spec example: it is
  optional today although `check`'s `workflowLint` needs it — parked; no change yet.

- Extend `verifyTableRows` with an inline-code-span whitespace mode — parked; no change yet. A run of two or more spaces
  inside a single-line inline code span passes `spotlessCheck` (Prettier never rewrites a span's interior) and the
  120-character check measures length only; reproduced twice, by `fix-gateway-cors-allowed-headers` (#359) and by
  `bump-kps-chart-2026-10-25`'s five-space `kubectl get pods -n monitoring` span. Scope it to the Spotless markdown
  target (active `openspec/changes/*/` included, `archive/**` excluded), with those two spans as the known-bad cases and
  fenced blocks, delimiter rows and an escaped `\|` as the known-good ones; prototype over the gated set first, since
  one false-positive class would block it.

- Verify the worktree workspace rule on the first two real parallel units — parked; verification pending.
  `adopt-worktree-isolation` adds the rule and its recorded preflight, but the practical test needs two parallel units,
  which exist only after it lands. Completion:
  - each unit's tracked and untracked files stay in its own tree;
  - the primary stays the other unit's workspace;
  - the recorded preflight names the right tree and branch;
  - a sibling carries no `origin/main` upstream;
  - the first unit's cleanup leaves the second untouched.

- Require an audit finding's report item to carry evidence per proposition, and name the search that found a drift
  attribution — parked; no change yet. `docs/audits/2026-10-24.md`'s agent-tooling finding attributed the drift to two
  AGENTS.md changes when `extract-ci-narrative` (#644) removed it, and said the CI section points at the `gradle-build`
  pack when that pointer is in `## Build & Test`; the report contract asks for one line of evidence per item, so the
  finding satisfied it with evidence for the cross-reference while both causal clauses travelled unevidenced into the
  applying change's proposal. Proposed mechanism: an item states evidence per proposition it carries, and a drift
  attribution names the commit or change with the search that found it (`git log -S`), or is marked a hypothesis. The
  route is a change with a `MODIFIED` delta on `showcase/quality/agent-skills`' report-item requirement and the auditor
  definitions; surfaced during `fix-gradle-build-pack-cross-reference`.
- Re-check `.opencode/package.json`'s `toml`/`zod` overrides once `@opencode-ai/plugin` publishes a graph with no
  vulnerable `toml` (resolved at ≥ 5.0.1, or dropped) and `zod` ≥ 4.6.6 — parked; no change yet. The plugin pins
  `effect` 4.0.0-beta.83 (which declares `toml: ^4.1.1`) and `zod` 4.1.8 exactly, so the overrides force patched
  releases, and the gap is not reported upstream (the `AGENTS.md` venue rule records why). Decide with a probe:
  1. install the current plugin **without** the overrides in a throwaway directory;
  2. run `npm ls toml zod` and `npm audit` there;
  3. if that graph is clean and the condition holds, drop the `toml`/`zod` entries from `.opencode/package.json`.

- Bold-lead the plain rules a `captured:` marker has to land on — parked; no change yet. `scripts/commit-hygiene.py`'s
  marker check requires a lead starting `- **` or `**`, so the two plain `- Text` bullets a capture may need to annotate
  — the "Work done while awaiting the approval pass" bullet and the failure-handling bullet below it — cannot carry a
  marker at all; the fix is to bold-lead those bullets when a capture next merges into them, never to widen the guard,
  since accepting plain leads is what the check exists to prevent (`stage-new-files-before-manual-review`'s capture hit
  this).

## 2026-10-24

- Harden the `Recreate` upgrade against a foreign-owned `rollingUpdate` — parked; no change yet. The chart's
  `rollingUpdate: null` converts a Deployment the Helm release itself owns, but a live Deployment whose
  `strategy.rollingUpdate` is owned by another manager (a `kubectl` patch, a controller update) still fails the upgrade
  with `spec.strategy.rollingUpdate: Forbidden`, as the local baseline restore reproduced; deleting the Deployment first
  (Helm recreates it) or an explicit `--force` path belongs in ADR-0022's procedure.

- Add a Grafana panel for the projector's operating state — parked; no change yet. `projector-operability` exposes
  `showcaseProjector.state`, `.processing` and `.last.progress`; the dashboard's ingest-age panel is their natural home,
  but the panel is its own change so the metric contract lands first.

- Detect a stalled projector from consumer lag — parked; no change yet. `projector-operability`'s reported state
  distinguishes assignment from stream-restart waits, but an assigned receiver can remain `UP` while Kafka is
  unreachable or processing stalls, and an old progress timestamp alone cannot tell a stall from healthy idleness
  (ADR-0023); judging staleness needs the consumer's lag, i.e. a Kafka admin client the service does not have today.

- Correct the documented platform override for the image builds — parked; no change yet. The `deployment-operations`
  pack's Docker Images section gives `-PimagePlatform=linux/amd64`, but `-P` sets a Gradle project property and no build
  script forwards it to the task, while Spring Boot's `BootBuildImage` reads the value from a `--imagePlatform` task
  option (`@Option(option = "imagePlatform")` in 3.5.16). Surfaced while pinning the JVM builder
  (`pin-jvm-buildpack-builder`), which touched the same section for an unrelated reason; verifying the correct form
  needs a real build on each platform, so it ships as its own docs change.

- Assert that every catalog `paketo-*` pin has a `buildpackChecks` entry — parked; no change yet. `verifyDocumentedPins`
  derives its key set from `buildpackChecks`, and `buildpackUpdates` is the only check that reads a bare Paketo
  `[versions]` pin, so a new catalog Paketo pin added without an entry is neither reported stale nor required in the
  docs. A `check`-time assertion over the catalog's `paketo-*` keys closes it; parked rather than folded into
  `pin-jvm-buildpack-builder`, whose tasks and delta specs cover no new gate.

- Remove the shadowed `check_unique_review_rounds` definition — parked; no change yet. `scripts/commit-hygiene.py`
  defines it twice (the earlier without `expected`), so Python keeps only the later definition and the earlier is dead;
  the review of the ledger-checkpoint capture surfaced it. Deleting the dead definition needs the `commit-hygiene`
  spec's verification control to stay green.

- Guard every Deployment template's `Recreate` strategy the way the projection service's now is — parked; no change yet.
  The other four Deployments render `strategy` from their `updateStrategy` values without the `rollingUpdate: null` the
  projection service gained: a target that sets `type: Recreate` on any of them hits the same server-side-apply
  rejection on an upgrade (the live release's `rollingUpdate` block survives the apply and the API refuses the pair).
  `idempotent-projection-writes` only needed the projection service; the chart-wide guard is its own change.

- Rehearse the snapshot-repository upgrade path and a multi-node projection rebuild — parked; no change yet. The
  populated legacy upgrade and its rollback ran on the local cluster in `idempotent-projection-writes`, but that path
  used the clone checkpoint on a single-node OpenSearch; a deployment with a snapshot repository and several nodes
  exercises the snapshot restore and the shard allocation the clone path never touches.

## 2026-10-23

The audit findings carry-forward work remains parked under the 2026-09-24 section; this change does not re-enter it.

- Dead-letter queue with re-drive for terminal projection failures — parked; no change yet.
  `idempotent-projection-writes` classifies a bulk item's failure and retries only load rejections; a terminal failure
  is logged and its record acknowledged with the event's fields missing. A dead-letter topic with a re-drive command
  would surface each skipped event to an operator instead of a log line, but it needs the queue's retention and
  monitoring, the producer, and a re-drive that respects the aggregate's sequence — see the re-park below.

- Gap-aware repair of a superseded partial event — parked; no change yet. `idempotent-projection-writes` drains a batch
  one event per aggregate per round, so a retry cannot be suppressed by a later event; a terminal failure, however,
  still leaves the failed event's fields missing, and replaying that event through the guard is a no-op once a later
  event has advanced the document. Repairing it needs the aggregate's state reconstructed (from its event stream or the
  event store) rather than the single event replayed; this is the piece a dead-letter re-drive alone cannot provide.

- Read-model rebuild with its runbook — parked; no change yet. The upgrade path in ADR-0022 deletes the index and
  replays the topic only when the retained history is complete, and aborts otherwise. A rebuild facility would make the
  read model reconstructible without that precondition (from the PostgreSQL event store or a retained topic), with a
  documented runbook and a way to verify completeness.

- Consolidate the capture requirement's scenarios into their definition — parked; no change yet.
  `consolidate-review-gate-ownership` split the requirement and carried the four capture scenarios, three verbatim and
  the first with the narrow `review-gate` retarget (a destination and an existing-rule entry); the first still restates
  the promotion gate, the evidence threshold, the decision-difference test, and the growth bound that
  `lesson-capture.md` owns. Route: the ownership model the review gate pilot established — the procedure in the
  definition, the spec stating outcomes — with the same per-obligation trace before any clause is removed. The retarget
  the extraction rule required is done: the destinations and the existing-rule enumeration both name the `review-gate`
  skill, so a gate-procedure lesson has a listed destination. The record's own hand-listed enumeration stays here.

- Give an unparseable quick-review report an explicit recovery in the review-gate skill's stage 2 — parked; no change
  yet. The registered-definition probe for `consolidate-review-gate-ownership` verified the narrow gap:
  `review-quick.md` requires a verdict line and a machine-readable record line, while the skill's stage 2 states no
  explicit recovery for output that arrives without them. Whether that gap can produce a false pass is untested — the
  stage still requires a round row, its checkpoint, and the reviewer's own report of no new observations, so this pilot
  asserts no such outcome. Route: the `review-gate` skill's stage 2, extending its preflight; the probe's proposed text
  is a starting point, not an accepted rule.

- Consolidate the diagram and vision scenarios into their definitions — parked; no change yet. The four diagram
  scenarios carried verbatim by the same change restate the `vision` definition's review groups and measurement rule,
  and the `diagrammer` definition's mapping, hand-off, and render rule. Same route: one owner per obligation, the spec
  pointing at the definition.

- Decide what a retrospective may state when the evidence cannot establish a cause — parked; no change yet.
  - **Gap:** the `agent-skills` requirement asks the retrospective to trace each significant failure to a root cause,
    drawing on the digest's review-round data, and does not say what to state when that data — class and round counts —
    cannot establish one. The report can then state a cause the evidence does not support.
  - **Evidence:** the first draft's over-claims were rejected before review, and round 1 recorded three `over-claim`
    findings. That shows the risk is real; it does **not** establish that the requirement produced it — the instruction
    → wrong decision → different action link is missing, and `lesson-capture.md` requires that link before an
    authoring-system gap is called established.
  - **Rejected in this unit:** the same capture proposed replacing the "the digest is authoritative, read its figures
    rather than re-derive them" clauses, and a conditional `AGENTS.md` replacement. The first is rejected: using an
    authoritative source and verifying its derivation and resolving its declared boundaries are compatible, the 63/59
    difference is a boundary the digest already asks the author to resolve, and recomputing every figure per report
    would replace the single fact producer without a demonstrated need. The second was not applied: it would contradict
    the requirement without that spec change.
  - **Route (if the gap is confirmed):** a `MODIFIED` delta on `showcase/quality/agent-skills` plus the
    `experience-analyzer` definition, in one change. No change now.
  - **Revisit when:** a concrete instance supplies the link `lesson-capture.md` requires — the requirement's root-cause
    clause (the instruction), the draft's decision to state a cause the counts cannot support (the wrong decision), and
    the action the proposed wording would change, stating a hypothesis with what would settle it (the different action)
    — with that instance named in `docs/review-rounds.tsv`'s rows for the unit or in the pull-request checklist line
    carrying its process-defect record (internal trigger, at that coordinate) **and** the 2026-10-23 pause lifted
    (external coordinate: the P1 acceptance in `docs/retrospectives/2026-10-23.md`).

## 2026-10-22

- Single-source the vendored-skill set — parked; no change yet. Which trees under `.opencode/skills/` are vendored is
  declared four times: the Spotless `targetExclude` and the marker task's input file tree in `build.gradle.kts`,
  `VENDORED_SKILL_PREFIXES` in `scripts/commit-hygiene.py`, and `EXCLUDED_SKILL_PREFIXES` in
  `build-logic/src/main/kotlin/UpstreamReferenceScope.kt`. `vendor-ast-grep-skill` updated every one of them and named
  them in `AGENTS.md`, but the next vendored set repeats the sweep. The fix is one declaration the build, the checker,
  and the Kotlin scope each read. Revisit when a third set is vendored, or when one of the four drifts.

- Gate the formatter-blind inline-code interior — parked; no change yet. Prettier never rewrites an inline span's
  interior, and neither `spotlessCheck`, the 120-character recipe, nor the pre-commit guard looks inside one, so a
  whitespace run there passes every gate (the `Formatting` convention says so and it has now recurred across units). The
  fix is a check over the Spotless markdown scope with its known-bad/known-good cases in a `*RulesTests`, after which
  the convention's "proofread inline-code content" clause trims to a pointer. Revisit as its own change; it is a
  build-logic addition, not a copy edit.

- Guarantee the ledger row is written before the fixes with a reviewer wrapper — parked; no change yet. A `/consult`
  report's closing prompt was once applied before the owner decided, and the round's ledger row is repeatedly deferred
  (five misses in two units), which costs extra review work when the next reviewer reports it.
  `extract-deployment-operations-guidance` recorded the cause as the fix competing with a separate bookkeeping act. The
  feasibility pass for a wrapper (`opencode run --agent review-quick`, withholding findings until the row is written)
  found: `mode: all` makes the same definition directly runnable in v1.19.3 and v2 (the refusal is `mode: subagent`'s
  entry point, not the runtime); v1 continues a session by id and fails hard on a missing one (`Session not found`)
  while v2 silently creates; the reviewer's model pin and the cloud action's own path remain unverified.
  `check-review-round-contiguity` ships the cheap net instead (contiguity + an expected-round checkpoint). This idea's
  revisit condition is superseded by the reconsideration below.

  **Reconsidered 2026-10-27**, on the `clarify-the-refresh-recovery-route` unit's review:

  - **Evidence:** according to the caller, that unit's round-14 report arrived without its row being appended, and its
    closing boundary ran only `--expect-clean`, which reads the highest recorded round and so passed. The repaired
    ledger carries the rows but no append times or command transcript, so it does not evidence the sequence itself.
  - **Disposition:** deferred, not rejected. The checkpoint obligation already exists — the review gate requires "repeat
    both" before the manual pass — and `check-review-round-contiguity`'s design records that a wrapper buys ordering
    only within its own route and cannot prevent the Task-tool path.
  - **Revisit when:** either internal trigger is recorded:
    - `docs/review-rounds.tsv` carries an `Acknowledged gap` for a report received after `check-review-round-contiguity`
      landed, whose reason names where that unit recorded the recovery attempt — its `tasks.md`, or its pull-request
      checklist when it has no change dir — and that record states the row fields that could not be recovered and the
      report or hand-over sources it checked.
    - The owner explicitly requires technical prevention of the Task-tool bypass, recorded in a unit's `tasks.md` or
      pull-request checklist.

  Either trigger reopens the design decision; neither commits to implementing the wrapper.

- Route the wrapped-prose gotcha to where prose edits retrieve it — parked; no change yet. The rule sits under
  `### Git & repository workflow`, but the edits that keep failing it are prose edits in `docs/` and a change dir; a
  pointer from the `Formatting` convention or a move into a prose-editing section is an `/audit-agents` route candidate.
  The retrieval gap is a hypothesis rather than a finding: the remedy is explicit and unconditional, so it cannot be
  confirmed as the cause of the recurrence.

- Confirm the `pack 0.40.10` pin on its first scheduled image build — parked; no change yet.
  `bump-tooling-pins-2026-10-22` bumped it in `e2e.yml`, `release.yml` and `deployment-smoke.yml` without dispatching a
  run that installs it, because a whole-cluster smoke to prove one CLI tag is not a proportionate pre-merge cost; the
  first scheduled run that installs it is the verification, and a red one there is this pin's to answer for. Revisit if
  that run does not install the tag, or if the web UI image build regresses after the bump.

- Retarget the guidance-evidence record's stale `#755` location — parked; no change yet.
  `docs/guidance-evidence/documentation-and-durable-claims.md` says `#755` "stays cited in ADR-0007, `README.md`, and
  `## Build & Test`", but the citation moved to `.opencode/skills/gradle-build/SKILL.md` in `6518d081`; `AGENTS.md`
  holds no `#755` (only `100755`). `extract-deployment-operations-guidance` edits the adjacent `#1340` clause and
  deliberately leaves this one, so the fix is its own docs unit: name the `gradle-build` pack.

## 2026-10-20

- Convert the remaining Git-tranche Gotchas rules — parked; no change yet. `convert-remaining-gotchas-rules` completed
  the `### Documentation & durable claims` tranche: all eleven remaining eligible rules there converted. The conversion
  alone was **−8,269 bytes (2.49%)** of root text for 10,861 bytes of record; with the implementation capture's two
  clauses (**+701 B**) the delivered unit is **−7,568 bytes (2.28%)** at 324,296 B / 3,361 lines, the record 16,608 B,
  and total maintained content **+3,293 bytes**. Its `design.md` drafts all six `### Git & repository workflow`
  candidates as well, each measured against `AGENTS.md` at `e07f5f8f`: C1 `git stash pop` −692, C2 `git reset --hard`
  −461, C3 `openspec archive` −256, C4 programmatic edits −1,039, C5 the source range-boundary −270, C6 never chain an
  edit −1,004 — **−3,722 bytes (1.12%)** of root for **5,812 bytes** of record (ratio 0.64, against the shipped
  tranche's 0.76). The tranche is a candidate for one unit of its own; a mixed per-rule selection was measured (−9,006 B
  root, +2,171 B maintained) and set aside in favour of a single-subsection boundary. R3 (`formatter-owned-file`, net
  −69 B with a token) stays deferred: its provenance is already carried by the em-dash record, so nothing beyond the
  byte saving argues for it. The four rules with no separable material and the one rejected on a retrieval failure
  remain settled. Re-measure against the file before acting.

- Extend the extraction conditions so the retained pointer identifies each converted unit — parked; the subject-scope
  class now carries the association, other classes still do not. `separate-agents-md-operational-from-evidence` found
  that a destination-level pointer satisfies every extraction condition while leaving one direction of the pairing
  unverifiable: an unconverted unit and a unit whose destination entry was deleted look alike. Its own destination
  solved this with a per-rule `evidence:` token, and the subject-scope class applies a per-pack `Source:` line; the
  general condition in `showcase/quality/agent-skills` still permits a bare pointer, so the other destination classes
  inherit the gap. The fix is one added condition in that requirement's extraction-candidate paragraph, with a scenario;
  route it as its own change with the delta.

## 2026-10-19

- Seed a smoke-run's suppression control with a distinct action, not a verbatim existing rule — parked; no change yet.
  `sharpen-capture-review`'s first suppression seed was the existing conditioned failure-handling bullet copied
  verbatim, which the reloaded screen correctly flagged as a restatement (`single-sourcing`) rather than leaving
  unflagged; the valid seed (the change's own new `AGENTS.md` clause) was then left unflagged. The recovery is already
  prescribed — the subagent/smoke-run gotcha says to re-seed and record which branch the run exercised, which the unit
  did — and the occurrence is single and non-severe, so it is below the capture gate for `AGENTS.md`. Parked as a
  convention candidate: if a second suppression seed repeats the mistake, add one clause to that gotcha ("seed a
  distinct action; a verbatim existing rule is a must-report control").

- Carry the retrieval-path result in the audit ledger's "safe to load on demand" class — parked; no change yet. The
  ledger contract (`showcase/quality/agent-skills`) requires each entry to state its trigger and retained pointer, but
  not whether a destination can carry the rule, so the 2026-10-19 report lists five such rules while its own candidates
  section says no destination was verified — a reader inherits an untested pointer (the three remaining entries were
  verified to fail the retrieval path while implementing `add-extraction-procedure`). The fix is to the mechanism that
  emits the report — the spec requirement and the `agents-auditor` definition — so each entry carries the retrieval-path
  result or states that it is untested, not to how an agent reads it; an `AGENTS.md` warning would not close it. Parked
  rather than built: it needs an `agent-skills` delta and the auditor definition's refresh.

- Run a configuration-cache store sweep over the tasks the workflows invoke — parked; no change yet.
  `fix-upstream-references-config-cache` restored `upstreamReferences`'s cache compatibility, but the class it belongs
  to is unreachable by anything the build runs locally: `./gradlew tasks --all` realizes the graph without executing a
  task action, so a task no `check` graph requests — one invoked only by its own workflow — is never executed by any
  sweep the build itself can perform, and its store defect surfaces only in CI, on a schedule, with no gate and no actor
  to notice it (#581 missed `upstreamReferences` for a week). A job that runs each workflow-invoked task with the cache
  enabled and requires a stored entry, then a reused one, would close it — in the scheduled pattern rather than `check`,
  since the set is credentialed and networked (`dependencySecurityCheck` needs `SNYK_TOKEN`, `upstreamReferences` needs
  `gh`, `helmInstallToCi` needs a cluster) and cannot be a pull-request gate. Parked rather than built: it needs its own
  delta and a control, and its set is derived from the workflows' own `./gradlew` invocations, so a newly added
  workflow-invoked task joins it by construction.

- Enforce the command lifecycle preflight with a check rather than prose in seventeen files — parked; no change yet.
  `harden-command-definitions` added the requirement that a project-authored command starting a mutating unit states the
  lifecycle its unit owes, and wrote it into ten command definitions, but nothing enforces it: the drift it repairs
  (four of five apply-finding commands running the formatter and a quick review instead of the full review gate) was
  invisible to every gate for many changes, and a rule copied into N files is the drift surface the single-sourcing
  bullet names. A `verifyCommandLifecycle` task in `check`, in the `VerifyDocumentedPinsTask` shape, would declare the
  mutating command set beside the check and fail when a declared command's text lacks the lifecycle anchors the
  requirement names (the freshly-fetched `origin/main` branch rule and the approval clause, matched as tokens) or when a
  declared entry names no file, so the list cannot go stale by deletion. Parked rather than built: it needs its own
  `commit-hygiene` or `agent-skills` delta and a `*RulesTests` control, and the judgment of _which_ commands are
  mutating stays human.

## 2026-10-18

- Widen `ci.yml`'s OpenSpec probe to fail on the CLI's "is not a delta section" warning — parked; no change yet.
  `openspec validate --all` exits 0 while warning and `openspec archive` drops the requirement, so a misnamed delta
  header is a silent trap — the config's `rules.specs` named `DEPRECATED` until `fix-openspec-config-rules`, where the
  CLI's fourth delta operation is `RENAMED`. The probe can grep `validate --all`'s output in the shape it already uses,
  and the change that trips it can remediate its own header. This subsumes the weaker "check the config's rules against
  the CLI's operation set" park, since a drafter following a wrong config name writes exactly this header.

- Assert the config's cross-artifact clause parity in a committed check — parked; no change yet. The `rules` are
  injected per artifact alone, so a clause several artifacts need is repeated identically, and nothing detects drift
  between the copies or an absence where a recorded failure happened — `fix-openspec-config-rules` found both by reading
  all four lists by hand, while `ci.yml`'s probe is shape-only. A check in the `allPropertiesHaveDocumentedDefaults`
  shape asserting a declared list of cross-artifact clauses appears verbatim in each artifact's `rules`, which the CLI
  returns byte-identical across `proposal`/`design`/`tasks`.

- Add `verifyUniqueChangeNames` to the commit-hygiene `verifyUnique*` family — parked; no change yet. The merge-time
  capture after `fix-openspec-config-rules` (#624) found its name collides with an already-archived change
  (`2026-09-14-fix-openspec-config-rules`, #218), so `git log --grep` matches both and a reader looking for "the change
  that fixed the config rules" finds two. The lesson fails the promotion gate for a prose rule — a duplicate-name scan
  is trivial to automate, and the evidence is one non-severe occurrence in 334 archived changes — so its home is a check
  rather than `AGENTS.md`. The check is a `check_unique_change_names` mode in `scripts/commit-hygiene.py`: strip the
  `YYYY-MM-DD-` prefix from `openspec/changes/archive/*` and fail on a duplicate, registered beside
  `verifyUniqueCronSchedules` and `verifyUniqueReviewRounds` and wired into `check`. It owes a
  `showcase/quality/commit-hygiene` delta. Parked rather than built because those siblings guard data integrity while
  this one guards naming hygiene. The generated `openspec-propose` skill already guards against a collision with
  **active** changes — "If a change with that name already exists, ask if user wants to continue it or create a new one"
  — and the archive, being date-prefixed, is invisible to it.

## 2026-10-17

- Check that every recorded class is in the reviewer's canonical list — parked; no change yet. The 2026-10-15
  retrospective's S2, the remaining enabler of the window comparison its baseline names. The label set lives as prose in
  `review-quick`'s class list, and the retrospective found it drifting across the record — which matters because the
  whole convergence signal is label comparability. Add a rules-object check carrying its known-bad and known-good cases
  (the `*RulesTests` shape) asserting that every class in a `docs/review-rounds.tsv` row recorded on or after the
  canonical boundary is in that list, so a genuinely new class is added to the contract before it is recorded. Routes to
  a `code-quality` requirement plus an `agent-skills` delta.

- Replace the chart-packaging and install-path workarounds with the plugin's own retried fetches — parked; both
  workarounds are in place and the retry is open upstream as `build-extensions-oss/gradle-helm-plugin#240`; not yet
  merged. The gate's repository setup and dependency fetch run through the repository's `FetchChartDependenciesTask`
  (`helm/chart/build.gradle.kts`) and the release-install path's through the root `UpdateHelmRepositoriesTask`, each
  retrying its network steps a bounded number of times; the plugin's own `HelmAddRepository`, `HelmUpdateRepositories`
  and `HelmUpdateDependencies` tasks have no retry and cannot be wrapped. The proposal covers the three fetch tasks and
  a `repo update` timeout, and it follows issue `build-extensions-oss/gradle-helm-plugin#238`. Retire the install-path
  workaround when a plugin release carries the retry and this build turns it on (`attempts`), since the proposal
  defaults to one attempt; retire the chart-packaging workaround on such a release only once the plugin's dependency
  task also stops skipping under the configuration cache (the 2026-10-16 entry).

## 2026-10-16

- Report the plugin's cache-dependent chart-dependency skip upstream — the build no longer runs the task's fetch; the
  report stays. The `helmUpdateMainChartDependencies` task (which the plugin's docs equate with
  `helm dependency update`) silently skips when the **configuration cache** (#581) stores its entry before the chart
  sources are collected — a clean checkout or CI: its `onlyIf` resolves false there, so nothing fetches the chart's
  external Bitnami `common` dependency, `helm/chart`'s packaging produces a chart with no `charts/`, and both the strict
  lint and the deployed install fail. The repository's `FetchChartDependenciesTask` replaced the task in the packaging
  path, so the workaround below is moot here. Measured on one commit with only the cache toggled: SKIPPED with the cache
  on, fetches with `--no-configuration-cache`. The workaround was `setOnlyIf { true }` in `helm/chart/build.gradle.kts`
  and was removed when the task became a disabled bridge; `build-extensions-oss/gradle-helm-plugin` has issues enabled
  (`has_issues: true`, `has_discussions: false`), so a fix belongs there as an issue or a pull request.

## 2026-10-15

- Spec the owner-notification path for a bot-authored pull request — parked; no change yet. `merge-governance` owns the
  rulesets, the CI gates and the scheduled workflows, but nothing states how the owner learns that a Dependabot PR is
  open — and that path was dead on arrival: GitHub retired `dependabot.yml`'s `reviewers` option on 2025-05-20 and `#91`
  added the key anyway sixteen months later, so no review request (and no notification) was ever raised until `#586`
  replaced it with `.github/CODEOWNERS`. A requirement would state the outcome — a pull request the code owner did not
  author requests their review — while leaving the mechanism free to change, which is what the repo's own experience
  says it will do.
- Park the review loop's rejected levers, and the comparison its baseline enables — parked; no change yet. Four levers
  the owner's proposal for optimizing the iterative review loop asked for are **contradicted by the repository's own
  record**, so they are not to be re-derived the next time the question is asked: a **persistent reviewer session as a
  substitute for the per-round instance hand-off** (the loop's measured cost is re-raised claims about the change's own
  artifacts rather than re-exploration, per the retrospective's baseline, so reusing a session buys little; and the
  class-exhaustion clause requires searching the artifact anew each round, so a session's prior context must not stand
  in for that search). The session itself is the caller's to reuse: the 2026-10-16 runs reused one across rounds to good
  effect, and the day's one fresh-session loop was fresh only because its sessions had expired; **diff-scoped
  follow-ups** (an unapplied fix produces no diff, so a diff-only round can report a false clean); a **fixed iteration
  cap on the quick loop** (`bound-the-review-loop` explicitly rejected one, since a count would stop a productive loop);
  and **token, elapsed-time or session-count telemetry** (a process count is not a verifiable repository fact, per the
  documented-numbers rule). The one change the record did support — the canonical class list — has since landed. The
  companion measurement: `docs/retrospectives/2026-10-15.md` records the review loop's baseline — the figures and the
  boundary that makes them quotable live there rather than here, so the two cannot drift apart — to be compared against
  the first window whose rounds are both post-lever and canonically labelled. That comparison is what should decide any
  further **quick-loop** change, not a fresh proposal — the bound `rework-the-review-loop` adds to the quick loop is a
  scope and a stop condition rather than a count, so it stays measurable against the baseline. The owner overrode the
  wider reading for the **deep pass**, which that change bounds at a default of two full passes with an explicit
  recorded extension: a deep-pass budget, not the quick loop's cap, and an owner decision rather than a re-derivation.

- Measure whether the review loop's non-convergence is model-bounded — parked; no change yet. The loop's reviewer shares
  the main agent's model (the model-pin gotcha), so it cannot find what that model cannot see — plausibly why
  `contradiction`, `over-claim`, and `premise` survive it. The decisive cheap experiment is to run one unit through both
  `review-quick` (flash) and `review-thorough` (pro) and diff the _classes_ each finds, which would show whether those
  classes should escalate to the pro pass.

- Re-check the Gatling plugin's configuration-cache gap — parked; no change yet. The `gatlingRecorder` task (the
  plugin's `GatlingRecorderTask`, the pinned Gatling Gradle plugin's own task) holds a `Configuration` and a
  `SourceSet`, which the configuration cache cannot serialize, so `load-tests/build.gradle.kts` marks it
  `notCompatibleWithConfigurationCache` — a graph containing it runs cache-less. The plugin's own "support gradle
  configuration cache" work (#131, a merged PR) left this task uncovered, and the plugin repository's own tracker and
  discussions are disabled (`has_issues: false`, `has_discussions: false`), so it carries no report thread and no PR
  addressing it to cite; a fix reaches it only through a pull request. Re-check with
  `./gradlew :load-tests:gatlingRecorder --configuration-cache`, and retrigger on a release whose recorder task no
  longer holds those types.

- Widen the reviewers' 120-character check to every changed file — parked; no change yet. `review-quick.md` and
  `review-thorough.md` both scope it to the files the formatter does not cover (YAML, and so on), treating everything
  else as formatter-gated, which excludes Java — yet the Formatting convention records that Javadoc and JSDoc prose is
  not reflowed and only the manual check catches it, and two 122-character Javadoc lines passed `spotlessCheck` until an
  explicit scan found them. Name Javadoc prose in both definitions, and have the recipe iterate the changed set (split
  correctly — an unquoted stored value does not word-split in zsh).

## 2026-10-12

- Report Gradle's `--info` environment dump upstream — parked; no change yet. `--info`/`--debug` logs each exec'd
  process's full environment (values included), so a diagnostic run puts `SNYK_TOKEN`/`GH_TOKEN`/`NVD_API_KEY`-style
  values into captured output; the owning repository is `gradle/gradle`, and the reproduction is
  `./gradlew --info :helm:chart:helmLintMainChart` (7 `with environment:` lines). The durable rule is in `AGENTS.md`;
  this entry tracks the upstream report.

- Correct the `architecture-auditor`'s deferral check for renamed and compound ADR gates — partly landed
  (`sharpen-architecture-audit`): the compound-gate axis is covered, the renamed-coordinate axis is still open. Its
  definition resolves the coordinates the `Revisit when:` line names, and the `agent-skills` scenario frames the check
  as an external coordinate gate whose pinned coordinate published a newer major — so a gate whose coordinate upstream
  renamed reads as permanently unmet (it resolves a frozen artifact), and a compound gate can read as met on one axis
  while a sibling clause is open. The 2026-10-11 advisory read ADR-0011's gate that way. Routes to
  `.opencode/agent/architecture-auditor.md` and the scenario, which owes that capability's delta.

## 2026-10-10

- Confirm the vendored migration skills' model pins resolve — parked; no change yet. The vendored `axon4to5-*` skills
  carry frontmatter model pins (`axon4to5-isolatedtest` → `haiku`, `axon4to5-openrewrite` → `sonnet`, Anthropic aliases)
  while most project-authored model pins are `opencode-go/deepseek-*` (the vision and technical-advisor pins are
  deliberate exceptions). Surfaced by the 2026-10-10 `agents-auditor` smoke-run as a low-confidence advisory: the harm
  is a hypothesis until it is established whether OpenCode applies (or how it resolves) a skill's frontmatter `model` —
  a triggered skill may ignore it. Confirm that before acting; if it applies, the fix is a re-vendor decision (the
  skills are copied verbatim from `AxonIQ/agent-skills`), not a local edit.

## 2026-10-08

- Gate the `@NullMarked` package-info convention — parked; no change yet. NullAway 0.14.3 (the pinned version) ships the
  `RequireExplicitNullMarking` Error Prone checker, which reports a top-level class unless it or its package is
  explicitly `@NullMarked`/`@NullUnmarked`, so enabling it (`-Xep:RequireExplicitNullMarking:ERROR`) would enforce the
  convention mechanically. It needs its own change: it is a separate Error Prone check from `NullAway`, so the existing
  `disable("NullAway")` on `*TestJava` tasks does not disable it — enablement must be scoped to the non-`TestJava`
  branch (or the test packages marked), which is a real investigation.

## 2026-10-06

- Explain or eliminate the gateway controller CT's load-sensitive hang — parked; the diagnostic fired on 2026-10-25
  (below), the failure is now frequent enough on CI to bisect, and the unit that hit it withdrew its candidate hold and
  shipped an in-flight dump instead, so the mechanism stays open with the next stall's data as the next step.
  `ShowcaseRestControllerCT` fails with a `TimeoutException` at the `WebTestClient` `.exchange()`: 38 of its 76 cases at
  the default 5s timeout (while #514 was in flight, 2026-10-06), and once on the Docker-free PR gate at the 30s the
  slice configures (2026-10-15), so that raise did not hold there. The mechanism is unproven and does not reproduce
  locally: shrinking the JVM-wide `Schedulers.boundedElastic()` pool to 4 and running the suite six times all passed,
  which leaves boundedElastic starvation unconfirmed rather than refuted — a local pass is no evidence against a CI-only
  cause. The failed CI attempt ran the suite concurrently with the web UI's `check` and spotbugs on a 4-vCPU runner (the
  #514 starvation shape), and the suite's CT classes share one JVM (the #301 interference shape); JUnit kept dispatching
  distinct tests, each new one timing out on a 30s cadence for four minutes, so the stall is in the request path's
  threads rather than the JVM. The diagnostics are armed: `ThreadDumpOnFailure` (since `e3e6475e`, 2026-10-15) prints
  every thread's stack trace on a failure, and the suite's `showStandardStreams` carries it to the console — that
  setting also prints every test's standard output and error, a deliberate cost to remove with the extension once the
  mechanism is found. As of 2026-10-22 `ci.yml` had completed 221 runs since that commit with no failure (the one
  cancelled run is a duplicate `push` event on a single merge commit), so no dump exists to read and nothing here is
  diagnosable yet; whether the failure is simply rare or the runner's load changed is unknown, and the zero is not
  evidence of a fix. Revisit on the first CI failure of `ShowcaseRestControllerCT`: the log's
  `=== thread dump after failure: … ===` marker is the material to read, and a failure carrying no such marker is itself
  the finding that the diagnostic did not fire.

  **The revisit fired on 2026-10-25** (`bump-dependencies-2026-10-25`'s first CI run, `1dd4f1ce`): 38 of its cases and 7
  of `TelemetryControllerCT`'s failed, all with the same `TimeoutException` at `.exchange()`, one 30s cadence apart, and
  45 dumps were captured. The dumps looked like the finding — 17 threads, none in the request path — but they cannot be:
  this application enables virtual threads, Reactor's `boundedElastic` therefore runs on them (measured in the failing
  slice as `loomBoundedElastic-1`, `virtual=true`), and `Thread.getAllStackTraces()` omits virtual threads. So the "idle
  JVM" reading is an artefact of the instrument, and the boundedElastic-starvation question is untouched by it (the pool
  does exist, on virtual threads). **Neither the mechanism nor the frequency is established**: three failures in six
  attempts of the delivered configuration, then six consecutive green runs of the same tree once the diagnostic's dump
  changed — which touches no test behaviour. Next diagnostic: a snapshot taken _while_ a request is in flight, from a
  source that includes virtual threads (`jcmd Thread.dump_to_file`); the failure-time dump can only show what remains
  after the timeout, and it cannot see them at all.

  **A CI bisect raised a suspicion about `reactor-blockhound` `1.0.18.RELEASE`** — a separate catalogue coordinate
  bumped in the same update set as `reactor-bom` 2025.0.8 (not a member of that BOM), and the row the 2026-10-25 bump
  unit briefly held back and then shipped. Two failures in three runs of the bumped pair, against three green runs with
  `blockhound` at `1.0.17.RELEASE` — two at the same `reactor-core` 3.8.8 (the bisect run and the control run, 12m29s),
  one at 3.8.7; `blockhound` 1.0.18 with `reactor-core` 3.8.7 was not tried, so the interaction with 3.8.8 is untested,
  and each cell of that table is one to three runs. A local A/B then left the version unmoved: with platform threads the
  validation path fails identically on 1.0.17 and 1.0.18 (15 cases, all
  `BlockingOperationError: Blocking call! java.io.RandomAccessFile#readBytes`), and with virtual threads neither version
  flags it. The hold was therefore a precaution, and the 2026-10-26 bump unit **withdrew it by owner decision**,
  shipping `reactor-blockhound` `1.0.18.RELEASE` with a `ThreadDumpWatchdog` whose snapshot comes from
  `jcmd Thread.dump_to_file`; that control **passed on the runner** — a temporary probe parked `vt-runner-control`, and
  the CI log carried one `(virtual threads included)` snapshot with its `VirtualThread.parkNanos` stack and no
  `INCOMPLETE` header. `reactor-core` 3.8.8's own discriminator has not been run. The reproduction drafted for
  `reactor/BlockHound` stays unpublished: no evidence of a defect in a specific version has been established.

  **Reopening rule:** the next stall — of either gateway controller suite — is read from its in-flight snapshot; an
  `INCOMPLETE` header or a missing snapshot is a separate observability problem, not evidence about the stall.

  **First red run under the proven diagnostic** (2026-10-26, the archive commit's run `40255154046`, head `99592fb7`;
  the build job 25m34s, the run 25m38s; the gateway component-test task 124 tests, 42 failed): 157 in-flight snapshots
  and **all of them complete** — the `jcmd` path never degraded — yet **no virtual thread appears in any snapshot**. The
  JVM shows its platform threads (`Test worker` parked in `Mono.block()`, `parallel-N`, `ForkJoinPool-1-worker`,
  Gradle's IPC workers, the watchdog) and nothing in the request path. Read narrowly, that is what the snapshots
  contain, not what happened between them: a snapshot cannot tell a task that finished from one not yet run, nor whether
  the request reached the elastic scheduler at all — and the two failing slices are configured differently (the REST
  suite scans `showcase.api.rest`, the telemetry suite includes only its own controller), so one slice's execution path
  does not carry over to the other. What the run establishes is that the diagnostic works where it matters (157 of 157
  complete) and that these states show no worker to blame. The open question is where a request stops progressing, and
  answering it needs a correlated per-request event sequence (subscribe, scheduling, execution, terminal or cancel)
  rather than more snapshots.

## 2026-10-05

- Guarantee the weekly reports against a dropped or delayed `schedule` — parked; no change yet. The `schedule:` trigger
  is best-effort (delayed or dropped under load — a recent run slipped ~3 hours), and no `cron` value makes it punctual,
  so `reschedule-weekly-workflows-earlier` only widens the margin. A dispatch-fallback would turn "likely" into
  "present-or-retriggered": one scheduled watchdog that, after the expected window, checks via `gh` whether each weekly
  workflow produced its artifact for the current cycle (the audit's dated report/PR, each update check's tracker issue)
  and calls `gh workflow run <file>` for any that is absent — idempotent, so a healthy week is a no-op. Needs
  `workflow: write` + `issues: read`, and owes a `merge-governance` spec delta (a new scheduled workflow). Deferred
  while the reschedule's added margin is judged sufficient; its own change when the guarantee is wanted.

- Revisit ADR-0003's Jackson-3 deferral gate — parked; no change yet. The 2026-10-04 architecture audit reports the
  recorded `Revisit when:` condition ("the pinned `org.axonframework` and the Elasticsearch Java client … resolve
  Jackson 3") appears substantially met: `elasticsearch-client-java = 9.5.5` declares
  `tools.jackson.core:jackson-databind`/`jackson-core` as non-optional runtime dependencies, and
  `axon-framework = 4.13.2` now declares `tools.jackson.core:jackson-databind` as an **optional** dependency — Axon 4.13
  gained Jackson-3 support since the ADR (2026-08-18) characterized it as "targets Jackson 2". Open question: is the
  gate met, or does Axon's support being opt-in (not the default) mean it is still not? A decision, not a defect: if
  met, the Jackson-3 backend migration becomes due for re-planning (currently deferred behind Spring Boot 4, ADR-0004).

- Make the audit workflow's report filename collision-safe — parked; no change yet. The prompt names the report for
  "today's date", so two runs on the same UTC day write the same `docs/audits/<date>.md` and the second silently
  overwrites the first: the 2026-10-04 scheduled run reused the dispatch run's report filename (`#494`), which the
  archived `sync-audit-spec-findings-2026-10-04` change and the scheduled report's own "prior finding" reference cite,
  so the scheduled run's report had to be renamed to `docs/audits/2026-10-04-scheduled.md` (`#500`) to preserve both.
  Routes to `.github/workflows/audit.yml`'s prompt (name a same-day second run distinctly, e.g. by including the run's
  trigger) and the `merge-governance` spec requirement that owns that workflow's report behavior.

## 2026-09-30

- Retire the OpenSpec config probe and the `/opsx-tool-update` re-verification once `validate` checks the config —
  parked; no change yet. `Fission-AI/OpenSpec#1891` closed 2026-09-29, but its fix `Fission-AI/OpenSpec#1894` is still
  unmerged and in no release (no released CLI has `inspectProjectConfig`), so the `ci.yml` probe and the
  `/opsx-tool-update` re-verification remain the only guards against a config the CLI silently drops. The watch
  condition is a release carrying `#1894` (and `#1892`'s unparseable-config half), checked when the CLI is bumped rather
  than on the next weekly sweep. The `#1891` status is recorded inline in `AGENTS.md`'s config-read-path gotcha.

## 2026-09-28

- Schedule the load-test drift check in CI — parked; no change yet. The `check-load-test-drift` change adds the
  comparison but keeps it where the reference lives (a developer's cluster). A scheduled run needs three things it does
  not have: an environment identity that is not the base URL (the reference's name and its `target` guard key on the URL
  host, so a CI run at `http://axon-showcase-api` — what the deployment smoke already uses — would compare against, or
  refresh, the committed local reference), a runner-specific reference with a tolerance calibrated there, and a stable
  environment to attribute a delta to the code at all — a shared runner's timings are host state, the same reason the
  smoke runs only the `smoke` profile and records no performance numbers. Observational only if ever done, never a merge
  gate.

## 2026-09-24

- Widen the `commit-hygiene` test-coverage requirement to every check — parked; no change yet. The spec's "The guard's
  checks are covered by tests run in the build" is scoped to the guard's classes, while the build checks
  (`verifyCapturedMarkers`, `verifyTrackedIgnoredFiles`, `verifyConflictMarkers`, `verifyExecutableBits`,
  `verifyUniqueCronSchedules`, `verifyUniqueReviewRounds`, `verifyReviewSessionContinuity`, `verifyLargeFiles`,
  `verifyTableRows`) carry no test-coverage clause; widening it needs a REMOVED+ADDED retitle, since a `MODIFIED` block
  cannot rename a requirement header.

- Carry findings forward across audit reports — parked; no change yet. The reports under `docs/audits/` have been
  write-only: a finding recurs run after run with nothing to say it is the same unapplied item, and the recorded
  smoke-run lesson is that "the archived run's note is never swept again". Have the verdict name which items also appear
  in the newest prior report and remain unapplied, so the report ages into a backlog. Routes to the `agents-auditor`
  definition, its report contract, and the `/audit-agents` command's read-list. `docs/audits/2026-10-24.md` now ends
  with a manual `## Disposition` section — each finding's applying PR, and the extraction opportunity rejected with its
  reason — so that report no longer reads as write-only; the mechanism stays wanted because the check is still the
  author's manual look-back, and a later report can regress to write-only silently.

- Sharpen route-candidate verification the way merge candidates were sharpened — parked; no change yet. Merge candidates
  got the target-verification rule (#366) only after a false delegation dropped identifiers; route candidates (added
  2026-09-24) carry the lighter "confirm the mechanism exists and covers the rule's subject". A mechanism can enforce a
  _sibling_ case rather than the rule's subject (Spotless enforces the SPDX header but not "no comments"; the pre-commit
  hook enforces formatting, not the timing a rule advises), so the candidate should verify full coverage and keep
  whatever the mechanism does not cover. Parked: no incident yet — a false route is the trigger to add the clause, as a
  false delegation was for merge candidates.

- Trend the audit counts across reports — parked; no change yet. The capture rule leans on the verdict's accreted-rule
  count as the growth control, but nothing reads it back, so the only quantitative signal that consolidation is winning
  goes uncollected. A small report — or a `/retrospective` input — extracting the verdict lines from `docs/audits/*.md`
  would show findings, merge, removal, accreted, and authoring-system gap counts over time. Each meta rule counted once
  however many `captured:` markers it carries gives the three reports from `docs/audits/2026-10-04-scheduled.md` (71
  units) through `2026-10-11` (~95, ~24 new) to `2026-10-19` (~141, ~46 new) a buildable trend — reported approximate
  counts, with the counting rule stated in full only from `2026-10-11.md` on. The reports before `2026-10-04-scheduled`,
  and the on-demand `2026-10-04` report (118 raw markers, no distinct-unit count), record figures that predate the
  metric — raw marker counts, or no stated unit basis — so none is comparable with the three. The same extraction would
  now also trend the **instruction-budget ledger** the reports from `docs/audits/2026-10-19.md` on carry
  (`optimize-agents-auditor-for-instruction-budget`) — `AGENTS.md`'s size, its size by section, and the growth since the
  prior report — which is the one series that measures whether the reductions are holding the always-loaded surface
  flat.

- Record the lesson-capture's rejected proposals — parked; no change yet. A "nothing durable" verdict is a judgment that
  vanishes (the `widen-auditor-to-route-candidates` capture's, for instance); recording each rejected proposal with the
  gate criterion it failed would let the retrospective surface a recurring blind spot or a recurring false positive.
  Routes to the `lesson-capture` definition plus the retrospective's digest, and needs a durable home, since the
  capture's output is currently not persisted.

- Name a durable home for a smoke-run's beyond-seed findings — parked; no change yet. The smoke-run convention says to
  fix the findings the user approves in the introducing change, but not where an unapproved one goes: in
  `widen-auditor-to-route-candidates` the second surfaced item landed only in the archive-bound `tasks.md`, which no
  sweep reads. One line in the convention — fix it, or park it in `docs/ideas.md` — closes it. Parked: one occurrence so
  far, below the capture gate.

- Explain an unexplained `.opencode/opencode.json` truncation — parked; no change yet. In
  `retire-opencode-permission-plugin` (#388) the file was truncated in the working tree between the edit and the commit,
  so only `{ "$schema": … }` shipped — losing the config the change added plus `model`, `small_model`, and the
  Playwright MCP (restored in #389). The cause is unknown: running `opencode debug config` leaves the file unchanged and
  it has held on `main` since the restore, so something in the session (a subagent probe, or a config write) rewrote it.
  Watch for a recurrence — a silent truncation strips any config-based grant, and the staged-diff-size rule in
  `AGENTS.md` names the tell. If it recurs, find the writer before trusting the file as a durable surface.

## 2026-09-22

- Check the ADR `Status:` vocabulary mechanically — parked; no change yet. `docs/adr/README.md` enumerates the
  vocabulary as `Proposed | Accepted | Superseded by ADR-NNNN`, and `narrow-query-api-dependency` invented
  `Status: Accepted (amended …)` — caught by review and reverted. The `architecture-auditor` already reports "an ADR
  `Status` that is stale", so widening it to the vocabulary (or a cheap grep in a docs check) would catch the deviation
  where the prose rule cannot be relied on. Parked: one non-severe occurrence, and it is tooling — which the
  retrospectives' own "use the tooling rather than extend it" direction argues against until it recurs.

- Make a capture confirm the rule excludes the incident that produced it — parked; no change yet. The gate requires "one
  severe verified incident with a clear preventive action" but never tests that the rule would have _caught_ that
  incident: the `extract-specd-rationale-from-agents-md` outcome-only clause passed the gate and, about an hour later
  (`sharpen-spec-worthiness-precondition`), licensed an over-move because it did not exclude the internal-control-flow
  case — that change had to add the precondition. The fix is a capture-procedure step: state the incident and confirm
  the rule excludes it, re-reading the incident to ask "would this rule have caught it?" It routes to the
  `lesson-capture` definition rather than `AGENTS.md`. Parked rather than proposed: one incident so far and the fix is
  subtle, so a second occurrence or the next `/retrospective` should confirm the class first.

- Refresh the `gateway/rest-api` Purpose to name the CORS capability — parked; no change yet. The 2026-09-21 specs-audit
  (`docs/audits/2026-09-21.md`) reported that the Purpose (`openspec/specs/showcase/gateway/rest-api/spec.md`) covers
  the request/response contract but never mentions cross-origin access, which the spec holds as its own fail-closed
  requirement, and suggested extending the closing clause with "…and configurable cross-origin access (CORS) for the
  standalone web UI." It called it Purpose-fit drift rather than a hard defect, and the finding was neither applied nor
  parked when the audit-fix change landed. A standalone `skip_specs` Purpose refresh: a delta cannot carry a Purpose, so
  the edit lands in the archive commit and the implementing PR's diff would show no spec change — state the deferral in
  the report.

## 2026-09-21

- Verify the audit workflow's generated PR title end to end — parked; no change yet. `fix-audit-pr-title` reworded the
  `audit` workflow's prompt so the agent no longer leads its response with the owner mention, because the OpenCode
  GitHub action derives the PR title by summarising the response in under 40 characters. The prompt change's effect on
  the generated title cannot be checked by any gate — no CI job runs the workflow and `workflowLint` reads only the YAML
  — so a `gh workflow run audit.yml` dispatch (or the next scheduled run) is the check: confirm the resulting report
  PR's title names the report rather than the mention. `standardize-pr-conventions` now checks an action-derived title
  against the standard and renames it when it deviates, so the dispatch verifies the prompt's effect rather than the
  merged title.

## 2026-09-16

- GitHub Discussions are disabled — parked; no change yet. Enabling them would give the project a second support surface
  beside issues, but it needs moderation, so it is a decision rather than a toggle.
- No project homepage — parked; no change yet. The README _is_ the documentation, so the `homepage` field stays empty
  until there is a site (or a GitHub Pages rendering of the README) to point at.
- No social preview image — parked; no change yet. It is the card shown when the repository is shared, it needs a
  design, and it is a repository-settings upload rather than a file in the tree.
- No Code of Conduct — parked; no change yet. Adopting one is a commitment with enforcement expectations, so it is the
  owner's decision rather than a file to drop in; the MIT license and the README already state the project's posture.

- The GitHub description and topics are un-gated — parked; no change yet. Nothing reads them, and the description sat
  unset until a human noticed. A pull-request check could read the live values and fail on drift, but no pull request
  causes or can remediate that drift, so every unrelated PR would carry the failure. Gating them means the repository's
  observational pattern instead — single-source the expected description and topics in-repo (a small
  `config/github-metadata.*`, or the `openspec/config.yaml` context block that already carries a near-copy of the same
  facts), then a scheduled workflow that compares the live values via the API and opens or updates an issue on drift,
  like the four update-check workflows. Worth it only if the added workflow and the fourth copy of the stack facts are
  judged cheaper than the silence.

## 2026-09-14

- Retire the `NANOS_DATE_PATTERN` workaround once its fix reaches us — parked; no change yet.
  `spring-projects/spring-data-elasticsearch#3334` closed 2026-08-30 (PR #3337, milestone 6.2.0-M2), and the fix is now
  published — but only on lines above ours: `6.2.0-M2` (2026-09-24) carries it, while we resolve
  spring-data-elasticsearch 5.5.13 on the `spring-data-opensearch` 2.0.8 line (which declares 5.5.13 directly), so the
  truncation is still live. The condition to watch is therefore a release _containing the fix on our line_, not a line
  or a milestone: `spring-data-opensearch` 3.x is the line that would carry it (the latest, 3.1.4, still ships 6.1.1),
  but 3.x targets Spring Boot 4 and `spring-data-elasticsearch` 6.x, so retiring the workaround rides the deferred
  Spring Boot 4 migration (ADR-0004) — re-check when that migration lands, not on a chart or patch bump. The other
  closure candidate does not apply — `#1060` leaves our `checkBuildEnvironmentConstraints` row untouched (the `#755`
  verdict is recorded in the upstream-reference bullet in `AGENTS.md` and ADR-0007). Recorded here rather than in the PR
  body that surfaced it, which no tool reads.

## 2026-09-07

- Migrate off the deprecated OpenSearch low-level REST client — parked; no change yet.
  `org.opensearch.client.RestClientBuilder` (and the `RestClient` it builds) is `@Deprecated`, to be removed in future
  releases in favor of the official OpenSearch Java Client. The projection service's
  `openSearchRestClientBuilderCustomizer` bean (`ShowcaseProjectionApplication`) surfaces a `[deprecation]` compile
  warning because Spring Data OpenSearch's `RestClientBuilderCustomizer` contract forces touching the deprecated type to
  configure connection pooling / idle eviction. When Spring Data OpenSearch updates its customizer to the newer client
  (or we migrate the projection/query services to the OpenSearch Java Client transport directly), the warning resolves;
  track so it does not become a hard break when the low-level client is removed.

## 2026-09-01

- Managed-k8s staging for free or cheaply — explored, parked (no change yet). Goal: a managed Kubernetes staging env for
  the chart, ideally free, otherwise as cheap as reasonable. Findings: (1) the two real free-control-plane paths are AKS
  Free (control plane free, but you pay for nodes — not \$0 ongoing) and OKE (control plane free + 2 Always Free ARM
  nodes = 12 GB, genuinely \$0); (2) Oracle halved the Always Free Ampere A1 to 2 OCPU / 12 GB in June 2026, so the full
  stack (~11-12 GiB with the `kps` + `tempo` observability stack, 5 services, Kafka, OpenSearch, Postgres) does **not**
  fit OKE's free budget with observability enabled — it fits only if staging trims observability/single-replica/smaller
  OpenSearch, and images must be ARM (`-PimagePlatform=linux/arm64` already supported); (3) there is no free managed k8s
  that runs the full chart as-is, always-on, at \$0 — every free path needs either trimming (OKE), paying for nodes
  (AKS, ~\$40-80/mo), or accepting ephemerality. Reference: `nce/oci-free-cloud-k8s` runs OKE free but on a leaner
  stack. Open decision: how faithful staging must be to the observability stack (the real decider between OKE-free and
  AKS-paid), and whether always-on vs on-demand node-pool stop/start changes the budget.
