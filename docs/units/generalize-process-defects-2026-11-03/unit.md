# Unit: generalize-process-defects-2026-11-03

Workspace:
`unit=generalize-process-defects-2026-11-03; path=/Users/afanasyev/Development/axon-showcase; branch=generalize-process-defects-2026-11-03; writer=ses_e5dece39fffeYkl8XkXm6Yg9Q3`

State: closed

A guidance unit with no change directory: it analyzed the 44 process-defect records that carried no `Decision` line,
applied the improvements the owner approved, and appended a `Decision` line to each record.

## The selection

44 records across 22 units, read from `docs/units/<unit>/process-defects.md` at `origin/main` `ca9e7dcb`. 233 records
exist; 189 already carried a `Decision` line and 17 record files declare that no defect was observed. The unit count is
the distinct `docs/units/<unit>/` directories the 44 records' ids name
(`python3 … | sed -E 's/#[0-9]+$//' | sort -u | wc -l` → `22`).

## Dispositions

- 29 `no-change` — an existing rule already records the incident, or the record's evidence establishes no additional
  guidance need.
- 4 `no-change: already parked` — `bound-self-learning-loop-consolidation#1` and `discover-paused-units#1` against the
  `docs/ideas.md` 2026-11-03 entry this unit created; `record-markdown-lint-decision#3` against the 2026-10-22
  reviewer-wrapper entry; `split-the-review-ledger#3` against the 2026-10-27 reply-language entry it re-parked.
- 11 `proposed` — 4 records feed P6, 5 feed A1, 1 feeds P4, 1 feeds P2. A record feeding an applied change carries
  `proposed` with the site the change landed in.
- `record-ast-grep-skill-usage#2` carries a parked ledger-row half and is decided `no-change` for its other half.

## Applied

- **P2** — `AGENTS.md`, the parsed-output rule: "take the tool's machine-readable form" replaces the git-only `-z`
  remedy, and the clause is net zero lines. Extension of the existing rule, not a new one.
- **P4** — `check-and-test-authoring`, a restore-from-snapshot bullet in the control section, verifying the restored
  content against the snapshot rather than grepping one token.
- **P6** — `review-gate` §2, a mid-loop re-stage bullet that applies §5's explicit-add remedy after each round's fixes.
- **A1** — `openspec/config.yaml` `rules.tasks`, each review-gate stage as its own task, pointing at the owning skill.
- **R3** — `docs/ideas.md` 2026-10-27 reply-language entry, third instance (`split-the-review-ledger#3`).
- **R1** — `docs/ideas.md` 2026-11-03, the corrected-scope consultation parked for a spec amendment rather than a rule.
- 44 `Decision` lines.

## Declined, with the reasons recorded on the records

- **P1** (interpreter as a reproduction context) — already covered by "The binary is part of that context".
- **P3** (a delta holds exactly the altered requirements) — already covered by the programmatic-edit rule's
  verify-at-the-granularity-edited and by the spec-delta workflow's block copying.
- **P5** (an exemption is always its last exempt case) — the archived design contradicts the universal form.
- **R2** (a repeated module-level binding) — a check's subject, one occurrence, caught by an existing control.

## Applied net delta

`AGENTS.md` net **0 lines** (2 added, 2 removed after the advisor required the replacement not to grow the file),
measured with `git diff --numstat` after `spotlessApply`. The skill and config additions are not `AGENTS.md` growth:
`check-and-test-authoring` +11, `review-gate` +5, `openspec/config.yaml` +4, `docs/ideas.md` +24 (the re-park and the
new parked entry).

## Open item at merge

Every `proposed` `Decision` line names the site it landed in, without a link, because no pull request exists while the
lines are authored. This unit's pull request link is added to each of those lines before the merge, the bootstrap the
`docs/ideas.md` 2026-11-01 "Gate a `Decision` line's link with a check" entry records.
