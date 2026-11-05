# Tasks — pin the wrapper's distribution checksum

The unit's record is `docs/units/pin-wrapper-distribution-checksum/unit.md`.

## 0. The pre-write pass

- [x] 0.1 The review gate's pre-write derivation pass ran, seeded with `python3 scripts/claim-scan.py` over the staged
      artifacts; each candidate is resolved into `design.md`'s claims table or dismissed beside it, which states one
      reason per candidate kind. Verify: `design.md` carries the table and the dismissal line, and every scanned
      candidate is accounted for.

      Done: the claims table carries the change's substantive claims, one row per claim; the dismissal line accounts for
      the `path`, `absolute`, `count`, and `cross-ref` candidates the scan yields.

## 1. The pin and the gate

- [x] 1.1 `gradle/wrapper/gradle-wrapper.properties`: add `distributionSha256Sum` with the published SHA-256 of the
      pinned `-all` distribution. Verify: `./gradlew --version` succeeds with the line present, and a copy carrying a
      zeroed checksum is refused in a throwaway `GRADLE_USER_HOME` (an empty cache, since a cached distribution is not
      re-validated). Done: the properties carry `distributionSha256Sum=a1bb7e96…18fdf` for the pinned 9.8.2, and the
      refusal is proven — a scratch project whose URL points at a tiny local archive with a zeroed checksum fails with
      "Verification of Gradle distribution failed!" naming the expected and actual values, under an empty
      `GRADLE_USER_HOME`.

- [x] 1.2 `scripts/verify-wrapper-checksum.py`: read the properties, fail naming the missing pin when there is no
      `distributionSha256Sum`, fail naming the value when it is not 64 hex characters or when `distributionUrl` does not
      end `-all.zip`, and exit zero otherwise. Verify: `./gradlew verifyWrapperChecksum` passes on the pinned file.
      Done: `scripts/verify-wrapper-checksum.py` fails naming a missing pin, a value that is not 64 hex characters, and
      a `distributionUrl` that does not end `-all.zip`; it passes on the committed file.

- [x] 1.3 `scripts/test-verify-wrapper-checksum.py`: a present-and-valid properties file passes; a file without the line
      fails; a short value, a non-hex value, and a `-bin.zip` URL each fail. Verify: each case asserts its own verdict,
      and each fails against a mutation of the rule it covers. Done: the suite decides each arm with its own case — the
      valid shape, the repository file, a missing pin, a short value, a non-hex value, an uppercase value, a `-bin.zip`
      URL, a missing URL, and both defects at once.

- [x] 1.4 The root `build.gradle.kts`: register `verifyWrapperChecksum` and `testVerifyWrapperChecksum` as `Exec` tasks
      in the shape their siblings take, and add both to the `check` block's `dependsOn` list. Verify:
      `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` runs both, and each fails when its subject is broken.
      Done: `verifyWrapperChecksum`, `testVerifyWrapperChecksum`, and `testUpdateGradleWrapper` are `Exec` tasks beside
      their siblings and appear in `check`, which ran them green.

## 2. The bump tool

- [x] 2.1 `scripts/update-gradle-wrapper.py`: require `--version` — a missing one is a usage error, never a resolved
      default — and fetch `https://services.gradle.org/distributions/gradle-<version>-all.zip.sha256`, stopping before
      touching a file when the response is absent, empty, or not 64 hex characters. Verify: the tests' fixture body
      resolves, a missing `--version` exits non-zero with nothing written, and each malformed body does the same. Done:
      the tool requires `--version`, validates it as a stable release, fetches `<version>-all.zip.sha256`, and stops
      before any write on an unreachable source, an empty body, or a non-checksum body — the tests cover all three.

- [x] 2.2 The same tool: snapshot the four wrapper files' bytes and executable modes, run
      `./gradlew wrapper --gradle-version=<v> --distribution-type=all --gradle-distribution-sha256-sum=<sha>` twice,
      confirm `distributionUrl` names `<v>` and `distributionSha256Sum` equals `<sha>`, and restore what it captured —
      bytes and executable modes — with a non-zero exit when a run fails or the result is incomplete, printing the
      snapshot's directory before the first write so a killed process leaves its recovery material findable. It does not
      refuse a dirty tree. Verify: `scripts/test-update-gradle-wrapper.py` drives a fake `gradlew` that succeeds, fails
      on the second run, and writes only `distributionUrl`, asserting the restore in the last two, plus a same-target
      re-run that reports no change and succeeds. Done: the tool captures the four files' bytes and modes into a printed
      snapshot directory, runs both passes with the checksum, verifies `distributionUrl`/`distributionSha256Sum`, and
      restores on a failed pass or an incomplete result; the tests drive a fake `gradlew` for each path.

- [x] 2.3 `scripts/test-update-gradle-wrapper.py` is registered as `testUpdateGradleWrapper` in the `check` block.
      Verify: it runs from `check` and fails when the tool's restore path is broken. Done: the suite's cases are its own
      enumeration — each arm the tool decides has one, and the two restore outcomes (a completed restore and an
      incomplete one) plus the capture failure each carry a case.

- [x] 2.4 `.opencode/commands/gradle-update.md`: the command keeps its report/current decision points and its lifecycle,
      and replaces the two-run prose with the tool invocation, passing the version the report selected. Its completion
      criterion changes with it: a bump is complete when the second generation ran under the target distribution and the
      resulting URL and checksum agree — not when all four artifacts differ, since the generated jar is byte-identical
      when the running and target versions match. Verify: the command names no manual wrapper step, its text matches
      what the tool accepts, and its completion criterion no longer requires four differing files. Done:
      `/gradle-update` invokes the tool with the version `dependencyUpdates` selected, and its completion criterion is
      the second generation under the target with URL and checksum agreeing — not four differing files.

