# Review hand-over — decouple-python-guidance-from-pyenv

One section per quick-review invocation, written before the caller waits on it, so a stalled session is recovered from
this record rather than from the session.

## Round 1 — proposal stage

- **Unit** — `decouple-python-guidance-from-pyenv`; change dir `openspec/changes/decouple-python-guidance-from-pyenv/`;
  record `docs/units/decouple-python-guidance-from-pyenv/unit.md`.
- **Base and current revision** — `origin/main` at `bfe30a08`; `HEAD` at the same commit (nothing committed).
- **Workspace** — tree `/Users/afanasyev/Development/axon-showcase-decouple-python-guidance-from-pyenv`, branch
  `decouple-python-guidance-from-pyenv`.
- **Supplied artifacts** — `proposal.md`, `design.md` (D1–D5, the eight-row claims table, the per-kind dismissals,
  non-goals, verification), `tasks.md`, `.openspec.yaml`, the unit record, and the four files the change will edit:
  `README.md`'s interpreter guidance, `.opencode/skills/setup-idea/SKILL.md`, `scripts/ensure-idea-settings.py`, and
  `scripts/test-ensure-idea-settings.py`; also the `showcase/quality/ide-config` requirement the design cites.
- **Scope** — proposal-stage screen: coherence of proposal/design/tasks; whether the wording is genuinely
  manager-agnostic without claiming uv support the repository does not give; whether the `skip_specs` rationale holds;
  whether each claims row reproduces; Structured Writing, code spans, the 120-character recipe, and the table rows. No
  implementation exists yet.
- **Prior findings** — none; this is the unit's first round.
- **Completed checks** — `spotlessApply`/`spotlessCheck` pass; `openspec validate --all` reports 27 passed, and
  `openspec validate --changes` accepts `skip_specs`; `claim-scan.py --base origin/main` seeds the table, and every
  candidate is a row or is covered by its kind's dismissal.
- **Limitations** — the uv behavior rows were probed on this machine's `uv 0.13.7` and `pyenv 2.8.14`; the wording rests
  on them, and no other manager's behavior is claimed.
- **Subject packs** — none: the change edits markdown, a Python diagnostic message, and its Python test; no JVM code,
  test tier, Gradle build decision, or deployment surface.
- **Deadline** — 20 minutes from this record's write.

## Round 2 — proposal stage

- **Unit** — `decouple-python-guidance-from-pyenv`; change dir `openspec/changes/decouple-python-guidance-from-pyenv/`;
  record `docs/units/decouple-python-guidance-from-pyenv/unit.md`.
- **Base and current revision** — `origin/main` at `bfe30a08`; `HEAD` at the same commit (nothing committed).
- **Workspace** — tree `/Users/afanasyev/Development/axon-showcase-decouple-python-guidance-from-pyenv`, branch
  `decouple-python-guidance-from-pyenv`.
- **Supplied artifacts** — the same planning artifacts and edit targets as round 1, plus `findings/round-1.md`.
- **Scope** — re-screen of round 1's six instances, and the corrected premise they turned on: the guidance now states
  the selector condition (`python3` resolves to the selected interpreter) and keeps `pyenv local` as the one example,
  with uv named only for what its own commands read. Also re-screen the widened dismissals, the three new claims rows
  (C9, C10, C11; none renumbered), the split `What Changes` bullets, and the four-file count.
- **Prior findings** — round 1's six, all applied: the premise (`uv python pin` does not change a bare `python3`; the
  plan now states the condition and the README distinguishes uv's own commands), the missing rows (C9 imports, C10 the
  pin's `PATH`-level effect), the widened per-kind dismissals, "five files" → four, the split proposal bullets, and the
  inline-span/133-character task text.
- **Completed checks** — `spotlessApply`/`spotlessCheck` pass; `openspec validate --all` and `--changes` pass;
  `claim-scan.py` re-seeded the table after the rewrite; `--unique-rounds --expect …:1`, `--session-continuity`, and
  `--finding-instances` pass; `--handover decouple-python-guidance-from-pyenv:2` passes.
