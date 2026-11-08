# Review hand-over — select-the-installed-idea-config

## Round 1 — proposal screen

- **Unit, tree, branch, base** — `select-the-installed-idea-config`; tree `/Users/afanasyev/Development/axon-showcase`;
  branch of the same name; base `origin/main` `0727fb8b`; nothing committed; the unit's files staged.
- **Packs** — `none`: a Python tooling script, its suite, an OpenSpec delta, and records. No JVM production or test
  code, no Gradle build decision, no deployment surface.
- **Supplied artifacts** — `openspec/changes/select-the-installed-idea-config/{proposal.md,design.md,tasks.md}` and its
  delta at `specs/showcase/quality/ide-config/spec.md`; the current `scripts/ensure-idea-settings.py` and
  `scripts/test-ensure-idea-settings.py`; and `openspec/specs/showcase/quality/ide-config/spec.md`.
- **Scope** — the proposal's coherence: the parked idea it implements, the design decisions, the delta's scenario
  preservation, and the tasks' coverage.
- **Prior findings with their instances** — none; no quick-review round has run for this unit.
- **Completed checks** — `openspec validate --all` 27/0 (the change itself valid); `spotlessApply`; `--review-ready`.
- **Known limitations** — the version resolution is specified for macOS only; the change ships no live macOS-upgrade
  verification beyond the suite's fixtures.

## Round 2 — proposal screen, after round 1's findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `0727fb8b`; the unit's files staged.
- **Packs** — `none`.
- **Supplied artifacts** — the change's `proposal.md`, `design.md`, `tasks.md`, and its delta; the current
  `scripts/ensure-idea-settings.py` and its suite; the main `ide-config` spec; and the parked idea entry.
- **Scope** — the proposal's coherence, the design decisions, the delta's scenario preservation, and the tasks'
  coverage.
