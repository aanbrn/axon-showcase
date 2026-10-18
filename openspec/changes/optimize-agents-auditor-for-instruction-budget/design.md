# Design

## Context

The `agents-auditor` reconciles the project-owned agent tooling along two axes — consistency and conciseness — and
reports four things: findings (grouped by severity), the advisory class for an excluded file that contradicts our usage,
the accretion class for meta rules with their origins, and the authoring-system gap class for a recurring defect traced
to the mechanism that produces it. Within conciseness it reports three standing analyses — merge, removal, and route
candidates — counted in the verdict line. It never edits a file. Its report contract is the shared one, whose verdict
line names the count of every class the report carries.

**What the change measures, verified rather than assumed.**

| Fact                                                         | Value                                                                                                                                                             | Derivation                                                                                                               |
| ------------------------------------------------------------ | ----------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------ |
| `AGENTS.md` size before this change                          | 357,100 bytes; 3,622 lines                                                                                                                                        | `git show HEAD:AGENTS.md`, counted with `wc` (this change edits the file, so the figure anchors to its pre-change state) |
| Its 14 top-level sections, largest first, before this change | Gotchas 1,500 lines/154,759 B (43.3%); Conventions 602/67,338; OpenSpec Workflow Agreement 668/62,434; Continuous Integration 249/23,198; Build & Test 174/13,562 | the same pass over `git show HEAD:AGENTS.md`                                                                             |
| Whether the loading mechanism caps an instruction file       | it does not — the loader reads each file whole and emits it verbatim                                                                                              | the `SessionInstructions.load` implementation in the installed CLI                                                       |
| The effective bound                                          | the pinned model's context window: 1,000,000 tokens for `opencode-go/deepseek-v4.1-flash` and for `…-deepseek-v4-pro`                                             | the models catalog in the local OpenCode cache                                                                           |
| Whether a subagent pays that surface                         | it does — a subagent's context carries the whole file                                                                                                             | a context probe of a subagent (it quoted the file's first section and named all 14 headings)                             |
| Whether a nested `AGENTS.md` is loaded on demand             | it is — a `read` walks up from the file's directory to the project directory for `AGENTS.md` files and loads each as session instructions, deduped per session    | the read tool's implementation in the installed CLI                                                                      |
| Whether the repository has any nested `AGENTS.md`            | none                                                                                                                                                              | `git ls-files`                                                                                                           |
| Growth since the newest prior report                         | the 2026-10-11 report recorded 2,899 lines; the file held 3,622 before this change — +723 lines, +25%, in seven days                                              | that report's own text against the pre-change measurement                                                                |
| The accretion metric's history                               | `~71` (2026-10-04 scheduled), `4` (2026-10-04), `~95` (2026-10-11)                                                                                                | the three reports                                                                                                        |
| Where the auditor runs                                       | `/audit-agents` on demand, and the weekly `audit` workflow (`cron 13 17 * * 6`, model `opencode-go/deepseek-v4-pro`)                                              | the command and the workflow                                                                                             |
| Whether the CI checkout can attribute a rule's origin        | it cannot — `actions/checkout` is shallow there, so `git blame` fails; the local clone is not shallow (863 commits)                                               | the workflow's checkout step and `git rev-parse --is-shallow-repository`                                                 |

**The premise interrogated.** The task asks for an auditor that optimizes `AGENTS.md` "as a bounded, always-loaded
instruction interface" and requires it to report "the active instruction-loading limit and its source" and "the byte
position and location of the first potentially unreachable rule". Interrogating that premise against the mechanism
rather than the intent yields three recorded conflicts, each stated here with why the design differs:

- **No configured instruction-loading limit exists.** OpenCode applies no size cap to instruction files, no
  `instructions` key is configured in either the project or the global config, and the whole 357 KB file reaches a live
  session's system prompt. A rule therefore cannot be "unreachable" for being past a byte cap, and reporting one would
  be a false claim of exactly the kind this auditor exists to catch. The design keeps the field and re-derives it: the
  ledger reports the **effective** bound with the source establishing it, states explicitly whether the loading
  mechanism caps the surface, and expresses the threshold as an estimate with its derivation rather than as a position
  at which content is dropped.
- **The repository already records that the bound is a discipline, not a cap.** `AGENTS.md` says its "growth is bounded,
  but the bound is a discipline rather than a hard cap", and the capture's rule is to leave the file no larger than it
  was by pointing at the spec rather than copying it. This change does not add a cap and does not restate that rule; it
  gives the existing discipline the measurement it lacks — size, growth, and what a rule costs by staying loaded.
