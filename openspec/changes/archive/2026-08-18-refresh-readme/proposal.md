# Proposal: Refresh the README

## Why

The README predates the repo's current development practices and is missing several things a contributor to a
reference application needs: the OpenSpec spec-driven workflow, the Architecture Decision Records, and the four-tier
test model. Its project-structure tree also omits `docs/`, `openspec/`, and `gradle/`, and it does not state the
service HTTP ports or the current Spring Boot baseline.

## What Changes

- Update the **Project Structure** tree to include `docs/` (ADRs), `openspec/` (specs + changes), and `gradle/`
  (version catalog).
- Add a **Spec-Driven Development** section describing the OpenSpec workflow (specs under `openspec/specs/`, changes
  under `openspec/changes/`, propose → apply → archive via the `opsx-*` commands), pointing to `AGENTS.md` as the
  behavioral source of truth, and to the architecture decision records under `docs/adr/`.
- Add a **Testing** section summarizing the four test tiers (`test`, `componentTest`, `integrationTest`, `e2eTest`)
  and the commands to run them.
- Clarify the **service HTTP ports** (API gateway `8080`, command `8081`, projection `8082`, query `8083`) in the local
  development section.
- Note the current baseline (**Spring Boot 3.5.16**) and that a Spring Boot 4 migration is deferred (see ADR-0004).

## Capabilities

### New Capabilities

(none)

### Modified Capabilities

(none — documentation-only; no spec-level behavior change, so `skip_specs: true` is set in `.openspec.yaml`)

## Impact

- `README.md` — structure tree, new sections, and baseline/port clarifications.
- No application code, build, dependency, or runtime behavior changes.
