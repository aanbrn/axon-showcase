# Verify the model picker before an Air unit session's first request

## Why

A unit's model is pinned in `.opencode/opencode.json`, but JetBrains Air carries a per-session model picker over the
agent's advertised list, and an explicit pick wins: the agent's `session/new` still advertises the pinned default while
the picker showed another model. Nothing reads the model a session runs on — `check`, the pre-commit guard and CI all
pass — so a session can run on a model the repository did not choose, with nothing to notice it, and the picker can
change between sessions with no repository change.

## What Changes

- **`.opencode/skills/setup-agent-tools/SKILL.md`**, the Air section — a best-effort check: the skill is loaded on a
  setup request and from `AGENTS.md`'s Air pointer at session selection, not automatically in every session:
  - when this guidance is loaded, check the model before continuing substantive work;
  - `IF` trustworthy runtime model information is available, `THEN` compare it with the selected agent's configured
    model; `OTHERWISE` ask the owner to confirm the picker;
  - on a mismatch, ask the owner to restore it or confirm the override, unless a verified switching mechanism is
    available;
  - recheck after switching agent or model.
- **The same section** points at `AGENTS.md`'s existing default-versus-session caveat rather than restating it.
- **The same section** names no model identifier: the pin's value lives in `.opencode/opencode.json`.
- **The same section** makes no enforcement claim: it calls the check a precaution, because no project-level lock on a
  host's picker is established by the evidence.
- **`README.md`**, the Parallel Units section: one sentence telling a person starting a unit in Air that its picker may
  override the pinned model, so they check it before submitting the first prompt and after changing agent or model — the
  agent cannot see the picker, only they can.

### New Capabilities

None.

### Modified Capabilities

None. No capability spec covers the setup skill or the model pin's session-selectability
(`grep -rn "setup-agent-tools" openspec/specs/` returns no hits, and the model pin is `AGENTS.md` guidance), so it is
`skip_specs: true`.

## Impact

- **Files**: `.opencode/skills/setup-agent-tools/SKILL.md`, `README.md`, and the change's own artifacts.
- **Code, guard, build**: none. The check is a precaution, not a lock: no supported v2 mechanism to veto a model
  selection has been demonstrated, and a commit-time refusal would land after the off-pin spend while proving no
  attribution.
- **Verification**: the skill's text is read back against its version boundary and the `AGENTS.md` caveat, and the
  README sentence against its section; `check` and `openspec validate --all` pass; neither text gains a model
  identifier.
