# Tasks

Workspace: unit=address-snyk-advisories-2026-11-01; path=/Users/afanasyev/Development/axon-showcase;
branch=address-snyk-advisories-2026-11-01; writer=ses_e6bb3d98effeNURMqTdoGwAu9f

## 1. Tool classpaths (ASM)

- [x] 1.1 `gradle/libs.versions.toml`: bump `jacoco` from `0.8.13` to `0.8.16`, add an `asm` version (`9.11.1`) with its
      library entry, and add the `brotli4j` (`1.24.0`) and `objenesis` (`3.6`) entries.
- [x] 1.2 `build.gradle.kts`: pin the root project's JaCoCo tool version from the catalog
      (`jacoco { toolVersion = libs.versions.jacoco.get() }`), so `jacocoRootReport` stops resolving Gradle's built-in
      `0.8.15`.
- [x] 1.3 `build-logic/src/main/kotlin/code-check-conventions.gradle.kts`: add `spotbugs(libs.asm)` to the existing
      `constraints` block, alongside `checkstyle(libs.guava)`.
- [x] 1.4 Verify the root's `jacocoAnt` classpath resolves ASM `9.11.1`:
      `./gradlew :dependencyInsight --configuration jacocoAnt --dependency org.ow2.asm:asm`.
- [x] 1.5 Verify a module's `spotbugs` classpath resolves ASM `9.11.1`, and that the constrained tool still executes:
      `./gradlew :showcase-command-service:dependencyInsight --configuration spotbugs --dependency org.ow2.asm:asm`,
      then `./gradlew :showcase-command-service:spotbugsMain`.

## 2. Platform constraints

- [x] 2.1 `platform/build.gradle.kts`: import `libs.brotli4j.bom` as an `api(platform(...))` and add `api(libs.asm)` and
      `api(libs.objenesis)` to the existing `constraints` block.
- [x] 2.2 Verify `brotli4j` resolves to `1.24.0` for all six artifacts on `:load-tests`' `gatlingRuntimeClasspath`, and
      `objenesis` to `3.6` on `:showcase-command-service`'s `componentTestRuntimeClasspath`.
- [x] 2.3 Constrain `org.ow2.asm:asm` in the platform too, after the first local credentialed scan showed
      `accessors-smart`'s ASM `9.7.1` still on the suites, and verify it resolves `9.11.1` on `:showcase-api-gateway`'s
      `componentTestRuntimeClasspath`.

## 3. The suppression

- [x] 3.1 `.snyk`: add the `SNYK-JAVA-NETMINIDEV-21715438` ignore pinned to `'* > net.minidev:json-smart@2.5.2'`, with a
      reason naming the missing fixed release and the test scopes it is reached on, an expiry, and no `: ` in the
      reason.
- [x] 3.2 Extend `.snyk`'s header comment so the new coordinate and its route are stated beside the existing ones.

## 4. The spec delta

- [x] 4.1 Add the `MODIFIED` delta for `showcase/quality/dependency-security` carrying the "Vulnerable transitive
      dependencies are constrained to patched versions" requirement's full body and all eight scenarios, with the new
      coordinates and one scenario each for them: ASM on the three surfaces it needs (JaCoCo's release ships the fix,
      the platform constrains every classpath it reaches, the `spotbugs` tool classpath takes its own constraint),
      brotli4j, and objenesis.
- [ ] 4.2 Record the `## Purpose` refresh as its own step: a delta cannot carry a Purpose, so add the new coordinates to
      the Purpose's enumeration in the archive commit, and verify it names them after archive.
- [x] 4.3 Confirm `docs/ideas.md` carries no entry this change implements; none was found, so nothing is removed.

## 5. Verification

- [x] 5.1 Run the Docker-free check on a module the ASM and objenesis constraints touch:
      `./gradlew :showcase-command-service:check -PskipITs -Pcoverage.gate.enabled=false`.
- [x] 5.2 Run `./gradlew :load-tests:compileGatlingJava` to prove the forced brotli4j family still resolves and compiles
      against Gatling's API.
- [x] 5.3 Run the credentialed scan locally once the owner refreshed the Snyk credential:
      `./gradlew dependencySecurityCheck` reports "Tested 20 projects, no vulnerable paths were found". Its first run
      found ASM `9.7.1` through `accessors-smart`, which task 2.3 fixed.
- [x] 5.4 **After the owner's approval pushes the branch** (before the archive commit), dispatch
      `dependency-security.yml` from it
      (`gh workflow run dependency-security.yml --ref address-snyk-advisories-2026-11-01`) and watch the run: it must
      report no vulnerable paths. A remaining finding names either an unconstrained coordinate or a suppression that did
      not take effect. Run `41257599218` from this branch reports "Tested 20 projects, no vulnerable paths were found"
      (`BUILD SUCCESSFUL`).
- [x] 5.5 Run `./gradlew spotlessApply` and `./gradlew spotlessCheck`.
- [x] 5.6 Run `openspec validate --all` and confirm the change validates.
- [x] 5.7 Run the manual 120-character check over the whole changed set, derived with
      `git status --porcelain --untracked-files=all`.
- [x] 5.8 Run the review gate's `review-quick` loop over the proposal and the implementation, repeating until clean.
      Seven rounds: 10, 4, 1, clean, 5, 1, clean — the fourth was superseded when the local credentialed scan changed
      the implementation, so the loop closed on the final state. Deep-review routing is `RECOMMENDED` (three rows repeat
      a prior class); the owner decided **skip** on 2026-11-01, so no deep review runs and those signals stand as the
      recorded limitation.
- [x] 5.9 Run the `lesson-capture` subagent over the diff, apply its durable proposals, and record the applied net
      `AGENTS.md` delta here. Four proposals applied, all in `gradle-build`: the ordering (a tool's own release before a
      constraint) and the family-BOM lesson merged into the tool-transitive bullet; the direct-plugin `toolVersion` rule
      into the version-catalog bullet; and two new bullets (a suppression's evidence, and `maven-metadata.xml` over a
      search API). Net `AGENTS.md` delta is 0 lines (`git diff --numstat AGENTS.md` empty); the pack carries the four
      proposals' growth, measured by `git diff --numstat` on the pack rather than stated as a figure. No retirements
      were proposed. A fifth clause joined the pack when the local scan showed a suppression does not cover a vulnerable
      transitive of the suppressed package.
- [x] 5.10 Ask the owner for the manual review pass, after 5.8 is clean and 5.9 is done. Asked with the deep-review
      `RECOMMENDED` run-or-skip decision, which the gate leaves to the owner.

## 6. The change's own records

- [x] 6.1 Record the pre-write derivation pass as a task (every unit owes it). It ran: the claims table is in
      `design.md`, and its rows come from the run log, Snyk's advisory pages, Central's metadata and POMs, and the
      `dependencyInsight` probes.
- [x] 6.2 Write `docs/process-defects/address-snyk-advisories-2026-11-01.md`, or record that the unit observed none.

## Workflow follow-up

- Archive the change after the owner approves; its archive commit rides this PR.
- Dispatch the scan after the push: it is the only verification the local environment cannot run, so it completes inside
  this PR before the archive commit.
