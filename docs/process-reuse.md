# Process transfer preparation guide

This guide locates the evidence and owning artifacts needed to plan a bounded transfer of the process into another
project, and to record its adaptation burden. It has not established transfer speed, complete executability,
cross-project applicability, or that remaining changes will be minor.

It is a retrieval aid, not an acceptance test: nothing here decides whether a transfer succeeded.

## What the repository already holds

| Evidence                      | What it establishes                                                                        | Where                                                                                                                                                                                       |
| ----------------------------- | ------------------------------------------------------------------------------------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| The pause/resume transfer     | one slice, one constructed target, six mapping groups, the adaptation burden it recorded   | `docs/decisions/2026-10-31-process-reuse-pilot.md`                                                                                                                                          |
| The lesson-capture transfer   | the same for a second slice, with its own ceiling                                          | `docs/decisions/2026-10-31-lesson-capture-reuse.md`                                                                                                                                         |
| The bindings decision         | why the process declares bindings instead of substituting them                             | `docs/adr/0026-process-bindings-are-declared.md`                                                                                                                                            |
| The admission criterion       | when a consolidation may be undertaken at all                                              | `docs/adr/0027-consolidation-is-evidence-bound.md`                                                                                                                                          |
| The declaration               | the roles an adopting component resolves, this repository's values, and the consumer trace | `docs/contracts/process-bindings.md`                                                                                                                                                        |
| The adoption changes          | how a declaration was adopted in slices and then in commands                               | `openspec/changes/archive/2026-11-02-process-bindings-contract/`, `openspec/changes/archive/2026-11-02-adopt-process-bindings/`, `openspec/changes/archive/2026-11-02-adopt-heading-roles/` |
| The scope of this preparation | what it authorizes and what it does not                                                    | `docs/decisions/2026-11-02-process-reuse-readiness.md`                                                                                                                                      |

## What does not travel

The pilots' specifications, protocols, and fixtures were frozen outside the repository. The pause/resume record names
their temporary paths and their SHA-256 digests; the lesson-capture record names the filenames and digests only. That
material is not retrievable from the repository, and this guide does not pretend otherwise: a new transfer re-derives
its own specification and protocol from the pilot records' descriptions and freezes them before the transfer starts,
outside the target's working tree, as the pilots did.

The digests remain useful for one thing — proving that a copy of a protocol you still hold is the one the record
describes.

## What a transfer selects

A transfer selects **one component**, and that component's own dependencies decide what the target must supply. The
declaration names the roles and their owners; its consumer trace shows which components resolve which role. Read both
rather than restating them here, and let the selected component's slice state what it needs.

A target may decline a component whose dependency it cannot supply — ADR-0025 records that a missing contract is an
explicit incompatibility, not a reason to invent the artifact or to delete the obligation.

## Techniques the two counterfactuals used

Each is a few invocations against a constructed copy, and both are narrower than they look:

- **A renaming counterfactual** renames the declared value in a constructed copy of the target, updates only the
  declaration and the target's own artifacts, and runs the component or components that resolve the renamed role — the
  first one ran a resume and a lesson capture, the second a single command. It establishes that the component resolves
  through the declaration instead of a literal. It does not establish that the component's obligations survive.
- **A missing-binding control** removes one required declaration row, runs the same component, and requires an explicit
  stop that names the missing dependency. It establishes one component's refusal to invent a value for one role. An
  earlier version of this control ran before the adopting components carried a stop instruction, and the run inferred a
  plausible value instead — which is what that instruction now prevents.

Run each arm in a fresh repository, record the tool sequence rather than the agent's summary, and keep the arms isolated
from each other; a control that can read the positive fixture's declaration is weaker evidence, even when it declines.

## What a transfer records

Following the pilots' accounting, record for the selected component: the adaptations it required, the incompatibilities
it hit, the unplanned rescues it needed, the obligations the transfer left unexercised, and the target's own state at
the start. A first transfer records what the component _needed_; adaptation counts that dropped are a comparison, and a
comparison needs a second run.

## What this guide does not do

- It does not offer a target package to install: which files a target needs is the selected component's dependency list,
  not a bundle.
- It does not gate a transfer: a pass through these steps is not evidence that a transfer succeeded.
- It does not promise speed. No transfer latency has been measured.
