# Design

## Context

See `proposal.md` — Why. The repository's tracked-set checks live in `scripts/commit-hygiene.py` as modes
(`--tracked-ignored`, `--conflict-markers`, `--executable-bits`, `--unique-crons`), each wrapped by a `verify*` Gradle
task in the root `build.gradle.kts` and added to `check`, and each with a test in `scripts/test-commit-hygiene.py`.

The largest tracked file today is `AGENTS.md` at ~263 KiB; then `showcase-web-ui/package-lock.json` (~177 KiB) and the
bundled Grafana dashboard JSON (~119 KiB). The only binary is `gradle/wrapper/gradle-wrapper.jar` (~47 KiB), which is
legitimate. So a useful limit must sit comfortably above ~263 KiB — bounding only the genuinely anomalous blob, not the
largest legitimate source.

## Goals / Non-Goals

**Goals:**

- Fail `check` when a tracked file exceeds a configured maximum size, naming the file, its size, and the limit.
- Make the limit a single declared value that is tuned by editing a config file, not a code constant.

**Non-Goals:**

- A git-history rewrite or any action on already-committed blobs — the check guards new commits, and shrinking history
  is a separate, destructive operation.
- A git LFS integration — the point is to refuse the blob, not to store it elsewhere.
- Capping the pre-commit guard's `--staged` set — a different mechanism (the guard refuses a _staged_ set; this check
  verifies the _tracked_ set in `check`, the same split as the sibling `--executable-bits`/`--tracked-ignored` modes).
  The guard is left untouched.

## Decisions

### D1: A `--large-files` mode in `commit-hygiene.py`, wrapped by a `verifyLargeFiles` `check` task

The scan is stdlib-only and iterates `git ls-files`, mirroring `--executable-bits` (which already reads the tracked set
and a file's mode). It is wrapped like every sibling: a `verifyLargeFiles` task running
`python scripts/commit-hygiene.py --large-files` and added to `check`.

- **Alternative — a `.gitattributes`/`.gitignore` rule:** neither can express a size bound; git has no built-in
  file-size limit.
- **Alternative — a GitHub-side check (e.g. a max-blob action):** the repository's convention is to gate locally in
  `check`; a CI-only check would be skipped by a contributor without the tool.
- **Alternative — a `build-logic` Gradle task:** the sibling tracked-set checks are Python; a Gradle task would split
  one mechanism across two languages.

### D2: The limit is a config file, not a constant

The limit lives in `config/commit-hygiene/large-files.properties` as `maxBytes`, read by the mode; the file documents
the default in a comment. This makes "a threshold to tune" a one-line edit, and keeps the value out of the code — the
same shape as `config/dependency-updates/major-disabled.txt` and the jacoco baseline.

- **Alternative — a constant in `commit-hygiene.py`:** recording it as "tunable" while burying it in code is the drift
  the config-file convention exists to avoid; the pre-commit guard would also need to import it.
- **Alternative — a Gradle property (`-PlargeFileLimit=`):** a build property is per-invocation, not a committed policy;
  the limit should be reviewable in the tree.

### D3: A single limit with the file's size and the limit named in the failure

The mode reports each oversized file as size and limit in bytes (and a human-readable KiB), so the failure tells the
reader both how big the file is and what the ceiling is. Exceeding by any byte fails.

- **Alternative — per-path/per-type limits (a lockfile allowance):** premature — the current largest files are all under
  ~263 KiB, so one generous limit covers the tree; per-type rules are added only if a legitimate file ever needs one.

### D4: A new `commit-hygiene` requirement, matching the siblings

Each tracked-set check is its own requirement in the `commit-hygiene` capability, so the size bound is an `ADDED`
requirement ("No tracked file exceeds the configured size limit") with a standard-check scenario, an oversized-file
scenario, and a clean-repository scenario — the same three the sibling requirements carry.

## Risks / Trade-offs

- [A legitimate large file is refused] → the limit is set above the current largest (`AGENTS.md`, ~263 KiB), and the
  config file exists precisely to raise it; the failure names the limit so the fix is obvious.
- [The limit drifts from the code that reads it] → the default is stated in the config file's comment and the code reads
  the file; there is one value, not a code constant plus a doc copy.
- [The check passes vacuously if `git ls-files` returns nothing] → the mode prints how many files it checked, and the
  test suite covers the over/under-limit cases; a repository always has tracked files.

## Migration Plan

Not applicable — a build-check addition with no data or API migration. Rollback is reverting the task registration.

## Open Questions

None.
