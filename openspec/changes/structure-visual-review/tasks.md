# Tasks

## 1. Spec delta

- [x] 1.1 `openspec/changes/structure-visual-review/specs/showcase/quality/agent-skills/spec.md` — one `MODIFIED` block
      for "Per-change quality-gate and analysis subagents are available", copied from the main spec rather than
      hand-written (the block is 306 content lines and 18 scenarios): the "Screenshots are reviewed visually" scenario
      carries the structured review, the "Mermaid diagrams are drawn by the pro-model diagrammer" scenario carries the
      handoff and the parse-and-layout-only clause, and a new sibling scenario "A Mermaid render is visually reviewed
      before the diagram ships" states the caller's path. No existing scenario is renamed, dropped, or reordered (the
      new one is appended). Verify: `openspec validate --changes` passes and the block holds 19 scenarios; the diff of
      the block against `git show HEAD:openspec/specs/showcase/quality/agent-skills/spec.md` is exactly the three
      scenario edits. **Done** — the block is built from `git show HEAD:…` rather than hand-written;
      `openspec validate --changes` passes; 19 scenarios (18 preserved in order, 1 appended); the diff against `HEAD`'s
      block is 2 hunks — 3 lines removed, 35 added, the block growing by 32 lines — with no `#### Scenario` header
      removed.

## 2. The `vision` agent

- [x] 2.1 `.opencode/agent/vision.md` — repin the model, ending on `opencode-go/claude-haiku-5-5` (see `design.md`
      Context: as pinned, the agent cannot run). **Done** — the pin resolves, and after two further moves it names a
      model that reads images: `opencode-go/gemini-4-argon-contributor`. The pinned DeepSeek line proved retired (its
      name aliased to the latest Flash) and its endpoint unreachable; `opencode/deepseek-v4-flash` ran but rejected
      images.
- [x] 2.2 `.opencode/agent/vision.md` — replace the general-description body with the structured visual review: the
      artifact and scope line; the three labelled groups (observed facts; visual risks or suspected defects; what the
      image cannot settle); the diagram checks; every item naming its visible element and approximate position; a claim
      needing a measurement marked as a hypothesis naming its method; how it loads the artifact (an `http(s)://` URL —
      the browser blocks `file://`); read-only and concise. Widen the frontmatter `description` to match. Verify: the
      file states each obligation the delta's vision scenario states, and no obligation twice. **Done** — the body
      states the artifact-and-scope line, the three groups, the diagram checks, the element-and-position rule, the
      hypothesis rule, the `http(s)`/`file://` loading rule, and read-only concision, each obligation once.

## 3. The `diagrammer` agent and its trigger

- [x] 3.1 `.opencode/agent/diagrammer.md` — in the Mermaid section, add the visual-review handoff (the delta and the
      definition state its fields), render both the PNG the review reads and the SVG a measurement runs against, and
      state that the local render proves parse and layout only — never host toolbar clearance or browser layout. Keep
      the ASCII rules unchanged, and point the pitfall reference at the two `AGENTS.md` bullets that own the mechanics.
      Verify: the file states each obligation the delta's Mermaid scenario states. **Done** — the Mermaid section gains
      the handoff bullet, renders both the PNG and the SVG, and reports the render as parse/layout proof only; the ASCII
      rules are unchanged, and the pitfall reference now names both `AGENTS.md` bullets that record the mechanics.
- [x] 3.2 `.opencode/commands/diagram.md` — orchestrate the Mermaid path: serve the render (the browser blocks
      `file://`), delegate it to `vision` with the handoff, inspect a live GitHub render where reachable, report the
      local check and the visual review separately, state an unavailable live render as a limitation, and settle a
      measurement hypothesis before acting on it. The ASCII path stays as it is. Verify: the command's steps match the
      delta's review-path scenario. **Done** — the Mermaid step serves the render, delegates it to `vision` with the
      handoff, inspects a live GitHub render where reachable, reports the two results separately, states the limitation,
      and settles a measurement hypothesis; the ASCII step ends the path.

