# Handover — proposal stage, review loop at round 4

**State.** The change dir is complete; its proposal-stage review loop stands at round 4 (2 findings, both fixed in the
same pass). **No implementation file is edited yet** — the change dir is untracked, and the only tracked file touched is
`docs/review-rounds.tsv` (the loop's rows), so the unit's substance is not at risk. The implementation (tasks 1.1-1.6,
2.1) has not started. `docs/review-rounds.tsv` carries rounds 1-4.

**Rounds.** 1: `contradiction, coherence, wording x3, claim-missing`. 2: `coherence` (repeat), `missing-task`. 3:
`coherence` (repeat), `missing-task` (repeat). 4: `incomplete-enumeration`, `proposal-schema` — both fixed and confirmed
in round 5. 5: `claim-unreproducible` (the claims row's `git show HEAD:` directive re-anchored to `69d26b02`),
`incomplete-enumeration` (a point-in-time count deleted) — both fixed and confirmed in round 6. 6:
`claim-unreproducible` (repeat) — the task 4.3 parenthetical "(the last two analogous units both carry this task)"
over-claimed its provenance; **deleted** per the repeated-class rule's remedy, so a round 7 owes the confirming pass.

**The loop's shape, for the next session.** Six rounds, all in one family: a claim written from the paraphrase beside it
rather than derived — a site list from a report's instances, a count that grows, a statement about another unit's tasks.
Each fix is now either a sweep by tokens _and_ falsified statements, or a deletion of a claim the mechanism does not
need. Expect round 7 to confirm the deletion; if it raises a new instance of the same family, derive _that_ claim
mechanically before patching it.

**Round 4's instances, for the round-5 brief** (a re-run must report an unfixed prior instance or a new instance of an
exhausted class):

- `incomplete-enumeration` — `AGENTS.md:1144` + `:1148-1149` state "the un-audited docs" and "No auditor covers either
  file — the four auditors own `AGENTS.md`/`.opencode/`, the spec corpus, `docs/adr/` plus the architectural surface,
  and `README.md`", which this change makes questionable (the architecture-auditor now reports a stale parked entry in
  `docs/ideas.md`). **Fixed** by task 1.6 (widen or record why it stays true) and by extending the derivation beyond the
  token families to the statements the change _falsifies_ — an absence claim carries no token.
- `proposal-schema` — the `## Impact` omitted the build/tests/deployment line the `openspec/config.yaml` rules require.
  **Fixed** with an explicit "no code, build, dependency or deployment change" line.

**Fixed and not to be re-reported** (their sites are named in the tasks; the class-exhaustion clause still applies to
new instances): round 1's six (the item moved into the inverse class; tasks 1.1/1.3 named the sibling sites; the doubled
`and`/wrong predicate; task 4.2 ticked; the smoke-run claims row; task 3.1's inapposite half); round 2/3's site-set
fixes (tasks 1.1/1.3 are now sweeps by both token families, and 4.3 carries the per-round record).

**Next, in order:** run the round-5 confirming pass with the instances above; then the implementation (tasks 1.1-1.6,
2.1); its review; the capture (3.4); the manual ask (3.5); and — **before the archive** — the smoke-run (3.3), which
needs the owner's reload or a new session, since an edited definition is served from the pre-change copy in the session
that edited it.
