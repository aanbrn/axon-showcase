<!-- This file is the repository's pull-request standard, and it governs every pull request. A human, or the agent's
     `gh pr create`, supplies the body directly — and `--body-file` bypasses this template, so that path carries these
     sections and the title rule itself. A pull request the cloud action opens takes its body and title from the agent's
     response: the response carries the sections, and the title is checked and renamed before the merge when it deviates
     from the rule. A report pull request's body is the report, which its capability requirement owns.

     Title — one imperative English phrase naming the change, with no prefix: not `<change-name>: …`, not `docs: …`,
     not a Conventional Commits type. The OpenSpec change name and the issue link belong in Summary, where they are
     clickable. Merged titles become the release notes, so aim at one commit subject.

     Body — `## Summary` then `## Test plan` are required, in that order; further sections may follow them. -->

## Summary

<!-- What changes, and why. Name the OpenSpec change or link the issue this implements. -->

## Test plan

<!-- How this was verified — the commands run, the gates that passed, and anything deliberately left undone.
     `./gradlew check` is the baseline (`-PskipITs -Pcoverage.gate.enabled=false` for a Docker-free run), and
     `./gradlew spotlessApply` belongs after the last edit to a Spotless-owned file. -->

- [ ] `./gradlew check` (or the Docker-free variant) is green
- [ ] `spotlessApply` run after the final edit to a Spotless-owned file
- [ ] Behavior changes went through an OpenSpec change (with its archive commit on this PR) — or this is a docs refresh,
      a standalone fix, or a dependency bump, which need no change dir
- [ ] The per-unit `lesson-capture` decision is recorded — it ran with its applied `AGENTS.md` delta, or it was skipped
      under `AGENTS.md`'s skip condition with the reason — as a task in the change's `tasks.md` or, for a unit with no
      change dir, by ticking this line and noting the outcome
- [ ] The pre-write derivation pass ran, its **claims table** recorded — in the change's `design.md` (or `tasks.md` for
      a unit that writes no `design.md`), or by ticking this line and noting the table for a unit with no change dir
