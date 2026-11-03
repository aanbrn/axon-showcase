- [missing-task] docs/units/generalize-process-defects-2026-11-03/review-handover.md:84 — the file has no section for
  this invocation, so a stall would hand a successor the previous round's artifact set.
- [claim-unreproducible] docs/units/generalize-process-defects-2026-11-03/findings/round-5.md:1 — the file's single line
  is unwrapped, so `spotlessMarkdownCheck` fails and the invocation's "spotlessCheck is clean" does not reproduce.
