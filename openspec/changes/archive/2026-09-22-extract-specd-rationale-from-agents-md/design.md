## Context

See `proposal.md` - Why. The relevant current state, verified before writing:

- `AGENTS.md`'s promotion gate (and its `agent-skills` counterpart) routes a rule that **fails** the gate to "a check, a
  spec, an ADR, or the change dir instead of the always-loaded file" — a rule for _rejected_ rules only.
- `AGENTS.md` carries a 16-line gotcha on the WebFlux blocking-execution configurers; the gateway and query-service
  specs state the error outcomes (`400`/`404`) without the routing that makes them resolve.
- `code-quality`'s Purpose scopes it to conventions **enforced through the build**, and its requirements are shaped to
  that scope: most name the build, a check, or an enforcement mechanism, and the remainder reach it through their
  scenarios. It is not a home for ungated conventions.

## Goals / Non-Goals

**Goals:**

- State the spec/`AGENTS.md` boundary as a rule, extending the existing routing clause rather than adding a new one.
- Give the configurer invariant a home where a reader of the affected capability finds it, and leave `AGENTS.md` a
  pointer so its size decreases without losing the rule.

**Non-Goals:**

- Moving ungated code conventions (Javadoc, Lombok, no-comments) into `code-quality` — that spec is gate-scoped, and
  adding unenforced conventions would misstate it (the third band below records this as a decision).
- Changing any code. The invariant is documented, not altered; the configurers stay as they are.

## Decisions

### The four-band boundary

A rule goes to the spec when the spec can own it; otherwise it stays in `AGENTS.md`:

| Band                            | Example                                                       | Home                | Why                                                                                    |
| ------------------------------- | ------------------------------------------------------------- | ------------------- | -------------------------------------------------------------------------------------- |
| Enforced by a gate              | formatting, 120-col, license headers, module graph            | the capability spec | the build verifies it; the spec is a check contract                                    |
| Runtime behavior                | the error paths' scheduling dependency                        | the capability spec | it is the system's observable behavior, so it belongs where that behavior is specified |
| Ungated code convention         | Javadoc, Lombok, no-comments                                  | `AGENTS.md`         | no gate verifies it; `code-quality` is enforced-by-the-build and would misstate itself |
| Agent process / incident memory | commit discipline, capture loop, "an earlier draft claimed X" | `AGENTS.md` only    | not about the system's behavior, so not spec-able                                      |

The rule this change sharpens is the second row: an accepted rule whose rationale is **normative in a spec** already
keeps a pointer. It adds the neighbouring case — where the specs describe the subject **only as an outcome**, the
mechanism belongs in the capability spec and the bullet keeps the same shape: the rule, and a pointer.

**Alternatives considered.** (a) _A new requirement for the boundary_ — rejected: the routing clause already exists in
both homes, so extending it costs one clause where a new requirement costs a permanent corpus entry. (b) _Putting the
boundary in `AGENTS.md` only_ — rejected: the routing clause it extends is spec'd, so its missing half belongs there too
(the `bound-work` precedent: the promotion gate shipped in both). (c) _Moving the ungated conventions_ — rejected per
the table.

### Fold the invariant into the requirements it underpins

Each affected requirement gains a routing clause and one scenario rather than a new requirement: the spec's own
instruction is to avoid internal class/framework names and to keep specs behavioral, so the clause states the **routing
and its consequence**, not the class that implements it. The measured consequence is the point — remove the routing and
the mapping changes:

| Path                                      | With routing      | Without routing                             |
| ----------------------------------------- | ----------------- | ------------------------------------------- |
| gateway, invalid payload (CT)             | `400 Bad Request` | `500 Internal Server Error` (9 of 76 cases) |
| query-service, invalid/missing query (IT) | `400` / `404`     | `503 Service Unavailable` (3 of 10 cases)   |

Two evidence points bound the mechanism and are worth keeping in view: the gateway's not-found test **passes** without
the routing because its lookup is stubbed to return a direct error, while the query-service's **fails** because its
error comes off the dispatched query bus — so the routing is what carries a bus error to its handler, not a blocking
workaround (there are zero BlockHound hits either way).

## Risks / Trade-offs

- **A `SHALL NOT` on a mechanism can rot if the code changes.** Mitigation: the clause is phrased behaviorally (the
  routing and its consequence), so it stops being true exactly when the behavior changes — and the scenario pins the
  consequence the tests already assert, so a removal reddens CI.
- **`AGENTS.md` shrinks less than the 16 lines removed**, because the rule and a one-line why stay as a pointer.
  Mitigation: the shrink is a side effect; the correctness win (the spec owns its own behavior) is the reason.
- **Five `MODIFIED` blocks in one change** raise the chance a block drops a scenario. Mitigation: each block was
  generated from the main spec's own text and the scenario counts checked (5/5, 3+1, 2+1, 3+1, 2+1); tasks includes the
  check.

## Migration Plan

None — no code, data, or deployment change.

## Open Questions

None.
