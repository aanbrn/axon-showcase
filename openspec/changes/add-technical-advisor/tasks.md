# Tasks — add an independent technical advisor

## 1. Definitions

- [x] 1.1 Move `drafts/technical-advisor.md` to `.opencode/agent/technical-advisor.md` and apply the approved
      closing-sentence correction (distinguish owner-reported account information from independently verified facts;
      infer no authentication, billing, or per-request cost from the pin): `mode: subagent`,
      `model: openai/gpt-6.1-sol#high`, and the ordered `permissions` ruleset — the wildcard deny, then `read` (with the
      `.env` denies), `glob`, `grep`, and `skill`, and no shell rule. Verify with
      `test -f .opencode/agent/technical-advisor.md` and `sed -n '1,30p' .opencode/agent/technical-advisor.md` (the
      model, the wildcard deny, and the absence of `action: shell`).
- [x] 1.2 Move `drafts/consult.md` to `.opencode/commands/consult.md` unchanged. Verify the frontmatter carries only
      `description` (`sed -n '1,5p' .opencode/commands/consult.md`) and that
      `grep -n '^agent:\|^model:' .opencode/commands/consult.md` finds nothing.
- [x] 1.3 Inspect the loaded definition rather than the file: `opencode debug agents`, filtered to `technical-advisor`,
      must show model `gpt-6.1-sol`, provider `openai`, variant `high`, and the merged rules in order: the six base and
      global rules, the wildcard deny, the read allow, the `.env` denies and the `.env.example` re-allow, `glob`,
      `grep`, `skill`, and the built-in trailing `browser` deny — with no shell allow after the wildcard deny, so shell
      resolves to denied. Read the command's whole output for this entry, not a `head`/`grep` slice that could hide a
      missing field.
- [x] 1.4 Record the definition-discovery evidence and its limit: `opencode debug agents` is the local V2 check for the
      agent, while the command and the V1-consumer resolution are asserted only by the CI probe on the pull request
      (`.opencode/agent/*.md` and `.opencode/commands/*.md` are its scan paths;
      `sed -n '166,190p' .github/workflows/ci.yml`). Do not claim a local equivalent.

## 2. Spec capture

- [x] 2.1 Keep the delta at `openspec/changes/add-technical-advisor/specs/showcase/quality/agent-skills/spec.md` (the
      mirrored `showcase/…` path) and run `openspec validate --changes`; record the result.
- [x] 2.2 Confirm the `MODIFIED` block copies the whole shared report-contract requirement and preserves all five
      scenarios; `openspec validate --changes` is the check, and read the block against
      `git show HEAD:openspec/specs/showcase/quality/agent-skills/spec.md | sed -n '1311,1352p'`.
- [ ] 2.3 Refresh the capability's `## Purpose` in the archive commit to name the independent technical advisor among
      the on-demand subagents. A delta cannot carry a Purpose, so the edit is made in the archive commit and recorded
      there.

## 3. Documentation

- [x] 3.1 In `AGENTS.md`'s "Agent tooling, OpenCode & permissions" section, add one bullet: when to consult the
      `technical-advisor` (`/consult <question>` or direct delegation), that it recommends rather than edits, approves,
      or gates, and that its definition owns the contract. Verify the bullet is one item and copies no procedure from
      the definition.
- [x] 3.2 In `AGENTS.md`'s model-pin bullet, name the advisor's `openai/gpt-6.1-sol#high` pin beside the vision pin as a
      deliberate exception a model sweep must not "fix". Verify with
      `grep -n "exclude the vision agent's pin" AGENTS.md` and reading the sentence.
- [x] 3.3 In `README.md`'s agent table, add a `technical-advisor` row; in the slash-command table, add a `/consult` row.
      Add one short paragraph covering the advisory role and when to use it, a practical `/consult` example, the focused
      child-session context, the default `high` effort with a link to
      [`.opencode/commands/consult.md`](.opencode/commands/consult.md), and owner-facing language behavior. Verify each
      site; do not repeat the handoff or report procedure from the definition.
