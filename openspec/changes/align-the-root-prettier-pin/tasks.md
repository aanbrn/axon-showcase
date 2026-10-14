# Tasks

## 0. The pre-write derivation pass (this change carries its own claims table)

- [x] 0.1 Run the pre-write derivation pass and record its output as the claims table in `design.md`'s Context — every
      claim it states about the pins, the skip and the reflow, each with the command that derives it.

## 1. The pin

- [x] 1.1 Add the `prettier` version to `gradle/libs.versions.toml` as a `[versions]`-only entry holding the version the
      web module resolves, and confirm the resolved version is still npm's latest before recording it.
- [x] 1.2 Make both `prettier("3.9.6")` sites in `build.gradle.kts` read that entry, so the root's two formats and the
      web module's range are aligned to the version the web module resolves.
- [x] 1.3 Run `./gradlew spotlessApply` and confirm the working tree shows **no diff beyond this change's own edits** —
      the alignment's risk is a tree-wide reflow, so the tree is the check.

## 2. The upstream report

- [x] 2.1 Resolve the owning repository from the plugin's own metadata and search its tracker for an existing report of
      the same gap. Outcome: `diffplug/spotless` (the plugin POM's `url`), and the search returns no report of _this_
      gap — #1689 shares the non-convergence mechanism but asks for a different thing (that ktlint converge, not that a
      skip be visible), so it is cited as related rather than as a duplicate.

- [x] 2.2 Draft the report at `$TMPDIR/opencode/spotless-skip-issue.md`: the skip and its zero exit, why a green check
      is not evidence for that file, the reproduction (Prettier 3.9.6, the construct, the message, exit 0), and the ask
      (a `failOnSkip`-style option, or a non-zero exit when a target file was not checked).

- [x] 2.3 Post it through the review gate, and record the reference where the closure report scans. Outcome: posted as
      `diffplug/spotless#3143`, and cited with its close-out in `AGENTS.md`'s skip rule — a file the
      `upstreamReferences` report reads — which states the retiring mechanism. The close-out is stated once there rather
      than restated here.

## 3. Docs the change owns

- [x] 3.1 Remove the parked entry in `docs/ideas.md` this change implements.
- [x] 3.2 Sweep the docs for the pin's old value and confirm each site (only the parked entry named it).

## 4. Verification

- [x] 4.1 Re-run the claims-table rows with their sources and confirm each reproduces. The loop re-derived the table
      twice (a malformed row, a vacuous `javap` target, an undriven count), and every row now verifies against a known
      positive.
- [x] 4.2 Run `./gradlew spotlessApply` then `spotlessCheck`, `openspec validate --all`, and the manual 120-character
      check over the changed set — green, with no reflow beyond this unit's edits.
- [x] 4.3 Run a `review-quick` pass over the implementation, passing the prior round's instances.
- [x] 4.4 Run the per-unit `lesson-capture` and record the applied net `AGENTS.md` delta. Outcome: **nothing durable**
      (0 additions, 0 retirements; net 0 from the capture) — every class the eight rounds raised is a compliance miss of
      a rule the file already carries: the batched-edit write-once mode, the known-positive rule for an absence claim,
      the undriven count, single-sourcing a claim, and a close-out naming the exact retiring mechanism. The file's own
      +2 lines are this unit's prescribed upstream citation in the skip rule.

- [ ] 4.5 Request the user's manual review pass.
