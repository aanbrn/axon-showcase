# re-review-after-boundary-refresh process defects

### re-review-after-boundary-refresh#1 — The refresh reconciliation took one side wholesale, dropping the upstream entry

- **Recorded**: 2026-11-01, by the implementing session, on its own observation during the refresh.
- **Stage**: review.
- **Expected**: refreshing an uncommitted tree that both sides changed keeps both facts — the base's new content and the
  unit's edits — and a conflict is reconciled for both sides, as `AGENTS.md`'s branch-refresh rule states.
- **Observed**: a `git stash` → `git merge --ff-only origin/main` → `git stash pop` left `docs/ideas.md` resolved as the
  unit's copy alone, dropping the entry the base's `#795` commit had added to `## 2026-11-01`; the ledger conflict was
  resolved for both sides and neither side noticed the dropped idea until the diff was read.
- **Evidence**: after the pop, `git show HEAD -- docs/ideas.md` reported `+14` lines for `#795` while
  `grep -c 'docs-refresh' docs/ideas.md` returned 0 and `git diff HEAD --stat` listed the file as 7 deletions only; the
  repair restored `HEAD`'s file and re-applied the unit's removal, after which #795's entry is present and the removal
  is the only diff.
- **Consequence**: one reconciliation round and a repair; no content was lost in the final tree.
- **Knowledge boundary**: observed the dropped entry and the repair. Did not check whether the pop's resolver chose a
  side for any other file the unit and the base both changed.
