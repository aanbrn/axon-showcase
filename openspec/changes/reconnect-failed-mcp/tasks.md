# Tasks

Workspace: unit=reconnect-failed-mcp; path=/Users/afanasyev/Development/axon-showcase-reconnect-failed-mcp;
branch=reconnect-failed-mcp; writer=ses_e884408e9ffeE8HmdsjrJA8W4N

## Resume checkpoint (paused and resumed 2026-10-26)

Written to the field set `.opencode/skills/pause-resume-unit/SKILL.md` owns. The pause and the resume both happened on
2026-10-26: the checkpoint was first improvised before that skill landed, and is restated here in the skill's shape.

| Field                       | Value                                                                                                                                                                                                                                                                                                                                                                 |
| --------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Time and revision           | Paused 2026-10-26 at `02f839d9`; resumed the same day at `cc07730c`, after refreshing the branch from `origin/main` behind it                                                                                                                                                                                                                                         |
| Workspace                   | `unit=reconnect-failed-mcp; path=/Users/afanasyev/Development/axon-showcase-reconnect-failed-mcp; branch=reconnect-failed-mcp; writer=ses_e884408e9ffeE8HmdsjrJA8W4N`                                                                                                                                                                                                 |
| Stage                       | Implementation complete and green; quick-review round 5 was the clean closing round; the owner's manual pass was outstanding                                                                                                                                                                                                                                          |
| Next step                   | Run round 6 over the advisor's two corrections (the discovery budget and the `inf`/`nan` rejection), then ask for the manual pass                                                                                                                                                                                                                                     |
| Change set                  | Seven new files staged (`.opencode/commands/reconnect-mcp.md`, the four change-dir files, `scripts/reconnect-mcp.py`, `scripts/test-reconnect-mcp.py`) and six tracked files modified but unstaged (`AGENTS.md`, `README.md`, `build.gradle.kts`, `docs/ideas.md`, `docs/review-rounds.tsv`, `.opencode/skills/check-and-test-authoring/SKILL.md`); nothing untracked |
| Blockers and open decisions | Nothing blocking. The pin question was closed by measurement rather than preference; the config-reload wording was narrowed instead of asserting a mechanism; no commit was made, so no delivery state existed to recover                                                                                                                                             |
| Pending decision            | The owner's manual pass. A "safety WIP commit" question I raised was withdrawn: no rule asks for one, and the workflow keeps work uncommitted until approval                                                                                                                                                                                                          |
| Findings no record holds    | Round 5 raised nothing; the instances that mattered were the advisor's two: discovery bypassing the deadline, and a non-finite `--deadline` accepted                                                                                                                                                                                                                  |
| Operations in flight        | None — no build, review invocation, or background command was running at the pause                                                                                                                                                                                                                                                                                    |

**Resume outcome.** The recorded next step ran: rounds 6–13 followed (the owner then asked for a no-target report and
`--all`, which are in section 2a), the branch was refreshed to `cc07730c`, and the manual pass is the outstanding step
again.

## 1. Records

- [x] 1.1 Confirm the workspace against the registry: `git worktree list --porcelain` shows this path on
      `refs/heads/reconnect-failed-mcp`, the branch was created from freshly fetched `origin/main` with `--no-track`,
      and the primary checkout's in-flight unit is untouched.
- [x] 1.2 Land the pre-write derivation pass as `design.md`'s claims table, with each claim's command and result.

## 2. The recovery tool

- [x] 2.1 Write `scripts/reconnect-mcp.py`: one named server, one connect attempt, a verified `connected` result, one
      monotonic deadline, the 0/1/2 exit contract, and a one-line result with sanitized diagnostics.
- [x] 2.2 Write `scripts/test-reconnect-mcp.py`, driving the tool's `main()` against a fixture `opencode` executable on
      `PATH`: failed → connected, a no-op, an unknown name, a second failed server left alone, an ineligible status, a
      server still failed after the acknowledgement, a delayed transition, a missing-server error, a failing transport,
      a malformed response, a directory with spaces, a stalled listing, and a stalled connect.
- [x] 2.3 Control: break one mechanism at a time — treat the acknowledgement as success, and drop the deadline — and
      confirm a named case fails each time; then restore.

## 2a. Extension: report and `--all`

- [x] 2a.1 The owner asked for the tool to work out which servers need reconnecting: a no-target run now reports every
      server's status and changes nothing (exit non-zero when any failed), and `--all` repairs exactly the `failed` set,
      one attempt each. `--all` with a name is a usage error. The delta's first requirement, the design's decision 2 and
      its exit table, the command, and the README paragraph were rewritten with it; five cases were added (report clean,
      report with a failure, `--all` repairs all, one unrepaired member still reports the rest, `--all` with nothing
      failed) and the old at-least-one-argument case became the report case.
- [x] 2a.2 Re-run the gates after the extension: suite at 24 cases, `spotlessCheck`, `openspec validate --strict`, and a
      live `--all` run against a server killed on purpose.

