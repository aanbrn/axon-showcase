# Proposal

## Why

The `technical-advisor` definition and the `/consult` command justify the handoff's excerpts with "because you have no
shell under OpenCode V2", and that reason is true for what it covers: no shell means no `git`, so history, diffs, and an
earlier revision must travel. What neither artifact states is which paths the advisor's file tools reach. Probes settled
that — reads inside the session's workspace succeed, and a path outside it is refused as `external_directory`, the
primary checkout and sibling worktrees among them — so a caller reading the old text inlines what the advisor could have
read and checked itself, against a definition that requires it to "read the evidence yourself rather than accepting the
caller's summary as proof".

## What Changes

- **`.opencode/agent/technical-advisor.md`** — the operational paragraph now names both limits on the advisor's
  evidence: no shell, and file tools that stop at the session's creation workspace, where a path outside it is refused
  as `external_directory` unless a configured grant allows it. Its four bullets state what that leaves to the caller —
  the excerpts, git history, diffs, and earlier revisions — and keep the two imperatives the old text carried. The
  handoff's excerpt field states the duty without giving the absence of a shell as its reason.
- **`.opencode/commands/consult.md`** — step 3's matching field keeps the caller's action and points at the definition
  instead of stating the boundary a second time, and step 4 states that a continued session keeps the workspace it was
  created in.
- **No permission widening**, and no change to `AGENTS.md`'s technical-advisor bullet or to `openspec/config.yaml`: the
  fix is the statement, not the sandbox (see the design's alternatives).

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None. `showcase/quality/agent-skills`' "The technical advisor is read-only" requirement states the split this change
keeps — "It SHALL rely on its read tools and the handoff for history and diff excerpts" — and its "The handoff supplies
history and diff context" scenario gives the no-shell reason for the handoff's hunks. This change keeps both and adds
which paths the read tools reach: a mechanism whose alternatives (a permission grant, or the same sentence placed
elsewhere) leave those outcomes unchanged, so no requirement or scenario moves. The capture's `review-gate` step
correction lands under that skill's requirement, which mandates a step correction in the skill. `skip_specs: true`
records the judgement.

## Impact

- **Consultations**: a caller stops inlining what the advisor can read, so the handoff shrinks and the advisor can
  verify a claim against the file itself instead of against the caller's excerpt.
- **Sandbox**: unchanged. The advisor still cannot read the primary checkout or a sibling worktree, and that remains a
  deliberate boundary rather than an accident.
- **Docs**: the boundary appears at the site that owns it, and the no-shell reason stays where it is true — the
  handoff's history and diffs.
