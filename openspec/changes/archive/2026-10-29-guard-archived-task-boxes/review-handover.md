# Review hand-over

## Round 1 — the proposal

- **Unit** — change `guard-archived-task-boxes`, branch of the same name, base `6f74424d`; no commits; the change
  directory is staged, not committed; the branch name is the change name, so no claim file is needed.
- **Workspace** — `unit=guard-archived-task-boxes`;
  `path=/Users/afanasyev/Development/axon-showcase-guard-archived-task-boxes`; `branch=guard-archived-task-boxes`;
  `writer=ses_e7e0a3d04ffepyFcMfYYisYm9y`. This is a sibling worktree: the primary checkout belongs to the
  `proofread-readme-language` unit and was returned to `main` with its foreign changes intact.
- **Supplied artifacts** — `proposal.md`, `design.md` (five decisions, a seven-row claims table, risks, non-goals),
  `tasks.md`, `specs/showcase/quality/commit-hygiene/spec.md` (one `MODIFIED` block carrying the pre-commit
  requirement's description and all 33 existing scenarios plus seven new ones); the evidence those artifacts cite:
  `scripts/commit-hygiene.py` (`find_unpaired_archive_sync`, `check_staged`, `staged_file_text`, the archive path
  pattern), `scripts/git-hooks/pre-commit`, the spec's pre-commit requirement, `docs/ideas.md`'s 2026-10-19 entry,
  `AGENTS.md`'s `git add <dir>` gotcha and its deferred-task clause, `README.md`'s guard enumeration.
- **Scope** — the proposal round: whether an archived change directory's unticked task is refused at commit time, the
  staged-set scoping, the no-exemption decision, the box-form rule, the delta and the human-facing surfaces. No
  implementation exists.
- **Prior findings** — none; the unit's first round.
- **Completed checks** — `openspec validate --all`: 27 passed, 0 failed; `spotlessApply`/`spotlessCheck` green; the
  claims rows were re-run at the base; the delta's scenario count was compared with the main spec's (33 preserved, 7
  added).
- **Known limitations** — the controls are planned, not written; the entry-point scan of the corpus found 45 archived
  `tasks.md` files with historical unticked boxes, which is why the check judges the staged set only.
- **Deadline** — 15 minutes, the caller's policy.

## Open questions

- The delta's `MODIFIED` block carries the whole pre-commit requirement, so the new archived-tasks clause sits inside a
  long description; is the sentence placement (after the unpaired-sync clause) the right authority, or should the clause
  be its own requirement? The design's D4 chose the fold-in because the requirement already enumerates the guard's
  refusals. **Settled by the consultation**: keep D4 — the existing requirement owns the guard's refusals.
- **Owner decision pending** — every staged archived `tasks.md` is judged in full, so a commit that stages an
  already-archived file with historical unticked boxes is refused too, without implying its tasks should now be ticked;
  `--no-verify` remains the deliberate-exception path. The owner is asked to accept that policy.

## Consultation — proposal stage

- **Session** — `ses_e7b239670ffeKkeNlLXBnIpKOq`, the technical advisor; the owner's protocol is to reach agreement
  before implementing.
- **Verdict** — retain the whole-staged-file guard, subject to an explicit historical-content policy and three
  corrections.
- **Corrections applied** —
  1. The proposal's "historical unchecked boxes are untouched" overclaimed: any staged modification of such a file is
     refused. The artifacts now state the policy plainly (D2, the proposal bullet, the Risk bullet) and that historical
     tasks are never ticked to satisfy the guard.
  2. The delta's description spoke of a staged _directory_ while its scenarios name a `tasks.md`; it now states
     selection by staged tasks-file path, judgment of its entire indexed content, and no working-tree substitution.
  3. The controls did not establish index-content judgment. Two were added: an indexed-versus-worktree discriminator in
     both directions with a mutation, and a live→archive move carrying an unchanged unticked task.
- **Design change from the consultation** — D3 now distinguishes a checkbox list item from a Markdown link (`](`), and
  refuses `[~]`/`[-]` as an unsupported marker rather than counting it as ticked; the delta gains a scenario for it.
- **Kept** — D4 (fold the refusal into the existing requirement) and D5 (no Purpose refresh).

## Round 2 — the quick pass's findings

- **Prior findings and their instances (round 1), with fixes** —
  1. The delta's unsupported-marker predicate said "neither empty nor `x` or `X`", which literally classified `[ ]` as
     unsupported → "neither empty nor whitespace, nor `x` nor `X`", matching D3 and the scenario.
  2. The new refusal had no merge carve-out, so a legitimate merge of an advanced `main` would be refused → D1 and the
     delta now exempt a staged archived tasks file the commit's merge accounts for, by the `merge_accounts_for` test the
     ownership check already applies, and a scenario plus control 2.9 assert it.
  3. D3's link exclusion and list-item form were decided and controlled but absent from the delta → the description now
     states the task form and the `](` exclusion, with a scenario.
  4. The basename selection was never exercised → control 2.8 stages an archived non-`tasks.md` file carrying `- [ ]`
     and expects no failure.
  5. The premise over-claimed that a merge-time verification can only be ticked inside the archive commit → `AGENTS.md`
     also allows a commit before the archive, and `design.md`'s Context now says so; the proposal no longer states the
     premise at all.
  6. Claims row 6's command returned nothing at the base because Prettier had wrapped the phrase → the token is now
     `reword it`, which the reflow cannot split.
  7. The hand-over said three new scenarios where the delta carried five → seven, now that findings 2 and 3 added
     theirs.
  8. The proposal's control list named four where the tasks require nine → the proposal points at `tasks.md` §2.
  9. One proposal bullet carried four actionable statements → split into three bullets.
- **Completed checks** — `openspec validate --all`: 27 passed, 0 failed; `spotlessApply`/`spotlessCheck` green; the
  delta is 40 scenarios (33 preserved, 7 added); the round-1 row is in `docs/review-rounds.tsv`.
- **Scope** — the proposal round; nothing is implemented.
- **Deadline** — 15 minutes, the caller's policy.

## Round 3 — each predicate swept across every site that states it

- **Prior findings and their instances (round 2), with fixes** —
  1. The merge exemption claimed ownership but tested path membership only: `merge_accounts_for` compares names, so a
     merge that edits the file during conflict resolution stayed exempt. D1, the delta sentence, the scenario and
     control 2.9 now require **blob equality** (the index blob against a merged revision's blob for the same path), and
     control 2.9 must fail when the comparison is path-only.
  2. An empty bracket `- []` had three dispositions (proposal: unsupported, description: neither class, D3: neither
     class). One predicate now stands in all four sites: empty or whitespace is unticked; `x`/`X` is ticked; any other
     content is unsupported. `- []` joins control 2.4 as an unticked case.
  3. The link control used `- [text](url)`, whose four-character bracket is excluded by the length rule anyway, so it
     could not detect a missing `(` exclusion → the control is `- [ ](url)` and must fail when the branch is removed.
  4. "At any indentation" was uncontrolled → control 2.1 now stages an indented unticked task under a heading and
     asserts it is named.
  5. "A staged path the commit does not carry SHALL NOT be judged" said the opposite of its intent → "an archived
     `tasks.md` absent from the staged set SHALL NOT be judged".
- **Root cause and sweep** — every round-2 finding was a residue of a round-1 fix landing at the one sentence it named,
  so this round swept each predicate — the checkbox dispositions, the task form, the staged-set boundary, the merge
  exemption — across the proposal, D1/D3, the delta description, the delta scenarios and the tasks' controls, rather
  than patching the five named sentences.
- **Also corrected** — the round-2 note claimed both `proposal.md` and `design.md` carried the tick-placement premise;
  only `design.md`'s Context does, and the note now says so.
- **Completed checks** — `openspec validate --all`: 27 passed, 0 failed; `spotlessApply`/`spotlessCheck` green; the
  delta is 40 scenarios (33 preserved, 7 added); rows 1–2 are in the ledger.
- **Scope** — the proposal round; nothing is implemented.
- **Deadline** — 15 minutes, the caller's policy.

## Round 4 — the scenario and the quiet lookup

- **Prior findings and their instances (round 3), with fixes** —
  1. The unsupported-marker scenario still classified whitespace as unsupported, so the promised one predicate was not
     in all four sites → its WHEN now reads "neither empty nor whitespace nor `x` nor `X`", matching the proposal, D3
     and the description.
  2. D1's merge lookup aborted on a merged revision that lacks the path (`git rev-parse <revision>:<path>` exits 128,
     and `git_output` raises) → D1 now requires a quiet lookup that reads the exit status, and control 2.9 adds the
     absent-path case beside the blob-differs and path-only ones.
  3. The round-3 sweep appended a second rule to a proposal bullet → the staged-set boundary is its own bullet.
- **Sweep** — the four predicate sites now read identically: proposal, D1/D3, delta description, delta scenarios,
  controls 2.1/2.4/2.9.
- **Completed checks** — `openspec validate --all`: 27 passed, 0 failed; `spotlessApply`/`spotlessCheck` green; the
  delta is 40 scenarios (33 preserved, 7 added); rows 1–3 are in the ledger.
- **Scope** — the proposal round; nothing is implemented.
- **Deadline** — 15 minutes, the caller's policy.

## Gate decisions — the restart, the deep pass, the policy

- **Session-continuity violation** — round 2 started a fresh reviewer session without a recovery case; the ledger
  records it as `caller-restarted` on row 2, and rounds 2–4 ran in `ses_e7b188549ffeLWwNArbfWIqFrw`. The owner's
  recovery decision: accept the recorded violation and continue the loop in this session.
- **Deep review** — routing `RECOMMENDED` (`[repeated-class] 2 rows repeat a prior class`). The consultation's direction
  and the owner's decision: skip the proposal-stage pass, since the deep reviewer's checks need the implementation;
  route again once the implementation is complete.
- **Historical-content policy** — accepted by the owner as recommended: every staged archived `tasks.md` the merge does
  not exempt is judged in full, a deliberately staged historical file is refused and uses `--no-verify`, and the refusal
  never implies its tasks should be ticked.
- **Coherence residues the consultation found** — the proposal's "every unticked box is a defect", the design's opening
  "without judging the archive's history", its Risk bullet denying a legitimate unchecked case, and D1's categorical "a
  conflict resolution is still judged" → all restated as the actual predicate (whole staged content, incoming-merge blob
  equality as the only exemption).

