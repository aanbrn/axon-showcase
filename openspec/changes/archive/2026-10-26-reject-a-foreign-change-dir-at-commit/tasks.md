# Tasks

## 1. The assignment

- [x] 1.1 `scripts/commit-hygiene.py` — resolve the claim at `git rev-parse --git-path axon-showcase-change`, read its
      single line, and treat the literal `none` as a declaration that the unit has no change dir.
- [x] 1.2 `scripts/commit-hygiene.py` — derive the assignment with one precedence: the branch's name when it is a change
      name (live under `openspec/changes/<name>/` **or** archived under `openspec/changes/archive/<date>-<name>/`, so
      the archive commit still resolves after the directory moves); otherwise the claim's line; a failure naming the
      missing or contradictory source when neither resolves, or when a claim and a branch name two different changes —
      while a `none` claim contradicted by a change-named branch names no change, so the branch's assignment stands.
- [x] 1.3 `AGENTS.md` — the `Workspace` rule's record gains the claim: the fixed git path, the one-line content (a
      change name or `none`), and the branch-named case where it may be omitted; the rule's release definition gains a
      pointer to the settle step that removes it.
- [x] 1.4 `AGENTS.md` — a continued unit records the claim when it establishes its workspace: a branch that is not a
      change name, an `opencode/…` continuation included, writes it before its first commit. That is the bootstrap, so
      no separate migration is owed.
- [x] 1.5 `.opencode/skills/ship-change/SKILL.md` — the settle step removes the primary's claim when the tree is
      released; a sibling worktree's claim disappears with `git worktree remove`, and no one clears a neighbour's.

## 2. The check

- [x] 2.1 `scripts/commit-hygiene.py` — refuse a staged path under `openspec/changes/<name>/` whose `<name>` is not the
      assigned change, whether it is added, modified, or deleted, and including when it is the only change directory
      staged; name the path and the assignment.
- [x] 2.2 `scripts/commit-hygiene.py` — read the deletions and the rename sources under `openspec/changes/` as well,
      since the existing reader filters to added, copied, modified and renamed paths and a rename hides its old name.
- [x] 2.3 `scripts/commit-hygiene.py` — accept the assigned change's archive transition by comparing the deleted
      `openspec/changes/<name>/` and the added `openspec/changes/archive/<date>-<name>/` by change identity, and refuse
      any other change's archived path.
- [x] 2.4 `scripts/commit-hygiene.py` — when the claim declares `none`, refuse any staged path under
      `openspec/changes/`, naming the declaration.
- [x] 2.5 `scripts/commit-hygiene.py` — leave paths outside `openspec/changes/` out of the ownership check.
- [x] 2.6 Every enumeration of the guard's checks gains the new member: the module docstring and the header of
      `scripts/git-hooks/pre-commit`, `AGENTS.md`'s gotcha listing what the guard refuses, and the README's count and
      list of the slips.
- [x] 2.7 `docs/adr/0024-tree-write-right-is-not-session-bound.md` — append a **dated amendment** (the 2026-10-26 record
      stays as written) saying the Context's "has no notion of path ownership" no longer holds, and that the staged-set
      check was rejected **as a prohibition substitute** and is now adopted as the consequence check, the Decision
      standing unchanged.

## 3. Tests

- [x] 3.1 `scripts/test-commit-hygiene.py` — the assigned change's own directory passes, and its archive move passes in
      both path shapes (a detected add and a detected rename), including a branch-named assignment with no claim, which
      is the shape the archive commit itself has.
- [x] 3.2 `scripts/test-commit-hygiene.py` — another change's directory fails, including as the only change dir staged;
      another change's archived path fails; deleting another change's directory without archiving it fails; and the
      assigned change's own file deletion passes, whether it drops a file the change no longer needs or carries it into
      the archive.
- [x] 3.3 `scripts/test-commit-hygiene.py` — a missing assignment fails with the message naming how to record it, and
      writing the claim then resolves the same staged set; a claim and a branch naming different changes fail; a `none`
      claim with a staged change path fails; and a `none` claim on a branch whose name is a change name leaves that
      branch's assignment standing, so the change's own directory passes.
