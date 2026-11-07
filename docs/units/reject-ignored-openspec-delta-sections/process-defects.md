# Process defects — reject-ignored-openspec-delta-sections

Process defects observed while working this unit. Recorded under `AGENTS.md`'s "Workflow: Record process defects".

### reject-ignored-openspec-delta-sections#1 — A review finding was patched at the named sentence, not swept across the artifact set

- **Recorded** — 2026-11-04, by the writing session, on round 5's report.
- **Stage** — proposal (the review loop).
- **Expected** — a correction to a claim the artifacts state in several places sweeps every site, as `AGENTS.md`'s
  span-replace rule requires; the reviewer names one instance, not the set.
- **Observed** — `contradiction` was raised in rounds 2, 3 and 5 and `claim-missing` in rounds 2 and 5, each time in a
  passage adjacent to the one the previous fix edited: round 2 fixed a half-replaced clause, round 3 found two proposal
  bullets left behind, round 5 found the Consequences line beside the newly added decision.
- **Evidence** — round 2's `contradiction` and `claim-missing`, round 3's `contradiction`, round 5's `contradiction` in
  `docs/units/reject-ignored-openspec-delta-sections/findings/`; the reviewer's round-5 root-cause paragraph names the
  pattern.
- **Consequence** — two extra proposal rounds; the sweep was run once, at round 5, over the four artifacts.
- **Knowledge boundary** — observed this unit's round sequence and the passages each finding named. Did not check
  whether the same patch-at-the-named-line pattern drove the repeated classes in the two units before this one.
- **Decision** — no-change: already covered by `AGENTS.md`'s span-edit rule — a replacement preserves every valid
  obligation, verified by sweeping the removed text's distinctive tokens.

### reject-ignored-openspec-delta-sections#2 — The build edit carried no `gradle-build` pack

- **Recorded** — 2026-11-04, by the writing session, on round 7's missing-input stop.
- **Stage** — implementation.
- **Expected** — a change that edits `build.gradle.kts` loads `gradle-build`, whose subject table row covers "changing a
  Gradle build decision" and the `check` gate's composition, before the first decision in that subject; every hand-over
  names it.
- **Observed** — the change registered `testOpenSpecDeltaGuard` and added it to `check`'s `dependsOn` while every
  hand-over from round 1 to round 7 declared `Packs: none`; the round-7 reviewer stopped on the missing pack rather than
  screening the build edit.
- **Evidence** — round 7's report ("Missing input — the `gradle-build` pack"); the hand-over's `Packs: none` lines; the
  `build.gradle.kts` hunk.
- **Consequence** — one stopped invocation and one re-run with the pack named; the build edit was then checked against
  the pack's task-registration rules and conforms (the `testCommitHygiene` shape, `outputs.upToDateWhen { false }` for a
  verdict task).
- **Knowledge boundary** — observed this unit's classification and the stop. Did not check whether earlier units that
  edited build scripts carried the pack.
- **Decision** — proposed: applied as P1 in `AGENTS.md`'s subject-pack section — the caller checks a change's planned or
  actual edited paths for a subject its decisions did not name.
