# Advisor consultation — implementation pass

- [over-claim] .opencode/commands/gradle-update.md:41-43 — the command promised a restore on a nonzero exit, while
  `restore()` can itself fail; the three outcomes (restored, incomplete, untouched) had to be reported as themselves.
- [claim-unreproducible] scripts/update-gradle-wrapper.py:23-24 — the tool's own exit-status docstring still said the
  four files "are restored", contradicting the incomplete outcome.
- [claim-unreproducible] .opencode/commands/gradle-update.md:43-44 — the command listed a killed process among the cases
  where "the report says it is incomplete", though a killed process writes no final report.
- [over-claim] openspec/changes/pin-wrapper-distribution-checksum/design.md:122 — the design described the pinned
  archive's acceptance as CI's, where no accepting run had been observed.
