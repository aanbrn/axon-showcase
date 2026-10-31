# Spec Delta

## MODIFIED Requirements

### Requirement: Experience analysis is available to agents

The repository SHALL provide an `experience-analyzer` agent subagent that, given a gathered digest of recent experience
(merged pull requests, archived changes, `AGENTS.md` gotchas, `docs/ideas.md`, and a derived-facts block), produces a
retrospective and forward-looking improvement suggestions. The derived-facts block SHALL carry the window's countable
claims already resolved — the merged-PR boundary facts (the search's count, each PR merged on the `since` date, and the
previous retrospective's file), the archived changes scoped to the window's commits, `AGENTS.md`'s line,
top-level-bullet, and `captured:` metrics at HEAD and just before each `since`-date PR (so the window's baseline is read
off the first counted PR), each `captured:` marker added since the window start with its introducing commit, and the
window's review rounds from `docs/review-rounds.tsv` — its round count, the classes raised, and the compliance-miss
count stated for the window proper (the rows dated after the `since` date) beside the `since`-inclusive figures, with
the classes normalized to the reviewer's canonical vocabulary so a label recorded before that vocabulary landed compares
with the ones that follow it, each class counted once per round that raised it regardless of how many of that round's
labels normalized to it and stated as a round occurrence rather than a share of the compliance-miss count, and a
pre-canonical label that cannot be normalized reported as such rather than passed through silently — and the
process-defect records the window's commits touched, as an index naming each record file with its incident headings and
stating the unique-file count, the incident-heading count, the revision the index was derived at, the files the `since`
date's own commits touched, a selected path absent at that revision rather than dropping it, and an empty selection as
such rather than leaving it blank, so the boundary is resolvable and a record's body can be read — so the retrospective
is written around derived facts rather than re-deriving them by hand. The retrospective SHALL group shipped PRs by
theme, summarize lessons learned, and record went-well/went-wrong observations. Each improvement suggestion SHALL be
classified as a system suggestion (addressed via `docs/ideas.md` or an OpenSpec proposal) or a process suggestion
(addressed via `AGENTS.md` or a subagent definition). The agent SHALL propose the retrospective and suggestions without
editing files; the main agent verifies and applies them.

Beyond grouping the shipped work, the retrospective SHALL trace each significant failure it reports to a root cause and
name the earliest development stage at which the problem could reasonably have been prevented or detected — drawing the
evidence from the digest's review-round data rather than restating the symptom — so an improvement can move prevention
earlier. When a causal claim rests on a process-defect record, the retrospective SHALL have drawn it from that record's
body rather than from the index's heading, and SHALL name the record it read. It SHALL evaluate the previous
retrospective's recommendations when their outcomes can be observed, stating for each whether it was applied and whether
the available evidence shows it helped, or that the window cannot yet show it, rather than presuming effectiveness from
the absence of the failure. Each improvement suggestion SHALL state the failure pattern it targets, the observable
outcome expected, and how that outcome would be verified, so a later window can judge whether it worked.

#### Scenario: A retrospective is produced from gathered experience

- **WHEN** the main agent gathers a digest of recent experience and invokes the `experience-analyzer` subagent
- **THEN** it returns a retrospective grouping shipped PRs by theme, summarizing lessons, and recording went-well and
  went-wrong observations

#### Scenario: The digest carries the window's countable claims

- **WHEN** the main agent gathers the digest for a retrospective window
- **THEN** the digest includes a derived-facts block stating the window's merged-PR boundary facts (the search's count,
  each PR merged on the `since` date, and the previous retrospective's file), the archived changes scoped to the
  window's commits with the `since`-date dirs named for the same resolution, `AGENTS.md`'s line, top-level-bullet, and
  `captured:` metrics at HEAD and just before each `since`-date PR (so the window's baseline is read off the first
  counted PR), each `captured:` marker added since the window start with its introducing commit, the window's review
  rounds from `docs/review-rounds.tsv` — its round count, the classes raised, and the compliance-miss count for the
  window proper stated beside the `since`-inclusive figures, with the classes normalized to the reviewer's canonical
  vocabulary, each class counted once per round that raised it, and a pre-canonical label that cannot be normalized
  reported as such — and the process-defect records the window's commits touched, as an index naming each record file
  with its incident headings and stating its unique-file and incident-heading counts, the revision it was derived at,
  the `since` date's own touches, a selected path absent at that revision, and an empty selection — so the
  retrospective's countable claims are read from the block rather than authored from memory

#### Scenario: Improvement suggestions are classified

- **WHEN** the `experience-analyzer` subagent proposes improvements
- **THEN** each suggestion is labeled as a system change (→ `docs/ideas.md` or a proposal) or a process change (→
  `AGENTS.md` or a subagent definition), so the main agent can route it

#### Scenario: The agent does not edit files itself

- **WHEN** the `experience-analyzer` subagent runs
- **THEN** it returns proposed text and suggestions without modifying files; the main agent verifies and applies them

#### Scenario: A significant failure is root-caused to its earliest prevention point

- **WHEN** the retrospective reports a significant failure or a recurring review-finding class
- **THEN** it names the underlying cause and the earliest development stage at which the problem could reasonably have
  been prevented or detected, drawn from the digest's review-round data, so the improvement targets that stage rather
  than the symptom
- **AND** when that cause rests on a process-defect record, it draws the cause from the record's body rather than the
  index's heading and names the record it read

#### Scenario: The previous retrospective's recommendations are evaluated

- **WHEN** the digest names the previous retrospective and its recommendations' outcomes can be observed
- **THEN** the retrospective states, for each, whether it was applied and whether the available evidence shows it
  helped, or that the window cannot yet show it — never presuming effectiveness from the absence of the failure

#### Scenario: Each suggestion carries a verifiable expected outcome

- **WHEN** the `experience-analyzer` subagent proposes an improvement
- **THEN** the suggestion states the failure pattern it targets, the observable outcome expected, and how that outcome
  would be verified, so a later window can judge whether it worked

#### Scenario: A class's count is stated in rounds

- **WHEN** the digest reports the classes raised for the window
- **THEN** each class counts once per round that raised it, whatever labels on that round normalized to it, and the
  figure is stated as a round occurrence rather than a share of the compliance-miss count
