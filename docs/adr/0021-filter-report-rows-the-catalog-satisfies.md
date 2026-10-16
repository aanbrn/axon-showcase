# ADR-0021: Filter dependency update report rows the catalog already satisfies

Date: 2026-10-16

Status: Accepted

## Context

`dependencyUpdates` reports `org.apache.logging.log4j:log4j-core [2.17.1 -> 2.26.1]` although the project takes `2.26.1`
everywhere. ADR-0007 recorded the row as a known plugin limitation and decided to live with it, attributing the floor to
`com.github.spotbugs:spotbugs-annotations`; that attribution does not hold at the pinned `4.10.5` (its module metadata
carries no `log4j` entry), and the floor is Gradle's own Log4Shell ban — `Log4jBannedVersion` in the distribution's
`gradle-logging` jar, `LOG4J2_CORE_REQUIRED_VERSION = 2.17.1` — applied to every classpath Gradle builds, which is why
the row names twenty projects, several of which declare no SpotBugs. ADR-0007's remaining alternatives were the flag
(`checkBuildEnvironmentConstraints = false`) and two ineffective filters; it did not weigh a rule over the catalog,
because the rules object such a rule extends arrived later (`hold-back-minor-dependency-updates`, #464).

The plugin **does** consult `rejectVersionIf` for the build-environment-constraint path: with a rule rejecting the
coordinate's candidates, the task's per-project partials record `latestVersion: "none"` with "Versions rejected by
component selection rules", and no update row is reported for it. An earlier reading that the row survived came from a
stale aggregate report its run had not regenerated — a run's partials are its authoritative output, the aggregate is
written afterwards.

## Decision

The `dependencyUpdates` report filters a candidate whose version the version catalog does not place below it: a
catalog-owned coordinate the project already takes, or is ahead of, is not reported, whatever "current" the plugin read
from a build-environment constraint's floor. The catalog is the project's version authority, which
`showcase/quality/dependency-management` already states for the report's scope. `checkBuildEnvironmentConstraints` stays
`true`, so a build-environment constraint that is not Gradle's own remains visible.

The rule needs both halves of that condition. The catalog's declared version is the authority; but the plugin treats a
coordinate whose every candidate is rejected as unresolved and moves it out of the report's "using the latest release
version" section, so a rule keyed on the catalog alone — rejecting any candidate no newer than the declared version,
whatever the plugin reports as current — emptied that section of all 83 catalog-owned entries, as the change's own
review round caught and a run confirmed.

Alternatives considered and rejected:

- _Requiring the candidate to equal the catalog's version_ — misses the observed case, where the candidate sits below
  it.
- _Rejecting any candidate no newer than the declared version, without requiring the plugin's current to disagree_ — the
  resolver treats the coordinate's own declared version as a rejected candidate, so every up-to-date coordinate leaves
  the up-to-date section.
- _Disabling `checkBuildEnvironmentConstraints`_ — removes the row but also the visibility ADR-0007 valued.
- _A `hold-back.txt` entry_ — the file means "the last version line the project can take", false here, and it would
  suppress genuine same-major candidates.

## Consequences

- The report's "have later release versions" section no longer lists a `log4j-core` row, and its other four rows are
  unchanged; the coordinate itself moves to the informational "Failed to determine the latest version" section for the
  projects whose reported current is the ban floor, which the weekly update issue does not extract.
- `dependencyUpdates` takes substantially longer, because the resolver walks the candidates the rule rejects. The weekly
  update workflow is observational, so the cost is accepted rather than optimized away.
- A catalog-owned coordinate whose resolved version another surface governs below the catalog's is no longer reported —
  the accepted false-negative surface, stated in the spec requirement.
- ADR-0007 is superseded: its Context and its recorded experiments remain the evidence for the reporting path, and its
  attribution is corrected here rather than edited there.
