# Tasks — keep Air-hosted units on the repository's worktree process

The unit's record is `docs/units/document-air-worktree-safety/unit.md`.

## 0. The pre-write pass

- [x] 0.1 The review gate's pre-write derivation pass ran, seeded with
      `python3 scripts/claim-scan.py --base origin/main` over the change's artifacts; `design.md` carries the claims
      table and the per-kind dismissals beside it. Verify: every scanned candidate is a row or is covered by its kind's
      dismissal, and each row's command reproduces its figure.

## 1. The disposable exercise

- [x] 1.1 Create a throwaway clone of this repository under the agent scratch directory, remove its `origin` remote, and
      place one marker in each class — a staged new file, an edit to a tracked file left unstaged, and an untracked new
      file — each with distinct content so a copy can be compared. Arm the guard in the clone
      (`./scripts/install-git-hooks.sh` sets `core.hooksPath`; a fresh clone carries no hooks), set a local commit
      identity (`user.name`/`user.email`) so the attempt is not refused for it, and record the pre-state. Verify:
      `git status --porcelain` lists one `A `, one ` M`, and one `??` path; `git config --get core.hooksPath` prints
      `scripts/git-hooks`; `git remote` prints nothing; and each marker's status line and content are recorded for 1.2's
      comparison.
- [x] 1.2 Start one Air session in **New Local Worktree** from that clone (the owner runs the IDE part), then record for
      each of the two trees — the clone and Air's — its path and branch from the clone's `git worktree list`, its
      `git status --porcelain`, `git diff --cached --name-status`, `git diff --name-status`, and every marker's content.
      Verify: Air's tree path and branch come from the registry rather than the UI; and the record states each marker's
      index and worktree state in Air's tree and whether the original tree's three markers kept their pre-state.
- [x] 1.3 Attempt the commit **in Air's tree**, with its path as the working directory rather than the original clone.
      Before the attempt, record there its `git config --get core.hooksPath`, `git rev-parse --abbrev-ref HEAD`,
      `git rev-parse --git-path axon-showcase-change`, whether that claim file exists, and `git rev-parse HEAD`; if the
      copied tree has nothing staged (1.2 already recorded that state), stage one marker so the attempt reaches the
      hook. Verify: the effective hook path resolves against that tree's root to an existing executable
      `scripts/git-hooks/pre-commit`; the branch, resolved claim path, absent claim, and pre-attempt `HEAD` are
      recorded; the output carries `No commit assignment:` naming that branch and that claim path; `git rev-parse HEAD`
      is unchanged afterwards; a failure for any other reason — including one that never reached the hook — is recorded
      as inconclusive, never as the expected refusal; and a commit that succeeds with the guard armed stops this unit
      for a scoped guard design instead of shipping the claim.
- [x] 1.4 Clean up in order: remove Air's worktree and branch, inspect the clone's registry, then delete the clone.
      Verify: after the removal `git worktree list` in the clone lists neither Air's tree nor a stale entry; after the
      deletion `git worktree list` here lists nothing of the exercise; and `test ! -e <clone>` holds.

## 2. The root warning

- [x] 2.1 `AGENTS.md`, the Workspace rule: one bullet stating that an Air-hosted unit runs in **Local** mode in its
      assigned tree and never in **New Local Worktree**, pointing at the setup skill's Air section for session selection
      and accidental-mode recovery. Verify: the bullet names the mode and the tree, carries the pointer, and adds no
      second worktree procedure.

## 3. The skill's Air block

- [x] 3.1 `.opencode/skills/setup-agent-tools/SKILL.md`, the Air section: a version-qualified "before a unit session"
      block, one bullet per rule:
  - choose **Local** in the assigned tree;
  - verify the session's absolute path and branch against the unit record before the first write;
  - reach for another tree only through `scripts/new-worktree.sh`;
  - keep Air's automatic worktree cleanup disabled.

  Verify: each bullet is present, the tested-version boundary is stated, and the block claims no global worktree off
  switch.

