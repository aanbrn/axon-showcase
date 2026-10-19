# Design — a capture addition states its decision difference

## Context

**The premise, verified rather than assumed.** The promotion gate tests a proposed rule in isolation: true, actionable,
not automatable, material, general enough, high-confidence. Every criterion asks whether the rule is _good_; none asks
whether the file already determines the same action, so the gate cannot see an addition's increment.

**The workflow already carries one instance of the test, with a different remedy.** `AGENTS.md`'s capture workflow has a
Decision point: "IF the addition restates a remedy the target bullet already carries THEN read the duplication as the
target rule's wording being the gap rather than a missing mode … the owner's 'would following the existing discipline
already have prevented this?' is the test, verified against the text before a bullet is added." So a restatement test
exists, and it is the one this change generalizes: its scope is one shape of restatement (a restated _remedy_), its
remedy is to reword the target rule, and it never refuses an addition. The gate test makes the check apply to every
addition, names the **already covered** disposition for the whole-candidate case and **split** for the partial one, and
leaves the workflow's branch as the site that applies it. Task 1.4 reconciles the two rather than leaving a duplicate.

**A second artifact owns the test for its own candidates.** `agents-auditor`'s candidate contract carries a
`Future decision` field ("the decision or action this guidance changes — the candidate's practical impact"), and its
merge class already resolves the restatement case: where merged text "would only restate a rule the file already
carries", it reports a `remove` instead. Its removal class asks "whether git preserves it". So the notion, the
disposition, and the evidence's fallback all exist — in the auditor's contract, which runs weekly and **after** the
rules land. The subagent that writes them has no equivalent. That is the gap this change closes, and it is why the
change ports a test rather than inventing one.

**The hypothesis's strong form is not confirmed by the record.** Five recent captures that extended existing entries
were inspected (`git show <commit> -- AGENTS.md`): `4a7b2687`, `c7c7f28f`, `dcac66a7`, `ce6e1269`, `8e410308`. Every one
changed the guidance — none added _only_ an incident — and the 2026-10-19 agent-tooling audit reported "no new
merge/removal/route candidates". So the verified cause is narrower than "captures restate rules": the gate has no
increment test, and each capture's rule arrives wrapped in incident narrative and explanation that the gate never
bounds. The test closes the first and bounds the second; it does not, on this evidence, remove restatements from the
recent record.

**Where the evidence stays.** `AGENTS.md` already states that on a merged bullet the marker records only the latest
contribution and "the pre-existing text stays recoverable from `git blame` / `git log -S`". The change dir holds the
unit's design, review findings, and validation for its reader, and the audit reports hold the periodic reconciliation —
neither is a rule's evidence home, which is the marker and git — so no new journal is needed, which is why this change
adds none.

## Decisions

- **D1 — the test lives in the promotion gate, as a `MODIFIED` block, not a new requirement.** It refines the capture
  requirement the spec already holds; a new requirement would cost a permanent corpus entry for a clause.
- **D2 — `already covered` and `split` are the dispositions, both already carried.** `already covered` is the report
  contract's term for a verified candidate; `split` is in the ported candidate contract's `Safe disposition` list
  (`agents-auditor.md`). The change extends both to a proposed addition rather than coining terms.
- **D6 — the workflow's existing branch is generalized, not duplicated.** Task 1.4 points the branch at the gate test,
  so one test governs additions and no second overlapping branch remains.
- **D3 — the evidence's home is named, not merely excluded.** The test must say where an incident's evidence remains
  available, or it reads as "discard it"; the marker and git history are the established homes.
- **D4 — the replacement preference preserves normative strength.** An exact replacement of the existing rule is
  preferred over appended qualifications, but the established scope, exceptions, and any identifier the rule needs to be
  applied are preserved — brevity never weakens guidance.
- **D5 — single-sourced.** The test is stated in the capture requirement and in the subagent definition that applies it;
  the `AGENTS.md` capture workflow points at the gate rather than restating the test.

## Validation

