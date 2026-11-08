# Check the quick reviewer's hand-over section before the delegation

## Why

The quick-review preflight requires each invocation's hand-over section, but nothing checks that it exists:
`surface-projector-backlog` recorded five consecutive rounds without a section, and the last was caught only by the next
reviewer. `commit-hygiene.py --agent-sessions` resolves the slug of a **registered** invocation's section, while quick
reviews are excluded from that register, so the section the quick loop depends on has no mechanical check.

## What Changes

- `scripts/commit-hygiene.py` — a `--handover <unit>:<round>` mode: a second-level `Round <N>` heading must exist in the
  unit's `review-handover.md`, and a malformed argument or an unreadable file exits non-zero.
- `scripts/test-commit-hygiene.py` — a known-bad and a known-good case per predicate, the mode's entry in the
  mode-coverage mapping, and the parameterized-mode control the existing mode iteration needs.
- `.opencode/skills/review-gate/SKILL.md` §2 — the mode is invoked after the hand-over section is written and before
  each quick-review delegation, its failure stops the call, and the accepted heading form is stated so the writer and
  the predicate share one convention.
- `showcase/quality/commit-hygiene` (delta) — the check's requirement, its predicate, and what it does not establish.
- `README.md` — the review paragraph names the pre-delegation check.
- `docs/ideas.md` — the implemented idea is removed.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/commit-hygiene` — the quick reviewer's hand-over section is checked for presence before the
  delegation, and the check states that it establishes presence only.

## Impact

- **Scripts and tests**: the checker's new mode and its cases; no build task, no pre-commit rule, and no register-schema
  change.
- **Documents and specs**: the review-gate skill's preflight, the `commit-hygiene` delta, the README, and the ideas
  entry.
- **Behavior**: a quick-review delegation stops when the round's section is absent; copied content beneath a matching
  current-round heading, or a heading written after the call, still passes, which the requirement states.