## 4. Documentation

- [x] 4.1 `AGENTS.md` — the vision bullet: the corrected pin, the structured review, the `http(s)`/`file://` constraint,
      and the hypothesis rule, pointing at the definition for the report's shape; the diagrammer bullet: the handoff and
      the `vision` delegation; and the model-pin bullet's parenthetical about what `README.md` names, which the README's
      new provider mention falsifies. Keep each bullet's length near what it replaces. Verify:
      `./gradlew spotlessCheck verifyCapturedMarkers` passes and neither bullet restates a pitfall `AGENTS.md`'s Mermaid
      bullet owns. **Done** — all three sites: the vision bullet carries the pin, the structured review, the
      `http(s)`/`file://` constraint and the hypothesis rule; the diagrammer bullet carries the handoff and the `vision`
      delegation; the model-pin bullet's parenthetical now names what `README.md` does and does not carry;
      `spotlessCheck` and `verifyCapturedMarkers` pass, and neither restates the Mermaid bullet's pitfalls.
- [x] 4.2 `README.md` — the `vision` agent-table row, the `diagrammer` row, the `/diagram` slash-command row, and the
      model-pin paragraph the provider correction falsifies. Verify: `./gradlew spotlessCheck` passes. **Done** — all
      three rows and the paragraph are updated; `spotlessCheck` passes.