- **The adjacent parked idea is not the same question.** `docs/ideas.md` parks "Trend the audit counts across reports",
  whose objective is a digest of the verdict counts over time. This change's ledger records the size and the growth
  since the newest prior report, which is one trendable series rather than that digest; the entry is updated to name it
  and stays parked.

The change rests on measurement and on the artifact set the last analogous archived change established
(`widen-audit-agents-to-authoring-gaps`, the floor for a subagent/command change), not on a recorded mis-fire: no clause
in the auditor or the spec measures the always-loaded surface, and none proposes relocating a rule out of it.

**The revision's premise: this change's first draft made its own extraction contract circular.** It required a verified,
reachable destination _before_ the audit could report an extraction, so a source unit whose destination does not exist
yet could never be surfaced at all — the auditor could only report "retain". The post-restart run demonstrated the cost:
it considered the Continuous Integration, Kubernetes, and Docker reference sections, rejected each as a candidate on a
failed retrieval path, and reported them as _retained_ with no actionable recommendation, even though they are among the
file's largest sections. The safety conditions are sound — a candidate that may be applied must have a reachable
destination — so the revision does not weaken them. It adds a preceding, **non-applicable** class,
`extraction opportunity`, that carries exactly the analysis the circular contract suppressed: the source unit, the
destination type its content justifies, why no destination exists yet, and what must remain at root until one does.

## Goals / Non-Goals

**Goals:**

- Give the auditor a measured **instruction-budget ledger** for `AGENTS.md`: size, size by section, the effective bound
  and its source, the first rule beyond a derived threshold, the growth since the newest prior report, the sections and
  `captured:` units responsible, the bytes safely removable from the always-loaded surface, the rules that must stay
  loaded with the risk their unavailability carries, the rules safe to load on demand with their trigger and retained
  pointer, and the post-change verification.
- Require every reduction candidate to carry the seven fields — location, the decision it changes, its trigger, the
  unique content at risk, its loading cost, its disposition, and its verification — so a candidate is weighable rather
  than a rewrite.
- Add **extraction** as a distinct disposition: a verified partition of one `AGENTS.md` unit into a retained root entry
  point, an authoritative destination that loads when the trigger occurs, and a tested retrieval path.
- Add **extraction opportunity** as a preceding, non-applicable class: a source unit that is materially costly at root
  but whose destination does not yet exist — and therefore cannot yet pass the retrieval-path test — with its content
  classification, its justified destination type, its blocking precondition, and the content that must remain at root
  until a destination is created and verified.
- Keep root guidance scoped to what an agent must see before safely starting work, without weakening any existing check.

**Non-Goals:**

- **No cap, no quota, no line budget.** The ledger measures; it does not gate. A number would make the file's size a
  pass/fail and would contradict the recorded discipline.
- **No new auditor, no new axis, no new class in the verdict line.** The ledger is a section of the auditor's report;
  the four reported classes and the verdict line's counts are unchanged, so the accretion metric stays comparable across
  reports.
- **No weakening of the existing checks.** Consistency, conciseness, the advisory class, the accretion class with its
  fixed metric, the authoring-system gap class, the fix-scope partition, and the never-edit rule all stay.
- **No weakening of the extraction candidate's conditions, and no wish-list.** An opportunity never becomes applicable
  without a destination that exists and a retrieval path that passes; it is bounded by the same content classification
  and by a material-cost test, so it cannot become a general "this could move" list.
- **No application of any candidate.** The report is the deliverable; applying a reduction stays with the standing
  owner-gated unit, and an extraction's application routes to the change workflow.

## Decisions

**The field is reported as an effective bound, never as an invented cap.** Alternatives rejected: reporting a byte cap
(none exists — a false claim); omitting the field (the bound is real and the task asks for it); reporting the model's
context window as "the limit" without saying OpenCode applies none (true but incomplete — it hides that the risk is cost
and attention, not truncation). The ledger states the mechanism's own behavior, names the source of the effective bound,
and labels the threshold an estimate with its derivation.

**The new dispositions live in the ledger, not the verdict line.** Alternatives rejected: adding `extraction` and the
other dispositions to the verdict line's counts (it would break the accretion metric's comparability, which the
2026-10-10 change fixed precisely so successive reports could be compared, and would extend a line the shared report
contract already bounds); a second counted class (the ledger is a measurement, not a finding class). The verdict line
and the four classes are preserved verbatim; the ledger carries the new material.

