# Process defects — sharpen-readme-promises-and-routes

### sharpen-readme-promises-and-routes#1 — The unit branched inside an occupied primary checkout, carrying another unit's staged work

**Recorded** — 2026-10-29, by the unit's writing session.

**Stage** — implementation.

**Expected** — `AGENTS.md`'s Workspace rule: a new unit that finds the primary checkout assigned to another unit works
in a sibling worktree, created from a freshly fetched `origin/main`.

**Observed** — the primary's `git status` already listed staged paths for `widen-the-claim-scanner-absences`, and the
same command chain then ran `git switch -c sharpen-readme-promises-and-routes` there. The other unit's staged set rode
onto the new branch, the failure the Workspace rule's own decision point describes.

**Evidence** — the pre-switch status listed `docs/ideas.md`, `docs/process-defects/widen-the-claim-scanner-absences.md`,
`docs/review-rounds.tsv`, `openspec/changes/widen-the-claim-scanner-absences/`, `scripts/claim-scan.py` and
`scripts/test-claim-scan.py` as staged; the post-switch status carried the same paths on the new branch; the repair
ended with `git branch -D sharpen-readme-promises-and-routes` reporting `(was f2fae423)`, and `git worktree list`
showing the unit in `../axon-showcase-sharpen-readme-promises-and-routes`.

**Consequence** — one switch and one branch creation undone; the other unit's staging was restored byte for byte (its
status matched the pre-switch list), and the unit then took its own worktree. No work was lost.

**Knowledge boundary** — observed: both statuses, the branch deletion and the worktree registry. Not checked: whether a
session for the other unit was still writing to that tree while the switch ran.

- **Decision** — no-change: the Workspace rule already determines the action; the second occurrence questions
  compliance, not the rule's text.

### sharpen-readme-promises-and-routes#2 — Three edit anchors matched nothing because the formatter had re-wrapped them

**Recorded** — 2026-10-29, by the unit's writing session.

**Stage** — implementation.

**Expected** — an anchor for a prose edit is taken from a read that follows the formatter run, or is short enough to sit
on one source line.

**Observed** — three replacement scripts anchored on multi-line paragraphs (the Architecture CQRS paragraph, the
Develop-from-Source paragraph, and the intro boundary sentence) and aborted with a zero match count, because
`spotlessApply` had re-wrapped those paragraphs since the last read. Each script wrote nothing, so no partial state
resulted; the successful attempts anchored on a single line's distinctive substring.

**Evidence** — the aborted runs printed `anchor count 0: "The command's answer is not the read model's. \`POST
/showcas…"`and`anchor count 0: 'The compose stack runs the application as pre-built containers. To
dev…'`against text that the rendered README shows; the next attempts replaced one line-anchored substring each and reported`applied`.

**Consequence** — three wasted script runs and one re-read of the two paragraphs. Nothing was corrupted.

**Knowledge boundary** — observed: the anchors, their counts, and the re-wrapped source. Not checked: whether the same
class sits in another live script that anchors on wrapped prose; `widen-readme-auditor-with-language-axis#3` records the
same shape from an earlier unit in this repository.

- **Decision** — no-change: already parked — the `docs/ideas.md` 2026-10-22 'Route the wrapped-prose gotcha' entry.
