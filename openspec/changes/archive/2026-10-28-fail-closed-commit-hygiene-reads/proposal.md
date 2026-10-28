# Proposal

## Why

`commit-hygiene.py` parses several reads as if an empty result meant "no offenders". When a read fails — an unreadable
index, a repository the command cannot resolve, a tracked file it cannot measure — the same empty result arrives, so the
guard approves a commit, or a `check` task reports a clean set, on evidence it never obtained. `--review-ready` was
fixed this way for its own boundary; the other modes still read a failure as clean, which is the failure mode a check
exists to prevent.

## What Changes

- `scripts/commit-hygiene.py`: every read whose result decides an offender list goes through the failure-raising
  `git_output`, except the ones where a non-zero exit is the answer (`git grep` with no match, the revision questions
  that already split exit 1 from a failure, and the branch read's exit 1 for a detached HEAD; an unborn HEAD names its
  branch rather than failing). `main` reports the failure once (`CheckFailure`) as the check having failed, naming the
  read, and exits non-zero for every mode. The staged-content read loses its absent-path branch, since `git show`
  answers an absent path with the same exit as a fatal read. `--large-files` stops skipping a file it cannot measure: a
  tracked path absent from the worktree is measured from its index blob, and any other measurement failure is reported.
- `scripts/test-commit-hygiene.py`: a case per mode that its read fails, and one that a no-match grep stays an answer.
- `openspec/specs/showcase/quality/commit-hygiene` (delta): one added requirement stating the rule and the exits that
  remain answers.
- The capability `## Purpose` refresh, which a delta cannot carry, is applied in the archive commit if the Purpose's
  text is falsified.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/commit-hygiene`: a failed read is a reported failure in every mode, not a clean result.

## Impact

The checker, its tests, and one capability spec. No production code, API, dependency, build-file, or deployment surface
changes. `AGENTS.md`, `README.md`, and the ADRs are unchanged: the checker's placement and the gate descriptions do not
depend on how it reports an error.
