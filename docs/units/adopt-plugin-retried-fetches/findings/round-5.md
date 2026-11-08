- [contradiction] `openspec/changes/adopt-plugin-retried-fetches/design.md:41` — the claim that a timeout needs a
  reproduced slow-index failure, and this unit has neither, is contradicted by #238's reproduction.
- [missing-task] `openspec/changes/adopt-plugin-retried-fetches/design.md:83` — the bump's task-class risk rests on a
  release-install run that no task performs.
- [test-control] `openspec/changes/adopt-plugin-retried-fetches/tasks.md:17` — the verification that the bullet names no
  task this change deletes cannot fail, because that revision deleted no task.
- [wording] `openspec/changes/adopt-plugin-retried-fetches/proposal.md:28` — "no build script changes beyond the pin"
  while the list edits helm/chart/build.gradle.kts.