**`extraction` is a distinct disposition, not a synonym for `route`.** A `route` candidate reduces a rule to a pointer
at a deterministic mechanism that already enforces its subject; an `extraction` candidate partitions a unit into a
retained root entry point plus an authoritative destination that is _loaded_ when its trigger occurs. Different subjects
— a gate versus a loaded destination — so a merged disposition would lose the retrieval-path test that makes an
extraction safe. The eight conditions and the retrieval-path test are what separate them.

**The ledger carries dispositions, not only figures.** Alternatives rejected: reporting the measurements alone (a size
with no statement of what may move, what must stay, and how a move would be verified is not actionable, and the owner
would have to re-derive the analysis the pass exists to perform); reporting the must-stay and on-demand lists as
findings (they are the ledger's own statements about the surface, not defects in it). The ledger therefore states the
bytes safely removable, the rules that must remain loaded with the risk their unavailability carries, the rules safe to
load on demand with their trigger and retained pointer, and the post-change verification, alongside the measurements.

**An opportunity is a separate, non-applicable class, not a weakened candidate.** Alternatives rejected: relaxing the
candidate's conditions so an unreachable destination could still be reported (it would let an agent apply a move it
cannot follow, which is the one thing the retrieval-path test exists to prevent); folding the case into the extraction
candidate as a lower-confidence variant (the two differ in kind — one may be applied, the other may not — and the
verdict line, the reduction unit's scope, and the report's shape all turn on that distinction); reporting the case as a
finding (it is a design recommendation about the file's structure, not a defect in the file's content); and leaving the
case unreported, as the first draft did (the auditor then has nothing actionable to say about the file's largest
sections). The opportunity is therefore bounded by the same content classification and by a material-cost test, must
state that its destination is a design hypothesis, and must name the content that stays at root until a destination
exists and passes the test.

**The destination mapping is derived from the content's class, and a new destination needs an owner.** Alternatives
rejected: a flat list of destination types chosen by taste (the mapping is a decision table, and the class determines
the type); allowing a generic "documentation" destination or a catch-all extracted-guidance directory (both defer the
real decision and create an unowned home); and permitting a proposed new destination without a named owner, a stable
scope, and multi-task discoverability (a destination no future task can find fails the retrieval path by construction,
so it would make every opportunity unapplicable).

**The spec owns the obligations; the definition owns the method and the tables.** Consistent with the existing pair: the
requirement states what the auditor must report and the conditions a candidate must meet; the definition carries the
field tables, the destination-type table, the prioritization order, and the procedure. The rules the new pass must not
restate are **pointed at**, not copied: the internal-control-flow clause is `AGENTS.md`'s own growth-bound rule — which
the definition points at, while the delta states the scope it places on the new disposition — and the "a meta rule is
not a removal candidate for being meta" rule is the accretion paragraph's.

**A nested `AGENTS.md` is a live destination, verified rather than assumed.** The read tool loads it on demand, so it is
a real destination type for directory-local guidance — with its real caveat, which the retrieval-path test exists to
catch: it loads on the first `read` under its subtree, so a task that works in that directory through another tool may
never trigger it. The ledger reports a failed retrieval path as a reason to retain the content.

**The reduction unit's enumeration widens by one word plus one scenario.** `apply-audit-reductions.md` and the
reduction-unit requirement both enumerate the candidates they apply (merge, removal, route). Adding `extraction` to that
set without widening them would leave two live enumerations stale, so both are updated, and the unit's application of an
extraction is routed to the change workflow — the move creates or changes a destination the change owns, which the
requirement's existing content-move routing already anticipates. The unit's procedure is otherwise unchanged; the
report-only scope of _this_ change is what keeps the two apart.

**The origin-source limitation is preserved and now has a second axis.** The audit already reports that `git blame` is
unavailable in the scheduled run's shallow checkout. The ledger's growth figure depends on the newest prior report
rather than on history, so it survives that limitation; the pass reports `none recorded` when no prior report exists, as
the accretion metric already does.

## Risks / Trade-offs

- **A judgment, not a gate** → the ledger and its candidates cost the owner review lines, not a broken build; nothing
  here can fail CI.
- **An estimate read as a measurement** → the threshold is labelled an estimate with its derivation, and the ledger
  states the mechanism's own behavior beside it, so a reader cannot take it for a configured cap.
- **Report length** → the ledger is a section with a fixed set of figures plus a candidate table and an opportunities
  subsection; the report contract's per-item budget still applies, and a report with no candidates and no opportunities
  collapses both to a line.
