# Process defects — scope-chart-repository-fetch

### scope-chart-repository-fetch#1 — A dependency's limitation was asserted without its docs or source

- **Recorded** — 2026-10-27, by the writing session (`ses_e832e4043ffe6ADYTEa13DvV1s`), during the planning stage.
- **Stage** — proposal (the plan presented to the owner before the unit started).
- **Expected** — a claim about a third-party plugin's capability is established from the plugin's own documentation or
  source before it drives a design, as `AGENTS.md` requires for a dependency's mechanism.
- **Observed** — the plan stated that the plugin's `HelmUpdateRepositories` offered no way to configure the timeout and
  proposed a repo-authored fetch task. The plugin documents `extraArgs` as the escape hatch for options it lacks, and a
  probe produced `helm repo update ingress-nginx --timeout 15m`.
- **Evidence** — the plugin documentation, Global Helm Options: `extraArgs` — "Additional arguments to pass to _every_
  invocation of helm. This can be used for command-line options that have no counterpart in the plugin." The probe's
  executed command line: `[helm, repo, update, ingress-nginx, --timeout, 15m]`.
- **Consequence** — one consultation round and a revised plan; no artifact shipped and no code was written on the wrong
  premise. The corrected scope was implemented.
- **Knowledge boundary** — observed for this unit's planning. Not checked whether other units' plans make similar
  third-party capability claims without reading the source.

- **Decision** — no-change: the third-party capability rule already requires a plugin's own documentation or source to
  establish a capability claim before it drives a design.

### scope-chart-repository-fetch#2 — A CLI flag was asserted from a call site, not the pinned tool

- **Recorded** — 2026-10-27, by the writing session (`ses_e832e4043ffe6ADYTEa13DvV1s`), during the review stage.
- **Stage** — implementation (the claims table).
- **Expected** — a claim about a tool's CLI contract is derived from the pinned tool itself, as this unit did for the
  `--timeout` default and the positional repository name.
- **Observed** — the row said "`helm repo add` downloads the index unless `--no-update`", taken from the plugin's
  `HelmAddRepository.kt`. The pinned Helm 4.3.1 rejects `--no-update` (`Error: unknown flag: --no-update`), so the
  "unless" half does not reproduce; the default-download half does.
- **Evidence** — `helm repo add --help` on the pinned client lists `--force-update` and `--timeout`, no `--no-update`;
  passing the flag returns `Error: unknown flag: --no-update`. A probe add wrote `bitnami-index.yaml` (27,557,342
  bytes).
- **Consequence** — the round-4 review caught the row; the row was narrowed to the reproducible half, and the plugin's
  `failIfExists` incompatibility with Helm 4 (it emits the rejected flag) was recorded beside it. The build does not set
  that option, so no shipped behavior was affected.
- **Knowledge boundary** — observed for this unit's claims table. Not checked whether other units derive CLI claims from
  call sites rather than the pinned tool.

- **Decision** — no-change: the CLI-contract rule and review-gate §1 already require a tool's behaviour to be derived
  from the pinned tool, not from a call site.

### scope-chart-repository-fetch#3 — A round's ledger row was appended a round late

- **Recorded** — 2026-10-27, by the writing session (`ses_e832e4043ffe6ADYTEa13DvV1s`), during the review stage.
- **Stage** — review.
- **Expected** — each round's row is appended when its report arrives, before the next reviewer invocation.
- **Observed** — round 7's report arrived, its finding was fixed and round 8 was invoked before the row was appended;
  the ledger carried rounds 1–6 and 8.
- **Evidence** — `python3 scripts/commit-hygiene.py --unique-rounds --expect scope-chart-repository-fetch:8` printed
  "Unit 'scope-chart-repository-fetch' is missing round 7: its rounds must be contiguous from 1"; the row's content is
  the round-7 report's record line (`7  contradiction  -  1`).
- **Consequence** — the row was reconstructed from the report and inserted in order; the pre-commit checks run before
  the unit ships, so the gap would have blocked the commit rather than reaching the repository.
- **Knowledge boundary** — observed for this unit. Not checked whether earlier units appended rows late; the
  session-continuity check validates the appended rows' fields, not the timing of the append.

- **Decision** — no-change: already parked — the `docs/ideas.md` 2026-10-22 "reviewer wrapper" entry, whose
  `Revisit when:` condition is not met.

### scope-chart-repository-fetch#4 — The record was staged before its last edit, twice

- **Recorded** — 2026-10-27, by the writing session (`ses_e832e4043ffe6ADYTEa13DvV1s`), during the review stage.
- **Stage** — review (the staging step before the manual pass and the commit).
- **Expected** — the unit's new file is staged after its last edit, and the staged-set guard runs before the commit.
- **Observed** — record `#2` was staged before it was written and re-staged in round 6; the `#3` append left the indexed
  copy at the `#1`–`#2` version.
- **Evidence** — `python3 scripts/commit-hygiene.py --staged` exited 1 with "Staged and then edited again, so the index
  is stale: docs/process-defects/scope-chart-repository-fetch.md"; `git status` showed `AM` before each refresh.
- **Consequence** — the record was re-staged after each occurrence, and the guard would have refused the commit; no
  artifact shipped a stale copy.
- **Knowledge boundary** — observed for this unit. Not checked whether other units stage a new file before its last
  edit.

- **Decision** — no-change: the staging-snapshot rule and the pre-commit guard already require a new file to be
  re-staged after its last edit.
