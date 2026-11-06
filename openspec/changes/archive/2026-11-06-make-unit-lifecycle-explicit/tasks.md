# Tasks

The unit's workspace record is `docs/units/make-unit-lifecycle-explicit/unit.md`.

## 1. Proposal stage

- [x] 1.1 Ran the pre-write derivation pass over this proposal, design, and deltas, and recorded the claims table in
      `design.md` — **Done** when every row's source reproduces the text it records, and each candidate
      `python3 scripts/claim-scan.py` returns is a row or a dismissal stating the reason by class.
- [x] 1.2 Ran the proposal-stage quick review to a clean closing round, recording each round in
      `docs/units/make-unit-lifecycle-explicit/rounds.tsv` and its instances in `findings/round-<N>.md` — **Done** when
      the closing round's classes read `-` and
      `python3 scripts/commit-hygiene.py --unique-rounds --expect-clean make-unit-lifecycle-explicit` passes.
- [x] 1.3 Ask the owner for the manual review pass over the proposal and end the turn there — **Done** when the owner
      replies.

## 2. Decisions and model

- [x] 2.1 Write `docs/adr/0030-the-unit-lifecycle-is-the-enclosing-process.md` with the Decision, the alternatives it
      declines, and its Consequences — **Done** when it is read against ADR-0025, ADR-0026, and ADR-0029 and states no
      mechanism those decisions own.
- [x] 2.2 Apply the `agent-skills` delta's added requirements and its two modified ones — **Done** when
      `openspec validate make-unit-lifecycle-explicit` accepts the delta, each added requirement states one behaviour,
      and `design.md`'s amended-clause trace quotes every changed clause beside its replacement.
- [x] 2.4 Refresh the `showcase/quality/agent-skills` Purpose at archive to name the Begin procedure and the lifecycle
      model among the project-authored guidance it provides — **Done** in the archive commit, since a delta cannot carry
      a Purpose.
- [x] 2.3 Replace `AGENTS.md`'s lifecycle section with the published model: the seven stages, their transitions and
      exits, the change operations marked conditional, and the owner map — **Done** when the section is read against the
      delta and carries no procedure that a skill owns.

## 3. Begin

- [x] 3.1 Write `.opencode/skills/begin-unit/SKILL.md` owning the workspace-establishment procedure for a new and a
      continued unit — **Done** when every decision point and failure branch of the retired command is present, and the
      script keeps creation and preparation.
- [x] 3.2 Write `.opencode/commands/begin-unit.md` carrying the lifecycle declaration and the full preflight, and retire
      `.opencode/commands/new-worktree.md` — **Done** when the command loads the skill rather than reproducing it, and
      no live artifact names the retired command.
- [x] 3.3 Reconcile `AGENTS.md`'s Workspace block with the skill: keep the authorization, occupancy, preservation, and
      release imperatives, and replace the steps the skill owns with its pointer — **Done** when no procedure sentence
      remains at both sites.
- [x] 3.4 Retarget the `lifecycle`, `workspace-record`, and `branch-refresh` values in
      `docs/contracts/process-bindings.md` and extend the consumer trace with the Begin components — **Done** when every
      listed consumer resolves its role.

## 4. Boundaries and delivery

- [x] 4.1 Align `review-gate`'s two boundaries with the stage names, keeping its procedure and reviewer ownership —
      **Done** when no step contradicts the model's transitions.
- [x] 4.2 Align `pause-resume-unit`'s pending-decision vocabulary and operation names with the model, leaving its
      checkpoint's `Stage` the interrupted operation's — **Done** when it introduces no new field set, adds no
      lifecycle-stage field, and its resolution rules still hold.
- [x] 4.3 Retitle and re-scope `.opencode/skills/ship-change/SKILL.md` to the unit lifecycle, make its archive step
      conditional, and align its completion criteria with settlement — **Done** when a change-less unit's delivery is
      representable end to end.
- [x] 4.4 Replace the unconditional archive in `AGENTS.md`'s completion criteria and state the
      `closed`-does-not-prove-delivery boundary — **Done** when both surfaces agree with the delta.
- [x] 4.5 Restate the publication prohibition with its narrow exceptions in the command preflight and the lifecycle
      section — **Done** when the requested-commit and draft-PR routes are stated, not inferred.

## 5. Projection and enforcement

- [x] 5.1 Add the lifecycle declaration block to every project-authored command, declaring each branch where they differ
      — **Done** when the derived command set's declarations are complete against the tracked files.
- [x] 5.2 Map the generated `opsx-*` entry points inside the lifecycle section without editing them — **Done** when each
      generated command's stage and containment are stated.