- [x] 4.3 `docs/ideas.md` — retire any parked entry this change implements, or record that none matches; and park the
      follow-up this change surfaces (the review-round record's session column and its continuity check). Verify: grep
      the file for `vision`, `diagram`, and `mermaid` and account for every hit. **Done** — the grep returns no match,
      so no parked entry is implemented by this change and none is retired; and the session-continuity follow-up is
      parked at the top of the `2026-10-18` section with its disposition tag.

## 5. Verification

- [x] 5.1 Build the non-repository fixture: a small Mermaid source in `$TMPDIR/opencode/` exercising labelled edges, a
      span/subgraph, and directional arrows, and render it with `mermaid-cli` to both SVG and PNG. **Done** —
      `$TMPDIR/opencode/diagram-review/fixture.mmd` (`flowchart LR`: two labelled edges, one labelled non-nested
      subgraph spanning three nodes, one dashed labelled edge to a fourth node);
      `npx @mermaid-js/mermaid-cli@11 -i fixture.mmd -o fixture.svg` and `… -o fixture.png` both succeeded, the PNG
      being 784×118 (`file fixture.png`).
- [x] 5.2 Run the `vision` agent over the rendered image (served over HTTP) and confirm its report separates observed
      layout facts from suspected issues requiring measurement. **Done** — the fixture (`fixture.mmd`, rendered to
      `fixture.png` at 784×118 and served at `http://127.0.0.1:8791/fixture.png`) was reviewed by the `vision` agent on
      the final pin, `opencode-go/claude-haiku-5-5`, which loaded it in the browser and returned the structured review:
      observed facts (four nodes in `LR` order; the subgraph spanning `SCHEDULED`→`STARTED`→`FINISHED` and not
      `REMOVED`; each arrowhead at its target), an expected-against-seen line per handoff element, visual risks each
      marked as a hypothesis with the method that would settle it (`boundingBox()`, `getComputedStyle`), and a
      not-assessable section. The superseded investigation is in `design.md`'s Context; the two earlier pins and their
      failures are recorded there too. No visual-review claim is made beyond this fixture.

- [x] 5.3 `./gradlew spotlessApply` after the final edit to a Spotless-owned file, then `spotlessCheck` green;
      `openspec validate --all` and `openspec validate --changes` pass; the 120-character recipe applied over the
      changed set derived from `git status --porcelain --untracked-files=all`. **Done** — `spotlessApply` re-run after
      every edit to a Spotless-owned file, and `spotlessCheck` exits 0 on the tree as it now stands (the review rounds
      that followed each re-ran it); `openspec validate --all` 27/27 and `--changes` 1/1; the 120-character recipe
      applied over the changed set derived from `git status --porcelain --untracked-files=all`.
- [x] 5.4 The pre-write derivation pass: derive each claim the change states from its source (the pinned-provider and
      `file://` findings re-derived from the commands that produced them; every count re-grepped), record it as this
      change's **claims table** in `design.md` seeded by `scripts/claim-scan.py`, and resolve each candidate into a row
      or a by-class dismissal. **Done** — the claims table is in `design.md`, each row carrying its source or deriving
      command, plus four by-class dismissals, seeded from `scripts/claim-scan.py`'s candidates for this diff.
- [x] 5.5 Run the `review-quick` subagent over the planning artifacts, then over the implementation, repeating until it
      reports no new observations; append each round's record line to `docs/review-rounds.tsv` as its report arrives,
      and record the deep-review routing decision (5.6) on this task. **Done** — the loop's rows are in
      `docs/review-rounds.tsv`, one per round, appended as each report arrived; rounds 1–6 found the definition, delta,
      and record defects, rounds 7–10 only record-completeness residue, and every finding is `FIXED_AND_VERIFIED`.
      **Deviation recorded:** rounds 1–6 each ran in a fresh reviewer session — I did not carry the session id forward,
      against the review gate's rule to continue the reviewer's session (none of its exceptions applied); rounds 7–10
      continued round 7's session (`ses_eb09babdeffeWFFWmFI3l3uc3N`), and the follow-up is parked in `docs/ideas.md`.
      **Routing decision (task 5.6): `RECOMMENDED`** — `scripts/review-routing.py --change structure-visual-review`
      reports `[repeated-class]` (rows repeat a prior class) — awaiting the owner's explicit resolution.
- [x] 5.6 Derive the deep-review routing decision with
      `python3 scripts/review-routing.py --change structure-visual-review`, decide at or above the floor, run a
      `REQUIRED` review before completion (or record a `RECOMMENDED` one's explicit resolution), and record the outcome
      and its signals on task 5.5. **Done** — the derivation reports `ROUTING: RECOMMENDED` on the `[repeated-class]`
      signal (rows repeating a prior class), and the owner resolved it to **run** the deep review, on the reasoning that
      the loop's non-convergence traces to a premise — the `vision` pin — that moved three times mid-flight. **Deep
      review (session `ses_eb04e6e6bffe…`), pass 1 of 2** — findings recorded below as they arrive. **Deep review (pass
      1, session `ses_eb058f008ffeeoBltn3CjHbD49`): 6 findings, all fixed.** Its blocking pair was the pin's unswept
      state — the capture's own `AGENTS.md` worked example asserted the false premise this change corrects, and
      `README.md` named a superseded pin; also fixed were the Context's move count and fact count, and a missing claims
      row for the final pin. **Pass 2 (the independent verification) stalled** after its checks ran, returning no
      report, so this unit ships on pass 1 with the owner's approval; the stall is recorded rather than reported as a
      clean pass.
- [x] 5.7 Run the `lesson-capture` subagent over the diff, apply its durable proposals, and record the applied net
      `AGENTS.md` delta (or the skip and its reason) on this task. **Done** — **1 addition, 0 retirements, 1
      authoring-system recommendation.** The addition widens the model-pin bullet's lead to require resolving a pin
      before setting it (net **+5 `AGENTS.md` lines**; the change's whole `AGENTS.md` delta is +38/−30, net +8). The
      recommendation is the review-round record's missing session field and its check — routed to `docs/ideas.md` rather
      than `AGENTS.md`, since the rule already exists and only a record slot plus a check makes the omission visible. It
      reported the two recurring review classes (`claim-unreproducible`, `impact-omission`) and the session miss as
      already covered by existing rules, so no wording was added for them.
- [ ] 5.8 Ask the user for the manual review pass once 5.5 is clean and 5.7 is done; do not commit, push, archive, open
      a PR, or merge before they approve. Ask them to reload OpenCode so the edited `vision` definition is served, and
      re-run 5.2 against the reloaded agent.
