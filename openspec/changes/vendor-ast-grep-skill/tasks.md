# Tasks

## 1. Vendor the skill

- [x] 1.1 Read the upstream tree at the version being vendored and copy it verbatim. **Done**: cloned
      `ast-grep/agent-skill` at `f2175af`, copied `ast-grep/skills/ast-grep/` (its `SKILL.md` and
      `references/rule_reference.md`) into `.opencode/skills/ast-grep/`; `diff -r` against the clone reports no
      difference.
- [x] 1.2 Keep the vendored tree out of the formatter and out of the project-owned marker and executable-bit checks.
      **Done**: `.opencode/skills/ast-grep/**` joins the Spotless markdown target's `targetExclude`, and
      `.opencode/skills/ast-grep/` joins `VENDORED_SKILL_PREFIXES` in `scripts/commit-hygiene.py`.
- [x] 1.3 Give the new exclusion its committed control. **Done**:
      `test_a_vendored_skill_file_is_out_of_the_marker_scope` in `scripts/test-commit-hygiene.py` asserts the vendored
      file is out of `marker_paths` while a project-owned sibling stays in; it fails when the prefix is removed.
- [x] 1.4 Record the second set's provenance. **Done**: the `AGENTS.md` vendored-skills entry names the repository,
      plugin version, commit, license, and upstream path, plus the two files' git blob hashes — the set's anchor, since
      its upstream publishes no release tag (verified against the `sha` the contents API serves at that commit).

- [x] 1.5 Anchor the existing vendored set to its upstream ref, since the requirement now names one. **Done**: every
      vendored AxonIQ file matches the upstream tree at `518acd865a` (the 0.2.2 merge; `git hash-object` per file
      against `gh api …/git/trees/518acd865a?recursive=1`), and the six files that differ at the 0.2.2 bump commit
      `edfb88b` show the ref is load-bearing. Recorded in `AGENTS.md`.

## 2. Make the CLI part of the toolchain

- [x] 2.1 Add the optional probe to the doctor's declaration. **Done**:
      `ast-grep|optional|tool|yes|ast-grep|--version|`, with a macOS and a Debian install hint.
- [x] 2.2 Document it in the README's Prerequisites table and its optional-tools paragraph, and map the row in the
      agreement check. **Done**: the row and the `scripts/test-doctor.sh` mapping are in place;
      `./scripts/test-doctor.sh` reports 14 probes with the required set unchanged.
- [x] 2.3 Mention the capability in the README's agentic-process section. **Done**: a sentence naming the two vendored
      skill sets and when the agent reaches for the structural one.

## 3. State the tool choice

- [x] 3.1 Add the rule to `AGENTS.md`'s `### Evidence and search`. **Done**: a structure-dependent search loads the
      skill and runs `ast-grep run`/`scan`, `rg` keeps text, identifiers, and paths, the qualified-versus-bare form is
      named as a pattern's own choice, a zero result needs the pattern proven first, and the no-types/no-behavior
      boundary is stated.

## 4. The spec delta

- [x] 4.1 Write the delta on `showcase/quality/agent-skills`. **Done**: three `MODIFIED` requirements — availability now
      covers both sets, provenance becomes per-set with the commit a tagless upstream needs and the verbatim-copy rule,
      and the auditor's exclusion covers the vendored skill trees; `openspec validate` accepts it `--strict`.
- [x] 4.2 Sweep every enumeration of the vendored set, since a set gained a member. **Done**: `AGENTS.md`'s formatter
      scope, its auditor provenance partition, its placement and tooling-setup entries and the vacuous-glob incident now
      name the skill trees rather than one set; `.opencode/agent/agents-auditor.md` names both sets and its advisory
      class names "a vendored skill"; `UpstreamReferenceScope` excludes `ast-grep` with a test case; the marker task's
      input file tree excludes it; and the `AGENTS.md` vendored-skills bullet lists the four declarations — the Spotless
      `targetExclude`, the marker task's file tree, `VENDORED_SKILL_PREFIXES`, and `EXCLUDED_SKILL_PREFIXES`.
