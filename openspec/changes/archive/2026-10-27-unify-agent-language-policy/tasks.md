# Tasks — unify the agent output-language policy

`Workspace: unit=unify-agent-language-policy; path=/Users/afanasyev/Development/axon-showcase-unify-agent-language-policy; branch=unify-agent-language-policy; writer=ses_e871281c8ffeAE76YXQnwbedmt`

## 1. Planning record

- [x] 1.1 Run the review gate's pre-write derivation pass over the planning artifacts and record its output as the
      claims table in `design.md`, each row anchored to `git merge-base HEAD origin/main`
      (`a539afc370203be0dfebb0f8d53b5bf7deec8bda`). Verify each row's command reproduces its stated result; re-derive
      the table after the unit's last edit to its own artifacts, and again after a branch refresh moves the base.
- [x] 1.2 Keep the delta at the mirrored path
      `openspec/changes/unify-agent-language-policy/specs/showcase/quality/agent-skills/spec.md` and run
      `openspec validate --changes`; record the result.

## 2. The convention and the requirement

- [x] 2.1 Write the `REMOVED` block for "A consultation report follows the language rules" with a `**Reason**:` and a
      `**Migration**:`, and one `ADDED` requirement whose body points at the `AGENTS.md` convention and whose scenarios
      cover the destination `design.md`'s trace maps. Verify `openspec validate --changes` passes with no
      requirement-length warning.

## 3. Pointers and the reconciled sites

- [x] 3.1 In `AGENTS.md`'s "Conventions" section, after the `**"OpenCode" is capitalized in prose**` bullet, add one
      convention stating that output language follows the output's audience, with these branches as separate sub-bullets
      rather than one sentence:
  - a reply the owner reads uses the owner's current request language, unless they explicitly requested another, and
    follows a later switch;
  - an agent-facing report is English, and so is the prompt or handoff an agent sends another agent — including the
    prompt the main agent composes for a `subagent` invocation, whatever language the owner's request used;
  - a durable artifact is English, a chat-composed draft included;
  - identifiers, paths, commands, exact quotations, and machine-readable fields stay untranslated;
  - a relayed report keeps its structure, evidence, and uncertainty;
  - IF the owner's language cannot be established, THEN ask rather than guess.

  Verify `grep -n "Output language follows the output" AGENTS.md` — the phrase that does not wrap — that the
  explicit-request exception is present, and that the bullet copies no spec scenario and no definition's procedure.

- [x] 3.2 In `.opencode/agent/technical-advisor.md`, replace the `**Language.**` section's three bullets with one
      pointer that names the `AGENTS.md` convention and tells the advisor to read that section, keeping the handoff's
      "requested response language" field in its input list. Verify
      `grep -n "output-language convention" .opencode/agent/technical-advisor.md`, that the pointer names the read, and
      that the definition no longer restates the three rules.
- [x] 3.3 In `.opencode/commands/consult.md`, rewrite step 7's "Present the report as returned" to present it in the
      owner's language while preserving its structure, evidence, and uncertainty, keeping the recommendation-first rule,
      the stop, and the authorization boundary. Verify `grep -n "as returned" .opencode/commands/consult.md` finds
      nothing and the step still forbids re-summarizing.
- [x] 3.4 In `README.md`, put the one-sentence human summary the site-role table assigns in "The Agentic Process" (after
      its first paragraph) with the pointer to the `AGENTS.md` convention, and remove the advisor-only language sentence
      from the independent-consultation paragraph. Verify with `grep -n "Owner-facing reports" README.md` (no match),
      `grep -n "Talk to the agents in your own language" README.md` (one hit in "The Agentic Process"), and that the
      consultation paragraph still states the effort and V2 boundary.
- [x] 3.5 In `openspec/config.yaml`'s `context:` conventions, add the English-artifact line. Verify the added line is
      present (`grep -n "English" openspec/config.yaml`), the block still parses (`openspec context --json`), and no
      list item contains an unquoted `: `.

## 4. Verification

- [x] 4.1 `openspec validate --all` passes, and the delta sits at its mirrored path.
- [x] 4.2 `./gradlew spotlessApply`, then `./gradlew spotlessCheck`, after the last edit to any markdown or JSON file.
- [x] 4.3 Sweep the changed set for a restated copy of the rule, deriving the set with
      `git status --porcelain --untracked-files=all`: each site carries the role `design.md`'s site-role table assigns
      it, no site but the `AGENTS.md` convention writes the destination list, and every scenario's title names every
      behavior its WHEN/THEN asserts.
- [x] 4.4 Check `docs/adr/` and `docs/guidance-evidence/` for a language statement the change makes stale
      (`grep -rn "owner-facing\|agent-facing\|language of the owner" docs/`); correct any hit.
- [x] 4.5 Run the 120-character check over the changed markdown with the boundary-safe recipe
      (`perl -CSD -lne 'print if length > 120'`), treating a formatter skip as a failure for that file.

## 5. Archive-stage edits

- [x] 5.1 Apply the `## Purpose` refresh for `showcase/quality/agent-skills` in the archive commit: name the
      output-language convention beside the structured writing standard, and that it is stated once in `AGENTS.md`. A
      delta cannot carry a Purpose, so the edit belongs here and the change's report states the deferral. **Done** —
      the Purpose now names the output-language convention beside the structured writing standard.

## 6. Review, routing, and capture

