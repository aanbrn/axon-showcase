# Tasks

## 1. Record the pre-write derivation pass

- [x] 1.1 Run the pre-write derivation pass over the planning artifacts: read each claim about a prior artifact from
      that artifact, re-derive every count against the set it counts, single-source each rule stated more than once, and
      derive each mechanism claim from the tool it names. Record the result as the claims table in `design.md`, seeded
      with `scripts/claim-scan.py` and carrying its by-class dismissals beside it.

## 2. The rule in the capability spec

- [x] 2.1 `openspec/changes/harden-command-definitions/specs/showcase/quality/agent-skills/spec.md` — a `MODIFIED` block
      for "Audit reduction candidates are applied by a standing owner-gated unit", copied whole from
      `git show HEAD:openspec/specs/showcase/quality/agent-skills/spec.md`, whose "Application is owner-gated and
      reviewed" scenario widens the gate it names to the full per-unit review gate. Preserve the requirement's other
      scenarios and their order; the delta is the record of what the scenario now says, so this task points at it rather
      than restating its content.
- [x] 2.2 `openspec/changes/harden-command-definitions/specs/showcase/quality/agent-skills/spec.md` — two `ADDED`
      requirements, "An audit's approved findings are delivered as separate units" and "An audit finding's delivery
      route follows its kind", each with one scenario per behavior it states. The delta is the record of what they say,
      so this task points at it rather than restating its content.
- [x] 2.3 `openspec/changes/harden-command-definitions/specs/showcase/quality/agent-skills/spec.md` — an `ADDED`
      requirement "Project-authored command definitions that mutate files carry the lifecycle preflight", with one
      scenario per behavior it states: the unit's own branch from a freshly fetched `origin/main`; unrelated dirty
      changes preserved; work left uncommitted; no commit, push, pull request, archive, or merge without the explicit
      approval; and the session-continuity preflight on every `review-quick` invocation. The delta is the record of what
      the requirement says, so this task points at it rather than restating its content.

- [x] 2.4 `openspec/specs/showcase/quality/agent-skills/spec.md` — record the `## Purpose` decision. The delta adds
      requirements governing surfaces the Purpose's enumeration never names (project-authored command definitions, and
      how an audit's findings are delivered), so the refresh is owed and applies in the archive commit — a delta cannot
      carry a Purpose. State the decision in this task either way.

## 3. The audit and reduction commands

- [x] 3.1 `.opencode/commands/audit-agents.md`, `audit-architecture.md`, `audit-readme.md`, and `audit-specs.md` —
      restructure to the labeled sections, separate report generation from applying findings, stop without a unit when
      nothing is actionable, split approved findings by route and scope, keep advisory observations as owner decisions,
      and route the apply path through the full review gate.
- [x] 3.2 `.opencode/commands/apply-audit-reductions.md` — restructure to the labeled sections, preserve the
      candidate/opportunity distinction and each candidate kind's route, and route its apply path through the full
      review gate rather than formatting plus a quick review.

## 4. The dependency and tooling commands

- [x] 4.1 `.opencode/commands/dependency-updates.md` — one Gradle invocation where the task graph supports it, the
      current run's output only, a task failure reported apart from an available update, the web UI task's error and
      exit evidence surfaced, and the existing distinctions preserved.
- [x] 4.2 `.opencode/commands/dependency-security-check.md` — report the two scans separately, surface the npm-audit
      task's applied-suppression, violation, and unsuppressed-finding outcomes, and replace the Snyk quota, billing, and
      reset-schedule assertions with operational guidance.
- [x] 4.3 `.opencode/commands/gradle-update.md` — derive the candidate version only from a successfully regenerated
      report, treat a failed or indeterminate report as indeterminate, keep the two wrapper invocations and the `-all`
      distribution type, verify all four wrapper files before reporting success, and state the lifecycle preflight the
      command's mutation of the wrapper files owes.
- [x] 4.4 `.opencode/commands/opsx-tool-update.md` — a collision-safe probe name, the probe's own removal, the prior
      `config.yaml` restored on every exit path, each positive control confirmed to have changed its source before its
      diagnostic is read, the pin inventory derived from `toolingUpdates`' declared checks, and the existing
      deep-review-routing requirement preserved.

## 5. The remaining commands

- [x] 5.1 `.opencode/commands/seed-claims.md` — treat an unavailable base ref or a failed scan as a visible failure, and
      re-run the scan after the last claims-table or dismissal edit that can add candidate-shaped text.
- [x] 5.2 `.opencode/commands/review-thorough.md` — stop and ask when the target is ambiguous, honor the active OpenSpec
      store context rather than assuming a repository-local change path, and keep the session, hand-over, status-record,
      and pass-budget semantics intact.
- [x] 5.3 `.opencode/commands/retrospective.md` — validate an explicit `since` as a date before invoking the digest,
      treat the context note as optional, and keep the digest's derived-facts block as the authority for countable
      claims.
