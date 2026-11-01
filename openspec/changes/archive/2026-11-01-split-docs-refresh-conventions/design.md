# Design

## Context

The bullet is the repository's most load-bearing convention: every unit reads it to decide which docs to refresh, how a
version bump's sweep works, which docs ship with the change's PR, and which un-gated copies exist. It is 66 lines, ten
`captured:` markers, and seven obligation families. The 2026-10-31 dispatch audit run found the density — its report
lives only on `#781`'s branch, which cannot merge as it stands, and the merged `docs/audits/2026-10-31.md` records a
clean verdict without it — while the split itself was parked rather than applied. The transaction is a restructure: no
clause may be dropped, and the markers must keep evidencing the clause they belong to.

## Goals / Non-Goals

**Goals:**

- Give each obligation family its own labeled container under the existing bold lead, and each independently actionable
  obligation its own bullet beneath it.
- Carry every clause's content unchanged, apart from the splices decision 3 names.
- Keep each marker with the clause it evidences.
- Prove no clause was dropped, rather than asserting it.

**Non-Goals:**

- Rewriting any obligation, adding a new rule, or moving content out of `AGENTS.md`.
- Attributing a marker whose captured change is unavailable, where its base position does not identify its clause either
  and the attribution would be invented.

## Decisions

1. **Family containers, one obligation per bullet.** The body splits into seven family containers — the version bump,
   the change's own idea, the un-audited docs, the guidance-evidence records, the causation boundary, the un-gated
   copies, and a file that depends on an outside surface — the groupings the bullet's own sentences already form, and
   each container holds one bullet per independently actionable obligation. Granularity is the obligation, not each
   internal action: a bullet stating one obligation and its own verification stays whole (the sweep and the fix it
   requires, the enable and the naming it owes), while obligations with different triggers become separate bullets.
   `correct-subagent-reload-rule`'s decision 5 decided this the last time a unit rewrote such a block, and it is the
   precedent adopted here.
2. **The bold lead keeps the trigger.** The "on every change, verify …" sentence, the ADR cases, and "update them before
   reporting the change done" stay above the family containers, so the convention's trigger reads first.
