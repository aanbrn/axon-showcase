# Tasks — add a README auditor

## 1. The subagent and its trigger

- [x] 1.1 Add `.opencode/agent/readme-auditor.md`, pinned to `opencode-go/deepseek-v4-pro` (`mode: subagent`,
      `temperature: 0`), with a frontmatter `description` naming its three axes; scope `README.md` only; the evidence
      rule (verify against the repository); the advisory treatment of subjective quality; the shared report contract;
      and "report, do not edit".
- [x] 1.2 Add `.opencode/commands/audit-readme.md` that triggers the subagent and closes with the shared step asking
      which findings to apply (the same shape as `/audit-agents`, `/audit-specs`, `/audit-architecture`).

## 2. Spec capture

- [x] 2.1 Write the `ADDED` requirement into
      `openspec/changes/add-readme-auditor/specs/showcase/quality/agent-skills/spec.md` (the header must not collide
      with an existing requirement's). Done — header "The README is audited for accuracy, design intent, and experience
      coverage", 5 scenarios, no collision.
- [x] 2.2 Add a `MODIFIED` block for "Report-producing subagents follow a shared report contract", whose bearer list
      names the three auditors and so goes stale with a fourth; copy the whole requirement (4 scenarios) and add the
      README auditor to the list. Done — copied in full, 4 scenarios preserved.
- [ ] 2.3 Refresh the capability's `## Purpose` in the archive commit to name the README as a fourth audited artifact (a
      delta cannot carry a Purpose, so the edit is deferred to that commit and recorded here).

## 3. Docs

- [x] 3.1 Add a `readme-auditor` convention bullet to `AGENTS.md` beside the other auditor bullets, in the same shape
      (what it audits, its trigger, the pro model, and the report contract).
- [x] 3.2 Update the three stale auditor enumerations in `AGENTS.md`: the report-contract sentence (~795), the "three
      auditors own …" enumeration in the `docs/ideas.md` sweep clause (~634 — the clause's "no auditor covers that file"
      stays true, only its parenthetical count changes), and the multi-artifact-sweep sentence's "no auditor covers
      `README.md`" (~867).
- [x] 3.3 Add the `readme-auditor` row to the README agent table (`README.md` ~232) and the `/audit-readme` row to the
      slash-command table (~358), and a sentence at the site that describes the auditors (`README.md` ~310, the
      guidance-and-tooling paragraph; the specs and architecture auditors have their own prose sites at ~191 and ~207).
- [x] 3.4 Remove the implemented `README auditor` idea from `docs/ideas.md`; update the sibling "Reconcile the
      architecture description" idea whose text calls it "the parked _README auditor_"; and fix the off-peak entry's
      counts, which this change makes wrong ("the three `/audit-*` agents … the pin's fifth agent, `diagrammer`" → four
      and sixth, and "the three `/audit-*` sweeps" → four).

## 4. Verification

- [x] 4.1 **Smoke-ran `/audit-readme`** after a session reload (the subagent was initially unregistered,
      `Unknown agent     type: readme-auditor`). Both controls behaved: it found the seeded inaccuracy (README:75 said
      "three replicas" against `commandService.replicaCount: 2`; reverted) and it _considered and excluded_ the
      deliberate OpenSpec-flow diagram asymmetry, quoting the `AGENTS.md` record. It reported the control plus **three
      further defects** (none spurious): two pre-existing — the `check -PskipITs` Docker-free command missing
      `-Pcoverage.gate.enabled=false` at two README sites, and an automation narrative claiming the agent "merges once
      green" contradicting the README's own "the agent only merges with your approval" — and one introduced by this
      change, a stray fourth table cell on the new `readme-auditor` row that `spotlessCheck` passes on. Not
      "everything", not "nothing"; all three verified against the repo and fixed here.
- [x] 4.2 Confirm the subagent is discoverable (`.opencode/agent/readme-auditor.md` present with `mode: subagent` and
      the model pin) and that the `/audit-readme` command names it.
- [x] 4.3 `./gradlew spotlessApply` then `spotlessCheck` green; no line over 120.
- [x] 4.4 `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` green, and `openspec validate --all` with the delta
      accepted.
- [x] 4.5 Grep for other stale enumerations this change makes false (the auditor count, "the three `/audit-*` agents",
      any "no auditor covers X") across `AGENTS.md`, `README.md`, `docs/ideas.md`, and the spec corpus.
