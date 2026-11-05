# Review hand-over — pin-wrapper-distribution-checksum

## Round 1 — proposal screen

- **Unit, tree, branch, base** — `pin-wrapper-distribution-checksum`; tree `/Users/afanasyev/Development/axon-showcase`
  (the primary checkout, this unit's tree); branch of the same name; base `origin/main` `ad78b101`; nothing committed,
  every unit file staged.
- **Packs** — `gradle-build`, loaded in full: the change decides a build gate, a task registration, and the wrapper's
  update procedure.
- **Supplied artifacts** — `proposal.md`, `design.md` (Context, Goals/Non-Goals, five decisions with alternatives,
  risks, verification, the claims table and its dismissals), `tasks.md`, `.openspec.yaml` (`skip_specs`), the unit
  record.
- **The proposed change** — the wrapper pins `distributionSha256Sum`; a `verifyWrapperChecksum` gate joins `check`; a
  new `scripts/update-gradle-wrapper.py` performs the bump with the checksum and restores the four files on failure,
  with `/gradle-update` as its trigger; the tool and the gate each carry a `test…` task in `check`.
- **Prior findings** — none; this is the unit's first round.
- **Completed checks** — `openspec validate pin-wrapper-distribution-checksum --strict` reports the `skip_specs`
  declaration; `spotlessApply` has run; the staged tree passes `commit-hygiene --review-ready`; the claims table's
  sources were re-run while the proposal was drafted.
- **Known limitations** — the tool's behaviour is verified by its fixture-driven tests and one live rehearsal; the gate
  can only assert the checksum's presence and shape.

## Round 2 — proposal screen, after round 1's twelve findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `ad78b101`; every unit file staged.
- **Packs** — `gradle-build`.
- **Supplied artifacts** — round 1's fixes, each re-derived rather than patched: the claims row escapes its pipes and
  `verifyTableRows` passes; `skip_specs` is gone and the change carries an `ADDED` requirement in
  `showcase/quality/commit-hygiene` beside the executable-bit guard; the tool no longer refuses a dirty tree, because
  the snapshot restores the bytes it captured (and refusing would block this unit's own rehearsal); the mismatch probe
  runs the wrapper against a zeroed checksum with an empty `GRADLE_USER_HOME` under `$TMPDIR/opencode`, and says why a
  cached distribution never re-validates; the gate asserts presence, 64-hex shape and `-all.zip` only, since nothing
  owns the version but the file itself; the command passes the candidate version and the tool resolves "latest stable"
  only without one; the checksum's guarantee is scoped to every download after the pin, with the pin-time trust stated;
  the gate is `scripts/verify-wrapper-checksum.py` with its own suite in the `Exec`-plus-script shape of its siblings;
  Gradle's `gradle-wrapper-upgrade` skill is weighed and rejected with its reason; Impact names all three gained tasks
  and both scripts; task 0.1 is ticked; the fenced line is wrapped.
- **Prior findings with their instances** — round 1's twelve, in `findings/round-1.md`.
- **Completed checks** — `openspec validate pin-wrapper-distribution-checksum --strict` passes with the delta;
  `verifyTableRows` passes; `spotlessApply` has run; `commit-hygiene --review-ready` passes.
- **Known limitations** — the tool's restore paths are verified by fixtures and one live rehearsal; the gate can only
  assert the pin's presence and shape.

## Round 3 — proposal screen, after round 2's three findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `ad78b101`; every unit file staged.
- **Packs** — `gradle-build`.
- **Supplied artifacts** — round 2's fixes: the mismatch probe now swaps the properties file itself, runs
  `GRADLE_USER_HOME="$SCRATCH" ./gradlew --version` against a zeroed checksum expecting a refusal, restores the original
  byte-for-byte, and re-runs expecting acceptance; task 3.3 refreshes `showcase/quality/commit-hygiene`'s `## Purpose`
  in the archive commit, since a delta cannot carry one; and the claims row states the grep's eleven hits as what they
  are — the executable bit, the update report's currency clause, and three unrelated uses of the word, none naming a
  checksum.
- **Prior findings with their instances** — rounds 1–2 in `findings/`.
- **Completed checks** — `openspec validate pin-wrapper-distribution-checksum --strict` passes; `verifyTableRows`
  passes; `spotlessApply` has run; `commit-hygiene --review-ready` passes.
- **Known limitations** — the tool's restore paths are verified by fixtures and one live rehearsal; the gate asserts the
  pin's presence and shape only.

## Round 4 — proposal screen, after round 3's two findings and the advisor's pass

- **Unit, tree, branch, base** — unchanged; base `origin/main` `ad78b101`; every unit file staged.
- **Packs** — `gradle-build`.
- **Supplied artifacts** — round 3's fixes (the claims row now landed and describes the eleven hits as they are; the
  probe copies into `$TMPDIR/opencode` and leaves no `.bak`), and the advisor's pass, whose findings are applied: the
  tool requires `--version` (the command owns the candidate, one source per run), the command's completion criterion
  changes with the pin (a bump completes when the second generation ran under the target and the URL and checksum agree
  — not when all four artifacts differ), the `gradle-wrapper-upgrade` skill is recorded as _not assessed_ rather than as
  rejected for an unverified failure path, the rollback contract names its boundary and the snapshot it prints, and the
  gate's verdict is described as pin-configuration validation.
- **Prior findings with their instances** — rounds 1–3 and the advisor's pass, in `findings/`.
- **Completed checks** — `openspec validate pin-wrapper-distribution-checksum --strict` passes; `verifyTableRows`
  passes; `spotlessApply` has run; `commit-hygiene --review-ready` passes.
- **Known limitations** — the tool's restore paths are verified by fixtures and one live rehearsal; the gate asserts the
  pin's configuration, not that its value names the archive.

## Round 5 — proposal screen, after round 4's four findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `ad78b101`; every unit file staged.
- **Packs** — `gradle-build`.
- **Supplied artifacts** — round 4's fixes: task 3.2's scope and verify now cover the pack's four-file-diff criterion,
  so the superseded completion rule is corrected where it lives rather than only in the command; the rollback contract
  says "bytes and executable modes" in the proposal, the decision and the consequence; the pre-write note no longer
  states a row count; and the Goal justifies the gate by what it alone catches (a hand edit, or a pin never added)
  instead of borrowing Gradle's own refusal at bump time.
- **Prior findings with their instances** — rounds 1–4 and the advisor's pass, in `findings/`.
- **Completed checks** — `openspec validate pin-wrapper-distribution-checksum --strict` passes; `verifyTableRows`
  passes; `spotlessApply` has run; `commit-hygiene --review-ready` passes.
- **Known limitations** — the tool's restore paths are verified by fixtures and one live rehearsal; the gate validates
  the pin's configuration, not that its value names the archive.

## Round 6 — implementation screen, first pass

- **Unit, tree, branch, base** — unchanged; base `origin/main` `ad78b101`; every unit file staged.
- **Packs** — `gradle-build`.
- **Supplied artifacts** — the implementation: `distributionSha256Sum` pinned for 9.8.2;
  `scripts/verify-wrapper-checksum.py` and its eight-case suite; `scripts/update-gradle-wrapper.py` (required
  `--version`, checksum fetch, byte-and-mode capture, two passes with the checksum, verification, restore) and its
  seven-case suite; the three `Exec` tasks wired into `check`; `/gradle-update` and the `gradle-build` pack rewritten to
  point at the tool and to drop the four-file-diff criterion; the ideas entry removed.
- **Evidence** — a real bump 9.8.2 → 9.8.1 and back through the tool, with `./gradlew --version` reporting each and the
  round trip leaving only the checksum line added against `HEAD`; the refusal probe (a zeroed checksum against a tiny
  local archive under an empty `GRADLE_USER_HOME`) reporting "Verification of Gradle distribution failed!"; `check`
  green with the three tasks; `openspec validate --all` 27.
- **Prior findings with their instances** — rounds 1–5 and the advisor's pass, in `findings/`.
- **Completed checks** — `check -PskipITs -Pcoverage.gate.enabled=false` green (1 m 19 s); `spotlessCheck`,
  `verifyTableRows`, `verifyIdeasSections` green.
- **Known limitations** — task 3.3 (the capability Purpose) lands in the archive commit; the accepting half of the
  checksum's evidence is CI's cold-cache bootstrap, not a local run.

## Round 7 — implementation screen, after round 6's nine findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `ad78b101`; every unit file staged.
- **Packs** — `gradle-build`.
- **Supplied artifacts** — round 6's fixes: the snapshot is a `mkdtemp` directory removed only on success, and a
  `BaseException` handler restores the four files on an interruption, so a killed or interrupted run leaves the printed
  snapshot as recovery material; the ideas entry's stale sentence names the tool instead of the two-run procedure; the
  fixture server answers only the `-all.zip.sha256` path and a wrong-path case asserts the refusal; the empty-body and
  same-target cases exist; the mode assertion now chmods a file the failing pass leaves alone, so it fails if the
  restore drops the mode; the byte-identical-jar claim has a claims row sourced to the round trip; the check accepts
  upper- and lower-case hex, matching the requirement's wording; task 4.2 is ticked; the nine over-long Python lines are
  wrapped.
- **Prior findings with their instances** — rounds 1–6 and the advisor's pass, in `findings/`.
- **Completed checks** — the two suites (10 and 8 cases) green; `check -PskipITs -Pcoverage.gate.enabled=false` green (1
  m 29 s) with the three wrapper tasks; `spotlessCheck`, `verifyTableRows`, `verifyIdeasSections` green.
- **Known limitations** — task 3.3 (the capability Purpose) lands in the archive commit; the accepting half of the
  checksum's evidence is CI's cold-cache bootstrap.

## Round 8 — implementation screen, after round 7's four findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `ad78b101`; every unit file staged.
- **Packs** — `gradle-build`.
- **Supplied artifacts** — round 7's fixes, each proved by a mutation probe rather than by reading: the failing fixture
  now chmods the jar during its run and the assertion pins the captured `0o750`, which the restore returns through
  `shutil.copy2` (the redundant `chmod` and its imports are gone, and disabling `restore` fails the suite); an
  interruption case patches `subprocess.run` to rewrite the properties and raise `KeyboardInterrupt`, asserting the
  bytes return and the printed snapshot directory survives (disabling the `BaseException` handler fails it); a case with
  no `--checksum-url` patches `urlopen` and asserts the production URL is the `-all.zip.sha256` one (mutating the
  constant fails it); and an uppercase-checksum case pinning the widened acceptance (reverting the pattern fails it).
- **Prior findings with their instances** — rounds 1–7 and the advisor's pass, in `findings/`.
- **Completed checks** — the suites at 12 and 9 cases; `check -PskipITs -Pcoverage.gate.enabled=false` green (1 m 12 s);
  `spotlessCheck`, `verifyTableRows`, `verifyIdeasSections`; every new Python line within 120 characters.
- **Known limitations** — task 3.3 (the capability Purpose) lands in the archive commit; the accepting half of the
  checksum's evidence is CI's cold-cache bootstrap.

## Round 9 — implementation screen, after round 8's two findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `ad78b101`; every unit file staged.
- **Packs** — `gradle-build`.
- **Supplied artifacts** — round 8's fixes: both Done notes state the arms their suites decide instead of a tally, which
  had drifted 8→9 and 7→12 across the last two rounds; and the unused `import os` and the two dead `REPO` constants are
  deleted, so nothing in the suites is unread.
- **Prior findings with their instances** — rounds 1–8 and the advisor's pass, in `findings/`.
- **Completed checks** — the suites at 12 and 9 cases green; `check -PskipITs -Pcoverage.gate.enabled=false` green (1 m
  25 s) with the three wrapper tasks; `spotlessCheck`, `verifyTableRows`, `verifyIdeasSections` green.
- **Known limitations** — task 3.3 (the capability Purpose) lands in the archive commit; the accepting half of the
  checksum's evidence is CI's cold-cache bootstrap.

## Round 11 — implementation screen, after the deep review's two nitpicks

- **Unit, tree, branch, base** — unchanged; base `origin/main` `ad78b101`; every unit file staged.
- **Packs** — `gradle-build`.
- **Supplied artifacts** — the deep review's two nitpicks, both applied: the unit record's stale proposal-stage
  pending-decision line is gone, leaving the manual pass as the one outstanding decision; and the tool removes its
  snapshot on each handled-failure return, since the restore has already run there, keeping the directory only where the
  restore could not finish (the interruption path).
- **Prior findings with their instances** — rounds 1–10 (round 10 being the deep pass) and the advisor's pass, in
  `findings/`.
- **Completed checks** — the suites at 12 and 9 cases; `check -PskipITs -Pcoverage.gate.enabled=false` green; the deep
  pass's re-checks of the transaction, the gate, the delta and the prose all sound.
- **Known limitations** — task 3.3 (the capability Purpose) lands in the archive commit; the accepting half of the
  checksum's evidence is CI's cold-cache bootstrap.

## Round 12 — implementation screen, after round 11's three record findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `ad78b101`; every unit file staged.
- **Packs** — `gradle-build`.
- **Supplied artifacts** — round 11's fixes: the unit record's Next step is the owner's manual pass, its Change set
  names the four scripts it stages, and the routing command's inline code span is on one source line with no whitespace
  run.
- **Prior findings with their instances** — rounds 1–11 (round 10 being the deep pass) and the advisor's pass, in
  `findings/`.
- **Completed checks** — the suites at 12 and 9 cases; `check -PskipITs -Pcoverage.gate.enabled=false` green; the deep
  pass's substance verified in rounds 10–11.
- **Known limitations** — task 3.3 (the capability Purpose) lands in the archive commit; the accepting half of the
  checksum's evidence is CI's cold-cache bootstrap.

## Round 13 — implementation screen, after round 12's finding

- **Unit, tree, branch, base** — unchanged; base `origin/main` `ad78b101`; every unit file staged.
- **Packs** — `gradle-build`.
- **Supplied artifacts** — round 12's fix: the recorded routing command runs as written again (the script is not
  executable, so `python3` is part of the command), and the Done note names the tool and the outcome without a
  line-broken code span.