- [x] 3.4 In `README.md`, add the "OpenAI sign-in for `/consult`" subsection from the design's README plan: a two-row
      comparison (ChatGPT sign-in vs API key), a connection instruction using `/connect` or
      `opencode auth login openai`, the conditional recommendation, the included-plan-usage and conditional-credits
      note, the setting that lets connected applications use credits after the included usage limit, the distinction
      from separately billed API-key usage, the README sentence that method labels are version-specific, the corrected
      access-evidence sentence, and the links including
      [Sign in with ChatGPT](https://learn.chatgpt.com/docs/sign-in-with-chatgpt). Read the linked pages before writing;
      do not copy the V1 "ChatGPT Plus/Pro" label into a V2 instruction (the design's V1/V2 label note is commentary,
      not README text), make no claim about this installation's route or plan, and keep the owner's plan and
      auto-replenishment settings out of README. Verify the table has two rows, the text states included usage and
      conditional credits, the credit setting and its separation from API-key billing, both routes are distinguished
      from OpenCode Go, and the exact pin is unchanged.
- [x] 3.5 In `README.md`, correct the claim that the repo's agents are pinned to `opencode-go/*`: name the direct-OpenAI
      advisor pin (and the existing vision pin) as the deliberate exceptions. Verify against
      `grep -rn '^model:' .opencode/agent`.
- [x] 3.6 In `docs/ideas.md`, correct the parked vendored-skill note's "every project-authored model pin is
      `opencode-go/deepseek-*`" to name the deliberate exceptions. Verify the sentence still makes the note's point.
- [x] 3.7 Sweep the live sites this change makes stale: grep `technical-advisor`, `/consult`, `advisor`,
      `opencode-go/*`, `OAuth`, `API key`, `provider`, `credits`, `auto-replenishment`, `ChatGPT Plus`, `paid`,
      `billing`, `quota`, `cost`, `allowlist`, and `tool-output dump` across `AGENTS.md`, `README.md`, `.opencode/`,
      `docs/ideas.md`, and `openspec/specs/`; name each site and its disposition. The design may record the owner's
      reported plan; `README.md` must not. Distinguish the README's documented API-key billing sentence (intended) from
      any claim about the local account. Confirm the three README Mermaid diagrams do not depict the agent inventory and
      record that no diagram edit is owed.

## 4. Verification

- [x] 4.1 Pre-write derivation pass — done for the proposal in `design.md`'s claims table. Re-derive every row after the
      last edit to the unit's artifacts (4.9), so a figure measured before the final edit does not ship.
- [x] 4.2 `./gradlew spotlessApply` then `./gradlew spotlessCheck`; read the output for a
      `Skipping '<path>' because it does not converge` line and treat a skip as a failure for that file.
- [x] 4.3 Run the 120-character check over the changed set derived with `git status --porcelain --untracked-files=all`;
      every changed file is formatter-owned markdown here, so record that no manual-wrap file is in scope rather than
      skipping the check silently.
- [x] 4.4 `openspec validate --all` and `openspec validate --changes`; both pass with no new warning.
- [x] 4.5 `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` (the pull-request gate); record the untested tail
      (the `.opencode/` definition probe runs in CI on a pull request, not locally) and the v1-consumer limitation.
- [x] 4.6 **Reload before the smokes.** Restart OpenCode or start a new session so the new subagent is registered (a
      definition added mid-session is unregistered until a reload), then confirm `opencode debug agents` resolves
      `technical-advisor` and compare its `system` field with the body only of `.opencode/agent/technical-advisor.md`
      (the text after the closing `---`, not the frontmatter); a run echoing the new text is not proof, since the
      subagent can read its own definition from disk.
- [x] 4.7 **Smoke 1 — owner-facing Russian.** Invoke the advisor with the command's handoff (the owner separately
      reports the TUI `/consult` expansion works after the restart; the CI probe covers discovery) and the question
      `Стоит ли сохранять отдельную команду /consult, или достаточно прямой делегации к техническому советнику?`; read
      the whole report. It must answer in Russian, start with the recommendation, read `.opencode/commands/consult.md`
      or the proposal (one harmless repository read), edit nothing, and return a session identifier. Evidences: the
      owner-facing language rule, the report shape, and the focused handoff. It does not evidence the configured model
      or effort — the static `opencode debug agents` inspection does — and it does not prove the permission engine
      denies a call at runtime.
- [x] 4.8 **Smoke 2 — English agent-facing follow-up.** Continue the same advisor session with the saved `sessionID` and
      an agent-facing English question about the same decision. Verify the identifier continues the session, the report
      is English, and the recommendation is not presented as authorization. Reuse smoke 1's evidence; do not run a third
      call.
- [x] 4.9 Re-derive the claims table in `design.md` from its named commands after the final edit; correct or delete a
      row whose source no longer reproduces.
- [x] 4.10 Run the `review-quick` loop over the proposal, then the implementation: retain the reviewer session
      identifier, pass the prior round's finding instances, fix until it reports no new observations, and append each
      round's row to `docs/review-rounds.tsv` with the session identifier, transition, and replacement reason.
