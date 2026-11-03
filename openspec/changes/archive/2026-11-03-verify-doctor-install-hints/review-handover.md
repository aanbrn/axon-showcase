# Review hand-over

## Round 1 — implementation

- **Unit**: `verify-doctor-install-hints`; **base** `0a412be1079f1097bc9b5dbeb5b1f93304adbdb2`; **state**: uncommitted
  working tree, staged, no commit; **tree**: `/Users/afanasyev/Development/axon-showcase-verify-doctor-install-hints`;
  **branch**: `verify-doctor-install-hints`.
- **Supplied artifacts**: the full diff against the base — `README.md`, `scripts/doctor.sh`, `scripts/test-doctor.sh`,
  `docs/ideas.md` — and the change dir: `proposal.md`, `design.md` (Context, Goals/Non-Goals, five decisions with
  rejected alternatives, Risks, a fourteen-row claims table, per-kind dismissals), `tasks.md` (with the `Workspace:`
  record), and the delta spec.
- **Stage**: implementation. The unit is small enough that its planning artifacts and its implementation were written in
  one turn; no separate proposal round ran, so the screen covers both.
- **Packs**: none — the subject is a POSIX-shell doctor, its check script, and the README table, not a Gradle decision,
  JVM code, or a deployment surface. The `check-and-test-authoring` skill applies: a check and its controls changed.
- **The premise the design rests on**: the capability already requires the doctor's guidance to agree with the README's
  install column, and the check asserted only that a hint exists, so the texts had drifted on three (platform, probe)
  pairs. The README's `Docker & Compose` row cannot carry two different per-platform installs, so it becomes two rows.
- **Measured reachability**: with the agreement assertion in place and the texts unreconciled,
  `bash scripts/test-doctor.sh` exited `1` on
  `the doctor's install hint for 'compose' on debian is 'sudo apt install docker-compose-v2', but the README row 'Docker & Compose' carries 'sudo apt install docker.io docker-compose-v2'`.
- **Measured controls**: one word changed in each of the three reconciled cells failed the run naming the right probe
  and platform (`compose`/macos, `compose`/debian, `python3`/debian), README restored byte-identical; narrowing the
  cells extraction failed on `probe 'compose' is declared readme=yes but the install-hint agreement never compared it`;
  dropping the marker restore failed on the OpenCode Debian pair.
- **Completed checks**: `bash scripts/test-doctor.sh` exits `0`;
  `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` BUILD SUCCESSFUL; `spotlessCheck`, `verifyTableRows` clean;
  `openspec validate --all` 27 passed with the change item valid; the 120-character recipe flags only the README's table
  rows and the `Workspace:` record.
- **Known limitations**: the cell normalizer treats backtick placement as presentation, so a cell that differs from its
  hint only in emphasis passes; the check does not verify that following a hint satisfies its probe (the compose macOS
  branch keeps `Docker Desktop, or …` for that reason); `gh` is `readme=no` and outside the comparison.
- **Deadline**: this caller's policy — the foreground invocation's return. A call that cannot complete is recorded as
  `stopped-responding` and replaced from this hand-over, never by an unexplained fresh session.
- **Risk focuses**: (1) whether the README is the right authority and splitting its row the right reconciliation, or
  whether the simulator side should have moved instead; (2) whether the normalizer is too lenient — what difference it
  could hide that the requirement forbids; (3) whether a new install column, or a new row, fails loudly rather than
  shrinking the check; (4) whether the coverage assertion is meaningful or a restatement of the existing readme=yes/row
  direction.

## Round 2 — implementation, after round 1's six findings

- **Round 1** raised six findings, all verified and applied:
  - `test-control` — the "changed word" case could not fail independently of the exact-equality fixture above it, so no
    committed case drove the comparison itself. The comparison and the coverage predicate are now functions
    (`hint_agrees`, `covers_documented_probe`) with committed cases, one of which is the drift this change repaired (a
    cell carrying one command more than its hint) — a substring or prefix comparison fails on it, measured.
  - `docs-residue` — the README's "Only **Java and Docker** are required…" under-named after the row split; it now says
    Java, Docker, and Compose, which matches the doctor's required set and the spec's list.
  - `claim-unreproducible` — the proposal said "two hint texts" where `scripts/doctor.sh` changes one line.
  - `claim-unreproducible` — two `tasks.md` spans carried a wrapped continuation's spaces; both are rewritten.
  - `claim-unreproducible` — the `[count]` dismissal claimed a command where rows 3, 6, and 8 name artifacts; reworded.
  - `structured-writing` — the proposal's reconciliation bullet bundled three changes; split into one bullet each.
- **Re-measured**: `bash scripts/test-doctor.sh` exits `0`; a comparison weakened to a substring match fails on the
  repaired drift pair; `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` BUILD SUCCESSFUL; `spotlessCheck`,
  `verifyTableRows` clean; no inline span in the change dir carries a two-space run.

