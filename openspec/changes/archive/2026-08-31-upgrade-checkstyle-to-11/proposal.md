## Why

The weekly Snyk scan failed (2026-08-31) because a new advisory — `SNYK-JAVA-ORGAPACHEHTTPCOMPONENTSCLIENT5-19432178`
(CVE-2026-71290, Improper Certificate Validation, critical, affects `httpclient5` < 5.6.4, published 2026-08-30) —
landed on the checkstyle build-tool chain: `checkstyle@10.26.1 -> Saxon-HE@12.5 -> xmlresolver@5.2.2 ->
httpclient5@5.1.3`. The app runtime is already patched (the catalog pins `httpclient5` at 5.6.4), so only the
build-tool occurrence is affected. Rather than add yet another `.snyk` ignore on this dead node — `httpclient5@5.1.3`
already carries one ignored advisory and `httpcore5-h2@5.1.3` carries three, so new vuln IDs on the same pinned
version turn the weekly scan red again — fix it at the source by upgrading checkstyle (to 13.11.0, which Snyk
recommends because it drops the whole tooling subtree).

## What Changes

- Upgrade the `checkstyle` catalog version from `10.26.1` to `13.11.0` in `gradle/libs.versions.toml`. 13.11.0 is
  Snyk's suggested fix: it removes the entire tooling chain (`Saxon-HE -> xmlresolver`), which a stop at 11.0.0 would
  not fully achieve (11.0.0 still pulls `doxia-core -> httpclient4 -> commons-codec@1.11`).
- Verify the `checkstyleMain`/`checkstyleTest` gates still pass under checkstyle 13.11.0 against the project ruleset
  (`config/checkstyle/checkstyle.xml`, `maxErrors = 0`) across all modules that apply `code-check-conventions`.
- Remove the now-dead `.snyk` ignore entries whose vulnerable nodes disappear from the checkstyle 13.11.0 tree:
  `httpclient5@5.1.3` (`SNYK-JAVA-ORGAPACHEHTTPCOMPONENTSCLIENT5-18857813`), `httpcore5-h2@5.1.3`
  (`...CORE5-15857052`, `...CORE5-17817217`, `...CORE5-17817218`), `plexus-utils@3.3.0`
  (`SNYK-JAVA-ORGCODEHAUSPLEXUS-15766699`), and `commons-lang3@3.8.1` (`SNYK-JAVA-ORGAPACHECOMMONS-10734078`).
- Remove the obsolete tooling comment in `.snyk`; the policy then holds only the Spring cluster entries.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/dependency-security`: the `httpclient5` patched-version floor in the platform constraint
  requirement changes from "at least `5.6.3`" to "at least `5.6.4`", matching the fix floor of the new advisory
  and the version the catalog already resolves to.

## Impact

- `gradle/libs.versions.toml` — `checkstyle` version ref bump.
- `.snyk` — six ignore entries removed, one comment removed; no new ignores added.
- `openspec/specs/showcase/quality/dependency-security/spec.md` — requirement text updated.
- Build gate: `checkstyleMain`/`checkstyleTest` on every module that opts into `code-check-conventions`; checkstyle 13
  is a major release and may change rule behavior, so the ruleset must be re-verified (see `design.md`).
- Gradle's built-in `checkstyle` plugin (bundled with the Gradle wrapper) must support tool version 13.x.
- The dependency security scan output: the checkstyle chain no longer reports any tooling advisories, so the
  remaining `.snyk` policy covers only the Spring cluster.