- [test-control] scripts/commit-hygiene.py:2287 — the new fence tracker approximates CommonMark's rules: a pseudo-closer
  inside a fence closes it, so a `## Round 3` still inside passes, an indented closer and an indented opener are
  misread, and the fenced test ends at its closer so a broken close still produces its expected failure.
- [redundant-code] scripts/commit-hygiene.py:2289 — the fence loop duplicates `find_table_row_offenders`' own open/close
  rule, so the same too-permissive closer exists in two copies; extract one shared tracker.
