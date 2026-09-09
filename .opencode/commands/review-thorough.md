---
description: Run a thorough review of a change against its specs, tasks, and surrounding code
---

Run the review-thorough workflow to do a deep review pass of a change.

1. Determine the change to review: use the argument after `/review-thorough` as the change name if provided (e.g.
   `/review-thorough add-actionlint-gate`), otherwise use the current active change (openspec list) or the most recent
   uncommitted work if none is active.
2. Gather the change's planning artifacts (proposal, design, tasks, delta specs) and the implementation diff.
3. Invoke the `review-thorough` subagent (`.opencode/agent/review-thorough.md`) with those artifacts.
4. Present its findings grouped by severity (blocking / should-fix / nitpick), each with a file/line reference.
5. Ask the user which findings to fix. Do not edit any files without the user's go-ahead.