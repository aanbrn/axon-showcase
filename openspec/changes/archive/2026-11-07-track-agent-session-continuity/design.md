# Design

## Context

See `proposal.md` — Why. This section carries the state and constraints the approach rests on.

**The mechanism being generalized.** The quick loop already has four parts, each with an owner: a recorded session
identifier with its transition and replacement reason in `docs/units/<unit>/rounds.tsv`; a preflight the review-gate
skill runs before each invocation, comparing the returned identifier with the saved one; a hand-over written before the
call at `docs/units/<unit>/review-handover.md`; and a `commit-hygiene.py` mode run by `check`. The deep review and the
advisor have the first part's _intent_ in their command definitions and a spec scenario, but no record, no pre-wait
hand-over, and no verification.

**What the current arrangement produces.** Two units recorded a `review-thorough` pass as a quick round with
`replaced`/`definition-changed` (`assert-paketo-pin-coverage` round 8, `unit-layout-allowlist` round 16), and the quick
reviewer's definition did not change on that date. The quick ledger's population is deliberately the quick loop's — the
deep-review requirement says so — so the record was bent to fit a session it does not own, and the quick reviewer's own
continuity was abandoned in the process.

**What the harness does (probe).** Recorded in `docs/units/track-agent-session-continuity/unit.md` under "Plan probe":

- a child session continued by the main session that created it returns the same identifier and keeps its context;
- a child session invoked from a different main session is refused with
  `Session <id> is not a child of the current session`, and an unknown identifier is refused with the same message, so
  the `subagent` route fails hard rather than silently creating;
- continuing a main session by its identifier (`opencode run --session <id>`) preserves that identifier and its child's
  continuability, so resuming the same main session is the one route that keeps the recorded sessions.

**Precedents adopted, not re-derived.**

- `openspec/changes/archive/2026-10-19-track-review-session-continuity` built the quick loop's recorded continuity; this
  change reuses its shape (identifier, transition, reason, pre-wait hand-over, checker) rather than inventing another.
- `openspec/changes/archive/2026-11-03-keep-review-handoff-per-unit` put the hand-over in the unit's file, one section
  per invocation; this change extends that file's use to every registered invocation.
- `openspec/changes/archive/2026-10-21-add-technical-advisor` D5 fixes the advisor's boundary at one conversation per
  decision and rejects one long-lived advisor conversation per branch, because accumulated context erodes independence.
- `docs/ideas.md` (2026-10-15) records that reusing the session is the caller's lever, and rejects a persistent session
  **as a substitute for** the per-round instance hand-off; this design keeps the hand-off and adds the recorded session,
  not one in place of the other.
- ADR-0024 records that nothing binds a write to a session, so a session identifier is a recorded fact, never an
  authorization; ADR-0029 puts the unit's record at `docs/units/<unit>/unit.md`.

**Constraints.** The quick ledger stays the quick loop's population; a unit's directory admits only the kinds the layout
names (so a new kind is a spec delta plus its verification); an outside-unit consultation starts no unit and writes
nothing; the checker validates records and cannot attest timing, authorization, or a stated cause.

## Goals / Non-Goals

**Goals:**

- One recorded session per `(unit, agent, scope, opinion)` for the deep review and the unit-bound advisor, reused by
  default.
- Replacement only on an evidenced reason: a forced cause is recorded and reported, a discretionary one needs the
  owner's approval.
- A hand-over written before the invocation, so a replacement costs a wait rather than the work.
- A resumed unit continues its sessions when the owner resumes the same main session, and records the forced replacement
  when it does not.
- Every rule the change adds is verified by a committed check with a known-bad and a known-good case.

**Non-Goals:**

- Reusing a session for a stateless subagent (`diagrammer`, `vision`, `explore`, the auditors, `lesson-capture`).
- Recording an outside-unit consultation.
- Transferring a transcript, or exporting/importing sessions.
- Re-parenting a child session, or changing the harness.
- Recording durations, token counts, or session counts as measurements.
- A per-unit advisor conversation, which the archived advisor design rejects.

## Decisions

### D1 — A per-unit agent-session register, not an extension of the quick ledger and not prose

`docs/units/<unit>/sessions.tsv` is a new append-only record with one row per registered invocation. Its columns are
stated in the `commit-hygiene` delta, which owns the format.

Alternatives rejected:

