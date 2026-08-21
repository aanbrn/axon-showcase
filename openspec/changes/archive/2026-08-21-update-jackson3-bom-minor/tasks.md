## 1. Bump the Jackson 3 BOM

- [x] 1.1 Bump `jackson3-bom` from `3.1.6` to `3.2.2` in `gradle/libs.versions.toml` and verify the query-service and
      projection-service runtime classpaths resolve `tools.jackson.core:jackson-databind`/`jackson-core` to `3.2.2`
- [x] 1.2 Confirm the `dependencyUpdates` report no longer lists `tools.jackson:jackson-bom [3.1.6 -> 3.2.2]`

## 2. Amend ADR-0003

- [x] 2.1 Amend `docs/adr/0003-retain-jackson-2-defer-jackson-3.md` with a short note stating Jackson 3 is present
      transitively via `elasticsearch-java` on the query and projection service classpaths and that the `jackson3-bom`
      is kept current on minor versions, while backend adoption remains deferred; verify the ADR remains consistent

## 3. Verify the change artifacts

- [x] 3.1 Run `openspec validate` on the change and confirm all planning artifacts are complete with no errors