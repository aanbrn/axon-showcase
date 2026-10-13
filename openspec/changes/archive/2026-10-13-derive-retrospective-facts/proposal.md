# Proposal

## Why

The review of `docs/retrospectives/2026-10-13.md` took eleven rounds (the session's own count — the rounds are recorded
nowhere, the gap the per-round-record unit closes), and nearly every finding was one family: a hand-written **count,
enumeration, attribution, or present-tense claim** in prose — a PR count whose boundary needed re-deriving, an
`AGENTS.md` baseline taken a PR late, lesson→PR misattributions, an archive count date-scoped while the PR count was
PR-scoped, and lists under-describing their own stated derivation. The review was doing, one finding per round, what the
artifact's claims were **mechanically derivable** and the author wrote from memory.

`scripts/experience-analysis.sh` already gathers the raw material (the windowed PRs and git log, the archived changes,
`AGENTS.md`, `docs/ideas.md`), but it prints it raw — so the retrospective's countable claims are re-derived by hand at
authoring time and again by the review. The digest should **compute** them.

## What Changes

- **`scripts/experience-analysis.sh`** — a `Derived facts` block, printed with the rest of the digest:
  - the window's merged-PR set with its **boundary stated**: the `merged:>=<since>` search is inclusive of the `since`
    date (so it also returns that date's merges, some belonging to the previous retrospective's window) and open-ended
    (so a later run includes later PRs) — the block prints the search's count, each PR merged on the `since` date, and
    the previous retrospective's file, so the author resolves the split against it rather than a hard-coded exclusion;
  - the archived changes **scoped to the same window** (the archive dirs the window's commits added, via a time-explicit
    `since`, naming the `since`-date dirs so the author resolves the same overlap the PR side carries), so the PR count
    and the change count share one window;
  - `AGENTS.md`'s line/top-level-bullet/`captured:` metrics at HEAD and just before each `since`-date PR, so the author
    reads the window's baseline off the first counted PR (a squash merge's commit date can differ from its merge date,
    so the git window's start is not the window's start);
  - the `captured:` markers added since the window start, each with the commit that introduced it and that commit's PR.
- **`showcase/quality/agent-skills`** (delta) — the digest's described contents gain the block, with a scenario.
- **`.opencode/commands/retrospective.md`**, **`.opencode/agent/experience-analyzer.md`**, and **`AGENTS.md`** — each
  digest-contents description names the block. The two `README.md` `/retrospective` descriptions are **deliberately left
  alone**: they summarize the digest at capability level and already omit `docs/ideas.md`, so the block is an internal
  detail they need not carry.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/agent-skills`: the "Experience analysis is available to agents" requirement's digest description.

## Impact

- Files: `scripts/experience-analysis.sh`, `.opencode/commands/retrospective.md`,
  `.opencode/agent/experience-analyzer.md`, `AGENTS.md`, `docs/ideas.md` (parking the two review-contract follow-ons
  this analysis surfaced), and the delta spec for `showcase/quality/agent-skills`.
- Not edited (deliberately): `README.md`'s two `/retrospective` descriptions — capability-level summaries that already
  omit `docs/ideas.md`; the omission is stated rather than left silent.
- No build or test impact: a shell tool whose verification is running it against a known window and checking the block's
  numbers against the retrospective that already covers that window (`docs/retrospectives/2026-10-13.md`).
- **Not a CI check**: the record is frozen at its window, so a check that re-derived its numbers would fight the
  record's purpose. Generation is the route.