- **What changed since round 1** — the bundle scan takes an injectable root parameter and skips an unreadable plist; the
  product prefix is read alongside the version, the same-version tie is refused, and a version with no directory falls
  back and is reported; the context's count is eight directories and seven siblings with the actual names; the claims
  table gained the duplication-trigger row (source: the parked entry and this host's SDK tables); task 1.4 lists each
  case with its assertion, task 1.5 names the delta path, and the overloaded passages are split.
- **Prior findings with their instances** — round 1 at `findings/round-1.md`; compare each and report an unfixed
  instance or a new instance of a raised class as such.
- **Completed checks** — `openspec validate --all` 27/0; `spotlessApply`; `--finding-instances`; `--review-ready`.
- **Known limitations** — the resolution is macOS-only; the change's live verification is its suite's fixtures.

## Round 3 — proposal screen, after round 2's findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `0727fb8b`; the unit's files staged.
- **Packs** — `none`.
- **Supplied artifacts** — the change's `proposal.md`, `design.md`, `tasks.md`, and its delta; the current
  `scripts/ensure-idea-settings.py` and its suite; the main `ide-config` spec; and the parked idea entry.
- **Scope** — the proposal's coherence, the design's decision rules, the delta's scenario preservation, and a
  clause→control trace over every delta clause.
- **What changed since round 2** — D2 and D3 are decision tables with one condition→action row each; a skipped pair is
  always reported, including in the refused outcomes; task 1.4 gained the absent-configuration, later-pair, and
  skip-then-refusal cases, and a unique-match-only case; the artifact-wide pass was run as a clause→control trace over
  the delta, which is what named the three missing cases.
- **Prior findings with their instances** — rounds 1–2 at `findings/`; compare each and report an unfixed instance or a
  new instance of a raised class as such.
- **Completed checks** — `openspec validate --all` 27/0; `spotlessApply`; `--finding-instances`; `--review-ready`.
- **Known limitations** — the resolution is macOS-only; the change's live verification is its suite's fixtures.

## `advisor-proposal-1` — proposal readiness, before the owner's approval

- **Unit, tree, branch, base** — `select-the-installed-idea-config`; tree `/Users/afanasyev/Development/axon-showcase`;
  branch of the same name; base `origin/main` `0727fb8b`; nothing committed; the unit's files staged.
- **Owner's authorization** — the instruction of 2026-11-08: "Отревьювь с советником до согласия."
- **Question and proposed next action** — is this proposal ready for the owner's approval, or does anything need to
  change first? The next action proposed is the owner's approval, then implementation of tasks 1.1–1.6.
- **Stage** — a change unit at the proposal boundary: the planning artifacts are written and the quick loop closed clean
  at round 3; nothing is implemented.
- **Artifacts** — `openspec/changes/select-the-installed-idea-config/{proposal.md,design.md,tasks.md}` and its delta at
  `specs/showcase/quality/ide-config/spec.md`; the current `scripts/ensure-idea-settings.py` and
  `scripts/test-ensure-idea-settings.py`; the main `showcase/quality/ide-config` spec; and the parked idea entry in
  `docs/ideas.md` (2026-11-08).
- **Prior findings and dispositions** — rounds 1–3 at `findings/`; round 3 clean; eleven findings fixed.
- **Completed checks** — `openspec validate --all` 27/0; `spotlessApply`; `--expect-clean`; `--finding-instances`;
  `--session-continuity`; `--review-ready`.
- **Known limitations** — the resolution is macOS-only by design; live verification of the upgrade state is the suite's
  fixtures, since the host's own duplicate registration sits in stale directories.
- **Packs** — `none`: a Python tooling script, its suite, an OpenSpec delta, and records.
- **Report language** — English; the caller presents to the owner in Russian.

**Outcome** — revise the proposal before seeking approval, on three confirmed problems: (1) the missing-SDK fallback
would write an SDK _name_ from a configuration the installed IDE does not read, so the stage must stop and name the
registration owed there; (2) the new selector could disagree with an explicitly chosen launcher, since `setup-idea.sh`
resolves the launcher after the Python stage — one target identity must carry through, honouring the launcher unless
`--idea-config-dir` overrides; (3) the plan did not force both `sys.platform` branches on a Linux runner. Its
hypothesis: a bundle's basename and numeric version identify the intended configuration only for the default layouts, so
the claim must be evidenced or bounded. All three are corrected and the hypothesis is bounded in the design.

## Round 4 — proposal screen, after the advisor's consultation

- **Unit, tree, branch, base** — unchanged; base `origin/main` `0727fb8b`; the unit's files staged.
- **Packs** — `none`.
- **Supplied artifacts** — the revised `proposal.md`, `design.md`, `tasks.md`, and delta; the current
  `scripts/ensure-idea-settings.py`, `scripts/setup-idea.sh`, and their suites; the main `ide-config` spec; the parked
  idea entry; and the archived `2026-10-30-reproduce-python-ide-setup/design.md` D3 the design now engages.
- **Scope** — the proposal's coherence, the design's rules, the delta's clauses and scenarios, and the tasks' controls.
- **What changed since round 3** — the missing-SDK fallback is replaced by a stop naming the registration owed in the
  identified configuration; the setup resolves its launcher before the Python stage and passes the bundle it targets;
  the precedence table gained the launcher row and a stop row; the platform is a parameter so both branches run on any
  runner; the design engages the archived D3 and bounds automatic selection to the default bundle layouts, naming custom
  `idea.config.path` as out of scope.
- **Prior findings with their instances** — rounds 1–3 at `findings/`; compare each and report an unfixed instance or a
  new instance of a raised class as such.
- **Completed checks** — `openspec validate --all` 27/0; `spotlessApply`; `--agent-sessions`; `--review-ready`.
- **Known limitations** — the resolution is macOS-only; the mapping claim is bounded to the default bundle layouts.

## Round 5 — proposal screen, after round 4's findings were swept artifact-wide

- **Unit, tree, branch, base** — unchanged; base `origin/main` `0727fb8b`; the unit's files staged.
- **Packs** — `none`.
- **Supplied artifacts** — the revised `proposal.md`, `design.md`, `tasks.md`, and delta;
  `scripts/ensure-idea-settings.py`, `scripts/setup-idea.sh`, and their suites; the main `ide-config` spec; the parked
  idea entry; and the archived `2026-10-30-reproduce-python-ide-setup/design.md` D3.
- **Scope** — the proposal's coherence, the delta's clauses against its scenarios, the claims table, the risks, and the
  tasks' controls and marks.
- **What changed since round 4** — the artifact-wide sweep the reviewer named as the root cause: the refusal scenario is
  qualified to "no bundle pair applies", the delta gained the launcher-precedence and absent-configuration scenarios,
  the claims table gained the launcher-resolution and `jdkName` rows, the stale-SDK risk now describes the stop, the
  task controls assert the "no installed bundle resolved" line for the unique and no-match orders and the absent stop,
  and task 3.1 is unchecked until this loop closes.
- **Prior findings with their instances** — rounds 1–4 at `findings/`; compare each and report an unfixed instance or a
  new instance of a raised class as such.
- **Completed checks** — `openspec validate --all` 27/0; `spotlessApply`; `--agent-sessions`; `--review-ready`.
- **Known limitations** — the resolution is macOS-only; the mapping claim is bounded to the default bundle layouts.

## Round 6 — proposal screen, after round 5's findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `0727fb8b`; the unit's files staged.
- **Packs** — `none`.
- **Supplied artifacts** — the revised `proposal.md`, `design.md`, `tasks.md`, and delta;
  `scripts/ensure-idea-settings.py`, `scripts/setup-idea.sh`, `scripts/test-setup-idea.sh`, and the Python suite; the
  main `ide-config` spec; and the parked idea entry.
- **Scope** — the stop's full variant set, the wrapper's own suite, the claims table, the risks, and the tasks'
  controls.
- **What changed since round 5** — the stop's two reasons each gained the no-other-configuration variant in the cases
  and the delta's WHENs; the wrapper's suite (`scripts/test-setup-idea.sh`) has its own task for the launcher-first
  order and the same IDE target; the stale-SDK risk now says the setup adds no unresolvable reference, leaving an
  earlier module as it is.
- **Prior findings with their instances** — rounds 1–5 at `findings/`; compare each and report an unfixed instance or a
  new instance of a raised class as such.
- **Completed checks** — `openspec validate --all` 27/0; `spotlessApply`; `--finding-instances`; `--review-ready`.
- **Known limitations** — the resolution is macOS-only; the mapping claim is bounded to the default bundle layouts.

## Round 7 — proposal screen, after round 6's findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `0727fb8b`; the unit's files staged.
- **Packs** — `none`.
- **Supplied artifacts** — the revised `proposal.md`, `design.md`, `tasks.md`, and delta;
  `scripts/ensure-idea-settings.py`, `scripts/setup-idea.sh`, `scripts/test-setup-idea.sh`, and the Python suite; the
  main `ide-config` spec; and the parked idea entry.
- **Scope** — the proposal's own summaries against the artifacts it ships, the Python suite's launcher case, and the
  artifact-wide sweep over every summary.
- **What changed since round 6** — the Python suite's launcher case now asserts only the selected configuration and
  bundle; the What Changes test bullet names `scripts/test-setup-idea.sh`; the Modified Capabilities summary lists the
  launcher, absent-configuration, and no-other-matching scenarios; every summary in the proposal was re-read against the
  delta and tasks, not only the two named.
- **Prior findings with their instances** — rounds 1–6 at `findings/`; compare each and report an unfixed instance or a
  new instance of a raised class as such.
- **Completed checks** — `openspec validate --all` 27/0; `spotlessApply`; `--finding-instances`; `--review-ready`.
- **Known limitations** — the resolution is macOS-only; the mapping claim is bounded to the default bundle layouts.

## `advisor-proposal-2` — the same decision, follow-up invocation

- **What it asks** — whether the three confirmed problems and the hypothesis are corrected, and whether the proposal may
  go to the owner's manual pass.
- **What changed** — the missing-SDK fallback is a stop naming the registration owed in the identified configuration (no
  fallback, no `CONFIGURED` on an unresolvable name), in both the design and the delta; the launcher the setup resolves
  is read before the Python stage and carried as one target, with the wrapper's own suite controlling it; the platform
  is a parameter so both branches run on a Linux runner; the mapping claim is bounded to the default bundle layouts,
  with custom `idea.config.path` left to `--idea-config-dir`, and the archived D3 is engaged rather than reversed. Seven
  quick rounds closed clean at round 7.
- **Packs** — `none`.
- **Report language** — English; the caller presents to the owner in Russian.

## Round 8 — proposal screen, after the advisor's boundary correction

- **Unit, tree, branch, base** — unchanged; base `origin/main` `0727fb8b`; the unit's files staged.
- **Packs** — `none`.
- **Supplied artifacts** — the revised `proposal.md`, `design.md`, `tasks.md`, and delta; the scripts and suites; the
  main `ide-config` spec; the parked idea; and the archived `2026-10-30-reproduce-python-ide-setup/design.md` D3.
- **Scope** — the boundary statement against the pair mechanism, the delta's clauses against its scenarios, the claims
  table, and the tasks' controls.
- **What changed since round 7** — the custom-`idea.config.path` boundary no longer claims a detection the mechanism
  cannot perform: the design now says automatic selection **assumes the default configuration location**, cannot detect
  a custom one, and that such a contributor supplies `--idea-config-dir`; the Context's archived-decision sentence says
  the same.
- **Prior findings with their instances** — rounds 1–7 at `findings/`; compare each and report an unfixed instance or a
  new instance of a raised class as such.
- **Completed checks** — `openspec validate --all` 27/0; `spotlessApply`; `--review-ready`.
- **Known limitations** — the resolution is macOS-only; the mapping claim is bounded to the default bundle layouts.

## Round 9 — proposal screen, after round 8's finding

- **Unit, tree, branch, base** — unchanged; base `origin/main` `0727fb8b`; the unit's files staged.
- **Packs** — `none`.
- **Supplied artifacts** — `proposal.md`, `design.md`, `tasks.md`, and the delta; the scripts and suites; the main
  `ide-config` spec; the parked idea; and the archived `2026-10-30-reproduce-python-ide-setup/design.md` D3.
- **Scope** — the boundary through the mechanism: the D2 rows and the stop messages, the delta's requirement and
  scenarios, the proposal's Why and boundary bullet, the claims table, and the controls.
- **What changed since round 8** — the boundary is carried through the mechanism: D2's rows and D5's report name the
  default configuration location and `--idea-config-dir` for a custom path; the delta's requirement states the default
  location and that a custom `idea.config.path` is not detected, and its absent/lacks scenarios name the location, the
  flag, and the owed registration; the proposal's Why and boundary bullet are qualified; the controls name the flag.
- **Prior findings with their instances** — rounds 1–8 at `findings/`; compare each and report an unfixed instance or a
  new instance of a raised class as such.
- **Completed checks** — `openspec validate --all` 27/0; `spotlessApply`; `--review-ready`.
- **Known limitations** — the resolution is macOS-only; a custom configuration path is not detected by design.

## Round 10 — proposal screen, after round 9's findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `0727fb8b`; the unit's files staged.
- **Packs** — `none`.
- **Supplied artifacts** — `proposal.md`, `design.md`, `tasks.md`, and the delta; the scripts and suites; the main
  `ide-config` spec; the parked idea; and the archived D3.
- **Scope** — the tasks' clauses and controls against the design and the delta, the delta's scenarios, the claims table,
  and the marks.
- **What changed since round 9** — tasks 1.4, 1.5, and 1.6 carry the default-location wording and `--idea-config-dir`
  for a custom path, including the corrected absent-configuration message; task 3.1 is unchecked; the whole task list
  was re-read against the design and delta rather than patched at the named clauses.
- **Prior findings with their instances** — rounds 1–9 at `findings/`; compare each and report an unfixed instance or a
  new instance of a raised class as such.
- **Completed checks** — `openspec validate --all` 27/0; `spotlessApply`; `--review-ready`.
- **Known limitations** — the resolution is macOS-only; a custom configuration path is not detected by design.

**Outcome (`advisor-proposal-2`)** — one confirmed problem: the custom-configuration-path boundary claimed a detection
the pair mechanism cannot perform. Corrected by carrying the bound through the mechanism — the D2 rows, D5's report, the
delta's requirement and its absent/lacks scenarios, the proposal's Why and boundary bullet, and the tasks' clauses — so
automatic selection assumes the default configuration location, names `--idea-config-dir` for a custom path, and says so
when it stops. The advisor's condition for supporting the manual pass is met.

## Round 11 — implementation screen

- **Unit, tree, branch, base** — unchanged; base `origin/main` `0727fb8b`; the unit's files staged, nothing committed.
- **Packs** — `none`: the Python tooling script and its shell wrapper, their suites, the delta, the docs, and records.
- **Supplied artifacts** — the implemented diff: `scripts/ensure-idea-settings.py` (bundle resolution, the target
  configuration, the stops, the platform parameter, `--idea-bundle`), `scripts/setup-idea.sh` (launcher resolved first),
  `scripts/test-ensure-idea-settings.py` (47 tests, 14 new cases), `scripts/test-setup-idea.sh` (the wrapper's two new
  cases), `README.md`, `.opencode/skills/setup-idea/SKILL.md`, `docs/ideas.md` (the implemented entry removed), and the
  change's `tasks.md` ticks.
- **Scope** — the implementation against the approved design and delta, the case coverage of every clause, and the docs.
- **Prior findings with their instances** — rounds 1–10; round 10 was clean. Compare each and report an unfixed instance
  or a new instance of a raised class as such.
- **Completed checks** — `python3 scripts/test-ensure-idea-settings.py` 47 OK; `sh scripts/test-setup-idea.sh` all
  cases; `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` BUILD SUCCESSFUL; `openspec validate --all` 27/0;
  `spotlessApply`; a live run on this host selected `IntelliJIdea2026.3` for the installed bundle and reported
  `CONFIGURED`.
- **Known limitations** — the resolution is macOS-only; a custom configuration path is not detected by design.

## Round 12 — implementation screen, after round 11's findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `0727fb8b`; the unit's files staged.
- **Packs** — `none`.
- **Supplied artifacts** — the implemented diff: `scripts/ensure-idea-settings.py`, `scripts/setup-idea.sh`,
  `scripts/test-ensure-idea-settings.py` (50 tests), `scripts/test-setup-idea.sh`, `README.md`,
  `.opencode/skills/setup-idea/SKILL.md`, `docs/ideas.md`, and the change dir with the amended tie/launcher clauses.
- **Scope** — the wrapper's argument path, the launcher fall-through, the tie's scope, the amended design and delta, and
  the task marks.
- **What changed since round 11** — the wrapper builds no array and calls the helper with `--idea-config-dir`, with
  `--idea-bundle`, or with neither, so bash 3.2's empty-array expansion cannot abort it, and a case covers the
  no-launcher/no-explicit run; a launcher outside a bundle or with an unreadable plist now falls through to the
  installed-bundle scan; the tie is refused only when no launcher resolved, and the design, the delta's tie scenario,
  and a new case say so; the claims table records the base revision; tasks 1.1–1.8, 2.1–2.5, and 3.1 are ticked.
- **Prior findings with their instances** — rounds 1–11 at `findings/`; compare each and report an unfixed instance or a
  new instance of a raised class as such.
- **Completed checks** — `python3 scripts/test-ensure-idea-settings.py` 50 OK; `sh scripts/test-setup-idea.sh` all
  cases; `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` BUILD SUCCESSFUL; `openspec validate --all` 27/0;
  `spotlessApply`.
- **Known limitations** — the resolution is macOS-only; a custom configuration path is not detected by design.

## Round 13 — implementation screen, after round 12's finding

- **Unit, tree, branch, base** — unchanged; base `origin/main` `0727fb8b`; the unit's files staged.
- **Packs** — `none`.
- **Supplied artifacts** — the implemented diff and the change dir.
- **Scope** — the tie's statement across the delta, the proposal's summaries, the design, and the implementation.
- **What changed since round 12** — the delta's tie sentence and the proposal's What Changes/Modified Capabilities now
  say the tie is refused when no launcher resolves one of them, matching the design, the tie scenario, and the two
  cases.
- **Prior findings with their instances** — rounds 1–12 at `findings/`; compare each and report an unfixed instance or a
  new instance of a raised class as such.
- **Completed checks** — `python3 scripts/test-ensure-idea-settings.py` 50 OK; `sh scripts/test-setup-idea.sh` all
  cases; `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` BUILD SUCCESSFUL; `openspec validate --all` 27/0;
  `spotlessApply`.
- **Known limitations** — the resolution is macOS-only; a custom configuration path is not detected by design.

## Round 14 — implementation screen, after round 13's finding

- **Unit, tree, branch, base** — unchanged; base `origin/main` `0727fb8b`; the unit's files staged.
- **Packs** — `none`.
- **Supplied artifacts** — the implemented diff and the change dir.
- **Scope** — the tie's statement in every artifact, the wrapper, the cases, and the marks.
- **What changed since round 13** — the proposal's Modified Capabilities entry carries the launcher qualifier, so every
  statement of the tie (design, delta sentence, delta scenario, proposal's two summaries, implementation, cases) agrees.
- **Prior findings with their instances** — rounds 1–13 at `findings/`; compare each and report an unfixed instance or a
  new instance of a raised class as such.
- **Completed checks** — `python3 scripts/test-ensure-idea-settings.py` 50 OK; `sh scripts/test-setup-idea.sh` all
  cases; `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` BUILD SUCCESSFUL; `openspec validate --all` 27/0;
  `spotlessApply`.
- **Known limitations** — the resolution is macOS-only; a custom configuration path is not detected by design.

## Round 15 — implementation screen, after round 14's finding

- **Unit, tree, branch, base** — unchanged; base `origin/main` `0727fb8b`; the unit's files staged.
- **Packs** — `none`.
- **Supplied artifacts** — the implemented diff and the change dir.
- **Scope** — the 120-character rule over every changed non-formatter file, the suites, the docs, and the mark states.
- **What changed since round 14** — the docstring and the tie message string in `scripts/ensure-idea-settings.py` are
  wrapped so no line exceeds 120, and the recipe over the changed files reports none.
- **Prior findings with their instances** — rounds 1–14 at `findings/`; compare each and report an unfixed instance or a
  new instance of a raised class as such.
- **Completed checks** — the 120-character recipe over the changed files reports none;
  `python3 scripts/test-ensure-idea-settings.py` 50 OK; `sh scripts/test-setup-idea.sh` all cases;
  `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` BUILD SUCCESSFUL; `openspec validate --all` 27/0;
  `spotlessApply`.
- **Known limitations** — the resolution is macOS-only; a custom configuration path is not detected by design.

## Round 16 — implementation screen, after round 15's finding

- **Unit, tree, branch, base** — unchanged; base `origin/main` `0727fb8b`; the unit's files staged.
- **Packs** — `none`.
- **Supplied artifacts** — the implemented diff and the change dir.
- **Scope** — the 120 rule against the message's own line breaks, the suites, the docs, and the marks.
- **What changed since round 15** — the tie message is split within its first line, so the rendered message keeps the
  line breaks it had while every source line stays within 120.
- **Prior findings with their instances** — rounds 1–15 at `findings/`; compare each and report an unfixed instance or a
  new instance of a raised class as such.
- **Completed checks** — the 120-character recipe reports none; `python3 scripts/test-ensure-idea-settings.py` 50 OK;
  `sh scripts/test-setup-idea.sh` all cases; `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` BUILD SUCCESSFUL;
  `openspec validate --all` 27/0; `spotlessApply`.
- **Known limitations** — the resolution is macOS-only; a custom configuration path is not detected by design.

## `advisor-routing-1` — whether the `RECOMMENDED` deep review should run

- **Unit, tree, branch, base** — `select-the-installed-idea-config`; tree `/Users/afanasyev/Development/axon-showcase`;
  branch of the same name; base `origin/main` `0727fb8b`; nothing committed; the unit's files staged.
- **Owner's authorization** — the instruction of 2026-11-08: "Уточни у советника."
- **Question and proposed next action** — should the `RECOMMENDED` deep review run or be skipped, and what would a deep
  pass add that sixteen quick rounds could not? The next action proposed is whichever the owner then decides.
- **Stage** — Implementation review: the implementation is complete, the quick loop closed clean at round 16, and the
  routing decision (`RECOMMENDED`, `[repeated-class] 10 rows`) awaits the run-or-skip resolution.
- **Artifacts** — `openspec/changes/select-the-installed-idea-config/{proposal.md,design.md,tasks.md}` and the delta;
  `scripts/ensure-idea-settings.py`; `scripts/setup-idea.sh`; both suites;
  `openspec/specs/showcase/quality/ide-config/spec.md`; the unit's `unit.md`, `rounds.tsv`, `findings/`,
  `deep-review.md`, and the advisory consultations (`advisor-proposal-1/2`).
- **Prior findings and dispositions** — rounds 1–16; rounds 10 and 16 clean; the advisor's proposal consultation's three
  problems and its boundary correction, all fixed.
- **Completed checks** — `python3 scripts/test-ensure-idea-settings.py` 50 OK; `sh scripts/test-setup-idea.sh` all
  cases; `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` BUILD SUCCESSFUL; `openspec validate --all` 27/0;
  `spotlessApply`; `--expect-clean`; `--agent-sessions`; `--review-ready`; a live run on this host selected
  `IntelliJIdea2026.3` and reported `CONFIGURED`.
- **Known limitations** — the resolution is macOS-only; a custom configuration path is not detected by design.
- **Packs** — `none`.
- **Report language** — English; the caller presents to the owner in Russian.

**Outcome** — run the deep review. Three observations, no hypothesis, one confirmed problem: the bundle-selection path
accepted two matching SDK entries in the identified configuration through `selected[0]`, bypassing the duplicate-entry
refusal the fallback branch keeps, which contradicts the design's "SDK match unchanged" promise and the existing
ambiguity contract. Fixed with that refusal and a case; the advisor's focus for the pass is the selection decision model
crossed with SDK cardinality, the failure states, and the wrapper's launcher selection — not another formatting sweep.

## Round 17 — implementation screen, after the consultation's confirmed problem

- **Unit, tree, branch, base** — unchanged; base `origin/main` `0727fb8b`; the unit's files staged.
- **Packs** — `none`.
- **Supplied artifacts** — the implemented diff and the change dir.
- **Scope** — the identified-configuration path against the SDK-cardinality rules the fallback branch keeps.
- **What changed since round 16** — the identified configuration now refuses two matching SDK entries (naming them and
  the keep-one remedy) instead of taking the first, and a case pins it; the fallback's refusal is unchanged.
- **Prior findings with their instances** — rounds 1–16 at `findings/`; compare each and report an unfixed instance or a
  new instance of a raised class as such.
- **Completed checks** — `python3 scripts/test-ensure-idea-settings.py` 51 OK; `sh scripts/test-setup-idea.sh` all
  cases; `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` BUILD SUCCESSFUL; `openspec validate --all` 27/0;
  `spotlessApply`; `--agent-sessions`.
- **Known limitations** — the resolution is macOS-only; a custom configuration path is not detected by design.

## Round 18 — implementation screen, after round 17's findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `0727fb8b`; the unit's files staged.
- **Packs** — `none`.
- **Supplied artifacts** — the implemented diff and the change dir.
- **Scope** — the identified-path refusal's message and case, the proposal's summaries, the 120 rule, the tie, the
  wrapper, the docs, and the marks.
- **What changed since round 17** — the identified-path several-entries refusal names the interpreter path and version,
  and its case asserts it; the proposal's What Changes and Modified Capabilities state the duplicate-entry refusal; the
  case's docstring is wrapped.
- **Prior findings with their instances** — rounds 1–17 at `findings/`; compare each and report an unfixed instance or a
  new instance of a raised class as such.
- **Completed checks** — `python3 scripts/test-ensure-idea-settings.py` 51 OK; `sh scripts/test-setup-idea.sh` all
  cases; the 120 recipe reports none; `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` BUILD SUCCESSFUL;
  `openspec validate --all` 27/0; `spotlessApply`.
- **Known limitations** — the resolution is macOS-only; a custom configuration path is not detected by design.

## `deep-1` — deep review, pass 1

- **Unit, tree, branch, base** — `select-the-installed-idea-config`; tree `/Users/afanasyev/Development/axon-showcase`;
  branch of the same name; base `origin/main` `0727fb8b`; nothing committed; the unit's files staged.
- **Packs** — `none`: the Python tooling script and its shell wrapper, their suites, the OpenSpec delta, the docs, and
  records.
- **Supplied artifacts** — `openspec/changes/select-the-installed-idea-config/{proposal.md,design.md,tasks.md}` and the
  delta; `scripts/ensure-idea-settings.py`; `scripts/setup-idea.sh`; `scripts/test-ensure-idea-settings.py`;
  `scripts/test-setup-idea.sh`; `openspec/specs/showcase/quality/ide-config/spec.md`; the archived
  `2026-10-30-reproduce-python-ide-setup/design.md` (D3); and the unit's `unit.md`, `rounds.tsv`, `findings/`,
  `deep-review.md`.
- **Scope** — the selection decision model crossed with SDK cardinality, the failure states, the wrapper's launcher
  selection, and the change against the delta and the archived decision; the advisor's requested focus.
- **Prior findings with their statuses** — the quick loop's rounds 1–18, all fixed, with rounds 10 and 18 clean; the
  advisor's `advisor-proposal-1/2` problems and its `advisor-routing-1` confirmed problem, all fixed.
- **Completed checks** — `python3 scripts/test-ensure-idea-settings.py` 51 OK; `sh scripts/test-setup-idea.sh` all
  cases; `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` BUILD SUCCESSFUL; `openspec validate --all` 27/0;
  `spotlessApply`; `--expect-clean`; `--review-ready`; a live run selecting `IntelliJIdea2026.3`.
- **Known limitations** — the resolution is macOS-only; a custom `idea.config.path` is not detected by design.
- **Expected session** — a new session (the key has no recorded session); transition `created`.

## Round 19 — implementation screen, after the deep pass's findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `0727fb8b`; the unit's files staged.
- **Packs** — `none`.
- **Supplied artifacts** — the implemented diff and the change dir.
- **Scope** — the tie's condition against the delta and the design, the duplicate-entry refusal's specification, the
  cases, the docs, and the marks.
- **What changed since round 18** — the tie refuses only when both same-version editions register the interpreter (one
  selects it, none takes the missing-SDK stop), pinned by a case; the delta gains the duplicate-entry scenario, D2 gains
  its row, and the tie paragraph states the one-registration case.
- **Prior findings with their instances** — rounds 1–18 at `findings/`; compare each and report an unfixed instance or a
  new instance of a raised class as such.
- **Completed checks** — `python3 scripts/test-ensure-idea-settings.py` 52 OK; `sh scripts/test-setup-idea.sh` all
  cases; `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` BUILD SUCCESSFUL; `openspec validate --all` 27/0;
  `spotlessApply`.
- **Known limitations** — the resolution is macOS-only; a custom configuration path is not detected by design.

## Round 20 — implementation screen, after round 19's findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `0727fb8b`; the unit's files staged.
- **Packs** — `none`.
- **Supplied artifacts** — the implemented diff and the change dir.
- **Scope** — the delta's scenario set, the tie's registration probe, the cases, the docs, and the marks.
- **What changed since round 19** — the duplicated delta scenario is removed; the tie's registration probe propagates an
  unparsable SDK table as the stage's `ERROR`, with a case pinning it; task 3.3 is ticked.
- **Prior findings with their instances** — rounds 1–19 at `findings/`; compare each and report an unfixed instance or a
  new instance of a raised class as such.
- **Completed checks** — `python3 scripts/test-ensure-idea-settings.py` 53 OK; `sh scripts/test-setup-idea.sh` all
  cases; `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` BUILD SUCCESSFUL; `openspec validate --all` 27/0;
  `spotlessApply`.
- **Known limitations** — the resolution is macOS-only; a custom configuration path is not detected by design.

## Round 21 — implementation screen, after round 20's finding

- **Unit, tree, branch, base** — unchanged; base `origin/main` `0727fb8b`; the unit's files staged.
- **Packs** — `none`.
- **Supplied artifacts** — the implemented diff and the change dir.
- **Scope** — the 120 rule over the changed scripts, the tie/probe paths, the delta's scenarios, the docs, and the
  marks.
- **What changed since round 20** — the new case's docstring is wrapped; the recipe reports none over the changed
  scripts.
- **Prior findings with their instances** — rounds 1–20 at `findings/`; compare each and report an unfixed instance or a
  new instance of a raised class as such.
- **Completed checks** — the 120 recipe reports none; `python3 scripts/test-ensure-idea-settings.py` 53 OK;
  `sh scripts/test-setup-idea.sh` all cases; `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` BUILD SUCCESSFUL;
  `openspec validate --all` 27/0; `spotlessApply`.
- **Known limitations** — the resolution is macOS-only; a custom configuration path is not detected by design.

## `deep-2` — deep review, pass 2 (verification)

- **Unit, tree, branch, base** — unchanged; base `origin/main` `0727fb8b`; the unit's files staged.
- **Packs** — `none`.
- **Supplied artifacts** — the same diff and planning artifacts, with the fixes the pass 1 findings produced: the tie
  now counts the same-version configurations that register the interpreter (one selects, more than one refuses, none
  takes the missing-SDK stop); the tie's probe propagates an unparsable SDK table as the stage's `ERROR`; the delta
  gains one duplicate-entry scenario and D2 its row.
- **Scope** — independent verification of both pass-1 findings against the current state, plus any material new
  evidence.
- **Prior findings with their statuses** — `deep-review.md` pass 1 (D1, D2), and the quick loop's rounds 1–21 (rounds
  10, 16, 18, and 21 clean).
- **Known limitations** — the resolution is macOS-only; the fixtures verify the paths and the live run covers one host
  state.
- **Expected session** — the pass 1 session, continued.

## Round 22 — implementation screen, after the deep verification's nitpick

- **Unit, tree, branch, base** — unchanged; base `origin/main` `0727fb8b`; the unit's files staged.
- **Packs** — `none`.
- **Supplied artifacts** — the implemented diff and the change dir.
- **Scope** — the tie's three states, the cases, the deep record, the docs, and the marks.
- **What changed since round 21** — the tie's "neither registers" state has a case asserting the missing-SDK stop rather
  than the tie wording; the deep record carries both passes and the budget.
- **Prior findings with their instances** — rounds 1–21 at `findings/`; compare each and report an unfixed instance or a
  new instance of a raised class as such.
- **Completed checks** — `python3 scripts/test-ensure-idea-settings.py` 54 OK; `sh scripts/test-setup-idea.sh` all
  cases; `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` BUILD SUCCESSFUL; `openspec validate --all` 27/0;
  `spotlessApply`.
- **Known limitations** — the resolution is macOS-only; a custom configuration path is not detected by design.

## Round 23 — implementation screen, after round 22's finding

- **Unit, tree, branch, base** — unchanged; base `origin/main` `0727fb8b`; the unit's files staged.
- **Packs** — `none`.
- **Supplied artifacts** — the implemented diff and the change dir.
- **Scope** — the 120 rule over every changed script, the tie's three states, the deep record, the docs, and the marks.
- **What changed since round 22** — the new case's docstring is wrapped; the recipe is clean over all four changed
  scripts.
- **Prior findings with their instances** — rounds 1–22 at `findings/`; compare each and report an unfixed instance or a
  new instance of a raised class as such.
- **Completed checks** — the 120 recipe clean; `python3 scripts/test-ensure-idea-settings.py` 54 OK;
  `sh scripts/test-setup-idea.sh` all cases; `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` BUILD SUCCESSFUL;
  `openspec validate --all` 27/0; `spotlessApply`.
- **Known limitations** — the resolution is macOS-only; a custom configuration path is not detected by design.

## `advisor-routing-2` — the same decision, follow-up after both deep passes

- **What it asks** — whether the deep review's outcome and the fixes that followed it settle the decision, and whether
  the unit may go to the owner's manual pass.
- **What changed** — the owner resolved to run the pass (the advisor's recommendation); pass 1 raised one should-fix
  (the tie refused on bundle presence, not on "both register") and one nitpick (the duplicate-entry refusal had no delta
  scenario or D2 row), both fixed; pass 2 verified both and raised one nitpick (the tie's "neither registers" state had
  no case), fixed; the quick loop then closed clean at round 23.
- **Packs** — `none`.
- **Report language** — English; the caller presents to the owner in Russian.

**Outcome (`advisor-routing-2`)** — the substantive concerns are addressed, no third deep pass is justified, and the
only confirmed problem was the missing process-defect record; once written (two records: the two-location findings line
and the overwritten findings file) the advisor's recommendation is to request the owner's manual review.

## `advisor-routing-3` — the same decision, follow-up after the process-defect record

- **What it asks** — whether the condition you set is met, and whether the unit may go to the owner's manual pass.
- **What changed** — the unit's `process-defects.md` now exists with two factual records: a findings line whose location
  carried two paths (the instances check rejected it, fixed in the same round) and an overwritten `findings/round-7.md`
  beside a duplicated round-7 row (restored and de-duplicated). Task 3.5 is ticked; `--unit-layout`, `--review-ready`,
  `--expect-clean`, and both suites are green; nothing is committed.
- **Packs** — `none`.
- **Report language** — English; the caller presents to the owner in Russian.

**Outcome (`advisor-routing-3`)** — agreement: the remaining condition is met, no confirmed problem remains, no further
deep pass is warranted, and the recommendation is to request the owner's manual review. Agreement to proceed is not
delivery approval.