- [x] 5.3 Implement `verifyCommandLifecycle` as a `Task` + `CommandLifecycleRules` + `CommandLifecycleRulesTests`, and
      wire it into `check` — **Done** when each predicate carries a known-bad case and a known-good one: an undeclared
      command, an unrecognized value, an unresolved owner reference, a unit-starting branch without its preflight
      anchors, an unmapped generated command, and an empty derived set; and when `./gradlew check --dry-run` lists the
      task and a deliberately undeclared command makes a real `./gradlew check` run fail (a dry run executes nothing).
- [x] 5.4 Apply the `commit-hygiene` delta's verification requirements — **Done** when `openspec validate` accepts them
      and the check's claim matches what it proves.
- [x] 5.5 Refresh the `showcase/quality/commit-hygiene` Purpose at archive to name the command-lifecycle projection
      among the checks it owns — **Done** in the archive commit, since a delta cannot carry a Purpose.

## 6. Cleanup and verification

- [x] 6.1 Delete every replaced procedure body and confirm each remaining repetition is a safety imperative or a pointer
      — **Done** when the clause-to-owner trace has one destination per moved clause and no old body survives.
- [x] 6.2 Sweep the live references: the literal heading, `/new-worktree`, the binding values, the README's command row
      and narrative, and the tooling-placement example; and remove the implemented `docs/ideas.md` entry for the
      command-lifecycle check this change builds — **Done** when `git grep` finds no live reference to a retired name
      and the parked entry is gone.
- [x] 6.3 Restate the README's process narrative around the unit lifecycle and name its existing OpenSpec diagram as the
      nested change path — **Done** when no diagram is redrawn and the two layers are distinguishable to a human reader.
- [x] 6.4 Run the composed routes in disposable repositories: free primary, occupied primary, failed preparation,
      continued unit, unanswered approval, change-less delivery, archived-but-unmerged continuation — **Done** when each
      route reaches its intended owner or stops as specified. Results, each run by following the Begin skill's steps: a
      free primary was established in place — `git switch -c unit-fresh --no-track origin/main` left one registered
      tree, a branch without an upstream, and the record and claim written, with no worktree created; an occupied
      primary produced the sibling tree on its own branch through the wrapper; a failed preparation kept the tree and
      reported `NEEDS_ACTION` with its remedy; a continuation took a tree on the existing branch via `--continue`; a
      change-less unit's archive was refused, showing the delivery step is conditional; an archived-but-unmerged unit
      continued on its branch with its archive present and its record intact. The unanswered-approval route has no
      filesystem fixture: its evidence is the lifecycle's boundary rule and the requirement's scenario.
- [x] 6.5 Write `docs/units/make-unit-lifecycle-explicit/process-defects.md` as defects are observed, or state that the
      unit observed none — **Done** when the file exists and each record carries the field set.
- [x] 6.6 Write the implementation-stage finding instances and round rows as each quick-review round arrives — **Done**
      when the ledger's checks pass.
- [x] 6.7 Run the implementation-stage quick review to a clean closing round — **Done** when it reports no new
      observations and `--expect-clean` passes.
- [x] 6.8 Derive the deep-review routing floor with
      `python3 scripts/review-routing.py --unit make-unit-lifecycle-explicit --change make-unit-lifecycle-explicit` and
      record the outcome with its signals on this task — **Done** when the outcome line is written here. Outcome:
      `REQUIRED`, signals `[requirement-block-added]` for both delta specs.
- [x] 6.9 Run the routed deep review when the outcome requires or the owner selects it — **Done** when its status is
      recorded here, or when the outcome reads `NOT_NEEDED`. Deep review ran two passes: pass 1 raised an Accepted ADR's
      stale heading reference and the Begin skill's dropped `NEEDS_ACTION` routing, both `FIXED_AND_VERIFIED` in pass 2,
      which found nothing else. Deep review: an owner-requested explicit run reached proposal stage; pass 1 raised three
      should-fix findings — the claims row's `^opsx-` anchor, a missing task removing the implemented `docs/ideas.md`
      entry, and the state requirement's "delivered" wording against its added no-proof clause — and pass 2 marked all
      three `FIXED_AND_VERIFIED` and found nothing else.
- [x] 6.10 Run the gates over the finished tree — `./gradlew spotlessApply`, then
      `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`, and `openspec validate --all` — **Done** when each
      command exits zero on the final edit.
- [x] 6.11 Ask the owner for the manual review pass over the finished unit and end the turn there — **Done** when the
      owner replies.
