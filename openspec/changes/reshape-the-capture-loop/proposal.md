## Why

The capture convention runs the `lesson-capture` subagent after every merge, including the merge of a capture's own docs
PR — so a capture can chain into another capture indefinitely, its only bound being the arbiter's "nothing durable"
verdict. A four-round chain ended only when the owner capped it by hand: each round found something, and the later
rounds found rules about the capture process itself, which is exactly the accretion the `agents-auditor` exists to
catch.

## What Changes

- The trigger becomes: one capture after a change's implementation, and one more after the merge — but only when the
  merged PR was not itself a capture.
- A capture PR's merge triggers no further capture. Instead: read the capture's diff, report any candidate lesson with
  the bullet it would extend, and ask the owner for explicit confirmation before running another.
- The README's self-learning description matches the trigger.

## New Capabilities

None.

## Modified Capabilities

None — no spec requirement describes the merge trigger; the `agent-skills` scenario is the implementation capture.

## Impact

`AGENTS.md` (the capture bullet's trigger and the "merge itself is the trigger" sentence) and `README.md` (the
self-learning loop's description). No code or definition change.
