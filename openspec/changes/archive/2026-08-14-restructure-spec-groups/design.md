## Context

See proposal.md - Why. Nine specs sit flat under `openspec/specs/showcase/`, mixing pipeline stages (gateway, write
side, read side), infrastructure libraries (the three extensions), deployment (helm-chart), and quality (load-tests).
The change relocates the existing `spec.md` files into six architectural groups without altering any requirement or
scenario text. No build, test, or runtime system is affected; only spec-capability paths change.

## Goals / Non-Goals

**Goals:**

- Establish a spec-tree layout that mirrors the CQRS architecture and the project's existing doc conventions.
- Keep the relocation purely mechanical: `git mv` of `spec.md` files with zero content edits.
- Leave `openspec validate --specs` green after the moves.

**Non-Goals:**

- Rewording, merging, or splitting any requirement or scenario — behavior text stays byte-identical.
- Renaming capability *content* (spec titles, requirement IDs) — only directory placement changes.
- Reorganizing archived changes under `openspec/changes/archive/` — they are immutable snapshots.

## Decisions

- **Group by architectural role, not artifact kind.** Six groups: `gateway/`, `write-side/`, `read-side/`,
  `platform/`, `deployment/`, `quality/`. Alternative considered and rejected: grouping by artifact kind (`services/` vs
  `extensions/` vs `delivery/`) — that collapses the pipeline signal this repo exists to showcase.
- **`load-tests` and `helm-chart` get their own groups.** They are distinct concerns (quality vs deployment), and
  lumping them into a single "delivery" group would blur the semantic split. `deployment/` holds how the system runs;
  `quality/` holds how it is verified.
- **Extensions go under `platform/`.** The three extension modules are compile-time/startup infrastructure that
  cross-cuts all stages, so they sit beside the pipeline rather than inside any stage.
- **Pure directory moves via `git mv`, no content edits.** Keeps diffs readable and history intact; the `skip_specs`
  marker already records that no spec behavior changes.

## Risks / Trade-offs

- [Future delta specs must reference the new capability paths; a stale old-path reference would break sync] →
  Mitigation: all moves land in one commit with the proposal capturing the full old→new mapping as reference.
- [A future change proposal cites `showcase/<old-path>` and silently targets the wrong spec] → Mitigation: the six-group
  layout is documented in the proposal and validated by `openspec validate --specs` at archive time.
- [Deep nesting (`showcase/gateway/api-gateway`) adds a path segment to every capability] → Mitigation: depth is fixed
  at two segments under `showcase/`, consistent with how the project already nests capabilities.
