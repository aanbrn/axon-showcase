# Tasks

## 1. The cleanup mode

- [x] 1.1 `.opencode/commands/ideas.md` — a `cleanup` mode: one bounded batch, oldest section first, each entry shown
      with its own text; the owner disposes `keep` / `narrow` / `promote` / `drop`; removals are presented before they
      are applied; a drop resting on "this shipped" verifies that claim first and routes the durable lesson to its
      destination; a retained entry keeps its date and needs no re-justification; no renewal dates and no semantic
      verification of every entry. The batch takes the oldest section after the header's `**Last cleanup:**` cursor and
      advances it to the covered section once every entry has a disposition, with a terminal branch once all are
      covered.
- [x] 1.2 The same file's `description` and decision points name the mode and keep the existing append/print behaviour
      unchanged; `README.md`'s ideas paragraph and its `/ideas` row name the cleanup beside the append.

## 2. The audit loses the parked-entry lifecycle

- [x] 2.1 `.opencode/agent/architecture-auditor.md` — remove the two parked-entry sweep bullets (work landed; own
      condition met), the parked member of the premise-met lead, the parked half of the inverse-class bullet, and the
      parked-entry distinction sentence; narrow the classification block and the frontmatter to the ADR condition.
- [x] 2.2 The same file's Scope line keeps `docs/ideas.md` as evidence for a deferred architectural decision's
      rationale, and says so, rather than as a lifecycle surface.
- [x] 2.3 `.opencode/commands/audit-architecture.md` — its description and step 1 name the narrowed class; the trigger
      and the lifecycle block are unchanged.
- [x] 2.4 `AGENTS.md` — the architecture-auditor bullet's premise-met summary, swept-surface clause, and classification
      sentence drop the parked-entry members; the `docs/ideas.md` reach sentence returns to the deferral-rationale
      reading; the generalization's capture rule states when it checks a parked entry's condition without claiming a
      periodic sweep.
- [x] 2.5 `README.md` — the architecture-auditor paragraph and its agent and slash-command rows state what the audit
      reports now.
- [x] 2.6 Sweep the class's own tokens (`parked`, `landed`, `due`, `revisit`) across the working tree, untracked files
      included, and correct every live site; archived changes and dated records stay as recorded.

## 3. The scratchpad's own statement

- [x] 3.1 `docs/ideas.md` — the header states the pruning discipline (nothing in the file is a commitment, and an entry
      nobody wants is dropped at a cleanup rather than kept by inertia) and carries the `**Last cleanup:**` cursor line.

## 4. The spec delta

- [x] 4.1 The delta at the change's `specs/showcase/quality/agent-skills/spec.md` is `REMOVED` (the current header, with
      its Reason and Migration) + `ADDED` (the retitled, narrowed requirement carrying the six retained scenarios in
      their original order and under their original names).
- [x] 4.2 The design's obligation trace has one row per clause of the removed requirement, each marked retained or
      retired, and every retired scenario is named.
- [x] 4.3 `openspec validate --all` passes.

## 5. Verification

- [x] 5.1 `./gradlew spotlessApply` after the final edit, then `spotlessCheck`, the PR gate
      `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`, `verifyTableRows`, and `openspec validate --all` are
      clean.
- [x] 5.2 Run the cleanup procedure read-only over the oldest batch: it must present that batch with its dispositions
      and stop before any removal, recording which entries it offered. **Done** — the oldest section (`## 2026-09-01`)
      holds one entry, "Managed-k8s staging for free or cheaply"; the procedure presented it with its text and a
      proposed `keep` (its open decision — how faithful staging must be to the observability stack — is unresolved), and
      applied nothing, and the cursor stays `none` until a batch's dispositions are applied — so a plain re-run would
      offer the same section again, which is the intended behaviour for an unanswered batch.
- [x] 5.3 Run the **seeded smoke-run** of the narrowed auditor: save a byte copy of `docs/ideas.md`, seed it with three
      entries — one whose described work exists, one whose recorded condition is checkable and met, and one plain note —
      and run `architecture-auditor` once with a neutral prompt. The first two must **not** be reported (the class is
      gone); the ADR-0003 gate the audit already reports must still be, which shows the boundary held. Restore the copy
      byte for byte, on a failed run too, and record the verdict line and which control each item came from. **Done** —
      two runs. Run A (the three seeds, no synthetic control) returned `0 findings, 1 advisory`: none of the seeds was
      reported, but ADR-0003's gate — which two earlier runs surfaced as "substantially met on one axis" — was silent,
      so that run established only that the seeds were not reported. Run B added a synthetic control, a temporary
      `docs/adr/9999-smoke-control.md` whose `Revisit when:` ("the `include(` block in `settings.gradle.kts` lists more
      than ten modules", met at 19) is unambiguous: it returned `0 findings, 2 advisory`, reporting that ADR as a
      deferral whose condition is met and again reporting none of the three seeds — the retained class fires and the
      retired class does not, in one run. Both the temporary ADR and `docs/ideas.md` were restored (`332e6bd8…`); the
      run also surfaced the `docs/decisions/` boundary gap, already parked as an idea.
- [x] 5.4 Run the `review-quick` loop over the proposal, then the implementation, appending each round's row to
      `docs/review-rounds.tsv` as its report arrives, until a round reports nothing.
- [x] 5.5 **Routing `REQUIRED`** (`[requirement-block-added]`, `[requirement-block-removed]`) — deep pass 1 reports one
      should-fix: the cleanup command's durable-lesson enumeration diverged from the header's mapping (a subject pack
      the header never names, the retrospective it omits, `docs/decisions/` grouped under the wrong class). Fixed by
      dropping the inline enumeration and keeping the pointer. Pass 2 (same session): `nothing remains`, F1
      `FIXED_AND_VERIFIED`, no other finding. **Pass 3** — run past the default two-pass budget on a recorded decision:
      a technical-advisor review found the cleanup could never advance past a kept oldest section, so the change added
      the header cursor and corrected the capture-timing wording _after_ pass 2. That is a material change to the new
      mechanism, so a verification pass was worth its cost; it reports `nothing remains`.
- [x] 5.6 Ask for the owner's manual review pass and stop there. **Asked** after the deep review closed; the PR waits on
      the owner's answer, then archive and merge.

## 6. The change's own records

- [x] 6.1 **Owed — for the archive commit**: refresh the `agent-skills` `## Purpose`, whose premise-met clause names a
      parked entry's work and condition; a delta cannot carry a Purpose.
- [x] 6.2 Record the pre-write derivation pass: the claims table is in `design.md`.
- [x] 6.3 Write `docs/process-defects/scope-ideas-to-owner-cleanup.md`, or state that the unit observed none. **Done** —
      one record: removing the class left a `due-recurrence` clause in a sibling bullet, found by the token sweep.

## Workflow follow-up

- Archive the change after the owner approves; the archive commit rides this PR.