- **Prior findings with their instances** — rounds 1–12 (round 10 being the deep pass) and the advisor's pass, in
  `findings/`.
- **Completed checks** — the suites at 12 and 9 cases; `check -PskipITs -Pcoverage.gate.enabled=false` green (1 m 20 s);
  the deep pass's substance verified in rounds 10–11.
- **Known limitations** — task 3.3 (the capability Purpose) lands in the archive commit; the accepting half of the
  checksum's evidence is CI's cold-cache bootstrap.

## Round 15 — implementation screen, after the advisor's agreement and round 14

- **Unit, tree, branch, base** — unchanged; base `origin/main` `ad78b101`; every unit file staged.
- **Packs** — `gradle-build`.
- **Supplied artifacts** — round 14's six fixes: the design's consequence and risk bullet now state the three outcomes
  and separate a failed restore from a killed process that writes no report; the pre-write paths print "No wrapper file
  was touched." and the command names each cause; the interruption test is named for what it asserts; the duplicated
  cold-cache sentence is gone; and a capture-failure case asserts no snapshot announcement and no leftover directory —
  the last with a control that fails when the announcement moves back above the capture.
- **Prior findings with their instances** — rounds 1–14 (round 10 being the deep pass) and the two advisor passes, in
  `findings/`.
- **Completed checks** — the suites at 14 and 9 cases; `check -PskipITs -Pcoverage.gate.enabled=false` green (1 m 14 s);
  `spotlessCheck`, `verifyTableRows`, `verifyIdeasSections`; every edited line within 120 characters.
