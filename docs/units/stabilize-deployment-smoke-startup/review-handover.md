# Review hand-over — stabilize-deployment-smoke-startup

One section per quick-review invocation, written before the caller waits on it, so a stalled session is recovered from
this record rather than from the session.

## Round 1 — proposal stage

- **Unit** — `stabilize-deployment-smoke-startup`; change dir `openspec/changes/stabilize-deployment-smoke-startup/`;
  record `docs/units/stabilize-deployment-smoke-startup/unit.md`.
- **Base and current revision** — `origin/main` at `bfe30a08` (the branch's fork point); `HEAD` at the same commit,
  everything uncommitted and untracked.
- **Workspace** — tree `/Users/afanasyev/Development/axon-showcase-stabilize-deployment-smoke-startup`, branch
  `stabilize-deployment-smoke-startup`.
- **Supplied artifacts** — `proposal.md`, `design.md` (context, goals/non-goals, three decisions with rejected
  alternatives, risks, a seven-row claims table with dismissals by kind, migration plan), `tasks.md`, the delta at
  `specs/showcase/quality/merge-governance/spec.md`, the unit record, and the targets the change will edit:
  `helm/values/axon-showcase/values-ci.yaml`, `.github/workflows/deployment-smoke.yml`, and
  `.opencode/skills/deployment-operations/SKILL.md`.
- **Scope** — proposal-stage screen: coherence of proposal/design/tasks/delta; whether the delta's scenario set matches
  the behavior the design and tasks describe; whether the tasks name how each step is verified, including the gate's
  failing case; whether the claims rows reproduce from the commands they name; whether the runner-fit decision is argued
  against the chart default; Structured Writing, code spans, and the 120-character rule.
- **Prior findings** — none; this is the unit's first round.
- **Completed checks** — `./gradlew spotlessApply` and `spotlessCheck` pass; `openspec validate --all` reports 27
  passed, 0 failed; `claim-scan.py --base origin/main` seeded the claims table and every candidate is a row or covered
  by its kind's dismissal rather than enumerated.
- **Limitations** — the failing run is read through `gh run view`; the runner's contention is not reproducible locally,
  so the behavior of the probe override and the gate is the smoke dispatch's to establish. The `values-ci.yaml` and
  workflow edits do not exist yet at this boundary.
- **Subject packs** — `deployment-operations` (the `ci` values, startup probes, the smoke's deployment surface). No JVM
  production code, test tier, or Gradle build decision is touched.
- **Deadline** — 20 minutes from this record's write.

## Round 2 — proposal stage

- **Unit** — `stabilize-deployment-smoke-startup`; base `origin/main` at `bfe30a08`; everything uncommitted/untracked.
- **Workspace** — tree `/Users/afanasyev/Development/axon-showcase-stabilize-deployment-smoke-startup`, branch
  `stabilize-deployment-smoke-startup`.
- **Supplied artifacts** — the planning artifacts after round 1's fixes, `findings/round-1.md`, and the same targets and
  sources as round 1.
- **Scope** — re-screen of round 1's four instances: the seeding-behavior row, the per-service CPU-requests sentence,
  the gate control's fixture, and the narrowed run-log grep. No other surface changed.
- **Prior findings** — round 1's four, all applied: the load test's seeding branch is a claims row sourced to the `READ`
  chain; the rejected CPU alternative now names the projection service's `100m` default; the gate control's fixture
  sends keep-alive comments only; the run-log row greps `showcaseEvent` alone and states one line.
- **Completed checks** — `spotlessApply`/`spotlessCheck` pass after the fixes; `openspec validate --all` reports 27
  passed, 0 failed; the ledger checks pass (`--unique-rounds --expect`, `--session-continuity`).
- **Limitations** — unchanged from round 1: the edits themselves do not exist at this boundary.
- **Subject packs** — `deployment-operations`.
- **Deadline** — 20 minutes from this record's write.

## Round 3 — proposal stage

- **Unit** — `stabilize-deployment-smoke-startup`; base refreshed to `origin/main` at `21b100bf` (the tree
  fast-forwarded over #863, which added `docs/audits/2026-11-07.md`, a file this unit does not touch); everything
  uncommitted/untracked.
- **Workspace** — tree `/Users/afanasyev/Development/axon-showcase-stabilize-deployment-smoke-startup`, branch
  `stabilize-deployment-smoke-startup`.
- **Supplied artifacts** — `tasks.md` after the deep-review pointer task (3.5), the new
  `docs/units/stabilize-deployment-smoke-startup/deep-review.md`, and the planning artifacts re-read for coherence with
  them.
- **Scope** — the two artifacts added after round 2: whether the deep-review record's outcome, derivation, and signals
  match what the routing tool printed, and whether the pointer task states its verification. The rest of the tree is
  unchanged since round 2.
- **Prior findings** — rounds 1–2, all fixed; round 2 reported none.
- **Completed checks** — `spotlessApply`/`spotlessCheck` and `openspec validate --all` (27 passed, 0 failed) after the
  edits; `review-routing.py` printed `ROUTING: NOT_NEEDED`; the ledger checks pass.
- **Limitations** — the refresh added one base file outside this unit's surface; the `values-ci.yaml` and workflow edits
  still do not exist at this boundary.
- **Subject packs** — `deployment-operations`.
- **Deadline** — 20 minutes from this record's write.

## Round 4 — proposal stage

- **Unit** — `stabilize-deployment-smoke-startup`; base `origin/main` at `21b100bf`; everything uncommitted/untracked.
- **Workspace** — tree `/Users/afanasyev/Development/axon-showcase-stabilize-deployment-smoke-startup`, branch
  `stabilize-deployment-smoke-startup`.
- **Supplied artifacts** — `docs/units/stabilize-deployment-smoke-startup/deep-review.md` (rewritten) and `tasks.md`'s
  task 3.5 (rewritten), plus `scripts/review-routing.py` as the source the record cites.
- **Scope** — round 3's single instance: whether the record's outcome is labelled provisional for the reason that the
  `helm/` and `.github/` paths return `REQUIRED` from the cited tool, whether the tool's classification the record
  states matches its `INFRASTRUCTURE` table and `floor()`, and whether task 3.5 now runs the command instead of
  asserting the proposal-boundary outcome.
- **Prior findings** — round 3's one, applied; rounds 1–2 fixed and re-verified.
- **Completed checks** — `spotlessApply`/`spotlessCheck` and `openspec validate --all` (27 passed, 0 failed) after the
  rewrite; ledger checks pass.
- **Limitations** — the implementation edits still do not exist, so the re-derived outcome cannot be observed at this
  boundary.
- **Subject packs** — `deployment-operations`.
- **Deadline** — 20 minutes from this record's write.

## Round 5 — proposal stage

- **Unit** — `stabilize-deployment-smoke-startup`; base `origin/main` at `21b100bf`; everything uncommitted/untracked.
- **Workspace** — tree `/Users/afanasyev/Development/axon-showcase-stabilize-deployment-smoke-startup`, branch
  `stabilize-deployment-smoke-startup`.
- **Supplied artifacts** — `deep-review.md` (rewritten from its intended content: the moving ledger figure is gone and
  the signals statement names only what the command printed) and `tasks.md` task 3.5 (the span collapsed to one space).
- **Scope** — round 4's two instances: the removed ledger sentence and the inline span's whitespace.
- **Prior findings** — rounds 1–4, all fixed; round 4's class recurrence was answered by rewriting the record rather
  than patching its sentence.
- **Completed checks** — `spotlessApply`/`spotlessCheck` after the rewrite; `openspec validate --all` (27 passed, 0
  failed); ledger checks pass.
- **Limitations** — the implementation edits still do not exist, so the post-edit routing output cannot be observed
  here.
- **Subject packs** — `deployment-operations`.
- **Deadline** — 20 minutes from this record's write.

## Round 6 — proposal stage

- **Unit** — `stabilize-deployment-smoke-startup`; base `origin/main` at `2b4c9a00` (#871), fast-forwarded into the
  tree; `git diff --stat origin/main` is empty and only the unit's files are untracked.
- **Workspace** — tree `/Users/afanasyev/Development/axon-showcase-stabilize-deployment-smoke-startup`, branch
  `stabilize-deployment-smoke-startup`.
- **Supplied artifacts** — `docs/units/stabilize-deployment-smoke-startup/deep-review.md` (rewritten to the derived
  `RECOMMENDED` outcome), `rounds.tsv`, and `scripts/review-routing.py` as the record's cited source.
- **Scope** — round 5's two instances: whether the record now states the derived outcome and its signal with the
  signal's origin and no moving count, whether the run-or-skip obligation is stated, and whether the branch sits on
  `origin/main`.
- **Prior findings** — rounds 1–4 fixed and re-verified; round 5's two applied.
- **Completed checks** — the refresh is a clean fast-forward with an empty diff against `origin/main`;
  `spotlessApply`/`spotlessCheck` and `openspec validate --all` (27 passed, 0 failed) pass; the ledger checks pass.
- **Limitations** — the implementation edits still do not exist, so the post-edit routing output cannot be observed
  here; the `RECOMMENDED` run-or-skip is owed to the owner.
- **Subject packs** — `deployment-operations`.
- **Deadline** — 20 minutes from this record's write.

## Round 7 — proposal stage

- **Unit** — `stabilize-deployment-smoke-startup`; base `origin/main` at `2b4c9a00`; only the unit's files untracked.
- **Workspace** — tree `/Users/afanasyev/Development/axon-showcase-stabilize-deployment-smoke-startup`, branch
  `stabilize-deployment-smoke-startup`.
- **Supplied artifacts** — `docs/units/stabilize-deployment-smoke-startup/deep-review.md` (round 6's parenthetical
  replaced by the tool's actual reason), and `scripts/review-routing.py`'s `gather` and prefix tables as its source.
- **Scope** — round 6's single instance: whether the record's absence-of-signal statement now matches the tool's own
  scan sets and prefixes.
- **Prior findings** — rounds 1–5 fixed and re-verified; round 6's applied.
- **Completed checks** — `spotlessApply`/`spotlessCheck` and `openspec validate --all` (27 passed, 0 failed) pass; the
  ledger checks pass; the branch is on `origin/main`.
- **Limitations** — the implementation edits still do not exist, so the post-edit routing output cannot be observed; the
  owner's `RECOMMENDED` run-or-skip answer is still owed.
- **Subject packs** — `deployment-operations`.
- **Deadline** — 20 minutes from this record's write.

## `advisor-1` — consultation on the proposal's direction, scope, and evidence

- **Owner's authorization** — the owner's explicit instruction of 2026-11-09: "Пропустить. Отревьювь с советником до
  согласия" — skip the `RECOMMENDED` deep review and consult the technical advisor until agreement.
- **Owner's objective and constraints** — ship a minimal, correct fix for the 2026-11-08 Deployment Smoke failure
  without weakening what the smoke verifies; no product-behavior change unless the defect is in the product; the repo's
  process (proposal → owner's manual approval → implementation) holds.
- **Question and proposed next action** — does the proposal's chosen direction hold (the two changes, the rejected
  alternatives, the scope), and what, if anything, must change before implementation? Proposed next action: ask the
  owner for the manual proposal review, then implement the approved proposal.
- **Change identity and stage** — unit/change `stabilize-deployment-smoke-startup`, proposal stage; seven quick-review
  rounds, the seventh clean; the owner skipped the `RECOMMENDED` deep review.
- **Artifacts and diff base** —
  `openspec/changes/stabilize-deployment-smoke-startup/{proposal.md,design.md,tasks.md,specs/showcase/quality/merge-governance/spec.md}`,
  `docs/units/stabilize-deployment-smoke-startup/{unit.md,deep-review.md,rounds.tsv,findings/round-*.md}`; base
  `origin/main` at `2b4c9a00`; no commits — 17 staged files, `git diff --cached --stat` = 511 insertions.
- **Evidence excerpts (from `gh run view 42427617536 --log-failed`)** — the assertion
  `showcaseEvent: count of failed events is 0.0 : false (actual : 10.0)`;
  `Killing pod/axon-showcase-api-gateway-… Container main failed startup probe, will be restarted` (the projection
  service too); the gateway's second start `Started ShowcaseApiApplication in 14.949 seconds` at 00:06:04.147; the Kafka
  member eviction `Member … has failed, removing it from the group` at 00:06:30,177; the consumer's
  `Found no committed offset` and `Resetting offset for partition axon-showcase-events-1 … offset=3`; the ten `/events`
  responses of 72 bytes held 65 s each.
- **Findings and dispositions** — rounds 1–6: 11 instances, all fixed (classes `claim-unreproducible`, `claim-missing`,
  `test-control`, `formatting`, `stale-base`); round 7: none. Unresolved disagreements: none known.
- **Subject packs** — `deployment-operations`.
- **Report audience and language** — owner-facing, Russian.
- **Fresh or follow-up** — fresh consultation.
- **Deadline** — 20 minutes from this record's write.

## `advisor-2` — follow-up: the concrete revision after `advisor-1`'s finding

- **Owner's authorization** — the same explicit instruction of 2026-11-09 ("…Отревьювь с советником до согласия"),
  continued for the same decision.
- **Objective and constraints** — unchanged: a minimal, correct fix that does not weaken what the smoke verifies; no
  product-behavior change unless the defect is the product's; the process keeps its approval stop.
- **Question and proposed next action** — does the concrete revision below close the hollowed assertion `advisor-1`
  found, without weakening the check or over-scoping, and is its control sufficient? Proposed next action: fold it into
  the proposal, run the quick-review loop over the amended artifacts, then ask for the owner's manual review.
- **Verified mechanism (read in the Gatling 3.16.0 sources, `gatling-http-3.16.0-sources.jar`)** —
  `io/gatling/http/action/sse/fsm/SsePerformingCheckState.scala`: a message that fails the check's `matchConditions` is
  logged as "Received non-matching message", buffered as unmatched, and the await continues (`NextSseState(this)`); only
  a matching message is checked, and the timeout records `Check <name> timeout`. So a match condition is a filter, and
  adding one cannot turn a pre-run replay into a KO.
- **Proposed revision** —
  1. keep the gate (publish a showcase, confirm `/events` delivers a `data:` frame).
  2. `ShowcaseSimulation` records the IDs of showcases this run creates (the read stream's seed and the write stream's
     schedule both create one) in a concurrent set, and the SSE message check's match condition restricts to that set:
     `jsonPath("$.data.showcaseId").in(session -> List.copyOf(RUN_CREATED_IDS))` — the
     `Validate.in(Function<Session, List<X>>)` overload exists in `gatling-core-java`'s `CheckBuilder`.
  3. the mid-hold check that sustained profiles run takes the same condition.
  4. control: a local stub gateway (Python, no cluster) that serves `/showcases` and `/events` and replays **only** a
     pre-run event must make the smoke profile fail on `showcaseEvent`; the same stub also emitting an event for a
     run-created showcase must make it pass. Then the branch dispatch of the real smoke.
  5. delta: `MODIFIED` `showcase/quality/load-tests`, requirement "Simulation exercises the SSE event stream" — the
     check matches only events for showcases this run created, so an event buffered before the run cannot satisfy it.
- **Known residual** — a run-created event can reach the gateway before the write user's HTTP response registers its ID;
  that first event is then filtered, and a later lifecycle event for the same showcase (start or remove) matches within
  the same await. At smoke scale the write lifecycle continues for seconds after the create response.
- **Alternative considered and rejected** — filtering by `timestamp` at or after the simulation's start (needs the DTO's
  timestamp and `transform(...).gte(...)`): it couples the check to clock agreement between the runner and the
  containers, which the ID set does not.
- **Subject packs** — `deployment-operations`; the check-and-test-authoring guidance informs the control.
- **Report audience and language** — owner-facing, Russian.
- **Fresh or follow-up** — follow-up on the same decision.

## Round 8 — proposal stage

- **Unit** — `stabilize-deployment-smoke-startup`; base `origin/main` at `2b4c9a00`; only the unit's files untracked.
- **Workspace** — tree `/Users/afanasyev/Development/axon-showcase-stabilize-deployment-smoke-startup`, branch
  `stabilize-deployment-smoke-startup`.
- **Supplied artifacts** — the amended planning artifacts after the advisor's finding: `proposal.md`, `design.md` (the
  new warm-up-exclusion decision, the added risks, five new claims rows), `tasks.md` (section 3, the simulation change
  and its two stub controls), the merge-governance delta, and the new `specs/showcase/quality/load-tests/spec.md` delta;
  plus the sources they cite — `load-tests/src/gatling/java/showcase/loadtests/ShowcaseSimulation.java`,
  `load-tests/build.gradle.kts`, `openspec/specs/showcase/quality/load-tests/spec.md`, and the two Gatling 3.16.0
  sources jars in the Gradle cache.
- **Scope** — the material scope change from the consultation: whether the proposal/design/tasks/load-tests delta agree
  on the warm-up exclusion, whether the two `MODIFIED` blocks copy the main spec with every scenario retained, whether
  the tasks verify the property passes through and the check fails and passes in the two controls, and whether the
  claims rows reproduce from their commands.
- **Prior findings** — rounds 1–7 fixed and re-verified (round 7 clean); the advisor's `advisor-1` finding closed by
  this amendment, `advisor-2`'s correction applied (exclude the warm-up by ID rather than matching run-created IDs).
- **Completed checks** — `spotlessApply`/`spotlessCheck` and `openspec validate --all` (27 passed, 0 failed) pass; the
  ledger and session checks pass; the claims rows' commands were run.
- **Limitations** — the implementation edits still do not exist, so neither control can run at this boundary; the
  Gatling sources are read from the cached 3.16.0 sources jars.
- **Subject packs** — `deployment-operations`; the check-and-test-authoring guidance was loaded for the controls.
- **Deadline** — 20 minutes from this record's write.

## Round 9 — proposal stage

- **Unit** — `stabilize-deployment-smoke-startup`; the tree fast-forwarded to `origin/main` at `0deca079` (#872); all
  unit files staged or untracked, `git diff --name-status origin/main` deleting nothing.
- **Workspace** — tree `/Users/afanasyev/Development/axon-showcase-stabilize-deployment-smoke-startup`, branch
  `stabilize-deployment-smoke-startup`.
- **Supplied artifacts** — the planning artifacts after round 8's fixes: the plural warm-up IDs across
  `proposal.md`/`design.md`/`tasks.md` and the load-tests delta, the added mid-hold control (task 3.5), and the
  `deployment-operations` pack at the refreshed base.
- **Scope** — round 8's three instances: whether every published attempt's ID is collected and excluded, whether the
  mid-hold check's exclusion is exercised by a control a sustained profile builds, and whether the tree sits on
  `origin/main`.
- **Prior findings** — rounds 1–7 fixed and re-verified; round 8's three applied.
- **Completed checks** — the refresh is a clean fast-forward with no deletions against `origin/main`;
  `spotlessApply`/`spotlessCheck` and `openspec validate --all` (27 passed, 0 failed) pass; ledger and session checks
  pass.
- **Limitations** — neither stub control can run at this boundary; the pack was re-read at `0deca079`, whose only change
  since the unit's fork point is the repository-fetch guidance.
- **Subject packs** — `deployment-operations`; `check-and-test-authoring` for the controls.
- **Deadline** — 20 minutes from this record's write.

## Round 10 — proposal stage

- **Unit** — `stabilize-deployment-smoke-startup`; base `origin/main` at `0deca079`; only the unit's files staged or
  untracked.
- **Workspace** — tree `/Users/afanasyev/Development/axon-showcase-stabilize-deployment-smoke-startup`, branch
  `stabilize-deployment-smoke-startup`.
- **Supplied artifacts** — `tasks.md`'s Control C (rewritten to isolate the mid-hold check), and the profile constants
  it cites in `ShowcaseSimulation.java` (`SSE_SUSTAINED_WRITES`, `SSE_HOLD`, the `baseline` assertions).
- **Scope** — round 9's single instance: whether Control C now makes the initial check pass and withholds run-created
  events from the mid-hold await, and whether it states the discriminating direction (passing once that one exclusion is
  removed).
- **Prior findings** — rounds 1–8 fixed and re-verified; round 9's applied.
- **Completed checks** — `spotlessApply`/`spotlessCheck` and `openspec validate --all` (27 passed, 0 failed) pass;
  ledger and session checks pass.
- **Limitations** — the controls cannot run at this boundary; the stub that delivers a run-created event on connect is
  the implementation's to write.
- **Subject packs** — `deployment-operations`; `check-and-test-authoring`.
- **Deadline** — 20 minutes from this record's write.

## Round 11 — implementation stage

- **Unit** — `stabilize-deployment-smoke-startup`; base `origin/main` at `0deca079`; the implementation is uncommitted
  in this tree.
- **Workspace** — tree `/Users/afanasyev/Development/axon-showcase-stabilize-deployment-smoke-startup`, branch
  `stabilize-deployment-smoke-startup`.
- **Supplied artifacts and diff** — the planning artifacts and both deltas; the implementation diff:
  `helm/values/axon-showcase/values-ci.yaml` (startup-probe override on the four JVM services),
  `.github/workflows/deployment-smoke.yml` (the live-event gate, the `warmUpShowcaseIds` pass-through),
  `load-tests/build.gradle.kts` (the forwarded property),
  `load-tests/src/gatling/java/showcase/loadtests/ShowcaseSimulation.java` (`WARM_UP_SHOWCASE_IDS` and the
  `sseMessageCheck` helper used by both SSE checks), and `.opencode/skills/deployment-operations/SKILL.md` (two new
  paragraphs).
- **Scope** — implementation screen: the delta specs against the code (both `MODIFIED` blocks and the new scenarios),
  the tasks' done marks against the work and their verification notes, the gate's shell (pipefail, the ID parse, the
  bound), the simulation's exclusion on both checks, the documentation paragraphs against what the code does, and the
  Structured Writing Standard.
- **Prior findings** — rounds 1–10, all fixed and re-verified (round 10 clean); `advisor-1`'s and `advisor-2`'s findings
  are closed by the implementation.
- **Completed checks** — `spotlessApply`/`spotlessCheck`; `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`
  green (workflowLint, chart packaging, verify*); `openspec validate --all` 27/0; the three stub controls ran (one red,
  one green, one red-then-green under the perturbation); the gate harness ran both ways; the chart render verified the
  override.
- **Limitations** — no cluster local, so the deployment itself is the dispatch's (task 5.1).
- **Subject packs** — `deployment-operations`, `jvm-testing`, `gradle-build`.
- **Deadline** — 20 minutes from this record's write.

## Round 12 — implementation stage

- **Unit** — `stabilize-deployment-smoke-startup`; the tree fast-forwarded to `origin/main` at `634bc3b5` (#873), which
  touched `AGENTS.md` and the `gradle-build` pack (an upstream-reference closure note, not a rule this change uses);
  `git diff --name-status origin/main` lists only this unit's files.
- **Workspace** — tree `/Users/afanasyev/Development/axon-showcase-stabilize-deployment-smoke-startup`, branch
  `stabilize-deployment-smoke-startup`.
- **Supplied artifacts and diff** — the same implementation diff as round 11, with the workflow step rewrapped: the
  payload is built in `payload=$(printf …)`, the `sed` pipeline continues on its own line, and the failure message is a
  two-argument `printf`.
- **Scope** — round 11's two instances: whether the workflow's lines are within 120 characters (the pre-existing comment
  lines 65 and 67 excepted), whether the rewrapped gate still fails and passes in the harness, and whether the tree sits
  on `origin/main`.
- **Prior findings** — rounds 1–10 fixed; round 11's two applied.
- **Completed checks** — the refresh is a clean fast-forward with no deletions against `origin/main`; the harness still
  exits 1 for a keep-alive-only stream and 0 with "live-event gate passed with run-1" for a data frame;
  `spotlessApply`/`spotlessCheck` and `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` are green;
  `openspec validate --all` 27/0.
- **Limitations** — no cluster locally; the deployment end-to-end remains the branch dispatch's.
- **Subject packs** — `deployment-operations`, `jvm-testing`, `gradle-build`.
- **Deadline** — 20 minutes from this record's write.

## `deep-1` — deep review of the implementation

- **Change identity and stage** — unit/change `stabilize-deployment-smoke-startup`, implementation stage, first deep
  pass; the review gate's routing decision returned `REQUIRED` (`infrastructure-or-deployment` on
  `.github/workflows/deployment-smoke.yml` and `helm/values/axon-showcase/values-ci.yaml`).
- **Planning artifacts** — `openspec/changes/stabilize-deployment-smoke-startup/{proposal.md,design.md,tasks.md}` and
  both deltas (`specs/showcase/quality/{merge-governance,load-tests}/spec.md`); the main specs they modify
  (`openspec/specs/showcase/quality/{merge-governance,load-tests}/spec.md`).
- **Diff base and revision** — `origin/main` at `634bc3b5`; the work is uncommitted;
  `git diff --name-status origin/main` lists only this unit's files.
- **The diff** — `helm/values/axon-showcase/values-ci.yaml` (startup-probe override on the four JVM services),
  `.github/workflows/deployment-smoke.yml` (the live-event gate and the `warmUpShowcaseIds` pass-through),
  `load-tests/build.gradle.kts` (the forwarded property),
  `load-tests/src/gatling/java/showcase/loadtests/ShowcaseSimulation.java` (`WARM_UP_SHOWCASE_IDS` and the
  `sseMessageCheck` helper), `.opencode/skills/deployment-operations/SKILL.md` (two paragraphs), and the unit's records.
- **Prior findings and statuses** — quick rounds 1–12; rounds 1–11's findings are fixed and re-verified, round 12
  reports none. The two consultation findings (the warm-up event hollowing the SSE check; excluding the warm-up by ID
  rather than matching the run's IDs) are implemented.
- **Current pass** — pass 1, a full pass (initial review).
- **Known limitations** — no cluster locally, so the deployment end-to-end is the branch dispatch's; the gate harness
  and the three stub controls ran locally against a stub gateway, not against the deployed system.
- **Subject packs** — `deployment-operations`, `jvm-testing`, `gradle-build`.

## Round 13 — implementation stage

- **Unit** — `stabilize-deployment-smoke-startup`; the tree fast-forwarded to `origin/main` at `caf72739` (#876, the
  Paketo pin bump) with this unit's pack edit stashed and popped; `git diff --name-status origin/main` lists only this
  unit's files, 0 deletions.
- **Workspace** — tree `/Users/afanasyev/Development/axon-showcase-stabilize-deployment-smoke-startup`, branch
  `stabilize-deployment-smoke-startup`.
- **Supplied artifacts and diff** — the gate step after the deep pass's two fixes: `start_time` computed inside each
  attempt, the POST written to a header file and a body file, and the `Idempotency-Key` header read as the fallback when
  the body carries no ID; plus the refreshed pack file (14 insertions against the new base) and the deep-review record.
- **Scope** — the three deep-review fixes: whether the pack diff no longer reverts #876 or deletes another unit's
  records, whether the per-attempt timestamp removes the `@Future` expiry, and whether the header fallback yields the ID
  on a `202`.
- **Prior findings** — rounds 1–12 fixed and re-verified; the deep pass's three findings fixed (see `deep-review.md`).
- **Completed checks** — the harness passes for a data frame and for the `202`-with-header stub, and stays red for a
  keep-alive-only stream; `spotlessApply`/`spotlessCheck` and `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`
  are green; `openspec validate --all` 27/0.
- **Limitations** — no cluster locally; the deployment end-to-end is the branch dispatch's.
- **Subject packs** — `deployment-operations`, `jvm-testing`, `gradle-build`.
- **Deadline** — 20 minutes from this record's write.

## Round 14 — implementation stage

- **Unit** — `stabilize-deployment-smoke-startup`; base `origin/main` at `caf72739`; only the unit's files differ.
- **Workspace** — tree `/Users/afanasyev/Development/axon-showcase-stabilize-deployment-smoke-startup`, branch
  `stabilize-deployment-smoke-startup`.
- **Supplied artifacts and diff** — `tasks.md`: task 2.2's note rewritten (the first draft's `curl | grep -q` failure
  came from the harness's `set -o pipefail`; the step declares no `shell:` and runs `bash -e`; the harness now runs
  those flags, and all three stub modes behave) and task 4.5 ticked with its verification note; `deep-review.md`
  unchanged.
- **Scope** — round 13's two instances: whether the note's attribution now matches the shells involved and the harness's
  recorded runs, and whether task 4.5's tick agrees with the record it cites.
- **Prior findings** — rounds 1–12 fixed and re-verified; round 13's two applied; the deep pass's three verified fixed.
- **Completed checks** — the harness under `bash -e` exits 1 for keep-alive-only and 0 for the data-frame and
  `202`-header stubs; `spotlessApply`/`spotlessCheck` and `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` were
  green after the workflow change; `openspec validate --all` 27/0.
- **Limitations** — no cluster locally; the deployment end-to-end is the branch dispatch's.
- **Subject packs** — `deployment-operations`, `jvm-testing`, `gradle-build`.
- **Deadline** — 20 minutes from this record's write.

## `deep-2` — fix verification

- **Change identity and stage** — unit/change `stabilize-deployment-smoke-startup`, implementation stage, pass 2 (fix
  verification) of a `REQUIRED` deep review.
- **Planning artifacts** — unchanged from pass 1: `proposal.md`, `design.md`, `tasks.md`, and both deltas.
- **Diff base and revision** — `origin/main` at `caf72739`; work uncommitted; only the unit's files differ.
- **The diff since pass 1** — the branch fast-forwarded to `caf72739` (with the `deployment-operations` edit stashed and
  popped), the gate's `start_time` computed inside each attempt, the POST writing `-D`/`-o` files with an
  `Idempotency-Key` fallback, and `tasks.md`'s note corrected to the step's `bash -e` shell.
- **Prior findings and statuses** — pass 1's `stale-base`, `expired-start-time`, and `id-only-from-body` are recorded as
  `FIXED_AND_VERIFIED` in `deep-review.md`; quick rounds 13 and 14 raised and closed the shell-attribution note and the
  unticked routing task.
- **Current pass** — pass 2, a targeted verification of the three fixes and of the gate's behaviour under the runner's
  shell.
- **Known limitations** — no cluster locally; verification is by source, the stub harness, and the check suite.
- **Subject packs** — `deployment-operations`, `jvm-testing`, `gradle-build`.

## Round 15 — implementation stage

- **Unit** — `stabilize-deployment-smoke-startup`; base `origin/main` at `caf72739`; only the unit's files differ.
- **Workspace** — tree `/Users/afanasyev/Development/axon-showcase-stabilize-deployment-smoke-startup`, branch
  `stabilize-deployment-smoke-startup`.
- **Supplied artifacts and diff** — `design.md`'s claims-table row re-anchored to the structural fact (the readiness
  loop precedes the `gatlingRun` invocation, with the live-event gate between them) and `deep-review.md`'s pass-2 entry.
- **Scope** — the deep pass-2 nitpick: whether the row no longer depends on a line number, and whether the deep-review
  record agrees with the pass and the register.
- **Prior findings** — rounds 1–14 fixed and re-verified; round 14 clean; the deep pass 2 confirmed pass 1's three fixes
  and raised the one nitpick now applied.
- **Completed checks** — `spotlessApply`/`spotlessCheck`; `openspec validate --all` 27/0; the register and ledger checks
  pass.
- **Limitations** — no cluster locally; the deployment end-to-end is the branch dispatch's.
- **Subject packs** — `deployment-operations`, `jvm-testing`, `gradle-build`.
- **Deadline** — 20 minutes from this record's write.

## `advisor-3` — consultation on the finished implementation

- **Owner's authorization** — the owner's explicit instruction of 2026-11-09: "Отревьювь с советником до согласия",
  after the implementation reached its review boundary.
- **Owner's objective and constraints** — a minimal, correct fix that does not weaken what the smoke verifies; no
  product behaviour change unless the defect is the product's; the process keeps its approval stop before delivery.
- **Question and proposed next action** — does the implementation as built hold against the two agreed directions (wait
  for the live-event stream; exclude the warm-up showcases), and what, if anything, must change before the owner's
  manual pass? Proposed next action: ask the owner for the manual implementation review.
- **Change identity and stage** — unit/change `stabilize-deployment-smoke-startup`, implementation stage, at the
  implementation-review boundary; fifteen quick rounds (round 15 clean), deep review `REQUIRED` with a full pass (three
  findings) and a targeted verification pass (three confirmed fixed, one nitpick fixed).
- **Artifacts and diff base** — `origin/main` at `caf72739`; the work is uncommitted and staged; the diff is
  `helm/values/axon-showcase/values-ci.yaml` (startup-probe override), `.github/workflows/deployment-smoke.yml` (the
  gate), `load-tests/build.gradle.kts` (the forwarded property),
  `load-tests/src/gatling/java/showcase/loadtests/ShowcaseSimulation.java` (`WARM_UP_SHOWCASE_IDS` and
  `sseMessageCheck`), `.opencode/skills/deployment-operations/SKILL.md` (two paragraphs), and the change's artifacts and
  unit records.
- **Evidence of the implementation's behaviour** — the chart render (four Deployments at `failureThreshold: 20`,
  `timeoutSeconds: 3`; the defaults without the values file); three stub-gateway controls (warm-up-only replay: the SSE
  check fails 10/10; a run event pushed before its POST response: the run passes 35/35; the mid-hold check under a
  sustained profile: fails with the exclusion and passes when it is removed, the file restored); the gate harness under
  the step's `bash -e` flags (keep-alive-only exits 1, a data frame exits 0, a `202` with `Idempotency-Key` exits 0 with
  the header's ID); `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` and `openspec validate --all` green.
- **Prior findings and statuses** — all quick-round findings fixed and re-verified; the deep pass's three findings
  `FIXED_AND_VERIFIED` in pass 2, and its nitpick fixed.
- **Subject packs** — `deployment-operations`, `jvm-testing`, `gradle-build`.
- **Report audience and language** — owner-facing, Russian.
- **Fresh or follow-up** — fresh consultation on a new decision.