- [x] 3.2 Same section: the accidental-mode recovery as numbered steps:
  1. stop the session's work;
  2. change nothing — no commit, push, reset, or deletion of either tree or Air's branch;
  3. find both trees in the registry;
  4. preserve their unique files and commits, staged and untracked work included;
  5. present the differences and a recovery proposal to the owner;
  6. stop for their decision, and never add a claim or rewrite the unit record to make the stray tree pass the guard.

  Verify: each forbidden action is named, the preserve-before-reconcile order is stated, and the owner's decision is the
  stop.

- [x] 3.3 Same section: one sentence stating what this does not cover, on ADR-0024's own terms — the v2 surface offers
      no session-bound write control, v1 is unverified, and the property is recorded as not ensured — so the pre-commit
      guard, the branch ruleset, and the review stop are the boundary. Verify: the sentence states the ADR's finding
      rather than "not session-bound", and names the three boundaries.

## 4. Verification

- [x] 4.1 `./gradlew spotlessApply`, then read back the reflowed markdown. Verify: `./gradlew spotlessCheck` passes and
      no changed file was skipped as non-converging; every inline code span is read as content.
- [x] 4.2 `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` and `openspec validate --all`. Verify: both pass and
      `skip_specs` validates.
- [x] 4.3 Re-read each claims-table row against its source and delete any row that no longer reproduces. Verify: every
      remaining row's source was re-run or re-opened, and the table states what was observed.

## 5. The review gate and the records

- [x] 5.1 The proposal stage's quick review over the planning artifacts until a clean round, each round's row and
      findings recorded under `docs/units/document-air-worktree-safety/`. Verify:
      `python3 scripts/commit-hygiene.py --unique-rounds --expect document-air-worktree-safety:<N>`,
      `--session-continuity`, and before the manual pass `--expect-clean document-air-worktree-safety` pass.
- [x] 5.2 The owner's manual pass over the proposal. Verify: the approval is recorded before any implementation edit.
- [x] 5.3 The implementation stage's quick review. Verify: the closing round carries no finding class and
      `--review-ready` passes.
- [x] 5.4 The deep-review routing decision on the completed diff. Verify:
      `python3 scripts/review-routing.py --unit document-air-worktree-safety --change document-air-worktree-safety` ran
      and its outcome and signals are recorded.

      **Done** — `RECOMMENDED`, signal `[repeated-class] 4 rows repeat a prior class`: round 2 repeated
      `structured-writing`, and rounds 5–7 repeated `missing-task`, `claim-unreproducible`, `over-claim`, and
      `contradiction`. The owner decided to run one proportionate pass, on the technical advisor's recommendation (an
      independent comparison of the exact final diff against its plan and evidence, since the advisor's earlier
      agreement covered wording and decision only). Recorded 2026-11-06; the pass is task 5.5.

- [x] 5.5 The deep review, when 5.4 selects one, with its findings applied and re-reviewed. Verify: the pass's status
      and its findings' disposition are recorded.

      **Done** — pass 1: 2 findings, both nitpick. (1) the decision record's three new-file byte counts did not state
      their trailing newline — `FIXED_AND_VERIFIED` (36+1, 39+1, 65+1). (2) `5.4`'s repeated-class gloss omitted round
      2's `structured-writing` — `FIXED_AND_VERIFIED` (now names round 2 and rounds 5–7). Pass 2 (independent
      verification): `nothing remains`; both statuses confirmed, no new findings. Subject packs: none, re-confirmed.

- [x] 5.6 The owner's manual pass over the finished unit. Verify: the approval is recorded, and nothing is committed,
      pushed, or archived before it.

      **Done** — the owner approved the finished unit on 2026-11-06 ("Одобряю. Отгружай."), after the technical
      advisor agreed on the reconciled wording and the deep pass closed with `nothing remains`. Nothing had been
      committed, pushed, or archived before that approval.

- [x] 5.7 `docs/units/document-air-worktree-safety/process-defects.md`: one record per defect observed, or the statement
      that none was. Verify: the file exists before 5.6 is requested.
