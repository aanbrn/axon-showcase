# Design

## Context

The audit report is `docs/audits/2026-11-07.md`, merged at `21b100bf`; its agent-tooling section names the config-guard
tracking bullet and routes the correction as a small docs/consistency unit, separate from the spec-corpus fixes the
owner moved to `apply-2026-11-07-audit-findings`. No capability spec states the bullet's rule, which `skip_specs`
records.

## Decisions

### D1 — The arm is marked retired, not deleted

The rule exists to tell a future editor which upstream report still gates a live guard; deleting the retired arm would
lose that record. The bullet keeps the arm in the past tense and names the mechanism that replaced it
(`openspec validate --all` failing on an unparseable config).

### D2 — The live arm keeps its `#1894` key and the command's whole-file control

`Fission-AI/OpenSpec#1894` is still open and unmerged, so the `must be an array of strings` arm and its
`/opsx-tool-update` controls stay live; the whole-file control stays because it exercises the failure the retired arm
handed over to.

### D3 — The marker records the unit that rewrote the bullet

The bullet's trailing markers already name its origins; the applying unit appends its own marker at the end of the rule,
as `apply-2026-09-28-audit-docs-findings` did when it rewrote a rule (commit `7545efe8` ends that ADR bullet with its
marker).

## Risks

- **The marker lands outside the rule block.** Mitigated by appending it to the last sub-bullet, where the existing
  markers end the rule, and by `verifyCapturedMarkers`.
- **A stale retirement claim.** Mitigated by the claims table re-resolving `#1894`'s state before the manual pass.

## Verification

- `./gradlew spotlessApply` then `spotlessCheck`.
- `./gradlew verifyCapturedMarkers`.
- `openspec validate --all`.

## Claims table

The base revision is `634bc3b5ba17815ee9f83c467b7116dcd7eacc3a` (`git merge-base HEAD origin/main`).

| Claim                                                                                  | Source                                                                                                                           | Reproduces                                                                                                                                                                                                                                                                                         |
| -------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| The bullet lists the `could not parse` arm as a live guard                             | `git show 634bc3b5:AGENTS.md` (the config-guard bullet)                                                                          | two sub-bullets under "Each guard retires on its own condition"                                                                                                                                                                                                                                    |
| The `openspec` pin is `1.16.0` in all three workflows                                  | `git grep -n 'fission-ai/openspec' 634bc3b5 -- .github/workflows`                                                                | three hits, each `@1.16.0`                                                                                                                                                                                                                                                                         |
| `ci.yml` holds only the array-shape probe                                              | `git show 634bc3b5:.github/workflows/ci.yml`                                                                                     | the `must be an array of strings` grep, with its comment naming validate                                                                                                                                                                                                                           |
| The pin bump is the archived change the bullet cites                                   | `git ls-tree --name-only 634bc3b5 openspec/changes/archive/ \| grep bump-openspec-cli-2026-11-01`                                | `2026-11-01-bump-openspec-cli-2026-11-01`                                                                                                                                                                                                                                                          |
| The `#1894` fix has not shipped                                                        | `gh pr view 1894 --repo Fission-AI/OpenSpec --json state,mergedAt`                                                               | `state: OPEN`, `mergedAt: null`                                                                                                                                                                                                                                                                    |
| No capability spec states the config-guard tracking rule                               | `git grep -n 'could not parse' 634bc3b5 -- openspec/specs`                                                                       | no hits                                                                                                                                                                                                                                                                                            |
| The 2026-09-28 audit unit appended a `captured:` marker to a rule it rewrote           | `git show 7545efe8 -- AGENTS.md`                                                                                                 | the ADR bullet rewrite ends with `captured: apply-2026-09-28-audit-docs-findings`                                                                                                                                                                                                                  |
| `Fission-AI/OpenSpec#1892` closed 2026-10-30                                           | `gh issue view 1892 --repo Fission-AI/OpenSpec --json state,closedAt`                                                            | `state: CLOSED`, `closedAt: 2026-10-30T19:52:13Z`                                                                                                                                                                                                                                                  |
| Its fix `#2127` merged 2026-10-30                                                      | `gh pr view 2127 --repo Fission-AI/OpenSpec --json state,mergedAt`                                                               | `state: MERGED`, `mergedAt: 2026-10-30T19:52:11Z`                                                                                                                                                                                                                                                  |
| `Fission-AI/OpenSpec#1891` closed 2026-09-29                                           | `gh issue view 1891 --repo Fission-AI/OpenSpec --json state,closedAt`                                                            | `state: CLOSED`, `closedAt: 2026-09-29T18:37:10Z`                                                                                                                                                                                                                                                  |
| Its shipped fix `#1984` names the offending item and does not change the drop behavior | `gh pr view 1984 --repo Fission-AI/OpenSpec --json title,state,mergedAt,body,files`                                              | `state: MERGED`, `mergedAt: 2026-09-29T18:37:09Z`; title "fix(config): name the offending rules item when a list is malformed"; body "Parsing behavior is unchanged — the malformed artifact's rules are still dropped wholesale"; files the changeset, `src/core/project-config.ts`, and its test |
| `agent-skills` requires approved findings of different routes to be split              | `git show 634bc3b5:openspec/specs/showcase/quality/agent-skills/spec.md`                                                         | the requirement "An audit's approved findings are delivered as separate units"                                                                                                                                                                                                                     |
| The audit report merged at `21b100bf`                                                  | `git log -1 --format='%h %s' 21b100bf`                                                                                           | `21b100bf Record the 2026-11-07 repository audit report (#863)`                                                                                                                                                                                                                                    |
| `validate` fails on an unparseable config under `1.16.0`                               | `git show 634bc3b5:openspec/changes/archive/2026-11-01-bump-openspec-cli-2026-11-01/design.md`                                   | the probe-replay row: a dropped closing quote → `validate` exit 1, `Error: Could not parse …`                                                                                                                                                                                                      |
| Release `v1.16.0` contains `#2127`                                                     | `gh api repos/Fission-AI/OpenSpec/compare/ae7ad75025b5b84f605e9616692ec97f9fb09f28...v1.16.0 --jq '{status,ahead_by,behind_by}'` | `status: ahead`, `behind_by: 0`                                                                                                                                                                                                                                                                    |
| `/opsx-tool-update` carries the list-shape positive controls                           | `git show 634bc3b5:.opencode/commands/opsx-tool-update.md`                                                                       | step 3 exercises one positive control per warning, `must be an array of strings` included                                                                                                                                                                                                          |

Re-derived after the implementation's last edit: every row's source was re-run at the base revision, and each reproduces
as stated.

### Dismissals

- **`path` candidates** — a path, or a path-shaped phrase such as "docs/consistency", named in a task, the proposal, or
  a design sentence, or inside quoted rule text; each names a file or command the change's own artifacts reference (or a
  phrase rather than a path), not a claim about a corpus, and the rows above carry the ones that assert evidence.
- **`absolute` candidates** — words such as "only", "no", "every", and "all" inside quoted rule text, task instructions,
  and scope statements; the scope statements are verified by the diff itself.
- **`count` candidates** — a task number and its verb, not a count of any set.
- **`cross-ref` candidates** — a `#NNN` reference inside a `captured:` marker's accumulated history (such as `#375`),
  which names the pull request that introduced the rule rather than asserting a fact about a corpus.
