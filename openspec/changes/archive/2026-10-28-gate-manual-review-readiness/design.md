# Design

## Context

The review gate's manual-pass boundary has carried two preparation steps since 2026-10-27, and both were written from an
incident rather than designed: the base check came with `unify-agent-language-policy`
(`git log -1 -S "confirm the unit's base has not moved" -- .opencode/skills/review-gate/SKILL.md` → `ea1ee3b5`), and the
staging step with `stage-new-files-before-manual-review`, whose ledger carries 18 rounds (`docs/review-rounds.tsv`).
Both are prose an agent executes by hand, and the surrounding machinery does not see them:

- `--staged` inspects paths that are already in the index, so a new file that was never added does not satisfy its
  condition (`scripts/commit-hygiene.py`, `find_staged_then_edited`).
- The hook that runs `--staged` fires at commit time, after the manual-pass request.
- Nothing in `scripts/`, `build-logic/`, or `.github/` reads the base at all at the unit's base revision:
  `git grep -n "merge-base" bf0dbd61 -- scripts/ build-logic/ .github/` exits 1, and the two `--base` arguments that do
  exist (`claim-scan.py`, `review-routing.py`) compare file lists for other questions.

The repository already holds the pattern for this class of fix. `--expect-clean` is a `commit-hygiene.py` assertion the
review gate's manual-pass step invokes and no `check` task does, precisely because its subject is the unit's own history
(`openspec/specs/showcase/quality/commit-hygiene/spec.md`, "Review-round ledger rows are unique", where the requirement
states the boundary invocation). This change is that shape for the tree the request describes.

Two 2026-10-27 records evidence the index half failing in practice
(`docs/process-defects/scope-chart-repository-fetch.md#4`: staged before its last edit, twice;
`docs/process-defects/hand-over-unfinished-work.md#3`: edited after staging, costing two review rounds). No record
establishes a past skipped _request_, so this design treats the base half as an unobserved gap in the mechanism rather
than a proven recurrence of the behaviour — the owner's observation is the trigger, the missing check is the finding.

## Goals and non-goals

- **Goal** — a skipped base refresh or skipped staging fails a check, naming the path or the revisions, at the moment of
  the request.
- **Goal** — the check is the existing tool's mode, its contract is one spec requirement, and the boundary procedure
  stays in the review-gate skill.
- **Non-goal** — preventing the agent from writing the request anyway. A check can fail loudly; it cannot stop a
  message. Technical prevention of that bypass is not attempted here (the deferred reviewer-wrapper `docs/ideas.md`
  entry records why a wrapper buys ordering only within its own route).
- **Non-goal** — changing the branch-refresh procedure. `AGENTS.md`'s "Refreshing a branch" owns it and keeps it.
- **Non-goal** — a new `check` task. CI checks out a clean tree, so the mode would pass vacuously there, and the
  pull-request gate only takes checks a tripping change can remediate.

## Decisions

### The criterion is history containment, not a diff of the working tree

The prose criterion — `git diff --stat origin/main` "deletes nothing the unit did not author" — describes the symptom,
so the check tests its cause instead: **`HEAD` contains the base**, i.e. `git merge-base --is-ancestor <base> HEAD`
succeeds. A unit that sits on the current base passes; one whose base carries commits `HEAD` lacks is stale, which is
what "the base moved" means and what `AGENTS.md`'s "Refreshing a branch" refreshes from.

The first implementation of this unit derived a stricter-looking reading of the prose instead — diff the working tree
against the base and against the merge base, and fail when the first diff deletes a line the second does not. The second
consultation falsified it with a state the heuristic passes: the worktree and the merge base carry `A` and `B`, the base
now carries only `A`, so the tree shows **no deletion at all** — relative to the base it _adds_ `B`. Reproduced in a
scratch repository: `git merge-base --is-ancestor main HEAD` exits 1 while `git diff --unified=0 --no-renames main --`
yields only a `+B` line and no deletion. The base dropped a line the unit still carries — a merge landed on a file the
unit never touched — and the deletion reading reports the tree ready. So the deletion heuristic was replaced, not
patched:

- it was a subset test: every state it caught, the containment test also catches;
- it needed a diff parser, rename handling, and a multiset comparison to approximate authorship, none of which the
  question requires;
- its failure modes (binary paths, mode-only changes, a same-size swap, a content-preserving rename) were artifacts of
  reading a diff rather than the history.

The containment test costs one `merge-base` call, and its accepted false negative is only the stale local ref the step's
own `git fetch` prevents.

### The three obligations are reported together

The mode collects every failure and exits non-zero once, because the agent's remedies are independent (stage, re-stage,
refresh) and a fail-fast mode would cost one invocation per finding. Diagnostics go to stderr and the ready line to
stdout, matching the other modes.