## Closing hand-over — after round 4

- **Verdict** — nothing remains; no finding class raised, so no class repeats a prior round's.
- **Sweep confirmed** — the predicate sites and their bounds agree: `- []` unticked, `- [ ]` unticked, `- [~]`
  unsupported, `- [ ](url)` not a task, an indented box a task, and a path a merged revision lacks judged rather than
  aborting.
- **Checks** — `openspec validate --all` 27/0; the delta is 40 scenarios (33 preserved, 7 added); `spotlessCheck` green;
  claims rows unchanged and reproducing at `6f74424d`; rows 1–4 in the ledger, one reviewer session from round 2 onward.
- **Scope** — the proposal round: the routing decision and the owner's manual pass follow. This closing hand-over is not
  a review round; the ledger carries rounds 1–4.

## Round 6 — the categorical residues

- **Prior findings and their instances (round 5), with fixes** —
  1. The proposal still said a post-merge edit or conflict resolution "is still judged" while D1 and the delta exempt a
     resolution that reproduces a merged revision's blob → qualified identically ("a resolution or an edit that leaves a
     different blob is judged").
  2. D2 stated that every unticked box fails, beside D1's merge exemption, and the gate record restated the universal
     form → D2 is scoped "by task category … apart from the file-level merge exemption D1 states", and the policy bullet
     reads "every staged archived `tasks.md` the merge does not exempt".
