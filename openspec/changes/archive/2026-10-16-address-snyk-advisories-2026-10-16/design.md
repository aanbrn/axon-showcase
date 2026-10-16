# Design

## Context

See `proposal.md` — Why. The findings came from a credentialed dispatch rather than a local run
(`dependencySecurityCheck` needs the Snyk CLI and a token), and the fix question for each was answered from the tool's
own statement rather than inferred:

- `SNYK-JAVA-ORGHIBERNATEVALIDATOR-20877468`: Snyk's page says "Upgrade `org.hibernate.validator:hibernate-validator` to
  version 9.2.0.Beta1 or higher", and Central's metadata confirms `8.0.6.Final` is the newest 8.x while 9.2.0.Beta1 is
  where the fix lands. `8.0.6.Final` exists and does not fix it, so no patch-level bump is available below the Spring
  Boot 4 line.
- `SNYK-JAVA-JAKTAVALIDATION-20936881`: Snyk says `4.0.0-M1`, a milestone; `3.1.1` is the newest stable release.
  GitHub's advisory database returns no entry for this coordinate at all, so Snyk's statement is the evidence here.

Both are declared by accessor in four modules — `libs.hibernate.validator` in `showcase-command-api`,
`showcase-query-client`, `showcase-api-gateway` and `showcase-identifier-extension`'s tests, and
`libs.jakarta.validation.api` in `showcase-identifier-extension` — and their versioning differs, which decides what a
fix would take: `hibernate-validator` is BOM-managed (the catalog entry carries only `group`/`name`) so only a Boot 4
line can move it, while `jakarta.validation-api` is catalog-pinned at `3.1.1`, whose only newer release is the
`4.0.0-M1` milestone.

## Goals / Non-Goals

**Goals:**

- Turn the two findings into scoped, time-boxed suppressions so the weekly credentialed run goes green again, and state
  the suppression policy where it is spec'd.
- Record, in each ignore's reason, the exact fix that would retire it.

**Non-Goals:**

- Bumping `hibernate-validator` or `jakarta.validation-api`: neither has a usable fixed release below the Jakarta
  Validation 4 line, and taking a milestone would be the migration ADR-0004 defers.
- The Helm and Paketo findings, which are separate units.

## Decisions

**Suppress rather than bump, and pin each ignore to the exact assessed version.** The `.snyk` policy is already pinned
that way (its header says so), so a different version of the same package — potentially fixable — stays visible. Both
entries carry the same short-term expiry as the policy's existing Spring ignores, since all three wait on the same
deferred migration.

**State the suppression policy in the requirement that relies on it.** The npm requirement beside it already states the
same policy in full; the Snyk one mentioned only the `--policy-path` flag, so the policy the ignores depend on was
spec'd nowhere. This is a `MODIFIED` block carrying its full body and all three scenarios, plus one for an unexpired
suppression and one for its expiry passing.

**Verify by dispatch, since no local gate reads `.snyk`.** `dependencySecurityCheck` cannot run here, so the change's
verification is the credentialed workflow dispatched from its branch before the archive — the same route the
update-check rule gives a verification the local environment cannot run.

## Risks / Trade-offs

- **A suppression hides a real finding** → each is pinned to the exact vulnerable version and carries a short-term
  expiry, so the weekly scan re-surfaces it and the reason names the fix that retires it.
- **The policy file must stay parseable** → each reason avoids `: ` (the shape that silently breaks a YAML scalar in
  this repo's configs), and the dispatch is what proves the policy takes effect.

## Migration Plan

None: policy and spec text only, taking effect on the next credentialed run.

## Claims table

| Claim                                                       | Source                                                                                                                                                                                                                         |
| ----------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| Both advisories are new and unrelated to the tooling pins   | the run's own output (`38877275517`): one finding carries `(new)`, and neither coordinate is a CLI pin                                                                                                                         |
| The hibernate finding's only fix is the 9.x line            | `https://security.snyk.io/vuln/SNYK-JAVA-ORGHIBERNATEVALIDATOR-20877468` ("Upgrade … to version 9.2.0.Beta1 or higher") plus Central's metadata (`8.0.6.Final` newest 8.x)                                                     |
| The jakarta finding's only fix is a milestone               | the same page ("Upgrade … to version 4.0.0-M1 or higher") plus Central's metadata (`3.1.1` newest stable)                                                                                                                      |
| The two are declared by accessor, and versioned differently | `grep -rn 'libs.hibernate.validator\|libs.jakarta.validation.api' --include='build.gradle.kts'` finds four modules; the catalog's `hibernate-validator` entry has no `version.ref` while `jakarta-validation-api` pins `3.1.1` |
| The policy's existing shape                                 | `.snyk`'s header ("pinned to the exact assessed vulnerable version") and its Spring entries' expiry                                                                                                                            |
| No local gate reads `.snyk`                                 | `check`'s task list; `dependencySecurityCheck` is outside it and needs the CLI and a token                                                                                                                                     |
