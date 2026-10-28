# Proposal

## Why

A branch refresh by merge stages the other branch's content, so a merge that carries another change's archived directory
is refused by the guard's assignment check even though that content is not the commit's own. The documented workaround
is `git commit --no-verify`, which disables every check for that commit — the conflict-marker and stale-index checks a
conflict resolution needs most.

## What Changes

- `scripts/commit-hygiene.py`: the assignment check reads the merge state (`MERGE_HEAD`) and exempts a staged change
  path a merged revision accounts for — a path that revision holds, or a deletion a fork of it held and the revision
  removed. A foreign path the merge does not account for, and an ordinary commit, are refused as before; a squash merge
  records no merge state, so it stays refused.
- `scripts/test-commit-hygiene.py`: the merge that carries another change's archive move passes; a foreign path staged
  during the merge, a foreign deletion the merge does not make, and a squash merge are still refused; the merge-state
  read answers "no merge" and raises outside a repository.
- `openspec/specs/showcase/quality/commit-hygiene` (delta): the assignment requirement gains the exemption and its three
  scenarios.
- `AGENTS.md`, the hook's comment, and the checker's docstring state the exemption; the README's human-facing overview
  of the guard is left as it stands.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/commit-hygiene`: the assignment check exempts the change paths a merge commit merges in.

## Impact

The checker, its tests, one capability spec, and the guard's descriptions in `AGENTS.md`, the hook's comment, and the
checker's docstring. No production code, API, dependency, build-file, or deployment surface changes.
