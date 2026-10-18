# Tasks

## 1. The reviewer definitions

- [x] 1.1 `.opencode/agent/review-quick.md` — replace the depth clauses with the delta's fixed scope and stop condition:
      the pass inspects only the supplied diff and the explicitly supplied planning artifacts; the seven fixed subjects;
      the "not required to" list (class exhaustion, every claims-table source, mutation-style test analysis, exhaustive
      boundary/error-path/caller/configuration/documentation analysis beyond the supplied scope); the once-per-artifact
      stop condition; the report's statement of that scope; and the statement that the governance the pass does not
      replace stays in force. Keep the verdict-first shape, the class list, the prior-instance handling, the record
      line, the formatter routing, and the capture durability check. Verification: every clause the delta adds or
      narrows appears in the definition, and no clause the delta removed survives.
- [x] 1.2 `.opencode/agent/review-thorough.md` — add the risk dimensions the delta names (specification compliance and
      delta-spec scenarios; correctness, state transitions, failure paths, retries, timeouts, and idempotency;
      architecture, dependency boundaries, and approved-design deviations; concurrency, reactive behavior, ordering,
      transaction boundaries, persistence, and distributed failure modes; tests and missing verification for concrete
      risks; security, trust boundaries, configuration, deployment, observability, and operational risks; the applicable
      repository conventions and structured-writing rules); the finding requirements (a concrete defect or material
      risk, repository evidence, a precise location, the consequence, a corrective direction, and confirmed-versus-
      uncertainty); and the hand-over, independent-reinspection, and pass expectations. Verification: each dimension and
      finding requirement appears once; the existing severity-group report format is unchanged.
- [x] 1.3 Verify the two definitions and the delta state the same obligations. Verification: read each definition
      against the delta's scenarios; report any clause stated in one and not the other.

## 2. The routing decision

- [x] 2.1 `scripts/review-routing.py` — derive the routing floor from the change's own facts: its changed paths and the
      declarations in its diff against the signal table in `design.md`, its delta spec's block kinds, and its recorded
      rounds in `docs/review-rounds.tsv`; return `REQUIRED`, `RECOMMENDED`, or `NOT_NEEDED` with the matched signals;
      and return `REQUIRED` on an unreadable, missing, or malformed input. Mirror `scripts/claim-scan.py`'s interface
      (`--base`, default `origin/main`) and include the untracked change dir. Verification: run it on this change and on
      a docs-only path; the outputs name the signals.
- [x] 2.2 `scripts/test-review-routing.py` — the known-bad and known-good cases the repository requires of a check, one
      per signal class in `design.md`'s table: a public-contract path → `REQUIRED`; a domain path → `REQUIRED`; a
      persistence path (`*.sql`, `db/**`) → `REQUIRED`; a changed file declaring a security filter chain → `REQUIRED`;
      an infrastructure or deployment path (`helm/**`, `.github/**`, `config/**`, `docker-compose.yml`, a `Dockerfile`)
      → `REQUIRED`; an `ADDED`, a `REMOVED`, and a `RENAMED` requirement block → `REQUIRED` each; a repeated-class
      ledger → `RECOMMENDED`; a docs-only change → `NOT_NEEDED`; and each fail-safe input — an unreadable, a missing,
      and a malformed one → `REQUIRED`. Verification: each case fails against a rule with its signal removed, so every
      arm of every alternation is exercised and the test is not vacuous.
- [x] 2.3 `build.gradle.kts` — register `testReviewRouting` (the `testReviewRounds` shape: `inputs.file` for the script,
      its test, and the ledger; `outputs.upToDateWhen { false }`) and add it to `check`. Verification:
      `./gradlew testReviewRouting` runs it; `./gradlew check --dry-run` lists it.

## 3. The command and the workflow

