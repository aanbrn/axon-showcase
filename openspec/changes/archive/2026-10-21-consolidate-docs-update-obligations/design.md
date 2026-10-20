# Design

## Context

`proposal.md` asks whether the three `AGENTS.md` spans that govern documentation updates duplicate each other, and what
a consolidation that preserves every obligation would measure. This design resolves the merge criterion against the
governing requirement, drafts every option completely, measures each on the whole file, and recommends one unit or
retention.

The pilot spans, named by their leads:

| Span                | Anchor (leading text)                                                                                    | Subsection                           |
| ------------------- | -------------------------------------------------------------------------------------------------------- | ------------------------------------ |
| A — the convention  | `**Docs refresh on change**:`                                                                            | `## Conventions`                     |
| B — sweep scope     | `**A doc-consistency sweep is scoped by the convention or the claim, not by the review's findings list…` | `### Documentation & durable claims` |
| C — set staleness   | `**A "the only X" claim in AGENTS.md goes stale the moment a change adds a second X…`                    | `### Documentation & durable claims` |
| R — the review gate | `**A finding that adds behavior is a scope change, not a code fix:**` and `**Single-source a claim…**`   | `### Workflow: The review gate`      |

### Reproducible baseline

Every figure below is measured on the pre-change revision (`git show HEAD:AGENTS.md`), because this change edits the
file.

| Fact            | Command                                                               | Result                                     |
| --------------- | --------------------------------------------------------------------- | ------------------------------------------ |
| Revision        | `git rev-parse HEAD`                                                  | `3903a889b3955505b695504e4cdc28b4d1112bbc` |
| `origin/main`   | `git rev-parse origin/main`                                           | `3903a889b3955505b695504e4cdc28b4d1112bbc` |
| Branch          | `git branch --show-current`                                           | `consolidate-docs-update-obligations`      |
| Working tree    | `git status --porcelain --untracked-files=all`                        | empty before planning began                |
| `AGENTS.md`     | `git show HEAD:AGENTS.md > /tmp/pre.md; wc -c -l /tmp/pre.md`         | `324,296` bytes / `3,361` lines            |
| Span A          | the span script below, on `/tmp/pre.md`                               | `7,183` bytes / `61` lines                 |
| Span B          | the same script                                                       | `1,461` bytes / `19` lines                 |
| Span C          | the same script                                                       | `2,026` bytes / `19` lines                 |
| Pilot subtotal  | —                                                                     | `10,670` bytes / `99` lines                |
| Evidence record | `wc -c -l docs/guidance-evidence/documentation-and-durable-claims.md` | `16,608` bytes / `249` lines               |

Span figures include each span's trailing newline, matching the byte boundary the script computes; a draft's figure
below uses the same convention, so span figures and draft figures are comparable.

```text
import sys, re
path = sys.argv[1] if len(sys.argv) > 1 else "AGENTS.md"
raw = open(path, "rb").read(); lines = raw.decode("utf-8").split("\n")
if lines and lines[-1] == "": lines = lines[:-1]
offs = [0]
for ln in lines[:-1]: offs.append(offs[-1] + len(ln.encode()) + 1)
def bspan(i, j):
    a = offs[i-1]; b = offs[j] if j < len(offs) else len(raw); return b - a
starts = [i for i, ln in enumerate(lines, 1) if ln.startswith("- ")]
def span(prefix):
    st = [i for i in starts if lines[i-1].startswith(prefix)]
    assert len(st) == 1, (prefix, st)
    st = st[0]
    later = [i for i in starts if i > st] + [i for i, ln in enumerate(lines,1) if i > st and re.match(r"^#{2,3} ", ln)]
    en = min(later) - 1
    while en > st and lines[en-1].strip() == "": en -= 1
    return st, en, bspan(st, en)
for name, prefix in [("A", "- **Docs refresh on change**"),
                     ("B", "- **A doc-consistency sweep is scoped"),
                     ("C", '- **A "the only X" claim')]:
    st, en, n = span(prefix)
    print("%s lines %d-%d  %d lines  %d bytes" % (name, st, en, en-st+1, n))
```