- **An `agent` column on `rounds.tsv`.** The ledger's population is the quick loop's trend basis, and the existing
  misfilings show the ledger bends when it must carry another agent's session. `review-routing.py` and
  `review-rounds.py` also read rows by position, so a wider row is a silent change to both consumers.
- **Sections in `review-handover.md` only.** The file is formatter-owned and reflowed, so a machine-readable row in it
  is not stable; and the hand-over is written _before_ the call while the row must record the identifier the call
  _returns_, so the two are different instants.
- **A second column set on the unit record.** `unit.md` is reflowed prose whose `Resume checkpoint` is replaced at each
  pause, while the register is append-only across the unit's life.

### D2 — Scope: the unit for a reviewer, the decision and opinion for the advisor

The reviewer's key is the unit: its passes are continuous across the proposal and the implementation, and the repository
does not admit two live change directories for one unit. The advisor's key is the decision plus the opinion, because the
archived advisor design fixes one conversation per decision and an owner-requested independent second opinion must be
its own conversation rather than a replacement.

Alternatives rejected:

- **One advisor conversation per unit.** The archived design rejects it: accumulated context erodes the independence the
  advisor exists for, and a second opinion on the same decision becomes impossible to distinguish from a continuation.
- **One session per invocation.** No continuity to record.
- **Registering the stateless agents.** A fresh session is their correct behaviour; registering it would make the
  correct behaviour a recorded replacement.

### D3 — The record separates requested from returned identity, and an unresolved row advances nothing

A row carries the identifier the invocation was asked to continue (`-` when none was) and the identifier it returned
(`-` when it reported none). The session's transition is established only by a `report` row, which carries a returned
identifier and, when one was requested, that same one. A `stalled`, `failed`, or `mismatch` row carries neither a
transition nor a reason and does not change the key's resolved session; a `mismatch` row carries a requested identifier
and a different returned one, recording a continuity failure rather than a replacement.

Alternatives rejected:

- **Recording the requested identifier as the session.** The probe shows the requested identifier is not evidence of the
  session that executes: a wrong one is refused, and a future harness could create another. Only the returned identifier
  establishes the session.
- **Treating a mismatch as expiry.** It would name a cause the evidence does not establish; the mismatch is a stop for
  the owner, not a replacement.

### D4 — The recorded reasons gain `parent-session-changed`, and the policy that permits one stays at its owner

- The `commit-hygiene` record-format requirement owns the **recorded values**: `expired`, `definition-changed`,
  `stopped-responding`, `parent-session-changed`, and `caller-restarted`, the last recording a violation rather than a
  justified case. Both records share that list.
- The **permitted cases** are the policy owners': the quick-loop requirement names the four that justify a replacement,
  and the register requirement names the forced and discretionary causes for its agents. Neither points at the format's
  list as a permission, because that list contains the violation label.
- A forced cause — expiry, a stall, a main session that cannot continue the child — is recorded with its reason and
  reported to the owner.
- A discretionary cause — a definition that changed after the session began, which makes its instructions stale — needs
  the owner's approval before the invocation, and the approval is recorded in the replacement's hand-over section, the
  one place the register does not hold it. An independent screen is not a cause, as the quick-loop requirement states.
- The register carries the invoking main session, so the checker validates the parentage claim: a `continued` row must
  have the same main session as the last resolved row, and a `parent-session-changed` row a different one. That control
  is why the register is not a copy of the ledger.

Alternatives rejected:

- **Calling a resume in a new main session `expired`.** False: the session still exists; it is not a child of the new
  session.
- **`caller-restarted`.** It is the violation record for an unjustified restart, not an objective cause.
- **Asking the owner before every forced replacement.** The existing quick loop already records a stall instead of
  asking; requiring approval for the unavoidable is friction, and the owner's rule is that the agent may not _choose_ to
  drop continuity, not that it must ask permission for a cause it cannot avoid.

### D5 — The hand-over stays one section per invocation, with the command's own fields and a slug link

Every registered invocation writes its section in the unit's `review-handover.md` before it is awaited. An invocation
that predates the mechanism, in the unit whose change introduces it, writes one from its reports and states that it was
written afterwards; an earlier unit's invocations are not reconstructed. The section carries the fields the invocation's
own command declares for its hand-over, plus the agent, scope, opinion, expected identifier, and — for a discretionary
replacement — the owner's approval; it does not restate those fields as a second list. Its heading begins with the slug
the register row carries, so the row and the section resolve to each other and the checker can verify the link.

Alternatives rejected:

