# Tasks

## 1. Reschedule the seven weekly workflows

- [x] 1.1 Rewrite the `cron` block of each of the seven workflows to the new Saturday-17:00–19:00 UTC block, `audit`
      first: `audit.yml` `13 17 * * 6`, `dependency-security.yml` `27 17 * * 6`, `dependency-updates.yml` `43 17 * * 6`,
      `helm-updates.yml` `7 18 * * 6`, `buildpack-updates.yml` `23 18 * * 6`, `tooling-updates.yml` `37 18 * * 6`,
      `upstream-references.yml` `53 18 * * 6`. Change only the `cron` value in each.
- [x] 1.2 Verify the set is collision-free: `./gradlew verifyUniqueCronSchedules` and `./gradlew workflowLint` both
      pass, and a `grep` of the seven files confirms no two share a minute/hour/weekday and none uses a
      `:00`/`:10`/`:20`/`:30`/`:40`/`:50` minute.

## 2. Documentation

- [x] 2.1 Record the new block in the `AGENTS.md` scheduling convention (the "Schedule each workflow's runtime into the
      repo owner's night (UTC+7) …" passage): its "owner's night" guidance names no day or time today, so extend it to
      name the block (Saturday-17:00–19:00 UTC = Sunday 00:00–02:00 owner, on minutes off the top-of-hour) as the rule
      the scheduled workflows follow, keeping it a rule, not a table of per-workflow times. Reconcile the wording with
      where the block actually lands — Sunday 00:00–02:00 owner is the owner's small hours just past midnight, not the
      "night" the passage previously implied — so the durable rule names the window without mislabelling it.
- [x] 2.2 Account for the `docs/ideas.md` addition this branch carries: the dispatch-fallback idea parked in this
      change's branch (a watchdog that re-triggers a dropped `schedule`) is a delivered artifact of this unit and needs
      a task; it is parked under the change's in-flight branch per the repo's docs-refresh convention.
- [x] 2.3 Sweep `AGENTS.md`, `README.md`, and `openspec/config.yaml` for any other statement naming a specific day/time
      for these workflows and correct each (none is expected beyond 2.1 — `README.md` says only "weekly/nightly
      schedule" and `openspec/config.yaml` carries no day/time — but confirm rather than assume).

## 3. Verification

- [x] 3.1 Run `./gradlew spotlessApply spotlessCheck workflowLint verifyUniqueCronSchedules` and
      `openspec validate --all`; confirm all pass.
- [x] 3.2 Confirm the change is verified by first real fire — record in the change that a dispatch
      (`gh workflow run <file> --ref <branch>`) proves the workflow body, **not** the cron, so the new schedule is
      verified only when its first scheduled fire occurs (the next Saturday-UTC window); a dispatch is run only if a
      body-level check is needed.

## 4. Capture and delivery

- [x] 4.1 Run the `lesson-capture` subagent over this change’s diff and apply its durable `AGENTS.md` proposals (or
      record its "nothing durable" verdict) and the applied net delta. _Done: 1 proposal applied — a merge into the
      schedule bullet (the `schedule` trigger is best-effort; an earlier slot is margin, not a guarantee). Applied net
      `AGENTS.md` delta: **+3 lines**. 0 retirements._
- [ ] 4.2 Push the branch, open the PR with the change dir, confirm `build` is green, then archive
      (`openspec archive reschedule-weekly-workflows-earlier`) as an additional commit in the same PR and merge once the
      owner approves.
