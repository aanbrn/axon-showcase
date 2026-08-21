## Purpose

Ensures the build does not ship known-vulnerable transitive dependencies: the platform constrains vulnerable
transitives — Jackson 3 (`tools.jackson.core`) and Apache HttpClient 5 — to patched versions so dependency scans report
clean.

## ADDED Requirements

### Requirement: Vulnerable transitive dependencies are constrained to patched versions

The platform SHALL constrain the transitive dependencies that dependency scans flag as vulnerable to their patched
versions: `tools.jackson.core` modules SHALL resolve through the `tools.jackson:jackson-bom` at a version that fixes
the reported issues (`jackson-core` at least `3.1.4`, `jackson-databind` at least `3.1.5`), and
`org.apache.httpcomponents.client5:httpclient5` SHALL resolve to at least `5.6.3`.

#### Scenario: Jackson 3 modules resolve to the aligned BOM version

- **WHEN** a module that depends on `elasticsearch-java` resolves its runtime classpath
- **THEN** `tools.jackson.core:jackson-core` and `tools.jackson.core:jackson-databind` resolve to the
  `tools.jackson:jackson-bom` version, which is at least `3.1.4` for core and at least `3.1.5` for databind

#### Scenario: httpclient5 resolves to a patched version in every consuming module

- **WHEN** a module that depends on `opensearch-rest-client` resolves its runtime classpath
- **THEN** `org.apache.httpcomponents.client5:httpclient5` resolves to version `5.6.3` or newer

#### Scenario: Dependency scan reports no vulnerable paths

- **WHEN** `snyk test --all-sub-projects` runs against the build
- **THEN** none of `showcase-projection-model`, `showcase-projection-service`, `showcase-query-client`, and
  `showcase-query-service` report a vulnerable path for Jackson 3 or `httpclient5`