# Design

## Context

See `proposal.md` — Why. Two declarations format this repository's text: the root Spotless `markdown` and `json`
formats, whose Prettier version comes from the version catalog, and the web module's npm Prettier, whose version its
lockfile resolves. They agreed by coincidence after the `align-the-root-prettier-pin` unit, and nothing keeps them
agreeing. That archived unit declared `skip_specs` and recorded that the `code-quality` spec owns the markdown scope and
the formatter gate but _not the tool's version_ — true of a pin _bump_, which changed no specified behavior. This change
adds one: the agreement is now verified, so the requirement it refines states it, and the delta is owed for that new
behavior rather than for the version.

| Fact                                                                                                                                                                              | Source (re-run)                                                                                                                                                                                                                                      |
| --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| the catalog holds the root's Prettier version, and the root's two formats read it                                                                                                 | `grep -n "^prettier" gradle/libs.versions.toml`; `git show HEAD:build.gradle.kts \| grep -n "prettier(libs.versions.prettier.get())"` (two sites)                                                                                                    |
| the web module's resolved version is the lockfile's                                                                                                                               | `python3 -c "import json;print(json.load(open('showcase-web-ui/package-lock.json'))['packages']['node_modules/prettier']['version'])"` → `3.9.10`                                                                                                    |
| the web module declares a caret, not a version                                                                                                                                    | `git grep -n '"prettier"' -- showcase-web-ui/package.json` → `"prettier": "^3.9.6"`                                                                                                                                                                  |
| the archived pin-alignment unit declared `skip_specs` and recorded that this capability owns the markdown scope and the gate, not the tool's version (the Context reconciliation) | `git show HEAD:openspec/changes/archive/2026-10-15-align-the-root-prettier-pin/.openspec.yaml \| grep -n skip_specs`; `git show HEAD:openspec/changes/archive/2026-10-15-align-the-root-prettier-pin/proposal.md \| grep -n "not the tool"`          |
| no live doc names a Prettier version, and `verifyDocumentedPins` matches only the Paketo pins, so the doc surface is unguarded                                                    | `git grep -in "prettier" HEAD -- '*.md' '*.yaml' '*.yml' \| grep -E "3\.[0-9]" \| grep -v "openspec/changes/archive/"` (empty); `git show HEAD:build-logic/src/main/kotlin/DocumentedPinRules.kt \| grep -n "paketo-buildpacks\|builder-jammy-base"` |
| no update check reads the catalog's entry                                                                                                                                         | `git grep -rln prettier HEAD -- build-logic/src/main/kotlin/` → empty; `toolingUpdates` reads workflow-file pins by pattern, which a catalog entry is not                                                                                            |
| the web module's report watches only its own declaration                                                                                                                          | `./gradlew :showcase-web-ui:npmOutdated`, whose report (an untracked build artifact) lists the module's packages and no catalog entry                                                                                                                |
| the divergence had a consequence: a markdown file was silently ungated                                                                                                            | `git show HEAD:openspec/changes/archive/2026-10-15-make-the-p99-verdict-confirm-first/tasks.md \| grep -n "does not converge"`                                                                                                                       |
| a Prettier version formats differently across the pair                                                                                                                            | the archived unit's Context (the pin at 3.9.6 does not converge) with its claims table (`openspec/changes/archive/2026-10-15-align-the-root-prettier-pin/design.md`, the 3.9.10 half)                                                                |

## Decisions

**Compare against the resolved version, not the declared range.** The lockfile's `node_modules/prettier` version is what
actually formats the web module, so it is what must equal the catalog's; the `^3.9.6` caret is npm's input and may
legitimately trail the resolution. Both files are committed, so the check reads text and resolves nothing.

**The rules live in `build-logic`, pure, with their cases in a `*RulesTests`.** That is the shape every other check here
takes, and the check-control rule requires the known-bad and known-good cases to be committed tests: the drift this unit
exists for (the 3.9.6-against-3.9.10 pair), a missing entry on either side, an unreadable lockfile, and the two parse
boundaries a naive read would cross — a key the pin's name prefixes (`prettier-plugin`) and one it merely ends
(`my-prettier`), each excluded by a different part of the pattern.

**A missing entry is a failure, not a skip.** If either side stops recording Prettier, the agreement is unverifiable and
the check would otherwise pass by finding nothing — the vacuous-match failure mode, so both sides are required to be
present.

**In `check`, not a scheduled run.** A drift is caused by a pull request that moves one declaration, so the pull request
can remediate it; the repository's rule for a check's home is exactly this distinction.

## Risks / Trade-offs

- The check compares two of the three surfaces that name a Prettier version (the catalog, the lockfile, and the web
  module's caret) → the caret is deliberately out of scope: it is a range, and the lockfile is its resolution. No live
  doc names a Prettier version at all, and `verifyDocumentedPins` matches only the Paketo pins, so a doc that started
  naming one would be caught by neither check — a gap this change does not close.
- The check pins agreement, not currency: a new Prettier release is still surfaced by the web module's npm report, and
  the catalog entry is still audited by hand.
- A future decision to let the two surfaces use different Prettier versions on purpose would falsify this check → that
  decision would carry its own change and retire this one, which is the right place for it.

## Migration Plan

None: a verification joining `check`.
