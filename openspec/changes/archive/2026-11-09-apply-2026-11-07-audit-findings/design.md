# Design

## Context

The audit report is `docs/audits/2026-11-07.md`, merged at `21b100bf`; its spec-corpus section names two structural
defects in `openspec/specs/showcase/quality/commit-hygiene/spec.md`. The report's other findings are separate units: the
config-guard guidance correction is `apply-2026-11-07-audit-doc-findings`, and the dead `handlebars` platform constraint
is `remove-dead-handlebars-constraint`. `showcase/quality/agent-skills` requires findings of different routes or scopes
to be delivered as separate units, which is why this change carries only the delta.

## Goals / Non-Goals

**Goals:**

- The two structural defects are corrected with every SHALL obligation and every scenario preserved.

**Non-Goals:**

- The config-guard guidance correction and the `handlebars` advisory, which are their own units.
- The report's accretion and instruction-budget sections, and any Purpose refresh.

## Decisions

### D1 — The spec fixes ride a delta, not a direct main-spec edit

`showcase/quality/agent-skills` requires a spec change to route through an OpenSpec change with a delta, so each
requirement travels as a whole `MODIFIED` block copied from the main spec and edited in place. The delta is the only
place the corrected text lives until `openspec archive` writes it into the main spec.

### D2 — The structure is corrected, the obligations are not

The orphaned sentence carries no SHALL, so it is deleted. The detached paragraph's first clause is already stated in the
body ("a violation record, not a recovery case"), so only the two obligations the body lacks move into it. No scenario
is renamed, dropped, or reordered.

## Risks

- **A `MODIFIED` block that drifts from the main spec.** Mitigated by generating each block from the main spec's own
  lines and diffing the result; `openspec validate --all` checks header and scenario preservation.

## Verification

- `openspec validate --all`.
- The generator's diff against the main spec shows exactly the intended change per block.
- `./gradlew spotlessApply` then `spotlessCheck`.

## Claims table

The base revision is `21b100bf47a67746b63884b5238c7d3648e47fbb`, the unit's fork point; `origin/main` has since advanced
to `634bc3b5` without changing any file these rows name.

| Claim                                                               | Source                                                                     | Reproduces                                                            |
| ------------------------------------------------------------------- | -------------------------------------------------------------------------- | --------------------------------------------------------------------- |
| The orphaned sentence stands between the two SHALL paragraphs       | `git show 21b100bf:openspec/specs/showcase/quality/commit-hygiene/spec.md` | the sentence between "staged set." and the `--expect-clean` paragraph |
| The detached SHALL paragraph sits after the requirement's scenarios | same source                                                                | the paragraph after "does not authorize another restart"              |
| The body already states the dropped clause                          | same source                                                                | "a violation record, not a recovery case" in the requirement body     |
| The requirement headers match the delta's verbatim                  | same source                                                                | the two `### Requirement:` lines the delta copies                     |
| `origin/main` advanced without touching this change's subject       | `git diff --name-only 21b100bf 634bc3b5`                                   | sixty-nine paths, none of them the `commit-hygiene` spec              |

Re-derived after the implementation's last edit: every row's source was re-run at the base revision, and each reproduces
as stated.

### Dismissals

- **`path` candidates** — a path named in a task, the proposal, or a design sentence, or inside quoted spec text; each
  names a file or command the change's own artifacts reference, not a claim about a corpus, and the rows above carry the
  ones that assert evidence.
- **`absolute` candidates** — words such as "only", "no", "every", and "all" inside quoted requirement text, task
  instructions, and scope statements; the scope statements are verified by the diff itself, and the quoted text is
  copied from the main spec rather than asserted by this change.
- **`count` candidates** — a task number and its verb, not a count of any set.
