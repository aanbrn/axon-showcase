# Tasks

## 1. Resolve the expressible ask-defaults

- [x] 1.1 In `.opencode/opencode.json`, add `read: {"*.env": "deny", "*.env.*": "deny", "*.env.example": "allow"}`
      (re-allowing `*.env.example`, which `*.env.*` would otherwise override) and `doom_loop: "deny"` (leaving
      `external_directory` to its grants). Verify with `opencode debug config` (exits 0; the project document's
      `permissions` carry `effect: "deny"` for `read` `.env` and `doom_loop`) and `./gradlew spotlessJsonCheck`.
- [x] 1.2 Read the resolved project `permissions` (the local v2 binary) and confirm no default `ask` remains that the
      config could resolve — the only `ask` entry is the `git push *` guard (v2's `external_directory` and `doom_loop`
      have no default rule, so they never asked locally).

## 2. Bound the `/oc` job

- [x] 2.1 Add `timeout-minutes: 120` to the `opencode` job in `.github/workflows/opencode.yml`. Verify with
      `./gradlew workflowLint` and by reading the job block back; confirm 120 exceeds the observed healthy `/oc` run
      (14m38s) with wide margin.

## 3. Docs

- [x] 3.1 In `AGENTS.md`, update the `anomalyco/opencode#54477` reference: keep the retire-when clause keyed on the
      handler resolving a non-`question` `ask` (the action is v1-coupled; there is no v2 action path), and record the
      `wont_fix` and the workaround. Verify by reading the clause back.
- [x] 3.2 Add the workaround to the captured "unanswered `ask`" gotcha (resolve the expressible ask-defaults to
      `allow`/`deny`, and bound each unattended job). Verify by reading the bullet back.

## 4. Verification

- [x] 4.1 Run `./gradlew spotlessApply` then `./gradlew spotlessCheck`, and `openspec validate --all`; confirm the
      config, workflow, and change dir are formatter- and schema-clean.
- [x] 4.2 Run a `review-quick` pass over the proposal and implementation, repeating until clean.

## 5. Reply

- [x] 5.1 Draft and post a reply on `anomalyco/opencode#54477` accepting the `wont_fix` and noting the workaround is
      applied in this repository, passing the draft through `review-quick` first.

## 6. Capture

- [x] 6.1 Run the `lesson-capture` subagent over the diff, apply its durable proposals, and record the applied net
      `AGENTS.md` delta on this task. Applied: one merge into the "unanswered `ask`" bullet (the per-binary ask-defaults
      and the "deny only a default whose internal allows are re-expressible" rule) — net +6 lines (10 insertions, 4
      deletions). No retirements were proposed.
- [x] 6.2 Ask the owner for the manual review pass, after 4.2 is clean and 6.1 is done.

## Workflow follow-up

- Archive the change after the project's review requirements are satisfied and the owner approves.
- Verify the archived result.