- **An opportunity read as a ready move** → it is not counted in the verdict, not applied by the reduction unit, and it
  must state that its destination is a design hypothesis; the candidate's own conditions remain the only route to an
  applicable move.
- **An opportunity list that grows without bound** → it is bounded by the material-cost test and by the requirement that
  the destination type be justified from the content's class, and a global or cross-cutting source must be retained
  rather than listed.
- **Requirement size** → the audit requirement grows by four body paragraphs and fourteen scenarios. Accepted over a new
  requirement because the requirement already owns the audit's report contents and the conciseness analyses; the
  alternative (a separate requirement for the budget pass) was rejected as splitting one auditor's contract across two
  requirements and costing a permanent corpus entry.
- **Overlap with the reduction unit** → the two state different subjects (proposing a candidate versus applying an
  approved one); the extraction disposition's application is routed to the change workflow, so the unit does not need a
  new procedure.
- **A definition edit is served from the pre-change copy until a reload or a new session** → a smoke-run of the edited
  definition is a precondition for the archive, per `AGENTS.md`'s subagent-reload gotcha.

## Migration Plan

None: the change takes effect on the next audit run — the surfaces are the proposal's Impact. A `## Purpose` refresh is
owed at the archive: the capability's Purpose enumerates what the auditor reports ("the merge, removal, and route
candidates it reports, and the authoring-system gaps it traces to a mechanism that produces an artifact") and now omits
the budget ledger and the extraction disposition. A delta cannot carry a Purpose, so the edit lands with the archive
commit.

## Claims table