The preceding reductions are merged, and the branch is forked from their `HEAD`: `reduce-agents-md-loaded-surface`
(#648), `separate-agents-md-operational-from-evidence` (#649), `load-stage-workflow-instructions-on-demand` (#651), and
`convert-remaining-gotchas-rules` (#652), which is `HEAD`.

### Prior decisions recovered

| Prior unit / artifact                                                 | What it settled                                                                                                                                                                                                       | How it constrains this proposal                                                                                                                                                                     |
| --------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `reduce-agents-md-loaded-surface` (2026-10-20)                        | Extraction to a destination that loads on demand is permitted only where the retained root text leaves no warning, imperative, exception, or target-less fact inferred.                                               | Keeps Option 3 inside root-to-root consolidation: no destination is created, and every draft states its condition, action, exceptions, and identifiers at root.                                     |
| `separate-agents-md-operational-from-evidence` (2026-10-20)           | Evidence moves to `docs/guidance-evidence/`; a root entry whose evidence moved states every operational element and never requires reading the record.                                                                | The record is the evidence home for both options; the merge owes a section merge, not a new record.                                                                                                 |
| `convert-remaining-gotchas-rules` (2026-10-20)                        | Converted the eleven remaining eligible rules in `### Documentation & durable claims`; one record section per converted rule, keyed by a verbatim `Rule:` paragraph.                                                  | B and C are already converted; a merge owes a record-section merge. Its disposition table is the floor for what stays at root.                                                                      |
| `add-extraction-procedure` (2026-10-19)                               | A destination must be owned and discoverable before content moves, and a merged pointer must carry every imperative the old text held.                                                                                | The record is an owned destination; a merge may not replace prose with pointers that drop operands.                                                                                                 |
| `trim-restated-agents-md-rules` (2026-10-11)                          | Single-sourced four restatements as pointers; the pointer must not claim more than its target, and a site keeps its own lesson.                                                                                       | Adopted as the trimming test.                                                                                                                                                                       |
| `harden-command-definitions` (2026-10-19)                             | The imperative-versus-procedure boundary: an imperative a reader must act on before the step that would touch it is stated at every site where that step appears.                                                     | The reason the delivery routing and the ideas-file criteria are retained, and the reason each merged row keeps its operand.                                                                         |
| `record-agents-md-authoring-standard` (2026-10-17)                    | One authoritative definition per subject; a procedure is not duplicated across sections; an edit re-reads the affected section for duplication and contradiction.                                                     | Supplies the permission to merge and the obligation to verify the merged text carries every anchor.                                                                                                 |
| `make-captured-rules-traceable` (2026-09-18)                          | `captured:` markers are provenance, sit at the end of the rule block they record, and a merge retitles a bullet's shorthands.                                                                                         | The merged rule carries all four markers; the one `AGENTS.md` site naming C by its old title is retargeted.                                                                                         |
| `docs/audits/2026-09-21.md`                                           | Its merge paragraph concerns a different family — the "verify against a known positive / probe the consumer's real read path" rules — and states that merging **that family** further "would blend distinct lessons". | **Records nothing about B or C.** What it supplies is the criterion's own wording, which "the merge criterion, resolved" applies below.                                                             |
| `docs/audits/2026-10-19.md`                                           | Agent-tooling verdict `2 findings (0 merge, 0 removal, 0 route)`; the report names neither B nor C anywhere.                                                                                                          | **Records a count, not an examination.** It establishes that the audit reported no candidate in that run; it is neither an endorsement nor a rejection of this merge, and is not treated as either. |
| `.opencode/agent/agents-auditor.md`                                   | Owns the pairing check: a section's `Rule:` paragraph repeats a live rule's leading text, and a rule's `evidence:` token names a record holding its section.                                                          | The merge must leave one section paired with one live rule. The check derives the pair from the tokens, not from the root section's name.                                                           |
| `agent-skills` spec, "A merge candidate's pointer target is verified" | A merged text keeps any delegated item its target does not carry, and never blends two distinct lessons.                                                                                                              | Applied literally: no pointer replaces an operand, and every B and C row survives as a row.                                                                                                         |

### Deliberately repeated warnings kept

These repetitions are not consolidation targets: each sits where a reader must act before reaching the canonical
statement, which the imperative-versus-procedure boundary permits.

| Repetition                                                                       | Site                                                                                                                                        | Why it stays                                                                                                                      |
| -------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------- |
| Where a docs edit ships (this PR vs its own docs PR)                             | `docs/ideas.md` header; `ship-change/SKILL.md:37-38`; the capture workflow's merge-time step; the review gate's scope-change decision point | Each is read at a different trigger — parking an idea, shipping a unit, running a merge-time capture, applying a review finding.  |
| The status tag's wording and disposition                                         | `AGENTS.md`'s parked-work gotcha, pointing at the convention                                                                                | The gotcha states the obligation; the convention owns the wording, so the pointer carries no operand.                             |
| The idea-removal criterion and the file's section order and dating               | `AGENTS.md` and the `docs/ideas.md` header                                                                                                  | Two audiences act on one file: an agent decides removal from the always-loaded root, a human reads the scratchpad's own contract. |
| The pre-merge citation being contingent                                          | The convention and the issue's own body                                                                                                     | The convention states it; the published artifact carries the drop clause the merge must edit.                                     |
| The 120-character check, the `captured:` placement rule, the guard's five checks | Single-sourced by `trim-restated-agents-md-rules`; not reopened here                                                                        | Settled; no candidate in this pilot.                                                                                              |

### Parked ideas reconciled

| Parked idea                                                          | Relationship                                     | Disposition                                        |
| -------------------------------------------------------------------- | ------------------------------------------------ | -------------------------------------------------- |
| Extend the extraction conditions so the pointer identifies each unit | Concerns a destination pointer, not a root merge | **Not adopted.** No destination is introduced.     |
| Carry findings forward across audit reports                          | Concerns the audit report's verdict              | **Unrelated.** No audit output changes.            |
| Trend the audit counts across reports                                | Would measure whether reductions hold            | **Out of scope by the request.**                   |
| Sharpen route-candidate verification                                 | Constrains a pointer's coverage                  | **Constraint applied**; no route candidate arises. |
| Add `verifyUniqueChangeNames` to commit hygiene                      | A duplicate-name scan for change dirs            | **Unrelated.**                                     |
| Convert the Git-tranche rules (deferred)                             | The sibling tranche of the same section policy   | **Out of scope by the request.**                   |

**Entries resolved by this unit: none.** No parked idea is implemented or removed, and `docs/ideas.md` needs no edit.

## Goals / Non-Goals

**Goals**

- Resolve whether merging B and C satisfies the governing merge criterion, or requires a policy amendment.
- Measure every option — retention, a canonical root procedure, and local merges — on its complete draft.
- Reconcile scope: identify any obligation a draft adds, and decide whether it belongs.
- Compare the viable options on reduction, clarity, navigation, preservation risk, and supporting effort.
- Recommend one unit that complies with the policy, or retention.

**Non-Goals**

- Implementing anything: no `AGENTS.md` edit, no record edit, no spec edit.
- The deferred Git evidence-conversion tranche, a new skill or evidence-storage architecture, audit-ledger or trend
  tooling, a general review-gate redesign, or unrelated documentation fixes.
- Inventing a size cap or percentage target.

## Method

### The quantities

| Quantity                     | Definition                                                          | Why it is separate                                                                  |
| ---------------------------- | ------------------------------------------------------------------- | ----------------------------------------------------------------------------------- |
| Root reduction               | Bytes removed from `AGENTS.md`, the surface loaded every session.   | The objective.                                                                      |
| Pointer and warning overhead | Bytes a retained local statement or pointer adds at its site.       | Charged inside the root figure, never beside it.                                    |
| Total maintained content     | Bytes of guidance and evidence maintained anywhere (root + record). | Reported as a cost; a merge may leave it nearly flat.                               |
| Reading cost at a trigger    | How much a reader must read to act on one trigger.                  | The cost a byte figure cannot see, and the clearest difference between the options. |

### How a span is measured

Each draft is written as the complete replacement text, formatted with Prettier 3.9.10 (`printWidth: 120`,
`proseWrap: always`, the version `libs.versions.toml` pins), and measured by applying the replacement to
`git show HEAD:AGENTS.md` and formatting the whole file. The whole-file figure is authoritative: it includes re-wrapped
neighbours and the separator line a deletion removes. Per-merge figures are the whole-file delta of applying that merge
alone, so they sum to the unit's figure.

### The merge criterion, resolved

The governing text is `showcase/quality/agent-skills`' "Merge and removal candidates are standing findings":

> it reports merge candidates (with a merged text that preserves every anchor and piece of evidence the originals
> carried, and never blending two distinct lessons — or, where that text would only restate a rule the file already
> carries, a deletion of the duplicate, still counted as a `remove`, saying which)

Its merge class is **"overlapping or complementary entries"** (`agents-auditor.md`). The criterion is therefore not
"merge only duplicates": complementary entries are eligible, and the constraint falls on the **merged text** — it must
preserve every anchor and piece of evidence, and must not fuse two remedies into one. The authoring standard adds the
positive duty: "keep one authoritative definition for each subject and reference it rather than duplicating its
contents".

Classifying each part of the proposed union against the three categories this refinement names:

| Part of the union                                                                                                        | Classification                                                               | Basis                                                                                                                                                       |
| ------------------------------------------------------------------------------------------------------------------------ | ---------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------- |
| B's scoping clause and method (`not by the review's findings list`, `derive by grepping`)                                | the frame every branch shares; not an obligation of its own                  | It qualifies each row; merging does not move it.                                                                                                            |
| B's six claim classes                                                                                                    | **(b) complementary branches of one decision**                               | Same action (derive the site set, correct the hits) under different conditions; each row keeps its trigger, operand, and site set.                          |
| C's four change classes (a uniqueness claim; an absence claim; a rename or removal; a set's membership)                  | **(b) complementary branches of the same decision**                          | The action is the same; the conditions differ. Each row keeps its own sites and its own claim.                                                              |
| C's standing check ("re-verify such a claim whenever you edit its section") and its timing ("fix it in the same change") | **a standing obligation and a condition carried by the branch they qualify** | Both stay inside the uniqueness row, not lifted to the lead.                                                                                                |
| C's `--help`/docstring warning                                                                                           | **a warning attached to the enumeration branch**                             | Stays inside that row.                                                                                                                                      |
| B's code-verification clause (`a claim about the code is verified against the code`) and its closing mapper sentence     | **(c) an independent lesson that B already carries beside the sweep**        | The merge carries it exactly as B did — the lead's second clause and the closing sentence. It is not fused with any C row, and no C row is qualified by it. |
| Between B and C                                                                                                          | **(a) no duplicate**                                                         | No two rows select the same action under the same conditions; the members are complementary branches, not restatements.                                     |

**Conclusion: the merge satisfies the criterion as written.** It is a merge of complementary entries, which the
scenario's own class admits; the merged text preserves every anchor and every piece of evidence (the preservation map
below names each), and it fuses no lesson — eleven rows stay independently actionable, and the one (c) item rides where
it already rode. No policy amendment is required, and this is not a departure: the earlier draft's "policy departure"
framing was wrong.

The honest cost is a **clarity** cost, not a criterion cost: C's lead was a direct handle for its trigger, and in the
merged rule that trigger becomes one row among ten. That belongs in the proportionality comparison (D3), not in the
criterion analysis.

### The obligation map

Every independently actionable statement in the pilot is one row. `A`/`B`/`C` are the spans above; `R` the review gate.

| Anchor | Trigger                                                                            | Required action                                                                                                                                              | Exceptions                                                                                     | Scope                                                                                  | Authorization or ordering          | Evidence                                         | Duplicate or distinct                                        |
| ------ | ---------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------ | ---------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------- | ---------------------------------- | ------------------------------------------------ | ------------------------------------------------------------ |
| A1     | every change                                                                       | verify whether `AGENTS.md`, `README.md`, `docs/adr/` need refresh (commands, config, conventions, gotchas)                                                   | —                                                                                              | the three root docs                                                                    | before reporting the change done   | ADR-0006 episode                                 | distinct trigger                                             |
| A2     | an ADR's Consequences names a follow-on this change lands, or its Decision changes | refresh that ADR                                                                                                                                             | —                                                                                              | `docs/adr/`                                                                            | same change                        | ADR-0006                                         | distinct                                                     |
| A3     | a version bump (dependency, tool, wrapper)                                         | grep the old string and fix each live doc naming it                                                                                                          | a historical record keeps its recorded values                                                  | `AGENTS.md`, `README.md`, `openspec/config.yaml`, `docs/adr/`, `docs/ideas.md`         | same change; no gate reads them    | `bump-dependencies-2026-10-12`                   | distinct operand; shares only the method with B2             |
| A4     | the change's own idea is implemented or decided against                            | remove it from `docs/ideas.md`, in this PR                                                                                                                   | a promoted idea is linked, not removed                                                         | `docs/ideas.md`                                                                        | not a separate docs PR             | header owns the criterion                        | route duplicates A13; deliberate for two audiences           |
| A5     | promote an idea to an issue                                                        | annotate the issue number; keep the entry                                                                                                                    | —                                                                                              | `docs/ideas.md`                                                                        | prefer at the unit's merge         | `park-retro-marking-idea (#281)`                 | distinct                                                     |
| A6     | the unit's PR is open while citing an artifact it ships                            | mark the citation contingent; drop the clause at the merge                                                                                                   | an already-published copy is edited                                                            | any artifact                                                                           | the merge-time read owns the drop  | `mark-s3-applied`                                | distinct                                                     |
| A7     | park an idea                                                                       | give it a trailing status tag                                                                                                                                | a promotion replaces the tag                                                                   | `docs/ideas.md`                                                                        | never append past the period       | `park-review-loop-follow-ups`                    | distinct; the parked-work gotcha points here for the wording |
| A8     | the change shipped something                                                       | sweep the un-audited docs for references to it                                                                                                               | a historical record keeps its values                                                           | `docs/ideas.md`; a retrospective's suggestion rows and prose                           | same change                        | `attribute-load-test-tail (#570)`                | distinct                                                     |
| A9     | a retrospective suggestion                                                         | mark its disposition, do not remove it                                                                                                                       | —                                                                                              | retrospectives                                                                         | same change                        | —                                                | distinct                                                     |
| A10    | an open idea still calls it "the proposed X"                                       | update its prose, including any enumeration or count                                                                                                         | —                                                                                              | `docs/ideas.md`                                                                        | same change                        | —                                                | distinct                                                     |
| A11    | a stale un-audited doc needs an owner                                              | none owns it; the architecture-auditor reaches `docs/ideas.md` for parked-entry staleness                                                                    | —                                                                                              | `docs/ideas.md`, retrospectives                                                        | —                                  | the auditors' own bullets                        | duplicate: a re-listed enumeration                           |
| A12    | the docs are the change                                                            | ship them with the change's PR                                                                                                                               | —                                                                                              | new agent/command/skill docs, README rows, the idea removal                            | —                                  | —                                                | duplicate: A13's caused branch                               |
| A13    | any other docs edit                                                                | route by causation while the change is in flight                                                                                                             | not caused, or surfaced after the change is done → its own docs PR forked from `main`, batched | all docs                                                                               | decide the owner before committing | `fix-openspec-config-rules`                      | distinct — the canonical delivery rule                       |
| A14    | the routing decision                                                               | decide the edit's owner before committing                                                                                                                    | —                                                                                              | —                                                                                      | before the commit                  | `fix-openspec-config-rules`                      | distinct                                                     |
| A15    | a docs fix exists                                                                  | verify it against the merged PR's diff, not the description                                                                                                  | —                                                                                              | —                                                                                      | after the merge                    | —                                                | distinct                                                     |
| A16    | a parked-idea docs PR                                                              | fold the idea's durable fact into `AGENTS.md`/`README.md`                                                                                                    | —                                                                                              | those two                                                                              | same docs PR                       | —                                                | distinct                                                     |
| A17    | a project fact moves                                                               | refresh the `config.yaml` `context:` block                                                                                                                   | —                                                                                              | runtime/Spring/Gradle versions, module count, service list, image names                | same change                        | `fix-openspec-config-rules`                      | distinct action; shared rationale with A19                   |
| A18    | a rule's obligation moves                                                          | refresh its per-artifact `rules` copy                                                                                                                        | —                                                                                              | `config.yaml` `rules`, injected per artifact                                           | a green gate does not mean current | —                                                | distinct action                                              |
| A19    | a project fact moves                                                               | refresh the GitHub description and topics                                                                                                                    | —                                                                                              | out-of-repository metadata                                                             | no gate, no auditor                | —                                                | distinct action; shared rationale with A17                   |
| A20    | a sweep deletes an entry as non-durable                                            | check the un-gated copies for the same sentence                                                                                                              | —                                                                                              | the copies above                                                                       | the move rule does not fire        | `disable-axoniq-console-message`                 | distinct                                                     |
| A21    | `SECURITY.md` ships an instruction that depends on a repository setting            | enable the setting; name it in the report                                                                                                                    | —                                                                                              | repository settings                                                                    | a setting leaves no diff           | —                                                | distinct                                                     |
| B1     | a doc-consistency sweep                                                            | scope it by the convention or the claim, never by the review's findings list                                                                                 | —                                                                                              | —                                                                                      | —                                  | —                                                | the sweep's scoping frame                                    |
| B2     | any sweep                                                                          | derive each site set by grepping the artifact or corpus, never from the plan's list                                                                          | —                                                                                              | artifact or corpus                                                                     | —                                  | —                                                | the method; operands stay local (subsumption test)           |
| B3a    | a convention changes (spelling, `MUST`/`SHALL`, an enumeration)                    | every instance in the artifact or corpus                                                                                                                     | —                                                                                              | —                                                                                      | —                                  | —                                                | (b) complementary branch; shared method with C5              |
| B3b    | a corrected fact                                                                   | every repetition plus the statements depending on it                                                                                                         | —                                                                                              | —                                                                                      | —                                  | `reconcile-showcase-cache-default`               | deliberate restatement at the review trigger (R2)            |
| B3c    | a rule the change authored or replaced                                             | its paraphrases by subject, in every document describing the workflow                                                                                        | —                                                                                              | `AGENTS.md`, file headers, `.opencode/`, specs, change artifacts                       | —                                  | —                                                | deliberate restatement at the review trigger (R1)            |
| B3d    | an audit finding                                                                   | the pattern the finding instantiates                                                                                                                         | —                                                                                              | —                                                                                      | —                                  | —                                                | (b) complementary branch                                     |
| B3e    | a widened or narrowed check                                                        | its describers, including a worked example                                                                                                                   | —                                                                                              | —                                                                                      | —                                  | 2026-10-11 audit                                 | (b) complementary branch                                     |
| B3f    | a javadoc or field-doc pass                                                        | every declaration in the touched classes                                                                                                                     | —                                                                                              | —                                                                                      | —                                  | `fix-javadoc-consistency`                        | (b) complementary branch                                     |
| B4     | a claim about code                                                                 | verify it against the code                                                                                                                                   | —                                                                                              | —                                                                                      | —                                  | —                                                | (c) independent lesson B already carries                     |
| B5     | a converter or mapper's direction                                                  | read the field's Javadoc and one call site                                                                                                                   | —                                                                                              | —                                                                                      | before writing `@param`/`@return`  | `fix-javadoc-consistency`                        | (c), B4's instance                                           |
| C1     | a change adds a second X                                                           | fix the docs' "only X" claim in that change                                                                                                                  | —                                                                                              | `AGENTS.md`                                                                            | same change                        | `monitor-web-ui-npm-dependencies (#395)`         | (b) complementary branch; carries its own timing             |
| C2     | editing a section                                                                  | re-verify its uniqueness claims against the repository                                                                                                       | —                                                                                              | —                                                                                      | —                                  | —                                                | (b)'s standing check, inside C1's row                        |
| C3     | a change makes an absence claim false                                              | grep `AGENTS.md`/`README.md`, including a claim a recent change added                                                                                        | —                                                                                              | both                                                                                   | same change                        | `verify-unique-review-rounds`                    | (b) complementary branch                                     |
| C4     | a code symbol or file is renamed or removed                                        | grep the whole repository for the old name                                                                                                                   | —                                                                                              | config, values, template comments included                                             | same change                        | `monitor-web-ui-npm-dependencies (#395)`         | (b) complementary branch                                     |
| C5     | a set gains a member                                                               | sweep every live enumeration of it; an auditor's set also takes its definition, trigger command, `AGENTS.md` summary, and the owning spec's `MODIFIED` delta | —                                                                                              | `.opencode/`, `openspec/specs/`, `AGENTS.md`, `README.md`, `docs/ideas.md`, `scripts/` | before writing the addition        | —                                                | (b) complementary branch; shared method with B3a             |
| C6     | a tool's docstring or `--help`                                                     | treat it as a live enumeration                                                                                                                               | the change's planning copies can look consistent while live sites go stale                     | —                                                                                      | —                                  | —                                                | (b)'s warning, inside C5's row                               |
| C7     | recording that a component is deliberately not used                                | grep the whole repository for its name                                                                                                                       | —                                                                                              | non-doc artifacts                                                                      | —                                  | ADR-0009 episode                                 | (b) complementary branch                                     |
| R1     | a review finding adds behavior                                                     | sweep proposal, design, tasks, delta spec, and the docs                                                                                                      | —                                                                                              | planning artifacts and the shipped copies                                              | before re-running the review       | `record-baseline-operating-point`                | deliberate restatement of B3c at its own trigger             |
| R2     | a framing or coherence correction                                                  | re-read every artifact stating the corrected claim, the section inside it included                                                                           | —                                                                                              | planning and shipped                                                                   | before re-running the review       | `widen-architecture-auditor-to-revisit-triggers` | deliberate restatement of B3b at its own trigger             |
| R3     | a claim, rule set, or record is stated twice                                       | single-source it; repeat only a local imperative                                                                                                             | an imperative at its own site is repeated deliberately                                         | —                                                                                      | —                                  | `harden-command-definitions`                     | policy, not a doc-update obligation                          |

**Findings of the map.** The map holds 41 obligations. Three are folded into a row that determines the same action in
the same circumstances — A12 and A4's route become A13's first branch, and A11's re-listed enumeration becomes a pointer
at the auditor bullets. Two are the review gate's restatement of a sweep action at its own trigger (B3b/R2, B3c/R1) and
stay, by the local-imperative boundary. The remaining 36 are distinct under the (a)/(b)/(c) test above: B's and C's
classes are complementary branches of one decision, not duplicates of each other, and B's code-verification clause is an
independent lesson the merge carries unchanged.

## Decisions

### D1 — The three options, each measured on a complete draft

| Option                      | Shape                                                                                                                                                                                                               | Root Δ (measured)              | Policy                                | Reading cost                                                                        |
| --------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------ | ------------------------------------- | ----------------------------------------------------------------------------------- |
| 1. Retain                   | No edit                                                                                                                                                                                                             | `0`                            | none                                  | two leads, each a direct handle; the method stated twice                            |
| 2. Canonical root procedure | A new `### Workflow:` subsection holds the trigger, a five-step procedure, the eleven classes, a completion criterion, the four `captured:` markers, and an `evidence:` token; the convention gains a pointer to it | **`+432` bytes / `+13` lines** | **A `MODIFIED` delta** (quoted in D5) | one heading, five steps, and eleven classes read at every trigger                   |
| 3. Local merges             | Merge B and C into one claim→site rule; condense four duplicates in the convention; merge the record sections                                                                                                       | **`−776` bytes / `−7` lines**  | none                                  | one lead plus eleven rows; the uniqueness/absence handle moves from a lead to a row |

Option 2 is **drafted completely** (`design.md`, Draft 5 — `3,808` bytes / `51` lines with its markers and token) and
measured as the option's full shape: replacing spans B and C with the procedure and adding the convention's pointer
yields `324,728` bytes / `3,375` lines — it **grows** the always-loaded surface by `432` bytes and `13` lines. Its
evidence arrangement and delta are in D5, its other costs in D3. That is a measurement of the complete shape.

### D2 — Option 3 complies with the criterion

The criterion analysis above concludes that B and C are complementary branches of one decision, which the scenario's own
merge class admits, and that the merged text preserves every anchor and fuses no lesson. So the design does not call
this a policy departure and does not seek an exception: it applies the criterion. Two consequences follow for the
review:

- **A reviewer who reads the merge as a blend is answered by the classification table**, part by part, not by the
  owner's approval.
- **If a reviewer still finds a blend, the remedy is retention of B and C** (Option 1), not an implicit exception. The
  convention's trim is a separate result and is not offered as a substitute for the sweep merge.

### D3 — Proportionality: the five dimensions

| Dimension                       | 1. Retain                                                       | 2. Canonical procedure                                                                                                                                       | 3. Local merges                                                                                                          |
| ------------------------------- | --------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------ | ------------------------------------------------------------------------------------------------------------------------ |
| Net root reduction              | `0`                                                             | `+432` bytes, `+13` lines (grows)                                                                                                                            | `−776` bytes (`0.239%`), `−7` lines                                                                                      |
| Clarity of triggers and actions | two leads, each a direct handle; the shared method stated twice | all handles demoted into one procedure; every trigger pays the five steps                                                                                    | one lead plus eleven rows; the uniqueness/absence handle becomes a row among ten                                         |
| Navigation and maintenance      | two rules and two record sections to keep consistent            | one authority, but a new root section, a widened record scope, and the convention still owning the routing and gaining a pointer                             | one rule and one record section; the convention stays the routing authority; one fewer rule to keep in step              |
| Preservation risk               | none                                                            | eleven rows survive, but the episode evidence needs the record's scope widened, and the procedure's trigger risks absorbing the convention's per-change duty | row-by-row preserved; one token drops (`.opencode/`, from the removed auditor enumeration); the record keeps both bodies |
| Supporting edits and review     | none                                                            | one `MODIFIED` delta, a record-header change, a new `###` heading, and a 3.5 KB procedure to review                                                          | one record file edit; no delta                                                                                           |

**The judgement.** `776` bytes is `0.239%` of the file, and on its own that does not carry the unit — stated plainly,
without comparing it to individual rules of an earlier tranche. What does carry it, if it is carried, is narrower:

- B and C define the same subject — the site set for a changed claim — in two places; the authoring standard asks for
  one authoritative definition per subject.
- Two of the three folded statements (A12, A11) are the kind of duplication the repository's own conventions forbid: an
  instance restated beside its rule, and an enumeration re-listed from the bullets that own it.
- The record loses one section, so the maintenance surface shrinks in both files.

Against that, the merge demotes C's handle, and the whole unit costs a review cycle. **Retention is a legitimate
outcome** if the owner weighs that cycle above the reduction; the design does not claim the byte figure alone justifies
it.

### D4 — Scope reconciliation: the union adds nothing

The union must state B's and C's obligations and no more. Four statements in earlier drafts added one; each is removed:

| Drafted wording                                            | What it added                                                          | Now                                                                                        |
| ---------------------------------------------------------- | ---------------------------------------------------------------------- | ------------------------------------------------------------------------------------------ |
| The lead's "in the same change"                            | Extended C's timing clause to B's six classes                          | The lead keeps B's framing; C's timing stays inside the uniqueness row                     |
| "a set that gained **or lost** a member"                   | A removal was never a trigger in C                                     | "a set that gained a member"                                                               |
| "a code symbol, **file, config key, or workflow** renamed" | Config keys and workflows were never named as trigger objects          | "a code symbol or file renamed or removed" (C4's own wording)                              |
| One row joining uniqueness and absence with one arrow      | Gave the uniqueness branch `README.md` as a target, which C1 never had | Split: the uniqueness claim → `AGENTS.md`; the absence claim → `AGENTS.md` and `README.md` |

The fourth was caught by the deep review, not by the quick rounds: a shared `→` after the `or` applied one target list
to both branches, and the obligation map's own scopes (C1 `AGENTS.md`, C3 both) contradict it. The split keeps both
targets and both rows.

After those four removals the union states exactly B's and C's obligations. The remainder is a rewording of the frame
("sweep the pair for each kind of change the unit made") and the retention of every operand; nothing is generalized.

### D5 — The delta decision

**Option 3 owes no delta.** The evidence-record requirement says evidence is kept "one file per `## Gotchas` subsection,
with one section per converted rule". The merged rule stays in `## Gotchas` → `### Documentation & durable claims`, and
the merged section stays in that subsection's record, so the requirement holds unchanged. The requirement's other two
clauses hold as well: the `Rule:` paragraph repeats the live lead verbatim, and the record holds only sections paired
with a live rule. `skip_specs: true` is therefore accurate, and its grep evidence is that no capability spec names
either rule's subject.

**Option 2 would require this amendment**, which is the smallest explicit change its shape needs (quoted, with the
replacement):

> Evidence moved out of an `AGENTS.md` entry SHALL be kept under `docs/guidance-evidence/`, one file per `## Gotchas`
> subsection, with one section per converted rule.

→ "…one file per root section or `## Gotchas` subsection that owns a converted rule, named in the record's header, with
one section per converted rule."

**Option 2's evidence arrangement**, so its draft is complete rather than procedure-only: the procedure carries the four
`captured:` markers and `evidence: documentation-and-durable-claims`; that record keeps one file, its header's scope
sentence widened to name the workflow as well as the Gotchas subsection; and the merged section's `Rule:` paragraph
repeats the procedure's heading (`Update the docs a change makes stale`), which is the closest equivalent a `###`
heading has to a rule's leading text. The `agents-auditor`'s pairing check needs no change either way: it derives the
pair from the `evidence:` token and the section's `Rule:` paragraph, not from the root section's name.

The amendment is not needed for Option 3, and the design does not propose it.

### D6 — What is explicitly retained

| Retained                                                                                                                 | Why                                                                                                |
| ------------------------------------------------------------------------------------------------------------------------ | -------------------------------------------------------------------------------------------------- |
| B's "not by the review's findings list" and B2's "never from the plan's list"                                            | The sweep's warrant: without them the list reads as a checklist an agent may complete from memory. |
| Every operand — the five version-bump docs, the six enumeration paths, the auditor's four sites, `AGENTS.md`/`README.md` | The subsumption test: the method does not supply the operand.                                      |
| C's standing check and its timing clause                                                                                 | Both stay inside the branch they qualify.                                                          |
| The delivery routing in A, in the `docs/ideas.md` header, in `ship-change/SKILL.md`, and in the capture workflow         | Local imperatives at four different triggers.                                                      |
| The three un-gated surfaces as separate entries                                                                          | Different actions; only their shared rationale consolidates.                                       |
| `SECURITY.md`'s dependent-setting rule                                                                                   | A distinct action with no other host.                                                              |
| R1 and R2 (the review gate's sweeps)                                                                                     | Their own scope and trigger.                                                                       |

### D7 — The record sections merge, and the `Rule:` key is re-derived

The record holds one section per converted rule, keyed by a verbatim repeat of the root lead. Merging the rules without
merging their sections would leave an unmatched section, which the `agents-auditor` reports as a pairing finding. The
merged section takes a new slug (`claim-site-sweep`), the merged root lead as its `Rule:` paragraph, both bodies, and
all four `captured:` markers. Its first sentence becomes "The sweep scopes below come from these episodes." rather than
the old "Each of the rule's six scopes…", because the count is now ten and a corpus count in guidance drifts.

## Drafts

### Draft 1 — the merged sweep rule (replaces spans B and C)

Placed at B's position in `### Documentation & durable claims`. Formatted; `3,066` bytes / `35` lines (was `3,487` /
`38`).

```text
- **A doc-consistency sweep is scoped by the convention or the claim, not by the review's findings list — and a claim
  about the code is verified against the code.** Derive the sites by grepping the artifact or corpus, never from the
  plan's list, and sweep the pair for each kind of change the unit made:

  - a convention (a coordinate or identifier spelling, `MUST` vs `SHALL`, an enumeration) → every instance in the
    artifact or corpus;
  - a corrected fact (a direction, a value, a magnitude) → every repetition, plus the statements that depend on it
    without repeating it (a caveat, a rationale, a concluding sentence);
  - a rule the change authored or replaced → its paraphrases by the rule's **subject**, in every document that describes
    the workflow: `AGENTS.md`, file headers (e.g. `docs/ideas.md`'s), the `.opencode/` definitions, the specs, and the
    change's own artifacts;
  - an audit finding → the pattern the finding is an instance of, not the finding alone;
  - a widened or narrowed check → its describers, including a worked example in a definition;
  - a javadoc or field-doc pass → every declaration in the touched classes, not only the ones a review named;
  - a set that gained a member, or an enumeration of one → every live enumeration of the set, since a plain enumeration
    is the case a name-grep cannot see: grep its distinctive members across `.opencode/`, `openspec/specs/`,
    `AGENTS.md`, `README.md`, `docs/ideas.md`, and `scripts/` before writing the addition — a tool's module docstring
    and `--help` text are live enumerations too, and the change's own planning copies can stay identical while every
    live site goes stale — and when the set is an auditor's, sweep its definition, its trigger command, `AGENTS.md`'s
    summary of it, and the spec requirement that owns the sweep, which is a main spec and so needs a `MODIFIED` delta
    rather than a docs edit;
  - a code symbol or file renamed or removed → the whole repository for the old name, config, values, and template
    comments included, since docs cite these names as examples and nothing resolves them;
  - a second instance of something the docs call unique (an `only`/`sole`/`never` claim) → `AGENTS.md`, for the claim
    about the first instance; fix it in the same change, and re-verify such a claim against the repository whenever you
    edit its section rather than trusting the prose;
  - a capability the change makes no longer absent (`nothing covers X`, `A and B ignore X`) → `AGENTS.md` and
    `README.md`, including a claim a recent change added;
  - a component you record as deliberately _not_ used → the whole repository for its name.

  Two directions read plausibly for a converter or mapper, so read the field's Javadoc and one call site before writing
  its `@param`/`@return`. captured: bump-tooling-pins-2026-10-16 captured: make-the-capture-selective captured:
  monitor-web-ui-npm-dependencies (#395) captured: verify-unique-review-rounds

  evidence: documentation-and-durable-claims

```

### Draft 2 — the condensed convention (replaces span A)

Formatted; `6,838` bytes / `58` lines (was `7,183` / `61`). Every sentence outside the four merged statements is
unchanged.

```text
- **Docs refresh on change**: on every change, verify whether `AGENTS.md`, `README.md`, and `docs/adr/` need to be
  refreshed to reflect the new state (commands, config, conventions, gotchas) — including an ADR whose Consequences name
  a follow-on this change lands, or whose Decision it alters (ADR-0006 called scheduled Snyk monitoring a follow-on
  concern for weeks after the Snyk workflow landed, until the first architecture audit caught it). A **version bump** (a
  dependency, tool, or wrapper version) is the mechanical case: it makes stale every live doc that names the bumped
  version — including a coordinate version an ADR or idea quotes as the catalog's current value — so grep the old string
  across `AGENTS.md`, `README.md`, `openspec/config.yaml`, `docs/adr/`, and `docs/ideas.md` and fix each, since no gate
  reads any of them. captured: bump-dependencies-2026-10-12 Update them before reporting the change done; also remove
  the change's idea from `docs/ideas.md` in this PR, not as a separate docs PR. An idea is removed once implemented
  (captured by a change) or once explored and decided against (the durable lesson is captured in `AGENTS.md`/an ADR
  instead); only open ideas remain (see the file's header). Promotion to a GitHub issue is a **link, not a removal** —
  annotate the idea with the issue number when you promote it, since the file's header names only the issue's link to
  the change, not the scratchpad's back-link to the issue, so the two drift apart — and prefer promoting a unit's idea
  at that unit's merge: while its PR is open, citing an artifact the unit ships is a contingent claim that must say so
  (`open as #NNN; not yet merged`) and the clause drops at the merge — the merge-time read owns that drop, and where the
  claim was already published outside the repository it means editing the published artifact, not only omitting it from
  new prose — because the issue is a permanent record outside the repository and cannot cite what the default branch
  does not have. Give every parked entry a trailing status tag stating its disposition (`— parked; no change yet.` is
  the common form; a promoted or explored-and-set-aside idea says so instead: the promotion _replaces_ the parked tag,
  `— promoted to issue #NNN; no change yet.`, never a clause appended past the tag's sentence-final period), since the
  header fixes the sections' order and dating but not the tag, and the tag is what marks the entry as a still-unowned
  idea rather than one already routed to work. Also sweep the un-audited docs for references to the thing this change
  shipped — `docs/ideas.md`, and a retrospective's suggestion rows and prose, which the change marks with the
  suggestion's disposition (**Applied** where it applies it; its routing where it routes it: parked in `docs/ideas.md`,
  a named change, an issue; or closed where it decides against it) rather than removes. An open idea that still calls it
  "the proposed X" is itself a stale claim: update its prose in the same change, including any enumeration or count it
  carries ("two others remain open: A, B") that the change's new instance makes wrong. No auditor **owns** either file
  (each auditor's bullet states its own scope; the architecture-auditor's sweep reaches `docs/ideas.md` for a parked
  entry's staleness). For every docs edit the boundary is **causation _while the change is in flight_**: docs that ARE
  the change (new agent/command/skill documentation, README rows describing a new capability, the change's idea
  removal), and any edit the change causes that exists while it can still ride a branch — a lesson from its
  implementation (the capture), an idea its own sweep surfaced, a fact it falsified — ship with the change's PR, while
  an edit the change did not cause (a stale fact it merely passed by, a standalone reword, a refresh of an
  already-completed change) and one surfaced only after the change is done (a merge-time capture, caused by the change
  but with no branch left to ride) ships as its own docs PR, forked from `main`, batched into one docs PR rather than
  one per item. Decide the owner before committing — a docs PR forked from `main` cannot carry an edit committed on a
  change branch, so committing it there first for a clean tree silently leaves it out of the docs PR and `main`
  unchanged — and verify the fix against the merged PR's diff rather than the PR description, which can claim a change
  the diff does not contain. A parked-idea docs PR owes the refresh too: fold any durable fact the idea reveals into the
  relevant `AGENTS.md`/`README.md` section (e.g. add a newly surfaced manual pin to an existing enumeration) —
  `docs/ideas.md` is a prunable scratchpad, so a fact left only there is lost once the idea is implemented or dropped.
  The un-gated copies of what this file and the specs state take the same sweep, refreshed in the change that moves one:
  the `openspec/config.yaml` `context:` block (the runtime/Spring/Gradle versions, module count, service list, and
  Docker image names that OpenSpec shows the AI when creating artifacts), that file's per-artifact `rules` (which
  restate obligations stated here and in the specs, injected per artifact alone — `projectConfig.rules[artifactId]` — so
  a clause several artifacts need lives in each list rather than in a shared one), and the repository's GitHub
  description and topics. What is un-gated is their content and currency: `ci.yml`'s probe fails on a malformed item —
  the list's shape, since a bad one silently drops the whole artifact's rule set — but not on whether a rule's wording
  still matches `AGENTS.md` and the spec, so refresh a rule whose obligation moves even though the gate stays green, and
  `openspec validate` never checks the rest, so it drifts silently. A **removal** counts too: when a sweep deletes an
  entry as non-durable, check these copies for the same sentence — the fact has not moved, so the move rule does not
  fire (`disable-axoniq-console-message`'s sentence outlived #292's removal from `AGENTS.md` by four PRs in
  `config.yaml`, until the audit noticed). No auditor owns a surface outside the repository. A file can also _depend_ on
  such a surface rather than describe one: `SECURITY.md`'s private-reporting path is a dead end unless private
  vulnerability reporting is enabled. Enable the setting as part of the change that ships the instruction — a repository
  setting leaves no diff, so a diff-only review cannot see it — and name the enabling in the change's report. captured:
  park-retro-marking-idea (#281) captured: re-measure-load-test-baseline (#429) captured: mark-s3-applied captured:
  attribute-load-test-tail (#570) captured: park-review-loop-follow-ups captured: fix-openspec-config-rules
```

### Draft 3 — the merged record section (replaces `consistency-sweep` and `uniqueness-claims`)

Formatted; `2,961` bytes / `36` lines including its separator line (the two sections were `3,109` / `40`). The `Rule:`
paragraph joins to Draft 1's lead verbatim, and the whole record measures `16,460` bytes / `244` lines (was `16,608` /
`249`).

```text
## claim-site-sweep

Rule: **A doc-consistency sweep is scoped by the convention or the claim, not by the review's findings list — and a
claim about the code is verified against the code.**

The sweep scopes below come from these episodes. `fix-javadoc-consistency` wrote a `@param elasticsearchConverter`
reading "OpenSearch results to entities" for a converter that maps entities → OpenSearch; the field's Javadoc and its
`mapObject(ShowcaseEntity…)` call sites said so, and the implementation quick review caught it. The same change had to
add a field Javadoc the review never enumerated (`ShowcaseProjector.METER_NAME_PREFIX`), caught by the owner.

`apply-specs-audit-findings` normalized one coordinate spelling in the `spring-data-opensearch` requirement and left the
identical `org.springdoc: springdoc-openapi-starter-webflux-ui` spelling lower in the same file, deferred as "a future
audit" when a grep would have caught it; `fix-springdoc-coordinate-spelling` followed. A
`reconcile-showcase-cache-default` review corrected a reversed direction in the design's Context, and the same claim's
residue in the Decisions section — plus a "tenfold" that was a hundredfold — was caught only in the next pass.

Replacing the commit discipline left four restatements: two gotchas, the docs-refresh convention, and `docs/ideas.md`'s
header. The diagram bullet's `diagramPadding` rule survived in `diagrammer.md` until the quick review. A widened check's
describers went stale: the 2026-10-11 audit found `toolingUpdates`' worked example stale once the smoke's `pack` pin
gained its own entry, and `bump-tooling-pins-2026-10-16` left `/opsx-tool-update`'s "the check detects a release from
`ci.yml` alone" in place.

The web UI's Playwright e2e suite was added while `AGENTS.md` still said "the only e2e suite is
`showcase-api-gateway`'s", and the stale claim survived until a later cleanup pass (PR #116). The absence mode: the
`buildpackUpdates` check in #149 falsified the "`dependencyUpdates` and `helmUpdates` ignore bare `[versions]` entries"
sentence #148 had added one change earlier. The rename mode: the first audit found `ShowcaseApiController*` references a
rename had left behind, and the workflow-file rename `snyk.yml` → `dependency-security.yml` also lived in `SECURITY.md`,
ADR-0006, and `build.gradle.kts`'s `toolingUpdates` pin. The enumeration mode: `AGENTS.md` said the `ci.yml` and
`e2e.yml` workflows "additionally extend `gradle-home-cache-includes` with `nodejs`" while the npm-monitoring change
added that include to `dependency-updates.yml` and `dependency-security.yml` too. The non-doc mode: ADR-0009's "without
Axon Server" left `helm/chart/src/main/helm/values.yaml` still calling the `db-scheduler` settings "Axon Server
scheduler settings".

`captured: bump-tooling-pins-2026-10-16`, `captured: make-the-capture-selective`,
`captured: monitor-web-ui-npm-dependencies (#395)`, `captured: verify-unique-review-rounds`
```

### Draft 4 — the retargeted reference

One line inside the `recurring unit type` bullet:

```text
  enumerates the sweep rule's member grep is the sweep — and point the proposal's site list at the tasks that
```

### Draft 5 — Option 2's canonical procedure (measured, not recommended)

Placed as a new `### Workflow: Update the docs a change makes stale` under `## OpenSpec Workflow Agreement`. Formatted;
`3,808` bytes / `51` lines including its four `captured:` markers and its `evidence:` token (the procedure alone is
`3,600` / `46`); the two rules it replaces are `3,487` / `38`.

```text
### Workflow: Update the docs a change makes stale

**Trigger** — a unit alters anything the repository documents: a convention, a fact, a rule, a set's membership, a name,
a uniqueness or absence claim, a check, a declaration's documentation, or a recorded non-use.

**Procedure**

1. Name what the change altered, from the classes below. A change usually alters more than one.
2. Derive each site set by grepping the artifact or corpus for that class — never from the plan's list, and never from a
   review's findings list.
3. Classify each hit: a current-state description is corrected in this change, while a historical record keeps the
   values its run used — a baseline, a retrospective, or an audit states them in the past tense rather than being
   rewritten, and a retrospective suggestion is marked with its disposition rather than removed.
4. Route each edit by the `Docs refresh on change` convention's causation boundary.
5. Verify the fix against the merged pull request's diff rather than its description, which can claim a change the diff
   does not contain.

**The classes**

- a convention (a coordinate or identifier spelling, `MUST` vs `SHALL`, an enumeration) → every instance in the artifact
  or corpus;
- a corrected fact (a direction, a value, a magnitude) → every repetition, plus the statements that depend on it without
  repeating it (a caveat, a rationale, a concluding sentence);
- a rule the change authored or replaced → its paraphrases by the rule's **subject**, in every document that describes
  the workflow: `AGENTS.md`, file headers (e.g. `docs/ideas.md`'s), the `.opencode/` definitions, the specs, and the
  change's own artifacts;
- an audit finding → the pattern the finding is an instance of, not the finding alone;
- a widened or narrowed check → its describers, including a worked example in a definition;
- a javadoc or field-doc pass → every declaration in the touched classes, not only the ones a review named;
- a set that gained a member, or an enumeration of one → every live enumeration of the set, since a plain enumeration is
  the case a name-grep cannot see: grep its distinctive members across `.opencode/`, `openspec/specs/`, `AGENTS.md`,
  `README.md`, `docs/ideas.md`, and `scripts/` before writing the addition — a tool's module docstring and `--help` text
  are live enumerations too, and the change's own planning copies can stay identical while every live site goes stale —
  and when the set is an auditor's, sweep its definition, its trigger command, `AGENTS.md`'s summary of it, and the spec
  requirement that owns the sweep, which is a main spec and so needs a `MODIFIED` delta rather than a docs edit;
- a code symbol or file renamed or removed → the whole repository for the old name, config, values, and template
  comments included, since docs cite these names as examples and nothing resolves them;
- a second instance of something the docs call unique (an `only`/`sole`/`never` claim) → `AGENTS.md`, for the claim about
  the first instance; fix it in the same change, and re-verify such a claim against the repository whenever you edit its
  section rather than trusting the prose;
- a capability the change makes no longer absent (`nothing covers X`, `A and B ignore X`) → `AGENTS.md` and `README.md`,
  including a claim a recent change added;
- a component you record as deliberately _not_ used → the whole repository for its name.

**Completion** — every class the change touched has been swept, and every current-state hit is corrected or left as
history with its reason recorded.

captured: bump-tooling-pins-2026-10-16 captured: make-the-capture-selective captured: monitor-web-ui-npm-dependencies
(#395) captured: verify-unique-review-rounds

evidence: documentation-and-durable-claims
```

The convention entry point Option 2 requires — one clause inserted into `Docs refresh on change`'s first sentence, which
is what makes the measured `+432` reproduce:

```text
  refreshed to reflect the new state (commands, config, conventions, gotchas) — the sites each kind of change reaches
  are the `Workflow: Update the docs a change makes stale` workflow's — including an ADR whose Consequences name
```

### Preservation map

Every pilot obligation keeps a home. "Same text" means the sentence is unchanged in the draft.

| Old obligation                          | Survives in                                                                                                      |
| --------------------------------------- | ---------------------------------------------------------------------------------------------------------------- |
| A1–A3, A5–A10, A13–A16, A18, A20–A21    | Draft 2, same text                                                                                               |
| A4 (the idea rides this PR)             | Draft 2, "remove the change's idea … in this PR, not as a separate docs PR"                                      |
| A11 (ownership)                         | Draft 2, "each auditor's bullet states its own scope…"                                                           |
| A12 (docs that are the change)          | Draft 2, listed inside the causation boundary's first branch                                                     |
| A17, A19 (the copies' rationale)        | Draft 2, "The un-gated copies … take the same sweep" + "What is un-gated is their content and currency"          |
| B1, B2                                  | Draft 1, the lead (verbatim) and its first sentence                                                              |
| B3a–B3f                                 | Draft 1, rows 1–6                                                                                                |
| B4, B5                                  | Draft 1, the lead's second clause and the closing sentence                                                       |
| C1 (uniqueness — its target and timing) | Draft 1's uniqueness row — `AGENTS.md` "for the claim about the first instance", and "fix it in the same change" |
| C2                                      | Draft 1's uniqueness row — "re-verify such a claim against the repository whenever you edit its section"         |
| C3 (absence — its target)               | Draft 1's absence row — `AGENTS.md` and `README.md`, "including a claim a recent change added"                   |
| C4                                      | Draft 1, row 8 ("a code symbol or file renamed or removed")                                                      |
| C5                                      | Draft 1, row 7 — the six member paths, the `MODIFIED`-delta rule, and the auditor's four sites                   |
| C6                                      | Draft 1, row 7's "docstring and `--help` … planning copies can stay identical while every live site goes stale"  |
| C7                                      | Draft 1, row 10                                                                                                  |
| Every `captured:` marker                | Draft 1 carries all four; Draft 2 keeps all seven                                                                |
| Historical identifiers                  | Draft 3 keeps every one the two sections held                                                                    |
| R1, R2, R3                              | Unchanged; R3 is the policy the merge applies                                                                    |

The token diff drops one backticked token — `.opencode/`, from A11's removed auditor enumeration — and it names no
action or operand of its own. Every identifier an obligation acts on survives.

## Validation

The proposed instructions are exercised against the ten cases the request names. Each row is **reasoned** — the
instruction is read and applied — except where it names a grep whose output was observed; the note below says which.

| Case                                                         | Trigger the agent sees                                  | Authoritative instruction                                                            | Surfaces derived                                                                                          | Route                                        | Over-broad guard                                                    |
| ------------------------------------------------------------ | ------------------------------------------------------- | ------------------------------------------------------------------------------------ | --------------------------------------------------------------------------------------------------------- | -------------------------------------------- | ------------------------------------------------------------------- |
| Version bump                                                 | The catalog's version changed                           | Draft 2's version-bump clause                                                        | The five named docs; the old string only                                                                  | This PR (caused)                             | The literal old string; a baseline record keeps its recorded values |
| Rename (class, file)                                         | The old name is gone from the code                      | Draft 1, row 8                                                                       | Whole repository, config/values/template comments included                                                | This PR                                      | "the old name" — not every mention of the concept                   |
| Enumeration member added                                     | A set gained a member                                   | Draft 1, row 7                                                                       | The six live paths; an auditor's set also its definition, trigger command, summary, and owning spec delta | This PR, with the delta                      | "the set's distinctive members", not every doc mentioning the set   |
| Second instance invalidates an "only" claim                  | A second e2e suite, image, or workflow exists           | Draft 1's uniqueness row                                                             | `AGENTS.md`                                                                                               | This PR                                      | The claim about the first instance, re-verified against the repo    |
| New capability invalidates an absence claim                  | "Nothing covers X" is now false                         | Draft 1's absence row                                                                | `AGENTS.md` and `README.md`, including a claim a recent change added                                      | This PR                                      | The capability's absence statements only                            |
| Configuration-default change                                 | A default moves                                         | The configuration-default gotcha (retained, outside this pilot)                      | Its declared surfaces, the test assertions pinning them, and the docs describing them                     | This PR                                      | A historical record states its values in the past tense             |
| Review correction changing behavior or framing               | A `review-quick` finding says the model is wrong        | Review gate R1/R2 (unchanged) → Draft 1, rows 2–3 for the site set                   | Planning artifacts, the shipped copies, and the section inside an artifact                                | This PR                                      | The corrected claim and its dependants                              |
| Unrelated stale fact found during implementation             | A doc sentence is stale and the change did not cause it | Draft 2's causation boundary, second branch                                          | That doc only                                                                                             | Its own docs PR, forked from `main`, batched | "the change did not cause"                                          |
| Merge-time discovery                                         | After the merge, the merge itself revealed a lesson     | Draft 2, "one surfaced only after the change is done"; the capture workflow's step 4 | The artifact the lesson extends                                                                           | Its own docs PR                              | No branch is left to ride                                           |
| Historical measurement whose conditions must remain recorded | A baseline, retrospective, or audit quotes old values   | The configuration-default gotcha's past-tense rule; Draft 2's disposition marking    | That record, marked rather than rewritten                                                                 | Untouched, or its disposition marked         | "its measured data is what it records"                              |

**Cases that legitimately cause no edit.** A version bump whose old string appears only under
`openspec/changes/archive/**`; a rename whose old name survives only in an archived change dir; a doc quoting a rule
verbatim; a historical record quoting a value its run used.

**Observed, not reasoned:** the six pointers naming the convention (five in `AGENTS.md`, one in `ship-change/SKILL.md`),
the one site naming C by title, and the auditor's pairing derivation were found by grepping the live corpus. Every other
cell is reasoned from the drafted text.

## Policy and specification fit

- **Option 3 is permitted as written.** The merge class admits complementary entries; the merged text preserves every
  anchor and every piece of evidence, and fuses no lesson. The authoring standard's "one authoritative definition for
  each subject" is satisfied by the single rule and the single record section. The evidence-record requirement holds
  unchanged (D5).
- **No delta is owed for Option 3**, so `skip_specs: true` is accurate. Its grep evidence: no capability spec names
  either rule's subject; the only related text is the auditor definition's defect-class name "a 'the only X' that is no
  longer the only one", which names a defect the auditor looks for, not a requirement about the rule.
- **Option 2's amendment** is quoted in D5 and is not proposed. `agents-auditor`'s pairing check already derives the
  pair from the tokens, so it would need no change.
- **Provenance.** The four `captured:` markers move with the merged rule; the record's `Rule:` key is re-derived rather
  than copied; the section title is an address, not a key.

## Measurement and maintenance

### The figures

| Quantity                             | Value                                                                                 |
| ------------------------------------ | ------------------------------------------------------------------------------------- |
| Current pilot-span size              | 10,670 bytes / 99 lines                                                               |
| Replacement size                     | 9,904 bytes / 93 lines (Draft 1 + Draft 2)                                            |
| Local warnings and pointer overhead  | 0 added; neither draft introduces a pointer                                           |
| Consolidation root reduction         | **−776 bytes** (324,296 → 323,520), **−7 lines** (3,361 → 3,354)                      |
| Capture contribution                 | **+418 bytes** (one merge-verification clause in `Avoid redundancy`)                  |
| Whole-unit `AGENTS.md` change        | **−358 bytes** (324,296 → 323,938), **+1 line** (3,361 → 3,362)                       |
| Consolidation subtotal               | −766 bytes across the three spans; −9 for the reference; −1 for the removed separator |
| Evidence-record change               | −148 bytes (16,608 → 16,460), −5 lines (249 → 244)                                    |
| Total maintained-content change      | **−506 bytes** (root −358 + record −148)                                              |
| Supporting edits outside `AGENTS.md` | one record file; no spec, skill, agent, command, or README edit                       |

Per-merge figures, each measured alone on the whole file and summing to the subtotal: the un-gated copies −104, the
auditor ownership −128, the delivery instance −31, the idea-removal route −82 (convention subtotal −345); the B+C merge
−422; the reference retarget −9. `345 + 422 + 9 = 776`.

The capture this unit owes (`lesson-capture`) was run and applied one proposal: a merge-verification clause in the
`Avoid redundancy` convention, +418 bytes. Its second proposal — that another artifact's empty result carries no verdict
— failed the promotion gate on its evidence threshold (one occurrence, not severe), so it is recorded in the
prior-decisions table above rather than added. The whole unit is therefore −358 bytes / +1 line in `AGENTS.md` and −506
bytes of maintained content.

### How future corrections reach the canonical source

- Draft 1 is the authority for the site set of a changed claim; a future lesson about a new change class is **added as a
  row**, never as a new rule beside it.
- Its episode detail goes to the `claim-site-sweep` section, whose `Rule:` key ties it to the rule; the `agents-auditor`
  reports a rule whose `evidence:` token has no matching section, or a section matching no live rule.
- A future capture proposing a second sweep rule is rejected by the authoring standard's one-authoritative-definition
  clause; no new mechanism is introduced.
- The convention's repetitions are not re-introduced with a pointer: a future obligation there states its condition,
  action, operand, and exceptions in place, as the retained clauses do.

## Claims table

Each row is a fact this change states about its evidence, with the command that derives it. Rows about an edited
artifact are anchored to its pre-change state (`git show HEAD:<path>`).

|   # | Claim                                                                                                                             | Source / command                                                                                                                                                                                                     | Result                                                                                                                           |
| --: | --------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------- |
|   1 | The branch is forked from freshly fetched `origin/main`, and the preceding reduction is merged                                    | `git rev-parse HEAD; git rev-parse origin/main; git log --oneline -4`                                                                                                                                                | both `3903a889…`; `HEAD` is `Convert the remaining Documentation Gotchas rules… (#652)` over `#651`, `#649`, `#648`              |
|   2 | The working tree was clean before planning began                                                                                  | `git status --porcelain --untracked-files=all`, run before this change dir existed                                                                                                                                   | empty; it now lists this unit's four untracked artifacts                                                                         |
|   3 | `AGENTS.md` is 324,296 bytes / 3,361 lines pre-change                                                                             | `git show HEAD:AGENTS.md \| wc -c -l`                                                                                                                                                                                | `324296 3361`                                                                                                                    |
|   4 | The pilot spans are 10,670 bytes / 99 lines                                                                                       | the span script above, on `git show HEAD:AGENTS.md`                                                                                                                                                                  | A `7183/61`, B `1461/19`, C `2026/19`                                                                                            |
|   5 | `AGENTS.md` carries no markdown table                                                                                             | `git show HEAD:AGENTS.md \| grep -c '^\|'`                                                                                                                                                                           | `0`                                                                                                                              |
|   6 | The consolidation subtotal is −776 bytes / −7 lines                                                                               | apply Drafts 1–4 to `git show HEAD:AGENTS.md`, format with Prettier 3.9.10 at `printWidth: 120`, `proseWrap: always`, then `wc -c -l`                                                                                | `323520 3354`                                                                                                                    |
|   7 | Draft 1 is 3,066 bytes / 35 lines and Draft 2 6,838 / 58                                                                          | the same formatting step applied to each draft alone                                                                                                                                                                 | `3066/35`, `6838/58`; replacement `9904/93`                                                                                      |
|   8 | The two record sections are 3,109 / 40, the merged section 2,961 / 36 including its separator, and the record 16,460 / 244        | `wc` on the two sections of `git show HEAD:docs/guidance-evidence/…`, then Prettier over the whole record with Draft 3 applied                                                                                       | section `3109/40` → `2961/36`; file `16608/249` → `16460/244`                                                                    |
|   9 | One backticked token drops, and it carries no action or operand                                                                   | a token diff of the replaced spans against their replacements                                                                                                                                                        | dropped: `.opencode/` (A11's removed enumeration); the four `captured:` markers match exactly                                    |
|  10 | The convention has six inbound pointers                                                                                           | a flattened-phrase grep for `docs-refresh convention` over `git show HEAD:AGENTS.md` and the skill, plus a read of the compound phrase                                                                               | `4` exact matches in `AGENTS.md`, one compound phrase naming it beside `README-design-intent`, and `1` in `ship-change/SKILL.md` |
|  11 | One live `AGENTS.md` site names span C by its old title                                                                           | `git show HEAD:AGENTS.md \| grep -n 'the only X'`                                                                                                                                                                    | two matches: the rule itself and the recurring-unit-type bullet                                                                  |
|  12 | The 2026-10-19 audit reported no merge candidate, and names neither rule                                                          | `sed -n '7,14p' docs/audits/2026-10-19.md`; `grep -n 'doc-consistency\|the only X' docs/audits/2026-10-19.md`                                                                                                        | `2 findings (0 merge, 0 removal, 0 route)`; the grep returns no match                                                            |
|  13 | The merge class admits "overlapping or complementary entries", and the constraint is on the merged text                           | `grep -n 'complementary entries' .opencode/agent/agents-auditor.md`; `sed -n '906,913p' openspec/specs/showcase/quality/agent-skills/spec.md`                                                                        | the class and the criterion, quoted above                                                                                        |
|  14 | The 2026-09-21 audit's merge paragraph is about a different family                                                                | `sed -n '44,52p' docs/audits/2026-09-21.md`                                                                                                                                                                          | it names the "verify against a known positive" rules, not B or C                                                                 |
|  15 | Option 2's complete draft is 3,808 / 51 and grows the file by 432 bytes / 13 lines                                                | format Draft 5 alone; replace spans B and C with it and add the convention's pointer in `git show HEAD:AGENTS.md`; format the whole file                                                                             | `3808/51`; file `324728/3375`                                                                                                    |
|  16 | The record requirement is "one file per `## Gotchas` subsection"                                                                  | `grep -n 'one file per' openspec/specs/showcase/quality/agent-skills/spec.md`                                                                                                                                        | the clause, quoted in D5                                                                                                         |
|  17 | The previous tranche's eleven adopted rules ran −401 (C16) to −1,317 (C11), and its delivered conversion was −8,269 bytes (2.49%) | `grep -n 'C11 .*−1,317'`; `grep -n 'C16 .*−401'`; `grep -n 'Adopted subtotal (11 rules)'` — all in `openspec/changes/archive/2026-10-20-convert-remaining-gotchas-rules/design.md`; `sed -n '38p'` its `proposal.md` | the boundary rows, the subtotal row naming eleven rules at `−8,269`, and "−8,269 bytes (2.49%)"                                  |
|  18 | No capability spec names either rule's subject                                                                                    | `grep -rn 'doc-consistency\|the only X' openspec/specs/`                                                                                                                                                             | no match                                                                                                                         |
|  19 | The capture adds 418 bytes and the whole unit is −358 bytes / +1 line                                                             | `git diff --numstat AGENTS.md` after `spotlessApply`                                                                                                                                                                 | `79 78`; `wc -c -l` → `323938 3362`                                                                                              |

Dismissals, one reason per kind:

- **A source that is a command's output** is dismissed as an enumerating clause: the baseline and measurement rows name
  the command and the figure it emitted, never the lines it printed.
- **A count of a growing corpus** (the record's section count, the live pointers) is stated with the command that lists
  the instances, because the change itself can add one.
- **A claim about the change's own mechanism** (that Prettier is the formatter, that the record is the evidence home) is
  derived from the build configuration and the spec, not from a prior design's prose.
- **A `claim-scan.py` candidate on a re-wrapped line of a replaced span** is dismissed as a reflow of text present at
  `HEAD`: the word-level span diff shows every changed region inside one of the four merged statements, and the token
  diff shows the single dropped identifier.
- **A candidate inside the change's own artifacts** is resolved by the claims table above, which the implementation
  round re-derives; the tasks' `**Done**` notes restate rows 3, 6, 7, and 8 rather than adding facts.
- **A `[path]` or `[cross-ref]` candidate** names an identifier the replaced span already carried — the token diff is
  the completeness check — so it introduces no new claim about the repository.
- **An `[absolute]` candidate** (`only`, `every`, `never`, `all`) sits inside a row carried from B or C, inside a
  convention clause whose wording is unchanged apart from re-wrapping, or inside a clause this unit's capture authored —
  in the last case the quantifier's referent is that clause's own operand ("every anchor", "never an added one").
- **A ledger row's count** is the review report's own field, recorded by the round that produced it.

## Risks / Trade-offs

| Risk                                                                   | Mitigation                                                                                                                                                                             |
| ---------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| A reviewer reads the merge as blending two distinct lessons            | The classification table answers it part by part; if it still fails, the remedy is retention of B and C, not an exception.                                                             |
| The merged rule reads as a checklist an agent can complete from memory | The lead keeps both warrants, and every row keeps its operand.                                                                                                                         |
| C's trigger is harder to find once its lead becomes a row              | The row names the claim in its own words ("a second instance of something the docs call unique"), so a text search still reaches it; the cost is recorded in D3 rather than dismissed. |
| The record's pairing breaks silently                                   | The merged section's `Rule:` paragraph is compared to the merged lead with normalized whitespace, and `agents-auditor` is the standing check.                                          |
| The convention loses a clause in the condensing                        | The preservation map names each folded obligation; the token diff and a sentence-by-sentence re-read are verification tasks.                                                           |
| The unit is too small for its review cost                              | Stated as measured: 0.239% of the file, which alone does not carry it. D3 names what else does, and retention is the acceptable alternative.                                           |
