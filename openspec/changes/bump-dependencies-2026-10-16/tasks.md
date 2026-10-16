# Tasks

## 1. The bumps

- [x] 1.1 Bump the three catalog keys in `gradle/libs.versions.toml` to the versions the re-resolved report names:
      `swagger-ui` `5.33.1`→`5.33.2`, `caffeine` `3.3.0`→`3.3.1`, `grpc` `1.84.2`→`1.85.0`.
- [x] 1.2 Run the web UI's npm update (`./gradlew :showcase-web-ui:npm_update`) and describe the lockfile diff by what
      moved: 50 packages, the twelve direct rows among them, with one cross-major transitive (`@asamuzakjp/dom-selector`
      `9.2.4`→`10.0.3`, a `jsdom` dependency). `package.json` is unchanged.
- [x] 1.3 Confirm no formatter reflow is owed: neither the Spotless plugin nor the npm `prettier` is in the set.

## 2. Verification

- [x] 2.1 Run the full `./gradlew check` (integration tests on Testcontainers, the coverage gate enabled) — the tier
      `caffeine` and `grpc` need. **Result: green**, with the integration tier executed for the command service, the
      gateway and the query service (the projection service's was `UP-TO-DATE`). `verifyInfraImageVersions` — the parked
      cold-run helm-repository flake — did not bite on this warm run.
- [x] 2.2 Run the frontend `check`, the bundle `build`, and `npmAudit`: `:showcase-web-ui:check` green,
      `:showcase-web-ui:build` green (the bundle emits with the moved `react`/`react-dom`), and
      `:showcase-web-ui:npmAudit` green — which it would not be had the update left an `npm-audit-ignores.json` entry
      matching no finding.
- [x] 2.3 Re-run the reports and confirm the rows are gone: `dependencyUpdates`' update section is empty and
      `:showcase-web-ui:npmOutdated` lists no rows.
- [x] 2.4 Sweep the documents for a bumped version: no durable doc names one as a current value. The two mentions found
      are records — `AGENTS.md`'s "(verified against Caffeine `3.3.0`)" and `package.json`'s caret ranges — and stay.
- [x] 2.5 Sweep the suppressions: `.snyk`'s seven coordinates are all Spring and none moves with this bump; the npm side
      is covered by 2.2's `npmAudit` pass.
- [x] 2.6 Run a `review-quick` pass over the proposal and the implementation, repeating until clean: three rounds (4, 2,
      clean). `incomplete-enumeration` repeated across both finding rounds because the three keys' _reach_ was written
      per site from recall; the remedy was one list derived from the modules' resolved classpaths, which the Context
      states and the proposal and the claims row point at.

## 3. Capture and the manual pass

- [x] 3.1 Record the pre-write derivation pass as a task — every unit owes it, and deriving this bump's own task set
      from its analogous ancestors lost it, since those predate the rule. It ran: the claims table is in `design.md`,
      and its counts and the three keys' reach were re-derived from the resolved graph.
- [x] 3.2 Run the `lesson-capture` subagent over the diff, apply its durable proposals, and record the applied net
      `AGENTS.md` delta here: **+1 line.** One proposal passed the gate and merged into the "A recurring unit type"
      bullet: a recurring unit derives its change dir from the last analogous archived change as a _floor_, not the
      whole set, unioned with the tasks the rules mandate now — because the bump ancestors predate the pre-write-pass
      rule, so deriving this unit's set from them dropped the pass's record (its own finding, and the reason 3.1 above
      exists). It confirmed the reach-from-recall candidate as a compliance miss against three existing rules, and
      routed nothing else. No retirements.
- [ ] 3.3 Ask the owner for the manual review pass, after 2.6 is clean and 3.2 is done.

## Workflow follow-up

- Archive the change after the owner approves; its archive commit rides this PR.
- The Helm, buildpack, and tooling findings are separate units, as before.