- **Limitations** — the uv rows were probed on this machine's `uv 0.13.7` and `pyenv 2.8.14`; no uv-managed interpreter
  is installed, so whether a uv-installed `python3` honours `.python-version` remains unprobed and is not claimed.
- **Subject packs** — none, as in round 1.
- **Deadline** — 20 minutes from this record's write.

## Round 3 — proposal stage

- **Unit** — `decouple-python-guidance-from-pyenv`; change dir `openspec/changes/decouple-python-guidance-from-pyenv/`;
  record `docs/units/decouple-python-guidance-from-pyenv/unit.md`.
- **Base and current revision** — `origin/main` at `bfe30a08`; `HEAD` at the same commit (nothing committed).
- **Workspace** — tree `/Users/afanasyev/Development/axon-showcase-decouple-python-guidance-from-pyenv`, branch
  `decouple-python-guidance-from-pyenv`.
- **Supplied artifacts** — the planning artifacts with the twelve-row claims table (C1–C12), the rewritten dismissal
  table, the edit targets, and `findings/round-1.md` and `findings/round-2.md`.
- **Scope** — re-screen round 2's five instances and the root cause they shared. The dismissal section is now a table
  mapping each scanner kind to the artifact's own claim kind, rather than one widened clause per finding; C12 carries
  the resolution claim the plan rests on; C10's source is a runnable command; the hand-over's round descriptions state
  each round's own table size.
