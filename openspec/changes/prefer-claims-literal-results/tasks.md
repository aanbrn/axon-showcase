# Tasks — prefer-claims-literal-results

Workspace: unit=prefer-claims-literal-results; path=/Users/afanasyev/Development/axon-showcase;
branch=prefer-claims-literal-results; writer=ses_e731f42aaffeKQAMVYfJ2KxAYN

## 1. The preference

- [x] 1.1 Add the bullet to `.opencode/skills/review-gate/SKILL.md` §1, beside the `--verify` pointer: a row whose
      result is an output states the expected literal or literal list where the complete assertion and the supported
      command fit the mode; the explanation stays in the Claim cell; evidence is never weakened to obtain a checked row;
      a prose Result stays valid and advisory; the `captured: retrospective-2026-10-31` marker.

## 2. Verification

- [x] 2.1 `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`, `openspec validate --all`, and
      `spotlessApply`/`spotlessCheck`.

## 3. Review and records

- [x] 3.1 Record the pre-write derivation pass and its claims table in `design.md`.
- [x] 3.2 Run the quick-review loop and record each round's row in `docs/review-rounds.tsv`.
- [x] 3.3 Record the deep-review routing outcome on the change's review task. Outcome: `NOT_NEEDED`.
- [x] 3.4 Write the unit's process-defect record, or state that none was observed. Outcome: none observed —
      `docs/process-defects/prefer-claims-literal-results.md`.

## 4. Docs refresh

- [x] 4.1 Check `AGENTS.md`, `README.md`, and `docs/adr/` for a reference to the claims table's result form; no
      `docs/ideas.md` entry is owed.
- [x] 4.2 Edit `docs/retrospectives/2026-10-31.md`'s S2 block to mark it **Applied** in this change, recording that the
      `agent-skills` delta was not owed.

## 5. Ship

- [ ] 5.1 Commit, push, open the pull request, wait for `build`, archive (`skip_specs`), and merge.
