# Tasks

## 1. Planning artifacts

- [x] 1.1 Establish the reproducible baseline: the Git revision, the clean working tree, `AGENTS.md`'s bytes and lines,
      and its size by top-level section (design.md, Context).
- [x] 1.2 Recover the prior decisions that bound this unit: the two shipped extractions, the three retained "safe to
      load on demand" rules, the owner's deliberate retention of the capture gate list, and the three gates that read
      `AGENTS.md` (design.md, "Prior decisions recovered").
- [x] 1.3 Verify the loading behavior from the repository's own tooling and the opencode documentation: root-only
      traversal, manual pointers, the widening `instructions` array, on-demand skills, the permissive skill permission,
      and the project command surface (design.md, "How the loading actually works").
- [x] 1.4 Split the check/test cluster by the decision each bullet governs, and record that the proposed trigger reaches
      only the eight check-authoring bullets (design.md, "Why one trigger cannot cover the whole cluster").
- [x] 1.5 Classify the candidate spans and record the candidate decision table, including every examined-and-rejected
      row with its reason (design.md, "Candidate decision table").
- [x] 1.6 Record the recommended unit with each move's obligation inventory, retained root text, destination structure,
      and destination additions (design.md, "Recommended first unit").
- [x] 1.7 Verify the capture contract (`lesson-capture.md`, the `agent-skills` capture requirement) and design the
      maintenance loop with its two clause edits and the delta (design.md, "The maintenance loop").
- [x] 1.8 Choose the delta's ownership and record why the destination and routing obligations sit in `agent-skills`
      rather than `code-quality` (design.md, "Ownership of the delta").
- [x] 1.9 Record the retrieval-path validation with each path's evidence class, the skill's visibility and permission
      facts, and the implementation-time checks (design.md, "Retrieval-path validation").
- [x] 1.10 Record the size estimate with its basis, the alternatives with reproducible or qualitative support, the
      risks, and the rollback (design.md).
- [x] 1.11 Write the `agent-skills` delta — the `ADDED` destination requirement and the `MODIFIED` capture requirement
      copied whole — and confirm no other capability spec needs one.
- [x] 1.12 Run the pre-write derivation pass and record the claims table with its dismissals (design.md, "Claims").

## 2. Implementation

- [x] 2.1 Create `.opencode/skills/check-and-test-authoring/SKILL.md` carrying the eight moved modes verbatim, one `##`
      section each, with the invariant section and an intro stating the routing criterion: an authoring mode is recorded
      here, a correction updates its section, and an evidence-hygiene rule belongs in `AGENTS.md`'s
      `### Evidence and search`, never here. Confirm its `name` matches its directory and its `description` is within
      the 1,024-character cap.
- [x] 2.2 In `AGENTS.md`, move the five evidence-hygiene bullets under a new `### Evidence and search` heading, keeping
      their text verbatim and in order.
- [x] 2.3 Replace the eight moved check-authoring bullets with the three retained bullets: the invariant, the
      control-is-a-committed-test rule, and the trigger that names the skill.
- [x] 2.4 Add the eight marked items to `.opencode/agent/diagrammer.md` — comment-column alignment, the block-diagram
      evidence, the body-pipes evidence, the `viewscreen` cap with the toolbar width and `mermaid-js/mermaid#7117`, the
      `diagramPadding` two-view reserve, the "old diagram is not the contract, re-derive from the code" rule, the
      fallback label re-check, and the lifecycle worked example — and retarget its "recorded in `AGENTS.md`" sentence to
      its own text.
- [x] 2.5 Move the three `mermaid`-fence provenance markers onto the `diagrammer.md` rendering section, and keep the
      vision bullet's marker on the retained fence bullet.
- [x] 2.6 Delete the five moved diagram bullets from `AGENTS.md`'s `## Documentation & durable claims`, and replace the
      three `## Conventions` diagram bullets with the seven retained ones design.md's "Retained root text" drafts
      (delegation for every edit, the definition's rules, mapping confirmation, the un-gated fence, the `http(s)`-only
      rule, measurement-as-hypothesis, and the human-edit caution).
- [x] 2.7 Replace `## Prerequisites`' body with the five retained bullets (toolchain, README ownership, the
      required-versus-optional map, hooks, the doctor out of `check`).
- [x] 2.8 Replace the `check` run line's task-name list with the three-line summary that points at `build.gradle.kts`'s
      `check` block.
- [x] 2.9 Edit `.opencode/agent/lesson-capture.md`: the proposal gives the owning artifact, chosen by the decision the
      lesson governs — an authoring mode goes to the skill, a correction updates that section, an evidence rule goes to
      `AGENTS.md`'s `### Evidence and search`, and a mixed lesson is split — and names the existing rule it extends (a
      bullet, or a mode already in the skill) or states that none does.
