# Design — the heading roles

## Context

ADR-0026 made the process bindings declared; two changes adopted the review ledger and moved the consumer trace into the
declaration. The lifecycle and review-gate roles were declared from the start but never adopted: thirteen commands cite
`AGENTS.md`'s headings literally — twenty-six occurrences across thirteen commands that a target must rewrite.

ADR-0025 records a standing consolidation pause the owner accepted on 2026-10-23, whose resumption condition is a
confirmed contradiction between owners, a retrieval failure in practice, or another material process defect. The owner
lifted it by explicit instruction on 2026-10-31 for one unit and the first consolidation, and again on 2026-11-02 for
this unit, when directing this sweep; ADR-0027 requires that resolution, and this record is it.

ADR-0027 owns the admission criterion this change is held to; the design records where each of its clauses is satisfied
rather than restating it — the trace's thirteen rows name the components, the diff is the adoption, §Evidence is the
recorded removal, the proposal's Impact list is the artifacts — and the pause lift below is the owner's instruction, not
the criterion's grant.

## Decisions

### D1 — The thirteen commands, and one deliberate exception

Thirteen commands adopt; `review-gate`'s `Source:` line does not. That line names the section whose procedure the skill
is, which is a component's own entry pointer, and the declaration's "what stays explicit" already carries entry
pointers.

### D2 — One sentence shape, so the block stays one block

Each command's "Repository lifecycle" block keeps its structure and its own emphases; only the owner reference changes,
to "the lifecycle the bindings declare (`docs/contracts/process-bindings.md`)" and, where the block names the gate, "the
review gate" the same declaration names.

### D3 — No new role and no new declaration section

The two roles, their values, and their owners already exist; this change adds adopters and removes literals. The trace
gains thirteen rows and loses one non-adopter class.

### D4 — The missing-binding stop belongs at each site

The counterfactual's control found the gap: with the declaration's lifecycle row removed, a run inferred a plausible
`AGENTS.md` heading, labelled it "an inference, not a binding", and proceeded. The declaration forbids that, but no
component carried the instruction. Each of the thirteen now ends its lifecycle sentence with "a declaration that names
none stops the unit rather than being inferred" — an imperative a reader must act on before the step that would touch
it, so it is stated where the step is.

The clause names the lifecycle, the arm the counterfactual exercised. `flag-process-defect.md` states the review-gate
arm as well, because its sentence covers both roles; the six other dual-role commands leave that arm to the
declaration's general missing-binding rule, since asserting a stop the counterfactual never exercised would be a claim
without evidence. That asymmetry is deliberate and recorded here rather than left to read as coverage.

### D5 — Evidence is a renaming counterfactual

A constructed target renames both headings and declares the renamed values; a command that resolves the lifecycle runs
against it, and a control removes the declaration's lifecycle row. The recorded evidence is each run's tool sequence,
not the agent's claim.

## The consumer trace

`docs/contracts/process-bindings.md` owns it: the twenty-one adopters (eight from the two earlier changes, thirteen from
this one) and the non-adopter classes with their reasons.

## Evidence

Targets: `$TMPDIR/opencode/heading-iso/positive` and `$TMPDIR/opencode/heading-iso/control`, each a fresh git repository
built by `$TMPDIR/opencode/heading-pilot/build.py` from this repository's adopted `ideas.md` command. Both rename the
headings (`## How work is delivered`, `### How review works`), so neither contains a `Workflow:` heading, and both
declare the review-gate role; only the positive one declares the lifecycle role.

Invocation, identical in both:
`opencode run --standalone --auto --model opencode-go/deepseek-v4.1-flash "/ideas Add a note about the fixture. Then state which file and heading you treated as the lifecycle and as the review gate, and where that came from."`

- **Positive**, tool sequence: `read AGENTS.md`, `read docs/contracts/process-bindings.md`, `edit docs/ideas.md`, then
  the report — lifecycle `AGENTS.md` / `## How work is delivered`, review gate `AGENTS.md` / `### How review works`,
  "where that came from: `docs/contracts/process-bindings.md`".
- **Control**, tool sequence: `read AGENTS.md`, `read docs/contracts/process-bindings.md`, `read` the command, then the
  report that it did not append the note because the command's Repository lifecycle stops the unit before its first
  write when the bindings declare no lifecycle, with the lifecycle listed as none and the `AGENTS.md` heading explicitly
  not taken as one; `git status` in that tree stayed empty.

Two limitations, recorded rather than smoothed over:

- The control could read the sibling positive fixture — it did (`cat ../positive/AGENTS.md`) — and still declined to
  infer a lifecycle. The earlier control, run in `$TMPDIR/opencode/heading-pilot/renamed-missing` before the stop clause
  existed, inferred the heading and labelled it "an inference, not a binding"; that is the defect the clause fixes.
- A single run per arm; the reading is that the declaration is read in both, not a measure of how often an agent would
  fall back to inference.

## Claims table

| Claim                                                  | Source                                                                                                                           | Result                                                        |
| ------------------------------------------------------ | -------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------- |
| The thirteen commands name the roles, not the headings | `git grep -c 'Workflow: Change lifecycle' .opencode/commands/` and `git grep -c 'Workflow: The review gate' .opencode/commands/` | 0 hits each, exit 1; other `Workflow:` headings are untouched |
| A declaration without a lifecycle stops the unit       | the control run                                                                                                                  | reported none, appended nothing, tree unchanged               |
| The declaration's trace names the adopters             | `docs/contracts/process-bindings.md`                                                                                             | 21 adopter rows                                               |
| A renamed lifecycle heading resolves                   | the positive run                                                                                                                 | both headings reported from the declaration                   |
| The stop clause is at every adopting site              | `git grep -c 'stops the unit' .opencode/commands/`                                                                               | 13 files                                                      |
| No requirement text changes                            | the `agent-skills` requirement's wording                                                                                         | it is generic and names no component                          |
