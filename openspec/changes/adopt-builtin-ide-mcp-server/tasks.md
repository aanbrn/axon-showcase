# Tasks — adopt the IDE's built-in MCP server

The unit's record is `docs/units/adopt-builtin-ide-mcp-server/unit.md`.

## 0. The pre-write pass

- [x] 0.1 The review gate's pre-write derivation pass ran, seeded with
      `python3 scripts/claim-scan.py --base origin/main` over the change's artifacts; `design.md` carries the claims
      table and the per-kind dismissal line beside it. Verify: every scanned candidate is a row or is covered by its
      kind's dismissal, and each row's command reproduces its figure. Done: the scan seeded the table; its candidates
      are accounted for by the rows or the four kind-dismissals. Rows 11–19 were re-run and reproduce; rows 1–10 rest on
      IDE-side probes whose outputs the rows quote, re-checked at 6.3.

## 1. The root guidance

- [x] 1.1 `AGENTS.md`, the "IDE inspections (optional)" bullet: replace the Steroid sentence — the optional pass through
      `steroid_execute_code` and its `runInspectionsDirectly` helper — with the built-in server's inspection tools as
      the tested build names them (`lint_files` for a set of files, `get_file_problems` for one), read from the
      installed server rather than assumed. Keep the bullet's remaining rules unchanged: the build gates are canonical,
      the pass is optional and never a gate, and the assertion/Checkstyle guidance below it. Verify: the bullet names
      both tools as the tested server's, points at the installed server's list, and no Steroid identifier survives in
      it; `grep -in "steroid" AGENTS.md` lists only the tooling-setup bullet's fallback sentence and the agent-tooling
      gotcha's incident.
- [x] 1.2 `AGENTS.md`, the "Tooling-setup skill and command" bullet: the optional IDEA server becomes the IDE's built-in
      MCP server, with the Steroid bridge as its fallback for the cases the built-in server cannot serve. The
      configuration location becomes path-specific instead of one flat `mcp` map: the client entry lives in the flat
      `mcp` map of the global config, while Air's registration lives in `~/.jetbrains/acp.json`. Keep the bullet's other
      clauses: the `--global`/`mcp.servers` caveat and its upstream reference, Playwright being project-configured, and
      the `/setup-agent-tools` trigger. Verify: the bullet states the primary server, the fallback, and both
      configuration surfaces, and keeps the flat-map caveat.
- [x] 1.3 Same bullet, the README restriction is rewritten rather than bypassed: the README names the integrations and
      their user-facing benefit while the skill owns configuration, fallback selection, and hosting, and the Steroid
      bridge stays a fallback documented in the skill. Verify: the old "documents only GitHub … do not re-add it"
      sentence is gone, the replacement states the new division, and no clause the old sentence carried is left unowned
      — the Steroid-stays-in-the-skill rule survives.
- [x] 1.4 Confirm the agent-tooling gotcha is deliberately left as recorded: `AGENTS.md`'s "Before describing an MCP
      server…" bullet keeps its `runInspectionsDirectly` incident sentence. Verify: the sentence is unchanged, and
      `design.md`'s D4 states why.

## 2. The README

- [x] 2.1 `README.md`, `#### Tooling MCP Servers`: add an **IntelliJ IDEA's built-in MCP server** entry to the server
      list, after GitHub, stating what it gives the agent (inspections, refactoring, formatting, builds, run
      configurations, the debugger) and pointing at `/setup-agent-tools` to connect it. Verify: the entry reads as
      optional, carries no port, tool count, registry key, or JSON recipe, and makes no claim that a session has been
      exercised.
- [x] 2.2 Same file, the `/setup-agent-tools` row of the slash-command table: replace "optional extras" with the IDE
      connection it offers, and adjust the "one that matters" sentence so it distinguishes the GitHub setup, the
      optional IDE connection, and the already-configured Playwright. Verify: the row names the IDE server, the sentence
      no longer reads as if GitHub were the only optional server, and the manual-setup paragraphs stay true of the
      GitHub entry they describe.

