# Design

## Context

`proposal.md` asks whether separating a rule's operational instruction from its historical evidence can reduce the
always-loaded `AGENTS.md` while keeping every operational meaning and making the evidence recoverable. This design
answers it with a bounded pilot in two Gotchas subsections, gives every rule in them a disposition, drafts the rewrites
that carry the answer, and specifies one evidence destination before recommending a unit.

**The objective, stated once.** The primary objective is **root-file reduction** — the surface loaded into every session
— subject to two constraints: the root text must still support the governed decision, and the evidence must remain
recoverable. Total maintained content is a _separate cost_, not a constraint that every relocation must shrink the
repository. An earlier draft of this design treated "total content does not fall" as a reason to reject a destination;
that was the wrong criterion, and this revision corrects it.

### Reproducible baseline

| Fact              | Command                           | Result                                     |
| ----------------- | --------------------------------- | ------------------------------------------ |
| Revision          | `git rev-parse HEAD`              | `c5dea506f00d12761e7941d8e3c80eac5718fbf8` |
| Working tree      | `git status --porcelain \| wc -l` | `0` at that revision, before this change   |
| `AGENTS.md` size  | `wc -c -l AGENTS.md`              | `338964` bytes / `3463` lines              |
| `## Gotchas` size | the census block below            | `139963` bytes / `1369` lines (41.3%)      |

Size by top-level section, measured from each `##` heading through the line before the next one; the rows sum to 338,951
bytes, and the 13-byte remainder is the file's title line and its blank line.

| Section                          |   Bytes | Lines | Share |
| -------------------------------- | ------: | ----: | ----: |
| `## Gotchas`                     | 139,963 | 1,369 | 41.3% |
| `## OpenSpec Workflow Agreement` |  71,844 |   767 | 21.2% |
| `## Conventions`                 |  68,998 |   619 | 20.4% |
| `## Continuous Integration`      |  12,772 |   145 |  3.8% |
| `## Build & Test`                |  10,761 |   149 |  3.2% |
| `## Kubernetes Deployment`       |  10,671 |   124 |  3.1% |
| `## Local Development`           |   7,327 |    84 |  2.2% |
| `## Docker Images`               |   6,742 |    69 |  2.0% |
| `## Structured Writing Standard` |   4,147 |    56 |  1.2% |
| `## Architecture`                |   1,776 |    28 |  0.5% |
| `## Project Overview`            |   1,214 |    15 |  0.4% |
| `## Key Environment Variables`   |   1,185 |    15 |  0.3% |
| `## Prerequisites`               |   1,150 |    14 |  0.3% |
| `## Line Length`                 |     401 |     7 |  0.1% |

### Prior decisions recovered

| Prior unit                                             | What it settled                                                                                                                                                                                                                                                                                                                                          |
| ------------------------------------------------------ | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `reduce-agents-md-loaded-surface` (2026-10-20, merged) | The task-scoped extraction is done (−4.98%, measured). Its preservation policy is "Preserve every imperative, warning, exception, identifier, gate condition, and `captured:` provenance marker" (`design.md`:202), so a gate condition travels with the moved rule. It states no rule about an entry's _internal_ split, which is this unit's question. |
| `add-extraction-procedure` (2026-10-19)                | Three "safe to load on demand" rules were verified to fail their retrieval paths and stay. It requires a destination to be owned and discoverable _before_ the decision — the condition this unit tests against the archive and the other durable-document classes.                                                                                      |
| `extract-load-test-commands`, `extract-ci-narrative`   | The 2026-10-19 audit's two extraction opportunities shipped; the Gotchas subsections are post-extraction text.                                                                                                                                                                                                                                           |
| `trim-restated-agents-md-rules` (2026-10-11)           | The owner deliberately retained the capture section's one-word promotion-gate list. A "this restates the spec" argument alone does not authorise deletion here either.                                                                                                                                                                                   |
| `optimize-agents-auditor-for-instruction-budget`       | The budget is measured, never capped. No threshold is configured and none is invented by this design.                                                                                                                                                                                                                                                    |
| `record-agents-md-authoring-standard` (2026-10-17)     | Entries follow a stated representation and shape, and a gotcha distinguishes observation, hypothesis, confirmed cause, and verified resolution. This is the standard the displacement of evidence must not weaken.                                                                                                                                       |
| `make-captured-rules-traceable` (2026-09-18)           | `captured:` markers are provenance, and a marker sits at the end of the rule block it records. Every draft keeps its marker in place.                                                                                                                                                                                                                    |
| `establish-structured-writing-standard` (2026-10-17)   | The mandatory is separated from the explanatory. A split entry is the strongest form of that separation: the action at root, the context in the record.                                                                                                                                                                                                  |

### What the newest audit already rejected

`docs/audits/2026-10-19.md` rejected a Gotchas _extraction_:

> No opportunity arises for `## Gotchas` (1,509 lines): its entries are evidence-anchored incident memory whose subjects
> are cross-cutting meta rules an agent must see before starting work, and no existing destination owns them —
> extracting them would split procedures across files and violate the "global, cross-cutting, retain at root" rule.

Two of its clauses bind this design, one does not:

- **Binds:** the rule must stay at root, and no _existing_ destination owns the evidence. Both are honoured here — no
  rule leaves root, and the destination this design proposes does not yet exist.
- **Does not bind:** "extracting them would split procedures across files". A gotcha is not a procedure, and the split
  proposed here keeps every trigger, action, condition, exception, and identifier at root; only the episode moves.

### Parked ideas reconciled

| Parked idea                                                                               | Relationship                                                                                        | Disposition                                                                                                                                     |
| ----------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------- |
| Carry the retrieval-path result in the audit ledger's "safe to load on demand" class      | Constrains how a ledger entry's destination may be trusted; this unit runs that test on the archive | **Constraint applied.** Each destination claim carries an evidence class; Probe 3 states the classes it swept. The idea's own fix stays parked. |
| Trend the audit counts across reports                                                     | Would measure whether reductions hold; overlaps the size accounting                                 | **Dependency, not adopted.** This design reports one unit's figures and does not build the trend report. Not bundled.                           |
| Carry findings forward across audit reports                                               | The durable-record class this unit's destination touches                                            | **Independent deferred work.** It changes the audit report's verdict, not the guidance surface.                                                 |
| Widen the reviewers' 120-character check to every changed file                            | Relevant to the drafts' own width claim and to the manual check                                     | **Deferred as independent work.** This unit runs the formatter against its drafts instead.                                                      |
| Enforce the command lifecycle preflight with a check rather than prose in seventeen files | Alternative philosophy: enforce a repeated rule mechanically                                        | **Unrelated, cited as precedent.** This unit adds no gate: prose placement is not mechanically checkable.                                       |
| Make a capture confirm the rule excludes the incident that produced it                    | Governs a captured rule's evidence quality                                                          | **Independent deferred work.** It changes the capture gate; this unit changes no gate.                                                          |
| Record the lesson-capture's rejected proposals                                            | Would persist rejected proposals                                                                    | **Unrelated** to guidance placement; no effect on this unit's scope.                                                                            |
| Sharpen route-candidate verification                                                      | Constrains how a pointer's coverage is judged                                                       | **Constraint applied:** each rewrite is checked against what it actually loses, not against "a related mechanism exists".                       |
| Name a durable home for a smoke-run's beyond-seed findings                                | Same class — a durable home for material currently only in a change record                          | **Addressed in kind, not in scope:** this design proposes such a home for rule evidence; smoke-run findings are a different subject.            |

**Entries resolved by this unit: none.** No parked idea is implemented or removed.

## Goals / Non-Goals

**Goals:**

- Give every rule in the two selected subsections an explicit disposition, and narrow any conclusion to the coverage
  actually achieved.
- Test whether a scope-forming example can be restated as an operational condition, action, exception, or decision-table
  row, keeping the identifiers needed to act.
- Design one narrowly scoped evidence destination and state what it costs in each of the four quantities.
- Reassess whether the preservation and removal requirements actually prohibit a non-lossy relocation; propose the
  smallest delta if they do.
- Recommend the smallest justified unit, or a no-change outcome the completed pilot supports.

**Non-Goals:**

- Implementing any change; editing `AGENTS.md`, a main spec, an agent definition, or an implementation file.

  **One exception, stated up front:** the change now proposes an implementation, so its delta specification is written
  here for the owner's review. No implementation file is touched.

- Expanding the pilot beyond the two selected subsections.
- Bundling the duplicated-word fix, the reviewer-contract improvement, the audit-trending tool, or any stage-specific
  workflow redesign.
- A generic evidence dump, a size cap, or a new automated gate.

## Analysis method

### The four quantities

Every option is reported in four quantities, kept apart, because they trade against each other:

| Quantity                             | Definition                                                                            | Why it is separate                                                       |
| ------------------------------------ | ------------------------------------------------------------------------------------- | ------------------------------------------------------------------------ |
| Root-file reduction                  | Bytes removed from `AGENTS.md`, the surface loaded into every session.                | This is the objective.                                                   |
| Additional routinely loaded material | Bytes added to a surface every session reads (a per-rule token, a skill description). | The token is not free; it is charged inside the root reduction, once.    |
| Total maintained content             | Bytes of guidance and evidence the repository maintains anywhere.                     | A relocation may leave this flat or grow it; that is a cost, not a veto. |
| Maintenance and retrieval cost       | Who keeps the material true, and what a reader must do to reach it.                   | A destination nobody audits is a slower kind of loss.                    |

A destination is **not** rejected because the moved content is still maintained somewhere. It is rejected when it fails
retrieval, ownership, or preservation of the root decision.

### Disposition categories

Each of the two subsections' rules takes exactly one disposition:

| Disposition                                      | Meaning                                                                                                      |
| ------------------------------------------------ | ------------------------------------------------------------------------------------------------------------ |
| **No separable material identified**             | The rule is already operational, or its example _is_ its substance; nothing can move without losing meaning. |
| **Operational rewrite worth drafting**           | The narrative yields a condition, action, exception, or decision-table row that can replace it at root.      |
| **Evidence relocation worth drafting**           | The rule's text is already operational; only episode detail moves.                                           |
| **Rejected — preservation or retrieval failure** | A specific obligation or referent would be lost, or no reader would reach the record before deciding.        |
| **Not yet evaluated**                            | Recorded as unassessed; the conclusion must not cover it.                                                    |

### The sentence classes

Within a rule, each sentence is one of: **condition/trigger**, **mandatory action**, **scope-forming evidence** (an
example that defines the rule's extent, an exception, or a sub-rule), or **illustration** (an actor, date, or count with
no attached condition). Only the last two are candidates to move; a condition and an action never move. The `captured:`
marker is provenance and never moves.

### Census of the Gotchas subsections

Each row is measured from the `###` heading through the line before the next heading. "Evidence" is the summed byte
length of sentences matching the census block's regex — a **fixed lexical estimate** that locates narrative, never a
licence to delete it.

