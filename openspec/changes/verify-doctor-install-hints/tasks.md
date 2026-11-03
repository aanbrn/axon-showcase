# Tasks

Workspace:
`unit=verify-doctor-install-hints; path=/Users/afanasyev/Development/axon-showcase-verify-doctor-install-hints; branch=verify-doctor-install-hints; writer=ses_e653ab5caffeGjp1myeRL03Emh`

## 1. The check

- [x] 1.1 `scripts/test-doctor.sh` extracts the doctor's own `install_hint()` function and runs it per (platform, probe)
      in a subshell, the pattern the script already uses for `validate_probe_table` and `version_at_least`, so the hint
      text is read from its owner rather than restated.
- [x] 1.2 The install columns are derived from the README's own header through a `readme_platform` mapping beside
      `readme_to_probe`; a column whose label maps to no platform token fails the check rather than being skipped, so
      the platform set follows the table. The hint-existence loop now reads that same `$platforms` list instead of its
      own `macos debian`.
- [x] 1.3 Row cells are split with the escaped pipe (`\|`) protected by a marker first and restored in
      `install_cell_text()`, which also strips backticks and collapses whitespace — the two normalizations the cells
      need and the hints do not.
- [x] 1.4 Every mapped (row, probe, platform) pair is compared: the doctor's hint must equal the cell's text, and a
      mismatch fails naming the probe, the platform, the hint, the cell, and the row.
- [x] 1.5 Coverage is asserted: every probe declared `readme=yes` must appear among the compared pairs, and the
      comparison must yield at least one pair, so a row or a probe the extraction stops seeing fails instead of
      shrinking the check silently.
- [x] 1.6 `readme_to_probe` gains `Docker` and `Compose` arms and drops the two-probe `Docker & Compose` arm; its
      comment describes the mapping's authority without claiming a row documents several probes.

## 2. The controls

- [x] 2.1 `install_cell_text` is exercised on fixtures in the CONTROLS group: a backticked cell, a whitespace-run cell,
      and the escaped-pipe marker, each asserting the normalized text, plus a changed word that must not normalize away.
- [x] 2.2 The live pass over the OpenCode Debian pair is the case that proves the marker survives the split: dropping
      the marker restore fails the run on that pair (measured, restored afterwards).

## 3. The reconciliation

- [x] 3.1 `README.md`: the `Docker & Compose` row becomes `Docker` and `Compose`, each with its own install column.
      `Docker`'s Debian cell is `sudo apt install docker.io`; `Compose`'s cells are the hints the doctor already printed
      — `Docker Desktop, or brew install docker-compose` on macOS and `sudo apt install docker-compose-v2` on Debian —
      and the `Needed for` cells say which concern each covers.
- [x] 3.2 `README.md`: the Python Debian cell becomes the doctor's sentence
      (`sudo apt install python3 on Ubuntu 24.04+, or a 3.12 build for your release`), since the column is an install
      command and the row's tool cell already carries the floor.
- [x] 3.3 `scripts/doctor.sh`: `debian:docker` drops `docker-compose-v2`, which is Compose's package and now Compose's
      row; every other hint is unchanged at this point, because it already agrees (3.6 later changes the macOS compose
      one).
- [x] 3.4 `README.md`: the Prerequisites paragraph names the install-text agreement beside the class assertion the check
      already performs.
