## 1. Reconcile the value

- [x] 1.1 Raise the `showcaseCache` Java default in
      `showcase-command-service/src/main/java/showcase/command/ShowcaseCommandProperties.java` from `1000` to `100000`,
      and verify the three implementation surfaces agree (`ShowcaseCommandProperties`, the `application.yml`
      placeholder, and `helm/chart/src/main/helm/values.yaml`'s `showcaseCache.maxSize`) — `application.yml` and the
      chart are already `100000`, so only the Java field changes
- [x] 1.2 Update the `showcaseCache` maximum-size assertion in `allPropertiesHaveDocumentedDefaults`
      (`ShowcaseCommandPropertiesCT`, the Java-defaults test) from `1000` to `100000`, since it asserts the field this
      task changes — verify the Java-defaults test passes

## 2. Close the gap that let it drift

- [x] 2.1 Add a `showcaseCache` maximum-size assertion to `applicationYmlPlaceholdersBindDocumentedDefaults`
      (`ShowcaseCommandPropertiesCT`, the yml-wiring test), and verify it binds by temporarily perturbing the **yml
      placeholder** (`${SHOWCASE_CACHE_MAX_SIZE:…}`) and confirming that test fails — it loads `application.yml`, so the
      Java field is irrelevant to it; the Java-defaults half is task 1.2's assertion
- [x] 2.2 Verify the whole component-test class passes and that no other cache surface (saga, saga-associations, the
      gateway's query caches) changed

## 3. Docs refresh

- [x] 3.1 Remove this change's own idea from `docs/ideas.md` — the `showcaseCache`/ADR-0002 entry under `## 2026-09-14`
      — on this branch, since the change implements it
- [x] 3.2 Check `AGENTS.md`, `README.md`, `docs/adr/`, and `openspec/config.yaml` for a statement of the cache default,
      and refresh any the reconciled value makes stale; verify by grepping each for `showcaseCache` and
      `SHOWCASE_CACHE_MAX_SIZE` — none currently states it, so a no-op is the expected outcome

## 4. Verification

- [x] 4.1 Run `./gradlew :showcase-command-service:componentTest` and the module's `test`, then the PR gate
      `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`, and confirm `spotlessCheck` and
      `openspec validate --all` are clean after the final edit
- [x] 4.2 Confirm no deployed value moved: `git diff` touches neither `application.yml` nor the chart for the
      `showcaseCache` size, and the module's yml-defaults test still asserts the other caches unchanged
- [x] 4.3 The `showcase/write-side/command-service` `## Purpose` check for the archive commit: it still fits — it
      describes the write side's behavior without naming cache values — so it needs no edit. Recorded rather than
      applied, since a delta cannot carry a Purpose
