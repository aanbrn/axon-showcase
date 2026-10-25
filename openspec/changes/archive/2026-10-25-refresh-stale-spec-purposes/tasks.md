# Tasks

Workspace: unit=refresh-stale-spec-purposes; path=/Users/afanasyev/Development/axon-showcase;
branch=refresh-stale-spec-purposes; writer=ses_e8f026f7dffeGSlwOe26i6eZUF

## 1. Refresh the two stale Purposes

- [x] 1.1 In the **archive commit**, rewrite the `clients/query-client` Purpose's "Contract source" sentence to this
      text, re-derived from the requirement's own clauses (`query-client/spec.md:9-11` and the "Query error translation"
      requirement at `:93-98`), so the audit's clause-by-clause form is not reproduced a piece at a time: "**Contract
      source:** the RPC, query types, and error codes this client uses are owned by the `read-side/query-service` spec —
      the generic `Dispatch` RPC, the queries `FetchShowcaseListQuery` and `FetchShowcaseByIdQuery`, and the error codes
      `INVALID_ARGUMENT` (which this client maps to its own `INVALID_QUERY` when the status carries field errors) and
      `NOT_FOUND`." No requirement or scenario changes.
- [x] 1.2 In the **archive commit**, rewrite the `quality/infra-image-versions` Purpose to this paragraph, covering both
      `check` gates the requirement holds (`infra-image-versions/spec.md:14-24`): "Keeps the repository's pinned
      versions single-sourced: the version catalog holds the infrastructure components' (PostgreSQL, Kafka, OpenSearch)
      official image tags for the test surfaces (docker-compose, Testcontainers) and the Helm chart versions, every
      chart coordinate being concrete — the Bitnami infrastructure charts, the observability charts, and the application
      chart's `common` subchart. The `deployment-operations` pack states those chart versions in its manual
      `helm install` commands and the catalog's Paketo builder/buildpack pins, and a build gate verifies each documented
      pin against its catalog coordinate, and each infrastructure component's chart-preconfigured `image.tag` against
      the official image tag used in tests." No requirement or scenario changes.
- [x] 1.3 Confirm both edits are confined to their `## Purpose` paragraphs, that the main spec stays unedited before the
      archive, and that `openspec validate --all` still passes after they land. **Done** — `git diff` before the archive
      showed only the two `## Purpose` paragraphs, and `openspec validate --all` reported 27 passed, 0 failed after them.

## 2. Pre-write derivation pass

- [x] 2.1 The pre-write derivation pass ran over this change's artifacts: each fact below was re-derived from its named
      command before the artifacts were finished, and every candidate `scripts/claim-scan.py` surfaced was resolved into
      a row or a dismissal beside the table. **Done** — the table and its four per-kind dismissals are below, seeded
      from `python3 scripts/claim-scan.py`, whose diff against `origin/main` includes this change dir's intent-to-add
      files and the review-ledger row the loop appends.

### Claims table

| #   | Claim the change states                                                                                                                                            | Source command and its output                                                                                                                                                                                                                                                                                                           |
| --- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------ | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1   | The query-service spec produces `INVALID_ARGUMENT` and never `INVALID_QUERY`                                                                                       | `grep -c INVALID_ARGUMENT openspec/specs/showcase/read-side/query-service/spec.md` → `5`; `grep -c INVALID_QUERY …` → `0`                                                                                                                                                                                                               |
| 2   | The client derives `INVALID_QUERY` from an `INVALID_ARGUMENT` status                                                                                               | `grep -n "Query error translation" openspec/specs/showcase/clients/query-client/spec.md` → `93:`; the requirement's first sentence (`:95-98`) maps an `INVALID_ARGUMENT` status to `INVALID_QUERY`                                                                                                                                      |
| 3   | The `command-client` Purpose's form is "the service's own codes, each present in the service spec"                                                                 | `grep -n "Contract source" openspec/specs/showcase/clients/command-client/spec.md` → `9:` (its four codes at `:10-12`); `grep -c` over `openspec/specs/showcase/write-side/command-service/spec.md` → `ILLEGAL_STATE` 6, `NOT_FOUND` 2, `TITLE_IN_USE` 1, `INVALID_COMMAND` 3                                                           |
| 4   | The infra requirement governs the observability charts, the `common` subchart, and the Paketo pins, and states its own universal claim over every chart coordinate | `grep -c` over `openspec/specs/showcase/quality/infra-image-versions/spec.md` → `prometheus-community-stack` 2, `grafana-tempo` 2, `common` 2, `Paketo` 3; the requirement reads "Every Helm chart coordinate in the version catalog … SHALL be a concrete version"                                                                     |
| 5   | `check` verifies the documented pins and each infrastructure component's chart-preconfigured `image.tag`                                                           | `grep -n "verifyInfraImageVersions\|verifyDocumentedPins" build.gradle.kts` → registered at `:162` and `:626`, `dependsOn`-ed from `check` at `:662` and `:664`                                                                                                                                                                         |
| 6   | The change carries no delta and no requirement text change                                                                                                         | `git status --porcelain -uall` → this change dir's three files (intent-to-add) and this unit's `docs/review-rounds.tsv` rows, with no other tracked file modified; `git diff -- openspec/specs/` → empty (the two Purpose edits are planned in tasks 1.1-1.2 for the archive commit, not measured)                                      |
| 7   | No `AGENTS.md`, `README.md`, `docs/adr/`, or `docs/ideas.md` text restates either Purpose, so no docs refresh and no parked idea is owed                           | `grep -n "infra-image-versions\|query-client" AGENTS.md README.md` → references to the capability path, the module name, and a `captured:` unit name only; `grep -rn "INVALID_QUERY\|infra-image-versions" docs/adr/` → no output; `grep -n "Purpose" docs/ideas.md` → the only entry is the unrelated `gateway/rest-api` one at `:613` |