- **Prior findings** — round 2's five, all applied: the missing resolution row (C12), the `cross-ref` and `absolute`
  dismissal classes (rewritten from the artifacts' claim kinds, and C9 added to the row list), C10's placeholder source,
  and the hand-over's two counts.
- **Completed checks** — `spotlessApply`/`spotlessCheck` pass; `openspec validate --all` and `--changes` pass;
  `--unique-rounds --expect …:2`, `--session-continuity`, `--finding-instances`, and `--handover …:3` pass.
- **Limitations** — as round 2: no uv-managed interpreter is installed, so whether a uv-installed `python3` honours
  `.python-version` remains unprobed and unclaimed.
- **Subject packs** — none, as in rounds 1 and 2.
- **Deadline** — 20 minutes from this record's write.

## Round 4 — proposal stage

- **Unit** — `decouple-python-guidance-from-pyenv`; change dir `openspec/changes/decouple-python-guidance-from-pyenv/`;
  record `docs/units/decouple-python-guidance-from-pyenv/unit.md`.
- **Base and current revision** — `origin/main` at `bfe30a08`; `HEAD` at the same commit (nothing committed).
- **Workspace** — tree `/Users/afanasyev/Development/axon-showcase-decouple-python-guidance-from-pyenv`, branch
  `decouple-python-guidance-from-pyenv`.
- **Supplied artifacts** — the planning artifacts with the twelve-row claims table (C1–C12) and the widened dismissal
  table, the edit targets, and `findings/round-1.md` through `findings/round-3.md`.
- **Scope** — re-screen round 3's single instance: the dismissal table now names the non-claim kinds in its `absolute`
  and `path` cells, so its exhaustiveness sentence is true as written. Re-check the rest of the artifact unchanged since
  round 3.
- **Prior findings** — round 3's one, applied: the two cells widened.
- **Completed checks** — `spotlessApply`/`spotlessCheck` pass; `openspec validate --all` and `--changes` pass;
  `--unique-rounds --expect …:3`, `--session-continuity`, `--finding-instances`, and `--handover …:4` pass.
- **Limitations** — as before: no uv-managed interpreter is installed, so whether a uv-installed `python3` honours
  `.python-version` remains unprobed and unclaimed.
- **Subject packs** — none, as in rounds 1–3.
- **Deadline** — 20 minutes from this record's write.

## `advisor-1` — consultation on the proposal's scope, premise, and evidence

- **Unit, tree, branch, base** — `decouple-python-guidance-from-pyenv`; tree
  `/Users/afanasyev/Development/axon-showcase-decouple-python-guidance-from-pyenv`; branch of the same name; base
  `origin/main` `bfe30a08`; nothing committed, the unit's files staged.
- **Authorization** — the owner's instruction of 2026-11-09: "Отревьювь с советником до согласия" — review with the
  advisor until agreement, before their manual pass over the proposal.
- **Objective and constraints** — the proposal must decouple the repository's optional Python interpreter guidance from
  pyenv without changing behavior. Constraints: no interpreter resolution change, no `.python-version` inheritance
  change, no CI provisioning change, no doctor probe or Prerequisites row for uv, no uv-specific support the repository
  does not need.
- **The question** — does this proposal hold as it stands, and what must change before the owner's manual pass? In
  particular: (a) is the corrected premise right — `pyenv local` as the one concrete example, the condition "`python3`
  resolves to the selected interpreter" stated, and uv named only for what its own commands read — or should uv be
  dropped from the README entirely, or named as a `PATH`-level route through `uv python install`; (b) is `skip_specs`
  correct against `showcase/quality/ide-config`; (c) is the claims table's evidence sufficient for every factual
  statement the shipped text will make; (d) is anything missing from the plan (a site, a test, a doc, `docs/ideas.md`)?
- **Change identity and stage** — change `decouple-python-guidance-from-pyenv`, proposal stage, four quick-review rounds
  closed clean (round 4 reported "nothing remains"), no implementation exists.
- **Supplied artifacts** — `openspec/changes/decouple-python-guidance-from-pyenv/{proposal.md,design.md,tasks.md}`; the
  unit records under `docs/units/decouple-python-guidance-from-pyenv/`; the four edit targets `README.md` (remedy
  paragraph near line 1030, optional interpreter-selection paragraph near 1043–1046),
  `.opencode/skills/setup-idea/SKILL.md` (lines 40, 59), `scripts/ensure-idea-settings.py` (message near line 653),
  `scripts/test-ensure-idea-settings.py` (assertion near line 463); and the cited requirement
  `openspec/specs/showcase/quality/ide-config/spec.md` "The Python analysis interpreter is the one the build runs".
- **Evidence excerpts (commands run by the caller; the advisor has no shell)** —
  `git grep -n "pyenv" bfe30a08 -- ':!openspec/changes/archive' ':!docs/units'` returns exactly the four files above
  (seven lines). `git grep -n "pyenv" bfe30a08 -- openspec/specs` returns no hits. `build.gradle.kts` 492–498 resolves
  `pythonExecutable` as the first executable `python3` on `PATH`; `scripts/git-hooks/pre-commit:17` runs
  `exec python3 …`; `scripts/setup-idea.sh` 108–112 runs `python3 "$SCRIPT_DIR/ensure-idea-settings.py"`. With a temp
  dir whose `.python-version` says `3.12.16`,
  `PATH="$HOME/.local/bin:/opt/homebrew/bin:/usr/bin:/bin" python3 --version` prints `Python 3.15.2`, while the login
  `PATH` (pyenv shims first) prints `3.12.16`. With `.python-version` naming an uninstalled `3.13.99`, the login `PATH`
  prints `Python 3.15.2` and exits 0. `uv python find` in a dir pinning `3.12.16` reports the pyenv install; in a dir
  pinning `3.13.99` it exits 2 with pyenv's "not installed" error. `uv python pin 3.12.16` writes `.python-version`.
  `uv python dir --bin` prints `/Users/afanasyev/.local/bin`; `uv python install --help` documents `--no-bin` ("Do not
  install a Python executable into the `bin` directory"). No uv-managed interpreter is installed here, so whether a
  uv-installed `python3` honours `.python-version` is unprobed and unclaimed.
- **Prior findings and dispositions** — four quick rounds, `docs/units/decouple-python-guidance-from-pyenv/findings/`;
  all findings applied; the round-1 premise finding is what produced the corrected uv scope.
- **Packs** — none: markdown, a Python diagnostic message, and its Python test; no JVM code or test, no build decision,
  no deployment surface.
- **Report language and audience** — English, agent-facing; the caller presents to the owner in Russian.
- **Effort** — the configured default; no override requested.
- **Fresh or follow-up** — fresh consultation for this decision.

## `advisor-2` — follow-up: the narrowed wording and evidence after `advisor-1`

- **Unit, tree, branch, base** — `decouple-python-guidance-from-pyenv`; tree
  `/Users/afanasyev/Development/axon-showcase-decouple-python-guidance-from-pyenv`; branch of the same name; base
  `origin/main` `bfe30a08`; nothing committed, the unit's files staged.
- **Authorization** — the same owner instruction of 2026-11-09, "review with the advisor until agreement", which names
  this follow-up under the same decision and opinion.
- **Objective and constraints** — unchanged: decouple the guidance from pyenv without changing behavior; no uv-specific
  support, no prerequisite row, no doctor probe.
- **The question** — does the revised package now hold, and is anything left? Specifically: (a) is the narrowed uv
  contrast right (the pin's effect depends on which selector is first on `PATH`, rather than "uv's own commands read it
  while a bare `python3` does not"); (b) is C3's narrowed wording right; (c) is C11's "observation rather than a claimed
  mechanism" right; (d) anything else before the owner's manual pass?
- **What changed since `advisor-1`** —
  1. `proposal.md`'s `## Why` and the README bullet: "a `.python-version` written by `uv python pin` changes what a bare
     `python3` runs only through whichever selector is first on `PATH`"; the paragraph now says a pin matters "only
     where the environment integrates it into that selection (pyenv's shims do, and uv's own commands read the same
     file)" and "a pin does not by itself change which `python3` the shell resolves".
  2. `design.md` Context: "a pin's effect depends on what reads it there"; D1: "a pin's effect on a bare `python3`
     depends on which selector is first on `PATH` (C10, C11)".
  3. `tasks.md` 1.2: the same narrowing, with the verify clause now "the selector distinction is stated without a
     universal claim about uv".
  4. C3: "uv reports `~/.local/bin` as the directory it installs Python executables into", sourced from
     `uv python dir --bin` and its `--help`.
  5. C11: "An uninstalled pin can leave a bare `python3` on a different interpreter, an observation rather than a
     claimed mechanism", with the archived D6 as the recorded observation.
- **Prior findings** — `advisor-1`'s two confirmed problems, both applied above; its open question (uv-managed
  interpreter integration) stays unprobed and unclaimed.
- **Packs** — none, as before.
- **Report language and audience** — English, agent-facing; the caller presents to the owner in Russian.
- **Effort** — the configured default; no override.
- **Fresh or follow-up** — follow-up on `advisor-1`, same decision and opinion.

## `advisor-3` — follow-up: the exclusive-selector wording removed

- **Unit, tree, branch, base** — `decouple-python-guidance-from-pyenv`; tree
  `/Users/afanasyev/Development/axon-showcase-decouple-python-guidance-from-pyenv`; branch of the same name; base
  `origin/main` `bfe30a08`; nothing committed, the unit's files staged.
- **Authorization** — the same owner instruction of 2026-11-09, naming this follow-up under the same decision and
  opinion.
- **Objective and constraints** — unchanged.
- **The question** — after removing the exclusive-selector formulation, is anything left before the owner's manual pass?
- **What changed since `advisor-2`** — `proposal.md`'s `## Why` now reads "a `.python-version` written by
  `uv python pin` changes what a bare `python3` runs only where the local environment integrates it into the interpreter
  selection; the repository itself selects `python3` from `PATH`". `design.md`'s Context says "the repository itself
  selects `python3` from `PATH`, so a pin matters only where the local environment integrates it into that selection —
  pyenv's shims do here"; D1 says "a pin matters only where the environment integrates it into the selection (C10,
  C11)"; C10 is "A `.python-version` affects a bare `python3` only where the environment integrates it into the
  selection", its Result stating the two observed outcomes rather than a mechanism.
- **Prior findings** — `advisor-1`'s two and `advisor-2`'s one, all applied. The uv-managed integration question stays
  unprobed and unclaimed.
- **Packs** — none.
- **Report language and audience** — English, agent-facing; the caller presents to the owner in Russian.
- **Effort** — the configured default; no override.
- **Fresh or follow-up** — follow-up on `advisor-2`, same decision and opinion.

## Round 5 — implementation stage

- **Unit** — `decouple-python-guidance-from-pyenv`; change dir `openspec/changes/decouple-python-guidance-from-pyenv/`;
  record `docs/units/decouple-python-guidance-from-pyenv/unit.md`.
- **Base and current revision** — `origin/main` at `bfe30a08`; `HEAD` at the same commit (nothing committed); the whole
  change set staged.
- **Workspace** — tree `/Users/afanasyev/Development/axon-showcase-decouple-python-guidance-from-pyenv`, branch
  `decouple-python-guidance-from-pyenv`.
- **Supplied artifacts** — the planning artifacts (the twelve-row claims table, the dismissal table), the unit records,
  and the implementation diff: `README.md` (the `NEEDS_ACTION` remedy paragraph and the optional interpreter-selection
  paragraph), `.opencode/skills/setup-idea/SKILL.md` (initialization step 5 and the `NEEDS_ACTION` remedy),
  `scripts/ensure-idea-settings.py` (the `NEEDS_ACTION` message), `scripts/test-ensure-idea-settings.py` (one added
  assertion).
- **Scope** — implementation-stage screen: does the diff implement tasks 1.1–3.2 and nothing else; does every sentence
  the shipped text states match the claims table and the advisor's narrowed wording; is the test's control real (the new
  assertion failed against the pre-change message and passes now); are the tasks' verify clauses true of the tree;
  Structured Writing, inline code spans, the 120-character recipe.
- **Prior findings** — rounds 1–4 under `findings/`; all applied. The advisor's three passes ended in explicit
  agreement, with the "only where the environment integrates it" wording applied across the four sites.
- **Completed checks** — `spotlessApply`/`spotlessCheck` pass; `scripts/test-ensure-idea-settings.py` passes (54 tests,
  exit 0), and each added assertion was seen to fail against the message before its fix;
  `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` is `BUILD SUCCESSFUL`; `openspec validate --all` reports 27
  passed; `--review-ready` passes.
- **Limitations** — no uv-managed interpreter is installed, so uv-managed integration is unprobed and unclaimed; the
  README's pre-existing over-120 table rows and badge are untouched by this change.
- **Subject packs** — none: markdown, a Python diagnostic message, and its Python test; no JVM code or test, no build
  decision, no deployment surface.
- **Deadline** — 20 minutes from this record's write.

## Round 6 — implementation stage

- **Unit** — `decouple-python-guidance-from-pyenv`; change dir `openspec/changes/decouple-python-guidance-from-pyenv/`;
  record `docs/units/decouple-python-guidance-from-pyenv/unit.md`.
- **Base and current revision** — `origin/main` at `21b100bf` (the branch fast-forwarded onto it after the base advanced
  with `docs/audits/2026-11-07.md`, a file this unit does not touch); `HEAD` at the same commit; the whole change set
  staged.
- **Workspace** — tree `/Users/afanasyev/Development/axon-showcase-decouple-python-guidance-from-pyenv`, branch
  `decouple-python-guidance-from-pyenv`.
- **Supplied artifacts** — the planning artifacts, the unit records, `findings/round-5.md`, and the implementation diff.
- **Scope** — re-screen round 5's four instances: the emitted remedy now states the requirement ("Make python3 resolve
  to that version with your interpreter manager (for example: pyenv local <installed-version>)"), C3 quotes `--help`
  verbatim, tasks 0.1 and 5.1 are ticked, and this record was reflowed by the formatter before the final `check`. Also
  re-screen the rest of the implementation unchanged since round 5.
- **Prior findings** — round 5's four, all applied. The test's control was re-demonstrated for the corrected assertion
  (`Make python3 resolve to that version` failed against the intermediate message).
- **Completed checks** — `spotlessApply`/`spotlessCheck` pass; `scripts/test-ensure-idea-settings.py` passes (54 tests,
  exit 0); `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` is `BUILD SUCCESSFUL` on the final tree;
  `openspec validate --all` reports 27 passed; `--handover …:6` and `--review-ready` pass.
- **Limitations** — as round 5: uv-managed integration is unprobed and unclaimed.
- **Subject packs** — none.
- **Deadline** — 20 minutes from this record's write.

## `advisor-4` — consultation on the finished implementation and the deep-review decision

- **Unit, tree, branch, base** — `decouple-python-guidance-from-pyenv`; tree
  `/Users/afanasyev/Development/axon-showcase-decouple-python-guidance-from-pyenv`; branch of the same name; base
  `origin/main` `21b100bf` (fast-forwarded after the base advanced with `docs/audits/2026-11-07.md`, a file this unit
  does not touch); nothing committed, the whole change set staged.
- **Authorization** — the owner's instruction of 2026-11-09: "Отревьювь с советником до согласия" — review with the
  advisor until agreement, before the implementation's manual pass.
- **Objective and constraints** — the unit ships manager-agnostic interpreter guidance with no behavior change: the
  interpreter resolution, `.python-version` inheritance, CI provisioning, the doctor's `python3` probe, and the README
  Prerequisites row all stay as they are.
- **The question** — (a) does the implementation as shipped hold — the shipped wording, the emitted message, the test's
  control, and the records — and is anything left before delivery; (b) given the routing's `RECOMMENDED` on a
  repeated-class signal whose instances are all closed, should the deep review run or be skipped; (c) anything missing —
  a site, a doc, a spec delta, a `docs/ideas.md` entry?
- **Change identity and stage** — change `decouple-python-guidance-from-pyenv`, implementation stage; six quick rounds
  (round 6 clean); three advisor passes on the proposal scope ended in explicit agreement.
- **Diff, base `21b100bf`** — four files:
  - `README.md`: the `NEEDS_ACTION` remedy now reads "(for example `pyenv local <installed-version>`, so that `python3`
    resolves to that interpreter)"; the pyenv paragraph became "An interpreter manager is optional, and the repository
    itself selects `python3` from `PATH`. A personal `.python-version` matters only where the local environment
    integrates it into that selection — pyenv's shims do (`pyenv local <installed-version>` for an installed Python
    3.12+), and uv's own commands read the same file (`uv python pin <installed-version>`) — and a pin does not by
    itself change which `python3` the shell resolves. It is ignored rather than a shared repository runtime pin. Verify
    `python3 --version` in the environment running the tooling."
  - `.opencode/skills/setup-idea/SKILL.md`: step 5 now says "through a selector that reads it, such as pyenv's shims";
    the `NEEDS_ACTION` remedy says "(for example `pyenv local <version>`, so that `python3` resolves to it)".
  - `scripts/ensure-idea-settings.py`: the message's remedy is "Make python3 resolve to that version with your
    interpreter manager / (for example: pyenv local <installed-version>)."
  - `scripts/test-ensure-idea-settings.py`: one added assertion,
    `self.assertIn("Make python3 resolve to that version", message)`.