| Subsection                                      |       Bytes |     Lines |   Rules | `captured:` | Evidence B |     Share |
| ----------------------------------------------- | ----------: | --------: | ------: | ----------: | ---------: | --------: |
| Documentation & durable claims                  |      32,926 |       309 |      17 |          32 |     17,801 |     54.1% |
| Agent tooling, OpenCode & permissions           |      23,344 |       225 |      17 |          24 |     10,580 |     45.3% |
| Git & repository workflow                       |      16,890 |       164 |      13 |          16 |      7,063 |     41.8% |
| Build, Gradle & dependency management           |      12,189 |       124 |      10 |          11 |      5,971 |     49.0% |
| CI & GitHub Actions                             |      11,837 |       116 |       8 |          12 |      5,984 |     50.6% |
| Evidence and search                             |      10,863 |       101 |       5 |          14 |      4,629 |     42.6% |
| Deployment, Helm & Kubernetes                   |       7,690 |        74 |       4 |           4 |      3,176 |     41.3% |
| Testing, measurement & load verification        |       7,126 |        72 |       6 |           7 |      3,329 |     46.7% |
| Formatting & IDE integration                    |       6,789 |        69 |       6 |           1 |      1,015 |     15.0% |
| Runtime code, libraries & Spring                |       5,406 |        61 |       8 |           5 |      2,304 |     42.6% |
| Environment & host toolchain                    |       3,921 |        40 |       4 |           1 |        991 |     25.3% |
| Writing checks and tests that can actually fail |         970 |        12 |       3 |           0 |        369 |     38.0% |
| **Total**                                       | **139,951** | **1,367** | **101** |     **127** | **63,212** | **45.2%** |

The twelve rows sum to 139,951 bytes and the `## Gotchas` heading line adds the remaining 12, matching 139,963.

````text
import re, os, glob
raw = open("AGENTS.md", "rb").read(); txt = raw.decode("utf-8"); lines = txt.split("\n")
if lines and lines[-1] == "": lines = lines[:-1]
offs = [0]
for ln in lines[:-1]: offs.append(offs[-1] + len(ln.encode()) + 1)
def bspan(i, j):                     # lines i..j-1, 1-based
    a = offs[i-1]; b = offs[j-1] if j-1 < len(offs) else len(raw); return b - a
in_fence = False; heads = []
for i, ln in enumerate(lines, 1):
    s = ln.strip()
    if s.startswith("```"): in_fence = not in_fence; continue
    if in_fence: continue
    m = re.match(r"^(#{2,3}) (.+)$", ln)
    if m: heads.append((i, len(m.group(1)), m.group(2)))
EVID = re.compile(r'(20\d\d-\d\d-\d\d|#\d{2,5}\b|captured:|reproduc|surfac|caught|turned out|was found|measured|A/B'
                  r'|first (attempt|draft|version|dispatch)|discovered|shipped|landed|previously|until the user)', re.I)
SENT = re.compile(r'(?<=[.!?])\s+')
g = [i for (i, l, t) in heads if l == 2 and t == "Gotchas"][0]
gend = ([i for (i, l, t) in heads if l == 2 and i > g] or [len(lines)+1])[0]
subs = [(i, t) for (i, l, t) in heads if l == 3 and g < i < gend]
for k, (a, t) in enumerate(subs):
    b = subs[k+1][0] if k+1 < len(subs) else gend
    seg = lines[a-1:b-1]
    body = re.sub(r'\s+', ' ', " ".join(x.strip() for x in seg))
    print("%-46s %6d %4d %2d %2d %6d" % (t, bspan(a, b), len(seg),
          sum(1 for x in seg if x.startswith("- ")),
          sum(x.count("captured:") for x in seg),
          sum(len(s.encode())+1 for s in SENT.split(body) if EVID.search(s))))
````

## Pilot selection

| Criterion                       | Test, and the measure it is judged on                                                                                                |
| ------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------ |
| C1 — the rule must stay at root | A narrowed task trigger cannot reach it, so the pilot tests the internal split rather than extraction.                               |
| C2 — material evidence mass     | The **absolute** evidence bytes from the census; the share is context, not the measure.                                              |
| C3 — the narrative is history   | The bulk of the narrative is a past-tense episode, not a live operational fact such as a tool's current contract or a pin's meaning. |
| C4 — the subject is settled     | A fast-churning subject makes the destination unstable.                                                                              |

| Subsection                                      | C1  | C2 (evidence B) | C3 — the narrative's class                                                                             | Verdict                      |
| ----------------------------------------------- | --- | --------------: | ------------------------------------------------------------------------------------------------------ | ---------------------------- |
| Documentation & durable claims                  | ✔   |          17,801 | Mixed, which is why it is Pilot A: episodes are history, but some examples _are_ the rule's scope.     | **Pilot A — the hard case**  |
| Git & repository workflow                       | ✔   |           7,063 | History: past-tense episodes with `#NNN`s, file names, and commits.                                    | **Pilot B — the clean case** |
| Agent tooling, OpenCode & permissions           | ✔   |          10,580 | Live tool contract (CLI defaults, permission semantics) — C3 rejects it despite C2.                    | Excluded on C3               |
| CI & GitHub Actions                             | ✔   |           5,984 | Live operational identifiers (action inputs, `gh` limits, check membership).                           | Excluded on C3               |
| Build, Gradle & dependency management           | ✔   |           5,971 | Live configuration (catalog pins, plan semantics, task wiring).                                        | Excluded on C3               |
| Evidence and search                             | ✔   |           4,629 | Would pass C3, but the preceding unit created it and it is the retrieval-integrity core.               | Excluded on scope            |
| Testing, measurement & load verification        | ✔   |           3,329 | Passes; its measurement evidence is already archived, so it tests preservation rather than separation. | Excluded on C2 and scope     |
| Deployment, Helm & Kubernetes                   | ✔   |           3,176 | Its destinations lack its warnings, so it was retained by `add-extraction-procedure`.                  | Excluded on C3 and C2        |
| Runtime code, libraries & Spring                | ✔   |           2,304 | Live library semantics.                                                                                | Excluded on C3               |
| Formatting & IDE integration                    | ✔   |           1,015 | Passes C3, but too little evidence mass.                                                               | Excluded on C2               |
| Environment & host toolchain                    | ✔   |             991 | Passes C3; second-smallest evidence mass.                                                              | Excluded on C2               |
| Writing checks and tests that can actually fail | ✔   |             369 | The prior unit's post-extraction stub.                                                                 | Excluded on C2               |

The two pilots cover 49,816 bytes, 473 lines, 30 rules, and 48 `captured:` markers; their estimated evidence mass is
24,864 bytes (49.9% of the pair, 17.8% of `## Gotchas`).

## Rule-by-rule dispositions

All 30 rules take a disposition. Anchors are the subsection plus the rule's quoted leading text. "Rule bytes" is
measured over the rule's own lines, without its terminating newline, so the deltas subtract exactly. "Est. saving" is
the draft's delta for the eight drafted rules (measured) and a reading-based estimate for the rest; the method is stated
under "Size accounting", and no estimate here is a measurement.

### `### Git & repository workflow` (13 rules)

| #   | Anchor (quoted lead)                                                          | Bytes | Disposition            | Est. saving | Note                                                                                                                        |
| --- | ----------------------------------------------------------------------------- | ----: | ---------------------- | ----------: | --------------------------------------------------------------------------------------------------------------------------- |
| 1   | "**`git stash pop` can leave conflict markers after a rebase.**"              | 2,219 | Rewrite worth drafting |        −950 | Two hazard modes (conflict vs silent revert) restate as conditions; the `#64`/`#336` stories move                           |
| 2   | "**`git reset --hard` on a branch with uncommitted work…**"                   | 1,637 | Rewrite worth drafting |        −590 | The actionlint and `git checkout -- README.md` episodes move; the recovery ladder stays                                     |
| 3   | "**`openspec archive` relocates the change dir with a filesystem move…**"     | 1,997 | Rewrite worth drafting |        −650 | The two `captured:` episodes move; the staging, ordering, `--yes`, and `git mv` traps stay                                  |
| 4   | "**Programmatic edits to a structured file…**"                                | 2,299 | Rewrite worth drafting |      −1,000 | The README-reorder and `retro-mark-captured-rules` stories move; the two boundary checks stay                               |
| 5   | "**The same range-boundary hazard bites source:**"                            |   629 | Rewrite worth drafting |        −250 | The three-comparator episode moves; "a lost brace fails the compile, an orphaned KDoc does not" stays                       |
| 6   | "**A formatter-owned file is re-wrapped…**"                                   |   691 | Rewrite worth drafting |        −110 | **Drafted (R3)**; **not converted** — the net after a token is −69 B. The four failures become four hazard classes at root  |
| 7   | "**An AST codemod is a further mode…**"                                       |   951 | No separable material  |           0 | Nearly all of it is identifiers (`Range.end`, `LogFileData` → `vala`, `++`/`--`); the one narrative sentence is its warrant |
| 8   | "**An edit to wrapped prose can mangle the clause…**"                         |   348 | No separable material  |           0 | Pure rule; no incident in it                                                                                                |
| 9   | "**A write to an already-occupied path replaces the file silently…**"         |   700 | Evidence relocation    |        −189 | **Drafted (D1, measured)**                                                                                                  |
| 10  | "**Work a change surfaces is parked durably — a PR body is not a record.**"   |   651 | No separable material  |           0 | WHEN/THEN plus the verification; no episode                                                                                 |
| 11  | "**A change merged without its archive is incomplete…**"                      |   719 | Evidence relocation    |        −100 | **Drafted (D2, measured)**                                                                                                  |
| 12  | "**`git add <dir>` / `git add -A` can sweep untracked generated artifacts…**" | 1,534 | Evidence relocation    |        −240 | **Drafted (D3, measured)**                                                                                                  |
| 13  | "**Never chain an edit to a commit — or trust an edit script's own report…**" | 2,458 | Rewrite worth drafting |      −1,050 | Three modes (stale anchor, multi-file apply, batch-write) restate as conditions; the incidents move                         |

### `### Documentation & durable claims` (17 rules)

