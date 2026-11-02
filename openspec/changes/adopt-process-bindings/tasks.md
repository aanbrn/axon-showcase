# Tasks — adopt-process-bindings

Workspace: unit=adopt-process-bindings; path=/Users/afanasyev/Development/axon-showcase; branch=adopt-process-bindings;
writer=ses_e731f42aaffeKQAMVYfJ2KxAYN

## 1. The declaration

- [x] 1.1 Add the consumer trace to `docs/contracts/process-bindings.md`: the adopters and the deliberate non-adopters
      with each one's reason, replacing the archived change's trace as the live set.

## 2. The adopting components

- [x] 2.1 Adopt the review-ledger role in `.opencode/skills/review-gate/SKILL.md` — every literal ledger path becomes
      the role, and the component carries the read route.
- [x] 2.2 Adopt it in `.opencode/agent/review-quick.md`.
- [x] 2.3 Adopt it in `.opencode/agent/experience-analyzer.md`.
- [x] 2.4 Adopt it in `.opencode/commands/review-thorough.md`.

## 3. The ADR and the spec delta

- [x] 3.1 Record the second adopting change in ADR-0026's Consequences.
- [x] 3.2 Write the `MODIFIED` delta for the `agent-skills` requirement: the consumer trace is the declaration's.
- [x] 3.3 Park the lifecycle and review-gate heading roles' adoption in `docs/ideas.md`, with the reason it is its own
      sweep.

## 4. Verification

- [x] 4.1 Sweep every remaining literal ledger path and classify it against the declaration's non-adopter classes, with
      each control run and reverted.
- [x] 4.2 `openspec validate --all` passes.

## 5. Delivery

- [x] 5.1 Write `docs/process-defects/adopt-process-bindings.md`.
- [x] 5.2 The review gate: the pre-write pass and claims table, the quick loop, the routing decision, the deep review,
      the advisor. - Quick loop: 6 rounds, classes 8, 7, 4, 2, 0, 0; the closing round is clean. - Routing:
      `RECOMMENDED`, signal `repeated-class` (3 rows); the owner resolved to run the deep review. - Deep review pass 1,
      `S1` — `docs/contracts/process-bindings.md`'s `review-gate` trace row and
      `.opencode/skills/review-gate/SKILL.md`'s read route listed three roles where the component resolves one —
      `FIXED_AND_VERIFIED` in pass 2. - Deep review pass 1, `S2` — `design.md`'s delta claim row cited
      `openspec validate --all`, which validates the corpus rather than the delta — `FIXED_AND_VERIFIED` in pass 2. -
      Deep review pass 1, `N1` — `design.md`'s evidence described the count command's result as `returns 0` where it
      prints nothing and exits 1 — `FIXED_AND_VERIFIED` in pass 2. - Deep review pass 2: nothing remains. - Advisor: one
      confirmed problem (the enforcement-tooling class explained the dependency as arguments), corrected; the follow-up
      review's one confirmed problem (this record) is applied here.
- [ ] 5.3 The owner's manual review pass.
- [ ] 5.4 Refresh the `agent-skills` Purpose where this change falsifies it, applied in the archive commit.
- [ ] 5.5 Commit, push, open the pull request, wait for `build`, archive, and merge on the owner's delivery request.
