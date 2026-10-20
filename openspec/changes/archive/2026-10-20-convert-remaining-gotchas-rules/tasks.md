# Tasks

## 1. Planning artifacts

- [x] 1.1 Establish the reproducible baseline: revision, branch, working-tree state, `AGENTS.md` bytes and lines, and
      the two subsections' sizes (design.md, "Reproducible baseline").
- [x] 1.2 Verify the preceding reductions merged and that both pilot subsections are byte-identical to the parent
      change's merge commit `b7f26469` (design.md, "Reproducible baseline").
- [x] 1.3 Recover the parent change's disposition table and derive the candidate set: 16 undrafted rewrites plus 1
      undrafted relocation, with R3 kept separate (design.md, "The candidate set").
- [x] 1.4 Draft all 17 candidates completely — root text and evidence section — and measure each on the file, with the
      per-rule deltas summing to the measured whole-file delta (design.md, "Drafts and measurements").
- [x] 1.5 Verify each draft against the five boundary conditions and record the result per rule (design.md, "Boundary
      verification").
- [x] 1.6 Record that the four no-separable-material rules and the one rejected rule are not reopened, and that no later
      change supplies new evidence against them (design.md, "What this unit does not reopen").
- [x] 1.7 Mechanically check every backticked span in each replaced rule against the draft root text and its record
      section; record the zero-lost result (design.md, "The identifier inventory, mechanically checked").
- [x] 1.8 Check the destination, pairing, reference, upstream, and gate constraints (design.md, "The destination,
      reused" and "Reference, gate, and upstream checks").
- [x] 1.9 Determine whether a delta is owed from the existing requirements, rather than adding one (design.md, "Delta
      determination").
- [x] 1.10 Report the conversion subtotal separately from the whole-unit delta, and rank the candidates by reduction and
      preservation risk (design.md, "Measured totals" and "Recommendation").
- [x] 1.11 Record the candidate selection table with adopted and deferred dispositions and their reasons (design.md,
      "Recommendation").
- [x] 1.12 Record the capture decision and its decision-difference check (design.md, "Capture and regrowth").
- [x] 1.13 Run the pre-write derivation pass and seed the claims table with `scripts/claim-scan.py`, resolving every
      candidate into a row or a kind-level dismissal (design.md, "Claims" and "Seed and dismissals").

## 2. Review gate

- [x] 2.1 Run `review-quick` over the planning artifacts, continuing the same reviewer session across re-runs until it
      reports no new observations. Session `ses_ea71ab02bffem5LGtE052YdGrB`, created on the first round and continued on
      every later round; no replacement was needed.
- [x] 2.2 Append every round's row to `docs/review-rounds.tsv` with the session identifier, the transition, the classes
      raised, and any replacement's reason. The ledger holds one row per round run, the last appended after that round's
      report arrived.
- [x] 2.3 Re-derive the deep-review floor with
      `python3 scripts/review-routing.py --change convert-remaining-gotchas-rules` **after the final ledger append**,
      and record the outcome and its signals here. **`RECOMMENDED`**, signal `[repeated-class]`: some finding rounds
      carry repeats, which is what that signal counts. Its count is not restated here because each later finding row
      moves it — re-running the derivation after every append is the only form that stays true, and the last run before
      the manual pass is the one that counts. The two stable bases hold: the changed set matches no public-contract,
      domain, persistence, infrastructure, or security-declaration path, and the change has no `specs/` directory, so no
      `ADDED`/`REMOVED`/`RENAMED` block is read.
- [x] 2.4 Record the resolution the outcome requires, and run the deep review when the outcome is `REQUIRED`.
      **Resolution of `RECOMMENDED`: the deep review ran once on the planning artifacts**, in its own session, and its
      findings are applied — so the outcome was acted on rather than waived. A `review-thorough` pass ran on the
      planning artifacts in its own session (`ses_ea6dafbeaffel5bc0krWZ9754c`) and reported three findings, all applied:
      C9's draft dropped "or the figure moves every time the unit appends one" (restored; C9 re-measured to −722), C10's
      dropped "not the plan's list" (restored; C10 re-measured to −621), and tasks 3.3/5.3 duplicated the
      `docs/ideas.md` edit (3.3 edits, 5.3 verifies). Its own report states that its preservation sweep over C7, C8 and
      C11–C17 found nothing, and that the record boundary, the measurements, the regrowth decision and the delta
      determination passed; that report lives in the deep-review session, not in the repository. The implementation unit
      re-derives with its own diff and runs a fresh deep review if that outcome is `REQUIRED`.
- [x] 2.5 Request the owner's manual review pass. Do not commit, push, open a PR, archive, or merge before it. Requested
      after the implementation review reached a clean round; the owner reviewed the implemented unit — `AGENTS.md`
      324,296 B / 3,361 lines, the record 16,608 B, rounds 1–18 — and approved commit-and-push.

## 3. Implementation (after the owner approves; not part of this planning unit)

- [x] 3.1 Add the eleven sections to `docs/guidance-evidence/documentation-and-durable-claims.md` in rule order, exactly
      as drafted, and confirm each `Rule:` paragraph matches its root lead after whitespace normalization. Done: the
      file holds 15 sections in rule order (the four converted earlier plus the eleven new) at **16,608 bytes**, the
      projected size; the pairing check reports 18 token-bearing rules and 18 sections with no finding.
- [x] 3.2 Rewrite the eleven `### Documentation & durable claims` rules (C7–C17) to the drafted text and append each
      rule's `evidence:` token as its own paragraph. Do not touch the Git subsection or R3. Done: eleven rewrites and
      eleven tokens applied, each from the design's own fence; the Doc subsection carries 15 tokens and the Git
      subsection is byte-identical to `HEAD`.
- [x] 3.3 Rewrite the parked `docs/ideas.md` entry to the deferred Git tranche, with this unit's measured figures and
      the trailing status tag. Done: the entry records the shipped Documentation tranche (−8,269 B / 10,861 B record /
      +2,592 B maintained), the six measured Git candidates (−3,722 B for 5,812 B, ratio 0.64), the mixed set that was
      set aside, and R3's continued deferral; it is rewritten in place under its existing `## 2026-10-20` section.
- [x] 3.4 Re-measure `AGENTS.md` and the record against the planning figures, and record both levels: the conversion
      subtotal beside −8,269 and the whole unit beside it (no accepted capture is projected). Done: `wc -c -l` reports
      **323,595 bytes / 3,355 lines** (projected 323,595 / 3,355) and the record **16,608 bytes** (projected 16,608), so
      the conversion subtotal is **−8,269 bytes** exactly; the whole-unit delta is unchanged at −8,269 until the capture
      decision below.

## 4. Verification

- [x] 4.1 **Condition 1, per converted rule.** For each of the eleven, state the incident's action and confirm the root
      text still yields it without the record. Recorded per rule: C7 the markdown-target scan → derive the members from
      the declaration; C8 the "actionlint + shellcheck" artefacts → derive the claim from the complete set; C9 the
      157/158 count → state the mechanism with no count; C10 the missed `allPropertiesHaveDocumentedDefaults` → name
      every assertion and every doc surface; C11 the reworded quote → copy the exact text or drop the quote marks; C12
      the `/var/folders/**` draft → find the evidence; C13 the `#266` rounds → run the query before writing and at
      review; C14 the 8 wrong origins → a per-item control; C15 the cache-mode misattribution → isolate the variable;
      C16 the `#1892` transcript → version, state and ordered commands; C17 the Playwright e2e "only" → the uniqueness
      grep. Each action is stated in its root text.
- [x] 4.2 **Condition 2.** For each, state a neighbouring case the rule should prevent and confirm the root text covers
      it. Recorded per rule: C7 a `check`-member or CLI-mode split; C8 a placement list naming a subset; C9 an in-flight
      review-round count; C10 a new `BPE_DEFAULT_*` property; C11 a pronoun bound to the wrong actor; C12 a recurrence
      claim sourced from the event; C13 a task's evidence pinned to a live row; C14 any generated-claim sweep; C15 a
      two-dimension probe; C16 a second posted reproduction; C17 a rename plus an enumeration member. Each is covered by
      a condition the root text states.
- [x] 4.3 **Condition 3.** For each, state a legitimate case the rule must allow and confirm the draft does not block it
      — the exception check the design's "Boundary verification" lists per rule. Recorded per rule: C7 a set with no
      partition; C8 a single-tool claim; C9 a claims-table count with a stated boundary; C10 a value with no image
      surface; C11 a verbatim quote; C12 an owner-labelled account; C13 a URL verified by request; C14 a hand-derived
      claim; C15 a single-dimension comparison; C16 a quoted third-party run; C17 a uniqueness claim that is still true.
      None is blocked.
- [x] 4.4 **Condition 4.** Give an agent `AGENTS.md` alone and confirm the governed action is taken for each rule;
      record each result rather than asserting it. Run as a behavioural probe in a fresh session
      (`ses_ea6783cbeffe3F02H7U77z8reN`) against the implemented text: eleven neutral tasks, one per converted rule,
      read-only probes allowed and every mutating action prohibited. **11 of 11 actions matched**, and no defect was
      found, so no rule or record changed; the per-rule observed actions and the probe's limitations are in `design.md`
      "Condition 4, observed". The probe verifies the first move, not a completed outcome, and the "guidance alone"
      condition rests on the session's instruction to stay out of the records.
- [x] 4.5 **Condition 5.** Starting from each rule's token, reach its section and the identifiers that moved; record any
      that did not survive. The pairing script resolves each of the eleven tokens to its section, and the span inventory
      reports no identifier absent from both the root text and its record (`total lost spans: 0`). The identifiers
      checked per rule are the ones the design's candidate table names under "Preserved obligations" and its "Boundary
      verification" re-lists, and none is missing.
- [x] 4.6 **Pairing.** Confirm every token names an existing record, every section's `Rule:` key matches a live lead,
      and the audit reports no orphan or missing section. Seed one deleted section and one renamed lead and confirm each
      is reported, then remove the seeds. The check is `verify-pairing.py` in this change dir, run from the repository
      root (`python3 openspec/changes/convert-remaining-gotchas-rules/verify-pairing.py .`): 231 rules, 18
      token-bearing, 18 sections, no finding. Against a temporary copy, seed (a) a deleted `lint-set` section reports
      one `token-with-no-section` finding; seed (b) a renamed C8 lead reports that finding plus a
      `section-with-no-single-rule` finding for `lint-set`; both seeds removed, clean again.
- [x] 4.7 **Upstream closure tracking.** Run `./gradlew upstreamReferences` before and after, and confirm
      `Fission-AI/OpenSpec#1892` is still reported (its citation at `AGENTS.md:2994` remains). Done: the report lists
      `Fission-AI/OpenSpec#1892  open  AGENTS.md:2994`; C16's citation moved to its record, and the remaining citation
      is the one that owns the retirement condition, so the reference set is unchanged apart from that line.
- [x] 4.8 **Gates.** `./gradlew spotlessApply` after the final edit, then `spotlessCheck`, `openspec validate --all`,
      `verifyCapturedMarkers`, `verifyDocumentedPins`, `verifyInstallCommands`, and the Docker-free
      `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`. All pass: the three verification tasks report their
      pins and install commands matching the catalog, `spotlessCheck` is clean, `openspec validate --all` reports 27
      passed and 0 failed (one pre-existing long-requirement warning), the marker check is clean, and the Docker-free
      `check` is `BUILD SUCCESSFUL`.
- [x] 4.9 **Measured result.** `wc -c -l AGENTS.md` beside 323,595 bytes / 3,355 lines and `wc -c` on the record beside
      16,608 bytes. Both match exactly: `3355 323595 AGENTS.md` and `16608` bytes for the record at the conversion, and
      the delivered file after the capture is `3361 324296 AGENTS.md` (324,296 bytes / 3,361 lines) with the record
      unchanged at 16,608.

## 5. Capture and disposition

- [x] 5.1 Run the per-unit `lesson-capture` decision once the implementation review is clean and before the manual pass,
      and record the applied net `AGENTS.md` delta or the skip and its reason. The planning pass proposed no lesson, so
      the projection was 0 bytes until this task ran. Ran (session `ses_ea6a28bb3ffeQl5VZK2K5Nz2uI`): **two additions
      accepted, no retirement**, for a net `AGENTS.md` delta of **+701 bytes** (323,595 → 324,296). (a) The C4 rule
      gains the token-and-table granularity: a file-wide substring replacement rewrites every occurrence that
      legitimately shares the value, and a row keyed on its table's column count lands in any sibling table sharing that
      count — the defect this unit's own correction hit. (b) The claims-table anchoring clause states that a row's
      _embedded commands_ are part of its source, since a script that opens the working-tree file reports the
      implemented result while the row reads as anchored — this unit's census and measurement scripts did exactly that,
      and both were parameterised to take the file to measure. The un-gated copy of (b) in `openspec/config.yaml`'s
      `design` rule was refreshed in the same edit. Dropped as already covered: the routing-derivation staleness (C9's
      rule and the review gate's last-append re-derivation), the Done-note over-claim (the `check-and-test-authoring`
      false-negative rule), and the baseline row's own anchoring (a compliance miss of the clause addition (b) extends).
- [x] 5.2 Record the owner's decision on the recommendation — the eleven-rule Documentation unit, retention, or a wider
      tranche — here. **Approved: the eleven-rule Documentation unit**, after the owner's consistency correction to the
      selection rationale and scope comparison; the Git tranche and R3 remain deferred.
- [x] 5.3 Verify the `docs/ideas.md` entry task 3.3 rewrites records the deferred Git tranche with its measured figures
      and its trailing status tag — rewritten in place, not duplicated into a new dated section — and that a grep for
      the entry finds it. Done: one entry under `## 2026-10-20` carries the Git figures and the tag
      `— parked; no change yet.`; no second dated section was added and no duplicate entry exists.