## 3. The rehearsal and the records

- [x] 3.1 Rehearse one real bump: run the tool against the live distribution service to a named release, confirm the
      four files and the checksum, then bump back to the pinned version through the same tool. Verify: the final tree
      names the pinned version and checksum, `./gradlew --version` reports it, and the rehearsal's outputs are recorded
      here. Done: the tool bumped 9.8.2 → 9.8.1 (the `./gradlew --version` reporting 9.8.1), then back to 9.8.2 through
      the same tool; the tree ended with only the checksum line added against `HEAD`, so the round trip restored the jar
      and scripts byte-for-byte, and `verifyWrapperChecksum` passes on the result. The rehearsal ran on a warm cache, so
      it proves the mechanics and the restore — not that the pinned value matches the 9.8.2 archive bytes, which is an
      accepting CI run not yet observed.

- [x] 3.2 `.opencode/skills/gradle-build/SKILL.md`: the wrapper bump bullet states the tool, the checksum, and the
      snapshot restore, in place of the two-run prose and the "not hand-edited" failure handling, and replaces the
      four-file-diff completion criterion with the check that the second generation ran under the target distribution
      and the resulting URL and checksum agree. Verify: the bullet points at the tool and names the gate; no live site
      still describes the bump as two manual runs; and no live site states that only the properties file changing means
      an incomplete bump. Done: the pack's wrapper bullet now names the tool, the required `--version`, the checksum,
      the byte-and-mode restore, the completion criterion that replaces the four-file diff, and the gate.

- [x] 3.3 `openspec/specs/showcase/quality/commit-hygiene/spec.md`: refresh the `## Purpose` so its enumeration of the
      capability's build checks names the wrapper-checksum gate. A delta cannot carry a Purpose, so the edit is applied
      in the archive commit. Verify: the archived spec's Purpose names the new check, and the requirement's own text is
      unchanged by the edit.
      Done: the archived spec's `## Purpose` names the wrapper-checksum guard among the capability's build checks;
      the requirement's own text is unchanged by the edit.
- [x] 3.4 `docs/ideas.md`: remove the entry this change implements. Verify: the entry is gone and its section keeps its
      other entries. Done: the entry is gone from `docs/ideas.md`, and `verifyIdeasSections` passes.

- [x] 3.5 `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` and `openspec validate --all`. Verify: both pass.
      Done: `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` and `openspec validate --all` both pass.

## 4. The review gate and the records

- [x] 4.1 The proposal stage's quick review: `review-quick` over the planning artifacts until a clean round, with each
      round's row and findings under `docs/units/pin-wrapper-distribution-checksum/`. Verify: `--expect-clean` and
      `--session-continuity` pass. Done: rounds 1–5 in `rounds.tsv` (12 → 3 → 2 → 4 → 0 findings), the closing round 5
      with no finding class; `--expect-clean`, `--session-continuity` and `--review-ready` pass.

- [x] 4.2 The owner's manual pass over the proposal. Verify: the approval is recorded before the implementation starts.
      Done: the owner approved the proposal and instructed implementation in one message ("Одобряю. Реализуй."),
      recorded in `unit.md`'s Resume checkpoint; nothing was implemented before it.

- [x] 4.3 The implementation stage's quick review. Verify: the closing round is clean and the ledger checks pass. Done:
      rounds 6–9 in `rounds.tsv` (8 → 4 → 2 → 0 findings), the closing round 9 with no finding class; `--expect-clean`,
      `--session-continuity` and `--review-ready` pass.

- [x] 4.4 The deep-review routing decision on the completed diff. Verify: the outcome and its signals are recorded.
      Done: `python3 scripts/review-routing.py` over this unit and change reports REQUIRED on the added requirement
      block, so the deep review runs.

- [x] 4.5 The deep review, when 4.4 selects one, with its findings applied and re-reviewed. Verify: the pass's status
      and its findings' disposition are recorded. Done: the routing made the deep review required, and `review-thorough`
      reported two nitpicks — a proposal-stage pending-decision line left in the unit record, and a snapshot directory
      each handled failure leaked after its restore had already run. Both applied. The pass re-checked the transaction's
      failure paths, the gate's verdict, the delta, the command and pack text, and the rehearsal's evidence as sound.

- [x] 4.6 The technical advisor's consultation on the implementation, when the owner authorizes it. Verify: the
      authorization and the findings' disposition are recorded. Done: the owner authorized the pass ("Отревьювь с
      советником до согласия"), and the consultation closed in explicit agreement after four rounds ("I agree, with no
      further corrections requested"). Round 1 kept the design and found one confirmed problem — the command treated a
      nonzero exit as proof of restoration — plus a buffering hazard and an over-stated acceptance claim. The recovery
      contract now reports three outcomes, the snapshot is flushed after capture, and the acceptance is stated as
      pending. Rounds 2–3 caught the same claim surviving in the tool's docstring and in the command's failure
      paragraph, and the killed-process case being promised a report it cannot write; round 4 reported nothing
      remaining.

- [x] 4.7 The owner's manual pass over the finished unit. Verify: the approval is recorded, and nothing is committed,
      pushed, or merged before it. Done: the owner reviewed the finished unit and instructed delivery in one message
      ("Одобряю. Отгружай."); nothing was committed before it.

- [x] 4.8 `docs/units/pin-wrapper-distribution-checksum/process-defects.md`: one record per defect observed, or the
      statement that none was. Verify: the file exists before 4.7 is requested. Done: the file states that no process
      defect was observed, and notes that the premise corrected during the proposal loop was a hypothesis tested there
      rather than a defect.
