# Review hand-over — apply-2026-11-07-audit-doc-findings

## Round 1 — implementation screen

- **Unit, tree, branch, base** — `apply-2026-11-07-audit-doc-findings`; tree
  `/Users/afanasyev/Development/axon-showcase-apply-2026-11-07-audit-doc-findings` (the unit's sibling worktree); branch
  of the same name, forked `--no-track` from `origin/main` `0deca079`; nothing committed; the unit's files staged.
- **Packs** — none: the unit edits one guidance bullet and takes no JVM, test, Gradle-build, or deployment decision.
- **Supplied artifacts** — `openspec/changes/apply-2026-11-07-audit-doc-findings/` (`proposal.md`, `design.md` with the
  claims table, `tasks.md`, `.openspec.yaml` with `skip_specs`),
  `docs/units/apply-2026-11-07-audit-doc-findings/unit.md`, and the staged diff against `origin/main` (the `AGENTS.md`
  bullet plus the change dir and unit record).
- **Scope** — the implementation: the `AGENTS.md` bullet rewrite, the claims table, and the `skip_specs` decision.
- **Prior findings** — none; this is the unit's first round.
- **Completed checks** — `openspec validate --all` exits 0;
  `openspec validate apply-2026-11-07-audit-doc-findings --strict` reports the change valid with its `skip_specs` note;
  `./gradlew verifyCapturedMarkers` passes; `spotlessApply` has run after the last edit; each claims-table row was
  re-run at `0deca079`.
- **Known limitations** — the report's spec-corpus fixes are the separate unit `apply-2026-11-07-audit-findings`, and
  the `handlebars` advisory is `remove-dead-handlebars-constraint`.

## Round 2 — implementation screen, after round 1's four findings

- **Unit, tree, branch, base** — unchanged; base `0deca079`; nothing committed; the unit's files staged, this section
  included.
- **Packs** — none, for the same reason as round 1.
- **Supplied artifacts** — round 1's fixes: `design.md` gains five claims-table rows (`#1892`, `#2127`, `#1891`, the
  `agent-skills` split requirement, and the report's `21b100bf` merge) and a `cross-ref` dismissal; `tasks.md` ticks
  1.1, 1.2, and 2.1–2.4. The `AGENTS.md` bullet is unchanged.
- **Scope** — the same bounded screen as round 1.
- **Prior findings** — round 1 raised three `claim-missing` findings (the upstream-status facts, the split requirement
  and the report merge, and the missing `cross-ref` dismissal) and one `missing-task` (six unticked tasks); all fixed.
- **Completed checks** — `openspec validate --all` exits 0; the change validates strictly with `skip_specs`;
  `spotlessCheck` and `verifyCapturedMarkers` pass; `--review-ready` reports the base contained and the index current.
- **Known limitations** — unchanged from round 1.

## Round 3 — implementation screen, after round 2's finding

- **Unit, tree, branch, base** — unchanged; base `0deca079`; nothing committed; the unit's files staged, this section
  included.
- **Packs** — none, for the same reason as round 1.
- **Supplied artifacts** — round 2's fix: the claims table gains three rows derived from the applied bullet's remaining
  assertions — the archived probe replay (`validate` exit 1 on an unparseable config under `1.16.0`), the `v1.16.0`
  release publishing after `#2127`'s merge, and `/opsx-tool-update`'s list-shape positive controls. No other artifact
  changed.
- **Scope** — the same bounded screen as round 1.
- **Prior findings** — round 1's four are fixed; round 2 raised one `claim-missing` (the retirement's mechanism and
  release attribution), fixed by the three rows.
- **Completed checks** — `openspec validate --all` exits 0; `spotlessCheck` passes; `--review-ready` reports the base
  contained and the index current.
- **Known limitations** — unchanged from round 1.

## Round 4 — implementation screen, after round 3's finding

- **Unit, tree, branch, base** — unchanged; base `0deca079`; nothing committed; the unit's files staged, this section
  included.
- **Packs** — none, for the same reason as round 1.
- **Supplied artifacts** — round 3's fix: the `v1.16.0` containment row's source is now the compare endpoint against
  `#2127`'s merge commit, reproducing `status: ahead`, `behind_by: 0`; no other artifact changed.
