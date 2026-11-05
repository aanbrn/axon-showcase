## MODIFIED Requirements

### Requirement: Diagrams and screenshots are produced by their specialist subagents

The repository SHALL produce and inspect diagrams and screenshots through the specialist subagents — `vision` for visual
review and `diagrammer` for diagram geometry — rather than through the text-only main agent. The scenarios below are
carried unchanged from the requirement this one replaces, and the last is added by this change; the diagram stage's own
consolidation is a later unit.

#### Scenario: Screenshots are reviewed visually

- **WHEN** the main agent needs to inspect a screenshot, image, or visual UI state (e.g. web-UI styling, or a rendered
  diagram)
- **THEN** the `vision` subagent reads the image and returns a **structured visual review**, so the text-only main agent
  can delegate visual review
- **AND** the review states the artifact it inspected and the scope of the review
- **AND** it reports in three separated groups rather than one description: the **observed facts** (what is visibly
  present), the **visual risks or suspected defects**, and **what the image cannot settle**
- **AND** for a diagram render it checks every visible node and whether its shape matches its intended role, each edge's
  direction and arrowhead, which label belongs to which edge, duplicate, missing, or ambiguous labels, collisions,
  overlap, clipping, cropping, unreadably small content, and the whitespace left near the right edge and near any host
  control overlay
- **AND** each item names the visible element it concerns and its approximate position in the image
- **AND** a visual claim that needs a measurement to settle is reported as a **hypothesis** naming the method that would
  settle it (e.g. `getComputedTextLength()` for SVG text against its container, or a label's `scrollWidth` against its
  container's width) — an apparent clipping, overflow, or overlap is never reported as a confirmed fact
- **AND** it stays read-only and concise: it reports what it sees and never edits a file

#### Scenario: ASCII diagrams are drawn by the pro-model diagrammer

- **WHEN** an ASCII diagram needs to be created, aligned, or fixed (e.g. the README project tree or a diagram's
  plain-text fallback)
- **THEN** the `diagrammer` subagent renders it with the pro model: it establishes the semantic mapping (which span
  starts and ends where), aligns by character width, and preserves deliberate asymmetry — so the cheap flash main agent
  does not spend effort on ASCII geometry

#### Scenario: Mermaid diagrams are drawn by the pro-model diagrammer

- **WHEN** a Mermaid diagram needs to be created, fixed, or reworked (e.g. a README flow diagram)
- **THEN** the `diagrammer` subagent produces it with the pro model: it chooses the diagram type and constructs and
  keeps the content clear of the host renderer's controls
- **AND** it returns the diagram **source**, so the cheap flash main agent does not spend its own effort on Mermaid's
  rendering pitfalls
- **AND** it returns a **visual-review handoff** beside the source: the nodes, edges, directions, labels, and spans the
  render is expected to show, the rendered artifact that must be inspected, and the visual risks a source-level check
  cannot establish
- **AND** its own `mermaid-cli` render is reported as what it proves — that the source parses and lays out — never as
  proof of the host renderer's toolbar clearance or of a browser's layout

#### Scenario: A Mermaid render is visually reviewed before the diagram ships

- **WHEN** the `diagrammer` subagent has returned Mermaid source with its visual-review handoff
- **THEN** the calling agent makes the render `diagrammer` produced inspectable — serving it, since a browser cannot
  open a `file://` path — and delegates it to the `vision` subagent with the handoff, so the render is inspected rather
  than only parsed
- **AND** where the target is GitHub-rendered Markdown and Playwright is available, it also inspects a live GitHub
  render — the host `viewscreen` toolbar collision and browser layout a local render cannot reveal
- **AND** it reports the local render check and the visual review separately
- **AND** a claim the review raises as needing a measurement is settled by one before the diagram ships, since an
  observation is not a measurement
- **AND** an unavailable live GitHub render is stated as a limitation, never treated as equivalent to one that was
  inspected

#### Scenario: A review that obtains no pixels says so

- **WHEN** the artifact the caller names yields no inspectable image — the page cannot be loaded, or the capture returns
  no image
- **THEN** the `vision` subagent reports that the visual review was not performed, showing the tools' own outputs under
  "what the image cannot settle", rather than returning a review of nothing