- **Sweep** — every categorical claim about the check (every, any, always, never) across the proposal, the design, the
  delta and this record was re-read against the exemption; the remaining ones state the exemption's own terms.
- **Base refresh** — the branch was refreshed from `origin/main` while its work was uncommitted (`6f74424d` →
  `1842b105`, which touched `README.md`, two process-defect records and the ledger); the ledger's both sides survive
  (this unit's rows 1–4 and `correct-readme-mechanism-claims`' 1–3), and `README.md`'s guard enumeration still opens
  "catches eight commit-hygiene slips", so task 4.1's edit and count remain valid.
- **Completed checks** — `openspec validate --all`: 27 passed, 0 failed; `spotlessApply`/`spotlessCheck` green; the
  delta is 40 scenarios (33 preserved, 7 added); rows 1–5 are in the ledger.
- **Scope** — the proposal round; nothing is implemented.
- **Deadline** — 15 minutes, the caller's policy.

## Round 7 — the implementation

- **Prior findings** — the proposal rounds' findings are all closed; round 6 was clean. This round is the first pass
  over the implementation.
- **What changed** — `scripts/commit-hygiene.py` gains `quiet_rev_parse`, `merge_carries_unchanged` and
  `find_unticked_archived_tasks`, and `check_staged` calls the finder after the archive-sync check; the hook header,
  `README.md` (eight→nine) and the `AGENTS.md` `git add <dir>` gotcha name the refusal, and `docs/ideas.md` loses the
  implemented 2026-10-19 entry; the change's process-defect record is written; tasks 1.1–5.3 are ticked.
