# Tasks

## 1. Fix the README lifecycle diagram

- [x] 1.1 Replace the Mermaid lifecycle diagram's `SCHEDULED -.->|"REMOVED (any time)"| REMOVED` and
      `FINISHED -.-> REMOVED` edges with a single `subgraph lifecycle["REMOVED (any time)"]` … `end` around
      `SCHEDULED`/`STARTED`/`FINISHED` and one `lifecycle -.-> REMOVED` edge; keep `direction LR` inside the subgraph
      and set `diagramPadding` to 230. Realign the plain-text fallback's right bracket bar to `FINISHED`'s center.
      Verify: the three lifecycle arrows are intact, there is no `STARTED → REMOVED` edge, and the fallback still
      matches. Evidence: `git diff README.md`.
- [x] 1.2 Render the diagram with `npx @mermaid-js/mermaid-cli@11` and read the render with the `vision` subagent.
      Evidence: one horizontal row `SCHEDULED → STARTED → FINISHED → REMOVED`, the span encloses the three lifecycle
      states, the dotted edge runs span → `REMOVED`, no clipping.

## 2. Relax the recorded span rule

- [x] 2.1 In `.opencode/agent/diagrammer.md`, draw a **single** range over a run as one non-nested labelled subgraph and
      state **overlapping or nested ranges** over the same run inline as edge labels; qualify `direction` to the
      subgraph-level outside edge (a member-node edge to outside ignores it). Verify: the rule reads coherently and
      `./gradlew spotlessCheck` passes.
- [x] 2.2 In `AGENTS.md`, amend the "a diagram's geometry and content encode semantics" bullet (carve-out + the
      `direction` correction) and the "Diagrammer subagent" bullet's Mermaid-pitfall parenthetical. Verify: both read
      coherently and `./gradlew spotlessCheck verifyCapturedMarkers` passes.

## 3. Verification

- [x] 3.1 Run `openspec validate --changes` and confirm the change validates with `skip_specs: true`. Evidence: the
      command's output.
- [x] 3.2 Run `./gradlew spotlessCheck` and confirm all touched markdown is formatted. Evidence: the task's exit status.
- [x] 3.3 Ran the `lesson-capture` subagent over the diff; applied its one durable proposal (a third root-cause shape
      folded into the "Auto-review the change…" bullet) and no retirements. Applied net `AGENTS.md` delta: +10 lines (27
      insertions / 17 deletions).
