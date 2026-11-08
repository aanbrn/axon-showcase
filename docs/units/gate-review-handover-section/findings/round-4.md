- [test-control] scripts/commit-hygiene.py:123 — the heading scan is a literal `^## Round ` line match, so a fenced
  `## Round 3` passes while a legal Markdown heading with two spaces after `##` or up to three leading spaces fails; no
  control covers either shape.
- [test-control] scripts/commit-hygiene.py:2270 — `partition(":")` splits at the first colon, so `unit-a:3:4` fails
  naming the round where the delta's unit scenario says the extra colon fails naming the unit; the tests assert only
  `--handover`, so they pass on either message.
- [redundant-code] scripts/commit-hygiene.py:117 — the new `HANDOVER_RECORD_BASENAME` duplicates the existing
  `HANDOVER_BASENAME`, and `check_handover` rebuilds the relative path that `handover_label` already builds.
