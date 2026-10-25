# Proposal

## Why

The `ship-change` skill's ban on closing a long-lived tracker sits only inside its "Complete a cloud (`/oc`) agent pull
request" section, so a locally-opened pull request reads it only as part of a procedure about a different path. The
hazard is not cloud-specific: any pull request can carry a `Closes #NNN` line, and the weekly workflows look their
tracker up with `is:issue is:open`, opening a duplicate once it has closed. The technical advisor's review of the
placement confirmed the defect is where the rule lives, not whether the rule should exist — and that no post-merge check
is owed.

## What Changes

- **`.opencode/skills/ship-change/SKILL.md`** — move the branch into "Ship a change"'s `### Decision points`, scoped by
  whether the tracker outlives the pull request: a long-lived tracker must not close, while a one-shot work item's issue
  closes as intended.
- **`.opencode/skills/ship-change/SKILL.md`**, cloud section — keep only the provenance: the action writes the closing
  reference into the pull request it opens, and that decision is the shared branch's, since the tracker's lifetime
  decides it rather than the pull request's origin.
- **`.opencode/skills/ship-change/SKILL.md`**, completion criteria — "Ship a change"'s criteria gains the tracker
  outcome, because the procedure that makes the decision owns its check, and the cloud procedure's criterion points at
  it instead of restating it.

- **`AGENTS.md`** — the implementation capture's applied delta: the capture workflow gains the placement rule (a rule
  lives under the trigger that reaches every case it governs, and a moved rule takes its completion check with it), and
  the cross-reference this move made stale is retargeted. The growth is +5 net lines after the extraction clause's
  placement sentence becomes a pointer in the same change. The capture's other proposal, a review-gate check for a
  guidance edit, is declined: one occurrence, no severe incident, so the promotion gate's threshold is unmet.
- No new check on the tracker itself: the rule already exists, and only its placement was wrong.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None: `skip_specs`. `showcase/quality/agent-skills` requires each shipping procedure to live in the skill and to carry
its own decision branches, and it states neither which procedure holds this branch nor the tracker rule itself — a grep
of `openspec/specs/` for `is:issue` and `closing keyword` returns no match. Moving the branch between two procedures
inside that skill changes no scenario's outcome.

## Impact

- **An agent shipping a locally-opened pull request** now reads the tracker branch instead of only the cloud procedure
  reading it.
- **The cloud procedure** keeps the fact that the action writes the closing reference, and delegates the decision.
- **No runtime behavior and no gate**: the change is skill guidance, verified by the formatter, `openspec validate`, and
  the `build` gate's definition-inventory probe.
