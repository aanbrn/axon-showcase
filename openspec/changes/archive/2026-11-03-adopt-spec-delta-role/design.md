# Design — the spec-delta role

## Context

The dependency inventory (2026-11-02) recorded five `AGENTS.md` sections as unresolved after this unit's candidate was
taken: no transfer or counterfactual had exercised them. This unit takes the one with the most consumers — three audit
commands route a finding's spec change through "Workflow: Author a spec delta and archive".

The owner lifted ADR-0025's pause for this unit on 2026-11-02 by directing that an unresolved inventory row be taken.

## Decisions

### D1 — One role, under the role-not-string rule

`spec-delta` names the section's location, owned by that workflow. It satisfies ADR-0026's rule that the declaration
binds a location or an authority rather than a string.

### D2 — The stop belongs at each adopting site

The counterfactual's first control found the gap: with the row removed, the run inferred the procedure from the
fixture's only heading and labelled the route "undefined" while still naming one. The three commands now end the
sentence with "a declaration that names none stops the unit rather than being inferred" — the same wording the thirteen
lifecycle adopters carry, so one sweep finds every site.

### D3 — ADR-0028 gains an amendment, not a new ADR

The decision is the same — a role list extended by a location with an owner — so a dated amendment records it,
supersedes the ADR's stale role count, and leaves the count to the declaration's table.

### D4 — No delta

The `agent-skills` requirement delegates the role set to the declaration, so this unit changes no requirement text.

## The consumer trace

Three `spec-delta` rows; the non-adopter classes unchanged.

## Evidence

Both arms are fresh git repositories built by one script, `$TMPDIR/opencode/specdelta-pilot/build.py`, whose inputs are
recorded here so the experiment reconstructs without it:

- the fixture's `AGENTS.md` holds a single section, `## How a spec change is delivered`, with the rule "A spec edit is
  itself a change: it carries a delta spec, and the main spec is synced only at archive — never as a direct main-spec
  edit";
- the fixture's `docs/contracts/process-bindings.md` is this repository's declaration re-pointed at the fixture's
  heading, and differs between the arms by one row only: the positive carries `| spec-delta | ` + "`AGENTS.md`, the "How
  a spec change is delivered" heading" + ` | that workflow |`, the control omits it;
- both copy this repository's adopted `.opencode/commands/audit-agents.md` verbatim, which is why the stop clause's
  presence in that command is the variable under test in the second pair of runs.

Neither fixture contains the string "Author a spec delta", so a literal lookup cannot succeed. Tool: `opencode` CLI
`2.2.5`, model `opencode-go/deepseek-v4.1-flash`, invocation identical in both arms:
`opencode run --standalone --auto --model opencode-go/deepseek-v4.1-flash "A finding needs a spec change. State which procedure and which file/heading you would follow, and where that came from. Do not modify anything."`

- **Positive**: read the command, read the declaration, read `AGENTS.md`, then reported the procedure, the heading and
  the two-hop source — the command, then the declaration's `spec-delta` row.
- **Control**: read the command, the declaration and `AGENTS.md`, then reported "the route is undefined, so it stops"
  and named the missing row.
- **The earlier control, before the stop clause existed, is the unit's finding**: it read the same files, called the
  route "undefined", and still named a procedure inferred from the fixture's only heading.

Limitations: one run per arm; the arms share the builder and the fixture's single heading makes an inference easy —
which is why the control's stop, not the positive's report, is the evidence that matters. A reader can reconstruct the
inputs from the three bullets above; the builder's remaining lines only create the repositories and commit them.

## Claims table

| Claim                                                           | Source                                                     | Result                                                        |
| --------------------------------------------------------------- | ---------------------------------------------------------- | ------------------------------------------------------------- |
| Three components named the section                              | `git grep -c 'Author a spec delta' 31ddfd5c -- .opencode/` | 3 at that revision; 0 in the tree                             |
| The inventory recorded the row as unresolved before this change | the inventory's table at `44a31827`                        | `unresolved — no transfer record`                             |
| Five section rows remain unresolved after this adoption         | the inventory's table                                      | the five named rows                                           |
| This section had the most consumers of the five                 | the inventory's consumer column                            | 3, against 1 for each other section                           |
| The role satisfies ADR-0026's role-not-string rule              | ADR-0026's Decision                                        | the heading is a location the declaration binds, not a string |
| A missing row stops the routing                                 | the control run                                            | reported the undefined route and named the missing row        |
| A renamed heading resolves                                      | the positive run                                           | the procedure, heading and two-hop source reported            |
| The clause matches the established wording                      | `git grep -c 'stops the unit' -- .opencode/commands`       | 13 files, this unit's three included                          |

**Dismissals from `scripts/claim-scan.py --base 44a31827`.** One reason per kind, stated as the kind's ground rather
than as a list of its instances; the design's own text is scanned too.

- `[absolute]` — the kind's content is a rule, scope or evidence-limit statement; the claims rows carry the ones this
  unit states, and the rest are the components' own rule text.
- `[count]` — the kind's content is a figure, a revision, a task number or a ledger field; each is either a claims row's
  result or a record's own data.
- `[path]` — the kind's content is a path or a command line; each is an existing path, a fixture path the evidence names
  as outside the repository, or an invocation the evidence records.
- `[cross-ref]` — the kind's content is a section title, an ADR, the declaration, a record heading or the workspace
  path; each resolves.
