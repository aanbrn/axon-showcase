# Decision record — an edit-path verification check is not feasible in the running binary

A dated record of a probe and what it allows the repository to do. The generalization pass
`generalize-process-defects-2026-10-30` left 13 records about edit, staging, and procedure failures, a subset of which a
hand-over then reported as fixed, and an advisor consultation proposed a bounded pilot of a check at the edit path. This
record is the pilot's verdict; it binds no standing rule.

## The question

Can a repository check — a `check` task, a pre-commit guard, a helper script, or a plugin — intercept the edit path (the
`edit` tool and the ad-hoc scripts agents write) so that a rejected, no-op, or partially applied replacement cannot
become a "fixed" hand-over? The pilot's stop condition was: if it cannot govern the real caller without a bypassable
helper or unsupported runtime integration, the recommendation changes rather than shipping another prose reminder.

## The probe

Measured on 2026-10-30 with the repository at `c0712cca`, on the local binary (`opencode --version` →
`opencode v2.2.3`).

| Probe                                                       | Command                                                                                                                                                                   | Result                                                                                                                                                                                                                               |
| ----------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| The running binary is v2                                    | `opencode --version`                                                                                                                                                      | `opencode v2.2.3`                                                                                                                                                                                                                    |
| The v2 plugin surface                                       | `ls .opencode/node_modules/@opencode-ai/plugin/dist/v2/promise/ .opencode/node_modules/@opencode-ai/plugin/dist/v2/effect/`                                               | the `promise` variant carries `agent`, `aisdk`, `catalog`, `command`, `context`, `integration`, `plugin`, `reference`, `registration`, `skill`; the `effect` variant adds `event`, `filesystem`, `location`, `npm`, `path`           |
| The v2 surface has no tool, permission, or session hook     | `grep -rn 'tool\|permission\|session' .opencode/node_modules/@opencode-ai/plugin/dist/v2/promise/*.d.ts .opencode/node_modules/@opencode-ai/plugin/dist/v2/effect/*.d.ts` | no output, exit 1                                                                                                                                                                                                                    |
| The six `*.transform` domains are static drafts             | `grep -n 'transform' .opencode/node_modules/@opencode-ai/plugin/dist/v2/promise/*.d.ts`                                                                                   | six `*.transform` drafts (`agent`, `catalog`, `command`, `integration`, `reference`, `skill`)                                                                                                                                        |
| The `aisdk` hooks are model wrappers                        | `grep -n 'sdk\|language' .opencode/node_modules/@opencode-ai/plugin/dist/v2/promise/aisdk.d.ts`                                                                           | `sdk` and `language`; model wrappers, not tool or permission hooks                                                                                                                                                                   |
| The v1 legacy surface carries a tool hook                   | `grep -n 'tool.execute' docs/adr/0024-tree-write-right-is-not-session-bound.md`                                                                                           | `40` — the ADR records `tool.execute.before`/`after` with the `sessionID` on the **v1** `Hooks` surface                                                                                                                              |
| No edit-path script exists                                  | `ls scripts \| grep -i 'edit\|apply'`                                                                                                                                     | no output                                                                                                                                                                                                                            |
| The pre-commit guard reads the staged tree, not a hand-over | `sed -n '4,13p' scripts/git-hooks/pre-commit`                                                                                                                             | the checks its header enumerates: the formatter, a force-staged artifact, a staged-then-edited path, a conflict marker, marker placement, the archive sync, the ledger, an archived task, an ideas section, and change-dir ownership |
| The `edit` tool errors on a missing anchor                  | `grep -n 'Could not find oldString' docs/process-defects/trim-readme-practices-density.md`                                                                                | `12` (the failure) and `16` (the evidence) — the tool returned `{"error":{"type":"tool.execution","message":"Could not find oldString …"}}`                                                                                          |
| The fix-verification rule exists                            | `grep -n 'Verify each fix' .opencode/skills/review-gate/SKILL.md`                                                                                                         | `141` — "Verify each fix against the finding's own claim — re-read the text the finding named, or assert the replacement's match count — before the next round and before any record reports it fixed"                               |
| The mechanism alternative is parked                         | `sed -n '148,158p' docs/ideas.md`                                                                                                                                         | "Give claims-table rows an executable verification mode" — an opt-in `claim-scan.py --verify`, with a bounded pilot first                                                                                                            |

