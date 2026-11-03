# Design

## Context

`scripts/doctor.sh` prints, for every unsatisfied tool probe, the install command for the detected platform. The hint
lives in `install_hint()`, one `case` arm per `platform:tool` pair, and the README's Prerequisites table carries the
same guidance as two install columns (claims table rows 1 and 3). The capability spec already requires the two to agree:

> Its documented install guidance SHALL agree with the README's Prerequisites install column for each platform the
> README covers, so the two surfaces cannot give conflicting advice.

Nothing enforces it. `scripts/test-doctor.sh` asserts only that a hint exists for each declared tool and platform (row
1), so the texts have drifted while every gate stayed green: the `compose` row's cell carries Docker's command on both
platforms, and the `python3` Debian cell and hint are different sentences (row 4). The README is also the documented
list the check anchors its row mapping to, so the same file already arbitrates which probe a row documents.

Two properties of the two surfaces decide what an exact comparison must do. Seventeen of the twenty install cells carry
markdown backticks while the hints carry none, so a literal comparison fails on those before it reaches any real
disagreement (row 6). One cell — OpenCode's Debian command — carries an escaped pipe (`\|`), and the matching hint
carries a literal `|`, so a naive split on `|` cuts that row's cell in two (row 5).

## Goals / Non-Goals

**Goals.**

- A doctor hint that differs from its README cell, on any platform the README covers, fails `testDoctorCharter` and so
  `check`, naming the probe, the platform, both texts, and the row.
- A README install column or Prerequisites row the mapping does not know fails the check rather than being skipped.
- The two surfaces agree after the change, and the table can express a probe whose install differs per tool.

**Non-Goals.**

- Re-deciding any hint's content beyond making the two surfaces agree — with one exception the advisory consultation
  forced: the macOS compose remedy now names the step that makes the plugin discoverable (claims row 15), because this
  change moved that text into the README, where a column is a claim that following it satisfies the probe. No check
  still verifies that a hint works; only its text agreement.
- Comparing hints for probes the README does not document (`gh`, row 10): the requirement's subject is the documented
  guidance, and `gh` is covered in prose.
- Gating the doctor's own exit status, or adding the doctor to `check`.

## Decisions

**1. The README is the authority, and the check compares after stripping the cell's markdown.**

- Chosen: for every probe with `readme=yes`, for every install column, the doctor's hint for that platform must equal
  the row's cell with backticks removed, the escaped-pipe marker restored, and whitespace collapsed. This is the
  requirement's own shape — the diagnostic agrees _with the README's column_ — and it makes the cell the single text a
  contributor reads.
- Rejected — comparing only the first command token, or a substring: both accept a cell that adds, drops, or reorders
  advice, which is exactly the drift the requirement forbids.
- Rejected — normalizing the doctor's text instead: the doctor's output is the terminal guidance, so the README's
  presentation is what has to be stripped, not the tool's.

**2. The `Docker & Compose` row becomes two rows.**

- Chosen: `Docker` and `Compose`, each with its own install column. One row cannot carry two per-platform installs: on
  macOS Docker Desktop serves both while `brew install docker-compose` is Compose's own remedy once its plugin is made
  discoverable, and on Debian the two packages are separate. Splitting makes every row document exactly one probe, which
  is what an exact comparison needs.
- Rejected — comparing Compose against the combined row as a special case: the exception would be the only place the
  check reads a cell differently, and it would still leave Compose's macOS remedy undocumented.
- Rejected — declaring `compose` `readme=no`: a row names it, and the capability's "a probe cannot erase its own
  documentation" scenario forbids claiming it is undocumented while the table documents it.
- Consequence: `docker`'s Debian hint and cell lose `docker-compose-v2`, which is Compose's package and now Compose's
  row.

**3. The one text where the two disagreed and neither was canonical: the README follows the doctor.**

- Chosen: the Python Debian cell becomes the doctor's sentence
  (`sudo apt install python3 on Ubuntu 24.04+, or a 3.12 build for your release`). The column is an _install command_,
  and the row's Tool cell already carries the `3.12+` floor; the doctor's sentence leads with the command and keeps the
  fallback.
- Rejected — bending the doctor's hint into the cell's phrasing (`a 3.12+ python3 (…)`): a remedy line printed after
  `missing` should start with the command that fixes it.
- Every other pair already agrees and is left alone, which the check then proves by passing.

**4. The platform set and the row mapping stay declared in the test, and an unknown member fails.**

- Chosen: install columns map to the doctor's platform tokens through a `readme_platform` function beside the existing
  `readme_to_probe`, and a header cell with no mapping fails the check. The doctor's platform tokens are its own
  (`macos`, `debian`), so deriving one from the other's label would need the same table anyway.
- Rejected — hard-coding `macos debian` as the loop's list: the README's columns are the documented platform set, and a
  third column would then go unchecked exactly as the texts did.

**5. The comparison's two moving parts are controlled separately, and the live pass covers the third.**

