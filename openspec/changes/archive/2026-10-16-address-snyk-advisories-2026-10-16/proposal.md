# Proposal

## Why

The credentialed dispatch the tooling-pin unit ran (run `38877275517`) surfaced two advisories published since the last
weekly scan, so the scheduled `Dependency Security` run stays red: `SNYK-JAVA-ORGHIBERNATEVALIDATOR-20877468`
(Expression Language injection, medium, `org.hibernate.validator:hibernate-validator@8.0.3.Final`) and
`SNYK-JAVA-JAKTAVALIDATION-20936881` (low, `jakarta.validation:jakarta.validation-api@3.1.1`). The run flags both in
every project it scans. Four modules declare them by accessor — `libs.hibernate.validator` in `showcase-command-api`,
`showcase-query-client`, `showcase-api-gateway` and `showcase-identifier-extension`'s tests, and
`libs.jakarta.validation.api` in `showcase-identifier-extension` — and their versioning differs: the catalog's
`hibernate-validator` entry carries only `group`/`name` (the Spring Boot BOM manages it), while `jakarta-validation-api`
is catalog-pinned at `3.1.1`.

## What Changes

- **`.snyk`** — two ignores, each pinned to its exact assessed vulnerable version
  (`'* > org.hibernate.validator:hibernate-validator@8.0.3.Final'`,
  `'* > jakarta.validation:jakarta.validation-api@3.1.1'`), each with a reason naming the fix that would retire it (the
  path they reach is recorded in the policy's header comment), and each with a short-term expiry so the weekly scan
  re-surfaces it.
- **`showcase/quality/dependency-security`** — the "Local dependency security scan task" requirement gains the
  suppression-policy clause the npm requirement beside it already states, so the policy the ignores rely on is spec'd
  rather than only living in `.snyk`'s header.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/dependency-security`: the "Local dependency security scan task" requirement states the `.snyk`
  suppression policy (an unfixable advisory, pinned to its exact version with a reason and an expiry, re-surfacing when
  it expires).

## Impact

- **The scheduled scan goes green again** once its next credentialed run applies the policy — the verification this
  change owes, since no local gate reads `.snyk`.
- **No dependency moves**: nothing is bumped, so no code path changes.
