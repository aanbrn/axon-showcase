# Design

## Context

§5's readiness bullet lists the remedies a failed `--review-ready` names; the branch-refresh reply is the fourth of
five, and it says refresh, re-stage, re-run. A refresh can rewrite a unit file the closing quick round had reviewed —
the recorded incident hand-resolved two conflicts and asked for the manual pass without a round over the result. The
obligation already exists in `/sync-branch`'s lifecycle; the gap is that §5, the boundary the agent reads at the ask,
does not point at it.

## Goals / Non-Goals

**Goals:**

- Make the return to the quick round an explicit step of the manual-pass boundary.
- Point at the lifecycle line rather than restating the sync procedure.

**Non-Goals:**

- Any change to `/sync-branch`'s procedure, to the refresh mechanism, or to `--review-ready`.
- A check that could observe the omission mechanically: the gate's own steps are the agent's, and no in-repo check reads
  which round covered which tree.

## Decisions

1. **Place the branch in §5, not in the sync command.** The ask is where the omission happened and §5 is the only place
   the agent reads at that moment; the lifecycle line stays the obligation's owner.
2. **Re-open the loop only when the refresh changed a reviewed file.** Requiring a round for every refresh would spend a
   review cycle on an unchanged tree.
3. **Point, do not restate.** The branch names `/sync-branch`'s lifecycle line, so the sync procedure keeps one owner.
4. **A `MODIFIED` delta on `agent-skills`.** Its loop requirement states the run points and the
   repeat-before-the-request condition, which the new run point refines; the step correction itself lands in the skill,
   as that capability's maintenance requirement mandates.

## Risks / Trade-offs

- **The branch is procedural, so nothing observes it.** A unit that refreshes and asks anyway still resembles a
  conforming one; the review gate's own rounds are the only evidence, as before.
- **A re-opened loop for an unchanged tree:** a hand-resolved conflict can land the same text the round judged, so the
  return costs a round for no new judgment; the wording keys on "changed a file the closing round reviewed", which the
  agent judges from the diff.

## Verification

- `./gradlew spotlessApply`/`spotlessCheck` and the Docker-free `check` (the skill is markdown under Spotless).
- A read of §5 end to end against the recorded incident's sequence.
- `openspec validate --all`, and `commit-hygiene.py --markers` / `--ideas-sections` after the idea removal.

## Claims

| Claim                                        | Source                                                                                                                                                                                                                                              |
| -------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| The incident happened as stated              | `docs/process-defects/retry-infra-verification-fetches.md#1`'s `Observed` and `Evidence` fields (quick round 4 → sync with two hand-resolved conflicts → ask → round 5)                                                                             |
| The lifecycle already owns the obligation    | `.opencode/commands/sync-branch.md`, its "The full per-unit review gate runs over the result" line                                                                                                                                                  |
| §5 was silent on the return                  | the section's readiness bullet at `57f416a0` (unchanged from `de9c0273`), whose fourth of five remedies is the branch-refresh reply (refresh, re-stage, re-run); a fifth remedy follows it                                                          |
| The behavior refines an existing requirement | `showcase/quality/agent-skills`'s requirement "The quick review loop runs on every unit" states the run points and the repeat-before-the-request condition; the delta adds the boundary-refresh run point and one scenario, carrying the other four |
| The change is a step correction in the skill | `showcase/quality/agent-skills`'s "The review gate's caller procedure is maintained in its project skill" requirement                                                                                                                               |

**Dismissals.** `scripts/claim-scan.py --base <unit base>` seeds the table; its `[path]`, `[count]`, `[absolute]` and
`[cross-ref]` candidates are resolved by the row that states them or by the gate/lifecycle they name, and any candidate
no row covers is dismissed at its kind rather than member by member.
