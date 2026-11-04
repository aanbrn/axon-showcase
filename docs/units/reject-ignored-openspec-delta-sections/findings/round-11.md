- [test-control] scripts/verify-openspec-delta-guard.py — the probe treated any non-zero validation exit as evidence the
  CLI now rejects a shape, so a broken fixture environment would print the retirement note and pass; it had no
  valid-only control distinguishing strictness from breakage.
