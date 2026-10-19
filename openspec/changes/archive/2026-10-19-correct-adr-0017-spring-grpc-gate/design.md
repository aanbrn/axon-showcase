# Design

## Context

See `proposal.md` — Why. The 2026-10-19 architecture audit's advisory was verified live on this branch before planning,
against the sources it names rather than against its prose:

- `docs/adr/0017-grpc-query-transport.md`'s `Revisit when:` reads "the signal is a first-party Spring gRPC release
  targeting Boot 3.x", with no qualifier, so a Boot-3-targeting release of any maintenance status reads as meeting it.
- The same file's `_Alternatives considered:_` sentence for Spring gRPC cites only "its 1.0 line targets Spring Boot 4",
  which leaves the `0.x` line unaddressed at that site.
- `docs/adr/0017-grpc-query-transport.md` is the only live artifact that **owns** the gate: the audits state it as a
  finding (that is how the advisory reached us), while no capability spec, `AGENTS.md`/`README.md` entry, config
  comment, or `docs/ideas.md` entry names `ADR-0017`, `Spring gRPC`, or `org.springframework.grpc`.
- The `## Consequences` bullet that ties the community starter's lag to the gate ("which is the `Revisit when:` above")
  stays accurate under the corrected wording.

## Goals / Non-Goals

**Goals:**

- Record the owner's 2026-10-19 resolution in the gate itself, so the condition a check reads is the one the owner
  decided — a **maintained** first-party Boot-3 line — with the evidence that the `0.x` line does not meet it.
- Label the clause's type, as the ADR-gate convention requires for a deferral's `Revisit when:`.
- Give the alternatives sentence the alternative's rejection reason and point it at the gate for the qualifier's full
  evidence, so the reason is stated where the rejection is argued and the evidence where the qualifier lives.

**Non-Goals:**

- Reopening the transport decision: the deferral stands, and the owner's resolution is what confirms it.
- A mechanism that keeps a gate's qualifier from being read loosely: the architecture-auditor's deferral check already
  reports a record whose premise appears met, and the correction belongs in the gate's wording rather than in a second
  check.
- ADR-0017's other content — its Decision, its Consequences, and its relationship to ADR-0004 and ADR-0009 — which the
  audit verified as consistent.

## Decisions

**State the qualifier in the gate rather than adding a clarifying sentence beside it.** A `Revisit when:` line is what a
check reads, so the condition it states must carry the qualifier the owner resolved; a note elsewhere in the ADR leaves
the line a check reads unchanged, which is the defect the advisory reports.

**Record the qualifier's full evidence at the gate, and give the alternatives sentence the rejection reason with a
pointer to it.** The alternative's rejection reasoning is why the decision was made, so the sentence must still state
why Spring gRPC was rejected — dropping it would leave the sentence citing only the `1.0` line, the gap this change
closes — while the gate is what a future check evaluates, and the release-level evidence belongs where the qualifier it
supports lives.

## Risks / Trade-offs

- The qualifier is a wording, not a gate — nothing enforces that a future edit keeps it → the architecture-auditor
  reports a record whose premise appears met, which is the same mechanism that surfaced this one.
- The evidence names an end-of-life release that upstream could in principle revive → the gate states the condition
  (maintained) rather than the release, so a revival meets it and the named release is only the current evidence.

## Migration Plan

None: a recorded decision's wording, taking effect on the next read.

## Claims

Each fact the planning artifacts state about their evidence, with its source or deriving command. Seeded with
`python3 scripts/claim-scan.py`; its candidates are each resolved into a row or dismissed by kind below the table.

| #   | Claim                                                                                  | Source / deriving command                                                                                                                                                                                                |
| --- | -------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| 1   | The gate states no qualifier, so any Boot-3 release reads as meeting it                | `git show HEAD:docs/adr/0017-grpc-query-transport.md \| sed -n '7,9p'`                                                                                                                                                   |
| 2   | The alternatives sentence cites only the `1.0` line's Boot 4 target                    | `git show HEAD:docs/adr/0017-grpc-query-transport.md \| sed -n '47,48p'`                                                                                                                                                 |
| 3   | The ADR is the only live artifact that owns the gate; the audits state it as a finding | `grep -rln "ADR-0017\|Spring gRPC\|org.springframework.grpc" docs/adr/ AGENTS.md README.md openspec/specs/ config/ docs/ideas.md` → the ADR only; the audits state it as a finding (`docs/audits/2026-10-19.md:320-321`) |
| 4   | No capability spec names the ADR or the coordinate                                     | `grep -rl "ADR-0017\|Spring gRPC\|org.springframework.grpc" openspec/specs/` → no matches                                                                                                                                |
| 5   | The `0.x` line targets Boot 3 and is end-of-life                                       | `docs/audits/2026-10-19.md`, the architecture advisory, which cites `spring-projects/spring-grpc#393`                                                                                                                    |
| 6   | The Consequences bullet tying the starter's lag to the gate stays accurate             | `git show HEAD:docs/adr/0017-grpc-query-transport.md \| sed -n '62,64p'`                                                                                                                                                 |

**Dismissals** (the scan's candidates resolved to neither a row nor a fact, one reason per kind the scan classifies):
**path candidates** name a file, coordinate, or identifier — the sites themselves, not evidence claims;
**cross-reference candidates** name a record's own field or a comparison procedure rather than asserting a fact;
**absolute-quantifier candidates** are modal or structural words inside a rule's or a row's own sentence, not an
independent universal claim about the corpus; **count candidates** are numerals inside the unit's own artifacts and
records, never a count of a corpus the change rests on.
