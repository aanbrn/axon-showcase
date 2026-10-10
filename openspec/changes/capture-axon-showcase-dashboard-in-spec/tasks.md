# Tasks

## 1. Implementation

- [x] 1.1 Confirm the delta spec at
      `openspec/changes/capture-axon-showcase-dashboard-in-spec/specs/showcase/deployment/helm-chart/spec.md` is a
      `MODIFIED` block for "Extra deployments and dashboards" carrying both existing scenarios in the main spec's order,
      matched by name (the provisioning scenario's `THEN` updated to name the dashboard), with the new clause naming the
      Axon Showcase dashboard.
- [x] 1.2 Run `./gradlew spotlessApply` after the last edit to the change-dir markdown, then `./gradlew spotlessCheck`.

## 2. Verification

- [x] 2.1 `openspec validate --all` passes.
- [x] 2.2 Read the delta back and confirm it names the Axon Showcase dashboard and adds no panel inventory, section
      list, or panel count.
- [x] 2.3 Run the `review-quick` subagent over the proposal, then again over the implementation, until clean.
- [x] 2.4 Run the `lesson-capture` subagent; apply its durable proposals and record the applied net `AGENTS.md` delta on
      this task. Applied: **+4 lines** — one new paragraph in the "Sync the main spec only at archive" bullet (a
      capability spec covers a human-facing artifact's outcome, not its presentation content),
      `captured:     capture-axon-showcase-dashboard-in-spec`.
- [x] 2.5 Docs refresh: verified `AGENTS.md`, `README.md`, `docs/adr/`, and `docs/ideas.md` — no further change owed
      (the README already describes the dashboard; no ADR or idea touches it; the `AGENTS.md` change is this unit's
      capture, 2.4).
- [x] 2.6 Confirmed the `helm-chart` `## Purpose` needs no refresh — its "observability wiring" already covers the
      dashboard, and the change does not falsify the Purpose's text.
- [x] 2.7 Request the user's manual review pass; the commit → push → PR sequence starts only after that approval.
- [ ] 2.8 After the `build` check is green **and the user approves**, archive the change on this branch
      (`openspec archive capture-axon-showcase-dashboard-in-spec --yes`); confirm the main `helm-chart` spec carries the
      new clause and run `openspec validate --specs`.
