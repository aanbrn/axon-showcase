# Tasks

## 1. Fix the classification

- [x] 1.1 In `scripts/doctor.sh`, change the `git-hooks` probe's report class from `required` to the advisory class (the
      same one the optional tools use), in both the satisfied and the unsatisfied branch, so an uninstalled hook is
      reported with `./scripts/install-git-hooks.sh` but does not set the failure flag. Leave the `docker-daemon` probe
      `required`. Verify: read the two branches; run the script on this machine (hooks installed) and confirm no
      behavior change to the output; then run it with the hooks path unavailable and confirm `git-hooks` reads as
      not-installed while the default run still exits 0.
- [x] 1.2 Prove the new classification against a controlled input, both directions, before reading the result: with the
      Docker daemon reachable and the hooks absent the default run exits **0**; with the daemon unreachable it exits
      **non-zero**. Use a controlled `PATH`/config (a `git` shim returning no `core.hooksPath`, and a `docker` shim
      whose `info` fails) so the control does not depend on this machine's state. Verify: the two exit codes, taken from
      the command itself and not through a pipe.

## 2. Verification

- [x] 2.1 Run `openspec validate --changes` and confirm the delta validates with the repo-state requirement's two
      existing scenarios preserved plus the two new ones. Verify: exit 0, `1 passed`, and that the four scenarios appear
      under the `MODIFIED` block.
- [x] 2.2 Run `./gradlew spotlessApply` after the final edit to a Spotless-owned file (the change dir's markdown) and
      then `./gradlew spotlessCheck`; `scripts/doctor.sh` is not formatter-wrapped, so check it against the manual
      120-character rule (`perl -CSD -lne 'print if length > 120'`) and keep its own shell syntax check
      (`dash -n scripts/doctor.sh`, since it is strict POSIX `sh`). Verify: both Gradle tasks succeed, the shell syntax
      check is clean, and no over-120 line was added.
- [x] 2.3 Run the Docker-free gate `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` and confirm green — the
      change alters no build input. Verify: the task completes successfully.
- [x] 2.4 Read the doctor's output as content against the shipped spec, not against this plan: confirm the hooks are in
      the repo-state block and reported with a remedy, the daemon still fails the default run, and no other probe's
      class moved. Record the reading in the change's report.
- [x] 2.5 Run the `lesson-capture` subagent over the diff, apply the durable proposals the main agent judges worth
      keeping, and record the applied net `AGENTS.md` delta on this task. Verify: the subagent's verdict is recorded and
      any applied edit is visible in `git diff AGENTS.md`.
