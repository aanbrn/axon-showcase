# Design

## Context

`proposal.md` states the problem: the weekly report's state column collapses every closure into `closed <date>`. The
2026-10-25 sweep resolved 11 closed references by hand. One carried a fix that reached us
(`build-extensions-oss/gradle-helm-plugin#140`, fixed in 3.2.0, which our catalog pins), and one is a counter-example
whose closure does not bear on how the reference is used (`Fission-AI/OpenSpec#1322`, cited for duplicate screening).
The other 9 carried no fix for us:

- 4 closed `not_planned` (`anomalyco/opencode#49127`, `#49904`, `#50247`, `#54477`);
- 1 is a duplicate of a still-open thread (`warpdotdev/warp#6320` → `#5550`);
- 1 is an unmerged fix PR (`micromatch/braces#72`);
- 1 closed `completed` on a line above our resolution (`spring-projects/spring-data-elasticsearch#3334`, fixed in
  `6.2.0-M2` while we resolve `5.5.13` on the `spring-data-opensearch` 2.0.8 line);
- 1 closed `completed` without changing the behavior our build documents (`ben-manes/gradle-versions-plugin#755`);
- 1 has a fix still unmerged upstream (`Fission-AI/OpenSpec#1891` → `#1894`).

The API's `state_reason` had reported each disposition in the same call that reported the state.