- [x] 5.4 `.opencode/commands/setup-agent-tools.md` — state the one confirmation imperative the owner must act on before
      the global configuration is touched or an installer runs, and keep the skill as the procedure's authority rather
      than restating it.
- [x] 5.5 `.opencode/commands/ideas.md` — keep the file a scratchpad, make its mutation path comply with the lifecycle
      for a docs unit, and preserve the date-grouped append behavior.
- [x] 5.6 `.opencode/commands/check-tooling.md` and `setup-idea.md` — restructure for scanability, preserving the script
      and skill authorities without duplicating a tool inventory, an install command, or a script's semantics.
- [x] 5.7 `.opencode/commands/diagram.md` — restructure only, preserving the ASCII alignment, local Mermaid render,
      vision delegation, live-GitHub render, and measurement-before-action behavior, and add the lifecycle preflight its
      write into a repository file owes.

## 6. Verification

- [x] 6.1 `./gradlew spotlessApply` then `spotlessCheck`.
- [x] 6.2 `openspec validate --all` and `openspec validate --changes`.
- [x] 6.3 Inspect the final diff for generated or vendored files: no `opsx-*` command, `openspec-*` skill, or
      `axon4to5-*` skill is modified.
- [x] 6.4 Verify each command's referenced script, task, subagent, and skill still resolves, and that no command names a
      path or task the repository does not have.
- [x] 6.5 Run the 120-character check over the changed files no formatter owns.
- [x] 6.6 `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` (the pull-request gate).

## 7. Review gate

- [x] 7.1 Run the `review-quick` loop — proposal round then implementation round — with the session-continuity
      preflight, recording each round's row in `docs/review-rounds.tsv` from the actual invocation. **Done** — seven
      rounds, all in `ses_eaeb2de76ffe7q1CaPC3ImhTEe` (round 1 `created`, rounds 2–7 `continued`, no replacement), clean
      at round 7 (`nothing remains`); findings 9 → 6 → 5 → 3 → 3 → 2 → 0, every one fixed.
- [x] 7.2 Derive the deep-review routing decision with
      `python3 scripts/review-routing.py --change harden-command-definitions`, decide at or above the floor, record the
      outcome and its signals here, and run a `REQUIRED` review before completion or record a `RECOMMENDED` one's
      explicit resolution. **Done** — the derivation reports `REQUIRED` on the `[requirement-block-added]` signal (the
      delta's three `ADDED` requirements), so the deep review runs before completion; its session and findings are
      recorded here as they arrive. **Deep review (session `ses_eae951b36ffeG59LNDRf4CvVdG`): pass 1 four findings, pass
      2 two — all fixed.** Pass 1's should-fixes were the web UI report's file suffixes (the command and claims row 6
      implied names the task does not write), `/review-thorough`'s decision point governing a fallback its procedure no
      longer reached, and a design Decision whose rationale contradicted its chosen course; its nitpick was the delta's
      discriminator being broader than the boundary it enforces. Pass 2 confirmed three and found that two of the fixes
      were incomplete — the restored fallback had dropped the "active change" link, and the requirement's header still
      read "mutate files" — both now corrected. **No third pass:** the two pass-2 findings were one-line precision edits
      in files the two passes had already reviewed, and the gates verify them, so the recorded decision is that no
      additional pass rests on residual material risk. The deep review also resolved the lifecycle-copy question: the
      ten copies are the correct reading of the pointer rule once the Decision states the imperative-versus-procedure
      distinction.
- [x] 7.3 Run the per-unit `lesson-capture` decision: run the `lesson-capture` subagent unless the skip condition holds
      in full, apply its durable proposals, and record the applied net `AGENTS.md` delta or the skip and its reason
      here. **Done** — the skip condition did not hold (seven round classes beyond the local set, a diff touching
      authoring surfaces, a `REQUIRED` deep review), so the subagent ran (session `ses_eae86501dffe6Wzafe2z5QfCxR`).
      Applied: the single-sourcing bullet gains the imperative-versus-procedure boundary (the class this unit's seven
      rounds kept re-finding); the `dependencySecurityCheck` comment's pointer no longer names the command, which no
      longer holds the rate-limit nuance, and states where a provider's quota belongs; and the three auditor bullets'
      delivery-route sentences reduce to pointers at the delta's new requirement, keeping the architecture bullet's
      parked-as-an-idea fallback. Its authoring-system recommendation — enforce the lifecycle preflight with a
      `verifyCommandLifecycle` check rather than prose in seventeen files — is parked in `docs/ideas.md` as its own
      unit. The applied `AGENTS.md` net delta is the figure `git diff --numstat AGENTS.md` reports.
- [ ] 7.4 Ask the user for the manual review pass of the uncommitted diff. Do not commit, push, open a pull request,
      archive, or merge before that approval.
