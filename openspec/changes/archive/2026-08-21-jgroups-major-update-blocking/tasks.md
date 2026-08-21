## 1. Revert the ad hoc entry

- [x] 1.1 Remove the uncommitted `org.jgroups` line from `config/dependency-updates/major-disabled.properties` so the
      file matches `HEAD`; verify with `git diff -- config/dependency-updates/major-disabled.properties` that no
      changes remain

## 2. Re-add the entry with rationale

- [x] 2.1 In `config/dependency-updates/major-disabled.properties`, add `org.jgroups` with a one-line comment pointing
      at the dependency-management spec rationale (e.g. `# JGroups major blocked: ecosystem locked to 4.x — see
      showcase/quality/dependency-management`); verify the file ends with a newline after the Spring entry

## 3. Verify the blocking behavior

- [x] 3.1 Run `./gradlew dependencyUpdates` and confirm no `org.jgroups:jgroups` or `org.jgroups.kubernetes:
      jgroups-kubernetes` 5.x major jump is listed
- [x] 3.2 Confirm 4.x (same-major) updates for the JGroups coordinates are still reported when available, and other
      non-blocked catalog-owned majors remain visible (e.g. `springdoc 2.x -> 3.x`)