- Chosen: the cell normalizer is a shell function exercised on fixtures (backticks, the escaped-pipe marker, whitespace
  runs), and the two predicates the comparison stands on — exact text equality and whole-key coverage — are functions
  driven on fixtures too, each of the weakenings they must reject (substring, prefix, case-insensitive, a key that is a
  prefix of another) pinned by its own case.
- Chosen: the live comparison's escaped-pipe row (OpenCode Debian) is the case that proves the marker survives the table
  split: it only passes if the pipe was restored.
- Chosen: coverage is asserted — every `readme=yes` probe must appear among the compared pairs, so a row the extraction
  stops seeing fails instead of shrinking the check silently.
- Rejected — a fixture table fed through the same awk program: the program is inline over the README, and extracting it
  to a function would restate the parsing the check reads.
- Reachability: with the assertion in place and the texts unreconciled, it fails on the three pairs of row 4; the
  reconciliation is what makes it pass.

## Risks

- **The check is only as good as the row mapping.** A new probe added with `readme=yes` and no row fails the existing
  coverage assertion, and a new row with no mapping fails it too, so the failure surfaces at the check rather than as a
  silently unchecked probe.
- **A new install column needs a mapping arm.** The check fails on the unmapped header, which is intended: the platform
  set is derived from the README, so a column cannot be documented without being compared.
- **Normalization could hide a real difference** — a cell whose only difference from the hint is backtick placement
  passes. That is presentation, not guidance; the risk is accepted and stated here rather than widening the comparison
  to markdown positions.
- **The compose hint's macOS branch names a second step.** `brew install docker-compose` alone leaves `docker compose`
  unable to find the plugin: the formula's caveat asks for a `cliPluginsExtraDirs` entry in `~/.docker/config.json`.
  Both surfaces instead name the equivalent route that needs no config edit — symlink the formula's binary to
  `~/.docker/cli-plugins/docker-compose` — which an isolated fixture establishes (claims row 17), together with the
  destination filename Docker's discovery requires. No check verifies that a hint satisfies its probe, so a later hint
  edit could still reintroduce an incomplete remedy without failing the agreement check; the agreement keeps the two
  surfaces equal, not correct.

## Claims

