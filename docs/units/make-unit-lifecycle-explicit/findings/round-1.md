- [contradiction] `openspec/changes/make-unit-lifecycle-explicit/proposal.md:48` — the Impact both adds a Gradle task
  wired into `check` and states no build behavior change.
- [contradiction] `openspec/changes/make-unit-lifecycle-explicit/specs/showcase/quality/agent-skills/spec.md:201` — the
  amended preflight description permits the narrow exceptions while the retained scenario still forbids publishing
  unconditionally.
- [contradiction] `openspec/changes/make-unit-lifecycle-explicit/design.md:52` — decision 2 keeps the checkpoint's
  `Stage` operation-scoped while task 4.2 aligns it with the lifecycle stage names.
- [claim-unreproducible] `openspec/changes/make-unit-lifecycle-explicit/design.md:175` — `opsx-tool-update` is
  project-authored despite the prefix, so the generated set is 6 and the project-authored set 25.
- [missing-task] `openspec/changes/make-unit-lifecycle-explicit/design.md` — no per-clause quote of the two amended
  requirements' changed clauses, and decision 2 says the states are unchanged where the delta adds a Begin write.
- [missing-task] `openspec/changes/make-unit-lifecycle-explicit/tasks.md` — no task refreshes either capability's
  Purpose.
- [missing-scenario] `openspec/changes/make-unit-lifecycle-explicit/specs/showcase/quality/commit-hygiene/spec.md:5` —
  decision 5 verifies generated-mapping coverage with no such predicate or scenario in the delta.
- [test-control] `openspec/changes/make-unit-lifecycle-explicit/tasks.md:64` — task 5.3's Done criterion names four
  cases; the preflight-anchor and generated-mapping predicates would ship without a known-bad case.
- [test-control] `openspec/changes/make-unit-lifecycle-explicit/design.md:85` — "verified by running the Docker-free
  check" cannot fail.
- [claim-unreproducible] `openspec/changes/make-unit-lifecycle-explicit/design.md:168` — the claims table anchors no
  base revision while its rows cite line ranges the change's own edits move.
- [single-sourcing] `openspec/changes/make-unit-lifecycle-explicit/specs/showcase/quality/agent-skills/spec.md:79` — the
  Begin requirement restates the preflight requirement's workspace trio.
- [wording] `openspec/changes/make-unit-lifecycle-explicit/specs/showcase/quality/agent-skills/spec.md:204` — "the
  previous requirements state" resolves to nothing at archive.
- [claim-unreproducible] `openspec/changes/make-unit-lifecycle-explicit/design.md:182` — the script's opening lines do
  not state that session movement is the caller's; line 15 does.
- [coherence] `openspec/changes/make-unit-lifecycle-explicit/design.md:70` — the design's Lifecycle value set includes
  `outside-unit` while the requirement recognizes only `cross-cutting`.
- [structured-writing] `openspec/changes/make-unit-lifecycle-explicit/design.md:57` — decision 3 packs nine procedure
  steps into one sentence, and decision 5 five predicates into another.
- [structured-writing] `openspec/changes/make-unit-lifecycle-explicit/specs/showcase/quality/agent-skills/spec.md:117` —
  two requirements each carry two behaviors in one body.
- [formatting] `openspec/changes/make-unit-lifecycle-explicit/design.md:173` — raw pipes inside inline code spans split
  the table's cells and fail `verifyTableRows`.
