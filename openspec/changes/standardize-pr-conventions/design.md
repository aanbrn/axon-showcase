# Design

## Context

The owner's observation — "there is no single template for titling and describing pull requests" — holds for titles and
partly for bodies, and the two halves have different histories.

**Bodies.** `.github/PULL_REQUEST_TEMPLATE.md` states two sections (`## Summary`, `## Test plan`) and a five-line
checklist, and its header comment claims the shape "mirrors the shape recent pull requests follow". Adoption is partial:
530 of 660 merged bodies carry `## Summary`, 471 carry `## Test plan`, 58 carry no heading at all, and none is empty.

**Titles.** Nothing states a rule. The only pull-request-title sentence in the corpus — `openspec/specs/`, `docs/adr/`,
`AGENTS.md`, and the template — is `showcase/quality/merge-governance`'s audit requirement, and it is about the OpenCode
action's _derivation_ (a mention leading the response summarises into a title about the mention), not about a format;
the `ship-change` skill's derivation section describes how the action generates a title (row C9), not what shape it
should take. Practice split instead: 55 of 660 merged titles carry a `:` and 605 do not; 49 of those 55 are a kebab
OpenSpec change name (`adopt-worktree-isolation: …`), a form that appears in 22 of the 28 pull requests merged since
#650 and in none of the 582 merged before #600.

**The reader.** `.github/workflows/release.yml` runs `gh release create … --generate-notes`, and
`showcase/quality/releases` states that the notes are generated from the pull requests merged since the previous
release. A merged title is therefore public text, and the squash merge makes it the permanent commit subject.

**The agent path.** `AGENTS.md`'s `--body-file` gotcha records that a body written to a file bypasses the template, so
the template reaches an agent-opened pull request only if the agent copies its sections. That is the mechanism this
change has to account for; the template alone cannot.

### Premise check

The question "should we write a convention down" first needed "is there already one, recorded or deliberate":

- **No spec states a pull-request title or body format.** Two greps settle the spec half: the 44 case-insensitive
  `title` hits across `openspec/specs/` and `docs/adr/` are 35 occurrences of the showcase's own `title` field, 4 of the
  `specs-auditor`'s spec-title matching, 2 of `docs/adr/README.md` (its filename convention and its ADR title template),
  and 3 in `merge-governance` (441–442, 457); `test plan|## Summary|pull request body` over the same trees is 1 hit,
  `merge-governance:456`, the owner-mention clause this change leaves intact.
- **No ADR states it.** `git grep -n -i -E "pull request|pull-request" -- docs/adr/` returns 2 hits, both
  `docs/adr/0016-releases-and-versioning.md`: one records that the repository carried hundreds of merged pull requests
  without a tag or Release, the other that the notes are generated from the merged pull requests.
- **`AGENTS.md` states no format**, only the `--body-file` bypass mechanics and the zsh `--body` quoting hazard.
- **The template states a body shape**, not a title rule, and its own comment concedes it describes practice rather than
  setting it.

So the emergent kebab form is drift, not a recorded decision — but it is 22 of the newest 28, which makes "write down
what practice already does" a real alternative rather than a straw man. It is weighed below and rejected on the release
notes reader, not on taste.

## Decisions

| #   | Decision                                                                                      | Rationale                                                                                                                                                                                                                                                                                  |
| --- | --------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| 1   | The template header owns the standard                                                         | It is the one artifact every pull-request author opens, human or agent, and it already owns the body shape; a second owner would be a copy that drifts                                                                                                                                     |
| 2   | The title is one imperative English phrase naming the change, with no prefix                  | The reader of a release note does not know a change name; the change name and the issue link belong in `## Summary`, where they are clickable                                                                                                                                              |
| 3   | `## Summary` then `## Test plan` are required, in that order, and further sections may follow | This is what the corpus already does most often, and it keeps the claims-table and reviewer-notes sections the review gate produces; a report pull request's body is the report, which its requirement owns                                                                                |
| 4   | No gate validates a title or a body                                                           | A format check would be cheap, but the owner's decision was a written standard first; the revisit condition is parked so a later drift has a recorded trigger                                                                                                                              |
| 5   | The `ship-change` skill points at the standard rather than restating it                       | The skill is the agent's path into `gh pr create --body-file`, and single-sourcing keeps one wording                                                                                                                                                                                       |
| 6   | An action-derived title is checked, and renamed only when it deviates                         | The action summarises the response, so the summary may paraphrase rather than name the change; a summary that already obeys the rule needs no rename                                                                                                                                       |
| 7   | No spec delta (`skip_specs: true`)                                                            | No capability spec covers a pull-request title or body **format**; `merge-governance`'s report-body requirement is unchanged, and `agent-skills` mandates a step correction in its own skill — which is what the capture lands in `review-gate` (see the proposal's Modified Capabilities) |

