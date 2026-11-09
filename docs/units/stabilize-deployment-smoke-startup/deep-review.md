# Deep-review record — stabilize-deployment-smoke-startup

## Routing decision

- **Outcome** — `REQUIRED`, derived at the implementation boundary; the proposal boundary's `RECOMMENDED` was superseded
  by the owner's decision to skip it.
- **Derived** — 2026-11-09 with
  `python3 scripts/review-routing.py --unit stabilize-deployment-smoke-startup --change stabilize-deployment-smoke-startup`,
  which printed `ROUTING: REQUIRED` with two `infrastructure-or-deployment` signals.
- **Signals** — `.github/workflows/deployment-smoke.yml` and `helm/values/axon-showcase/values-ci.yaml`, both matched by
  the tool's `INFRASTRUCTURE` prefixes and both returning `REQUIRED` through `floor()`.
- **Owner's run-or-skip** — not applicable to a `REQUIRED` outcome; the deep review runs before completion.

## Pass 1 — full pass

- **Reviewed revision** — `HEAD` `634bc3b5` with the unit's work uncommitted; the branch was one commit behind
  `origin/main` (`caf72739`, #876) at the time of the pass.
- **Outcome** — 3 findings (1 blocking, 2 should-fix); no finding rejected.
- **Limits** — no cluster locally, so the deployment end-to-end remains the branch dispatch's; the gate harness and the
  three stub controls run against a stub gateway.

### Findings

- `stale-base` — **FIXED_AND_VERIFIED.** The branch sat one commit behind `origin/main`, so its diff reverted #876's
  Paketo pins and deleted a merged unit's archive and records. Fixed by stashing the `deployment-operations` pack edit,
  `git merge --ff-only origin/main` to `caf72739`, and popping the stash. Evidence:
  `git diff origin/main --stat -- .opencode/skills/deployment-operations/SKILL.md` is 14 insertions and 0 deletions, and
  `git diff --name-status origin/main` lists only this unit's files.
- `expired-start-time` — **FIXED_AND_VERIFIED.** The warm-up `startTime` was computed once outside the 30-attempt loop,
  so an attempt past ~2 minutes would carry a past timestamp and be rejected by `@Future` at the command service. Fixed
  by computing `start_time` inside the loop. Evidence: the keep-alive-only and data-frame harness controls still behave
  (red and green respectively); the workflow lints and the check is green.
- `id-only-from-body` — **FIXED_AND_VERIFIED.** A command-dispatch timeout answers `202` with the showcase ID only in
  the `Idempotency-Key` header, so a body-only read failed falsely under the very contention this change targets. Fixed
  by writing the response headers to a file and falling back to the header. Evidence: a new stub mode answering `202`
  with an empty body and the header drives the harness green (`live-event gate passed with run-1`), while the
  keep-alive-only control stays red.

## Pass 2 — targeted fix verification

- **Reviewed revision** — `HEAD` `caf72739` with the unit's work uncommitted (equal to `origin/main`).
- **Outcome** — the three pass-1 findings confirmed `FIXED_AND_VERIFIED`; 1 new nitpick, fixed in this pass.
- **Limits** — no cluster locally; verification by source, the stub harness, and the check suite.

### Findings

- `claims-anchor` — **FIXED_AND_VERIFIED.** The claims table anchored the `gatlingRun` invocation at a line number that
  the pass-1 fixes moved. Fixed by stating the structural fact instead of the line: the `/showcases` readiness loop
  precedes the `gatlingRun` invocation, with the live-event gate between them.
