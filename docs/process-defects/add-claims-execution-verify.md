# Process defects — add-claims-execution-verify

Process defects observed while working this change. Recorded under `AGENTS.md`'s "Workflow: Record process defects".

### add-claims-execution-verify#1 — The implementation was written against the pre-advisor contract

- **Recorded** — 2026-10-31, by the writing session (`ses_e731f42aaffeKQAMVYfJ2KxAYN`), during the implementation.
- **Stage** — implementation (the `--verify` mode, the first implementation round).
- **Expected** — the code implements the revised design's D2–D5: the two-span `piped through` join, the read-only `git`
  read set, the working-tree rule, the literal-only separator, and the deferred forms including `python3`.
- **Observed** — the first implementation returned advisory for any multi-span source and any `\|`, executed the broader
  `git` read set, always marked a working-tree `grep`/`sed` advisory without the design's state clause, admitted a
  semicolon separator, and did not defer `python3`; the design had been revised after the advisor's review, and the code
  followed the earlier text. The quick review's round 10 raised the five mismatches.
- **Evidence** — the review round 10's findings 1–5 (D2's join, D4's read set, D3's separator, D5's exception,
  `python3`); the code's `verify_eligible` before the fix; the design's D2–D5.
- **Consequence** — four review rounds (10–14) to align the code with the design; no artifact shipped with the
  divergence.
- **Knowledge boundary** — observed: the code, the design, and the review findings. Not checked: whether the divergence
  came from the design's revision timing or from the implementation's reading of the earlier text.

- **Decision** — no-change: the review gate's plan/scope rules and the apply procedure's requirement to read every
  context file are the control; the cause is unconfirmed, so no rule can be scoped from it.
