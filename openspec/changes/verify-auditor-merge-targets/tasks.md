# Tasks

## 1. Widen the auditor definition

- [ ] 1.1 In `.opencode/agent/agents-auditor.md`, state the merge-candidate target check in the conciseness axis and in
      the method: a candidate's merged text that points at a spec or ADR for content it removes must be verified against
      that target — its behavior and the identifiers, declarations, and gate conditions the delegated content names —
      and a delegated item the target does not carry stays in the merged text, the candidate saying which it kept for
      that reason.
- [ ] 1.2 Verify the widened definition keeps every existing element unchanged: the two axes, the merge/removal
      candidate definitions, the `remove` counting, the advisory and accretion classes, the provenance partition, the
      report contract, and the "never edits a file" rule.
- [ ] 1.3 Verify the definition's wording matches the delta spec's new clause and scenario one-for-one (the target's
      behavior **and** identifiers, declarations, gate conditions; the item stays; the candidate says which).

## 2. Verify the surrounding artifacts (no edit expected — confirm, do not assume)

- [ ] 2.1 Re-read the `AGENTS.md` agents-auditor bullet and confirm it already delegates the finding classes to the
      `agent-skills` spec, so this widening owes no bullet edit; if it names the merge-candidate contract inline, update
      it.
- [ ] 2.2 Confirm the caller-side rule is already present and consistent with this change — the pointer-verification
      clause and sweep instruction in `AGENTS.md`'s capture/growth paragraph (landed with #365) — so the producer side
      this change adds complements it rather than duplicating it.
- [ ] 2.3 Confirm the definition's frontmatter `description`, its own report-contract verdict line, the shared
      report-contract requirement's bearer list in the spec, and the `/audit-agents` command's step-1 read-list all need
      no edit (no new output class, no new section, no new read target); edit only what the sweep actually shows.
- [ ] 2.4 Confirm `README.md`'s auditor prose and slash-command table row need no edit — the verification rigor is not
      human-visible behavior — and state the omission in the change's report.
- [ ] 2.5 Grep the corpus and docs for the merge-candidate contract's other mentions (`grep -rn "merge candidate"`) and
      reconcile any copy this change makes stale.
- [ ] 2.6 Read the `showcase/quality/agent-skills` spec's `## Purpose` and decide whether this change falsifies its
      text; record the decision as a task, and if a refresh is owed, apply it in the archive commit (a delta cannot
      carry a `## Purpose`).

## 3. Verification

- [ ] 3.1 Run `openspec validate --changes` and confirm the change validates with the `MODIFIED` block carrying every
      scenario the main spec's requirement has, plus the new one.
- [ ] 3.2 Run `./gradlew spotlessApply` after the final edit to any Spotless-owned file, then confirm `spotlessCheck`
      and the PR gate (`./gradlew check -PskipITs -Pcoverage.gate.enabled=false`) are green.
- [ ] 3.3 Smoke-test the widened subagent after an OpenCode reload (a changed definition is read at session start), with
      both controls: a merge candidate whose pointer target genuinely lacks a named identifier (it must report the kept
      item) and one whose target genuinely holds the delegated content (it must not). Verify the seed's premise against
      the repository before reading the run, and record which branch the run exercised.
- [ ] 3.4 Run `review-quick` over the implementation diff and fix its findings, re-running until clean; then request the
      user's manual review pass before committing.
