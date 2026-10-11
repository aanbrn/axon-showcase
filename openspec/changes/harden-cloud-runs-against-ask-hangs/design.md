# Design

## Context

See `proposal.md` — Why. The action installs v1 (`releases/latest`) and runs `opencode github run`; its handler
(`packages/opencode/src/cli/cmd/github.handler.ts`) denies `question` only and subscribes to no permission event, so any
other `ask` blocks. v2's CLI `run` resolves asks (`packages/cli/src/run/noninteractive.ts` replies to
`permission.asked`) — but the action has no v2 path: `github/action.yml` runs `opencode github run`, and the v2 CLI
ships no `github` command. Upstream marked it `wont_fix` (`anomalyco/opencode#54477`) with the workaround below. The
repo's config is one file shared by the cloud runs and local sessions. Beyond the shared `git push *` config guard, the
binaries' default asks differ: the cloud (v1) binary asks for `read` `.env`, `external_directory`, and `doom_loop`; the
local (v2) binary asks for `read` `.env` only — its `external_directory` and `doom_loop` fall through to the base
`allow`. The `/oc` job carries no timeout — the previous change (`fix-unattended-permission-hang`) declined one
deliberately, on the premise that a human triggers and watches `/oc`; that premise no longer holds, since the residual
asks (`git push *`, an unlisted external-directory path) can still hang a run, and the two `/oc` cancels took 42m and
17m for a human to notice.

## Goals / Non-Goals

**Goals:**

- No default with an expressible `deny` raises an `ask` in a cloud run.
- Bound the `/oc` job.

**Non-Goals:**

- `external_directory: deny`: in the cloud (v1) binary it would override the v1 defaults' scratch/skill/reference allows
  (`<tmpdir>/opencode/*`, the skill and reference dirs), which the config cannot re-express — `expand()` handles only
  `~`/`$HOME`.
- The `git push *: ask` guard: local wants the prompt, and it is bounded.
- An action-path dispatch: the audit exercises none of the changed rules (`read` `.env`, `doom_loop`), so a dispatch
  would prove only that the audit still runs; the resolved-rule check and the CI loadability probe are the verification.
- A CI-only config override: the action exposes no permission input, so a workflow would rewrite the tracked config —
  fragile and invisible to the config-loadability probe.
- Moving the action to v2: no such path exists.

## Decisions

**Deny the ask-defaults whose internal allows are re-expressible (`read` `.env`, `doom_loop`).** `read` of `.env` files
becomes `deny`, re-allowing `*.env.example` — which `*.env.*` would otherwise override, since the config's rules are
appended last — turning an `ask` into a `deny` in both binaries. `doom_loop` becomes `deny`: no default ruleset carries
a `doom_loop` allow, so it turns the cloud (v1) `ask` and the local (v2) `allow` into a `deny`. `external_directory` is
left to its grants. _Alternatives rejected:_ denying `external_directory` (in v1 it breaks the defaults'
scratch/skill/reference allows, which the config cannot re-express); a CI-only override (no action input; fragile,
invisible to the probe); leaving the timeout as the only bound (a hang wastes the full bound and fails the run).

**Bound `/oc` with `timeout-minutes: 120`.** The `audit` job's 60 is ~2x its ~29-minute maximum; `/oc` is
human-triggered and a legitimate change implementation can run longer (healthy observed 14m38s; two cancels at 42m and
17m), so 120 is a hang backstop that does not clip a real run. _Alternative rejected:_ 60 — it could clip a long
legitimate `/oc`.

## Risks / Trade-offs

- In the cloud (v1) the `read` `.env` and `doom_loop` asks become denials; locally (v2) a `.env` read becomes a denial
  (was a prompt) and `doom_loop` a denial (was allowed) → accepted; both are cases an unattended agent should not do,
  and a `.env` holds secrets.
- An unlisted external-directory path still asks → bounded by the job timeouts; the two observed paths are already
  granted.
- The `git push *: ask` guard still asks → bounded by the timeout; the agent is not told to push.

## Migration Plan

None: a config change taking effect at session start, and a workflow bound; the cloud runs read them on their next run.
