# Tasks

## 1. Config rule

- [x] 1.1 Add the positive-control rule to the `tasks` rule set in `openspec/config.yaml` — a change that adds or
      changes a check must include a task proving the check fails on a known-bad input and passes on a known-good one,
      and a task to read the output of any scratch command used as evidence — appended to the existing three rules and
      written so it parses as a single string (no unquoted `: `).
- [x] 1.2 Verify the CLI consumes the rule (the read-path probe the config gotcha requires): run
      `openspec instructions tasks --change inject-positive-control-task-rule --json`, confirm the new rule appears
      alongside the three pre-existing `tasks` rules (a malformed rule drops the whole artifact's rule set, so the
      unchanged rules are the control), then temporarily remove the new rule and confirm it disappears (positive
      control), and restore it.

## 2. Documentation

- [x] 2.1 Remove the "Inject a positive-control task into every check-adding change via `openspec/config.yaml`" idea
      from `docs/ideas.md`.
- [x] 2.2 Update the disposition of suggestion 5 in `docs/retrospectives/2026-09-19.md` to name this change.

## 3. Verification

- [x] 3.1 Run `openspec validate inject-positive-control-task-rule --strict` and confirm it passes.
- [x] 3.2 Run the OpenSpec configuration probe the CI `build` job runs — create the throwaway change exactly as `ci.yml`
      does, confirm the CLI emits no "ignoring this artifact's rules" warning, and remove the probe directory afterwards
      (so it cannot pollute `openspec validate --all`) — so the added rule cannot be silently dropped.
- [x] 3.3 Run the manual 120-character check over `openspec/config.yaml`, which Spotless does not cover:
      `perl -CSD -lne 'print if length > 120' openspec/config.yaml` returns nothing.
- [x] 3.4 Run `./gradlew spotlessCheck` and confirm the tree is clean.
