# Tasks

## 1. Planning artifacts

- [x] 1.1 Establish the reproducible baseline: the revision, `AGENTS.md`'s bytes and lines, and its size by top-level
      section (design.md, "Reproducible baseline").
- [x] 1.2 Recover the prior decisions that bound this unit and reconcile the parked `docs/ideas.md` entries; record that
      none is implemented or removed (design.md, "Prior decisions recovered" and "Parked ideas reconciled").
- [x] 1.3 Record what the 2026-10-19 audit rejected and which of its clauses bind this design (design.md, "What the
      newest audit already rejected").
- [x] 1.4 State the objective and the four quantities, and record that total maintained content is a cost rather than a
      veto (design.md, "The four quantities").
- [x] 1.5 Measure every Gotchas subsection and record the census with its arithmetic checked (design.md, "Census of the
      Gotchas subsections").
- [x] 1.6 Give **all 30 rules** of the two pilot subsections a disposition, with quoted anchors, rule bytes, and a
      measured or estimated saving (design.md, "Rule-by-rule dispositions"). Coverage: 19 rewrite, 6 relocation, 4 no
      separable material, 1 rejected, 0 unassessed.
- [x] 1.7 Draft the three scope-preserving rewrites for the cases whose examples define scope, and record for each
      whether the shortened text makes the same decisions (design.md, "Drafted rewrites").
- [x] 1.8 Record the two rules that could not be relocated, with the specific preservation or retrieval failure
      (design.md, rows 19 and 21, and "Where the scope could not be stated").
- [x] 1.9 Verify the existing destinations before proposing a new one: the marker census, the per-rule coverage probe,
      and the sweep of the other durable-document classes (design.md, "Destination verification").
- [x] 1.10 Design the pairing representation: a per-rule `evidence:` token, a single-line section heading, and a `Rule:`
      paragraph as the key, with the five cases worked — untouched rule, valid entry, deleted entry, orphaned entry, and
      renamed rule (design.md, "The pairing token" and "The five cases, worked").
- [x] 1.11 Draft both record files in full and measure them at their formatted size (design.md, "Complete drafts").
- [x] 1.12 Inventory the five upstream references R2 moves, and record the treatment of each against the closure
      report's input set and its own retirement condition (design.md, "Upstream references the relocation moves").
- [x] 1.13 Reassess the preservation contract, separating operational identifiers from historical ones and stating the
      duplication boundary (design.md, "Operational and historical identifiers").
- [x] 1.14 Reassess the preservation and removal requirements and record that relocation is not removal, that the only
      prohibition is the ownership gap, and that the smallest delta is one `MODIFIED` block plus four `ADDED`
      requirements (design.md, "Preservation policy examined").
- [x] 1.15 Write the `agent-skills` delta: the `MODIFIED` audit-scope requirement and the four `ADDED` requirements,
      with every scenario (`specs/showcase/quality/agent-skills/spec.md`).
- [x] 1.16 Compare retention, rewriting with existing sources, rewriting with the new reference, and the lossy trim in
      the four quantities, and record the recommendation and the follow-up table (design.md, "Alternatives", "Size
      accounting", and "Recommendation").
- [x] 1.17 Run the pre-write derivation pass and record the claims table with its kind-level dismissals (design.md,
      "Claims" and "Seed and dismissals").

## 2. Investigation verification

- [x] 2.1 Confirm no gate reads the pilot spans: `grep -c 'helm install'` over
      `sed -n '2097,2260p;3115,3423p' AGENTS.md` returns `0`, the `paketo` matches are name and issue references, and
      every draft keeps its marker.
- [x] 2.2 Re-run the census block and the three probe blocks from the repository root and confirm each prints what the
      tables state.
- [x] 2.3 Verify each of the eight drafts against its current span, and confirm the seven converted ones plus their
      tokens total −4,931 bytes.
- [x] 2.4 Confirm each `After` block and both record files are formatter-stable under the build's Prettier configuration
      (3.9.10, `printWidth` 120, `proseWrap: always`).
- [x] 2.5 Confirm every record section's `Rule:` paragraph repeats a live rule's leading text (whitespace-normalized),
      and that both files are measured at their formatted size.
- [x] 2.6 Record the duplicated word the pilot found in the em-dash rule as a separate follow-up (task 6.3); it is not
      part of this unit.

## 3. Review gate

- [x] 3.1 Run `review-quick` over the planning artifacts, continuing the same reviewer session across re-runs until it
      reports no new observations. Session `ses_ea962149bffeXQRQtWwcvba5uQ`; rounds 1–9 on the earlier draft, 10–17
      after the first redirection, and a further round after this refinement.
- [x] 3.2 Append each round's row to `docs/review-rounds.tsv` with the session identifier, the transition, the classes
      raised, and any replacement's reason.
- [x] 3.3 Derive the deep-review floor with
      `python3 scripts/review-routing.py --change separate-agents-md-operational-from-evidence` and record the outcome
      and its signals. Earlier draft: `RECOMMENDED`, `repeated-class`. After the revision: **`REQUIRED`**, signal
      `requirement-block-added`. Re-derive after this refinement.
- [x] 3.4 Run the deep review when the routing outcome is `REQUIRED`, or record the explicit resolution when it is
      `RECOMMENDED`. Ran five times — `ses_ea94145d0ffeRSGoFB2ajrVit4` and `ses_ea91366fbffeXUeSEB4SeabeEl` on the
      earlier revisions, `ses_ea8d3ecf5ffeAuiTk0rKIjkp2o` on the refinement (six findings),
      `ses_ea899d90fffenW66Y1YsZk6HKe` on the final consistency pass (two should-fix), and
      `ses_ea888cc06ffeQcgLj4IpRAOjf2` on the record-boundary correction (one should-fix); every finding is applied and
      re-verified.
- [x] 3.5 Re-run the quick review over the refined design, delta, proposal, and tasks until it reports no new
      observations. Rounds 18–28 on the same session; clean at 23, 26, and 28 after the final consistency pass, with
      every finding applied and its row appended.
- [x] 3.6 Run the per-unit `lesson-capture` decision once the review is clean and before the manual pass, and record the
      applied net `AGENTS.md` delta or the skip and its reason. Ran (session `ses_ea8c3a191ffeTYxIsL4O1GAaBV`) and
      decided in design.md "Capture decisions": **accepted and riding this unit** — the "point-in-time narrative belongs
      in the change archive" sentence becomes "belongs in a dated record" (+118 B), and the extraction gate decision
      becomes per item (+153 B); **dropped as already covered** — a created artifact's size measured from its draft,
      which the pre-write pass's "derive each figure from the mechanism that produces it, re-derived after that
      mechanism's last edit" governs; **dropped as not established** — the identifier-leaves-root generalisation,
      because step 5 governs extraction (destination read at the trigger) while this unit's record is not read before
      acting; **parked** — the extraction-pointer generalisation, by the owner's direction (task 4.7). Net `AGENTS.md`
      delta **+271 B**, all of it in the two accepted corrections, riding this unit's branch rather than a docs PR.
- [x] 3.7 Request the owner's manual review pass. Do not commit, push, open a PR, archive, or merge before it. Asked at
      the end of this refinement turn; nothing is committed, pushed, or archived.

## 4. Implementation

- [x] 4.1 Create `docs/guidance-evidence/git-and-repository-workflow.md` exactly as drafted — three sections,
      `occupied-path`, `archive-incomplete`, `staged-set-sweep` — each with its single-line heading, its `Rule:`
      paragraph, its episode, and its markers.
- [x] 4.2 Create `docs/guidance-evidence/documentation-and-durable-claims.md` exactly as drafted — four sections —
      including the upstream-reference episode and the closure-tracking statement (the per-reference inventory is
      planning evidence in design.md "Upstream references the relocation moves").
- [x] 4.3 Rewrite the seven converted rules in `AGENTS.md` (D1–D5, R1, R2) to the drafted text and append each rule's
      `evidence:` token as its own paragraph. Do not convert R3.
- [ ] 4.4 Add the `agent-skills` delta to the main spec at archive: the `MODIFIED` audit-scope requirement and the four
      `ADDED` requirements, with every scenario carried.
- [x] 4.5 Extend `agents-auditor`'s scope to `docs/guidance-evidence/**` and add its pairing check, with the delta's
      requirement as the authority.
- [x] 4.6 Record the follow-up set in `docs/ideas.md` under a dated section: R3's drafted-but-unconverted status, and
      the 22 undrafted or unchanged rules by group (16 undrafted rewrites, 1 undrafted relocation, 4 with no separable
      material, 1 rejected). Verify by grepping the file.
- [x] 4.7 Route the capture's authoring-system recommendation to its own change: the extraction-candidate conditions in
      `showcase/quality/agent-skills` should require the retained pointer to identify each converted source unit, so an
      audit can tell an unconverted unit from one whose destination entry was deleted. Record it as parked; do not fold
      it into this delta.
- [x] 4.8 Replace the last sentence of the `### Documentation & durable claims` bullet "A durable artifact may assert
      only what the repository can evidence" with the dated-record text in design.md "Capture decisions", and re-read
      the bullet to confirm the prohibition on narrative inside the guidance survives.
- [x] 4.9 Apply the extraction workflow's per-item gate decision: "or keep each item the scan would stop seeing", plus
      "An item nothing live depends on may leave, with its reason recorded", with the unit's `captured:` marker appended
      beside the existing one.

## 5. Verification

- [x] 5.1 **Pairing, the five cases.** Seed each case and confirm the audit's behaviour: an untouched rule (no finding);
      a converted rule with a valid section (no finding); a deleted section (finding); an orphaned section (finding); a
      renamed rule (two findings). Remove the seeds and confirm the clean state.
- [x] 5.2 **Pointer resolution.** Every `evidence:` token names a file that exists, and every section's `Rule:`
      paragraph matches a live rule's leading text after whitespace normalization. Report the counts checked.
- [x] 5.3 **Operational identifiers at root.** For each converted rule, confirm every identifier needed to perform or
      verify the action is in the root text — D1 `test -e`/`git ls-files`; D2 `test ! -d` and the archive
      `git ls-files`; D3 `git diff --cached --name-only`/`git status` and the guard enumeration; D4 the em-dash action
      and "proofread blank lines"; D5 the source list and `helm show values`; R1 the six scopes and `@param`/`@return`;
      R2 the scanned-file list, `owner/repo#NNN`, `retires when`, and `upstream-references.yml`.
- [x] 5.4 **Historical identifiers, and the record's boundary.** For each converted rule, confirm every historical
      identifier from the removed span is present in its section — the inventory in design.md "Validation" is the
      checklist — and that any command or identifier the body names is factual description of the episode, not an
      instruction a reader could follow instead of the root rule.
- [x] 5.5 **Action without the record.** For each converted rule, give an agent `AGENTS.md` alone and confirm the
      governed action is taken; record each result rather than asserting it.
- [x] 5.6 **Evidence retrieval.** Starting from each rule's token, reach its section and the identifiers that moved;
      record any that did not survive.
- [x] 5.7 **Upstream closure tracking.** Run `./gradlew upstreamReferences` before and after the implementation and
      confirm the four retained references are still reported and that `micromatch/braces#70` is the only one that
      leaves the set.
- [x] 5.8 **Measured result.** `wc -c -l AGENTS.md` against 338,964 bytes / 3,463 lines, `wc -c` on the two record files
      against 1,822 and 5,747, and record both levels: the seven-rule conversion subtotal beside the drafted −4,931, and
      the whole unit beside the provisional −4,660.
- [x] 5.9 **The capture corrections.** Confirm the corrected sentence still forbids narrative inside the guidance and
      now names the repository's dated homes; confirm the extraction decision point reads per item and that the upstream
      inventory in 5.7 is the case it governs.
- [x] 5.10 **Gates.** `./gradlew spotlessApply` after the final edit, then `spotlessCheck`, `openspec validate --all`,
      `verifyCapturedMarkers`, and the Docker-free `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`.

## 6. Disposition and follow-ups

- [x] 6.1 Record the owner's decision on the recommendation — the seven-rule unit, retention, or the lossy option —
      here. **Approved: the seven-rule unit**, with R3 left unchanged, the extraction-pointer recommendation parked, and
      the lossy option not chosen; implemented in this branch.
- [x] 6.2 Park the duplicated `paragraphs` in the em-dash rule in `docs/ideas.md` under a dated section with its
      trailing status tag, unless D4's rewrite covers it — and verify by grepping `docs/ideas.md`. **D4's rewrite covers
      it**: `grep -c 'paragraphs paragraphs' AGENTS.md` now returns `0`, so no entry is parked.
