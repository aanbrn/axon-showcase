# showcase/quality/dependency-security Specification

## Purpose

Ensures the build does not ship known-vulnerable dependencies: the platform constrains vulnerable transitive
dependencies — Jackson 3 (`tools.jackson.core`), Apache HttpClient 5, `zstd-jni`, and `io.netty` — to patched versions
so the Snyk scan reports clean, and the web UI's npm dependencies are audited by `npmAudit`.

## Requirements

### Requirement: Vulnerable transitive dependencies are constrained to patched versions

The platform SHALL constrain the transitive dependencies that dependency scans flag as vulnerable to their patched
versions: `tools.jackson.core` modules SHALL resolve through the `tools.jackson:jackson-bom` at a version that fixes the
reported issues (`jackson-core` at least `3.1.4`, `jackson-databind` at least `3.1.5`),
`org.apache.httpcomponents.client5:httpclient5` SHALL resolve to at least `5.6.4`, `com.github.luben:zstd-jni` SHALL
resolve to at least `1.5.7-14`, and `io.netty` modules SHALL resolve through the `io.netty:netty-bom` at a version that
fixes the reported issue (at least `4.2.18.Final`).

#### Scenario: Jackson 3 modules resolve to the aligned BOM version

- **WHEN** a module that depends on `elasticsearch-java` resolves its runtime classpath
- **THEN** `tools.jackson.core:jackson-core` and `tools.jackson.core:jackson-databind` resolve to the
  `tools.jackson:jackson-bom` version, which is at least `3.1.4` for core and at least `3.1.5` for databind

#### Scenario: httpclient5 resolves to a patched version in every consuming module

- **WHEN** a module that depends on `opensearch-rest-client` resolves its runtime classpath
- **THEN** `org.apache.httpcomponents.client5:httpclient5` resolves to version `5.6.4` or newer

#### Scenario: zstd-jni resolves to a patched version wherever kafka-clients is present

- **WHEN** a module that depends on `kafka-clients` resolves its runtime classpath
- **THEN** `com.github.luben:zstd-jni` resolves to version `1.5.7-14` or newer

#### Scenario: Netty modules resolve to a patched version wherever reactor-netty is present

- **WHEN** a module that depends on `reactor-netty-http` resolves its runtime classpath
- **THEN** `io.netty:netty-codec-http` resolves to version `4.2.18.Final` or newer

#### Scenario: Dependency scan reports no vulnerable paths

- **WHEN** `snyk test --all-sub-projects` runs against the build
- **THEN** none of `showcase-projection-model`, `showcase-projection-service`, `showcase-query-client`, and
  `showcase-query-service` report a vulnerable path for Jackson 3 or `httpclient5`, no sub-project reports one for
  `zstd-jni` or `io.netty`, and the only suppressed findings are those pinned in `.snyk` with a stated reason and expiry

### Requirement: Local dependency security scan task

The build SHALL provide a `dependencySecurityCheck` Gradle task that runs the Snyk dependency scan
(`snyk test --all-sub-projects`) across all sub-projects and reports the result to the developer. The task SHALL NOT be
part of the `check` lifecycle.

#### Scenario: Developer runs the dependency security scan

- **WHEN** a developer runs `./gradlew dependencySecurityCheck` with the Snyk CLI installed
- **THEN** the task invokes `snyk test --all-sub-projects` against the build and reports the scan result, failing when
  vulnerable paths are found

#### Scenario: Normal build does not run the dependency scan

- **WHEN** a developer runs `./gradlew check` or any build task other than `dependencySecurityCheck`
- **THEN** the dependency scan does not run

#### Scenario: Snyk CLI is not installed

- **WHEN** a developer runs `./gradlew dependencySecurityCheck` without the Snyk CLI on `PATH`
- **THEN** the task fails with a clear message that the Snyk CLI is required

### Requirement: Web UI dependency vulnerability scan

The build SHALL provide an `npmAudit` task that scans the web UI's npm dependencies with `npm audit`, covering both
production and development dependencies, and failing when a high-severity (or greater) vulnerability is present. The
task SHALL use the project's pinned Node and SHALL NOT be part of the `check` lifecycle.

#### Scenario: Developer runs the web UI vulnerability scan

- **WHEN** a developer runs `./gradlew :showcase-web-ui:npmAudit` and a high-severity vulnerability is present
- **THEN** the task fails and reports the vulnerable npm package and its advisory

#### Scenario: A development-dependency advisory is reported

- **WHEN** a high-severity vulnerability is present in a development dependency
- **THEN** the task fails, because the scan covers development as well as production dependencies

#### Scenario: A clean web UI audit passes

- **WHEN** a developer runs `./gradlew :showcase-web-ui:npmAudit` and no high-severity vulnerability is present
- **THEN** the task completes successfully

#### Scenario: Normal build does not run the web UI vulnerability scan

- **WHEN** a developer runs `./gradlew check` or any build task other than `npmAudit`
- **THEN** the web UI vulnerability scan does not run
