# Proposal

## Why

`openspec archive` writes the delta→main spec sync into the working tree and relocates the change dir with a filesystem
move, staging neither. A commit that stages only the change dir (`git add openspec/changes`) therefore ships the archive
without its spec sync — it happened in #496 and needed #497 to recover — and the prose gotcha in `AGENTS.md` describes
the hazard without preventing it. The pre-commit guard already inspects the staged set, so the class belongs in a
mechanical check rather than more prose.

## What Changes

- **`scripts/commit-hygiene.py`** (`--staged` mode): add `find_unpaired_archive_sync` — when a change dir under
  `openspec/changes/archive/**` is staged and that change carries delta specs, refuse the commit when
  `openspec/specs/**` has an unstaged or untracked change (the un-staged sync), naming the archived change and the spec
  paths. A change with no delta specs (`skip_specs`) is exempt.
- **`scripts/test-commit-hygiene.py`**: cover the paired (pass), unpaired (refuse), and no-delta (pass) cases; the tests
  run in `check` via `testCommitHygiene`.
- **`openspec/specs/showcase/quality/commit-hygiene/spec.md`**: a `MODIFIED` delta adding the archive-sync defect class
  to the guard requirement and a scenario for it.
- **Docs**: point the `openspec archive` gotcha in `AGENTS.md` at the new check; widen **every** live enumeration of the
  guard's checks (each lists the same five, so each goes stale) — the `AGENTS.md` Prerequisites list and its `git add
  <dir>` gotcha, the README's pre-commit-guard sentence ("catches five …"), and the two scripts' own docstring/comment —
  and remove the now-implemented idea from `docs/ideas.md`.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/commit-hygiene`: the pre-commit guard's defect set gains the unpaired archive move / spec sync.

## Impact

- `scripts/commit-hygiene.py` and `scripts/test-commit-hygiene.py` (stdlib-only, runs under the documented system
  `python3`), the `commit-hygiene` spec, the `openspec archive` gotcha and every live guard-check enumeration in
  `AGENTS.md` and `README.md`, and `docs/ideas.md`.
- No build, API, or deployment impact; the check runs locally in the pre-commit hook and its tests run in `check`.
