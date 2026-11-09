# Deep review

- **Derivation** —
  `python3 scripts/review-routing.py --unit bump-kps-chart-2026-11-09 --change bump-kps-chart-2026-11-09` reports
  `ROUTING: RECOMMENDED` on its `[repeated-class]` signal ("13 rows repeat a prior class"), taken from
  `docs/units/bump-kps-chart-2026-11-09/rounds.tsv` after round 19.
- **Signals** — the repeated classes are `incomplete-enumeration`, `coherence`, `claim-unreproducible`, `stale-base` and
  `formatting`, across a proposal that was retargeted after a blocked live attempt and re-reviewed from round 11.
- **Owner's run-or-skip resolution** — run (2026-11-09, "Запускай"), after the advisory consultation
  `deep-review-decision` recommended it, raising the substantive risk above the machine floor because a catalog pin
  change alters a substantial deployment dependency (kube-state-metrics `8.8.0` → `9.0.0`, grafana `14.4.0`).
- **Pass 1** — full pass, initial review; reviewed revision `73aacad4`; register row in `sessions.tsv` (event 23),
  hand-over section `deep-review-1`; outcome: one should-fix finding, no blocking finding; limits: the reviewer cannot
  run shell or a cluster, so the live results are the caller's records.
- **Findings**
  - `render-inspection-under-covers-declared-set` — **FIXED_AND_VERIFIED** (should-fix). Evidence: the disabled-set
    derivation returned seven keys while task 2.3's Done note recorded five, and the operator and its config-reloader
    were collapsed into one entry. Fix: the Done note now records all seven keys with their toggle-flipped render
    comparisons and the config-reloader's resources distinctly. Pass 2 re-derived the set, mapped each key to a recorded
    rendered absence, and verified the `--config-reloader-*` mechanism against the chart's operator deployment template
    — `nothing remains`.
- **Pass 2** — targeted verification of the pass 1 fix; register row in `sessions.tsv` (event 24), hand-over section
  `deep-review-2`; outcome: `nothing remains`.
- **Status** — both passes complete; pass 1's finding fixed and verified; no open finding remains.
