# Tasks

## 1. Single-source the mechanism in the spec

- [x] 1.1 Write the delta at
      `openspec/changes/single-source-the-scheduled-audit-mechanism/specs/showcase/quality/agent-skills/spec.md`: one
      `MODIFIED` requirement carrying the main spec's `The on-demand audits are also available on a schedule` block
      verbatim (both existing scenarios, in order, byte-identical header), with the mechanism clause replaced by the
      pointer to `showcase/quality/merge-governance` and everything `agent-skills` uniquely owns retained. Verify with
      `openspec validate --changes`.
- [x] 1.2 Confirm the mechanism detail has one effective spec site: enumerate `merge-governance`'s mechanism assertions
      (the `prompt` input, `id-token`, the single batched run, the report/pull-request artifact, and the commit-nothing
      outcome) and check the whole `agent-skills` delta — body and both scenarios — leaves each stated once (in
      `merge-governance`); the main spec still holds the pre-delta text until the archive sync replaces it.
- [x] 1.3 Record the docs-refresh decision (the docs-refresh convention's per-change check): this change edits only the
      two quality specs, so `AGENTS.md`, `README.md`, and `docs/adr/` need no edit (AGENTS.md describes the mechanism as
      workflow prose, not a spec duplicate); `docs/ideas.md` has no entry to remove.

## 2. Docs the change owns

- [ ] 2.1 Refresh `merge-governance`'s `## Purpose` **in the archive commit** (a delta cannot carry a Purpose): drop the
      "that report" qualifier and name every scheduled workflow — the update checks, the upstream-reference report, the
      repository audits, and the scheduled end-to-end, dependency-security, and deployment-smoke runs. Record the
      deferral in the change's report, since the diff shows no spec edit for it.

## 3. Verification

- [x] 3.1 Run the implementation `review-quick` loop over the diff against this change's planning artifacts; fix its
      findings and re-run until it reports nothing new.
- [x] 3.2 Run the per-unit `lesson-capture` over this change and apply its durable proposals.
- [x] 3.3 Run `./gradlew spotlessApply` after the last edit to a Spotless-owned file and confirm `spotlessCheck` passes;
      run the manual 120-character check over the change dir's `.openspec.yaml`.
- [x] 3.4 Request the user's manual review pass — the step before committing.