- [x] 3.4 `scripts/test-commit-hygiene.py` — a staged set with no `openspec/changes/` path passes once the assignment
      resolves, and a branch-named change needs no claim; an unresolved assignment still fails such a set, which is what
      the fail-closed default means.
- [x] 3.5 `scripts/test-commit-hygiene.py` — the shared fixture records the assignment `demo`, which the two archive
      cases already stage (`openspec/changes/archive/2026-01-01-demo/…`), so their exact `1`/`0` counts still measure
      what they test; the assertions stay exact rather than `!= 0`.

## 4. Verification

- [x] 4.1 `./gradlew testCommitHygiene` green.
- [x] 4.2 `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` green, proving the `check` task's behaviour is
      unchanged.
- [x] 4.3 Positive control in a scratch clone: stage another change's directory and confirm the commit is refused naming
      both, then stage the assigned change's and confirm it passes.
- [x] 4.4 `./gradlew spotlessApply` after the final edit, then `spotlessCheck`, and
      `python3 scripts/commit-hygiene.py --markers`.
- [x] 4.5 `openspec validate --all` green.
- [x] 4.6 Record the pre-write derivation pass and re-derive its claims table after the final edit.

## 5. Review gate and delivery

- [x] 5.1 Quick-review loop clean, with a row per round in `docs/review-rounds.tsv` and both ledger checks after each,
      the closing round confirmed with `--expect-clean`. **Resume here:** the loop closed on a clean round, after the
      branch was refreshed onto `origin/main`'s `pause-resume-unit` merge — a 3-way merge over `AGENTS.md`, `README.md`
      and `docs/review-rounds.tsv`, clean in the first two and resolved as a union of both sides' rows in the third. The
      confirmation round after the refresh found nothing. Remaining: 5.5's archive, after the owner's manual pass.
- [x] 5.2 Deep-review routing decision recorded with its outcome. Routing: `RECOMMENDED`, nine rows repeated a prior
      class; the owner resolved to run the pass on the advisor's recommendation (the assignment model and the
      completeness of the staged-transition reads). **Pass 1 of 2** (initial review): one `should-fix` — the delta's
      rename-away scenario had no test, since the R→D downgrade a source-only pathspec produces was covered only by a
      `git rm` case — and one `nitpick`, the boundary clause being narrower than the classifier. Fixed: a pinning test
      (`git mv` out of the prefix → refusal) and the clause restated by what it names. **Pass 2 of 2** (verification):
      both confirmed; one `nitpick` of its own — the restated clause collided with the `none` sentence — fixed by
      scoping it to the attribution. No residual `should-fix`; the pass budget is spent.
- [x] 5.3 The unit's own capture decision recorded — its applied delta, or the skip and its reason. Done: 2 durable
      additions applied — `review-gate` §5's repeated-class bullet now names the model as the unit to re-derive (+5),
      and `AGENTS.md`'s ADR clause gains the dated `## Amendment` form (+2); 0 retirements. The guard's authority model
      needed no rule (delta + ADR amendment own it), and the archive-commit incident and the marker miss are
      `already covered`. The unit's own `AGENTS.md` delta is +7 net, so the file totals +9. Growth justified: the
      amendment form's two lines buy the choice the parenthetical names — a falsified statement would otherwise stay
      false at its read site or force a superseding ADR that reopens a Decision nobody changed — and no existing
      sentence in the clause can carry it without losing the "superseded, not edited" rule it sits in.
- [x] 5.4 The merged `pause-resume-unit` skill's `Workspace` checkpoint field leaves the claim this unit adds unnamed —
      **no change**, on the technical advisor's consultation. The skill's step 1 already sends the reader to
      `AGENTS.md`'s lifecycle rule, which owns the claim, so a gloss would be the copy the single-sourcing invariant
      forbids; and the checkpoint describes the `Workspace:` record rather than every workspace mechanism, so leaving it
      is not a false statement. The premise that "a resumed unit is exactly the case the claim exists for" was too
      narrow: the resolver reads branch identity and the claim without asking whether work was paused, so a fresh
      non-change-named branch needs it too. Reopen only if a continuation shows resume step 1 failing to reach the rule.
- [x] 5.5 Archive the change after the owner's approval, with the delta synced into the main spec in the same pull
      request.
