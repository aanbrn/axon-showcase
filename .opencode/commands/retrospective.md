---
description: Run a sprint retrospective / experience analysis over a window of merged work
---

Run the experience-analyzer workflow to produce a retrospective and improvement suggestions from recent work.

1. Determine the window: use the argument after `/retrospective` as the `since` date if provided (e.g.
   `/retrospective 2026-09-02`), otherwise default to the last 7 days.
2. Gather the digest with `./scripts/experience-analysis.sh [since]` — this prints the merged PRs and the git log for
   the window, plus the archived OpenSpec changes, `AGENTS.md` gotchas, and `docs/ideas.md` as current state (not
   windowed), and a **derived-facts block** carrying the window's countable claims already resolved (the merged-PR
   boundary facts, the archived-change count, `AGENTS.md`'s metrics, the window's `captured:` markers, and the window's
   review rounds) — read the retrospective's countable claims from it rather than re-deriving them.
3. Invoke the `experience-analyzer` subagent (`.opencode/agent/experience-analyzer.md`) with that digest plus a short
   context note on the window (what went well/wrong that no diff captures).
4. Present the retrospective (shipped PRs by theme, lessons, went-well/went-wrong, the root cause and earliest
   prevention stage of each significant failure, and the previous retrospective's recommendations evaluated) and the
   improvement suggestions, each classified as `system` (→ docs/ideas.md or an OpenSpec proposal) or `process` (→
   AGENTS.md or a subagent definition) and stating the pattern it targets, the expected outcome, and how it would be
   verified.
5. Ask the user which suggestions to apply, and whether to store the retrospective under `docs/retrospectives/<date>.md`
   as a docs change. Do not edit any files without the user's go-ahead.
