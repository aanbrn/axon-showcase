## 1. Upgrade checkstyle to 13.11.0 and verify the style gates

- [x] 1.1 Bump `checkstyle = "10.26.1"` to `"13.11.0"` in `gradle/libs.versions.toml` (Snyk's suggested fix for the
      commons-codec finding that 11.0.0 would leave behind) and verify the 13.11.0 tool runs under Gradle 9.7.1
      without the built-in checkstyle plugin rejecting it (`:showcase-command-service:checkstyleMain` succeeds)
- [x] 1.2 Run checkstyle across every module that applies `code-check-conventions`
      (`./gradlew checkstyleMain checkstyleTest`) and confirm `maxErrors = 0` holds against the existing
      `config/checkstyle/checkstyle.xml`; adapt the ruleset or suppressions only if the major bump tripped a check,
      then re-verify all modules
- [x] 1.3 Confirm the Saxon/XPath suppression path behaves unchanged under `xmlresolver` 5.3.3 (the HTTP catalog
      transport was dropped) by spot-checking that suppressed-violation counts on a sample of modules are identical
      before and after the bump

## 2. Prune the .snyk policy

- [x] 2.1 Remove the dead ignore entries whose vulnerable nodes vanish with checkstyle 13.11.0:
      `SNYK-JAVA-ORGAPACHEHTTPCOMPONENTSCLIENT5-18857813` (`httpclient5@5.1.3`), the three `httpcore5-h2@5.1.3`
      entries (`SNYK-JAVA-ORGAPACHEHTTPCOMPONENTSCORE5-15857052`, `...-17817217`, `...-17817218`),
      `SNYK-JAVA-ORGCODEHAUSPLEXUS-15766699` (`plexus-utils@3.3.0`), and `SNYK-JAVA-ORGAPACHECOMMONS-10734078`
      (`commons-lang3@3.8.1`); verified by re-running `dependencySecurityCheck` — the scan is still clean without them
- [x] 2.2 Remove the now-obsolete tooling comment in `.snyk` (the checkstyle tree no longer carries any tooling
      nodes, so nothing remains to document) and confirm the policy holds only the Spring cluster entries

## 3. Verify the scan and refresh docs

- [x] 3.1 Run `./gradlew dependencySecurityCheck` (Snyk CLI on PATH) and confirm it passes; verified clean
      ("Tested 19 projects, no vulnerable paths were found") with the pruned policy, whose only remaining
      suppressions are the Spring cluster entries
- [x] 3.2 Review AGENTS.md and README.md for statements affected by this change (dependency-security/checkstyle
      mentions) and refresh them if stale, then verify no edited line exceeds 120 characters