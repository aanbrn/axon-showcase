# Design

## Context

**The mechanism already exists.** `pause-resume-unit` owns the checkpoint's field set and the resume's steps; two
commands trigger it; `AGENTS.md` carries the entry point; the README documents both commands; `agent-skills` specifies
the procedure. The premise inquiry found no parked idea covering this trigger — `docs/ideas.md` carries two
pause-related entries about the `Stage` field and freezing a checkpoint at the resume, neither of which is this subject
— and no archived change that added it.

**The incident.** Working in `separate-lesson-capture-from-implementation` on 2026-10-27, an agent wrote that the
session budget was exhausted (09:32:16 and 09:33:34, "а бюджет этой сессии исчерпан"), after having moved the unit one
step per owner reply: seven replies between 08:44 and 09:32 ("Продолжаем." five times, plus two consultation requests).
The owner then invoked `/pause-unit` (09:34:11), the checkpoint was written (09:34:43), and a `/resume-unit` session
continued at 09:35:17. The agent never offered the pause itself, though it had read the command definitions in that same
session the morning they merged.

**What the local measurement showed.** The harness was OpenCode v2.1.4; no agent or config set a step limit
(`opencode debug agents` reports `steps: unset` for all 18 agents, and neither config file sets one); the session's
per-request context at the claim was ~530–540k tokens, about 54 % of the model's 1,000,000-token window
(`~/.cache/opencode/models.json`); no compaction ever ran (`time_compacting` is null for every `axon-showcase` session);
the session's finishes were 272 `tool-calls` and 17 `stop`, with no `length` and no error; its cost was \$0.605. The
claim was therefore an unmeasured assessment, not a reported limit — consistent with OpenCode's absent budget visibility
(`anomalyco/opencode#19471`, closed `not_planned` on 2026-07-05; the diagnostic is filed as `anomalyco/opencode#59531`)
but not proof of that cause. These figures are a point-in-time measurement of one local session store, so they live in
this change dir and the process-defect record, never in a durable guidance artifact.

**What this change does not do.** It does not configure or remove a limit, add a budget monitor or plugin, pause on
every context worry, refresh a checkpoint continuously, or restate the procedure at a second site. The parked
`Stage`-field and freeze-at-resume ideas stay parked.

## Decisions

1. **The trigger moves; the procedure does not.** `AGENTS.md`'s existing entry-point bullet gains the agent's own
   decision as a trigger, and the skill owns what follows. A wholly skill-only rule would not be loaded at the moment
   the decision is made, which is why the root bullet is touched rather than left alone.
2. **The alternative to continuing is stated, not a detector.** The rule governs the choice between continuing and
   handing over; it adds no budget test. The agent continues whenever it can and awaits no real owner decision.
3. **The pause report carries the recovery path.** A checkpoint nobody is told how to resume is half a handover, so the
   report names the record and `/resume-unit`.
4. **An unmeasured limit is an assessment.** The evidence rule already requires verifying or labelling such a claim; the
   trigger restates it because the reader must act on it there, and a new general truthfulness rule would duplicate it.
5. **Approval stops are unchanged.** Required owner decisions, the review gate's approval stop, and permission prompts
   are not the confirmation-per-step pattern this rule forbids.

## Claims table

Each row states a fact this change asserts and the source that reproduces it; the local session-store rows are measured
on 2026-10-27 from the incident session named in Context.

| Claim                                                                        | Source                                                                                                                                                                                                                                                                                          |
| ---------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| The agent claimed the budget was exhausted and continued step by step        | Owner's account; the session's messages at 09:32:16 and 09:33:34, and the seven owner replies from 08:44 to 09:32                                                                                                                                                                               |
| Context at the claim was ~54 % of a 1,000,000-token window                   | `~/.cache/opencode/models.json` (`limit.context`); per-request `input` + `tokens.cache.read` 527,360–540,032                                                                                                                                                                                    |
| No step limit was configured or reported                                     | `opencode --version` → 2.1.4; `opencode debug agents` → `steps: unset` for all agents; both config files carry no `steps`                                                                                                                                                                       |
| No compaction ran, and no turn was truncated or errored                      | Session store: `time_compacting` null for every `axon-showcase` session; finishes 272 `tool-calls` / 17 `stop`, no `length`, no `error`                                                                                                                                                         |
| The incident session's recorded cost was \$0.605                             | Session store `session_v2.cost` for the incident session                                                                                                                                                                                                                                        |
| No parked idea or archived change covers this trigger                        | `git grep -n -i "reliably continue" 2b724094 -- openspec/changes/archive openspec/specs` returns nothing; `git show 2b724094:.opencode/skills/pause-resume-unit/SKILL.md` shows the trigger was owner-only; `grep -in -e pause -e resume docs/ideas.md` returns no entry naming a session limit |
| The pause path worked once invoked                                           | Pause message at 09:34:43 with `finish: stop`; `/resume-unit` session created 09:35:17                                                                                                                                                                                                          |
| The entry point has these sites and no others                                | `grep -rn "pause-unit\|Pause and resume\|pause-resume-unit"` over `AGENTS.md`, `README.md`, `.opencode/commands`, `.opencode/skills/pause-resume-unit`, and the `agent-skills` spec                                                                                                             |
| Both commands are already documented for humans                              | `README.md`'s pausing bullet and its slash-command table row                                                                                                                                                                                                                                    |
| `anomalyco/opencode#19471` is closed `not_planned` (2026-07-05) and unlocked | `gh api repos/anomalyco/opencode/issues/19471` (read 2026-10-27; state, state_reason, locked)                                                                                                                                                                                                   |
| `AGENTS.md` grows only by the new trigger case and its marker                | `git diff --numstat AGENTS.md` → `+6 −5` lines; `wc -c` 261,552 → 261,678 bytes (+126), measured after `spotlessApply`                                                                                                                                                                          |

## Risks

- **An eager pause.** A rule keyed on the agent's own assessment could stop a unit that could continue; the "continues
  when it can and awaits no owner decision" clause is the guard, and the failure handling keeps the existing
  stop-and-report rule for a checkpoint that cannot be written.
- **Read as a new approval stop.** The rule could be mistaken for one more confirmation gate; the decision point states
  that approval stops are unchanged.
- **Model-specific behaviour.** The incident is one model on one provider, and the change does not claim the behaviour
  generalizes; the upstream report is prepared as a diagnostic case, not as a defect proof.
