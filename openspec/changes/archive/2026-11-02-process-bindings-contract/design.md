# Design — the process bindings declaration

## Context

ADR-0025 §4 deferred the path/heading abstraction; the two pilots measured six bindings in the pause/resume slice and
the review ledger in the lesson-capture slice. The first consolidation moved the governance contracts into
`docs/contracts/`, and the `agents-auditor` now covers that directory.

## Decisions

### D1 — A target-owned declaration, not substitution

`docs/contracts/process-bindings.md` holds the canonical names and the target's values. A component reads the
declaration before using a binding and reads the referenced owner before relying on its contract. The executing agent
performs the lookup; there is no text expansion and no loader feature. Substitution tokens, universal names, and runtime
discovery are set aside: a resolver is premature, a universal name makes the target adopt this repository's names, and
discovery cannot establish which candidate owns the contract.

### D2 — Six roles, not six strings

The declaration binds a role: the review ledger's location, not its format; the review gate's governance entry point,
not its procedure; the prior finding instances' authoritative retrieval source, not an invented filename; the
`Workspace:` record's location and declaration, not a replaceable label; the change artifacts' locator; the lifecycle's
owning file and heading.

### D3 — The read routes

An adopting component reads the declaration, then the referenced owner. The unit's consumer trace names a component that
does not adopt the bindings as a non-adopter.

### D4 — A missing binding stops the operation

A missing, ambiguous, or semantically incompatible binding stops the affected operation and reports the specific
dependency. The component does not invent a value, delete the obligation, or weaken its policy.

### D5 — The target maintains its own copy

The target's own values live in its `docs/contracts/process-bindings.md`; this repository's copy names its own. The
declaration does not duplicate the values in `AGENTS.md` or a config.

### D6 — What stays explicit

Entry paths, command names, the `Resume checkpoint` field set, the concrete values at their owners, script arguments,
generated OpenSpec paths, the enforcement mechanisms, and the lesson destinations and recurrence routing.

## The consumer trace

| Component                                     | Adopts?          | Bindings used                                                                             |
| --------------------------------------------- | ---------------- | ----------------------------------------------------------------------------------------- |
| `.opencode/commands/pause-unit.md`            | yes              | lifecycle, review-gate                                                                    |
| `.opencode/commands/resume-unit.md`           | yes              | lifecycle, review-gate                                                                    |
| `.opencode/skills/pause-resume-unit/SKILL.md` | yes              | lifecycle, review-gate, workspace-record, change-artifacts, prior-findings, review-ledger |
| `.opencode/agent/lesson-capture.md`           | yes              | review-ledger, change-artifacts                                                           |
| `.opencode/skills/review-gate/SKILL.md`       | no — non-adopter | names the ledger and the `AGENTS.md` sections directly                                    |
| `.opencode/skills/ship-change/SKILL.md`       | no — non-adopter | names its own workflow                                                                    |
| `.opencode/agent/review-quick.md`             | no — non-adopter | names the review ledger directly                                                          |
| `.opencode/agent/experience-analyzer.md`      | no — non-adopter | names the review ledger directly                                                          |
| `.opencode/commands/review-thorough.md`       | no — non-adopter | names the review ledger directly                                                          |
| the audits and the scripts                    | no — non-adopter | name their own surfaces                                                                   |

## The measurement

A renaming counterfactual against the constructed target: freeze the adopting definitions, rename the declared paths and
headings, update only the target declaration and the target artifacts, then exercise a resume that needs the ledger, the
writer record, the change artifacts, and the prior findings, and a lesson capture against evidence that requires the
renamed ledger. A separate control removes a required binding and requires an explicit stop. Record the actual reads,
not the agent's claim. A static sweep finds stale literals and undeclared canonical references.

## Alternatives

- **B — substitution tokens** — rejected: a resolver before the evidence shows a need.
- **C — universal names** — rejected: makes targets adopt this repository's names.
- **D — runtime discovery** — rejected: cannot establish which candidate owns the contract; discovery is for onboarding.

## Risks

- The declaration can drift from the owned artifacts; the `agents-auditor` covers `docs/contracts/`, and the consumer
  trace names the adopters.
- The first unit abstracts only the adopting slices, not every matching path; a non-adopter is explicit.

## The counterfactual

The target: a copy of the constructed `ledger-service-lc` fixture with the binding-aware slice definitions, the
declaration, and the review ledger renamed `docs/reviews.md` → `docs/review-log.md`; its `AGENTS.md` points at the
declaration rather than naming the ledger.

- **Positive:** a fresh resume read the declaration, then the prior finding report, the renamed ledger, and the change's
  `tasks.md`; it then stopped on the checkpoint mismatch (the fixture's checkpoint names the original tree), making no
  change. The reads occurred before the mismatch stop.
- **Missing-binding control:** with the declaration's review-ledger row removed, the same request read the declaration,
  the prior finding report, the renamed ledger, and the `tasks.md`, then reported "`docs/contracts/process-bindings.md`
  does not declare the review ledger" and stopped, asking which ledger applies.
- **Static sweep:** the adopting slices contain no `docs/review-rounds.tsv`; the declaration names the six roles.

### The counterfactual evidence

The positive run's tool sequence: `skill(pause-resume-unit)`, `read docs/contracts/process-bindings.md`,
`read openspec/changes/unit-a/tasks.md`, `read docs/reviews/unit-a-round-1.md`, `read docs/review-log.md`, then the
mismatch report.

The control's tool sequence: `skill(pause-resume-unit)`, `read docs/contracts/process-bindings.md`,
`read docs/reviews/unit-a-round-1.md`, `read docs/review-log.md`, `read openspec/changes/unit-a/tasks.md`, then the
missing-binding report.

The lesson-capture run's tool sequence: `read AGENTS.md`, `read docs/process-defects/unit-b.md`,
`read docs/contracts/process-bindings.md`, `read docs/review-log.md`, `read docs/contracts/lesson-capture-analysis.md`,
`read docs/reviews/unit-b-round-1.md`, then the report; it reported "Review ledger read: `docs/review-log.md`, declared
as the `review-ledger` binding".

The static sweep: `grep 'docs/review-rounds.tsv'` over the four adopting slices returns 0.

The counterfactual renames the review ledger's path; the lifecycle and review-gate headings are not exercised, so their
resolution remains unverified.

## Claims table

| Claim                                                  | Source                                          | Result                                                                           |
| ------------------------------------------------------ | ----------------------------------------------- | -------------------------------------------------------------------------------- |
| The declaration names six roles                        | `docs/contracts/process-bindings.md`            | 6 rows                                                                           |
| The adopting slices resolve the bindings               | the counterfactual evidence's sequences         | the resume and lesson-capture runs read the declaration, then the renamed ledger |
| A missing binding stops                                | the counterfactual evidence's control sequence  | the missing-binding report                                                       |
| No hardcoded ledger in the adopting slices             | `grep 'docs/review-rounds.tsv'` over the slices | 0                                                                                |
| The consumer trace names the adopters and non-adopters | the design's table                              | 4 adopters, 6 non-adopter rows                                                   |
| The spec delta adds the requirement                    | `openspec validate --all`                       | 27/0                                                                             |
