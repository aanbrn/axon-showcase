# Vendor the AxonIQ Axon 4→5 migration agent skills

## Why

The Axon 5 migration is deferred (see `config/dependency-updates/major-disabled.properties`, `org.axonframework`
blocked) and will be reopened as a dedicated change. AxonIQ publishes agent skills for migrating applications from
Axon Framework 4 to Axon Framework 5 (`AxonIQ/agent-skills`, plugins `axoniq-migration`), and the skills follow the
same `SKILL.md` format opencode uses for project skills. Vendoring the migration plugin's three skills into
`.opencode/skills/` gives agents in this repo the official migration recipes (phased orchestrator, OpenRewrite bulk
recipe runner, per-target isolated-test helper) ready for when the migration change starts — and lets an agent run the
migration against this very codebase.

## What Changes

- Vendor the `axoniq-migration` plugin (version 0.2.2, Apache-2.0) from `AxonIQ/agent-skills` into
  `.opencode/skills/`, adding three skills: `axon4to5-migrate-code`, `axon4to5-openrewrite`, `axon4to5-isolatedtest`.
- Record provenance in the change (source repo, plugin version, license) and capture it in the spec so future updates
  pin to an explicit upstream version rather than drifting.
- Skills are discovered by opencode automatically (project skills dir `.opencode/skills/`), with no runtime/build
  impact on the application.

## Capabilities

### New Capabilities

- `showcase/quality/agent-skills`: the repository vendors curated agent skills (currently the AxonIQ Axon 4→5
  migration plugin) under `.opencode/skills/`, records their upstream source and version, and keeps them usable by
  agents without affecting the application build.

### Modified Capabilities

(none)

## Impact

- **Added**: `.opencode/skills/axon4to5-{migrate-code,openrewrite,isolatedtest}/` (~141 files, ~774 KB).
- **Build/runtime**: none — skills are agent-facing tooling, not project source; they do not enter Gradle module
  classpaths or Docker images.
- **Provenance**: pinned to `axoniq-migration` plugin 0.2.2 from `AxonIQ/agent-skills` (Apache-2.0); a future
  upstream update is a deliberate change, not silent drift.
- **Docs**: `AGENTS.md` gains a note on where the vendored skills live and how to refresh them.