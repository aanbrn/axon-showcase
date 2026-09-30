# Design

## Context

See `proposal.md` — Why. The state that constrains the approach, verified against the repository:

- **The layout is already consistent with the rule; only the statement is missing.** The repository has **13 tracked
  executables in three locations**, and the rule is derived from that whole set rather than from one of them:
  - 8 executable under `scripts/`, plus 2 Python helpers that are **not** executable (`commit-hygiene.py`,
    `test-commit-hygiene.py` — run via `python3`, so mode `100644`): the repository-wide tooling is the executables
    `doctor.sh`, `ensure-idea-settings.py`, `experience-analysis.sh`, `git-hooks/pre-commit`, `install-git-hooks.sh`,
    `load-test-baseline.sh`, `setup-idea.sh`, and `test-doctor.sh`.
  - 2 module-local — `showcase-web-ui/scripts/outdated-report.sh` (the `npmOutdated` report) and
    `showcase-web-ui/start.sh` (the web UI image's nginx entry point): a module's own tooling kept beside that module.
  - 3 at the repository root — `gradlew` (generated) and `db.sh` / `setup-hosts.sh` (human-facing setup entry points the
    README documents). `.opencode/` holds no executable: `agent/`, `commands/`, `skills/`, and config, with no
    `.opencode/plugin/` (the directory a retired plugin used to occupy).
- **Three script+trigger pairs exist** and the rest of the scripts have no trigger: `/check-tooling`→`doctor.sh`,
  `/setup-idea`→`setup-idea.sh`, `/retrospective`→`experience-analysis.sh`. `install-git-hooks.sh` and
  `load-test-baseline.sh` are human-run; `commit-hygiene.py` has two callers — the tracked `git-hooks/pre-commit` and
  the Gradle `verify*` tasks that invoke it directly (`build.gradle.kts`).
- **`experience-analysis.sh`'s header already carries the answer** ("It lives under `scripts/` because that is the
  repo's home for dev tooling whatever the caller … while `.opencode/` has no root-level script location") — a
  per-script statement of a general rule, which this change relocates.
- **The `/opsx-*` commands and `openspec-*` skills are generated** (by `openspec update`) and the `axon4to5-*` skills
  are vendored; the rule must not claim authorship of those, matching the provenance partition the `agents-auditor`
  uses.

## Goals / Non-Goals

**Goals:**

- One stated rule for where an implementation lives vs where an agent trigger lives, in the file a contributor and an
  agent both read.
- Every existing instance routed to it, so the split is auditable rather than inferred.
- The per-script restatement removed, so the fact lives once.

**Non-Goals:**

- **Moving files.** The rule describes the layout the repository has; relocating anything would be a separate change
  with its own risk (URLs, hook paths, `helmInstall` deps, docs).
- **Constraining generated or vendored content.** The `opsx-*` commands and `openspec-*`/`axon4to5-*` skills are not the
  repository's to place.
- A new spec requirement — a placement convention changes no observable behavior, so `skip_specs` applies.

## Decisions

### The boundary, derived from the executable inventory rather than from one location

The rule states where each kind of tooling lives, taken from the full 13-file inventory: `scripts/` for repository-wide
tooling, a module's own directory for its tooling (`showcase-web-ui/scripts/`, `showcase-web-ui/start.sh`), the
repository root for the handful of setup entry points and the generated wrapper, and `.opencode/` for the runtime
surface (no executables). Rejected: **split by caller** (agent-invoked under `.opencode/`, human-run under `scripts/`) —
it would move `doctor.sh` and `experience-analysis.sh` and their tests, diverge from where the repository already put
them, and make a tool's home depend on who runs it rather than on what it is. Rejected: **rule only the pairing** (a
`scripts/` tool with a trigger gets a command entry) — it states the easy half and leaves the actual question open.
Rejected: **"every executable lives in `scripts/`"** — twice falsified in review, first by the root executables and then
by the module-local pair. The lesson recorded here: a descriptive claim about placement must be derived from the
complete set (`git ls-files -s | awk '$1=="100755"'`), not from the location under discussion — the first draft patched
the root case and still misdescribed `showcase-web-ui/`'s two scripts, which is the same defect one level down.

### State it in `AGENTS.md`, extending existing guidance rather than adding a bullet

`AGENTS.md` already describes the tooling-setup skill+command and the `scripts/` tools in several places, and the
capture rule prefers a merge or an extension to an accretion. The rule belongs near that guidance (and near the
Prerequisites bullet the doctor was placed under), not as a new standalone bullet.

### Relocate the header rationale rather than duplicating it

`experience-analysis.sh`'s header explains placement; once the rule is stated, that explanation is a restatement of a
general rule at a specific site — the duplication the docs-refresh convention warns against. The header keeps what the
script does and drops the placement clause.

## Risks / Trade-offs

- **A prose rule is unenforced.** Nothing gates it, so it can drift like any convention. Mitigation: keep it short and
  place it where the adjacent tooling guidance already is, so it is read when a contributor adds a tool; the
  `agents-auditor` already audits `AGENTS.md` for drift, and its route-candidate analysis would surface the rule if a
  deterministic mechanism ever subsumes it.
- **The rule could over-claim.** Stated too broadly ("all tooling lives in `scripts/`") it would misdescribe
  `.opencode/`'s agent/command/skill content. Mitigation: state both sides of the boundary explicitly, and name the
  generated/vendored exception.
- **Relocating the header clause could drop a fact.** The clause carries a sub-fact ("`.opencode/` has no root-level
  script location"), which the rule must keep or the reader loses why the tool is not under `.opencode/`. Mitigation:
  the rule states that `.opencode/` holds no script location, and the change verifies the header's remaining text still
  reads coherently.
