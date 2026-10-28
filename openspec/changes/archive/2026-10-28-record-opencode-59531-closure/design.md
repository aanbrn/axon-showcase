# Design — record the closure of `anomalyco/opencode#59531`

## Context

The `hand-over-unfinished-work` change (archived 2026-10-27) wrote the pause rule "a session limitation the agent could
not measure is its own assessment" and cited `anomalyco/opencode#59531` as the upstream report asking for a
remaining-budget signal. The issue closed `not_planned` on 2026-10-27, with the maintainer answering that newer models
get confused by such a figure and that a plugin is the way to get it.

That closure is a trigger to check, not an answer. The rule itself is unaffected: OpenCode still reports no budget to
the agent, so a limitation the agent cannot measure stays its own assessment. What the closure changes is the note
beside the reference — its disposition and its retirement condition — and the work it implies.

The work it implies is a plugin that tells the session its remaining budget. The running runtime's own plugin surface
decides whether that is reachable: ADR-0024 records that the v2 `PluginContext` — the surface the installed binary loads
— exposes `options`, `agent`, `aisdk`, `catalog`, `command`, `integration`, `plugin`, `reference`, and `skill`, with no
tool and no permission domain, while the legacy `Hooks` surface (which v2 does not load) carries `tool`, `chat.message`,
and `experimental.chat.messages.transform`. The SDK reports a session's token totals and cost, but no field reports a
remaining budget, and whether a remainder can be derived from those totals is unverified.

## Goals / Non-Goals

**Goals**

- State the closure's disposition beside the reference it sits on, so a later sweep does not re-derive it.
- Re-derive the reference's retirement condition to one that can fire given the maintainer's answer.
- Give the plugin work a durable home instead of a pull-request body.

**Non-Goals**

- Building the plugin, or claiming it is feasible on a channel this change did not exercise.
- Changing the pause rule, the `agent-skills` requirement, or the weekly report.
- Commenting upstream again: the untested wrapper leaves no failure of the maintainer's direction established, and the
  surface constraint is already recorded in ADR-0024.

## Decisions

### Record the disposition beside the note rather than relaxing the rule

The issue's decline removes the expectation of a harness signal; it does not create one. The rule the note sits on
therefore keeps its text, and the note gains the closure.

- Alternative — relax the rule because upstream declined: rejected. The agent still has no figure to cite, and a report
  calling an unmeasured limitation an exhausted window would remain the defect the rule exists to prevent.

### Re-derive the retirement condition to name both retiring paths

The reference retires when a session can cite a reported figure — from the harness, or from a plugin this repository
ships. "when the harness exposes one" alone was a condition the maintainer has now said may never be met. The rule the
reference sits on outlives it: a limitation the agent cannot measure stays its own assessment, which a citable figure
makes checkable rather than unnecessary.

- Alternative — keep the harness-only wording: rejected. The maintainer names a plugin as the intended path, so the
  harness-only condition is narrower than the mechanism that would actually retire the reference.

### Park the plugin work instead of building it

The parked entry states what the runtime offers and what an implementation would have to settle. On the v2 surface the
only agent-facing channels are a static `agent.transform` prompt — the caching-breaking form the issue's discussion
already cautioned against — and wrapping the model through `aisdk.language`, whose ability to append a reminder to the
latest message without invalidating the cached prefix is unverified. Feasibility is therefore not established in either
direction, which is what the parked entry records.

- Alternative — ship a v1-shaped plugin now: rejected. v2 does not load the legacy shape (the retired
  `grant-cli-config-dirs.ts` plugin is the recorded instance), and the local runtime is v2.
- Alternative — leave the work unrecorded: rejected. `docs/ideas.md` is the durable home `AGENTS.md` names for work a
  change surfaces but does not deliver.

### Declare `skip_specs`

The change edits a note beside an upstream reference in a project-authored skill. The `showcase/quality/agent-skills`
requirement states the behaviour (an unmeasured limitation is an assessment, not an exhausted window) and is unchanged;
`git grep` confirms the requirement names no issue and carries no note to move.

## Claims table

Each row is a fact this change states about its evidence, read at the merge-base `c93607bd` and re-checked at `a4c3cd9e`
after the branch was refreshed, unless the source is the installed package or the binary, which git does not carry.

