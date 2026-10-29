# Proposal

## Why

A parked idea's lifecycle is not an architecture question, and a scratchpad is not a register of commitments. Yet the
audit reads all 99 entries of `docs/ideas.md` as a lifecycle to track on every run — a landed entry as an item to
retire, a met condition as a recurrence that is due — and so makes an architecture auditor the lifecycle owner of notes
about social previews and support surfaces. An unimplemented note is not an overdue obligation, and a condition coming
due is a decision to take, not a defect to remove. The owner proposed a cleanup command earlier and it was turned down
because the auditor already read the file (the owner's account; no repository record states it); a shared input is not a
shared purpose. This change retires both sweeps and gives the scratchpad an owner-run cleanup.

## What Changes

- **`.opencode/commands/ideas.md`** — `/ideas` gains a `cleanup` mode: owner-triggered, one bounded batch oldest-first
  with a cursor in the file's header, each entry disposed `keep` / `narrow` / `promote` / `drop`, with removals
  presented for approval, a "this shipped" reason verified before it is used, the durable destination found first, and
  no renewal dates.
- **`.opencode/agent/architecture-auditor.md`** — both parked-entry sweeps are removed (work landed; own condition met),
  while the ADR `Revisit when:` gate, the rationale search over `docs/ideas.md`, and the condition-classification rules
  stay. Its frontmatter, advisory lead, inverse-class bullet, and scope line are narrowed to match.
- **`AGENTS.md`** — the architecture-auditor bullet loses the parked-entry members; the `docs/ideas.md` reach sentence
  returns to the deferral-rationale reading; the generalization's capture rule states when it checks a parked entry's
  condition without claiming a periodic sweep.
- **`README.md`** — the architecture-auditor paragraph and its two table rows name what the audit actually reports.
- **`docs/ideas.md`** — the header states the pruning discipline: nothing in the file is a commitment, and an entry
  nobody wants is dropped at a cleanup.
- **`showcase/quality/agent-skills`** — `REMOVED` + `ADDED` on the "Unexplained design intent" requirement, retitled to
  the architectural subject and narrowed to the ADR condition; the obligation-level trace is in `design.md`.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/agent-skills`: the intent-clarification requirement covers architectural rationale and a deferred
  ADR's recorded condition; the parked-idea lifecycle leaves the audit and is pruned by the owner through `/ideas`.

## Impact

- The audit stops reporting parked entries; the owner prunes the scratchpad in bounded batches when they choose to.
- **Build / tests / services**: no code, build, dependency, or deployment change — a command procedure, a definition,
  and prose.
- The verification is a **seeded smoke-run** of the narrowed auditor — a parked entry whose work landed must no longer
  be reported, and the retained ADR gate must still be — plus a read-only run of the cleanup procedure over the oldest
  batch, and the usual gates.
