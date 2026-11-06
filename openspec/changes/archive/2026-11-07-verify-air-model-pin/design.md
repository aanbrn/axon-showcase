# Design — verify the Air model picker before the first request

## Context

- The repository pins the cheap flash model in `.opencode/opencode.json` (`model` and `small_model`), in the
  flash-pinned subagent frontmatter, and in the workflow's `model` input. `AGENTS.md`'s model-pin bullet already records
  that the config key is a default for **new** sessions rather than a live override, and that OpenCode persists the
  last-used model for the terminal.
- JetBrains Air adds a **per-session picker over the agent's advertised model list**. A probe of the ACP surface shows
  the agent still advertises the pinned default (`currentValue: opencode-go/deepseek-v4.1-flash`) while offering 394
  selectable models, among them `openrouter/google/gemini-omni-1.2-flash`; the owner's Air panel displayed that model.
  The picker's pick is the session's model: an ACP `session/set_config_option` on the `model` option applies the value
  and reports it back as the option's `currentValue`.
- Nothing reads the model a session runs on: the pre-commit guard and the root build contain no model read, and CI runs
  in its own host. A session can therefore run on a model the repository did not choose, with nothing to notice it — a
  resource concern, not a correctness defect.

## Decisions

### D1 — Two halves, at the two points the model is visible

- The human's half is a pre-prompt check: the person starting a new or resumed session checks the host's picker before
  submitting the first prompt, and rechecks after changing agent or model. Only they see the picker before the session
  exists, and the README is where a person starting a unit reads.
- The agent's half is best-effort and runs **when this guidance is loaded**, not before every request: the skill is
  loaded on a setup request and from `AGENTS.md`'s Air pointer at session selection, not automatically in each session,
  so the check happens before continuing substantive work at that point.
- If trustworthy runtime model information is available, the agent compares it with the selected agent's configured
  model; otherwise it asks the owner to confirm the picker.
- On a mismatch the agent asks the owner to restore the model or confirm the override, unless a verified switching
  mechanism is available.
- The skill stays out of the lifecycle's Begin step: Begin establishes the workspace, and the model is chosen and can
  change after that point, including on a resumed session.
- It stays out of `AGENTS.md`: the root's model-pin bullet already states the default-versus-session caveat, and a
  second root bullet would grow the always-loaded file for a host-specific check.

### D2 — No plugin bridge and no commit gate

- The installed plugin package (1.19.4) declares `chat.params`, `shell.env`, and `experimental.chat.system.transform` on
  the legacy `Hooks` interface, while the v2 promise and effect contexts declare different domains;
  `docs/adr/0024-tree-write-right-is-not-session-bound.md` records the same contract split. Relying on those hooks in a
  v2 plugin would need its own versioned probe, and no supported v2 mechanism to veto a model selection has been
  demonstrated.
- Even with a working attribution bridge, a commit-time refusal lands **after** the off-pin requests have spent the
  quota, and the variable can be absent, removed, or stale across a mid-session model switch. It would be a partial
  detector, not enforcement.
- A script reading the database's `modelID` values is diagnostic; it cannot prevent the spend and no recurring detector
  is justified by the cost.

### D3 — A precaution with a stated boundary

- No project-level model lock in the host is established by the evidence, so the guidance says "verify and restore or
  ask" and never describes the check as enforcement.
- An intentional owner override is a resource-choice decision: the check asks rather than forbids, and work already done
  under another model is not discarded or regenerated on that basis.

### D4 — The wording references the existing caveat and repeats no model identifier

- The pin's value lives in `.opencode/opencode.json`; the default-versus-session caveat lives in `AGENTS.md`. The skill
  points at the caveat and names the configured model without restating its id, so a pin bump does not stale the skill.

## Non-goals

- Changing the pin, the subagent pins, or the workflow's model input.
- A plugin, an environment-variable bridge, a commit gate, or a database detector.
- Changing the lifecycle's Begin step or the `AGENTS.md` model-pin bullet.
- Establishing a host-level model lock; the evidence shows none.

