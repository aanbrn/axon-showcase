# Design

## Context

See `proposal.md` — Why for the motivation. Base revision: `7f76216b7ff8c4963402305a0988d1b9942550e7` (the branch was
refreshed from `origin/main` after the planning round) (`git merge-base HEAD origin/main`); every line anchor below is
read at that revision, and each row of the site table also names its text, so a later refresh moves the numbers without
moving the identification.

The approach has to fit the current state:

- The review surface and the untracked premise are spread across `AGENTS.md`, the skill, and the README. The set below
  is **derived**, not hand-listed: `grep -n "untracked" AGENTS.md` and
  `grep -n "review pass\|review runs against\|review surface" AGENTS.md`, plus the skill's manual-pass section and the
  README's review-loop paragraph. Decision 6 carries every hit with its disposition.
- The owner reviews in IntelliJ IDEA's Changes panel, on the tree the workspace record names; several working trees are
  live at once, so a per-project IDE setting is a per-tree cost.
- Nothing may be committed before the owner approves, and the pre-commit guard refuses a path staged and then edited
  (`scripts/commit-hygiene.py:89–108`, specified in `showcase/quality/commit-hygiene`).

## Goals / Non-Goals

**Goals:**

- Make the unit's new files — the change dir and any new source or test file — visible in the owner's manual pass.
- Keep the approval stop intact: staging is not committing.
- Deliver the mechanism through the repository, not through per-project IDE state.
- Single-source the procedure in the skill that already owns the gate's steps, and the hazards where they are stated.

**Non-Goals:**

- Changing the pre-commit guard, the gate's stage order or membership, or the reviewer definitions.
- Staging modified tracked files: they already appear in the panel and in `git diff`.
- Shipping a `config/idea/*.xml` component or a `scripts/setup-idea.sh` change: `.idea/` is git-ignored and the owner's
  panel preference is his own.
- Declaring a spec delta: the ownership requirement routes a step correction into the skill, and no requirement states
  the surface (see the claims table).

## Decisions

**1. Stage the unit's own new files with `git add -- <explicit paths>` as part of the manual-pass step.**

- Rejected — `git add -N` (intent-to-add), which would put the files in `git diff` without staging content: it makes
  `git stash` fail (`error: Entry '<path>' not uptodate. Cannot merge.` / `Cannot save the current worktree state`, exit
  1). The workflow names `git stash` on two routes — the commit-forced decision point (`AGENTS.md:350–355`, "commit (or
  `git stash`)" when a push or a `git switch` forces it) and the reset gotcha's stash → `git reset --hard` → pop recipe
  for a tree the advance touched (`AGENTS.md:1616–1632`) — so an intent-to-add entry can block both.
- Rejected — relying on IDEA's show-unversioned-files option: `.idea/` is git-ignored and `config/idea/` carries four
  formatter configs and no VCS state, so the option would have to be re-set in every working tree and on a fresh clone;
  the repository cannot deliver it. It remains a personal preference, not the process.
- Rejected — a blanket `git add -A` / `git add .`: it stages non-ignored strays the unit never intended. The
  repository's own ignores (`build/`, `__pycache__/`, `*.pyc`) keep the usual build output out, so the residual risk is
  a stray the ignores do not cover, and explicit paths remove it.

**2. Put the step in the skill, and let `AGENTS.md` state the surface and point at the skill.**

The `agent-skills` requirement "The review gate's caller procedure is maintained in its project skill" makes the skill
the owner of the caller's steps and sends a correction there (`spec.md:2328`); restating the step at root is the
duplicate the single-sourcing invariant forbids. `AGENTS.md` keeps what a reader must act on before loading the skill:
the surface, and the pointer.

**3. Make re-staging part of the same step, not a separate habit.**

