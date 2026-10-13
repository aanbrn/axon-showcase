# Proposal

## Why

The review loop's **pre-write derivation pass** — deriving the four claim classes the loop keeps catching (`AGENTS.md`'s
review-loop bullet names them) before the first review round — is stated only as prose in that bullet. A judgment-sized
obligation stated only as prose leaves no trace when it is skipped: a skipped pass looks exactly like a pass that found
nothing, so the rounds it exists to save are spent anyway. The four review-cost changes (#560–#563) each needed several
rounds to converge.

The repository already has the working pattern. `make-the-capture-visible` made the per-unit capture a **ticked task**,
so a skip is a _missing box_ rather than a recollection. S1 mirrors it for the pre-write pass.

## What Changes

- **`openspec/config.yaml`** — a `tasks` rule so every change's `tasks.md` carries the pre-write derivation pass as a
  visible, tickable task, pointing at the bullet that names its four claim classes rather than restating them.
- **`.github/PULL_REQUEST_TEMPLATE.md`** — a checklist line recording the pass, so a unit with no change dir (a docs
  refresh, a standalone fix, a dependency bump) carries the record too (the `gh pr create --body-file` convention means
  the line reaches the body through the `AGENTS.md` obligation, with the template fixing its place).
- **`AGENTS.md`** — the review-loop bullet states the record: the pass is a task the unit carries, and a missing box is
  a skip rather than a pass that found nothing.
- **`showcase/quality/agent-skills`** (delta) — the quick-review requirement gains the record obligation and a scenario
  making a skipped pass observable.
- **`README.md`** — the self-learning bullet notes the visible record.
- **`docs/retrospectives/2026-10-13.md`** — S1's row records this change as applied. The table's columns are
  `# / Suggestion / Class / Target` (no Disposition column, unlike `2026-09-28.md`'s), so the record is an annotation on
  the S1 row rather than a cell fill.
- **`.opencode/agent/review-quick.md`** — assessed, no edit: the pass is the main agent's, run before the review, and
  the subagent cannot observe its own skip.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/agent-skills`: the "Per-change quality-gate and analysis subagents are available" requirement's
  quick-review scenario gains the record obligation, and a scenario makes a skipped pass a missing record.

## Impact

- Files: `openspec/config.yaml` (one `tasks` rule, written to parse as a single string),
  `.github/PULL_REQUEST_TEMPLATE.md` (one checklist line), `AGENTS.md` (the review-loop bullet), the
  `showcase/quality/agent-skills` delta, `README.md`, and `docs/retrospectives/2026-10-13.md`.
- No code, build, test, runtime, or deployment change: a process record, its spec scenario, and the docs that describe
  it.
