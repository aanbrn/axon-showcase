# Tasks

## 1. Widen the diagrammer definition and its trigger

- [x] 1.1 In `.opencode/agent/diagrammer.md`, widen the frontmatter `description` and the body from ASCII to ASCII
      **and** Mermaid: keep the ASCII geometry rules and add the Mermaid path (choose the diagram type and constructs;
      keep the content clear of the host renderer's GitHub control toolbar; render the result once with `mermaid-cli` to
      check it; return the diagram **source**). Point the Mermaid path at the pitfalls recorded in the `AGENTS.md`
      "geometry and content encode semantics" bullet. Verify: the file reads coherently for both media and
      `./gradlew spotlessCheck` passes.
- [x] 1.2 In `.opencode/commands/diagram.md`, widen the `description` and the steps to name both media (draw or fix an
      ASCII **or** Mermaid diagram). Verify: it reads coherently and `./gradlew spotlessCheck` passes.

## 2. Update the guidance and process docs

- [x] 2.1 Update the diagrammer bullet in `AGENTS.md` (~line 1101) to cover Mermaid as well as ASCII, and correct its
      stale ASCII example ("a README flow diagram") — README flow diagrams are Mermaid now — so it names the ASCII
      diagrams that remain (e.g. the README project tree or a diagram's plain-text fallback). Verify: it reads
      coherently and `./gradlew spotlessCheck verifyCapturedMarkers` passes.
- [x] 2.2 Update the two `README.md` rows — the agent table's `diagrammer` row and the `/diagram` slash-command row — to
      drop the ASCII-only wording. Verify: they read coherently and `./gradlew spotlessCheck` passes.

## 3. Retire the implemented idea

- [x] 3.1 Remove the `## 2026-10-02` section from `docs/ideas.md` entirely (it holds only the now-implemented
      diagrammer-widening entry). Verify: `grep -c 'diagrammer\|## 2026-10-02' docs/ideas.md` returns 0.

## 4. Verification

- [x] 4.1 Run `openspec validate --changes` and confirm the change validates — the `MODIFIED` requirement keeps the
      ASCII scenario and adds the Mermaid one. Evidence: the command's output.
- [x] 4.2 Run `./gradlew spotlessCheck` and confirm all touched markdown is formatted. Evidence: the task's exit status.
- [x] 4.3 Smoke-run the widened subagent: after restarting OpenCode so the edited definition is loaded, invoke
      `diagrammer` on a small Mermaid mapping and confirm it returns Mermaid **source** that parses and lays out with
      `npx @mermaid-js/mermaid-cli`. (The restart is required because an edited subagent definition is not reloaded
      mid-session. Toolbar clearance is the caller's live-render step, not this smoke-run.) Evidence: the returned
      diagram and its render. Done: it returned `flowchart LR` Mermaid (a `sign-off` subgraph over Review/Publish),
      citing the toolbar and subgraph-title pitfalls; re-rendered at 484×140, so it parses.
- [x] 4.4 Run the `lesson-capture` subagent over the diff, apply its durable proposals, and record the applied net
      `AGENTS.md` delta on this task. Applied: +3 lines, merged into the "A `MODIFIED` requirement block…" bullet (a
      scenario cannot be renamed).
