# Tasks

Workspace: unit=correct-subagent-reload-rule;
path=/Users/afanasyev/Development/axon-showcase-correct-subagent-reload-rule; branch=correct-subagent-reload-rule;
writer=ses_e880ee8c5ffeSMHo0aBStdezbm

## 1. Records

- [x] 1.1 Confirm the workspace against the registry: `git worktree list --porcelain` shows this path on
      `refs/heads/correct-subagent-reload-rule`, the branch was created from freshly fetched `origin/main` (`02f839d9`)
      with `--no-track`, and the primary checkout (`reject-a-foreign-change-dir-at-commit`, holding another unit's
      staged work) is untouched.
- [x] 1.2 Run the pre-write derivation pass and land it as `design.md`'s claims table; re-derive the table if the
      change's facts move before the manual pass.
- [x] 1.3 Run the probe the rule's rewrite rests on, and record its procedure, results, closed false-success channels,
      excluded confound, and cleanup in `design.md`.
- [x] 1.4 Refresh the branch when `origin/main` advanced mid-unit (four commits): stash,
      `git merge --ff-only origin/main`, pop, and reconcile both sides rather than picking one. `AGENTS.md`, the
      review-gate skill and the change dir merged cleanly; `docs/ideas.md` and `docs/review-rounds.tsv` conflicted and
      keep **both** sides (upstream's new entries plus this unit's). The unit's base is now `905e33a5`, and claims rows
      5 and 7 were re-derived against it after the final edit (row 7). The refresh made two records stale and both are
      corrected: the base revision line, and the checkpoint's note that the `pause-resume-unit` skill was not on this
      branch.

## 2. Rewrite the rule

- [x] 2.1 Rewrite the "A subagent added or edited mid-session is not registered …" bullet in `AGENTS.md`: the rule
      states current behaviour only (the dated incident stays in the change record), the V2 mechanism and its two
      disable conditions replace the restart requirement, the restart is demoted to a fallback, and the marker probe
      carries the confirmation. Verify: the bullet names the probed version (`v2.1.4`), scopes the behavioural claim to
      a project definition in an interactive session, names the surfaces it leaves unverified, and keeps its retained
      obligations.
- [x] 2.2 Drop the neighbouring frontmatter bullet's now-vacuous contrast (its "unlike the not-registered … failure"
      clause). Verify: `grep -c "Unknown agent type" AGENTS.md` → `0`.
- [x] 2.3 Sweep the repository for other copies of the restart claim, one command per phrase, over `AGENTS.md`,
      `README.md`, `docs/`, `.opencode/` and `openspec/specs/`: `grep -rn "restart OpenCode"` and
      `grep -rn "reloads its agent list"`. Result: the only other hits are the MCP-server statements (`README.md` and
      the `setup-agent-tools` command and skill), whose subject is a server list read at startup; they stay as they are.
- [x] 2.4 Split the bullet's lumped obligations in place, per `design.md` Decision 5 and the advisor's recommendation:
      the marker probe as a short numbered sequence, one bullet per remaining obligation, with no rationale, constraint,
      or `captured:` marker moved away from its rule. Verify: `review-quick` raises no `structured-writing` finding
      against the rule, and claims row 7 records the measured growth.
- [x] 2.5 Apply the advisor's bounded pass on the final state: drop the capture clause that restated existing coverage,
      narrow the reload claim to what the evidence shows (the watcher stops, so a later edit may not apply — not that
      the startup body is served), separate the two tree revisions in the verification records, and replay the published
      probe procedure literally on a scratch agent. Replay result: the baseline answered no marker, the marker added to
      the definition came back in the reply, and the scratch file was deleted with `.opencode/agent/` clean. It exposed
      one gap, now fixed: step 1 assumed an existing definition while the rule also covers a new one.

## 3. Verification

