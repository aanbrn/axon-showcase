# Design

## Context

`proposal.md` asks which of the remaining Gotchas rules in the two piloted subsections are worth converting under the
evidence-record mechanism `separate-agents-md-operational-from-evidence` established. This design drafts all seventeen,
measures each, verifies each draft against the preserved obligations, and recommends one coherent unit rather than the
whole candidate set.

**The objective, stated once.** The objective is **root-file reduction** — the surface loaded into every session —
subject to two constraints: the root text must still support the governed decision without the record, and every
historical identifier must survive in the record. Total maintained content is a reported cost, not a veto; the previous
unit settled that.

### Reproducible baseline

**Every baseline figure is measured on the pre-change revision**, `git show HEAD:AGENTS.md` (`HEAD` is `e07f5f8f`),
because this change edits the file: a live `wc` on the working tree reports the implemented size, and the census and the
`b7f26469` comparison would report the conversion rather than the state it was decided from.

| Fact                                 | Command                                                         | Result                                                                |
| ------------------------------------ | --------------------------------------------------------------- | --------------------------------------------------------------------- |
| Revision                             | `git rev-parse HEAD`                                            | `e07f5f8f7eb3a417155df93e82408994beeabeff`                            |
| Branch                               | `git branch --show-current`                                     | `convert-remaining-gotchas-rules`                                     |
| Working tree                         | `git status --porcelain --untracked-files=all` at planning      | empty before this unit; the unit's own changes are the current output |
| `origin/main`                        | `git rev-parse origin/main`                                     | `e07f5f8f7eb3a417155df93e82408994beeabeff`                            |
| `AGENTS.md` size                     | `git show HEAD:AGENTS.md > /tmp/pre.md && wc -c -l /tmp/pre.md` | `331864` bytes / `3409` lines                                         |
| `## Gotchas` size                    | the census script below, on `/tmp/pre.md`                       | `135399` bytes / `1,348` lines                                        |
| `### Git & repository workflow`      | the same script, pre-change revision                            | `16484` bytes / `167` lines / 13 rules                                |
| `### Documentation & durable claims` | the same script, pre-change revision                            | `28521` bytes / `282` lines / 17 rules                                |

