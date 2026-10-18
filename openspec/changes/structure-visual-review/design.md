# Design

## Context

The repository's diagram path is two subagents plus one trigger, and the seam between them is open. `diagrammer`
(`.opencode/agent/diagrammer.md`, pinned to the pro model) draws in ASCII or Mermaid and verifies a Mermaid render with
`mermaid-cli`; `widen-diagrammer-to-mermaid` decided that its local render proves only parse and layout, and that
toolbar clearance is "the caller's Playwright + `vision` step, not here" — a Non-Goal it did not implement. `vision`
(`.opencode/agent/vision.md`) reads a screenshot and returns "a description". `AGENTS.md`'s Mermaid bullet already
requires the opposite of that split: "A `mermaid` fence is un-gated by Spotless, so render it and have the `vision`
subagent read the render — node shapes and edge-label attribution are only visible there", and the same bullet records
that a local `mermaid-cli` render cannot show the host's `viewscreen` toolbar collision, so it must be reproduced on the
live GitHub render. Nothing connects those requirements to the agents: `diagrammer` has no handoff to make, `vision` has
no shape to report in, and `/diagram` has no review step.

Three facts shape this change, and each drives a decision below:

- **The `vision` pin moved three times, and my first diagnosis of it was wrong.** It read
  `opencode-go/deepseek-v4-flash-vision-exp`; then `opencode/deepseek-v4-flash`; then
  `opencode-go/gemini-4-argon-contributor`; and finally `opencode-go/claude-haiku-5-5`, which is what ships. The first
  model is **retired** — DeepSeek's own Vision guide says the name is aliased to the latest Flash — so its endpoint
  answered `Upstream request failed: Endpoint is unavailable.`, and `opencode/deepseek-v4-flash`, which runs, rejects
  images (`read` fails with "does not support image input"). My first reading, that the `opencode-go` provider serves no
  vision model, came from a **name grep** (`opencode models | grep -i vision`) and was wrong: it missed the multimodal
  models the plan does serve. The pin is now `opencode-go/claude-haiku-5-5`, and the fixture review runs as a real
  visual review.
- **The Playwright MCP blocks `file://`.** `browser_navigate` on a `file://` URL fails with
  `Access to "file:" protocol is blocked`, while an `http://127.0.0.1:<port>/…` URL loads the same artifact. A locally
  rendered diagram therefore cannot be inspected from its path; it has to be served.
- **A vision observation is not a measurement.** The `AGENTS.md` vision bullet records the case: the agent "reported a
  diagram label clipped across repeated renders while a measurement found no overflow".

## Goals / Non-Goals

**Goals:**

- `vision` returns a structured visual review — the artifact and scope it inspected; observed facts; visual risks or
  suspected defects; and what the image cannot settle — applying the diagram checks to a diagram render, naming each
  item's visible element and approximate position, and marking a claim that needs a measurement as a hypothesis that
  names its method.
- `diagrammer`'s Mermaid path returns a visual-review handoff beside the source and stops implying its local render
  proves host layout.
- `/diagram` completes the Mermaid path: render, serve, delegate to `vision` with the handoff, inspect a live GitHub
  render where one is reachable, report the two results separately, and state an unavailable live render as a
  limitation.
- The definitions, the command, `AGENTS.md`, `README.md`, and the `agent-skills` spec agree.

**Non-Goals:**

- Changing `diagrammer`'s role or model; `vision` stays a read-only review agent, but its model moved to one that reads
  images (see the Context).
- Letting either subagent edit repository files, or letting `vision` publish a verdict on a measurement it cannot make.
- Adding a Mermaid pitfall catalogue to the agents: the pitfalls stay owned by `AGENTS.md`'s diagram bullet and are
  referenced from the agent, not restated.
- Making a live GitHub render mandatory — an uncommitted diagram has none, which is exactly the case the limitation
  statement covers.

## Decisions

**Strengthen `vision` in place rather than add a second visual agent.** One agent keeps one trigger and one model pin;
the gap is its output shape, not its reach. Alternative: a separate `diagram-reviewer` — rejected: a second definition
duplicating the image-reading mechanism, and the `agent-skills` requirement already names one "visual" agent.

**Give `vision` its own output shape, and keep it outside the shared report contract.** That contract
(`showcase/quality/agent-skills`) is a _findings_ contract — a verdict-first line with class counts, a per-item budget,
a verified-and-set-aside summary — and its bearer list names the review, lesson-capture, and audit agents, not the
visual or diagram ones. `vision` describes an image in three labelled groups, so its shape is stated in the scenario
that specs it. Alternative: make `vision` a contract bearer — rejected: it would demand a verdict line and class counts
from an agent that reports what it can see, and it widens a bearer list this change has no reason to touch.

