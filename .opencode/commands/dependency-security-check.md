---
description: Run Snyk security test across all Gradle sub-projects
---

Run `./gradlew dependencySecurityCheck` from the repository root and report the results.

Report any vulnerabilities found, grouped by project and severity, and note any errors. Do not attempt to fix findings
unless asked.

The scan passes `--policy-path=.snyk` (the root Snyk policy), which version-pins the ignored findings
(`* > pkg@version`) so only the exact assessed vulnerable versions are suppressed. The suppressed Spring Framework
6.2.x / Spring Security 6.5.x cluster is fixed only by the deferred Spring Boot 4 migration (ADR-0004), and the
remaining findings are transitive checkstyle/Maven tooling in the load-tests module. Ignore entries carry a
short-term `expires` so they re-surface if not resolved in time — see the `.snyk` header comment.

**Snyk rate limit:** the free org plan allows **200 Open Source tests per billing period** (monthly; confirmed by the
plans-page FAQ and the CLI's own "monthly limit of 200 private tests" message — note the docs.snyk.io usage-settings
page's "400" is stale). A test is counted **only for a manifest file where vulnerabilities are identified**, so the
quota is consumed by scans that *find* issues, not by clean ones. Because `--policy-path=.snyk` suppresses every
finding, `dependencySecurityCheck` reports zero identified vulnerabilities and effectively does not consume quota —
a policy-passing scan works even after the quota has been exhausted by unfiltered scans. The rate-limit failure
("You have reached your monthly limit of 200 private tests") therefore only bites when a scan actually identifies
findings, which is the case to expect on the raw `snyk test --all-sub-projects --json` verification runs. If the
quota is exhausted, wait for the monthly reset (start of the next calendar month in practice; see
`app.snyk.io → Settings → Usage`) rather than retrying repeatedly, and do not treat the rate-limit failure as a real
vulnerability finding. A full unfiltered report is a large response; cache it to a temp file if it will be queried
more than once.