The preceding reductions are merged: `reduce-agents-md-loaded-surface` (#648), the parent
`separate-agents-md-operational-from-evidence` (#649), and `load-stage-workflow-instructions-on-demand` (#651), which is
`HEAD`. At `HEAD` — before this unit — both pilot subsections are **byte-identical to `b7f26469`**, the parent change's
merge commit, so no later change had touched a candidate rule; the working tree differs in the Documentation subsection
by this unit's eleven rewrites:

The subsection census, run from the repository root:

````text
import re
import sys
path = sys.argv[1] if len(sys.argv) > 1 else "AGENTS.md"
raw = open(path, "rb").read(); txt = raw.decode("utf-8"); lines = txt.split("\n")
if lines and lines[-1] == "": lines = lines[:-1]
offs = [0]
for ln in lines[:-1]: offs.append(offs[-1] + len(ln.encode()) + 1)
def bspan(i, j):                      # lines i..j-1, 1-based
    a = offs[i-1]; b = offs[j-1] if j-1 < len(offs) else len(raw); return b - a
in_fence = False; heads = []
for i, ln in enumerate(lines, 1):
    s = ln.strip()
    if s.startswith("```"): in_fence = not in_fence; continue
    if in_fence: continue
    m = re.match(r"^(#{2,3}) (.+)$", ln)
    if m: heads.append((i, len(m.group(1)), m.group(2)))
print("TOTAL", len(raw), "bytes", len(lines), "lines")
tops = [(i, t) for (i, l, t) in heads if l == 2]
for k, (a, t) in enumerate(tops):
    b = tops[k+1][0] if k+1 < len(tops) else len(lines)+1
    print("%-40s %7d %5d" % (t, bspan(a, b), b-a))
for head in ("Git & repository workflow", "Documentation & durable claims"):
    a = [i for (i, l, t) in heads if l == 3 and t == head][0]
    nxt = [i for (i, l, t) in heads if i > a and l <= 3]
    b = nxt[0] if nxt else len(lines)+1
    starts = [i for i in range(a, b) if lines[i-1].startswith("- ")]
    print("%-40s %7d %5d rules=%d" % (head, bspan(a, b), b-a, len(starts)))
````

Identity against the parent change's merge commit:

```text
$ git diff b7f26469 HEAD -- <the two subsections>   →   identical at HEAD
```

### Prior decisions recovered

| Prior unit                                                  | What it settled and what it leaves this unit                                                                                                                                                            |
| ----------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `separate-agents-md-operational-from-evidence` (2026-10-20) | It drafted eight rules, converted seven, and dispositioned all 30 in the two subsections. Its estimates are reading-based for the 17 undrafted candidates; this design replaces them with measurements. |
| `reduce-agents-md-loaded-surface` (2026-10-20)              | Extraction is settled; this unit tests only the internal split. Its preservation policy — every imperative, warning, exception, identifier, gate condition, and marker — is this unit's contract.       |
| `add-extraction-procedure` (2026-10-19)                     | A destination must be owned and discoverable before the decision. `docs/guidance-evidence/` now satisfies this for the two subsections' rules.                                                          |
| `record-agents-md-authoring-standard` (2026-10-17)          | Entries follow a stated representation and shape; a gotcha separates observation, hypothesis, confirmed cause, and verified resolution.                                                                 |
| `make-captured-rules-traceable` (2026-09-18)                | `captured:` markers are provenance and sit at the end of the rule block they record. Every draft keeps or relocates its markers accordingly.                                                            |
| `lesson-capture` and `agent-skills` capture requirement     | A unit's accepted lesson rides its branch; `AGENTS.md` growth is bounded by a discipline, not a cap.                                                                                                    |

### What this unit does not reopen

| Class                                            | Rules                                                                     | Why it stays settled                                                                                                                                                                                           |
| ------------------------------------------------ | ------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| No separable material (parent rows 7, 8, 10, 19) | AST codemod; edit to wrapped prose; work a change surfaces; named example | The rule is its identifiers or its example; nothing can move without losing the decision. No later change touched them, so no new evidence invalidates the disposition.                                        |
| Rejected on retrieval failure (parent row 21)    | "An abandoned approach needs the same sweep"                              | Its example is a live decision about a live file (`.opencode/agent/readme-auditor.md`). Unchanged since.                                                                                                       |
| Drafted, unconverted (parent row 6 / R3)         | "A formatter-owned file is re-wrapped"                                    | Net −69 B; its four hazard classes are the rule's scope, and its `captured:` provenance is already carried by the em-dash record. No substantive reason beyond the byte saving has emerged (see `C18`, below). |

### Parked ideas reconciled

| Parked idea                                                               | Relationship                                                | Disposition                                                                                                  |
| ------------------------------------------------------------------------- | ----------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------ |
| The entry this unit acts on (convert the remaining Gotchas rules)         | This unit is its implementation                             | **Partly resolved.** The Documentation tranche converts; the Git tranche is re-parked with measured figures. |
| Extend the extraction conditions so the pointer identifies each unit      | Would change the extraction pointer, not the evidence token | **Not adopted.** Out of scope by the request; the evidence token already identifies its record.              |
| Carry the audit ledger's retrieval-path result                            | Constrains how a destination is trusted                     | **Constraint applied.** The destination's owner and pairing check are verified in `agents-auditor.md`.       |
| Trend the audit counts across reports                                     | Would measure retention of a reduction over time            | **Not adopted.** This design reports one unit's measured figures.                                            |
| Refuse a commit that stages an archived change dir with an unchecked task | Touches commit hygiene, not guidance placement              | **Unrelated.** No effect on this unit.                                                                       |

**Entries resolved by this unit: none.** The acted-on entry stays parked for its Git tranche and is only partly drained.

## Goals / Non-Goals

**Goals:**

- Draft every one of the 17 eligible candidates completely, and measure each against the baseline above.
- Verify each draft against the five boundary conditions and against every backticked identifier in the span it
  replaces.
- Rank the candidates by verified reduction and preservation risk, and recommend one coherent unit.
- Report the conversion subtotal separately from the projected whole-unit root delta, and state whether the saving
  justifies the maintenance and review cost.

**Non-Goals:**

- Implementing anything: no `AGENTS.md` rule is edited, no evidence file is written, no main spec is edited.
- Expanding stage-specific workflow loading, adding an evidence-storage mechanism, or changing the pointer policy.
- Audit-ledger or trend tooling, a repository-wide deduplication sweep, or unrelated typo fixes.
- Converting a rule the parent unit settled as having no separable material or as rejected.

## Method

### The four quantities

| Quantity                             | Definition                                                             | Why it is separate                                                                      |
| ------------------------------------ | ---------------------------------------------------------------------- | --------------------------------------------------------------------------------------- |
| Root-file reduction                  | Bytes removed from `AGENTS.md`, tokens included, measured on the file. | This is the objective.                                                                  |
| Additional routinely loaded material | Bytes added to a surface every session reads (the `evidence:` tokens). | Charged inside the root reduction, not beside it.                                       |
| Total maintained content             | Bytes of guidance and evidence the repository maintains anywhere.      | A reported cost; the records are larger than what they absorb.                          |
| Maintenance and retrieval cost       | Who keeps the material true, and what a reader does to reach it.       | The records are owned by the agent-tooling audit, which pairs each section to its rule. |

### How a candidate is measured

Each per-rule figure is the **whole-file delta** of replacing that one rule's span in `AGENTS.md` with its draft,
including the draft's `evidence:` token paragraph. Measuring on the file rather than by arithmetic on the spans keeps
the figures additive: the seventeen per-rule deltas sum exactly to the measured whole-file delta, and the recommended
unit's deltas sum to its own candidate file.

Record figures are each section's bytes plus the blank line that separates it from its neighbour. The record files' two
existing headers are unchanged, so a file's projected size is its current size plus the new sections' figures.

Both scripts take the file to measure as an argument (the census its first, the other two their second, after this
design's path), so they run against the pre-change revision once the change has landed
(`git show HEAD:AGENTS.md > /tmp/pre.md`). The script reads this design's own fenced drafts, so the figures below
reproduce from the text the change ships:

````text
import re, sys
design = open(sys.argv[1], encoding="utf-8").read()      # this change's design.md
agents = sys.argv[2] if len(sys.argv) > 2 else "AGENTS.md"
raw = open(agents, encoding="utf-8").read(); base = len(raw.encode()); lines = raw.split("\n")
anchors = dict(re.findall(r"^\| (C\d+) +\| \"\*\*(.+?)\*\*\" +\|", design, flags=re.M))
drafts = dict(re.findall(r"^### (C\d+)[^\n]*\n\n(?:Root text after conversion:\n\n)?```text\n(.*?)\n```",
                        design, flags=re.S | re.M))
starts = [i for i, ln in enumerate(lines, 1) if ln.startswith("- ")]
def build(ids):
    # replace from the last rule to the first, so earlier line numbers stay valid
    located = {}
    for cid in ids:
        prefix = anchors[cid].split("…")[0]
        hit = [i for i in starts if lines[i - 1].startswith("- **" + prefix)]
        assert len(hit) == 1, (cid, hit)
        located[cid] = hit[0]
    def boundary(st):
        # the first line that starts another block: the next bullet or the next heading
        later = (
            [i for i in starts if i > st]
            + [i for i, ln in enumerate(lines, 1) if i > st and re.match(r"^#{2,3} ", ln)]
        )
        return min(later) if later else len(lines) + 2
    new = raw
    for cid in sorted(ids, key=lambda c: -located[c]):
        st = located[cid]
        L = new.split("\n")
        new = "\n".join(L[:st - 1]) + "\n" + drafts[cid] + "\n" + "\n".join(L[boundary(st) - 2:])
    return new
for cid in sorted(anchors):
    print("%-4s %6d" % (cid, len(build([cid]).encode()) - base))
doc = [c for c in anchors if c not in ("C1", "C2", "C3", "C4", "C5", "C6", "C18")]
print("doc unit", len(build(doc).encode()) - base, "bytes", len(build(doc).split("\n")) - 1, "lines")
````

A record section's figure is its fenced section's byte length plus the blank line that separates it from its neighbour:

````text
python3 - <<'PY'
import re
d = open('openspec/changes/convert-remaining-gotchas-rules/design.md', encoding='utf-8').read()
for body in re.findall(r'```markdown\n(.*?)\n```', d, flags=re.S):
    slug = re.match(r'## ([\w-]+)', body).group(1)
    print('%-26s %5d' % (slug, len(body.encode()) + 2))
PY
````

### The candidate set, derived from the parent's disposition table

| Parent row | Rule                                         | Parent disposition     | This unit's candidate |
| ---------: | -------------------------------------------- | ---------------------- | --------------------- |
|          1 | `git stash pop` conflict markers             | Rewrite worth drafting | **C1**                |
|          2 | `git reset --hard` on a work branch          | Rewrite worth drafting | **C2**                |
|          3 | `openspec archive` filesystem move           | Rewrite worth drafting | **C3**                |
|          4 | Programmatic edits to a structured file      | Rewrite worth drafting | **C4**                |
|          5 | The same range-boundary hazard bites source  | Rewrite worth drafting | **C5**                |
|          6 | A formatter-owned file is re-wrapped (R3)    | Drafted, unconverted   | **C18 — deferred**    |
|         13 | Never chain an edit to a commit              | Rewrite worth drafting | **C6**                |
|         16 | A two-way split of an enumerated set         | Rewrite worth drafting | **C7**                |
|         17 | `workflowLint` runs `actionlint` alone       | Rewrite worth drafting | **C8**                |
|         18 | Point-in-time count vs durable one           | Rewrite worth drafting | **C9**                |
|         22 | A configuration-default change               | Rewrite worth drafting | **C10**               |
|         23 | Doc claims must match source and strength    | Rewrite worth drafting | **C11**               |
|         24 | A durable artifact may assert only evidence  | Rewrite worth drafting | **C12**               |
|         25 | A claim about a surface outside the repo     | Rewrite worth drafting | **C13**               |
|         26 | When a script generates many claims          | Evidence relocation    | **C14**               |
|         27 | A comparison between two runs                | Rewrite worth drafting | **C15**               |
|         28 | A reproduction in an outward-facing artifact | Rewrite worth drafting | **C16**               |
|         30 | A "the only X" claim                         | Rewrite worth drafting | **C17**               |

**16 undrafted rewrites and 1 undrafted relocation** are the parked entry's figure, and the table reproduces it. R3
(`C18`) is the parent's drafted-but-unconverted rule, kept separate from the parked count.

## Drafts and measurements

### The candidate table

"Preserved obligations" names what the draft keeps at root; every backticked identifier removed from the root text was
verified present in the record (see "Boundary verification"). The complete root text and the complete evidence addition
are the subsections below each row.

| ID  | Current quoted anchor (lead)                                                         | Governed decision                                                                                                  | Preserved obligations at root                                                                                                                                               |      Root Δ |   Record Δ | Disposition | Reason                                                                                                             |
| --- | ------------------------------------------------------------------------------------ | ------------------------------------------------------------------------------------------------------------------ | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------: | ---------: | ----------- | ------------------------------------------------------------------------------------------------------------------ |
| C1  | "**`git stash pop` can leave conflict markers after a rebase.**"                     | What to do when a popped stash conflicts, and how to detect a silent stale-copy revert                             | both hazard conditions; reconcile-from-`main`; `--numstat` check; truncation reading; `--soft`/`--mixed` rule; `gh pr update-branch`; capture-branch rule                   |        −692 |        910 | Deferred    | Git tranche: not this review; same subject as C2–C6.                                                               |
| C2  | "**`git reset --hard` on a branch with uncommitted work…**"                          | The recovery ladder for a work branch, and the sibling per-file revert                                             | what a reset destroys and what survives; the ladder; `git status` confirmation; `checkout --`/`restore`; inspect with `diff`/`show HEAD:`                                   |        −461 |        741 | Deferred    | Git tranche; its ladder is the rule, so the record carries the episode only.                                       |
| C3  | "**`openspec archive` relocates the change dir with a filesystem move…**"            | How to stage an archive so the move, the sync, and the formatting land together                                    | both staged sides; the inspection; the guard; format-before-archive; `git mv` trap; `--yes`; the two non-remedies                                                           |        −256 |        864 | Deferred    | Git tranche, and the set's weakest economics: the record would restate command identifiers that must stay at root. |
| C4  | "**Programmatic edits to a structured file…**"                                       | Verify at the granularity edited: heading, anchor, item boundary                                                   | heading grep/diff; prefer the edit tool; anchor on the changed text; item-end derivation; assert the boundary; marker-placement pointer                                     |      −1,039 |      1,322 | Deferred    | Git tranche's largest candidate; C5 depends on it, so both move together.                                          |
| C5  | "**The same range-boundary hazard bites source:**"                                   | A source deletion's end comes from member boundaries, and surviving KDoc is checked                                | member-boundary derivation; KDoc attachment check; the compile/orphan contrast                                                                                              |        −270 |        604 | Deferred    | Git tranche; two sentences, and it refers to C4's boundary.                                                        |
| C6  | "**Never chain an edit to a commit…**"                                               | Gate a commit on content, not on an edit command's exit status                                                     | staged-set inspection; read the content; anchor freshness; occurrence-count assert; `openspec-apply-change`; batch-write recovery                                           |      −1,004 |      1,371 | Deferred    | Git tranche; three execution modes preserved.                                                                      |
| C7  | "**A two-way split of an enumerated set can also sum to its total…**"                | Derive a declared set's members from its declaration; the same for what a check runs and what a chart renders      | declared-set derivation; the three examples of set; update every enumeration; the `commandLine` rule; the selector rule; no totals in durable artifacts                     |        −529 |        792 | Adopted     | Documentation tranche: one subsection, one review, one record.                                                     |
| C8  | "**`workflowLint` runs `actionlint` alone**"                                         | A tool-set list must not read as complete while naming a subset; a count's arithmetic is checked against its total | the tool fact and the absent `shellcheck`; derive from the complete set; enumerate in full or name the location only; name a count's referent                               |        −737 |        910 | Adopted     | Documentation tranche; the tool fact stays at root as a live contract.                                             |
| C9  | "**Distinguish a point-in-time count from a durable one:**"                          | Which counts may be stated, where, and in what form                                                                | every branch: change artifacts; in-flight own artifact; claims-table evidence with the boundary; durable artifacts; process counts; review rounds; machine-measured figures |        −722 |        452 | Adopted     | Documentation tranche; a short record absorbs a long incident narrative.                                           |
| C10 | "**A configuration-default change names every test assertion…**"                     | Every surface a default is declared on, and every doc that states it                                               | test grep; per-assertion tasks; `BPE_DEFAULT_*` surface and precedence; new joiners; docs; the plan-list contrast; historical records in past tense; the URL fallback rule  |        −621 |      1,115 | Adopted     | Documentation tranche; five live surfaces stay at root.                                                            |
| C11 | "**Doc claims must match their source and their strength…**"                         | Six checks on a claim's source, strength, scope, subject, qualifiers, and mechanism                                | verbatim quoting; the mechanism check; the criterion-scope rule; grammatical subject; qualifier scope; mechanism-vs-measurement                                             |      −1,317 |      1,617 | Adopted     | Documentation tranche; six checks stay, five incidents move.                                                       |
| C12 | "**A durable artifact may assert only what the repository can evidence…**"           | A claim needs a repository source; a cited source's provenance is checked                                          | evidence search; provenance test; commit-as-claim; conversation fallback; mechanism-not-observation; the dated-record list                                                  |        −614 |        900 | Adopted     | Documentation tranche; the provenance test is the mechanism's own warrant.                                         |
| C13 | "**A claim about a surface outside the repository…**"                                | Run the query before writing and at review; do not pin a live surface's value as evidence                          | run and re-run; URL verification; review as the check; live-lookup evidence limit                                                                                           |        −569 |        908 | Adopted     | Documentation tranche; both directions stay at root.                                                               |
| C14 | "**When a script generates many claims at once…**"                                   | A per-item control, not a spot check, and no hedge tag                                                             | per-item control; the nine-of-ten failure; hedge tags do not repair                                                                                                         |        −407 |        620 | Adopted     | Documentation tranche; the parent's one relocation candidate.                                                      |
| C15 | "**A comparison between two runs or files that differ in more than one dimension…**" | Isolate the variable; read the authoritative policy; a route probe reaches the surface only by that route          | isolate; the policy; the privilege signal; single-experiment caution; disagreement; route probe and the graph command                                                       |      −1,261 |      1,607 | Adopted     | Documentation tranche; three sub-rules and the graph command stay.                                                 |
| C16 | "**A reproduction in an outward-facing artifact is itself part of the claim…**"      | A posted reproduction states version, state, and ordered commands, and its details are re-checked                  | version and state; ordered commands; re-check details; direct exit-code capture; the upstream/drafting counterpart                                                          |        −401 |        644 | Adopted     | Documentation tranche; four requirements stay, one upstream reference inventoried.                                 |
| C17 | "**A "the only X" claim in `AGENTS.md` goes stale…**"                                | Four sweep classes for a change that changes a set                                                                 | the uniqueness grep; the absence sweep; the rename sweep; the enumeration sweep incl. the owning spec's delta; the member grep; non-doc artifacts                           |      −1,091 |      1,296 | Adopted     | Documentation tranche; four sweep classes survive as conditions.                                                   |
|     | **Adopted subtotal (11 rules)**                                                      |                                                                                                                    |                                                                                                                                                                             |  **−8,269** | **10,861** |             |                                                                                                                    |
|     | **Deferred subtotal (6 rules)**                                                      |                                                                                                                    |                                                                                                                                                                             |  **−3,722** |  **5,812** |             |                                                                                                                    |
|     | **All 17**                                                                           |                                                                                                                    |                                                                                                                                                                             | **−11,991** | **16,673** |             |                                                                                                                    |
| C18 | "**A formatter-owned file is re-wrapped…**" (R3)                                     | Locate by line index or verbatim line; the four hazard classes                                                     | its four hazard classes; the action; the marker                                                                                                                             |         −69 |          — | Deferred    | No substantive reason beyond the byte saving; its provenance is already carried by the em-dash record.             |

### C1 — `git stash pop` (parent row 1)

Root text after conversion:

```text
- **`git stash pop` can leave conflict markers after a rebase, or re-apply a stale copy cleanly and silently revert the
  pull.** The commit-only-at-push workflow leaves the change uncommitted and stashes it when a rebase needs a clean
  tree. WHEN a docs file (e.g. `docs/ideas.md`) advances on `main` between the stash and the rebase, the pop can leave
  `<<<<<<<` conflict markers because the stash carries the pre-rebase copy: reconcile the file — take `main`'s copy and
  re-apply the change's idea removal — rather than resolving the markers by hand, since a change branch carries its own
  `docs/ideas.md` removal (see the docs-refresh convention) and cannot simply be reset to `origin/main`. IF the stash
  was taken while the working tree held a stale copy — a pre-pull version of a file whose `HEAD` already carries the
  pull — THEN its pop applies that deletion cleanly and without a conflict, silently reverting the pull; check a staged
  diff's insertions/deletions ratio (`git diff --cached --numstat`) before committing, and treat a large deletion count
  on a file the change only appends to as a truncation, not a tidy-up. Under the commit-only-at-push rule the branch has
  no commits to rebase before its first push, so the stash path arises only after a commit: refresh a pushed branch with
  `gh pr update-branch` rather than a local stash-rebase, and start a capture on its branch rather than in `main`'s
  working tree. captured: capture-stash-stale-copy

  evidence: git-and-repository-workflow
```

Evidence addition:

```markdown
## stale-stash

Rule: **`git stash pop` can leave conflict markers after a rebase, or re-apply a stale copy cleanly and silently revert
the pull.**

The conflict-marker mode surfaced when the ideas-dates fix (PR #64) merged mid-rebase, leaving `<<<<<<<` markers in the
working tree. The silent mode was reproduced both ways: a stash taken while the tree held a pre-pull copy records a diff
that deletes the pull's lines, and popping it onto the same `HEAD` re-applies the deletion cleanly, while popping the
other way — stash first, then `HEAD` advances — makes git run a 3-way merge, so the pull's lines survive or raise a
conflict. Moving a capture started in `main`'s working tree onto its own branch this way left `AGENTS.md` ~380 lines
short, and only the staged diff's size (`+31/-409`, PR #336, closed unmerged) revealed it; a content scan missed it
entirely.

`captured: capture-stash-stale-copy`
```

The lead is broadened to name the second mode, because the original lead named only the conflict markers and an agent
scanning leads would not learn the rule covers a silent truncation. That is a change to the rule's _title_, not its
obligations; the record's `Rule:` key carries the new lead, so the pair stays intact.

### C2 — `git reset --hard` (parent row 2)

```text
- **`git reset --hard` on a branch with uncommitted work discards tracked-file edits and deletes branch-added files.** A
  change branch holds the work uncommitted (per the workflow), so a `git reset --hard origin/main` to "rebase" it
  reverts every tracked-file modification **and deletes the files a commit on that branch added** — the change dir
  included, once it has been committed at a push or a branch-switch checkpoint — while a still-untracked change dir
  survives. Never `reset --hard` a branch carrying uncommitted work: with no local commits, `git reset --soft`/`--mixed`
  to `origin/main` keeps the working tree; with local commits, `git rebase` (or stash → rebase → stash pop, per the
  stash-pop gotcha above) is the way, and `git status` afterwards confirms the diff survived. The same class of mistake
  occurs outside a rebase: `git checkout -- <file>` (or `git restore <file>`) reverts just that file to `HEAD`,
  discarding its uncommitted edits. While a work branch holds uncommitted edits, inspect committed content with
  `git diff`/`git show HEAD:<file>` rather than `checkout --`/`restore`/`reset`.

  evidence: git-and-repository-workflow
```

```markdown
## reset-hard

Rule: **`git reset --hard` on a branch with uncommitted work discards tracked-file edits and deletes branch-added
files.**

The reset reverts every tracked-file modification — `build.gradle.kts`, workflows, and docs among them — before deleting
the files a commit on the branch added. The actionlint change hit this when rebasing onto a `main` that had advanced. A
still-untracked change dir was verified to survive a reset, since the commit-only-at-push rule leaves the whole change
untracked until the first commit, so a pre-push reset loses only tracked-file edits. Outside a rebase, a README change
was lost to a `git checkout -- README.md` run inside an unrelated verification step and recovered only from a backup.
```

### C3 — `openspec archive` (parent row 3)

```text
- **`openspec archive` relocates the change dir with a filesystem move, not `git mv` — nothing is staged, so stage both
  sides — and the archived tree is outside Spotless, so a post-move `spotlessApply` will not normalize it.** The tool
  moves the directory, so git reports the old path deleted and the new path untracked: `git add -A openspec/changes`
  stages the move, whereas staging only the archive directory commits the addition while the original stays tracked — a
  **duplicated** change dir. The same command also writes the delta → main sync into `openspec/specs/**`, a second,
  separate staged output, so stage `openspec/specs` too and inspect the staged set (`git diff --cached --name-only`
  against `git status` — an ` M openspec/specs/...` line is unstaged) before committing; the pre-commit guard refuses a
  staged archive dir whose sync is left unstaged. Format the change-dir markdown **before** the archive, since
  `build.gradle.kts` excludes `openspec/changes/archive/**` from the markdown target. A hand-run `git mv` is a different
  trap: it stages the **already-committed** content, so a file carrying an uncommitted edit (typically the `tasks.md`
  tick) shows `RM`, and a plain `git commit` records the pre-edit content. `openspec archive` also prompts for
  confirmation when it would update specs, and a non-interactive agent shell cannot answer
  (`no answer could be read from stdin`) — run it as `openspec archive <change> --yes`; `--skip-specs` (which would skip
  the sync) and `--no-validate` (which would skip the validation) are not the remedy. captured:
  sync-audit-spec-findings-2026-10-04 (#497) captured: cover-the-included-builds-pins (#594)

  evidence: git-and-repository-workflow
```

```markdown
## archive-move

Rule: **`openspec archive` relocates the change dir with a filesystem move, not `git mv` — nothing is staged, so stage
both sides — and the archived tree is outside Spotless, so a post-move `spotlessApply` will not normalize it.**

A change arrived on `main` with its archive but without the delta → main sync, and the sync was recovered as a follow-up
PR. The move is `fs.rename`/`mv`, which is why git sees a delete and an untracked path rather than a rename. The
`git mv` trap and the non-interactive `--yes` requirement were met in `sync-audit-spec-findings-2026-10-04` (#497) and
`cover-the-included-builds-pins` (#594). The Spotless exclusion is the `openspec/changes/archive/**` entry in
`build.gradle.kts`'s markdown target.

`captured: sync-audit-spec-findings-2026-10-04 (#497)`, `captured: cover-the-included-builds-pins (#594)`
```

### C4 — programmatic edits (parent row 4)

```text
- **Programmatic edits to a structured file — markdown or source — can silently drop or misplace structure; verify at
  the granularity you edited.** After any script-driven move or rewrite of a markdown file, grep for each expected
  heading (or diff the heading list before and after) before reporting done, and for a docs reorder prefer the edit tool
  over a hand-rolled reordering script. The edit tool's anchor is one such granularity: an edit anchored on a
  neighbouring item's line is applied at the neighbour — its uniqueness check is satisfied by any unique line — so
  anchor on the text being changed. An item's end is another: derive it from the list's own structure (a wrapped
  continuation is indented; a `- ` at column zero starts a new item, but a heading at column zero ends the list, since a
  search for the next `- ` otherwise crosses the section boundary) and have any script that ranges over or appends to
  items assert that boundary itself — the marker-placement rule above states what the pre-commit guard enforces — before
  trusting the result. captured: retro-mark-captured-rules captured: add-a-fresh-session-trigger captured:
  optimize-agents-auditor-for-instruction-budget

  evidence: git-and-repository-workflow
```

```markdown
## programmatic-edits

Rule: **Programmatic edits to a structured file — markdown or source — can silently drop or misplace structure; verify
at the granularity you edited.**

Reordering the README with a Python boundary script dropped the entire "Getting Started" section: the move used
`lines[ends['Development Workflow']:]`, which starts at the _next_ section and skips the block sitting between the two,
and only a follow-up grep for `## Getting Started` caught the loss.

The item-granularity mode came from the `retro-mark-captured-rules` provenance sweep, which derived each rule's line
range with a boundary heuristic that treated a top-level plain `- Text` bullet directly after a bold-lead rule as the
rule's continuation. At the time, three such adjacencies existed in `AGENTS.md` (`A buildpack pin is verified…`,
`palantir-java-format does not manage imports`, `A dependency a module does not use…`), and the second rule's range ran
through the eight plain bullets behind it — so both the `git log -L` range an origin was read from and the line the
`captured:` marker was appended to belonged to the wrong item, and the marker landed on the following bullet.

`captured: retro-mark-captured-rules`, `captured: add-a-fresh-session-trigger`,
`captured: optimize-agents-auditor-for-instruction-budget`
```

### C5 — the source range-boundary (parent row 5)

```text
- **The same range-boundary hazard bites source: derive a deletion's end from the member boundaries, and after removing
  members confirm each surviving KDoc still attaches to a declaration** — a compile catches the lost brace, never the
  orphan. captured: test-build-logic-rules-and-unify-version-comparison (#306)

  evidence: git-and-repository-workflow
```

```markdown
## source-range-boundary

Rule: **The same range-boundary hazard bites source: derive a deletion's end from the member boundaries, and after
removing members confirm each surviving KDoc still attaches to a declaration**

Removing three tasks' private comparators with a range running from the first deleted member to the end of the file took
each class's **closing brace** — the members were last in the body — failing three compiles, and orphaned a KDoc above
the deleted region, which compiled clean and only a reviewer saw.

`captured: test-build-logic-rules-and-unify-version-comparison (#306)`
```

### C6 — never chain an edit to a commit (parent row 13)

```text
- **Never chain an edit to a commit — or trust an edit script's own report — without reading the edit's result: gate the
  commit on a content check, not on the edit command's exit status.** Inspect the staged set before committing (the
  `git add <dir>` gotcha above owns that files check) and read the diff's _content_, not just its file list, since a
  names-only check cannot see an edit that no-opped. Take an anchor from a read that follows `spotlessApply`, or re-read
  the file before reusing a saved one, since an anchor read before a formatter run is stale once the formatter rewrites
  the file; make a scripted replacement fail loud on a stale anchor — assert the anchor's occurrence count before
  replacing, since `str.replace` returns the input unchanged when it does not match. A multi-file change belongs in the
  `openspec-apply-change` apply workflow rather than an ad-hoc edit script, because its per-task edits make a skipped
  file visible and a script's "edited" line is no per-file evidence. A script that batches its edits and writes once is
  a third mode: an assert that aborts on a later edit discards every earlier edit, because none reached the single write
  — so write each edit as it succeeds (or split the batch into per-edit scripts) and verify the batch's full result
  afterward. captured: make-captured-rules-traceable (#279) captured: track-review-session-continuity

  evidence: git-and-repository-workflow
```

```markdown
## chained-edit

Rule: **Never chain an edit to a commit — or trust an edit script's own report — without reading the edit's result: gate
the commit on a content check, not on the edit command's exit status.**

While implementing the `concise-agent-reports` change, an anchor assertion in the edit script failed because Spotless
had re-wrapped the text (a formatter run rewrites `*italic*` to `_italic_`, rewrapping), so the edit no-opped — and the
next command in the shell sequence committed anyway. The unfixed state was caught only by reading `git show --stat`, not
by the commit message or the exit codes, since an assertion that fails does not by itself stop a following command
unless the chain is gated.

The same change showed the multi-file mode: hand-editing six agent definitions with `python` `replace` scripts left five
of six files edited while `architecture-auditor.md` was untouched and still reported edited, and duplicated a line in
`lesson-capture.md`. The batch-write mode is the `retro-mark-captured-rules` `tasks.md` script: it applied 11 checkbox
ticks and two rewording replacements, hit a stale anchor on the last, and `sys.exit(1)`'d before its one `write_text`,
so all 11 ticks were lost; only a later review caught the file still unticked.

`captured: make-captured-rules-traceable (#279)`, `captured: track-review-session-continuity`
```

### C7 — a two-way split of an enumerated set (parent row 16)

```text
- **A two-way split of an enumerated set can also sum to its total and still be wrong when its boundary is hand-drawn:**
  for a gated-versus-excluded split, or any enumeration or partition of a set the repository declares — the root `check`
  task's members, a CLI's modes, the tracked executables (`git ls-files -s | awk '$1=="100755"'`) — derive the members
  from that declaration (`build.gradle.kts`'s `check` block, its `target`/`targetExclude` entries, the `git ls-files`
  query), never from intent or a hand-written complement, and update every doc that enumerates the set when a change
  adds a member. A claim about what a gate, task, or check _runs_ is the same class: derive it from that tool's own
  definition — its task registration and `commandLine` — not from recall, and a claim about what a chart renders
  likewise from the selector the template emits rather than its comments, which can contradict it. Never write the
  resulting totals into a durable artifact — they move with every change the corpus gains. captured:
  reconcile-netpol-spec-peer-wording

  evidence: documentation-and-durable-claims
```

```markdown
## partition-boundary

Rule: **A two-way split of an enumerated set can also sum to its total and still be wrong when its boundary is
hand-drawn:**

A scan of the markdown target's completeness re-listed exclusions from memory and counted the vendored `axon4to5-*` and
generated `openspec-*` skill files as gated, while the target's own `target(...)` / `targetExclude(...)` entries put
them on the excluded side (the project-authored skills stay gated) — and both splits summed to the same total, so the
arithmetic check could not catch it.

The chart mode came from the netpol templates, which comment the same release-wide `common.labels.matchLabels` selector
as both "from other pods in the release" and "selected peers"/"cluster peers".

`captured: reconcile-netpol-spec-peer-wording`
```

### C8 — `workflowLint` runs `actionlint` alone (parent row 17)

```text
- **`workflowLint` runs `actionlint` alone** (the repository installs and documents no `shellcheck`; actionlint invokes
  it only if it happens to be on `PATH`). **A list naming a tool or check set must not read as complete while naming a
  subset, and a claim patched at the instance is still wrong one level down:** derive the claim from the complete set,
  and either enumerate a set in full or name the location only. Name a count's referent too, and check its arithmetic
  against the total it belongs to.

  evidence: documentation-and-durable-claims
```

```markdown
## lint-set

Rule: **`workflowLint` runs `actionlint` alone**

Two artifacts asserted "actionlint + shellcheck": the `add-actionlint-gate` proposal, which named a spec scenario, and a
later change's verification story.

The "one level down" mode came from a placement rule first drafted as "executables live in `scripts/`": it was corrected
for the repo-root wrappers, then re-broken by the module-local pair, while the sibling clauses of the same rule
enumerated 2/2 and 3/3 so its `scripts/` colon-list (5 of 8) read as exhaustive.

The count mode came from an early draft of the `make-captured-rules-traceable` design, which wrote "46 of them
capture-class, and those 65 are concentrated in Gotchas (41) and Conventions (21)" — 41 + 21 = 62, so the split could
not belong to the 46; it was the 65-bullet set the same sentence also named, and the mismatch was invisible until the
review did the addition.
```

This rule carries no `captured:` marker today. The conversion adds none rather than inventing provenance; its record's
episodes cite their changes by name.

### C9 — point-in-time versus durable counts (parent row 18)

```text
- **Distinguish a point-in-time count from a durable one:** an exact count of a growing corpus (capability specs,
  requirements, archived changes) belongs in a change's own artifacts — except when the counted artifact is the change's
  own and grows while it is in flight (its review-round record, its appended rows), where a checked task certifying "the
  first N rows" is falsified by the next row, so state the mechanism with no count — unless the count is the artifact's
  evidence (a claims-table row counting a live corpus), where the deriving command names the boundary that stabilizes
  it, excluding the unit's own rows (`$2 != "<unit>"`), or the figure moves every time the unit appends one. Never put a
  corpus count in a durable artifact (a subagent definition, `AGENTS.md`, the README, a command file, a main spec): it
  drifts on every archive, exactly the drift the `specs-auditor` exists to catch, so describe the shape instead of
  freezing a tally. A process count — elapsed time, effort — is not durable either, since no reader can verify it from
  the repository, so describe it qualitatively; review rounds are the exception, cited from `docs/review-rounds.tsv`
  (aggregated by `scripts/review-rounds.py`) rather than from a session-only number. A _machine-measured_ figure is a
  different class, evidenced by its CI run log, so an order-of-magnitude build cost is not a process count and need not
  be made qualitative.

  evidence: documentation-and-durable-claims
```

```markdown
## point-in-time-count

Rule: **Distinguish a point-in-time count from a durable one:**

The `add-specs-auditor-agent` proposal said the corpus held "157 requirements"; the change's own delta made the main
spec hold 158, and the README still stated a hard "22 capability specs". The `specs-auditor` definition's "a corpus …
that grows with every archived change" and the README's "120+ and counting" are the shape-describing form the rule asks
for.
```

### C10 — a configuration-default change (parent row 22)

```text
- **A configuration-default change names every test assertion that pins the value and every doc that describes it, on
  each surface it is declared.** Grep the test sources for the field accessor when changing a default, and give each
  pinning assertion its own task — the Java-defaults test and the yml-wiring test each bind a different surface. A
  variable carried in a service's `bootBuildImage` `BPE_DEFAULT_*` map has one surface more: the image's
  launch-environment default (`paketo-buildpacks/environment-variables`), which a deployment's env var overrides but
  which in turn overrides the yml fallback. A new property the image carries joins that map too, its value decided from
  the map's closest sibling there — not as a diff against the Java default — because the map is the image's declared
  deployment environment. Documents that describe the value are surfaces too: grep the docs for it and correct every
  description — a doc's sites come from that grep, not from the plan's list — but state the values a **historical
  record** (a baseline, a retro, an audit) ran under in the past tense rather than rewriting it to the new ones, since
  its measured data is what it records. A service URL's yml fallback is overridden by the container path
  (`BPE_DEFAULT_*`; in-container every service listens on 8080), so it is exercised only under host `bootRun` and must
  equal the target service's local `server.port` — a component test asserting the placeholder literally pins whatever
  value is there, not the right one. captured: fix-gateway-cors-allowed-headers (#359) captured:
  right-size-chart-resources captured: fix-gateway-query-service-url captured: guard-archive-spec-sync

  evidence: documentation-and-durable-claims
```

```markdown
## config-default-surfaces

Rule: **A configuration-default change names every test assertion that pins the value and every doc that describes it,
on each surface it is declared.**

The `reconcile-showcase-cache-default` plan initially missed that `allPropertiesHaveDocumentedDefaults` asserts the Java
field, so the change would have failed it, and that the yml-wiring test's missing `showcaseCache` assertion was the gap
which let the drift pass.

For the image's declared deployment environment, the keep-alive mirrors its own default and the origins bake the empty
fail-closed override of a local-development default. `right-size-chart-resources` changed the chart defaults: the Target
bullet and the Result paragraph of `docs/load-tests/2026-09-26.md` both described the old ones in the present tense, the
plan enumerated only the Target bullet, and review caught the Result paragraph. The URL mode was
`fix-gateway-query-service-url`.

`captured: fix-gateway-cors-allowed-headers (#359)`, `captured: right-size-chart-resources`,
`captured: fix-gateway-query-service-url`, `captured: guard-archive-spec-sync`
```

### C11 — doc claims must match their source and strength (parent row 23)

```text
- **Doc claims must match their source and their strength — quote verbatim or paraphrase explicitly, and reserve
  "enforced" for a real gate.** When a doc quotes a rule, spec, or comment, copy the exact text (or drop the quote marks
  and describe it), and check the mechanism before using words like "enforced", "always", "never", or "every" — against
  the variants other docs define too, since an unconditional rule must hold on every documented path, not only the
  default one. One of those paths is the rule's own text: state a formal criterion — a count, a delimiter balance, a
  uniqueness test — with its scope where the criterion is stated, not only in the rule's opening sentence, so a literal
  reading cannot apply it to the rule itself (the `Keep em-dashes paired…` rule scopes its odd-count check to a sentence
  that already uses paired dashes). Check the claim's grammatical subject too, not only its wording, since a pronoun can
  bind a claim to the wrong actor; and check a qualifier's scope, because a condition or exception introduced for one
  variant must not swallow the rule's primary mandate — strip the qualifiers and confirm the imperative verb still
  governs the default case. Check a recorded _mechanism_ against every measurement the change made, naming the evidence
  points the explanation must cover and confirming the wording holds for each, because a generalization one of your own
  runs contradicts is wrong however well it reads. captured: record-blocking-execution-rationale (#335) captured:
  grpc-query-transport

  evidence: documentation-and-durable-claims
```

```markdown
## claim-strength

Rule: **Doc claims must match their source and their strength — quote verbatim or paraphrase explicitly, and reserve
"enforced" for a real gate.**

The self-learning README section described `AGENTS.md` rules in quotes; `/review-thorough` caught a reworded rule
rendered as a verbatim quote, an "enforced" that no gate backs, and an overstated process claim.

The variant mode: the README's proposal-stage draft PR is a documented variant of the branch rules. The subject mode: a
README draft closed with "… and a practice of reporting gaps in the libraries and tools _it_ depends on", which bound
the project's upstream-report practice to OpenCode — the bullet's subject — rather than to the project; naming the
subject fixed it. The qualifier mode: the `reshape-the-capture-loop` rewrite of the capture trigger lost its lead's main
verb and folded the implementation capture under the merge condition; the PR's test plan records the review catching it.
The mechanism mode: the blocking-execution mechanism's evidence point is the gateway's not-found test passing (its
`fetchById` is stubbed, so the handler maps the error directly), while the query-service's mirror (`404` → `503`) went
with its HTTP controller (`grpc-query-transport`), so a second evidence point had to be re-derived rather than cited
from memory; both the surviving evidence point and the removed one are recorded in `design.md` of
`extract-specd-rationale-from-agents-md`, which moved the rule into the specs it belongs to.

`captured: record-blocking-execution-rationale (#335)`, `captured: grpc-query-transport`
```

The `Keep em-dashes paired…` instance stays at root: it names a rule in the same file, and the action it teaches — scope
a formal criterion where the criterion is stated — is unreadable without a concrete criterion whose scope is in
question.

### C12 — a durable artifact asserts only what the repository can evidence (parent row 24)

```text
- **A durable artifact may assert only what the repository can evidence — a history that lives only in the conversation
  is not repo history.** Before writing a historical or behavioral claim into `AGENTS.md`, `README.md`, a subagent
  definition, or a main spec, find its evidence — a config file, a log line, a commit, or a run whose output you have.
  Check a cited source's provenance too, not only its existence: a clause written _for_ an event cannot evidence a
  _prior_ occurrence of it, so before a recurrence claim take a source that predates the event and was not produced by
  it (`git blame`/`git log -S` the clause, or grep the rule's `captured:` token to find its introducing commit). A
  commit cited as evidence is itself a claim: read what it did (`git show --stat`) and whether the path existed at
  `<commit>^` before attributing lines or a reflow to it. If the only source is the conversation, omit it or label it as
  the owner's account; for an outcome you did not observe, state the mechanism ("a bash-script write runs under the
  `shell` permission") rather than the observation ("it did not prompt"). Point-in-time narrative belongs in a dated
  record — the change archive, a retrospective, an audit report, a load-test record, or a rule's
  `docs/guidance-evidence/` entry — never in the guidance itself. captured: store-2026-10-05-retrospective

  evidence: documentation-and-durable-claims
```

```markdown
## evidence-provenance

Rule: **A durable artifact may assert only what the repository can evidence — a history that lives only in the
conversation is not repo history.**

An earlier draft of this bullet cited a `/var/folders/**` config attempt — a pattern proposed in conversation but never
written to a config file — and asserted an unobserved `setup-hosts.sh` outcome; a review pass caught both.

The provenance mode: a retrospective draft cited the `openspec archive` gotcha's parenthetical as a second
lost-spec-sync occurrence, but the incident's own capture #498 had added it. The commit mode: an early draft of the
`make-captured-rules-traceable` artifacts called `c62feda` one of "two Spotless commits" that reflowed `AGENTS.md`, but
`c62feda` created the file (164 insertions, and `AGENTS.md` is absent at its parent); a review caught it.

`captured: store-2026-10-05-retrospective`
```

The marker currently sits mid-sentence in this rule; the draft moves it to the end of the rule block, which is what the
marker-placement rule requires.

### C13 — a claim about a surface outside the repository (parent row 25)

```text
- **A claim about a surface outside the repository — a registry's contents, a repository setting, a live URL — has no
  config file or commit behind it, so its evidence is a query whose output you have:** run it before writing the claim,
  and re-run it at review, because such a surface can change between the two and no gate reads it. Verify a URL by
  requesting it, and treat the review, not a gate, as the check. An evidence statement for a task that _touches_ such a
  surface has the same limit in the other direction: do not pin a value the surface produced as the evidence the task
  was done, since the rows of a live lookup vary run to run. captured: rework-the-helm-suppression-list

  evidence: documentation-and-durable-claims
```

```markdown
## external-surface

Rule: **A claim about a surface outside the repository — a registry's contents, a repository setting, a live URL — has
no config file or commit behind it, so its evidence is a query whose output you have:**

`#266`'s repeated review rounds caught several unverified external assertions — a GitHub docs URL written from memory, a
false "Docker Hub carries nothing" against five live repositories, wrong dates; the owner deleted those repositories
mid-session, invalidating the entry between its writing and its review.

The live-lookup mode: `helmUpdates`'s rows are a live `helm search repo` result; the same tick's evidence read "only
`prometheus-community-stack`" in one run and also showed `bitnami-kafka: 31.5.0 -> 32.4.3` in another, so the tick rests
on the run succeeding plus the parse pinned by unit tests, never on any row.

`captured: rework-the-helm-suppression-list`
```

### C14 — a script's many generated claims (parent row 26, the relocation)

```text
- **When a script generates many claims at once, the method that derived them is not their evidence:** each generated
  item needs its own control, applied per item rather than as a spot check, because a heuristic that is right on nine of
  ten items writes its one error silently into the durable file and a hedge tag (`approximate`, "uncertain") does not
  repair it. captured: retro-mark-captured-rules

  evidence: documentation-and-durable-claims
```

```markdown
## per-item-control

Rule: **When a script generates many claims at once, the method that derived them is not their evidence:**

The `retro-mark-captured-rules` backfill's first attempt named 8 origins whose commit contained no text of the rule at
all — including `ddaa51e`, claimed for the log-assertion rule whose text it never mentions — because the range and the
phrase were never required to be about the same rule. Adding a per-item control — the candidate origin's added lines had
to contain the rule's own opening phrase — substantiated 30+ origins independently.

`captured: retro-mark-captured-rules`
```

### C15 — a comparison that differs in more than one dimension (parent row 27)

```text
- **A comparison between two runs or files that differ in more than one dimension cannot attribute the difference to
  either — isolate the variable before naming a cause.** Read the authoritative policy for the mechanism you are
  hypothesising, or vary only that dimension; a fix that enlarges a privilege to explain a behavior is a signal the
  cause is still undiagnosed. The same rule binds a single experiment that varies two dimensions at once, and a
  disagreement between two runs that differ in an uncontrolled dimension refutes neither — compare the inputs before
  concluding either wrong. It extends to a single observation: a token used to evidence which route supplies a surface
  must reach it only by that route — read the route the graph prints
  (`./gradlew :<module>:dependencies --configuration <suite>CompileClasspath`, whose tree names each route's parent), or
  pick a token with one route. captured: guard-agent-loader-read-path captured: fix-mapstruct-suite-warning

  evidence: documentation-and-durable-claims
```

```markdown
## single-variable

Rule: **A comparison between two runs or files that differ in more than one dimension cannot attribute the difference to
either — isolate the variable before naming a cause.**

Comparing `ci.yml`'s `Cache mode: write` with the `opencode` workflow's read-only run attributed the difference to the
latter's `permissions:` block, but the two differ in two ways — the trigger (`push`/`pull_request` vs the comment
triggers) and the blocks' contents (`ci.yml`: a top-level `contents: read`; `opencode.yml`: a job-level
`id-token: write` plus reads) — and a grep anchored to an indented `permissions:` had hidden `ci.yml`'s top-level one.
GitHub's dependency-caching reference names the trigger, so a fix built on the misattribution (granting
`actions: write`) would have widened the token for nothing.

The two-dimension mode: probing one malformed frontmatter on an agent and a different one on a command read the results
as kind-keyed ("command → exit 1") when the status depends on the resolved YAML shape, not the kind —
`guard-agent-loader-read-path` fixed the design only by re-deriving at a held-constant shape; a later review round then
could not reproduce the silent-drop row because it chose a different shape, and read its clean run as a refutation of a
true fact. The route mode: a `commons-lang3` probe of `main`'s `implementation` on a custom suite's compile classpath
"confirmed" the opposite, because the coordinate arrived through the suite's declared `project(":showcase-test")`.

`captured: guard-agent-loader-read-path`, `captured: fix-mapstruct-suite-warning`
```

### C16 — a reproduction in an outward-facing artifact (parent row 28)

```text
- **A reproduction in an outward-facing artifact is itself part of the claim — write it so a reader reruns it to the
  same output, and rerun the exact sequence before posting.** State the tool version and the starting state, and record
  the commands in the order they ran, since an order-dependent transcript can self-contradict and a precondition left
  implicit is not reproducible elsewhere. Re-check every derived detail — line numbers, exit codes — against the actual
  output, and capture an exit code directly, not through a pipe (`cmd | jq` reports `jq`'s status, not the CLI's). This
  is the drafting counterpart to "report it upstream with a reproduction and the evidence"; the tool-behavior claim
  itself is verified per the `--help`/read-path rules.

  evidence: documentation-and-durable-claims
```

```markdown
## reproduction

Rule: **A reproduction in an outward-facing artifact is itself part of the claim — write it so a reader reruns it to the
same output, and rerun the exact sequence before posting.**

A `Fission-AI/OpenSpec#1892` draft showed `openspec validate --all` exiting 0, but the delta-less change its own
`new change` had created makes `validate` exit 1; in a directory with no planning shape the same malformed config
hard-errors with exit 1 instead of warning. The `#1892` draft took repeated review passes, each catching a different
mis-reproduction, because the review gate reads the artifact while no gate reruns its transcript.
```

This rule carries no `captured:` marker today; the conversion adds none.

### C17 — a "the only X" claim (parent row 30)

```text
- **A "the only X" claim in `AGENTS.md` goes stale the moment a change adds a second X — fix it in that change.** When a
  change adds a second instance of anything the docs call unique (a second e2e suite, image, or workflow), grep
  `AGENTS.md` for `only`/`sole`/`never` claims about the first and update them, and when editing a section, re-verify
  such claims against the repo instead of trusting the prose. The negative case takes the same sweep: when a change
  makes a previously-true "nothing covers X" / "A and B ignore X" statement false, grep `AGENTS.md`/`README.md` for the
  capability's absence claims and fix them in the same change, including one a recent change added. A **code-symbol or
  file rename or removal** does too — docs cite class, test, config, and workflow-file names as examples and nothing
  resolves them — so grep the whole repository for the old name in the same change. A plain **enumeration of a set** is
  the case the name-grep cannot see: adding a **member** widens every live enumeration of it in the same change, and
  when an auditor sweeps the set that includes its definition, its trigger command, `AGENTS.md`'s summary of the
  auditor, and the **spec requirement that owns the sweep**, which is a main spec and so needs a `MODIFIED` delta rather
  than a docs edit. Grep the set's distinctive members across `.opencode/`, `openspec/specs/`, `AGENTS.md`, `README.md`,
  `docs/ideas.md`, and `scripts/` before writing the addition — a tool's own module docstring and its `--help` text are
  live enumerations of the same set too, and the change's own planning copies can be kept identical while every live
  site goes stale. The sweep covers non-doc artifacts too: recording that a component is deliberately _not_ used must
  grep the whole repo for its name, because config, values, and template comments carry claims no auditor reads.
  captured: monitor-web-ui-npm-dependencies (#395) captured: verify-unique-review-rounds

  evidence: documentation-and-durable-claims
```

```markdown
## uniqueness-claims

Rule: **A "the only X" claim in `AGENTS.md` goes stale the moment a change adds a second X — fix it in that change.**

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

`captured: monitor-web-ui-npm-dependencies (#395)`, `captured: verify-unique-review-rounds`
```

### C18 — the formatter-owned file, deferred (parent row 6)

The parent's draft, restated here so the deferral is a decision rather than an omission:

```text
- **A formatter-owned file is re-wrapped, so a whitespace-tolerant substitution — a normalization or `\s*`-style pattern
  — silently matches the wrong span or rebuilds it unformatted.** Locate the target by line index in the content you
  just read, or match one full line verbatim including its indentation, and re-read the result. The hazard classes are
  an indentation-sensitive block (a YAML step), an anchor whose occurrence count you did not assert, a line-length
  limit, and prose whose wrapping the formatter owns. captured: notify-owner-from-the-audit-report (#327)

  evidence: git-and-repository-workflow
```

Its net is −69 B, its four hazard classes are the rule's scope rather than an episode, and its `captured:` provenance
(`notify-owner-from-the-audit-report (#327)`) is already carried by the em-dash record's `paragraph-boundary` section. A
record section for it would restate the hazard list it keeps at root. **No substantive reason beyond the byte saving has
emerged, so it stays deferred.**

## Boundary verification

### The five conditions, per candidate

These are **reasoned validations of the drafts against the current rules**, not observations of an agent's behaviour.
Condition 4 was run as a behavioural probe after implementation (below); the other four remain reasoned, and tasks
4.1–4.3 and 4.5 record the per-rule check.

| ID  | 1. Motivating incident still yields the action           | 2. A neighbouring case is prevented       | 3. A legitimate case is allowed        | 4. A task naming no destination acts                           | 5. A reviewer retrieves the record                           | Note                                              |
| --- | -------------------------------------------------------- | ----------------------------------------- | -------------------------------------- | -------------------------------------------------------------- | ------------------------------------------------------------ | ------------------------------------------------- |
| C1  | ✔ `#64` conflict → reconcile from `main`                 | a different docs file advancing           | a clean pop with no staleness          | ✔ both modes and the `--numstat` check at root                 | ✔ `stale-stash` holds `#64`, `#336`, `+31/-409`              | lead broadened; the reason is in the draft's note |
| C2  | ✔ the actionlint rebase → the ladder                     | a `git checkout --` on any file           | a reset on a branch with no work       | ✔ ladder and confirmation at root                              | ✔ `reset-hard` holds the episode and the enumeration         | —                                                 |
| C3  | ✔ the lost-sync change → stage both sides                | a hand-run `git mv` or a prompt           | an archive with no spec change         | ✔ staging, inspection, `--yes` at root                         | ✔ `archive-move` holds `fs.rename`, `#497`, `#594`           | —                                                 |
| C4  | ✔ the dropped README section → heading grep              | a script appending a list item            | a source-file edit with no list        | ✔ three granularities at root                                  | ✔ `programmatic-edits` holds the three episodes              | —                                                 |
| C5  | ✔ the three-comparator delete → member boundary          | a KDoc left orphaned                      | a deletion that ends at a member       | ✔ the rule and the contrast at root                            | ✔ `source-range-boundary` holds the episode                  | —                                                 |
| C6  | ✔ the no-opped edit → content gate                       | a stale anchor or a batch script          | a single edit that succeeded           | ✔ three modes at root                                          | ✔ `chained-edit` holds `concise-agent-reports`, the 11 ticks | —                                                 |
| C7  | ✔ the markdown-target scan → derive from the declaration | a `check`-member or CLI-mode split        | a set with no partition                | ✔ the three set classes at root                                | ✔ `partition-boundary` holds the episode and the chart case  | —                                                 |
| C8  | ✔ the "actionlint + shellcheck" artifacts                | a placement list naming a subset          | a single-tool claim with no list       | ✔ both rules at root                                           | ✔ `lint-set` holds both episodes                             | the tool fact and the two decision rules stay     |
| C9  | ✔ the 157/158 corpus count                               | an in-flight review-round count           | a claims-table count with a boundary   | ✔ every branch at root                                         | ✔ `point-in-time-count` holds the incidents                  | the longest chain; every branch survives          |
| C10 | ✔ the missed `allPropertiesHaveDocumentedDefaults`       | a new `BPE_DEFAULT_*` property            | a value with no image surface          | ✔ every surface and the URL rule at root                       | ✔ `config-default-surfaces` holds the episodes               | —                                                 |
| C11 | ✔ the reworded-quote review                              | a pronoun bound to the wrong actor        | a quote copied verbatim                | ✔ six checks at root                                           | ✔ `claim-strength` holds all five episodes                   | the in-file em-dash instance stays                |
| C12 | ✔ the `/var/folders/**` draft and `c62feda`              | a recurrence claim sourced from the event | an owner-labelled account              | ✔ evidence, provenance, commit, fallback, dated record at root | ✔ `evidence-provenance` holds all three episodes             | marker moves to the rule's end                    |
| C13 | ✔ the `#266` review rounds                               | a task's evidence pinned to a live row    | a URL verified by request              | ✔ run/re-run, URL check, evidence limit at root                | ✔ `external-surface` holds both episodes                     | —                                                 |
| C14 | ✔ the 8 wrong origins                                    | any generated-claim sweep                 | a single hand-derived claim            | ✔ per-item control at root                                     | ✔ `per-item-control` holds `ddaa51e`                         | —                                                 |
| C15 | ✔ the cache-mode misattribution                          | a two-dimension probe; a route probe      | a comparison that varies one dimension | ✔ every branch at root                                         | ✔ `single-variable` holds all three episodes                 | —                                                 |
| C16 | ✔ the `#1892` transcript                                 | a second posted reproduction              | a quote of someone else's run          | ✔ version, state, order, re-check at root                      | ✔ `reproduction` holds `#1892`, `new change`                 | the upstream reference stays scanned elsewhere    |
| C17 | ✔ the Playwright e2e "only" claim                        | a rename and an enumeration member        | a uniqueness claim that still holds    | ✔ four sweep classes at root                                   | ✔ `uniqueness-claims` holds all five episodes                | —                                                 |

**Condition 3 is the one that could fail silently.** A draft that keeps every condition but drops an exception would let
a legitimate case be blocked. Checked per rule: C1 keeps the window where a pop is safe; C2 keeps a reset on a clean
branch legal by scoping the prohibition to uncommitted work; C3 keeps an archive with no spec change valid; C4 does not
forbid a script, only requires verification; C5 scopes to deletions and to KDoc above a deleted region; C6 allows an
ad-hoc script where its result is read; C7 keeps a set with no partition; C8 keeps a single-tool claim; C9 keeps a
claims-table count with a stated boundary; C10 keeps a value with no image surface; C11 accepts a verbatim quote; C12
accepts an owner-labelled account; C13 accepts a URL verified by request; C14 leaves a hand-derived claim alone; C15
accepts a single-dimension comparison; C16 accepts a quoted third-party run; C17 accepts a uniqueness claim that is
still true.

### Condition 4, observed

Condition 4 asks whether an agent holding `AGENTS.md` alone takes the governed action. It was run as a behavioural probe
in a fresh session (`ses_ea6783cbeffe3F02H7U77z8reN`) against the implemented text: the session read a copy of the
implemented `AGENTS.md` (`/private/var/…/behavioral/AGENTS.md`, 324,296 B) and was told to treat it as the project's
guidance and not to open `docs/guidance-evidence/` or `openspec/changes/`. It received eleven neutral, realistic tasks —
one per converted rule, with neither the rule nor its token named — and was asked for its **first move**, with read-only
probes allowed and every mutating action prohibited.

| Rule | Task given (abridged)                                                                                      | Observed first action                                                                                                                                                                                                                                                                                                                  | Match |
| ---- | ---------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----- |
| C7   | A remembered list of what root `check` runs is to go into the README                                       | Ignored the list, read the `check` block in `build.gradle.kts`, and would point at it rather than transcribe                                                                                                                                                                                                                           | ✔     |
| C8   | Confirm or correct "the workflow lint runs actionlint and shellcheck"                                      | Read `workflow-lint-conventions.gradle.kts`; found `commandLine(actionlint …)` and no shellcheck                                                                                                                                                                                                                                       | ✔     |
| C9   | One Purpose sentence on how large the spec corpus is                                                       | Counted 26, then wrote **no number**, describing the shape instead                                                                                                                                                                                                                                                                     | ✔     |
| C10  | Change `showcase.events.keepAliveInterval`'s default                                                       | Grepped and enumerated Java field, yml placeholder, `BPE_DEFAULT_*`, chart values, both test assertions, and docs                                                                                                                                                                                                                      | ✔     |
| C11  | Review "the commit guard requires a formatter check, and this is enforced"                                 | Read `pre-commit` and `commit-hygiene.py`; found the hook subset-scoped, opt-in and bypassable, and corrected it                                                                                                                                                                                                                       | ✔     |
| C12  | Add "a teammate saw a CI flake last week" to `AGENTS.md`                                                   | Would not write it; looked for a run, ledger row, or parked idea first, else labelled it the owner's account                                                                                                                                                                                                                           | ✔     |
| C13  | Document that the images publish under `ghcr.io/<owner>/…` and that the registry serves five tags for them | Read `release.yml`'s push loop, found five images with two tags each per release, judged the sentence a second copy, and would query the registry per package before writing and re-run that query at review — refusing the local daemon, the existing prose and a failed lookup as evidence, and any live tally in a durable artifact | ✔     |
| C14  | Validate 40 script-guessed `captured:` markers                                                             | Established the guard checks only placement, then ran one `git log -S` per-item control and proposed repeating it                                                                                                                                                                                                                      | ✔     |
| C15  | Conclude that a permissions block caused a 40% slowdown                                                    | Refused the attribution (two dimensions) and proposed a controlled A/B                                                                                                                                                                                                                                                                 | ✔     |
| C16  | Post an upstream bug reproduction                                                                          | Listed version, starting state, ordered commands, exact output, a direct exit code, and review before posting                                                                                                                                                                                                                          | ✔     |
| C17  | Add a second e2e suite to a "the only e2e suite" docs claim                                                | Swept `only`/`sole`, then widened every live enumeration and flagged the owning spec's `MODIFIED` delta                                                                                                                                                                                                                                | ✔     |

**Result: 11 of 11 matched; no defect was found**, so no rule text, record section, or measured figure changed. The
probe's limitations are recorded rather than glossed:

- It is **one session answering eleven tasks**, not eleven independent sessions, so cross-task priming cannot be
  excluded; the tasks are unrelated and were answered in order.
- The "guidance alone" condition rests on the instruction: the session had repository access and used it for read-only
  probes, and it reports reading the guidance copy, but nothing enforces that it never opened a record.
- The probe verifies the **first move**, not a completed outcome: mutating actions were prohibited for reversibility, so
  each observed action is a stated plan plus the reads it actually performed.
- Condition 4 is the only condition observed this way; 1–3 and 5 remain reasoned or mechanically checked, and the report
  says which is which.
- **The tasks were authored by this change's implementer**, so task-design bias is not excluded; review caught one
  mis-framed scenario (C13's first version asked for a version the catalog owns, which exercises the documented-numbers
  rule rather than the external-surface one), and it was corrected and re-run in the same probe session — which makes
  that row the most primed of the eleven, since the session had already discussed the mis-framing when it answered.

### The identifier inventory, mechanically checked

For each candidate, every backticked span in the replaced text was searched in the draft root text and then in the
record section. **Zero spans are missing from both** across all 17 candidates. Run it on the pre-change file, as the
section's script takes it as its second argument
(`git show HEAD:AGENTS.md > /tmp/pre.md && python3 <script> <this design.md> /tmp/pre.md`): on the implemented file the
rules _are_ the drafts, so the check would compare each span with itself and pass vacuously.

````text
import re, sys
design = open(sys.argv[1], encoding="utf-8").read()      # this change's design.md
agents = sys.argv[2] if len(sys.argv) > 2 else "AGENTS.md"
raw = open(agents, encoding="utf-8").read(); lines = raw.split("\n")
norm = lambda s: re.sub(r"\s+", " ", s).strip()
anchors = dict(re.findall(r"^\| (C\d+) +\| \"\*\*(.+?)\*\*\" +\|", design, flags=re.M))
drafts = dict(re.findall(r"^### (C\d+)[^\n]*\n\n(?:Root text after conversion:\n\n)?```text\n(.*?)\n```",
                        design, flags=re.S | re.M))
sections = re.findall(r"^## ([\w-]+)\n\nRule: \*\*(.+?)\*\*\n\n(.*?)(?=\n## |\n```|\Z)", design, flags=re.S | re.M)
records = {norm(lead): body for slug, lead, body in sections}
starts = [i for i, ln in enumerate(lines, 1) if ln.startswith("- ")]
lost_total = 0
for cid in sorted(anchors):
    prefix = anchors[cid].split("…")[0]
    hit = [i for i in starts if lines[i - 1].startswith("- **" + prefix)]
    assert len(hit) == 1, (cid, hit)
    st = hit[0]; nxt = [i for i in starts if i > st]
    body = "\n".join(lines[st - 1:(nxt[0] if nxt else len(lines)) - 1])
    draft = drafts[cid]; after = norm(draft)
    lead = norm(re.match(r"- \*\*(.+?)\*\*", draft, flags=re.S).group(1))
    record = norm(records.get(lead, ""))
    spans = {m.group(1) for ln in body.split("\n") for m in re.finditer(r"`([^`\n]{2,})`", ln)}
    lost = [t for t in spans if norm(t) not in after and norm(t) not in record]
    lost_total += len(lost)
    print("%-4s spans=%2d LOST=%d %s" % (cid, len(spans), len(lost), lost))
print("total lost spans:", lost_total)
````

Its last line prints `total lost spans: 0`.

The spans that moved are the historical identifiers — change names, PR and issue numbers, commit hashes, file and symbol
names, and the failed commands. The spans that stayed are the operational ones the candidates actually carry:
`git diff --cached --numstat`, `git show HEAD:<file>`, `openspec archive <change> --yes`,
`./gradlew :<module>:dependencies --configuration <suite>CompileClasspath`, `docs/review-rounds.tsv`,
`scripts/review-rounds.py`, `$2 != "<unit>"`, `BPE_DEFAULT_*`, `server.port`, `openspec-apply-change`,
`docs/guidance-evidence/`, `git ls-files -s | awk '$1=="100755"'`, `build.gradle.kts`, `target`/`targetExclude`,
`commandLine`, and the four sweep-class locations.

### Where scope had to be stated rather than exemplified

| Candidate | The example that defined scope                             | How the draft states it                                                                   |
| --------- | ---------------------------------------------------------- | ----------------------------------------------------------------------------------------- |
| C1        | "the stash path arises only after a commit" (illustration) | kept as the condition that bounds the hazard, in one clause                               |
| C7        | the markdown target's own exclusions                       | stated as "derive from that declaration … never from intent or a hand-written complement" |
| C8        | the placement rule's `scripts/` colon-list                 | stated as "enumerate a set in full or name the location only"                             |
| C11       | the em-dash rule's own odd-count scope                     | kept as the one in-file instance; the general action is stated first                      |
| C17       | the four incidents each introduced a sweep class           | the four classes are named as conditions, and the incidents move                          |

No candidate failed this test. One draft's lead changed (C1); no other lead changed, so no other rule's title moves.

### What a rewrite must not do, checked

| Risk                                             | Result                                                                                                           |
| ------------------------------------------------ | ---------------------------------------------------------------------------------------------------------------- |
| Turn a `MUST`-strength action into a suggestion  | Checked per draft: "never", "must not", "contains no cache" style obligations keep their strength (C2, C9, C17). |
| Drop an exception or a scope limit               | Condition 3 above, per rule.                                                                                     |
| Leave a condition stated only in the record      | None: each draft's conditions are at root; the records state what happened.                                      |
| Put a second operational instruction in a record | Read per section: bodies are past-tense episode description; no imperative a reader could follow instead.        |
| Rename a rule without updating its key           | Only C1's lead changes, and its record section carries the new lead verbatim.                                    |

## The destination, reused

The parent unit created and staffed the destination; this unit adds sections and changes nothing about its mechanics.

| Element       | This unit's use                                                                                                                                                                                                                                                       |
| ------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| File          | `docs/guidance-evidence/documentation-and-durable-claims.md`, the existing record for the subsection.                                                                                                                                                                 |
| Section order | The rules' order in `AGENTS.md`, so the record reads in the same sequence as the guidance.                                                                                                                                                                            |
| Heading       | A single-line lowercase hyphenated slug: `partition-boundary`, `lint-set`, `point-in-time-count`, `config-default-surfaces`, `claim-strength`, `evidence-provenance`, `external-surface`, `per-item-control`, `single-variable`, `reproduction`, `uniqueness-claims`. |
| `Rule:` key   | The rule's lead, verbatim, whitespace-normalized for the pairing check.                                                                                                                                                                                               |
| Body          | The episode, its historical identifiers, its verification, and its provenance markers; no guidance.                                                                                                                                                                   |
| Ownership     | The agent-tooling audit, which already pairs sections to rules and reports an orphan or a missing section.                                                                                                                                                            |

**Projected record size.** The file is 5,747 bytes today; the eleven sections add 10,861 bytes, so it becomes 16,608
bytes. The Git record is untouched by the recommended unit.

**No header edit is needed.** The record's header already says it holds "the converted rules … one section per rule",
and nothing in it counts sections.

## Reference, gate, and upstream checks

| Check                                     | Result                                                                                                                                                                                                                                                                                                                                                                                                          |
| ----------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Other artifacts quoting a rewritten rule  | One related match, no edit owed: `.opencode/agent/agents-auditor.md:54` lists "a \"the only X\" that is no longer the only one" among its consistency classes. That is the auditor's own description of the check it runs, not a quotation of C17's rule, and it restates none of C17's sweep obligations. Every other distinctive phrase of the eleven leads has no match outside `AGENTS.md` and the records. |
| `openspec/config.yaml` per-artifact rules | Restate claim/count obligations in their own words; no obligation moves, so nothing to refresh.                                                                                                                                                                                                                                                                                                                 |
| Upstream reference moved                  | Only C16 moves `Fission-AI/OpenSpec#1892` out of `AGENTS.md`. It stays cited at line 2994 in "Track the upstream reports…", which owns its retirement condition, so the reference stays in the scanned set. No scanner change.                                                                                                                                                                                  |
| `verifyInstallCommands`                   | No `helm install … --version` command appears in any candidate span.                                                                                                                                                                                                                                                                                                                                            |
| `verifyDocumentedPins`                    | No Paketo version pin appears in a candidate span; C10 names `paketo-buildpacks/environment-variables` without a version, as today.                                                                                                                                                                                                                                                                             |
| `verifyCapturedMarkers`                   | Every draft keeps its markers inside the rule block; C12 **moves** a mid-sentence marker to the rule's end, which is the placement the rule requires.                                                                                                                                                                                                                                                           |
| `verifyPrettierAlignment`                 | The whole candidate file for the recommended unit is formatter-stable at 323,595 bytes (rebuilt, Prettier-formatted, byte-identical to the draft).                                                                                                                                                                                                                                                              |
| `openspec validate`                       | No spec edit, so nothing to validate; the delta question is answered under "Delta determination".                                                                                                                                                                                                                                                                                                               |
| Conflict-marker guard                     | C1's `<<<<<<<` is inside an inline code span, as it already is; the guard matches only line-initial markers.                                                                                                                                                                                                                                                                                                    |

**Overlap between candidates.** C8 and C17 both govern a set: C8 says a list must not claim completeness while naming a
subset and must be derived from its declaration; C17 says adding a member obliges updating every live enumeration. They
are adjacent but distinct decisions — one about a claim's completeness, one about the sweep a set change triggers — and
both stay. C11 and C12 are likewise distinct: claim strength versus claim evidence. No candidate's validity depends on
merging another.

## Measured totals

### Conversion subtotal, recommended unit (11 rules)

| Quantity                              | Value                                                                                                             |
| ------------------------------------- | ----------------------------------------------------------------------------------------------------------------- |
| Root text removed, tokens included    | **8,269 bytes** (2.49% of 331,864)                                                                                |
| Of which `evidence:` token paragraphs | 506 bytes (11 × 46), inside the figure above                                                                      |
| Complete evidence-file additions      | **10,861 bytes** (11 sections, each with its separator)                                                           |
| Net root reduction                    | **−8,269 bytes**                                                                                                  |
| Total maintained-content change       | **+2,592 bytes** (10,861 − 8,269)                                                                                 |
| Additional routinely loaded material  | **506 bytes**, charged inside the root figure                                                                     |
| Necessary supporting edits            | `docs/ideas.md`: the parked entry is rewritten to the deferred Git tranche with these figures (not always-loaded) |
| Projected `AGENTS.md`                 | **323,595 bytes / 3,355 lines**                                                                                   |

### Conversion subtotal, the two tranches and the whole candidate set

| Scope                           |  Root Δ | Records Δ | Ratio (root ÷ record) | Projected `AGENTS.md` |
| ------------------------------- | ------: | --------: | --------------------: | --------------------: |
| Recommended: Documentation (11) |  −8,269 |    10,861 |                  0.76 |   323,595 B / 3,355 L |
| Deferred: Git (6)               |  −3,722 |     5,812 |                  0.64 |   328,142 B / 3,385 L |
| All 17                          | −11,991 |    16,673 |                  0.72 |   319,873 B / 3,331 L |

### Whole-unit delta

At planning the **whole-unit root delta equalled the conversion subtotal**, because no capture lesson was accepted then.
The implementation capture accepted two additions (see "Capture and regrowth" and task 5.1), measured at **+701 bytes**
of root text, so the delivered unit is:

| Level                       |             Root Δ |  Records Δ | Total maintained | `AGENTS.md`             |
| --------------------------- | -----------------: | ---------: | ---------------: | ----------------------- |
| Conversion subtotal         |     −8,269 (2.49%) |     10,861 |           +2,592 | 323,595 B / 3,355 L     |
| Capture (two additions)     |               +701 |          0 |             −701 | 324,296 B / 3,361 L     |
| **Whole unit as delivered** | **−7,568 (2.28%)** | **10,861** |       **+3,293** | **324,296 B / 3,361 L** |

The capture's 701 bytes are two clauses merged into existing rules, not new bullets; the conversion subtotal stays the
figure the drafts and the per-rule table are keyed to.

## Recommendation

**Implement one unit: convert the eleven remaining eligible rules of `### Documentation & durable claims` (C7–C17).** It
completes the subsection the parent unit designated the hard case, at the better of the two tranches' economics, and
leaves the Git tranche parked with measured figures.

### The selection criterion

The selection criterion is the **unit boundary**, not a per-rule ratio. A tranche is one `## Gotchas` subsection: the
smallest unit whose guidance and record share one subject and can be reviewed in one pass. Within a tranche every
eligible rule converts, so no rule is chosen or dropped on its individual ratio.

**Partial conversion is a valid state under the pairing contract**, not an ambiguous one: an untouched rule carries no
`evidence:` token, a converted rule carries one naming its record, and a token whose record holds no matching section is
a pairing finding. A mixed set is therefore legible — it is a tranche whose rules sit in different, distinguishable
states.

| Trade-off           | Subsection tranche (11 Documentation rules) | Mixed set (row E: 10 rules, 7 Documentation + 3 Git) |
| ------------------- | ------------------------------------------- | ---------------------------------------------------- |
| Thematic coherence  | one subject, one record                     | two subjects, two records at different stages        |
| Review effort       | one pass over one subject                   | one pass that switches between guidance areas        |
| Rules edited        | 11                                          | 10                                                   |
| Root reduction      | −8,269 B                                    | −9,006 B (737 B more)                                |
| Maintained evidence | +2,592 B                                    | +2,171 B (421 B less growth)                         |

The mixed set has the stronger arithmetic and the weaker reading: 737 more bytes removed and 421 fewer bytes of
maintained growth, for two converted subsections that stop partway — C1, C4 and C6 convert while C2, C3 and C5 do not,
and C10, C13, C14 and C16 stay at root. The recommendation keeps the subsection boundary because that margin is 0.22
percentage points of the loaded file, and the eleven rules share one subject a reviewer can hold at once.

The ratios are reported because the request asks for a ranking; they answer **which tranche goes first**, not which
rules go inside one.

### Candidate selection

| ID  | Rule                                              | Root Δ | Record Δ | Ratio | Disposition | Reason                                                                                                             |
| --- | ------------------------------------------------- | -----: | -------: | ----: | ----------- | ------------------------------------------------------------------------------------------------------------------ |
| C7  | A two-way split of an enumerated set              |   −529 |      792 |  0.67 | Adopted     | Documentation tranche: one subsection, one review, one record.                                                     |
| C8  | `workflowLint` runs `actionlint` alone            |   −737 |      910 |  0.81 | Adopted     | Documentation tranche; the tool fact stays at root as a live contract.                                             |
| C9  | Point-in-time versus durable counts               |   −722 |      452 |  1.60 | Adopted     | Documentation tranche; a short record absorbs a long incident narrative.                                           |
| C10 | A configuration-default change                    |   −621 |    1,115 |  0.56 | Adopted     | Documentation tranche; five live surfaces stay at root.                                                            |
| C11 | Doc claims must match source and strength         | −1,317 |    1,617 |  0.81 | Adopted     | Documentation tranche; six checks stay, five incidents move.                                                       |
| C12 | A durable artifact asserts only evidence          |   −614 |      900 |  0.68 | Adopted     | Documentation tranche; the provenance test is the mechanism's own warrant.                                         |
| C13 | A claim about a surface outside the repository    |   −569 |      908 |  0.63 | Adopted     | Documentation tranche; both directions stay at root.                                                               |
| C14 | When a script generates many claims               |   −407 |      620 |  0.66 | Adopted     | Documentation tranche; the parent's one relocation candidate.                                                      |
| C15 | A comparison differing in more than one dimension | −1,261 |    1,607 |  0.78 | Adopted     | Documentation tranche; three sub-rules and the graph command stay.                                                 |
| C16 | A reproduction in an outward-facing artifact      |   −401 |      644 |  0.62 | Adopted     | Documentation tranche; four requirements stay, one upstream reference inventoried.                                 |
| C17 | A "the only X" claim goes stale                   | −1,091 |    1,296 |  0.84 | Adopted     | Documentation tranche; four sweep classes survive as conditions.                                                   |
| C1  | `git stash pop`                                   |   −692 |      910 |  0.76 | Deferred    | Git tranche: not this review; same subject as C2–C6.                                                               |
| C2  | `git reset --hard`                                |   −461 |      741 |  0.62 | Deferred    | Git tranche; its ladder is the rule, so the record carries the episode only.                                       |
| C3  | `openspec archive` filesystem move                |   −256 |      864 |  0.30 | Deferred    | Git tranche, and the set's weakest economics: the record would restate command identifiers that must stay at root. |
| C4  | Programmatic edits to a structured file           | −1,039 |    1,322 |  0.79 | Deferred    | Git tranche's largest candidate; C5 depends on it, so both move together.                                          |
| C5  | The same range-boundary hazard bites source       |   −270 |      604 |  0.45 | Deferred    | Git tranche; two sentences, and it refers to C4's boundary.                                                        |
| C6  | Never chain an edit to a commit                   | −1,004 |    1,371 |  0.73 | Deferred    | Git tranche; three execution modes preserved.                                                                      |
| C18 | A formatter-owned file is re-wrapped (R3)         |    −69 |        — |     — | Deferred    | No substantive reason beyond the byte saving; its provenance is already carried by the em-dash record.             |

### Why this boundary, and why not the whole set

1. It is the parked idea's scope narrowed to one of its two piloted subsections — the smallest complete part of it.
2. It finishes the subsection: C7–C17 convert, the four already-converted rules stay, and the two remaining rules are
   settled by design (no separable material, rejected on retrieval), so the subsection needs no further planning.
3. Its eleven rules share one subject — how a durable claim is made, strength-checked, and evidenced — so the eleven
   sections form one corpus a reader can follow, rather than a mixed guidance-and-git record.
4. It is the better-priced tranche: 0.76 bytes of root removed per byte of record against the Git tranche's 0.64.
5. Converting all seventeen adds the six Git rules: 3,722 more bytes of reduction and 5,812 more bytes of record, plus
   six rule rewrites, six record sections and a second record file — 17 rules and 1.53× the record bytes of the
   recommended unit, over two subjects. The Git tranche is worth doing as its own unit, not as ballast here.

### Does the saving justify the cost

| Comparison              | Parent unit (accepted)                 | This unit        |
| ----------------------- | -------------------------------------- | ---------------- |
| Rules converted         | 7                                      | 11               |
| Root reduction          | 1.45%                                  | **2.49%**        |
| Root reduction per rule | ~704 bytes                             | **~752 bytes**   |
| Record added            | 7,569 bytes                            | 10,861 bytes     |
| Total maintained change | +2,638 bytes                           | **+2,592 bytes** |
| New mechanism surface   | 2 files, auditor scope, 5 requirements | **none**         |

The per-rule saving is slightly better than the parent's, the retained-content cost is slightly lower, and this unit
adds no owned surface, no spec requirement, and no auditor change — so the marginal cost per converted rule is lower
than the pilot's. The review cost is real: eleven rewrites, eleven record sections, and the branch tests that this
design could only reason about. That is why the unit stops at the subsection boundary rather than taking the Git tranche
in the same review.

**If the owner prefers the whole set**, the measurements support it: −11,991 bytes (3.61%), 16,673 bytes of record, and
one additional record file. Nothing in this design changes; only the set of rules task 3.2 converts grows, from eleven
to seventeen.

## Alternatives

| Option                                             | Root-file reduction           | Additional routinely loaded | Total maintained content | Maintenance and retrieval                                                                         |
| -------------------------------------------------- | ----------------------------- | --------------------------- | ------------------------ | ------------------------------------------------------------------------------------------------- |
| **A. Retention**                                   | 0                             | 0                           | unchanged                | unchanged                                                                                         |
| **B. Recommended: Documentation (11)**             | **−8,269 B (2.49%)** measured | 506 B, inside that figure   | **+2,592 B**             | eleven sections in one owned record; one hop for verification                                     |
| **C. Both tranches (17)**                          | −11,991 B (3.61%) measured    | 752 B, inside that figure   | +4,682 B                 | a second record and six more sections; 17 rules over two subjects                                 |
| **D. Git tranche only (6)**                        | −3,722 B (1.12%) measured     | 246 B, inside that figure   | +2,090 B                 | completes the weaker tranche at 0.64 bytes of root per record byte                                |
| **E. Mixed per-rule selection (top ten by ratio)** | −9,006 B (2.71%) measured     | 445 B, inside that figure   | +2,171 B                 | the better arithmetic, two records at different stages of conversion, ten rules over two subjects |

- **B is the recommendation.** It is 69% of the whole set's reduction for 65% of its record growth, and it completes the
  subsection the parent unit designated the hard case.
- **C is rejected as one unit, not as work.** Its six additional rules add 3,722 bytes of reduction for 5,812 bytes of
  new record at a worse ratio, with a second record file and six more sections to review and pair. It remains the next
  tranche.
- **D is rejected.** It completes the subsection with the weaker economics and leaves the larger, more coherent tranche
  untouched.
- **E is not selected, though its arithmetic is slightly better.** It removes 737 more bytes and grows the maintained
  content 421 bytes less, but it edits ten rules across two subjects, leaves both converted subsections partway, and
  touches both records in one review. The recommendation trades that margin — 0.22 percentage points of the loaded file
  — for one coherent subject.
- **A stays coherent** if the owner judges 2.49% not worth eleven record sections.

## Delta determination

**No delta is owed.** Each obligation the change exercises is already specified, and the derivation is:

| Obligation exercised                                                            | Where it is already specified                                                             |
| ------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------- |
| The record's location, file-per-subsection shape, slug heading, and `Rule:` key | `showcase/quality/agent-skills`, "Relocated guidance evidence is kept in a scoped record" |
| A section states the episode and no second rule                                 | the same spec, "A record section states the episode and not a second rule"                |
| The root entry keeps every operational element and carries its token            | the same spec, "A root entry whose evidence moved states every operational element"       |
| A historical identifier survives in the record                                  | the same spec, "A historical identifier survives in the evidence record"                  |
| The audit owns the record and pairs its sections                                | the same spec's audit requirement, and `.opencode/agent/agents-auditor.md`, unchanged     |

The change adds no requirement, changes no record format, and moves no ownership; it applies the mechanism to eleven
more rules. Adding a requirement to document another application of an already-specified mechanism is what the request
excludes, and `openspec validate` has nothing new to check.

## Capture and regrowth

**No lesson is proposed at planning**, so the capture subtotal is **0 bytes** and the whole-unit delta equals the
conversion subtotal. The reasons are stated rather than assumed:

| Candidate lesson                                                               | Decision | Basis                                                                                                                                       |
| ------------------------------------------------------------------------------ | -------- | ------------------------------------------------------------------------------------------------------------------------------------------- |
| "Re-measure a parked estimate before acting on it"                             | Rejected | Already covered: the pre-write pass requires each figure to be derived from the mechanism that produces it.                                 |
| "A converted rule's record key must repeat the new lead when the lead changes" | Rejected | Already covered: the record requirement makes the `Rule:` paragraph the pairing key, and the audit reports a mismatch.                      |
| "Check every inline code span survived at root or in the record"               | Rejected | Already covered by the root-operational-element and historical-identifier requirements; it is the check that verifies them, not a new rule. |
| "The parked idea's Git tranche should be re-parked with measured figures"      | No rule  | A record-keeping act, performed in `docs/ideas.md` by the implementation, not guidance.                                                     |

The capture contract's own trigger — a unit's implementation review — was not the planning pass's; the implementation
ran `lesson-capture` (task 5.1), which **accepted two additions and retired none**:

| Accepted addition                                                                                                                                                                                                                                                     | Destination                                                                       | Why it is not already covered                                                                                                                                                                 |
| --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| A file-wide substring replacement rewrites every occurrence that shares the value, and a row keyed on its table's column count lands in any sibling table sharing that count: scope the replacement, key the row on its identifying cell, re-check every edited table | the C4 rule (Git subsection), merged into its existing granularity sentence       | The rule's stated granularities are the heading, the edit anchor, and the item end; the _match set_ of a replacement and a table row's identity are new, and the defect hit both in this unit |
| A claims row anchors its _embedded commands_ too, since a script that opens the working-tree file reports the implemented result while the row reads as anchored                                                                                                      | review-gate step 2, and the same clause in `openspec/config.yaml`'s `design` rule | The rule required the row's source to name the pre-change state; both scripts in this design opened the live file and stopped reproducing once the change landed                              |

The two clauses merged into existing rules, added **701 bytes** of root text and no new bullet. `lesson-capture.md`
still sends a caught lesson's supporting evidence to the change record, and this unit does not change that contract.

**Regrowth after conversion.** A correction to a converted rule goes to the rule's text and, when its evidence moved, to
the same section, reached from the rule's `evidence:` token. Additional evidence for a covered rule stays
`already covered` in the change record, so the record does not silently accumulate material with no root rule.

## Risks / trade-offs

- **A record nobody reads.** The token must be honoured for a reader to reach an entry; the root text carries the
  action. That is the mechanism's accepted cost, and it is unchanged from the parent unit.
- **The records are larger than what they absorb.** The recommended unit's sections are 10,861 bytes against 8,269 bytes
  of removed root text; the loaded surface is what falls.
- **The multi-branch drafts are the risk concentration.** C9, C11, C15, and C17 each carry five or more branches; a
  dropped branch would change behaviour silently. The drafts keep every branch, and the identifier inventory is
  mechanical, but the implementation must re-test each branch rather than trust the draft.
- **The recommendation leaves a tranche.** The Git rules stay parked with measured figures, so the next unit starts from
  measurements rather than estimates — the failure mode this unit exists to correct.
- **Six deferred rules keep their bytes.** 3,722 bytes of reduction is left on the table deliberately: the tranche
  boundary and a single review pass are the reason, not an individual rule's economics — three of the six out-rank
  adopted rules on that measure.

## Rollback

Nothing is implemented. The planning outputs are this change directory (untracked until committed) and the unit's
`docs/review-rounds.tsv` rows. If the unit is later implemented and reversed, restoring the eleven spans from
`git show <base>:AGENTS.md`, deleting their eleven record sections, and removing their `evidence:` tokens restores the
prior state; the Git record was never touched.

## Claims

Every figure is re-derived from the mechanism that produces it, after the drafts' last edit.

| #   | Claim                                                                                                                                                                                                                                 | Source / deriving command                                                                                                                                                                                                                                                                                            |
| --- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1   | The baseline revision is `e07f5f8f…`, the branch forked from it, and the tree was clean at planning                                                                                                                                   | `git rev-parse HEAD`, `git rev-parse origin/main`, and `git status --porcelain --untracked-files=all` before this unit's edits (empty)                                                                                                                                                                               |
| 2   | `AGENTS.md` at `HEAD` is 331,864 bytes over 3,409 lines                                                                                                                                                                               | `git show HEAD:AGENTS.md > /tmp/pre.md && wc -c -l /tmp/pre.md` → `3409 331864`                                                                                                                                                                                                                                      |
| 3   | At `HEAD`: `## Gotchas` 135,399 B; Git 16,484 B / 167 L / 13 rules; Documentation 28,521 B / 282 L / 17 rules                                                                                                                         | the census script below, run on the pre-change file (`git show HEAD:AGENTS.md > /tmp/pre.md && python3 <script> /tmp/pre.md`), whose output is in "Reproducible baseline"                                                                                                                                            |
| 4   | At `HEAD`, both pilot subsections are byte-identical to `b7f26469`                                                                                                                                                                    | a script extracts each subsection from `git show b7f26469:AGENTS.md` and from `git show HEAD:AGENTS.md` and compares them; both print `IDENTICAL True`, and the working tree's Documentation subsection differs by this unit                                                                                         |
| 5   | The candidate set is 16 undrafted rewrites plus 1 undrafted relocation, plus R3                                                                                                                                                       | the parent design's disposition table, rows 1–5, 13, 16–18, 22–28, 30, mapped in "The candidate set"                                                                                                                                                                                                                 |
| 6   | The recommended unit's per-rule deltas sum to −8,269 bytes                                                                                                                                                                            | the per-rule table's `Root Δ` column: −529 −737 −722 −621 −1,317 −614 −569 −407 −1,261 −401 −1,091 = −8,269                                                                                                                                                                                                          |
| 7   | The whole candidate set's deltas sum to −11,991 bytes                                                                                                                                                                                 | the same column plus the deferred tranche: −8,269 + (−692 −461 −256 −1,039 −270 −1,004) = −11,991                                                                                                                                                                                                                    |
| 8   | The recommended unit's record additions are 10,861 bytes                                                                                                                                                                              | the per-rule `Record Δ` column for the 11 adopted rows, summed; each figure is the section's bytes plus its separator                                                                                                                                                                                                |
| 9   | The deferred tranche's record additions are 5,812 bytes                                                                                                                                                                               | the same column for the 6 deferred rows                                                                                                                                                                                                                                                                              |
| 10  | A rebuilt, Prettier-formatted candidate for the recommended unit is 323,595 bytes                                                                                                                                                     | build the file with the eleven drafts over the pre-change revision (the measurement script's second argument: `/tmp/pre.md`), run Prettier 3.9.10 at printWidth 120 / proseWrap always, `wc -c`; the formatted file is byte-identical to the built one                                                               |
| 11  | A rebuilt, Prettier-formatted candidate for all 17 is 319,873 bytes over 3,331 lines                                                                                                                                                  | the same procedure with all seventeen drafts                                                                                                                                                                                                                                                                         |
| 12  | Zero backticked spans are absent from both the draft root text and the record                                                                                                                                                         | the span script in "The identifier inventory", run on the pre-change file (`/tmp/pre.md`, its second argument); it reports `LOST=0` per candidate and `total lost spans: 0`                                                                                                                                          |
| 13  | The evidence record is 16,608 bytes after implementation, from a 5,747-byte pre-change file                                                                                                                                           | `wc -c docs/guidance-evidence/documentation-and-durable-claims.md` → `16608` now; `git show HEAD:docs/guidance-evidence/documentation-and-durable-claims.md \| wc -c` → `5747`, plus the 10,861-byte addition                                                                                                        |
| 14  | Only C16 moves an upstream reference, and that reference stays cited in the same file                                                                                                                                                 | the `owner/repo#NNN` scan of each candidate span finds only `Fission-AI/OpenSpec#1892`; `grep -n` finds its other citation at `AGENTS.md:2994`                                                                                                                                                                       |
| 15  | No live artifact outside `AGENTS.md` restates a rewritten rule's obligations                                                                                                                                                          | a grep for each lead's distinctive phrases across `README.md`, `SECURITY.md`, `.opencode/`, `docs/`, `openspec/specs/`, `scripts/`, and `build.gradle.kts`; the records' own `Rule:` keys match, plus `agents-auditor.md:54`'s description of its "the only X" check, which restates none of C17's sweep obligations |
| 16  | No gate validates the rewritten prose, while the gates that read the file still run over it                                                                                                                                           | `verifyInstallCommands` scans for `helm install … --version` and `verifyDocumentedPins` for Paketo pins; neither pattern occurs in a span. `verifyCapturedMarkers` reads each `captured:` marker, which stays inside its rule block, and `verifyPrettierAlignment` reads the whole document                          |
| 17  | The delta question is answered by existing requirements                                                                                                                                                                               | the four requirements and the audit requirement in `showcase/quality/agent-skills`, quoted in "Delta determination"; `agents-auditor.md` already scopes `docs/guidance-evidence/`                                                                                                                                    |
| 18  | The record's header needs no edit                                                                                                                                                                                                     | `docs/guidance-evidence/documentation-and-durable-claims.md:1-7` states the file's shape without counting sections                                                                                                                                                                                                   |
| 19  | The parent's R3 draft nets −69 B                                                                                                                                                                                                      | the parent design's "Size accounting", restating a 110-byte text saving against a 41-byte token                                                                                                                                                                                                                      |
| 20  | The recommendation's ratio is 0.76 and the deferred tranche's is 0.64                                                                                                                                                                 | 8,269 ÷ 10,861 and 3,722 ÷ 5,812, from rows 6–9                                                                                                                                                                                                                                                                      |
| 21  | The parent unit converted 7 rules for 1.45%, ~704 B per rule, 7,569 B of record, and +2,638 B of total maintained content, adding 2 files, an auditor scope, and 5 requirements                                                       | `openspec/changes/archive/2026-10-20-separate-agents-md-operational-from-evidence/design.md`, "Size of the conversion" and "Size accounting"                                                                                                                                                                         |
| 22  | The mixed set removes 9,006 B, carries 11,177 B of record and grows maintained content by 2,171 B; the whole set's record is 1.53× the recommended unit's; the recommended unit's extra margin is 0.22 percentage points of 331,864 B | the ten mixed-set rows' deltas summed (C9, C17, C11, C8, C15, C1, C4, C6, C12, C7 → −722 −1,091 −1,317 −737 −1,261 −692 −1,039 −1,004 −614 −529 and their record figures 452 1,296 1,617 910 1,607 910 1,322 1,371 900 792); 16,673 ÷ 10,861; 737 ÷ 331,864                                                          |

### Seed and dismissals

`scripts/claim-scan.py` seeds the table with the four claim shapes over the change's added lines; each kind is resolved
once, and the scan is re-run after the unit's last append to this file.

| Kind         | Disposition                                                                                                                                                                                                                                                                                                         |
| ------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `count`      | A figure in the drafts or the tables is a claims row above; a figure marked as the parent's reading estimate is identified as such, and none is used as a projection.                                                                                                                                               |
| `absolute`   | Dismissed: a quantifier inside a draft ("every", "never", "only") is the rule's own normative text, not a claim about the repository; a quantifier in this design's prose is resolved by the table it introduces.                                                                                                   |
| `cross-ref`  | Resolved by the row whose source names that artifact, or by the draft it names (each `C<n>` is defined in "Drafts and measurements").                                                                                                                                                                               |
| `path`       | Resolved by the command in the row it appears in, or it is a path the change would create (`docs/guidance-evidence/…`) or a draft's own content.                                                                                                                                                                    |
| ledger row   | A row appended to `docs/review-rounds.tsv` is the reviewer's machine-readable record line, written verbatim with the session fields the ledger declares; its numerals are the round number and the compliance-miss count, not claims about the repository.                                                          |
| fenced draft | A numeral, path, or reference inside a `text` or `markdown` fence is the artifact's own content — a drafted rule, a record section, or one of the three scripts this design ships — not a claim about the repository; the drafts' sizes are rows 6–13 and their identifiers are checked by "Boundary verification". |