- **A file per invocation.** The layout admits one hand-over file per unit, and the per-unit rule is a shipped decision.
- **No slug.** A resumed caller would find the section by guessing from the pass; the link is the one thing that ties
  the identity record to the context record.

### D6 — Pause/resume offers the same main session; the row's main session decides what is continuable

The pause report states that continuing the main session by its identifier keeps the recorded sessions, and that a new
session records each one's `parent-session-changed` replacement and restarts it from the hand-over. The resume compares
the current session's identifier — the caller's own, the value the unit record already records its writer by, never the
identifier the record names — with each needed row's main session: equal, it continues by the recorded identifier and
verifies the returned one; different, it starts the replacement from the section and records the forced replacement when
its outcome is known. A wrong initial identifier is not detected: when it differs from the resuming session's own, it
costs one restart from the hand-over; when it coincides with that identifier, the refused continuation leaves the honest
replacement rejected by the parentage rule, and the resume stops for the owner rather than recording a false reason.

The resume restores references and establishes unresolved outcomes first, and invokes an agent only when the stage's
next step warrants it; a consultation still needs the owner's authorization.

Alternatives rejected:

- **Always require a new session.** It discards the one achievable case, which the owner asked for.
- **Always continue the paused main session.** The pause exists because the session may be unusable; the owner chooses,
  and the agent's own assessment of the pause's cause informs the choice.
- **A `parent` field instead of the main-session column.** The row needs the invoking main session anyway to validate
  the parentage claim (D4), so a separate field would duplicate it.

### D7 — One checker mode, one `check` task, the layout and the pre-commit guard move together

`commit-hygiene.py` gains an `--agent-sessions` mode: field width and order, the unit column, agent membership, pass
contiguity per key, transitions and reasons, the parentage consistency (D4), and the hand-over link. A separate mode,
rather than extending `--session-continuity`, because the two records have different widths, different boundary rules,
and different failure messages. `build.gradle.kts` gains `verifyAgentSessionContinuity` as a `check` member; the layout
requirement and `UNIT_LAYOUT_FILES` admit `sessions.tsv`; the pre-commit guard refuses a staged record that fails the
mode, removes a committed row, or rewrites one — the staged record's committed prefix must equal the committed rows.

Alternatives rejected:

- **Extending `--session-continuity`.** One mode would read two formats and report one failure stream, obscuring which
  record failed.
- **No check.** The register would be a convention with no failure, which is the state the review-loop change replaced.

## Risks / Trade-offs

- **A resumed new session loses the advisor's thread.** → The hand-over carries the decision's recorded state, the row
  records `parent-session-changed`, and the pause report states the alternative; the owner can choose the same main
  session.
- **A wrong main-session identifier in a row.** → The checker rejects a `continued` row with a different main session
  and a `parent-session-changed` row with the same one. A wrong identifier on a `created` row is not detected: a resume
  from a different session pays one restart from the hand-over, and a resume whose own identifier equals the wrong value
  meets a refused continuation that the parentage rule rejects as a replacement, so it stops for the owner. The caller
  writes its own identifier — the value the unit record already records its writer by — never the identifier the record
  names, so a wrong value is a caller error, and the record is not rewritten to repair it.
- **Bookkeeping for every deep pass and consultation.** → One row per invocation, not per round; the quick loop's row is
  unchanged; the checker validates shape, not prose.
- **Context growth in a long-lived session.** → The deep reviewer re-inspects the current state on every pass (existing
  requirement), and the advisor stays per decision.
- **The slug link drifts when a heading is edited.** → `check` fails on the row, naming the slug, so the drift is caught
  before a commit rather than at a resume.
- **The checker cannot attest authorization or timing.** → The requirement says so explicitly; the evidence is the
  recorded fields and the owner's reply, not the gate.
- **A unit that already holds a `sessions.tsv` before the layout moves.** → No unit does today (the kind is new); the
  layout requirement and the checker change in the same commit.

## Amended clauses