- **Prior findings and dispositions** — six rounds under `docs/units/decouple-python-guidance-from-pyenv/findings/`, all
  applied; round 6 reported "nothing remains". Three process-defect records: a missing `Packs:` line, a zsh read-only
  `status`, and a hand-over written after the last `spotlessApply`.
- **Deep-review routing** — `python3 scripts/review-routing.py --change decouple-python-guidance-from-pyenv` reports
  `RECOMMENDED` on `[repeated-class] 3 rows repeat a prior class`; the repeated classes were the claims/dismissal
  accounting (rounds 2–3, re-derived at the root in round 3 and narrowed by the advisor) and round 5's formatter, quote,
  and unticked-task findings (all closed in round 6). Recorded in `docs/units/<unit>/deep-review.md`.
- **Verification run** — `spotlessCheck` passes; `scripts/test-ensure-idea-settings.py` 54 tests OK, and the added
  assertion was seen to fail against the message before its fix;
  `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` is `BUILD SUCCESSFUL` on the refreshed base;
  `openspec validate --all` reports 27 passed; `--unique-rounds --expect-clean`, `--session-continuity`,
  `--agent-sessions`, and `--review-ready` pass.
- **Packs** — none: markdown, a Python diagnostic message, and its Python test; no JVM code or test, no build decision,
  no deployment surface.
- **Report language and audience** — English, agent-facing; the caller presents to the owner in Russian.
- **Effort** — the configured default; no override.
- **Fresh or follow-up** — fresh consultation for a new decision (`implementation-readiness`); the proposal-scope
  consultation (`interpreter-guidance-premise`, sections `advisor-1`–`advisor-3`) is a different decision.
