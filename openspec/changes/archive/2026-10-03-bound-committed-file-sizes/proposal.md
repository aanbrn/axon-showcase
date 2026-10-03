# Proposal

## Why

Nothing bounds a tracked file's size: a large blob — a binary, a vendored archive, an accidentally committed build
output — can be committed and stays in history forever, slowing clones and repositories. The repository's existing
hygiene checks (`verifyTrackedIgnoredFiles`, `verifyExecutableBits`, `verifyConflictMarkers`) each guard a class of
tracked-set defect, but none caps size.

## What Changes

- `scripts/commit-hygiene.py`: add a `--large-files` mode that reports every tracked file whose size exceeds a
  configured limit, naming the file, its size, and the limit.
- The limit is declared in `config/commit-hygiene/large-files.properties` (a `maxBytes` key, with a documented default),
  so it is tuned by editing a file rather than a hard-coded constant, and every tracked file is checked against it.
- `build.gradle.kts`: register a `verifyLargeFiles` task (`--large-files`) and add it to `check`.
- `scripts/test-hygiene` coverage: a test asserting the mode flags an over-limit file and passes an under-limit one.
- Refresh every live prose enumeration of the `check` members the new member joins — `AGENTS.md`'s `check` note,
  `README.md`'s verification-command block **and** its commit-hygiene build-checks paragraph, and the parked
  `docs/ideas.md` idea that lists the five build-check names. The boundary: `README.md`'s "five commit-hygiene slips"
  sentence counts the pre-commit guard's `--staged` slips (not the build checks) and stays unchanged. Remove the
  implemented idea from `docs/ideas.md`.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `showcase/quality/commit-hygiene`: adds a requirement that the tracked set is checked against a configured maximum
  file size, failing `check` and naming each oversized file — the capability already owns the tracked-set checks
  (`--tracked-ignored`, `--conflict-markers`, `--executable-bits`) and their `check` membership.

## Impact

- **Files**: `scripts/commit-hygiene.py`, `scripts/test-commit-hygiene.py`, a new
  `config/commit-hygiene/large-files.properties`, `build.gradle.kts`, and the docs (`AGENTS.md`, `README.md`,
  `docs/ideas.md`), plus the change dir and the `commit-hygiene` delta. Within `README.md`, the commit-hygiene
  build-checks prose (lines 613-618) is edited; the "five commit-hygiene slips" sentence is not (it counts the guard's
  slips, not the build checks).
- **Build / tests / services**: one more `check` member (a stdlib-only scan over the tracked set, no network, no
  Docker); no Java, dependency, or service change.
- **Verification**: proven with a known-bad (a file over the limit fails the check naming it) and a known-good (the
  current tree's largest file, `AGENTS.md` at ~263 KiB, passes under the chosen limit), and by the task running in
  `check`.
