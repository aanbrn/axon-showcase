# Tasks

The ordered sections are a gate, not a preference: **no root text is deleted until the pack, the proposed root, the
retargeted gates, and the retrieval matrix have passed in a scratch clone.** Each task is one implementable chunk with
its own verification. Planning artifacts: `proposal.md`, `design.md`, and the delta spec.

## 1. Preflight in a scratch clone (entry gate)

- [x] 1.1 In a disposable clone, build the pack from the design's **transfer manifest**: extract rows 1–7 by their
      `e42213a8` ranges, apply the stated heading transformation (source `##`/`###` → pack `##`; rows 6–7 gain a `##`
      heading), write the frontmatter and `Source:` line from "Destination shape", add the one-sentence Kafka-bump
      pointer (documented edit 21) to its `## Local development` infra-versions paragraph, and keep the manifest's
      order. Then append row 8 verbatim to `jvm-testing`'s Testcontainers group and extend that pack's `Source:` line.
      Verify by a **full source→candidate comparison**: every line of the seven ranges appears in the candidate, in
      order, except where a documented edit or the heading transformation changes it, and no warning sentence inside a
      range is missing. Byte totals, endpoint lines and identifier lists are supporting evidence, never the acceptance
      check. **Done**: built from the manifest in the scratch clone; the pre-format line comparison and the post-format
      word comparison both report 0 misses.
