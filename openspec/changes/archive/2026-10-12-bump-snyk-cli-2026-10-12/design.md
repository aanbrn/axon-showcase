# Design

## Context

See `proposal.md` — Why. The `toolingUpdates` task reads the pin from the workflow file through a declared entry
(`build.gradle.kts`): `snyk-cli`, `workflowFile = "dependency-security.yml"`,
`pinPattern = "snyk-version:\s*(v?[0-9][^\s]*)"`, `source = GITHUB_RELEASE`, `sourceRef = "snyk/cli"`. The pattern is
asserted to match exactly once — a renamed input fails the task rather than reading as current — which is why the pin
lives in the workflow and not the version catalog: the catalog owns build-tool versions _the build resolves_, while this
is a CLI the workflow installs.

The report named `v1.1307.4 -> v1.1308.0`; the live release lookup confirms `v1.1308.0` is still the latest, so the
issue's target was not stale this time (the rule exists because it can be).

## Goals / Non-Goals

**Goals:**

- Move the pin to the resolved latest, keeping the declared `pinPattern` matching (the check fails on an unmatched
  pattern, so the YAML shape matters as much as the value).
- Verify by the credentialed dispatch, which is the only thing that proves the tag installs and the scan runs.

**Non-Goals:**

- Touching `snyk/actions/setup@master` — a deliberate floating ref (the action's own releases are not what this pin
  tracks).
- Touching the `.snyk` policy: the suppressed findings and their expiry rationale are owned by the policy file and the
  `/dependency-security-check` command, and no advisory is retired by a CLI bump.

## Decisions

**Verify by dispatch, not by the lint.** `workflowLint` parses the YAML; it cannot install a tag. A Snyk CLI bump has no
local equivalent — `dependencySecurityCheck` needs a token — so the dispatch on the pushed branch is the check of
record, and it exercises the whole workflow path (setup, scan, report).

**Re-resolve the target before bumping.** The report is generated weekly and the upstream may publish in between; the
target is confirmed with the tool's own lookup and the report re-run afterwards, so the finding's clearance has a run
behind it rather than an assumption.

## Risks / Trade-offs

- The dispatch is the only evidence, and it consumes the repository secret → it is run once, deliberately, on the
  branch, not repeatedly.
- A future Snyk CLI release could move the pin again before the next report → the check re-derives it weekly, so the
  drift surfaces as a fresh row rather than a stale one.

## Migration Plan

None: a workflow pin, taking effect on the next scheduled or dispatched run.
