# Proposal

## Why

`scripts/test-doctor.sh` cannot verify the classification it exists to check. It rebuilds the doctor's probe list by
**regex over `scripts/doctor.sh`** and asserts things about the names that pattern matched, so a probe it misses passes
silently. Reproduced: flipping `git-hooks` back to `required` (the mis-classification that shipped in #455, fixed by
#460) leaves the suite green.

Two attempts to fix this were **abandoned after review** (five and seven rounds). Both declared the probe set as data
and asserted things _about the data_, and each round found another way a probe could slip through — the last being a
`kind` flipped from `tool` to `state`, which made the probe **vanish from the doctor's output while the check still
reported "13 probes"**. The lesson is structural: a content assertion cannot see a probe that never reaches the code,
and a check that never runs the doctor cannot see what it reports. Both attempts' evidence is in the design's Context.

## What Changes

- `scripts/doctor.sh`: declare its probes as one **table with a single owner** (name, class, kind, and the tool facts
  each probe needs), drive **both** probe forms from it, and validate the table's **shape** at load — a malformed row
  fails loudly rather than making a probe invisible.
- `scripts/test-doctor.sh`: read the table (no regex over the doctor's text) and assert **three things**:
  1. **Shape** — the declaration is well-formed and unambiguous (its columns, key uniqueness, and the values each column
     allows), validated by the same rules the doctor enforces, so the two cannot disagree.
  2. **Behaviour** — every declared probe is **actually reported** by running the doctor and matching its output. A
     probe that never reaches the code is detected here; this is what the abandoned attempts lacked.
  3. **Agreement** — the declared set matches the README's Prerequisites table and the spec's required set (the two
     assertions the current check keeps, plus the class assertion it lacks today).
- Gate the check in `./gradlew check`. It parses files and **runs the doctor against the local host**, whose tool
  presence varies — but the doctor reports every probe _regardless_ of what is installed (a missing tool is a status,
  not an omission), so the behavioural assertion holds on any host and needs no Docker, network, or prerequisite.
- `docs/ideas.md`: the parked idea this implements is removed — reframed, since the answer is declaration plus a
  behavioural check rather than derivation.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/toolchain-check`: the diagnostic's probes become a declared set with a single owner, validated for
  shape where the table is owned, and asserted by a check that verifies both the declaration and what the diagnostic
  actually reports — currently the probe set is implied by source text and asserted by a script nothing runs.

## Impact

- Edited files: `scripts/doctor.sh` (probe table, shape validation, table-driven probes), `scripts/test-doctor.sh` (read
  the table; shape, behavioural, and agreement assertions), `build.gradle.kts` (a `check` member running the check),
  `AGENTS.md` (the doctor bullet and the check-members enumeration), `README.md` (the check's description), and the
  change's delta spec.
- `build.gradle.kts` gains a check; per the repo's rule that costs a one-time full rebuild on the next CI run (the
  build-config hash changes), which the change's report should note.
- The doctor's output, exit codes, and platform behavior do not change: the table carries the same names, classes, and
  facts the call sites carry today. Verified for the abandoned attempt's rebuild (byte-identical output) and a task here
  requires re-confirming it.
