---
description:
  Scan the current unit's authored diff for claim shapes and hand the candidates to the author, so the pre-write pass's
  claims table starts from a search rather than from what the author noticed stating
---

Seed the pre-write derivation pass's claims table from the unit's own diff.

1. Run `python3 scripts/claim-scan.py` from the repository root — it scans the added lines of the diff against
   `origin/main` (override with `--base`) plus the untracked, non-ignored files, so the change dir is covered while the
   pass runs. It prints one candidate per line as `path:line  [kind]  text`, with the kinds `count`, `absolute`,
   `cross-ref`, and `path`.
2. Resolve every candidate: give it a claims-table row (in the change's `design.md`, or in `tasks.md` for a unit that
   writes no design, or on the pull-request checklist line for a unit with no change dir), or record its dismissal
   beside the table — grouped by kind where the dismissal is uniform, with the reason.
3. The scan is a lead list, not a gate: it exits zero whatever it finds, and a candidate is not a finding. It seeds the
   table and never replaces the derivation, since it enumerates shapes rather than facts — a claim with no shape in the
   list is still the author's to find.

Do not restate the tool's patterns or the claims-table convention from memory — run the tool and read the pass's rule in
`showcase/quality/agent-skills`. The review reports a scan candidate that is neither a row nor a listed dismissal.
