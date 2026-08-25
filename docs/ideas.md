# Ideas

Short notes to remember emerging development ideas. An idea becomes an OpenSpec change only when acted on — this file
is a scratchpad, not a backlog of planned work.

## 2026-08-25

- Add CI: the repo has no workflows today, but every quality gate already lives inside Gradle tasks (`check`,
  `integrationTest` via Testcontainers, `e2eTest` building the four service images, Snyk, Helm lint). Open question: PR
  fast-check (`-PskipITs`, Docker-free) vs full `check` on main, e2e cadence, Snyk scheduling, OpenSpec validation as a
  gate.
