# Deep review

- **Derivation** — `python3 scripts/review-routing.py --unit pause-scheduled-jobs-2026-11-09` reports
  `ROUTING: REQUIRED` on its `[infrastructure-or-deployment]` signal, `.github/dependabot.yml`, after the quick loop's
  fourth round.
- **Signals** — the unit edits a dependency-automation configuration file that the repository's own gate does not read,
  and it records a nine-workflow Actions-state change no diff can show; the routing floor is the tool's, and a
  `REQUIRED` outcome runs before completion whatever its size.
- **Pass 1** — full pass, initial review; reviewed revision `4ce1a996` plus the uncommitted working tree; register row
  in `sessions.tsv` (event 5), hand-over section `deep-review-1`; outcome: one should-fix finding, no blocking finding;
  limits: the reviewer has no shell, so the Actions state and the Dependabot suspension are supplied facts.
- **Findings**
  - `resume-revert-enumeration-incomplete` — **FIXED_AND_VERIFIED** (should-fix). Evidence: the resume entry named the
    notices to drop as "the dependabot paragraph and the CI section's suspension sentence" and the Dependabot change as
    "raise it back to `10`", while the unit's diff also adds a three-line comment in `dependabot.yml` and rewrites three
    `AGENTS.md` auditor bullets to "when (that workflow) is enabled". Consequence: a literal restoration would leave the
    comment asserting the suspension and the three qualifiers in place. Fix: the entry now names the comment's deletion
    alongside the limit, and the three bullets' qualifiers among the edits to revert. Pass 2 re-derived the revert set
    from the diff and found every pause edit named — `nothing remains`.
- **Pass 2** — targeted verification of the pass-1 fix; register row in `sessions.tsv` (event 6), hand-over section
  `deep-review-2`; outcome: `nothing remains`.
- **Status** — both passes complete; no open finding remains, and the unit may proceed to the owner's manual pass.