- **Controls** — twelve `ArchivedTasksGuardTests` over a repository whose claim names the archived change: an unticked
  task at top level and indented (named, with the ticked sibling absent from the report), a fully ticked file, a
  never-staged archived tasks file, `- []` as unticked, `- [~]` as an unsupported marker, `- [ ](url)` as not a task, an
  archived non-`tasks.md` file, the indexed-versus-worktree pair, a live→archive `git mv`, and three merge cases
  (unchanged blob exempt, edited blob refused, absent path judged rather than aborting).
- **Mutations proven** — the staged selection replaced by a worktree listing, the staged-blob read replaced by a
  worktree read, blob equality replaced by path membership, the `(` exclusion removed, the quiet lookup made raising,
  and the basename selection dropped each fail their control.
- **Completed checks** — `scripts/test-commit-hygiene.py` 218 tests OK; `openspec validate --all` 27/0; `spotlessCheck`
  green; the Docker-free `check` `BUILD SUCCESSFUL`.
- **Scope** — the implementation round; routing and the deep pass follow.
- **Deadline** — 15 minutes, the caller's policy.

## Round 8 — the task form widened and bounded

- **Prior findings and their instances (round 7), with fixes** —
  1. `TASK_LINE_RE` accepted only `-` and `*`, while the delta says "a list item" — an unticked `+` or ordered task
     would have passed. The pattern now accepts `-`, `*`, `+` and ordered markers, D3's heading and What state that set,
     and a control stages `+ [ ]` and `1. [ ]` and requires both to be named.
  2. The "at most one character" bound was unexercised — widening it left every control passing. A control stages
     `- [see below] a note` and requires the commit to proceed; widening the bound fails it.
  3. The round-7 hand-over said `docs/ideas.md` "names the refusal", but that file loses the implemented entry and names
     nothing — corrected.
- **Mutations proven** — narrowing the marker set back to `-`/`*`, and widening the bracket bound, each fail their
  control.
- **Completed checks** — `scripts/test-commit-hygiene.py` 220 tests OK; `openspec validate --all` 27/0; `spotlessCheck`
  green.
- **Scope** — the implementation round; the deep pass follows.
- **Deadline** — 15 minutes, the caller's policy.

## Round 9 — every alternative of the task pattern controlled

- **Prior finding (round 8), with its fix** — the widened pattern's `*`, the ordered `)` form and the tab indentation
  were live behavior with no control. The plus/ordered control now stages `+`, `1.`, `*` and `1)` lines and requires
  each to be named, and the unticked control adds a tab-indented box; dropping `*`, `+`, `)` or the tab alternative each
  fails a control (four mutations run).
