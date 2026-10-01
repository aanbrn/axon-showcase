# Tasks

## 1. Correct the global-config instructions

- [x] 1.1 `.opencode/skills/setup-agent-tools/SKILL.md`: for both the GitHub and the Steroid step, add the entry to the
      flat `mcp` map of the global config (via `--global` if `opencode mcp add` is used, then move the entry out of the
      `servers` key) and drop the stale "writes the global config" / "not shown in `--help`" claims. Update the Notes
      fallback (`mcp.servers` → the flat `mcp` map) and add the offer to flatten an existing `mcp.servers` nesting, with
      the `anomalyco/opencode#49904` retire-when. Verify by reading the file: no instruction tells the reader to write
      `mcp.servers`, and the fallback names the flat map. — verified: every `grep -nF 'mcp.servers'` hit is a warning
      against the nesting or the repair offer, none an instruction to write it; both steps and the Notes lead with the
      flat map.
- [x] 1.2 `README.md`: the global-config snippet and its sentence — the GitHub server goes under the flat `mcp` map of
      the global config. Verify with `grep -nF 'mcp.servers' README.md`: every hit is a warning against the nesting,
      none an instruction to write it. — verified: the only hits are `:348-349`, both warnings (a plain
      `grep 'mcp.servers'` also matches the `[Tooling MCP Servers](#tooling-mcp-servers)` anchor, since `.` matches `-`;
      use `-F`).
- [x] 1.3 `AGENTS.md`: the tooling-setup bullet (`opencode mcp add <name> -- <command…>` → the `--global` form plus the
      flat shape) and the config gotcha (extend the V1-shape requirement to the global config, naming the 1.x/ACP
      consumer) and the `mcp add` probe passage in "A CLI's `--help` and its docs are not its contract" (defaults to the
      project config; the `-- <command>` form _is_ listed in 2.0.21's help). Verify with `grep -n 'mcp add' AGENTS.md`
      and by reading each corrected sentence. — verified: all three sentences corrected (the tooling bullet, the
      V1-shape note in the config gotcha, and the `mcp add` probe passage of the CLI-`--help` gotcha).
- [x] 1.4 The permission-proof `debug config` gotcha's normalized-document paragraph — re-probe the claim under the
      local 2.0.21 (`opencode debug config`, real `HOME`, grep only the relevant keys — do not print the document, which
      can carry credentials) and reconcile it with this change's rule: the paragraph calls the flat `mcp` form "legacy"
      and `mcp.servers.<name>` "native", while this change keeps the flat form, and its `disabled: false` detail is
      pinned to v2.0.15. Either re-label the characterization and re-state the observation for 2.0.21, or record in the
      design why the label stays accurate. Verify by reading the corrected sentence against the probe's output. —
      verified: 2.0.21's normalized document lifts the flat entry into `mcp.servers.<name>` with **no** `disabled` key
      materialized (`'disabled' in cfg` is false for all four servers) and masks the credential value
      (`GH_TOKEN: MASKED(asterisks)`); the passage now states both and drops the "legacy/native" labels.
- [x] 1.5 Prove the documented shape loads under both lines: build the skill's shape in a scratch `HOME`
      (`/private/var/folders/.../T/opencode/...`, flat `mcp` + `permission`), run the 1.x ACP binary against it
      (`<bin> acp < <(sleep 6)`, background + liveness check) and confirm it starts, and run `opencode debug config`
      with the real `HOME` to confirm 2.x loads it; record both outputs. (The control — the same 1.x binary against an
      `mcp.servers` config exiting 1 — is the evidence already recorded in `design.md`.) — verified: 1.x stays alive
      after 5s against the documented shape (scratch `HOME`), and 2.x's `opencode debug config` exits 0 against the real
      global config, which is that shape.

## 2. Close-out

- [x] 2.1 Run `./gradlew spotlessApply` after the last edit to a Spotless-owned file, then
      `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`; confirm green. — verified: `spotlessCheck` and the
      Docker-free `check` are green after the final edits (BUILD SUCCESSFUL).
- [x] 2.2 Run the `lesson-capture` subagent over the diff, review findings, and change dir; apply its durable
      `AGENTS.md` proposals and record the applied net `AGENTS.md` delta on this task. — applied: two merged rules — the
      upstream-reference bullet gains the monitored-corpus requirement (a close-out reference cited only in a
      skill/definition/code comment is invisible to `upstreamReferences`, whose scan set is `AGENTS.md`, `README.md`,
      `docs/ideas.md`, `docs/adr/**`), with a pointer from the "Report an upstream gap" bullet; and the CLI-probe bullet
      gains the probe-write hazard (a probe whose default target is the repo runs from a scratch working directory, with
      `git status` after). The capture's proposed dedup was applied too: the tooling bullet's rationale moved to the
      V1-shape note, which keeps the `anomalyco/opencode#49904` citation in a scanned file. Three further candidates
      were already covered (the duplicate check, the `-F`/anchor rule, the point-in-time-snapshot rule) and were not
      added. Measured net for the capture's edits: **+8 AGENTS.md lines** (its hunks `+1/+0/+1/+3/+3`).