3. **Verbatim clauses, with every splice named.** Each clause moves to its family with its content unchanged: no
   obligation, exception, example, or identifier is altered. The splice has three scopes, each named rather than
   derived:
   - **At a boundary:** the family label; the connective or punctuation a bullet boundary replaces ("Also …", "while …",
     "and …", an aside's dashes becoming a colon or parentheses); the wording by which a clause refers to its own
     subject where the label now names it; and the capital a bullet opens with.
   - **A merged explanation:** inside a container, an explanatory sentence is merged into the bullet it explains.
   - **A fronted obligation:** an obligation the base expressed as a `so`-consequence is fronted as the bullet's lead.
4. **Markers where they are evidenced.** The three `bump-dependencies-*` markers move to the version-bump container's
   last bullet, whose rule they record, and `address-scan-findings-2026-10-25` stays with the `docs/guidance-evidence/`
   clause it followed in the base text. The remaining six trailed the bullet's final clause as one cluster and stay at
   the rule's end, because their captured changes are not all available to attribute; a wrong attachment is worse than a
   rule-level one.
5. **The coverage control is a token inventory.** Every distinctive token of the old bullet (words of six characters or
   more, with case and trailing punctuation normalized) must appear in the new text; the differences a raw comparison
   reports are a bullet's opening capital and a line-ending period, which the normalization absorbs.
   `verifyCapturedMarkers` and the review cover placement and meaning.

## Risks / Trade-offs

- **A clause is dropped in the move.** The token inventory and a clause-by-clause read cover it; the inventory is coarse
  enough to pass a mangled sentence, so the read stays necessary.
- **A marker is misattributed.** One moves on its readable base position — `address-scan-findings-2026-10-25` — and the
  rest keep their rule-level position.
- **A bullet separates an obligation from what it verifies or excludes.** Decision 1 keeps a pair whole, and the
  clause-by-clause read checks that each new bullet still stands alone.
- **The bullet grows.** The split adds labels and bullet boundaries, not rules; the measured growth is 88 non-whitespace
  characters, and the formatter owns the wrapping.

## Verification

- The token inventory over the old and new bullet texts, applied with decision 5's case-and-punctuation normalization.
- `./gradlew spotlessApply`/`spotlessCheck`, `verifyCapturedMarkers`, `verifyIdeasSections`, `openspec validate --all`,
  and the Docker-free root `check`.
- A clause-by-clause read of the old and new text against each other.

## Claims

The derivation pass ran over the change dir and the diff against base `fff14836` (the unit's merge base with
`origin/main`), seeded by `scripts/claim-scan.py --base fff14836`, and each claim was re-derived from the source the row
names after the last edit. The branch was fast-forwarded onto `origin/main` (`0734891b` → `fff14836`, PR #797) while the
work was uncommitted; that advance changed neither `AGENTS.md` nor `docs/ideas.md`, so the rows' commands reproduce
unchanged against the new base.

| Claim                                                       | Source                                                                                                                                                                                                                                |
| ----------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| The bullet carried the density the audit found              | `git show fff14836:AGENTS.md`'s "Docs refresh on change" bullet: 66 lines, ten `captured:` markers, seven obligation families                                                                                                         |
| Each family is one container with one bullet per obligation | the new `AGENTS.md` text: seven family containers, five carrying sixteen obligation bullets and two stating a single obligation each                                                                                                  |
| Granularity is the obligation, not each internal action     | `openspec/changes/archive/2026-10-26-correct-subagent-reload-rule/design.md` decision 5, which decided the same in-place split and records the owner's direction                                                                      |
| The 2026-10-31 dispatch audit run found it                  | `git show fff14836:docs/ideas.md`, the entry this change removes; its report is on `#781`'s branch, and `grep -n "density\|Docs refresh" docs/audits/2026-10-31.md` → no match                                                        |
| No clause was dropped                                       | the distinctive-token inventory of the base bullet against the new one, applied with decision 5's case-and-punctuation normalization — no token is missing — and read clause by clause                                                |
| The split adds labels and bullet boundaries, not rules      | the bullet's non-whitespace characters measure 6,303 in the base text and 6,391 now: 88 characters of labels and bullet boundaries                                                                                                    |
| The markers still sit inside the rule block                 | `./gradlew verifyCapturedMarkers` → green; ten markers in both texts, three `bump-dependencies-*` on the version-bump container's last bullet, `address-scan-findings-2026-10-25` on the evidence clause it followed in the base text |
| The convention stays at root                                | `git show fff14836:docs/ideas.md`: "The content stays at root: it is a repository-wide rule that fires on every change"                                                                                                               |
| No capability spec states the obligations                   | `grep -rn "docs refresh" openspec/specs` and `grep -rn "docs-refresh" openspec/specs` → no match, both exiting 1                                                                                                                      |
| The gates pass                                              | `spotlessCheck`, `verifyCapturedMarkers`, `verifyIdeasSections`, `openspec validate --all` and the Docker-free `:check` are green                                                                                                     |

**Dismissals**, one reason per candidate kind.

- `[absolute]` — in `AGENTS.md` they are the base bullet's own words (`every`, `only`, `never`, `no`) inside clauses
  whose content the inventory row proves carried; in the planning artifacts they are the change's own scope statements,
  which their own rows carry.
- `[path]` — each `AGENTS.md` candidate sits inside a carried clause, so it names the same in-repo surface the base text
  named; each planning-artifact candidate is a surface this change runs or greps, which was checked to exist.
- `[cross-ref]` — each is a pull-request number or a slash-joined phrase: the two the change introduces are the audit
  row's (`#781`) and the refresh record's (`#797`, sourced to `git log -1 --format=%s fff14836`), and the rest sit
  inside carried text.
- `[count]` — the line and marker counts are the density row's, the container and bullet counts are the structure row's,
  the character figures are the growth row's, and the removed entry's counts are the audit row's, so no count is left to
  this dismissal.
