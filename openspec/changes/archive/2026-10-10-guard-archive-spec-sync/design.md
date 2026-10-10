# Design

## Context

See `proposal.md` — Why. `openspec archive` relocates the change dir with a filesystem move and writes the delta→main
sync into `openspec/specs/**`, staging neither; a commit that stages only `openspec/changes` ships the archive without
its sync (the `AGENTS.md` `openspec archive` gotcha records the #496/#497 incident). The repository already runs a
tracked `pre-commit` hook — `scripts/commit-hygiene.py --staged` — that inspects the staged set for a formatter failure,
a force-staged generated artifact, a path staged then edited, a staged conflict marker, and a misplaced `captured:`
marker. It is the staged-set view the build's tracked-set checks cannot provide (three of those five also have a
tracked-set counterpart), and it is Python-stdlib-only, running under the documented system interpreter
(`/usr/bin/python3`, 3.9).

## Goals / Non-Goals

**Goals:**

- Refuse a commit that stages an `openspec/changes/archive/**` change dir whose delta-spec sync is left unstaged or
  untracked.
- Keep the `skip_specs` / no-delta change working (no sync is owed).

**Non-Goals:**

- Staging the sync automatically — the guard is spec'd to not modify the index or the working tree.
- Deriving the exact main-spec files a delta maps to (see Decisions).

## Decisions

### Put the check in the pre-commit guard, not the build

The slip is a _staged-set_ defect: the sync exists in the working tree; the commit omits it. The build's tracked-set
checks run on committed content and cannot see an omission (there is nothing to fail on once the sync was simply never
committed). The hook is the surface that inspects the index before the commit exists, so it is the right home.

### Detect by "archive move with deltas staged, and `openspec/specs/**` carries an unstaged/untracked change"

A staged path under `openspec/changes/archive/**` belongs to an archived change dir; when that dir carries a
`specs/**/spec.md` delta, the guard requires that no path under `openspec/specs/` differs from the index or is untracked
— i.e. the sync is staged. A change dir with no delta specs is exempt. The trigger is any staged archive-dir path of a
with-delta change (an edit to an already-archived change qualifies, not only a fresh move).

- _Alternative — derive the exact expected main-spec paths from each delta's capability path_ (`specs/<capability>` →
  `openspec/specs/<capability>`): rejected for now as more surface and mapping logic for a guard; the whole-`specs/`
  check is simpler and the failure it guards is the common one.
- _Accepted limitation:_ an unrelated unstaged/untracked `openspec/specs/` edit present while a with-delta archived
  change is staged is also refused. The message names the spec paths, so it is resolvable (stage them, or stash the
  unrelated edit), and the guard stays bypassable with `git commit --no-verify`.

### The guard reads but never writes

The check runs `git status`/`git diff` and prints offenders; it does not stage, move, or edit anything, matching the
spec'd "does not change repository state" obligation.

## Risks / Trade-offs

- **False refusal on an unrelated unstaged spec edit** → the message names the spec paths (resolvable), and the hook is
  bypassable for a deliberate exception.
- **The check depends on the working tree**, so it is meaningful only in `--staged` mode, not in the build's tracked-set
  checks — intended, since only the index-vs-worktree split reveals an un-staged sync.
- **A change archived with `--no-validate`/partial staging** → the staged-set rule still holds: if the sync is unstaged,
  it is refused; if there is none to stage (no deltas), it is exempt.

## Migration Plan

None — a local guard change; contributors pick it up through the tracked hook.
