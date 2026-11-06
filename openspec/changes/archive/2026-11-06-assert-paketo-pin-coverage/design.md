# Design — assert every catalog Paketo pin has a `buildpackChecks` entry

## Context

`buildpackChecks` (`build.gradle.kts`) is a hand-written `List<BuildpackUpdateCheck>`, one entry per Paketo pin,
carrying the check's name, its Docker Hub repository and its pinned version. Two gates read it and nothing else:

- `buildpackUpdates` reports a newer release for each entry;
- `verifyDocumentedPins` is fed `buildpackChecks.associate { it.name to it.pinnedVersion }`, so it verifies exactly the
  versions that list names — and its "a catalog key the pack does not state fails `check`" scenario is computed from the
  same map.

So the list is the corpus's only enumeration of "the catalog's Paketo pins", and a `paketo-*` entry added to
`gradle/libs.versions.toml` without a matching check is outside both gates: no update report row, no documented-pin
requirement, no failure. The repository has been bitten by a hand-maintained pin list before — the `toolingUpdates`
check missed the OpenSpec pin and then the `pack` CLI until each gained an entry, which is why that list is
machine-asserted against the workflow files; `buildpackChecks` has no such assertion, and the catalog is where a pin is
added.

The catalog's version aliases are enumerable through Gradle's `VersionCatalogsExtension` (`versionAliases`), so the
build script can pass them to a rule without parsing the TOML, and the rule selects the `paketo-*` keys from them — the
alias form is `paketo.nginx` for the key `paketo-nginx`.

## Goals / Non-Goals

**Goals**

- Fail `check` when a catalog `paketo-*` pin has no `buildpackChecks` entry, naming the key.
- Fail `check` when an entry names a key the catalog does not hold, naming the entry.
- Keep the rule pure and unit-tested, in the shape `DocumentedPinRules` already takes.

**Non-Goals**

- Deriving `buildpackChecks` from the catalog: an entry also carries the Docker Hub repository, which the catalog does
  not hold and no convention derives.
- Reading the TOML with a hand-written parser: the catalog extension already enumerates the aliases.
- Reporting a newer release for an entry the catalog no longer holds.
- Any change to the documented-pin comparison, the update report, or the pins' versions.

## Decisions

1. **The rule lives in a pure `PaketoPinCoverageRules` object in `build-logic`, beside `DocumentedPinRules`.** The
   comparison is a set difference, and a `build-logic` object's rules are unit-tested in `build-logic/src/test` without
   a Gradle run — the repository's established shape for a rule a task applies. Alternative rejected: the comparison
   inline in `VerifyDocumentedPinsTask`, which no test can reach without a test-kit build the included build does not
   have.
2. **The catalog's keys are read through `VersionCatalogsExtension`, not by parsing `libs.versions.toml`.** The
   extension exposes `versionAliases`, so the build script passes names the catalog itself produced; a hand-written TOML
   parser would be a second reader of a file the build already owns, and the repository's config-read-path rule forbids
   substituting one for a format a tool owns. Alternative rejected: a text scan of the `[versions]` block, which would
   have to re-implement TOML's quoting and comment rules for one prefix.
3. **The comparison normalizes the alias form to the catalog key form.** The extension yields `paketo.nginx` where the
   catalog key and the check name are `paketo-nginx`; the rule maps `-` to `.` (or the reverse) so a check name and a
   catalog key compare as the same pin. Alternative rejected: requiring check names in alias form, which would rename
   every entry and every report row for no gain.
4. **Over-coverage is a failure too.** A check naming a key the catalog no longer holds means the update report is
   querying a pin nobody depends on, and the documented-pin gate would demand a doc state a version the catalog lacks —
   both silent today. Alternative rejected: reporting over-coverage as a warning, which the `check` gate has no
   mechanism for and which an operator would not read.
5. **The coverage runs before the documented-pin comparison, in the same task.** One gate over "the catalog's Paketo
   pins" keeps the failure message in one place, and the coverage result is a precondition for the comparison's meaning.
   Alternative rejected: a separate `verifyPaketoPinCoverage` task, which would run the same inputs through a second
   task and split the failure a reader sees.

## Risks / Trade-offs

- **A new Paketo pin now needs a check entry in the same change.** That is the rule's point, and the failure names the
  key and the fix; a pin the repository does not want tracked would have to be added to the check list with a
  repository, which is the decision the gate asks for.
