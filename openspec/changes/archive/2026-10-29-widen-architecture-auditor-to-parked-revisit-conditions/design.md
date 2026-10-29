# Design

## Context

See `proposal.md` — Why. The gap is not new: the 2026-10-27 parked entry ("Read a parked idea's own `Revisit when:`
condition, not only whether its work had landed", removed by this change) records it, names the shape ("a sibling branch
— a parked entry whose own recorded condition appears met — classified by the same four-way rule as an ADR condition"),
the falsified sentence in `AGENTS.md`'s capture rule, and the `MODIFIED` delta. That entry is the design adopted here,
so this change derives no second mechanism.

Why a widening rather than a fifth auditor, under the auditor-justification rule:

- `architecture-auditor` already sweeps `docs/ideas.md` and already holds a recorded condition against the repository —
  an ADR's `Revisit when:` gate. The added class is the same shape in the other artifact, a sibling of the parked
  entry's landed-work member the 2026-10-16 widening added.
- A `docs/ideas.md` auditor would have to read the architecture and the parked entries to judge a condition naming an
  ADR, a consumed artifact, or a mechanism, so its comparison span would reproduce this one; and `/audit-architecture`
  plus the scheduled `audit` already give the artifact a recurring reader. The prior widening reached the same
  conclusion for the landed-work member.
- No other auditor's artifact covers `docs/ideas.md`: `agents-auditor` owns `AGENTS.md` and the project-authored
  `.opencode/` files, `specs-auditor` the spec corpus, and `readme-auditor` the README.

## Goals / Non-Goals

**Goals:**

- Have the audit report a parked entry whose own recorded reopening condition appears met, with the condition and the
  signal meeting it, as a recurrence that is due.
- Keep the check a reading of the condition, in whatever form the entry states it, rather than a keyword match on the
  `Revisit when:` label.
- Keep the four-way classification and the unverifiable-is-no-claim rule that the ADR branch already applies.

**Non-Goals:**

- A mechanical gate: a condition says a state must hold, not that a path must exist, so an artifact-existence check
  would report entries whose named prerequisite never concerned the artifact's presence (the 2026-10-16 design rejected
  the same shape for the landed-work member).
- Automatic removal or unparking: only the owner decides, and a met condition may mean the work should be done.
- A new auditor or command: the artifact already has a recurring reader.
- Changing the report contract: the item is advisory, so the verdict line stays `<n> findings, <n> advisory`.

## Decisions

**The condition class is the third member of the same item, not a new item.** The requirement already describes one
inverse class — a record whose premise may have expired, whose rationale is therefore recorded and which the
recorded-rationale search must not suppress. The parked-entry condition joins it beside the ADR gate and the landed-work
member, so the suppression, the advisory status, and the per-item question carry over without restatement.

**A met condition is reported as a recurrence that is due, never as a removal instruction.** The entry's own text may
say "add this when X", in which case a met condition means the work is now justified; the item states the condition and
the signal, and the owner decides whether to reopen, do the work, narrow the entry, or remove it. The definition and the
requirement both state the prohibition on reading it as "remove".

**Read the condition in whatever form the entry states it.** Only a few entries carry a bold `Revisit when:` label, and
a condition is often written in prose instead — the 2026-10-22 entry ending "Revisit when a third set is vendored, or
when one of the four drifts", and the same date's `pack 0.40.10` entry, "Revisit if that run does not install the tag".
A label-only rule would miss those, and a rule that classified only labelled conditions would make the label, not the
entry, the subject.

**Classification, qualifiers, and compound conditions carry over unchanged.** The four-way classification
(repository-checkable, externally checkable, human judgment, not currently verifiable), the "a newer published version
alone does not satisfy a condition requiring compatibility, adoption, or support" rule, and the "an inability to verify
is not a claim" rule are the ADR branch's, applied to the entry's own condition; a conjunction is met only when every
clause is, and an entry whose clause remains open is not reported.

**The capture rule keeps its event-time reader and loses its false reason.** `AGENTS.md`'s generalization Decision point
justified the capture's ownership with "no other reader tests a parked entry's condition", which this change makes
false. The capture still owns the moment an incident meets a condition — the periodic audit reports only at its next run
— so the sentence is corrected rather than deleted.

### Amended clauses

The delta is a `MODIFIED` block, so each amended clause of the current requirement is quoted beside its replacement:

