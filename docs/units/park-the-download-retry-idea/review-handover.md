# Review hand-over — park-the-download-retry-idea

## Round 1 — implementation screen (the single pass this unit takes)

- **Unit, tree, branch, base** — `park-the-download-retry-idea`; tree `/Users/afanasyev/Development/axon-showcase`;
  branch of the same name; base `origin/main` `96190e7f`; nothing committed; the unit's files staged.
- **Packs** — `none`: one `docs/ideas.md` entry plus this unit's records.
- **Supplied artifacts** — the diff (`docs/ideas.md`'s new entry under `## 2026-11-08`) and this unit's records
  (`unit.md`, `process-defects.md`), the drafted PR body with its claims table and dismissals, and the context the entry
  rests on: `.github/workflows/ci.yml`'s two download steps and PR #867's run (`run_attempt: 10`).
- **Scope** — the entry's form against `docs/ideas.md`'s header (day-section rule, trailing status tag, an origin
  marker), its claims against the workflow and the run's attempt history, and the unit record's stated route.
- **Prior findings with their instances** — none; no quick-review round has run for this unit.
- **Completed checks** — `spotlessApply`; `verifyIdeasSections`; `--unit-layout`; `--review-ready`.
- **Known limitations** — the outage itself is external and not reproducible on demand; the entry states what the run
  history and the workflow show.

## Round 2 — implementation screen, after round 1's findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `96190e7f`; the unit's files staged.
- **Packs** — `none`.
- **Supplied artifacts** — the diff (`docs/ideas.md`'s entry), `unit.md`, `process-defects.md`, `rounds.tsv`,
  `findings/round-1.md`, the drafted PR body with its claims table and dismissals, and the context:
  `.github/workflows/ci.yml` and PR #867's run history.
- **Scope** — the entry and the record against the workflow, the run's attempts, and the status page's incidents.
- **What changed since round 1** — the pin/checksum asymmetry is stated (`gitleaks` pinned and verified, `actionlint` on
  the installer's `latest`), so the pinning step is scoped to `actionlint` first and "keep the pin" to `gitleaks`; the
  attempt count is ten (nine re-runs) everywhere; the 92-byte non-gzip body is attributed to the actionlint install and
  the `504` to the caller's `curl`; the window is stated as 08:53–09:23Z; the status-page clause is scoped to that
  window with the later incident named as corroboration; the claims table gained rows for the caller's `curl` and the
  timestamps; the Docker-free `check` ran and its tick is now supported.
- **Prior findings with their instances** — round 1 at `findings/round-1.md`; compare each and report an unfixed
  instance or a new instance of a raised class as such.
- **Completed checks** — `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` BUILD SUCCESSFUL; `spotlessApply`;
  `verifyIdeasSections`; `--unit-layout`; `--finding-instances`; `--review-ready`.

## Round 3 — implementation screen, after round 2's findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `96190e7f`; the unit's files staged.
- **Packs** — `none`.
- **Supplied artifacts** — the diff (`docs/ideas.md`'s entry), `unit.md`, `process-defects.md`, `rounds.tsv`,
  `findings/round-1.md` and `round-2.md`, the drafted PR body with its claims table and dismissals, and the context:
  `.github/workflows/ci.yml` and PR #867's run history.
- **Scope** — the entry and the record against the workflow, the run's attempts, and the status page's incidents.
- **What changed since round 2** — "failed in alternation" is replaced in all three sites by the actual distribution
  (six attempts on the actionlint install, three on the gitleaks scan, between 08:53Z and 09:23Z), and the unit record's
  summary sentence now states the same window and scopes the status reading to it.
- **Prior findings with their instances** — rounds 1–2 at `findings/`; compare each and report an unfixed instance or a
  new instance of a raised class as such.
- **Completed checks** — `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` BUILD SUCCESSFUL; `spotlessApply`;
  `verifyIdeasSections`; `--unit-layout`; `--finding-instances`; `--review-ready`.
- **Known limitations** — the outage is external; the gitleaks step's log records no HTTP status, so the `504` rests on
  the caller's `curl`.

## Round 4 — implementation screen, after round 3's finding

- **Unit, tree, branch, base** — unchanged; base `origin/main` `96190e7f`; the unit's files staged.
- **Packs** — `none`.
- **Supplied artifacts** — the diff (`docs/ideas.md`'s entry), `unit.md`, `process-defects.md`, `rounds.tsv`,
  `findings/round-1.md`–`round-3.md`, the drafted PR body with its claims table and dismissals, and the context:
  `.github/workflows/ci.yml` and PR #867's run history.
- **Scope** — the entry and the record against the workflow, the run's attempts, and the status page.
- **What changed since round 3** — the claims table gained the six/three distribution row (sourced to the attempts'
  `jobs` endpoint), and the table was re-derived by walking the entry sentence by sentence rather than seeding it from
  review rounds: every factual sentence now has a row, and the remaining sentence states the proposed change.
- **Prior findings with their instances** — rounds 1–3 at `findings/`; compare each and report an unfixed instance or a
  new instance of a raised class as such.
- **Completed checks** — `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` BUILD SUCCESSFUL; `spotlessApply`;
  `verifyIdeasSections`; `--unit-layout`; `--finding-instances`; `--review-ready`.
- **Known limitations** — the outage is external; the gitleaks step's log records no HTTP status, so the `504` rests on
  the caller's `curl`.
