- [test-control] scripts/test-commit-hygiene.py:4604 — the per-agent scope checks are in the mode but have no negative
  case for a reviewer's wrong scope or a consultation's non-slug scope
- [test-control] scripts/commit-hygiene.py:1929 — two mode branches are unexercised: the mismatch row's
  transition/reason check and the first-row-not-created case for a continued or replaced row
