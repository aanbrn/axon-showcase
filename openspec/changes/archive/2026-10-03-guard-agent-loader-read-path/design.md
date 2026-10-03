# Design

## Context

See `proposal.md` — Why. `.github/workflows/ci.yml`'s "Probe the OpenCode config's V1 read path" step installs the
binary the `opencode` GitHub action resolves (`releases/latest` — the install script fetches
`releases/latest/download/…`, currently `v1.18.34`; the `V1` in the step name is that line) and runs
`opencode debug config`, failing on a non-zero exit. It runs only for `pull_request` and only when a changed-file check
finds `.opencode/opencode.json*` or `.github/workflows/ci.yml`.

Probing the pinned consumer (`v1.18.34`) against deliberately malformed definitions established the failure surface:

| Definition / malformation                                                                                       | Load   | Observable                                                                                                    |
| --------------------------------------------------------------------------------------------------------------- | ------ | ------------------------------------------------------------------------------------------------------------- |
| agent, `description` a multi-line plain scalar with a buried `: ` (the #477 shape)                              | exit 0 | present, but `mode`/`description` dropped — `debug agent <name>` reports `mode: "all"` instead of `subagent`  |
| command, same shape                                                                                             | exit 0 | present, but `description` dropped (the entry has only its `template`)                                        |
| skill, same shape                                                                                               | exit 0 | **absent** from `opencode debug skill`                                                                        |
| agent or command, `description` resolving to a YAML collection (e.g. `description:` then one `Key: value` line) | exit 1 | `Error: Configuration is invalid at …: SchemaError: Expected string or undefined, got {…} at ["description"]` |

The distinction is the resolved YAML, not the definition kind — for identical content, agent and command return the same
exit status. A `description` that resolves to a collection makes the load fail (exit 1); the #477 multi-line plain
scalar folds into a degenerate value and the loader silently drops the metadata (exit 0) — for a command as much as an
agent, and it removes a malformed skill from the inventory outright. `opencode debug config` resolves the `agent` and
`command` maps, and `opencode debug skill` lists the skills; the silent drops carry neither a debug log line nor a
stderr warning (all exit 0 with empty stderr). The description assertion below covers all three definition kinds
uniformly, so every silent degradation is caught; the exit-status check is kept for the collection case the load
rejects.

The incident this guards is `fix-readme-auditor-frontmatter` (#477): a `.opencode/agent/readme-auditor.md` whose
`description` carried an unquoted `: ` left `/audit-readme` unspawnable, and that pull request did not touch
`opencode.json` or `ci.yml`, so the probe never ran.

## Goals / Non-Goals

**Goals:**

- Fail a pull request's `build` check when a project agent, command, or skill definition fails to reach the loader's
  resolved inventory intact — covering the silent drops the exit-status check cannot see.
- Run the probe whenever a pull request changes the OpenCode surface, so a definition edit exercises it.

**Non-Goals:**

- Validating definitions against V2 or the schema at large — the probe reads the consumer's own resolution, per the
  config-read-path convention.
- A second YAML/frontmatter parser — the loader owns that contract.
- Moving the probe into `check` — the opencode binary is not a build prerequisite, so it stays a CI step.

## Decisions

### D1: Assert the resolved inventory (presence + non-empty `description`), not the load alone

For every `.opencode/agent/*.md` and `.opencode/commands/*.md` on disk, the resolved `opencode debug config` must
contain the matching `agent`/`command` entry with a non-empty `description`, and for every `.opencode/skills/*/SKILL.md`
the resolved `opencode debug skill` must list it — read **file-driven**, so a definition the loader drops entirely fails
rather than escaping an entry-driven loop. `description` is the field the incident's parse failure dropped, it is
present on every current project definition, and OpenCode surfaces it for all three definition kinds, so a silently
degraded definition (agent or command fields dropped, skill absent) fails while a healthy one passes. The existing
exit-status check is kept alongside it for the case the load rejects — a `description` that resolves to a YAML
collection, or a V2-only `opencode.json` key.

- **Alternative — assert the `mode` for agents:** catches the agent case but bakes in "every agent is a subagent", which
  a future primary agent would falsify; `description` is the general signal.
- **Alternative — diff each entry against the file's frontmatter with our own parse:** the config-read-path convention
  rejects a second parser of a tool-owned format.
- **Alternative — assert only counts/names:** the malformed agent keeps its name and count, so this would not catch it.

### D2: Widen the changed-path gate to all of `.opencode/`

The probe runs when the changed-file check finds `.opencode/` (any tracked path under it) or `.github/workflows/ci.yml`
— so an edit to an agent, command, or skill (or the generated `opsx-*`/`openspec-*`/vendored `axon4to5-*` files) runs
it, not only a change to `opencode.json`. Keeping `ci.yml` in the gate keeps the probe's own edit self-exercising.

- **Alternative — enumerate `.opencode/agent/** .opencode/commands/** .opencode/skills/**`:** equivalent but a list that
  must track a new definition kind; the directory prefix cannot go stale.
- **Alternative — run on every pull request:** adds a network install to every PR; the directory gate bounds it.

### D3: Parse with `python3` (stdlib `json`)

The step parses the two JSON outputs with the runner's `python3` (GitHub-hosted `ubuntu-latest` provides it, and the
repository already documents Python 3 as a prerequisite elsewhere).

- **Alternative — the `jq` binary:** also preinstalled, but the repository's existing `--jq` usages are `gh`'s built-in
  filter, not the standalone binary, so `python3` is the better-evidenced dependency.

### D4: Prove the check with a known-bad and a known-good input under the pinned consumer

The check-verification rule requires a known-bad input to fail and a known-good one to pass. The known-bad inputs are
the three silent-shape definitions the Context table names and task 1.4 seeds — the malformed agent, command, and skill
— and the known-good is the repository's unchanged inventory. The local control runs the pinned consumer (`v1.18.34`,
installed under a temporary `HOME` and invoked by absolute path) against both; the pull request's own run exercises
run-and-pass; the run-and-fail path in CI is proven at the merge stage by temporarily committing a malformed definition,
observing the probe fail, and reverting it.

