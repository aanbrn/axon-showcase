# Design — unify the agent output-language policy

## Context

The owner reports the failure the change removes, and refined it in this session: the documentation is English and that
is not the problem — the problem is that the main agent uses different languages when it talks to different subagents.
The advisor answers in the owner's language, while the prompt the main agent composes for a `subagent` invocation
changes language from call to call, so the child agents answer in mixed languages. The repository already holds one
language rule, and it is deliberately scoped to the consultant — the archived `add-technical-advisor` design (D7) states
"This scopes the rule to the consultation rather than introducing a new repository-wide language convention", and
rejects both always-English and always-request-language for the handoff case. So this change generalizes a recorded
decision rather than inventing a rule.

The owner's opening request proposed the simplest generalization: every agent answers in the language of the initiating
prompt. The consultation widened that into a destination-based policy, because at a delegation the initiating prompt
_is_ the English handoff while the output may still be for the owner.

Three facts the current state supplies:

- The rule is stated in three project-authored places — the advisor definition's `**Language.**` section, the README's
  consultation paragraph, and one spec requirement — and nowhere else (the claims table's "Only the advisor states an
  output-language rule" and "The language rule is spec'd once" rows, plus its README row).
- `AGENTS.md` states no output-language rule, so nothing governs the main agent's own replies (the "`AGENTS.md` states
  no output-language rule" row).
- Six command files carry a presentation step across seven lines, of which only `consult.md`'s "present the report as
  returned" wording literally forbids translation (the "Six command files carry a presentation or no-re-summarize step"
  row).

Mid-unit the branch was refreshed four times: onto `19857c5c`, whose `AGENTS.md` gained a definition-edit probe rule;
then onto `a24d637b`, whose `AGENTS.md` rewrite touched the branch-refresh bullet and which added `docs/ideas.md`
entries; then onto `2cfde45d`, which refined the capture rule's recurrence dispositions; then onto `a539afc3`, which
added the `/sync-branch` command. The probe rule adds the advisor smoke-run to the verification plan; no refresh changed
a decision, and every claims row was re-derived at each base.

## Goals / Non-Goals

**Goals**

- One operational statement, in the always-loaded `AGENTS.md`, that every agent can act on: output language follows the
  output's audience.
- One `agent-skills` requirement that references that statement and tests it with scenarios, replacing the
  consultation-only requirement.
- Every site that restates or contradicts the rule either points at it or is reconciled.

**Non-Goals**

- Any change to `.opencode/opencode.json`, a permission, or a model pin — no mechanism is needed for a behavioral rule.
- Translating repository documentation, specs, commit messages, or pull-request bodies.
- A gate that enforces the language: no in-repo check can observe how a model phrases a reply, and a text scan over
  agent output would pass on any English-looking artifact regardless of audience.
- Editing the audit workflow's prompt or its `merge-governance` requirement: the durable-artifact rule already covers
  its published body, and the extra clause would buy a spec delta and a full cloud dispatch (D5).
- Restating the policy at a site the site-role table (under Decisions) gives no role: the table is the set, and every
  site it lists carries its assigned role rather than a copy of the destination list.

## Decisions

### D1 — The destination audience selects the language, not the triggering prompt

| Output                                                                                                                            | Language                                                                                                   |
| --------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------- |
| A reply the owner reads, including a subagent report presented to them                                                            | The owner's current request language, unless they explicitly requested another; a later switch is followed |
| An agent-facing report, and the prompt or handoff an agent sends another agent — the `subagent` prompt a caller composes included | English                                                                                                    |
| A durable artifact: repository documentation or spec, commit message, pull-request or issue body, a report a workflow publishes   | English, including a draft composed in chat for one                                                        |
| Identifiers, paths, commands, exact quotations, machine-readable fields                                                           | Untranslated                                                                                               |

- The literal "language of the initiating prompt" proposal was rejected: a `subagent` invocation's initiating prompt is
  the caller's English handoff, so the rule would make an owner-facing report English whenever the work was delegated —
  the case the advisor is used for.