The repository already parked this as an idea (`docs/ideas.md`, "Carry a closed reference's disposition in the
`upstreamReferences` report"), so the design below adopts it rather than re-deriving one.

The report is one `build-logic` task (`UpstreamReferencesTask`) writing `build/upstream-references/report.txt`, one row
per reference: `owner/repo#NNN<TAB><state><TAB><citations>`. `.github/workflows/upstream-references.yml` reads that
file, filters the closed and unresolved rows, and posts them to the rolling "Upstream references" issue.

## Goals / Non-Goals

**Goals**

- A row for a closed reference names the closure's disposition, so a duplicate or a declined closure reads differently
  from a completed one without another API call.
- The three notes the sweep found short of that disposition record it.
- The report's consumers keep working unchanged.

**Non-Goals**

- Changing the report's corpus, its schedule, or its issue notification.
- Turning the disposition into an action: a closure stays a _trigger to check_.
- Re-deriving a disposition's meaning for us in code; that stays the reader's judgment, recorded in the notes.
- Humanizing the API's tokens into a vocabulary of our own.

## Decisions

### Render the API's `state_reason` verbatim

The state is `closed (<state_reason>) <closed date>` when the API reports a reason, `closed <date>` when it reports
none, and `open` / `unresolved` as today.

- Alternative — humanize the tokens (`not_planned` → `not planned`): rejected. It invents a vocabulary the API does not
  promise, and a new `state_reason` value would render as an unmapped gap rather than as the API's own answer.
- Alternative — keep the boolean and put the reason in a separate column: rejected. The workflow's `awk` and the issue
  body both read one state field; a new column changes the workflow for no reader benefit.

### A plain `internal object` carries the parsing, with a unit test

`UpstreamReferenceState.parse` takes the `gh api` output and returns the rendered state;
`UpstreamReferencesTask.resolveState` keeps the exec and delegates the rendering.

- Alternative — test the task action: rejected. `build-logic` has no `gradleTestKit`, and its `src/test` holds unit
  tests of plain helper objects only (the `gradle-build` pack's rule); a task action needs an injected `ExecOperations`
  and would test the harness rather than the decision.
- The object sits beside `UpstreamReferenceScope` and `UpstreamReferencePattern`, the task's other two extracted
  collaborators.

### Field separator `|` in the `--jq` program

The API call asks for `.state + "|" + (.state_reason // "") + "|" + (.closed_at // "")` and the parser splits on `|`
with a limit of 3.

- Alternative — a tab: rejected. The report row is itself tab-separated (`"$id\t$state\t$citations"`), so a tab inside
  the state would split one row into extra columns and shift what the workflow's `$2` filter reads. It also has to carry
  the two characters `\t` through Kotlin, an escaping trap no test covers (the test sees the parsed output, not the
  argument).
- Alternative — three `gh` calls: rejected. Three API calls per reference for data one response carries.

### The workflow is not edited

Its filters are `$2 ~ /^closed/` and `$2 == "unresolved"`, so a longer closed row still matches and an unresolved row
keeps its exact value.

- Alternative — teach the workflow the new shape: rejected as unnecessary; a prefix match is already the narrowest
  contract the workflow needs.
- Consequence to verify: the workflow's job runs the report and posts the rows, so the first end-to-end evidence is a
  dispatch of the workflow against the pushed branch, not a local run alone.

### The three notes record the disposition, not the report's format

`AGENTS.md` (`#49127`), `AGENTS.md` and `.opencode/skills/setup-agent-tools/SKILL.md` (`#49904`), and `AGENTS.md`
(`#50247`) gain that upstream closed the report `not_planned`, so that closure brought no fix and each note's retirement
condition is behavioural. The report already prints the disposition once this change lands; the notes state what it
means for the clause they sit on.

## Amended clauses

The `MODIFIED` delta replaces the whole requirement, so each amended clause is quoted beside its replacement.

| Amended clause                                                                                                                 | Replacement                                                                                                                                                                                                                                                                                           | Why                                                                                                                                            |
| ------------------------------------------------------------------------------------------------------------------------------ | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------- |
| "resolves its state through the GitHub API, and reports every reference with that state and the file and line it is cited at." | The same sentence, plus: "A closed reference's reported state SHALL carry the closure's disposition — the `state_reason` the API returns, such as `completed`, `not_planned`, or `duplicate` — beside its closed date, so a duplicate or a declined closure is distinguishable from a completed one." | The obligation the report's state column carries is what changes; the corpus, the trigger, and the notification clauses are carried unchanged. |

Every other clause of the requirement is carried verbatim, and every existing scenario keeps its text and relative
order; two scenarios are added, one per branch the new obligation has (a reason present, a reason absent).

## Claims table

Each row is a fact this change states about its evidence. The rows are anchored at `837aaa52`, the base revision when
they were derived; the branch was then refreshed onto `528fdcce`, whose `libs.versions.toml` edits move the two Paketo
pins and leave the `spring-data-opensearch` and `helm-plugin` values the rows quote.

| Claim                                                                                  | Source or command                                                                                                                                        | Result                                                                                                                 |
| -------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------- |
| The API returns a closure reason with the state                                        | `gh api repos/<owner>/<repo>/issues/<n> --jq '{state, state_reason}'` for the 11 closed references                                                       | 5 `completed`, 4 `not_planned`, 1 `duplicate`, 1 `null`                                                                |
| `state_reason` values to render are `completed`, `not_planned`, `duplicate`, and null  | the same command, read as a set                                                                                                                          | those four forms; no other value appeared                                                                              |
| `warpdotdev/warp#5550`, the duplicate's live thread, is open                           | `gh api repos/warpdotdev/warp/issues/5550 --jq .state`                                                                                                   | `open`                                                                                                                 |
| `micromatch/braces#72` reports no closure reason                                       | `gh api repos/micromatch/braces/issues/72 --jq .state_reason`                                                                                            | `null`                                                                                                                 |
| `anomalyco/opencode#50277`, the community fix for `#50247`, never merged               | `gh api repos/anomalyco/opencode/pulls/50277 --jq '{state, merged}'`                                                                                     | `closed`, `false`                                                                                                      |
| `spring-projects/spring-data-elasticsearch#3334`'s fix sits above our resolution line  | `git show 837aaa52:docs/ideas.md`, the NANOS workaround entry, and `gradle/libs.versions.toml`'s `spring-data-opensearch` pin                            | fix in `6.2.0-M2`; we resolve `5.5.13` on the `2.0.8` line                                                             |
| `ben-manes/gradle-versions-plugin#755`'s closure changed no behaviour for us           | `git show 837aaa52:docs/guidance-evidence/documentation-and-durable-claims.md`, its upstream-references section                                          | `satisfiesDeclaredBound`, "nothing changes by default"; the row remains                                                |
| `build-extensions-oss/gradle-helm-plugin#140` is fixed in the release our catalog pins | `gh api repos/build-extensions-oss/gradle-helm-plugin/releases --jq 'map(select(.tag_name=="3.2.0"))'` and `git show 837aaa52:gradle/libs.versions.toml` | `3.2.0` carries PR #146 ("migrate to full task notation", the clause #140 names); the catalog pins `helm-plugin` 3.2.0 |
| `Fission-AI/OpenSpec#1894`, the fix for `#1891`, is unmerged                           | `gh api repos/Fission-AI/OpenSpec/pulls/1894 --jq '{state, merged}'`                                                                                     | `open`, `false`                                                                                                        |
| The workflow matches the closed prefix and `unresolved` exactly                        | `git show 837aaa52:.github/workflows/upstream-references.yml`                                                                                            | `$2 ~ /^closed/` and `$2 == "unresolved"`                                                                              |
| `README.md` enumerates the state vocabulary                                            | `git show 837aaa52:README.md`, the update-check section                                                                                                  | "its state (open / closed / unresolved)"                                                                               |
| The idea is parked with this scope                                                     | `git show 837aaa52:docs/ideas.md`, the entry "Carry a closed reference's disposition"                                                                    | parked, `closed (duplicate) 2026-03-27` as the proposed row                                                            |
| `build-logic` unit-tests plain helper objects, not task actions                        | `.opencode/skills/gradle-build/SKILL.md`, the convention plugin's verification bullet                                                                    | confirmed                                                                                                              |

### Dismissals

`scripts/claim-scan.py --base origin/main` seeds the table; each class it flags beyond the rows above is dismissed for
one reason.

| Class       | Why it needs no row                                                                                                                                                                        |
| ----------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| `path`      | The candidate is a file path, an `owner/repo#NNN` reference, or a command fragment the sentence around it resolves; every reference whose state or fix the change asserts has a row above. |
| `count`     | The candidates are a timestamp component and the Context's closure tallies, whose split the first row derives.                                                                             |
| `absolute`  | Each `only`/`every`/`never` sits in a sentence whose own clause states the claim, and the row covering that clause is above.                                                               |
| `cross-ref` | The candidate is the `repos/…/issues/…` path inside the `gh api` invocation, not a claim about another artifact.                                                                           |

## Risks / Trade-offs

- **A longer state column could break a consumer.** The only consumer is the workflow's `awk`, whose `closed` filter is
  a prefix match; the dispatch against the pushed branch is the end-to-end evidence, recorded as a task.
- **The report's row for a closure now varies in width.** Accepted: the disposition is one token, and the issue body
  already carries one row per reference.
- **A future `state_reason` value renders verbatim.** Accepted: that is the API's answer, and the reader judges it; a
  mapping would silently drop an unknown value into a fallback.
- **The three note edits repeat what the report prints.** Each states what the disposition means for its own clause (no
  fix is coming; the retirement condition is behavioral), which the report's row cannot answer for a reader of the note.
