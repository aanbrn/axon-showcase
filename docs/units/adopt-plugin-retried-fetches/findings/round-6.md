- [missing-task] `docs/units/adopt-plugin-retried-fetches/rounds.tsv:1` — round 5's report arrived, but its row and
  findings file were never written.
- [test-control] `openspec/changes/adopt-plugin-retried-fetches/tasks.md:21` — the verification reads the timeout and
  retry settings through --info, where neither is observable.
- [coherence] `openspec/changes/adopt-plugin-retried-fetches/proposal.md:22` — it gives the update and dependency task
  the same retry and cache TTL, while the dependency task has no cache TTL.
- [wording] `openspec/changes/adopt-plugin-retried-fetches/tasks.md:29` — skipRefresh = true is a no-op because the
  plugin's rule already sets it.
- [missing-task] `openspec/changes/adopt-plugin-retried-fetches/tasks.md:54` — the design requires the packaging's cache
  store and reuse, but the packaging runs once.
