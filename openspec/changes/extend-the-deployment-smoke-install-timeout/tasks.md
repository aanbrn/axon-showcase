# Tasks

## 1. Set the smoke target's install timeout

- [x] 1.1 `build.gradle.kts` — the `ci` release target sets `remoteTimeout` to fifteen minutes, with a short comment
      saying why (the runner is slow to bring OpenSearch and the ingress up, and Helm's default wait expires).
      `AGENTS.md` — the deployment-smoke note records that the smoke's install wait is longer than Helm's default, and
      why. Verify with `./gradlew help` (the helm extension configures) and by reading both edits. — verified: the build
      configures; the `ci` target sets `remoteTimeout.set(Duration.ofMinutes(15))` (import `java.time.Duration` added),
      and the smoke note names it.
- [x] 1.2 Prove the setting reaches the install tasks, with a control: a scratch init script (under `$TMPDIR/opencode`)
      prints the resolved `remoteTimeout` of a `ci`-target install task (`helmInstallAxonShowcaseOsViewsToCi`), run once
      BEFORE the edit (expect no value — the plugin would pass no `--timeout`, leaving Helm's default) and once after
      (expect the fifteen minutes). Record both outputs; a value set in the build script but not reaching the task is
      the failure this catches. — verified: before → `remoteTimeout: null` (`wait: true`, `waitForJobs: true`); after →
      `remoteTimeout: PT15M`.

## 2. Close-out

- [ ] 2.1 `./gradlew spotlessApply`, then `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`; confirm green.
- [ ] 2.2 Run the `lesson-capture` subagent over the diff, review findings, and change dir; apply its durable
      `AGENTS.md` proposals and record the applied net `AGENTS.md` delta on this task.

## 3. At the PR (after the owner's approval, as part of the merge)

- [ ] 3.1 Push the branch, open the PR, and dispatch the smoke against the branch
      (`gh workflow run deployment-smoke.yml --ref extend-the-deployment-smoke-install-timeout`); confirm the run
      completes green and record its URL and conclusion. This is the end-to-end check of the install step — no local
      environment runs it — and it completes inside the change's PR, before the archive commit.
