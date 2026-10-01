# Tasks

## 1. The report filter

- [x] 1.1 Add `config/web-ui-updates/major-disabled.txt` — the web UI's suppression list (one npm package per line, `#`
      comments), shipped with `typescript` and a comment naming the `typescript-eslint` cap as the rationale. Verify
      with `grep -n '^typescript$' config/web-ui-updates/major-disabled.txt` and by reading the file. — verified: the
      single shipped entry is `typescript`, with the peer-range rationale in its comment.
- [x] 1.2 `build-logic/src/main/kotlin/NpmOutdatedRules.kt` (new) — the rule as a pure object: parse the list (blank/`#`
      skipped, each name kept whole), and filter an `npm outdated` table, keeping a listed package's row only when
      `Latest` shares `Wanted`'s leading integer. Locate the columns from the header; keep any row the parse cannot
      read. KDoc on the object and every function. Verify `./gradlew -p build-logic compileKotlin` succeeds. — verified:
      compiles; KDoc on the object and every member.
- [x] 1.3 `build-logic/src/test/kotlin/NpmOutdatedRulesTests.kt` (new) — `@DisplayName` on the class and every test,
      covering the rule's decisions: the suppression list's parse, each drop/keep case (a suppressed major dropped, a
      suppressed same-major kept, an unlisted major kept, an unreadable row kept, an exact match not a prefix), and the
      header-only/empty renders. Verify `./gradlew -p build-logic test` passes, and run a control that keeps every row,
      confirming the dropped-major case fails against it (a control must perturb the surface the test reads) — read the
      failing output, not only the result. — verified: 8 tests pass; the keep-every-row control reddened exactly the
      dropped-major case (`66 tests completed, 1 failed`), then reverted green.
- [x] 1.4 Wire it: `showcase-web-ui/scripts/outdated-report.sh` writes the raw table to `build/npm-outdated-raw.txt`
      (and keeps `npm-outdated.err.txt`/`npm-outdated.exit`), and `frontend-conventions.gradle.kts`'s `npmOutdated`
      `doLast` renders the filtered text to `build/npm-outdated.txt` — the file the task logs and the workflow reads —
      keeping the "no updates" / "report failed" branches, and its task `description` updated (it currently says the
      task _writes_ `npm-outdated.txt`, which the filter now produces), and the script's own header comment corrected
      (it says it "writes the web UI's npm update report", which the filter now renders). Verify
      `./gradlew :showcase-web-ui:npmOutdated` reports without the `typescript` major row and still lists the other
      packages' updates, and that `./gradlew check` runs no part of it. — verified: with the shipped list the report
      drops `typescript` (raw file still lists it, so the filter — not the lookup — removed it) and keeps `@types/node`,
      `vite`, and `vitest`; the control (entry removed) puts `typescript` back in the rendered report (1 row), and
      restoring the entry drops it again (0 rows). `check` composes `npmLint`/`npmFormatCheck`/`npmTypeCheck`/`npmTest`
      only, so it runs no part of `npmOutdated`.

## 2. Docs and close-out

- [x] 2.1 Sweep the docs and definitions this change falsifies — `AGENTS.md` (the npm report note beside the JVM
      suppression paragraph), `README.md` (the web UI reporting paragraph), `.opencode/commands/dependency-updates.md`
      (its npm sentence says the report _writes_ `build/npm-outdated.txt`, and says nothing of the suppression), the
      architecture-auditor's swept-surface enumeration in its definition and the `/audit-architecture` command (both now
      name the third list), and the `agent-skills` delta carrying that requirement — and remove the implemented idea
      from `docs/ideas.md`. Verify by reading each and confirming the idea is gone. — verified: every doc and definition
      site names the suppression list; the idea is removed, along with its now-empty dated section heading, and the
      unrelated runner-migration idea is still present.
- [x] 2.2 Refresh the `showcase/quality/dependency-management` `## Purpose` — it says the web UI's `npmOutdated` report
      "lists its outdated npm dependencies", which this change narrows by filtering suppressed majors. A delta cannot
      carry a Purpose, so the edit lands in the archive commit; record the deferral in the change report. — applied in the archive commit: the Purpose now says the report filters out the packages whose majors the project defers.
- [x] 2.3 `./gradlew spotlessApply`, then `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`; confirm green.
      Re-run `spotlessApply` after the last edit to a Spotless-owned file (a task tick included). — verified: `check`
      BUILD SUCCESSFUL; `spotlessCheck` green after the final `spotlessApply`.
- [x] 2.4 Run the `lesson-capture` subagent over the diff, review findings, and change dir; apply its durable
      `AGENTS.md` proposals and record the applied net `AGENTS.md` delta on this task. — applied: two merges into
      existing bullets, no new bullet. Net **+8 `AGENTS.md` lines** (read from the two capture hunks: `+5/−3` and
      `+10/−4`) — the "enumeration of a set" bullet now says adding a member widens every live enumeration of it,
      including an auditor's definition, its trigger command, `AGENTS.md`'s summary, and the spec requirement that owns
      the sweep (a main spec, so a `MODIFIED` delta); and the `spotlessApply`-on-final-write gotcha now names the
      self-falsifying shape (the tick claiming a gate green is the edit that breaks it). Two candidates were rejected as
      already covered (the count/enumeration drift by the non-converging-loop root-cause clause; the JVM-vs-npm
      text-filtering premise by the premise bullet).
