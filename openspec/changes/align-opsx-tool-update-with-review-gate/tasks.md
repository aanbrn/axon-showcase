# Tasks

## 1. Edit the command

- [x] 1.1 Confirm the `skip_specs: true` premise by grepping `openspec/specs/` for any requirement that describes the
      `/opsx-tool-update` command's behavior (only the `agent-skills` and `code-quality` file-scope examples should
      match); record the grep output. A requirement that describes it would owe a delta and refute the proposal. Done:
      `grep -rn "opsx-tool-update" openspec/specs/` returns only four file-scope example lines — `agent-skills` (two:
      the auditor's in-scope set) and `code-quality` (two: the formatter target set); no requirement describes the
      command's behavior, so `skip_specs: true` stands.
- [x] 1.2 Reword `.opencode/commands/opsx-tool-update.md`'s terminal step so the commit is taken after the repository's
      review gate — the `review-quick` pass, the per-unit `lesson-capture`, and the user's manual review pass — pointing
      at `AGENTS.md` rather than restating the gate. Leave the frontmatter `description` unchanged (it still describes
      what the command does). Verify by reading the step and confirming it no longer instructs an unconditional commit.
      Done: the step now reads "Before committing, run the repository's review gate (the `review-quick` pass, the
      per-unit `lesson-capture`, and the user's manual review pass — see `AGENTS.md`); commit only after that approval."
      followed by the conditional commit bullet; the frontmatter `description` is unchanged.
- [x] 1.3 Run `./gradlew spotlessApply` after the edit and confirm `spotlessCheck` passes; run the manual 120-character
      check over the change's non-formatter content only — the change dir's `.openspec.yaml` (the `*.md` files and the
      command file are Prettier-gated). Done: `spotlessCheck` `BUILD SUCCESSFUL`; `.openspec.yaml` passes the manual
      check.

## 2. Verification

- [x] 2.1 Run the implementation `review-quick` loop over the diff; fix its findings and re-run until it reports nothing
      new. Done: clean on the first round (no findings) — tasks 1.1–1.3 verified, no other artifact owed.
- [x] 2.2 Run the per-unit `lesson-capture` over this change and apply its durable proposals; record the applied net
      `AGENTS.md` delta. Done: the capture proposed **1 addition** (adding "a project-authored agent definition" to the
      capture bullet's routing list, symmetric with the architecture-auditor bullet's definition-edit route). Judged not
      durable for this unit — its evidence is a wording asymmetry, not the "two independent occurrences or one severe
      verified incident" the promotion gate requires, and applying it would expand this `skip_specs` change into an
      `agent-skills` `MODIFIED` delta (the clause also sits in that spec's "Lessons are captured after implementation"
      scenario and in `.opencode/agent/lesson-capture.md`). Net applied `AGENTS.md` delta: 0. Surfaced to the user at
      2.4; the user declined it, keeping this unit a narrow command fix.
- [x] 2.3 Sweep the docs the change could touch — `AGENTS.md`, `README.md`, `docs/adr/`, `docs/ideas.md` (any idea this
      change implements), and `openspec/config.yaml`'s `context:` block — and confirm none describes the command's
      commit step, so no edit is owed; record that finding. Done: every `opsx-tool-update` mention in `AGENTS.md`
      (seven) and `README.md` (two) describes what the command regenerates or its in-scope-file role; no `docs/adr/`
      mention; `docs/ideas.md`'s two mentions are the still-parked config-probe retirement, which this change does not
      implement; `openspec/config.yaml` names neither. No edit owed.
- [x] 2.4 Request the user's manual review pass — the step before committing. Done: the user reviewed the implementation
      and approved it ("Let's decline and proceed." — declining the 2.2 capture proposal).
