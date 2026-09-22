# Tasks

## 1. Spec delta

- [x] 1.1 Add the `MODIFIED` delta for `showcase/quality/agent-skills`, carrying the full scheduling requirement
      (description + both existing scenarios) with the `experience-analyzer` on-demand-only clause added

- [x] 1.2 Add the second `MODIFIED` block for "per-change quality-gate and analysis subagents are available", carrying
      its description and all five scenarios verbatim with the pointer-technique `AND` bullet added to the "Lessons are
      captured after implementation" scenario

## 2. Docs

- [x] 2.1 Extend the `experience-analyzer` bullet in `AGENTS.md` with the on-demand rule, a one-line rationale, and a
      pointer to the spec — the full reasoning and the rejected alternatives live in the delta and the design, matching
      the `readme-auditor` bullet's shape rather than duplicating what the spec owns

- [x] 2.2 Fold the pointer technique into the growth-discipline clause in `AGENTS.md`, and state the change's net
      `AGENTS.md` growth (+6 lines) in the proposal's Impact

- [x] 2.3 Sweep `docs/ideas.md`: disambiguate the "unattended work is shiftable" entry, which read as if
      `/retrospective` were scheduled — it stays on-demand by design

## 3. Verify

- [x] 3.1 `./gradlew spotlessApply` and `spotlessCheck`
- [x] 3.2 `openspec validate --all` passes and the delta carries every scenario of both modified requirements (2 + 5)
- [x] 3.3 Confirm no workflow, tracker issue, or schedule is added — this is a documentation change only
