## Context

See proposal.md — Why. Today `scripts/setup-idea.sh` finds the IDE launcher, aborts if the IDE is running, runs
`installPlugins` for `palantir-java-format` and `com.facebook.ktfmt_idea_plugin`, copies three templates from
`config/idea/` to `.idea/` **only when the target is absent**, and upserts the test-tier naming inspection via
`scripts/ensure-idea-inspection.py`. The four artifacts differ: `palantir-java-format.xml` and `ktfmt.xml` are
single-`<component>` plugin-owned files; `codeStyles/codeStyleConfig.xml` is an IDEA-owned single component; and
`inspectionProfiles/Project_Default.xml` is an IDEA-owned file holding every inspection (already merged, not copied).
`.idea/` is git-ignored by spec, so there is no `git restore` path — `config/idea/` is the committed source of our
settings.

IntelliJ's live settings are reachable programmatically (`CodeStyleSettings.getCustomSettings(Class)`,
`InspectionProjectProfileManager.getCurrentProfile()`, `PluginManager.enablePlugin`/`disablePlugin`), and so — via the
Steroid MCP's `steroid_execute_code` — from the agent while the IDE runs (probed read-only).

## Goals / Non-Goals

**Goals:**

- Re-running the setup repairs a drifted configuration instead of silently leaving it stale.
- Applying the configuration does not depend on the IDE being closed.
- Re-applying after drift is one convenient step.

**Non-Goals:**

- Extending the setup to the web module (Prettier / TS code style / Vitest naming) — a follow-up change.
- Building the setup on the Steroid MCP (see D4).
- Making the configuration durable by versioning `.idea/` files — that conflicts with `ide-config`'s "configuration is
  not versioned" requirement.

## Decisions

**D1: Merge/upsert our elements into IntelliJ's files, rather than overwrite them with the templates.** Overwriting a
whole file drops IDE- or plugin-managed content (options a newer plugin added; a user's other inspections). The existing
inspection upsert proves the pattern; generalize it — renaming `scripts/ensure-idea-inspection.py` to
`scripts/ensure-idea-settings.py` — so every artifact is reconciled by upserting our `<component>`/`<option>`/inspection
block and preserving the rest. `config/idea/*.xml` remains the source of our components. _Alternatives considered:_ keep
the `cp` overwrite (rejected — destructive, and cannot repair a partially-rewritten file); version the managed `.idea`
files (rejected — it contradicts the spec's "not versioned" requirement and reintroduces churn); make the configuration
_survive_ by design (an `.idea` scheme referenced by name, a shared-workspace XML, or a re-import hook — rejected
because the scheme/workspace variants require tracking `.idea` files, which the spec forbids, and the hook is
IntelliJ-version-specific, so this change guarantees _repair on re-run_ instead).

**D2: Split the flow — configuration always, `installPlugins` only when the IDE is closed.** The current
abort-before-copy ordering is a bug: applying the configuration needs no closed IDE. When the IDE is running, apply the
configuration and warn that the plugin install was skipped (with a retry hint).

**D3: Keep `scripts/setup-idea.sh` as the primary, token-free path; add an agent `/setup-idea` command as an optional
convenience.** Reapplying after drift is a mechanical, frequent operation a contributor can run directly
(`./scripts/setup-idea.sh`) without spending agent tokens, so the script stays the documented path and its output should
be explicit about what it restored, left unchanged, or skipped. The agent command (mirroring `/setup-agent-tools`) adds
the interactive value the script cannot — detecting state, asking the user to quit IntelliJ when a plugin install is
needed, retrying, and explaining the restart — for those who prefer to ask.

**D4: Do not build the setup on the Steroid MCP.** `steroid_execute_code` can read and mutate code-style settings and
inspection profiles live, and having IntelliJ write its own files would sidestep merge-vs-overwrite entirely. But it
cannot install plugins (that needs the closed-IDE `installPlugins` CLI plus a restart), it uses internal,
IDEA-version-fragile APIs, and it requires a running IDE _and_ the optional Steroid bridge — so a fresh clone, CI, or a
non-IDEA contributor could not use it. Steroid stays an optional convenience, never the mechanism.

## Risks / Trade-offs

- [Merge code is more involved than a copy, and must cope with IntelliJ's XML quirks] → restrict the operation to
  upserting our element(s) by `name` and preserving the rest of the file; test against both the templates and a drifted
  file.
- [A future IntelliJ or plugin version changes a file's shape] → a merge tolerates unknown content (upsert our element,
  keep the rest), where an overwrite would either clobber it or be ignored.
- [IntelliJ only re-reads `.idea` at startup, so applying the configuration while the IDE runs has no visible effect
  until a reload] → the setup tells the user to apply it with **File → Reload All from Disk** (or a restart); the merge
  itself is unaffected.
- [The configuration can drift — never applied cleanly, or an IntelliJ/plugin version rewrites a file] → the merge
  upserts our element into whatever file exists, so drift is repairable regardless of how it arose.
