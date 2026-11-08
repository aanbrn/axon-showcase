# Process defects — adopt-plugin-retried-fetches

### adopt-plugin-retried-fetches#1 — A consultation hand-off carried a premise the repository's own artifacts contradict

- **Recorded** — 2026-11-09, main agent session, proposal stage.
- **Stage** — proposal.
- **Expected** — the hand-off to the `technical-advisor` supplies the premise the decision rests on, verified against
  the repository's own evidence before it is offered as context.
- **Observed** — the first hand-off stated that the plugin's `helm repo update` cannot be scoped to the declared
  repositories, and the advisor's confirmed problem and its first two recommendations rested on that statement. The
  repository's own commit `bf0dbd61` (`#718`) scopes that command with `extraArgs.addAll("bitnami")`, and the upstream
  issue `#238`'s body records the same route plus `extraArgs.addAll("--timeout", "15m")`, stating both were checked
  against the executed command line.
- **Evidence** — `git show bf0dbd61 -- helm/chart/build.gradle.kts` carries `extraArgs.addAll("bitnami")`, and
  `gh api repos/build-extensions-oss/gradle-helm-plugin/issues/238 --jq .body` carries the "What we work around today"
  list.
- **Consequence** — the advisor's first two reports recommended keeping both workarounds on a false premise; the
  artifacts were rewritten twice and the plan returned to retiring both, with `extraArgs` as the scoping route. The
  correction is applied in `proposal.md`, `design.md` and `tasks.md`; the runtime check of `extraArgs` on 3.3.1 is the
  implementation's probe.
- **Knowledge boundary** — observed the hand-off text, the advisor's reports, and the two sources; did not run the
  plugin's tasks with `extraArgs` on 3.3.1, so the route is established from `#718`'s shipped diff and the upstream
  report, not from a run of this change.

### adopt-plugin-retried-fetches#2 — A quick-review round's report arrived and its record was not written

- **Recorded** — 2026-11-09, main agent session, implementation stage.
- **Stage** — implementation.
- **Expected** — after a quick-review report arrives, append its ledger row and write
  `docs/units/<unit>/findings/round-<N>.md` before the next round.
- **Observed** — round 5's report arrived and the loop continued to round 6; its row and instances file were missing
  until round 6's review reported the gap.
- **Evidence** — round 6's `missing-task` instance;
  `commit-hygiene.py --unique-rounds --expect adopt-plugin-retried-fetches:5` failed before the row was written.
- **Consequence** — the ledger was non-contiguous for one round; the repair cost one round's work; no product damage.
- **Knowledge boundary** — observed the missing row and the check's failure; did not establish why the step was skipped
  beyond the session's own sequence.

### adopt-plugin-retried-fetches#3 — The pack claimed the upstream papercuts were reported before the comment was posted

- **Recorded** — 2026-11-09, main agent session, implementation stage.
- **Stage** — implementation.
- **Expected** — a claim about a surface outside the repository is written once the publication exists and its URL
  resolves.
- **Observed** — the install-path bullet said the two API papercuts "are reported on `#238`" while task 5.1 was open;
  deep-review pass 1 reported it as `upstream-report-not-posted`.
- **Evidence** — pass 1's finding in `docs/units/adopt-plugin-retried-fetches/deep-review.md`; `tasks.md` 5.1 was
  unticked at that point.
- **Consequence** — the pack would have shipped a false present-tense claim had the change merged before posting; the
  comment is now posted at
  `https://github.com/build-extensions-oss/gradle-helm-plugin/issues/238#issuecomment-6728129491` and pass 2 verified
  it.
- **Knowledge boundary** — observed the claim and the unticked task; the comment's publication is verified by the API
  response.
