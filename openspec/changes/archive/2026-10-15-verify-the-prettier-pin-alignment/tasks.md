# Tasks

## 0. The pre-write derivation pass (this change carries its own claims table)

- [x] 0.1 Run the pre-write derivation pass and record its output as the claims table in `design.md`'s Context.

## 1. The rules

- [x] 1.1 `PrettierAlignmentRules` in `build-logic`: parse the catalog's `prettier` version from the `[versions]`
      section and the version the web module's lockfile resolves, and return a finding when they differ or either is
      absent — pure functions, so they are testable without Gradle.
- [x] 1.2 `PrettierAlignmentRulesTests`: the known-bad and known-good cases — an aligned pair writes no finding; the
      drift this change exists for (a catalog pin beside a different resolved version) fails naming both; a missing
      entry on either side fails; an unreadable lockfile fails rather than reading as no version; and the two keys a
      naive read would cross — `prettier-plugin` (excluded by the assignment) and `my-prettier` (excluded by the anchor)
      — are not read as the pin. Each case asserts a decision the rules encode, and the drift case fails against an
      implementation that only checks presence.

## 2. The check

- [x] 2.1 `VerifyPrettierAlignmentTask` in `build-logic`: read the two committed files, call the rules, fail with their
      findings, print the agreement, and declare the files as inputs and a result file as its output so it is cacheable.
- [x] 2.2 Register it in the root `build.gradle.kts` (the `verification` group, pointing at the catalog and the
      lockfile) and add it to `check`'s `dependsOn` list.

## 3. Capture the behavior in the spec

- [x] 3.1 The delta for `showcase/quality/code-quality`: a `MODIFIED` block carrying the "Source formatting is enforced
      by the build" requirement's description and **all ten** of its scenarios by name and in the main spec's order,
      with the description gaining the version-agreement sentence and one sibling scenario for a drift — never a renamed
      scenario.

## 4. Docs the change owns

- [x] 4.1 Add the new task to the two live enumerations of the `verify*` members — `AGENTS.md`'s note on `check`'s
      members and `README.md`'s `verify*` block — and sweep `openspec/config.yaml` and `docs/` for any statement of the
      checks the change falsifies.
- [x] 4.2 Record the capability's `## Purpose` outcome: **no refresh owed** — the Purpose describes the capability's
      scope and its checks, not the Prettier version, and the change adds a check without moving that scope.

## 5. Verification

- [x] 5.1 Re-run the claims-table rows with their sources and confirm each reproduces.
- [x] 5.2 Run `./gradlew :build-logic:test` (the rules' cases), then
      `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`, `spotlessApply`/`spotlessCheck`,
      `openspec validate --all`, and the manual 120-character check over the changed set.
- [x] 5.3 Verify the check itself bites: perturb the catalog's `prettier` version, run the task, and confirm it fails
      naming both versions; then restore and confirm it passes.
- [x] 5.4 Run a `review-quick` pass over the implementation, passing the prior round's instances.
- [x] 5.5 Run the per-unit `lesson-capture` and record the applied net `AGENTS.md` delta. Outcome: **nothing durable**
      (0 additions, 0 retirements; net 0) — every class was a compliance miss of a rule the file already carries. The
      capture also found a live defect it routed here: the rules' KDoc attributed the anchor to the wrong case (the
      trailing assignment excludes `prettier-plugin`, the anchor excludes `my-prettier`), now corrected in the KDoc and
      in the artifacts that repeated it.

- [x] 5.6 Request the user's manual review pass.
