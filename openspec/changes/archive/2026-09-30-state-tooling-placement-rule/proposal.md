# Proposal

## Why

The repository splits its tooling across two homes with no stated rule: `scripts/` holds `doctor.sh`, `setup-idea.sh`,
`experience-analysis.sh`, `install-git-hooks.sh`, `load-test-baseline.sh`, and the commit-hygiene checker, while
`.opencode/` holds agents, commands, and skills — including the three commands that trigger scripts
(`/check-tooling`→`doctor.sh`, `/setup-idea`→`setup-idea.sh`, `/retrospective`→`experience-analysis.sh`). Which artifact
belongs where is re-decided per instance, and the closest thing to an answer lives only in `experience-analysis.sh`'s
header comment. Nothing states the boundary, so it drifts and is re-litigated on every new tool.

## What Changes

- State the placement rule once, in `AGENTS.md`, derived from the repository's actual executables (13 tracked, in three
  locations): **`scripts/` owns repository-wide tooling** — the checkers, dev scripts, and guards a shell or Python runs
  for the whole repo; **a module keeps its own tooling beside it** (`showcase-web-ui/scripts/outdated-report.sh`, the
  `npmOutdated` task's report script, and `showcase-web-ui/start.sh`, the container entry point); and **`.opencode/`
  owns the OpenCode-runtime surface** (agents, commands, skills) and holds no script, so a command may trigger a
  `scripts/` tool but never hosts its implementation. The few repo-root executables are named as their own case —
  `gradlew` (generated) and `db.sh` / `setup-hosts.sh` (human-facing setup entry points the README documents) — so the
  rule describes where things are rather than claiming one home for all of them.
- Route every current instance to the rule in the same place, so the split is legible rather than inferred: the three
  script+trigger pairs, the trigger-less tools (`install-git-hooks.sh`, `load-test-baseline.sh`), the guard's checker
  (`commit-hygiene.py`, run by the hook **and** by the Gradle `verify*` tasks), the module-local pair, and the root
  entry points above.
- Remove the placement rationale from `scripts/experience-analysis.sh`'s header, leaving it to describe what the script
  does — the answer now lives in the one stated rule rather than repeated per script.
- Reconcile `README.md`'s project-structure tree with the rule: its `scripts/` comment enumerates two of eight files,
  and the tree does not show the module-local pair at all.

This is a prose-only rule: it states where artifacts already live, moves no file, and changes no behavior.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None. This change states a documentation convention about where files live; it changes no observable behavior, so no
spec requirement is affected and the change sets `skip_specs: true`.

## Impact

- Edited files: `AGENTS.md` (the rule, extending the existing tooling/`scripts/` guidance rather than accreting a new
  bullet), `scripts/experience-analysis.sh` (header rationale removed), `README.md` (the structure tree reconciled with
  the rule).
- No code, build, test, or deployment behavior changes; no files move. The rule describes the layout the repository
  already has. Verified against the full tracked-executable set (13 files): 8 under `scripts/`, 3 at the repository root
  (`gradlew`, `db.sh`, `setup-hosts.sh`), and 2 module-local (`showcase-web-ui/scripts/outdated-report.sh`,
  `showcase-web-ui/start.sh`); `.opencode/` holds no executable — its only non-config content is `agent/`, `commands/`,
  and `skills/`.
- `docs/ideas.md`: the promoted idea (issue #456) is removed once implemented, per the docs-refresh convention — the
  change implements it, so the entry leaves the scratchpad.