- [x] 3.1 Run `./gradlew spotlessApply` after the last edit, then `spotlessCheck` → green.
- [x] 3.2 Run `./gradlew check` on the current tree (`48 35`; 264262 bytes against `905e33a5`'s 263638) →
      `BUILD SUCCESSFUL in 36s`, covering the markdown format, the guidance-file gates (`verifyCapturedMarkers`,
      `verifyInstallCommands`, `verifyDocumentedPins`), the chart packaging, and build-logic's tests. Earlier runs: 37s
      on the pre-refresh tree (261339 bytes against 261149), 34s after the refresh. Two runs failed: one while the tree
      was edited mid-run, one **unattributed** — its log was tail-truncated, so unconfirmed, not refuted.
- [x] 3.3 Run `openspec validate correct-subagent-reload-rule` on the current tree → valid, with `skip_specs` reported
      as declared; `openspec validate --all` → 27 passed, 0 failed (run before the refresh).
- [x] 3.4 Re-derived claims rows 7 and 8 after the capture and again after the advisor-directed pass; the current tree's
      `AGENTS.md` numstat is `48 35` (+13 lines; 264262 bytes against `905e33a5`'s 263638), and the probe left
      `review-quick.md` at its base blob `f6e69271…` with `.opencode/agent/` clean.

## 4. Review gate

- [x] 4.1 Ran the `review-quick` loop to a clean close; its rounds are `docs/review-rounds.tsv`'s rows for this unit.
      The loop closed, reopened for the advisor-directed pass on the final state, and closed again: the reopened pass's
      closing round reports no new observation, and
      `commit-hygiene.py --unique-rounds --expect-clean     correct-subagent-reload-rule` exits 0.
- [x] 4.2 Derived the deep-review floor: `RECOMMENDED`, signal `[repeated-class]` (the derivation reported 8 rows
      repeating a prior class at the time); resolved to run the pass rather than decline it. Pass 1 (initial) ran: it
      confirmed the rule's proportionality to its two evidence sources, the split's preservation of every obligation and
      marker, `skip_specs`, and the reproducible claims rows, and raised B1 (the loop had no clean closing round,
      `--expect-clean` exit 1) — `FIXED_AND_VERIFIED` by the clean closing round — and N1 (the resume checkpoint
      restated the task list) — `FIXED_AND_VERIFIED` by freezing that section.
- [x] 4.3 Ran the `lesson-capture` decision; the skip condition did not hold (the rounds recorded classes beyond the
      local set). **Applied:** two additions — `.opencode/skills/review-gate/SKILL.md` §2 step 2 gains "verify each fix
      against the finding's own claim" (two fixes in this unit silently no-opped and were reported fixed), and
      `AGENTS.md` gains one clause: a rule recording a tool's behavior names the version probed and the surfaces it did
      not establish. **Removed on the advisor's review:** the capture's second `AGENTS.md` clause (a state no artifact
      owns is named by its measured evidence) restated what the durable-claims bullet and the review gate's text-anchor
      rule already determine, so it was dropped as `already covered`. **Net delta:** `AGENTS.md` carries the capture's
      one clause, `review-gate` +5 lines; no retirement candidate was found. **Already covered, no rule:** the
      installer-pin rewrite, a `FIXED_AND_VERIFIED` set before the verifying pass, a task ticked ahead of its verify
      clause, the probe's baseline call, and split-in-place versus extraction. **Authoring-system note, parked:**
      `.opencode/skills/pause-resume-unit/` has no clause freezing the checkpoint at the resume; it landed on `main`
      with this unit's refresh, so the correction is parked in `docs/ideas.md` (2026-10-26) rather than folded into a
      closed loop. The `review-gate` edit owes no spec delta: the `agent-skills` requirement delegates the caller's
      steps to it.
- [ ] 4.4 Stage the unit's new files (explicit paths), ask the owner for the manual review pass, and end the turn —
      nothing committed, pushed, opened as a PR, or archived before the approval. Before staging, diff
      `.opencode/package.json` against `HEAD`: the running v2 binary rewrites its `@opencode-ai/plugin` pin, and round 1
      caught such a rewrite (`1.19.4` → `1.19.3`), reverted here.

## Resume checkpoint

Snapshot frozen at the round-10 freeze point on 2026-10-26; not maintained after it, because a field that restates
another artifact's state drifts. The live state is the task list above and `docs/review-rounds.tsv`.

Recorded 2026-10-26 on the owner's pause request. The field set is `.opencode/skills/pause-resume-unit/SKILL.md`'s,
which landed on `main` while this unit was in flight; nothing was committed, stashed, pushed, cleaned, or reset for the
pause.

- **Time and revision** — 2026-10-26; `git rev-parse HEAD` → `02f839d9be8950ccd437a74e46175b5248fe116c`, so the whole
  unit is uncommitted on its branch.
- **Workspace** — the `Workspace:` record at the top of this file: unit `correct-subagent-reload-rule`, path
  `/Users/afanasyev/Development/axon-showcase-correct-subagent-reload-rule`, branch `correct-subagent-reload-rule`,
  writer `ses_e880ee8c5ffeSMHo0aBStdezbm`.
- **Stage** — the review loop, at task 4.1. Its state is `docs/review-rounds.tsv`'s (the rows for this unit), not this
  field's; the split of the bullet's obligations is in place on the owner's direction (2026-10-26, on the advisor's
  recommendation). Not started: the capture (4.3), the deep-review routing (4.2), staging, and the manual pass (4.4).
- **Next step** — finish the quick loop, then the `lesson-capture` decision and the deep-review routing
  (`python3 scripts/review-routing.py --change correct-subagent-reload-rule`), then stage and ask for the manual pass.
- **Change set** — `git status --porcelain --untracked-files=all`:
  - modified: `AGENTS.md`, `docs/ideas.md`, `docs/review-rounds.tsv`;
  - untracked: `openspec/changes/correct-subagent-reload-rule/{.openspec.yaml,proposal.md,design.md,tasks.md}`. Nothing
    is staged; `.opencode/agent/review-quick.md` is byte-identical to the base (`f6e69271…`), and
    `.opencode/package.json` is unmodified (round 1's installer-rewritten pin was reverted).
- **Blockers and open decisions** — none outstanding. The owner decided the split in place on 2026-10-26 (on the
  advisor's recommendation), and the parked extraction is a separate idea. The reviewer session was replaced after three
  invocations produced no report — `stopped-responding`, recorded in ledger row 6, with the prior session's identifier
  in rows 1–5.
- **Pending decision** — none. The next owner interaction is the manual-review approval this unit asks for once the loop
  closes.
- **Findings no record holds** — round 8's instances, the latest report, all fixed:
  1. `claim-unreproducible` — claims row 7 still said `44 34` against a tree giving `43 34`; corrected.
  2. `single-sourcing` — this checkpoint restated the loop's round-by-round state, which `docs/review-rounds.tsv` owns,
     and drifted twice; the Stage and Next-step fields now point at the ledger and task 4.1 and carry only the
     pause-specific state.
  3. `missing-task` — task 2.4 was unchecked though its verify clause had held since round 6; ticked. Rounds 1–7's
     instances live in their report and their dispositions in the ledger rows, `design.md` Decisions 1–5, and claims
     rows 4–11.
- **Operations in flight** — none. The last `./gradlew check` finished (`BUILD SUCCESSFUL in 37s`), and no build,
  container, or daemon was stopped.

## Workflow follow-up

- After the owner approves: run CI, archive the change (`skip_specs`), and merge per the `ship-change` skill; remove
  this worktree afterwards.
