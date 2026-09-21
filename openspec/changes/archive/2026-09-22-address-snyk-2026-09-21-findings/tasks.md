# Tasks

## 1. Bump netty

- [x] 1.1 Bump the `netty-bom` version in `gradle/libs.versions.toml` from `4.2.17.Final` to `4.2.18.Final`
- [x] 1.2 Confirm the resolved version: a gateway/command-service runtime classpath shows `netty-codec-http` at
      `4.2.18.Final`, not `4.2.17.Final`

## 2. Suppress the unfixable advisories

- [x] 2.1 Add the three `snappy-java` ignores to `.snyk`, each pinned to `'* > org.xerial.snappy:snappy-java@1.1.10.5'`,
      with a reason naming the production path (`axon-kafka` → `cloudevents-kafka` → `kafka-clients`) and the absence of
      a patched release ("vulnerable through 1.1.10.8"), and a rolling expiry
- [x] 2.2 Add the `t-digest` ignore for `SNYK-JAVA-COMTDUNNING-19778375`, pinned to `'* > com.tdunning:t-digest@3.3'`,
      with a load-tests-only reason matching the existing `SNYK-JAVA-COMTDUNNING-19659413` entry, and a rolling expiry

## 3. Spec delta

- [x] 3.1 Add the `MODIFIED` delta for `showcase/quality/dependency-security` carrying the full constraint requirement
      (description + all four existing scenarios) plus `io.netty` and its new scenario

## 4. Verify

- [x] 4.1 `./gradlew spotlessApply` and `workflowLint` (the `.snyk` file is not formatted, but the workflow is)
- [x] 4.2 `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` (the bump touches the runtime classpath)
- [x] 4.3 Run `./gradlew dependencySecurityCheck` with `SNYK_TOKEN` set and confirm no vulnerable paths remain (the only
      real check for the suppressions; `workflowLint` cannot see inside the policy file)
- [x] 4.4 Confirm `openspec validate --all` passes and the delta's `MODIFIED` block lists all four existing scenarios

## 5. Docs

- [ ] 5.1 Refresh the `dependency-security` spec `## Purpose` in the archive commit (a delta cannot carry a Purpose) —
      the Purpose enumerates the constrained coordinates and now needs `io.netty`
- [x] 5.2 Check `AGENTS.md`'s dependency-security bullet and `.snyk` header for a stale enumeration of the suppressed
      findings or the constrained coordinates, and update in the same change — checked: `AGENTS.md:292` cites `zstd-jni`
      only as an example of the Purpose-refresh rule, not an enumeration of the constrained set, so no edit is owed