The 13 records the pass grouped under this subject span several mechanisms, not one: missing or stale anchors
(`guard-the-ledger-at-commit#1`, `record-first-run-resource-budget#1`, `run-pr-refresh-on-explicit-invocation#1`,
`verify-empty-ideas-sections#2`), wrong replacement boundaries (`add-a-markdown-table-integrity-check#3`,
`seed-and-home-a-recordless-units-claims#2`, `trim-readme-workflow-density#2`), a batch abort leaving intended edits
absent (`guard-archived-task-boxes#3`, `#4`), a completion claim over an unresolved write mechanism
(`shorten-the-record-review-path#1`), a stale index (`state-the-adr-decision-boundary#1`,
`widen-the-claim-scanner-absences#2`), and a method deviation with no artifact damage
(`relocate-dependency-updates-section#1`).

## What the probe establishes

- **No reliable interception in the running binary.** The v2 plugin surface, both variants, has no tool, permission, or
  session hook; the `edit` tool and the ad-hoc scripts are outside any repository check. A `check` task or a pre-commit
  guard reads the staged tree, not the hand-over's prose.
- **A helper script is bypassable.** Nothing in the repository can require an agent to route an edit through a helper,
  and a check cannot tell a helper-applied edit from a direct one.
- **The v1 hook is a cloud-only surface.** `tool.execute.before` exists on the v1 `Hooks` surface, which the cloud
  workflows run; the local binary is v2, where the 13 records were observed. A v1-only control would not govern the path
  the incidents took.
- **The existing controls cover the failure modes.** The `edit` tool errors on a missing anchor, the review gate's
  fix-verification requires the edited text re-read or the replacement's match count asserted before any record reports
  it fixed, and the pre-commit guard catches a stale index. The false-completion subset are compliance failures of the
  fix-verification rule; the aborted attempts aborted on their own assertions (one's missing edits were caught by the
  next mutation run), a stale index by `--review-ready` or a review round, and the method deviation caused no damage —
  so the record does not rest on a missing mechanism.

## What the probe does not establish

- Whether a v1 `tool.execute.before` hook would catch the cloud instances — the cloud path was not probed.
- Whether an agent would use a helper script — unverified, and the helper remains bypassable.
- The exact fixtures (the anchor/no-op and batch-abort scripts) — not reconstructed, because the check is moot; they
  remain evidence for the claims-execution pilot.
- Whether retrieval or wording explains the recurrence — the advisor's hypothesis remains unverified.
- Whether a hand-over claim with no reproducing command row is caught — the claims-execution mode executes a row's
  command, and a claim outside that subset stays outside it, as the idea's own pilot note says.

## The decision

- **Do not build an edit-path check.** No repository check can govern the `edit` tool or the ad-hoc scripts in the
  running v2 binary, and a helper is bypassable.
- **The control is the review gate's fix-verification**, which requires the edited text re-read or the replacement's
  match count asserted before any record reports it fixed; the false-completion subset are compliance failures of it.
- **The route is the parked claims-execution mode** (`docs/ideas.md` 2026-10-27): it executes a claims row's command at
  the revision the row names and fails on a mismatch, so it catches a claim that carries a reproducing command row. A
  hand-over claim with no such row stays outside it — the limitation this record states, not a coverage claim.
- **No `AGENTS.md` or skill change is owed.** The rule exists, and another prose reminder is what the pilot's stop
  condition excludes.

## Reopening condition

Reopen this decision when any one of the following holds:

- **A v2 hook can deny an edit** — the loaded v2 surface (`dist/v2/`, both the `promise` and `effect` variants) gains a
  tool or permission hook that can deny an edit before it happens (external coordinate: that package's v2 types). This
  is the surface ADR-0024's first revisit clause names; this record's predicate is edit-denying, not the ADR's
  session-and-tree predicate, so a hook that denies edits without a session dimension fires this condition and not the
  ADR's.
- **A v1 hook governs the local path** — a probe shows `tool.execute.before` running on the local binary, either because
  the local binary moves to the v1 line or because the v2 binary loads the v1 `Hooks` surface (internal trigger: the
  probe).
- **The claims-execution pilot lands without catching this class** — if executing the claims rows does not catch a false
  completion claim about an edit, an edit-path control is back in question (internal trigger: that pilot's result).
