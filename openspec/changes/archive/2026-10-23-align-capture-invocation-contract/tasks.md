# Tasks

Four prose sites state the same defect; the planning artifacts are `proposal.md`, `design.md`, and the delta.

## 1. Align the live descriptions with the adopted rule

- [x] 1.1 `README.md`'s agent table row (`:383`): drop "for a unit whose evidence shows a lesson" and the trailing
      "(evidence-gated)", so the row describes what the subagent does without stating the invocation precondition.
      Verification: the row still fits the table's column count and names no invocation condition. **Done**: the row now
      reads "Captures gotchas/conventions into AGENTS.md or the owning subject pack, consolidating rather than
      accreting, …" with no precondition and no "(evidence-gated)"; its two-column pipe count is unchanged.
- [x] 1.2 `README.md`'s "Lesson capture" bullet (`:430`): replace the "for a unit whose evidence shows a candidate
      lesson" precondition with the default-invocation outcome and the skip condition as its only exception.
      Verification: the sentence states the exception without listing the conjunction's clauses. **Done**:
      "`lesson-capture` runs after every unit's implementation quick review unless the skip condition `AGENTS.md`
      defines holds in full, and proposes …".
- [x] 1.3 `README.md`'s "Every unit closes the loop" bullet (`:492`): replace "runs ... when its evidence shows a
      candidate lesson" the same way. Verification: the three README sites read consistently with each other and with
      the spec. **Done**: "runs after a unit's implementation quick review unless the skip condition `AGENTS.md` defines
      holds in full"; all three README sites now state the outcome and name the owner.
- [x] 1.4 `.opencode/agent/lesson-capture.md`'s frontmatter usage sentence (`:6`): replace the "shows evidence of a
      reusable lesson" precondition with the outcome — invoked after a clean implementation quick review unless the
      condition defined by `AGENTS.md`'s capture workflow holds in full — and leave the input contract below untouched.
      Verification: the frontmatter still parses and the contract's remaining sentences are unchanged. **Done**: "Use
      after a unit's implementation quick review unless the skip condition defined by `AGENTS.md`'s capture workflow
      holds in full, so …"; the input contract is untouched.

## 2. Verification

- [x] 2.1 Sweep the inverted phrasing with
      `git grep -nE -e 'candidate lesson' -e 'shows a lesson' -e 'shows evidence' -e 'evidence-gated' -- README.md     .opencode/agent/lesson-capture.md`,
      proved against the four known matches first. Verification: no live site states the precondition; `AGENTS.md`'s
      conjunction is untouched. **Done**: the four-pattern grep returns no match in `README.md` or the definition (exit
      1), and the same pattern matches `AGENTS.md`'s merge-time line, proving it can match.
- [x] 2.2 Confirm the delta carries the requirement's four scenario names and pass
      `openspec validate align-capture-invocation-contract --strict`. Verification: four names, no rename, valid.
      **Done**: four scenario names, no rename, `--strict` valid; the description measures 488 characters.
- [x] 2.3 Run `./gradlew spotlessApply` after the last edit, then `spotlessCheck`, `openspec validate --all`, and
      `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`. Verification: all green. **Done**: `spotlessCheck`,
      `openspec validate --all` (27/0), and the Docker-free `check` (394 tasks) are green.
- [x] 2.4 Run the pre-write derivation pass and record its output as the design's claims table, re-derived after the
      last edit. Verification: each row's source reproduces. **Done**: the claims table was re-derived; the four-site
      row now anchors to `git show HEAD:<path>` and reproduces `README.md:383/430/492` and `lesson-capture.md:6`.
- [x] 2.5 Run the `review-quick` loop to a clean round, appending each round's row and running
      `python3 scripts/commit-hygiene.py --unique-rounds --expect align-capture-invocation-contract:<round>` after each
      append. Verification: the final round reports `nothing remains` and each checkpoint passes. **Done**: rounds 1–6
      in one reviewer session; round 6 reports `nothing remains`, each checkpoint passed in the same turn as its append.
      **One process incident, recorded:** round 7's ledger row was appended late — after the fixes, not as the report
      arrived — and the `--unique-rounds --expect` checkpoint caught the missing row, which was then restored. The late
      check found the gap; it does not make the immediate-append requirement met, and the incident was absent from the
      hand-off the implementation capture received. The capture's contract already governs it (`already covered`), so no
      new guidance rule is added; the record is the change's evidence.
