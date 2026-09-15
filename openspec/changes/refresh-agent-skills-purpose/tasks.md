## 1. Refresh the Purpose

- [x] 1.1 Confirm the `agent-skills` Purpose omits the visual and diagram agents that its "Per-change quality-gate and
      analysis subagents are available" requirement covers, and that the wording to add is that requirement's own ("the
      visual and diagram agents")
- [x] 1.2 Confirm the edit is confined to the Purpose paragraph and that no requirement, scenario, or other spec text
      changes — a Purpose cannot ride a delta, and the main spec is never edited before the archive
- [ ] 1.3 Apply the Purpose edit in the **archive commit** (not the implementation commit), as the archived
      `address-new-snyk-findings` and `apply-specs-audit-findings` changes did, and confirm `openspec validate --all`
      still passes after it lands

## 2. Remove the implemented idea

- [x] 2.1 Remove the parked `agent-skills` Purpose idea from `docs/ideas.md` under `## 2026-09-15`, and confirm no other
      idea in that section becomes stale or mis-numbered by its removal

## 3. Verify

- [x] 3.1 Confirm no agent definition, `AGENTS.md`, or `README.md` edit is needed — the Purpose is prose about the
      capability, not a description any of them restate
- [x] 3.2 `openspec validate --all` passes; `./gradlew spotlessApply` and `spotlessCheck` are green
