# Deep review — address-kafka-client-advisories

Routing outcome: `RECOMMENDED`, resolved by the owner to **run**.

- Derived with
  `python3 scripts/review-routing.py --unit address-kafka-client-advisories --change address-kafka-client-advisories`
  after round 9 closed clean (2026-11-09).
- Signals: `repeated-class` — two rows repeat a prior class: round 5 (`contradiction`, repeating round 2's class) and
  round 8 (`missing-task`, repeating round 1's class).
- Owner's resolution: run the deep review (instruction "Запускай", 2026-11-09), after a technical-advisor follow-up
  recommended it for an independent check of the version-baseline exception.

## Pass 1 — initial review

- **Kind**: full pass, initial review of the implementation against the planning artifacts.
- **Reviewed revision**: the staged working tree on base `2b4c9a00`, uncommitted.
- **Outcome**: 3 findings — 2 should-fix, 1 nitpick; none blocking.
- **Limits**: the credentialed Snyk scan had not run at this pass; the e2e evidence came from the local Docker run.
- **Record references**: register row event 12 (`sessions.tsv`); hand-over section `deep-review-pass-1`
  (`review-handover.md`).
- **Findings and their final statuses**:
  - `coherence` — the proposal and the consultation hand-over stated the supersession condition as "Spring Boot 4 /
    Spring Kafka 4", while ADR-0031 and the claims table state it as the pinned Spring Boot line managing
    `kafka-clients` at `4.4.0` or newer; Boot 4.0.7 manages 4.1.2, below that line. Evidence: `proposal.md:19`,
    `design.md:116`, `docs/adr/0031-…md:7-9`. Status: `FIXED_AND_VERIFIED` (pass 2).
  - `missing-task` — task 4.4 was unticked although the implementation quick review closed clean at round 9. Evidence:
    `tasks.md:81`, `findings/round-9.md`, `rounds.tsv` row 9. Status: `FIXED_AND_VERIFIED` (pass 2).
  - `over-claim` — the parked broker entry said the client-side fix "shipped" while the unit was uncommitted. Evidence:
    `docs/ideas.md:33-34`. Status: `FIXED_AND_VERIFIED` (pass 2).

## Pass 2 — fix verification

- **Kind**: targeted verification of the three pass-1 findings, run independently on the current tree.
- **Reviewed revision**: the staged working tree on base `2b4c9a00`, after the fixes.
- **Outcome**: `nothing remains` — all three findings fixed and verified, no new findings introduced.
- **Limits**: same as pass 1 — no credentialed Snyk scan; the broker exposure stays unassessed and parked.
- **Record references**: register row event 13 (`sessions.tsv`); hand-over section `deep-review-pass-2`
  (`review-handover.md`).

The budget's two passes are spent; no finding remains open, so no third pass is proposed.

## Reassessment after the base refresh

`python3 scripts/review-routing.py --unit address-kafka-client-advisories --change address-kafka-client-advisories` was
re-run on the refreshed tree (base `0deca079`, round 12 closed clean) and returned `RECOMMENDED` again, with the same
signal as before: `[repeated-class] 2 rows repeat a prior class` (rounds 5 and 8).

The tree's changes since pass 2 are: the base fast-forward (`build.gradle.kts`, `build-logic`'s chart tasks, two subject
packs — none of this change's files), a hand-resolved `docs/ideas.md` merge that kept both same-day entries, the claims
table's re-anchored base SHAs, and two wording fixes (a redundant clause, and three restatements of ADR-0031's gate
replaced with pointers). None of them touches the implementation the two passes reviewed, and the build was re-verified
after the refresh (`compileJava`, the Docker-free `check`, `spotlessCheck`, `openspec validate --all`).

The owner's run-or-skip decision on the reassessment is pending.

The owner's resolution on the reassessment: skip a further deep-review pass. The instruction was the delivery request
("Отгружай", 2026-11-09), following a second advisor follow-up that recommended not repeating the pass and proceeding to
the owner's manual review with Snyk kept as the delivery-completion condition; no new implementation risk appeared since
pass 2. The manual review pass is the same delivery instruction.
