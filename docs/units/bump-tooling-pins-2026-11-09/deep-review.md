# Deep review — bump-tooling-pins-2026-11-09

## Routing decision

- **Outcome** — `REQUIRED`.
- **Signals** —
  `python3 scripts/review-routing.py --unit bump-tooling-pins-2026-11-09 --change bump-tooling-pins-2026-11-09` returned
  `REQUIRED` with `[infrastructure-or-deployment]` on each of the four edited workflows: `.github/workflows/audit.yml`,
  `.github/workflows/ci.yml`, `.github/workflows/dependency-security.yml`, and `.github/workflows/opencode.yml`.
- **Owner's resolution** — not applicable; a `REQUIRED` outcome runs without one.
- **The change's review task** — `tasks.md`'s `3.3`, which points here rather than carrying the statuses.

## Passes

### Pass 1 — full pass, initial review

- **Reviewed revision** — `0deca079ac3a57c8f18c673d47bf9e2f66448c72`; the unit's ten modified targets and its records.
- **Pass number and kind** — 1, initial full pass.
- **Outcome** — `nothing remains`; no finding was raised, so the record carries no finding entries.
- **Verified** — the diff is exactly the four pin lines and six stamps; both targets re-resolved live (`1.16.1`,
  `v1.1308.4`); the check's three OpenSpec entries and one Snyk entry are the four edits; `skip_specs` holds;
  regeneration is stamp-only; the config-read-path controls fire in both directions; the docs sweep leaves only
  historical citations; `Fission-AI/OpenSpec#1894` is `OPEN`/`null`; `audit.yml`'s prompt invokes no `openspec` command.
- **Limits** — the Snyk tag's installability and both dispatches are delivery-stage evidence.
- **References** — register row `review-thorough` pass 1; hand-over section `` `deep-1` ``.

### Pass 2 — full pass, verification of the re-plan

- **Reviewed revision** — `caf72739` base with the implementation commit `46102b8` and the staged re-plan.
- **Pass number and kind** — 2, independent verification after the CI discovery.
- **Outcome** — one should-fix finding, `F1`: the unit checkpoint's revision line was stale by one rebase (`634bc3b5` /
  `41e05ab` against the actual `caf72739` / `46102b8`). **Status `FIXED_AND_VERIFIED`** — the checkpoint now records the
  actual base and `HEAD`.
- **Disputed point adjudicated** — round 14's `[premise]` claim that `merge-governance` needs a `MODIFIED` delta for the
  section shape: no delta is owed. The scenario's normative clause is its `THEN`, which the non-zero `validate` exit
  strengthens for that shape, and the stale fragment is a mechanism description the growth-bound rule keeps out of a
  spec unless it changes an outcome. The re-planned proposal wording is accurate.
- **Verified** — the guard fix and its control reproduce against the real `1.16.1` (22/22 with the phrase, the new test
  fails without it), and the `ci.yml` comment and `AGENTS.md` bullet state both shapes' current behavior.
- **Why it runs** — pass 1 reviewed the pre-re-plan change set; the re-plan adds the delta-guard probe and its control,
  the `ci.yml` guard-step comment, and `AGENTS.md`'s delta-guard bullet.
- **References** — register row `review-thorough` pass 2; hand-over section `` `deep-2` ``.
