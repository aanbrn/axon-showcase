# Tasks

## 1. Define the metric in the auditor

- [x] 1.1 In `.opencode/agent/agents-auditor.md`'s accretion section, state the one metric: the `accreted` count is the
      number of distinct meta rule units carrying at least one `captured:` marker, each counted once however many
      markers it carries, with rules written before the marker convention falling outside it — and require the
      new-since-the-newest-`docs/audits/`-report figure with its `none recorded` and unavailable fallbacks. Verify by
      reading the section back and confirming it states the metric and both fallbacks.
- [x] 1.2 Update the definition's report-contract verdict line to carry the new-since figure alongside `<n> accreted`.
      Verify the line's shape matches the delta spec's "The accretion count uses one fixed metric" scenario.
- [x] 1.3 Confirm the definition's frontmatter `description` still describes what the audit reports and needs no metric
      wording (it names the accreted meta rules with their origins, not a count); leave it unchanged and record the
      decision.
- [x] 1.4 Verify `.opencode/agent/agents-auditor.md` and
      `openspec/changes/define-agents-auditor-growth-metric/specs/showcase/quality/agent-skills/spec.md` state one and
      the same definition — same metric, same fallbacks — with no drift between them.

## 2. Align the trigger command

- [x] 2.1 In `.opencode/commands/audit-agents.md` step 1, name the metric and the new-since figure so the trigger's
      read-list matches the definition. Verify by reading step 1 back and confirming it names both.

## 3. Docs sweep

- [x] 3.1 In `docs/ideas.md`, remove the implemented "Give the agents-auditor's growth metric one definition" entry, and
      update "Trend the audit counts across reports" so its closing sentence drops the reference to the removed
      2026-10-05 entry and reflects that one definition now makes the counts comparable. Verify with
      `grep -n "growth metric one definition"` (absent) and `grep -n "2026-10-05 entry"` (absent), and by reading the
      trend entry.
- [x] 3.2 Confirm `README.md` and `AGENTS.md` need no edit — their references say "accreted meta rules with their
      origins" and "the accreted-rule count", which the new metric satisfies and no site states a competing definition;
      record the grep. Correct any site that does state one.
- [x] 3.3 Confirm the `showcase/quality/agent-skills` capability's `## Purpose` needs no refresh (the metric refines the
      audit's accretion class rather than changing the capability's scope); record the check.

## 4. Verification

- [x] 4.1 Run `openspec validate --all` and confirm the change and the corpus validate (the delta's scenario
      preservation in particular).
- [x] 4.2 After the final edit, run `./gradlew spotlessApply` and then `./gradlew spotlessCheck`, and confirm the
      touched markdown (`.opencode/agent/agents-auditor.md`, `.opencode/commands/audit-agents.md`, `docs/ideas.md`, the
      delta spec) is formatter-clean.
- [x] 4.3 After an OpenCode reload (an edited subagent is not registered until then), run the updated `agents-auditor`
      (via `/audit-agents`) and confirm its verdict line states the metric and the new-since-last-report figure; record
      the output. This run is a precondition for archive. **Run (2026-10-10):** the `agents-auditor` returned the
      verdict line
      `4 findings (3 merge, 0 removal, 1 route), 2 advisory, 80 accreted (8 new since 2026-10-04-scheduled)`. The metric
      branch is exercised — `80` distinct marker-carrying units (below the `118` markers the 2026-10-04 report noted, so
      the count is units, not markers) and the new-since figure is stated against a named prior report. The
      `none recorded` / unavailable fallbacks were not exercised (a prior report exists and history is complete): the
      unit's residual, named in the report. A run echoing the edited text is not evidence of definition pickup (the
      registration gotcha says a self-echo proves nothing), so no reload claim is made here. The run's findings: the
      standing AGENTS.md reduction candidates (already parked) and one new low-confidence advisory (the vendored
      `axon4to5-*` skills pin Anthropic model aliases `sonnet`/`haiku`), now parked in `docs/ideas.md`.

## 5. Capture

- [x] 5.1 Run the `lesson-capture` subagent over the diff, apply its durable proposals, and record the applied net
      `AGENTS.md` delta on this task. **Outcome:** the subagent proposed one addition (a rule that a change rewriting a
      parked idea entry must not claim more than the change delivers); **declined as a duplicate** of the existing "Doc
      claims must match their source and their strength" bullet, which already covers an overstated claim and the
      grammatical-subject binding (`AGENTS.md:2452`) — its removal/retirement list was empty. Applied net `AGENTS.md`
      delta: **0 lines**.

## Workflow follow-up

- Archive the change after the project's review requirements are satisfied, folding the delta into
  `openspec/specs/showcase/quality/agent-skills/spec.md`.
- Verify the archived result.
