# Proposal

## Why

The scheduled `Dependency Security` run `41239187028` (2026-10-31) failed: 16 of 20 projects report vulnerable paths,
where the 2026-10-25 run found none. Four coordinates account for every finding — `org.ow2.asm:asm` (six medium
advisories, via JaCoCo's tool classpath at 9.8/9.10.1 and SpotBugs' at 9.11), `com.aayushatharva.brotli4j` (six critical
HTTP2Shell out-of-bounds writes at 1.23.0, via Gatling), `net.minidev:json-smart` (medium, no fixed release), and
`org.objenesis:objenesis` (low, via Mockito).

## What Changes

- **`gradle/libs.versions.toml`** — `jacoco` `0.8.13` → `0.8.16` (which ships ASM `9.11.1`), plus new catalog entries
  for `org.ow2.asm:asm` (`9.11.1`), `com.aayushatharva.brotli4j:brotli4j` (`1.24.0`), and `org.objenesis:objenesis`
  (`3.6`).
- **`build.gradle.kts`** — the root project applies the `jacoco` plugin for `jacocoRootReport` without a `toolVersion`,
  so its `jacocoAnt` classpath resolved Gradle's built-in `0.8.15`; pin it to the catalog's `jacoco` version.
- **`build-logic/.../code-check-conventions.gradle.kts`** — constrain `org.ow2.asm:asm` to `9.11.1` on the `spotbugs`
  tool classpath, which pins ASM `9.11` and does not reach the platform.
- **`platform/build.gradle.kts`** — import `brotli4j-bom` (`1.24.0`) as a platform, and constrain `objenesis` to `3.6`
  and `org.ow2.asm:asm` to `9.11.1` for every classpath the platform reaches.
- **`.snyk`** — one ignore for `SNYK-JAVA-NETMINIDEV-21715438` (no fixed release exists), pinned to
  `net.minidev:json-smart@2.5.2` with a reason and an expiry.
- **`showcase/quality/dependency-security`** — the "Vulnerable transitive dependencies are constrained to patched
  versions" requirement gains the newly constrained coordinates and their scenarios.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/dependency-security`: the constraint requirement names the ASM, brotli4j, and objenesis coordinates,
  the three surfaces ASM needs (JaCoCo's release ships the fix; the platform and the `spotbugs` tool classpath carry the
  constraint), and the family-BOM route brotli4j takes. The json-smart suppression owes no delta, because the scan
  task's existing suppression policy already covers it.

## Impact

- **No production code changes**: every coordinate is a tool or test classpath, `brotli4j` reaches only `load-tests`,
  and nothing new ships to a service at runtime.
- **The scan goes green** — verified locally on 2026-11-01: `./gradlew dependencySecurityCheck` reports "Tested 20
  projects, no vulnerable paths were found" — and the scheduled run confirms it once its next credentialed pass applies
  the same constraints.