The guard reads the index, so a fix applied after staging refuses the commit until it is re-added — and
`git diff --cached`, which the surface statement names for the new files, shows the older copy. One rule ("re-stage
after any edit to those files") covers both; whether the IDE panel also shows the stale copy is unverified, so nothing
rests on it.

**4. State the staged-case hazard where the hazards are already stated, and make preservation the first step of
recovery.**

What staging changes is the premise of the workflow's own safety statements, on both operations they name.
`git reset --hard` deletes a staged new file, and it **overwrites** an untracked one when the target revision contains
that path (verified) — so "an untracked change dir survives a reset" holds only when the target does not contain it.
`git checkout -- <path>` fails harmlessly on an untracked path (`pathspec … did not match any file(s) known to git`) but
silently restores the index copy of a staged one, discarding edits made after the last `git add`.

The safety is the ordering, not an unstage: a plain `git stash` saves a staged new file and restores it staged, while it
saves **nothing** for an untracked one (`No local changes to save`, no stash entry) — so unstaging before a reset
removes the only copy a stash would have kept, and the target revision then overwrites the file. An untracked new file
therefore gets its own preserve action: stage it first so the plain stash keeps it, or use `git stash -u`, which
includes untracked paths (verified). The workflow therefore has two routes, and each restores what it preserved. The
reset route is preserve → `git reset --hard` → restore: a `git stash pop` after a stash (the gotcha's own recipe), or a
return to the preserved commit after a commit. The rebase route is `git stash` → `git rebase` → `git stash pop`, or a
rebase alone when the work is committed — there the rebase is the middle step, not a restore, and a hard reset would
drop the commits the route exists to keep. The unit's new files are never unstaged before a hard reset, and the existing
rule that forbids a hard reset on the only copy of the work stays first and authoritative. For `checkout --`, the
protection is the re-stage rule: with the current content staged a checkout discards nothing, so the re-stage rule — not
a preserve step — is what protects those files; a checkout still restores the index copy when the index is stale.

The statements carrying the premise are the failure-handling bullet (`AGENTS.md:385–386`) and the `git reset --hard`
gotcha (`AGENTS.md:1620–1631`); each keeps its untracked case, gains the staged one, and states the conditionality. In
the gotcha that includes the consequence clause the staged state falsifies — "under the commit-only-at-push rule the
whole change is untracked until the first commit", and its inference that a pre-push reset "loses only tracked-file
edits" — so the edit qualifies both, and it must agree with the gotcha's own stash → reset → pop recipe rather than
contradict it. This ordering is stated **once**, in the failure-handling bullet, which is where the change dir's safety
is asserted; the other sites point at it. The skill carries none of it: §5 has no reset or refresh step to attach it to.

**5. Add one experiential clause to the README's development narrative (`README.md:305`), no more.**

The owner experiences the change in the review loop; the README's design intent makes the loop the place to say it. The
staging command stays in the skill — the README is not a procedure dump.

**6. Every derived site, with its disposition.**

| Site                                                                                  | Disposition                                                                                            |
| ------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------ |
| `AGENTS.md:329–330` — the record reaches a sibling tree's diff                        | unchanged: states the record's purpose, which staging does not change                                  |
| `AGENTS.md:340` — lifecycle step 2                                                    | edited: names the staged surface, points at the skill for the step                                     |
| `AGENTS.md:343` — step 4 asks for the pass                                            | unchanged: the ask is the step; the surface is step 2's and the skill's                                |
| `AGENTS.md:351–354` — commit-forced decision point                                    | edited: the reset guarantee is conditional on the target's path; points at the failure-handling bullet |
| `AGENTS.md:368` — a review pass in a historical incident                              | unchanged: a past account                                                                              |
| `AGENTS.md:385–386` — untracked change dir is safe                                    | edited: split by staged and untracked, with the preserve → reset → restore ordering                    |
| `AGENTS.md:423` — gate stage list                                                     | unchanged: names the stage's owner, not its surface                                                    |
| `AGENTS.md:437` — capture trigger names the pass                                      | unchanged: a trigger, not a surface                                                                    |
| `AGENTS.md:490–491` — "deliberately left untracked"                                   | edited: qualified to "until the manual pass stages it"                                                 |
| `AGENTS.md:1215` — Formatting's changed-set command                                   | unchanged: `git status --porcelain --untracked-files=all` lists staged paths as `A ` too               |
| `AGENTS.md:1620–1621` — the reset gotcha's untracked-survives sentence                | edited: the staged case does not survive, and an untracked one only when the target lacks its path     |
| `AGENTS.md:1630–1631` — the reset gotcha's `git checkout --` sentence                 | edited: a new file has no `HEAD` entry, so a staged one restores the index copy                        |
| `AGENTS.md:1638` — the archive move's untracked path                                  | unchanged: the move's mechanics                                                                        |
| `AGENTS.md:1741–1743` — the `git add <dir>` gotcha                                    | unchanged: it already prescribes explicit paths, which this step follows                               |
| `AGENTS.md:1816`, `:2415`, `:2494` — grep scope, a `captured:` marker, the docs sweep | unchanged: none states the review surface or relies on the staged state                                |
| `.opencode/skills/review-gate/SKILL.md:141` — manual pass                             | edited: carries the staging step and the surface                                                       |
| `README.md:305` — development narrative                                               | edited: one experiential clause                                                                        |

## Risks / Trade-offs

- **A staged new file is deleted by `git reset --hard`, and `git checkout --` silently reverts its edits** (verified);
  an untracked file survives a reset only when the target revision does not contain its path → decision 4's
  conditionality and its preserve → reset → restore ordering, with the existing "never reset the only copy" rule still
  first; the documented refresh prefers `git merge --ff-only`, which keeps staged work.
- **A staged-then-edited path refuses the commit** → decision 3's re-stage rule; the guard stays as it is, and a bypass
  stays the deliberate `--no-verify` exception it already is.
- **The staged copy and the working tree can diverge between the add and the review** → re-stage immediately before the
  ask: the guard reads the index, and `git diff --cached` is what the surface statement points at. Whether the panel
  also shows a later unstaged edit is unverified (claims row 10), so nothing rests on that.
- **The owner's panel configuration could show only one side of the diff** → the surface statement names the panel and
  the CLI fallback (`git diff --cached` for new files, `git diff` for modifications); the owner confirmed the panel
  shows staged files as Added and modified ones as blue.
- **Staging looks like progress toward a commit** → the step states that staging is not committing and the approval stop
  is unchanged.

## Claims table

Each fact the change rests on, with the source or command that reproduces it. Repository anchors are read at base
`7f76216b`; the probe rows are scratch repositories under `$TMPDIR/opencode`, re-runnable from an empty `git init` with
one commit and one new file.

| #   | Claim                                                                                                                    | Source / command                                                                                                                                                                                                                                    | Result                                                                                                                                                                                                      |
| --- | ------------------------------------------------------------------------------------------------------------------------ | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1   | An untracked file is absent from `git diff`                                                                              | scratch repo; `git diff --name-only` with one untracked file                                                                                                                                                                                        | empty output                                                                                                                                                                                                |
| 2   | A fully staged new file leaves `git diff` and appears in `git diff --cached`                                             | scratch repo; `git add <path>`, then both diffs                                                                                                                                                                                                     | `git diff` empty; `--cached` lists the path                                                                                                                                                                 |
| 3   | `git add -N` makes `git stash` fail                                                                                      | scratch repo; `git add -N <path>`; `git stash`, `git stash push -u`, `git stash push -- <path>`                                                                                                                                                     | each exits 1 with `Entry '<path>' not uptodate. Cannot merge.`                                                                                                                                              |
| 4   | A fully staged new file is stash-compatible                                                                              | scratch repo; `git add <path>`, `git stash`, `git stash pop`                                                                                                                                                                                        | exits 0 both ways; the pop restores the path as staged (`A `)                                                                                                                                               |
| 5   | `git reset --hard` deletes a staged new file, and keeps an untracked one only when the target does not contain that path | scratch repo; stage `new.md`, `git reset --hard`; untracked `new2.md`, `git reset --hard`; then untracked `new.md`, `git reset --hard` to a commit that contains `new.md`                                                                           | staged `new.md` gone; `new2.md` present; the matching-path case **overwrites** the untracked file (`v2-untracked` → `v1`)                                                                                   |
| 6   | `git checkout --` fails on an untracked path and restores a staged path's index copy                                     | scratch repo; `git checkout -- new.md` untracked, then staged and edited to `v2-edited`, `git checkout -- new.md`                                                                                                                                   | exit 1 with `pathspec … did not match`; then exit 0 and the content returns to the staged copy                                                                                                              |
| 7   | A fast-forward keeps staged work                                                                                         | scratch repo; staged new file and staged modification, unrelated remote advance, `git merge --ff-only origin/<branch>`                                                                                                                              | the merge updates the branch and `git status` still shows `A <new>` and `M <modified>`                                                                                                                      |
| 8   | `git add -A` respects `.gitignore`                                                                                       | scratch repo with `build/` and `__pycache__/` ignored; `git add -A`                                                                                                                                                                                 | only the non-ignored new file staged                                                                                                                                                                        |
| 9   | The pre-commit guard refuses a path staged and then edited                                                               | `scripts/commit-hygiene.py:89–108`; `find_staged_then_edited`                                                                                                                                                                                       | index status in `MARC` and worktree status in `MADRC` is an offender                                                                                                                                        |
| 10  | The owner's Changes panel shows staged new files as Added, and showed the change dir only after staging                  | the owner's report before and after `git add` of the change dir, this session                                                                                                                                                                       | the owner's account; whether a later unstaged edit is hidden from the panel is **unverified**, so the re-stage rule rests on the guard and on `git diff --cached`, not on the panel being an index snapshot |
| 11  | `.idea/` is git-ignored and `config/idea/` holds four formatter configs                                                  | `git check-ignore -v .idea/workspace.xml`; `ls -1 config/idea \| wc -l`                                                                                                                                                                             | `.gitignore:15:.idea/`; `4`                                                                                                                                                                                 |
| 12  | No requirement states the review surface or staging                                                                      | `grep -rn "manual review" openspec/specs/` → two clauses (`agent-skills:1086`, `:1995`); `grep -rn "untracked" openspec/specs/` → **two**, both `commit-hygiene`'s spec-sync check (`:25`, `:93`); `grep -rn "reset --hard" openspec/specs/` → none | no requirement changes, so `skip_specs: true`                                                                                                                                                               |
| 13  | The review-gate steps are owned by the skill                                                                             | `openspec/specs/showcase/quality/agent-skills/spec.md:2328` ("A correction to a step SHALL be made in the skill")                                                                                                                                   | the step lands in the skill                                                                                                                                                                                 |
| 14  | A plain `git stash` saves a staged new file and saves nothing for an untracked one                                       | scratch repo; staged `new.md` → `git stash`, `git stash list`, `git stash pop`; then untracked `new.md` → `git stash`                                                                                                                               | one entry, and the pop restores the content; the untracked file gives `No local changes to save` and no entry                                                                                               |
| 15  | `git stash -u` preserves an untracked new file across a reset                                                            | scratch repo; untracked `new.md` → `git stash -u`, `git reset --hard`, `git stash pop`                                                                                                                                                              | one entry; content intact after the pop, restored as untracked (`?? new.md`)                                                                                                                                |

## Open Questions

None that change the approach or the task breakdown. Whether a staged file shows in a _different_ IDE configuration is
outside this repository's control and is answered per user, not per change.