- [x] 1.2 Apply the documented edits with assertions, in two groups. **Inside the moved text:** item 11 only (the
      `verifyDocumentedPins` sentence at `AGENTS.md:1875`). **Outside it:** items 1–10 and 12–20 (edit 21 is applied by
      task 1.1 with the pack build). The other five in-pack references need **no** edit — `AGENTS.md:1737` and `:1792`
      ("the Docker Images note above"), `:1873` ("this file's Kubernetes Deployment section"), `:1916` ("see Local
      Development"), and the Docker Images forward reference to "the buildpack-pin gotcha below" — all resolve once the
      pack preserves the manifest's order. Verify each replacement by asserting its anchor's occurrence count before
      writing, and assert the five unchanged references still read as they did before the move. **Done**: edit 11 inside
      the moved text, edits 1–10 and 12–21 outside it; every anchor asserted at count 1.
- [x] 1.3 Verify contract preservation across the move: every `captured:` marker still sits **inside the rule it
      records** (the pack's format must satisfy `commit-hygiene.py --markers`), no moved unit carries an `evidence:`
      token, and the distinctive identifiers survive — `paketo-nginx`, `paketo-procfile`, `paketo-builder-jammy-base`,
      `builder-jammy-base:0.4.662`, `BP_NGINX_STUB_STATUS_PORT`, `installDependsOn`, `mustInstallAfter`,
      `mustUninstallAfter`, `helm.local.kubeContext`, `KAFKA_LISTENERS`,
      `PLAINTEXT://:9092,BROKER://:9093,CONTROLLER://:9094`, `webUi.apiBaseUrl`, `apiGateway.events.keepAliveInterval`,
      `SHOWCASE_CORS_ALLOWED_ORIGINS`, `SHOWCASE_CORS_ALLOWED_HEADERS`, `SHOWCASE_API_BASE_URL`, `BPL_DEBUG_PORT`,
      `THC_PATH`, `JAVA_OPTS`, `DB_PASSWORD`, `paketo-buildpacks/nginx#1340`, `buildpacks/pack#2527`,
      `spring-projects/spring-boot#49251`, `verifyInfraImageVersions`, `verifyInstallCommands`, `verifyDocumentedPins`.
      The identifier sweep is a second net over the comparison, not a substitute: a lost condition without a lost
      identifier is what it cannot see. **Done**: 10 markers in their rules, 0 `evidence:` tokens, every identifier
      resolves.
- [x] 1.4 Build the complete proposed root in the clone: the `## Subject guidance packs` row and preamble, the pointer
      at each vacated section, the Gotchas subsection's "**Moved:**" line, the `## Build & Test` Helm pointer, and the
      documented edits 2–6. Verify that every pointer reaches **the section and obligation it names**, not merely an
      existing file — the failure mode is a pointer that opens a file the reader then has to search. **Done**: each of
      the five pointers names the pack, its path, and the trigger; the pack carries the matching section.
- [x] 1.5 Validate the retrieval design: check the design's full matrix statically — each row's complete pack set
      against the root table and its `deployment-operations` need against the row text — and run **five** isolated,
      read-only fresh-session probes: a docs-only change; an installed release answering 503 after an infra values
      re-apply; a Gradle-only entry that must change a buildpack/builder pin or its registry rule, starting from
      `gradle-build`'s update-check list pointer (documented edit 19); Kafka-container test authoring with no tag
      change; and an unrelated editorial task (a duplicated word in a retrospective) that loads no pack — the negative
      control. A probe prompt states the task only and never names the pack it should load; mismatches are recorded as
      boundary evidence rather than smoothed over. **Done**: matrix checked statically; probe 1 loaded the pack (a docs
      edit touching the tool list — recorded as a row qualifier), probe 2 loaded it before the hypothesis on the re-run,
      probes 3 and 4 passed, and probe 5 (an unrelated editorial task) loaded no pack — the negative control.
- [x] 1.6 Run the gate controls **once, on the final candidate**: point `verifyInstallCommands` at the pack, and make
      `verifyDocumentedPins` scan `AGENTS.md`, `README.md` **and** the pack through a rules object that keys each
      document by project-relative path; corrupt one chart version and one Paketo pin in the pack, run each task, and
      confirm the failure names the chart/pin; revert. Verify: the old document set fails the new committed test
      (positive control), all three documents are in the scanned set, two `SKILL.md` documents are both scanned, and the
      misplaced-marker case fails in the pack. Re-run a mutation only after the candidate changes in a way that affects
      that wiring or the checked text — not after every read-only check. **Done**: both gates pass naming the pack; a
      wrong version and a wrong pin fail each task; dropping the pack from `docs.from` fails `verifyDocumentedPins`
      ("does not scan: …"), and the `LIVE_DOCS` test stays as the set's change-detector.
- [x] 1.7 Apply the entry gate: IF any fixture or control fails, THEN stop with the real root untouched, and report the
      failing case, the pack, and a proposed boundary change to the owner. Do not ship a partial move, do not re-route
      an obligation to a pack that will not ship, and do not choose a reduced unit silently. **Done**: the entry gate
      passed with probe 5 supplying the negative control and probe 1's qualifier recorded; no boundary change was
      needed.

## 2. Apply to the real tree

- [x] 2.1 Create `.opencode/skills/deployment-operations/SKILL.md` from the clone copy the preflight passed, and verify
      the real file is byte-identical to it. After an OpenCode reload, confirm the skill loads by its exact name and its
      resolved metadata carries the `name` and `description` from the file — a definition edited mid-session is served
      from the pre-change copy until then, so the reload is part of the check. **Done**: the real pack is byte-identical
      to the clone copy; the refreshed skill inventory lists `deployment-operations` with its description.
- [x] 2.2 Apply the root edits: remove the seven moved spans, insert the classification row and preamble change, the
      four section pointers, the Gotchas "**Moved:**" line, the `## Build & Test` Helm pointer and DB-scripts pointer,
      and the documented edits 2–6, 11, 16 and 18. Apply the Kafka note's relocation (transfer-manifest row 8) and
      `jvm-testing`'s `Source:` line edit (documented edit 20) in the same step. Verify every removed range is present
      exactly once across its destination **modulo the documented edits applied in 1.2** (compare against the clone's
      edited files, not the raw source ranges), and every pointer reaches its section and obligation. **Done**: the
      seven spans removed, the Kafka note relocated, `jvm-testing`'s `Source:` extended, every pointer resolves.
- [x] 2.3 Retarget the two gates: `build.gradle.kts`'s `verifyInstallCommands` input, and `verifyDocumentedPins`'s
      `docs.from` **gaining** the pack while keeping `AGENTS.md` and `README.md`; one scanned-document rules function
      per gate (keyed by project-relative path) and its committed test cases (all three documents present; a bad
      version/pin in the pack reported; a document set missing the pack failing; two `SKILL.md` documents both scanned).
      Update the KDoc in `InstallCommandRules`, the KDoc and `agentsFile` property (renamed `sourceFile`) in
      `VerifyInstallCommandsTask`, and the task's `description` so none still names `AGENTS.md` (documented edits
      12–14). Verify `-p build-logic test` fails on the old document set and passes on the new one. **Done**:
      `sourceFile`/`sourceLabel`, `docs.from` keeps `AGENTS.md` and `README.md` and adds the pack, path-keyed;
      `-p build-logic test` passes.
- [x] 2.4 Sweep every site that enumerates the pack set or the moved surface: `.opencode/agent/lesson-capture.md`
      (frontmatter description and the body clause), `openspec/config.yaml`'s context block (pack list, Docker-images
      and Deployment restatements, the `./db.sh init` line), `README.md` where it names the packs or states a deployment
      command the move changed, `docs/guidance-evidence/documentation-and-durable-claims.md` (documented edit 15),
      `.opencode/skills/gradle-build/SKILL.md`'s cross-reference and Subject line (documented edit 1), `README.md`'s
      verification-command comment (documented edit 17), `gradle-build`'s update-check list pointer (documented edit
      19), `.opencode/skills/jvm-testing/SKILL.md`'s `Source:` line (documented edit 20), `AGENTS.md`'s capture
      destination clause, and the auditor/command describers that list destinations. Derive the site set rather than
      reading this list: grep `verifyInstallCommands`, `verifyDocumentedPins`, `AGENTS.md`, and "command surface" across
      `AGENTS.md`, `README.md`, `docs/`, `.opencode/`, and `build-logic`, and resolve every hit as an edit or an
      unchanged site. **Done**: lesson-capture, config, README, the evidence record, `gradle-build`'s three sites, and
      the capture clause swept.
