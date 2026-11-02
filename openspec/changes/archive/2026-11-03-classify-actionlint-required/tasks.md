# Tasks

Workspace:
`unit=classify-actionlint-required; path=/Users/afanasyev/Development/axon-showcase-classify-actionlint-required; branch=classify-actionlint-required; writer=ses_e653ab5caffeGjp1myeRL03Emh`

## 1. The declaration

- [x] 1.1 In `scripts/doctor.sh`, change the `actionlint` row's class from `optional` to `required`, leaving its kind,
      readme flag, binary, version arguments, and empty minimum version as they are — so the only behavioural change is
      the default run's exit code when the tool is absent.
- [x] 1.2 Confirm the declaration is still well-formed through the doctor's own validator (`sh scripts/doctor.sh` runs
      `validate_probe_table` before reporting) and that `dash -n scripts/doctor.sh` is clean, since the file is strict
      POSIX `sh`; both pass, and the doctor reports 14 probes with the required set now naming `actionlint` first.

## 2. The control

- [x] 2.1 `scripts/test-doctor.sh`: `expected_required` gains `actionlint` in its alphabetical position
      (`actionlint compose docker docker-daemon java python3`), and the comment's quotation of the required-tool
      scenario gains `actionlint`, since it quotes the scenario verbatim.
- [x] 2.2 `scripts/test-doctor.sh`'s below-floor fixture gains an `actionlint` stub printing a version, so the fixture
      satisfies every required probe on a host without the tool and its exit status stays attributable to Python alone;
      the fixture's own comment stays true.
- [x] 2.3 The fixture asserts that invariant instead of stating it: at the interpreter's floor, every key in
      `expected_required` must report `satisfied` in the fixture's own output, checked before the exit-status verdict,
      so a future class change fails naming the unstubbed probe. Measured: with the `actionlint` stub removed the run
      exits `1` on `fixture leaves required probe 'actionlint' unsatisfied at the floor`, so the verdict names the
      fixture rather than Python; restored, it exits `0`.

## 3. The delta

- [x] 3.1 The delta's two `MODIFIED` blocks are copied whole from
      `openspec/specs/showcase/quality/toolchain-check/spec.md` — "The diagnostic distinguishes required from optional
      prerequisites" and "The diagnostic reports repo-state prerequisites" — with `actionlint` added to the required
      list in the required-tool scenario and in the Git-hooks scenario; every other scenario is preserved in its order.
      `openspec validate classify-actionlint-required` reports the change valid.

## 4. The records

- [x] 4.1 The unit's process-defect record (`docs/process-defects/classify-actionlint-required.md`): one observation,
      that a pipeline's exit status was read as the control's verdict.
- [x] 4.2 The parked idea is removed from `docs/ideas.md`.
- [x] 4.3 Docs refresh: confirmed `AGENTS.md` and `README.md` need no edit, and swept `docs/adr/`, the spec corpus, and
      `.opencode/` for an enumeration of the doctor's required set — only `scripts/test-doctor.sh` holds it. The
      README's "Only Java and Docker are required to run the application" is scoped to running and its next sentence
      already places `actionlint` in the full `check`, so it is not a contradiction.
- [x] 4.4 The capability's `## Purpose` needs no refresh (it describes the probe set generically and `actionlint` was
      already a probe); record the confirmation in the archive commit rather than editing the Purpose. Confirmed: the
      sync changed only the two scenario lines and left the Purpose untouched.

## 5. The evidence

- [x] 5.1 Before/after measured under one `PATH` stub that provides `java`, `docker`, and `python3` with a reachable
      daemon but no `actionlint`: before, the doctor exits `0`, reports `actionlint missing` in the optional block, and
      prints `Result: every required prerequisite is satisfied.`; after, it exits `1` with the same report line in the
      required block and `Result: at least one required prerequisite is unsatisfied (see above).`
- [x] 5.2 The control bites: with the class flipped back to `optional`, `bash scripts/test-doctor.sh` exits `1` with
      `not 'actionlint compose docker docker-daemon java python3'` in the failure; restored, it exits `0`.
- [x] 5.3 `bash scripts/test-doctor.sh` exits `0` under a `PATH` that lacks `actionlint`; with the fixture's stub
      removed the same run exits `1` on `fixture leaves required probe 'actionlint' unsatisfied at the floor`, so the
      stub — not the host's toolchain — carries the below-floor verdict and the failure names the fixture.
- [x] 5.4 `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` BUILD SUCCESSFUL, `spotlessCheck` clean, and
      `openspec validate --all` 27 passed with the change item valid and no warning attributed to it. The manual
      120-character recipe over the changed set flags only `design.md`'s claims-table rows and `tasks.md`'s `Workspace:`
      record, each one line by construction.

## 6. The review

- [x] 6.1 `review-quick` rounds recorded in `docs/review-rounds.tsv`: round 1 raised two `claim-unreproducible`, one
      `test-control`, and one `formatting`; round 2 raised one `claim-unreproducible` and one `formatting`; both rounds'
      findings were applied. The round `--expect-clean` accepts is recorded with no class.
- [x] 6.2 Deep-review routing: `NOT_NEEDED`, with no signal matched — no changed path in the contract, domain,
      persistence, or infrastructure/deployment class, no added requirement block, and one ledger row repeating a class.
      Derived with
      `scripts/review-routing.py --unit classify-actionlint-required --change classify-actionlint-required`.
- [x] 6.3 The deep review does not run: a `NOT_NEEDED` outcome creates no deep-review session and spends no
      expensive-model invocation.

## 7. The claims

- [x] 7.1 The pre-write derivation pass ran over the planning artifacts: its claims table is in `design.md`, each row
      derived from the command or read its Source cell names, with `scripts/claim-scan.py`'s candidates resolved beside
      it as one dismissal per kind. The before-state rows are anchored to `ee7bdc58`.
- [x] 7.2 Every row whose Result read `pending implementation` is re-derived from the implementation: row 11 states the
      suite's exit status with and without the fixture's stub, and row 16 states the doctor's, the control's, and the
      reverted class's verdicts.

## 8. The archive

- [x] 8.1 Confirmed in the archive commit: the sync applied `~ 2 modified` to
      `openspec/specs/showcase/quality/toolchain-check/spec.md`, adding `actionlint` to the required list at lines 97
      and 129 and changing nothing else (the Purpose included), and the archived `tasks.md` carries the unit's
      `Workspace:` record.
