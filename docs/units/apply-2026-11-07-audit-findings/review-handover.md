# Review hand-over — apply-2026-11-07-audit-findings

## Round 1 — implementation screen

- **Unit, tree, branch, base** — `apply-2026-11-07-audit-findings`; tree
  `/Users/afanasyev/Development/axon-showcase-apply-2026-11-07-audit-findings` (the unit's sibling worktree); branch of
  the same name, forked `--no-track` from `origin/main` `21b100bf`; nothing committed; the unit's files staged.
- **Packs** — none: the unit edits guidance prose and a spec's structure, and takes no JVM, test, Gradle-build, or
  deployment decision.
- **Supplied artifacts** — `openspec/changes/apply-2026-11-07-audit-findings/` (`proposal.md`, `design.md`, `tasks.md`,
  `.openspec.yaml`, `specs/showcase/quality/commit-hygiene/spec.md`),
  `docs/units/apply-2026-11-07-audit-findings/unit.md`, and the staged diff against `origin/main` (an `AGENTS.md` bullet
  plus the change dir).
- **Scope** — the implementation: the `AGENTS.md` bullet rewrite and the two `MODIFIED` requirement blocks; the
  proposal/design/tasks coherence with them; the claims table in `design.md`.
- **Prior findings** — none; this is the unit's first round.
- **Completed checks** — `openspec validate --all` exits 0; `openspec validate apply-2026-11-07-audit-findings --strict`
  reports the change valid; `./gradlew verifyCapturedMarkers` passes; `spotlessApply` has run after the last edit; each
  claims-table row was re-run at `21b100bf`.
- **Known limitations** — the report's `handlebars` advisory is out of scope (the separate unit
  `remove-dead-handlebars-constraint`), and the CI probe behavior is unchanged, so no workflow file is in the diff.

## Round 2 — implementation screen, after round 1's two findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `21b100bf`; nothing committed; the unit's files staged
  and formatted.
- **Packs** — none, for the same reason as round 1.
- **Supplied artifacts** — round 1's fixes: `design.md` gains two claims-table rows (the tracker state of `#1894` and
  the 2026-09-28 precedent), and D3 now cites commit `7545efe8`; no other artifact changed.
- **Scope** — the same bounded screen as round 1.
- **Prior findings** — round 1 raised two `claim-missing` findings: (1) `AGENTS.md:2525` — the claim that `#1894` is
  still open and unmerged had no claims-table row; (2) `design.md:40` — D3's precedent claim had no row. Both are rows
  now.
- **Completed checks** — `openspec validate --all` exits 0; the change validates strictly; `verifyCapturedMarkers`
  passes; `spotlessApply` has run after the last edit.
- **Known limitations** — unchanged from round 1.

## Round 3 — implementation screen, after the base advance

- **Unit, tree, branch, base** — unchanged tree and branch; `origin/main` advanced to `2b4c9a00` and the tree
  fast-forwarded onto it with the unit's work still uncommitted; nothing committed; the unit's files staged.
- **Packs** — none, for the same reason as round 1.
- **Supplied artifacts** — round 2's tree plus one `design.md` change: the claims table's base line names `21b100bf` as
  the fork point and records that `origin/main` advanced to `2b4c9a00` without touching the rows' subjects, with a row
  sourcing that advance. No other artifact changed.
- **Scope** — the same bounded screen as round 1.
- **Prior findings** — round 1's two `claim-missing` instances are fixed; round 2 raised none.
- **Completed checks** — `commit-hygiene.py --review-ready` reports the base `2b4c9a00` contained in `HEAD` and the
  index current; `openspec validate --all` exits 0; `spotlessApply` has run after the last edit.
- **Known limitations** — unchanged from round 1.

## `advisor-implementation-readiness` — implementation readiness

- **Slug** — `advisor-implementation-readiness`.
- **Owner's authorization** — the owner's explicit instruction of 2026-11-09: consult the technical advisor and iterate
  until it agrees, on this unit's implementation. The same owner authorized the unit and its scope earlier.
- **Owner's objective and constraints** — apply the 2026-11-07 audit report's approved findings (the stale config-guard
  bullet in `AGENTS.md`; two structural spec defects in `showcase/quality/commit-hygiene`) as one unit, under the full
  review gate, with the `handlebars` advisory split into its own unit. Nothing is committed, pushed, archived, or merged
  before the owner's manual pass; the manual pass is the next stage.
