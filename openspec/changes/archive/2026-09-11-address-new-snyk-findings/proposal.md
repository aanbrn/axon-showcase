# Proposal: Address new Snyk dependency findings

## Why

A fresh `snyk test --all-sub-projects` (2026-09-11) reports two vulnerable paths the current policy does not cover:
`com.github.luben:zstd-jni:1.5.6-4` (five high-severity issues — out-of-bounds read, integer overflow, use-after-free)
pulled in by `kafka-clients:3.9.2` across 12 sub-projects, and `com.tdunning:t-digest:3.3` (high "Inefficient
Algorithmic Complexity") pulled in by `gatling-charts` in `load-tests`. zstd-jni has a patched release; t-digest has no
fixed version at all.

## What Changes

- **Constrain `zstd-jni`**: add `com.github.luben:zstd-jni` to the `platform` BOM constraints at a patched version, so
  every module that brings it transitively via `kafka-clients` resolves the patched jar.
- **Suppress the unfixable `t-digest` finding**: add a version-pinned ignore to the root `.snyk` policy with a
  short-term expiry, because no patched release exists (it is a `gatling-charts` transitive in `load-tests` only).
- **Refresh `AGENTS.md`**: its description of the `.snyk`-suppressed findings gains the `t-digest` entry (docs that ARE
  the change — same PR).

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `showcase/quality/dependency-security`: the requirement "Vulnerable transitive dependencies are constrained to patched
  versions" gains `com.github.luben:zstd-jni` (resolving to at least `1.5.7-14`).

## Impact

- **Code/config**: `gradle/libs.versions.toml` (new `zstd-jni` version + library), `platform/build.gradle.kts`
  (constraint), `.snyk` (t-digest ignore), `AGENTS.md` (suppressed-findings description).
- **Dependencies**: `com.github.luben:zstd-jni` `1.5.6-4` → `1.5.7-16` (patch line) on the resolved classpaths of every
  module consuming `kafka-clients`.
- **Build/tests**: `dependencySecurityCheck` reports no vulnerable paths; Kafka serialization (zstd compression) is
  exercised by the existing integration tests.
- **Not shipped**: `t-digest` is a `load-tests`-only transitive (Gatling chart generating), so the suppression does not
  alter any deployed artifact.