- [x] 4.11 Derive the deep-review floor with `python3 scripts/review-routing.py --change add-technical-advisor`, decide
      at or above it, and record the outcome and its signals on this task; run `review-thorough` if the outcome is
      `REQUIRED`. Outcome: `REQUIRED` (`requirement-block-added`). Pass 1 found one should-fix — the spec's "Credentials
      are not exposed" behavior had no definition instruction — and one nitpick (task 4.10 unticked); both were fixed.
      Pass 2 verified the fixes and raised one record nitpick (the 4.10 row's round range), fixed here; the quick loop
      then closed clean. Session `ses_ea135fc75ffecwPpj9CCLWhrID`.
- [x] 4.12 Run `lesson-capture` over the diff and review rounds unless the skip condition holds in full; apply its
      durable proposals and record the applied net `AGENTS.md` delta, or the skip and its reason, on this task. Capture
      ran. Applied two proposals: the claims-table record rule now covers a self-moving status, and the spec-delta
      workflow gains the clause-to-implementation-site traceability step. Capture-only `AGENTS.md` delta: +11/−5 (net
      +6); no retirement candidate was found. Skipped the fetch-rendering proposal: one occurrence, tool-specific, and
      the HTML-vs-markdown check is already recorded in task 4.9 rather than promoted to always-loaded guidance.
- [x] 4.13 Ask the owner for the manual review pass and stop. Do not commit, push, open a pull request, archive, or
      merge. Requested in the implementation hand-off; the work remains uncommitted.

## Implementation record

| Task    | Evidence                                                                                                                                                                                                                                                                                                                                                                                                                |
| ------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1.1–1.2 | Moved `drafts/technical-advisor.md` to `.opencode/agent/technical-advisor.md` and `drafts/consult.md` to `.opencode/commands/consult.md`; the definition's closing sentence now requires the owner-reported/verified distinction instead of naming a plan.                                                                                                                                                              |
| 1.3     | `opencode debug agents` resolves `technical-advisor` with `{providerID: openai, id: gpt-6.1-sol, variant: high}`; the repo's merged rules end with the wildcard deny, the read/`.env`/glob/grep/skill rules, and the built-in `browser` deny; no shell allow follows the wildcard deny.                                                                                                                                 |
| 1.4     | Under v2, `opencode debug config` lists configuration sources only and `debug skill` is unavailable; `debug agents` is the local agent check, while the command and V1-consumer resolution are asserted only by the CI probe.                                                                                                                                                                                           |
| 2.1–2.2 | `openspec validate --changes` and `--all` pass; the `MODIFIED` block preserves all five shared-report-contract scenarios.                                                                                                                                                                                                                                                                                               |
| 3.1–3.9 | AGENTS.md gains the consultation trigger and the model-pin exception; README gains the agent and `/consult` rows, the advisory paragraph, the V2-only scope, and the OpenAI sign-in subsection; the pin sentence and the parked `docs/ideas.md` note are corrected; the command and definition state the V2 prerequisite; the AGENTS flat-rate sentence is scoped to `opencode-go/*`; the site sweep is recorded below. |
| 4.1–4.5 | Claims re-derived in 4.9; `spotlessCheck`, `openspec validate --all`/`--changes`, and `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` pass.                                                                                                                                                                                                                                                                   |
| 4.6     | `opencode debug agents` resolves the agent; its full `system` field equals the body after the closing `---` (re-checked after the credential sentence), and no separate reload was needed in this session.                                                                                                                                                                                                              |
| 4.7–4.8 | Two smokes: a Russian owner-facing consultation (recommendation first, read `.opencode/commands/consult.md`, the design, and AGENTS.md, edited nothing) and an English agent-facing follow-up on the returned session identifier. The smokes invoked the advisor with the command's handoff; the owner separately reports the TUI `/consult` expansion works after the restart.                                         |
| 4.9     | Claims table re-derived; the `HEAD`-anchored rows still resolve, and the sign-in/credits row matches the page's HTML rather than its markdown rendering.                                                                                                                                                                                                                                                                |
| 4.10    | The loop ran the proposal and implementation rounds; every finding was fixed, and `docs/review-rounds.tsv` records each round.                                                                                                                                                                                                                                                                                          |

**Site sweep (3.7):** the `AGENTS.md` consultation-trigger bullet and model-pin exception; the README intro's pin
sentence, the `technical-advisor` agent row, the advisory paragraph, the "OpenAI sign-in for `/consult`" subsection, and
the `/consult` slash-command row; and the parked model-pin note in `docs/ideas.md`. The README carries no
`ChatGPT Plus`, `auto-replenishment`, or `allowlist` string; the three Mermaid diagrams do not depict the agent
inventory.

**Deferred (2.3):** the capability `## Purpose` refresh is made in the archive commit; a delta cannot carry a Purpose.