## 3. The setup skill

- [x] 3.1 `.opencode/skills/setup-agent-tools/SKILL.md`: reframe the server list so the IDE entry is the built-in MCP
      Server (optional, IntelliJ IDEA only) with the Steroid bridge named as its fallback, and rewrite the frontmatter
      `description` to match — both the server it names and the trigger keywords. The introduction's claim that the
      per-user servers "live in the global OpenCode config" becomes path-specific: each server names its own
      configuration file. Verify: the introduction names the built-in server first and neither states nor implies that
      every server lives in one file, the description parses as one YAML value naming the built-in server, and the
      fallback's cases are stated.
- [x] 3.2 Same file, the IDE procedure opens with the hosting question — where the reader wants to run OpenCode:
      terminal/desktop, inside the IDE through JetBrains Air, or both — and the terminal/desktop branch follows: enable
      the server in the IDE's MCP Server settings (the consent dialog), pin the port, copy the URL the IDE shows, and
      wire it into the flat `mcp` map as `"intellij": { "type": "remote", "url": "<the URL the IDE shows>" }`. Verify:
      the question is asked before any writing, the branch names the enablement, the port pin, the URL source, and the
      flat-map entry shape, and no machine's port is written into the file.
- [x] 3.3 Same file, beside that branch: state that OpenCode is not one of the IDE's auto-detected clients, so the entry
      is added by hand; that the IDE-script toolset (`run_ide_script`, the built-in counterpart of
      `steroid_execute_code`) is off by default, gated by the `mcp.ide.script.toolset.enabled` registry key (the IDE's
      Registry dialog); that the MCP Server settings page is where the IDE's exposed tools are chosen; that each script
      is confirmed unless Brave Mode is on, which also skips the terminal and run-configuration confirmations; and that
      a server reading `failed` plausibly carries a stale URL, which `/reconnect-mcp` cannot retarget. Verify: the
      registry key and the settings page are named as separate surfaces, the stale URL stays a hypothesis, and nothing
      promises a fix the tool does not provide.
- [x] 3.4 Same file, the Air branch: detect first — whether `opencode acp` exists in the installed CLI, whether Air is
      available, whether `~/.jetbrains/acp.json` already registers an OpenCode agent (or Air lists one), and what its
      file-level `default_mcp_settings` hold — and reuse an existing registration when its flags match the reader's
      choice rather than adding a second; when they differ, report the difference and offer, with confirmation, to set
      them to the tested combination. Then, with confirmation, register the agent under `agent_servers` in
      `~/.jetbrains/acp.json`, preserving the file's other keys, with `command` the absolute `opencode` path,
      `args: ["acp"]`, and `use_idea_mcp`/`use_custom_mcp` set to the provisional combination for the reader's choice
      (Air only, or both) — the combination the change's live check settles, stated as provisional in the skill until it
      runs. When the reported defaults contradict that choice, offer to set them to match and leave them as found if the
      reader declines. Verify: the detection order is stated, the defaults are read and reported before anything is
      written, the write is presented as a per-user file the reader owns, the contradiction branch is stated with its
      confirm-and-decline outcomes, the entry shape matches the file's schema, and the combination is marked provisional
      rather than reported as observed.
- [x] 3.5 Same file, the Steroid step becomes the fallback: keep its `devrig` detection, installer, plugin install, and
      config-entry instructions, and state the cases it covers — an IDE release whose built-in server cannot serve, a
      headless backend provisioned through `devrig backend`, and screenshot or UI-input work, which the built-in server
      has no tools for. Verify: the fallback states its three cases and keeps every existing Steroid instruction.
- [x] 3.6 Same file, the fallback question and the switch's placement: the IDE step asks whether the reader needs the
      fallback cases — an IDE the built-in server cannot serve, a headless backend, or screenshot and UI-input work —
      and only then offers to remove an existing `steroid` entry when they do not; the fallback step itself runs only on
      a yes and installs without re-asking. Verify: the question precedes the offer, the offer sits in the IDE step
      rather than the skippable fallback step, the removal is never taken silently, and the file states which server the
      guidance tells the agent to use for IDE work, and that both may be connected.