- **Known limitations** — task 3.3 (the capability Purpose) lands in the archive commit; acceptance of the pinned 9.8.2
  archive is a cold-cache CI run not yet observed.

## Round 16 — implementation screen, after round 15's two findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `ad78b101`; every unit file staged.
- **Packs** — `gradle-build`.
- **Supplied artifacts** — round 15's fixes, applied at the lines the findings named: the Consequences bullet states the
  three-branch outcome instead of "leaves the four files as it found them", and task 2.3's Done note points at the
  suite's own enumeration rather than freezing a list that had drifted twice.
- **Prior findings with their instances** — rounds 1–15 (round 10 being the deep pass) and the two advisor passes, in
  `findings/`.
- **Completed checks** — the suites at 14 and 9 cases; `check -PskipITs -Pcoverage.gate.enabled=false` green;
  `spotlessCheck`, `verifyTableRows`, `verifyIdeasSections`.
- **Known limitations** — task 3.3 (the capability Purpose) lands in the archive commit; acceptance of the pinned 9.8.2
  archive is a cold-cache CI run not yet observed.

## Round 17 — implementation screen, after round 16's two findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `ad78b101`; every unit file staged.
- **Packs** — `gradle-build`.
- **Supplied artifacts** — round 16's fix, in the shape it asked for: the Consequences bullet no longer restates the
  outcome model branch by branch; it points at the risk section's outcomes — restored, an incomplete restore whose
  snapshot holds the bytes, or a killed process whose snapshot is its only report — so the summary and the model cannot
  drift apart again.
- **Prior findings with their instances** — rounds 1–16 (round 10 being the deep pass) and the two advisor passes, in
  `findings/`.
- **Completed checks** — the suites at 14 and 9 cases; `check -PskipITs -Pcoverage.gate.enabled=false` green;
  `spotlessCheck`, `verifyTableRows`, `verifyIdeasSections`.
- **Known limitations** — task 3.3 (the capability Purpose) lands in the archive commit; acceptance of the pinned 9.8.2
  archive is a cold-cache CI run not yet observed.