## Alternatives considered

| Alternative                                                      | Why not                                                                                                                                                                        |
| ---------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| Document the kebab `<change-name>: …` form practice already uses | It duplicates the change name that `## Summary` carries, and it lengthens the public text: titles merged since #600 average 71.3 characters against 54.9 across the corpus     |
| Conventional Commits (`feat:`, `fix:`, `docs:`)                  | The repository has no release-automation consumer that parses types (notes are generated, not curated), and one stray `docs:` prefix already reads as a type without being one |
| Classify pull requests with labels                               | None of the last 15 merged pull requests carries a label, so the mechanism would start from zero, and generated notes would need a labelling habit this change cannot create   |
| A CI check on the title shape                                    | Deferred rather than rejected: the standard ships first, and `docs/ideas.md` parks the check with the drift that would justify it                                              |
| Put the standard in `AGENTS.md`                                  | It would load on every turn for a rule that matters only at `gh pr create`, against the file's bounded-growth discipline                                                       |

## Claims table

Figures are as of 2026-10-25 and move as pull requests merge; each row names the command that reproduces it.

| #   | Claim                                                                                                                                                   | Source / deriving command                                                                                                                                                                                                                                                                                                                                                                                                                |
| --- | ------------------------------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| C1  | 660 merged pull requests; 55 titles contain `:`; 49 begin with a kebab change name                                                                      | `gh pr list --state merged --limit 1000 --json title --jq 'length'`; `… --jq '[.[].title \| select(contains(":"))] \| length'`; `… --jq '[.[].title \| select(test("^[a-z0-9]+(-[a-z0-9]+)+: "))] \| length'`                                                                                                                                                                                                                            |
| C2  | 22 of the 28 pull requests merged since #650 use the kebab form                                                                                         | `gh pr list --state merged --limit 1000 --json number,title --jq '[.[] \| select(.number >= 650) \| select(.title \| test("^[a-z0-9]+(-[a-z0-9]+)+: "))] \| length'` against the same query without the second `select`                                                                                                                                                                                                                  |
| C3  | 530 bodies carry `## Summary`, 471 `## Test plan`, 58 no heading, none empty                                                                            | `gh pr list --state merged --limit 1000 --json body > "$TMPDIR/opencode/prs.json"`, then a `re.search(r'^#{1,3}\s*Summary', body, re.I \| re.M)` count per pattern (jq's `test` with the `m` flag under-counted both, so the count runs in Python); emptiness: `… --json body --jq '[.[] \| select(((.body // "") \| gsub("\\s"; "")) \| length == 0)] \| length'` → `0`                                                                 |
| C4  | Titles merged since #600 average 71.3 characters; the corpus averages 54.9                                                                              | `gh pr list --state merged --limit 1000 --json number,title --jq '[.[] \| select(.number >= 600)] \| (map(.title \| length) \| add / length)'`; the same query without the `select`                                                                                                                                                                                                                                                      |
| C5  | None of the last 15 merged pull requests carries a label                                                                                                | `gh pr list --state merged --limit 15 --json labels --jq '[.[] \| select((.labels \| length) > 0)] \| length'` → `0`                                                                                                                                                                                                                                                                                                                     |
| C6  | The release workflow generates notes with `gh release create … --generate-notes`                                                                        | `grep -n "generate-notes" .github/workflows/release.yml` → line 225                                                                                                                                                                                                                                                                                                                                                                      |
| C7  | `showcase/quality/releases` states the notes are generated from merged pull requests                                                                    | the spec's first requirement: "whose notes GitHub generates from the pull requests merged since the previous release"                                                                                                                                                                                                                                                                                                                    |
| C8  | `AGENTS.md` records that `--body-file` bypasses the template                                                                                            | the `gh pr create --body` gotcha's closing sentence: "so a line added to `.github/PULL_REQUEST_TEMPLATE.md` reaches an agent-opened pull request only if the body file carries it"                                                                                                                                                                                                                                                       |
| C9  | The cloud action derives a title under 40 characters                                                                                                    | `.opencode/skills/ship-change/SKILL.md`, "How the action derives the pull request": `summarize` → `createPR(base, branch, title, body)`                                                                                                                                                                                                                                                                                                  |
| C10 | No spec or ADR states a pull-request title or body **format**; `merge-governance`'s audit clause is the corpus's only pull-request title/body statement | three greps, one per half: `git grep -n -i "title" -- openspec/specs docs/adr` → 44 hits (35 the showcase's `title` field, 4 the `specs-auditor`'s spec-title matching, 2 `docs/adr/README.md`, 3 `merge-governance`); `git grep -n -i -E "test plan\|## Summary\|pull request body" -- openspec/specs docs/adr` → 1 hit (`merge-governance:456`); `git grep -n -i -E "pull request\|pull-request" -- docs/adr/` → 2 hits, both ADR-0016 |
| C11 | The template's header comment describes practice rather than setting a rule                                                                             | `.github/PULL_REQUEST_TEMPLATE.md` line 1 at `HEAD`: "Keep the two sections below: they mirror the shape recent pull requests in this repository follow"                                                                                                                                                                                                                                                                                 |
| C12 | No workflow reads a pull-request title or body                                                                                                          | `git grep -n -i -E "pull_request\.title\|event\.pull_request\|pull_request\.body" -- .github/workflows/` → 0 matches                                                                                                                                                                                                                                                                                                                     |
| C13 | None of the 582 pull requests merged before #600 uses the kebab form                                                                                    | `gh pr list --state merged --limit 1000 --json number,title --jq '[.[] \| select(.number < 600)] \| length'` → 582; the same query with the kebab `select` → 0                                                                                                                                                                                                                                                                           |
| C14 | `agent-skills` mandates that a step correction land in its own skill, which is where the capture's `review-gate` edit goes                              | `openspec/specs/showcase/quality/agent-skills/spec.md:2328`, the review-gate requirement: "A correction to a step SHALL be made in the skill."                                                                                                                                                                                                                                                                                           |

## Dismissals

`python3 scripts/claim-scan.py` seeded the candidates; each is resolved into a row above or dismissed here, one reason
per kind.

| Candidate kind                                                                                             | Disposition                                                                                                                                                                         |
| ---------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `[path]` candidates (`.github/PULL_REQUEST_TEMPLATE.md`, `AGENTS.md`, `docs/ideas.md`, the two spec paths) | Dismissed as a kind: each is a live file the change reads or edits, verified by opening it at `HEAD`, and the changed-path set is the diff's own                                    |
| `[absolute]` candidates in the proposal and design (`only`, `all`, `never`, `none`)                        | Dismissed as a kind: each quantifier sits on a claim whose row is C1–C14, which names its scope                                                                                     |
| `[count]` candidates                                                                                       | Rows C1–C5                                                                                                                                                                          |
| `.opencode/package.json` `[cross-ref]` candidate                                                           | Not this unit's: the OpenCode startup install rewrote the tracked `@opencode-ai/plugin` pin downward when the session moved into this worktree; the file is reverted, not committed |

## Risks

- **The standard reverses the newest practice.** 22 of the 28 pull requests merged since #650 use the prefix this
  standard drops. Existing titles stand; only new ones change. The owner should read the standard as a forward rule, not
  a retroactive correction.
- **Nothing enforces it.** A deviation reaches `main` unless a review catches it; the parked revisit names that drift as
  the trigger for a check.
- **The template header is invisible in a rendered body.** It is read at creation, which is when it matters, but it is
  not visible on an existing pull request.