| Clause in the current requirement                                                    | Replacement in the delta                                                                                                                                                                                                              | Why                                                                                  |
| ------------------------------------------------------------------------------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------ |
| "or a parked `docs/ideas.md` entry whose work has already landed."                   | "…whose work has already landed, or a parked entry whose own recorded reopening condition appears met."                                                                                                                               | the premise-met class gains its third member                                         |
| "It SHALL hold the condition against the repository where it names a checkable fact" | "It SHALL hold a recorded condition — an ADR's or a parked entry's — against the repository where it names a checkable fact"                                                                                                          | the subject is no longer only the ADR condition                                      |
| "Each `Revisit when:` condition SHALL be classified before it is reported"           | "Each recorded reopening condition — an ADR's `Revisit when:` line, or a parked entry's own condition, labelled or in prose — SHALL retain its own qualifiers and compound structure … and SHALL be classified before it is reported" | conditions appear unlabelled, and a compound gate's clauses decide whether it is met |
| "the deferral SHALL be reported only where the condition is demonstrably satisfied"  | "the deferral or parked entry SHALL be reported only where the condition is demonstrably satisfied"                                                                                                                                   | both branches report on the same test                                                |
| "since an inability to verify SHALL NOT become an expiry claim"                      | "since an inability to verify SHALL NOT become an expiry or a due-recurrence claim"                                                                                                                                                   | the added report is a claim of the same kind                                         |
| (no existing clause)                                                                 | "A parked entry's met condition SHALL be reported as a recurrence that is due, and SHALL NOT be reported as an instruction to remove the entry."                                                                                      | an addition: the report's shape, which the removal rule must not absorb              |

The block keeps all nine existing scenarios verbatim and in order, and adds the three condition scenarios.

## Risks / Trade-offs

- **A false positive on a genuinely open entry** → the item is advisory and carries the condition with the signal that
  appears to meet it, so the owner can dismiss it; the definition's authority rule already requires the signal to be
  established before reporting.
- **The auditor reads more of `docs/ideas.md`** → it already sweeps the file for parked entries; the added pass reads
  each entry's condition, and the entries without one cost no verification.
- **An entry's condition is met but the work is not wanted** → the report says the recurrence is due, not that the entry
  is stale; the owner decides, and a narrowing is a legitimate disposition.
- **The edit's registered body must be confirmed before the run** → the reload gotcha's conditions apply: with the file
  watcher on and the auditor invoked as a project subagent in this session (its probed surface; `opencode run` is
  unverified), V2 serves the edited definition mid-session. A `revisit due` item for the prose-stated seed cannot come
  from the pre-change definition, so the positive control itself confirms the body; if it yields nothing, the body may
  be stale and a restart is the recovery. The run is a precondition for the archive rather than a task deferred across
  the merge.

## Migration Plan

None: a definition and spec edit, taking effect on the next audit run.

## Claims table

| Claim                                                               | Source                                                                                                                                                                                                                                                                                                                                 |
| ------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| The gap and its remedy are already recorded as a parked entry       | `git show 439c1f58:docs/ideas.md` — the 2026-10-27 entry opening "Read a parked idea's own `Revisit when:` condition", which this change removes                                                                                                                                                                                       |
| The auditor classifies a condition only for an ADR                  | `git show 439c1f58:.opencode/agent/architecture-auditor.md` — the "Classify a `Revisit when:` condition before reporting it" block and its sweep list, whose condition bullet is the deferred-ADR one; the `agent-skills` requirement's condition scenarios are all ADR-scoped, while its parked-entry scenarios test landed work only |
| Parked entries state conditions in other forms than the label       | the 2026-10-22 entry ending "Revisit when a third set is vendored, or when one of the four drifts", and the same date's `pack 0.40.10` entry ("Revisit if that run does not install the tag")                                                                                                                                          |
| A mechanical artifact-existence check cannot decide a condition     | the conditions the swept entries state name a state — a version shipped, a set vendored, a run's outcome — not a path, so the guard would report an entry whose named prerequisite never concerned that path; the 2026-10-16 design's Non-Goal rejects the artifact-existence check for the neighbouring landed-work member            |
| The trigger already exists and the scheduled run is report-only     | the `/audit-architecture` command's step 1 delegating the swept surfaces to the definition; the `audit` workflow's prompt naming the architecture-auditor and forbidding any finding to be applied                                                                                                                                     |
| `AGENTS.md`'s capture rule states the absence this change falsifies | `git show 439c1f58:AGENTS.md` — the generalization Decision point's closing reason, "since no other reader tests a parked entry's condition"                                                                                                                                                                                           |
| Removal is owner-gated and carries a durable destination            | the `docs/ideas.md` header's removal rule                                                                                                                                                                                                                                                                                              |
| The report contract's verdict line is unchanged                     | the definition's report contract, `<n> findings, <n> advisory`                                                                                                                                                                                                                                                                         |
| The sites that describe the audit's premise-met class               | the definition's advisory lead, sweep list, and classification; the `AGENTS.md` architecture-auditor bullet; `README.md`'s prose paragraph; and the capability `## Purpose` — swept by the class's own tokens (`premise`, `landed`, `due`, `revisit`), not by a file list                                                              |

Dismissals, one reason per kind:

- **`count`** — every candidate is a task identifier (`1.2`, `4.5`) in this change's `tasks.md`, not a claim about a
  corpus.
- **`absolute`** — the quantifiers are the rule's own normative text ("never as an instruction to remove the entry",
  "only where the condition is demonstrably satisfied") or ordinary English, not a count or universal over a corpus.
- **`cross-ref`** — the candidate names the file the text points at (`docs/ideas.md`, `docs/adr/`, the `agent-skills`
  spec), which the definition and the spec already own; the sweep above names them.
- **`path`** — the candidate names a path the text cites as evidence or as a swept surface, and the claims table's rows
  and the sweep task resolve it.