- [x] 3.7 Same file, the Notes section: keep the `opencode mcp add` caveat and add both IDE wirings to the
      already-present check, so a second run leaves an existing `intellij` entry and an existing `agent_servers` entry
      alone. Verify: the Notes name both wirings and keep the upstream reference.
- [x] 3.8 Same file, the closing "Verify and hand back" step: verify each path by its own surface — `opencode mcp list`
      and an OpenCode restart for the client entry; Air's own agent list and a session for the ACP registration, which
      `opencode mcp list` does not show — and hand back only what could not be done for the reader. Verify: the step
      names both surfaces and does not instruct the reader to restart OpenCode for an Air registration.
- [x] 3.9 Same file, the version and capability boundary (D8): record the versions the recipe was verified against (IDEA
      2026.3 / IU-263.7307.200, its bundled MCP Server 263.7307.200, Air 263.7307.200-mac-arm64, OpenCode 2.2.5) and
      instruct the reader to read the installed server's tool list and the applicable ACP configuration before applying
      the recipe, reporting an unknown version or schema as unsupported or unverified rather than inheriting these
      assumptions. Verify: the versions and the discovery step are stated, and the unsupported branch is explicit.

## 4. The setup command

- [x] 4.1 `.opencode/commands/setup-agent-tools.md`: the frontmatter description and procedure step 2 name the built-in
      IDE server, the two hosting paths, and the Steroid fallback. Keep the skill as the procedure's authority, and keep
      one confirmation decision point — named once, and covering both per-user files the command may write: the global
      OpenCode config and JetBrains' ACP file `~/.jetbrains/acp.json`. Widen the verification and completion criteria
      the way task 3.8 widens the skill's closing step, so each path is verified on its own surface: `opencode mcp list`
      and an OpenCode restart for the client entry, Air's agent list and a session after reopening Air for the ACP
      registration. Verify: the description parses as one YAML value, step 2 names the server and both paths, the
      confirmation trigger is stated once and names both files, neither the verification nor the completion criterion
      names only the OpenCode surface, and the command still points at the skill for the steps.

## 5. The idea and the sweep

- [x] 5.1 `docs/ideas.md`: remove the 2026-11-06 entry "Replace Steroid with IDEA's built-in MCP Server, and optionally
      run OpenCode as an ACP agent inside JetBrains Air." — both halves are delivered here — leaving the section's other
      entries and its heading. Verify: `./gradlew verifyIdeasSections` passes and the entry's text is gone.
- [x] 5.2 Re-run the readiness sweep from the claims table's row 13: every live Steroid reference is one this change
      names or a test fixture. Verify:
      `git grep -ni steroid -- ':!openspec/changes/archive' ':!docs/units' ':!openspec/changes/adopt-builtin-ide-mcp-server'`
      names only the fallback clauses this change installs — `AGENTS.md`'s tooling-setup sentence and agent-tooling
      incident, the skill's description, intro bullet, fallback step and already-present-server note, the command's
      description and step, `docs/ideas.md`'s parked upstream re-check, and `scripts/test-reconnect-mcp.py`'s fixtures —
      and no file that presents Steroid as the repository's IDE path. Record the command's output on this task's `Done`
      note rather than predicting its lines. Verify: the recorded output is the run's, not a rewrite. Done: the run
      lists `.opencode/commands/setup-agent-tools.md` (1 line), `.opencode/skills/setup-agent-tools/SKILL.md` (11),
      `AGENTS.md` (3), `docs/ideas.md` (2) and `scripts/test-reconnect-mcp.py` (4) — the parked upstream entry is why
      `docs/ideas.md` stays in the set.

## 6. Verification

- [x] 6.1 `./gradlew spotlessApply`, then read back the reflowed markdown of every changed file. Verify:
      `./gradlew spotlessCheck` passes, and no changed file was skipped as non-converging. Done: `spotlessApply` ran
      after the last edit and `spotlessCheck` passed with no skip line. Done: `spotlessApply` ran over the tree and
      `spotlessCheck` passed with no skip reported.
