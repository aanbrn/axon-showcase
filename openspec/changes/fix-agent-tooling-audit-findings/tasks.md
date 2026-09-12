## 1. review-quick subagent

- [x] 1.1 Replace the `awk 'length > 120'` instruction in `.opencode/agent/review-quick.md` with the canonical
      `perl -CSD -lne 'print if length > 120'` recipe
- [x] 1.2 Widen the description and body to cover the proposal pass (planning-artifact coherence) and the non-OpenSpec
      diff (no change dir), not only an implementation

## 2. AGENTS.md

- [x] 2.1 Drop the model-pin "six places" count (the list holds seven; a frozen tally drifts) — state the shape
- [x] 2.2 Collapse the duplicated Snyk policy/rate-limit text to a pointer at the `.snyk` header and the
      `/dependency-security-check` command
- [x] 2.3 Drop the point-in-time Snyk expiry dates and the checkstyle-version note from `AGENTS.md`
- [x] 2.4 Shorten the Build & Test Docker-free comment to reference the coverage-gate gotcha instead of restating its
      mechanism

## 3. Verification

- [x] 3.1 Run `openspec validate --all` and `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` (the PR gate)
- [x] 3.2 Confirm no `awk 'length > 120'` remains under `.opencode/`, no frozen Snyk date or checkstyle-version note
      remains in `AGENTS.md`, and the model-pin tally is gone
