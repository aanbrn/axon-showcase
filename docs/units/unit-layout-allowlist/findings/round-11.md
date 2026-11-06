- [missing-scenario] scripts/commit-hygiene.py:1909 — a tracked file directly under `docs/units/` is reported, though
  the requirement's subject is a unit's directory and no scenario or arm states that case.
- [claim-unreproducible] design.md:97 — the claims row sources `store_stray_files`, which this change deletes, without
  anchoring the row to the base revision.
- [wording] tasks.md:10 — all eleven Done notes read "Done: Done:".
