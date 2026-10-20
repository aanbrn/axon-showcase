# Proposal

## Why

Three `AGENTS.md` spans — the `Docs refresh on change` convention and the two Gotchas sweep rules, 10,670 bytes in all —
overlap on one activity: finding and updating the documents a change makes stale. An agent covering one change's doc
work must consult all three, and each restates part of the discovery step. Recent units moved incident evidence out of
these rules but never asked whether the remaining obligations duplicate each other.

## What Changes

- `AGENTS.md` — merge the two Gotchas sweep rules (`A doc-consistency sweep is scoped by the convention or the claim…`
  and `A "the only X" claim in AGENTS.md goes stale…`) into one rule whose rows are the ten change classes, in the same
  `### Documentation & durable claims` subsection. The merged text states exactly the obligations the two rules state:
  the scope-reconciliation step removed three generalizations an earlier draft had introduced (a same-change timing
  lifted to the lead, member removal, and config-key/workflow renames).
- `AGENTS.md` — condense four statements in the `Docs refresh on change` convention that duplicate a decision the same
  convention already states: the three un-gated copies' rationale, the four-auditor ownership enumeration, the delivery
  rule's specific instance, and the idea-removal route.
- `AGENTS.md` — retarget the one internal reference that names the second sweep rule by its old title.
- `docs/guidance-evidence/documentation-and-durable-claims.md` — merge the `consistency-sweep` and `uniqueness-claims`
  sections into one, whose `Rule:` paragraph repeats the merged root rule's leading text and whose body keeps every
  episode.
- No rule changes section or subsection: both sweep rules stay in `### Documentation & durable claims`, and the
  convention stays in `## Conventions`, so the six pointers naming the convention keep resolving (five in `AGENTS.md`,
  one in `.opencode/skills/ship-change/SKILL.md`).

## Capabilities

### New Capabilities

None — `skip_specs: true`.

### Modified Capabilities

None. The change applies `showcase/quality/agent-skills`' existing authoring standard (one authoritative definition per
subject), its existing merge criterion (a merged text preserves every anchor and piece of evidence and never blends two
distinct lessons — a merge of complementary entries, which the criterion admits), and its existing evidence-record
contract (one section per converted rule; a `Rule:` paragraph as the pairing key; only sections paired with a live root
rule). It changes no requirement's obligation, and the evidence record it edits is owned by the `agents-auditor`'s
pairing check.

## Impact

- `AGENTS.md` — consolidation: −776 bytes / −7 lines measured on the whole file (324,296 → 323,520 bytes; 3,361 → 3,354
  lines). The pilot spans go from 10,670 to 9,904 bytes.
- `AGENTS.md` — the implementation capture adds 418 bytes (one merge-verification clause in the `Avoid redundancy`
  convention); the whole unit is −358 bytes / +1 line (324,296 → 323,938 bytes; 3,361 → 3,362 lines). A second capture
  proposal was rejected on the promotion gate (one non-severe occurrence) and is recorded in `design.md` instead.
- `docs/guidance-evidence/documentation-and-durable-claims.md`: −148 bytes / −5 lines (16,608 → 16,460 bytes, 249 → 244
  lines). Total maintained content changes by −506 bytes.
- No code, API, dependency, build, test, or deployment impact: no gate reads either file's prose, and the pre-commit
  guard's `captured:`-marker and formatting checks apply unchanged.
- `docs/ideas.md`: no entry is added or removed; no parked idea covers this work.
- `docs/review-rounds.tsv`: gains this unit's review rows, which the review gate requires.