Base revision for the before-state rows: `0a412be1` — the unit's base when the rows were derived. The branch was
refreshed to `27dea0e8` (#825, commands, ADRs, contracts, and an archived change) before the closing review, and that
merge touched none of the files these rows cite, so every row still reproduces at `0a412be1`.

| #   | Claim                                                                                                       | Source                                                                                                                                                                                    | Result                                                                                                                                                                                                                                                                                                                                                                                                                      |
| --- | ----------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1   | The check asserts only that a hint exists, never what it says                                               | `sed -n '/Each declared .tool. probe must carry an install hint/,/^done/p' scripts/test-doctor.sh` at `0a412be1`                                                                          | one `grep -qE` per declared tool and platform against the doctor's source, failing only when no hint line matches at all                                                                                                                                                                                                                                                                                                    |
| 2   | The capability already requires the two surfaces to agree                                                   | `openspec/specs/showcase/quality/toolchain-check/spec.md`, "The diagnostic's guidance is platform-appropriate"                                                                            | "Its documented install guidance SHALL agree with the README's Prerequisites install column for each platform the README covers, so the two surfaces cannot give conflicting advice."                                                                                                                                                                                                                                       |
| 3   | The README documents ten rows in two install columns, and one row maps to two probes                        | the Prerequisites table plus `readme_to_probe` in `scripts/test-doctor.sh`                                                                                                                | 10 rows, 20 install cells; `"Docker & Compose") echo "docker compose"` is the only arm returning two probes; the other nine rows map one probe each                                                                                                                                                                                                                                                                         |
| 4   | Three (platform, probe) pairs disagree today                                                                | a comparison of every mapped pair at `0a412be1`                                                                                                                                           | `macos:compose` — hint `Docker Desktop, or brew install docker-compose`, cell `Docker Desktop, or brew install colima docker`; `debian:compose` — hint `sudo apt install docker-compose-v2`, cell `sudo apt install docker.io docker-compose-v2`; `debian:python3` — hint `sudo apt install python3 on Ubuntu 24.04+, or a 3.12 build for your release`, cell `a 3.12+ python3 (sudo apt install python3 on Ubuntu 24.04+)` |
| 5   | One cell escapes a pipe its hint carries literally                                                          | the OpenCode Debian cell and `install_hint opencode` under `platform=debian`                                                                                                              | the cell escapes the install script's pipe as a backslash-pipe while the hint prints the bare pipe, so a split on an unprotected pipe cuts the cell                                                                                                                                                                                                                                                                         |
| 6   | Most cells carry markdown backticks that the hints do not                                                   | a count over the README's install cells                                                                                                                                                   | 17 of 20 cells carry a backtick; no hint does, so a literal comparison fails before reaching a real disagreement                                                                                                                                                                                                                                                                                                            |
| 7   | The check runs in the build                                                                                 | `build.gradle.kts`                                                                                                                                                                        | `testDoctorCharter` executes `scripts/test-doctor.sh` and the `check` block depends on it                                                                                                                                                                                                                                                                                                                                   |
| 8   | The doctor has twelve tool probes, eleven documented and one (`gh`) documented only in prose                | the probe table and `readme_to_probe` at `0a412be1`                                                                                                                                       | 12 `tool` rows; `gh` is `readme=no`, so eleven probes are compared and `gh`'s hints are outside the comparison's subject                                                                                                                                                                                                                                                                                                    |
| 9   | The assertion fails on the three drifted pairs before the reconciliation                                    | the new check run on the unreconciled tree                                                                                                                                                | `bash scripts/test-doctor.sh` exits `1` on `the doctor's install hint for 'compose' on debian is 'sudo apt install docker-compose-v2', but the README row 'Docker & Compose' carries 'sudo apt install docker.io docker-compose-v2'` — the first of row 4's three drifts, so each reconciliation exposed the next                                                                                                           |
| 10  | The normalizer's fixtures pass, and every weakening the two predicates must reject is pinned by a case      | the check's three control fixture sets: `expect_cell_text`, `expect_hint_agreement`, and `expect_coverage`                                                                                | the suite passes with the normalizer's three fixtures and the agreement and coverage cases in place; weakening `hint_agrees` to a substring match fails on the repaired drift pair, and the case-only, changed-word, empty-hint, prefix-key, and case-only-coverage cases each pin a weakening its own predicate must not accept                                                                                            |
| 11  | The escaped-pipe row passes only once the marker is restored                                                | the new check's live pass over the OpenCode Debian pair                                                                                                                                   | with the marker restore dropped from `install_cell_text`, the run exits `1` on that pair — whose cell and hint both carry the install script's pipe — so it is the case that proves the restore                                                                                                                                                                                                                             |
| 12  | The README states what the check keeps in step                                                              | `README.md`, the Prerequisites paragraph                                                                                                                                                  | "the doctor is the mechanical check of it (`scripts/test-doctor.sh` keeps the two in step and asserts each probe's required/optional class against the spec's build-and-test path)" — the sentence needs the install-text half                                                                                                                                                                                              |
| 13  | The coverage assertion bites                                                                                | the cells extraction narrowed to skip one row, then restored                                                                                                                              | the run exits `1` on `probe 'compose' is declared readme=yes but the install-hint agreement never compared it`; restored, it exits `0`                                                                                                                                                                                                                                                                                      |
| 14  | Each reconciled pair is the one the check compares, not a neighbouring pair                                 | one word changed in each of the three reconciled cells, then restored                                                                                                                     | each run exits `1` naming the right probe and platform (`compose`/macos, `compose`/debian, `python3`/debian); the README restored byte-identical                                                                                                                                                                                                                                                                            |
| 15  | The macOS compose formula needs a step beyond the install                                                   | `brew info docker-compose` (5.7.0), its caveats                                                                                                                                           | "Compose is a Docker plugin. For Docker to find the plugin, add \"cliPluginsExtraDirs\" to ~/.docker/config.json" — so `brew install docker-compose` alone does not satisfy `docker compose version`                                                                                                                                                                                                                        |
| 16  | Both surfaces now carry that step, and the agreement accepts them                                           | `bash scripts/test-doctor.sh` after the wording change, and the `compose`/macos pair                                                                                                      | the suite exits `0`, so the hint and the README cell are equal after normalization                                                                                                                                                                                                                                                                                                                                          |
| 17  | The plugin-discovery route both surfaces name is established, with the destination filename Docker requires | an isolated `DOCKER_CONFIG` fixture holding a stub plugin that answers the `docker-cli-plugin-metadata` handshake, at `<config>/cli-plugins/docker-compose` and via `cliPluginsExtraDirs` | both routes print `Docker Compose version v2.99.0` under Docker CLI 29.9.2; a stub without the metadata answer lists with `Err: invalid metadata`, and the host baseline prints its own v2.32.1                                                                                                                                                                                                                             |

Dismissals, one reason per candidate kind `scripts/claim-scan.py` reports:

- `[count]` — a numeral labels a row, a probe, a cell, or a task; the corpus figures are rows 3, 6, and 8, each derived
  from the artifact its Source cell names (the Prerequisites table, a count over its install cells, and the probe table
  with its mapping).
- `[absolute]` — a universal or absence qualifier is normative text (a requirement, a decision, a scenario) or a rule's
  own wording; the factual instances are the claims rows.
- `[cross-ref]` — the candidates are slash-joined identifiers and the paths of files the change reads or edits; the
  change cites no tracker reference.
- `[path]` — every candidate is a code span carrying a command, a path the change reads or edits, or a marker the check
  uses; each path exists in the tree.