- **Process defect** — the first attempt to extend those controls aborted on a stale anchor and wrote nothing, so the
  mutation proof first printed two `STILL PASSES (blind)` results; the edits were re-applied and every mutation then
  failed its control. Recorded as a second instance of the unit's record #3.
- **Completed checks** — `scripts/test-commit-hygiene.py` 220 tests OK; `openspec validate --all` 27/0; `spotlessCheck`
  green; rows 1–8 in the ledger.
- **Scope** — the implementation round; the deep pass follows.
- **Deadline** — 15 minutes, the caller's policy.

## Round 10 — the live-path boundary and the record's fields

- **Prior findings and their instances (round 9), with fixes** —
  1. The archive-prefix half of the selection had no control: dropping it would have made the guard judge a unit's own
     live `tasks.md`, the very file a mid-unit commit stages. A control stages `openspec/changes/unit-a/tasks.md` with
     an unticked box and requires the commit to proceed; dropping the prefix test fails it.
  2. The process-defect record's second instance carried no `Recorded`, `Stage` or `Consequence` of its own and said
     "the same round" about a different stage. It is now record #4 with the full field set, and #3 carries only its
     proposal-stage instance.
- **Mutations proven** — dropping the archive-prefix test fails the live-path control (nine mutations ran this unit).
- **Completed checks** — `scripts/test-commit-hygiene.py` 221 tests OK; `openspec validate --all` 27/0; `spotlessCheck`
  green; rows 1–9 in the ledger.
- **Scope** — the implementation round; the deep pass follows.
- **Deadline** — 15 minutes, the caller's policy.

## Closing hand-over — after round 11

- **Verdict** — nothing remains; no class raised. Every finder boundary carries a control that fails when it is removed:
  the archive prefix, the `tasks.md` basename, staged-set membership, the staged blob, the task-line pattern, the three
  dispositions, the merge exemption and its quiet miss.
- **Checks** — 221 tests OK; `openspec validate --all` 27/0; `spotlessCheck` green; the Docker-free `check`
  `BUILD SUCCESSFUL`; rows 1–11 in the ledger, one reviewer session from round 2 onward; the deep pass's `R1` fixed and
  verified by round 11.
- **Scope** — the implementation round: the owner's manual pass follows, then commit, CI, archive and merge. This
  closing hand-over is not a review round.

## Deep review — pass 1

- **Session** — `ses_e7ae40944ffelDyGgXDH16YLhB`; routing `RECOMMENDED`
  (`[repeated-class] 6 rows repeat a prior class`); the deep pass was deferred from the proposal stage to this one by
  the owner's decision.
- **Verdict** — one finding, `R1` (should-fix): the change ships the guard both retrospectives proposed, but their
  suggestion rows and prose still said it was parked or not applied.
- **R1 — `FIXED`.** The 2026-10-15 S4 row and its prose, and the 2026-10-23 S4 row and its P2 prose, now read
  **Applied** — `guard-archived-task-boxes`, with what the guard refuses; task 4.4 records it.
- **Checks passed** — the finder, staged-blob read, task pattern, merge exemption and failure paths against the delta;
  every delta scenario has a control; the delta keeps the main spec's 33 scenarios verbatim and adds 7; the claims rows
  reproduce at `1842b105`; the surfaces agree (nine refusals); the four records carry their fields and no `Decision`
  line.

## Ship

- **Approval** — the owner approved the implementation and, after this consultation, shipping.
- **Self-application** — the hook runs the working-tree `scripts/commit-hygiene.py`, so the implementation commit
  already runs the new code; the archive commit is the first shipping commit whose staged set carries this change's own
  archived `tasks.md`, which is why every task is ticked before the archive.
- **Archive** — the change dir is formatted before `openspec archive` moves it (archived markdown is outside Spotless),
  and both sides of the move plus the `openspec/specs/**` sync are staged and inspected at the archive commit.