### D5: Refresh the docs the change falsifies

README's Continuous Integration paragraph and `AGENTS.md`'s `ci.yml` gate bullet both state the probe's scope as the
config and its trigger as `opencode.json`/`ci.yml`; both are corrected to the inventory scope and the `.opencode/`
trigger. The config-read-path gotcha is corrected twice: its "take the signal from the process exit status" clause gains
its blind spot (a silently-degraded definition passes the load), pointing at the inventory assertion, and its "no gate
validates that frontmatter" clause — now false — names the gate that does. The scratch-files bullet's "the `build`
gate's OpenCode config probe only loads the configuration" clause now also names the definition inventory (its "never
invokes `openspec`" half still holds).

## Risks / Trade-offs

- [The probe installs a binary over the network on more pull requests] → the gate is bounded to `.opencode/` changes and
  the install is a single curl; a transient failure retries nothing, but the existing actionlint step shows the
  mitigation pattern if flakiness appears.
- [A definition legitimately omits a `description`] → the check fails; every current agent/command/skill carries one and
  OpenCode surfaces it, so the invariant is deliberate (and the failure message names the definition).
- [The pinned consumer's resolution output changes shape] → the check reads a named field (`agent`/`command` maps,
  `debug skill` list) and fails loudly on a missing one rather than passing silently; the field is verified against the
  pinned version in the local control.
- [A malformed definition is committed to `main` directly] → direct pushes are blocked by the ruleset, and the probe ran
  in that change's pull request.

## Migration Plan

CI/workflow only; no data or API migration. Rollback is reverting the workflow edit. The probe's first real run-and-fail
is the merge-stage control described in D4.

## Open Questions

None.
