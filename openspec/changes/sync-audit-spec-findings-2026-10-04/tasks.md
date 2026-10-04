# Tasks

## 1. Spec deltas

- [x] 1.1 Add the `Spring major updates are suppressed until the Spring Boot 4 migration` requirement (with both
      scenarios) to the change's `specs/showcase/quality/dependency-management/spec.md` delta, and verify
      `openspec validate --changes` reports it valid. _Done: `openspec validate --changes` → 1 passed._
- [x] 1.2 Correct _every_ bare `dependencySecurityCheck` invocation in the change's
      `specs/showcase/quality/dependency-security/spec.md` delta — the
      `Vulnerable transitive dependencies are constrained to patched versions` requirement's "Dependency scan reports no
      vulnerable paths" scenario and the `Local dependency security scan task` requirement and its scenario — so each
      names `snyk test --all-sub-projects --policy-path=.snyk`. Carry each main-spec requirement's full block (all
      scenarios, in order) as a `MODIFIED` block, and verify `openspec validate --changes` reports it valid. _Done: two
      `MODIFIED` blocks carrying full blocks; `openspec validate --all` → 26 passed (scenarios diff-verified in review
      R4)._
- [x] 1.3 Verify each delta's factual premise against the repository: confirm
      `config/dependency-updates/major-disabled.txt` ships `org.springframework` and that
      `build-logic/src/main/kotlin/dependency-security-conventions.gradle.kts` passes `--policy-path=.snyk`, so the
      corrected specs describe the build that exists. _Done: both greps returned their sites._

## 2. Docs reconciliation

- [x] 2.1 Derive the sweep from the grep, not the audit's scoping. Before archive:
      `grep -rn "snyk test --all-sub-projects" openspec/changes/sync-audit-spec-findings-2026-10-04/specs/` must return
      only occurrences carrying `--policy-path=.snyk` (the three in the security delta), confirming no bare invocation
      rides into the main spec. After archive: the same grep over `openspec/specs/` must return only occurrences
      carrying the flag (the three now in `dependency-security/spec.md` and `merge-governance/spec.md:223`). Then
      confirm the two resolved findings are the ones recorded in `docs/audits/2026-10-04.md` (spec-corpus audit findings
      1 and 2). Note: `docs/adr/0006` and `docs/adr/0014` carry the bare invocation, but ADR-0006 predates the flag
      (dated 2026-08-21; `--policy-path=.snyk` landed in #9 on 2026-08-28), so they are historical decision records
      rather than live wrong claims — out of scope for this spec-only change. _Done (pre-archive half): the delta grep
      returned exactly the three flag-carrying sites; the after-archive half runs with task 4.1's archive step._

## 3. Verification

- [x] 3.1 Run `./gradlew spotlessApply` then `./gradlew spotlessCheck` and `openspec validate --all`, and confirm all
      pass. _Done: `spotlessCheck` green; `openspec validate --all` → 26 passed, 0 failed._
- [x] 3.2 Run the `lesson-capture` subagent over this change's diff and review findings; apply its durable `AGENTS.md`
      proposals (or record its "nothing durable" verdict), and record the applied net `AGENTS.md` delta on this task.
      _Done: subagent returned **nothing durable** — all three review incidents (unformatted-artifact gate claim,
      audit-scoped sweep vs pattern, Prettier-corrupted inline-code spans) are already covered by existing rules, none
      needing rewording. Applied net `AGENTS.md` delta: **0 lines**._

## 4. Delivery

- [ ] 4.1 Push the branch, open the PR with the change dir, confirm the `build` check is green, then archive the change
      (`openspec archive sync-audit-spec-findings-2026-10-04`) as an additional commit in the same PR and merge once the
      user approves.

_Note: `design.md` is deliberately omitted — this is a spec-only change (no cross-cutting concern, dependency,
data-model, security, or ambiguity), so the schema's conditional design artifact does not apply._
