# Security Policy

## Reporting a vulnerability

Please report security issues **privately**, using GitHub's private vulnerability reporting: this repository's
**Security** tab → **Advisories** → **Report a vulnerability**. Please do not open a public issue for a suspected
vulnerability.

This is a reference application, not a supported product, and it is provided under the MIT license (see
[LICENSE](LICENSE)) — there is no security team and no response-time commitment. Reports are welcome and will be looked
at as time allows.

## Scope

The interesting surface is the dependency tree and the deployment manifests rather than a hosted service:

- **Dependencies** are scanned weekly: JVM dependencies with [Snyk](.github/workflows/dependency-security.yml)
  (`./gradlew dependencySecurityCheck`) against the policy in `.snyk`, which records each currently-suppressed finding
  together with its expiry rationale, and the web UI's npm dependencies with `npm audit`
  (`./gradlew :showcase-web-ui:npmAudit`, failing on high-severity findings).
- **Container images** are built by the tasks documented in `AGENTS.md` → Docker Images.
- **Deployment manifests** live under `helm/chart/`, with the local target's values under `helm/values/`.