- [x] 2.10 Apply the six root capture clauses in `AGENTS.md` (procedure step 2's destination, the "existing rule it
      extends" decision point, "the target rule", the marker guard's "in `AGENTS.md`", step 4's "the rule it would
      extend", and the marker paragraph's "rule … end-of-rule marker"), preserving the promotion gate, the
      merge-versus-accretion discipline, the decision-difference test, and the growth bound.

- [x] 2.11 Retarget the marker guard: `marker_paths`/`MARKER_GLOBS` in `scripts/commit-hygiene.py` (project-authored
      agent and skill definitions, vendored and generated trees excluded), the `verifyCapturedMarkers` inputs in
      `build.gradle.kts`, the pre-commit hook's header comment, and a known-bad test for a misplaced marker in a skill
      file.
- [x] 2.12 Add the `commit-hygiene` delta — the pre-commit-guard and marker-placement requirements copied whole, scoped
      to the project-authored guidance files, plus the misplaced-marker-in-a-project-file scenario.

## 3. Implementation-time verification

- [x] 3.1 **Obligation preservation**: for each move, re-read the removed span and confirm every imperative, warning,
      exception, identifier, gate condition, and `captured:` marker is either in the retained root text or in the named
      destination section; record the section each landed in.
- [x] 3.2 **Pointer resolution**: confirm each retained pointer names a path or a build-file location that exists, and
      that no pointer claims more than its target holds.
- [x] 3.3 **Retrieval paths**: walk the eight scenarios from design.md against the edited tree, and confirm the five
      evidence rules are still at root and read before their decisions.
- [x] 3.4 **Skill availability**: confirm the new skill is listed with its description in a fresh session, that loading
      it returns the whole body, that a subagent which authors tests can load it, and that the repo's OpenCode
      definition probe (`build`) resolves it with its metadata.
- [x] 3.5 **Removed distinctive tokens**: grep the removed spans' identifiers across `AGENTS.md`, `README.md`, `docs/`,
      `openspec/specs/`, `.opencode/`, and `scripts/`, and confirm each survives only where intended — including the
      five evidence rules' identifiers (`:(glob)`, `core.quotePath`, `libs.hibernate.validator`, `mv` state space) which
      must stay at root.
- [x] 3.6 **Leftover fragments**: sweep for a moved bullet's tail fused onto its replacement (a partial line, a dangling
      parenthetical, an orphaned `captured:` marker) in every edited file.
- [x] 3.7 **Measured reduction**: `AGENTS.md` 356,723 B / 3,623 lines → 337,508 B / 3,451 lines after the moves (−19,215
      B, −5.39%; the estimate was ≈ −19.7 KB), and **338,964 B / 3,463 lines** after the capture and the marker-guidance
      edit (+1,456 B, +12 lines) — **−17,759 B (−4.98%)** for the unit. The skill body is 14,023 B on demand; its
      description adds 293 B to every session, so the net always-loaded effect is ≈ −17,466 B (−4.90%).
- [x] 3.8 **Formatting and checks**: run `./gradlew spotlessApply` after the final edit, then `./gradlew spotlessCheck`,
      `openspec validate --all`, and the Docker-free `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`; confirm
      `verifyCapturedMarkers`, `testDoctorCharter`, `verifyInstallCommands`, and `verifyDocumentedPins` stay green.
- [x] 3.9 **Subagent reliance**: confirm `.opencode/agent/review-quick.md` and `review-thorough.md` do not rely on the
      moved text, or name the skill where they do.
- [x] 3.10 **Capture loop**: simulate four captures against the edited tree — a new authoring mode (lands in the skill,
      names the mode it extends or states that none does, `AGENTS.md` gains nothing), a correction (updates the existing
      mode's section, adds no second rule), an evidence rule (lands at root and is not duplicated in the skill), and a
      mixed lesson (split, neither destination restating the other).
- [x] 3.12 **The `commit-hygiene` blocks, re-diffed**: the delta's requirements differ from the main spec only in the
      project-authored-files scope and the added scenario, with every existing scenario retained.
- [x] 3.11 **The `MODIFIED` block, re-diffed**: regenerate the block from the then-current main requirement if it moved,
      and re-run the line-level comparison — one hunk expected, at the two clauses, with every other line identical and
      20 scenarios on both sides.

## 4. Review gate

- [x] 4.1 Run `review-quick` over the planning artifacts, continuing the same reviewer session across re-runs, and fix
      every finding until it reports no new observations.
- [x] 4.2 Append each round's row to `docs/review-rounds.tsv` with the session identifier, transition, and any
      replacement's reason.
- [x] 4.3 Derive the deep-review floor with
      `python3 scripts/review-routing.py --change reduce-agents-md-loaded-surface`. Outcome: **`REQUIRED`**, signal
      `requirement-block-added` (the delta's added requirement).
- [x] 4.4 Run the implementation-stage `review-quick` round and the routed deep review (`REQUIRED`,
      `requirement-block-added`), and fix until clean: rounds 15–17 clean the quick loop; the deep pass raised 2
      should-fix and 5 nitpicks, all applied — the marker scan retargeted to the project guidance files (with a
      known-bad test), the skill's opening sentence, the marker census, `lesson-capture.md`'s no-edit clause, the
      historical `diagrammer.md` example, the trimmed routing paragraph, and the archive-time Purpose-refresh task.
- [x] 4.5 Run the capture, or record the skip and its reason, with the applied net `AGENTS.md` delta. Ran: 3 additions
      and 1 replacement applied to `### Workflow: Extract an `AGENTS.md` section` and the marker bullet — the split
      rule, the file-scoped-gate rule, the capture-destination rule, and the widened marker-guard sentence; +11 lines
      against a 245-line deletion.
- [x] 4.6 Request the owner's manual review pass; do not commit, push, open a PR, archive, or merge before it.
- [ ] 4.7 At archive, refresh the `agent-skills` Purpose to name the project-authored guidance skills (the
      `check-and-test-authoring` skill, and the existing `setup-*` skills) alongside the vendored set — a delta cannot
      carry a Purpose.
