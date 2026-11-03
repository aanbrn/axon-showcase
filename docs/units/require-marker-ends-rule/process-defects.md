# Process defects — require-marker-ends-rule

Process defects observed while working this change. Recorded under `AGENTS.md`'s "Workflow: Record process defects".

### require-marker-ends-rule#1 — A reconciliation script dropped rule text when a chain ended mid-line

- **Recorded**: 2026-11-03, by the writing session.
- **Stage**: implementation.
- **Expected**: moving a marker chain out of its block removes only the chain, leaving the surrounding sentences intact.
- **Observed**: the first version of the move script assumed the chain ended its line, so it discarded the rest of the
  chain's line. Three chains ended mid-line and each lost the text after it — for example "A workflow whose run is the
  only verification must also leave the evidence a" and "The markdown scope is `docs/`, `AGENTS.md`, `README.md`,".
- **Evidence**: the scripted join printed

  ```
  the follow-up in `docs/ideas.md` and name it in the change's report. failure needs: it deletes its own environment
  ```

  so the dropped sentence's subject and verb were gone; `git diff` after the three moves showed the missing spans.

- **Consequence**: `AGENTS.md` was restored from `HEAD` (`git checkout -- AGENTS.md`, after a copy to the scratch
  directory), the script gained a join that keeps the tail line's text, and the reconciliation was re-run over all
  thirteen blocks and diffed. Nothing remains broken.
- **Knowledge boundary**: observed — the script's output lines, the diff, and the restore. Not checked — whether the
  same shape occurs in the other three guidance files' moves, which were run with the fixed script and whose per-file
  marker counts were compared before and after.

- **Decision** — no-change: the programmatic-edit rule already requires deriving a range's end from the item's own
  boundary and verifying every moved span by absence at its source.

### require-marker-ends-rule#2 — A new module constant was shadowed by a later definition in the same file

- **Recorded**: 2026-11-03, by the writing session.
- **Stage**: implementation.
- **Expected**: a constant added for the marker predicate is the one the predicate reads.
- **Observed**: `scripts/commit-hygiene.py` already defined `BLOCK_START_RE` for the markdown table-row check near the
  end of the file, so the new definition at the top was overwritten at import time. The predicate silently used
  `^\s*(?:#{1,6}\s|>|[-*+]\s|\d+\.\s|<)` instead, which does not treat a bold-lead paragraph as a block start.
- **Evidence**: the detector reported 40 blocks and flagged `captured: hand-over-unfinished-work` inside a two-marker
  chain; printing the block showed it running from `- **Pause and resume are the skill's procedure.**` through
  `**Procedure**` to the next numbered step, and `grep -n BLOCK_START_RE` listed both definitions.
- **Consequence**: the constant was renamed to `PARAGRAPH_START_RE`, and the detector then reported the 13 blocks the
  design's scan had found. Nothing remains broken.
- **Knowledge boundary**: observed — the duplicate definition, the block dump, and the corrected count. Not checked —
  whether other constants in the module are shadowed the same way.

- **Decision** — no-change: no prose rule is proposed — a repeated top-level binding is a check's subject, and the one
  occurrence was caught by the design's expected block count.