- [x] 3.1 `.opencode/commands/review-thorough.md` — make the command the deep-review procedure's home: creating or
      reusing the one session, the hand-over contents, the pass count, and the status record on the change's review
      task; and state that a manual invocation is an explicit run that does not route and is never a substitute for a
      `REQUIRED` outcome. Verification: the command's steps name the session, the hand-over, the pass count, and the
      status record, and it carries no routing step.
- [x] 3.2 `AGENTS.md` — the review-gate workflow gains the routing step (derive the floor with the script, decide at or
      above it, record the outcome and signals on the change's review task, and create no session for `NOT_NEEDED`),
      then runs the deep review through the command's procedure when the outcome requires it; its completion criterion
      names a required deep review as a precondition of completion; and the thorough-review bullet's "intentionally not
      auto-scheduled" statement becomes the risk-routed decision, keeping its pointer at the command. Verification: the
      workflow's Trigger/Procedure/Decision points/Failure handling/Completion criteria name the routing decision, the
      script, the `NOT_NEEDED` bypass, and the pass budget; the bullet points at the spec and the command rather than
      restating their clauses.
- [x] 3.3 `openspec/config.yaml` — the `tasks` rule names the routing decision beside the capture decision, so a
      change's task list carries it. Verification: the item is a single-line string with no unquoted `: `, and the
      `openspec instructions tasks` command for this change returns it populated.

## 4. The documentation sweep

- [x] 4.1 `README.md` — the reviewer table rows, the `/review-thorough` slash-command row, and the prose describing the
      two passes state the bounded screen and the risk-routed deep review. Verification: grep `review-quick` and
      `review-thorough` across `README.md`; each live site states the new behavior.
- [x] 4.2 `docs/ideas.md` — the 2026-10-15 "rejected levers" entry is updated: its rejected cap is scoped to the quick
      loop, and the deep pass's budget is recorded as the owner's decision. Verification: the entry names both loops and
      the owner's decision; no other entry is disturbed.
- [x] 4.3 Sweep the corpus for a site that still describes the quick pass's removed obligations, the thorough pass as
      manual-only, or the review gate's steps without the routing decision — including the two live enumerations the
      review found: `AGENTS.md`'s "The review gate covers every unit of work and every outward-facing artifact" bullet,
      and `.opencode/commands/opsx-tool-update.md`'s "run the repository's review gate" step. Verification: grep
      `exhaust`, `auto-scheduled`, `bounds? the report`, and `review gate` across `AGENTS.md`, `README.md`,
      `.opencode/`, `openspec/specs/`, and `openspec/config.yaml`; each live site matches the delta.

## 5. Verification

- [x] 5.1 `./gradlew spotlessApply` after the last edit to a Spotless-owned file, then `./gradlew spotlessCheck`.
      Verification: both green, and the apply ran after the final write.
- [x] 5.2 `openspec validate --all` and `openspec validate --changes`. Verification: no failures; the delta's scenario
      count is preserved plus the eight added.
- [x] 5.3 The 120-character recipe over the changed set the formatter does not cover (the Python scripts and YAML).
      Verification: no line over 120.
- [x] 5.4 Run the per-unit `lesson-capture` decision against the skip condition and record the outcome here — the run,
      or the skip and its reason. **Done** — the change touches `AGENTS.md`, so the skip condition cannot hold, and the
      run returned 3 durable proposals (2 additions, 1 retirement), 0 authoring-system recommendations, net **0**
      `AGENTS.md` lines. Applied: the Structured Writing Standard's scope sentence now names "an agent, command, or
      skill definition under `.opencode/`" (the class that recurred across rounds 1, 9, 10, 11, with its marker); the
      fail-safe gotcha's title widened from a version lookup to "A derivation whose failure maps onto the benign outcome
      reads as a clean result" (this unit's routing floor first read a mistyped `--change` as `NOT_NEEDED`), with its
      referring site updated; and the routing step's duplicated enumeration of the spec's four items retired to a
      pointer. The retirement's own verdict counted the definitions' scope at +1, the retitle at 0, and the retirement
      at −1.
- [x] 5.5 Carry the pre-write derivation pass — its output is the claims table in `design.md`, and each row's source or
      deriving command reproduces.
- [x] 5.6 Inspect `lesson-capture` and `experience-analyzer` before changing any trigger: confirm neither needs a
      change, and record what the inspection found. **Done** — neither changes. `lesson-capture.md`'s opening already
      reads "Use for a unit whose implementation (and its quick review) shows evidence of a reusable lesson", and the
      `agent-skills` scenario already has "the main agent decides from the unit's own evidence whether it shows a
      candidate lesson, and invokes the `lesson-capture` subagent only when it does"; `experience-analyzer.md` is
      already "Use periodically", and the spec already keeps it on-demand only ("its retrospective is a narrative
      judgment about a period ... so a scheduled run could not supply it"). No post-review side workflow is added.
- [x] 5.7 Validate the orchestration the repository can validate, and report what it cannot. The routing floor's
      behavior is covered by 2.2 (automated). Static inspection covers the definitions and the command (1.3, 3.1). The
      session, hand-over, continuation, and pass-budget behavior is **runtime and unverified by any gate** — record
      which of those remain unverified, why (a subagent invocation is not reachable from `check`), and what evidence
      exists (a manual smoke-run of a routed change). Verification: the report names each behavior listed here — the
      `NOT_NEEDED` bypass (no session, no hand-over, no expensive-model invocation), the `REQUIRED` path running a deep
      review, the `RECOMMENDED` explicit resolution, the fail-safe on a missing or malformed decision, quick review and
      the tests still running when deep review is skipped, the hand-over's context and returned findings, a follow-up
      pass resuming the same session, a different change getting a fresh one, a failed hand-over never reading as a
      completed review, findings verified after fixes, the two-pass budget, the quick loop's own session lifecycle, and
      no alternate entry point invoking deep review unconditionally — each with its verification class (automated,
      static inspection, or runtime-unverified). **Done** — the classes, per behavior: **automated** — the routing
      floor's arms (24 cases in `scripts/test-review-routing.py`, one per signal arm plus the `NOT_NEEDED`,
      `RECOMMENDED`, and three fail-safe cases), which is what "a missing or malformed routing cannot silently skip
      mandatory review" reduces to mechanically; this unit's own decision (task 5.8) and its 11 recorded rounds exercise
      the floor end to end. **static inspection** — the low-risk bypass, the `REQUIRED` path, the `RECOMMENDED`
      resolution, the preserved quick review/tests/checks, the hand-over contents, the independent re-inspection, the
      two-pass budget, and the absence of an alternate unconditional entry point: each is stated in the delta, the two
      definitions, or the command, and was read against them (task 1.3, round 11's parity check). **runtime-unverified**
      — session creation, hand-over, continuation, and a failed hand-over's detection: a subagent invocation is not
      reachable from `check`, so no gate exercises them. The evidence available is this unit's own loop (11 rounds
      continuing one `review-quick` session) and the mechanism's documentation; a routed deep pass would be the only
      direct exercise, and its `RECOMMENDED` resolution is the owner's decision at the manual review. Recorded as the
      limitation it is, not as a pass.
- [x] 5.8 Record this change's own deep-review routing decision: derive the floor with
      `python3 scripts/review-routing.py --change rework-the-review-loop`, decide at or above it, and record the outcome
      and the signals it relied on here. Verification: the recorded outcome matches the script's. **Done** —
      `RECOMMENDED`, on the `repeated-class` signal (the ledger's rows for this unit repeat a class on more than one
      row). No `REQUIRED` signal: the change touches no public contract, no Axon domain surface, no persistence, no
      security declaration, and no infrastructure or deployment path, and its delta carries no
      `ADDED`/`REMOVED`/`RENAMED` block. The `RECOMMENDED` resolution — the evidence for running one deep pass (a large
      change introducing a mechanism, and the only way to exercise the runtime behaviors 5.7 cannot verify) against
      deferring it — is the owner's explicit decision at the manual review, not an automatic escalation.
