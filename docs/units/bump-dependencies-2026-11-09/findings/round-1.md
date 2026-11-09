- [single-sourcing] `openspec/changes/bump-dependencies-2026-11-09/tasks.md:3-5` — the tasks file copies the unit
  record's `Workspace:` fields and names no record; `--workspace-records` fails for it.
- [claim-unreproducible] `openspec/changes/bump-dependencies-2026-11-09/design.md:105` — the "no live document names a
  taken version" row's own grep hits `docs/adr/0012-prefer-typed-opensearch-java-client.md:49` and `docs/ideas.md:776`
  for `9.5.5`, and neither site is named in the artifacts.
- [missing-task] `openspec/changes/bump-dependencies-2026-11-09/design.md:78-86` — the verification plan never compiles
  the `e2eTest` classpath, which `check` excludes and a resolution-affecting change must exercise.
- [coherence] `openspec/changes/bump-dependencies-2026-11-09/design.md:53` — "the two `build-logic` pins" contradicts
  the proposal's one `build-logic` pin; `spotless-plugin` is shared with the root build and counted among the ten
  catalog rows.
- [impact-omission] `openspec/changes/bump-dependencies-2026-11-09/proposal.md:39-40` — the `guava` impact omits the
  `checkstyle` and Error Prone `annotationProcessor` tool classpaths it also moves.
- [wording] `openspec/changes/bump-dependencies-2026-11-09/design.md:21-24` — the eight npm rows are listed by the
  report's Current column, so the artifact never states the versions being taken.
- [claim-unreproducible] `openspec/changes/bump-dependencies-2026-11-09/design.md:38` — "the gateway and the clients
  carry the reactor-netty stack" does not reproduce; only the four services declare it.
- [incomplete-enumeration] `openspec/changes/bump-dependencies-2026-11-09/proposal.md:43-44` — the bundle-path bullet
  names four of the eight taken npm rows.
- [claim-missing] `openspec/changes/bump-dependencies-2026-11-09/design.md:69-71` — the "three coordinates the issue
  does not carry and one pin it understates" claim has no claims-table row, and "understates" does not reproduce.
- [incomplete-enumeration] `openspec/changes/bump-dependencies-2026-11-09/proposal.md:30-32` — the `skip_specs`
  justification names only the `zstd-jni` floor, not the other `dependency-security` floors this bump crosses.
- [structured-writing] `openspec/changes/bump-dependencies-2026-11-09/proposal.md:36-40` — one Impact bullet carries
  five independent reach statements joined by semicolons.
