# Tasks

## 1. The pattern

- [x] 1.1 In `scripts/claim-scan.py`, extend `ABSOLUTE` with `no`, `nothing` and `the one`/`the sole`/`the single`
      outright.

- [x] 1.2 State the added forms in the tool's docstring, and keep every other kind unchanged.

## 2. The controls

- [x] 2.1 One control per added form beside the existing absolute case: `nothing`, `no … is`, `the one`, `the sole`, and
      `the single` each surface.
- [x] 2.2 A bare `no` (`no change yet`) surfaces, and a hyphenated compound (`no-op`) does too.
- [x] 2.3 The unchanged kinds and the tool's exit contract still hold.
- [x] 2.4 Mutate the pattern (drop `nothing`; drop `no`; drop the definite singular) and confirm 2.1 and 2.2 fail;
      restore.

## 3. The measurement

- [x] 3.1 Re-run the probe over `AGENTS.md` and record the two shares with their corpus and command in `design.md`'s
      Context.

## 4. The human-facing record

- [x] 4.1 Remove the implemented `docs/ideas.md` entry.

## 5. Verification

- [x] 5.1 Run the pre-write derivation pass and record its claims table (`design.md`), with `claim-scan.py`'s candidates
      resolved into rows or dismissals beside it.
- [x] 5.2 Run `openspec validate --all`, `./gradlew spotlessApply`, `./gradlew testClaimScan`, and the Docker-free
      `./gradlew check`, and record the results — **Done**: `openspec validate --all` 27 passed, 0 failed;
      `spotlessApply`/`spotlessCheck` green; `testClaimScan` 25 tests OK; the Docker-free `check` `BUILD SUCCESSFUL`.
- [x] 5.3 Write the unit's process-defect record.
- [x] 5.4 Run the review gate: the quick loop (five rounds on one reviewer session, round 5 clean) and the routing
      decision — `RECOMMENDED` (`[repeated-class]`), which the owner resolved to skip on the technical advisor's
      recommendation: the change is a widened regex with controls, the loop's restatement problem is removed, and the
      consultation found only artifact-accuracy corrections. The manual pass follows.
