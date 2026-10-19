# Tasks

## 1. Restore the task's configuration-cache compatibility

- [x] 1.1 `build-logic/src/main/kotlin/UpstreamReferencesTask.kt` and `build.gradle.kts` — replace the action's
      execution-time `project.rootDir` read with an `@get:Internal DirectoryProperty` set by the registration from
      `rootProject.layout.projectDirectory`. Verify: `./gradlew upstreamReferences` reports "Configuration cache entry
      stored" and a second run reports "Configuration cache entry reused" with the task still executing. **Done** — run
      1 stored, run 2 reused, the task executing in both (its verdict comes from the GitHub API, so it re-runs by
      design).
- [x] 1.2 Sweep the build for the same defect. Verify: a grep of `build-logic/src/main/kotlin/*.kt` and
      `build.gradle.kts` for `project.`/`rootProject`/`gradle.`/`this.project` returns no execution-time access, and
      every `doLast`/`doFirst` body in every `*.gradle.kts` references only configuration-time values. **Done** — the
      grep's only execution-time hit was the fixed line; the six `doLast`/`doFirst` bodies (dependency-security, docker,
      frontend, workflow-lint) all read hoisted configuration-time values.

## 2. Verification

- [x] 2.1 The report's content is unchanged. Verify: `build/upstream-references/report.txt`'s labels are root-relative
      in the same form the pre-fix code wrote to issue #343. **Done** — both carry `AGENTS.md:NNN` and
      `docs/adr/….md:NNN` labels; the line numbers differ only because the corpus moved.
- [x] 2.2 `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`, `spotlessCheck`, and `openspec validate --all`
      pass. Verify: run them over the changed set. **Done** — `spotlessCheck` green; `openspec validate --all` 27/27 and
      `--changes` 1/1; `check -PskipITs -Pcoverage.gate.enabled=false` BUILD SUCCESSFUL; the marker, session-continuity,
      unique-round, review-rounds, and commit-hygiene checks green; the 120-character recipe clean over the changed set.
      One earlier `check` run failed on an unrelated transient — `helmUpdateMainChartDependencies` could not fetch the
      Bitnami `common` subchart from Docker Hub — and passed on every later run.
- [x] 2.3 Dispatch the `upstream-references` workflow from the change's pushed branch and confirm it succeeds end to
      end, which also refreshes the stale issue. Verify: `gh run view <run-id> --json conclusion` reports `success`.
      **Done** — dispatched from the pushed branch as run `39184066349` and reported `success` end to end, which
      also refreshed the stale "Upstream references" issue (#343, `updatedAt` 2026-10-19T01:11Z, unchanged since
      2026-10-10). This was the only verification that exercises the workflow.

## 3. The change's own records

- [x] 3.1 Record the pre-write derivation pass: the claims table is in `design.md`, its rows derived from the CI run
      log, the local reproduction, the greps, issue #343, and the pre-change files, with the `scripts/claim-scan.py`
      candidates each resolved into a row or dismissed beside the table. **Done** — the table's rows derived from the
      sources it names, with the by-class dismissals beside it.
- [x] 3.2 Run the `review-quick` subagent over the planning artifacts, then the implementation, repeating until it
      reports no new observations; record each round's row in `docs/review-rounds.tsv` from the actual invocation,
      continuing the reviewer session the first round created. **Done** — three rounds in one continued session
      (`ses_eae6267a2ffew6pob4Q5k5wAII`: round 1 `created`, rounds 2–3 `continued`, no replacement), clean at round 3;
      findings 5 → 1 → 0, every one fixed. Rounds 1–2 raised `claim-unreproducible`, `claim-missing`,
      `incomplete-enumeration`, and `formatting`; none recurred.
- [x] 3.3 Derive the deep-review routing floor with
      `python3 scripts/review-routing.py --change fix-upstream-references-config-cache`, decide at or above it, and
      record the outcome and its signals here; run a `REQUIRED` deep review, or record the owner's explicit resolution
      for a `RECOMMENDED` one. **Done** — the derivation reports `NOT_NEEDED`, matching no signal: the changed paths
      carry none of the path classes (no public-contract, domain, persistence, or infrastructure/deployment path —
      `build-logic/` is not among them), no changed source declares a security filter chain, and the change carries no
      delta so there is no requirement-block kind. The ledger signal is absent too: no class repeats on two or more of
      the three rows (all three record `-` in the repeated-class field). Decided at the floor rather than above it: the
      change restores a behaviour `merge-governance` already specifies, its diff is a property and one registration
      line, and the quick loop closed out the implementation — so a deep review would re-read two files whose
      correctness the build's own gates verify. No deep-review session is created.
- [x] 3.4 Run the `lesson-capture` subagent over the diff, apply its durable proposals, and record the applied net
      `AGENTS.md` delta or the skip and its reason here. **Also report** any rule this change obsoletes or makes
      redundant. **Done** — the skip condition did not hold (round classes beyond the local set, a diff touching the
      authoring surface `AGENTS.md`, a `REQUIRED`-class defect the repository already had a rule for), so the subagent
      ran (session `ses_eae567ae2ffe0qandBB8tRPUIg`). One durable addition, applied by **correcting the existing bullet
      rather than adding a second one on the same subject**: the configuration-cache gotcha's sweep clause now says to
      _execute_ the tasks a workflow invokes rather than realize the graph, since `./gradlew tasks --all`'s set never
      runs a task action and a task no `check` graph requests escapes every sweep the build itself can run. The #581
      task enumeration in the same bullet is compressed to offset the addition, so the bullet lands flat, and the second
      copy of the same fact in `openspec/config.yaml`'s `context:` block is refreshed in the same change. The applied
      net `AGENTS.md` delta is the figure `git diff --numstat AGENTS.md` reports (14 added, 11 removed). **No
      retirements** — no rule became obsolete or redundant. **No authoring-system recommendation**: a check cannot
      enforce this at reasonable cost (the workflow-invoked set is credentialed and networked), so the prose rule is not
      a duplicate of one; the empirical sweep is parked in `docs/ideas.md` by task 3.5, whose home is the scheduled
      pattern rather than `check`.
- [x] 3.5 `docs/ideas.md` — park the follow-up this change surfaces (an empirical cache-store sweep over the tasks the
      workflows invoke), under a dated section with its trailing status tag. Verify: the entry is present and no idea
      this change implements remains. **Done** — the entry is under `## 2026-10-19` with its `— parked; no change yet.`
      tag, and this change implements no parked idea.
- [x] 3.6 Ask the user for the manual review pass once 3.2 is clean and 3.4 is done; do not commit, push, or archive
      before they approve. **Done** — requested.