- **The alias normalization assumes a `paketo-` prefix.** A future Paketo pin named outside that prefix is not covered —
  the rule's set is "the catalog's `paketo-*` keys", as the capability states. The test asserts the normalization so a
  rename that breaks it fails there.
- **The task's inputs grow by one set — the catalog's version alias _names_, from which the rule selects the `paketo-*`
  keys.** The set carries names, not versions, so a version-only bump does not change this input and does not invalidate
  the task; adding, renaming or removing a catalog alias does, which is the edit the coverage exists to catch. The check
  names are the keys of the map the task already takes.

## Verification

```bash
# The rule's own cases, including the uncovered and the unknown-pin arms
./gradlew build-logic:test --tests '*PaketoPinCoverageRulesTests*'

# The gate on the current catalog, which is fully covered
./gradlew verifyDocumentedPins

# The repository's gates
./gradlew check -PskipITs -Pcoverage.gate.enabled=false
```

The rule's own tests decide each arm: a catalog key set with one entry removed from the checks must produce that key in
`uncovered`, and a check naming an absent key must produce it in `unknown` — each failing when the rule stops deciding
its arm. A fixture that adds a pin to the real catalog is deliberately not used there: the tests pass both sets in, so
they never need the catalog's live content.

Those tests cannot show that the build script supplies the **catalog's** set rather than a set derived from the checks —
both would be green against today's matching content. The wiring is settled on a disposable copy of the working tree
(`$TMPDIR/opencode/paketo-wiring`, copied with its uncommitted changes and without `build/` or `.gradle/`):

1. the baseline `./gradlew verifyDocumentedPins` passes on the copy's catalog, which also settles the alias
   representation: a form the rule could not normalize would fail here;
2. a catalog-only `paketo-coverage-probe = "1.0.0"` line inserted **inside the copy's `[versions]` block, before
   `[libraries]`** (the file ends in `[plugins]`, so an appended line would land in the wrong table) makes the gate
   fail, naming the uncovered key — after the catalog has loaded, since a load failure would name the probe without the
   gate deciding anything;
3. with the catalog restored, renaming one `buildpackChecks` entry makes the gate fail naming the **catalog key** it
   left uncovered (an entry naming a pin the catalog lacks reports the other message, reached by adding one while the
   catalog stays covered).

No image build and no TestKit infrastructure is added; the copy needs one `build-logic` compile, which is the control's
cost.

## Claims table

| Claim                                                                    | Source                                                             | Result                                                                                                                          |
| ------------------------------------------------------------------------ | ------------------------------------------------------------------ | ------------------------------------------------------------------------------------------------------------------------------- |
| The two Paketo gates read one hand-written list                          | `build.gradle.kts` — `buildpackUpdates` and `verifyDocumentedPins` | both are fed from `buildpackChecks`; `verifyDocumentedPins` takes `buildpackChecks.associate { it.name to it.pinnedVersion }`   |
| The catalog holds five `paketo-*` keys                                   | `grep -n "^paketo" gradle/libs.versions.toml`                      | `paketo-nginx`, `paketo-procfile`, `paketo-builder-jammy-base`, `paketo-builder-noble-java-tiny`, `paketo-health-checker`       |
| The checks cover exactly those five today                                | the `buildpackChecks` list                                         | five entries, the same five names                                                                                               |
| The extension enumerates version aliases                                 | Gradle's `VersionCatalog` API (`getVersionAliases`)                | the catalog exposes the alias set, so the build script needs no parser                                                          |
| The capability's scenario already covers the document side, not the list | `openspec/specs/showcase/quality/infra-image-versions/spec.md`     | "a catalog key the pack does not state fails `check`" is computed from the check map, so an entry without a check is outside it |

Dismissals, one reason per kind: `path` — every named path is a repository file this change reads, edits, or names in
its own verification; `absolute` — each flagged word is a universal or exclusion statement the change itself makes — its
title's "every catalog Paketo pin has a `buildpackChecks` entry", its "no parser" rejection, and the Context's
"`buildpackChecks` has no such assertion" — so the requirement and the rows state it rather than assert it from
elsewhere; `count` — the flagged figures are the five catalog keys and five check entries the rows report and the plan's
task identifiers, none of them a measured quantity; `cross-ref` — the flagged commands and paths are this change's own
verification instructions.

## Consequences

- A catalog Paketo pin and its check entry move together, or `check` names the one left behind.
- The update report and the documented-pin gate keep reading one list, and that list now provably covers the catalog.
