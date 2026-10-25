# Tasks

## 1. The lifecycle rule

- [x] 1.1 Add the workspace rule to `AGENTS.md`'s "Workflow: Change lifecycle": one active unit per working tree, the
      primary checkout counted as the unit's tree, occupancy read from the registry (a unit branch checked out means
      occupied; an unknown branch, a detached HEAD, or contradicting evidence is occupied), a sibling
      `../axon-showcase-<unit>` worktree when the primary belongs to another unit, and the recorded preflight with its
      home — verified by grepping the new sentences and by `./gradlew spotlessCheck`.
- [x] 1.2 Add the release rule: the primary is handed on only after the unit completed (merged, or rejected by the
      owner), no work remains (extra commits and unique local data included), and no writer continues, with the return
      to a clean `main` as the release's last step — verified against the design's assignment table.
- [x] 1.3 Scope the forced-commit decision point to a branch switch inside one tree, so it no longer reads as a
      requirement to commit or stash an in-flight unit before starting a sibling — verified by reading the decision
      point and confirming it names the switch it governs.
- [x] 1.4 Add the harness note (the session's cwd moves with `session_move`; verify the path and branch before the first
      write) and the registry note (`git worktree list --porcelain` is the source of truth; a directory name is a hint,
      never the identification; a tree whose ownership no unit can establish is resolved by the owner, the preflight
      record gains the resolved owner, and no unit takes the tree over or removes it) — verified by grepping both notes
      and the ownership sentence.
- [x] 1.5 Sweep `AGENTS.md` for statements the rule falsifies: the "Fork every new branch" invariant's parenthetical,
      the stashed-work gotcha, the "No in-flight branch" decision point, and the lifecycle step whose review pass reads
      "against the visible `git status`/`git diff`" — the last now needs the unit's recorded tree for a sibling —
      verified by grepping `worktree` and each of those clauses, and reading each hit against the new rule.
- [x] 1.6 Extend the "Rejecting a proposal is a branch delete, never a `main` cleanup" failure-handling bullet with the
- [x] 1.7 Add the continuation route: a unit continuing an existing branch or pull request takes a tree on that branch
      (a remote-only head from its verified `origin/<branch>`), never a new branch from `origin/main` and never by
      switching a busy primary — verified by grepping the bullet and reading it against the cloud-PR step.

## 2. Mutating command preflights

- [x] 2.1 Add the workspace bullet to the `Repository lifecycle` block of the ten mutating commands under
      `.opencode/commands/` (the audit commands, `apply-audit-reductions`, `ideas`, `gradle-update`, `diagram`,
      `opsx-tool-update`, `retrospective`), each pointing at the lifecycle workflow rather than restating it — verified
      by `grep -rln` over the command set and by confirming `consult.md` (which starts no unit) is unchanged.
- [x] 2.2 Read the written bullets beside the delta's new scenario, so the commands and the requirement state the same
      three obligations (established before the first write, a sibling when the primary belongs to another unit, an
      unestablished ownership treated as occupied) — verified by the comparison recorded in `design.md`'s "Task 2.2",
      with `openspec validate --all` as the schema check only.

## 3. Reviewer and shipping surfaces

- [x] 3.1 Add the unit's assigned working tree to `.opencode/agent/review-quick.md`'s hand-over enumeration, matching
      the delta's extended field set, and confirm `.opencode/skills/review-gate/SKILL.md` still points at that set
      instead of restating it — verified by grepping both files and by reading the skill's step against the delta.
- [x] 3.2 Add the linked-worktree cleanup to `.opencode/skills/ship-change/SKILL.md`: verify no unique work remains
      (ignored files included), move the session out, `git worktree remove` without `--force`, and `git branch -D` only
      after confirming that unit's PR merged with no unshipped commits — verified by grepping the steps and the
      `--force` prohibition.
- [x] 3.3 Add the tree-kind branch to the same skill: a linked-worktree unit removes only its tree and leaves the
      primary alone; a primary unit runs the three checks (completed, no work left, no writer) and returns the tree to a
      clean `main` as the last step, without removing the directory — verified by grepping the step.

## 4. Documentation

- [x] 4.1 Add a short `README.md` note on parallel units: where a unit's tree lives, that the primary checkout is the
      sequential unit's workspace, and that a parallel unit's tree is removed after merge — verified by reading the
      section and `./gradlew spotlessCheck`.
