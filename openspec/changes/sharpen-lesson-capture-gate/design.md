# Design — the capture's gate compares the decision, not the wording

## Context

**The observed behaviour, from the unit's own record.** `add-extraction-procedure`'s capture task records two candidates
the subagent proposed and the calling agent reassessed against the promotion gate. The first added a claim-sourcing
clause whose action the existing source-derivation guidance already required; the second inferred a shared drafting
cause from a repeated review-class label and prescribed a whole-section rewrite. The calling agent removed the first and
narrowed the second into the cause-conditioned failure-handling bullet `AGENTS.md` now carries. That record concludes
"no authoring-system gap was found", so this change tests that conclusion rather than assuming it.

**The gate's existing wording.** `.opencode/agent/lesson-capture.md` requires a decision difference — the action under
existing guidance against the action the proposed text requires — but its required artifact is the before/after pair,
and it never requires identifying the existing obligation and its condition, so a differently worded statement of the
same action can satisfy it. The gate's **True** criterion accepts "repeated observed evidence" without saying whether
that evidence supports the observation or a causal explanation of it. The authoring paragraph states the
observation/hypothesis/confirmed-cause distinction for an entry's text, but the gate's own True criterion never asks
what the repetition supports, so an inferred cause can still be proposed as true.

**The authority for the sharper reading already exists.** `AGENTS.md`'s non-converging-loop failure handling states the
class-level case with its condition: "A class the loop reports round after round whose instances share a confirmed
cause" — and "Do not infer the shared cause from the class label". The capture gate does not state the same condition,
so the rule that would have refused the second candidate lives in a section the capture does not apply.

**Single-source check.** The decision-difference test, the `already covered` and `split` dispositions, the replacement
preference, the evidence threshold, mechanism routing, and the no-edit rule already live in the "Per-change quality-gate
and analysis subagents are available" requirement's "Lessons are captured after implementation" scenario and in the
definition; this change refines them rather than adding a second checklist. `AGENTS.md`'s capture Decision points
already point at the gate for the test and its dispositions, so no gate copy is added there; only its capture workflow's
step 2 gains the inputs the subagent now requires.

## Decisions

- **D1 — refine the existing gate, not add a parallel checklist.** The additions are statements inside the gate's own
  two rules: the decision comparison in the increment rule, the cause rules after the criteria, and the
  preservation/bound in the replacement clause.
- **D2 — the spec delta is a `MODIFIED` block of an existing requirement.** The capture gate lives in the "Per-change
  quality-gate and analysis subagents are available" requirement's "Lessons are captured after implementation" scenario.
  The changed behaviour — what the gate compares, when a class-derived remedy is admissible, what a replacement
  preserves, and who supplies the evidence — is spec'd there; a new requirement would cost a permanent corpus entry for
  clauses.
- **D3 — one caller-side clause in `AGENTS.md`, no gate copy.** Its capture Decision points already route the test and
  dispositions to the definition and spec, and its failure-handling bullet already owns the section-rewrite preservation
  set; the only `AGENTS.md` edit is the capture workflow's step 2, which now passes the round rows and the existing
  guidance the gate's input rule requires.
- **D4 — the cause rule is conditional, not a ban on class-level lessons.** A preventive rule drawn from one severe
  verified incident still passes (the existing threshold), and a class whose instances share a confirmed cause still
  takes a remedy derived from it. Only a remedy resting on recurrence without the cause confirmed is refused.
- **D5 — the evidence home is the unit's record, with provenance unchanged.** Incident evidence the capture consults
  stays in the change record; the durable `captured:` marker and git history remain the rule's provenance, so no new
  journal is introduced.
- **D6 — an all-rejected capture is a completed outcome.** The gate's `already covered` branch says "propose nothing",
  and the verdict line already carries `nothing durable (net 0)`; this change states the outcome without adding a new
  disposition.

## Validation

