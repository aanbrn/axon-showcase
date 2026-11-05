- [docs-residue] docs/units/pin-wrapper-distribution-checksum/unit.md:23-24 — the Resume checkpoint carried two
  pending-decision lines, the second a proposal-stage residue the task 4.2 tick contradicts.
- [redundant-code] scripts/update-gradle-wrapper.py:187-216 — the snapshot directory was removed only on success, so
  each handled failure left one behind though its restore had already run.
