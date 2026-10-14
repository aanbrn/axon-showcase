# Design

## Context

See `proposal.md` — Why. The review's convention bullet reports defects the applying command would rewrite; the figures
are in the table below.

## Claims table

Each row is a fact this change states about its evidence — the prior artifacts it rests on — with the source that
derives it:

| Fact                                                                       | Stated in                | Source (re-run)                                                                                                                                                                    |
| -------------------------------------------------------------------------- | ------------------------ | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `formatting` is 5 of the record's findings                                 | `design.md`              | `awk -F'\t' '$1 !~ /^#/ && $2 != "dont-report-what-a-gate-covers" { n=split($4,a,","); for(i=1;i<=n;i++) if(a[i]=="formatting") c++ } END { print c+0 }' docs/review-rounds.tsv`   |
| the pre-change bullet reports convention violations, formatting among them | `design.md` — Context    | `git show HEAD:.opencode/agent/review-quick.md \| grep -n "convention violations"`                                                                                                 |
| the two commands the clause names are the build's apply tasks              | `proposal.md` — Why      | `grep -n "npmFormat" build-logic/src/main/kotlin/frontend-conventions.gradle.kts`; `grep -n "com.diffplug.spotless" build-logic/src/main/kotlin/code-check-conventions.gradle.kts` |
| the formatter leaves an inline code span's interior untouched              | `review-quick.md` bullet | `grep -n "inline code span" AGENTS.md`                                                                                                                                             |
| the formatter does not touch some file types (YAML, the Python scripts)    | `review-quick.md` bullet | `grep -n "formatter does not touch" AGENTS.md`                                                                                                                                     |
| the route-candidate rule reduces a rule a mechanism enforces to a pointer  | `design.md` — Decision   | `grep -n "route candidate" openspec/specs/showcase/quality/agent-skills/spec.md`                                                                                                   |

The `$2` filter excludes this unit's own rows: the corpus is live, so an unfiltered count would move every time this
unit's review appends a row.

## Decisions

**A defect the applying command rewrites is routed, not reported.** The clause names no categories: a finding the
applying command would rewrite is routed to it, and what it leaves alone — an inline code span's interior, a file type
no formatter owns — stays reportable. That matches the agents-auditor's route-candidate rule rather than extending it,
since that rule reduces a rule to a pointer where this routes a finding.

## Risks / Trade-offs

- Deliberately no category list. The cost is that a reader must know what the applying command rewrites; the bullet in
  `review-quick.md` covers the same ground for the reviewer.
- A churn-cost trade: the class is small (5 findings over the record), so this is a fix-time saving, not a round saving
  — recorded so the change is not read as a loop-cost fix.
