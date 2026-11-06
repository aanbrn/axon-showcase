# Review hand-over — park-local-definition-probe

## Round 1 — implementation screen (docs unit)

- **Unit, tree, branch, base** — `park-local-definition-probe`; tree `/Users/afanasyev/Development/axon-showcase` (the
  primary checkout); branch of the same name forked `--no-track` from `origin/main` `5f67d6d1`; nothing committed; the
  work is staged, and `git status` names the set.
- **Packs** — none: the unit parks one idea in `docs/ideas.md` and writes its own unit record. No JVM code, test, Gradle
  build decision, or deployment is touched — though the idea proposes build work of its own.
- **The change** — a new `## 2026-11-07` section with one entry: move the OpenCode definition-inventory probe from the
  `build` workflow's step into `check`, because it is the only check that a definition is registered —
  `make-unit-lifecycle-explicit` shipped a skill without frontmatter, every local gate passed, and only CI reported
  `agents=11 commands=31 skills=21` and the absent-skill error. The entry names what a `check`-time variant needs (the
  v1 binary or a committed inventory fixture, a control, its own delta or check-control) and the unverified part
  (whether the v2 binary the local workflow runs resolves definitions the same way).
- **Prior findings** — none; this is the unit's first round.
- **Completed checks** — `spotlessApply` ran; `verifyIdeasSections` passes (the new dated section carries an entry);
  `verifyUnitLayout` passes; `commit-hygiene --markers` and `--ideas-sections` pass.
- **Known limitations** — the entry states the v2-binary question as unverified rather than asserting either way, and
  names no cost or schedule.

## Round 2 — implementation screen, after round 1's three findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `5f67d6d1`; nothing committed; the work is staged, and
  `git status` names the set.
- **Packs** — none, for the same reason as round 1.
- **Supplied artifacts** — round 1's fixes: the idea entry cites run `42250066417` and says its
  `agents=11 commands=31 skills=21` line counts the tree's definition files rather than the loader's inventory, and the
  same correction is applied in place to the merged unit's record; the unit's own `process-defects.md` exists and states
  that it observed no process defect; and the entry names the `MODIFIED` delta on `showcase/quality/merge-governance` as
  owed, beside the known-bad control and the acquisition decision.
- **Prior findings with their instances** — round 1's three, in `findings/round-1.md`.
- **Completed checks** — `spotlessApply` ran; `verifyIdeasSections`, `verifyUnitLayout`, and `verifyFindingInstances`
  pass; the ledger's `--expect 1` passes.
- **Known limitations** — the entry states the v2-binary question as unverified; the ledger's round-1 compliance-miss
  count was corrected from 2 to 3 to match that round's report.

## Round 3 — implementation screen, after round 2's two findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `5f67d6d1`; nothing committed; the work is staged.
- **Packs** — none, for the same reason as round 1.
- **Supplied artifacts** — round 2's fixes: this file now carries both missing round sections (round 2's own was
  recorded after the omission), and the ledger's round-1 compliance-miss count matches that round's report (`3`).
- **Prior findings with their instances** — rounds 1 and 2, in `findings/`.
- **Completed checks** — `spotlessApply` ran after the edits; the ledger's `--expect 2`, `--finding-instances`, and
  `--session-continuity` pass; `--review-ready` confirms the base and the index.
- **Known limitations** — the entry's v2-binary question remains unverified by design.
