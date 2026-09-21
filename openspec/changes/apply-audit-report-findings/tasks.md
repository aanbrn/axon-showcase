# Tasks — apply the audit reports' findings

## 1. The Axon suppression rationale (both reports' F1)

- [x] 1.1 Add `docs/adr/0011-defer-axon-framework-5-migration.md`, recording the deferral, the verified blockers (the
      Kafka and JGroups extensions and the Micrometer and OpenTelemetry modules have no 5.x release; the Spring Boot
      starter is a preview), the reopen trigger, and a Related-decisions section naming ADR-0009 as a sibling under the
      shared Axon-without-Axon-Server intention.
- [x] 1.2 Add the `ADDED` requirement to the `dependency-management` delta, so the dead pointer resolves.
- [x] 1.3 Repoint the `org.axonframework` comment in `config/dependency-updates/major-disabled.properties` at ADR-0011.

## 2. ADR-0009's exclusion scope (both reports' advisory)

- [x] 2.1 Amend ADR-0009's Context and Decision to state the `axon-server-connector` exclusion **repo-wide** — every
      module depending on `axon-spring-boot-starter` (six build files, verified), because the project runs no Axon
      Server anywhere.

## 3. The four one-line findings

- [x] 3.1 `AGENTS.md` — drop the dead `codefmt`-skill reference from the Formatting convention; mark the gotcha's
      `codefmt` example as a since-removed skill.
- [x] 3.2 `.opencode/commands/dependency-security-check.md` — replace the `dependency-security` spec pointer with
      `.snyk`'s header and each ignore's `reason` (the spec holds a different concern).
- [x] 3.3 `.opencode/commands/audit-agents.md` — step 4 lists only the files the audit edits.
- [x] 3.4 `docs/adr/0005-testable-exit-on-startup.md` — Consequences states the command service adopted the seam.

## 4. The ideas this resolves

- [x] 4.1 Remove the two resolved entries from `docs/ideas.md` (the Axon pin rationale; ADR-0009's exclusion scope),
      keeping the three still-open ones (Lombok-over-records, the OpenSearch high-level-client exclusion, the
      `FutureReturnValueIgnored` suppression).

## 5. Verification

- [x] 5.1 Each finding's claim verified before the fix (the skill absent; the spec mentioning Axon/Spring zero times;
      the six excluding build files; the seam present in the command service).
- [x] 5.2 The Axon-5 blockers verified against Maven Central metadata rather than memory — which corrected the
      recollection: `axon-reactor` **has** migrated, so it is not a blocker.
- [x] 5.3 `./gradlew spotlessCheck` green; no line over 120 in the touched non-formatter files.
- [x] 5.4 `openspec validate --all` 23/23 with the delta accepted.
