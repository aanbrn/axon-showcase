## 1. State the contract in the six definitions

- [ ] 1.1 Add the shared output contract to `.opencode/agent/lesson-capture.md`: a verdict line first (the count of
      durable proposals, or that there are none), a per-item budget (the rule, its target location, one line of
      evidence), candidates verified as already-covered collapsed to one line each or one summary line, no alternatives,
      and the explicit "bound the report, not the analysis" sentence
- [ ] 1.2 Apply the same contract to `.opencode/agent/review-quick.md`, preserving its existing "say so in one line"
      terminating case as the verdict line and its 120-character manual check
- [ ] 1.3 Apply the same contract to `.opencode/agent/review-thorough.md`, preserving its severity grouping and its
      file/line references
- [ ] 1.4 Apply the same contract to `.opencode/agent/agents-auditor.md`, preserving its severity grouping
      (contradiction / stale / dead reference / redundant / structural) and its "lead with the highest-value fixes"
- [ ] 1.5 Apply the same contract to `.opencode/agent/specs-auditor.md`, preserving its severity grouping (structural /
      stale / duplicate / dead reference) and its judgment-not-defect flag for a reused requirement header
- [ ] 1.6 Apply the same contract to `.opencode/agent/architecture-auditor.md`, preserving its two separated sections
      (verified findings, advisory observations) and its question-per-advisory-item output — and reconcile its closing
      paragraph, which also instructs "Group findings by severity", against the two-section shape its own spec
      requirement fixes, so the definition stops contradicting itself
- [ ] 1.7 Verify the edited definitions with `./gradlew spotlessApply` and `spotlessCheck` (`.opencode/agent/**/*.md` is
      in the root markdown Spotless target, so the formatter owns their wrapping), and confirm no two definitions state
      the contract in conflicting words
- [ ] 1.8 Edit `.opencode/commands/{audit-agents,audit-specs,audit-architecture,review-thorough}.md` so each defers to
      the subagent's report contract instead of restating the grouping/section shape, and verify with `spotlessApply`
      plus `spotlessCheck` that only the intended prose changed

## 2. Capture the contract as a spec requirement

- [ ] 2.1 Add a requirement to the change's delta spec for `showcase/quality/agent-skills` stating the report contract:
      a verdict-first shape, a per-item budget, one-line coverage summaries, no alternatives, and that it bounds the
      report rather than the analysis
- [ ] 2.2 Verify the delta spec's requirement header does not rename an existing requirement (a MODIFIED header must
      match verbatim, and a new contract is an ADDED requirement), and that every scenario the existing spec still has
      under any MODIFIED requirement is carried in full
- [ ] 2.3 Confirm the delta does not carry a `## Purpose` (a delta cannot; refresh the main spec's Purpose in the
      archive commit and record it as a task there) — and decide whether the Purpose needs the report contract mentioned

## 3. Align the descriptions

- [ ] 3.1 Update the `AGENTS.md` agent bullets that describe a report's shape — the `review-thorough` bullet ("Findings
      come back grouped by severity with file/line references") and the three auditor bullets — to note the report
      contract, and verify with `./gradlew spotlessApply` plus `git diff` that only the intended prose changed
- [ ] 3.2 Confirm the `README.md` agent table rows and any report-shape prose do not contradict the contract (an
      expected no-op — the rows carry no report shape), preserving the section order and the one-line row style
- [ ] 3.3 Re-read the `agent-skills` spec requirement headers and confirm no other spec text describes a conflicting
      report shape

## 4. Verify

- [ ] 4.1 Confirm the change adds no code, workflow, or deployment edit, and that `openspec validate --all` passes
- [ ] 4.2 Smoke-run `lesson-capture` on the change itself and one auditor (e.g. `specs-auditor`) and confirm each report
      leads with its verdict and honours the per-item budget — the edited prompts take effect on an OpenCode reload, so
      reload before the run, and treat it as a precondition for archiving
- [ ] 4.3 Apply the capability `## Purpose` refresh in the archive commit if 2.3 found it necessary — recorded as an
      explicit task because a delta cannot carry a Purpose
