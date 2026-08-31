## Context

See `proposal.md` — the new advisory `SNYK-JAVA-ORGAPACHEHTTPCOMPONENTSCLIENT5-19432178` (affects
`httpclient5` < 5.6.4) only reaches the build via the checkstyle tool chain `checkstyle@10.26.1 ->
Saxon-HE@12.5 -> xmlresolver@5.2.2 -> httpclient5@5.1.3`; the app runtime is already patched (catalog pins
`httpclient5` at 5.6.4). This change upgrades the checkstyle tool to 13.11.0 to remove that chain at the source.

Constraint: checkstyle is applied to every module via `code-check-conventions` (Gradle built-in `checkstyle` plugin,
`toolVersion` from the catalog, `maxErrors = 0`, ruleset at `config/checkstyle/checkstyle.xml`). The Gradle wrapper is
9.7.1 (built-in checkstyle plugin). No module pins checkstyle directly; the version is a single catalog ref.

## Goals / Non-Goals

**Goals:**
- Eliminate the `httpclient5@5.1.3` / `httpcore5-h2@5.1.3` build-tool nodes from the dependency graph so the weekly
  Snyk scan stops failing on them — without adding new `.snyk` ignores.
- Keep the checkstyle style gate green under the upgraded tool.

**Non-Goals:**
- Not a review or rework of the `config/checkstyle/checkstyle.xml` ruleset beyond what the upgrade requires to stay
  green.
- Not touching the Spring cluster `.snyk` ignores (those remain until the deferred Spring Boot 4 migration, ADR-0004).
- Not changing how `snyk test --all-sub-projects` is invoked (that is spec'd behavior and stays as-is).

## Decisions

### Decision: Upgrade checkstyle 10.26.1 -> 13.11.0 instead of adding another ignore

Traced the upstream trees to confirm the fix is real, and verified the final version empirically:

```
checkstyle 10.26.1 -> Saxon-HE 12.5 -> xmlresolver 5.2.2 -> httpclient5 5.1.3     (vulnerable, critical)
checkstyle 11.0.0  -> Saxon-HE 12.8 -> xmlresolver 5.3.3                          (httpclient5 gone, but
                     + doxia-core 1.12.0 -> httpclient 4.5.13 -> commons-codec 1.11  leaves a low finding)
checkstyle 13.11.0 -> Saxon-HE 12.9 -> xmlresolver 5.3.3                          (empty subtree - clean)
```

`xmlresolver` 5.3.3 removed the `httpclient5` dependency entirely, so the whole vulnerable sub-tree disappears;
11.0.0 was a necessary stepping stone that still left `commons-codec@1.11` (via `doxia-core -> httpclient 4`), and
13.11.0 (Snyk's suggested fix) drops the doxia/httpclient path as well. Verified outcomes under 13.11.0: the
`checkstyle` configuration resolves to just `checkstyle -> Saxon-HE -> xmlresolver`, `checkstyleMain`/`checkstyleTest`
pass on every module, and `snyk test --all-sub-projects` reports no vulnerable paths. This obsoletes all six existing
tooling `.snyk` ignores (`httpclient5@5.1.3`, the three `httpcore5-h2@5.1.3` entries, `plexus-utils@3.3.0`,
`commons-lang3@3.8.1`), which will be pruned so the policy reflects reality.

Alternatives considered:
- **Add a `.snyk` ignore for the new advisory** — rejected as the recurring pattern: this is the second advisory on
  `httpclient5@5.1.3` and `httpcore5-h2@5.1.3` already carries three ignored IDs. Every new vuln ID on the same pinned
  build-tool version flips the weekly scan red again and grows the ignore list by hand.
- **Force `httpclient5` >= 5.6.4 into the checkstyle configuration** — rejected: it overrides xmlresolver 5.2.2's
  declared tree (a policy the `.snyk` comment already documents as deliberately avoided) and would not remove the
  `httpcore5-h2` occurrences. Upgrading checkstyle fixes both packages at once.
- **Stop at 11.0.0** — considered and rejected once the scan showed it trades the critical `httpclient5` finding for a
  low-severity `commons-codec@1.11` one, which would reintroduce a `.snyk` ignore and leave the ignore-list pattern
  intact.

### Decision: Prune the now-dead `.snyk` ignores rather than leave stale entries

After the upgrade the `httpclient5@5.1.3`, `httpcore5-h2@5.1.3`, `plexus-utils@3.3.0`, and `commons-lang3@3.8.1`
nodes no longer exist in the graph, so their six ignores are dead policy. Remove them and the obsolete tooling
comment; the policy then holds only the Spring cluster entries (which remain until the deferred Spring Boot 4
migration, ADR-0004). Keep the quarterly expiry pattern for anything that remains.

### Decision: Update the `dependency-security` spec floor to 5.6.4

The requirement's "at least `5.6.3`" floor is now below the fix version of the active advisory and below what the
catalog already resolves to (5.6.4). The normative floor becomes 5.6.4 to match reality and keep the scenario
meaningful for future scans.

## Risks / Trade-offs

- **[Checkstyle 13 is a major release]** rule behavior, defaults, or check names may differ from 10.x, and the
  `maxErrors = 0` gate applies across all modules. → Mitigation: `checkstyleMain`/`checkstyleTest` verified green on
  every module under 13.11.0 with the existing ruleset; if a future 13.x patch changes behavior, re-run the gate.
- **[Gradle's built-in checkstyle plugin vs tool 13.x]** the bundled plugin officially supports 8.x/10.x-era tools;
  13.x runs as a standalone Ant-based task. → Mitigation: verified the tool executes under Gradle 9.7.1
  (`checkstyleMain` ran the 13.11.0 jar successfully).
- **[xmlresolver 5.3.3 drops the HTTP catalog transport]** checkstyle's Saxon/XPath features that need remote catalog
  resolution could behave differently. → Mitigation: verified `checkstyleMain` output is unchanged on all modules; the
  ruleset uses local suppressions only.
- **[Residual risk of a future advisory on the new tree]** — Mitigation: the resolved tree is now
  `checkstyle -> Saxon-HE -> xmlresolver` (nothing else), and the scan is clean; any new advisory on that slimmer
  tree is much less likely and would still be caught by the weekly scan.

## Migration Plan

No runtime or deployable change — the checkstyle tool runs only inside the build.

1. Bump the catalog version and verify `checkstyleMain`/`checkstyleTest` on one module
   (e.g. `showcase-command-service`).
2. Verify across all modules that apply `code-check-conventions`.
3. Prune the dead `.snyk` ignores and remove the obsolete comment.
4. Run `./gradlew dependencySecurityCheck` (Snyk CLI on PATH) to confirm the scan is clean without the removed ignores.
5. Re-run `./gradlew :showcase-command-service:check -PskipITs` to confirm the style gates and quality checks pass.

Rollback: revert the single catalog version ref; `.snyk` pruning is a separate commit so it can be reverted
independently.

## Open Questions

None — all unknowns that could change the approach (upgrade viability, ruleset compatibility) are verified as part of
the tasks rather than deferred.