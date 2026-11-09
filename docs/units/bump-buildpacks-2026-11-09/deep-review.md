# Deep review — bump-buildpacks-2026-11-09

## Routing decision

- **Decided** — 2026-11-09, before any deep-review session, from the completed implementation and the review evidence.
- **Outcome** — `RECOMMENDED`, awaiting the owner's explicit run-or-skip resolution.
- **Floor and signals** —
  `python3 scripts/review-routing.py --unit bump-buildpacks-2026-11-09 --change bump-buildpacks-2026-11-09` reports
  `RECOMMENDED` on `[repeated-class] 3 rows repeat a prior class`: round 2 repeated round 1's `claim-unreproducible` and
  `wording`, round 5 repeated `claim-unreproducible`, and round 7 repeated it again after round 5's fix. Rounds 3, 6,
  and 8 are clean — not consecutive, so the clean rounds do not close the signal.
- **What the repeats were** — round 2's were the consumer-row source and the dismissal; round 5's was the hand-over
  readiness line; round 7's was the same readiness line after round 5 had already corrected it, plus an incomplete
  image-inspection command. The recurrence therefore reached the implementation stage rather than staying in the
  proposal's wording.
- **Scope** — five catalog pins, four documented pin sentences in one subject pack, this unit's planning artifacts and
  records; no JVM code, build script, spec, or deployment surface changed.

## Passes

### Pass 1 — full, initial review

- **Kind** — full initial pass, `deep-1`, run through `/review-thorough` at the owner's run resolution.
- **Reviewed revision** — `0deca079ac3a57c8f18c673d47bf9e2f66448c72`, the unit's staged tree against base
  `bfe30a08ce3924cbf1f0d343d135ea196004e819`.
- **Outcome** — one should-fix finding; the change's substance passed (builder/pin agreement, catalog↔gate consistency,
  the docs sweep, `skip_specs`, and the recorded runtime-verification evidence).
- **Register row and hand-over** — the `review-thorough` row for this unit in `sessions.tsv`, and the `` `deep-1` ``
  section of `review-handover.md`.
- **Limits** — the reviewer did not re-run the builds or the two `check` tasks; `pack builder inspect` output is cited
  by command, not stored; the JVM health probe ran without the downstream services.

### Pass 2 — full, fix verification

- **Kind** — the budgeted verification pass, `deep-2`, continuing the `deep-1` session.
- **Reviewed revision** — the same staged tree at `0deca079ac3a57c8f18c673d47bf9e2f66448c72`.
- **Outcome** — `stale-deep-review-state` was fixed at both sites it named; the reviewer reported one further
  should-fix, a stale status line inside this record itself, which is applied below.
- **Register row and hand-over** — the `review-thorough` row for this unit in `sessions.tsv`, and the `` `deep-2` ``
  section of `review-handover.md`.
- **Third pass** — not run: after pass 2 the only remaining item was a status line in this record, with no
  implementation, catalog, documentation, or behavior change behind it, so a third full pass would carry no material
  risk to find.
- **Limits** — same as pass 1; the reviewer re-ran no build.

### Findings

- `stale-deep-review-state` — **FIXED_AND_VERIFIED**. The change's own records stated the pre-resolution state. Both
  sites are corrected: `## Passes` records the passes, and task 4.2's Done note states that the owner resolved to run
  and that the pass ran. Pass 2 verified both sites and its one further should-fix — a stale status line in this record
  — is applied. Evidence: `deep-review.md`'s `## Owner's resolution`, `## Passes`, and the two pass reports.

## Owner's resolution

- **Resolved** — 2026-11-09: the owner chose to **run** the `RECOMMENDED` deep review, after the advisor recommended it
  (the owner first said skip, then corrected to run).
- **What that records** — one deep-review pass is created, bounded to the change's artifacts: builder/pin agreement, the
  runtime-verification evidence, and implementation-to-plan alignment. It is not a repository-wide audit.