**State the review path as a sibling scenario beside the Mermaid one, rather than in a requirement about the command.**
No requirement states an OpenCode command's steps — the corpus's command-named requirements are the CQRS command
service's — so what is observable is that the caller renders the source, delegates the render to `vision`, and states
the limitation; that is the new scenario, and the command file is its implementation. Alternative: fold it into the
Mermaid scenario — rejected: the scenario would then describe both the diagrammer's output and the caller's path, and
the two are separately checkable.

**The handoff names the artifact, and `diagrammer` produces both forms of it.** `mermaid-cli` writes one format per
invocation, and the two consumers need different ones: a PNG at the diagram's natural size for the visual review (a
browser scales a served SVG to its viewport, which changes what "unreadably small" means), and the SVG for a measurement
such as `getComputedTextLength()`. Alternative: render only the PNG and re-render an SVG on demand — rejected: it makes
the measurement path depend on remembering to render, for a second render of about two seconds.

**`/diagram` serves the artifact rather than handing over a path.** The Playwright MCP blocks `file://` (verified), so
the render must be reachable at an `http(s)://` URL; the scratch directory is already the agent's approved temp space,
so a one-line static server over it needs no new permission.

**Correct the `vision` model pin's provider, and record why.** The pin named a provider that does not serve the model,
so the agent could not run and this change's required verification could not be performed. Only the provider prefix
changes; the model line, the deliberate `-vision-exp` asymmetry the model-pin bump rule protects, and both agents' roles
are untouched.

## Risks / Trade-offs

- **A longer `vision` definition could make the agent verbose or over-report.** Mitigation: the definition keeps its
  concision clause and its "never report an apparent clipping as confirmed" rule, and the three groups give a suspected
  defect a home that is explicitly not a fact.
- **Serving the artifact adds a step to every Mermaid diagram.** Mitigation: it is one line in the command and needs no
  grant; the alternative — no local visual review — leaves the requirement unimplemented.
- **The structured shape could drift from the scenario.** Mitigation: the scenario states the obligations and the
  definition states the shape, and the shared report contract is deliberately not involved, so there is one owner rather
  than two.
- **The live GitHub render may be unavailable** — an uncommitted diagram has none. Mitigation: the scenario and the
  command both require the limitation to be stated rather than the local render treated as equivalent.

## Migration Plan

Documentation and agent-tooling only; nothing to deploy or roll back.

## Boundary

Sites that still state a fact this change's spec owns, kept deliberately:

- `AGENTS.md`'s vision bullet keeps the pin, the delegation trigger, the `http(s)`/`file://` constraint, and the
  "observation is not a measurement" rule — the imperatives a reader must act on — and points at the definition for the
  report's shape rather than restating its groups.
- `AGENTS.md`'s diagrammer bullet names the handoff and points at the definition for its fields; the enumeration lives
  in the delta and the definition.
- `AGENTS.md` keeps ownership of the rendering pitfalls — the `viewscreen` toolbar, `diagramPadding`, and the
  vertical-layout preference. `diagrammer.md` carries a condensed authoring form of them beside the rule that applies
  them and points at the two `AGENTS.md` bullets that record the mechanics, a deliberate keep-site: the agent needs the
  rule where it draws.
- `README.md`'s `vision` row, `diagrammer` row, and `/diagram` row state the capability for a human reader; they do not
  enumerate the review's fields.

## Claims

Seeded with `scripts/claim-scan.py` over the change's artifacts; each candidate is resolved into a row below or
dismissed by class. A row about an artifact this change edits reads its source at that artifact's pre-change state.

