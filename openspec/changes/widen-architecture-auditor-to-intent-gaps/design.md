## Context

The `architecture-auditor` already reports two sections: **findings** (verified drift, each with a location and a
suggested correction) and **advisory** observations (design judgment, no reference to check against, never fixed without
the user's decision). See proposal.md — Why for the motivation.

Two existing conventions shape the approach:

- The repository already treats a missing rationale as a question for the owner rather than a gap to fill: a
  retrospectively recorded ADR that cannot state _why_ must ask the project owner, because the repo's silence is not
  evidence (AGENTS.md).
- A new auditor is justified by a distinct artifact or property; when the property is held by an artifact an existing
  auditor already owns, widen that auditor instead (AGENTS.md). This property belongs to the same artifact the
  `architecture-auditor` owns — the ADRs plus the architectural surface — so it is a widening, not a fourth auditor.

The sweep that motivates this change found the property is real and mechanically reachable: the `org.axonframework`
major-version suppression has no recorded rationale (its `major-disabled.properties` comment points at a spec that does
not mention it), fourteen `@SuppressWarnings("ClassCanBeRecord")` annotations encode a deliberate choice with no
recorded reason, the `opensearch-rest-high-level-client` exclusions in four modules have no recorded reason, and the
`axon-server-connector` exclusions outside the routing pair (including the two client test suites) have no recorded
reason either. The retained-deprecated-API surface has no instance in the repository today: it is included because a
deprecated API the project keeps is the same kind of deliberate choice, not because the sweep found one. The
already-explained surfaces are the positive control — the Flyway, JGroups, spring-data-opensearch and springdoc
suppressions (each a spec requirement), ADR-0004, the `axon-server-connector` exclusion on the gateway/command-service
routing pair (ADR-0009), and the `CodeBlock2Expr` suppression convention (AGENTS.md) all carry one.

## Goals / Non-Goals

**Goals:**

- The architecture audit reports the deliberate choices and absences whose rationale is not recorded, as advisory items.
- The surfaces the auditor sweeps are enumerated, so the check is repeatable rather than ad hoc.
- Each item is directly triageable: it names the choice, its location, and the question whose answer would record the
  missing rationale.
- The property stays advisory, so the existing "the user decides" rule applies without amendment.

**Non-Goals:**

- Answering the questions — only the project owner can supply a rationale.
- Writing or amending an ADR automatically: a rationale cannot be inferred from the repository's silence.
- Exhaustive detection: the sweep is a bounded heuristic over named surfaces, not a proof that every rationale exists.
- Re-checking behavior against the implementation or the spec corpus's internal structure (other owners).

## Decisions

### The new class is advisory, not a finding

"The rationale is missing" is not a verifiable defect: the repository's silence is not evidence that the rationale does
not exist, and a finding must carry a suggested correction, which cannot be written without the owner. The advisory
section already carries exactly the right contract — no severity, and nothing is done without the user's decision.

_Alternative considered_: a finding class ("an ADR whose Context states what but not why"). Rejected — it would demand a
correction the auditor cannot supply, and it contradicts the AGENTS.md rule that the owner must be asked.

### The delta adds a requirement rather than modifying the drift requirement

The existing requirement `### Requirement: The architecture is audited for drift from its recorded decisions` owns the
auditor's output contract, so extending its advisory clause in place was the natural alternative. Rejected because the
new class is a _new concern_ — for which the delta schema directs ADDED rather than MODIFIED — and because an in-place
edit would fold a distinct property (whether a rationale is recorded) into a requirement titled for drift, while
obliging the delta to carry all five existing scenarios verbatim. The resulting overlap with that requirement's existing
advisory clause "apparent gaps in the ADR set" is deliberate and acceptable: the old clause covers a decision with **no
ADR at all**, while the new class covers a decision or absence whose **rationale is not recorded**, and it sweeps
surfaces (build-file exclusions, suppressed coordinates, retained deprecations) that are not the ADR set.

### The surfaces are enumerated, not searched free-form

Each named surface is a place where the project deliberately _spent or avoided_ something, so a rejected alternative
exists by construction and a rationale must exist somewhere:

- dependency `exclude(...)` declarations in the build files;
- major-version-suppressed coordinates in `config/dependency-updates/major-disabled.properties`;
- suppression annotations that encode a design choice (`@SuppressWarnings`), and deprecated-API usages the project still
  carries;
- deferrals and band-aids recorded in an ADR or parked in `docs/ideas.md`.

_Alternative considered_: a general instruction to look for "unexplained decisions". Rejected — a free-form search
returns mechanical noise (`unchecked`, `unused` suppressions, generated sources) and is not repeatable.

### A recorded rationale suppresses the item

The audit must verify, by searching the whole repository, that no rationale is recorded before reporting an item — the
same discipline the existing findings path uses ("treat every claim as a hypothesis"). The already-explained surfaces
are the positive control: a spec requirement for the suppressed major, an ADR for a deferral, or an AGENTS.md convention
for a suppression must not be reported. This also keeps the output a list of _open_ questions.

### The report states the question, not just the observation

Because the audit cannot know which absences matter, each item must be actionable by a human: what was done, where, and
the question to answer (for example, "why is Axon pinned to 4.x?"). The output is therefore a candidate list for the
owner's triage, and the requirement and the agent text both say so.

### The candidate list is parked in a separate docs PR

The sweep's candidate list is a newly parked idea about the project's intent, not an artifact of this change, so it
ships as its own docs PR forked from `main` — the AGENTS.md rule for a newly parked idea. The competing "an edit the
change itself causes rides the change branch" rule covers edits about the change's own subject (removing the change's
idea, or fixing prose about the thing it shipped), not an independent open question the sweep happened to surface.
Keeping it separate also avoids the `docs/ideas.md` rebase conflict the change branch would otherwise risk.

