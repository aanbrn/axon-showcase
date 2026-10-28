# Proposal

## Why

The review gate's manual-pass boundary asks the agent for three things — a base that has not moved, the unit's new files
in the index, and the request naming what was checked — and observes them with prose alone:
`git diff --stat origin/main` read by eye and a `git add -- <paths>` nobody re-checks. Two 2026-10-27 defect records
show the index half missed or ordered wrong, and no check reads the base at all, so a skip is invisible until the
review, the commit, or the pull request carries it. `commit-hygiene.py` already carries the boundary's other assertion
(`--expect-clean`); a sibling mode makes a skipped preparation visible as a failed check with the revisions it compared.

## What Changes

- `scripts/commit-hygiene.py`: add a `--review-ready` mode (with `--base`, defaulting to `origin/main`) that fails on a
  non-ignored untracked path, a path staged and then edited, and a base whose history `HEAD` does not contain; a git
  read it needs that fails is reported as readiness not established rather than as a clean tree. On success it reports
  the base and `HEAD` revisions it compared, and it never stages anything.
- `scripts/test-commit-hygiene.py`: the mode's known-bad and known-good fixtures, with the mode itself reached through
  `main` once.
- `.opencode/skills/review-gate/SKILL.md` §5: the two prose steps become one check-first procedure — fetch, run the
  mode, and apply each failure's remedy (stage an explicit path, re-stage an edited path, refresh by the branch rule and
  re-run) — and the request names the mode's output line. The refresh procedure itself stays in `AGENTS.md`.
- `openspec/specs/showcase/quality/commit-hygiene` (delta): one added requirement stating the mode's three obligations,
  its boundary invocation, and its fail-closed rule, with the scenarios that pin each.
- The capability `## Purpose` refresh, which a delta cannot carry, is applied in the archive commit: its enumeration of
  checks gains this one and its boundary-asserted clause gains the new member.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/commit-hygiene`: a new boundary-asserted mode over the working tree, in addition to the existing
  `--staged` guard and `--expect-clean` boundary assertion.

## Impact

Tooling, one skill procedure, and one capability spec. No production code, API, dependency, build-file, or deployment
surface changes: the mode is not a `check` task, since the state it reads exists only in an agent's uncommitted working
tree, and its tests already run through `testCommitHygiene` in `check`. `AGENTS.md`, `README.md`, and the ADRs are
unchanged — the branch-refresh rule keeps its owner, and the README's lists describe the pre-commit guard and the
`check` tasks, neither of which this mode joins.