| Requirement                          | Clause before                                                                                                                                                                      | Clause after                                                                                                                                                                                                                                         | Why                                                                                                                                               |
| ------------------------------------ | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------- |
| `commit-hygiene` layout              | `unit.md`, `rounds.tsv`, `review-handover.md`, `process-defects.md` at its first level                                                                                             | the same plus `sessions.tsv`                                                                                                                                                                                                                         | The register is a new admitted kind, and the requirement's own scenario says a new kind amends the requirement rather than placing the file alone |
| `commit-hygiene` review-round format | a replacement's reason (`expired`, `definition-changed`, `stopped-responding`, or `caller-restarted`)                                                                              | the same plus `parent-session-changed`                                                                                                                                                                                                               | A resume in a new main session is a cause none of the three names, and `expired` would be false                                                   |
| `commit-hygiene` continuity          | a reason the review loop's recovery cases allow                                                                                                                                    | a reason the record format allows or `caller-restarted` for a restart already performed without an evidenced recovery case                                                                                                                           | Single-sources the reason set at the format requirement; the continuity view validates the field                                                  |
| `commit-hygiene` continuity scenario | one of the three recovery reasons — `expired`, `definition-changed`, or `stopped-responding`                                                                                       | a reason the record format names                                                                                                                                                                                                                     | The same, and it stops a fourth reason from needing a second edit here                                                                            |
| `agent-skills` quick loop            | the only cases that justify a replacement are: the prior session expired, the reviewer's definition changed after that session began, or the reviewer stopped responding mid-round | a session that expired, a definition that changed after the session began, a reviewer that stopped responding mid-round, or a reviewer session the unit's new main session cannot continue; `caller-restarted` is a violation, never a justification | Adds the resume cause, and keeps the permitted set at the policy owner rather than pointing at a list that includes the violation label           |
| `agent-skills` quick-loop body       | one of the recovery cases the review gate's rule names                                                                                                                             | one of the recovery cases this requirement names                                                                                                                                                                                                     | The requirement owns the policy, so the gate skill points at it rather than owning a second list                                                  |
| `agent-skills` pause report          | SHALL tell the owner to continue in a new session with `/resume-unit <unit>`                                                                                                       | SHALL state that continuing the main session by its identifier keeps the recorded sessions, while a new session records each one's `parent-session-changed` replacement, naming `/resume-unit <unit>` beside that choice                             | The probe makes the choice meaningful; the old instruction silently forced the route that loses the sessions                                      |
| `agent-skills` consultation          | When the session identifier is unavailable, the caller SHALL start a fresh consultation and say that it is fresh                                                                   | the same, plus the unit-bound session resolved from the register and a second opinion as its own conversation                                                                                                                                        | The record is the identifier's durable home; a second opinion must not look like a replacement                                                    |

## Claims table

The base revision is `40088b3c5a5fb69d8fd421d1c80002057082d0af` (`git merge-base HEAD origin/main`), and each row about
the state before this change reads that revision.

