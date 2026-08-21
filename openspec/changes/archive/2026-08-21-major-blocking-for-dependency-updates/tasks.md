## 1. Create the opt-in config file

- [x] 1.1 Create `config/dependency-updates/major-disabled.properties` with a comment explaining the format
      (`group:module` exact or group prefix, value ignored) and the initial entries `org.axonframework` and
      `org.springframework`
- [x] 1.2 Verify the file loads as the expected properties set (both groups present)

## 2. Extend the dependency-versions convention

- [x] 2.1 In `build-logic/src/main/kotlin/dependency-versions-conventions.gradle.kts`, load
      `config/dependency-updates/major-disabled.properties` from the root project into a set of coordinates, tolerating
      a missing file (empty set)
- [x] 2.2 Add a `majorOf(version)` helper that parses the leading numeric segment, handling `-jre`, `.Final`,
      `.RELEASE`, and calendar-style versions; non-numeric versions return a sentinel that never triggers rejection
- [x] 2.3 Add an ownership filter: read `gradle/libs.versions.toml`, collect the `group:module` coordinates of
      `[libraries]` entries that carry an exact `version.ref`, and extend `rejectVersionIf` so a candidate whose
      coordinate is not in that set is rejected
- [x] 2.4 Extend the existing `rejectVersionIf` predicate so a candidate is rejected when its coordinate matches a
      major-disabled entry (exact `group:module` or group prefix, dot-boundary aware) AND
      `majorOf(candidate) > majorOf(currentVersion)`

## 3. Verify default behavior

- [x] 3.1 Run `./gradlew dependencyUpdates` and confirm the report contains only catalog-owned coordinates (e.g.
      `spring-tx`, `spring-boot-starter-*`, `micrometer-registry-*` are gone) while non-axon/spring catalog-owned
      majors are still listed (e.g. `springdoc 2.8.17 -> 3.1.0`)

## 4. Verify the blocking behavior

- [x] 4.1 Confirm the `org.axonframework` and `org.springframework` groups' major jumps (e.g. `axon-messaging
      4.13.2 -> 5.3.1`, `spring-boot-dependencies 3.5.16 -> 4.1.1`, `spring-framework-bom 6.2.18 -> 7.0.9`) are no
      longer listed
- [x] 4.2 Confirm a same-major update for a listed coordinate is still reported when available (e.g. guava stays
      visible; a hypothetical `axon-bom 4.13.3 -> 4.13.4` or `spring-boot-dependencies 3.5.16 -> 3.5.17` would appear)
- [x] 4.3 Confirm the group-prefix matching is dot-boundary aware: `org.axonframework` covers
      `org.axonframework.extensions.kafka` while `org.springframework` does not swallow an unrelated group like
      `org.springframework.security` separately (it should — it is part of the SB4 train)

## 5. Document the mechanism

- [x] 5.1 In `AGENTS.md`, add `./gradlew dependencyUpdates` to the Build & Test commands, noting the catalog-ownership
      scope and the opt-in major-disabled config at `config/dependency-updates/major-disabled.properties`
- [x] 5.2 In `README.md`, add a short "Dependency Updates" note next to the Dependency Security section explaining how
      to run the report and how to suppress a coordinate's major updates
- [x] 5.3 Update the `Impact`/docs references in the change artifacts if any other docs (e.g. `docs/adr/`) reference
      dependency update behavior

## 6. Run the affected checks

- [x] 6.1 Run the root `dependencyUpdates` once more and confirm no errors and a clean report with only the expected
      catalog-owned, non-blocked updates listed