- [x] 2.6 Derive the deep-review floor with
      `python3 scripts/review-routing.py --change     align-capture-invocation-contract`, decide at or above it, and
      record the outcome and signals on that task. Verification: the recorded outcome names its signals. **Done**:
      `RECOMMENDED` on `repeated-class` (2 rows repeat a prior class). Resolved without a deep pass: the change is four
      prose sentences plus one requirement description and one scenario clause — no code, test, configuration, or
      deployment surface — and both repeated `wording` instances were fixed (rounds 2 and 5) with the proposal round
      clean at round 4.
- [x] 2.7 Reload OpenCode after the definition edit, then run the **caller's** invocation decision twice, proving the
      policy boundary rather than the subagent's own judgment: (a) a fixture whose unit changed an authoring surface and
      noticed no candidate lesson — the skip conjunction fails, so the capture must be invoked; (b) a fixture where the
      whole skip conjunction holds — the capture must be skipped with its reason recorded. Verification: each run's
      transcript shows the invocation or the skip and its record, not a restatement of the rule; each fixture's premises
      are established before the run; the isolation and no-escape rules hold; a file read alone is not the
      registered-definition check. Also read and record that a compliance-miss finding requires the capture and that the
      merge-time re-verification stays mandatory. **Done**: OpenCode was reloaded after the definition edit, then two
      caller-decision fixtures ran in fresh sessions. (a) An authoring-surface diff with no candidate lesson noticed:
      the caller decided `invoke` on the conjunction's clause 3 (`a diff touching no authoring surface`), quoted it, and
      recorded that "no candidate lesson noticed" is not a skip basis; the invocation ran (this session executed it) and
      returned `nothing durable (net 0)`, the ordinary run-found-nothing outcome recorded as a 0 delta rather than a
      skip. (b) A `README.md`-only unit with `formatting`/`wording` rounds: all four clauses held, the caller decided
      `skip` and wrote "- [x] Lesson capture: skipped under the skip condition in full — round classes
      `formatting`/`wording` only; no compliance miss; diff is `README.md` alone (no authoring surface); no problem hit
      is recorded in a `captured:` rule or `docs/ideas.md`." Both fixtures' premises, decisions, and record lines are in
      `fixtures/caller-decision-a.md` and `fixtures/caller-decision-b.md`; no file, worktree, or session was created by
      either run. The difference from the old predicate in (a) is a comparison of the two contracts, not an observation
      of a past runtime run.
- [x] 2.8 Make the `lesson-capture` decision: run the subagent and apply its durable proposals, or record the skip and
      its reason. Verification: the applied net `AGENTS.md`/pack/skill delta, or the recorded skip and reason. **Done**:
      the capture ran (the diff touches authoring surfaces). One durable proposal, applied: the "recurring unit type"
      bullet's README-site clause now reads "whenever what it reports **or how it is invoked** changes", with
      `captured: align-capture-invocation-contract` appended to the bullet. Its evidence was verified before applying —
      `ce6e1269` added the agent-table row "(evidence-gated)" while changing only the invocation gate, and `acba49c5`
      changed the row's cadence ("after every change" → "after every unit") — so the clause under-stated the row's
      scope. Net `AGENTS.md` +1 line / +67 bytes (2,595 → 2,596; 249,990 → 250,057): a five-word condition replacement
      plus one marker, with the paragraph reflowed by `spotlessApply`. No retirements, no authoring-system
      recommendation; the capture also corrected the hand-off's premise (the fourth site was found by `review-quick`
      round 1, not by a consultation) and rejected a "six rounds" lesson as a process count.
- [x] 2.9 Ask the owner for the manual review pass and end the turn. Verification: the request is made, and nothing is
      committed, pushed, archived, or merged before the owner answers. **Done**: the pass was requested with the
      implementation, the review trail, the gates, the capture record, and the two fixture records; nothing was
      committed, pushed, archived, or merged.
- [x] 2.10 After approval: archive (`openspec archive align-capture-invocation-contract --yes`), stage the move and the
      main-spec sync, and ship per `ship-change`. No capability `Purpose` refresh is owed — the Purpose does not
      describe the invocation contract. Verification: the archived change and the synced spec are in the same pull
      request. **Done**: the owner approved on 2026-10-23; the change is archived as
      `2026-10-23-align-capture-invocation-contract` with the main spec synced in the same commit, and no `Purpose`
      refresh was owed.
