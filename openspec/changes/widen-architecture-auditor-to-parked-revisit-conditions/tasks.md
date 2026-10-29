# Tasks

## 1. The auditor definition

- [x] 1.1 `.opencode/agent/architecture-auditor.md` — the advisory lead's premise-met definition gains the third member
      (a parked entry whose own recorded reopening condition appears met), beside the ADR gate and the landed-work
      member.
- [x] 1.2 The same file's sweep list gains the sibling bullet for a parked entry's own condition: read the condition in
      whatever form the entry states it, hold it against the repository where it names a checkable fact, and classify a
      condition naming none as dependent on human judgment or not currently verifiable rather than reporting it. The
      file's Scope list names `docs/ideas.md`, which the sweep already reads.
- [x] 1.3 Its classification block widens from "a `Revisit when:` condition" to any recorded reopening condition — ADR
      or parked entry — with the ADR branch's four-way classification, the qualifier and compound-clause rules, and the
      unverifiable-is-no-claim sentence extended to a due-recurrence claim.
- [x] 1.4 It states the report shape: a met parked condition is a recurrence that is due, with the condition and the
      signal found meeting it, never an instruction to remove the entry.
- [x] 1.5 Its frontmatter `description` names the added shape in both sentences that state the premise-met class.
- [x] 1.6 Verify the definition states the same set the delta's clauses state — no narrower, no wider — and cites
      `showcase/quality/agent-skills` as the owning spec rather than this change directory.

## 2. The sites that describe the audit

- [x] 2.1 `.opencode/commands/audit-architecture.md` — the frontmatter `description`'s scope parenthetical names
      `docs/ideas.md`, and step 1's delegation names "how it reads and classifies a recorded reopening condition" where
      it named a `Revisit when:` condition. The procedure's decision points and the repository-lifecycle block are
      unchanged: the definition owns the surfaces.
- [x] 2.2 `AGENTS.md` — the architecture-auditor bullet's premise-met summary, its swept-surface clause, and its
      classification sentence gain the parked entry's own condition; the bullet states that a met condition is reported
      as due, never as a removal.
- [x] 2.3 `AGENTS.md` — the `docs/ideas.md` reach sentence (the docs-refresh bullet's "the architecture-auditor's sweep
      reaches `docs/ideas.md` for a parked entry's staleness") names a due condition beside staleness.
- [x] 2.4 `AGENTS.md` — the generalization's capture Decision point: its closing reason "no other reader tests a parked
      entry's condition" is falsified; state the capture's event-time role and the periodic sweep's next-run report
      instead of deleting the rule.
- [x] 2.5 `README.md` — the architecture-auditor prose paragraph names the added shape beside the landed-work and
      different-mechanism ones. The agent table row and the `/audit-architecture` row are generic ("a due record") and
      need no edit; record that they were checked.
- [x] 2.6 Sweep the class's own tokens (`premise`, `landed`, `different mechanism`, `due`, `revisit`) across the working
      tree, untracked files included, and correct every live site — the definition, the command, `AGENTS.md`, the
      README, and the capability `## Purpose`. The archived changes and the dated records are historical and stay as
      recorded.
- [x] 2.7 `docs/ideas.md` — remove the 2026-10-27 entry this change implements, per the header's removal rule.

## 3. The spec delta

- [x] 3.1 The `MODIFIED` delta at the change's `specs/showcase/quality/agent-skills/spec.md` carries the requirement's
      full body with all nine existing scenarios in their original order and under their original names, plus the three
      condition scenarios (met, not met, unsettleable).
- [x] 3.2 `openspec validate --all` passes.

## 4. Verification

- [x] 4.1 `./gradlew spotlessApply` after the final edit to a formatter-owned file, then `spotlessCheck`, the PR gate
      `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`, `verifyTableRows`, and `openspec validate --all` are
      clean.
- [ ] 4.2 Smoke-run the widened auditor with the controls below: verify each seed's premise against the repository
      **before** the run, write the expected outcome here rather than handing it to the auditor, save a byte copy of
      `docs/ideas.md`, seed the entries, and run `architecture-auditor` once with a neutral prompt. Restore the saved
      copy afterwards and confirm it matches byte for byte, on a failed run too. Record the run's output and which
      control each item came from. Each seed records its own rationale in the entry, so the intent-clarification branch
      stays silent; a negative is read as absent from the condition class rather than from the whole report. The
      positive control doubles as the registered-body confirmation: a `revisit due` item for the prose-stated seed
      cannot come from the pre-change definition, and the run applies the reload gotcha's conditions (file watcher on,
      the auditor invoked as a project subagent; `opencode run` is unverified), a restart being the recovery if the
      control yields nothing. **Blocked on the provider:** the pro-pinned auditor currently fails with
      `Model access is disabled`, so the run waits for the model and the archive waits on the run.

**Smoke-run controls**

| Control                  | Seed                                                                                                                                     | Expected in the report                                                                                               |
| ------------------------ | ---------------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------- |
| positive                 | a parked entry whose condition is written in prose, is repository-checkable, and appears met, while the work it describes does not exist | an advisory item naming the condition and the signal, stated as due rather than as a removal, and not as landed work |
| negative (unmet)         | a parked entry whose condition names a prerequisite that does not hold                                                                   | absent from the condition class                                                                                      |
| negative (unsettleable)  | a parked entry whose condition depends on human judgment or an unverifiable external fact                                                | absent from the condition class, no conclusion stated                                                                |
| regression (landed work) | a parked entry describing work that exists, with no condition                                                                            | reported under the landed-work class, not as due                                                                     |

- [x] 4.3 Run the `review-quick` loop over the proposal, then over the implementation, appending each round's row to
      `docs/review-rounds.tsv` as its report arrives, and re-run until a round reports nothing.
- [x] 4.4 **Routing `RECOMMENDED`** (`[repeated-class] 3 rows`) — skipped. The owner's instruction was to settle the
      open questions with the technical advisor and ship once agreed; the advisor recommended skipping the deep pass (a
      read-only definition-and-prose change whose remaining uncertainty is runtime behaviour, which the smoke-run
      tests), and that recommendation is the recorded decision.
- [ ] 4.5 Ask for the owner's manual review pass and stop there; no commit, push, or archive before the approval.

## 5. The change's own records

- [ ] 5.1 **Owed — for the archive commit**: refresh the `agent-skills` `## Purpose`, which says the auditor reports
      where a record's recorded premise appears met — a deferral's condition, or a parked entry whose work has landed,
      fully or partly, or whose objective a different mechanism achieves — and now covers a parked entry's own condition
      too. A delta cannot carry a Purpose, so the edit lands with the archive; the report states that the implementing
      PR's diff carries no main-spec edit beyond the delta.
- [x] 5.2 Record the pre-write derivation pass: the claims table is in `design.md`, and its rows derive from the parked
      entry, the definition's condition branches, the ideas corpus's non-labelled conditions, the capture rule, and the
      prior widening's Non-Goals.
- [x] 5.3 Write `docs/process-defects/widen-architecture-auditor-to-parked-revisit-conditions.md`, or state that the
      unit observed none.

## Workflow follow-up

- Archive the change after the owner approves and 4.2 has run; the archive commit rides this PR.
