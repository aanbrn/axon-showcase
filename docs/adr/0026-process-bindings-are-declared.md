# ADR-0026: Process bindings are declared, not substituted

Date: 2026-11-02

Status: Accepted

Revisit when **any** of the following is met — each alone would change this decision:

- **A controlled transfer records a reproducible lookup failure despite complete, unambiguous declarations** — a
  resolver or substitution mechanism may then be owed (internal trigger: the next transfer's record).
- **A real target project's declarations establish cross-project generality** — the six roles may then be confirmed or
  revised (external trigger: an owner-named project's record).
- **A binding's role changes** — the review ledger, the lifecycle, the review gate, the workspace record, the change
  artifacts, or the prior finding instances gains or loses its owning declaration (internal trigger: a change to the
  owning artifact).

## Context

ADR-0025 §4 deferred the path and heading abstraction: "The path and heading bindings stay explicit; abstracting them is
later work, not this decision." The two reuse pilots measured what that later work must cover. The pause/resume slice
mapped six identifier/path bindings — the review ledger (`docs/review-rounds.tsv`), the lifecycle heading, the
review-gate heading, the `Workspace:` record, the review gate's handover rule, and its approval stop — and the
lesson-capture slice mapped the review ledger among its six mappings. The change-artifacts role comes from this
repository's pause/resume skill, which reads the change dir's artifacts. The pilot's handover-rule and approval-stop
bindings are folded into the prior-findings and review-gate roles' referenced owners, so the transition from the pilot's
six bindings to the declaration's six roles is explicit. The subject-pack table and the ADR citation were missing
contracts, not locations.

The bindings are not interchangeable strings. The prior finding instances have no fixed filename: they come from the
prior review round's report and survive context loss through the `Resume checkpoint`'s `Findings no record holds` field
or the review hand-over. The `Workspace:` record is a declaration carrying the path, branch, and writer, not a
replaceable label. A target's value is an owner as much as a location.

The alternatives were: substitution tokens (`{{review_ledger}}`), which need a resolver the Markdown prose does not
have; a universal naming convention, which makes a target adopt this repository's names instead of separating names from
obligations; and runtime discovery, which can find candidates but cannot establish which one owns the contract.

## Decision

The bindings are **declared**, not substituted. The declaration is a target-owned file,
`docs/contracts/process-bindings.md`, with a stable canonical name for each role and the target's own value: a
repository-relative file reference, an exact heading, a record locator, or a reference to the declaration that owns the
retrieval.

- **The six roles.** The review ledger, the lifecycle, the review gate, the workspace record, the change artifacts, and
  the prior finding instances. Each binds a role, not a string: the ledger's location, not its format; the review gate's
  governance entry point, not its procedure; the prior findings' authoritative retrieval source, not an invented
  filename.
- **The read routes.** An adopting component reads the declaration before using a binding and reads the referenced owner
  before relying on that owner's contract. The executing agent performs the lookup; there is no text expansion and no
  claimed loader feature.
- **A missing binding.** A component that cannot resolve a binding — missing, ambiguous, or semantically incompatible —
  stops the affected operation and reports the specific dependency. It does not invent a value, delete the obligation,
  or weaken its policy.
- **The non-adopter.** The unit's consumer trace names a component that does not adopt the bindings as a non-adopter, so
  the consumer set is explicit rather than inferred.
- **What stays explicit.** Component entry paths and command names, the `Resume checkpoint` field set, the concrete
  values at their authoritative owners, script arguments, generated OpenSpec paths, the enforcement mechanisms, and the
  lesson destinations and recurrence routing. The declaration binds locations and authorities, not those.

The target maintains its own copy of the declaration with its own values; this repository's copy names its own. A target
owner decides whether to supply an equivalent contract or decline a component whose dependency it cannot satisfy.

## Consequences

- The first change adopts the declaration in the pause/resume and lesson-capture slices, adds a capability requirement
  for the binding use and incompatibility reporting, and records a consumer trace; it does not replace every matching
  path in the repository.
- A second change adopts the review-ledger role in `review-gate`, `review-quick`, `experience-analyzer`, and the
  `review-thorough` command, and moves the consumer trace into the declaration, where the live adopter set belongs. A
  component that names a role's literal value instead of resolving it stays a non-adopter the trace names, with the
  reason that keeps it one.
- The missing contracts (the subject-pack table, the ADR citation) are identified as incompatibilities, not created.
- A renaming counterfactual against the constructed target tests the binding without a third full transfer: the declared
  paths and headings are renamed, the declaration and the target artifacts are updated, and a resume and a lesson
  capture are exercised.
- A template repository, a generator, broad extraction, and a substitution engine keep their own signals.

## Amendment — 2026-11-02

One statement above is replaced, and this amendment records it without reopening the rest of the Decision.

- The Decision's **six roles** — "the review ledger, the lifecycle, the review gate, the workspace record, the change
  artifacts, and the prior finding instances" — are now seven: `process-defect-records` joins them under ADR-0028, which
  supersedes that sentence. Everything else stands: declared not substituted, the read routes, the role-not-string rule,
  the missing-binding stop, the non-adopter trace, and the explicit exceptions, including the lesson destinations and
  recurrence routing.
- The `Revisit when:` clause reading "the six roles may then be confirmed or revised" therefore now applies to seven; a
  reader holding this file's conditions against the repository counts the declaration's rows, not the sentence above.