- **Question** — is this implementation correct, complete, and proportionate to that objective, so that the next action
  is the owner's manual review pass — or does it carry a defect or unnecessary scope the evidence establishes?
- **Change identity and stage** — `apply-2026-11-07-audit-findings`; stage: Implementation review boundary (quick review
  closed clean, routing `NOT_NEEDED`).
- **Artifacts and diff** — tree `/Users/afanasyev/Development/axon-showcase-apply-2026-11-07-audit-findings`; branch
  `apply-2026-11-07-audit-findings`; fork point `21b100bf`, fast-forwarded to `2b4c9a00`; staged set 15 files, +542/−9
  at the time of the consultation. The substantive edits are `AGENTS.md` and the delta at
  `openspec/changes/apply-2026-11-07-audit-findings/specs/showcase/quality/commit-hygiene/spec.md`; the rest is the
  change's planning artifacts and the unit's records. The tree is readable directly; the pre-change excerpts follow.
- **Pre-change excerpt — the old bullet** (`git show 21b100bf:AGENTS.md`): it states "Each guard retires on its own
  condition:" and lists both arms as live — the `could not parse` arm "retired when this repository's pin reached
  `v1.16.0`" and the `must be an array of strings` arm "once a release carries `#1894`".
- **Pre-change excerpt — the two spec defects**
  (`git show 21b100bf:openspec/specs/showcase/quality/commit-hygiene/spec.md`): (1) the sentence
  `That input clause scopes the build's verification.` stands alone between two SHALL paragraphs; (2) the paragraph
  "`caller-restarted` SHALL record a continuity violation, not an additional permitted recovery case; the verification
  SHALL validate the recorded fields, and passing it SHALL NOT attest that the stated cause occurred or authorize the
  review gate to continue." sits after the requirement's final scenario.
- **Review evidence and dispositions** — quick round 1 raised two `claim-missing` findings, both fixed (claims-table
  rows for `#1894`'s tracker state and for the 2026-09-28 precedent); rounds 2 and 3 report `nothing remains`.
  `openspec validate --all` 0 failed, `--strict` valid, `spotlessCheck`, `verifyCapturedMarkers`, `--expect-clean`, and
  `--review-ready` green; routing `NOT_NEEDED`; one process-defect record (a generated delta block lost a paragraph
  break, caught by the diff and regenerated).
- **Unresolved disagreements** — none known to the caller.
- **Subject packs** — none: the change edits guidance prose and a spec's structure, and takes no JVM, test,
  Gradle-build, or deployment decision.
- **Report orientation and language** — owner-facing; English.
- **Effort override** — none.
- **Fresh or follow-up** — fresh consultation.
- **Report** —
  `Recommendation: resolve the unit-splitting conflict before the manual review pass. 3 observations, 1 confirmed problem, 0 hypotheses, 1 open question.`
  The confirmed problem: the unit batches the config-guard correction — which the report routes as a small
  docs/consistency unit — with the `commit-hygiene` spec corrections, which need a delta, and
  `showcase/quality/agent-skills`'s "An audit's approved findings are delivered as separate units" forbids batching
  findings whose routes or scopes differ. The open question: whether the owner authorized an exception to that rule. The
  advisor would recommend proceeding to the manual pass if the owner confirms the exception, and otherwise recommends
  splitting the unit. Its observations record that the AGENTS.md correction matches the existing mechanism, the delta
  preserves the affected contracts, and round 1's two findings are addressed.

## Round 4 — implementation screen, after the owner split the audit findings

- **Unit, tree, branch, base** — unchanged tree and branch; base `2b4c9a00`; nothing committed; the unit's files staged.
- **Packs** — none, for the same reason as round 1.
- **Supplied artifacts** — the re-scoped tree: `AGENTS.md` is back at the base (the guidance correction moved to
  `apply-2026-11-07-audit-doc-findings`), and `proposal.md`, `design.md`, `tasks.md`, `unit.md`, and the claims table
  now describe only the `commit-hygiene` delta. The delta itself is unchanged since round 3.
- **Scope** — the same bounded screen as round 1, with attention to re-scope residue.
- **Prior findings** — round 1 raised two `claim-missing` instances, both fixed; rounds 2 and 3 raised none. The
  advisor's confirmed problem — the unit batched findings with different routes — is resolved by the owner's split
  decision.
- **Completed checks** — `openspec validate --all` exits 0; `spotlessCheck` passes; `--review-ready` reports the base
  contained and the index current; `git diff --cached -- AGENTS.md` is empty.
- **Known limitations** — unchanged from round 1.

## Round 5 — implementation screen, after round 4's two findings

- **Unit, tree, branch, base** — unchanged; the tree fast-forwarded again to base `0deca079`, whose advance touches
  build and skill files this unit does not change; nothing committed; the unit's files staged, this section included.
- **Packs** — none, for the same reason as round 1.
- **Supplied artifacts** — round 4's fixes: `review-handover.md` re-staged, and `tasks.md`'s 3.4 and 3.5 unticked until
  the re-scoped review closes and the manual pass is re-asked; no implementation artifact changed.
- **Scope** — the same bounded screen as round 1.
- **Prior findings** — round 4 raised `claim-unreproducible` (the hand-over was staged then edited) and `missing-task`
  (3.4 and 3.5 ticked early); both are fixed.
- **Completed checks** — `--review-ready` passes with this section staged; `openspec validate --all` exits 0;
  `spotlessCheck` passes; `git diff --cached -- AGENTS.md` is empty.
- **Known limitations** — unchanged from round 1.

## `advisor-split-resolution` — split follow-up

- **Slug** — `advisor-split-resolution`.
- **Owner's authorization** — the same owner instruction of 2026-11-09 (consult the advisor until agreement), continued
  on the advisor session after the owner's split decision.
- **Owner's decision** — split the audit findings by route (the advisor's recommendation), not an exception to
  `showcase/quality/agent-skills`.
