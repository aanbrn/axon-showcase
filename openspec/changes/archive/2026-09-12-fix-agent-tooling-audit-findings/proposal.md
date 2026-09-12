## Why

The widened `agents-auditor` (from `widen-agents-auditor-to-tooling`) smoke-ran over the project-owned agent tooling and
found six verified defects. Each is an instance of a convention the repo already states — so these are fixes, not new
rules:

1. `.opencode/agent/review-quick.md` instructs `awk 'length > 120'` — the exact command `AGENTS.md` forbids (it counts
   bytes and false-flags non-ASCII), so the canonical quick-review pass has been propagating the bug the formatting
   gotcha records.
2. `AGENTS.md`'s model-pin gotcha says the flash model is pinned in "six places" and then lists seven.
3. `AGENTS.md` restates the Snyk policy and rate-limit text that `.opencode/commands/dependency-security-check.md`
   already carries in full — and still points at it.
4. `AGENTS.md` embeds point-in-time Snyk expiry dates and a checkstyle-version note; those facts live in `.snyk` and
   drift (the point-in-time-vs-durable-count doctrine).
5. The Docker-free-check recipe's full mechanism is stated in both the Build & Test comment and the coverage-gate
   gotcha.
6. `.opencode/agent/review-quick.md`'s description omits its proposal-review role, which the auto-review convention (and
   its own body) have it perform; it also still assumes a change dir, though the review gate is explicitly not
   OpenSpec-specific (it also runs over a docs-refresh or standalone-fix diff).

## What Changes

- **`.opencode/agent/review-quick.md`** — replace the `awk 'length > 120'` instruction with the canonical
  `perl -CSD -lne 'print if length > 120'` recipe; widen the description and body to cover the proposal pass (planning
  artifacts) and the non-OpenSpec diff (no change dir), not only an implementation.
- **`AGENTS.md`** — drop the model-pin "six places" count (state the shape, not a tally that drifts); collapse the
  duplicated Snyk text to a pointer at the `.snyk` header and the `/dependency-security-check` command; drop the
  point-in-time expiry dates and the checkstyle-version note; shorten the Build & Test Docker-free comment to reference
  the coverage-gate gotcha rather than restating its mechanism.

## Capabilities

### New Capabilities

- none

### Modified Capabilities

- none — a `skip_specs` change: it edits agent-tooling and guidance text, not a spec requirement.

## Impact

- `.opencode/agent/review-quick.md` — the 120-check command and the description/body.
- `AGENTS.md` — the model-pin count, the Snyk block, and the Docker-free comment.
- No spec, code, build, or deployment change. Still outstanding after this change: none of the audit's findings remain.
