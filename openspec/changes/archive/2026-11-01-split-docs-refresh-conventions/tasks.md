# Tasks

Workspace: unit=split-docs-refresh-conventions; path=/Users/afanasyev/Development/axon-showcase;
branch=split-docs-refresh-conventions; writer=ses_e6910641effev2kAkkWjp0pbzY.

## Resume checkpoint

- **Time and revision**: 2026-11-01; `HEAD` `fff14836` (`git rev-parse HEAD`). The branch was fast-forwarded onto
  `origin/main` (`0734891b` → `fff14836`, PR #797) mid-unit; that advance touched neither `AGENTS.md` nor
  `docs/ideas.md`, and this unit's thirteen ledger rows survived it.
- **Workspace**: unit=split-docs-refresh-conventions; path=/Users/afanasyev/Development/axon-showcase;
  branch=split-docs-refresh-conventions; writer=ses_e6910641effev2kAkkWjp0pbzY.
- **Stage**: implementation re-opened on the owner's decision after the advisor consultation — the split is now per
  obligation under family containers — so the loop re-ran from round 4 and converged on round 6. The routing decision,
  re-derived on the restructured tree, was `RECOMMENDED`; the owner deferred it to the technical advisor, which
  recommended the bounded pass, and it ran with two findings, both fixed; the rounds after it cleared the residue those
  fixes left in the planning artifacts, and the branch refresh's round cleared the one finding it raised. The loop is
  closed: a round reported nothing remains, `--expect-clean` passes, and the ledger carries every row.
- **Next step**: the owner's manual review pass. Shipping (`ship-change`) follows that approval only.
- **Change set**: `git status` shows `M AGENTS.md`, `M docs/ideas.md`, `M docs/review-rounds.tsv` (this unit's rows) and
  the staged change dir plus `docs/process-defects/split-docs-refresh-conventions.md`; nothing committed, nothing
  cleaned.
- **Blockers and open decisions**: none. Locally decided: seven family containers, sixteen obligation bullets beneath
  five of them and two families stating one obligation each; granularity is the obligation, so an obligation and its own
  verification stay in one bullet; the six remaining markers stay at the rule's end.
- **Pending decision**: the owner's manual review pass.
- **Findings no record holds**: rounds 1–6's instances are fixed in the tree and recorded as their ledger rows' classes;
  the deep review's two findings were fixed and rounds 7–10 cleared their residue — decision 3 now names three splice
  scopes, the checkpoint and task notes match the ledger, and the Goals no longer narrow the splice to labels.
- **Operations in flight**: none.

## 1. The split

- [x] 1.1 Split `AGENTS.md`'s "Docs refresh on change" bullet into its bold lead and seven labeled family containers,
      each holding one bullet per independently actionable obligation, carrying every clause's content apart from the
      splice decision 3 names and keeping the three `bump-dependencies-*` markers on the version-bump rule; the rest
      stay at the rule's end.
- [x] 1.2 Remove the `docs/ideas.md` 2026-11-01 entry, since this change implements it, and park the one follow-up its
      implementation surfaced in its place: recovering the stranded 2026-10-31 audit report.
- [x] 1.3 Place `captured: address-scan-findings-2026-10-25` with the `docs/guidance-evidence/` clause it followed in
      the base text, and record the splices decision 3 permits.

## 2. Verification

- [x] 2.1 Run the distinctive-token inventory over the old and new bullet texts, read the result clause by clause, then
      run `spotlessApply`/`spotlessCheck`, `verifyCapturedMarkers`, `verifyIdeasSections`, `openspec validate --all` and
      `./gradlew :check -PskipITs -Pcoverage.gate.enabled=false`; run the manual 120-character check over the changed
      set.

## 3. Review, records and the manual pass

- [x] 3.1 Record the pre-write derivation pass and its claims table in `design.md`, seeded by `scripts/claim-scan.py`.
- [x] 3.2 Run the `review-quick` loop, appending each round's row to `docs/review-rounds.tsv`, until a round reports
      nothing new. The loop closed on a round that reported nothing remains, `--expect-clean` passes on the current
      tree, and every round's row is in `docs/review-rounds.tsv`.
- [x] 3.3 Derive the deep-review routing with `scripts/review-routing.py` and run the routed pass. Re-derived on the
      restructured tree: `RECOMMENDED` on the `[repeated-class]` signal; the owner deferred the run-or-skip question to
      the technical advisor, which recommended the bounded pass, so it ran. Two findings — one should-fix (decision 3's
      closed splice list did not name three meaning-preserving moves the diff made) and one nitpick (this task's 3.4
      note) — both fixed. Obligation preservation confirmed clause by clause against the base bullet.
- [x] 3.4 Write `docs/process-defects/split-docs-refresh-conventions.md` — or state that no defect was observed. Four
      records written: `#3` arose during the per-obligation restructure, and `#4` spans rounds 2, 4 and 5.
- [x] 3.5 Consult the technical advisor before the manual pass. Its recommendation — complete the per-obligation split
      in this unit, keep the audit recovery parked — was adopted on the owner's decision.
- [x] 3.6 Ask the owner for the manual review pass; commit and push only after approval. Approved 2026-11-01.
