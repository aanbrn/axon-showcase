## Why

`AGENTS.md` cannot tell which of its rules arrived through a lesson capture rather than through the work itself. A trace
of the current file attributes 669 of its 1630 lines (41%) and 46 of its 142 top-level bullets (32%) to capture commits,
concentrated in Gotchas and Conventions; roughly 26 of those 46 are meta — rules about the agent, the tooling, the
workflow, or the documentation — rather than facts about the product. Nothing in the file marks the distinction, and a
capture's bullets carry no origin, so provenance lives only in `git blame` — which a markdown reflow rewraps and
re-attributes (the Spotless formatting commit rewrote 123 lines of the file while reflowing text it did not author).
Accretion is therefore visible only when a bullet goes stale, never attributable to the capture that added it.

## What Changes

- **Captured rules carry their origin.** Every `AGENTS.md` addition the `lesson-capture` subagent proposes names the
  change that produced it — and the PR at the post-merge capture — in a greppable `captured: <change>` form that
  survives a markdown reflow.
- **The tooling audit reports accretion.** `agents-auditor` gains an accretion section: the rules that are meta rather
  than product, each with the origin that introduced it, and its verdict line names that count alongside its findings
  and advisory counts.
- **The audit's read-list gains the provenance queries.** `git blame` and `git log -S` establish a rule's origin, with
  the in-prose marker as the authoritative source going forward.

## New Capabilities

None.

## Modified Capabilities

- `showcase/quality/agent-skills`: the agent-tooling audit gains an accretion class and its origin reporting; the
  lesson-capture scenario gains the origin-marker obligation; the shared report contract's verdict line names every
  class a report carries.

## Impact

`.opencode/agent/agents-auditor.md`, `.opencode/agent/lesson-capture.md`, `.opencode/commands/audit-agents.md`,
`AGENTS.md` (the capture and auditor conventions), `README.md` (the auditor's description, its agent-table row, and its
slash-command table row), `docs/ideas.md` (the reference sweep), and this capability's spec. No application code
changes.
