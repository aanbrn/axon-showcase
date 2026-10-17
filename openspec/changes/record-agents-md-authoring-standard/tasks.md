# Tasks

## 1. The authoring standard in the spec

- [x] 1.1 `openspec/changes/record-agents-md-authoring-standard/specs/showcase/quality/agent-skills/spec.md` — the
      `ADDED` requirement "AGENTS.md entries follow a stated authoring standard": the representation taxonomy and the
      one-authoritative-definition rule, the procedure and gotcha shapes, the evidence and no-invention rules, workflow
      integration, the validation pass, and the two scenarios naming the subagents that apply it. **Done** (written at
      the proposal).

## 2. The two `AGENTS.md`-specific subagents

- [x] 2.1 `.opencode/agent/lesson-capture.md` — add the authoring-standard block: the representation the entry takes,
      the procedure/gotcha shape, the evidence and validation discipline, pointing at the spec for the normative text.
      **Done.**
- [x] 2.2 `.opencode/agent/agents-auditor.md` — add the standard-conformance check: an entry that violates the standard
      is a finding with the rewrite that conforms it, using the existing `structural` severity. **Done** — folded into
      the consistency axis, so the "two axes" framing and the spec's "consistency and conciseness" summary hold.
- [x] 2.3 `.opencode/commands/audit-agents.md` — name the standard check in the step-1 audit description. **Done.**

## 3. `AGENTS.md` and `README.md`

- [x] 3.1 `AGENTS.md` — the "Capture lessons" workflow's "How a captured rule is recorded" block gets one bullet
      pointing at the standard, so the main agent applies it to a direct edit. **Done** — a pointer, not a condensed
      copy of the standard.
- [x] 3.2 `README.md` — the `/audit-agents` prose enumeration of the audit's consistency sub-checks gains the
      standard-conformance check. **Done.**

## 4. Verification

- [x] 4.1 `./gradlew spotlessApply spotlessCheck` and `openspec validate --all` pass; the 120-character check over the
      changed set is clean — the >120 lines are all formatter-owned markdown (the `design.md` claims table, the
      `README.md` tables and links) or the unwrappable `docs/review-rounds.tsv` record rows, plus one pre-existing
      `AGENTS.md` gotcha line; no non-formatter-owned line added by this change exceeds 120. **Done.**
- [x] 4.2 Run the `review-quick` subagent over the planning artifacts, then the implementation, repeating until it
      reports no new observations; record each round in `docs/review-rounds.tsv`. **Done** — recorded in
      `docs/review-rounds.tsv`; the loop repeats until a round reports no new observations.
- [x] 4.3 Smoke-run each edited definition after a reload, with a control each. **Done — ran after the owner's restart;
      both definitions picked up their edits.**
  - `agents-auditor`: given two candidate `AGENTS.md` entries, it reported the non-conforming one (a cause and a fix
    asserted as fact, no evidence) as a `structural` finding — the added standard-conformance check fires — and cleared
    the conforming one (the CORS-headers rule, verified against all four surfaces the entry names). Beyond the seed, it
    verified the bad control's asserted fix is contradicted by the `refresh-projection-writes` recorded decision.
  - `lesson-capture`: given a synthetic note, it shaped the durable item (representation: decision rule; target: the
    review gate's step-1 derivation rule) and rejected the trivia (an awkward-title preference) with reasons — the added
    authoring-standard block is applied.
  - The capture smoke-run surfaced a third durable rule (derive each record figure from the command that emits it); its
    premise was verified against the tree — this unit's four `claim-unreproducible` rounds, plus
    `verify-unique-review-rounds`'s recorded `+2` against the measured `+5` — and applied (see 4.4).
- [x] 4.4 Run the `lesson-capture` subagent over the diff, apply its durable proposals, and record the applied net
      `AGENTS.md` delta. **Also report** any rule this change obsoletes or makes redundant. **Done** — 3 durable
      proposals (2 from the capture run, 1 surfaced by the 4.3 smoke-run), 0 retirements: three merges into existing
      review-gate bullets (the record-figure derivation in the review gate's step 1; the intra-artifact sibling in the
      scope-sweep bullet; the handover written from the artifact in the instance-hand-off bullet). Applied net
      `AGENTS.md` delta **+10 lines** (`git diff -U0 AGENTS.md` header counts `+9/−4`, `+4/−1`, `+5/−3`), plus this
      change's own +3-line pointer bullet and the +1-line agents-auditor conventions extension; the change's whole
      `AGENTS.md` delta is +14 lines (`git diff --numstat AGENTS.md` → `+35/−21`). No retirements were warranted.
- [x] 4.5 Ask the user for the manual review pass once 4.2 is clean and 4.4 is done; do not commit or archive before
      they approve. **Done** — the owner restarted (so the definitions reloaded) and approved.

## 5. The change's own records

- [x] 5.1 Record the pre-write derivation pass (every unit owes it): the claims table is in `design.md`, its rows
      derived from the corpus and the three refactor commits, with the `scripts/claim-scan.py` candidates (base
      `origin/main`) each resolved into a row or dismissed beside the table. **Done** — the pass ran; one row (the
      "procedure's slots" claim) is derived from `AGENTS.md` at HEAD.
- [ ] 5.2 Append this unit's review-round rows to `docs/review-rounds.tsv` as each round's report arrives.
- [ ] 5.3 **Owed — for the archive commit**: the `agent-skills` `## Purpose` enumerates what the capability holds and
      now omits the authoring standard. A delta cannot carry a Purpose, so the edit lands with the archive; the
      implementing PR's diff therefore carries no main-spec edit beyond the delta, as the report states.

## Workflow follow-up

- Archive the change after the owner approves and 4.3 has run; the archive commit rides this PR.