- [x] 2.5 Run `spotlessApply` as the last write of the candidate phase, then `spotlessCheck`. Verify the pack and the
      root format cleanly and no inline code span was split. **Done**: `spotlessApply` ran after the last text edit and
      `spotlessCheck` passes.
- [x] 2.6 Park the stale `#755` location this change passes by: add a dated `docs/ideas.md` entry recording that the
      guidance-evidence closure clause still says `## Build & Test` while the citation moved to the `gradle-build` pack
      at `6518d081`, with its trailing status tag. Verify the entry exists and the design's "Deliberately left"
      paragraph names it. **Done**: parked in `docs/ideas.md` under 2026-10-22 with its status tag.

## 3. Verify the real tree (read-only)

- [x] 3.1 Run the manual 120-character check over the changed non-formatter files (the rules object, the build script,
      the config), and record any inherited over-length line inside a moved inline span as accepted. **Done**: the
      120-character check over the non-formatter files is clean; the round-ledger rows are the documented one-line
      exemption.
- [x] 3.2 Run `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` and, where Docker is available, the full
      `./gradlew check`. Verify both pass with the same members as before the change. **Done**:
      `./gradlew :check -PskipITs -Pcoverage.gate.enabled=false` passed (54 tasks, 34 executed, 53s).
- [x] 3.3 Run the lost-term sweep over `AGENTS.md`, `README.md`, `docs/`, `openspec/`, and `.opencode/`: no moved
      identifier is reachable only from the deleted root text; the five unchanged in-pack references still resolve and
      the one edited sentence (item 11) names all three documents; documented edit 1 resolves in `gradle-build`; the
      Kafka note is reachable in `jvm-testing` and pointed at from the pack. **Done**: the identifier sweep resolves
      every token; the in-pack references resolve; edit 1 resolves in `gradle-build`; the Kafka note is reachable in
      `jvm-testing` and pointed at from the pack.
- [x] 3.4 Re-run a gate positive control on the real tree only if the candidate changed after the preflight in a way
      that affects that wiring or the checked text (a deliberately wrong version or pin in the pack, failing its task,
      then reverted); confirm `verifyCapturedMarkers` and `upstreamReferences` report the moved markers and references.
      **Done**: the mutation controls ran on the final wiring in the clone (the code did not change afterwards); on the
      real tree both gates pass in `check` and the marker/upstream scans report the moved text.
