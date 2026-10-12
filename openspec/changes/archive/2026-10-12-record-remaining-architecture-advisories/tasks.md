# Tasks

## 1. ADR-0020 — the LZ4 substitution

- [x] 1.1 Write `docs/adr/0020-substitute-maintained-lz4-java.md` (Nygard shape): `Date: 2025-12-08`,
      `Status: Accepted`, with a Context line stating it was recorded retrospectively on 2026-10-12; the unmaintained
      `org.lz4:lz4-java` (last real release `1.8.0`; `1.8.1` is a relocation POM to the fork) and CVE-2025-12183
      (GHSA-vqf4-7m7x-wgfc, an out-of-bounds memory access); the decision to substitute `at.yawk.lz4:lz4-java` repo-wide
      via the build's resolution strategy (a drop-in — same `net.jpountz.*` API); and the Consequences (no per-consumer
      exclude, `.snyk` needs no ignore). Verify the CVE, the versions, and the drop-in claim against the fork's README.

## 2. ADR-0012 — the Elasticsearch client's role

- [x] 2.1 Add the read side's `co.elastic.clients:elasticsearch-java` coordinate to ADR-0012: declared in three modules,
      imported by no project source, and load-bearing at runtime — it is not supplied transitively because
      `spring-data-opensearch`'s POM excludes `co.elastic.clients:*` (and `org.elasticsearch.client:*`) from its
      `spring-data-elasticsearch` dependency, while Spring Data Elasticsearch's `client.elc` classes reference Elastic's
      client and Spring Boot's `ElasticsearchDataAutoConfiguration` conditions on `client.elc.ElasticsearchTemplate`. A
      probe removing all three declarations failed every read-side suite at context startup with
      `NoClassDefFoundError: co/elastic/clients/ApiClient`. Record its version choice too: the catalog pins `9.5.4`
      above Spring Boot's managed `8.18.8`.

## 3. ADR-0013 — the shared parenthetical

- [x] 3.1 Refresh ADR-0013's `shared` list to the layer's current slices (`api`, `format`, `retry`, `tracing`,
      `telemetry`). Verify against `showcase-web-ui/src/shared/`.

## 4. AGENTS.md and docs/ideas.md

- [x] 4.1 Point the LZ4 relocation line in `AGENTS.md` (`LZ4 relocation: root build forces …`) at ADR-0020 for the
      rationale.
- [x] 4.2 Remove the two implemented parked entries (`Record the LZ4 relocation's rationale` and
      `Record the Elasticsearch Java client's role on the read side`); the latter is the sole entry of the
      `## 2026-10-03` section, so remove that emptied heading too. Verify the file's section structure and that no other
      entry references them.

## 5. Verification

- [x] 5.1 Run `./gradlew spotlessApply` then `./gradlew spotlessCheck` and `openspec validate --all`; run the manual
      120-character check over the changed non-formatter files. Confirm the probe left no build-file change
      (`git status`).
- [x] 5.2 Run a `review-quick` pass over the proposal and the implementation, repeating until clean.

## 6. Capture

- [x] 6.1 Run the `lesson-capture` subagent over the diff, apply its durable proposals, and record the applied net
      `AGENTS.md` delta on this task. Applied: **net ~+5 rendered lines** (the raw churn is larger because the
      premise-interrogation paragraph reflows) — (a) a clause on "A review finding is a claim to verify": a correction
      that _deletes_ a statement has no downstream reader, so reproduce it at the source the statement names first; (b)
      a clause on the premise-interrogation bullet: a dependency-mechanism claim is derived from the resolved graph or
      the artifact, never hand-enumerated. It also tightened the LZ4 line to a pure pointer at ADR-0020. No retirements
      proposed.
- [x] 6.2 Ask the owner for the manual review pass, after 5.2 is clean and 6.1 is done.

## Workflow follow-up

- Archive the change after the owner approves; its archive commit rides this PR.
