# Design

## Context

See `proposal.md` — Why. The mechanism lives in `scripts/commit-hygiene.py`, the stdlib-only checker that already owns
the pre-commit guard's staged-set modes and the build checks (`--markers`, `--tracked-ignored`, `--conflict-markers`,
`--executable-bits`). Each build check is surfaced as an `Exec` task (`verifyCapturedMarkers`,
`verifyTrackedIgnoredFiles`, `verifyConflictMarkers`, `verifyExecutableBits`) wired into the root `check` task in
`build.gradle.kts`, with unit tests in `scripts/test-commit-hygiene.py`. The checker runs under macOS's system Python
3.9 (`typing.Optional`, never `str | None`) and the repository's `.github/workflows/` today holds eleven `*.yml` files —
nine carrying a `cron` (`ci.yml` and `opencode.yml` carry no `schedule`) — in the canonical block form:

```yaml
on:
  schedule:
    - cron: '20 19 * * 0'
```

## Goals / Non-Goals

**Goals:**

- Fail the CI `build` gate when two workflow `cron` schedules collide, naming each colliding pair.
- Keep the check stdlib-only and Python 3.9 compatible, matching the checker's existing modes and the `Exec` task style.
- Cover the collision classes with unit tests, including a positive control that the scan's file set is not vacuous.

**Non-Goals:**

- General cron-overlap analysis (arbitrary minute/hour ranges, lists, steps, or day-of-month/day-of-week intersection).
- Enforcing any property beyond "no shared firing minute" — for example that jobs are spread across the night.
- Reading or validating the YAML structure of `on.schedule`.
- Widening the existing test-coverage requirement (`The guard's checks are covered by tests run in the build`) from the
  pre-commit guard's defect classes to the checker's build checks. That is a separate, parked idea — "Widen the
  `commit-hygiene` test-coverage requirement to every check" in `docs/ideas.md`, which records that the widening needs a
  `REMOVED`+`ADDED` retitle because a `MODIFIED` block cannot rename a requirement header — and this change neither
  implements nor tries to fold it in.

## Decisions

### D1 — Collisions are same minute and hour with overlapping day-of-week

Two schedules collide when their minute and hour fields are equal and their day-of-week fields can select a common day:
equal day fields, or either field is `*`. This treats a daily workflow (`*` day-of-week) sharing a minute with a weekly
one as a collision — the weekly's day is one the daily also runs, which a cron string cannot express — while two weekly
schedules on different days at the same time pass, because they never run on the same day. A day-of-week field written
`7` is normalized to `0` before comparison, since both name Sunday — so `0 2 * * 7` collides with `0 2 * * 0`. No other
alias, range, list, or step form is normalized (see the Non-Goals).

- **Alternative — exact cron-string equality only**: rejects a daily/weekly pair that collides on the weekly's day, the
  case the requirement's day-overlap wording exists to catch. Rejected.
- **Alternative — full cron-overlap analysis**: cron's day-of-month/day-of-week union semantics make an exact
  intersection check disproportionate, and every schedule in this repository ends in `* * <day-of-week>`. Rejected.
- **Limitation**: when either entry restricts day-of-month or month (a field other than `*`), the pair falls back to
  exact-string comparison, so a partial overlap between two such crons goes unreported. The check's tests pin the
  supported shapes; a future workflow that needs another shape extends the check, and the conservative fallback never
  reports a false collision.

### D2 — The input is a line scan of the workflow files, not a YAML parse

The check reads each `.github/workflows/` file line by line and captures the canonical `- cron: '<expr>'` list-item
form, ignoring lines whose first non-whitespace character is `#`. Stdlib ships no YAML parser and PyYAML is a
third-party dependency the checker must not take, so a parse is not available.

- **Failure modes**: a commented-out `cron` is skipped by the comment rule; an inline trailing comment after the value
  is stripped from the captured expression; a bare `cron:` line — one without the `- ` list-item marker — is not matched
  because the scan anchors on the `- cron:` list-item form, though a scalar line that itself begins `- cron:` is
  matched, since the scan does not parse YAML; a folded/multi-line `cron` value or an inline `schedule: [{cron: …}]`
  form is missed, which the canonical block form the repository uses avoids. A missed entry under-reports a collision
  rather than failing a clean workflow.
- **Alternative — parse YAML with PyYAML**: adds a non-stdlib dependency and breaks the checker's Python 3.9 contract.
  Rejected.
- **Alternative — match `cron:` anywhere in the file**: also catches comments and unrelated strings. Rejected.

### D3 — The scan reads both `*.yml` and `*.yaml` workflow files

GitHub treats `.yml` and `.yaml` as equivalent workflow files. The scan matches both suffixes (the repository uses only
`.yml` today, so the `.yaml` arm is a no-op that a probe fixture must exercise), so a future `.yaml` workflow is not
silently unexamined.

### D4 — The check is a new mode and a new `Exec` task, not a new script

`scripts/commit-hygiene.py` gains `--unique-crons`, `build.gradle.kts` registers `verifyUniqueCronSchedules` beside the
other build checks (`inputs.file("scripts/commit-hygiene.py")`, `inputs.dir(".github/workflows")`,
`outputs.upToDateWhen { false }`) and adds it to `check`. This matches the checker's existing mode/CLI structure and the
one-test-file convention.

## Risks / Trade-offs

- **[A line scan can miss a non-canonical schedule]** → the scan anchors on the canonical list-item form and the tests
  cover the commented and duplicate cases; a missed entry under-reports, and the check is a cheap additional guard, not
  the only one a human reads.
- **[The collision rule may under-report restricted-day schedules]** → the fallback to exact-string comparison is
  conservative (never a false positive); the supported shapes are the ones every repository schedule uses.
- **[A wrong glob could make the check vacuous]** → a verification task enumerates the files both glob arms match by
  path (not only a count), shows a known workflow in the `.yml` set, and exercises the `.yaml` arm with a probe fixture.
- **[Python 3.9 incompatibility]** → use `typing.Optional` and stdlib only; verify under `/usr/bin/python3`.

## Migration Plan

None — additive check and tests. Rollback is deleting the mode, the tests, the task registration, and the `check`
dependency.

## Open Questions

None.