- [x] 4.3 Write the deltas the sweep owes. **Done**: `showcase/quality/code-quality` (the formatter's exclusion) and
      `showcase/quality/commit-hygiene` (the executable-bit scan's exclusion) carry `MODIFIED` blocks whose descriptions
      name the skill trees rather than one set, and `agent-skills` gains a third block for the auditor's exclusion.
- [ ] 4.4 Refresh the capability's `## Purpose` at archive time, in the archive commit: it currently frames the vendored
      sets as the Axon 4→5 recipes alone, which the second set falsifies. A delta cannot carry a Purpose.

## 5. Verification

- [x] 5.1 Prove the integration on characteristic queries, with the text cross-check where one exists. **Done**: the
      battery in `design.md` — `@ConfigurationProperties` 5 (rg 5), `@Bean` 56 (rg 56), the `@Test`-calls-`block()`
      relational query at 8 method sites in 4 files, `useMutation` 4 in the web UI.
- [x] 5.2 Prove a zero result before reading it as absence. **Done**: `@KafkaListener` is 0 in the repository,
      `rg -c KafkaListener --glob '*.java'` names no file, and the same pattern matches a snippet piped through
      `--stdin`.
- [x] 5.3 Verify discovery, not only the file's presence. **Done**: a private server in the project directory lists 35
      skills including `ast-grep` at `.opencode/skills/ast-grep/SKILL.md`, against a 34-skill baseline taken before the
      tree was copied.
- [x] 5.4 Verify the probe in both directions. **Done**: the doctor reports `ast-grep satisfied 0.51.0`, and `missing`
      with its install command under a `PATH` that lacks it.
- [x] 5.5 Run `./gradlew spotlessApply`, `spotlessCheck`, `verifyCapturedMarkers`, `testCommitHygiene`,
      `testDoctorCharter`, `openspec validate --all`, and the Docker-free `:check`; run the manual 120-character check
      over the changed set. **Done**: all green, plus `:build-logic:test` and `upstreamReferences` since the scope
      object and the marker task's inputs changed; the 120-character check is clean over the changed files this unit
      writes (the over-length lines it reports are table rows and a script it did not touch).

## 6. Review, capture and the manual pass

- [x] 6.1 Run the pre-write derivation pass and record its claims table in `design.md`, seeded by
      `scripts/claim-scan.py`, with every candidate resolved into a row or a class-level dismissal. **Done**: the table
      is `design.md`'s "Verification and claims"; the scanner's candidates are dismissed by class beneath it, and each
      row's command was re-run against the working tree.
- [x] 6.2 Run a `review-quick` pass over the proposal and the implementation, appending each round's row to
      `docs/review-rounds.tsv` and running the ledger checkpoint after each append; repeat until a round reports nothing
      new. **Done**: thirteen rounds, recorded in `docs/review-rounds.tsv`; the last reported nothing new. Two appends
      (rounds 10 and 11) were deferred into the following round, which the ledger checkpoint caught both times — the
      rows are now written as each report arrives.
- [x] 6.3 Derive the deep-review routing outcome, decide at or above the floor, and record the outcome and its signals
      here. **Done**: `scripts/review-routing.py --change vendor-ast-grep-skill` → `RECOMMENDED`, signal
      `[repeated-class] 4 rows repeat a prior class`; resolved as run. Pass 1 of the two-pass budget reviewed the change
      against its delta specs, tasks and conventions with `gradle-build` and `check-and-test-authoring` loaded: **2
      nitpicks, no blocking or should-fix findings**. N1 (task 6.2 unticked while the ledger records six rounds) —
      `FIXED_AND_VERIFIED`, ticked above. N2 ("SHALL work without its CLI" too strong for a normative sentence) —
      `FIXED_AND_VERIFIED`, now "SHALL remain discoverable without its CLI being installed". The pass also re-derived
      the claims rows independently (Q1 5/rg 5, Q2 56/rg 56, Q5 0, Q6 824 bare vs 0 qualified).
- [x] 6.4 Run the `lesson-capture` subagent and record its applied net delta or its skip reason with the reason.
      **Done**: two proposals applied, net `AGENTS.md` delta 0 lines. **A1** — the doc-consistency sweep's "a set that
      gained a member" item now says to grep the whole working tree (untracked files included) instead of naming six
      directories, because the four declarations this unit had to update live in `build.gradle.kts` and `build-logic/`,
      which the list did not name; marker appended. **R1** — the `Formatting` convention's parenthetical member list is
      dropped, so set membership has one definition, in the vendored-skills bullet. Its withheld proposal (a local
      OpenCode v2 discovery recipe) stays in this change's record rather than `AGENTS.md`: the obligation is already
      gated by the CI inventory probe, and the recipe rests on one occurrence.
- [x] 6.5 Ask the owner for the manual review pass; commit, push, archive and merge only after approval. **Done**: the
      owner approved on 2026-10-22 after the manual review request and the two follow-up consultations.
- [x] 6.6 Action the independent consultation the owner requested (`/consult`). **Done**: the `technical-advisor`
      confirmed one problem — the proposal claimed ast-grep is never invoked by the build, while `check` →
      `testDoctorCharter` → `scripts/test-doctor.sh` → `scripts/doctor.sh` runs the declared `--version` probes — and
      nothing else; every other question it was asked came back sound. Applied at three sites: the proposal's Impact
      sentence, the design's Non-Goal, and the claims row, which is retitled "The skill tree is on no build path" now
      that the CLI has one build-time execution. Verified by quick rounds 7–9 (two follow-on over-claims in the new
      sentences, both fixed) and by a control: with every tool present except ast-grep, the doctor reports it missing
      and `./scripts/test-doctor.sh` exits 0. Deep-review pass 2 is not run — pass 1 returned no blocking or should-fix
      finding, and what followed it is two nitpick fixes and one factual claim correction, each verified by the quick
      loop. Two follow-up consultations then shaped the provenance record: the first ended the clone-dependent evidence
      (the copy is now anchored by its commit and, for the two-file set, by blob hashes, both compared through GitHub's
      tree and contents API rather than a clone — which also anchored the AxonIQ set, whose record had no ref, to
      `518acd865a` after every vendored file matched that tree and only 92 of 98 matched the version-bump commit); the
      second narrowed the rule so that checking the current copy and updating to a new snapshot are separate
      obligations, neither constraining how the upstream content is obtained, and both refresh instructions now name the
      hashes they must update. Rounds 14–15 verified the result.
