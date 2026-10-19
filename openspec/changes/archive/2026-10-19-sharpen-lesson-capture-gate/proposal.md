# The capture's gate compares the decision, not the wording

## Why

`add-extraction-procedure`'s capture proposed two candidates the promotion gate should have stopped. One added a
claim-sourcing clause whose action the existing source-derivation rule already required; the other inferred a shared
drafting defect from a repeated review-class label and prescribed a whole-section rewrite. The calling agent reassessed
both and left the definition unchanged — which is the gap: the gate requires a before/after pair but never requires
naming the existing obligation's condition, never says different phrasing is not novelty, never separates a class's
recurrence from its cause, and never bounds the remedy. Restatements and cause-inferred rewrites therefore reach the
caller, who absorbs the rejection.

## What Changes

- `.opencode/agent/lesson-capture.md` — the gate compares the existing obligation, its explicit condition, and the
  different action; a candidate whose action is unchanged is `already covered`.
- `.opencode/agent/lesson-capture.md` — evidence-only candidates are `already covered`, and their incident evidence
  stays in the change record rather than in the rule's text.
- `.opencode/agent/lesson-capture.md` — a repeated review-class label establishes that label's recurrence, not a shared
  drafting defect, and a remedy resting on a class's recurrence requires the shared cause confirmed from the evidence.
- `.opencode/agent/lesson-capture.md` — the remedy's action and scope are derived from verified evidence, and a
  replacement or rewrite preserves every valid obligation, warning, exception, and identifier.
- `.opencode/agent/lesson-capture.md` — the inputs name the existing guidance and review evidence a candidate rests on,
  and an unsupported candidate is withheld with the limitation stated.
- `AGENTS.md` — the capture workflow's step 2 passes that evidence to the subagent; no gate copy is added there.
- `showcase/quality/agent-skills` — the "Per-change quality-gate and analysis subagents are available" requirement
  carries the same outcomes in a `MODIFIED` block.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/agent-skills` — the capture gate in the "Lessons are captured after implementation" scenario gains
  the decision comparison, the observation/cause separation, the remedy bound, and the supplied-evidence limitation; no
  new requirement.

## Impact

- Files: `.opencode/agent/lesson-capture.md`, `AGENTS.md` (one step-2 clause), and the change dir, with one `MODIFIED`
  requirement in `showcase/quality/agent-skills`.
- No code, build, or deployment impact: subagent instructions, one caller-side procedure clause, and one delta
  requirement.