| Case                                                                 | Evidence                                                                                                                                                                                                                                               | Result                                                                                                |
| -------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ | ----------------------------------------------------------------------------------------------------- |
| The pointer-source addition from `add-extraction-procedure`          | The unit's `tasks.md` task 4.1 records the clause as already covered by the source-derivation rule; the pointer-target bullet governs what a pointer written into `AGENTS.md` may claim, the opposite direction, so neither rule's action changed      | **`already covered`** — no proposal                                                                   |
| The recurring-class addition                                         | The same record says the candidate inferred a drafting cause from the class label; the ledger's rows for the unit repeat `claim-unreproducible` in rounds 1, 2, 7, and 8; `AGENTS.md` already conditions a section rewrite on a confirmed shared cause | **cause withheld** — report the recurrence, propose the cause-specific shape only; no section rewrite |
| A distinct preventive action with sufficient evidence (hypothetical) | A severe verified incident with a clear preventive action, naming an obligation and condition the existing guidance does not determine                                                                                                                 | **passes** — the threshold is unchanged and no confirmed cause is required for a preventive guard     |
| A lesson caused by a template or command                             | The definition's existing mechanism-routing paragraph; the requirement's "A lesson whose cause is an authoring mechanism is routed to it" scenario                                                                                                     | **routed** — an authoring-system recommendation naming the mechanism, gap, and proposed correction    |
| A capture with no durable additions (hypothetical)                   | The gate's `already covered` branch ("propose nothing") and the verdict line's `nothing durable (net 0)`                                                                                                                                               | **completed** — no rule is added                                                                      |

## Risks and unresolved tradeoffs

- **The cause condition can be over-read as a ban on class-level lessons.** D4's wording and the retained existing
  threshold are what bound it; the definition says a class with a confirmed shared cause still takes a remedy derived
  from it.
- **The supplied-evidence rule adds an input the caller must provide.** The change updates the capture workflow's step 2
  to pass the round rows and the existing guidance, so the subagent's input list and the caller's procedure agree; when
  they are still absent the gate withholds, trading a proposal for an explicit limitation.
- **The definition's smoke-run is a pre-archive task, and it completed.** The registered body was confirmed to carry the
  new tokens (`opencode debug agents`) after a restart; the run exercised all three named controls (the pointer-source
  addition resolved to `already covered`, the recurring-class addition withheld the cause, and the known-explained
  class-rewrite bullet was not re-proposed), with a first run recorded as exercising the withhold branch. The seeds are
  synthetic, so they establish the gate's behavior, not the facts of its output, whose premises were verified separately
  against `AGENTS.md`.

## Claims table

| Claim                                                                                                                                                                | Source                                                                                                                                 |
| -------------------------------------------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------- |
| The capture proposed a claim-sourcing clause already covered by source derivation                                                                                    | `openspec/changes/archive/2026-10-19-add-extraction-procedure/tasks.md`, task 4.1                                                      |
| The capture inferred a drafting cause from a review-class label and proposed a whole-section rewrite                                                                 | the same task                                                                                                                          |
| The gate at `HEAD` requires the two actions and a repository-backed example, but not the existing obligation's condition or the rule that phrasing is not novelty    | `.opencode/agent/lesson-capture.md`, the decision-difference paragraph at `HEAD` (`git show HEAD:.opencode/agent/lesson-capture.md`)   |
| `AGENTS.md` already conditions a section rewrite on a confirmed shared cause                                                                                         | `AGENTS.md`, "Failure handling — re-deriving a non-converging loop", the class-level bullet                                            |
| The capture gate lives in the "Lessons are captured after implementation" scenario of the "Per-change quality-gate and analysis subagents are available" requirement | `openspec/specs/showcase/quality/agent-skills/spec.md`                                                                                 |
| `AGENTS.md`'s capture Decision points already route the gate's test and dispositions to the definition and the spec                                                  | `AGENTS.md`, "Workflow: Capture lessons", Decision points                                                                              |
| The capture workflow's step 2 is the caller's input site                                                                                                             | `AGENTS.md`, "Workflow: Capture lessons", Procedure step 2                                                                             |
| The repeated class across the unit's rounds is `claim-unreproducible`                                                                                                | `docs/review-rounds.tsv`, the unit `add-extraction-procedure`'s rows: `claim-unreproducible` in rounds 1, 2, 7, and 8                  |
| The definition's mechanism-routing paragraph and the spec's routing scenario already send a template/command cause to the mechanism                                  | `.opencode/agent/lesson-capture.md`; `showcase/quality/agent-skills`, "A lesson whose cause is an authoring mechanism is routed to it" |
| The verdict line already carries `nothing durable (net 0)`                                                                                                           | `.opencode/agent/lesson-capture.md`, the report contract's verdict line                                                                |

**Dismissals** (the scan's candidates resolved to neither a row nor a fact, one reason per kind): **path candidates**
name a file or a reference rather than asserting a fact; **count candidates** are the change's own planning-artifact
numbering or a named round sequence already covered by the ledger row; **absolute-quantifier candidates** are modal or
structural words inside a rule's sentence, not universal claims about a corpus; **cross-reference candidates** name a
record's own field or a comparison procedure rather than asserting a fact; **verbatim-copied spec text** candidates are
the current requirement block the delta must reproduce whole, not assertions this change authors.
