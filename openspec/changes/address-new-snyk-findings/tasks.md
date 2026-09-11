## 1. Constrain zstd-jni

- [x] 1.1 Add a `zstd-jni` version (`1.5.7-16`) and library alias to `gradle/libs.versions.toml` and add
      `api(libs.zstd.jni)` to the `constraints` block in `platform/build.gradle.kts`
- [x] 1.2 Verify `com.github.luben:zstd-jni` resolves to `1.5.7-16` via `dependencyInsight` on a
      `kafka-clients`-consuming module's runtime classpath

## 2. Suppress the unfixable t-digest finding

- [x] 2.1 Add a version-pinned ignore for `SNYK-JAVA-COMTDUNNING-19659413` (`* > com.tdunning:t-digest@3.3`) with a
      reason and a short-term expiry to the root `.snyk` policy, and refresh the `.snyk`-suppressed-findings description
      in `AGENTS.md`

## 3. Verify

- [x] 3.1 Run `./gradlew dependencySecurityCheck` and confirm the scan reports no vulnerable paths (policy-suppressed
      findings consume no quota); run `./gradlew spotlessApply spotlessCheck` and `openspec validate --changes` cleanly
- [ ] 3.2 At archive, update the `showcase/quality/dependency-security` spec Purpose to name `zstd-jni` among the
      constrained transitives (a Purpose cannot be carried by a delta, and the main spec must not be edited before
      archive)