- The prompt a caller composes for a subagent is part of this rule, not an exception to it: it is the handoff the
  archived design already kept English, and the owner's observation is that its language currently varies from call to
  call. Nothing about the owner's language changes it.
- A handoff field naming the owner's language is kept as an input, not as a second statement of the rule: the handoff
  language never overrides the destination language.
- An unconditional "the owner's language" was rejected because a published pull-request body and a committed document
  outlive the conversation that produced them, and the repository's own corpus is English.

### D2 — `AGENTS.md` owns the statement; the spec references it and asserts outcomes

`AGENTS.md` is always loaded, so it is the only site an agent is guaranteed to read before it answers. The spec
requirement points at that convention by name and tests it with WHEN/THEN scenarios, the shape the repository already
uses for the structured writing standard (the "The spec already references an `AGENTS.md` standard instead of copying
it" row).

- Copying the full statement into the spec body was rejected: the spec would become a second operational copy, and the
  single-sourcing invariant makes a copy a drift surface.
- Leaving the rule only in the spec was rejected: the spec is not loaded on every turn, so the main agent's own replies
  would stay ungoverned.

### Site roles

The table lists the sites that carry the policy, and each carries one role. It is not every file the unit edits — the
change dir and the parked idea in `docs/ideas.md` ship without a policy role. The roles are stated here once; the
Non-Goals, the tasks, and the verification sweep reference this table rather than restating it.

| Site                                   | Role                                                                                               |
| -------------------------------------- | -------------------------------------------------------------------------------------------------- |
| `AGENTS.md` "Conventions" bullet       | The operational statement — the only place the destination list is written                         |
| `agent-skills` requirement (delta)     | Behavioral check: its body points at the convention, and its scenarios assert each destination     |
| `.opencode/agent/technical-advisor.md` | Pointer to the convention, with the instruction to read it                                         |
| `.opencode/commands/consult.md`        | Presentation step in the owner's language, keeping the report's contract                           |
| `README.md`                            | A one-sentence summary for human readers plus the pointer; it is a summary, not a second statement |
| `openspec/config.yaml` `context:`      | The English-artifact line for artifact generation                                                  |

### D3 — One requirement, whose body points and whose scenarios assert

The delta adds `Agent output language follows the output's audience`, whose body names the `AGENTS.md` convention as the
statement and whose eight scenarios assert what each destination receives.

- Enumerating the four destinations in the body was rejected on review: it would reproduce the convention's own
  destination list in a second place, which is the copy D2 rejects, and the scenarios already carry each case.
- Two requirements were rejected: the second one's body repeated the convention's relay and untranslated clauses, so it
  bought nothing over the scenarios; one requirement with a pointer body stays under `openspec validate`'s
  requirement-length warning and states one behavior.
- Dropping the relay scenario instead was rejected: a translated report can use the right language and still shorten the
  findings, so the outcome needs its own WHEN/THEN.

### D4 — Retitle by `REMOVED` + `ADDED`, with a per-obligation trace

`MODIFIED` matches a requirement by its verbatim header, so widening the scope while retitling it would fail
`openspec validate --changes`. The old block is removed with a reason and a migration, and every clause and scenario of
it is traced below.

### Obligation trace

| Old obligation                                                                                                         | Destination                                                                                                                                                      |
| ---------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| "An agent-facing report and the inter-agent handoff SHALL be English."                                                 | `ADDED` "Agent output language follows the output's audience" scenario "An agent-facing report and a handoff are English", carried by the `AGENTS.md` convention |
| "An owner-facing report SHALL use the language of the owner's current request unless they explicitly request another." | `ADDED` same requirement's scenario "An owner-facing reply uses the owner's language" (plus "A later language switch is followed"), carried by the convention    |
| "Identifiers and exact quotations SHALL remain unchanged."                                                             | `ADDED` same requirement's scenario "Identifiers and quotations stay untranslated", carried by the convention                                                    |
| Scenario "An owner-facing report uses the owner's language"                                                            | retained as the owner-facing scenario above                                                                                                                      |
| Scenario "An agent-facing report is English"                                                                           | retained as the agent-facing scenario above                                                                                                                      |

The delegation-prompt scenario is a widening rather than a mapping of an old clause: the old requirement had no scenario
for the prompt a caller composes, and the owner's observation is what added it.

### D5 — The five compatible presentation steps stay; only `consult.md`'s wording changes

The four audit commands and `review-thorough` say "present it in the contract it returns" and "do not re-summarize it" —
shape constraints, not language ones — so the new requirement governs their language without a wording change.

- `consult.md`'s "Present the report as returned" is the one site a translator would read as a prohibition, so it states
  the owner-language presentation directly and keeps "do not re-summarize".
- The scheduled audit's published body was left out of scope after an independent consultation narrowed the plan. Its
  prompt already says the final response becomes the pull-request body, which makes the body a durable artifact the
  convention covers; a separate clause there would owe a `merge-governance` delta and a cloud dispatch of the whole
  three-audit workflow, and would fix nothing about the defect this change exists for — the language of subagent
  prompts.
- Revisiting that workflow is parked in `docs/ideas.md` with the observable trigger that reopens it, rather than left in
  this change dir, which is archived and read by no tool.

### D6 — The advisor's definition and the README point instead of restating

- The advisor's `**Language.**` section becomes the pointer its site role assigns: it names the convention and tells the
  advisor to read that section, so the rule holds whether or not a child session receives the project instructions
  automatically. Its handoff field naming the requested language stays: that is an input, not the rule.
- The README's only mention currently sits in the "Independent consultation" paragraph and reads as advisor-only — the
  framing this change removes. The human-facing summary the table assigns therefore moves to "The Agentic Process",
  which narrates the human/agent loop the capability belongs to, and the consultation paragraph keeps only the advisor's
  own contract.

### D7 — The `openspec/config.yaml` context gains the English-artifact line

The context block is what OpenSpec shows the AI while it authors artifacts, and its convention list already restates
`AGENTS.md`'s comment ban and wrap width. An artifact written in the owner's language is the failure the policy
prevents, so the list gains the one line rather than relying on the agent to derive it.

## Risks / Trade-offs

- **No gate reads the rule.** A reply in the wrong language passes every check. Mitigation: the convention is
  always-loaded, the spec asserts it, and the owner observes it in use; the change records that a gate cannot verify it.
- **Translation loses report detail.** An owner-language rendering of an English report can drop evidence. Mitigation:
  the relay scenario and the untranslated-identifiers scenario state that structure, evidence, and uncertainty survive
  and that a translated finding is a paraphrase with the exact quotation kept.
- **`AGENTS.md` grows.** Mitigation: one convention, and the spec holds the rationale rather than the root file.
- **A V1 consumer may not load project instructions** the way V2 does. Mitigation: the rule is documentation-level, no
  config key is involved, and the advisor's own V1 limitation already stands recorded.
- **"Durable artifact" read too broadly.** A scratch file or a log line is not a published artifact. Mitigation: the
  scenario enumerates the artifact classes the rule governs.

## Claims table

Each row is a fact this change states about its evidence, with the command that derives it. The base revision is
`a539afc370203be0dfebb0f8d53b5bf7deec8bda` (`git merge-base HEAD origin/main`). The branch was refreshed onto newer
`origin/main` four times mid-unit (`19857c5c`, `a24d637b`, `2cfde45d`, then `a539afc3`), and every row was re-derived at
each base: the refreshes moved only `AGENTS.md`'s line count (2731 → 2744 → 2749 → 2755) and nothing else these rows
state.

| Claim                                                                                            | Source / command                                                                                                                                      | What it establishes, and its limit                                                                                                                                                                                                                                                                                                                                                         |
| ------------------------------------------------------------------------------------------------ | ----------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| The main agent's prompts to different subagents vary in language                                 | the owner's report in this session (2026-10-26)                                                                                                       | The problem statement the change removes; no repository artifact measures a prompt's language, so it is owner-reported and not independently verified, and the rule's effect stays owner-observable.                                                                                                                                                                                       |
| Only the advisor states an output-language rule, plus the handoff fields and the README sentence | `git grep -n "owner-facing\|agent-facing\|output-language" a539afc3 -- .opencode README.md AGENTS.md`                                                 | Returns the advisor definition's two rule lines (`:136`, `:137`) and its handoff field (`:56`), `consult.md`'s handoff field (`:31`) and the phrase "owner-facing entry point" (`:11`), and `README.md:398`. No other project-authored agent, command, or skill states a rule; the vendored skill trees lie outside the paths given and were not examined.                                 |
| The language rule is spec'd once, for the consultation only                                      | `git show a539afc3:openspec/specs/showcase/quality/agent-skills/spec.md \| grep -n "A consultation report follows the language rules"`                | One match, the header at line 1884; no repo-wide language requirement exists in that capability.                                                                                                                                                                                                                                                                                           |
| `AGENTS.md` states no output-language rule                                                       | `git show a539afc3:AGENTS.md \| grep -in 'output.language\|language of the owner'`                                                                    | No match (exit 1) against the file's 2755 lines, so the convention is new rather than a moved copy. The pattern covers the rule's own wording, not every use of the word "language".                                                                                                                                                                                                       |
| No parked idea covers this decision                                                              | `git grep -in 'language\|english' a539afc3 -- docs/ideas.md`                                                                                          | No match (exit 1): there is no parked design to adopt.                                                                                                                                                                                                                                                                                                                                     |
| Six command files carry a presentation or no-re-summarize step, in seven matching lines          | `git grep -n "as returned\|Do not re-summarize" a539afc3 -- .opencode/commands`                                                                       | `consult.md:53` and `:54`, `audit-agents.md:18`, `audit-architecture.md:18`, `audit-readme.md:16`, `audit-specs.md:15`, `review-thorough.md:28`. Only `consult.md:53`'s "as returned" forbids translation; the rest constrain the report's shape.                                                                                                                                          |
| A child session receives the project `AGENTS.md`                                                 | `https://opencode.ai/v2/docs/instructions` (read 2026-10-26) and the archived `add-technical-advisor` claims row "OpenCode adds project `AGENTS.md`…" | The page states a session loads every `AGENTS.md` from the workspace toward home and assembles instructions before session-specific entries; the archived row records that project instructions reach a child session. Limit: the page describes a session's instruction assembly, not a subagent call specifically, which is why the advisor's pointer also tells it to read the section. |
| The audit workflow already names its final response as the pull-request body                     | `git show a539afc3:.github/workflows/audit.yml \| grep -n "final response as the pull request body"`                                                  | Line 56: the workflow states the body the durable-artifact scenario governs, which is why D5 leaves that prompt unedited. The file is unmodified by this change, so the fact is read from the base revision.                                                                                                                                                                               |
| The spec already references an `AGENTS.md` standard instead of copying it                        | `git show a539afc3:openspec/specs/showcase/quality/agent-skills/spec.md \| grep -n "structured, scannable shape"`                                     | Line 1220: the "Agent-authored technical artifacts follow a structured writing standard" requirement points at `AGENTS.md`'s section and tests it by scenario — the precedent for the new requirement's shape.                                                                                                                                                                             |
| `openspec/config.yaml` carries a convention list in its `context:` block                         | `git show a539afc3:openspec/config.yaml \| grep -n "Conventions:"`                                                                                    | Line 22 of the context block: the list that gains the English-artifact line.                                                                                                                                                                                                                                                                                                               |
| The README frames the language behavior as advisor-only                                          | `git show a539afc3:README.md \| grep -n "Owner-facing reports"`                                                                                       | Line 397–398, inside the independent-consultation paragraph: the sentence this change generalizes.                                                                                                                                                                                                                                                                                         |
| The change's delta validates against the corpus                                                  | `openspec validate --changes`                                                                                                                         | It passes with the single delta written; the archive-time `openspec validate --all` is the verification task.                                                                                                                                                                                                                                                                              |
| The planned changed set is the change dir and six documentation or definition files              | the paths named in `proposal.md`'s "What Changes"                                                                                                     | Planned scope, not a diff: `AGENTS.md`, `.opencode/agent/technical-advisor.md`, `.opencode/commands/consult.md`, `README.md`, `openspec/config.yaml`, and `docs/ideas.md` (its two parked entries) — six files, no code, Gradle, chart, or workflow edit. Re-derive with `git status --porcelain --untracked-files=all` after implementation.                                              |

## Scan candidates

`scripts/claim-scan.py --base origin/main` seeds the table. Its remaining candidates are dismissed by kind:

- **`[absolute]` — normative quantifiers.** `every`, `only`, and `none` in the proposal, the spec, and the convention
  state a scope or an obligation, not an evidential fact; the text carrying the obligation is the check. The two factual
  families among them — "only the advisor states a rule" and "no parked idea covers it" — are rows above.
- **`[path]` — locators.** File paths in the proposal, the tasks, and the table's source column are instructions or
  citations; the fact each supports is a row above.
- **`[cross-ref]` — identifier groups.** `owner-facing/agent-facing`, `read/edit/write` and similar tokens are
  cross-references, not assertions.
- **`[count]` — section and step numbers.** `7 presents`, `D7`, and similar are a proposal step number and decision
  labels; every count stated as a claim is a row above.

## Unsupported requirements and owner decisions

**Unsupported, recorded rather than worked around**

- The rule is behavioral: no in-repo gate can read a reply's language or an artifact's audience, so a wrong-language
  output fails no check. The change's evidence is the diff and the review gate; its effect is owner-observable in use.
- A consumer that does not load the project instructions (a V1 or ACP session) may not receive the convention; the
  repository's recorded V1 boundary for the advisor already covers that path.

**Owner decisions requested at the manual review**

1. Approve the destination table in D1 as the policy, including "durable artifact in English".
2. Approve the planning artifacts for implementation.

## Verification plan

1. `openspec validate --all` passes.
2. `./gradlew spotlessApply` and `./gradlew spotlessCheck` pass; the markdown gate covers `AGENTS.md`, `README.md`, and
   the change dir.
3. A grep sweep confirms no site still restates the old rule: the advisor definition's language section, the README
   sentence, and the spec's old requirement are each replaced by a pointer or the new requirement.
4. `./gradlew check` is not required: no build input changes. The review gate's quick loop, its routing decision, and
   the owner's manual pass are the review evidence.
5. The policy's behavior is not verified by a test, so two observations carry it. The advisor's definition edit is
   verified as `AGENTS.md`'s definition-edit rule requires — the registered body confirmed by that rule's probe, then
   one owner-facing consultation in the owner's current language, the smoke-run the rule makes a precondition for
   archiving. The defect the change exists for is verified separately: an ordinary request in the owner's language that
   delegates to a subagent records the exact `subagent` prompt the main agent composed, which must be English — the
   advisor's owner-language reply evidences only the owner-facing side. One probe evidences the case, not universal
   reliability.

An independent consultation over this plan found that the advisor smoke-run evidences only the owner-facing half — the
advisor's existing rule already covers its reply language — so task 6.5 probes the delegation prompt itself. A later
pass narrowed the scope again: the audit workflow's prompt is left unedited (D5), which drops its delta and its
dispatch.

## Review records

The pre-write derivation pass is recorded by this claims table. Review rounds are appended to `docs/review-rounds.tsv`
with the reviewer's session identifier and transition; the change's review task records the deep-review routing outcome
after implementation.
