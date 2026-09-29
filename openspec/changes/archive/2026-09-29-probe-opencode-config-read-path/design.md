# Design

## Context

See `proposal.md` — Why. The `opencode` action (`.github/workflows/opencode.yml`, `anomalyco/opencode/github@latest`)
resolves `releases/latest` and runs the V1 binary; `packages/opencode/src/config/v2-compat.ts` throws `InvalidError`
("V2 permissions are not supported by OpenCode V1 …") when the config carries a `permissions` array, and
`opencode debug config` loads and resolves the config, running that check. The `ci.yml` `build` step already probes the
OpenSpec config's read path with `openspec new change` plus a warning grep; `check`/`workflowLint`/Spotless do not load
`.opencode/opencode.json` with V1.

## Goals / Non-Goals

**Goals:**

- Fail a pull request's required `build` check when `.opencode/opencode.json*` is not loadable by the V1 consumer the
  `opencode` action installs.

**Non-Goals:**

- Validating the config against V2 (the local agent's consumer) — V2 accepts the V1 shape, and the risk is the V1
  consumer rejecting a V2-only key, not the reverse.
- Treating every V1 compatibility warning as a failure — the probe asserts the load, not every diagnostic.
- A home-grown schema or JSON lint — the config-read-path convention probes the consumer rather than a second parser.

## Decisions

### Decision: probe the consumer's own read path with `opencode debug config`

The probe installs the same binary the action installs (the same `curl -fsSL https://opencode.ai/install | bash` line)
and runs `opencode debug config`, the load-only entry point, failing on a non-zero exit. The `permissions` case throws,
so the exit code is the signal; a message grep is deliberately not used, because the resolved output embeds the agent
and command prompts and so could carry the diagnostic text on a loadable config.

- **Alternative — a JSON-schema lint of `.opencode/opencode.json`:** the config-read-path convention rejects a second
  parser, which encodes an assumption about a contract the tool owns.
- **Alternative — run `opencode github run`:** it needs GitHub context and would attempt a session; `debug config` is
  the load-only command.

### Decision: PR-scoped, gated on the config or the probe changing, with a two-dot diff

The probe runs only for `pull_request` events and only when a changed-file check finds `.opencode/opencode.json*` or
`.github/workflows/ci.yml`. A config is caught at the pull request that changes it (main is PR-only via the ruleset, so
it was already probed); including the probe's own definition keeps an edit to the probe from skipping the probe it
defines, so the pull request that changes the step exercises it; the gate keeps the added binary install off runs that
cannot trip it.

The changed-file check compares the two available trees directly (`git diff --name-only FETCH_HEAD HEAD -- …`) rather
than three-dot, because `actions/checkout`'s default `fetch-depth: 1` leaves HEAD a grafted root with no merge base —
`FETCH_HEAD...HEAD` exits 128 and the probe would run unconditionally. Two-dot is a conservative superset (it can run
the probe when the base advanced rather than miss a changed config), so it never skips a config that changed.

- **Alternative — `fetch-depth: 0` on checkout:** full history makes three-dot exact but slows every run's checkout for
  a probe that runs only on config-changing pull requests.
- **Alternative — run it on every `build`:** adds a network install to every pull request and `main` push.
- **Alternative — a separate path-filtered workflow:** a path-filtered job is skipped, not required, so it would not
  block a merge.
- **Alternative — also run on `main`:** redundant — the config was probed in its pull request, and direct pushes are
  blocked.

### Decision: prove the check with a known-good and a known-bad input

The repository's check-verification rule requires the check to fail on a known-bad input and pass on a known-good one.
This pull request's own run exercises the run-and-pass path — it edits the probe's own definition, so the gate includes
it and the step runs against the unchanged, loadable config — but not the known-bad one, which would put a V2-only key
in the repository. The load's fail path is therefore proven before the manual review by installing the V1 binary under a
temporary `HOME` and invoking that binary by absolute path (a bare `opencode` on the host would resolve to V2, which
accepts the `permissions` array) against a copy carrying a V2 `permissions` array, with the repository's config as the
known-good counterpart. The gate's run-and-fail path is proven at the merge stage, after approval, by pushing a
temporary V2 `permissions` key (the step runs and fails) and reverting it, recording each run.

- **Alternative — rely only on the pull request's own run:** it exercises the run-and-pass path but cannot carry the
  known-bad input, so the fail path would be unproven.

### Decision: refresh the docs the change falsifies

The change makes `AGENTS.md`'s "no in-repo gate loads the config with the action's v1 binary" false and its gate
enumeration incomplete, and the README's Continuous Integration section incomplete, so all three are corrected in this
change, keeping the dispatch rule for a behavioural config change (a new grant, model, or server) the load does not
exercise.

- **Alternative — leave the docs:** the absence claim and the enumerations would mislead, which the repository's
  docs-refresh convention and absence-claim gotcha both forbid.

## Risks / Trade-offs

- **An install-script or diagnostic-text change silently weakens the probe** → the probe fails on the process exit, not
  only the message, and the local control in task 4.1 asserts the installed binary's reported version against the tag
  `releases/latest` resolves to, so a drift in which consumer the install line delivers is detected rather than assumed
  away.
- **Network flakiness** (the actionlint 504 precedent) → the probe is gated on a config change, bounding the exposure;
  the install script fetch is a single curl.
- **A V1 compatibility warning that does not fail the load** → the probe asserts the load (exit), not every diagnostic;
  the incident class (a V2-only `permissions` array) throws.
