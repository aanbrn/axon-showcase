# Tasks

## 1. Widen the auditor definition

- [x] 1.1 In `.opencode/agent/agents-auditor.md`, add the **route-candidate** standing analysis to the conciseness axis:
      its test (the rule's subject is enforced by a named, in-place deterministic mechanism — a build or CI gate, a lint
      or test, a CLI validation, or a deterministic workflow step such as the archive-time spec sync), its disposition
      (reduce the rule to a pointer at the mechanism, or move its content to the spec or ADR that owns it), and what the
      candidate must name (the mechanism, what would be lost, and confirmation that the mechanism exists and covers the
      subject). Update the axis's count words too: "Report two of these as **standing analyses**" becomes "three", and
      "Both are candidates for the owner, not actions." becomes "All are candidates for the owner, not actions."
- [x] 1.2 Add the review bound to the same axis: a rule whose only detector is a review — a human's or a review
      subagent's — is not a removal or route candidate, because review is not a gate and the rule may itself be what
      makes the violation visible to the reviewer.
- [x] 1.3 Add `route` to the finding labels and the report-contract verdict line
      (`<n> findings (<n> merge, <n> removal, <n> route), <n> advisory, <n> accreted`), and to the frontmatter
      `description`'s finding-class list.
- [x] 1.4 Update `.opencode/commands/audit-agents.md` step 2 to name the third count in the verdict line.

## 2. Docs the change owns

- [x] 2.1 Update the `AGENTS.md` agents-auditor bullet's finding-class parenthetical (line ~921) to name route
      candidates alongside merge and removal.
- [x] 2.2 Update `README.md`: the agent-table `agents-auditor` row, the `/audit-agents` slash-command row, and the
      "guidance and tooling are audited" prose to name route candidates.
- [x] 2.3 (Smoke-run follow-up) Trim the `AGENTS.md` `Line Length` bullet's inline markdown-scope enumeration to its
      existing pointer at the `Formatting` convention, which already carries the scope and its generated/vendored
      exclusions — the redundancy the 5.3 smoke-run surfaced as a merge candidate, against the `Avoid redundancy` rule.

## 3. Spec capture

- [x] 3.1 Write the delta at
      `openspec/changes/widen-auditor-to-route-candidates/specs/showcase/quality/agent-skills/spec.md`: one `MODIFIED`
      requirement carrying all nine existing scenarios in order, the three standing analyses, the review bound, the
      third verdict count in the audit-produced scenario, and the two new scenarios (a mechanism-enforced rule is a
      route candidate; a review-only rule is not a candidate).
- [ ] 3.2 The `showcase/quality/agent-skills` `## Purpose` enumerates the auditor's output ("the merge and removal
      candidates it reports"), which this change falsifies. Apply the refresh — "the merge, removal, and route
      candidates it reports" — in the archive commit, since a delta cannot carry a `## Purpose`.

## 4. Verify the surrounding artifacts (no edit expected — confirm, do not assume)

- [x] 4.1 Confirm the shared report-contract requirement and the definition's own verdict line need no structural edit —
      route is a finding label, not a new class, bearer subagent, or report section; edit only what the sweep shows.
- [x] 4.2 Confirm `.github/workflows/audit.yml`'s prompt names no finding class (it defers to the shared report
      contract) so it owes no edit; grep it for `merge`.
- [x] 4.3 Grep the corpus and docs for the existing class phrasing (`merge and removal`, `merge, removal`) and reconcile
      any copy this change makes stale.

## 5. Verification

- [x] 5.1 Run `openspec validate --changes` and confirm the `MODIFIED` block carries every scenario the main spec's
      requirement has, plus the two new ones.
- [x] 5.2 Run `./gradlew spotlessApply` after the final edit to any Spotless-owned file, then confirm `spotlessCheck`
      and the PR gate (`./gradlew check -PskipITs -Pcoverage.gate.enabled=false`) are green.
- [x] 5.3 Smoke-test the widened subagent after an OpenCode reload, with both controls: a rule whose subject a
      deterministic mechanism enforces (it must report a route candidate naming the mechanism) and a rule detectable
      only by review (it must not report it). Run 2026-09-24: `WIDENED: yes` (the definition carried the route
      analysis); the positive control was reported as a `route` candidate naming the Spotless `licenseHeader` gate
      (`code-check-conventions.gradle.kts:28`, wired into `check`); the negative control (constructor-over-field
      injection) was reported with no disposition, citing "review is not a gate". Seeds reverted. Beyond the seeds the
      run surfaced a merge candidate (the Line Length bullet re-lists the markdown scope the Formatting convention
      already enumerates; trimmed under 2.3) and one low item verified as not a defect ("the four audits" and "the three
      repository audits" are different groupings — all four auditor subagents vs. the three scheduled ones — both
      correct; the "no human paces" phrase is loose for the on-demand passes, an owner's wording call).
- [x] 5.4 Ran `review-quick` over the implementation diff to clean and the user gave the manual review pass; committed
      as `f5472b0` and opened PR #384 (the smoke-run follow-up diff got a further quick-review round).