| #   | The change states                                                                                                                                                    | Source / deriving command                                                                                                                                                                                                                                                                                                                                     |
| --- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1   | The pinned DeepSeek vision model is retired, and `opencode/deepseek-v4-flash` rejects images.                                                                        | DeepSeek's Vision guide (the `-vision-exp` name is aliased to the latest Flash); the probe's output (`does not support image input`); `opencode models`.                                                                                                                                                                                                      |
| 2   | The agent on the pin as it read fails with `Model unavailable: opencode-go/deepseek-v4-flash-vision-exp`.                                                            | The `Model unavailable` share of Context's blocked-verification tally.                                                                                                                                                                                                                                                                                        |
| 3   | The pin has read `opencode-go/…` since `vision.md` was introduced.                                                                                                   | `git log --format="%h %ad %s" --date=short --diff-filter=A -- .opencode/agent/vision.md` → `1961f80a 2026-09-04`; `git log -S 'deepseek-v4-flash-vision-exp' -- .opencode/agent/vision.md` → that commit only.                                                                                                                                                |
| 4   | The Playwright MCP blocks `file://` and loads the same artifact over HTTP.                                                                                           | `browser_navigate` to the fixture's `file://` URL → `Access to "file:" protocol is blocked`; to `http://127.0.0.1:8791/fixture.png` → the page loads as `fixture.png (784×118)`.                                                                                                                                                                              |
| 5   | The delta preserves all 18 scenarios and appends one.                                                                                                                | A `git diff --no-index --numstat` and a `difflib` opcode count of the delta's block against `git show HEAD:openspec/specs/showcase/quality/agent-skills/spec.md` → 2 hunks, 3 lines removed and 35 added (the block grows 306 → 338; block content, the trailing blank separator excluded), no `#### Scenario` header removed, order preserved, one appended. |
| 6   | The requirement block is 306 content lines and 18 scenarios at `HEAD`.                                                                                               | `git show HEAD:…/spec.md`, the block's line range, + `grep -c '^#### Scenario'`.                                                                                                                                                                                                                                                                              |
| 7   | `AGENTS.md` requires a Mermaid render to be read by `vision` and records the `viewscreen` toolbar.                                                                   | `git show HEAD:AGENTS.md`, the "A `mermaid` fence is un-gated…" bullet, quoted by text.                                                                                                                                                                                                                                                                       |
| 8   | `AGENTS.md` records the clipping observation a measurement refuted.                                                                                                  | `git show HEAD:AGENTS.md`, the vision bullet, quoted by text.                                                                                                                                                                                                                                                                                                 |
| 9   | `widen-diagrammer-to-mermaid` left live-render verification to the caller, not the diagrammer.                                                                       | `git show 2545eadb:openspec/changes/archive/2026-10-02-widen-diagrammer-to-mermaid/design.md`, its Non-Goals.                                                                                                                                                                                                                                                 |
| 10  | No requirement states an OpenCode command's steps.                                                                                                                   | `grep -rh '^### Requirement' openspec/specs/ \| grep -i command` → eight headers, all naming the CQRS command service.                                                                                                                                                                                                                                        |
| 11  | `mermaid-cli` writes one output format per invocation.                                                                                                               | `npx @mermaid-js/mermaid-cli@11 --help` → `-o` takes one file and `-e` one format.                                                                                                                                                                                                                                                                            |
| 12  | The rendered fixture is 784×118 pixels.                                                                                                                              | `file fixture.png` after the render.                                                                                                                                                                                                                                                                                                                          |
| 13  | The shared report contract's bearer list excludes the visual and diagram agents.                                                                                     | `openspec/specs/showcase/quality/agent-skills/spec.md`, the contract requirement's description, quoted by text.                                                                                                                                                                                                                                               |
| 14  | The corrected pin reaches the provider but its endpoint is unavailable.                                                                                              | The `Upstream request failed` share of Context's blocked-verification tally.                                                                                                                                                                                                                                                                                  |
| 15  | The gates pass: `openspec validate --all` 27/27, `--changes` 1/1, `spotlessCheck` exit 0, `verifyCapturedMarkers` exit 0.                                            | Those commands' own output, run after the last edit to a Spotless-owned file.                                                                                                                                                                                                                                                                                 |
| 16  | The `vision` model's endpoint alone is out: it fails while two other models on the same provider and stored credential, and a model on the Go plan, each reply `OK`. | The four `opencode run` probes, whose commands and results task 5.2 records.                                                                                                                                                                                                                                                                                  |
| 17  | The final pin works: `opencode-go/claude-haiku-5-5` read the fixture render and returned the structured review.                                                      | Task 5.2's fixture review over the served PNG (its Done note records the run).                                                                                                                                                                                                                                                                                |

Dismissals, one reason per kind:

- `[path]` candidates — the files the change edits and the change dir's own artifacts: a path names a file, not a claim
  about evidence.
- `[absolute]` candidates (`only`, `never`, `every`, `all`, `none`) — prose qualifiers in the change's own artifacts,
  each carrying either a normative statement the delta owns or a restatement of a row above; verified line by line.
- `[count]` candidates — a number in the change's own artifacts (a task identifier, a claims-table figure, or this
  unit's round number in the review record), not a claim about evidence.
- `[cross-ref]` candidates — a phrase or identifier inside the change's own artifacts (a construct label, a model id, a
  task's shorthand), not a claim about evidence.
