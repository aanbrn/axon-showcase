# Design

## Context

See `proposal.md` — Why. The gap is a scratchpad with no owner, and the resolution is a widening rather than a new
auditor, which the auditor-justification rule decides:

- `architecture-auditor` already sweeps "deferrals and band-aids recorded in an ADR or parked in `docs/ideas.md`" and
  already holds a _record_ against the repository in the inverse direction (an ADR's `Revisit when:` gate, whose premise
  may have expired). A parked entry whose work has landed is that same inverse shape in the other artifact, so it is a
  sibling item in the sweep it already runs — not a distinct artifact and property.
- The other three auditors are not candidates: `agents-auditor` owns `AGENTS.md` and the project-authored `.opencode/`
  files, `specs-auditor` the spec corpus, and `readme-auditor` the README; none owns `docs/ideas.md`.

## Goals / Non-Goals

**Goals:**

- Have the audit report a parked entry whose work has already landed, as an advisory question the owner can confirm and
  retire, so the scratchpad stops ageing silently.
- Keep the check a _reading_ of the entry's own work, not a keyword match on the artifacts it cites.

**Non-Goals:**

- A mechanical check in `check`: an entry may cite a change, workflow or file as _context_ rather than as its own work,
  so an automated "does this artifact exist" test would report the entry's neighbours.
- A fifth auditor: the artifact is already swept by one that holds the same shape.
- Changing the report contract: the item is advisory, so the verdict line stays `<n> findings, <n> advisory`.

## Decisions

**The inverse shape, stated as the second member of the class beside the ADR gate.** The item's rationale _is_ recorded
(the parked entry), so the requirement's "search before reporting a recorded rationale" clause must not suppress it —
the same asymmetry the requirement already states for the ADR gate. The delta states the item as the second member of
that inverse class, so the requirement's own suppression sentence does not reach it.

**Hold the entry's own work, not its citations.** The definition and the requirement both say the check is the change,
commit, workflow or mechanism the entry _describes_ — so an entry citing a companion change is not reported as
implemented, which is the false positive a naive artifact-existence check would produce.

**Advisory, so a false positive is a question.** No severity, no automatic fix: the owner confirms and removes the
entry.

## Risks / Trade-offs

- **A false positive on a genuinely-open entry** → the item is advisory and its report states the evidence (the work it
  found), so the owner can dismiss it; the definition's existing "search the whole repository for a recorded rationale"
  step applies to the intent-gap class, not to this inverse.
- **The auditor reads more of `docs/ideas.md`** → it already sweeps the file for parked deferrals; the added item is one
  more pass over the same entries.
- **The edit is not exercised in-session** → the smoke-run needs the edited definition picked up (a reload or a new
  session), so it is a precondition for the archive rather than a task to defer across the merge.

## Migration Plan

None: a definition and spec edit, taking effect on the next audit run.

## Claims table

| Claim                                                                                      | Source                                                                                                                                                                                                                                                                                                                                                                                                                                  |
| ------------------------------------------------------------------------------------------ | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| An implemented idea's entry can stay parked, unnoticed                                     | `69d26b02` (#463) implemented the actionlint retry, while the 2026-09-21 entry describing it as parked survived until `retry-verify-infra-image-versions-2026-10-16` removed it (`git show 69d26b02:docs/ideas.md`)                                                                                                                                                                                                                     |
| No auditor owns `docs/ideas.md`                                                            | the four auditors' definitions (`.opencode/agent/*.md`) and the `agent-skills` requirement headers; `upstreamReferences` matches `owner/repo#NNN` references only                                                                                                                                                                                                                                                                       |
| `architecture-auditor` already sweeps the file and holds the inverse shape for an ADR gate | its definition's sweep list, and the requirement's five scenarios including "A deferred decision whose condition appears met is surfaced"                                                                                                                                                                                                                                                                                               |
| A mechanical artifact-existence check would misfire                                        | the "Spec the owner-notification path for a bot-authored pull request" entry cites `#586`/`#91` as context while the work it describes is a spec requirement — an entry's citations are not its own work                                                                                                                                                                                                                                |
| A definition edit is not exercised in-session                                              | `AGENTS.md`'s subagent-reload gotcha (a changed definition body is served from the pre-change copy until a reload or a new session), which is why the smoke-run precedes the archive                                                                                                                                                                                                                                                    |
| The report contract's verdict line is unchanged                                            | `.opencode/agent/architecture-auditor.md`'s report contract (`<n> findings, <n> advisory`), which an added advisory item does not change                                                                                                                                                                                                                                                                                                |
| The sites that state what the audit reports                                                | swept by grep over **both** token families — the report phrasing (`unrecorded intent`, `deferral whose recorded`, `due deferrals`, `design drift`) and the inverse phrasing (`is the inverse`, `deferred-condition`, `deferred-decision`, `for a deferred decision`) — since a sweep by the report phrasing alone misses the inverse-class statements (the inverse sentence in each file, whichever line the next rebase moves them to) |