| Case                                                 | Verified instance                                                                                                                                                                                                                                                                                                                                                                                                              | Disposition                                                                                                                                                                      |
| ---------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| An extension that repeats an existing obligation     | `refresh-upstream-references-2026-10-19` (`5b5aeea1`) — the commit changed three things in one sentence: the noun widened from "document the **symptom** it is cited for" to "document the **claim** it is cited for" (a restatement of the obligation the bullet's `hold-back-nginx-buildpack` incident already carried), while "**sit on the clause it supports**" and "read the **surface that carries the claim**" are new | **split** of that pair — keep the two new halves, drop the restatement                                                                                                           |
| A necessary qualification that changes the decision  | `remove-unrecorded-redgate-repository` — under the existing guidance a resolution-affecting change's verification was the Docker-free check; the addition requires compiling `integrationTest`/`e2eTest` explicitly, and names the tiers a resolution claim covers                                                                                                                                                             | **apply** — the before/after differ, and the IT/e2e compile probe is the repository-backed evidence                                                                              |
| A distinct rule the existing guidance does not cover | `capture-merge-time-verification-tick` — where a merge-time verification's tick goes was unspecified                                                                                                                                                                                                                                                                                                                           | **apply**                                                                                                                                                                        |
| **The challenge**                                    | `optimize-agents-auditor-for-instruction-budget` — four markdown/Prettier rules added to one bullet (escape `\$`, escape `\|`, proofread inline code, never split a span across a line break). Each passes the test; the test would drop the _evidence_ that makes each retirable when the formatter is fixed                                                                                                                  | **apply, with the evidence routed** — which is why D3 requires the test to name the evidence's home rather than only excluding it                                                |
| **The premise challenged**                           | the strong hypothesis — a capture that adds only an incident                                                                                                                                                                                                                                                                                                                                                                   | **not found** in the five inspected captures; the 2026-10-19 audit found no merge candidates either. The test is preventive, and its record here is one split plus three applies |

## Risks and unresolved tradeoffs

- **A partial restatement's disposition is `split`.** Case (a) is half-restatement, half-new; the ported candidate
  contract already carries `split` in its `Safe disposition` list, and the design's own validation applies it. Adopted
  rather than left open.
- **The evidence's durability is behind git.** An agent reading `AGENTS.md` cannot see why a rule exists without
  `git log -S`, which raises the cost of judging whether a rule still applies — the intended trade (the file carries
  rules, not history), but it is a real cost and the reason the marker rule exists. The archived change dir is **not** a
  home: `AGENTS.md` records that it is invisible to gates and that no tool reads it.
- **The test adds a per-proposal obligation.** Stating a before/after pair for every proposal costs report length,
  bounded by the shared per-item budget; a proposal whose difference is trivial still spends that budget to say so.
- **A rule whose evidence is load-bearing is a boundary case.** Where a threshold, a version, or a gate condition is the
  rule's substance, dropping the evidence would break it — D4's preservation clause is what holds, and the validation's
  case (b) is the instance.

## Claims table

| Claim                                                                                                                | Source                                                                                                                                                                                                                                   |
| -------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `AGENTS.md`'s capture workflow already carries one instance of the test, with a different remedy                     | `AGENTS.md`'s capture Decision point, quoted in this Context                                                                                                                                                                             |
| The promotion gate carries six criteria and no increment test                                                        | `.opencode/agent/lesson-capture.md` (the promotion-gate bullet)                                                                                                                                                                          |
| The repository already defines the test, the disposition, and the evidence's fallback — for the auditor's candidates | `.opencode/agent/agents-auditor.md` — the candidate table's `Future decision` field, the merge class's "would only restate a rule the file already carries … reported as a `remove`", and the removal class's "whether git preserves it" |
| The spec's capture requirement states the gate but no increment test                                                 | `openspec/specs/showcase/quality/agent-skills/spec.md`, the "Lessons are captured after implementation" scenario                                                                                                                         |
| `already covered` exists only in the report contract's collapse line                                                 | the same spec's "Verified-and-set-aside candidates are summarized" scenario                                                                                                                                                              |
| The 2026-10-19 agent-tooling audit found no merge/removal/route candidates                                           | `docs/audits/2026-10-19.md`                                                                                                                                                                                                              |
| Five recent captures each changed guidance and each carried incident narrative                                       | `git show <commit> -- AGENTS.md` for `4a7b2687`, `c7c7f28f`, `dcac66a7`, `ce6e1269`, `8e410308`                                                                                                                                          |
| The evidence's established homes are the `captured:` marker and git history                                          | the `AGENTS.md` capture bullet ("the pre-existing text stays recoverable from `git blame` / `git log -S`") and the auditor's removal class                                                                                               |

**Dismissals** (the scan's candidates resolved to neither a row nor a fact, one reason per kind): **path candidates**
name a file or a reference rather than asserting a fact; **absolute-quantifier candidates** are modal or structural
words inside a rule's sentence, not universal claims about a corpus; **cross-reference candidates** name a record's own
field or a comparison procedure rather than asserting a fact; **count candidates** are this change's own planning
artifact numbering, not a claim about the repository.
