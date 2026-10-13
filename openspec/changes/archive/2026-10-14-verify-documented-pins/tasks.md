# Tasks

## 0. The pre-write derivation pass (this change carries its own record)

- [x] 0.1 Ran the pre-write derivation pass over this change's planning artifacts before the first review round,
      deriving each of the four claim classes `AGENTS.md`'s review-loop bullet names from its source: the artifact set
      was derived from the analogue (`VerifyInstallCommandsTask` and its rules/tests); the two pins the incident left
      stale were read from the retrospective's own text (not recalled); the doc forms and the catalog keys were read
      from `AGENTS.md` and `gradle/libs.versions.toml`; and the rule's shape was derived from `InstallCommandRules` and
      the task's registration. It also caught that the premise ("every pin a live doc names") was unbounded, which is
      why the scope is the catalog's Paketo pins.

## 1. The rule and its task

- [x] 1.1 Added `DocumentedPinRules` to `build-logic`: given the live docs' text and the catalog's Paketo pins, it
      extracts every stated pin in the two declared adjacent forms (`paketo-buildpacks/<name>@<version>`,
      `builder-jammy-base:<version>`, a version starting with a digit) and reports a mismatch naming the doc, the pin as
      written, and the catalog version; a stated pin with no catalog key fails too, naming the key.
- [x] 1.2 Added `DocumentedPinRulesTests` (`build-logic/src/test/kotlin/DocumentedPinRulesTests.kt`) — its cases are the
      class's own `@Test` methods.
- [x] 1.3 Added `VerifyDocumentedPinsTask` mirroring `VerifyInstallCommandsTask` (`@CacheableTask`; the docs as
      `@InputFiles`, the catalog pins as an `@Input` map; a `resultFile` `@OutputFile`), registered as
      `verifyDocumentedPins` in `build.gradle.kts` and added to `check`.
- [x] 1.4 Derived the task's inputs from the catalog and the named live docs (`AGENTS.md`, `README.md`): the pins come
      from the `buildpackChecks` list (extracted to a top-level `val`, so `buildpackUpdates` and this task share one
      source), not a hand-written map.

## 2. Capture the behavior in the spec

- [x] 2.1 Wrote the delta for `showcase/quality/infra-image-versions`: a `MODIFIED` block for "Infrastructure image
      references are single-sourced", carrying the full requirement (description + all six existing scenarios) with the
      documented-pin assertion widened to the catalog's Paketo pins and a new scenario for it.

## 3. Docs the change owns

- [x] 3.1 Stated the widened check in `AGENTS.md`'s pin-verification note, added the task to `AGENTS.md`'s
      `check`-member enumeration, and added it to `README.md`'s `verify*` command enumeration.
- [x] 3.2 Annotated `docs/retrospectives/2026-10-13.md`'s S2 row as **Applied**, scoped to the catalog's Paketo pins
      (the table has no Disposition column).

## 4. Verification

- [x] 4.1 Ran `./gradlew :build-logic:test` (the rule's cases pass) and `verifyDocumentedPins` — it reports the three
      current pins (`paketo-buildpacks/nginx@1.3.0`, `paketo-buildpacks/procfile@5.15.3`, `builder-jammy-base:0.4.659`)
      matching the catalog, and does not match the historical mentions.
- [x] 4.2 Proved the check fails on a known-bad input: changing `AGENTS.md`'s `paketo-buildpacks/nginx@1.3.0` to
      `@1.2.0` made `verifyDocumentedPins` fail with
      `AGENTS.md: states 'paketo-buildpacks/nginx@1.2.0', but the catalog pins 'paketo-nginx' at '1.3.0'.`; the doc was
      restored and the task passed again.
- [x] 4.3 Ran `./gradlew spotlessApply` (BUILD SUCCESSFUL) then `spotlessCheck`, `openspec validate --all`, and the
      manual 120-character check over the changed non-formatter files (clean).
- [x] 4.4 Ran the implementation `review-quick` loop: 11 implementation rounds (continuous rounds 3–13), each finding
      fixed, round 13 clean (0 findings).
- [x] 4.5 Ran the per-unit `lesson-capture`; applied its one durable proposal — the parser-gate bullet now names the
      **mutation battery** (find an unexercised regex branch by mutating it, add the discriminating fixture) and the
      **accepted-equivalence record** for a branch no live input can reach. Applied net `AGENTS.md` delta: **+9 lines**.
- [ ] 4.6 Request the user's manual review pass.