## Claims table

| #   | Claim                                                                                                                                                                                                       | Source                                                                                                                           |
| --- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------- |
| 1   | `.opencode/opencode.json` sets `model` and `small_model` to `opencode-go/deepseek-v4.1-flash`                                                                                                               | `python3 -c` reading the file's JSON                                                                                             |
| 2   | A fresh ACP `session/new` advertises the pinned `currentValue` among 394 options, including `openrouter/google/gemini-omni-1.2-flash`                                                                       | the `opencode acp` probe (`initialize`, `session/new`) in this tree                                                              |
| 3   | Air's panel displayed `openrouter/Gemini Omni 1.2 Flash`                                                                                                                                                    | the owner's report; not independently verified                                                                                   |
| 4   | OpenCode's database records `providerID`/`modelID` on assistant messages                                                                                                                                    | `sqlite3 ~/.local/share/opencode/opencode.db "SELECT session_id, json_extract(data,'$.modelID') …"`                              |
| 5   | The installed `@opencode-ai/plugin` 1.19.4 declares the three hooks on `Hooks`, while `dist/v2/promise/context.d.ts` declares different domains; no supported v2 model-veto mechanism has been demonstrated | the primary's `.opencode/node_modules/@opencode-ai/plugin/dist/` (installer state) and ADR-0024                                  |
| 6   | `AGENTS.md` records the config `model` key as a new-session default, not a live override                                                                                                                    | `AGENTS.md`'s OpenCode model-pin bullet                                                                                          |
| 7   | No capability spec covers the setup skill                                                                                                                                                                   | `grep -rn "setup-agent-tools" openspec/specs/` returns 0 hits                                                                    |
| 8   | The pre-commit guard and the root build read no session model                                                                                                                                               | `grep -n "model" scripts/commit-hygiene.py build.gradle.kts` returns no match                                                    |
| 9   | An ACP client sets the session's model with `session/set_config_option` (`configId: model`), and the result reports the new `currentValue`                                                                  | the `opencode acp` probe: set to `openrouter/google/gemini-omni-1.2-flash` then back to the pin, each reported as `currentValue` |
| 10  | The harness's session context names the model the agent runs on                                                                                                                                             | the agent's own session context (environment-supplied, not repository-evidenced)                                                 |
| 11  | The setup skill is loaded on a setup request and from `AGENTS.md`'s Air pointer at session selection, not automatically in every session                                                                    | the skill's frontmatter `description` (its trigger) and `AGENTS.md`'s Air bullet                                                 |

## Dismissals

`python3 scripts/claim-scan.py --base origin/main` seeds the table above; its candidates are accounted for by kind:

- **`[path]`** — each candidate is a coordinate a sentence names, resolved by opening it or re-running the row's
  command.
- **`[count]`** — each count is a value a row sources from its command, or a coordinate into the unit's own records.
- **`[absolute]`** — each limit or absence is the subject of the decision that states it, bounded by the row or artifact
  it names.
- **`[cross-ref]`** — each candidate is a repo path, a PR or issue reference, a git ref, or a root document, resolved by
  opening or resolving its target.

## Verification

- Read both texts back: the skill's loaded-guidance trigger, the runtime-information fallback, the mismatch branch, the
  recheck, the caveat pointer, and the boundary sentence; the README's one sentence in Parallel Units.
- No model identifier appears in the added text:
  `git diff HEAD -- .opencode/skills/setup-agent-tools/SKILL.md README.md | grep -c "deepseek\|gemini\|openrouter"`
  returns 0, after the same command is shown to return 1 with a `gemini` line planted and staged in `README.md` (then
  reverted). The index comparison must be against `HEAD` because the unit's paths are staged; `grep -c` exits 1 when the
  count is 0, which is the expected success case.
- `./gradlew spotlessApply`, then `spotlessCheck`; `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`;
  `openspec validate --all`.
- Re-run the claims rows after the last edit; delete any row that no longer reproduces.