### Widen the agent definition and its trigger's read-list, not the findings taxonomy

The `/audit-architecture` command already presents the two sections and already instructs the main agent that an
advisory item is the user's call, so its instructions change only for the new class's routing and for the scope
correction below: its step-1 read-list names only `docs/adr/` and the architectural surface, which now under-describes
what the auditor reads, so that list is extended to name the intent-clarification surfaces, and step 3 says an
intent-clarification item is a question for the user to answer. Its step 4 — and the identical one in `audit-agents.md`
— carried a vacuous `.opencode/*.md` glob (it matches no file, since the markdown lives in subdirectories), corrected to
name the `.opencode/` files the audit edited. The finding classes are untouched: this property is not drift and must not
dilute them.

## Risks / Trade-offs

- **[Noise on mechanical suppressions]** → the surfaces section names the deliberate deviation kinds; compiler/IDE
  mechanics (`unchecked`, `unused`, generated-source suppressions) are out of scope.
- **[A false gap when the rationale is recorded somewhere the audit did not read]** → the method requires a
  repository-wide search for the rationale before reporting, per the existing "verify, do not infer" discipline and the
  AGENTS.md lesson that a deliberately absent component's name must be grepped repo-wide.
- **[The advisory section blurs into findings]** → the class is advisory-only, stated in both the requirement and the
  agent definition, with a scenario that pins it.
- **[The sweep is unbounded in principle]** → the four surfaces are finite and enumerable, and the audit reports
  candidates rather than attempting completeness.

## Open Questions

- Whether a _broken_ rationale pointer (a comment pointing at a rationale that is not there, as the `org.axonframework`
  entry in `major-disabled.properties` does) should be reported as an intent-clarification advisory item or under a
  finding class. The existing finding classes do not cover it — "a cross-reference to an `ADR-NNNN` that does not exist"
  is about a missing ADR, not a pointer to an absent rationale — so a finding route would need a new finding kind, which
  this change deliberately does not add. Deferrable: it changes neither the specs nor the approach, and either route
  surfaces the same location.
