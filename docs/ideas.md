# Ideas

Short notes to remember emerging development ideas. An idea becomes an OpenSpec change only when acted on — this file is
a scratchpad, not a backlog of planned work. An idea is removed from the list once implemented (captured by a change) or
once explored and decided against (the durable lesson is captured in `AGENTS.md`, an ADR, or a dated retrospective where
the decision is a program direction with a revisit condition rather than a rule an agent applies); only open,
not-yet-implemented ideas remain.

Changes to this file travel with the change that owns them: an idea's **removal** ships in the implementing change's PR
— it rides that change's branch and commits with its push, like the rest of the work — and a **newly parked** idea the
in-flight change's own work surfaced rides that branch too; only a docs edit the change did _not_ cause, or one surfaced
after it is done (a merge-time capture), ships as its own docs PR (like an `AGENTS.md`/`README.md` refresh PR). When an
idea graduates into a concrete candidate for work, it may be promoted to a GitHub issue that links to the eventual
OpenSpec change. Ideas are grouped into `## YYYY-MM-DD` sections ordered newest-first; each idea goes under a section
dated when it was added (start a new section for a new day rather than appending to the most recent one). Search the
file for the idea before adding it: the same work already parked — however differently worded — is extended in place in
its existing section, since the day-section rule otherwise files a re-park as a second entry and splits one idea's
detail across two sections.

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

- Add a markdown table-integrity check to `commit-hygiene.py` — parked; no change yet. A table row wrapped onto a second
  line becomes a spurious row, and Prettier treats that as its fixpoint, so `spotlessCheck` stays green: reproduced with
  the build's Prettier 3.9.10 under this repo's config during `adopt-worktree-isolation`'s review (a `--table-rows` mode
  counting unescaped pipes per row against the delimiter row, a `verifyTableRows` task beside `verifyConflictMarkers`,
  and controls for a wrapped row, Prettier's output, an escaped `\|`, a fenced diagram and a clean table; a prototype
  over the 113 gated markdown files found no false positive).

