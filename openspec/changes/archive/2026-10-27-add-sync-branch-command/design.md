# Design

## Context

The owner's request is a saved instruction: "if the branch is behind main, update it", invoked as a command instead of
retyped. The behaviour that instruction needs is already specified and merged:

- `AGENTS.md`'s "Refreshing a branch" states the mechanism per state — recreate/fast-forward/rebase,
  `gh pr update-branch` once a pull request is open — the diff that deletes whatever merged since, the `git diff --stat`
  confirmation, and the both-sides conflict rule.
- `#704` (merged while this unit was in flight) added the recovery route: a fast-forward refused because the update
  touches a changed file is preceded by a stash and followed by a re-apply.
- `showcase/quality/agent-skills` already binds a project-authored command's lifecycle preflight.

An earlier draft of this change built a script for that procedure: 400 lines, a 41-case suite, a `check` task, a
three-requirement delta, and a README row for the tool. It reached a clean review round, but it duplicates the rule and
stops at exactly the step that carries the risk — a semantic conflict, which the caller resolves either way. The owner
already runs the procedure by prompt and the reviewers catch what it leaves, so the script's automation covered the
mechanical half of a task the agent performs reliably.

## Goals / Non-Goals

**Goals:**

- A command that invokes the existing rule, so the owner does not retype the instruction.
- A route table the agent reads at the moment it needs it, without a second implementation of the rule.

**Non-Goals:**

- A refresh engine: no script, no test suite, no build wiring.
- Automating conflict resolution: a conflict stays a judgement call, resolved for both sides.
- An unattended refresh: the command is owner-invoked, and a call reached from a diagnosis is read-only.

## Decisions

### D1. A prose command, not a script

The command carries the instruction and the route; the agent executes it with its own judgement. The alternative — the
script this change first delivered — is rejected because its automation ends where the difficulty starts: the `--apply`
path stashes, fast-forwards, and re-applies, then hands a conflicted re-apply back to the caller with the paths named.
The rule in `AGENTS.md` already covers that same sequence, so the script duplicated it while adding a second interface,
a suite, and a build gate to maintain.

### D2. The command points at the rule instead of restating it

`AGENTS.md` owns the mechanism per state, the recovery route, and the both-sides conflict rule. The command names them
and adds only what the rule does not say: that an explicit invocation authorizes the refresh, that a call from a
diagnosis is read-only, that an open pull request is handed to the owner, and what to report. A second copy of the table
would drift from the rule the moment either changed.

No branch state is routed differently from the rule: the command's step 6 names the state's route and hands it to the
rule, so a branch with no work at all is recreated as `AGENTS.md` says rather than fast-forwarded by a second opinion.

### D3. No spec delta

`skip_specs: true`. The command runs an existing rule and introduces no observable contract of its own; the capability
that covers a project-authored command's shape (`showcase/quality/agent-skills`) and the refresh rule `AGENTS.md` states
already govern it. The delta the script needed — report fields, an apply boundary, a verification — described behaviour
that no longer exists.

### D4. Three capture lessons are parked, not shipped

The capture from this unit produced five durable lessons. Two ship here — the spec-delta rule for an ordered-guard
subject, and the corrected tooling derivation — because they describe authoring this change exercised. Three are parked
in `docs/ideas.md`: the two test-authoring modes (an interception's partial reach; an assertion shared by sibling
outcomes), whose evidence came from the suite this change no longer delivers, and the `pause-resume-unit` `Stage` field,
whose wording invites the ledger's state — correcting a skill's field set is its own change.

## Risks / Trade-offs

- **The command depends on the rule staying accurate.** → It cites the bullet and never copies it; a change to the rule
  changes the command's behaviour without editing the command.
- **A prose command relies on the agent's judgement in the conflict case.** → That is the case the script could not
  decide either; the command requires the both-sides resolution and reports the outcome, and the review gate reads it.
- **No gate proves the command still matches the rule.** → Its `description` and body are checked by the CI definition
  probe and by the reviewers; the behavioural part is the rule's own text.
- **The parked lessons may be lost.** → They are parked with their evidence and the reason, in `docs/ideas.md`, which is
  read by the audits.

## Claims Table

| Claim                                                                  | Derivation                                                                                                                                                                        |
| ---------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| The refresh rule states the mechanism per state and the stash recovery | `grep -n "Refreshing a branch" -A9 AGENTS.md` → the bullet carries `recreate`/`fast-forward`/`rebase`, `gh pr update-branch`, and "stash first and re-apply after it"             |
| A project-authored command owes the lifecycle preflight                | `grep -n "lifecycle preflight" openspec/specs/showcase/quality/agent-skills/spec.md` → the requirement's header and scenarios                                                     |
| The unit's earlier script draft was reviewed and dropped               | `docs/review-rounds.tsv` rows 1–44 for `add-sync-branch-command`, and the advisor consultations `ses_e86fdfb51ffeIVk4evGH3db3GK` (reduce to prose-only)                           |
| The tooling derivation command missed the non-executable tools         | `git ls-files scripts` lists `claim-scan.py`, `review-routing.py`, `commit-hygiene.py`, `review-rounds.py` as mode `100644`, invisible to `git ls-files -s \| awk '$1=="100755"'` |
| Two test-authoring lessons have no artifact here                       | the deleted suite's incidents, recorded in this design's D4 and the parked entries                                                                                                |

## Migration Plan

None: a new command and three documentation edits. An agent that does not invoke `/sync-branch` is unaffected.
