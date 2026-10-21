# Retrieval preflight record

Scratch-only validation of the complete proposed instruction set, run before full implementation approval. No file in
the real tree was restructured. The clone and logs live under `$TMPDIR/opencode/preflight/`; they are evidence, not
committed artifacts.

## Environment

| Item            | Value                                                                                                                                        |
| --------------- | -------------------------------------------------------------------------------------------------------------------------------------------- |
| OpenCode        | `v2.1.1` (`opencode --version`)                                                                                                              |
| Model           | `opencode-go/deepseek-v4.1-flash`                                                                                                            |
| Clone           | `$TMPDIR/opencode/preflight/repo`, cloned from `4972e687`, no remote                                                                         |
| Instruction set | Proposed `AGENTS.md` (290,298 B), the three packs, `check-and-test-authoring`, and the edited `review-quick` / `review-thorough` definitions |
| Sandbox guards  | No git remote; `git push`, `git reset --hard`, `git clean`, `rm -rf` denied in the clone's config                                            |
| Isolation       | `opencode run --standalone --auto`; no publication, archive, or merge attempted                                                              |

## Fixtures and observed reads

Each row records the actual tool calls in the run, in order. "First governed action" is the first edit, command, or
judgment in the subject; a read of the subject's pack must precede it.

| ID  | Input                                                                                                                                                          | Observed reads (order)                                                                                                                                                                                                                                                                                                                                | First governed action                           | Result                                                                                                         |
| --- | -------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------- | -------------------------------------------------------------------------------------------------------------- |
| A   | "Add an `int retryCount` field to the `Showcase` value class in `showcase-command-api`…"                                                                       | `Skill jvm-development` (1st tool call); no other pack                                                                                                                                                                                                                                                                                                | First `Edit` (`Showcase.java`), after the skill | **Pass**; prompt named the wrong module, which the run corrected                                               |
| B   | "Run the existing integration tests for the command service."                                                                                                  | `Read jvm-testing/SKILL.md` (1st), then `ls`, `docker info`                                                                                                                                                                                                                                                                                           | First `integrationTest` invocation              | **Pass**; load precedes the command and its Docker prerequisite                                                |
| C   | "The coverage gate is failing on the query-service module. Explain what to do…"                                                                                | `Skill coverage` (global), `Skill jvm-testing`, `Skill gradle-build`; then reads of the coverage config                                                                                                                                                                                                                                               | First `jacocoTestCoverageVerification`          | **Pass**, but observed **P2 + P3**, not P2 alone; the class estimate was corrected                             |
| D   | "The gateway returns HTTP 500 instead of 400 for a malformed request body. Explain why…"                                                                       | `Skill jvm-development` (1st tool call); then reads of the controller, error resolver, and specs                                                                                                                                                                                                                                                      | First diagnostic read/command                   | **Pass**; the run timed out at 15 min after the evidence, so its _conclusion_ is unverified                    |
| E   | "Explain how to interpret the `dependencyUpdates` report…"                                                                                                     | `Skill gradle-build` (1st); `dependency-management` spec read; an extra partial `check-and-test-authoring` read                                                                                                                                                                                                                                       | First report classification                     | **Pass**; the partial authoring-skill read is an observed unnecessary load                                     |
| F   | Markdown-only JVM proposal review, hand-over names `jvm-development`                                                                                           | `proposal.md`, then `Read jvm-development/SKILL.md`; no repository search                                                                                                                                                                                                                                                                             | Findings                                        | **Pass after fix**; the first attempt searched the repository widely (see failures)                            |
| G   | Same proposal, hand-over names only `gradle-build`                                                                                                             | First attempt: `gradle-build` only, then judged and searched, no missing-input report. After fix: `gradle-build`, then `missing-input` report and stop                                                                                                                                                                                                | Judgment                                        | **Failed, then passed after fix** (G4)                                                                         |
| H   | "Add a Guava dependency to `showcase-query-service` and use `Preconditions.checkNotNull`…"                                                                     | `showcase-query-service/build.gradle.kts` (read), `Skill gradle-build`, `platform/build.gradle.kts` (read), `Skill jvm-development`; both packs before the first edit                                                                                                                                                                                 | First edit                                      | **Pass** for a mixed task; both packs loaded from the start, so it does **not** test discovery                 |
| D2  | **Clean sandbox, two steps:** (1) add `RetryProperties` to `showcase-query-service`; (2) change its `label` to `WordUtils.capitalize` from Apache Commons Text | `D2-step1.txt`: `Read jvm-development/SKILL.md` (line 6) before the first write (line 448); `jvm-testing` before the test write. `D2-step2.txt`: `Read gradle-build/SKILL.md` (line 4) before the first build read/edit (line 241) and before the catalog/build edits; P1 not re-read (still in context); line numbers are those transcripts' offsets | First Java write; first build edit              | **Pass**; the second pack loaded before the first decision in the discovered subject                           |
| I   | README typo fix                                                                                                                                                | No pack read; `Packs: none` stated                                                                                                                                                                                                                                                                                                                    | (No edit; the typo did not exist)               | **Pass** (negative control)                                                                                    |
| R1  | Java field request, fresh recovery session                                                                                                                     | `Packs:` line, then `Skill jvm-development`, `Skill jvm-testing`                                                                                                                                                                                                                                                                                      | First edit                                      | **Pass** for load order; the run chose to write a test, so P2 was relevant, but it loaded both before any edit |
| R2  | Ordinary continuation of R1: add a second field                                                                                                                | No re-read; stated "loaded earlier this session"                                                                                                                                                                                                                                                                                                      | Edits                                           | **Not compaction evidence**; the bodies were still in context                                                  |
| R4  | R1 after a **completed server compaction**, add a third field                                                                                                  | "Packs: reloading…" then `Skill jvm-development`, `Skill jvm-testing`; no reliance on the summary                                                                                                                                                                                                                                                     | First edit                                      | **Pass**; the root rule was regained and both packs reloaded before the edit                                   |

