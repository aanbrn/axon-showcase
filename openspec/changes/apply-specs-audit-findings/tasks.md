## 1. Title-only fixes (no delta)

- [x] 1.1 Edit `openspec/specs/showcase/quality/ide-config/spec.md` first line to
      `# showcase/quality/ide-config Specification`
- [x] 1.2 Edit `openspec/specs/showcase/quality/infra-image-versions/spec.md` first line to
      `# showcase/quality/infra-image-versions Specification`

## 2. Delta specs

- [x] 2.1 `quality/agent-skills` — modify the `Per-change quality-gate and analysis subagents are available` requirement
      to stop carrying a stale enumeration (carry every existing scenario, then edit)
- [x] 2.2 `clients/command-client` — add the `Time limiter` requirement (mirroring query-client's; the client's
      `@TimeLimiter` and its Purpose already claim it)
- [x] 2.3 `quality/code-quality` — change `MUST`/`MUST NOT` to `SHALL`/`SHALL NOT` in the two requirement bodies
- [x] 2.4 `quality/dependency-management` — normalize the OpenSearch coordinate spelling to `group:artifact` (drop the
      stray space after the colon in all five mentions in the requirement)

## 3. Docs and archive-commit edits

- [x] 3.1 `AGENTS.md` — update the spec-header gotcha: its "two specs still carry non-path titles" sentence is falsified
      by this change (both are now fixed); this ships with the change's PR, not the archive commit
- [ ] 3.2 `deployment/helm-chart` — correct the Purpose's "four showcase services" (the spec deploys five, including
      web-ui, per its own `Service deployments` requirement; a Purpose cannot ride a delta, so this is an archive-commit
      edit)

## 4. Verification

- [x] 4.1 Run `openspec validate --all` and `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` (the PR gate)
- [ ] 4.2 Confirm each finding is resolved: the two titles match their paths; the helm-chart Purpose describes five
      services; the agent-skills requirement no longer carries the stale set; the command-client spec has a
      `Time limiter` requirement; `code-quality` has no `MUST`; the OpenSearch coordinates have no stray space