| Claim                                                                                                  | Source                                                                                                                                                                                                                                                                            | Reproduces                                                                                                            |
| ------------------------------------------------------------------------------------------------------ | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------- |
| Two units record a deep pass as a quick round with `replaced`/`definition-changed`, both on 2026-11-06 | `git grep -n "ses_e513433d6ffePRVYkn6MBukAeC\|ses_e4ff042c4ffeg07bUWXO0uAVPp" 40088b3c -- 'docs/units/*/rounds.tsv'`                                                                                                                                                              | two rows, `assert-paketo-pin-coverage` round 8 and `unit-layout-allowlist` round 16                                   |
| Each unit's hand-over states that its session is the deep pass                                         | `git grep -n "ses_e513433d6ffePRVYkn6MBukAeC\|ses_e4ff042c4ffeg07bUWXO0uAVPp" 40088b3c -- 'docs/units/*/review-handover.md'`                                                                                                                                                      | each line names `review-thorough` as the agent of that session                                                        |
| The quick reviewer's definition did not change on 2026-11-06                                           | `git log -1 --format='%h %ad' --date=short -- .opencode/agent/review-quick.md`                                                                                                                                                                                                    | `26c92a96 2026-11-03`                                                                                                 |
| No hand-over records an advisor session identifier                                                     | `git grep -n "technical-advisor" 40088b3c -- 'docs/units/*/review-handover.md' \| grep -c ses_`                                                                                                                                                                                   | `0`                                                                                                                   |
| No unit holds an agent-session register today                                                          | `git ls-tree -r --name-only 40088b3c -- docs/units \| grep -c 'sessions.tsv$'`                                                                                                                                                                                                    | `0`                                                                                                                   |
| The quick ledger is the only per-unit session record                                                   | `git ls-tree -r --name-only 40088b3c -- docs/units \| grep -E '/(rounds\|sessions)\.tsv$' \| sed 's#.*/##' \| sort -u`                                                                                                                                                            | `rounds.tsv`                                                                                                          |
| The deep review and the advisor keep their identifier in the caller's session                          | `git grep -n "recorded session id" 40088b3c -- .opencode/commands/review-thorough.md`; `git grep -n "Retain the session identifier" 40088b3c -- .opencode/commands/consult.md`; `git grep -n "review-thorough\|technical-advisor" 40088b3c -- scripts/commit-hygiene.py \| wc -l` | a phrase naming a recorded id with no location, a retain instruction, and `0`                                         |
| The deep-review hand-over is an input contract, not a pre-wait record                                  | `git show 40088b3c:.opencode/commands/review-thorough.md`                                                                                                                                                                                                                         | steps 2–3 name a session identifier and an input list, and nothing is written before the invocation                   |
| Two hand-overs record a deep-review identifier in prose                                                | `git grep -l "review-thorough.*ses_" 40088b3c -- 'docs/units/*/review-handover.md'`                                                                                                                                                                                               | two files                                                                                                             |
| The unit layout admits four first-level kinds and `findings/round-<N>.md`                              | `git show 40088b3c:scripts/commit-hygiene.py`                                                                                                                                                                                                                                     | `UNIT_LAYOUT_FILES = ("unit.md", "rounds.tsv", "review-handover.md", "process-defects.md")`                           |
| The replacement reason set is three plus `caller-restarted`                                            | `git show 40088b3c:scripts/commit-hygiene.py`                                                                                                                                                                                                                                     | `SESSION_REPLACEMENT_REASONS = ("expired", "definition-changed", "stopped-responding", "caller-restarted")`           |
| The layout requirement excludes a third level such as `findings/consultations/advisor-1.md`            | `git show 40088b3c:openspec/specs/showcase/quality/commit-hygiene/spec.md`                                                                                                                                                                                                        | the scenario names that path as not admitted                                                                          |
| The quick ledger's population is the quick loop's, not a general session record                        | `git show 40088b3c:openspec/specs/showcase/quality/agent-skills/spec.md`                                                                                                                                                                                                          | the deep-review requirement's scenario says the quick ledger stays its own record                                     |
| The archived advisor design rejects one long-lived conversation per branch                             | `git show 40088b3c:openspec/changes/archive/2026-10-21-add-technical-advisor/design.md`                                                                                                                                                                                           | D5 states the rejection                                                                                               |
| The 2026-10-15 idea rejects a persistent session as a substitute for the instance hand-off             | `git show 40088b3c:docs/ideas.md`                                                                                                                                                                                                                                                 | the entry's first lever                                                                                               |
| Same-parent continuation works                                                                         | `docs/units/track-agent-session-continuity/unit.md`, "Plan probe"                                                                                                                                                                                                                 | the child `ses_e4ac50a39ffeDw7VGlTmNPWQfZ` returned `ORCHID-7391` to its creating session                             |
| Cross-parent continuation is refused                                                                   | the same record                                                                                                                                                                                                                                                                   | `Session ses_e4ac50a39ffeDw7VGlTmNPWQfZ is not a child of the current session`                                        |
| An unknown identifier is refused on the `subagent` route                                               | the same record                                                                                                                                                                                                                                                                   | the same message for `ses_00000000000000000000000000`                                                                 |
| Continuing a main session by its identifier preserves its children                                     | the same record                                                                                                                                                                                                                                                                   | `opencode run --session ses_e4aba0b55ffeWH1uUFdsdpdUZ6` kept that identifier and returned `MAPLE-5522` from its child |

**Dismissals** — one reason per candidate class the seed scan reports, governing every candidate of that kind:

- `path` — a live path resolves to the artifact it names, each read above; a path naming a file this change creates is
  its own and is verified by its task.
- `absolute` — a prose qualifier whose substantive claim is a row of this table; a qualifier stating no fact is not a
  claim.
- `cross-ref` — a heading, capability, or section the corpus resolves, or a phrase the scanner misreads as one
  (`exporting/importing`, `Pause/resume`).
- `count` — a figure the table records (the field count, rounds 8 and 16) or a count of a set a row names; no count
  stands without its row.

## Verification

- `openspec validate --all` passes with the two deltas at their mirrored capability paths.
- `python3 scripts/test-commit-hygiene.py` carries a known-bad and a known-good case per rule the mode adds.
- `./gradlew verifyAgentSessionContinuity` runs the mode, and the affected `check` includes it.
- `./gradlew spotlessApply` and `spotlessCheck` pass after the last edit.
- The review gate runs its stages; the probe's evidence is in the unit record.
