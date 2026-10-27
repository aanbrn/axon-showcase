# Design — extend the owner-reply language scope

## Context

`unify-agent-language-policy` (#707, `ea1ee3b5`) made one destination-based output-language rule operational in
`AGENTS.md`. It left the question of compliance open by design: its Non-Goal records that no in-repo check can observe
how a model phrases a reply (archived `design.md:49-50`), and its own verification probes were one-shot.

The owner reports that the mixed-language replies continue, "at least in one of the recent sessions". The caller located
two post-#707 patterns by a read-only query of the owner's local session store, both on the pinned
`opencode-go/deepseek-v4.1-flash`, and the consultation of 2026-10-27 (session `ses_e83f4b90dffewAsyNo4cpj5pK3`)
assessed those excerpts:

- in `ses_e84c9b955ffeY6V3z6ZN1VELva`, a Russian request at 13:20 received English progress narration at 13:20:02 and
  13:21:56 around tool calls, and an English sentence at 13:22:18, with the Russian report presentation only at
  13:22:07;
- in `ses_e85454d8ffe3XuHaxh0mxC4e1`, the 07:27 presentation of an English advisor report kept `**Recommendation:**` and
  the `4 observations, …` line in English inside otherwise-Russian prose.

Both are the owner's environment, not repository history: the session store is outside the repository and is cited here
as the observation this change acts on, not as versioned evidence.

## Goals / Non-Goals

**Goals**

- State in the existing convention, in the owner's operational words, that every assistant message the owner can see in
  a turn is owner-facing prose — narration included.
- State that a presented report's prose labels are translated with the rest of the presentation.
- Assert both outcomes in the existing spec requirement, as scenarios.

**Non-Goals**

- A gate or any automated reply-language check; the recorded decision against one stands.
- A second operational statement or a new requirement: the `AGENTS.md` convention stays the single statement, and the
  spec body stays a pointer.
- Widening the English rule for agent-facing reports, handoffs, or durable artifacts.
- Reopening the parked audit-workflow PR-body idea in `docs/ideas.md`, which concerns a different output.

## Decisions

### D1 — The evidence shows a scope gap at a boundary the rule never names

The excerpts show English owner-visible narration and untranslated report labels despite Russian presentations, so this
is not a model ignoring the policy wholesale: each session delivered its Russian deliverable. Those outputs conflict
with the policy's audience rule, which selects the owner's language for prose the owner reads. What the convention's
destination list does not name is this class of message, so the change extends the enumeration rather than restating the
principle; why the observed outputs did not follow the audience rule is not established here.

### D2 — Clarify the existing convention and the existing requirement; add nothing new

- `AGENTS.md` owns the statement (D2 of #707): both new obligations are edits to its existing sub-bullets, so the
  destination list is still written once.
- The spec keeps its body as a pointer and gains two scenarios. A `MODIFIED` block replaces the requirement with its
  existing scenarios plus the two new ones, in the existing relative order; the header is unchanged, so `MODIFIED`
  matches.
- A new requirement was rejected: it would restate the same destination and split the corpus's vocabulary for one
  behavior.

### D3 — Report labels are prose, not identifiers

The untranslated-content bullet covers identifiers, paths, commands, exact quotations, and machine-readable fields. A
heading, a label such as `Recommendation`, or a count description is none of those, so translating it violates no
exception; leaving it English is what the presentation scenario now forbids. The relayed-report bullet keeps its
structure-and-evidence obligation, extended so the prose around that structure is translated.

## Site roles

| Site                                                           | Role                                                                        |
| -------------------------------------------------------------- | --------------------------------------------------------------------------- |
| `AGENTS.md` "Conventions" output-language bullet               | The operational statement — the delta asserts the outcomes, not the rule    |
| `agent-skills` requirement delta                               | Behavioral check: body points at the convention, scenarios assert the cases |
| `openspec/specs/showcase/quality/agent-skills/spec.md` Purpose | Refresh owed at archive — a delta cannot carry a Purpose                    |
| `docs/process-defects/extend-owner-reply-language-scope.md`    | The unit's own defect record                                                |

`README.md` and `.opencode/commands/consult.md` need no edit: the README's summary ("receive replies in it") stays true,
and `consult.md` step 7 already says "in the owner's language"; the convention now defines what that covers.

## Claims table

Base: `50799b60b35dcbc6790b0a1c7ecf773bf73f4594` (`git merge-base HEAD origin/main`, re-derived after the branch was
created).

| Claim                                                                                            | Command                                                                                                                                                                                                 | Result at base                                                                                 |
| ------------------------------------------------------------------------------------------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------- |
| The owner-reply bullet scopes the rule to "a reply the owner reads" and names no narration       | `git show 50799b60:AGENTS.md \| grep -A9 "Output language follows"`                                                                                                                                     | the six sub-bullets; no narration or label clause                                              |
| The requirement carries eight scenarios and none names narration or a presented report's prose   | `git show 50799b60:openspec/specs/showcase/quality/agent-skills/spec.md \| awk '/^### Requirement: Agent output language/{f=1} /^### Requirement: Process defects/{f=0} f' \| grep -c '^#### Scenario'` | 8 scenarios; the same block has zero hits for `narration` or `labels`                          |
| The requirement body is a pointer to the convention, not a second statement                      | `git show 50799b60:openspec/specs/showcase/quality/agent-skills/spec.md \| sed -n '/^### Requirement: Agent output language/,+3p'`                                                                      | "following the output-language convention stated once in `AGENTS.md`'s "Conventions" section"  |
| `consult.md` step 7 already requires the owner's language                                        | `grep -n "owner's language" .opencode/commands/consult.md`                                                                                                                                              | line 53                                                                                        |
| No build gate reads reply language                                                               | `grep -rn "language" build.gradle.kts scripts/*.py build-logic/src/main`                                                                                                                                | one hit, `JavaLanguageVersion.of(...)` in `java-conventions.gradle.kts:17`                     |
| The no-gate decision is recorded in #707's design                                                | `sed -n '49,50p' openspec/changes/archive/2026-10-27-unify-agent-language-policy/design.md`                                                                                                             | "A gate that enforces the language: no in-repo check can observe how a model phrases a reply…" |
| The README summary remains accurate                                                              | `grep -n "Talk to the agents" README.md`                                                                                                                                                                | line 366                                                                                       |
| #707's verification probes were one-shot, so compliance was never established                    | `grep -in "evidences the case" openspec/changes/archive/2026-10-27-unify-agent-language-policy/tasks.md`                                                                                                | two hits (lines 113, 117)                                                                      |
| Post-#707 sessions show English narration and English report labels (owner-environment evidence) | session-store query on `opencode.db` (`session_v2`, `session_message`), 2026-10-27                                                                                                                      | the two excerpts in Context                                                                    |

## Risk and verification

- The change adds wording only, so it cannot regress a gate; `openspec validate --all` and `spotlessCheck` are the
  mechanical checks.
- The falsifier is behavioral: one owner-facing turn in Russian that uses tools and presents an English agent report
  must show Russian narration throughout and translated labels, with identifiers and quotations untouched. A single
  clean turn establishes that case, not universal reliability.
- If mixing persists after the boundary is this explicit, the next step is runtime compliance, not more wording.