| #   | Anchor (quoted lead)                                                            | Bytes | Disposition                  | Est. saving | Note                                                                                                                                                                |
| --- | ------------------------------------------------------------------------------- | ----: | ---------------------------- | ----------: | ------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 14  | "**Keep em-dashes paired when inserting a clause…**"                            | 1,112 | Evidence relocation          |        −242 | **Drafted (D4, measured)** — the blank-line paragraph                                                                                                               |
| 15  | "**Verify documented infrastructure/deployment numbers…**"                      | 1,440 | Evidence relocation          |        −419 | **Drafted (D5, measured)**                                                                                                                                          |
| 16  | "**A two-way split of an enumerated set can also sum to its total…**"           | 1,662 | Rewrite worth drafting       |        −650 | The markdown-target scan becomes the worked case for a stated partition rule; the incident moves                                                                    |
| 17  | "**`workflowLint` runs `actionlint` alone**"                                    | 1,293 | Rewrite worth drafting       |        −550 | The two wrong artifacts and the "one level down" episode move; both sub-rules stay                                                                                  |
| 18  | "**Distinguish a point-in-time count from a durable one:**"                     | 2,221 | Rewrite worth drafting       |        −950 | Five conditions restate as a decision list; the 157/22/tenfold incidents move                                                                                       |
| 19  | "**A named example or mechanism inside a convention is itself a claim…**"       |   658 | No separable material        |           0 | The `@DirtiesContext` example is the rule; nothing remains if it moves                                                                                              |
| 20  | "**A doc-consistency sweep is scoped by the convention…**"                      | 3,456 | Rewrite worth drafting       |      −2,042 | **Drafted (R1)**. The six sweep scopes become a decision list; five incidents move                                                                                  |
| 21  | "**An abandoned approach needs the same sweep:**"                               | 1,047 | Rejected — retrieval failure |           0 | Its example (`.opencode/agent/readme-auditor.md`'s "36 panels") is a _live decision about a live file_: relocating it separates a rule from the referent it governs |
| 22  | "**A configuration-default change names every test assertion…**"                | 2,379 | Rewrite worth drafting       |      −1,000 | The `reconcile-showcase-cache-default` plan and `docs/load-tests/2026-09-26.md` episodes move; the surfaces and the URL rule stay                                   |
| 23  | "**Doc claims must match their source and their strength…**"                    | 2,932 | Rewrite worth drafting       |      −1,150 | Six checks restate as a checklist; their examples move (the em-dash scoping example stays, as it names a rule in this file)                                         |
| 24  | "**A durable artifact may assert only what the repository can evidence…**"      | 1,923 | Rewrite worth drafting       |        −850 | The `/var/folders/**` draft and the `c62feda` episode move; the provenance test stays                                                                               |
| 25  | "**A claim about a surface outside the repository…**"                           | 1,313 | Rewrite worth drafting       |        −480 | The `#266` and `helmUpdates` episodes move; "run the query, re-run at review" stays                                                                                 |
| 26  | "**When a script generates many claims at once…**"                              |   858 | Evidence relocation          |        −130 | The `ddaa51e` detail moves; the per-item control and its form stay                                                                                                  |
| 27  | "**A comparison between two runs or files that differ…**"                       | 2,304 | Rewrite worth drafting       |      −1,000 | Three episodes move; "vary only the dimension", "read the authoritative policy", and the route-probe rule stay                                                      |
| 28  | "**A reproduction in an outward-facing artifact is itself part of the claim…**" | 1,218 | Rewrite worth drafting       |        −380 | The `#1892` transcript detail moves; the reproducibility requirements stay                                                                                          |
| 29  | "**An upstream issue reference is a status claim, not a citation…**"            | 3,924 | Rewrite worth drafting       |      −2,006 | **Drafted (R2)**. Five clauses restate without their episodes; the scanned-file and venue rules stay                                                                |
| 30  | "**A \"the only X\" claim in `AGENTS.md` goes stale…**"                         | 3,116 | Rewrite worth drafting       |      −1,400 | The three sweep classes (name, absence, enumeration) restate; the PR incidents move                                                                                 |

**Coverage:** 30 of 30 rules carry a disposition — 19 rewrite, 6 relocation, 4 no separable material, 1 rejected, 0 not
yet evaluated. **Eight** are drafted and measured, and **seven** of those are converted in the recommended unit (R3 is
drafted and left at root); **seventeen** carry a reading estimate, and those are named individually in "Follow-up"
rather than treated as a group.

## Drafted rewrites

Eight rules are drafted and **seven are converted**: D1–D5 are evidence relocations, and R1–R2 are scope-preserving
rewrites. R3 is drafted and left at root, because its 110-byte rewrite does not return the cost of a token and an entry
(net −69 B); its draft stays here as the method's third demonstration. Each converted `After` keeps every condition,
action, exception, and the identifiers needed to act — and carries the `evidence:` token that marks the rule as
converted. An incident's own identifiers travel with the episode to the record.

### D1 — "A write to an already-occupied path replaces the file silently…" (`-189 B` of text, measured)

Before:

```text
- **A write to an already-occupied path replaces the file silently: check the path before creating a "new" file.**
  `ShowcaseEventStreamControllerTests.java` was assumed new, but it dated from #43 and held two tests (event wrapping;
  REMOVED-type preservation), and the whole-file write destroyed both. Before writing a file you believe is new, check
  the path — `test -e <path>` (or a read) proves it exists, `git ls-files <path>` whether it is tracked — and when it
  exists, merge the addition into it rather than re-creating the class from its new-test shape. The loss happens at
  write time, so the `git add <dir>` staging gotcha does not cover it. captured: keep-sse-stream-alive (#301)
```

After:

```text
- **A write to an already-occupied path replaces the file silently: check the path before creating a "new" file.**
  Before writing a file you believe is new, check the path — `test -e <path>` (or a read) proves it exists,
  `git ls-files <path>` whether it is tracked — and when it exists, merge the addition into it rather than re-creating
  the file from its new-test shape. The loss happens at write time, so the `git add <dir>` staging gotcha does not cover
  it. captured: keep-sse-stream-alive (#301)

  evidence: git-and-repository-workflow
```

### D2 — "A change merged without its archive is incomplete…" (`-100 B` of text, measured)

Before:

```text
- **A change merged without its archive is incomplete — do not merge the implementation PR and defer the archive.** The
  "one PR per change" rule puts the archive commit in the _same_ PR before merge. WHEN the merge completes, verify the
  change dir is archived — the live dir is gone (`test ! -d openspec/changes/<change>`) and the dated archive exists
  (`git ls-files 'openspec/changes/archive/**' | grep <change>`); the `remove-redis-client-label` change was merged and
  sat unarchived until the user pointed it out. If it is not archived, fix it on its own branch and PR — never commit it
  to local `main` and `git push origin main`, which the branch ruleset rejects and forces a branch-then-reset dance.
```

After:

```text
- **A change merged without its archive is incomplete — do not merge the implementation PR and defer the archive.** The
  "one PR per change" rule puts the archive commit in the _same_ PR before merge. WHEN the merge completes, verify the
  change dir is archived — the live dir is gone (`test ! -d openspec/changes/<change>`) and the dated archive exists
  (`git ls-files 'openspec/changes/archive/**' | grep <change>`). If it is not archived, fix it on its own branch and PR
  — never commit it to local `main` and `git push origin main`, which the branch ruleset rejects and forces a
  branch-then-reset dance.

  evidence: git-and-repository-workflow
```

### D3 — "`git add <dir>` / `git add -A` can sweep untracked generated artifacts…" (`-240 B` of text, measured)

Before:

```text
- **`git add <dir>` / `git add -A` can sweep untracked generated artifacts into the commit — inspect the staged set
  first.** A tool that emits files beside sources (a Python script's `scripts/__pycache__/*.pyc`, a test/build run's
  output) leaves them untracked; a directory-wide `git add` stages them silently, so the commit carries files the change
  never intended. Stage explicit paths, add a `.gitignore` entry for the artifact, and check
  `git diff --cached --name-only` (or `git status`) before committing — the `rework-idea-setup` commit swept in a
  `scripts/__pycache__/*.pyc` that had to be removed and ignored. Staging is a snapshot, not a view of the working tree:
  an edit made after the last `git add` sits unstaged, so `git diff --cached` can look complete while the commit ships
  the older copy — check `git status` for a path listed under both staged and unstaged changes (or `git diff` for
  unstaged edits) before committing, since a review caught the staged set missing corrections made after the last
  `git add`, which would have shipped half the fix. A pre-commit guard (`scripts/git-hooks/pre-commit`, activated by
  `scripts/install-git-hooks.sh`) enforces this inspection mechanically: it refuses a commit whose staged set fails the
  formatter check, force-stages a generated artifact, stages a path and then edits it, carries a merge conflict marker,
  misplaces a `captured:` marker, or stages an `openspec/changes/archive/**` change dir while leaving its
  `openspec/specs/**` sync unstaged.
```

After:

```text
- **`git add <dir>` / `git add -A` can sweep untracked generated artifacts into the commit — inspect the staged set
  first.** A tool that emits files beside sources (a Python script's `scripts/__pycache__/*.pyc`, a test/build run's
  output) leaves them untracked; a directory-wide `git add` stages them silently, so the commit carries files the change
  never intended. Stage explicit paths, add a `.gitignore` entry for the artifact, and check
  `git diff --cached --name-only` (or `git status`) before committing. Staging is a snapshot, not a view of the working
  tree: an edit made after the last `git add` sits unstaged, so `git diff --cached` can look complete while the commit
  ships the older copy — check `git status` for a path listed under both staged and unstaged changes (or `git diff` for
  unstaged edits) before committing. A pre-commit guard (`scripts/git-hooks/pre-commit`, activated by
  `scripts/install-git-hooks.sh`) enforces this inspection mechanically: it refuses a commit whose staged set fails the
  formatter check, force-stages a generated artifact, stages a path and then edits it, carries a merge conflict marker,
  misplaces a `captured:` marker, or stages an `openspec/changes/archive/**` change dir while leaving its
  `openspec/specs/**` sync unstaged.

  evidence: git-and-repository-workflow
```

### D4 — the paragraph-boundary paragraph of "Keep em-dashes paired…" (`-242 B` of text, measured)

This draft also removes the duplicated `paragraphs` the pilot found across a source line break.

Before:

```text
  The same class covers a paragraph boundary: a blank line lost in an edit fuses two paragraphs (an added one splits a
  sentence), and Prettier reflows the result without complaint — the `audit.yml` and `buildpack-updates.yml` paragraphs
  paragraphs were fused in the README's CI section and rode `main` through `spotlessCheck` for several changes
  (`5fc1a09` introduced the fusion) until a drive-by fix separated them — so proofread blank lines as content too.
  captured: notify-owner-from-the-audit-report (#327)
```

After:

```text
  The same class covers a paragraph boundary: a blank line lost in an edit fuses two paragraphs (an added one splits a
  sentence), and Prettier reflows the result without complaint — so proofread blank lines as content too. captured:
  notify-owner-from-the-audit-report (#327)

  evidence: documentation-and-durable-claims
```

### D5 — "Verify documented infrastructure/deployment numbers…" (`-419 B` of text, measured)

Before:

```text
- **Verify documented infrastructure/deployment numbers against the config files, not memory.** The README rewrite
  claimed "the API gateway's two replicas" (only `commandService.replicaCount` is 2 in
  `helm/values/axon-showcase/values-local.yaml`; the gateway defaults to 1), "36 panels" (the raw top-level count, row
  separators included, not the real panel count), and "four services and a gateway" (double-counting a table that lists
  four components). The quick review against repo files caught all three. Before writing a replica count, panel/section
  count, or diagram count into a doc, read the source (`helm/values/*/values-*.yaml`, the dashboard JSON, the component
  table — or, for an upstream chart's shipped default or a container the values do not override, the pinned chart via
  `helm show values <chart> --version <pinned>`) and cite the real number. The same holds for _which check_ a documented
  figure belongs to: two mechanisms can bound the same metric at the same order of magnitude, so a boundary's owner
  cannot be inferred from the figure's size — the 2026-09-28 refresh left the below-knee profiles' floor-dominated
  thresholds unchanged (`5 x 27 ms = 135 ms` is under their 200 ms p99 floor), while the ~1.5x boundary an annotation
  named was the drift check's own tolerance. Read the mechanism's definition and name the one the boundary belongs to.
  captured: re-measure-load-test-baseline (#429)
```

After:

```text
- **Verify documented infrastructure/deployment numbers against the config files, not memory.** Before writing a replica
  count, panel/section count, or diagram count into a doc, read the source (`helm/values/*/values-*.yaml`, the dashboard
  JSON, the component table — or, for an upstream chart's shipped default or a container the values do not override, the
  pinned chart via `helm show values <chart> --version <pinned>`) and cite the real number. The same holds for _which
  check_ a documented figure belongs to: two mechanisms can bound the same metric at the same order of magnitude, so a
  boundary's owner cannot be inferred from the figure's size — the 2026-09-28 refresh left the below-knee profiles'
  floor-dominated thresholds unchanged (`5 x 27 ms = 135 ms` is under their 200 ms p99 floor), while the ~1.5x boundary
  an annotation named was the drift check's own tolerance. Read the mechanism's definition and name the one the boundary
  belongs to. captured: re-measure-load-test-baseline (#429)

  evidence: documentation-and-durable-claims
```

### R1 — "A doc-consistency sweep is scoped by the convention…" (`-2,042 B` of text, measured)

Before: one opening rule, then six incidents, each introducing a sweep scope — the `fix-javadoc-consistency` change,
`apply-specs-audit-findings`, the `reconcile-showcase-cache-default` residue, the authored-rule paraphrase sweep, the
widened check's describers, and a `Makefile`-style example — then the closing `captured:` markers.

After:

```text
- **A doc-consistency sweep is scoped by the convention or the claim, not by the review's findings list — and a claim
  about the code is verified against the code.** Derive the sites by grepping the artifact or corpus, never from the
  plan's list, and sweep each of these pairs together:

  - a convention (a coordinate or identifier spelling, `MUST` vs `SHALL`, an enumeration) → every instance in the
    artifact or corpus;
  - a corrected fact (a direction, a value, a magnitude) → every repetition, plus the statements that depend on it
    without repeating it (a caveat, a rationale, a concluding sentence);
  - a rule the change authored or replaced → its paraphrases by the rule's **subject**, in every document that describes
    the workflow: `AGENTS.md`, file headers (e.g. `docs/ideas.md`'s), the `.opencode/` definitions, the specs, and the
    change's own artifacts;
  - an audit finding → the pattern the finding is an instance of, not the finding alone;
  - a widened or narrowed check → its describers, including a worked example in a definition;
  - a javadoc or field-doc pass → every declaration in the touched classes, not only the ones a review named.

  Two directions read plausibly for a converter or mapper, so read the field's Javadoc and one call site before writing
  its `@param`/`@return`. captured: bump-tooling-pins-2026-10-16 captured: make-the-capture-selective

  evidence: documentation-and-durable-claims
```

The six scopes survive as six rows. What moves is _which change_ taught each one.

### R2 — "An upstream issue reference is a status claim…" (`-2,006 B` of text, measured)

Before: the rule, then the parked-report discovery of `#755` and `#3334`, the resolve/read/diff steps, the
note-is-a-claim clause with a second `#3334` episode, the "document the claim and sit on the clause" clause, the
scanned-file list, the `#1340` misattribution story, and the locked-venue episode.

After:

```text
- **An upstream issue reference is a status claim, not a citation — resolve it, and treat a closure as a trigger to
  check rather than an answer.** Resolve an `owner/repo#NNN` through the tracker before writing or editing such a
  reference. When it has closed, read the closing comment for its disposition (a duplicate's live thread is elsewhere,
  so the closed issue is not its authority) and read the fixing PR's own diff and the release it shipped in against our
  pin before treating the note as retirable. The note itself is a claim: re-verify its recorded reason, not only the
  open/closed state. An upstream reference must state the claim it is cited for and sit on the clause it supports; read
  the surface that carries the claim — the body for what the issue reports, the closing comment for its disposition —
  never the title or the release alone. A close-out reference must live in a file `upstreamReferences` scans
  (`AGENTS.md`, `README.md`, `docs/ideas.md`, `docs/adr/**`); cited only in a skill, command, subagent definition, or
  code comment it is invisible to the report that would fire its `retires when`. When a passage already explains a
  signature, reconcile with it rather than writing a second explanation beside it. A planned task that posts to an
  external tracker depends on that venue's mutable state: check the venue is writable before writing the posting task —
  a repository can disable issues and discussions outright, and a thread can be locked — and when the issue is closed or
  locked, cite the fixing PR the close-out keys on. The periodic corpus sweep is the `upstream-references.yml` report
  (`showcase/quality/merge-governance` specifies it with the other observational checks). captured:
  suppress-braces-npm-advisory captured: refresh-upstream-references-2026-10-05 captured: record-gatling-report-block
  captured: refresh-upstream-references-2026-10-19

  evidence: documentation-and-durable-claims
```

Every operational clause survives, including the reconciliation action and the sweep pointer the `#1340` and `#3334`
episodes taught; the identifiers (the scanned-file list, `owner/repo#NNN`, `retires when`, `upstream-references.yml`)
stay.

### R3 — "A formatter-owned file is re-wrapped…" (`-110 B` of text, measured)

Before: the rule, then one sentence listing four failures across one arc.

After:

```text
- **A formatter-owned file is re-wrapped, so a whitespace-tolerant substitution — a normalization or `\s*`-style pattern
  — silently matches the wrong span or rebuilds it unformatted.** Locate the target by line index in the content you
  just read, or match one full line verbatim including its indentation, and re-read the result. The hazard classes are
  an indentation-sensitive block (a YAML step), an anchor whose occurrence count you did not assert, a line-length
  limit, and prose whose wrapping the formatter owns. captured: notify-owner-from-the-audit-report (#327) evidence:
  git-and-repository-workflow
```

The four failures become four hazard classes — the scope is _stated_ instead of _exemplified_. The saving is small
because the list was already compact, and that smallness is a result: not every rewrite pays.

### What the rewrites show

| Test                                                 | D1  | D2  | D3  | D4  | D5  | R1  | R2  | R3  |
| ---------------------------------------------------- | --- | --- | --- | --- | --- | --- | --- | --- |
| The motivating incident still yields the same action | ✔   | ✔   | ✔   | ✔   | ✔   | ✔   | ✔   | ✔   |
| A neighbouring case the rule should also prevent     | ✔   | ✔   | ✔   | ✔   | ✔   | ✔   | ✔   | ✔   |
| A legitimate case the rule must still allow          | ✔   | ✔   | ✔   | ✔   | ✔   | ✔   | ✔   | ✔   |
| A task that names no destination                     | ✔   | ✔   | ✔   | ✔   | ✔   | ✔   | ✔   | ✔   |
| A review verifying the rationale, with the record    | ✔   | ✔   | ✔   | ✔   | ✔   | ✔   | ✔   | ✔   |

**Where the scope could not be stated.** Two rules stayed at root in full. The `@DirtiesContext` rule (row 19) is _only_
its example: "a named example is itself a claim" has no operational form without the false example that shows what a
false one looks like. The abandoned-approach rule (row 21) keeps its `readme-auditor.md` referent because the rule tells
a reader what to do to a **live file**; moving the referent to a record would leave the reader holding a rule about text
they cannot see. Both are recorded as failures of relocation, not of the rule.

## The evidence destination

### The pairing token, and why a pointer is not enough

A subsection pointer cannot say _which_ rules were converted: a rule with no entry and a rule that never had one look
alike. The smallest reliable representation is a **per-rule token**, appended to each converted rule as its own
paragraph:

```text
  evidence: git-and-repository-workflow
```

It is 38–46 bytes per rule, sits inside the rule block (where the marker guard already tolerates a trailing line), and
is greppable as `^  evidence: `. Seven converted rules cost **307 bytes** in total, and that figure is inside the root
reduction rather than added to it — the section "Size accounting" charges it once.

**Why not a shorter form.** A bare `evidence:` marker would need the record's name from a naming convention, and a list
in the subsection would duplicate the record's headings and drift from them. A token that names its record is
self-describing and local to the rule it converts.

### How an auditor identifies the converted set

Pairing runs in one direction per artifact, and the record's `Rule:` paragraph is the key — the section title is only an
address, so a wrapped title can never masquerade as a key.

| Step | Test                                                                                                                                                                         | Detects                                               |
| ---- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------- |
| 1    | Every `^  evidence: <stem>` token names a file `docs/guidance-evidence/<stem>.md`                                                                                            | a typo or a renamed record                            |
| 2    | For each token's rule, that record holds a section whose `Rule:` paragraph — its lines joined and whitespace collapsed — equals the rule's leading text, likewise normalized | a converted rule whose entry was deleted              |
| 3    | Every section's `Rule:` paragraph equals exactly one live rule's leading text                                                                                                | an orphaned entry, or a rule renamed after conversion |
| 4    | A section matching a live rule that carries no token                                                                                                                         | an unpaired entry left behind by a deleted token      |

### The five cases, worked

| Case                                     | Root                                          | Record                                       | Audit result                                                                                                                |
| ---------------------------------------- | --------------------------------------------- | -------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------- |
| An untouched rule                        | no token                                      | —                                            | nothing to check; it needs no entry                                                                                         |
| A converted rule with a valid entry      | token `evidence: git-and-repository-workflow` | a section whose `Rule:` is the rule's lead   | no finding                                                                                                                  |
| A converted rule whose entry was deleted | token present                                 | no matching section                          | **finding** — rule claims evidence, record has none                                                                         |
| An orphaned entry                        | no token names it, or its rule is gone        | a section whose `Rule:` matches no live rule | **finding** — unpaired entry                                                                                                |
| A renamed rule                           | token present; the lead changed               | the section still carries the old lead       | **two findings** — no section matches the rule, and the section matches no rule; the fix is to update the `Rule:` paragraph |

### Record format

| Element         | Rule                                                                                                                                                                 |
| --------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| File            | `docs/guidance-evidence/<subsection-stem>.md`, one per Gotchas subsection, named by slugifying the subsection heading                                                |
| Section heading | `## <entry-slug>`, a **single line** of lowercase words joined by hyphens — a valid ATX heading that is an address, never a key, so a long rule title cannot wrap it |
| Pairing key     | a `Rule:` paragraph repeating the root rule's leading text verbatim; it may wrap, and the check normalizes whitespace                                                |
| Body            | the episode, its **historical identifiers**, the verification, and the provenance markers                                                                            |
| Context         | the minimum needed to read the episode — what the rule assumes, not what the rule requires; the file carries no guidance of its own                                  |
| Ownership       | the agent-tooling audit, through the delta below                                                                                                                     |

**The duplication boundary, and the key's exemption.** Three things are distinguished:

| Element                                                                                                                                               | Status                          | Why                                                                                                     |
| ----------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------- | ------------------------------------------------------------------------------------------------------- |
| The `Rule:` paragraph repeating the rule's leading text                                                                                               | **required identification key** | without it a section cannot be paired to its rule; it is a pointer to the rule, not a second copy of it |
| The episode, its historical identifiers, its verification, and the factual description of the conditions, actions, commands, and identifiers involved | **permitted**                   | the episode is unreadable without them, and describing what happened is not prescribing what to do      |
| A second operational instruction that a reader could follow in place of the root rule                                                                 | **prohibited**                  | a reader could follow the record instead of the root entry, and the two would drift                     |

The exemption is narrow: the key repeats the leading text and nothing else — no added condition, no clarifying
exception, no extra identifier. The body's permission is factual, not normative: naming the command that failed is
description; telling a reader to run it is guidance, and guidance stays at root. The three parts below apply that
boundary; "Duplication boundary, applied" checks it against each draft.

### Complete drafts

Both files are drafted in full, formatted with the build's Prettier, and measured at their formatted size. They are the
implementation's expected output, not a sketch.

**`docs/guidance-evidence/git-and-repository-workflow.md` — 1,822 bytes, 3 entries** (the three converted Git rules;
`formatter-owned-file` is absent because R3 stays at root, see the recommendation):

```markdown
# Guidance evidence: `### Git & repository workflow`

Incident records for the converted rules of `AGENTS.md`'s `### Git & repository workflow`, one section per rule. A
section's `Rule:` paragraph repeats the root rule's leading text verbatim and is the pairing key; the section title is
only an address. The agent-tooling audit owns this file: it reports a section whose `Rule:` paragraph matches no live
rule, and a rule whose `evidence:` token names this record when no section matches that rule. The file holds no guidance
of its own.

## occupied-path

Rule: **A write to an already-occupied path replaces the file silently: check the path before creating a "new" file.**

`ShowcaseEventStreamControllerTests.java` was assumed new when `keep-sse-stream-alive` (#301) added a unit test for the
SSE keep-alive. It dated from #43 and held two tests — event wrapping, and REMOVED-type preservation — and the
whole-file write destroyed both. Nobody noticed until the suite failed for a missing method.

`captured: keep-sse-stream-alive (#301)`

## archive-incomplete

Rule: **A change merged without its archive is incomplete — do not merge the implementation PR and defer the archive.**

The `remove-redis-client-label` change was merged on its own branch and sat unarchived until the owner pointed it out;
the archive commit had been deferred past the merge.

## staged-set-sweep

Rule: **`git add <dir>` / `git add -A` can sweep untracked generated artifacts into the commit — inspect the staged set
first.**

The `rework-idea-setup` commit swept in a `scripts/__pycache__/*.pyc` that had to be removed and ignored, which is why
the repository now ignores `__pycache__/`. Separately, a review caught the staged set missing corrections made after the
last `git add`; that commit would have shipped half the fix.
```

**`docs/guidance-evidence/documentation-and-durable-claims.md` — 5,747 bytes, 4 entries:**

```markdown
# Guidance evidence: `### Documentation & durable claims`

Incident records for the converted rules of `AGENTS.md`'s `### Documentation & durable claims`, one section per rule. A
section's `Rule:` paragraph repeats the root rule's leading text verbatim and is the pairing key; the section title is
only an address. The agent-tooling audit owns this file: it reports a section whose `Rule:` paragraph matches no live
rule, and a rule whose `evidence:` token names this record when no section matches that rule. The file holds no guidance
of its own.

## paragraph-boundary

Rule: **Keep em-dashes paired when inserting a clause into a sentence that already uses them.**

The `audit.yml` and `buildpack-updates.yml` paragraphs in the README's CI section were fused — the word `paragraphs`
ended one source line and began the next — and the fusion rode `main` through `spotlessCheck` for several changes after
`5fc1a09` (PR #320) introduced it, until a drive-by fix separated them. No gate sees a fused paragraph: it is a prose
defect, not a parse error.

`captured: notify-owner-from-the-audit-report (#327)`

## documented-numbers

Rule: **Verify documented infrastructure/deployment numbers against the config files, not memory.**

A README rewrite claimed "the API gateway's two replicas" — only `commandService.replicaCount` is 2 in
`helm/values/axon-showcase/values-local.yaml`, and the gateway defaults to 1 — "36 panels", the raw top-level count with
row separators included rather than the real panel count, and "four services and a gateway", which double-counted a
table listing four components. A quick review against the repository's own files caught all three.

`captured: re-measure-load-test-baseline (#429)`

## consistency-sweep

Rule: **A doc-consistency sweep is scoped by the convention or the claim, not by the review's findings list — and a
claim about the code is verified against the code.**

Each of the rule's six scopes comes from an episode. `fix-javadoc-consistency` wrote a `@param elasticsearchConverter`
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

`captured: bump-tooling-pins-2026-10-16`, `captured: make-the-capture-selective`

## upstream-references

Rule: **An upstream issue reference is a status claim, not a citation — resolve it, and treat a closure as a trigger to
check rather than an answer.**

When the upstream-reference report was parked, a review found two of four cited references already closed while both
constraint notes still read as open: `ben-manes/gradle-versions-plugin#755` (closed 2026-08-06 by PR #1060) and
`spring-projects/spring-data-elasticsearch#3334` (closed 2026-08-30 by PR #3337). `#3334`'s entry then named a release
that later published, and the meta-rule's own "the periodic corpus sweep is parked" pointer outlived the sweep's build.
`#755` closed through a change to platform-sourced constraints (`satisfiesDeclaredBound`, "nothing changes by default"),
so the spurious row from `checkBuildEnvironmentConstraints` remains even though the fix shipped in 0.60.0.

The first `hold-back-nginx-buildpack` draft blamed an arm64 build-extraction failure on `paketo-buildpacks/nginx#1340`,
whose body documents only the runtime symptom (an AArch64 `nginx` in an amd64 image) and states that the
`io.buildpacks.buildpackage.metadata` label is present on both slices; the draft also contradicted the host-state
gotcha's account of that signature. A planned task to "comment on `micromatch/braces#70`" was unachievable because #70
is locked (comments disabled); the actionable reference was the fix PR `micromatch/braces#72`, which later closed
unmerged.

**Closure tracking for the references moved here.** `#755` stays cited in ADR-0007, `README.md`, and `## Build & Test`;
`#3334` in `docs/ideas.md` and the `projection-model` spec; `#1340` in `## Docker Images` and the deployment gotcha,
where its close-out clause lives; `micromatch/braces#72` in ADR-0014. `micromatch/braces#70` is cited only here: it is
locked, its disposition is the unmerged fix PR, and the acceptance it produced is dated in ADR-0014 and bounded by the
npm-advisory suppression's own expiry, so it carries no live retirement condition.

`captured: suppress-braces-npm-advisory`, `captured: refresh-upstream-references-2026-10-05`,
`captured: record-gatling-report-block`, `captured: refresh-upstream-references-2026-10-19`
```

### Size of the conversion, and what the drafts establish

This is the **seven-rule conversion subtotal**. The whole unit, which also carries the two accepted capture corrections,
is accounted in "Size accounting".

| Quantity                                       | Value                            |
| ---------------------------------------------- | -------------------------------- |
| New evidence files, as drafted and formatted   | **7,569 bytes** (1,822 + 5,747)  |
| Root text removed by the seven converted rules | 5,238 bytes                      |
| Pairing tokens added to those rules            | 307 bytes                        |
| **Conversion subtotal (routinely loaded)**     | **−4,931 bytes (1.45%)**         |
| **Total maintained, conversion only**          | **+2,638 bytes** (7,569 − 4,931) |

The records are **larger** than the root text they absorb, by 2,331 bytes (7,569 against 5,238): each section adds its
address, its `Rule:` key, and the context that makes the episode readable, and the two files carry a header. Counted
with the tokens, the conversion alone raises total maintained content by 2,638. The earlier draft's claim that "the
records hold exactly what root drops" was therefore wrong, and the drafts replace it: the ratio is measured, and the
growth is a stated cost.

### Upstream references the relocation moves

R2 moves five `owner/repo#NNN` references out of `AGENTS.md`'s scanned set. Each was inventoried against the closure
report (`upstreamReferences` reads `AGENTS.md`, `README.md`, `docs/ideas.md`, and `docs/adr/**`) and against its own
retirement condition.

| Reference                                        | State                        | Active condition?                                                                                             | Still scanned?                                        | Treatment                                                                                                                              |
| ------------------------------------------------ | ---------------------------- | ------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------- |
| `ben-manes/gradle-versions-plugin#755`           | closed 2026-08-06 (PR #1060) | yes — ADR-0021's filter and ADR-0007's recorded confirmation                                                  | **yes**: ADR-0007, `README.md`, `## Build & Test`     | retained where it already is; the record link records the episode only                                                                 |
| `spring-projects/spring-data-elasticsearch#3334` | closed 2026-08-30 (PR #3337) | yes — the `NANOS_DATE_PATTERN` workaround's `until the fix reaches us` clause                                 | **yes**: `docs/ideas.md`, the `projection-model` spec | retained where it already is                                                                                                           |
| `paketo-buildpacks/nginx#1340`                   | open                         | yes — `## Docker Images`' close-out ("re-take a mismatched pairing only once that upstream resolution lands") | **yes**: `## Docker Images`, the deployment gotcha    | retained where it already is; the active clause is untouched by R2                                                                     |
| `micromatch/braces#72`                           | closed unmerged              | no — ADR-0014 records the acceptance                                                                          | **yes**: ADR-0014                                     | retained where it already is                                                                                                           |
| `micromatch/braces#70`                           | locked (comments disabled)   | no — its disposition is the unmerged fix PR, and the suppression is a dated acceptance with its own expiry    | **no**                                                | **leaves the scan, deliberately**: closure tracking is unnecessary for a locked venue whose outcome is recorded; the record states why |

**No scanner change is needed, and none is made.** Four of the five references stay in files the report already reads,
so their tracking is unchanged; the fifth is the only one that would leave, and it has no live condition to track. A
verification task runs `upstreamReferences` before and after the implementation and compares the reference set.

## Destination verification

Before designing a destination, this unit asked whether one already exists. Three probes, run from the repository root.

**Probe 1 — marker coverage.** 274 `captured:` occurrences, 183 distinct: **142** match an archived change directory and
**41** match none.

```text
python3 - <<'PY'
import re, os, glob
names = re.findall(r'captured:\s*([A-Za-z0-9_.\-]+)', open("AGENTS.md").read())
arch = {os.path.basename(p) for p in glob.glob("openspec/changes/archive/*")}
uniq = list(dict.fromkeys(names))
print(len(names), len(uniq), sum(1 for n in uniq if any(a.endswith("-" + n) or a == n for a in arch)))
PY
```

**Probe 2 — per-rule coverage.** 24 of the 30 pilot rules carry a marker; **10** have at least one marker with no
archive directory, and **19** keep fewer than half their distinctive backticked tokens in the archive.

```text
python3 - <<'PY'
import re, os, glob
lines = open("AGENTS.md", encoding="utf-8").read().split("\n")
heads = [(i, len(m.group(1)), m.group(2)) for i, ln in enumerate(lines, 1)
         for m in [re.match(r"^(#{2,3}) (.+)$", ln)] if m]
g = [i for (i, l, t) in heads if l == 2 and t == "Gotchas"][0]
gend = ([i for (i, l, t) in heads if l == 2 and i > g] or [len(lines)+1])[0]
subs = [(i, t) for (i, l, t) in heads if l == 3 and g < i < gend]
arch = {os.path.basename(p): p for p in glob.glob("openspec/changes/archive/*")}
def blob(name):
    out = ""
    for a, p in arch.items():
        if a.endswith("-" + name) or a == name:
            for f in glob.glob(p + "/**/*", recursive=True):
                if os.path.isfile(f): out += open(f, encoding="utf-8", errors="ignore").read()
    return out
want = {"Git & repository workflow", "Documentation & durable claims"}
nr = nm = nn = low = 0
for k, (a, t) in enumerate(subs):
    if t not in want: continue
    b = subs[k+1][0] if k+1 < len(subs) else gend
    starts = [i for i in range(a, b) if lines[i-1].startswith("- ")]
    for j, st in enumerate(starts):
        en = starts[j+1] if j+1 < len(starts) else b
        body = "\n".join(lines[st-1:en-1])
        marks = re.findall(r'captured:\s*([A-Za-z0-9_.\-]+)', body)
        nr += 1
        if not marks: continue
        nm += 1
        if any(not blob(m) for m in marks): nn += 1
        toks = set(x for x in re.findall(r'`([^`]{10,})`', body))
        if toks:
            union = "".join(blob(m) for m in marks)
            if sum(1 for x in toks if x in union) / len(toks) < 0.5: low += 1
print("rules %d  marked %d  no-dir %d  low-coverage %d" % (nr, nm, nn, low))
PY
```

**Probe 3 — the other durable-document classes.** Swept across `docs/`, the specs, `README.md`, `SECURITY.md`,
`.github/`, and the project `.opencode/` definitions, excluding the change archive:

```text
python3 - <<'PY'
import subprocess
tokens = ["ShowcaseEventStreamControllerTests", "remove-redis-client-label", "__pycache__",
          "re-measure-load-test-baseline", "36 panels", "two replicas", "5fc1a09", "paragraphs were fused"]
paths = ["docs", "openspec/specs", "README.md", "SECURITY.md", ".github", ".opencode/agent", ".opencode/commands",
         ".opencode/skills"]
for t in tokens:
    out = subprocess.run(["grep", "-rl", "--", t, *paths], capture_output=True, text=True).stdout.split()
    print("%-36s %d  %s" % (t, len(out), out[:5]))
PY
```

| Token                                | Hits outside the archive                                                          |
| ------------------------------------ | --------------------------------------------------------------------------------- |
| `ShowcaseEventStreamControllerTests` | 0                                                                                 |
| `remove-redis-client-label`          | 0                                                                                 |
| `__pycache__`                        | 0                                                                                 |
| `re-measure-load-test-baseline`      | 4 — three audit reports and one retrospective                                     |
| `36 panels`                          | 1 — `.opencode/agent/readme-auditor.md`, as that auditor's own illustration       |
| `two replicas`                       | 2 — `README.md` (a current fleet fact) and `readme-auditor.md` (the illustration) |
| `5fc1a09`                            | 1 — `docs/audits/2026-09-21.md`, citing the commit                                |
| `paragraphs were fused`              | 0                                                                                 |

**What this establishes — and what it does not.** It establishes that **the existing document classes are not an
evidence destination**: the archive is indexed by change and records what a change did rather than what it learned, and
the other classes carry at most a token coincidence. It does **not** establish that evidence separation is impossible —
it establishes that a destination must be created and owned. That is what "The evidence destination" proposes, and it is
why the earlier draft's rejection of Alternative C rested on the wrong criterion.

## Preservation policy examined

Do the requirements prohibit a relocation that keeps every obligation and every piece of evidence across the artifact
set? Three clauses matter.

| Clause                                                                                                                                                                             | Does it prohibit relocation?                                                                                                                                                       |
| ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Authoring standard: "Established guidance SHALL NOT be removed … it is revised or removed only once its obsolescence is established"                                               | **No.** The guidance is not removed: the trigger, action, conditions, exceptions, and acting identifiers stay; the rule is revised. The clause governs _removal_, not splitting.   |
| Reduction-candidate field: "the unique content the change would otherwise lose (every imperative, warning, exception, identifier, rationale, provenance marker, and verification)" | **No.** The field asks what would be **lost**; a record that holds the rationale loses nothing.                                                                                    |
| Extraction candidate: the destination must be "durable, project-authored, and owned", and "loaded or deterministically discoverable when the trigger occurs"                       | **Yes, today** — `docs/guidance-evidence/` is owned by nobody, and no auditor scans it. **This is the only real prohibition, and it is an ownership gap, not a preservation one.** |

The authoring standard's gotcha shape also does not forbid the split: a gotcha's slots (Trigger, Risk, Cause, Required
action, Resolution, Verification, Scope) are satisfiable when the incident lives in a record the entry points at.

**Therefore the smallest delta is one `MODIFIED` block plus four `ADDED` requirements**, not a change to the
preservation rules: it widens the audit's scope to own the records, defines their shape, and states what the root text
must keep. It gives the records an owner and a pairing obligation, states what the root text must still carry, and keeps
the destination from becoming a dump. It is written at
`openspec/changes/separate-agents-md-operational-from-evidence/specs/showcase/quality/agent-skills/spec.md` for the
owner's review. It changes no preservation or removal clause, so the earlier draft's "owner's recorded exception"
framing is withdrawn: relocation is not loss, and no exception is needed for it.

### Operational and historical identifiers

The preservation contract has to say _which_ identifiers stay, or "preserve every identifier" reads as "keep them all at
root". The split this design applies:

| Class                                                                     | Examples from the pilot                                                                                                                                                                                                            | Where it lives                                                  |
| ------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------- |
| **Operational identifier** — needed to perform or verify the action       | `test -e`, `git ls-files`, `git diff --cached --name-only`, `helm show values <chart> --version <pinned>`, `docs/adr/**` in the scanned-file list, `owner/repo#NNN`, `retires when`, `upstream-references.yml`, `@param`/`@return` | **Root**, in the rule's own text. It may not move.              |
| **Historical identifier** — names the episode, its actor, or its artifact | `ShowcaseEventStreamControllerTests.java`, `#43`, `remove-redis-client-label`, `rework-idea-setup`, `5fc1a09`, `36 panels`, `#755`, `#3334`, `micromatch/braces#70`                                                                | **Record**, with the episode. It must survive, but not at root. |

The boundary test is the action, not the token: if a reader cannot perform or verify the governed action without it, it
is operational and stays; if it identifies _when_ the rule was learned, it travels. Applied to the two representative
entries: `test -e` and `git ls-files` stay at root while `ShowcaseEventStreamControllerTests.java` and `#43` move; the
scanned-file list stays at root while `#755`, `#3334`, and `micromatch/braces#72` move.

**Duplication boundary, applied.** Each drafted section carries its `Rule:` key verbatim and no normative body. The
`occupied-path` key is the rule's lead; its body explains that the file was assumed new and that nobody noticed until
the suite failed, and it restates neither the inspection nor the merge instruction, which stay in D1's root text. The
`upstream-references` key is that rule's lead, and its body names what the parked report found, the closing comment, the
fixing PR, and the scanned-file rule's consequence — but states no imperative: "resolve it", "read the closing comment",
and "cite the fixing PR" stay in `AGENTS.md`. The `consistency-sweep` body says the rule's six scopes each came from an
episode without listing them; the list stays in R1's root text. The `staged-set-sweep` body names `git add` because the
example is about a staging sweep — a factual mention, not an instruction: "stage explicit paths, then check the staged
set" stays in the root text. All seven bodies were read against this distinction: each names commands, files, or
identifiers as part of what happened, and none tells a reader what to do.

**The lossy trim is separate and stays separate.** Under that option (D), the D1–D5 deletions remove illustration with
no record at all. It is recorded as the lossy option, and the recommendation does not implement it unless the owner
prefers that path knowingly.

## Capture decisions

The unit's capture produced three proposed additions, one retirement, and one authoring-system recommendation. Each is
decided here, because this unit causes them and the repository's capture lifecycle puts a unit's own accepted lessons on
that unit's branch — not on a separate docs PR.

### Accepted: the point-in-time sentence becomes "a dated record"

**Why it must change.** The rule "A durable artifact may assert only what the repository can evidence" ends: _"Point-in-
time narrative belongs in a change's archived artifacts, not in a durable one."_ As written it forbids this unit's own
destination, and it is already false against `docs/retrospectives/`, `docs/audits/`, and `docs/load-tests/` — all
durable, all dated narrative. Its purpose is to keep narrative **out of the guidance**, which the replacement keeps.

**Destination:** `AGENTS.md`, `### Documentation & durable claims`, the last sentence of that bullet.

**Exact replacement** (measured `+118 B`):

```text
Point-in-time narrative belongs in a dated record — the change archive, a retrospective, an audit report, a
load-test record, or a rule's `docs/guidance-evidence/` entry — never in the guidance itself.
```

It is a _correction of a rule this unit makes obsolete_, not an addition: the sanctioned-home list names the homes the
repository already keeps, and "never in the guidance itself" is the original prohibition restated.

### Accepted: a file-scoped scan is decided per item

**Why it is not covered.** The extraction workflow's gate decision point is binary — retarget the gate, or keep the
block. Moving _some_ items out of a scanned file is neither, and its own example (19 markers) was a whole-block move.
The failure this unit met: `upstreamReferences` extracts from four declared files, so a reference moved to the record
silently leaves the report without anything failing.

**Destination:** `AGENTS.md`, the `### Workflow: Extract an AGENTS.md section` heading's gate decision point.

**Exact change** (measured `+153 B`): "or keep the block" becomes "or keep each item the scan would stop seeing", and
one sentence follows the existing example — _"An item nothing live depends on may leave, with its reason recorded."_ —
with the unit's `captured:` marker appended beside the existing one.

### Dropped: a created artifact's size measured from its draft

**Covered.** The pre-write pass already requires each figure to be derived from the mechanism that produces it, "the
window or instant it spans and the aggregation it applies", and re-derived "after that mechanism's last edit". For a
file this unit creates, that mechanism is the drafted file: measuring it is the rule applied, and subtracting the
replaced span is the rule broken. The incident — 7,569 B drafted against a 5,238 B projection — is recorded here, and no
new rule is added.

### Dropped: the identifier that may leave root

**The decision difference does not hold.** Step 5 requires the retained pointer to carry "every imperative, warning, or
identifier the destination does not carry" — so an identifier leaves root only when the destination _does_ carry it, and
for extraction that destination is read at the trigger (step 1 verifies it carries the complete contract). This unit's
case is the opposite: a split whose record is not read before acting, which the delta's "operational element"
requirement already governs. Replacing step 5's wording would weaken extraction to cover a case it does not govern.

### Parked, by the owner's direction: the extraction-pointer generalisation

The capture's authoring-system recommendation — the retained pointer must identify each converted source unit — stays
parked (task 4.7). The **specific** pairing contract this unit needs is in the delta and is not affected by that
decision.

### What the capture adds to the unit's root figure

| Change                                              | Destination                                   | Measured Δ |
| --------------------------------------------------- | --------------------------------------------- | ---------: |
| The point-in-time sentence becomes "a dated record" | `### Documentation & durable claims`          |     +118 B |
| A file-scoped scan is decided per item              | the extraction workflow's gate decision point |     +153 B |
| **Subtotal**                                        |                                               | **+271 B** |

Both are measured against `AGENTS.md` at `c5dea506`, before formatting. The disposition table stays measured against
that baseline too, so row 24's 1,923 B does not yet include its own +118 B correction.

## Validation

Each of the eight drafted rewrites was tested against five conditions. "Same decision" means an agent reading only the
root text reaches the decision the current text produces.

| Test                                              | D1  | D2  | D3  | D4  | D5  | R1  | R2  | R3  |
| ------------------------------------------------- | --- | --- | --- | --- | --- | --- | --- | --- |
| The motivating incident still yields the action   | ✔   | ✔   | ✔   | ✔   | ✔   | ✔   | ✔   | ✔   |
| A neighbouring case the rule should prevent       | ✔   | ✔   | ✔   | ✔   | ✔   | ✔   | ✔   | ✔   |
| A legitimate case the rule must allow             | ✔   | ✔   | ✔   | ✔   | ✔   | ✔   | ✔   | ✔   |
| A task naming no destination                      | ✔   | ✔   | ✔   | ✔   | ✘   | ✔   | ✔   | ✔   |
| A review verifying the rationale, with the record | ✔   | ✔   | ✔   | ✔   | ✔   | ✔   | ✔   | ✔   |

D4 (the paragraph-boundary paragraph) is the one draft whose root text alone does not carry the action, because its
action is "proofread blank lines", which is stated — so the ✘ marks the _reading_: an agent told only "keep em-dashes
paired" may not look at blank lines. The implementation retains the sentence **"proofread blank lines as content too"**
(it does), and the ✘ records that the rule is weak at the action boundary, not that the rewrite fails.

**Operational identifiers, checked.** For each of the seven converted rules, every operational identifier in the removed
span is in the `After` text: D1 keeps `test -e` and `git ls-files`; D2 keeps `test ! -d` and the archive `git ls-files`;
D3 keeps `git diff --cached --name-only`, `git status`, and the guard's enumeration; D4 keeps the em-dash action and
"proofread blank lines"; D5 keeps the source list and `helm show values <chart> --version <pinned>`; R1 keeps the six
scopes and `@param`/`@return`; R2 keeps the scanned-file list, `owner/repo#NNN`, `retires when`, and
`upstream-references.yml`. Every historical identifier the drafts remove is in the matching record section — the
inventory below is the check. A command or identifier the record's body names for the episode is a permitted
description, not a second instruction: the check is whether the body tells a reader what to do, not whether it quotes a
command.

| Draft | Historical identifiers that moved                                                                                                                                                                                                                                                                                                                                                                                        | Present in the record?  |
| ----- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ | ----------------------- |
| D1    | `ShowcaseEventStreamControllerTests.java`, `#43`, "two tests"                                                                                                                                                                                                                                                                                                                                                            | ✔ `occupied-path`       |
| D2    | `remove-redis-client-label`                                                                                                                                                                                                                                                                                                                                                                                              | ✔ `archive-incomplete`  |
| D3    | `rework-idea-setup`, `scripts/__pycache__/*.pyc`                                                                                                                                                                                                                                                                                                                                                                         | ✔ `staged-set-sweep`    |
| D4    | `audit.yml`, `buildpack-updates.yml`, `5fc1a09`, PR #320                                                                                                                                                                                                                                                                                                                                                                 | ✔ `paragraph-boundary`  |
| D5    | "two replicas", "36 panels", "four services and a gateway", `commandService.replicaCount`, the values file                                                                                                                                                                                                                                                                                                               | ✔ `documented-numbers`  |
| R1    | `fix-javadoc-consistency`, `@param elasticsearchConverter`, `mapObject(ShowcaseEntity…)`, `ShowcaseProjector.METER_NAME_PREFIX`, `apply-specs-audit-findings`, `org.springdoc: springdoc-openapi-starter-webflux-ui`, `fix-springdoc-coordinate-spelling`, `reconcile-showcase-cache-default`, "tenfold", the four restatements, `diagramPadding`, the 2026-10-11 audit, `toolingUpdates`, `/opsx-tool-update`, `ci.yml` | ✔ `consistency-sweep`   |
| R2    | `#755`, PR #1060, `#3334`, PR #3337, `satisfiesDeclaredBound`, `checkBuildEnvironmentConstraints`, `hold-back-nginx-buildpack`, `#1340`, `io.buildpacks.buildpackage.metadata`, `micromatch/braces#70`, `micromatch/braces#72`                                                                                                                                                                                           | ✔ `upstream-references` |

**Evidence classes.** All decision paths are _demonstrated_ — the root text stays. The record path is _proposed_ until
the layout exists. The residual reliance is the ordinary one: the token must be honoured for anyone to reach an entry,
and no gate enforces it; the pairing check catches a missing entry, not an unread one.

## Regrowth

| Case                                   | Where it goes now                                                                                                         |
| -------------------------------------- | ------------------------------------------------------------------------------------------------------------------------- |
| A new obligation                       | A new rule at root, as today; the capture names the existing rule it extends or states that none covers it.               |
| A correction to an existing obligation | The rule's text, and — when its evidence moved — the same section in the record, found from the rule's `evidence:` token. |
| Additional evidence for a covered rule | Unchanged: `already covered`, recorded in the change record, not in the root text.                                        |

`lesson-capture.md` still sends a captured lesson's supporting evidence to the **change record**, and this unit leaves
that contract alone. The evidence record is populated by the relocation work itself; afterwards it grows only through
corrections to converted rules, which the rule's `evidence:` token makes discoverable, and through a later tranche's
relocations. A capture's "additional evidence" therefore does **not** silently accumulate in the record, and the record
never receives material that has no root rule.

## Alternatives

Four options, each in the four quantities. "Measured" means the seven converted rules and the two drafted records; the
remaining rules are not estimated as a block.

| Option                                                      | Root-file reduction                                            | Additional routinely loaded               | Total maintained content                                                             | Maintenance and retrieval                                                                                                                     |
| ----------------------------------------------------------- | -------------------------------------------------------------- | ----------------------------------------- | ------------------------------------------------------------------------------------ | --------------------------------------------------------------------------------------------------------------------------------------------- |
| **A. Retention**                                            | 0                                                              | 0                                         | unchanged                                                                            | unchanged                                                                                                                                     |
| **B. Rewrite, evidence to existing sources (partly lossy)** | −5,238 B of rule text measured                                 | 0                                         | −5,238 B; the episode is deleted and survives only where an existing source holds it | none; two lose the record entirely (D2, D4); D5's examples survive only as `readme-auditor.md`'s illustration, and D3 only as an archive line |
| **C. Rewrite, evidence to the new reference**               | **−4,931 B net (1.45%)** measured, tokens included             | **0** — the tokens are inside that figure | **+2,638 B**: the drafted records are 7,569 B                                        | one owned file per subsection; one hop for verification; a pairing check in the audit                                                         |
| **D. Lossy trim, deletion only (no rewrite, no record)**    | −1,190 B measured (the D1–D5 deletions without their rewrites) | 0                                         | −1,190 B                                                                             | none; loses the record for the episodes the deletions carry                                                                                   |

- **B is rejected on preservation.** Two of the seven lose the record entirely (D2, D4); D5's examples survive only as
  `readme-auditor.md`'s illustration, and D3 only as an archive line. It is not rejected for leaving content elsewhere.
- **C is the option this design proposes.** It reduces root by a measured amount, keeps every obligation and every
  episode, and pays for that with 307 bytes of token and two owned files.
- **D is the explicitly lossy option**, kept separate from C and not mixed into it.
- **A is the baseline**, and remains coherent if the owner judges 1.45% not worth two new files and a spec delta.

## Size accounting

Two levels, kept apart: the **seven-rule conversion subtotal**, which is measured and is what the recommendation's
evidence rests on, and the **whole unit**, which also carries the two accepted capture corrections and is provisional
until they are applied and formatted.

| Move                                                       | Basis                                                              |     Root Δ |  Records Δ |
| ---------------------------------------------------------- | ------------------------------------------------------------------ | ---------: | ---------: |
| Seven rule rewrites, text only (D1–D5, R1–R2)              | measured: each `After` without its token, against the current span | **−5,238** |          0 |
| Seven `evidence:` tokens                                   | measured: each token's paragraph plus its blank separator          |       +307 |          0 |
| **Conversion subtotal (routinely loaded)**                 | the two rows above                                                 | **−4,931** |          0 |
| Capture: the point-in-time sentence becomes a dated record | measured: the replacement against the current sentence             |       +118 |          0 |
| Capture: a file-scoped scan is decided per item            | measured: the replacement against the current decision point       |       +153 |          0 |
| **Whole unit, provisional (routinely loaded)**             | the four rows above                                                | **−4,660** |          0 |
| Two drafted records                                        | measured: the formatted files in "Complete drafts"                 |          0 | **+7,569** |
| **Whole unit, provisional (total maintained)**             | root + records                                                     | **−4,660** | **+7,569** |

- **The conversion subtotal is −4,931 B (1.45%)** and still reproduces from the seven `After` texts. It is the figure
  the pairing, the records, and the record sizes are all keyed to.
- **The whole unit is −4,660 B (1.37%) provisional.** The two capture corrections add 271 B of root text; the figure is
  provisional because applying them re-wraps two paragraphs, which can move a few bytes, and because the final
  measurement happens after `spotlessApply`.
- **Net total maintained content, whole unit: +2,909 B provisional** (7,569 − 4,660). The records remain larger than the
  rule text they absorb, by 2,331 B; the corrections are root-only.
- **R3 is drafted and not converted.** Its rewrite saves 110 bytes, a token costs 41, and an entry would need
  maintaining: net −69 B, which does not pay for the entry. It stays at root, unchanged.
- **The remaining rules are not a tranche.** Their dispositions are carried in "Follow-up", and no aggregate saving is
  projected for them. The 13,030-byte figure in the disposition table is a reading estimate of their episode text.
- **The lossy alternative is not selected.** Option D stays in the comparison as the recorded cost of not building the
  records; the recommendation implements C.

No token budget, size cap, percentage target, or upper bound is proposed.

## Recommendation

**Implement C for the seven converted rules, as one unit.** It is the smallest unit that reduces the loaded surface by a
measured amount, keeps every obligation and every episode, and exercises the destination end to end.

- **Scope:** D1, D2, D3, D4, D5, R1, R2 — rows 9, 11, 12 (Git) and 14, 15, 20, 29 (Documentation).
- **Creates:** the two evidence files exactly as drafted, and one `evidence:` token per converted rule.
- **Root reduction:** the conversion subtotal is **−4,931 B (1.45%)**, measured; with the two accepted capture
  corrections the unit is **−4,660 B (1.37%) provisional**. Total maintained content **+2,909 B provisional**;
  additional routinely loaded material **0** beyond the tokens already counted.
- **Requires:** the `agent-skills` delta — one `MODIFIED` audit-scope requirement and four `ADDED` requirements — and
  `.opencode/agent/agents-auditor.md`, whose scope line and checks carry that obligation.
- **Does not include:** R3's conversion, the other 23 rules, the duplicated-word fix as its own unit beyond D4's line,
  the reviewer-contract improvement, the audit-trending tool, or any stage-specific workflow change.

**Follow-up: what remains, and what it needs.**

| Group                             | Rules                                                          | What it needs before drafting                                                                                              |
| --------------------------------- | -------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------- |
| Drafted, not converted            | R3 (row 6)                                                     | nothing — the net is −69 B; convert it only if a record section is wanted for its own sake                                 |
| Rewrite worth drafting, undrafted | rows 1, 2, 3, 4, 5, 13, 16, 17, 18, 22, 23, 24, 25, 27, 28, 30 | a per-rule draft and its own measurement; several (rows 2, 3, 5, 13) are smaller than the token-plus-entry they would need |
| Evidence relocation, undrafted    | row 26                                                         | a token and a record section, if its 130-B episode is worth one                                                            |
| No separable material             | rows 7, 8, 10, 19                                              | nothing to draft; they stay at root as they are                                                                            |
| Rejected                          | row 21                                                         | stays at root: its referent is a live file                                                                                 |

Two groups, and only one is pending work. **Candidates awaiting a draft** — R3 (drafted, unconverted), the 16 undrafted
rewrites, and the 1 undrafted relocation — each face the same checks (the operational-identifier inventory, the five
validation conditions, and the pairing test), and a preservation-based option is open for each. **Rules retained by
design** — the four with no separable material (rows 7, 8, 10, 19) and the one rejected on a retrieval failure (row 21)
— have no such option: their dispositions are final, and nothing further is owed for them.

## Risks / trade-offs

- **A record nobody reads.** The token must be honoured for anyone to reach an entry; an agent that never verifies a
  rule never opens it, and that is by design — the root text carries the action. The cost is that a _wrong_ rule stays
  wrong until someone verifies it, which is today's situation too.
- **The association can drift.** A rename leaves the section's `Rule:` paragraph stale; the pairing check reports it as
  two findings (no section matches the rule, and the section matches no rule) rather than letting it rot silently.
- **The records are bigger than what they absorb.** Measured: 7,569 B of record against 5,238 B of removed rule text.
  The loaded surface is what falls; total maintained content rises by 2,638 B for the conversion and 2,909 B for the
  unit, and that is a cost the owner is choosing.
- **A new owned surface.** Two files, an auditor scope, and a delta. That is the price of a destination that survives;
  the alternative is the archive, which is measurably incomplete.
- **The remaining rules are not a plan.** 17 of the 23 are undrafted and carry only a reading-based estimate; each needs
  its own draft, measurement, and identifier inventory before it can be converted.
- **The delta is new spec surface.** It is five requirements (one `MODIFIED`, four `ADDED`) and eleven new scenarios,
  but it is a durable obligation; if the owner rejects it, C is unavailable and A is the outcome.

## Rollback

Nothing is implemented, so there is nothing to reverse yet. The planning outputs are this change directory (untracked
until committed) and its `docs/review-rounds.tsv` rows.

If the unit is implemented and later reversed: restore the seven converted spans from `git show HEAD:AGENTS.md`, delete
the two `docs/guidance-evidence/` files, remove the seven `evidence:` tokens, restore the point-in-time sentence and the
extraction workflow's gate decision point (dropping the added sentence and its `captured:` marker), restore
`agents-auditor.md`'s scope line, and remove the delta's `MODIFIED` block and its four `ADDED` requirements. Reversing
only the guidance would leave an owned record nothing references; reversing only the delta would leave records no one
audits. The unit is consistent only as a whole.

## Claims

| #   | Claim                                                                                                                      | Source / deriving command                                                                                                                                                                                                                                                                                                                  |
| --- | -------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| 1   | The baseline revision is `c5dea506f00d12761e7941d8e3c80eac5718fbf8`, and `AGENTS.md` is unmodified on the branch           | `git rev-parse HEAD`; `git status --porcelain -- AGENTS.md` → `0`                                                                                                                                                                                                                                                                          |
| 2   | `AGENTS.md` is 338,964 bytes over 3,463 lines                                                                              | `wc -c -l AGENTS.md` → `338964` / `3463`                                                                                                                                                                                                                                                                                                   |
| 3   | The top-level section table sums to the file                                                                               | the census block over the `^## ` headings → 338,951 B; the 13-B remainder is the title line and its blank line                                                                                                                                                                                                                             |
| 4   | `## Gotchas` is 139,963 B / 1,369 L, and its twelve subsections sum to 139,951 B / 1,367 L                                 | the census block restricted to `###` headings between `## Gotchas` and the next `##`                                                                                                                                                                                                                                                       |
| 5   | The two pilots are 49,816 B / 473 L / 30 rules / 48 markers / 24,864 estimated evidence B                                  | the census block's Documentation and Git rows                                                                                                                                                                                                                                                                                              |
| 6   | The seven converted rules remove 5,238 bytes of rule text, and their `evidence:` tokens add 307, for a net **−4,931**      | each `After` against its current span, including the token paragraph: D1 −148, D2 −59, D3 −199, D4 −196, D5 −373, R1 −1,996, R2 −1,960                                                                                                                                                                                                     |
| 7   | The seven tokens are 307 bytes — 44 bytes a rule including the blank separator                                             | the `evidence:` paragraph in each `After` plus the blank line that separates it: 3 × 41 + 4 × 46; the seven token lines alone are 293                                                                                                                                                                                                      |
| 8   | The undrafted rules' episode text is estimated at 13,030 bytes                                                             | the disposition table's per-rule estimate, summed; **an estimate**, whose basis is each rule's identified episode sentences minus a retained rewrite of the drafted style; it is not a projected saving                                                                                                                                    |
| 9   | 30 of 30 rules carry a disposition: 19 rewrite, 6 relocation, 4 no separable material, 1 rejected, 0 unassessed            | the disposition table's rows                                                                                                                                                                                                                                                                                                               |
| 10  | `captured:` names: 274 occurrences, 183 distinct, 142 with an archived directory, 41 without                               | the Probe 1 snippet in "Destination verification"                                                                                                                                                                                                                                                                                          |
| 11  | 24 of 30 pilot rules are marked; 10 have a marker with no archive directory; 19 keep <50% of their distinctive tokens      | the Probe 2 snippet, whose last line prints `rules 30  marked 24  no-dir 10  low-coverage 19`                                                                                                                                                                                                                                              |
| 12  | The other durable-document classes carry no episode of their own                                                           | the Probe 3 snippet and its table                                                                                                                                                                                                                                                                                                          |
| 13  | `rework-idea-setup/proposal.md:43` records the `__pycache__` ignore                                                        | `grep -n pycache openspec/changes/archive/2026-09-11-rework-idea-setup/proposal.md`                                                                                                                                                                                                                                                        |
| 14  | No gate reads the pilot spans                                                                                              | `grep -c 'helm install'` over both spans → `0`; the `paketo` matches are name and issue references, not version pins; the marker guard's inputs are `AGENTS.md` plus the project guidance files, and every draft keeps its marker                                                                                                          |
| 15  | The authoring standard forbids decision-free content and requires a gotcha's evidence discipline                           | `openspec/specs/showcase/quality/agent-skills/spec.md`, "AGENTS.md entries follow a stated authoring standard"                                                                                                                                                                                                                             |
| 16  | The standard's removal clause is about removal, not splitting                                                              | the same requirement, "Existing guidance is revised or removed only once obsolete"                                                                                                                                                                                                                                                         |
| 17  | The destination must be owned and discoverable before the decision                                                         | the same spec, "Project-owned agent tooling is audited…", the extraction-candidate conditions                                                                                                                                                                                                                                              |
| 18  | `agents-auditor`'s scope today is `AGENTS.md` and `.opencode/`, so `docs/guidance-evidence/` is unowned                    | the same requirement's first sentence; `grep -n 'docs/' .opencode/agent/agents-auditor.md` names `docs/audits/` only as the prior-report location and no `guidance-evidence` scope                                                                                                                                                         |
| 19  | A captured addition that is only evidence is `already covered` and recorded in the change record                           | `.opencode/agent/lesson-capture.md` — the `already covered` bullet                                                                                                                                                                                                                                                                         |
| 20  | The 2026-10-19 audit rejected a Gotchas extraction, on grounds this unit does not propose                                  | `docs/audits/2026-10-19.md`, "Extraction opportunities", closing paragraph                                                                                                                                                                                                                                                                 |
| 21  | The previous unit's measured reduction is −4.98%                                                                           | `openspec/changes/archive/2026-10-20-reduce-agents-md-loaded-surface/tasks.md` 3.7                                                                                                                                                                                                                                                         |
| 22  | The `paragraphs` duplication in the em-dash rule is real                                                                   | `sed -n '3123,3126p' AGENTS.md \| tr '\n' ' ' \| grep -o 'paragraphs  *paragraphs'` → one match                                                                                                                                                                                                                                            |
| 23  | All eight drafts, and the two record files, are formatter-stable                                                           | Prettier 3.9.10 at `printWidth` 120 / `proseWrap: always` over each replacement and each drafted file; both records are shown at their formatted size                                                                                                                                                                                      |
| 24  | The delta is one `MODIFIED` block plus four `ADDED` requirements, and the `MODIFIED` block carries every existing scenario | a word-level diff of the delta's `MODIFIED` block against the main spec's requirement shows three changes — the widened scope sentence (which re-wraps its paragraph), the widened `An agent-tooling audit is produced` scenario, and three appended pairing scenarios — with every other word identical and 29 scenarios before, 32 after |
| 25  | The two drafted record files are 7,569 bytes: 1,822 and 5,747, formatted                                                   | `wc -c` over the two `markdown` fences in "Complete drafts", after Prettier                                                                                                                                                                                                                                                                |
| 26  | The records are 2,331 bytes larger than the 5,238 bytes of rule text they absorb                                           | 7,569 − 5,238; the growth is each section's address, `Rule:` key, and context, plus the two file headers                                                                                                                                                                                                                                   |
| 27  | Of the five upstream references R2 moves, four remain in scanned files and one (`micromatch/braces#70`) does not           | `grep -rn` for each across `AGENTS.md`, `README.md`, `docs/`, and the specs: `#755` in ADR-0007, `README.md`, and `## Build & Test`; `#3334` in `docs/ideas.md` and the `projection-model` spec; `#1340` in `## Docker Images` and the deployment gotcha; `#72` in ADR-0014; `#70` only in the moved span                                  |
| 28  | The point-in-time correction adds 118 bytes                                                                                | the replacement against `AGENTS.md`'s current sentence, `len(new) - len(old)`                                                                                                                                                                                                                                                              |
| 29  | The per-item scan correction adds 153 bytes                                                                                | the clause replacement (+27) and the sentence plus marker (+126), measured the same way                                                                                                                                                                                                                                                    |
| 30  | The whole unit is −4,660 B (1.37%), provisional                                                                            | −4,931 + 118 + 153; provisional until applied and formatted, when tasks 5.8 and 5.10 re-measure                                                                                                                                                                                                                                            |

### Seed and dismissals

`scripts/claim-scan.py --base origin/main` reports candidates of all four kinds, plus the numerals and paths inside the
two drafted files. Each kind is resolved once:

| Kind         | Disposition                                                                                                                                                                                                                  |
| ------------ | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `count`      | A figure the design states is a claims row above; a figure marked `est.` or `≈` is an estimate, and a count with no row would be an unreproducible claim.                                                                    |
| `absolute`   | Dismissed: a prose quantifier stating a rule of this design ("every draft keeps its marker") or ordinary English, not a repository fact. A universal claim about the corpus is resolved by the per-item table it introduces. |
| `cross-ref`  | Resolved by the row whose source names that artifact, or by the anchor in the disposition and candidate tables.                                                                                                              |
| `path`       | Resolved by the command in the row it appears in, or it is a path this change creates (`openspec/changes/…`, `docs/guidance-evidence/…`).                                                                                    |
| fenced draft | A numeral, path, or reference inside a `markdown` or `text` fence is the drafted artifact's own content, not a claim about the repository; each draft's size is row 25, and its identifiers are checked by "Validation".     |
