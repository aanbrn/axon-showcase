# Design

## Context

See `proposal.md` — Why. Three constraints shape the approach.

The harness generates a session's title from its first prompt and exposes no way to declare one: a command accepts
`template`, `description`, `agent`, `model`, and `subagent`, and the configuration carries no title setting. The only
lever is the session-rename tool, which acts after a session exists.

A unit acquires its authoritative name when Begin establishes its record (`docs/units/<unit>/unit.md`, ADR-0029), and a
resumed unit re-acquires it during Resume's verification. Before those points a name is a guess, so a rename placed at
command entry would be premature.

The lifecycle's Workspace rules already own where a unit lives and which tree is its own; the skills own the ordered
steps. The title policy is a rule about the unit's identity in the session, so it belongs with the Workspace rules, with
each skill applying it at its own attachment point.

## Goals / Non-Goals

**Goals:**

- A session's title names the unit the session is working on, once that unit is established.
- The title follows the session to a later unit begun in it.
- One statement of the policy, with the skills referencing it.
- A failed rename is visible and never blocks the work.

**Non-Goals:**

- Controlling the harness's title generation, or configuring it.
- Renaming historical sessions in bulk, or reconstructing a past title.
- Encoding lifecycle status, stage, or role in the title.
- A gate or test over session titles; no repository check can read them.

## Decisions

### D1 — The requirement owns the conditions, `AGENTS.md` points at it, the skills run the steps

The delta's requirement owns the behaviour and its conditions. `AGENTS.md`'s Workspace rules carry one imperative
sentence naming the rule and pointing at that requirement. The `begin-unit` and `pause-resume-unit` skills each carry
one reference-bearing step at their own attachment point.

Alternatives rejected:

- **Stating the conditions in both `AGENTS.md` and the requirement.** Two copies re-anchor on their own and drift.
- **Stating the rule in both skills.** The same duplication, one level down.
- **Putting it in the commands.** A command is the entry point; its procedure belongs to the skill.

### D2 — The title is the unit's name alone

The rename sets exactly the unit's established name, taken from its record directory (`docs/units/<unit>/`). A
continuation's branch may differ from that name, so the record directory is the source, not the branch.

Alternatives rejected:

- **A prefix such as `Main: <unit>`.** It appears on every session, so it distinguishes nothing.
- **A stage or status suffix.** It churns the title through the unit's life and duplicates a state the records own.

### D3 — Synchronize at Begin after establishment, and at Resume after verification

Begin renames after the workspace and record exist and the applicable claim step has run — a change-named branch may
omit the claim — and before the concluding report. Resume renames after the unit is verified and any writer handover or
legacy migration is done, and before the work continues.

Alternatives rejected:

- **Renaming at command entry.** The unit is not yet established, so the title would name a guess.
- **Requiring the claim at Begin.** Begin permits a change-named branch to omit it, so the condition would never hold
  for that route.

### D3a — The reference resolves to the delta until the archive commit lands it

Before the archive commit, the requirement exists only in this change's delta at
`openspec/changes/name-the-session-after-its-unit/specs/showcase/quality/agent-skills/spec.md`. A reader of the
reference during the unit's own implementation takes it from there; the archive commit and the skill steps ship in the
same pull request, so `main` never carries a reference to a requirement it lacks.

### D4 — An unresolved attachment leaves the title unchanged

When selection, verification, or a required handover stops the operation, no unit is established; the old title stays.

### D5 — No rename outside unit attachment

- Tangential discussion, a consultation, a review, and a delegate's session keep their titles.
- A pause and a settlement keep the last title, which names the session's latest established unit rather than its
  current state.
- An explicit owner preference overrides the convention.
- No mechanism tries to detect whether a title was chosen by hand.

### D6 — The rename is awaited, read back, and non-blocking

The step addresses the current main session explicitly, awaits the rename, and reads the title back before claiming it
happened. A failure is reported and the work continues.

Alternatives rejected:

- **A detached rename.** It can land after another unit becomes current.
- **Periodic corrective renames.** They paper over a harness that overwrites titles instead of diagnosing it.

## Risks / Trade-offs

- **The harness overwrites an explicit title later.** → Probed on 2026-11-08: an explicit title applied to a main
  session, read back, and survived a further prompt turn. A major harness upgrade voids the probe, so re-run it before
  trusting the mechanism again.
- **Two sessions of one unit share a title.** → Identifiers distinguish sessions; a title grants no writer authority.
- **A session's subject drifts from its unit.** → The policy names the unit the session works on; a later Begin or
  Resume re-synchronizes it.
- **A rename fails silently.** → The step reports the failure and never claims success without the readback.

## Claims table

The base revision is `7f29a1a8c2024a84df6469a52b82de0be900d97b` (`git merge-base HEAD origin/main` after the branch was
refreshed from the base).

| Claim                                                                          | Source                                                                                                           | Reproduces                                                                         |
| ------------------------------------------------------------------------------ | ---------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------- |
| A session's title is generated from its first prompt                           | the 2026-11-08 probe: disposable session `ses_e45fe96e3ffe7cosCoyndiX5pj`, first prompt "Reply with exactly: OK" | the session's title became `Reply with exactly OK` before any rename               |
| An explicit rename applies to a main session and survives a further turn       | the same probe                                                                                                   | the rename returned the unit name, the readback matched, and a second turn kept it |
| A command declares no title field, and the configuration has no title setting  | `https://opencode.ai/docs/commands` and `https://opencode.ai/docs/config`, read 2026-11-08                       | the command fields are `template`, `description`, `agent`, `model`, `subagent`     |
| The unit's name is authoritative in its record                                 | `docs/adr/0029-unit-state-lives-outside-the-change-plan.md`                                                      | the record binds the unit, its tree, and its state                                 |
| No requirement states a session title                                          | `git grep -n -i "session title\|rename the session\|session_rename" 7f29a1a8 -- openspec/specs AGENTS.md docs`   | the search returns no match                                                        |
| A session started by a bare `/resume-unit` was titled "Resuming a paused unit" | `GET /api/session/ses_e4960ba02ffeSVwO2K6w3UICYO`, read 2026-11-08                                               | the session's `title` field carried that string                                    |
| A session that begins a further unit keeps its previous title                  | the owner's report of 2026-11-08, not independently queried                                                      | the owner's account; no command reproduces it                                      |

Dismissed candidates from `scripts/claim-scan.py --base origin/main`, one reason per kind:

- `[path]` — the candidate is a repository path, command, or URL the change names and that exists at the base revision;
  the material facts are the table's rows.
- `[absolute]` — the match is an ordinary prose word (`no`, `every`, `never`, `only`), not a quantified claim about a
  corpus; the one absence claim is the table's own row.
- `[count]` — the match is a task or step number inside a reference the change names (`1.1`, `2.4`, `8`), not a tally of
  a corpus.
- `[cross-ref]` — the candidate names a file the change owns or cites, and that file exists at the base revision.
