# Tasks — decouple the optional interpreter guidance from pyenv

The unit's record is `docs/units/decouple-python-guidance-from-pyenv/unit.md`.

## 0. The pre-write pass

- [x] 0.1 The review gate's pre-write derivation pass ran, seeded with
      `python3 scripts/claim-scan.py --base origin/main` over the change's artifacts; `design.md` carries the claims
      table and the per-kind dismissals beside it. Verify: every scanned candidate is a row or is covered by its kind's
      dismissal, and each row's command reproduces its figure.

## 1. The README's interpreter guidance

- [x] 1.1 `README.md`, the paragraph that describes the `NEEDS_ACTION` remedy: replace "where pyenv supplies it" with
      the condition the example must satisfy, so the line names `pyenv local <installed-version>` as an example and says
      `python3` must resolve to that interpreter. Verify: the line drops "where pyenv supplies it", states the
      condition, keeps one concrete example, and the surrounding sentence still describes the same remedy.
- [x] 1.2 `README.md`, the optional interpreter-selection paragraph: rewrite it around the selector rather than around
      pyenv — the tooling runs the first `python3` on `PATH`, a personal `.python-version` matters only where the
      environment integrates it into that selection (pyenv's shims do, and uv's own commands read the same file), and a
      pin does not by itself change which `python3` the shell resolves — and keep the `python3 --version` check and the
      ignored-setting statement. Verify: pyenv is no longer presented as the mechanism, the selector distinction is
      stated without a universal claim about uv, the ignored-setting statement survives, and no uv behavior beyond C1–C3
      and C10–C11 is claimed.

## 2. The setup skill

- [x] 2.1 `.opencode/skills/setup-idea/SKILL.md`, initialization step 5: replace "pyenv's shims ahead of other
      installations" with a selector on `PATH` that reads the file, naming pyenv's shims as one example. Verify: the
      sentence still says the inherited selection applies only where the tree's `PATH` resolves `python3` through the
      selector that reads it, and names no manager as the mechanism.
- [x] 2.2 Same file, the `NEEDS_ACTION` remedy in the procedure: state the condition and keep one example, as the
      script's message does. Verify: the step still hands the block's remedies to the user in the same order.

## 3. The script and its test

- [x] 3.1 `scripts/ensure-idea-settings.py`, the `NEEDS_ACTION` message: replace the pyenv-only remedy line with the
      condition and one example. Verify: the message's remedy still precedes the SDK inventory, and the emitted text
      states that `python3` must resolve to the selected interpreter.
- [x] 3.2 `scripts/test-ensure-idea-settings.py`, the assertion that pins the remedy's command: pin the condition phrase
      beside the example. Verify: the test fails against the pre-change message and passes with the new one, and it
      still asserts the remedy's lead sentence and its position before the inventory.

## 4. Verification

- [x] 4.1 `./gradlew spotlessApply`, then read back the reflowed markdown and Python. Verify: `./gradlew spotlessCheck`
      passes and no changed file was skipped as non-converging; every inline code span is read as content; the Python
      files stay within 120 characters.
- [x] 4.2 `python3 scripts/test-ensure-idea-settings.py`. Verify: the suite passes, and the changed assertion was seen
      to fail against the pre-change message.
- [x] 4.3 `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` and `openspec validate --all`. Verify: both pass and
      `skip_specs` validates.
- [x] 4.4 Re-read each claims-table row against its source and delete any row that no longer reproduces. Verify: every
      remaining row's command was re-run or its source re-opened.

## 5. The review gate and the records

- [x] 5.1 The proposal stage's quick review over the planning artifacts until a clean round, each round's row, hand-over
      section, and finding instances recorded under `docs/units/decouple-python-guidance-from-pyenv/`. Verify, as
      separate invocations:

      ```bash
      python3 scripts/commit-hygiene.py --unique-rounds --expect decouple-python-guidance-from-pyenv:<N>
      python3 scripts/commit-hygiene.py --session-continuity
      python3 scripts/commit-hygiene.py --unique-rounds --expect-clean decouple-python-guidance-from-pyenv
      ```

- [x] 5.2 The owner's manual pass over the proposal. Verify: the approval is recorded before any implementation edit.

      **Done** — the owner approved the proposal on 2026-11-09 ("Реализуй"), after three advisor passes ended in
      explicit agreement. No implementation edit preceded it.

- [x] 5.3 The implementation stage's quick review, then the deep-review routing decision. Verify: the closing round
      carries no finding class, and this ran with its outcome and signals recorded in `deep-review.md`:

      ```bash
      python3 scripts/review-routing.py --change decouple-python-guidance-from-pyenv
      ```

- [x] 5.4 The deep review, when 5.3 selects one, with its findings applied and re-reviewed. Verify: the pass's status
      and its findings' disposition are recorded in `deep-review.md`.

      **Done** — 5.3 selected `RECOMMENDED`; the owner resolved it as skip on 2026-11-09, recorded in
      `deep-review.md`. No deep-review session was created.

- [x] 5.5 The owner's manual pass over the finished unit. Verify: the approval is recorded, and nothing is committed,
      pushed, or archived before it.

      **Done** — the owner approved the finished unit and requested shipment on 2026-11-09 ("Пропустить. Отгружай.").
      Nothing had been committed, pushed, or archived before it.

- [x] 5.6 `docs/units/decouple-python-guidance-from-pyenv/process-defects.md`: one record per defect observed, or the
      statement that none was. Verify: the file exists before 5.5 is requested.
