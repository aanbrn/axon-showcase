- [test-control] pr-body.md:35-36 — the described cases cannot fail for an over-broad lookbehind, and "no unresolved
  entry" at `pr-body.md:20` is satisfied by a pattern that drops real references; add a boundary positive and assert the
  report still lists known references.
- [contradiction] pr-body.md:70 — "both cases are empty against the fixed pattern" contradicts the path-tail control
  being the positive case at `:35-36`; enumerate each case's expected match count before and after.
- [claim-missing] pr-body.md:46-60 — the 13 "Not changed" upstream states have no evidence row, so none is reproducible
  from the supplied set; add the per-reference command or one captured output covering the set.
- [claim-unreproducible] pr-body.md:66 — "#1984 changes only the warning text" is sourced from the PR body, not its
  files; cite `pulls/1984/files` or restate as the PR's self-description.
- [claim-unreproducible] pr-body.md:68 — #145's "closed 2026-11-08" is inferred from the merge date; query the issue's
  own state and `closed_at`.
- [claim-missing] pr-body.md:8-9 — the no-delta decision rests on the merge-governance scope with no quoted clause and
  no claims row.
- [over-claim] pr-body.md:4 — "recorded fix had gone stale" overstates the #1891 correction, which adds the
  diagnostics-only closure beside the open behavior fix.
- [structured-writing] pr-body.md:3-6 — the Summary runs five change statements together and repeats the itemization in
  "What changes"; keep one outcome sentence.
- [formatting] pr-body.md:35 — two body lines (`:35`, `:41`) exceed 120 characters; wrap them by hand.
