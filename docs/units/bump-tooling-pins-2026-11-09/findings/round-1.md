- [missing-task] `openspec/changes/bump-tooling-pins-2026-11-09/tasks.md:3.2` — the review loop's task omits writing the
  round's finding-instance file, which `check` gates
- [contradiction] `docs/units/bump-tooling-pins-2026-11-09/unit.md:checkpoint` — the Resume checkpoint records Plan as
  not started and names a `design.md` the change deliberately skips
- [incomplete-enumeration] `openspec/changes/bump-tooling-pins-2026-11-09/tasks.md:dismissals` — the block covers
  `[path]`, `[cross-ref]` and `[absolute]` but not the `[count]` kind the scan emits
- [missing-task] `openspec/changes/bump-tooling-pins-2026-11-09/tasks.md:1.4` — the regeneration task drops
  `/opsx-tool-update` step 6's inventory-change branch
- [test-control] `openspec/changes/bump-tooling-pins-2026-11-09/tasks.md:2.5` — the config-read-path probe omits the
  dropped-quote control that must make `openspec validate --all` exit non-zero
- [impact-omission] `openspec/changes/bump-tooling-pins-2026-11-09/proposal.md:impact` — Impact names the `build` gate
  but not the `opencode` and `audit` installs of the same CLI
- [structured-writing] `openspec/changes/bump-tooling-pins-2026-11-09/proposal.md:what-changes` — a semicolon-joined
  sentence leaves the template-change branch implied