### A failed git invocation is not a clean result

An invocation whose failure maps onto the benign outcome reads as a clean result, so the history and status reads
distinguish the two: `git merge-base`'s exit 1 (no common ancestor) is an answer, while a higher code is a failure;
`git rev-parse --verify --quiet`'s "does not resolve" is an answer, while a repository error is not; a failing
`git status` or `git rev-list` is reported as **readiness not established** with the command and its stderr, and the
mode exits non-zero. Without that split, an unreadable index would produce an empty record list and a `Review-ready`
line asserting a state nothing verified.

### `--base` mirrors the existing convention

`--base` defaults to `origin/main`, as `claim-scan.py` and `review-routing.py` already do, so a unit on a different base
names it and the default needs no flag. The mode never fetches: the review-gate step fetches first, and a check that
reaches the network would fail for causes the agent cannot remediate in the same way.

### The skill, not another prose rule, carries the boundary

`AGENTS.md`'s review-gate entry point and its branch-refresh rule stay as they are; the boundary's procedure is `§5`'s,
which the `agent-skills` requirement already makes the owner of the steps. `§5` gains the invocation, the remedy per
failure class, and the requirement that the request names the mode's output line — so the owner reads the revisions that
were checked instead of trusting that a check ran.

## Accepted false negatives

- **A stale local `origin/main`.** The mode reads the base ref as the repository has it and never fetches; the
  review-gate step fetches first, and a checker that reached the network would fail for causes the agent cannot
  remediate in the same way.
- **A skipped invocation.** The check is evidence only when it ran; the request naming its output line is what makes a
  skip visible to the owner, and no mechanism enforces that the line is genuine.

## Claims table

Every row's source reproduces at `bf0dbd61`, the commit this change was authored against: a row about the state before
the change is anchored to that commit rather than to a tree the change itself rewrites, so a later advance of the base
does not move it.

- The base-check step came with `unify-agent-language-policy` (`ea1ee3b5`) —
  `git log -1 -S "confirm the unit's base has not moved" -- .opencode/skills/review-gate/SKILL.md`.
- The staging step's incident carries 18 ledger rounds —
  `awk -F'\t' '$2=="stage-new-files-before-manual-review"{n++} END{print n}' docs/review-rounds.tsv`.
- No readiness mode exists at the base —
  `git grep -n "review-ready\|review_ready" bf0dbd61 -- scripts/ build.gradle.kts .opencode/` exits 1.
- Nothing at the base reads a merge base in the tooling —
  `git grep -n "merge-base" bf0dbd61 -- scripts/ build-logic/ .github/` exits 1.
- `--base` already defaults to `origin/main` in two scripts at the base —
  `git grep -n "origin/main" bf0dbd61 -- scripts/` returns exactly `claim-scan.py:153` and `review-routing.py:198`.
- The mode's tests run in `check` without a build edit — `build.gradle.kts:695` (`check` depends on
  `testCommitHygiene`).
- A boundary-asserted mode is an existing pattern — `openspec/specs/showcase/quality/commit-hygiene/spec.md`, whose
  ledger requirement states "The assertion SHALL be invoked by the review gate's manual-pass step".
- The deletion criterion passed a state history containment rejects — reproduced in a scratch repository: the base
  dropped a line the worktree kept, `git merge-base --is-ancestor main HEAD` exited 1, and
  `git diff --unified=0 --no-renames main --` emitted only a `+B` line.
- The two index-side defect records exist and say what is cited — `docs/process-defects/scope-chart-repository-fetch.md`
  (record `#4`) and `docs/process-defects/hand-over-unfinished-work.md` (record `#3`).

## Alternatives rejected

- **Extend `--staged` with the readiness checks.** Rejected: `--staged` is the pre-commit guard's mode over the index,
  and folding a base comparison into it would fail every commit on a stale base — a state the commit workflow does not
  require to be fresh — while its hook runs after the request the check exists to precede.
- **Keep the deletion heuristic and patch its rename hole.** Rejected: it was a subset test of history containment, so
  patching one hole leaves the class (the base dropping a line the unit still carries) uncaught, and it carries a diff
  parser, rename handling, and a multiset comparison the question does not need.
- **Have the checker stage the unit's new files.** Rejected: staging is the agent's explicit-path decision, and a
  checker that stages would sweep a generated artifact into the index — the failure the `git add -A` gotcha records.
- **A `check` task, so CI enforces it.** Rejected: CI's clean checkout has nothing to compare, so the task would pass
  vacuously and its failure would mean nothing.
- **Another prose obligation in `AGENTS.md`.** Rejected: the skill already owns the boundary's steps, and the existing
  prose is what the misses happened under.