Dismissals, one reason per kind `scripts/claim-scan.py` reported for this diff:

- **`count`** — a candidate is a numbered task item's leading `N.M`, the source report's finding number, a figure the
  change records as a measured result (a claims row's own command output, or a recorded check result in the review
  handover), or a field of this unit's review-ledger row — the round, class count, compliance-miss count, and session
  identifier that `docs/review-rounds.tsv` owns, and whose round uniqueness, round contiguity, and session continuity
  `scripts/commit-hygiene.py` checks.
- **`path`** — a candidate is a repository path the change names as an edit target or a command's argument, a
  spec-relative shorthand (`query-client/spec.md:9-11`, resolved from its spec directory), a git ref (`origin/main`), a
  catalog term, or a shell command string; each resolves as what it names — a path, a ref, a catalog pair, or an
  invocation the change runs.
- **`cross-ref`** — the candidates are the workspace path in the `Workspace:` record and the phrase `builder/buildpack`
  inside "the Paketo builder/buildpack pins", which names two catalog terms rather than an artifact reference.
- **`absolute`** — every candidate is the proposal's `None` capability placeholder, a quantifier the row it sits in
  covers, or a scope word confined to its own sentence.

The report's suggested rewrites are leads, not sources — each clause above is re-derived from the spec or the build
script it names; the audit report's own line numbers are not quoted.

## 3. Verification

- [x] 3.1 Run `./gradlew spotlessApply` then `./gradlew spotlessCheck` and `openspec validate --all`; confirm all pass
      after the archive commit's edits. **Done (pre-archive half)** — `spotlessCheck` clean, `openspec validate --all` →
      27 passed, 0 failed; the after-archive half runs with task 6.2.
- [x] 3.2 Run the 120-character check over the change's whole changed set, derived with
      `git status --porcelain --untracked-files=all`, using the `perl -CSD -lne` recipe. **Done** — `AGENTS.md` 6
      over-length lines, the same count as `HEAD` (the formatter-padded packs table); `docs/review-rounds.tsv` rows are
      one line by contract; `tasks.md` 9 (the claims-table rows, unwrappable); `.openspec.yaml` and `proposal.md` 0.
- [x] 3.3 Verify per the "Docs refresh on change" convention whether `AGENTS.md`, `README.md`, or `docs/adr/` need a
      refresh: neither capabilities' Purpose prose is restated in them, and the only `docs/ideas.md` Purpose entry is
      the unrelated parked `gateway/rest-api` refresh (row 7). **Done** — no docs edit owed; the three greps in row 7
      found no live restatement.

## 4. Review gate

- [x] 4.1 The `review-quick` loop ran over the proposal and then the implementation, repeating until it reports no new
      observations, with each round's row appended to `docs/review-rounds.tsv` carrying the identifier the invocation
      returned. **Done** — the ledger's last row for this unit records no classes, its closing condition; the ledger
      owns the round state (one session, `ses_e8efbeff9ffenenOXA0hQqniNp`, `created` then `continued`), and both ledger
      checks pass after every append.

