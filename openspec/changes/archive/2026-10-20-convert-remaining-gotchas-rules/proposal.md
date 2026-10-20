## Why

`AGENTS.md` is 331,864 bytes loaded into every session. Its two piloted Gotchas subsections still hold seventeen rules
the evidence-record mechanism has not converted, and the parent unit carried only a reading estimate for them. This unit
drafts each one, measures it against the current file, and converts only the tranche whose saving pays for its record —
so the next reduction is chosen from verified figures, and the remainder is parked with them.

## What Changes

- **`AGENTS.md`** — eleven rules of `### Documentation & durable claims` are rewritten to their operational text
  (C7–C17), and each gains an `evidence: documentation-and-durable-claims` token. No lead in the eleven changes; C1, the
  only candidate whose title would move, is deferred.
- **`docs/guidance-evidence/documentation-and-durable-claims.md`** — eleven sections are added in rule order, each with
  a single-line slug heading, the rule's lead as its `Rule:` key, the episode's historical identifiers, and its
  provenance markers. The file grows from 5,747 to 16,608 bytes.
- **`docs/ideas.md`** — the parked entry is rewritten to the deferred Git tranche, with the measured figures this unit
  produced, so the remaining work starts from measurements rather than estimates.
- **Not in this unit:** the six Git candidates (measured and parked, −3,722 B with 5,812 B of record), R3's conversion
  (−69 B net), the four rules with no separable material, the one rejected on a retrieval failure, and any expansion of
  stage-specific workflow loading, evidence storage, pointer policy, audit-ledger tooling, deduplication, or typo fixes.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None. Every obligation this change exercises is already specified in `showcase/quality/agent-skills` — the record's
location and shape, the `Rule:` pairing key, the prohibition on a second rule in a section, the root entry's operational
elements, and the survival of a historical identifier — and the audit already owns and pairs the records. The change
applies that mechanism to eleven more rules, so `.openspec.yaml` sets `skip_specs: true`; `design.md`'s "Delta
determination" states the derivation rather than assuming it.

## Impact

- `AGENTS.md`: **−8,269 bytes (2.49%)**, measured on the file, with the eleven `evidence:` tokens (506 bytes) inside
  that figure; the projected file is **323,595 bytes over 3,355 lines**.
- Additional routinely loaded material: **506 bytes**, already counted in the root reduction; no new pointer, skill
  description, or `instructions` entry.
- Record: **+10,861 bytes**, so **total maintained content rises by 2,592 bytes** — the sections carry their address, a
  `Rule:` key, and the context that makes each episode readable.
- No code, build, dependency, API, or deployment impact. No gate validates the rewritten prose; the gates that read the
  file still run over it — the marker guard reads each `captured:` marker (all stay inside their rule blocks, and one
  moves from mid-sentence to the rule's end), the pin and install-command checks pattern-scan the file, and the
  formatter check reads the whole document.
- One upstream reference leaves the root text: `Fission-AI/OpenSpec#1892` moves with C16's episode but stays cited at
  `AGENTS.md:2994`, which owns its retirement condition, so `upstreamReferences` keeps scanning it and no scanner
  changes.
- `docs/review-rounds.tsv` gains this unit's review rows.
- Delivered whole unit: the conversion (−8,269 B) plus the implementation capture (+701 B, two clauses merged into
  existing rules) is **−7,568 bytes (2.28%)**, a projected **324,296 bytes / 3,361 lines**, with the record at 16,608
  bytes and total maintained content **+3,293 bytes**.
