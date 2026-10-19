# Tasks — a capture addition states its decision difference

## 1. The decision-difference test

- [x] 1.1 `.opencode/agent/lesson-capture.md` — the promotion gate gains the decision-difference test, stating the
      before/after pair, the **already covered** disposition, and that an incident, example, citation, or marker alone
      is not a difference.
- [x] 1.2 `.opencode/agent/lesson-capture.md` — the replacement preference: an exact replacement of the existing rule
      over appended qualifications, preserving scope, exceptions, and normative strength, with the incident evidence
      left behind the `captured:` marker.
- [x] 1.3 `.opencode/agent/lesson-capture.md` — the report contract's proposal budget names the decision difference as a
      required field of each proposal, and its collapsed line names the already-covered disposition.
- [x] 1.4 `AGENTS.md` — the capture workflow's gate pointer names the decision-difference test, so the calling agent
      checks the proposal's increment before applying it — both the gate pointer and the Decision-point edit that defers
      the branch's remedy to the gate test. Single-source the test: state it in the subagent definition and the spec,
      and let this site point at it.

## 2. The spec delta

- [x] 2.1 `openspec/changes/lesson-capture-decision-difference/specs/showcase/quality/agent-skills/spec.md` — a
      `MODIFIED` block carrying the whole capture requirement, with the test's two clauses added to the "Lessons are
      captured after implementation" scenario and every existing scenario preserved in order.

## 3. Validation

- [x] 3.1 Restart OpenCode (or start a new session) before smoke-testing the **edited** `lesson-capture` definition,
      since an edited subagent is served its pre-change body in-session, then smoke-run it against a seeded unit whose
      lesson **restates an obligation the target bullet already carries** (it must report it `already covered`, not as
      an addition) and one whose lesson **changes the decision** (it must report the before/after pair and propose the
      replacement). This gates the archive. **Done** — run after an OpenCode restart (session `ses_eacfc8167ffe3BXgnI7VxvGCos`), the registered body
      confirmed first (`opencode debug agents` carries all four new tokens), against a seeded unit. The run applied the
      decision-difference test: **Lesson A** (a restatement) reported `already covered` with the before/after pair, noting
      the pre-commit guard enforces it mechanically; **Lesson B** was rejected on the gate's **True** criterion after the run
      checked the seed's premise against the repository — `package.json`'s e2e script installs chromium itself, so the
      proposed lesson asserted a cause the task cannot produce. The re-seed replaced a first B whose premise the previous
      unit's capture had already covered. Branch exercised: `already covered` and a gate rejection; the **apply** branch
      was not, because both seeds resolved to a no-addition disposition.
- [x] 3.2 Re-run the historical validation cases against the applied instruction and confirm each disposition holds.
- [x] 3.3 `openspec validate --all` and the touched files' gates.

## 4. Records

- [x] 4.1 The proposal review gate ran, its rounds recorded in `docs/review-rounds.tsv`. **Done** — three rounds in one
      session (`ses_ead24130dffePYXEaqMq2lpLdt`): round 1 raised six
      (`premise,contradiction,missing-task,claim-unreproducible,coherence,structured-writing`), round 2 the same six
      with `claim-unreproducible` and `coherence` repeating, round 3 clean. The two repeats were a corrected fact not
      swept across its sibling sites; the sweep was the remedy and it landed.
- [x] 4.2 The deep-review routing decision is recorded on this task. **Done** —
      `python3 scripts/review-routing.py --change lesson-capture-decision-difference` returns `NOT_NEEDED`: the changed
      paths are an agent definition, `AGENTS.md`, and the change dir, carrying none of the path classes; no changed
      source declares a security filter chain; and the delta's requirement-block kind is a `MODIFIED` refinement of an
      existing requirement rather than a new contract. No deep-review session was created. **Escalated, not absorbed:**
      whether the workflow branch's unchanged remedy text should now defer to the gate's dispositions.
- [x] 4.3 The capture decision for this unit is recorded. **Done** — it ran (session `ses_ead083384ffes7xvvhNU134Iqf`),
      owed because the diff touches an authoring surface and rounds 1-2 recorded compliance misses of existing rules.
      Verdict: `nothing durable (net 0)` — applied net `AGENTS.md` delta **0**. It applied **this unit's own
      decision-difference test to its own candidates** and reported each `already covered`: the sweep discipline (the
      review-gate Decision point and the doc-consistency gotcha), the premise-at-`HEAD` rule, and the interrupted-loop
      lesson (which also fails the gate's **True** — the interruption is conversation-only history). No retirements and
      no authoring-system recommendation, since the unit _is_ the mechanism fix.
- [x] 4.4 The `## Purpose` decision is recorded — whether the requirement's scope change falsifies the capability's
      Purpose text. **Done — no refresh is owed.** The Purpose describes the capability's coverage; this change refines
      one requirement's clauses, adds no subagent, and widens no class the Purpose names, so its text is not falsified.