- **Scope** — the same bounded screen as round 1.
- **Prior findings** — rounds 1–2's `claim-missing` and `missing-task` are fixed; round 3 raised `claim-unreproducible`
  (the release row's source did not prove containment), fixed by the compare endpoint.
- **Completed checks** — `openspec validate --all` exits 0; `spotlessCheck` passes; `--review-ready` reports the base
  contained and the index current.
- **Known limitations** — unchanged from round 1.

## `advisor-docs-unit-readiness` — implementation readiness

- **Slug** — `advisor-docs-unit-readiness`.
- **Owner's authorization** — the owner's instruction of 2026-11-09 to review with the technical advisor until
  agreement, on the split units; this consultation covers the docs unit.
- **Owner's objective and constraints** — apply the 2026-11-07 audit report's config-guard finding as a docs/consistency
  unit, split from the spec-corpus fixes (`apply-2026-11-07-audit-findings`) per `showcase/quality/agent-skills`; the
  full review gate runs, and nothing is committed before the owner's manual review pass.
- **Question** — is this unit's implementation correct, complete, and proportionate to that objective, so the next
  action is the owner's manual review pass — or does it carry a defect or unnecessary scope the evidence establishes?
- **Change identity and stage** — `apply-2026-11-07-audit-doc-findings`; Implementation review boundary (quick round 7
  closed clean; routing `RECOMMENDED` on the repeated-class signal, owner resolution pending).
- **Artifacts and diff** — tree `/Users/afanasyev/Development/axon-showcase-apply-2026-11-07-audit-doc-findings` (the
  consultation's workspace); branch `apply-2026-11-07-audit-doc-findings`; base `caf72739` after two fast-forwarded
  advances; staged set 18 files, +383/−10. The substantive edit is `AGENTS.md` (one bullet); the rest is the change dir
  (`skip_specs`) and the unit's records.
- **Pre-change excerpt — the old bullet** (`git show 634bc3b5:AGENTS.md`): "Each guard retires on its own condition:",
  listing the `could not parse` arm and the `must be an array of strings` arm as live, with the `#1984`/`#1894` split in
  the `#1891` sentence.
- **Review evidence and dispositions** — rounds 1–7: round 1 three `claim-missing` and one `missing-task`, round 2 one
  `claim-missing`, round 3 one `claim-unreproducible`, round 4 clean, round 5 one `claim-unreproducible`, round 6 one
  `claim-unreproducible`, round 7 clean; every finding is fixed. Routing `RECOMMENDED` on three repeated-class rows; one
  process-defect record (the claims table's seeding).
- **Unresolved disagreements** — none known to the caller.
- **Subject packs** — none: the change edits one guidance bullet, taking no JVM, test, Gradle-build, or deployment
  decision.
- **Report orientation and language** — owner-facing; English.
- **Effort override** — none.
- **Fresh or follow-up** — fresh consultation.
- **Report** —
  `Recommendation: Retain the correction and ask the owner to skip the recommended deep review before requesting manual review. 3 observations, 0 confirmed problems, 0 hypotheses, 1 open question.`
  The correction serves the objective and matches the existing mechanism (`ci.yml:108–123` and the archived probe
  evidence); the scope is proportionate, and the parked decision already distinguishes the retired and remaining guards;
  the repeated findings were evidence-attribution defects in the records, not in the guidance correction. The open
  question is the owner's skip decision for the `RECOMMENDED` deep review.

## Round 5 — implementation screen, after the base advance (`#873`)

- **Unit, tree, branch, base** — base fast-forwarded to `634bc3b5` (`#873`), which changes this unit's own `AGENTS.md`
  bullet by adding the `#1984` fact to the `#1891` sentence; the stash → fast-forward → stash-pop conflict was resolved
  by keeping the retirement rewrite and the new fact together. Nothing committed; the unit's files staged, this section
  included.
- **Packs** — none, for the same reason as round 1.
- **Supplied artifacts** — the reconciled `AGENTS.md` bullet (retired arm plus the `#1984`/`#1894` split), and
  `design.md` re-anchored to `634bc3b5` with a new row for `#1984`; the rest is unchanged.
- **Scope** — the same bounded screen as round 1.
- **Prior findings** — rounds 1–3's findings are fixed; round 4 reports `nothing remains`. The base advance is new
  evidence the round must cover.
- **Completed checks** — `openspec validate --all` exits 0; `spotlessCheck` and `verifyCapturedMarkers` pass;
  `--review-ready` reports the base `634bc3b5` contained and the index current.
- **Known limitations** — unchanged from round 1.

## Round 6 — implementation screen, after round 5's finding

- **Unit, tree, branch, base** — unchanged; base `634bc3b5`; nothing committed; the unit's files staged, this section
  included.
- **Packs** — none, for the same reason as round 1.
- **Supplied artifacts** — round 5's fix: the `#1984` row's source now requests `title,state,mergedAt,body,files` and
  its `Reproduces` cell records the state, date, title, the body's "Parsing behavior is unchanged" statement, and the
  full file set; `process-defects.md` records round 5's instance and `deep-review.md` records the routing reassessment
  (`NOT_NEEDED` at the new base). No other artifact changed.
- **Scope** — the same bounded screen as round 1.
- **Prior findings** — rounds 1–3's findings are fixed; round 4 is clean; round 5 raised `claim-unreproducible` on the
  new row, fixed.
- **Completed checks** — `openspec validate --all` exits 0; `spotlessCheck` and `verifyCapturedMarkers` pass;
  `--review-ready` reports the base `634bc3b5` contained and the index current.
- **Known limitations** — unchanged from round 1.

## Round 7 — implementation screen, after round 6's finding

- **Unit, tree, branch, base** — unchanged; base `634bc3b5`; nothing committed; the unit's files staged, this section
  included.
- **Packs** — none, for the same reason as round 1.
- **Supplied artifacts** — round 6's fix: `deep-review.md` records the re-derived `RECOMMENDED` outcome with its
  `[repeated-class] 3 rows repeat a prior class` signal and the pending owner resolution; no implementation artifact
  changed.
- **Scope** — the same bounded screen as round 1.
- **Prior findings** — rounds 1–3's findings are fixed; round 4 is clean; rounds 5–6 raised `claim-unreproducible`, both
  fixed (the `#1984` row's source, then the deep-review record's outcome).
- **Completed checks** — `openspec validate --all` exits 0; `spotlessCheck` and `verifyCapturedMarkers` pass; the
  routing re-derivation reports `RECOMMENDED`; `--review-ready` reports the base `634bc3b5` contained and the index
  current.
- **Known limitations** — unchanged from round 1.
