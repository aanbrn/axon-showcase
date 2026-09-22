# Tasks

## 1. Sharpen the clause

- [x] 1.1 Add the precondition to the outcome-only clause in the `AGENTS.md` promotion-gate bullet — the mechanism
      belongs in the capability spec only when changing it would change a scenario's outcome, verified against the code
- [x] 1.2 Add the `MODIFIED` delta mirroring the same precondition into `showcase/quality/agent-skills`, carrying the
      requirement's description and all five scenarios verbatim

## 2. Verify

- [x] 2.1 `./gradlew spotlessApply` then `spotlessCheck`
- [x] 2.2 `openspec validate --all` passes (23 items) and the delta preserves the scenario count (5/5) — count it, do
      not trust the validator alone
- [x] 2.3 `grep -c "when a rule's rationale is normative"` is 1 in the delta (a duplicate clause was removed during
      editing)
- [x] 2.4 Confirm the `AGENTS.md` clause and the spec clause state the same precondition (they are verbatim copies and
      must not drift)
- [x] 2.5 Confirm no code changed: `git diff --stat` shows only `AGENTS.md` and the change dir
