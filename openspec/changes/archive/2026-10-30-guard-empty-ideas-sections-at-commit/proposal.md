# Proposal

## Why

The ideas-section verification runs in the standard `check` task, so an emptied dated section in `docs/ideas.md` is
caught at pull-request time. The pre-commit guard does not judge the file, so the commit that empties a section is
created locally and the failure surfaces only in CI — the one mechanical defect of its class the guard lets through,
while it already refuses misplaced `captured:` markers, a broken review-round ledger, and an unticked archived task. The
change that added the verification (`verify-empty-ideas-sections`) and the one that widened the coverage requirement
(`widen-commit-hygiene-test-coverage`) each left the hook out of scope; this is their follow-up.

## What Changes

- The guard judges a staged `docs/ideas.md` with the build check's own predicate — a dated section carrying no entry
  refuses the commit — and reads the index rather than the working tree, since the index is what the commit records.
- A staged deletion of the file refuses as a missing ideas file.
- The build check and the guard share one reporting helper, so the predicate, the message, and the count cannot drift.
- Tests cover the guard path: an emptied section refuses and names the line and heading, a section carrying entries
  passes, the judgment follows the index rather than the working tree in both directions, and a staged deletion refuses.
- Both refusals — the emptied dated section and the staged removal — are named in the hook's own header, `AGENTS.md`'s
  staging gotcha, and the README's guard paragraph.

## Capabilities

### New Capabilities

none

### Modified Capabilities

`showcase/quality/commit-hygiene`: the pre-commit guard requirement gains the ideas-section refusal, judged on the
index's content, plus four scenarios — the emptied section, the staged deletion, a commit that does not stage the file,
and a staged copy that differs from the working tree's.

## Impact

- `scripts/commit-hygiene.py` and `scripts/test-commit-hygiene.py`; the hook's header comment in
  `scripts/git-hooks/pre-commit`; the enumerations in `AGENTS.md` and `README.md`; the delta and its archive sync.
- No runtime, build-configuration, or deployment change: the same predicate already runs in `check`, and the guard
  invokes it over the staged copy.