- [x] 6.2 `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` and `openspec validate --all`. Verify: both pass;
      the definition inventory accepts the edited command and skill, and `skip_specs` validates. Done:
      `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` — BUILD SUCCESSFUL (411 tasks, 368 `testCommitHygiene`
      cases OK); `openspec validate --all` — 27 passed, 0 failed.
- [x] 6.3 Re-read each claims-table row against its source and delete any row that no longer reproduces. Verify: every
      remaining row's command was re-run, and the table states what the runs printed. Done: rows 11–13, 15–16, 19 and 21
      were re-run and reproduce; rows 1–10, 14 and 20 rest on IDE-side probes whose outputs the rows quote, re-checked
      against the same builds; row 17's file does not exist on this machine yet, so its schema claim rests on the
      plugin's serializer.
- [x] 6.4 Switch the client path first, and verify it by using it: through `/setup-agent-tools` on the client path,
      remove or disable the `steroid` entry, add the `intellij` entry with the URL the IDE shows, keep the other
      servers, then restart OpenCode. Verify: `opencode mcp list` reports `intellij` connected **and** one harmless IDE
      operation from a session against an identified project returns a result (for example `get_project_modules`, or
      `lint_files` on one tracked file) — the connection status alone is not evidence that the wiring works.

      Done: `~/.config/opencode/opencode.jsonc` now carries `intellij` (`http://127.0.0.1:64342/stream`) in place of the
      `steroid` entry, the other servers kept. `opencode mcp list` reports `intellij` connected without a restart, and
      `get_project_modules` with this worktree's path returned the project's module list. The endpoint hung until the
      owner toggled the IDE's MCP Server, which the skill's verification step now records as a remedy.

- [x] 6.5 Verify Air, run with the owner and after 6.4. Before registering anything, read and record the file's
      `default_mcp_settings`; register `opencode acp` in `~/.jetbrains/acp.json` (their file, their confirmation — the
      owner pre-confirmed both the write and the Air launch on 2026-11-06), then run the same harmless IDE operation
      against the same project in three cases, recording each result and whether the hand-added agent appears in Air's
      list:
  - the client path alone, already verified in 6.4;
  - **Air alone** — with the global `intellij` entry removed for the test, and re-added afterwards — which is the only
    case that shows Air's own injection works, since a working operation with the entry present could come through that
    entry;
  - the reader's final configuration, restored afterwards: for "both", the coexistence case (Air's injection on with the
    entry present, and injection off if the first doubles the IDE tools, which is D7's "both" default).

  Verify: the recorded cases decide which configuration the skill states, 3.4's provisional wording is replaced rather
  than confirmed, a case the run did not confirm is removed from the skill, and the owner's final configuration is the
  one restored on their machine.

      Done: `~/.jetbrains/acp.json` did not exist, so there was no `default_mcp_settings` to read or preserve; the
      agent was registered under `agent_servers.OpenCode` (absolute `opencode` path, `args: ["acp"]`). Final
      configuration (`use_idea_mcp: false`, `intellij` entry present): Air listed one OpenCode agent, and
      `get_project_modules` in an Air session returned the worktree's module list with no doubled IDE tools. Air alone
      (`use_idea_mcp: true`, entry removed): the same call reached the IDE through `jetbrains_air_ide.execute_tool` and
      returned the same list. The owner's restored configuration is the final one.

- [x] 6.6 Finalize only on a working set: if any of 6.4's client path and 6.5's three cases cannot be made to work, stop
      and report the failure rather than dropping the owner-requested hosting choice or shipping its guidance
      unverified. Verify: the outcome of every case is recorded, and no path's guidance stands on a check that failed.
      Done: both paths worked (client: `opencode mcp list` connected plus `get_project_modules` from a session; Air:
      both configurations above), so nothing was dropped and no guidance stands on a failed check. The restored
      configuration is the client entry plus `use_idea_mcp: false`.