- [x] 6.1 Run the `review-quick` loop through the `review-gate` skill's preflight, appending each round's row (with the
      returned session identifier and transition) to `docs/review-rounds.tsv` as its report arrives, and run both ledger
      checks plus `--expect-clean` before the manual pass.
- [x] 6.2 Derive the deep-review routing outcome with
      `python3 scripts/review-routing.py --change unify-agent-language-policy`, decide at or above it, and record the
      outcome and its signals on this task. **Done** — `REQUIRED`, with the signals `requirement-block-added` and
      `requirement-block-removed` on `specs/showcase/quality/agent-skills/spec.md`; the deep pass is run through
      `/review-thorough` and its findings recorded below. **Deep pass 1** (session `ses_e85820e5affek9rY3k01k0FqeC`,
      initial review): verdict `nothing remains`, no findings to record, so no finding carries a status.
- [x] 6.3 Decide the `lesson-capture` run against the skip condition; run the subagent when it does not hold, apply its
      durable proposals, and record the applied net delta or the skip and its reason here. **Done** — the skip condition
      failed (rounds raised classes beyond the local set, and the diff touches `.opencode/` and `AGENTS.md`), so the
      subagent ran (`ses_e857daa0effeNjjMLAhFxLPK7h`) and proposed two additions, both applied: a
      `check-and-test-authoring` mode on a verification command discriminating the edit from its absence, and a
      `review-gate` §5 base-refresh precondition. Net `AGENTS.md` delta 0 (both landed in skills); no rule retired. Its
      collapsed candidates are recorded in the capture record at the end of this section.
- [x] 6.4 Confirm the edited advisor definition is the registered one and that its pointer works, in this order,
      recording which order the run used: (1) run `AGENTS.md`'s probe task before saving any marker, so a marker
      arriving from elsewhere fails it; (2) save the marker line in `.opencode/agent/technical-advisor.md`; (3) re-run
      the probe and require the marker; (4) remove the marker; (5) re-run the probe and require that no marker is
      defined, which is what proves the marker-free body is the registered one; (6) run one owner-facing consultation in
      the owner's current language and confirm it answers in that language with the recommendation first; (7) re-run
      `spotlessApply` and `spotlessCheck`, since the definition is formatter-owned. This smoke-run is a precondition for
      archiving. Record the run's evidence and the surfaces it does not establish. `docs/ideas.md`'s 2026-10-26 entry
      parks the missing removal step in the rule itself. **Done** — baseline probe `ses_e8571bebcffeZxGsj7vYl6dP00`
      answered "no marker is defined"; with `PROBE-MARKER: pinecone-47` saved, `ses_e85714e06ffenplcxYNe9JeEmj` echoed
      it; after removal, `ses_e8570e3a1ffePQ2eKJA7kSvKlt` answered "no marker is defined", so the shipped marker-free
      body is the registered one; the Russian consultation `ses_e85709636ffejyHJrfbMhIYrVO` answered in Russian,
      recommendation first, with its contract; `spotlessApply`/`spotlessCheck` re-ran green afterwards. Not established:
      that every later session reloads the same body, and any V1 behaviour.
- [x] 6.5 Run the defect's own check after implementation: in an ordinary request in the owner's language, delegate to a
      subagent and record the exact `subagent` prompt the main agent composed and the language the subagent answered in.
      The prompt must be English; task 6.4's owner-language reply exercises the owner-facing side only and does not
      evidence this one. A prompt that is not English reopens the `AGENTS.md` convention's wording rather than being
      recorded as a pass, and the record states that one probe evidences the case, not universal reliability. **Done** —
      the probe prompt composed for the advisor consultation `ses_e85709636ffejyHJrfbMhIYrVO`, in a Russian-language
      owner thread, was English in full ("Owner-facing consultation, requested language: Russian. Owner's question,
      verbatim: …"), and the subagent answered in Russian; the review-round hand-overs were the same shape. One probe
      evidences the case, not universal reliability.
- [x] 6.6 Ask the owner for the manual review pass and end the turn; commit, push, archive, and merge only after that
      approval.

**Capture record** — `lesson-capture` session `ses_e857daa0effeNjjMLAhFxLPK7h`; applied net `AGENTS.md` delta 0: two
skill additions, no rule retired.

| Candidate                                                                                    | Disposition                                                                                                           |
| -------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------- |
| Round 1's `test-control`: a verification command that passes with its edit absent            | applied — a `check-and-test-authoring` mode ("A verification step must discriminate the edit from its absence")       |
| The closing round passing while `origin/main` had moved, leaving the uncommitted tree behind | applied — a `review-gate` §5 base-refresh precondition                                                                |
| Round 10: a file-wide SHA replacement rewrote the historical refresh mention                 | already covered — the programmatic-edit gotcha's substring-replacement rule                                           |
| Round 18: the `AGENTS.md` branch copied the delta scenario's artifact-class enumeration      | already covered — the single-sourcing invariant                                                                       |
| The near-miss `audit.yml` clause with its delta and cloud dispatch                           | rejected — one moderate-cost near-miss, caught before any edit, and parked in `docs/ideas.md` with its reopen trigger |
| Naming "the prompt a caller composes for a `subagent`" instead of "the inter-agent handoff"  | rejected — one occurrence, fixed in planning; a general rule would over-generalize a single drafting improvement      |
| `missing-task` (five rounds) and `claim-unreproducible` (four rounds) recurrences            | withheld — no per-instance evidence, so no shared cause could be confirmed                                            |
