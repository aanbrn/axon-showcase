# Process defects — park-gradle-skills-idea

### park-gradle-skills-idea#1 — A glob-shaped search flag was repeated after it had already failed

- **Recorded** — 2026-10-28, by the writing session (`ses_e80386b7cffe4xWXlGt68Lvxjj`), during the implementation stage.
- **Stage** — implementation (the premise check for the parked entry).
- **Expected** — a search is run in a form the shell accepts, and a command that fails on its own quoting is not
  re-issued in the same shape.
- **Observed** — a `grep -rn … --include=*.md` ran unquoted, and the shell expanded the pattern before `grep` started.
  The same unquoted shape was used again later in the session and failed identically.
- **Evidence** — `zsh:1: no matches found: --include=*.md`, printed once per invocation; the same searches without
  `--include` returned the expected hits.
- **Consequence** — two commands failed and each was re-run; no artifact and no branch state was affected.
- **Knowledge boundary** — observed for this session's searches. Not checked whether `AGENTS.md`'s evidence-and-search
  guidance, or the `gradle-build`/`ast-grep` material it points at, states anything about quoting a pattern handed to a
  tool.

### park-gradle-skills-idea#2 — The `Packs:` line was written after the first edits, not before them

- **Recorded** — 2026-10-28, by the writing session (`ses_e80386b7cffe4xWXlGt68Lvxjj`), during the implementation stage.
- **Stage** — implementation.
- **Expected** — `AGENTS.md`'s subject-pack rule asks for one `Packs:` line naming the loaded packs before the first
  edit of a unit.
- **Observed** — the entry in `docs/ideas.md` and the clause in `.opencode/skills/gradle-build/SKILL.md` were written
  first; the `Packs:` line was stated only afterwards, so it reports the classification rather than preceding the edit
  it governs.
- **Evidence** — the session's transcript: both edits precede the line; `.opencode/skills/gradle-build/SKILL.md` had
  been read in full earlier in the same session, so the classification itself was not in question.
- **Consequence** — no artifact states a wrong classification; the missing pre-edit statement is not repaired by a later
  one.
- **Knowledge boundary** — observed for this session. Not checked whether earlier units also stated the line late.

### park-gradle-skills-idea#3 — Two consecutive decisions about an external artifact were taken before reading it

- **Recorded** — 2026-10-28, by the writing session (`ses_e80386b7cffe4xWXlGt68Lvxjj`), during the review stage.
- **Stage** — implementation (the parked entry's text), then review (round 2's finding on the same class).
- **Expected** — a claim about a surface outside the repository is evidenced by a query whose output the author has, and
  a `Revisit when:` gate states a condition that can be checked and can fire.
- **Observed** — the entry was first written from the announcement alone: it named a pending 1.0.1 as the next event
  while `1.1.0` had been published on 2026-10-21, and it keyed the gate to release notes recording the promised fixes,
  which no release body satisfies because the bodies are one-line summaries. After round 2 the gate moved to the skill's
  own `SKILL.md` at a published tag, a file that already existed when the first gate was written and had not been read.
- **Evidence** — `gh api repos/gradle/gradle-skills/releases/latest` → `1.1.0`, `2026-10-21T14:53:40Z`, body "Minor
  improvements to make the Best Practices skill work better on weaker agents. … Fixes potential copy problems when
  rolling back using the Wrapper Upgrade skill."; `gh api repos/gradle/gradle-skills/tags` lists
  `gradle-best-practices-v1.0.1`; that tag's `SKILL.md` lines 38, 128 and 139.
- **Consequence** — two review rounds raised the `premise` class, the second naming the unread artifact; the entry now
  quotes the tagged file. No artifact shipped on the first premise, and the quick loop caught it both times.
- **Knowledge boundary** — observed for this unit. Not checked whether other parked entries key a gate to a source their
  author never read.

### park-gradle-skills-idea#4 — A round's ledger row was appended a round late

- **Recorded** — 2026-10-28, by the writing session (`ses_e80386b7cffe4xWXlGt68Lvxjj`), during the review stage.
- **Stage** — review (the round ledger).
- **Expected** — each round's row is appended when its report arrives, before the next reviewer invocation.
- **Observed** — round 7's report arrived, the staging fix it named was applied, and round 8 was invoked before the row
  was appended; the ledger carried rounds 1–6 and 8.
- **Evidence** — `python3 scripts/commit-hygiene.py --unique-rounds --expect park-gradle-skills-idea:8` printed "Unit
  'park-gradle-skills-idea' is missing round 7: its rounds must be contiguous from 1" and exited 1; the `--expect-clean`
  invocation printed the same line.
- **Consequence** — the row was reconstructed from round 7's report and inserted before round 8's; the check caught it
  before the commit, so the ledger did not reach the repository with the gap.
- **Knowledge boundary** — observed for this unit. Not checked whether earlier units appended rows late; the
  session-continuity check validates the appended rows' fields, not the timing of the append.