## 3. Build gate

- [x] 3.1 Register `testReconnectMcp` (both files as inputs, `outputs.upToDateWhen { false }`) and add it to the root
      `check`; run it standalone and through `check`.

## 4. Trigger command

- [x] 4.1 Write `.opencode/commands/reconnect-mcp.md`: the trigger, the named-server scope, and a pointer to the tool's
      own exit contract rather than a copy of the procedure.

## 5. Documentation

- [x] 5.1 `README.md` "Tooling MCP Servers": a failed server is recovered with `/reconnect-mcp <server>`, without
      restarting OpenCode; keep the existing config-load sentence, which states a different condition.
- [x] 5.2 `README.md` slash-command table: one row for `/reconnect-mcp`.
- [x] 5.3 `README.md` prerequisites: the Python 3.12+ row names the new script beside the existing tools.
- [x] 5.4 `AGENTS.md`, "Agent tooling, OpenCode & permissions": one pointer — a failed MCP server is recovered with the
      command, not by restarting, and the tool owns the contract.

## 6. Verification

- [x] 6.1 `openspec validate --all` and `openspec validate reconnect-failed-mcp --strict` pass.
- [x] 6.2 `./gradlew spotlessApply`, then `spotlessCheck`, after the final edit to any formatted file.
- [x] 6.3 Run the suite under Python 3.12 and the repository's `testReconnectMcp`.
- [x] 6.4 Live verification against the running service: disconnect Playwright through the API, then recover it with
      `python3 scripts/reconnect-mcp.py playwright` and confirm `connected` plus the tool inventory.
- [ ] 6.5 Record the `showcase/quality/agent-skills` `## Purpose` refresh for the archive commit: the Purpose enumerates
      the capability's subjects and does not name runtime recovery of a failed MCP server, so its sentence gains that
      subject. Verify: `git grep -c 'MCP' -- openspec/specs/showcase/quality/agent-skills/spec.md` before and after, and
      the Purpose names the recovery tooling.

## 7. Review and capture

