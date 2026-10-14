# Design

## Context

See `proposal.md` — Why. The root `build.gradle.kts` pins Prettier twice (`format("markdown")` and `format("json")`)
with an exact version; the web module declares `"prettier": "^3.9.6"` in its `package.json`, which its lockfile resolves
to 3.9.10. The two targets format different file sets (the root's markdown and `.opencode/opencode.json`; the web
module's JS/TS/CSS/HTML through npm), so the divergence is not a principle but a coincidence of an exact pin beside a
caret — and it is the exact pin that fails to converge on a construct the repository writes.

Probed before designing, each fact with the source that derives it:

| Fact                                                                                                                                       | Source (re-run)                                                                                                                                                                                                                                                                                                                                                   |
| ------------------------------------------------------------------------------------------------------------------------------------------ | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| the whole markdown target converges today, so the hole is latent rather than live                                                          | `./gradlew spotlessMarkdownCheck --rerun-tasks 2>&1 \| grep -i "does not converge"` (empty)                                                                                                                                                                                                                                                                       |
| a non-converging file is **skipped with a zero exit**, so the check reports success for a file it did not check                            | reproduced against the pre-change pin (`git show HEAD:build.gradle.kts \| grep -n prettier`): a probe file whose fenced block sits six spaces under a checklist item makes `./gradlew spotlessMarkdownCheck --rerun-tasks` print `Skipping 'docs/zz-convergence-probe.md' because it does not converge.` and exit 0 — the same file converges under 3.9.10        |
| the skip hit this repository's own change                                                                                                  | `git show HEAD:openspec/changes/archive/2026-10-15-make-the-p99-verdict-confirm-first/tasks.md \| grep -n "does not converge"`                                                                                                                                                                                                                                    |
| Spotless 8.10.4 exposes no fail-on-skip option                                                                                             | the class derived from the jar (`unzip -l <spotless-plugin-gradle-8.10.4.jar> \| grep -oE "com/diffplug/gradle/spotless/SpotlessExtension\.class"`), then `javap -classpath <jar> com.diffplug.gradle.spotless.SpotlessExtension \| grep -icE "skip\|converge"` → `0` — an option is a member, so a populated class with no such member is what shows its absence |
| `SkipFilesNamed` is a target filter in the library, not an option                                                                          | `unzip -l <spotless-lib-4.10.4.jar> \| grep SkipFilesNamed` → `SerializableFileFilterImpl$SkipFilesNamed.class`                                                                                                                                                                                                                                                   |
| the `[versions]`-only entry is read by no update check                                                                                     | `grep -rln prettier build-logic/src/main/kotlin/` (empty)                                                                                                                                                                                                                                                                                                         |
| Prettier 3.9.10 reproduces the committed formatting of gated markdown                                                                      | `cd showcase-web-ui && for f in ../AGENTS.md ../README.md ../docs/ideas.md ../openspec/specs/showcase/quality/load-tests/spec.md; do npx --no-install prettier --prose-wrap always --print-width 120 "$f" \| diff -q - "$f"; done` (no differences)                                                                                                               |
| 3.9.10 is both what the web module resolves and npm's latest                                                                               | `python3 -c "import json;print(json.load(open('showcase-web-ui/package-lock.json'))['packages']['node_modules/prettier']['version'])"`; `npm view prettier version` (both 3.9.10)                                                                                                                                                                                 |
| the pin's pre-change sites are four live files: the root script twice, the web module's declaration and its lockfile, and the parked entry | `git grep -n "3\.9\.6" HEAD -- ':!openspec/changes/archive'`                                                                                                                                                                                                                                                                                                      |

## Goals / Non-Goals

**Goals:**

- Move the pin to the version that converges on what the repository writes, and to the catalog, where the convention
  places build-tool versions (a `[versions]`-only entry, like `node` and `java`).
- Verify the alignment by the tree: no reflow beyond this change's edits.

**Non-Goals:**

- The web module's `^3.9.6` caret: it resolves 3.9.10 already, and tightening it is explicitness, not behaviour.
- A check that fails on a skip: no option to key on, so it would parse the tool's own output for a latent subject — the
  fragility the repository's conventions warn about. That gap is reported upstream instead.
- The parked idea's other half — pinning one Prettier "for both" — is what this change does at the root; the web
  module's range already lands on the same version, so there is nothing to pin there.

## Decisions

**Align to 3.9.10, in the catalog.** The catalog is where a build-tool version lives here, and the version is
unambiguous (the web module's resolution and npm's latest agree). The `[versions]`-only entry carries no library module,
matching how the repository records versions that are not `group:name` dependencies.

**The gate hole goes upstream, not into a check.** A skipped file is indistinguishable from a formatted one in
Spotless's output and exit status, and the repository cannot key on an option that does not exist; a wrapper that parsed
the tool's output would be a check whose subject is latent and whose failure mode is a false negative. Reporting it
where the defect is follows the repository's rule for a gap in a tool it relies on, and the captured convention — a skip
is a failure for that file, not a pass — covers the human side until the upstream answer lands.

## Risks / Trade-offs

- A Prettier bump can reflow a tree (a plugin bump reflowed thirteen Java test sources here once) → measured: 3.9.10
  reproduces the committed formatting on a sample, and `spotlessApply` plus the working tree is the full check; a reflow
  would be visible as a diff beyond this change's edits and would be taken deliberately, not silently.
- The `[versions]`-only entry is read by no update check (as the hard-coded pin was not) → unchanged exposure, and the
  convention records that such entries are audited by hand.
- The upstream report may be declined → the alignment stands on its own: it removes the known trigger regardless of what
  the maintainers decide about skips.

## Migration Plan

None: a formatter version, taking effect on the next `spotlessApply`/`spotlessCheck`.