- [x] 6.7 Upstream disposition for the Steroid defect, which `AGENTS.md`'s upstream-report rule requires and this change
      may not skip as a "third-party" defect: resolve the owning project first (claims row 22's evidence, and confirm
      the repository exists) rather than taking the coordinate on trust, then search that tracker for the reproduction —
      `NoClassDefFoundError: com/fasterxml/jackson/databind/ObjectMapper` in `McpScriptContextImpl` on IDEA 2026.3 — and
      either cite the existing report in the skill's fallback section, with what would retire the note, or draft the
      report and ask the owner before posting it (an outward-facing artifact takes the review gate). Verify: the skill
      carries an `owner/repo#NNN` reference or the owner's decision not to file one is recorded, and the reference sits
      in a skill `upstreamReferences` scans.

## 7. The review gate and the records

      Done: `gh repo view jonnyzzz/mcp-steroid` reports `hasIssuesEnabled: false` and `hasDiscussionsEnabled: false`, and `devrig.dev` publishes no contact link (its links are a font stylesheet and a YouTube playlist), so the project has no writable venue and no `owner/repo#NNN` exists to cite. The skill's fallback section records the reproduction and the closed venue, and `docs/ideas.md` parks the re-check of both surfaces. That is the rule's own venue check, recorded rather than skipped.

- [x] 7.1 The proposal stage's quick review over the planning artifacts until a clean round, each round's row and
      findings recorded under `docs/units/adopt-builtin-ide-mcp-server/`. Verify:
      `python3 scripts/commit-hygiene.py --unique-rounds --expect adopt-builtin-ide-mcp-server:<N>`, with `<N>` the
      round whose report just arrived, and `python3 scripts/commit-hygiene.py --session-continuity` pass; before the
      manual pass, `python3 scripts/commit-hygiene.py --unique-rounds --expect-clean adopt-builtin-ide-mcp-server`
      passes. Done: rounds 1–15, closing round 15 with no finding class; `--expect-clean` and `--session-continuity`
      pass.
- [x] 7.2 The owner's manual pass over the proposal. Verify: the approval is recorded before any implementation edit.
      Done: the owner approved after the advisor's three-round review-to-agreement ("Одобряю. Реализуй."), and
      implementation began only afterwards.
- [ ] 7.3 The implementation stage's quick review. Verify: the closing round carries no finding class, and
      `python3 scripts/commit-hygiene.py --review-ready` passes.
- [x] 7.4 The deep-review routing decision on the completed diff. Verify:
      `python3 scripts/review-routing.py --unit adopt-builtin-ide-mcp-server --change adopt-builtin-ide-mcp-server` ran
      and its outcome and signals are recorded. Done: RECOMMENDED on one `[repeated-class]` signal (ten rows repeat a
      prior class). The owner asked the advisor to settle it (C6, C7): it first recommended running it for two concrete
      workflow gaps, then — after rounds 19–21 repaired those and closed clean — reversed to recommending a skip, since
      the historical repeated-class signal alone establishes no remaining risk. The owner's explicit skip decision is
      what remains.
- [x] 7.5 The deep review, when 7.4 selects one, with its findings applied and re-reviewed. Verify: the pass's status
      and its findings' disposition are recorded. Done: skipped on the owner's explicit decision ("Да, пропускаем"),
      which the advisor's C7 reversal recommended after the two implementation gaps were repaired and the loop closed
      clean.
- [x] 7.6 The owner's manual pass over the finished unit. Verify: the approval is recorded, and nothing is committed,
      pushed, or merged before it. Done: approved with the delivery instruction ("Одобряю. Отгружай.") after the review
      surface was named; nothing was committed before it.
- [x] 7.7 `docs/units/adopt-builtin-ide-mcp-server/process-defects.md`: one record per defect observed, or the statement
      that none was. Verify: the file exists before 7.6 is requested. Done: four records — the truncated grep that
      became a claims row, the green formatter check recorded before the file's last edit, and two anchored-insertion
      slips that dropped a section heading.
