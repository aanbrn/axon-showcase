# Tasks

## 1. The boundary (the opportunity's own precondition)

- [x] 1.1 Draw and record the stay/move boundary the report said was undrawn, and put the mixed paragraph to the owner.
      Verify every unit is classified and the retained root carries what a reader must act on. **Done** — the boundary
      is recorded in `design.md`'s Decisions and confirmed by the owner on 2026-10-19, including the mixed paragraph:
      the rebuild-cost paragraph is split by clause, the test-timing lesson staying at root.
- [x] 1.2 Verify the boundary against the spec: confirm `merge-governance` already owns the gate composition and the
      observational workflows' outcomes, and that the gate-placement rule is the one thing it does not state. **Done** —
      `merge-governance` carries the gate composition (66 `gate` mentions) and the observational workflows' outcomes;
      the gate-placement rule is the one thing it does not state (`can remediate it` → section 1, spec 0).

## 2. No spec delta

- [x] 2.1 Confirm no capability spec changes: every moved narrative's outcome is already stated, so the trim deletes
      prose with a pointer. **Done** — corrected in review round 1: the planned `ADDED` requirement for the release
      workflow duplicated `showcase/quality/releases`, which already owns the trigger and its guards, the five images
      and the Helm chart, the tag created only after they publish, and the dry-run and backfill paths. The delta file is
      deleted, `.openspec.yaml` declares `skip_specs: true`, and `grep -rn "Continuous Integration" openspec/specs/`
      finds no spec owning the section's prose. `openspec validate --all` passes.

## 3. The trim

- [x] 3.1 In `AGENTS.md`, replace the moved per-workflow outcome narratives with the retained pointer, keeping the
      gate-placement rule, the two procedures, the schedule-placement rule, the `ubuntu-latest` rule, and the CI-only
      failure diagnostic lesson. Verify no identifier or warning a moved block carried is left homeless. **Done** — the
      section head now carries the pointer (the spec owns the outcomes, each workflow's YAML its mechanism) over the
      gate-placement rule with its retry-not-tolerate corollary (its "listed below" clause replaced by the
      out-of-repository half), the schedule-placement and `ubuntu-latest` rules, the CI-only diagnostic lesson, and both
      procedures. Every moved identifier or warning traced to a home by class: the `merge-governance` requirement owning
      that workflow's outcome; an ADR (the release tag and version, the image and chart publication) or
      `showcase/quality/releases`; a mechanism's own file (the workflow YAML, the Helm release-target declarations in
      `build.gradle.kts`, or the config carrying a value); the Build & Test `check` note for the `check` members; and
      the README's human-facing copy. The one imperative with no other home — a change to the built bundle or its
      runtime must run `:showcase-web-ui:e2eTest` — stays at root with its capture marker. Two cross-references the
      deletions orphaned are retargeted: the Kubernetes section's deployment-smoke note and the upstream-reference
      gotcha's "fifth observational check". At this task's end the section is 164 lines / 15,027 bytes, from 252 /
      23,620 at `HEAD`; 3.2 and 3.3 trim further. Also corrected `design.md`'s Context to point at claims row 1 for the
      section's size, since its own copy read 253 / 23,621 where the row's command measures 252 / 23,620.
- [x] 3.2 In `AGENTS.md`, split the rebuild-cost paragraph by clause: the cache mechanics stay at root as a pointer,
      their detail deleted with the outcome detail, and the test-timing lesson stays. Verify both halves read as one
      model. **Done** — the pointer paragraph is headed by the cost an agent plans around (a one-time full rebuild, ~9
      min vs ~1 min warm), names the cache configuration as the workflow YAML's, and keeps the workspace-`build/`
      warning, which no other live artifact carries; the hash-partition, `cache-read-only`, and re-warm clauses explain
      the cost without the log keys or the #138/#137 anecdote. The test-timing lesson is its own paragraph with a bold
      lead — the 4-vCPU starvation evidence, the load-timing heuristic, the `eslint-rules.test.ts` budget, the failed
      30s raise, the idea record, and the diagnostic imperative — and no longer carries the cache-specific "self-heals
      once `main` re-warms" clause. The halves read as one model: the pointer introduces the cold build the lesson names
      at its open and close. Also deleted the `main-required-checks` ruleset sentence that rode the cache paragraph,
      since `merge-governance` owns it. Section at this task's end: 151 lines / 13,550 bytes, from 164 / 15,027.