The G2–G4 reruns' hand-over ended with the sentence `Follow your definition's input contract exactly.`; the first G
invocation did not include it. The surviving first-preflight clones carry the pre-check twice above the bounded-screen
opening, and the per-run definition states were not preserved, so the first-preflight shape/placement account is not
reconstructed from them.

## Failures, fixes, and reruns

1. **G — missing required pack (blocking).** The hand-over named only `gradle-build`; the reviewer read it, then judged
   the JVM proposal and searched the repository, with no `jvm-development` read and no missing-input report. This is a
   missed required load.
2. **F — unbounded review search (bounded-contract miss).** The first complete hand-over read `jvm-development` and the
   proposal, then ran repository-wide searches to verify the proposal's premise.
3. **Fix.** The reviewer definitions gained a pre-check **before any other read**: classify the supplied artifacts
   against the root table; if a matching pack is not named, stop and report the missing pack; do not judge and do not
   search. A proposal review does not verify its premise against the code; an unverifiable claim is a finding or a
   limitation.
4. **Reruns across the two suites.** The caller clause is necessary: the final suite's G/G2 (no clause) failed and G3/G4
   (clause) passed. The first preflight's G2/G3 failed and G4 passed, but its per-run definition states were not
   preserved (the surviving copies carry the pre-check twice above the opening), so it contributes no shape or placement
   conclusion. Every final-suite passing run (G3/G4) places a single pre-check above the opening; the hard stop is a
   hardening (G3 passed without it). F2 stayed bounded.
5. **Evidence kept.** The failed transcripts are `G.txt`, `G2.txt`, `G3.txt`, and `F.txt`; the passing reruns are
   `G4.txt` and `F2.txt`; the discovery fixture is `D2-step1.txt` and `D2-step2.txt`. They are not committed.

## Recovery after context loss

- **Ordinary continuation is not compaction.** R2 continued the R1 session; the packs were still in the transcript and
  were not re-read. This is recorded as ordinary continuation, not as proof of recovery.
- **Actual compaction was exercised.** `POST /api/session/<id>/compact` admitted a compaction; the export shows two
  `compaction` records — one `failed` (`compaction.interrupted`) and one `completed` with a summary. The post-compaction
  run regained the root routing rule, stated it, and reloaded `jvm-development` and `jvm-testing` before the first edit.
- **What the environment restores, as observed:** the root routing rule (the agent applied it after compaction); the
  pack bodies are not re-supplied and were reloaded on demand; the reviewer definition is loaded per invocation, and a
  compacted reviewer child session was not exercised, so its restoration remains unverified.

## Limitations

- One model, one clone, one session per fixture; this is not evidence of long-term reliability or of a second model's
  behavior.
- The first clone carried uncommitted work from earlier fixtures; R1 and an early subject-change attempt saw an existing
  `retryCount` and treated the request as done. Those runs are not used as evidence for the routing rule. The controlled
  D2 fixture ran in a **fresh sandbox** (`repo2`) and supersedes that inconclusive attempt.
- Fixture D's run exceeded the 15-minute bound; its load order is recorded, not its conclusion.
- `Skill "coverage"` in fixture C is a globally installed skill, not a repository pack; it is environment noise.
- Fixture H created a session worktree outside the clone; the second sandbox denies the worktree and session-move tools
  and `git worktree`, and the D2 fixture created none. Its step 2 downloaded a source jar from Maven Central (a read,
  not a publication) into `$TMPDIR/opencode/ct`.
- The metadata figure remains an estimate (the listing overhead is assumed), and no long-term reliability claim is made.

## Revised figures (observed)

| Quantity                                | Before preflight         | After preflight                                                     |
| --------------------------------------- | ------------------------ | ------------------------------------------------------------------- |
| Proposed root `R1`                      | 288,972                  | **290,298** (the diagnostic-boundary wording and the reviewer rule) |
| Root reduction                          | −34,966 (−10.8%)         | **−33,640 (−10.4%)**                                                |
| Shared baseline (`R1` + 1,109 metadata) | 290,081                  | **291,407**                                                         |
| Coverage interpretation                 | P2 only, 302,995 (−6.5%) | **P2 + P3, 324,822 (+0.3%)**                                        |
| Total maintained                        | 332,990 (+2.8%)          | **334,316 (+3.2%)**                                                 |

The remaining per-class loads shift by the same +1,326 B root growth; the design's table carries the re-derived values.

## Recommendation

**Proceed with the full pilot.** The final suite's G/G2 failed without the caller clause and passed with it (G3/G4),
which isolates the clause; every final-suite passing run (G3/G4) places a single pre-check above the opening, and the
hard stop hardens it. The first preflight's G2/G3/G4 are history whose per-run states are not recoverable, and no claim
rests on them. The unbounded search was fixed and re-verified (F2); the controlled second-subject fixture (D2) loaded
`gradle-build` before the first build decision that followed the discovery; actual compaction was exercised and the
recovery rule worked for the build agent. The real-tree verification is read-only; mutating fixtures run only in a
final-state sandbox, so this record is the evidence for the behavioral gate.

Residual risks remain explicit: one model and one environment; reviewer-definition restoration after child-session
compaction is unverified; the metadata overhead is an estimate; a broad three-pack task may load more than before
(+3.4%); and coverage-gate interpretation is at parity (+0.3%).

## Final-state sandbox suite (implementation)

The final candidate set was copied to `$TMPDIR/opencode/final-suite/repo3` and verified **16/16 byte-identical** to the
real files, with no remote and the worktree/session-move/`git worktree` denies. The final root is **291,750 B**
(`wc -c AGENTS.md`) and the packs **10,529 / 12,822 / 20,278 B**; the Environment row's 290,298 B is the preflight's
proposed root before the capture rules (A1–A3). Each fixture ran in its own fresh copy (`work/<id>`), so no fixture
inherited another's edits or a session worktree.

| ID  | Observed before the decision                                                                                                                                             | Result                                                                                  |
| --- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------ | --------------------------------------------------------------------------------------- |
| A   | `jvm-development` read (line 11) before the first production edit (line 510); `jvm-testing` before the test edits                                                        | **Pass**                                                                                |
| B   | `jvm-testing` skill read first, before the `integrationTest` command                                                                                                     | **Pass**                                                                                |
| C   | `jvm-testing` and `gradle-build` before the coverage baseline read / gate command                                                                                        | **Pass** (P2+P3)                                                                        |
| D   | `jvm-development` first, before the controller/error-resolver reads                                                                                                      | **Pass**                                                                                |
| E   | `gradle-build` first, before the suppression/config reads                                                                                                                | **Pass**                                                                                |
| F   | complete hand-over: `jvm-development` read before the findings; bounded; 4 findings; reports the pack                                                                    | **Pass**                                                                                |
| G   | no clause; pre-check before the hand-over paragraph: read `gradle-build`, searched, judged                                                                               | **Fail** (evidence kept)                                                                |
| G2  | no clause; pre-check restored to after the hand-over paragraph: still searched and judged                                                                                | **Fail** (evidence kept)                                                                |
| G3  | after the hand-over + caller clause (no hard stop): stopped, no findings, no search                                                                                      | **Pass**                                                                                |
| G4  | after the hand-over + caller clause + hard stop: stopped, no findings, no search                                                                                         | **Pass**                                                                                |
| H   | `gradle-build` and `jvm-development` before the build/Java edits; `jvm-testing` before the test write                                                                    | **Pass**                                                                                |
| D2  | step 1 `jvm-development` before the Java write; step 2 `gradle-build` before the first catalog/build edit                                                                | **Pass** (step 2's run timed out after the build edits; the load order is the evidence) |
| I   | no pack read                                                                                                                                                             | **Pass** (negative control)                                                             |
| GT  | thorough reviewer, missing pack hand-over (`gradle-build` named), caller contract: only the proposal read; verdict "missing pack", no findings, no other read or command | **Pass**                                                                                |

**What the failures establish.** The missing-pack stop depends on the **caller's hand-over instructing the reviewer to
follow its definition's input contract** — isolated by the final suite's G2 (no clause) vs G3 (clause) — and every
final-suite passing run (G3/G4) places a single pre-check above the bounded-screen opening. The first preflight's
per-run shape and placement are not recoverable from the surviving copies, so no conclusion rests on them. The hard-stop
sentence is a hardening (G3 passed without it). The root reviewer rule and the `/review-thorough` command require the
clause; the quick definition's stop says the missing-input report is the only output.

**Ablation.** Task 2.4 was not run: the observed first reads are the loading evidence, and no result attributes the
loads to the `Packs:` line. The line reports; it does not cause the read.

**Isolation.** Fixture D2 edited a copy of `check-and-test-authoring` inside its own sandbox; the real tree was not
touched. The deep-review correction updated `review-thorough.md`; the sandbox was refreshed and the 16/16 identity
re-verified before the GT run. The sandbox tree is disposable and is not part of the candidate set.

## Acceptance-gap controls (final candidate)

Candidate identity: the reversal manifest's 16 post-images, root **291,750 B**, packs **10,529 / 12,822 / 20,278 B**,
`review-quick` **14,932 B**, `review-thorough` **5,447 B**. The template `$TMPDIR/opencode/acceptance-gaps/repo3` was
copied from the final sandbox and verified 16/16 byte-identical; every fixture below ran in a fresh copy under that
directory. The logs are `$TMPDIR/opencode/acceptance-gaps/logs/*.txt`; they are not committed.

| Required case                                               | Actual fixture or probe                      | Final candidate tested?        | Observed prerequisite reads                                                                                                                                                            | Status                                                |
| ----------------------------------------------------------- | -------------------------------------------- | ------------------------------ | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------- |
| Java production                                             | A                                            | Yes                            | P1 before the first production edit; P2 before the test edits                                                                                                                          | Pass                                                  |
| Test authoring                                              | J2b                                          | Yes                            | P2 and C before the first test edit                                                                                                                                                    | Pass                                                  |
| Runtime diagnosis                                           | D                                            | Yes                            | P1 before the first controller/error-resolver read                                                                                                                                     | Pass                                                  |
| Dependency bump                                             | Q2c                                          | Yes                            | P3 and DM before the catalog edit; the dependencyUpdates run was not observed                                                                                                          | Pass (load); report run not exercised                 |
| Dependency-report reading                                   | E                                            | Yes                            | P3 before the suppression/config reads                                                                                                                                                 | Pass                                                  |
| Build-logic modification                                    | K                                            | Yes                            | P3 before the first build-logic edit; the option's check verified                                                                                                                      | Pass                                                  |
| Gradle failure presented as an application defect           | L                                            | Yes                            | P1 first; P3 before the first Gradle command; the controlled missing artifact was found                                                                                                | Pass                                                  |
| Integration tests                                           | B                                            | Yes                            | P2 before the `integrationTest` command                                                                                                                                                | Pass                                                  |
| Coverage interpretation                                     | C                                            | Yes                            | P2 and P3 before the coverage gate                                                                                                                                                     | Pass                                                  |
| Gateway e2e prerequisites                                   | M                                            | Yes                            | P2 before `:showcase-api-gateway:e2eTest`; the suite passed in 4m11s                                                                                                                   | Pass                                                  |
| Test-failure diagnosis                                      | N2 (N failed)                                | Yes                            | N2: P2 before the explanation, no run. N ran the test before P2 and is recorded as a failed first attempt                                                                              | Pass on rerun                                         |
| Discovered second subject                                   | D2                                           | Yes                            | P1 first; P3 before the first build edit after the discovery                                                                                                                           | Pass                                                  |
| Markdown-only JVM proposal                                  | F2b (F2 pre-correction)                      | Yes (F2b)                      | P1 before the findings; bounded                                                                                                                                                        | Pass on the corrected definition                      |
| Missing-pack hand-over                                      | G4b / GT2 (G4/GT pre-correction)             | Yes (G4b/GT2)                  | Missing input reported; no other read or finding                                                                                                                                       | Pass on the corrected definitions                     |
| Dependency-policy proposal                                  | O2b (pure policy); O3 (O was pre-correction) | Yes (O2b/O3); O pre-correction | O2b: P3 before the findings. O: P3 only for a proposal that also changed JVM code; six findings before a late missing-P1 note. O3 after the pre-check correction: stopped, no findings | Pass on O2b; O's stop miss fixed and re-verified (O3) |
| Documentation claim where paths do not identify the subject | P                                            | Yes                            | P2 before the findings on a Markdown-only test-tier claim                                                                                                                              | Pass                                                  |
| Editorial / docs-only                                       | I                                            | Yes                            | No pack read                                                                                                                                                                           | Pass                                                  |
| Compaction / resume                                         | R4 (preflight)                               | Probe (pre-final root)         | P1 and P2 reloaded after compaction; the resume rule is byte-identical at `AGENTS.md:60`                                                                                               | Covered by probe; no final-candidate rerun            |
| Dependency-bump synthetic probe                             | C (design probe)                             | No (not the complete root)     | P3 in a two-pack synthetic probe                                                                                                                                                       | Not complete-root; superseded by Q2c                  |

**Failures and reruns.** J read P2 and C but found the existing test complete and made no edit; J2 then hit an
unavailable endpoint and J2b completed the authoring fixture. N ran the failing test before loading P2; N2 supplied the
failure output, forbade a run, and loaded P2 first. Q read P3 but not DM and is informational; Q2 and Q2b hit provider
failures; Q2c read P3 and DM before the catalog edit. L attempted `git worktree add` and the sandbox deny refused it; no
worktree was created. J2b edited a sandbox copy of `check-and-test-authoring`, and K edited a sandbox copy of the
build-logic plugin; neither reached the real tree. M left no application containers behind. **O's first run** did not
stop for the missing `jvm-development` pack: the proposal's second bullet changed JVM production code, but the hand-over
named only `gradle-build`, and the reviewer produced six findings before noting the missing pack. The pre-checks were
widened to check **every** matching row (in `review-quick`/`review-thorough`, the delta, and the design), invalidating
those candidates and changing `DQ` to 14,932 B; O3, F2b, G4b, and GT2 were re-run on the fixed definitions. O3 and G4b
stop on the missing pack with no findings; F2b loads P1 and reports findings; GT2 stops in the thorough pass; O2b, the
pure dependency-policy case, loads P3 and reports findings.

**Auditor control (task 3.5).** The edited `agents-auditor` was run in disposable copies (`AUD`–`AUD5`):

- `AUD`, supplied root `Value classes` duplicate; expected a subject-pack merge; observed finding 9, **merge** with
  destination `jvm-development`.
- `AUD2`, supplied unevidenced `Repository transactions`; expected removal; observed finding 12, **remove**, with
  `jvm-development` named as the destination if the convention is intended.
- `AUD3`–`AUD5`, supplied configuration-properties candidates; the auditor correctly declined a fabricated marker and a
  small valid cluster (the valid residue was ~0.93 KB, 0.32%, below materiality) and reported no extraction candidate.
  **No positive extraction candidate was produced**, because the final root has no material, valid subject-pack
  extraction candidate and no synthetic candidate change was made. The original task 3.5 acceptance (“a subject-pack
  extraction candidate is reportable”) is therefore **not met**; the owner decision is whether the merge/removal
  behavioural control plus the negative materiality result is accepted, or a synthetic material candidate is authorised
  for a positive extraction case. The auditor's unrelated guidance findings are not applied in this record-only pass.

**Task 3.8 process deviation.** No 3.8 initial-hash checkpoint was retained, and none is reconstructed or backdated. The
current checks establish: every manifest post-image matches the working tree (16/16); the sandbox is 16/16
byte-identical; every pre-image matches `HEAD`; and a reversal may act only on a path whose current content is the
unit's post-image. They do **not** establish the 3.8 intermediate state, the exact sequence between 3.8 and the first
recorded post set (the round-27 manifest), or that no unreviewed candidate change occurred in that gap. The round-27 set
and the review ledger's subsequent refreshes are the earliest recorded evidence.
