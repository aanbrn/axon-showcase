# Tasks

## 1. Anchor the git-push guard

- [x] 1.1 In `.opencode/opencode.json`, replace the `bash` rule `"*git push*": "ask"` with `"git push *": "ask"`. Verify
      with `opencode debug config` (exits 0 and resolves the project document's `permissions` array to
      `{"action": "shell", "resource": "git push *", "effect": "ask"}`) and `./gradlew spotlessJsonCheck` (the config is
      Spotless-owned).
- [x] 1.2 Prove the pattern's two directions with a scratch check that reproduces `Wildcard.match`'s semantics
      (anchored, `*`→`.*`, a trailing ` .*`→`( .*)?`) and applies them to the per-command patterns the parser emits: the
      `git push` pattern — bare, with arguments, or the `git push` node of a chained command (`shell.ts` emits one
      pattern per `command` node) — matches, while `grep -rin "git push" .` and `git log --grep push` do not. Read the
      scratch output, not only its exit code.

## 2. Bound the audit job

- [x] 2.1 Add `timeout-minutes: 60` to the `audit` job in `.github/workflows/audit.yml`. Verify with
      `./gradlew workflowLint` and by reading the job block back; confirm 60 exceeds the audit's observed maximum
      (`gh run list --workflow=audit.yml`, ~29 min) with margin.

## 3. Report the upstream gap

- [x] 3.1 Draft and post the upstream report for the shipped handler's unresolved tool-permission request: it denies the
      `question` permission in the session it creates but resolves no `ask` for any other permission (a tool permission,
      `external_directory`, `doom_loop`), so a raised `ask` blocks the run indefinitely. First derive the venue at
      posting time from the tracker's search and the closest reports' cross-references — starting from `#20823` (closed
      not planned, the same action and symptom), `#14473` (auto-closed after 60 days of inactivity, the headless
      `external_directory` case), `#44747` (open, a dropped subagent request), and `#36762` (closed as completed, the
      general headless-`ask` case) — rather than a fixed list, and either fold the report into an open thread or file a
      new issue naming the ones it relates to. Pass the draft through `review-quick` first. Record the reference with
      its close-out clause in `AGENTS.md` (a file `upstreamReferences` scans), naming the mechanism that retires it.

## 4. Verification

- [x] 4.1 Run `./gradlew spotlessApply` then `./gradlew spotlessCheck`, and `openspec validate --all`; confirm the
      config, workflow, and change dir are formatter- and schema-clean.
- [x] 4.2 Run a `review-quick` pass over the proposal and implementation, repeating until clean.
- [x] 4.3 Dispatch the audit workflow against this branch
      (`gh workflow run audit.yml --ref fix-unattended-permission-hang`) and confirm the run completes within the
      timeout and its log shows no unanswered `asking` event — the end-to-end check of the guard, run before the archive
      commit.

## 5. Capture

- [x] 5.1 Run the `lesson-capture` subagent over the diff, apply its durable proposals, and record the applied net
      `AGENTS.md` delta on this task. Applied: one new Gotchas bullet (an unanswered `ask` hangs an unattended run —
      anchor a `bash`/`shell` pattern to the parsed command and bound the job) and the `gh`-grant clause shortened to a
      pointer at it — net +8 lines (13 insertions, 5 deletions). No retirements were proposed.
- [x] 5.2 Ask the owner for the manual review pass, after 4.2 is clean and 5.1 is done.

## Workflow follow-up

- Archive the change after the project's review requirements are satisfied and the owner approves.
- Verify the archived result.