- [ ] 7.1 Run the review gate's quick-review loop, record the round rows, and take the deep-review routing decision.
      **Hand-over recorded before the wait (round 1):** unit `reconnect-failed-mcp`, workspace
      `/Users/afanasyev/Development/axon-showcase-reconnect-failed-mcp`, branch `reconnect-failed-mcp`, base `02f839d9`;
      artifacts handed over `proposal.md`, `design.md` (claims table and dismissals), `tasks.md`, the delta spec, and
      the diff (`scripts/reconnect-mcp.py`, `scripts/test-reconnect-mcp.py`, `.opencode/commands/reconnect-mcp.md`,
      `README.md`, `AGENTS.md`, `build.gradle.kts`, `docs/ideas.md`); evidence supplied as the suite run, the two
      control runs, and the live `failed -> connected` run; deadline policy one pass, and a stall is reported rather
      than retried. **Hand-over recorded before the wait (round 2):** the same workspace and artifacts, plus round 1's
      fifteen findings each passed back by its instance with the fix applied, and the re-run evidence
      (`spotlessApply`/`spotlessCheck`, `testReconnectMcp` at 17 cases, `openspec validate --strict`, and both controls
      re-applied). **Routing decision (recorded):** `python3 scripts/review-routing.py --change reconnect-failed-mcp` →
      `REQUIRED`, on its `[requirement-block-added]` signal (the delta's two `ADDED` requirements); the deep review runs
      before completion. Quick-review rounds 1–5 are in `docs/review-rounds.tsv`; round 5 is the clean closing round.
      **Deep review (run, `ses_e880843feffeaCiKVN1H0B8u6O`, one pass):** 2 findings. The should-fix — the suite never
      asserted `location[directory]` on the _connect_ call, so a dropped location would silently target `$HOME` — is
      fixed: the fixture now rejects a connect without a location, `test_only_the_named_server_is_mutated` asserts it,
      and control C (the connect call without the location) makes five cases fail. The nitpick — row 13's unescaped `|`
      splitting the table cell — is escaped, and every row of the table now has four columns. **Hand-over recorded
      before the wait (round 6):** the advisor's pre-manual corrections, applied after round 5 — the discovery path now
      spends the invocation's budget (`min(GIT_TIMEOUT_SECONDS, remaining)`, a timeout raising `DeadlineExceeded`
      instead of falling back to the working directory), `--deadline` rejects `inf`/`nan`, two cases were added with
      their own controls (19 total; control D unbounds discovery, control E drops the finiteness check, each reddening
      its case), and the config-reload wording was narrowed in `AGENTS.md` and `.opencode/commands/reconnect-mcp.md` to
      point at the README rather than assert startup-only loading. **Hand-over recorded before the wait (round 7):**
      round 6's single finding, fixed — the stalled-connect case no longer shares its budget with the pre-connect work
      (`connect_delay=5` with `--deadline 2`, elapsed bound 4.0 s), so a slow fixture spawn can no longer turn it into
      exit 2; nine consecutive suite runs were clean and control B (`timeout=120`) still reddens it and the
      stalled-listing case. **Independent consultation (three passes) — recorded outcome:** the first closed the pin
      question by measurement (keep `@latest`; `npx` needs the registry whatever the selector) and proposed the recovery
      unit; the second found the unassigned-`directory` defect and the wrong `--all` bound (both fixed above); the third
      found nothing material and recommends the manual pass with **no further generic review rounds**, asking two
      semantics to be disclosed to the owner: `--all` targets the snapshot taken before the first repair (a server that
      fails during the run is not picked up, and a target that recovered on its own still gets one idempotent connect),
      and exit 0 does not mean every configured server is connected (`disabled` or `needs-auth` servers do not fail the
      run). **Hand-over recorded before the wait (round 12):** the advisor's second pre-manual pass found a real defect
      and a wrong bound, both fixed — an unassigned `directory` in `main`'s transport handler turned a stalled discovery
      without a server name into an `UnboundLocalError` traceback (reproduced), now a labelled exit-2 line, with two new
      cases (no target, `--all`) and control H reddening exactly those two; the design's `--all` bound now reads "one
      discovery budget plus one attempt budget per failed server"; "usually several servers" narrowed to the incident's
      two; the "resolves the selector at every start" sentence names its claims rows. The advisor's open question — the
      README still asserting startup-only config loading, against OpenCode's reload guidance — is parked in
      `docs/ideas.md` with the probe that would settle it, since that sentence is pre-existing and unverified either
      way. **Hand-over recorded before the wait (round 11):** round 10's finding, fixed — the usage-error case now
      writes a fixture state and runs under the fixture environment (`fixture_environment()`, shared with `run_tool`),
      so its `self.calls() == []` sees a listing if one happens; control G (the `--all`+name check moved behind the
      listing) reddens it, where before both the missing state and the un-patched `PATH` made the assertion vacuous.
      **Hand-over recorded before the wait (round 10):** round 9's four one-line findings, each fixed — `--help`'s row 1
      now names the failed-report case the docstring and the design table already carried, the delta's report scenario
      states "in name order" (which the ordered-string assertion pins), the usage-error case asserts that no server was
      read or changed (`self.calls() == []`), and decision 4's heading says "one line per server". **Hand-over recorded
      before the wait (round 9):** round 8's nine findings, each fixed — the design's Goals and Non-Goals now cover the
      three forms (the "mutate unrelated services" worry is answered by the `failed`-only eligibility), the command's
      frontmatter/opening/trigger/decision/completion describe report + `--all`, the flaky `--all` case runs at
      `--deadline 2` (8/8 clean runs), the delta states the report's line shape and the usage-error scenario, the
      design's exit table gains the failed-report row, the proposal names the three forms, and the tool docstring, the
      design's output sentence, and the README slash-command row were brought in line (the 121-character docstring line
      rewrapped). **Hand-over recorded before the wait (round 8):** the owner-requested extension — a no-target report
      (`mcp.list`, one line per server, nothing mutated, exit 1 if any failed) and `--all` (repairs exactly the `failed`
      set, one attempt each, `--all` + a name is a usage error). The delta's first requirement, the design's decisions
      2–4, the command, and the README paragraph were rewritten; five cases added (24 total), control F (`--all`
      ignoring failure state) reddens `test_all_with_no_failed_server_changes_nothing`; live run: killing the Playwright
      child made the report print `playwright: failed (Connection closed: …)` and exit 1, then `--all` reconnected it in
      1.7 s and the report returned exit 0.
- [x] 7.2 Lesson capture run (`ses_e87febec5ffe4CxCNDyGecWYL7`): two proposals applied, none retired. **`AGENTS.md` +6
      lines** — a new bullet under "Environment & host toolchain": never signal a PID picked by pattern-matching a
      process list, because a step's own `sh -c` line carries the whole script and the filter matches the shell running
      it (the `Killed by SIGTERM` incident). **`check-and-test-authoring` +10/−1** — the fixture-contract heading
      widened to "A stand-in that does not hold the contract it replaces", and one bullet: a stand-in must require the
      load-bearing parameters of a call (here `server` and `location[directory]`), not only have them asserted in one
      case. Compressed by the capture as already covered, each verified at its rule: the file-list dismissal shape
      (three `review-gate`/`config.yaml` clauses), the absent-CLI-capability conclusion (the
      `--help`-is-not-the-contract bullet), and the pin hypothesis (the measurement-hypothesis bullet). Both markers are
      on their rule blocks. Net delta of the two files together: `AGENTS.md` +18/−7 and the skill +10/−1, the remainder
      being this unit's own documentation edits.
- [ ] 7.3 Ask the owner for the manual review pass.
