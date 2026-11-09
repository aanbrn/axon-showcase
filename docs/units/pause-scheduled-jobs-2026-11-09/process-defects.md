# Process defects — pause-scheduled-jobs-2026-11-09

### pause-scheduled-jobs-2026-11-09#1 — the failing task's diagnostic was lost to a `tail` pipe

- **Recorded** — 2026-11-09, by the unit's writing agent.
- **Stage** — implementation.
- **Expected** — a failed `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` run names the task that failed and
  its message.
- **Observed** — the run failed after 1m22s and reported only its closing lines, so the failing task and its message
  were not recorded; the same command passed 1m20s later on a later tree. The run was started in the shell invocation
  that also wrote the unit's `rounds.tsv`, `sessions.tsv`, and `findings/round-2.md`, the writes running first in that
  sequence.
- **Evidence** — the failed run's own last lines: `BUILD FAILED in 1m 22s` and
  `33 actionable tasks: 15 executed, 18 up-to-date`; the passing run's: `BUILD SUCCESSFUL in 1m 20s` and
  `415 actionable tasks: 43 executed, 372 up-to-date`. The command piped its output through `tail -8`, which kept only
  the closing lines.
- **Consequence** — one wasted run of about a minute and a half, and no diagnostic for the failure; the gate's result
  was re-established on the fixed tree, and nothing else was damaged.
- **Knowledge boundary** — observed: the failure, its duration, the write sequence in the same shell invocation, and the
  later pass with the same command. Not established: which task failed or why, and whether the writes bear on it at all.

## Notes on the knowledge boundary

- The nine workflow disables were applied before the review gate's first round, as implementation work. That ordering is
  the lifecycle's, and the reviewers screen the diff plus this unit's record, which names the external state a diff
  cannot show.
- `docs/ideas.md` was read from local `main` before the branch was cut from a freshly fetched `origin/main`; the two
  revisions differed, but not in the section this unit edits. Nothing was written from the stale copy.