- [x] 3.5 `README.md`: the sentence below the table names Compose in the required set ("Only **Java, Docker, and
      Compose** are required to run the application"), which the row split made due — the doctor declares `compose`
      required and the spec lists Compose v2 among the required prerequisites.
- [x] 3.6 Both surfaces: the macOS compose remedy names the step that makes the plugin discoverable —
      `Docker Desktop, or brew install docker-compose, symlinked to ~/.docker/cli-plugins/docker-compose` — because the
      formula's caveat asks for a `cliPluginsExtraDirs` entry instead, and this change chose the equivalent symlink
      route that needs no config edit (3.7 establishes it).
- [x] 3.7 The discovery route is established, not asserted: an isolated `DOCKER_CONFIG` fixture with a stub plugin that
      answers the `docker-cli-plugin-metadata` handshake, placed at `<config>/cli-plugins/docker-compose` and (in a
      second fixture) referenced from `cliPluginsExtraDirs`, prints `Docker Compose version v2.99.0` through both routes
      under Docker CLI 29.9.2; a stub without the metadata answer lists with `Err: invalid metadata`, and the host
      baseline prints its own v2.32.1.

## 4. The delta

- [x] 4.1 A `MODIFIED` block on "The local toolchain is diagnosed without the build", copied whole from the main spec,
      widens its "The check runs in the build and asserts the declaration" scenario to name the install-text agreement;
      all nine scenarios are preserved and `openspec validate` reports the change valid.

## 5. The records

- [x] 5.1 The unit's process-defect record (`docs/process-defects/verify-doctor-install-hints.md`): one observation, a
      claims-table row rebuilt from a naive pipe split and rejected twice.
- [x] 5.2 The parked idea is removed from `docs/ideas.md`.
- [x] 5.3 Docs refresh: `AGENTS.md` needs no edit — its Prerequisites bullet states the hint-is-a-claim rule and names
      `testDoctorCharter` without enumerating what it asserts — and `docs/adr/` carries no enumeration of the check's
      assertions or of the README's rows.

## 6. The evidence

- [x] 6.1 Reachability: with the check in place and the texts unreconciled, `bash scripts/test-doctor.sh` exited `1`,
      its first failing line naming `compose` on `debian` and both texts (the cell still carrying `docker-compose-v2`);
      after the reconciliation it exits `0`.
- [x] 6.2 The controls bite: the normalizer's, agreement's, and coverage's fixtures all assert, and one word changed in
      each of the three reconciled cells made the run fail naming the right probe and platform (README restored
      byte-identical).
- [x] 6.3 The coverage assertion bites: narrowing the cells extraction to skip one row made the run exit `1` on
      `probe 'compose' is declared readme=yes but the install-hint agreement never compared it`.
- [x] 6.4 `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` BUILD SUCCESSFUL; `spotlessCheck`, `verifyTableRows`
      clean; the 120-character recipe flags only the README's table rows and the `Workspace:` record;
      `openspec validate --all` 27 passed with the change item valid.

## 7. The review

- [x] 7.1 `review-quick` rounds recorded in `docs/review-rounds.tsv` with their session fields; the round
      `--expect-clean` accepts carries no class.
- [x] 7.2 Deep-review routing: `RECOMMENDED`, signal `repeated-class` (two ledger rows repeat a prior class — round 2's
      `test-control` and round 3's `claim-unreproducible`), derived with
      `scripts/review-routing.py --unit verify-doctor-install-hints --change verify-doctor-install-hints`.
- [x] 7.3 The deep review: **skipped**, on the owner's explicit decision after the advisory consultation recommended it
      (`Agreement: yes`) — the routing's `repeated-class` signal counts historical repeats whose instances are closed,
      the predicates carry discriminating fixtures, and the extraction path is covered by the row-loss, escaped-pipe,
      and per-pair perturbation controls. Accepted residual risks: the presentation normalizer, the extraction's
      unproven completeness, and that no check verifies a hint satisfies its probe.

## 8. The claims

- [x] 8.1 The pre-write derivation pass ran over the planning artifacts: its claims table is in `design.md`, each row
      derived from the command or read its Source cell names, with `scripts/claim-scan.py`'s candidates resolved beside
      it as one dismissal per kind. The before-state rows are anchored to `0a412be1`.
- [x] 8.2 Every row whose Result read `pending implementation` is re-derived: rows 9–11 state the measured failure,
      fixture and marker-restore outcomes, and rows 13–14 add the coverage and per-pair perturbation controls.

## 9. The archive

- [ ] 9.1 Confirm in the archive commit that the delta's block reached
      `openspec/specs/showcase/quality/toolchain-check/spec.md`, that no `## Purpose` edit is needed, and that the
      archived `tasks.md` carries the unit's `Workspace:` record.
