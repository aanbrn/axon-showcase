# Tasks

## 1. The widened sweep

- [x] 1.1 `.opencode/agent/architecture-auditor.md` — widen every statement of the reported set, swept by both token
      families: the frontmatter `description` (both sentences), the advisory lead, the sweep list, the parked-entry
      bullet beside the ADR-gate one, the inverse-class sentence, and the per-item question clause. **Done.**
- [x] 1.2 `.opencode/commands/audit-architecture.md` — its `description` and its step-1 read-list. **Done.**
- [x] 1.3 `AGENTS.md` — the same sweep: the bullet's lead, its report summary, its swept-surface sentence, and its
      inverse sentence. **Done.**
- [x] 1.4 `README.md` — the prose, the agent table row, and the slash-command table row. **Done.**
- [x] 1.5 `docs/ideas.md` — the entry this change implements is removed. **Done.**
- [x] 1.6 `AGENTS.md` — the falsified absence claim now reads "No auditor **owns** either file", naming the sweep's
      reach into `docs/ideas.md`. **Done.**

## 2. The spec delta

- [x] 2.1 The `MODIFIED` delta carries the requirement's full body and all five scenarios in order, plus the parked
      member of the inverse class and two scenarios. **Done** (written at the proposal).

## 3. Verification

- [x] 3.1 `spotlessApply`/`spotlessCheck`, `openspec validate --all` (27/0) and the marker guard are clean; the
      120-character check flags only the formatter-owned table rows and the by-contract tsv rows. **Done.**
- [x] 3.2 Run a `review-quick` pass over the implementation, repeating until clean: seven rounds across the two stages
      (proposal 8-11, implementation 12-14). They caught my own handiwork three times — the tick records I spliced
      corrupted every ticked task, the residue of that fix claimed a form it no longer used, and the class name and a
      list member were stated twice — all fixed by re-authoring or deleting, per the repeated-class remedy. The reused
      reviewer session (rounds 8-14) is what made the residues visible.
- [ ] 3.3 **Smoke-run the widened subagent** with positive controls: a seeded stale `docs/ideas.md` entry it must
      report, and a parked entry it must not (one whose citations are context). **Record the run's output and which
      control each finding came from, and verify each seed's premise against the repository before reading the run** — a
      seed the repository does not exhibit leaves the branch unexercised rather than failing, and the negative control
      is vacuous unless its described work is confirmed absent. Needed because the edited definition is served from the
      pre-change copy in this session, this smoke-run is a precondition for the **archive**: the owner's reload or new
      session comes first, and the run happens before the archive commit.
- [x] 3.4 Run the `lesson-capture` subagent over the diff, apply its durable proposals, and record the applied net
      `AGENTS.md` delta: **+2 lines**, one clause merged into the quick-review bullet's claims-table anchor sentence — a
      site named for a later sweep is quoted by text or token, never by a line number, since a concurrent merge moves
      it. It judged the tick-record corruption and the both-token-family sweep covered by existing rules, and found two
      live defects in this diff, both fixed: the definition's new bullet was detached from the sweep list by a blank
      line, and the proposal's Impact list was split by one. No retirements.
- [x] 3.5 Asked after 3.2 was clean and 3.4 done; the owner approved. **Done.**

## 4. The change's own records

- [x] 4.1 **Owed — for the archive commit**: the `agent-skills` `## Purpose` says the auditor reports "where a
      deferral's recorded `Revisit when:` condition appears met", which this change widens to a record whose premise is
      due (a deferral's condition, or a parked `docs/ideas.md` entry whose work has landed). A delta cannot carry a
      Purpose, so the edit lands with the archive; the implementing PR's diff therefore carries no main-spec edit beyond
      the delta, as the report states. Purpose), stating in the report that the implementing PR's diff carries no spec
      edit of its own beyond the delta.
- [x] 4.2 Record the pre-write derivation pass as a task (every unit owes it). It ran: the claims table is in
      `design.md`, and its rows are derived — the stale-entry incident from `69d26b02` and the removed entry, the owner
      set from the four definitions, the inverse shape from the requirement's own scenarios, and the misfire row from an
      entry whose citations are context.

- [x] 4.3 **Done**: rounds 1-14 are in `docs/review-rounds.tsv`. append is the calling agent's per-round act, so a
      deferred append lands late or never.

## Workflow follow-up

- Archive the change after the owner approves and 3.3 has run; the archive commit rides this PR.
