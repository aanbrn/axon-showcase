# Tasks

## 1. Guard implementation

- [x] 1.1 Add `find_unpaired_archive_sync(repo)` to `scripts/commit-hygiene.py` and call it from `check_staged`: for a
      staged path under `openspec/changes/archive/**` whose change dir carries a `specs/**/spec.md`, require that no
      path under `openspec/specs/` is unstaged or untracked, naming the archived change and the offending spec paths;
      exempt a change dir with no delta specs. Keep it stdlib-only and 3.9-compatible; verify
      `/usr/bin/python3 scripts/commit-hygiene.py --staged` reports nothing on the current clean tree.
- [x] 1.2 Prove the check on a known-bad and a known-good input (the control for a new check): in a scratch
      clone/`mktemp` repo, stage a change dir under `openspec/changes/archive/**` with a delta spec and leave an
      `openspec/specs/**` edit unstaged — confirm `--staged` refuses and names them; then stage the spec — confirm it
      passes and records/commits nothing itself.

## 2. Tests

- [x] 2.1 Add an `ArchiveSyncTests(unittest.TestCase)` class to `scripts/test-commit-hygiene.py` (using the existing
      `make_repo` helper) covering the unpaired→refuse, paired→pass, and no-delta→pass cases, each asserting the check's
      message or its absence; run `/usr/bin/python3 scripts/test-commit-hygiene.py`.
- [x] 2.2 Confirm the tests run in the build: `./gradlew testCommitHygiene`.

## 3. Documentation

- [x] 3.1 Point the `openspec archive` gotcha in `AGENTS.md` at the pre-commit check (stage both sides; a commit that
      stages only the change dir is now refused).
- [x] 3.2 Widen every live enumeration of the guard's checks that adding a sixth check makes stale, deriving the set by
      grepping the repository for the check tokens rather than from this list. It covers at least: the `AGENTS.md`
      Prerequisites list (`AGENTS.md:473`) and its `git add <dir>` gotcha (`AGENTS.md:2751`); the README's
      pre-commit-guard sentence (`README.md:731`, "catches five …"); the `scripts/commit-hygiene.py` module docstring
      (`:4-7`); and the `scripts/git-hooks/pre-commit` comment (`:4-6`, already at the 120-char limit, so the widened
      comment needs a wrap). Also make the count-only reference in `docs/ideas.md:74` ("the pre-commit guard's
      five-check enumeration") count-free — it paraphrases the 2026-10-04 audit's finding, so a bare `five`→`six` would
      misattribute the count to the audit.
- [x] 3.3 Remove the `Pair an archive move with its staged spec sync in the pre-commit guard` idea from `docs/ideas.md`,
      since this change implements it.
- [x] 3.4 Run `./gradlew spotlessApply` after the final edit to any formatter-owned file.

## 4. Verification

- [x] 4.1 Run `./gradlew testCommitHygiene spotlessCheck` and confirm green.
- [x] 4.2 Run the `lesson-capture` subagent over this unit's diff, review findings, and change dir; apply the durable
      proposals to `AGENTS.md` and record the applied net `AGENTS.md` delta on this task. Applied two merges (a check's
      _view_ vs its input set, into the input-set bullet; the manual 120-character check's scope covers `scripts/*.py`,
      into the Formatting bullet). Net `AGENTS.md` delta: **+4 lines** (48 insertions, 44 deletions).
