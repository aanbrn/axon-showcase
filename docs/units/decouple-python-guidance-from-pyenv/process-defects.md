# Process defects — decouple-python-guidance-from-pyenv

Process defects observed while working this unit. Recorded under `AGENTS.md`'s "Workflow: Record process defects".

Workspace: unit=decouple-python-guidance-from-pyenv;
path=/Users/afanasyev/Development/axon-showcase-decouple-python-guidance-from-pyenv;
branch=decouple-python-guidance-from-pyenv; writer=ses_e449e877cffe2Z8En9SaqD3jqp

### decouple-python-guidance-from-pyenv#1 — The `Packs:` line was not stated before the unit's first edit

- **Recorded** — 2026-11-08, by the writing session (`ses_e449e877cffe2Z8En9SaqD3jqp`), during the proposal stage.
- **Stage** — proposal.
- **Expected** — the subject-pack rule requires one `Packs:` line naming every pack loaded in full, or `Packs: none`
  with the reason, before the first edit.
- **Observed** — the unit record and the change's four artifacts were written before any `Packs:` line was stated.
- **Evidence** — the session's messages before the first `write` call carry no `Packs:` line, and the first file written
  was `docs/units/decouple-python-guidance-from-pyenv/unit.md`.
- **Consequence** — none known: the classification is `Packs: none` (the change edits markdown, a Python diagnostic
  message, and its Python test; no JVM production code or test, no Gradle build decision, no deployment surface), and no
  decision was made under a pack. The line is stated after the fact.
- **Knowledge boundary** — observed: the message sequence and the files written. Not checked: whether a later decision
  in the unit belongs to a pack.

### decouple-python-guidance-from-pyenv#2 — A shell variable named `status` failed the verification command

- **Recorded** — 2026-11-09, by the writing session (`ses_e449e877cffe2Z8En9SaqD3jqp`), during the implementation stage.
- **Stage** — implementation.
- **Expected** — capture the test suite's exit status and read it beside its output.
- **Observed** — the command `python3 scripts/test-ensure-idea-settings.py > /tmp/t.out 2>&1; status=$?; …` failed with
  `zsh:1: read-only variable: status`, so the suite's result was never captured.
- **Evidence** — the shell's message, `zsh:1: read-only variable: status`, and the exit code 1 of that invocation.
- **Consequence** — one re-run with `rc` in place of `status`; no artifact was affected and the suite result was then
  captured (`TEST_EXIT=0`).
- **Knowledge boundary** — observed: the failing invocation and the successful re-run. Not checked: which other
  identifiers zsh reserves (`status` is one; the full set was not enumerated).

### decouple-python-guidance-from-pyenv#3 — A formatter-owned record was written after the last `spotlessApply`

- **Recorded** — 2026-11-09, by the writing session (`ses_e449e877cffe2Z8En9SaqD3jqp`), during the implementation stage.
- **Stage** — implementation (the round-5 hand-over).
- **Expected** — `AGENTS.md`'s gotcha: after any last edit to a Spotless-owned file, re-run `spotlessApply` and
  `spotlessCheck` before reporting the gate green, and never tick a gate task as the last act.
- **Observed** — the round-5 hand-over section was appended after the last `spotlessApply` and before the recorded
  `check`, and its completed-checks line claimed `spotlessCheck` passed; the quick review found `:spotlessMarkdownCheck`
  failing on that file.
- **Evidence** — the round-5 finding instance (`findings/round-5.md`, `claim-unreproducible`), which names
  `review-handover.md` as the failing file and the record as written after the last formatter run.
- **Consequence** — one quick-review round; the record was reflowed and the full `check` re-run on the refreshed base.
  No shipped file was affected.
- **Knowledge boundary** — observed: the failing gate and the record's write order. Not checked: whether any other
  formatter-owned record in this unit was written after a formatter run.
