# Process defects — retain-auditor-run-reports

### retain-auditor-run-reports#1 — The retention directory was chosen without checking the artifact action's file selection

- **Recorded** — 2026-11-01, by the implementing agent.
- **Stage** — implementation (proposal artifacts).
- **Expected** — a mechanism the change rests on is checked against the tool that provides it before the artifact is
  written, per `AGENTS.md`'s rule that a tool's behavior is verified at its source.
- **Observed** — `design.md` and `tasks.md` proposed `docs/audits/.raw/` and an upload step expected to publish it,
  without reading `actions/upload-artifact`'s inputs; the action excludes hidden paths unless
  `include-hidden-files: true`, so the step would have published nothing on every run.
- **Evidence** — quick-review round 1, finding 1 (`missing-task`): the action's README states hidden paths are "any file
  beginning with `.` or files within folders beginning with `.`", and `include-hidden-files` defaults to `false`; the
  repository's own run logs show the consequence-shaped silence the change exists to remove.
- **Consequence** — one review round and a design rewrite; the location moved to `build/audit-reports/`, which also left
  the Spotless markdown target. Repaired in the same unit; nothing shipped with the hidden path.
- **Knowledge boundary** — observed in the artifact and its review finding; the action's behavior was confirmed from its
  published `action.yml` and README, not from a run of the step.

### retain-auditor-run-reports#2 — The deep-review routing was derived once, at the proposal stage, and not re-derived on the implementation

- **Recorded** — 2026-11-01, by the implementing agent.
- **Stage** — review.
- **Expected** — `review-gate` §3 derives the floor from the completed implementation, so a change whose stage moves
  from proposal to implementation re-derives it.
- **Observed** — `tasks.md` recorded the proposal-stage outcome (`RECOMMENDED`, repeated-class) and the owner's skip,
  and the same note stood after the workflow edit was implemented; the re-derived outcome on the completed tree is
  `REQUIRED` (`infrastructure-or-deployment`, `.github/workflows/audit.yml`).
- **Evidence** — quick-review round 6, finding 1, quoting
  `scripts/review-routing.py --unit retain-auditor-run-reports --change retain-auditor-run-reports` →
  `ROUTING: REQUIRED`; the recorded note read `RECOMMENDED … skip`.
- **Consequence** — the deep review would have been skipped on a `REQUIRED` outcome; caught before the manual pass, the
  routing note was rewritten and the deep review ran (pass 1: 1 should-fix, 1 nitpick; pass 2: both verified).
- **Knowledge boundary** — observed in the artifact and the tool's output; whether an earlier implementation stage would
  have changed the outcome is not established, since the first implementation edit and the routing re-derivation
  happened in the same session.
