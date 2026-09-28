# Tasks

## 1. Inject the capture step into every change's tasks

- [x] 1.1 Add a `tasks` rule to `openspec/config.yaml`, appended to the existing four: every change must include the
      per-unit `lesson-capture` step — run the `lesson-capture` subagent and apply its durable proposals — as a task, so
      an unrun capture is a missing box rather than a recollection. Write it as a single string (no unquoted `: `).
- [x] 1.2 Verify the CLI consumes the rule (the `openspec instructions` read path): run
      `openspec instructions tasks --change make-the-capture-visible --json` and confirm the new rule appears alongside
      the four existing `tasks` rules (a malformed rule drops the whole set, so the unchanged rules are the control);
      then temporarily remove the new rule and confirm it disappears (positive control), and restore it. This control
      covers the `openspec instructions` read path only — the CI gate runs `openspec new change`, which 1.3's known-bad
      arm exercises.
- [x] 1.3 Run the CI configuration probe locally as `ci.yml` does, both arms. Clean: creating a throwaway change with
      `openspec new change openspec-config-probe` outputs no `must be an array of strings` / `could not parse` warning,
      so the probe's grep would pass. Known-bad: temporarily add an unquoted `: ` to the new `tasks` rule, confirm
      `openspec new change` emits the `must be an array of strings` warning so the probe's grep would fail, restore the
      rule, and remove the probe directory.

## 2. Add the pull-request checklist record

- [x] 2.1 Add a checklist line to `.github/PULL_REQUEST_TEMPLATE.md`'s "Test plan" recording the per-unit
      `lesson-capture` run — recorded as the change's `tasks.md` task, or by ticking this line for a unit with no change
      dir. The line is not applied automatically: the repository's `gh pr create --body` gotcha prescribes
      `--body-file`, which supplies the whole body, so the line reaches the body through the `AGENTS.md` obligation
      (task 3.1), with the template fixing its place.
- [x] 2.2 Read the line back against the merge-time read: confirm it names the capture and is a record, not a gate, that
      it is carried by the `AGENTS.md` obligation rather than relying on the template, and that the template's two
      required sections remain.

## 3. State the record on the main agent's obligation

- [x] 3.1 In `AGENTS.md`'s capture bullet, state the record: every unit carries a capture record — a task in a change's
      `tasks.md`, or a line in the pull-request checklist for a unit with no change dir — and the merge-time read
      reports an unrun capture as that record missing or unticked, rather than reconstructing a skip from recollection.
- [x] 3.2 Widen the docs-refresh bullet's `openspec/config.yaml` copy sentence (`:888–891`) from the `context:` block to
      also cover the per-artifact `rules`, stating what is un-gated precisely — the rule's content and currency, since
      `ci.yml`'s probe guards the list's shape but not whether its wording still matches `AGENTS.md` and the spec.

## 4. Capture the behavior in the spec

- [x] 4.1 Write the delta at `openspec/changes/make-the-capture-visible/specs/showcase/quality/agent-skills/spec.md`:
      one `MODIFIED` requirement carrying the main spec's `Per-change quality-gate and analysis subagents are available`
      block verbatim (all six existing scenarios, in order, byte-identical header), with the record `AND` clause added
      to the capture scenario and the new `A skipped capture is a missing record at the merge` scenario.
- [x] 4.2 Confirm the requirement header matches the main spec's byte-for-byte, every existing scenario is present in
      order, and the new scenario uses four hashtags. Confirm the capability's `## Purpose` needs no refresh (its scope
      — per-unit review and capture — is unchanged) and record the decision rather than leaving it implicit.

## 5. Docs the change owns

- [x] 5.1 Update `README.md`'s self-learning bullet ("Every unit closes the loop", `:308–310`) to note the visible
      record — the unit carries the capture, so a skipped run shows as a missing box at the merge.
- [x] 5.2 Update `docs/retrospectives/2026-09-28.md`'s S1 disposition to name this change, as the analogous changes did
      for their retrospective's suggestion (`inject-positive-control-task-rule` → the 2026-09-19 table).

## 6. Confirm the subagent definition needs no edit (do not assume)

- [x] 6.1 Read `.opencode/agent/lesson-capture.md` in full and confirm no edit is owed: the record is planned and
      verified by the main agent around the subagent's run, and the subagent cannot observe its own skip — so the
      definition (its trigger, inputs, report contract, and frontmatter `description`) stays as widened by
      `scope-capture-trigger-to-the-unit`. Confirm there is no `lesson-capture` trigger command under
      `.opencode/commands/` (the capture has none). Record the no-edit decision in the change's report.

## 7. Verification

- [x] 7.1 Run the implementation `review-quick` loop: review the implementation diff against the change's planning
      artifacts, fix its findings, and re-run until it reports nothing new. Confirm the swept artifact set is complete
      against the archived analogues (`make-lesson-capture-consolidate`, `widen-lesson-capture-to-retirements`,
      `scope-capture-trigger-to-the-unit`, `inject-positive-control-task-rule`, `widen-config-probe-to-guidance`): the
      config rule, the PR template, `AGENTS.md`, the spec delta, the README, the no-edit decision on the definition, and
      the `## Purpose` decision.
- [x] 7.2 Run the per-unit `lesson-capture` over this change — dogfooding the new record, and satisfying the injected
      step this change adds — and apply its durable proposals. The capture ran before this review loop; its `AGENTS.md`
      proposals ride this diff, so the reviewer sees them.
- [x] 7.3 Re-run `openspec validate --changes` after the last artifact edit; confirm the `MODIFIED` block's scenario set
      equals the main spec's six scenarios plus the new one, with the header byte-identical.
- [x] 7.4 Run `./gradlew spotlessApply` after the last edit to a Spotless-owned file
      (`.github/PULL_REQUEST_TEMPLATE.md`, `AGENTS.md`, `README.md`, `docs/retrospectives/2026-09-28.md`, the change
      dir), then confirm `spotlessCheck` passes.
- [x] 7.5 Run the manual 120-character check (`perl -CSD -lne 'print if length > 120'`) over what Spotless does not
      cover — `openspec/config.yaml` and the change dir's `.openspec.yaml` (YAML has no formatter) — and confirm both
      return nothing.
- [x] 7.6 Request the user's manual review pass — the step before committing, once 7.2's applied capture and 7.4's
      formatter output are what the reviewer sees; no further editing step stands between the capture and this request.
