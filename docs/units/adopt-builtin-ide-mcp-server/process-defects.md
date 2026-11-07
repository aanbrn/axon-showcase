# Process defects — adopt-builtin-ide-mcp-server

### adopt-builtin-ide-mcp-server#1 — a truncated grep became a claims row

- **Recorded** — 2026-11-06, by the unit's writing session.
- **Stage** — proposal.
- **Expected** — the pre-write pass derives a claims row's figure from the command's own complete output, since a slice
  of a tool's output is a shorter answer, not the same answer.
- **Observed** — the `skip_specs` evidence was taken from `grep -rn "mcp\|MCP" openspec/specs/ | head -20`, and the row
  stated that the corpus's only `MCP` mention is the failed-server recovery requirement. The round-1 review's
  `claim-unreproducible` finding ran the same grep without the slice and found two further passages — the capability's
  Purpose and the technical advisor's tool-deny list.
- **Evidence** — the finding's row: the claims table's row 12, which stated that the corpus's only `MCP` mention is the
  failed-server recovery requirement. Re-running `grep -rn "mcp\|MCP" openspec/specs/` lists
  `showcase/quality/agent-skills/spec.md` lines 33, 34, 1798, 1804, 2369, and 2371.
- **Consequence** — one wrong claims row, caught before the proposal's manual pass. Repaired by replacing the row's
  source with the name-grep that actually decides `skip_specs` (`grep -rn "setup-agent-tools" openspec/specs/` returns
  no hits) and describing the `mcp` hits as three passages. Nothing else rested on the row.
- **Knowledge boundary** — observed: the truncated command, the row it produced, and the finding. Assumed: that the
  slice, not a stale file, produced the omission. Not checked: whether any other row in this unit's table rests on a
  similarly sliced read.
- **Decision** — no-change: already covered by `AGENTS.md`'s evidence rule — cite a command's complete output, never a
  `head`/`tail` slice or a grep filter.

### adopt-builtin-ide-mcp-server#2 — a green formatter check recorded before the file's last edit

- **Recorded** — 2026-11-06, by the unit's writing session.
- **Stage** — proposal.
- **Expected** — a record that states a gate passes is written from a run made after the artifact's last edit, since the
  edit that follows a green check can falsify it.
- **Observed** — the round-3 section was appended to `docs/units/adopt-builtin-ide-mcp-server/review-handover.md` after
  the last `./gradlew spotlessApply`, and its "Completed checks" line still stated that `spotlessApply` and
  `spotlessCheck` pass. The round-3 review re-ran `./gradlew spotlessCheck` and it failed with `:spotlessMarkdownCheck`
  naming that file.
- **Evidence** — round 3's finding: `review-handover.md` round 3, "Completed checks", against a re-run of
  `./gradlew spotlessCheck` that exits 1 on the same file.
- **Consequence** — one false green in a unit record, caught by the review before the manual pass; the staged set would
  also have been refused by the pre-commit guard. Repaired by formatting the tree and rewriting the line to state the
  run actually made and when it was made.
- **Knowledge boundary** — observed: the appended section, the recorded line, and the failing re-run. Assumed: nothing
  about other units' records. Not checked: whether any earlier "passes" line in this unit's records was likewise written
  before a later edit.
- **Decision** — no-change: already covered by `AGENTS.md`'s Formatting convention — run `spotlessApply` after the final
  write, and treat the tick claiming a gate as the edit that falsifies it.

### adopt-builtin-ide-mcp-server#3 — an anchored edit dropped the section heading it replaced

- **Recorded** — 2026-11-06, by the unit's writing session.
- **Stage** — proposal.
- **Expected** — an insertion anchored at the end of a section supplies the section's own heading when the anchor
  includes it, so the file keeps one heading per section.
- **Observed** — the round-6 section was inserted with an `oldString` that ended at the round-5 heading
  (`## Round 5 — proposal stage, widened scope`), and the replacement did not re-emit that heading, so round 5's body
  was left under the round-6 heading with no heading of its own. A `grep -n "^## "` over the file showed the missing
  heading; the file was reconstructed from its own line blocks with the round-5 heading restored.
- **Evidence** — the grep's output listed rounds 1–4 and 6, with round 5's `- **Unit**` block directly under round 6;
  after the repair the same grep lists rounds 1–6 with one heading each.
- **Consequence** — one unit record was mis-structured between the edit and its verification in the same turn; nothing
  was committed, and no other file was affected. Repaired before the round-6 invocation.
- **Knowledge boundary** — observed: the edit's anchor, the resulting heading list, and the repair. Assumed: that no
  other section in this file lost a heading the same way. Not checked: whether the same anchored-insertion pattern was
  used elsewhere in this unit's artifacts.
- **Decision** — no-change: already covered by `AGENTS.md`'s programmatic-edit rule — verify at the granularity edited
  (the heading list for a section move) and re-emit a heading an anchor spans.

### adopt-builtin-ide-mcp-server#4 — the same anchored-insertion defect recurred, and the heading check now gates it

- **Recorded** — 2026-11-06, by the unit's writing session.
- **Stage** — proposal.
- **Expected** — record #3's mechanism is recognised before the next anchored insertion, so a heading the anchor spans
  is re-emitted or the anchor excludes it.
- **Observed** — the round-7 hand-over section was inserted with the same anchor shape (ending at the round-6 heading),
  the round-6 heading was dropped again, and the file then held two copies of round 7's body beside an orphaned round-6
  body. `grep -n "^## "` showed five headings for seven sections, and `grep -c '^- \*\*Deadline\*\*'` showed seven
  deadline lines for five headings; the file was rebuilt from its own blocks in round order.
- **Evidence** — the heading list before the repair (rounds 1–5 and 7) and the deadline count (7) against it; after the
  repair, seven headings, seven deadlines, and each section's `Scope` line naming the prior round.
- **Consequence** — one unit record was duplicated and mis-structured across two edits in two consecutive turns; nothing
  was committed and no other file was affected. Repaired by reconstruction; the per-insert guard is now a heading and
  deadline count before any anchored insert into this file.
- **Knowledge boundary** — observed: both anchors, both heading lists, the duplicate bodies, and the reconstruction.
  Assumed: that the reconstruction preserved each block's text unchanged. Not checked: whether the same anchor shape is
  used in other files this unit edits.
- **Decision** — no-change: already covered by `AGENTS.md`'s programmatic-edit rule — verify at the granularity edited
  (the heading list for a section move) and re-emit a heading an anchor spans.
