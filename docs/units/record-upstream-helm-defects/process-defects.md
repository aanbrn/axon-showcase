# Process defects — record-upstream-helm-defects

Process defects observed while contributing two pull requests to `build-extensions-oss/gradle-helm-plugin`, an external
repository, together with those observed in this unit's own review loop. Recorded under `AGENTS.md`'s "Workflow: Record
process defects"; the upstream work carries no branch in this repository, so this unit is their home.

### record-upstream-helm-defects#1 — The rebase was piped into `head` and stopped mid-flight

- **Recorded** — 2026-11-07, by the writing session (`ses_e66c631b2ffe9B4tdVpOZL1X1P`), from an episode of the upstream
  delivery.
- **Stage** — implementation (rebasing the upstream #241 branch onto its new base).
- **Expected** — a rebase runs to completion, and no half-finished rebase is left in place.
- **Observed** — `GIT_SEQUENCE_EDITOR=cat git rebase -i -S --autosquash --force-rebase 6d51fb4 2>&1 | head -30` was run,
  and afterwards `git status` read `interactive rebase in progress; onto 6d51fb4` with
  `You are currently editing a commit while rebasing branch 'retry-repository-fetches-fix'`. `git rebase --abort`
  returned the branch to `2df9336`; the same command re-run without the pipe ended
  `Successfully rebased and updated refs/heads/retry-repository-fetches-fix`.
- **Evidence** — the two `git status` lines quoted above, and the reflog entry
  `6d51fb4 HEAD@{0}: rebase (start): checkout 6d51fb4`.
- **Consequence** — one abort and one full re-run; no work was lost and nothing had been published.
- **Knowledge boundary** — observed: the paused state, the abort, and the successful re-run. Assumed: that `head`'s
  early exit ended git's output stream. Not checked: git's own exit status, which the pipeline reported as `head`'s.
- **Decision** — no-change: below the promotion gate — one incident, repaired by an abort and a re-run; the pipe and
  exit-code class is recorded for reporting.

### record-upstream-helm-defects#2 — The first edit preceded the `Packs:` classification line

- **Recorded** — 2026-11-07, by the writing session (`ses_e66c631b2ffe9B4tdVpOZL1X1P`), from an episode of the upstream
  delivery.
- **Stage** — implementation (the Windows functional-test fixture).
- **Expected** — before the first edit, one `Packs:` line naming every pack loaded in full, or `Packs: none` with the
  reason.
- **Observed** — the work's first edit, the fixture file
  `helm-plugin/src/functionalTest/resources/executable/helm-fail-n-times.bat` in the upstream worktree, was made with no
  `Packs:` line stated. The classification was written afterwards, once the work had been judged to match no subject
  row.
- **Evidence** — session order: the edit precedes the first message carrying a `Packs:` line, and no pack had been
  loaded at that point.
- **Consequence** — the classification was retroactive; no produced artifact differs because of it.
- **Knowledge boundary** — observed: the ordering. Not checked: whether any gate or reviewer reads the line's timing.
- **Decision** — no-change: covered by `AGENTS.md`'s packs rule (state the `Packs:` line before the first edit).

### record-upstream-helm-defects#3 — A reviewer handoff omitted the subject pack

- **Recorded** — 2026-11-07, by the writing session (`ses_e66c631b2ffe9B4tdVpOZL1X1P`), from an episode of the upstream
  delivery.
- **Stage** — review (a delegated `review-quick` pass over an outward-facing artifact).
- **Expected** — a delegated reviewer's handoff names every applicable subject pack.
- **Observed** — the first handoff for an upstream pull-request comment named none. The reviewer stopped at
  classification, reporting the missing input and naming `jvm-testing` as the pack it judged applicable; it performed no
  review and produced no findings. Re-invoked with `jvm-testing` named, it reviewed the artifact and returned no
  findings.
- **Evidence** — the reviewer's reply, which named the missing pack and reported that no review had run, and the clean
  round its re-invocation returned.
- **Consequence** — one extra reviewer invocation; the refused run recorded no review round.
- **Knowledge boundary** — observed: the refusal and the repeat. Not checked: whether the refused invocation was billed.
- **Decision** — no-change: covered by the packs rule's delegated-agent sentence and its applied edited-paths bullet.

### record-upstream-helm-defects#4 — A gate's status was reported while its bot comment flagged uncovered code

- **Recorded** — 2026-11-07, by the writing session (`ses_e66c631b2ffe9B4tdVpOZL1X1P`), from two episodes of the
  upstream delivery.
- **Stage** — review (reporting the pull requests' check results to the owner).
- **Expected** — a gate's outcome is reported from what the gate produced, not only from its status flag, and the more
  so when the tool's own comment on the same run flags what the flag passes over.
- **Observed** — the report on upstream PR #241 told the owner the checks were all green including `codecov/patch`,
  while Codecov's comment on that pull request read `Patch coverage is 81.81818%` with `0 Missing and 4 partials`. The
  owner raised it later, and a commit covering those branches went in only at the next push. PR #240 then repeated the
  pattern: its comment reported `90.19608%` with five uncovered lines, surfaced again by the owner rather than by the
  report.
- **Evidence** — the two Codecov comment bodies. The report text existed only in the session and is described here
  rather than quoted.
- **Consequence** — #241's uncovered branches stayed unaddressed until the later push; #240 was delivered with its
  advisory unaddressed.
- **Knowledge boundary** — observed: both comments and both reports. Not checked: whether the project wants full patch
  coverage, or only that its threshold admits less. Not established: whether #240's delivery rested on a decision about
  its advisory, and by whom.
- **Decision** — proposed: https://github.com/aanbrn/axon-showcase/pull/866 — applied as P1 in `AGENTS.md`'s
  `**Checking CI status**` bullet by `generalize-process-defects-2026-11-08`.

### record-upstream-helm-defects#5 — A quick-review hand-over was written after the invocation returned

- **Recorded** — 2026-11-07, by the writing session (`ses_e66c631b2ffe9B4tdVpOZL1X1P`), from this unit's own review
  loop.
- **Stage** — review (the implementation round's first quick-review invocation).
- **Expected** — the invocation's hand-over is recorded before the invocation is waited on.
- **Observed** — round 1's hand-over was written after its report had arrived, and rounds 5, 6, 7 and 8 repeated it:
  each of those sections states the deviation in its own text rather than presenting the record as timely.
- **Evidence** — `docs/units/record-upstream-helm-defects/review-handover.md`'s "Round 1" section, whose "Recorded late"
  bullet says so.
- **Consequence** — nothing was lost: the invocation and its identifier are the ones the section names, and its findings
  are in `findings/round-1.md`. A stall during that wait would have had no record to recover it from.
- **Knowledge boundary** — observed: the ordering and the section's own statement. Not checked: whether any check reads
  the hand-over's timing.
- **Decision** — no-change: covered by `review-gate` §2 preflight; the `--handover` check verifies a section's presence,
  and only the ordering is unenforced.
