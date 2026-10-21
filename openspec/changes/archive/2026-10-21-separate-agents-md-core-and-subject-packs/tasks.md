# Tasks

Planning is complete; the implementation, verification, and final measurement records are below. The ordered sections
are a gate, not a preference: **no root text is deleted until the packs and the proposed root have passed the retrieval
suite in a scratch clone.** Each task is one implementable chunk with its own verification. The planning artifacts are
`proposal.md`, `design.md`, the delta specs, and `drafts/`.

## 1. Create the packs in a scratch clone

- [x] 1.1 In a disposable clone of the repository, create `.opencode/skills/jvm-development/SKILL.md` from
      `drafts/jvm-development.md`, applying only the documented cross-reference retargets. Verify every moved range is
      present (a `python3` containment check over the design's inventory ranges, after the retargets) and that its
      `captured:` markers are present. **Done**: `drafts/jvm-development.md` was applied in the scratch clone; the move
      audit (implementation review and deep review) found each moved span once in the final pack, and the final suite
      reads the real file by exact name.
- [x] 1.2 Create `.opencode/skills/jvm-testing/SKILL.md` from `drafts/jvm-testing.md` and
      `.opencode/skills/gradle-build/SKILL.md` from `drafts/gradle-build.md`, with the same preservation check.
      Verification for 1.1–1.2: no missing range and the marker count matches the source (25 moved). **Done**: the other
      two packs were applied the same way; the final three packs are byte-identical to the sandbox copies the suite
      passed (16/16).
- [x] 1.3 Confirm each skill is discoverable and loads by exact name in a fresh session in the clone: the three names
      appear in the available-skills list or load by name, and reading the named path returns the body. **Done**: the
      final suite's fresh sessions load each pack by exact name — `Read .opencode/skills/jvm-development/SKILL.md` (A),
      `Skill jvm-testing` (B), `Skill gradle-build` (E).

## 2. Validate retrieval before the root is touched

- [x] 2.1 In the clone, build the complete proposed `AGENTS.md` from the drafts: the routing section, the four pack
      rows, the reviewer rule, the section pointers, the rewritten `## Build & Test`, and the four capture edits. Do not
      edit the real tree. **Done**: `preflight.md` records the clone's complete proposed root; the real root is that
      construction plus capture A1–A3 and the caller contract, and no real-tree edit occurred during the preflight.
- [x] 2.2 Run the validation fixtures against the clone, with stage-aware expectations: the Java production change (P1
      at stage 1, P2 only at a test stage, P3 only at a build stage); test-only (P2 + C); runtime diagnosis (P1 before
      the first hypothesis or diagnostic experiment); dependency bump and report reading (P3 + DM); build-logic change
      (P3); a Gradle failure misreported as an application defect (P1, then P3 **before the first build hypothesis or
      diagnostic experiment**); run the existing integration tests, check coverage (P2 + P3), run the gateway e2e,
      explain a test failure (P2 before the first hypothesis or diagnostic experiment); discovered second subject (its
      pack at the discovery, not at the start); Markdown-only JVM proposal (P1, no repository search); a missing-pack
      hand-over with the definition's input-contract instruction (report the missing input and stop, no search);
      dependency-policy proposal (P3); documentation asserting a test-tier or runtime behavior (the claimed subject's
      pack); editorial and docs-only tasks (no pack). Record each run's observed reads and its `Packs:` line. **Done**:
      `preflight.md`'s acceptance-gap coverage table maps every required case to its fixture/probe, whether the final
      candidate was tested, and the observed reads. New final-candidate controls: J2b test authoring, K build-logic, L
      Gradle failure, M gateway e2e, N2 test-failure diagnosis (N's first attempt ran before P2 and is recorded), O2b
      pure dependency-policy proposal, P documentation review, Q2c dependency bump, and O3/F2b/G4b/GT2 after the
      partial-hand-over correction. A–E, D2, and I are reused; F2/G4/GT were re-run as F2b/G4b/GT2 after the correction;
      R4 compaction is probe-only because the resume rule is byte-identical at `AGENTS.md:60`; the synthetic
      dependency-bump probe C is not the complete root and is superseded by Q2c.
- [x] 2.3 Compare each `Packs:` line with the observed reads. A reported pack with no read is a reporting failure; a
      required pack read only after its stage's first decision fails the fixture; an early pack whose subject the task
      never reaches is excess, recorded and charged against that class's measured benefit. **Done**: the preflight
      compared the `Packs:` lines with the observed reads; E's extra authoring read is recorded as excess, and the F/G
      reporting/stopping failures were fixed and re-verified (F2/G4/GT). The new controls' reads are in the coverage
      table; Q's natural bump missed DM and is superseded by Q2c, whose policy prompt loaded P3 and DM.
- [ ] 2.4 Run the ablation: the same fixtures with the `Packs:` line omitted, recording whether each still loads its
      required pack, so the line's effect is attributed rather than assumed. **Deferred (independent research)**: the
      loading criterion is the observed first read before the first governed action, not the self-reported `Packs:`
      line; the line-alone effect is unattributed. A controlled ablation would need a scratch instruction variant with
      the reporting line removed, and none was run. Deferring does not weaken the loading acceptance criterion, and no
      record attributes the loads to the line; the design's routing item 3 says so.
- [x] 2.5 Apply the gate: if any positive control fails, stop with the real root untouched and return to the owner with
      the failing fixture, the pack, and a proposed reduced boundary. Do not delete root text, do not re-route an
      obligation to a pack that will not ship, and do not choose a partial unit silently. **Done**: the gate was
      applied; G's missed load failed the first preflight, the boundary was fixed (not a partial unit), and the final
      suite's positive controls pass with the caller contract.

## 3. Apply, review, capture, and freeze the final candidate

- [x] 3.0 Write the unit's **reversal manifest** before the first real-tree edit: an explicit list of the exact paths
      this unit changes. A modified path records its pre-image and post-image SHA-256; a **created** path records "no
      pre-image", its post-image hash, and `delete` as its reversal. IF a reversal is needed, THEN act only on manifest
      paths whose current content is the unit's post-image, leave a file with unrelated edits untouched, preserve
      `docs/review-rounds.tsv` and the change dir, and stop for owner review when a path is outside the manifest or its
      ownership is ambiguous. Never use `git status` as the rollback ownership manifest. **Done**:
      `reversal-manifest.txt` lists 11 modified and 5 created paths with pre-images from `HEAD` and final post-images;
      the 5.2 refresh is recorded there.
- [x] 3.1 Create the three packs in the real tree by copying the verified clone drafts to
      `.opencode/skills/{jvm-development,jvm-testing,gradle-build}/SKILL.md`, then confirm each loads by exact name.
      Verification: the real files are byte-identical to the clone files the suite passed. **Done**: the real packs are
      byte-identical to the sandbox copies the suite passed (16/16), and the final suite loads them by name.
- [x] 3.2 Apply the root edits from `drafts/root-routing.md`: insert the routing section, remove the moved spans, add
      the Conventions and Gotchas pointers, rewrite `## Build & Test`, and amend the README design-intent bullet.
      Verification: every removed range is in exactly one pack; 25 `captured:` markers survive; every pointer resolves
      to a real file. **Done**: the deep review's move audit found every moved span absent from root and present once in
      the right pack; every pointer resolves; the moved `captured:` markers survive.
- [x] 3.3 Apply the four capture-routing edits in `AGENTS.md` (step 2, step 3, the "Every proposal names" decision
      point, and the merge-time paragraph) and the eight destination/figure edits in
      `.opencode/agent/lesson-capture.md`. Verification: a capture proposal naming a Java or Gradle lesson lands in the
      named pack; a pack-rule addition and retirement are both reported by the subagent's contract. **Done**: the eight
      `lesson-capture` edits and the four root capture edits are applied; the capture run applied A1–A3 to root and R1
      (the pack intros) to the three packs, so the pack destination was exercised by a retirement. No Java/Gradle lesson
      was proposed.
- [x] 3.4 Apply the review edits: `review-quick.md`'s **pre-check before the bounded-screen section** (classify before
      any other read; a missing pack makes the missing-input report the only output; no other file, command, or
      finding), its named-pack hand-over input, capture definition, and compliance corpus; `review-thorough.md`'s input
      list, pre-check, and conventions bullet; and the `/review-thorough` command's step 3, including the instruction to
      follow the definition's input contract. Verification: a Markdown-only JVM proposal review loads P1 from the
      hand-over; a hand-over missing P1 reports the missing input and stops with no search; the caller's hand-over
      carries the contract instruction. **Done**: F2 reads P1; G4 and GT stop on the missing pack with no findings; the
      root and `/review-thorough` command carry the caller's contract instruction. The O edge found a partial-hand-over
      miss (P3 named, P1 missing, six findings before the note); the pre-checks now require checking every matching row,
      and O3/F2b/G4b/GT2 re-verify the fixed definitions.
- [ ] 3.5 Update `.opencode/agent/agents-auditor.md`: the subject-pack content class, the subject-scope condition, and
      the "Destination type" field. Verification: a subject-pack extraction candidate is reportable under its own rules.
      **Control run; original extraction acceptance unmet**: AUD reported the supplied `Value classes` duplicate as a
      **merge** with destination `jvm-development`; AUD2 reported an unevidenced rule as a **remove** naming
      `jvm-development`; AUD3–AUD5 declined a fabricated marker and a small valid cluster (valid residue ~0.93 KB,
      0.32%, below materiality) and reported no extraction candidate. No positive extraction candidate exists in the
      final root without a synthetic candidate change. Owner decision: accept the merge/removal behavioural control plus
      the negative materiality result, or authorize a synthetic material candidate.
- [x] 3.6 Extend the `upstreamReferences` task in `build.gradle.kts` to every project-authored skill under
      `.opencode/skills/`, excluding the generated (`opsx-*`, `openspec-*`) and vendored (`axon4to5-*`) prefixes. Add
      its committed file-set test: a reference in a project-authored skill is reported; one in each excluded kind is
      not. Verification: the test fails on the old scope and passes on the new one; the four moved references appear.
      **Done**: `UpstreamReferenceScopeTests` covers the project-authored skill, both excluded prefixes, nested ADRs,
      and resembling files; `-p build-logic test` passes and `upstreamReferences` reports the four moved references. The
      project-skill case is the positive control the old scope would fail.
- [x] 3.7 Rewrite both restating lists in `openspec/config.yaml`'s context (Conventions and Build & test) to point at
      the packs, keeping the DB-script prerequisite; refresh the README's three capture statements; update the parked
      extraction-condition entry in `docs/ideas.md`. Do not rewrite the README's Testing section — it is a human-facing
      summary, named in the design as such. Verification: `openspec validate --all`, the 51,200-byte context limit, and
      a grep for the removed restatements. **Done**: the config context is rewritten (10,060 B file, under the
      51,200-byte context limit), `openspec validate --all` passes 27, and the README's three capture statements and the
      parked idea are updated.
- [x] 3.8 Record the **initial candidate hashes**: the SHA-256 of every candidate file after 3.1–3.7. This is the
      baseline a review or capture correction invalidates; do not run the mutating suite here. **Closed as process
      deviation**: no 3.8 checkpoint was retained and none is reconstructed. Present checks: manifest 16/16 post-images
      = working tree; sandbox 16/16; pre-images = `HEAD`; a reversal can act only on a content-verified post-image. They
      cannot establish the 3.8 intermediate state, the exact sequence between 3.8 and the first recorded (round-27) set,
      or that no unreviewed candidate change occurred in that gap.
- [x] 3.9 Run the **implementation quick-review loop to its required clean state before capture**, with reviewer-session
      continuity; fix every finding and re-run until the latest round reports no new observations. **Done**: the
      implementation quick-review loop ran rounds 25–27 and was clean at 27 in the same session; the capture followed.
- [x] 3.10 Run the `lesson-capture` subagent with the implementation diff, the round rows, the change dir, and the
      existing guidance; apply its durable proposals to the owning destinations, or record the skip and its reason.
      **Done**: four durable proposals — A1 (a line range is a coordinate in the content it was measured on), A2 (a
      probe-verified definition is copied position-included), A3 (a probe's sandbox is state) in `AGENTS.md`, and R1
      (the three pack intro trigger sentences retired to the root table). Applied net: `AGENTS.md` +1,427 B, the three
      packs −638 B; the contiguous-ledger check is parked in `docs/ideas.md` (2026-10-21) rather than expanding this
      unit.
- [x] 3.11 Fix any finding a review or capture correction raises, and re-run the bounded review over those changes until
      clean. Record which candidate files the corrections touched; each invalidates its 3.8 hash. **Done**: capture
      rounds 28–29 and thaw rounds 30–38 fixed and re-reviewed; rounds 29, 37, and 38 are clean; the touched candidates
      are `AGENTS.md`, `review-quick.md`, `review-thorough.md`, and the three packs, as the refreshed manifest records.
- [x] 3.12 Run `spotlessApply` on the changed files as the **last write** of the candidate phase, then `spotlessCheck`;
      after this the candidate is frozen for verification. **Done**: `spotlessApply` ran after the deep-review
      correction and `spotlessCheck` passes; the later edits are records (tasks, design, preflight, ledger, delta) and
      change no candidate.
- [x] 3.13 Build the final-state sandbox from the final candidate files, verify each is byte-identical to its real
      counterpart, and run the **full** mutating suite there once. A candidate changed since 3.8 is expected; the full
      suite covers the final state. (A later post-snapshot correction takes 5.2's change-scoped path.) **Done**: the
      final sandbox (`$TMPDIR/opencode/final-suite/repo3`) is 16/16 byte-identical; A–E, F2, G4, H, D2, I, and GT pass;
      G/G2 fail without the caller contract and are recorded as the fix's evidence. Each fixture ran in a fresh copy.
- [x] 3.14 Contain the sandbox: no git remote; deny `git worktree *`, the worktree tools, and session-move in its
      config; assert the remote is empty and no worktree was created before and after the suite; any session or worktree
      a fixture creates is recorded and deleted with the sandbox. Verification: the config's resolved document shows the
      denies, and the suite's transcript contains no worktree path outside `$TMPDIR`. **Done**: the sandbox has no
      remote, the worktree/session-move/`git worktree` denies, and no worktree was created; each fixture ran in its own
      copy under `$TMPDIR`.
- [x] 3.15 Verify the real tree read-only: `openspec validate --all`, `spotlessCheck`, the touched modules' `check`
      gates, `upstreamReferences`, `verifyCapturedMarkers`, `verifyReviewSessionContinuity`, and a read-only lost-term
      sweep. Do not run a mutating fixture, edit a file, or create a session in the real tree. A failed check that would
      change a candidate leaves this phase for 5.2's thaw path. **Done**: the read-only gate ran `check` (Docker-free
      and full), `openspec validate --all` (27), `spotlessCheck`, markers, session continuity, unique rounds,
      `upstreamReferences`, and the lost-term sample; no mutating fixture ran in the real tree.

## 4. Verify the real tree (read-only)

The following run against the frozen real candidate tree; none of them mutates a behavioral fixture state.

- [x] 4.1 Run `spotlessCheck` and the manual 120-character check over the changed non-formatter files. Record the
      inherited 153-character line in `jvm-testing.md` as accepted. Do not run `spotlessApply` here. **Done**:
      `spotlessCheck` passes; the 120-character check over `openspec/config.yaml`, the reversal manifest, and
      `.openspec.yaml` is clean, and the `preflight.md` table rows are Prettier-formatted (formatter-owned, accepted);
      the inherited 153-character line in `jvm-testing.md` is the moved content, recorded as accepted.
- [x] 4.2 Run `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` and `openspec validate --all`; for the
      `upstreamReferences` change also run the full `check` with Docker. **Done**: the Docker-free check passed (394
      tasks, 20 executed, 24s); the full `./gradlew check` with Docker and the coverage gate passed (409 tasks, 17
      executed, 10s); `openspec validate --all` 27 passed.
- [x] 4.3 Run the lost-term sweep across `AGENTS.md`, `README.md`, `docs/`, `openspec/specs/`, `.opencode/`, and the
      packs; verify each of the six cross-reference retargets and the one sentence split resolves. **Done**: the
      implementation review's containment check found every moved span in exactly one pack and the six cross-reference
      retargets resolve; the sample sweep finds `checkstyle(libs.guava)` only in `gradle-build`,
      `toBuilder()`/`ErrorProne` in `jvm-development`, `jacocoTestCoverageVerification` in `jvm-testing`/`gradle-build`;
      `KAFKA-18281` stays at root as the retained Testcontainers infra note.
- [x] 4.4 Re-measure the complete-load table: `R1`, the added metadata, each pack, `C`, `DQ`, and `DM`; compare every
      class with the design's figures and record the measured result. Do not report one aggregate saving. **Done**: `R1`
      **291,750 B** (`wc -c`), `M` 1,109 (estimate) → baseline **292,859 B**; P1/P2/P3 **10,529 / 12,822 / 20,278 B**
      (planning 10,603 / 12,914 / 20,501; −74 / −92 / −223 from the capture's R1 intro retirement); `C` 14,032; `DQ`
      **14,932** (the planning 13,585 predates the reviewer-contract edits); `DM` 21,373. Final totals: JVM production
      303,388 (−20,550, −6.3%); test-only 319,713 (−18,257, −5.4%); coverage 325,959 (+2,021, +0.6%); JVM+build 323,666
      (−272, −0.1%); full JVM+tests+build 350,520 (+12,550, +3.7%); integration 305,681 (−5.6%); JVM+tests 330,242
      (−2.3%); dependency 334,510 (−3.1%); reviews 331,142 / 318,320 / 328,069 / 320,613 / 307,791. The root reduction
      is measured: 291,750 vs 323,938 = −32,188 (−9.9%); `M` is estimated and the task loads are arithmetic, not session
      measurements. See the design's "Final measured complete loads"; no aggregate saving is reported.
- [x] 4.5 Treat a failed check that would change a candidate as a 5.2 thaw, not a read-only edit. **Done**: no read-only
      check changed a candidate; the final suite's G/G2 failure was the 5.2 thaw (the caller contract, the hard stop,
      and the placement), fixed and re-reviewed before this gate. The remaining rounds were record-only.

## 5. Review and close-out

- [x] 5.1 Derive the deep-review routing floor with
      `python3 scripts/review-routing.py --change separate-agents-md-core-and-subject-packs`, decide at or above it, and
      record the outcome and its signals. **Planning-stage derivation: `REQUIRED` (`requirement-block-added`); the deep
      review runs after the final snapshot and before the manual pass.** **Done**: the final derivation returned
      `REQUIRED` (`requirement-block-added`); `review-thorough` ran (packs loaded: `gradle-build`) and returned 2
      should-fix findings — the delta's three capture clauses still said `AGENTS.md` only, and `review-thorough.md`'s
      missing-pack stop omitted the only-output sentence — with no blocking findings.
- [x] 5.2 **Thaw on a post-snapshot candidate change.** IF the deep review or a read-only check changes a candidate,
      THEN fix it, re-run the bounded review, format the changed files, re-hash the affected candidates, rebuild the
      sandbox, and rerun the checks the change affects: retrieval fixtures for root routing, a pack body, a reviewer
      definition, or a recovery rule; the capture/auditor definition checks and the read-only checks that read a changed
      `lesson-capture.md` or `agents-auditor.md`; docs/consistency for config/README/ideas; the upstream test and build
      checks for `build.gradle.kts`. A ledger row, task note, or change-dir note invalidates nothing. The completion
      claim then uses the refreshed snapshot. **Done**: the deep-review correction changed the delta (planning) and
      `.opencode/agent/review-thorough.md` (candidate); round 38's bounded review is clean, the manifest post-image was
      refreshed, the sandbox's `review-thorough.md` was updated and the 16/16 identity re-verified, and the thorough
      missing-pack fixture (GT) stops with no findings and no other read. The post-correction `check` and
      `openspec validate --all` pass. The acceptance-gap O finding then corrected both reviewer pre-checks to check
      every matching row, changed `DQ` to 14,932 B, refreshed the manifest and sandbox, and re-ran O3/F2b/G4b/GT2/O2b;
      the pure policy O2b passes and the partial hand-over stops.
- [x] 5.3 Ask the owner for the manual review pass before committing, pushing, opening a pull request, archiving, or
      merging; after approval, ship per the `ship-change` skill. Verification: the manual pass is requested and the
      owner's approval is recorded. **Done**: the manual pass was requested and the owner approved on 2026-10-21
      ("Approved. Let's ship the change."); shipping per `ship-change`.

## 6. Planning records (this unit)

- [x] 6.1 Run the pre-write derivation pass and record its output as the design's claims table. **Done**: the claims
      table and its scan-candidate dismissals are in `design.md`.
- [x] 6.2 Record the proposal-round reviewer session identifier, transition, and classes in `docs/review-rounds.tsv`.
      **Done**: the rounds are recorded under session `ses_ea4552e63ffey4eR060ZgxrLNd`; the ledger is authoritative for
      each round's classes and verdict.
- [x] 6.3 Record the scratch preflight: fixture inputs, observed reads and order, failures and fixes, the compaction
      result, the corrected boundary, and the revised loads. **Done**: `preflight.md`; the figures are carried into
      `design.md`.
