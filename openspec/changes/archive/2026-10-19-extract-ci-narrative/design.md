# Design

## Context

See `proposal.md` — Why. The audit's opportunity was verified on this branch before planning: `AGENTS.md`'s
`Continuous Integration` section is lines 974-1225 (its measured size is claims row 1), and
`showcase/quality/merge-governance` already owns the gate composition and the observational workflows' outcomes —
verified requirement by requirement: `Pull requests run the fast quality gate` (79),
`Pushes to main run the full quality gate` (149), `End-to-end tests run on a schedule and on demand` (193),
`Dependency security scan` (218), `Dependency update report` (251), `Helm update report` (311),
`Buildpack update report` (356), `Pinned-tool update report` (394), `Repository audits` (432),
`Upstream-reference report` (475), and `A deployment smoke` (520). So the moved narratives are **deletions with a
pointer**, not content moves. The release workflow's outcome is no exception: `showcase/quality/releases` owns it too —
the manual-dispatch trigger and its guards, the five images and the Helm chart published to the registry, the tag
created only after they publish, and the dry-run and backfill paths — so the change owes no delta and declares
`skip_specs: true`. The `setup-gradle` cache mechanics stay at root pointing at the workflow YAML, since they are
mechanism and changing them changes no scenario's outcome (the capability-spec rule). The report's precondition — that
the section is a partial split whose boundary must be drawn — is what the owner confirmed on 2026-10-19.

## Goals / Non-Goals

**Goals:**

- Draw the boundary the report said was undrawn: rules stay, per-workflow outcomes move, and the retained root carries
  what a reader must act on.
- Keep the section's two procedures and its imperatives at root, since they bind a future edit rather than describing an
  outcome.
- Reduce the always-loaded surface by the moved content, measured after the trim rather than estimated.

**Non-Goals:**

- The first extraction opportunity: shipped as its own unit (`extract-load-test-commands`).
- Moving the rules themselves, or the gate-placement rule the spec does not state.
- A mechanism to keep the section and the spec in step: the specs-auditor and the doc-consistency sweep own that.

## Decisions

**The boundary, as the owner confirmed it.** Stays at root: the gate-placement rule and its retry-not-tolerate
corollary; the `Workflow: Add or edit a workflow` and `Workflow: Action an update-check issue` procedures; the
schedule-placement rule; the `ubuntu-latest` validation rule; the CI-only-failure diagnostic lesson (an intermittent
failure that does not reproduce locally owes a diagnostic in place of another speculative mitigation, and a clean local
probe leaves a cause **unconfirmed, not refuted**); the `setup-gradle` cache mechanics, as a pointer to the workflow
YAML; and the retained pointer. Leaves the section: the `ci.yml` gate bullets and the `check`-members paragraph; the
`deployment-smoke.yml`, `e2e.yml`, `dependency-security.yml`, update-check, `audit.yml` and `release.yml` outcome
narratives; the cache detail (the log keys, the workflow enumeration, the npm cache paths, the `includeBuild` clause,
and the re-warm anecdote), deleted — the workflow YAML being the mechanism's authoritative configuration; and the
dependabot/CODEOWNERS narrative's detail.

**The one mixed paragraph is split by clause, not moved whole.** The rebuild-cost paragraph carries two things: the
cache-partition mechanism (`build[<hash>]` keys, `cache-read-only`, the `nodejs` includes, the one-time full-rebuild
cost) and the test-timing lesson. The mechanism's detail is deleted with the other outcome detail, the workflow YAML
being the mechanism's authoritative configuration; the mechanics stay at root as its pointer, and the lesson stays
beside them, because an agent diagnosing a CI-only failure must read it before diagnosing — the
imperative-versus-procedure rule.

## Risks / Trade-offs

- The moved outcome narratives duplicate the workflow YAML, which stays the mechanism's authoritative configuration →
  the spec states the outcomes and the YAML the mechanism, as `merge-governance` already does for the gate.
- The section's two procedures are long and rarely read → they stay because they bind a future edit; the report's own
  retained-root requirement names the gate-placement rule explicitly and the procedures are the same class.
- The reduction is an estimate until the trim is measured → the proposal states it as an estimate and the change records
  the measured result.

## Migration Plan

None: guidance prose, taking effect on the next read.

## Claims

Each fact the planning artifacts state about their evidence, with its source or deriving command. Seeded with
`python3 scripts/claim-scan.py`; its candidates are each resolved into a row or dismissed by kind below the table.

| #   | Claim                                                                                                                 | Source / deriving command                                                                                                                                                                   |
| --- | --------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1   | The section at `HEAD` is `AGENTS.md:974-1225`: 252 lines, 23,620 bytes                                                | `git show HEAD:AGENTS.md \| sed -n '974,1225p' \| wc -l -c`                                                                                                                                 |
| 2   | `merge-governance` states the gate composition and the observational workflows' outcomes                              | `grep -c "gate" openspec/specs/showcase/quality/merge-governance/spec.md` (66)                                                                                                              |
| 3   | The gate-placement rule is stated in the section and absent from the spec                                             | `grep -n "can remediate it" AGENTS.md`; `grep -c "can remediate it" openspec/specs/showcase/quality/merge-governance/spec.md` (0)                                                           |
| 4   | The `HEAD` section carries two procedures, both binding a future edit                                                 | `git show HEAD:AGENTS.md \| sed -n '974,1225p' \| grep -c "^\*\*Workflow:"` (2)                                                                                                             |
| 5   | The audit report's ~252-line size for this section measures exact; its ~26KB is rounded                               | `docs/audits/2026-10-19.md`, Opportunity 2, against row 1's command                                                                                                                         |
| 6   | The first opportunity shipped as its own unit                                                                         | `openspec/changes/archive/2026-10-19-extract-load-test-commands/` (present)                                                                                                                 |
| 7   | The release workflow's outcome is owned by `showcase/quality/releases`, so its narrative is a deletion with a pointer | `grep -c "release workflow" openspec/specs/showcase/quality/merge-governance/spec.md` (0); `grep -c "release workflow" openspec/specs/showcase/quality/releases/spec.md` (6)                |
| 8   | The trim reduced the section to 144 lines / 12,771 bytes, from 252 / 23,620                                           | `start=$(grep -n '^## Continuous Integration$' AGENTS.md \| cut -d: -f1); end=$(grep -n '^## Architecture$' AGENTS.md \| cut -d: -f1); sed -n "${start},$((end-2))p" AGENTS.md \| wc -l -c` |
| 9   | No capability spec specifies the trimmed section's prose, the `skip_specs` basis                                      | `grep -rn "Continuous Integration" openspec/specs/` (none); rows 2 and 7 own its behavior                                                                                                   |

**Dismissals** (the scan's candidates resolved to neither a row nor a fact, one reason per kind the scan classifies):
**path candidates** point at a site — a workflow, script, or file the text names — not a claim about the evidence;
**cross-reference candidates** name a repo path, an issue id, or a handoff label rather than asserting a fact;
**absolute-quantifier candidates** are quantifier words inside a rule's or an artifact section's own sentence, not an
independent universal claim about a corpus, which rows 2 and 7 carry where the text states one; and **count candidates**
are numerals inside the unit's own writing — the retained lesson's incident figures and its own records — rather than a
count of a corpus the change rests on.