- **What changed** — this unit is now spec-only: `AGENTS.md` is at its base, the proposal/design/tasks/unit record and
  the claims table describe only the `commit-hygiene` delta, and quick round 5 over the re-scoped tree reports
  `nothing remains`. The config-guard correction is now the unit `apply-2026-11-07-audit-doc-findings`, which carries
  its own branch, change dir (`skip_specs`), claims table, and a four-round quick loop that closed clean.
- **Question** — does the split resolve the advisor's confirmed problem, and is the owner's manual review pass the next
  action for both units?
- **Fresh or follow-up** — follow-up on the same decision.
- **Report** —
  `Recommendation: proceed to the owner's manual review pass for both units. 2 observations, 0 confirmed problems, 0 hypotheses, 0 open questions.`
  The prior batching problem is resolved: the spec unit excludes the guidance correction and `AGENTS.md` is back at the
  base, which satisfies `showcase/quality/agent-skills`; the re-scoped review closed clean. The advisor could not open
  the separate unit's tree (`external_directory`), so its judgment of that unit rests on the supplied hand-over.

## Round 6 — implementation screen, after the second base advance (`#873`)

- **Unit, tree, branch, base** — base fast-forwarded to `634bc3b5` (`#873`), whose advance changes `AGENTS.md` and
  build/skill files this unit does not edit, and leaves the `commit-hygiene` spec untouched; nothing committed; the
  unit's files staged, this section included.
- **Packs** — none, for the same reason as round 1.
- **Supplied artifacts** — `design.md` re-anchored to `634bc3b5` and its advance row updated to
  `git diff --name-only 21b100bf 634bc3b5` (sixty-nine paths, none the `commit-hygiene` spec); `deep-review.md` records
  the routing reassessment (`NOT_NEEDED` at the new base); the delta is unchanged.
- **Scope** — the same bounded screen as round 1.
- **Prior findings** — round 1's two `claim-missing` are fixed; rounds 2–3 clean; round 4's `claim-unreproducible` and
  `missing-task` are fixed; round 5 clean. The second base advance is new evidence this round must cover.
- **Completed checks** — `openspec validate --all` exits 0; `spotlessCheck` and `verifyCapturedMarkers` pass;
  `--review-ready` reports the base `634bc3b5` contained and the index current.
- **Known limitations** — unchanged from round 1.
