# Tasks

## 1. The widened sweep

- [ ] 1.1 `.opencode/agent/architecture-auditor.md` — sweep **every** statement of the reported set and widen each, by
      **both** token families rather than from a report's instance list: the report phrasing (`unrecorded intent`,
      `deferral whose recorded`, `due deferrals`, `design drift`) and the inverse phrasing (`is the inverse`,
      `deferred-condition`, `deferred-decision`, `for a deferred decision`). The hits at the time of writing: the
      frontmatter `description`; the advisory-lead (`:52`); the intent-clarification sweep list, where the parked-idea
      item is added beside the ADR gate it mirrors (a `docs/ideas.md` entry whose work has already landed, held against
      the change, commit, workflow or mechanism the entry describes rather than an artifact it cites); the sentence
      stating the deferred-condition item as the inverse (`:71-73`); and the per-item question clause (`:74-75`, "for a
      deferred decision") — both widened to cover the class's two members.
- [ ] 1.2 `.opencode/commands/audit-architecture.md` — carry the item in the step-1 read-list and in the command's
      `description`.
- [ ] 1.3 `AGENTS.md` — sweep by the same two token families and widen each statement: the architecture-auditor bullet's
      lead (`:1381`, "for design drift, unrecorded intent, and due deferrals"), its report summary (`:1385`), its
      swept-surface sentence (`:1386-1390`, which gains the parked-idea item), and the sentence stating the
      deferred-condition item as the inverse (`:1391-1392`). The finding classes stay in the `agent-skills` spec, so the
      bullet points rather than restates.
- [ ] 1.4 `README.md` — the three places that say what the audit reports: the slash-command table row, the agent table
      row, and the prose paragraph.
- [ ] 1.5 `docs/ideas.md` — remove the entry this change implements.

- [ ] 1.6 `AGENTS.md` — the statements this change **falsifies**, which carry no sweep token: the absence claim that "No
      auditor covers either file — the four auditors own `AGENTS.md`/`.opencode/`, the spec corpus, `docs/adr/` plus the
      architectural surface, and `README.md`" (`:1144-1149`). With the architecture-auditor now reporting a stale parked
      entry in `docs/ideas.md`, either widen that clause to name the widened coverage or record why it stays true (the
      auditor reports an entry's staleness; the file still has no _owner_). Sweep the shape, not only the token
      families: grep both files for `no auditor`, `un-audited`, and every "the four auditors own" phrasing, since an
      absence claim carries none of the site tokens the other sweeps use.

## 2. The spec delta

- [ ] 2.1 Add the `MODIFIED` delta for `showcase/quality/agent-skills`, carrying the "Unexplained design intent is
      surfaced for clarification" requirement's full body and all five scenarios in order, plus the parked-idea sweep
      and two scenarios (one per direction).

## 3. Verification

- [ ] 3.1 Run `spotlessApply` after the last edit to a Spotless-owned file (the `.opencode/` markdown and the README are
      in the markdown target), and confirm the definition still loads with its metadata intact, since a formatter can
      rewrite content a consumer parses, then `spotlessCheck`, `openspec validate --all`, and the manual 120-character
      check over the changed set.
- [ ] 3.2 Run a `review-quick` pass over the implementation, repeating until clean.
- [ ] 3.3 **Smoke-run the widened subagent** with positive controls: a seeded stale `docs/ideas.md` entry it must
      report, and a parked entry it must not (one whose citations are context). **Record the run's output and which
      control each finding came from, and verify each seed's premise against the repository before reading the run** — a
      seed the repository does not exhibit leaves the branch unexercised rather than failing, and the negative control
      is vacuous unless its described work is confirmed absent. Needed because the edited definition is served from the
      pre-change copy in this session, this smoke-run is a precondition for the **archive**: the owner's reload or new
      session comes first, and the run happens before the archive commit.
- [ ] 3.4 Run the `lesson-capture` subagent over the diff, apply its durable proposals, and record the applied net
      `AGENTS.md` delta on this task.
- [ ] 3.5 Ask the owner for the manual review pass, after 3.2 is clean and 3.4 is done.

## 4. The change's own records

- [ ] 4.1 Record the `agent-skills` capability `## Purpose` refresh for the archive commit (a delta cannot carry a
      Purpose), stating in the report that the implementing PR's diff carries no spec edit of its own beyond the delta.
- [x] 4.2 Record the pre-write derivation pass as a task (every unit owes it). It ran: the claims table is in
      `design.md`, and its rows are derived — the stale-entry incident from `69d26b02` and the removed entry, the owner
      set from the four definitions, the inverse shape from the requirement's own scenarios, and the misfire row from an
      entry whose citations are context.

- [ ] 4.3 Append each `review-quick` round's row to `docs/review-rounds.tsv` when that round's report arrives — the
      append is the calling agent's per-round act, so a deferred append lands late or never.

## Workflow follow-up

- Archive the change after the owner approves and 3.3 has run; the archive commit rides this PR.
