---
description:
  Scan the current unit's authored diff for claim shapes and hand the candidates to the author, so the pre-write pass's
  claims table starts from a search rather than from what the author noticed stating
---

Seed the pre-write derivation pass's claims table from the unit's own diff.

**Trigger** — the owner invokes `/seed-claims`, or the review gate's pre-write pass runs it.

**Procedure**

1. Run `python3 scripts/claim-scan.py` from the repository root. The script owns what it scans, its `--base` default,
   and the candidate kinds it prints — read its output rather than its documentation restated here.
2. Resolve every candidate: give it a claims-table row (in the change's `design.md`, or in `tasks.md` for a unit that
   writes no design, or on the pull-request checklist line for a unit with no change dir), or record its dismissal
   beside the table — grouped by kind where the dismissal is uniform, with the reason.
3. Re-run the scan after the last edit to the claims table or its dismissals, when that edit can add candidate-shaped
   text of its own.

**Decision points**

- IF the scan cannot run — an unavailable base ref, or any failure — THEN report that as a visible failure and stop. An
  empty candidate list and a failed scan are different outcomes, and only one of them means there is nothing to resolve.
- The scan is a lead list, not a gate: it exits zero whatever it finds, and a candidate is not a finding.
- IF a candidate is neither a row nor a listed dismissal, THEN the review reports it — resolve it or dismiss it.

**Failure handling** — a scan that crashes on an unavailable `--base` is reported with its error; the pass does not
proceed as though it had found nothing.

**Completion criteria** — every candidate the scan printed is a claims-table row or a recorded dismissal beside it.

Do not restate the tool's patterns or the claims-table convention from memory — run the tool and read the pass's rule in
`showcase/quality/agent-skills`.