- Extend that markdown-integrity check with an inline-code-span whitespace mode — parked; no change yet. A run of two or
  more spaces inside a single-line inline code span passes `spotlessCheck` (Prettier never rewrites a span's interior)
  and the 120-character check measures length only; reproduced twice, by `fix-gateway-cors-allowed-headers` (#359) and
  by `bump-kps-chart-2026-10-25`'s five-space `kubectl get pods -n monitoring` span. Scope it to the Spotless markdown
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

- Extend `scripts/claim-scan.py`'s absolute-quantifier pattern to the absence forms the rule names — parked; no change
  yet. Review-gate stage 1 names the absence form `no`/`none`/`nothing`, while `ABSOLUTE` matches
  `only|every|all|never|always|none` (`scripts/claim-scan.py:37`), so `no` and `nothing` never surface. Reproduced
  during `record-2026-10-24-audit-disposition` on its appended section: the line "nothing is parked for it" yields no
  candidate at all, and "no finding reduced `AGENTS.md`" yields only its `[path] AGENTS.md` — no absolute — so each such
  claim row needs the author to notice it unaided. Widen the pattern with one test per added form beside
  `test_an_absolute_quantifier_is_reported` (`scripts/test-claim-scan.py`) and re-measure the noise a widened pattern
  adds to a real guidance diff before committing it. A second instance (`document-java-version-selector`, round 1): the
  fact that made the word "optional" true — "Nothing in the repository reads `.java-version`" — had no claims-table row,
  and the scan surfaced no candidate for it. A third (`bump-buildpacks-2026-10-25`): a design risk bullet's "Nothing
  else in this image is pinned independently" shipped false and cost a review round, while its sibling "Only the web UI
  consumes …" was seeded — the author verified the claim the scan surfaced and not the one it did not. A fourth
  (`stage-new-files-before-manual-review`): "`git clean -fd` is the one loss vector" — an absolute its own evidence
  falsified, caught only by a deep review — sat on an added line of the diff and produced no candidate, so the
  definite-singular form (`the (?:only|one|sole|single)`) is a form to add beside the absence forms, with its own
  positive-control test.

- Bold-lead the plain rules a `captured:` marker has to land on — parked; no change yet. `scripts/commit-hygiene.py`'s
  marker check requires a lead starting `- **` or `**`, so the two plain `- Text` bullets a capture may need to annotate
  — the "Work done while awaiting the approval pass" bullet and the failure-handling bullet below it — cannot carry a
  marker at all; the fix is to bold-lead those bullets when a capture next merges into them, never to widen the guard,
  since accepting plain leads is what the check exists to prevent (`stage-new-files-before-manual-review`'s capture hit
  this).

## 2026-10-24

- Add a pre-commit ledger guard for the review-round checks — parked; no change yet. `commit-hygiene.py --staged` runs
  the staged-set checks but not `--unique-rounds`/`--session-continuity`, so a mis-transitioned ledger row reaches CI
  (it happened twice: #670 and `projector-operability`). The guard is the natural hook; the rule lives in the
  review-gate checkpoint. It should also refuse a staged `docs/review-rounds.tsv` diff that **removes** a data row: the
  ledger is append-only by contract, and contiguity passes over a silent deletion of another unit's rows — the shape a
  base refresh produces when the diff is re-applied against the wrong base.

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
  skill, so a gate-procedure lesson has a listed destination. The capture record's own hand-listed enumeration stays
  here.

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
    carrying its capture record (internal trigger, at that coordinate) **and** the 2026-10-23 pause lifted (external
    coordinate: the P1 acceptance in `docs/retrospectives/2026-10-23.md`).

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
  `check-review-round-contiguity` ships the cheap net instead (contiguity + an expected-round checkpoint). Revisit on a
  repeat where both the append and the checkpoint were skipped, or if a technical barrier against the Task path is
  required.

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

- Refuse a commit that stages an archived change dir with an unchecked task — parked; no change yet. This is the
  2026-10-15 retrospective's S4, restated with the machinery named, and the retrospective proposed it at lower
  confidence: `openspec archive --yes` continues past incomplete tasks with a warning line rather than failing, and a
  merge-time verification (a workflow dispatch, which needs the pushed branch) can only be ticked _after_ the change dir
  is committed, so the tick lands in the archive commit's own edit and nothing re-reads the warning afterwards. The
  outcome "an archived task is not left unchecked" is mechanically checkable, and the machinery exists:
  `commit-hygiene.py`'s `find_unpaired_archive_sync` already inspects staged archived change dirs, so a sibling check
  would read a staged `openspec/changes/archive/**/tasks.md` and refuse a commit carrying an unchecked item. It must be
  scoped to the **staged set** rather than the whole archive, because archived change dirs already carry unchecked tasks
  that are historical and would all be flagged. The exemption the 2026-10-17 entry asked for — for a task the owner
  deliberately decoupled from its merge — is unnecessary: `AGENTS.md`'s deferred-task clause (the smoke-run bullet)
  requires such a task to be reworded to record the deferral and then **ticked**, so it does not trip the guard. Which
  other boxes, such as a bookkeeping box whose edit landed or a completed control run, are exempt is the design question
  the 2026-10-23 retrospective's P2 leaves open, and until that list is settled the guard trips any unchecked box.
  Parked rather than built: it overrides `openspec archive --yes`'s deliberate non-blocking design by policy, so it
  needs its own `commit-hygiene` delta and a `*RulesTests` control.

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

- Draft the pull-request body before the pre-write derivation pass — parked; no change yet. A unit with no change dir
  carries its claims table and capture record "on the pull-request checklist line", but that line exists only once the
  body is drafted — so the pass is stated before its record has a home, and the quick review reports the missing record
  (`claim-missing`) before the checklist exists to hold it. State in the requirement that owns the destination (and in
  the `/seed-claims` step the pass runs) that the body is drafted from the template first. Parked rather than built: the
  destination is stated in the requirement, the command, and the pull-request template, so correcting only `AGENTS.md`
  would leave the two sites the pass actually executes designating a home created later.

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

- Make the chart-packaging gate's fetches resilient — parked; no change yet. `check` now runs the chart's packaging, so
  the plugin's repository setup (`helmAdd*Repository`, `helmUpdateRepositories`) and its dependency fetch
  (`helmUpdateMainChartDependencies`) run on every pull request. The gate-worthy-step rule wants such a fetch made
  resilient with a bounded retry, but these are the plugin's own task types, so no repo-side wrapper can retry another
  task's action; `verifyInfraImageVersions`'s `AbstractHelmRepositoriesTask` + `BoundedRetry` is the model for a
  repo-authored fetch. Verify whether the plugin can configure a retry before acting — if it cannot, the gap is an
  upstream report on `build-extensions-oss/gradle-helm-plugin` (its issues are enabled), and the remedy is a
  repo-authored fetch task.

## 2026-10-16

- Report the plugin's cache-dependent chart-dependency skip upstream — parked; no change yet. The
  `helmUpdateMainChartDependencies` task (which the plugin's docs equate with `helm dependency update`) silently skips
  when the **configuration cache** (#581) stores its entry before the chart sources are collected — a clean checkout or
  CI: its `onlyIf` resolves false there, so nothing fetches the chart's external Bitnami `common` dependency,
  `helm/chart`'s packaging produces a chart with no `charts/`, and both the strict lint and the deployed install fail.
  Measured on one commit with only the cache toggled: SKIPPED with the cache on, fetches with
  `--no-configuration-cache`. The repo's workaround is `setOnlyIf { true }` in `helm/chart/build.gradle.kts`;
  `build-extensions-oss/gradle-helm-plugin` has issues enabled (`has_issues: true`, `has_discussions: false`), so a fix
  belongs there as an issue or a pull request and would retire the workaround.

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
  45 dumps were captured. The dumps are the finding: the test JVM holds **17 threads and none is in the request path** —
  four BlockHound-instrumented `parallel-N` threads parked in `ScheduledThreadPoolExecutor.take`, the test worker,
  ForkJoinPool-1 workers and Gradle's IPC threads — so this sample is a request whose completion never arrives while the
  JVM is otherwise idle, not a thread blocked in the request path, and it does not support the boundedElastic-starvation
  hypothesis (that pool had not even been created). The failure is probabilistic, not deterministic: the same commit's
  re-run was green (12m9s) and the suite is green locally (77/77 in 6.8s — six runs, one initial plus five consecutive),
  and the failure mode predates this unit's bumped coordinates: what the bump changed is its frequency, not its
  existence (the bisect below), so the earlier "rare or the runner's load changed" question stands as answered by
  _frequency_. Next diagnostic: dump on a watchdog timer while a request is in flight (the failure-time dump shows only
  what remains after the timeout), so the next occurrence names the thread that lost the completion rather than only
  that nothing was running.

  **A CI bisect narrowed it to `reactor-blockhound` `1.0.18.RELEASE`** — a separate catalogue coordinate bumped in the
  same update set as `reactor-bom` 2025.0.8 (not a member of that BOM), and the one row of the 2026-10-25 bump unit's
  report that unit holds back. Two failures in three runs of the bumped pair, against three green runs with `blockhound`
  at `1.0.17.RELEASE` — two at the same `reactor-core` 3.8.8 (the bisect run and the control run, 12m29s), one at 3.8.7;
  `blockhound` 1.0.18 with `reactor-core` 3.8.7 was not tried, so the interaction with 3.8.8 is untested, and each cell
  of that table is one to three runs — enough to justify a recorded hold, not to call the cause proven. The mechanism
  stays unproven and the failure probabilistic, so the hold was a precaution — and the 2026-10-26 bump unit then
  **withdrew it by owner decision**, shipping `reactor-blockhound` `1.0.18.RELEASE` together with a new
  `ThreadDumpWatchdog` that snapshots every thread _while_ a test runs. The reasoning: each bisect cell is one to three
  runs, so a pin bought against it hides the mechanism, while the in-flight dump now names it on the next stall. Reading
  that dump is the next step here, and the reproduction drafted for `reactor/BlockHound` is filed with the correlation
  stated as observed rather than as a proven cause.

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

- Let `claim-scan.py` seed a no-change-dir unit's claims from its records — parked; no change yet. The pre-write pass is
  seeded by `scripts/claim-scan.py`, which reads "the added lines of an authored set (the diff against a base ref, plus
  the untracked, non-ignored files)" — its own docstring. A unit with no change dir keeps its claims table and capture
  record on the pull-request checklist, which is neither, and its code diff may be deletion-only, so the **mandated**
  seed returns an empty candidate list while the unit's real claims — a prior artifact it rests on, its greps, its
  probes — go unseeded, and that empty list reads exactly like the pass's "nothing to resolve" outcome.
  `remove-unrecorded-redgate-repository`'s five round-1 record findings arose under precisely those conditions. Extend
  the scan (and `/seed-claims`) to accept the unit's record text as an input, so the seed reaches those claims. Parked
  rather than built: the seed is already mandated by the spec and `AGENTS.md`, so what fails is the tool's input, not an
  agent's knowledge.

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
  `verifyUniqueCronSchedules`, `verifyUniqueReviewRounds`, `verifyReviewSessionContinuity`, `verifyLargeFiles`) carry no
  test-coverage clause; widening it needs a REMOVED+ADDED retitle, since a `MODIFIED` block cannot rename a requirement
  header.

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