- [x] 3.5 Re-measure and record in `design.md`: `wc -c` for the root, the new pack, and `jvm-testing`'s delta, the
      retained-pointer total, the added metadata, and one complete load per task class. Report classes, not an
      aggregate. **Done**: root 261,382 B (−31,741, −10.8%), pack 34,908 B, `jvm-testing` +467 B.
- [x] 3.6 Run `openspec validate --all` and confirm the change validates with both deltas (`infra-image-versions` and
      `agent-skills`), each preserving its main-spec scenario names and order. **Done**: `openspec validate --all`
      passes 27 items with both deltas.

## 4. Review, capture, and close-out

- [x] 4.1 Re-derive the claims table after implementation: re-run each row's command against the implemented tree, and
      delete or correct any row that no longer reproduces. Verify every figure in `design.md` matches the re-derivation.
      **Done**: the claims table re-derived after implementation; the measured rows replace the estimates, every row
      reproduces on the implemented tree or its anchored pre-change blob.
- [x] 4.2 Run the implementation quick-review loop with reviewer-session continuity until a round reports no new
      observations, passing each prior round's finding instances; append each round's row to `docs/review-rounds.tsv` as
      the round's report arrives. **Done**: implementation rounds 12–20 in the same reviewer session, one row each in
      `docs/review-rounds.tsv`; round 20 reports no new observations. The deep review's should-fix and the capture's
      routed instance were fixed inside the loop.
- [x] 4.3 Derive the deep-review routing floor with
      `python3 scripts/review-routing.py --change extract-deployment-operations-guidance`, decide at or above it, and
      record the outcome and its signals; run a `REQUIRED` review before completion. **Planning-stage derivation:
      `RECOMMENDED`, signal `repeated-class` (8 of 11 proposal rounds repeated a prior class).** Resolution: the deep
      review is deferred to the implementation stage, where the floor is re-derived from the final diff — at proposal
      time there is no implementation to review, and the repeated-class signal came from drafting defects this loop has
      since fixed. **Implementation-stage derivation: `RECOMMENDED`, `repeated-class` (15 of 20 rows); resolved to run
      the deep review before the manual pass, because the loop's repeated classes and the two gate retargets are exactly
      what that pass exists to check.** **Done**: `review-thorough` returned 1 should-fix — `verifyInstallCommands` had
      no committed control, so a document yielding no install command passed vacuously — fixed with
      `InstallCommandRules.noCommandsReason` and a control run (removing the block fails the gate naming the pack);
      three nitpicks: edit 18's second half applied, the Paketo-pin derivation reworded to a pin-form grep, and the
      scenario-title artefact recorded.
- [x] 4.4 Run the `lesson-capture` subagent with the diff, the round rows, the change dir, and the existing guidance;
      apply its durable proposals to the owning destinations, or record the skip and its reason. **Done**: one durable
      proposal applied — the `check-and-test-authoring` mode "An extraction that yields nothing is not a pass" (+13
      lines in that skill; `AGENTS.md` unchanged), which also exposed the pins gate's zero-yield hole; its routed
      instance was fixed in this unit (`DocumentedPinRules.unstated` + `PACK_DOC`), with a control run. No rule was
      retired.
- [x] 4.5 Ask the owner for the manual review pass; commit, push, archive, and merge only after the owner approves.
      **Done**: the owner approved on 2026-10-22 ("Одобряю").

## 5. Planning records (this unit)

- [x] 5.1 Run the pre-write derivation pass and record it as `design.md`'s claims table. **Done**: the table was
      re-derived from the final edit list, every row about an edited artifact anchors to `git show e42213a8:<path>`, and
      the `scripts/claim-scan.py` candidates are resolved by class in the dismissals paragraph.
- [x] 5.2 Record the proposal-round reviewer session identifier, transition, and classes in `docs/review-rounds.tsv`.
      **Done**: rounds 1–20 under session `ses_ea0d777a6ffe92MLRuqXDZszZ8`, `created` for round 1 and `continued` for
      2–17; round 17 is clean (`-` classes, 0 compliance misses). Rounds 9–11 are the post-advisor revision, 12–17 the
      implementation loop. task 2.4) and name the site, so the next capture proposes into the right owner. **Done**: the
      capture destination list now names `deployment-operations` in `AGENTS.md` and both `lesson-capture` clauses (task
      2.4).