| Claim                                                                                                                                                                                | Source or deriving command                                                                                                                                                                                                                                                      |
| ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `AGENTS.md` was 357,100 bytes / 3,622 lines before this change                                                                                                                       | `git show HEAD:AGENTS.md`, counted with `wc -c -l` (the change edits the file, so the row anchors to its pre-change state)                                                                                                                                                      |
| Its 14 top-level sections and their sizes, before this change                                                                                                                        | the same pass over `git show HEAD:AGENTS.md` (`## ` headings, line and byte counts per section)                                                                                                                                                                                 |
| OpenCode applies no size cap to an instruction file                                                                                                                                  | the installed CLI's `SessionInstructions.load` implementation (it reads each file whole and emits `Instructions from: <path>` + content)                                                                                                                                        |
| No `instructions` key is configured                                                                                                                                                  | `grep -n instructions .opencode/opencode.json ~/.config/opencode/opencode.jsonc` (no hit) and `opencode debug config`'s document keys                                                                                                                                           |
| The effective bound is the model's context window                                                                                                                                    | `~/.cache/opencode/models.json` — `opencode-go/deepseek-v4.1-flash` and `…-v4-pro` both `{context: 1000000}`                                                                                                                                                                    |
| A subagent's context carries the whole file                                                                                                                                          | a context probe of a subagent: it answered yes, quoted "Wrap code and text at 120 characters.", and named all 14 level-2 headings                                                                                                                                               |
| A nested `AGENTS.md` is loaded on demand                                                                                                                                             | the installed CLI's read-tool implementation: `s.up({targets:["AGENTS.md"], start:<read file's dir>, stop:<project dir>})` feeding `SessionInstructions.load`                                                                                                                   |
| The repository has no nested `AGENTS.md`                                                                                                                                             | `git ls-files \| grep -i agents.md` (only the root file; the other hits are filenames containing "agents.md")                                                                                                                                                                   |
| The 2026-10-11 report recorded 2,899 lines and ~95 accreted units                                                                                                                    | `docs/audits/2026-10-11.md` (its accretion section)                                                                                                                                                                                                                             |
| The accretion metric's earlier values                                                                                                                                                | `docs/audits/2026-10-04-scheduled.md` (`~71`) and `docs/audits/2026-10-04.md` (`4`)                                                                                                                                                                                             |
| The auditor runs on demand and weekly, on the pro model                                                                                                                              | `.opencode/commands/audit-agents.md` and `.github/workflows/audit.yml` (`cron 13 17 * * 6`, `model: opencode-go/deepseek-v4-pro`)                                                                                                                                               |
| The CI checkout is shallow, the local clone is not                                                                                                                                   | `.github/workflows/audit.yml`'s `actions/checkout` step (no `fetch-depth`) and `git rev-parse --is-shallow-repository` (`false`; 863 commits)                                                                                                                                   |
| The audit reports four classes and three standing analyses, and never edits                                                                                                          | `git show HEAD:.opencode/agent/agents-auditor.md`                                                                                                                                                                                                                               |
| The pre-change auditor and spec measure no always-loaded surface and propose no relocation                                                                                           | `git show HEAD:.opencode/agent/agents-auditor.md` and `git show HEAD:openspec/specs/showcase/quality/agent-skills/spec.md`, re-checked with a scoped `grep` over instruction-budget, extraction-candidate, and size-by-section terms (no hit)                                   |
| This change's first draft required a verified, reachable destination before an extraction could be reported, so a source unit with no destination could only be reported as retained | the unit's round-5 report (the pre-revision delta's conditions) and the post-restart run's report, which rejected the Continuous Integration, Kubernetes, and Docker reference sections as candidates on failed retrieval paths and reported them as retained                   |
| The post-restart run's growth derivation is more precise than the prior report's approximation                                                                                       | that run: `AGENTS.md` at the last commit before 2026-10-12 was 2,907 lines / 302,189 bytes, against the 2026-10-11 report's "~2,899 lines"                                                                                                                                      |
| A destination type is derivable from the content's class, and a proposed new destination needs an owner                                                                              | the post-restart run's extraction analysis: it rejected its third candidate for a failed condition 3 (no destination loaded or discoverable at the workflow-edit trigger) and kept a pointer pattern where the destination already carried the behavior but not the identifiers |
| The verdict line names the count of every class the report carries                                                                                                                   | the `agent-skills` spec's "Report-producing subagents follow a shared report contract" requirement, "A report opens with its verdict" scenario                                                                                                                                  |
| The accretion count uses one fixed metric, so reports are comparable                                                                                                                 | the audit requirement's accretion paragraph and its "The accretion count uses one fixed metric" scenario                                                                                                                                                                        |
| The reduction unit enumerates the candidates it applies                                                                                                                              | `git show HEAD:.opencode/commands/apply-audit-reductions.md` and the reduction-unit requirement's body                                                                                                                                                                          |
| `AGENTS.md` records its growth bound as a discipline rather than a cap                                                                                                               | `git show HEAD:AGENTS.md` (the capture section's growth-bound bullet)                                                                                                                                                                                                           |
| `docs/ideas.md` parks the trend of the audit counts                                                                                                                                  | `git show HEAD:docs/ideas.md` (the "Trend the audit counts across reports" entry)                                                                                                                                                                                               |
| The Purpose enumerates what the auditor reports                                                                                                                                      | `git show HEAD:openspec/specs/showcase/quality/agent-skills/spec.md` (the `## Purpose`)                                                                                                                                                                                         |
| `AGENTS.md`'s prose is read by gates                                                                                                                                                 | `git show HEAD:AGENTS.md` (`verifyInstallCommands`, `verifyDocumentedPins`, `verifyCapturedMarkers`) and `build.gradle.kts`                                                                                                                                                     |
| The auditor is described in `AGENTS.md`, `README.md`, and its command                                                                                                                | `git show HEAD:AGENTS.md` (the `agents-auditor` bullet), `git show HEAD:README.md` (the audit prose and two table rows), `git show HEAD:.opencode/commands/audit-agents.md`                                                                                                     |
| The last analogous change's artifact set                                                                                                                                             | `openspec/changes/archive/2026-10-17-widen-audit-agents-to-authoring-gaps/` (its proposal, tasks, and design)                                                                                                                                                                   |

**Dismissals** (the `scripts/claim-scan.py` candidates for this unit's diff, resolved to neither a row nor a fact — one
reason per kind, since a list of a kind's instances is incomplete by construction and the artifacts' own growth moves
the total): **`absolute`** are modal or structural wordings in prose about behavior or about the change's own rules
(`never`, `every`, `all`, `none`), not facts about the repository; **`path`** are file, module, command, or symbol
references rather than claims; **`count`** are the size, section, growth, and metric figures, which are the claims
table's rows, and the remaining numerals are those rows' own values or the delta's verbatim copies of the pre-existing
spec's text; **`cross-ref`** are references to another artifact, or a fraction-style figure (`1,500 lines/154,759 B`)
the scanner reads as a path. The delta's blocks are the main spec's two requirements copied verbatim, so their
quantifiers and cross-references are the pre-existing spec's text rather than claims this unit states; and the
`docs/review-rounds.tsv` rows this unit appends are its own record — a round number and a class label in a one-line
record, not a claim.
