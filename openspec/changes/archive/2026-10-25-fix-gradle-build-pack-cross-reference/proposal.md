# Proposal

## Why

The 2026-10-24 agent-tooling audit reported one stale cross-reference. The `gradle-build` pack's coverage-gate bullet
names the PR CI gate verbatim — `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`, verified against
`.github/workflows/ci.yml:69` — but its parenthetical sends the reader to `AGENTS.md`'s `## Continuous Integration`,
which no longer states that command or its Docker-free rationale: `extract-ci-narrative` (#644) thinned the section to
the gate-placement rule, the cold-build timeout, the `ubuntu-latest` migration, and its two workflow procedures
(add-and-edit and update-check). The Docker-free form and its rationale live in `AGENTS.md`'s `## Build & Test` — which
also carries the command-surface pointer to the `gradle-build` pack — and in the bullet itself, so the pointer lands on
a section that does not carry what it promises.

## What Changes

- `.opencode/skills/gradle-build/SKILL.md` — the coverage-gate bullet's parenthetical is repointed from `AGENTS.md`'s
  `## Continuous Integration` to its `## Build & Test`, which states the Docker-free form and why it keeps the coverage
  gate green without integration data. The command, its rationale, and every other clause of the bullet are unchanged.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None — no capability spec's behavior changes. The `showcase/quality/agent-skills` requirement covers the subject packs'
existence and loading, not the wording of one cross-reference inside a pack, so this ships as a `skip_specs` change.

## Impact

- `.opencode/skills/gradle-build/SKILL.md` — one parenthetical in one bullet.
- `docs/ideas.md` — one parked entry for the capture's authoring-system recommendation (a finding's report item carries
  evidence per proposition); no code, build, dependency, test, deployment, spec, `AGENTS.md`, or `README.md` change.
