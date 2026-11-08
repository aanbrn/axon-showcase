# Tasks

Unit record: `docs/units/address-kafka-client-advisories/unit.md`.

## 1. Dependency constraint

- [x] 1.1 Add the `kafka-clients = "4.4.0"` version entry and the `org.apache.kafka:kafka-clients` library entry to
      `gradle/libs.versions.toml`; verify the catalog parses with `./gradlew help`.
- [x] 1.2 Add `api(libs.kafka.clients)` to the `constraints` block in `platform/build.gradle.kts`; verify with
      `./gradlew :showcase-command-service:dependencyInsight --dependency kafka-clients --configuration runtimeClasspath`
      that it reports `org.apache.kafka:kafka-clients:4.4.0` selected "By constraint".
- [x] 1.3 Compile the build with `./gradlew compileJava`; verify it succeeds.

## 2. Verification

- [x] 2.1 Run the three services' unit and component suites:

  ```bash
  ./gradlew :showcase-command-service:test \
    :showcase-command-service:componentTest \
    :showcase-projection-service:test \
    :showcase-projection-service:componentTest \
    :showcase-api-gateway:test \
    :showcase-api-gateway:componentTest
  ```

  Verify it succeeds.

- [x] 2.2 Run the three services' integration suites with Docker available:

  ```bash
  ./gradlew :showcase-command-service:integrationTest \
    :showcase-projection-service:integrationTest \
    :showcase-api-gateway:integrationTest
  ```

  Verify every suite passes and that the containers booted `apache/kafka:3.9.0` (the deployed broker line).

- [x] 2.3 Run `./gradlew :showcase-command-service:check -PskipITs -Pcoverage.gate.enabled=false` and
      `./gradlew spotlessCheck`; verify both succeed.
- [x] 2.4 Run `openspec validate address-kafka-client-advisories --type change` and `openspec validate --all`; verify
      both pass.
- [x] 2.5 Compile the resolution tiers the Docker-free check drops, so the dependency change's resolution claim covers
      them: `./gradlew compileIntegrationTestJava compileE2eTestJava --refresh-dependencies`; verify it succeeds. The
      gateway's `e2eTest` suite resolves `kafka-clients`.
- [x] 2.6 Run the gateway e2e suite against freshly built images: `./gradlew :showcase-api-gateway:e2eTest`; verify it
      passes. This is the only suite that exercises the command-service's Kafka publisher — its integration suites set
      `axon.kafka.publisher.enabled=false` — so the full command → Kafka → projection → query path runs here.
- [ ] 2.7 Read the Snyk scan result for the change: dispatch `Dependency Security` against the pushed branch
      (`gh workflow run dependency-security.yml --ref address-kafka-client-advisories`) and verify its `snyk` job passes
      with no `kafka-clients` finding; if the Snyk plan's monthly quota blocks the run, record that in the change report
      and treat the next scheduled run as the confirmation. Until that run is green, the advisories' clearance is
      unconfirmed.

## 3. Documentation and records

- [x] 3.1 Write `docs/adr/0031-*.md` recording the decision to run `kafka-clients` 4.4.0 ahead of the Spring Kafka 3.3 /
      Boot 3.5 line, the verification (the three integration suites and the gateway e2e suite against
      `apache/kafka:3.9.0`), and a `Revisit when:` gate for the supersession condition; verify it follows the
      `docs/adr/README.md` template.
- [x] 3.2 Park the broker-side exposure in `docs/ideas.md` under today's dated section with its status tag: the deployed
      `apache/kafka:3.9.0` image runs a release inside both advisories' affected ranges, so the broker-side code paths
      they describe are present while the deployment's exposure is not assessed here; the newest Bitnami `kafka` chart
      (`32.4.3`) packages Kafka `4.0.0`, still inside the ranges. State the recheck condition as a maintained chart or
      image tagging Kafka `4.2.2` or newer. Verify by grepping `docs/ideas.md` for the entry.
- [x] 3.3 Sweep the live docs that could name the coordinate or the old version — `AGENTS.md`, `README.md`, `docs/`,
      `openspec/config.yaml`, and the subject packs — for `kafka-clients` and `3.9.2`; verify every hit is a historical
      or unrelated statement, or is corrected, and record the sweep's result in the change report.
- [ ] 3.4 Refresh the `showcase/quality/dependency-security` capability `## Purpose` to name `kafka-clients` among the
      constrained coordinates in the archive commit, since a delta cannot carry a Purpose; verify the archived spec's
      Purpose names it.

## 4. Process records and review

- [x] 4.1 Run the review gate's pre-write derivation pass and record the claims table in `design.md`; verify
      `python3 scripts/claim-scan.py` candidates are each a row or a recorded dismissal.
- [x] 4.2 Write `docs/units/address-kafka-client-advisories/process-defects.md` as defects are observed, or state that
      none was observed.
- [x] 4.3 Run the proposal quick review through `review-quick` and fix every finding; verify the closing round reports
      no new observations.
- [x] 4.4 Run the implementation quick review through `review-quick` and fix every finding; verify the closing round
      reports no new observations.
- [x] 4.5 Derive the deep-review floor with `python3 scripts/review-routing.py` and record the outcome and its signals
      in `docs/units/address-kafka-client-advisories/deep-review.md`; run `/review-thorough` when the outcome is
      `REQUIRED`, or on the owner's decision when it is `RECOMMENDED`.
- [x] 4.6 Ask the owner for the manual review pass of the proposal, then of the implementation, and end the turn at each
      stop; verify the approvals are recorded before any delivery step.
- [x] 4.7 Add the `record-shape` class to `.opencode/agent/review-quick.md`'s canonical list — round 2 raised a finding
      that fitted no existing class — and verify `python3 scripts/commit-hygiene.py --class-vocabulary` accepts the
      unit's ledger rows.

## Workflow follow-up

- Commit and push only on the owner's delivery request, then let the dispatch in task 2.7 run against the branch.
- Archive the change with `openspec archive address-kafka-client-advisories --yes` after the `build` check is green, and
  stage both `openspec/changes` and `openspec/specs`.