- [x] 4.2 Remove the implemented idea from `docs/ideas.md` and append the follow-up entry that verifies the rule on the
      first two real parallel units, restating the five criteria the design's "Verification route" lists as its
      completion condition — verified by grepping the file for the old idea's first sentence and for each criterion.
- [x] 4.3 Re-derive the changed artifact set: grep `AGENTS.md`, `README.md`, `openspec/specs/`, `.opencode/`, and
      `openspec/config.yaml` for the lifecycle wording the change moves, and fix each site it leaves stale — verified by
      the hits and dispositions recorded in `design.md`'s "Task 4.3".

## 5. Gates

- [x] 5.1 Run `./gradlew spotlessApply`, then `spotlessCheck` and `openspec validate --all` — verified by their exit
      status and by reading the formatter's output for skipped files.
- [x] 5.2 Record the pre-write derivation pass and its claims table (in `design.md`) as this change's record: seed it
      from `python3 scripts/claim-scan.py --base origin/main` and resolve each candidate into a row or a recorded
      dismissal, re-deriving every row from the source it names — verified by the rows against `git show HEAD:` of the
      files they anchor, and by the candidate list with one disposition per kind.
- [x] 5.3 Run `review-quick` over the change and fix its findings until it reports none: retain the reviewer session per
      the gate's preflight, append each round's row to `docs/review-rounds.tsv`, and run
      `python3 scripts/commit-hygiene.py --unique-rounds --expect adopt-worktree-isolation:<round>` plus
      `--session-continuity` after each append — verified by the ledger rows and both checks.
- [x] 5.4 Record the deep-review routing decision from
      `python3 scripts/review-routing.py --change adopt-worktree-isolation`, and run the routed deep review when it
      selects one — verified by the recorded outcome and, for a `REQUIRED` or owner-resolved `RECOMMENDED` outcome, the
      pass's recorded status.

      **Routing:** `RECOMMENDED`, signal `repeated-class` (six ledger rows repeat a prior class). Deep review pass 1
      (session `ses_e8f455ccfffeCEblul8DtGA2gn`, initial review) returned 3 findings; the owner approved the advisor's
      fix package, and pass 2 (fix verification) returned `nothing remains` with all three `FIXED_AND_VERIFIED`:
      - should-fix — the `Workspace:` record had no home and occupancy was not mechanically establishable: occupancy is
        now read from the registry (a checked-out unit branch owns the tree; unknown, detached, or contradicting evidence
        is occupied), the record keeps all four fields and lives with the unit's record, and the primary's release ends
        with a clean `main`;
      - should-fix — the forced-commit decision point now scopes its commit conclusion to switching that tree's branch;
      - nitpick — the premise-interrogation bullet reads "then-parked".

- [x] 5.5 Run the `lesson-capture` subagent over the diff and round rows and apply its durable proposals, or record the
      skip and its reason — verified by the capture record written in this task list.

      **Capture:** ran. One addition, merged into `AGENTS.md`'s evidence bullet rather than a new one (+5 lines): a rule
      that depends on a recorded fact points at the single-sourcing invariant for the record's owner and home and at the
      external-claim rule for its verification, and states that an unreadable record leaves the fact unestablished rather
      than permanently unestablishable (narrowed after the advisor's review), with
      its incident paragraph in `docs/guidance-evidence/documentation-and-durable-claims.md` (+6). The verified markdown
      table-integrity check the capture routed is parked in `docs/ideas.md` (a new check with its controls is its own
      unit). Three candidates were recorded as already covered (the model-change sweep, a mechanism change leaving a
      delta unchanged, a stale status word) and none of the unit's rules was retired.

- [x] 5.7 Verify the session move and file access into a sibling tree, with no build: create a sibling from
      `origin/main`, move the session into it, read and write a non-secret file, move back, then remove the tree and its
      branch — verified by the probe's result recorded here. **Result:** `session_move` into the sibling, a write and
      read of a scratch file there, and the move back all succeeded with no additional `external_directory` grant, and
      `git worktree list --porcelain` reported the three trees during the probe; the probe tree and branch were removed
      afterwards. The advisor's grant hypothesis is therefore falsified for the moved-session path (an absolute-path
      touch of a sibling from the primary was not tested).
- [x] 5.6 Ask the owner for the manual review pass and stop before any commit, push, pull request, archive, or merge —
      verified by the turn ending on that request.

## Workflow follow-up

- Archive the change after the owner's approval and a green `build` check, in the same pull request.
- Hand the parked follow-up (`docs/ideas.md`) its verification when the first two real parallel units run.