## Round 3 — implementation, after round 2's three findings

- **Round 2** raised three findings, all verified and applied:
  - `test-control` (the instance round 1 named, still present) — the redundant changed-word line is deleted, its job now
    held by `expect_hint_agreement`.
  - `test-control` (a new weakening) — a case-insensitive comparison passed every fixture, so the agreement cases gained
    a case-only difference, and the coverage predicate gained a key that is a prefix of a compared one and a case-only
    key; the two predicates were swept in the same pass rather than at the one instance named.
  - `impact-omission` — the README's required-set sentence was edited without a task or an Impact entry; task 3.5
    records it and the Impact bullet names it.
- **Re-measured**: `bash scripts/test-doctor.sh` exits `0`; `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`
  BUILD SUCCESSFUL; `spotlessCheck`, `verifyTableRows` clean; no inline span in the change dir carries a two-space run.

## Round 5 — implementation, after the advisory consultation

- **The consultation** recommended skipping the deep review (`Agreement: yes`), which the owner decided; the decision
  and its accepted risks are recorded on task 7.3.
- **Its open question was real.** `brew info docker-compose` shows the formula's caveat: Docker finds the plugin only
  with a `cliPluginsExtraDirs` entry in `~/.docker/config.json`, so `brew install docker-compose` alone does not satisfy
  `docker compose version`. Docker also discovers a plugin symlinked into `~/.docker/cli-plugins`, and that is the step
  this change names — it needs no config edit. Since the change moved that text into the README, where a column is a
  claim that following it satisfies the probe, the owner chose to correct it rather than park it: both surfaces now read
  `Docker Desktop, or brew install docker-compose (symlink into ~/.docker/cli-plugins)`, and the agreement check accepts
  them.
- **Swept with it**: design claim rows 15 (the formula caveat, quoted) and 16 (the suite passing on the corrected pair),
  the Non-Goal that said no hint's content moves, and the Risk bullet that previously accepted the incomplete branch;
  task 3.6 records the edit.
- **Re-measured**: `bash scripts/test-doctor.sh` exits `0`; `spotlessCheck`, `verifyTableRows` clean.

## Round 6 — implementation, after round 5's three findings

- **Round 5** raised three findings, all verified and applied:
  - the caveat's ask was overstated (it names only the `cliPluginsExtraDirs` entry); the design's Non-Goal, Risk, and
    Decision 2 now attribute the entry to the caveat and the symlink to this change's choice, with its reason (no config
    edit);
  - the proposal's What Changes lacked the compose-remedy bullet;
  - the proposal said one hint text where two changed, and task 3.3 said every other hint was unchanged.
- **Re-measured**: `bash scripts/test-doctor.sh` exits `0`; `spotlessCheck`, `verifyTableRows` clean.

## Round 8 — implementation, after the advisor returned `Agreement: no`

- **The advisor's two ordered changes**: substantiate the symlink route with its source and destination filename, and
  establish that those exact instructions satisfy the probe.
- **Established by a bounded fixture, not asserted**: under Docker CLI 29.9.2, an isolated `DOCKER_CONFIG` holding a
  stub plugin that answers the `docker-cli-plugin-metadata` handshake prints `Docker Compose version v2.99.0` when
  placed at `<config>/cli-plugins/docker-compose` and when referenced from `cliPluginsExtraDirs`; a stub without the
  metadata answer is listed with `Err: invalid metadata`, and the host baseline prints its own v2.32.1.
- **Both surfaces now read**
  `Docker Desktop, or brew install docker-compose, symlinked to ~/.docker/cli-plugins/docker-compose` — the destination
  filename is the one discovery requires, and the doctor's hint is a two-argument `echo` so neither source line exceeds
  120 columns.
- **Swept with it**: the design's Risk bullet (the route, the filename, and that the fixture establishes both), claims
  row 17 (the experiment), task 3.6 (rewritten) and 3.7 (the fixture).
- **Re-measured**: `bash scripts/test-doctor.sh` exits `0`; `spotlessCheck`, `verifyTableRows` clean.

## Standing limitations accepted at hand-off

- **The compose remedy's real formula install is untested.** The fixture establishes the discovery mechanism and the
  destination filename with a stub plugin under Docker CLI 29.9.2; `brew install docker-compose` itself was not run on
  this host, so the end-to-end sequence is supported at the mechanism level, not by an installation test.
- **The cell normalizer treats markdown as presentation**, so a cell differing from its hint only in backtick placement
  passes; the guidance text, not its emphasis, is what the check compares.
- **The extraction's completeness is asserted, not proven**: every documented probe must appear among the compared
  pairs, but the pairs come from parsing the README's table, whose future shapes the check does not enumerate.
- **No check verifies that a hint satisfies its probe.** The agreement keeps the two surfaces equal, not correct; the
  compose remedy is the one this change corrected on evidence rather than by comparison.
