# Tasks — verify the Air model picker before the first request

The unit's record is `docs/units/verify-air-model-pin/unit.md`.

## 0. The pre-write pass

- [x] 0.1 The review gate's pre-write derivation pass ran, seeded with
      `python3 scripts/claim-scan.py --base origin/main` over the change's artifacts; `design.md` carries the claims
      table and the per-kind dismissals beside it. Verify: every scanned candidate is a row or is covered by its kind's
      dismissal, and each row's command reproduces its figure.

## 1. The Air section's pre-request check

- [x] 1.1 `.opencode/skills/setup-agent-tools/SKILL.md`, the Air section — a best-effort check: the skill is loaded on a
      setup request and from `AGENTS.md`'s Air pointer at session selection, not automatically in every session:
  - when this guidance is loaded, check the model before continuing substantive work;
  - if trustworthy runtime model information is available, compare it with the selected agent's configured model;
    otherwise ask the owner to confirm the picker;
  - on a mismatch, ask the owner to restore it or confirm the override, unless a verified switching mechanism is
    available;
  - recheck after switching agent or model;
  - point at `AGENTS.md`'s default-versus-session caveat instead of restating it;
  - name no model identifier;
  - claim no enforcement.

  Verify: each rule is present, the pointer resolves, and the added text contains no model identifier.

- [x] 1.2 `README.md`, the Parallel Units section: one sentence telling a person starting a unit in Air that its picker
      may override the pinned model, so they check it before submitting the first prompt and after changing agent or
      model. Verify: the sentence names the picker, the pin, and both checks; it adds no model identifier; and it sits
      in that section.

## 2. Verification

- [x] 2.1 `./gradlew spotlessApply`, then read back both reflowed texts. Verify: `./gradlew spotlessCheck` passes, and
      `git diff HEAD -- .opencode/skills/setup-agent-tools/SKILL.md README.md | grep -c "deepseek\|gemini\|openrouter"`
      is 0 — after the same command returns 1 with a `gemini` line planted and staged in `README.md`, then reverted
      (`grep -c` exits 1 when the count is 0, which is the expected success case).
- [x] 2.2 `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` and `openspec validate --all`. Verify: both pass and
      `skip_specs` validates.
- [x] 2.3 Re-read each claims-table row against its source and delete any row that no longer reproduces. Verify: every
      remaining row's source was re-run or re-opened.

## 3. The review gate and the records

- [x] 3.1 The proposal stage's quick review over the planning artifacts until a clean round, each round's row and
      findings recorded under `docs/units/verify-air-model-pin/`. Verify, as three separate invocations because
      `--session-continuity` cannot be combined with the `--unique-rounds` checks:
      `python3 scripts/commit-hygiene.py --unique-rounds --expect verify-air-model-pin:<N>`;
      `python3 scripts/commit-hygiene.py --unique-rounds --expect-clean verify-air-model-pin`;
      `python3 scripts/commit-hygiene.py --session-continuity`.
- [x] 3.2 The owner's manual pass over the proposal. Verify: the approval is recorded before any implementation edit.

      **Done** — the owner approved the corrected proposal on 2026-11-07 ("Одобряю. Реализуй."), after the technical
      advisor agreed on the two corrected halves. No implementation edit preceded it.

- [x] 3.3 The implementation stage's quick review, then the deep-review routing decision. Verify: the closing round
      carries no finding class,
      `python3 scripts/review-routing.py --unit verify-air-model-pin --change verify-air-model-pin` ran, and its outcome
      and signals are recorded.

      **Done** — the implementation's quick loop closed clean at round 10; the routing is `RECOMMENDED`, signal
      `[repeated-class] 6 rows repeat a prior class`. The owner decided to skip the deep pass, recorded 2026-11-07, on
      the advisor's risk rationale: the final change's limited effect, its explicit fallbacks, and the reported
      resolution of the implementation findings — not the line count or the proposal's agreement, which do not establish
      a current defect.

- [x] 3.4 The owner's manual pass over the finished unit. Verify: the approval is recorded, and nothing is committed,
      pushed, or archived before it.

      **Done** — the owner approved the finished unit on 2026-11-07 and requested shipment after a branch refresh
      ("Одобряю. Отгружай. Только сначала ветку обнови."). The branch was fast-forwarded to `origin/main` `4709f9aa`
      before the first commit.

- [x] 3.5 `docs/units/verify-air-model-pin/process-defects.md`: one record per defect observed, or the statement that
      none was. Verify: the file exists before 3.4 is requested.
