---
description: Run the JVM Snyk scan and the web UI npm audit
---

Report the dependency-security scan results. This command reports only — it never fixes a finding unless asked.

**Trigger** — the owner invokes `/dependency-security-check`, or the weekly `dependency-security` workflow runs the same
scan tasks.

**Procedure**

1. Run both scans from the repository root: `./gradlew dependencySecurityCheck` (JVM, Snyk) and
   `./gradlew :showcase-web-ui:npmAudit` (web UI, `npm audit`).
2. Report the two outcomes **separately** — a clean JVM scan says nothing about the web UI, and the reverse.
3. Report the vulnerabilities found, grouped by project and severity.

**Decision points**

- IF a scan fails, THEN report the failure as a scan outcome, separately from any finding.
- IF the npm-audit task reports suppressions, THEN surface the outcomes it distinguishes: the suppressions it applied,
  each violation it reports (a malformed suppression entry, or one matching no finding), and the unsuppressed findings —
  an expired entry suppresses nothing, so its finding appears among the unsuppressed ones. The task fails on
  high-severity findings and needs no Snyk token or policy file (ADR-0014).
- IF a high-severity finding is unsuppressed, THEN report it as a finding rather than as a task failure.
- IF the Snyk output carries a rate-limit message, THEN report it as an operational condition, not as a vulnerability
  finding.

**Suppressions** — the Snyk scan passes `--policy-path=.snyk` (the root policy), which version-pins each ignored finding
(`* > pkg@version`) with a short-term `expires` so it re-surfaces if unresolved. The suppressed findings and their
rationale live in `.snyk`'s header comment and each ignore's `reason` — read them there rather than restating them here.
The web UI's suppressions are `showcase-web-ui/npm-audit-ignores.json`, whose staleness `npmAudit` fails on.

**Operational guidance for the Snyk service** — a plan's quota, its billing rule, and its reset schedule are properties
of an external service that change without this repository moving, so this command states none of them: IF a run reports
a quota or rate-limit condition, THEN read the provider's own usage page (or the CLI's message) at that moment and act
on what it says. Do not treat such a condition as a vulnerability finding, and do not retry a rate-limited scan in a
loop.

**Failure handling** — a scan that cannot run (a missing `SNYK_TOKEN`, no network) is reported as an unrun scan, never
as a clean one.

**Completion criteria** — the owner has both scans' outcomes separately, with every suppression outcome and finding the
tasks reported.
