# Design

## Context

See `proposal.md` — Why. Verified against the repository:

- **The template lives inside `docs/adr/README.md`** (an in-file fenced block), not a separate file: the `Status` line
  is `Proposed | Accepted | Superseded by ADR-NNNN`, with `Context`/`Decision`/`Consequences` sections.
- **Three deferral ADRs** state a reopen condition in prose in their `Decision`: ADR-0003 (adopt Jackson 3 once Axon and
  the OpenSearch client support it), ADR-0004 (reopen Spring Boot 4 "when there is capacity"), ADR-0011 (migrate to Axon
  Framework 5 once the Kafka/JGroups extensions and the Micrometer/OpenTelemetry modules publish 5.x, with a released
  Spring Boot starter). ADR-0011's "Related decisions" section explicitly classifies 0003 and 0011 as blocked on an
  **external** condition and 0004 on **capacity**.
- **Nothing surfaces a due trigger**: the `architecture-auditor` checks an ADR `Status` that is stale or a supersession
  not recorded, and reports unexplained choices as advisory — but not whether a deferred decision's condition has since
  been met. `Status` only changes after the fact (a replacement exists).
- **The disallowed-major mechanism already exists**: `config/dependency-updates/major-disabled.txt` suppresses a major
  and its comment points at a spec requirement or ADR for the rationale; `dependencyUpdates` reports _available_ updates
  but the suppression hides the very update a deferral awaits — so the awaited signal is exactly what the suppression
  hides.

## Goals / Non-Goals

**Goals:**

- Record each deferral's `Revisit when:` condition structurally, so it is readable and greppable rather than buried in
  the Decision prose.
- Surface a deferred decision whose recorded condition looks met, as an advisory item the owner triages — riding the
  existing on-demand + scheduled `architecture-auditor`.

**Non-Goals:**

- Automatically reopening or acting on a deferral — the audit reports; the owner decides.
- A new check, workflow, or Gradle task — the audit already runs on demand and on a schedule, so a due trigger reaches a
  human there without a new tool.
- Judging whether a deferral _should_ be reopened — only whether its stated condition appears met; the effort/priority
  remains the owner's call.

## Decisions

### D1: A `Revisit when:` line in the template, required for a deferral only

The template gains `Revisit when: <the condition, and the signal that shows it met>`, placed after `Status`. The
`docs/adr/README.md` prose says it is required when the decision is a deliberate deferral (not acted on now, revisited
later) and omitted for a settled decision — so its presence marks a deferral and its content is the trigger.

- **Alternative — fold the condition into `Status`:** `Status` is a fixed vocabulary
  (`Proposed | Accepted | Superseded by ADR-NNNN`); inventing `Accepted (deferred until …)` is the status-vocabulary
  drift a prior idea already flags. A separate field keeps `Status` clean.
- **Alternative — no template change, only record the three ADRs:** the point is that the _next_ deferral records its
  trigger by default; a template field is what makes that the path of least resistance.

### D2: The auditor reports a deferred ADR whose condition looks met, as advisory

The `architecture-auditor`'s intent sweep gains a check: for each ADR carrying `Revisit when:`, hold its condition
against the repository and report it when the condition appears met. The conditions are **not uniform** — the audit
handles each kind the ADR actually records:

- **An external coordinate gate** (ADR-0011: Axon Framework 4.x → 5.x): checkable by resolving the pinned coordinate and
  seeing a newer major published — the same query the update checks make.
- **A third-party adoption gate** (ADR-0003: adopt Jackson 3 once Axon and the OpenSearch client support it): the pinned
  `jackson2-bom` is a 2.x line that can never publish a 3.x, so the signal is **consumers' adoption** — whether the
  pinned Axon and OpenSearch artifacts resolve Jackson 3 — not a bump of the project's own pin. The auditor checks the
  consumers' dependency surface and reports the ADR when the gate appears satisfied.
- **An internal/capacity gate** (ADR-0004: reopen Spring Boot 4 "when there is capacity"): names no repository fact, so
  it is **not machine-decidable** — the auditor does not report ADR-0004 on the repository's own state, and the
  `Revisit when:` line records the trigger for the human, not for a check.

The item is **advisory** — no severity, not a defect — matching the intent-gap section, and names the ADR, the recorded
condition, and the signal that appears to meet it.

- **Alternative — a mechanical `deferredAdrs` report beside the update checks:** it covers only the coordinate-gate kind
  (ADR-0011) and would silently skip the third-party-adoption and capacity kinds; the mixed class fits the auditor,
  which already answers research questions across surfaces, better than a report that covers half its inputs.
- **Alternative — treat every `Revisit when:` as a coordinate bump:** wrong for ADR-0003 (its own pin is a 2.x line) and
  ADR-0004 (no coordinate); the sweep must read the condition the ADR records, not assume a shape.
- **Alternative — a new Gradle check that fails on a met condition:** a due trigger is not a build failure; the decision
  to act is the owner's, and gating a build on "Axon 5 shipped" would redden unrelated PRs.
- **Alternative — fold into `Status`-**stale detection:** a stale `Status` is a recorded-vs-recorded disagreement; a due
  deferral is recorded-intent vs external fact — the audit's existing classes do not cover it, so it is a new sweep.

### D3: The sweep rides the existing intent-gap advisory section

The new check is part of the "unexplained design intent" advisory sweep — both are advisory items about a decision's
record, both carry a location and a question/signal for the owner. It is added to the same requirement scenario set, not
a new requirement, keeping the corpus from accreting a near-duplicate.

## Risks / Trade-offs

- [The auditor asserts a condition is met from a stale or misread coordinate] → it reports advisory and names the
  signal, so the owner verifies; the sweep verifies the coordinate against the catalog before reporting, per the
  auditor's "treat every claim as a hypothesis" method.
- [A deferral's condition remains met and the item recurs each audit] → that is the intended reminder; the owner records
  the decision (a new ADR / a `superseded` status) once acted on, which stops the recurrence.
- [The template field accretes onto settled ADRs] → the README prose scopes it to deferrals; the auditor only acts on
  ADRs that carry it.

## Migration Plan

The template and the three ADRs are edited in this change; the widened sweep lands in the same change. The auditor
smoke-run is a precondition for archiving (the task records it). No runtime or data migration.

## Open Questions

None.