| Claim                                                                     | Source or command                                                                                                   | Result                                                                                                                                                                                   |
| ------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| The issue closed `not_planned` on 2026-10-27                              | `gh api repos/anomalyco/opencode/issues/59531 --jq '{state, state_reason, closed_at}'`                              | `{"closed_at":"2026-10-27T15:45:53Z","state":"closed","state_reason":"not_planned"}`                                                                                                     |
| The closing comment points at a plugin and cites model confusion          | `gh api repos/anomalyco/opencode/issues/59531/comments`, the `rekram1-node` entry                                   | "weve seen even newer sota models get confused by this, for now if you want it you should write a plugin for it"                                                                         |
| The thread raises prompt caching as a design constraint                   | `gh api repos/anomalyco/opencode/issues/59531/comments`, the `opencode-agent[bot]` entry                            | "a figure that changes every turn should go somewhere that doesn't break prompt caching, such as a read-only tool or a reminder on the latest message, rather than in the system prompt" |
| The skill is the issue's live citation; the archived change is history    | `git grep -n 59531 c93607bd`                                                                                        | `.opencode/skills/pause-resume-unit/SKILL.md:53`, and `archive/2026-10-27-hand-over-unfinished-work/{design,tasks}.md`                                                                   |
| The `agent-skills` requirement states the rule and cites no issue         | `git grep -n "exhausted context window" c93607bd -- openspec/specs`                                                 | requirement body line 2280 and scenario line 2310                                                                                                                                        |
| The current note reads "retires when the harness exposes one"             | `git show c93607bd:.opencode/skills/pause-resume-unit/SKILL.md`                                                     | lines 51–54 carry the clause this change rewrites                                                                                                                                        |
| v2's `PluginContext` names no tool and no permission domain               | `docs/adr/0024-*.md`, Context, which lists the domains it exposes                                                   | `options`, `agent`, `aisdk`, `catalog`, `command`, `integration`, `plugin`, `reference`, `skill`                                                                                         |
| The `aisdk` hook receives a request's model                               | the installed `@opencode-ai/plugin` typings, `dist/v2/effect/aisdk.d.ts`, read in 1.19.4 and 1.19.3                 | its input carries `model`, `sdk`, `options`, and an optional `language: LanguageModelV3`                                                                                                 |
| The legacy `Hooks` surface carries `tool` and a message transform         | the installed `@opencode-ai/plugin` typings, `dist/index.d.ts` (`Hooks`), read in 1.19.4 and 1.19.3                 | `tool`, `chat.message`, `experimental.chat.messages.transform`, `tool.execute.after`, `permission.ask`                                                                                   |
| The SDK reports a session's token totals and cost, not a remaining budget | the installed `@opencode-ai/sdk` typings, `dist/v2/gen/types.gen.d.ts` (`SessionV2Info`), read in 1.19.4 and 1.19.3 | carries `tokens {input, output, reasoning, cache{read, write}}` and `cost`; no field reports a remainder                                                                                 |
| The local runtime is v2                                                   | `opencode --version`                                                                                                | `opencode v2.2.0`                                                                                                                                                                        |
| The plugin pin the base revision tracks is 1.19.4                         | `git show c93607bd:.opencode/package.json`                                                                          | `"@opencode-ai/plugin": "1.19.4"`, a pin the binary rewrites at startup, so it is not the installed version                                                                              |
| A closed reference renders its disposition                                | `git show c93607bd:build-logic/src/main/kotlin/UpstreamReferenceState.kt`                                           | KDoc states `closed (<state_reason>) <YYYY-MM-DD>`, rendered verbatim                                                                                                                    |
| v2 loads only `{ id, effect }` or `{ id, setup }`, not the legacy shape   | `docs/adr/0024-*.md`, Context, and `AGENTS.md`'s retired-plugin note                                                | a legacy-shaped module fails to load (`Plugin must export a default definition with an id and an effect or setup function`)                                                              |

### Dismissals

The rows above record the claims this change makes about its evidence; the classes `scripts/claim-scan.py` reports
beyond them are dismissed for one reason each.

| Class       | Why it needs no row                                                                                                                                                                                                                                   |
| ----------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `path`      | Each candidate is a file path, an `owner/repo#NNN` reference, or a command fragment the sentence around it resolves; each path whose state a claim rests on has a row above.                                                                          |
| `count`     | No candidate is a tally over a corpus: each is a line number or threshold inside an instruction, a task ordinal, a record field, or a field of the review ledger's row.                                                                               |
| `absolute`  | No candidate is a universal claim about the repository or its corpus: each is a hedge, a section marker, a statement about the unit's own artifact, a verbatim quotation from the tracker, or the runtime channel or loader claim whose row is above. |
| `cross-ref` | The candidates are the `repos/…/issues/…` fragments inside the `gh api` invocations and the process-defect record's own `<unit>#<n>` identifier, not claims about another artifact.                                                                   |

## Risks / Trade-offs

- **The parked entry rests on a surface this change did not exercise.** Accepted and stated in the entry: neither the
  static prompt nor the model wrapper was tested, and the reopening condition names the probe that would settle the
  wrapper.
- **The note grows by one clause in a skill loaded on each pause and resume.** Accepted: the clause states a disposition
  a later sweep would otherwise re-derive from the tracker.
- **The plugin figure could still confuse a model, as upstream cautions.** Accepted as the entry's own caveat: any
  implementation must be measured against that concern rather than assumed helpful.