- [x] 4.2 The deep-review routing decision is recorded on this task, derived with
      `python3 scripts/review-routing.py --change refresh-stale-spec-purposes`. **Derived: `RECOMMENDED` on
      `[repeated-class]`** — ledger rows for this unit repeat prior classes; no path, declaration, or requirement-block
      signal matched. The tool's own contract reads that signal as evidence the quick loop is **not converging**
      (`scripts/review-routing.py:9-10,88-100`), not as the loop's cost — and the advisor consultation bore it out by
      finding a residual over-scoping in the infra Purpose text after round 5 had declared the loop clean. **Resolved by
      the owner: no deep review** — the owner resolved not to run one because the package is limited to the two verified
      Purpose rewrites, the capture remains change-record-only, and the scope contradictions are resolved; no
      deep-review session is created. A consultation returns to the deep review if a new material semantic defect
      appears or the package widens past those bounds.

### Review handover (written before waiting on round 1)

- **Unit:** `refresh-stale-spec-purposes`; base `origin/main` at `bd9c427b`; working tree the primary checkout; branch
  `refresh-stale-spec-purposes` (untracked change dir, no commits yet).
- **Supplied artifacts:** this change's `proposal.md` and `tasks.md`, the two target specs at `HEAD`
  (`openspec/specs/showcase/clients/query-client/spec.md`,
  `openspec/specs/showcase/quality/infra-image-versions/spec.md`), and the merged report `docs/audits/2026-10-24.md`
  (spec-corpus findings 1 and 2).
- **Scope:** two `## Purpose` paragraph rewrites only; no requirement, scenario, code, build, or other doc change; the
  spec edits land in the archive commit because a `## Purpose` cannot ride a delta.
- **Prior findings:** none — this is round 1.
- **Completed checks:** `openspec validate --all` → 27 passed, 0 failed; `spotlessApply`/`spotlessCheck` clean.
- **Known limitations:** the implementation diff is the change dir alone by design, so the implementation round reviews
  prose and the two planned edits rather than a spec diff.
- **Packs:** none — no JVM, test, Gradle-build, or deployment decision is made; a subject found in the artifacts that a
  pack does cover is reported as a missing input.

## 5. Capture

- [x] 5.1 The `lesson-capture` subagent ran over the diff and the review findings, its durable proposals were applied or
      its nothing-durable verdict recorded, and the applied net `AGENTS.md` delta is stated on this task. **Done** — it
      ran (session `ses_e8eedc728ffeFhADVPf8g36Yqm`): **1 proposal** — a deferred prose rewrite's task states the
      finished text verbatim, proposed for the `## Purpose` bullet of "Workflow: Author a spec delta and archive" and
      **not applied** — 0 retirements, 0 authoring-system recommendations; its `already covered` verdicts named the
      review-gate claims-table rules for the other four classes. **The proposal is routed to this change record instead
      of `AGENTS.md`:** the gate's threshold is two independent occurrences or one severe verified incident, while the
      evidence is one dependent lineage inside this unit (rounds 1-2 plus the advisor's residual over-scoping) and the
      archived `address-new-snyk-findings` task 3.2 merely describes a rewrite without losing content; the severity
      claim is further undercut because the residual over-scoping arose in a **verbatim** quoted text, so quoting does
      not prevent that class. Applied net `AGENTS.md` delta: **0 lines** — the working tree carries no `AGENTS.md`
      change. The proposal's two facts are kept here: a described rewrite is checked clause by clause, which is how
      rounds 1-2 lost one retained clause at a time.

## 6. Manual review and archive

- [x] 6.1 The owner's manual review pass was requested after the loop was clean and the capture ran. **Done** —
      requested with the unit's report after round 12 (clean) and task 5.1's capture record; the working tree is left
      uncommitted for the pass.
- [x] 6.2 After the owner's approval: push the branch, open the pull request, confirm `build` is green, then apply the
      two Purpose edits, run `openspec archive refresh-stale-spec-purposes --yes`, stage the change-dir move and the
      main spec edits together, and merge once approved. **Done** — pushed, PR #679 opened, `build` green, the two
      Purpose texts applied as quoted, the change archived as
      `2026-10-25-refresh-stale-spec-purposes` in the archive commit, and the PR merged (squash) with the branch
      deleted.

## Workflow follow-up

- Nothing committed, pushed, or archived before the owner approves the implementation.
- The implementation commit carries the change dir alone by design: a `## Purpose` cannot ride a delta, so the spec
  edits land in the archive commit — the change is expected to read as nearly empty in its first push.
