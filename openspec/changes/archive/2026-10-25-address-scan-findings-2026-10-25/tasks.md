# Tasks

## 1. Implementation

- [x] 1.1 `showcase-projection-service/build.gradle.kts` — remove the `componentTest` suite's redundant
      `implementation(libs.spring.data.opensearch)`; the dependency arrives through
      `project(":showcase-projection-model")`'s `api` edge, which carries the `opensearch-rest-high-level-client`
      exclusion. The change first added a second exclusion there; the consultation showed the declaration itself was
      redundant, and the removal leaves the resolved classpath unchanged.
- [x] 1.2 `showcase-web-ui/npm-audit-ignores.json` — remove the withdrawn `GHSA-vfj7-8cjw-p6xm` entry, leaving
      `{"suppressions": []}`.
- [x] 1.3 `docs/adr/0014-web-ui-npm-dependency-checks.md` — replace the `braces` retirement-condition consequence with
      the withdrawal (2026-10-23) and the entry's removal (2026-10-25).
- [x] 1.4 Confirm no spec delta is owed (`skip_specs: true`): grep `openspec/specs/` for `t-digest`,
      `npm-audit-ignores.json`, and the projection service's resolution behavior; both fixes restore outcomes the
      `showcase/quality/dependency-security` spec already states. **Done**: the spec names the suppression list's path
      and the scan-clean outcome, neither of which the change alters; no requirement or scenario states the dependency
      wiring or the duplicate declaration.

## 2. Verification

- [x] 2.1
      `./gradlew :showcase-projection-service:dependencyInsight --configuration componentTestCompileClasspath --dependency t-digest`
      reports no match, in both the compile and runtime classpaths. **Done**: "No dependencies matching given input were
      found" for `componentTestCompileClasspath`, `componentTestRuntimeClasspath`, and `runtimeClasspath`.
- [x] 2.2 `./gradlew :showcase-projection-service:componentTest --rerun` runs green (the removal left the resolved
      classpath unchanged, so a plain run reports `UP-TO-DATE`).
- [x] 2.3 `./gradlew :showcase-projection-service:check -PskipITs -Pcoverage.gate.enabled=false`, then
      `./gradlew compileIntegrationTestJava compileE2eTestJava --refresh-dependencies` (a resolution-affecting change
      compiles the tiers the Docker-free check drops). **Done**: both green.
- [x] 2.4 `./gradlew :build-logic:test` (the `NpmAuditRules` cases the empty list must still satisfy) and
      `./gradlew :showcase-web-ui:npmAudit` pass. **Done**: `npmAudit` reports "clean (0 suppression(s) applied)".
- [x] 2.5 `./gradlew dependencySecurityCheck` (credentialed) reports no vulnerable path — the scan that found the
      findings must stop reporting them. **Done**: "Tested 20 projects, no vulnerable paths were found.", re-run after
      the declaration was removed rather than excluded.
- [x] 2.6 `./gradlew spotlessApply` / `spotlessCheck`, `openspec validate --all`, and the change's markdown in the same
      pass. **Done**: `openspec validate --all` 27 passed, 0 failed (its one long-requirement warning predates this
      change).

## 3. Documentation and capture

- [x] 3.1 Sweep the live documents for the entry and the coordinate this change moves: `docs/ideas.md`, the
      `dependency-security` spec, `AGENTS.md`'s `.snyk` guidance,
      `docs/guidance-evidence/documentation-and-durable-claims.md`, and ADR-0012's exclusion consequences for any claim
      the removal falsifies. **Done**: no live document names the withdrawn advisory except ADR-0014 (updated) and that
      evidence record, whose closure sentence the change rewrote; `docs/ideas.md` and `AGENTS.md` carry no entry for it,
      the spec describes the mechanism without naming the advisory, and ADR-0012's consequences now state the
      inherited-edge case this fix relies on.
- [x] 3.2 Run the `lesson-capture` subagent over the diff and review rows, and apply its durable proposals (or record
      the skip and its reason) — the 2026-10-16 removal's consumer sweep is the likely lesson. **Done**: three proposals
      applied — the `.snyk` removal's consumer-set claim and the credentialed scan in the same change (`gradle-build`'s
      Snyk block), the re-declared edge's exclusions (`gradle-build`'s custom-suite bullet), and
      `docs/guidance-evidence/` as a docs-refresh surface (`AGENTS.md`). The instances the rounds recorded as misses of
      existing rules added no rule — which they are is `docs/review-rounds.tsv`'s record — and no rule was retired. Net
      delta: +3 lines in `AGENTS.md` (its bullet reflowed), +8 in `.opencode/skills/gradle-build/SKILL.md`.

## 4. Review

- [x] 4.1 Pre-write derivation pass and claims table: `python3 scripts/claim-scan.py` seeded the candidates, each
      resolved into `design.md`'s Claims table or a dismissal beside it.
- [x] 4.2 Quick-review loop: fix every round's findings and re-run until a round reports no new observation; each
      round's classes and state are `docs/review-rounds.tsv`'s appended rows, not restated here.
- [x] 4.3 Deep-review routing: derive the floor with
      `python3 scripts/review-routing.py --change address-scan-findings-2026-10-25`, record its outcome here, and run
      `/review-thorough` when the outcome is `REQUIRED` (or when a `RECOMMENDED` outcome is resolved to run). **Done**:
      the first derivation returned `NOT_NEEDED`; after the ledger gained this unit's repeat rows it re-derived
      `RECOMMENDED` on the `[repeated-class]` signal, which the caller resolved to run. Pass 1
      (`ses_e8f9c6c14ffe7pGuSuM4Q3fan5`) returned two should-fix findings: (1)
      `docs/adr/0012-prefer-typed-opensearch-java-client.md`'s parenthetical names only a module's main and
      `integrationTest` edges, while `showcase-query-client` carries the exclusion on its `componentTest` edge alone —
      status `FIXED_AND_VERIFIED`: applied on the owner's approval (the sentence now reads "every edge that pulls the
      starter"), and pass 2 verified it against the seven exclusion-carrying declarations; (2) this task's own stale
      `NOT_NEEDED` note — superseded by this pass record, status `FIXED_AND_VERIFIED`. Pass 2
      (`ses_e8f9c6c14ffe7pGuSuM4Q3fan5`) found no new issue, and the default budget of two full passes is used.
- [x] 4.4 Ask the owner for the manual review pass; commit, push, archive, and merge only after approval. **Done**: the
      owner approved (2026-10-25) and the caller's `review-handover.md` was removed before the commit, so the change dir
      ships with the artifacts other archived changes carry.
