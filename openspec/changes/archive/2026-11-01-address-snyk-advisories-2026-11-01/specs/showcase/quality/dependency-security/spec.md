## MODIFIED Requirements

### Requirement: Vulnerable transitive dependencies are constrained to patched versions

The platform SHALL constrain the transitive dependencies that dependency scans flag as vulnerable to their patched
versions: `tools.jackson.core` modules SHALL resolve through the `tools.jackson:jackson-bom` at a version that fixes the
reported issues (at least `3.2.3`), `com.fasterxml.jackson.core` modules (Jackson 2) SHALL resolve through the
`com.fasterxml.jackson:jackson-bom` at a version that fixes the reported issues (at least `2.22.3`),
`org.apache.httpcomponents.client5:httpclient5` SHALL resolve to at least `5.6.4`, `com.github.luben:zstd-jni` SHALL
resolve to at least `1.5.7-14`, `org.xerial.snappy:snappy-java` SHALL resolve to at least `1.1.10.10`,
`com.aayushatharva.brotli4j` modules SHALL resolve to at least `1.24.0`, `org.objenesis:objenesis` SHALL resolve to at
least `3.6`, `com.google.guava:guava` SHALL resolve to the platform's constrained version (at least `33.7.2-jre`) on the
tool classpaths a scan reads (`annotationProcessor` for the Error Prone tool, and `checkstyle`), `org.ow2.asm:asm` SHALL
resolve to at least `9.11.1` on every classpath the platform reaches and on the `spotbugs` tool classpath, and
`io.netty` modules SHALL resolve through the `io.netty:netty-bom` at a version that fixes the reported issue (at least
`4.2.18.Final`). The JaCoCo tool version SHALL be at least `0.8.16` on every project that applies the plugin — the root
project included — so its own ASM dependency is patched.

#### Scenario: Jackson 3 modules resolve to the aligned BOM version

- **WHEN** a module that depends on `elasticsearch-java` resolves its runtime classpath
- **THEN** `tools.jackson.core:jackson-core` and `tools.jackson.core:jackson-databind` resolve to the
  `tools.jackson:jackson-bom` version, which is at least `3.2.3`

#### Scenario: Jackson 2 modules resolve to the patched BOM version

- **WHEN** a module that depends on `jackson-databind` resolves its runtime classpath
- **THEN** `com.fasterxml.jackson.core:jackson-core` and `com.fasterxml.jackson.core:jackson-databind` resolve to the
  `com.fasterxml.jackson:jackson-bom` version, which is at least `2.22.3`

#### Scenario: httpclient5 resolves to a patched version in every consuming module

- **WHEN** a module that depends on `opensearch-rest-client` resolves its runtime classpath
- **THEN** `org.apache.httpcomponents.client5:httpclient5` resolves to version `5.6.4` or newer

#### Scenario: zstd-jni resolves to a patched version wherever kafka-clients is present

- **WHEN** a module that depends on `kafka-clients` resolves its runtime classpath
- **THEN** `com.github.luben:zstd-jni` resolves to version `1.5.7-14` or newer

#### Scenario: snappy-java resolves to a patched version wherever kafka-clients is present

- **WHEN** a module that depends on `kafka-clients` resolves its runtime classpath
- **THEN** `org.xerial.snappy:snappy-java` resolves to version `1.1.10.10` or newer

#### Scenario: Guava resolves to the constrained version on the tool classpaths

- **WHEN** a module applying the code-check conventions resolves its `checkstyle` and `annotationProcessor`
  configurations
- **THEN** `com.google.guava:guava` resolves to version `33.7.2-jre` or newer on each

#### Scenario: Netty modules resolve to a patched version wherever reactor-netty is present

- **WHEN** a module that depends on `reactor-netty-http` resolves its runtime classpath
- **THEN** `io.netty:netty-codec-http` resolves to version `4.2.18.Final` or newer

#### Scenario: ASM resolves to a patched version on every classpath that carries it

- **WHEN** the root project and a module applying the code-coverage conventions resolve their `jacocoAnt` configuration,
  a module resolves a test classpath, or a module applying the code-check conventions resolves its `spotbugs`
  configuration
- **THEN** `org.ow2.asm:asm` resolves to version `9.11.1` or newer on each, and the JaCoCo tool version is at least
  `0.8.16`

#### Scenario: brotli4j resolves to a patched version wherever Gatling's HTTP client is present

- **WHEN** a module that applies the load-testing conventions resolves its `gatlingRuntimeClasspath`
- **THEN** every `com.aayushatharva.brotli4j` artifact resolves to version `1.24.0` or newer

#### Scenario: objenesis resolves to a patched version wherever Mockito is present

- **WHEN** a module that depends on `mockito-core` resolves a test classpath
- **THEN** `org.objenesis:objenesis` resolves to version `3.6` or newer

#### Scenario: Dependency scan reports no vulnerable paths

- **WHEN** `snyk test --all-sub-projects --policy-path=.snyk` runs against the build
- **THEN** none of `showcase-projection-model`, `showcase-projection-service`, `showcase-query-client`, and
  `showcase-query-service` report a vulnerable path for Jackson 3 or `httpclient5`, no sub-project reports one for
  Jackson 2, `zstd-jni`, `snappy-java`, `brotli4j`, `objenesis`, `guava`, `org.ow2.asm:asm`, or `io.netty`, and the only
  suppressed findings are those pinned in `.snyk` with a stated reason and expiry
