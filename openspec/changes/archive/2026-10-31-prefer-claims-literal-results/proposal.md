# prefer-claims-literal-results

## Why

The retrospective's S2: `claim-unreproducible` is 275 of 1,399 misses (20%) in the window, and the new `--verify` mode
executed 6 of 44 historical rows. `review-gate` §1 states only a conditional — a row can be executed — and no authoring
preference, so a new row defaults to prose. A row whose result is an output can state the literal the command prints,
which the mode can compare. The preference is the retrospective's hypothesis: the coverage forecast is unestablished,
since the skips also reflect two-column tables, unsupported commands, ambiguous pipes, and working-tree measurements.

## What Changes

- `.opencode/skills/review-gate/SKILL.md` §1 — a preference beside the `--verify` bullet: a row whose result is an
  output states the expected literal or literal list where the complete assertion and the supported command fit the
  mode; an explanation stays outside the result cell; evidence is never weakened to obtain a checked row.

## New Capabilities

None.

## Modified Capabilities

None. The spec's quick-review requirement names the claims table as the `review-gate` skill's; it does not state the
row's result form. `skip_specs` records that.

## Impact

- The review-gate skill, and `docs/retrospectives/2026-10-31.md` (the S2 block gains its disposition). No code, build,
  test, or deployment change.
