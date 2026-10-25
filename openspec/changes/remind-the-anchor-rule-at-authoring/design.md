# Design

## Context

- The rule lives at `.opencode/skills/review-gate/SKILL.md` §1 step 2 ("A site named for a later sweep is quoted by its
  text or token, never by a line number"). It is absent from `AGENTS.md`, `openspec/config.yaml`, and the two
  `openspec-propose` / `openspec-apply-change` guidance files (claims row 1) — so the documented authoring route does
  not present it.
- `AGENTS.md`'s capture section now states the general rule this change applies: "A rule lives under the trigger that
  fires before every case it governs … place it where every such case passes, not where its incident was read"
  (`captured: move-tracker-guard-to-shipping-path`).
- This unit **is** the merge-time capture of `stage-new-files-before-manual-review` (PR #691): its site table keyed rows
  on `AGENTS.md:NNN`, and its branch advanced after the planning round, so the design reads every anchor at a fixed
  revision and names each row's text beside its number (claims row 9). The owner confirmed running the capture.
- `docs/guidance-evidence/` documents `AGENTS.md`'s own rules — its `Rule:` paragraphs pair with root `evidence:` tokens
  — so it does not own a skill rule's evidence (claims row 6).

## Goals / Non-Goals

**Goals:** put the anchoring rule in front of the author at artifact-creation time; keep the rule single-sourced.

**Non-Goals:** rewriting the rule; changing the legitimate `file:line` use for a review finding; touching the generated
`openspec-*` skills; adding a check (see Capture disposition).

## Decisions

**1. A pointer in both `rules.design` and `rules.tasks`, worded so the author can find the rule.** The item's source
form (YAML folds the two lines into one space-joined string, which is what the CLI delivers), identical in both lists:

```
- 'Before naming a site for a later sweep, read and apply the rule identified by "quoted by its text or token"
in `.opencode/skills/review-gate/SKILL.md` §1 step 2.'
```

- It names the section and quotes the rule's own searchable phrase (`quoted by its text or token`; `grep -c` in the
  skill → 1) without restating the normative sentence, so the rule stays single-sourced.
- Its trigger repeats the rule's own words ("a later sweep"), so it neither widens nor narrows the rule.
- Rejected: copying the rule into the lists — the duplication the single-sourcing invariant forbids, and two copies to
  drift.
- Rejected: moving the rule into `AGENTS.md` — the gate's procedure belongs to the skill (#665).
- Rejected: one list only — both artifacts name sites for a later sweep, and the delivery test covered the pair.
- Rejected: adding the pointer to `rules.proposal` and `rules.specs` — a proposal names the files a change touches and a
  spec states requirements; neither identifies an edit site by a coordinate a later merge could stale, so the rule's
  hazard does not arise there.

**2. The provenance lives in this archived change, not on the config lines.** The `captured:` marker guard reads
`AGENTS.md` and the project-authored `.opencode/` definitions, not `openspec/config.yaml` (claims row 7), so a marker on
a config rule would be inert text inside an instruction list. The change directory's name dates the record.

**3. The behavioural evidence is recorded with its limits.** It supports the route; it does not establish a general
effect (claims row 5).

## Capture disposition

The unit is a merge-time capture, so its promotion-gate determination is recorded here:

- **True:** the rule exists by that name and its placement is measurable (claims rows 1, 3).
- **Actionable:** one added item per list; no other edit.
- **Not automatable at reasonable cost:** a lint could flag `file:NNN` in planning artifacts, but a line number is
  legitimate for a finding's instance (`file:line` and the claim) and for evidence at a fixed revision, so a check would
  have to judge whether a reference names a future site — semantic, not lexical.
- **Material:** #691 keyed its site table on line numbers a branch advance invalidates; its own mitigation — each row
  naming its text beside its number — is what this capture moves in front of the author (claims row 9).
- **General:** the pointer sits in the `tasks` list every change's authoring reads, and in the `design` list for a
  change that writes one.
- **High-confidence, known scope:** the change alters a pointer, not the rule's obligation.

It is an **authoring-system recommendation** in the capture's sense: the fix belongs in the mechanism that produces
artifacts — the config's injected rules — not in `AGENTS.md`.

A second candidate — a rule saying a config rule is verified through what the CLI delivers, because the file's YAML
folds a multi-line item into one line — is **`already covered`**: `AGENTS.md`'s config gotcha already says "do not
substitute a parser of your own … probe the consumer's read path", and `openspec instructions` is that path. The chosen
fix takes the fold observation as its own rationale for the locator wording; no root rule and no growth. The observation
reproduces: `grep -c 'read and apply the rule identified by "quoted by its text or token" in ' openspec/config.yaml` →
`0`, while `openspec instructions design --change remind-the-anchor-rule-at-authoring --json` carries it as one line.

## Risks / Trade-offs

- **A malformed rule item silently drops a list's rule set** → the CI `build` job's quality-gates step runs
  `openspec new change` and fails on `must be an array of strings` or `could not parse` (claims row 8); no local Gradle
  task reads the config (claims row 10), so `check` alone cannot catch it.
- **The pointer could be read as the rule's owner** → it names the skill and the step, and the rule's text stays there.
- **A reader may over-read the probes as proof the violations stop** → claims row 5 states what they establish and what
  they do not.

## Claims table

| #   | Claim                                                                                                                                                                                 | Source / command                                                                                                                                                                                                       | Result                                                                                                                                                                                                                                                                           |
| --- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1   | At base `efbe072c` the anchoring rule is not in `AGENTS.md`, the config, or the two writer skills                                                                                     | `git grep -c "quoted by its text or token" efbe072c -- AGENTS.md openspec/config.yaml .opencode/skills/`                                                                                                               | only `.opencode/skills/review-gate/SKILL.md`: 1; every other path absent                                                                                                                                                                                                         |
| 2   | The rule predates the incident unit                                                                                                                                                   | `git log --oneline -S "quoted by its text or token"`; the incident's merge date from `gh pr view 691 --json mergedAt`                                                                                                  | introduced by `5ed8037d` (#605, 2026-10-16), moved into the skill by `b59d57c7` (#665); #691 merged 2026-10-25 — the rule existed first                                                                                                                                          |
| 3   | The change delivers the pointer to both artifact authors                                                                                                                              | before: `git show efbe072c:openspec/config.yaml`, rule items `rules.design` 9 and `rules.tasks` 11; after: `openspec instructions design --change remind-the-anchor-rule-at-authoring --json` and the same for `tasks` | 10 and 12, the pointer text present verbatim in both `rules` arrays                                                                                                                                                                                                              |
| 4   | The rule stays single-sourced in the skill                                                                                                                                            | `grep -c "never by a line number" openspec/config.yaml .opencode/skills/review-gate/SKILL.md`                                                                                                                          | `0` and `1` — the pointer carries the locating phrase, not the obligation                                                                                                                                                                                                        |
| 5   | Behavioural probes: with the rule present (prompt, a one-line file, or an entry in the rules payload) site identifiers were textual in every sample; with it absent they were numeric | 2 arms × 2 samples per design, one model, prompts recorded in the consultation                                                                                                                                         | supports the delivery route; establishes no general rate, no causal role for salience or volume, and not the incident's cause (the rule was already in context there)                                                                                                            |
| 6   | `docs/guidance-evidence/` pairs root rules by `Rule:`/`evidence:`, so a skill rule's evidence is not its subject                                                                      | `sed -n '1,12p' docs/guidance-evidence/documentation-and-durable-claims.md`                                                                                                                                            | the header states the pairing and that its sections mirror `### Documentation & durable claims`                                                                                                                                                                                  |
| 7   | The `captured:` marker guard does not read `openspec/config.yaml`                                                                                                                     | `sed -n '487,500p' build.gradle.kts`                                                                                                                                                                                   | its inputs are `AGENTS.md`, `.opencode/agent/*.md`, `.opencode/skills/*/SKILL.md` (vendored trees excluded), and `scripts/commit-hygiene.py`                                                                                                                                     |
| 8   | The CI gate fails a malformed rule list                                                                                                                                               | `grep -n "array of strings" .github/workflows/ci.yml`                                                                                                                                                                  | `ci.yml:109` greps `must be an array of strings\|could not parse` and fails on either                                                                                                                                                                                            |
| 9   | #691 keyed its site table by line number, and its branch advanced after the planning round                                                                                            | `grep -n "AGENTS.md:[0-9]" openspec/changes/archive/2026-10-25-stage-new-files-before-manual-review/design.md`; `sed -n '5,8p'` of the same file                                                                       | the table's sites are `AGENTS.md:329–330`, `:340`, `:343`, …; the Context records a refresh from `origin/main` after the planning round, reads every anchor at that revision, and names each row's text "so a later refresh moves the numbers without moving the identification" |
| 10  | No local Gradle task or script reads `openspec/config.yaml`                                                                                                                           | `grep -rn "openspec/config.yaml" build.gradle.kts build-logic/src scripts/`                                                                                                                                            | no match in the build files or scripts; the CLI is the config's reader, and the CI probe is the only gate that fails on a malformed list                                                                                                                                         |

## Open Questions

None that change the approach. Whether the pointer reduces violations in ordinary sessions is unmeasurable here; the
record says so rather than promising it.
