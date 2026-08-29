# Design: vendor the AxonIQ migration agent skills

## Context

The Axon 5 migration is deferred (ADR-0004 for Spring Boot 4; `org.axonframework` major blocked in
`config/dependency-updates/major-disabled.properties` for Axon). When it reopens, agents will need the official Axon
4→5 migration recipes. AxonIQ publishes them as an agentskills.io-format plugin (`axoniq-migration` v0.2.2,
Apache-2.0) in `AxonIQ/agent-skills`. opencode discovers project skills in `.opencode/skills/<name>/SKILL.md` — the
same layout the upstream skills use, so they can be vendored as-is. See `proposal.md` for motivation.

## Goals / Non-Goals

- **Goal**: vendor the three `axoniq-migration` skills under `.opencode/skills/` so agents can run them now.
- **Goal**: pin provenance (source repo, plugin version) so updates are deliberate.
- **Non-Goal**: build-on-Axon-5 (`axoniq-app-development`) or framework-contribution skills — not needed until after
  migration.
- **Non-Goal**: automate upstream refresh — an explicit, reviewed change per version (see spec).

## Decisions

### Vendor via direct file copy into `.opencode/skills/` (no tool)

The upstream `npx skills` installer requires Node ≥22.20 and only downloads skill folders anyway — the skills are
plain `SKILL.md` + assets/references/scripts. Copying the three skill directories from the
`plugins/axoniq-migration/skills/` tree of `AxonIQ/agent-skills@main` at plugin version 0.2.2 achieves the same
result without depending on the `skills` npm package. (Node was upgraded to v24.20.0 while planning, so `npx skills`
now works too; the direct copy is still preferred to keep the vendoring self-contained and reviewable as a plain
file change.)

- *Alternatives considered*: (1) `npx skills add` — functional since the Node 24 upgrade, but adds a tool dependency
  and an interactive picker for what is a static three-folder copy; (2) git subtree/submodule — heavier than
  warranted for static reference content. Rejected.

### Preserve the upstream skill layout verbatim

Each skill directory (its `SKILL.md`, `assets/`, `references/`, `scripts/`) is copied unchanged, preserving internal
relative references. Skills are self-contained (they reference Maven/Gradle and remote OpenRewrite recipes at runtime,
not sibling files), so no path rewriting is needed.

- *Risk*: a skill's `scripts/` may invoke local tools or network calls when executed; that is their intended behavior
  and is gated by opencode's `allowed-tools`/permission config at invocation time.

### Record provenance in the spec, not a separate manifest

The upstream source, plugin name, and version are captured in the `agent-skills` capability spec (and the change's
`proposal.md`/`tasks.md`). No extra vendoring-manifest file is added, keeping the footprint minimal and the
convention consistent with how the project records tooling provenance (e.g., openspec pin in ci.yml).

## Risks / Trade-offs

- [Vendored skills drift from upstream] → Mitigation: spec requirement that updates are explicit changes with the
  recorded version bumped; AGENTS.md note points at the source.
- [Skill execution side effects (builds, network)] → Mitigation: skills are agent-invoked and gated by opencode
  permissions; they do not run in CI or the build.
- [Repo size grows ~774 KB] → Mitigation: static reference content, one-time; acceptable for the value when the
  migration reopens.

## Migration Plan

No runtime migration — this is additive agent tooling. The skills appear as project skills immediately after the
files are committed. Rollback: delete the three directories and the AGENTS.md note.

## Open Questions

None.