- [x] 3.3 In `AGENTS.md`, point the dependabot/CODEOWNERS narrative at its home. Verify the retained line carries what a
      reader must act on. **Done** — the narrative is condensed to its rules and points at the two config files
      (`dependabot.yml`, `CODEOWNERS`) and the config-read-path gotcha that carries the probe. Retained: the owner
      notification lives in `CODEOWNERS` and a `reviewers` key is silently ignored, the `opencode` and Snyk refs are
      deliberately floating, and the `opencode` cache warning is benign with its upstream reference
      (`anomalyco/opencode#49127`, which stays in a file `upstreamReferences` scans). The probe's base-branch and draft
      caveat moved into that gotcha beside the author caveat it already carried; the 2025-05-20 changelog link, the
      `#91` anecdote, and the trigger names dropped, since the gotcha's service probe is the durable evidence. Section
      at this task's end: 143 lines / 12,707 bytes, from 151 / 13,550 — 252 / 23,620 at `HEAD`; the review round's
      corrections leave it at row 8's figure.

## 4. Pre-write derivation pass

- [x] 4.1 The pre-write derivation pass ran over the planning artifacts: each fact the artifacts state about their
      evidence resolved into a row of the claims table in `design.md` or a dismissal recorded beside it. **Done** —
      `python3 scripts/claim-scan.py` ran over the authored set (exit 0) and every candidate is a row or a by-kind
      dismissal beside the table. The table gained the release workflow's true home, `showcase/quality/releases` (row 7,
      re-derived over the whole corpus in review round 1), the measured reduction (row 8), and the `skip_specs` basis
      (row 9), and its size row is sharpened to the report's line count — the report's ~26KB is rounded, so the
      proposal's "measured accurate" clause now says the line size measured exact. Rows 1-4 and 6-9 were re-derived and
      reproduce; row 4's command cell had over-escaped asterisks, fixed so it runs as shown.

## 5. Verification and the review gate

- [x] 5.1 `./gradlew spotlessApply` then `./gradlew spotlessCheck` and `openspec validate --all`; confirm clean.
      **Done** — `spotlessApply` and `spotlessCheck` are clean, `openspec validate --all` passes 27/27, and the config
      read-path probe is silent.
- [x] 5.2 The 120-character check over the change's whole changed set, derived with
      `git status --porcelain --untracked-files=all`, using the `perl -CSD -lne` recipe. **Done** — the recipe flags
      only `AGENTS.md`'s pre-existing long `bootBuildImage` command (untouched by the diff) and the `design.md`
      claims-table rows (formatter-exempt and unwrappable); no added line exceeds 120, `openspec/config.yaml` included.
- [x] 5.3 The review gate ran: `review-quick` over the proposal and the implementation, repeating until clean, with each
      round's row in `docs/review-rounds.tsv` carrying the identifier the invocation returned. **Done** — eight rounds
      against one session (`ses_eac357de4ffe8XgTh2ZRIUbJBM`), created on round 1 and continued through round 8 with the
      returned identifier matching the saved one each time. The loop went 4 → 2 → 1 → 0 findings, the capture's round 5
      raised 3, rounds 6-7 one formatting finding each, and round 8 reported nothing; each row is in the ledger with its
      classes, compliance miss count, session identifier, and `continued` transition.
- [x] 5.4 The deep-review routing decision is recorded on this task, derived with
      `python3 scripts/review-routing.py --change extract-ci-narrative`. **Done** — the derivation reports `RECOMMENDED`
      on `[repeated-class]` (4 rows repeat a prior class). Resolved to proceed without a deep review: the repeated
      classes are all applied and round 8 is clean, the change ships guidance prose with no code or spec behavior, and
      the outside-`AGENTS.md` risk was swept — no live doc narrates the removed detail (the hits are ADR-0019's and the
      README's own release facts, plus a parked idea's reference to the still-existing `helmInstallToCi`).

## 6. Capture

- [x] 6.1 The `lesson-capture` subagent ran over the diff and the review findings, and its durable proposals were
      applied — or the skip condition held in full and its reason is recorded — with the applied net `AGENTS.md` delta
      on this task. **Done** — the subagent ran (the skip condition could not hold: the rounds recorded `premise`,
      `contradiction`, and `claim-unreproducible` classes, and compliance misses) and proposed two merges, both applied:
      the spec-delta step 1 now says an outcome's owning capability is not the section's heading or a report's proposed
      destination (one section can span capabilities), and the review gate's claims-table step 2 requires the stated
      command to be re-run so it reproduces the recorded figure; the matching `openspec/config.yaml` design rule is
      refreshed. It retired no rule and made no authoring-system recommendation; the two lessons it found already
      covered (the corrected-fact sweep, the trim's destination check) are recorded as such. Applied net `AGENTS.md`
      delta: +5 lines (the two `git diff -U0` hunks, +3 and +2).

## 7. Manual review and archive

- [x] 7.1 The owner's manual review pass was requested after the loop was clean and the capture ran. **Done** —
      requested on 2026-10-19 once round 8 was clean and the capture applied; the work stays uncommitted until the owner
      answers.
- [ ] 7.2 The change was archived with `openspec archive extract-ci-narrative --yes`, the change-dir move and the
      `openspec/specs` sync staged together, and the archive commit ridden on this unit's PR.

## Workflow follow-up

- Nothing committed, pushed, or archived before the owner approves the implementation.
