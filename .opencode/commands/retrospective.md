---
description: Run a sprint retrospective / experience analysis over a window of merged work
---

Run the experience-analyzer workflow to produce a retrospective and improvement suggestions from recent work.

**Trigger** — the owner invokes `/retrospective`, optionally with a window.

**Procedure**

1. Determine the window: use the argument after `/retrospective` as the `since` date if provided (e.g.
   `/retrospective 2026-09-02`), otherwise default to the last 7 days.
2. Gather the digest with `./scripts/experience-analysis.sh [since]`. It prints the merged PRs and the git log for the
   window, the archived OpenSpec changes, `AGENTS.md`'s gotchas, and `docs/ideas.md` as current state (not windowed),
   plus a **derived-facts block** carrying the window's countable claims already resolved.
3. Invoke the `experience-analyzer` subagent (`.opencode/agent/experience-analyzer.md`) with that digest, plus a short
   context note on the window if the owner has one — what went well or wrong that no diff captures. The note is
   optional.
4. Present the retrospective and the improvement suggestions, each classified as `system` (→ `docs/ideas.md` or an
   OpenSpec proposal) or `process` (→ `AGENTS.md` or a subagent definition) and stating the pattern it targets, the
   expected outcome, and how it would be verified.
5. Ask the owner which suggestions to apply, and whether to store the retrospective under
   `docs/retrospectives/<date>.md`.

**Decision points**

- IF an explicit `since` argument is not a valid `YYYY-MM-DD` date, THEN reject it and ask for a valid one before
  invoking the digest — do not pass it through to the script.
- Read the retrospective's countable claims from the digest's derived-facts block rather than re-deriving them; it is
  the authority for them.
- A suggestion is applied only on the owner's selection — never automatically.

**Failure handling** — a digest that cannot be gathered is reported; the subagent is not invoked without one.

**Verification** — each applied suggestion's stated expected outcome.

**Completion criteria** — the owner has the retrospective and the suggestions, has chosen which to apply, and has
decided whether to store it.

**Repository lifecycle**

- The unit starts on its own branch, created from a freshly fetched `origin/main`.
- Unrelated dirty-worktree changes are preserved.
- The work stays uncommitted until the manual approval pass.
- Nothing is committed, pushed, turned into a pull request, archived, or merged without that explicit approval.
- The full per-unit review gate runs over the result. `AGENTS.md`'s "Workflow: Change lifecycle" and "Workflow: The
  review gate" own the branch, uncommitted-work, approval, and review steps, including the reviewer-session preflight;
  this command points at them rather than restating them.
