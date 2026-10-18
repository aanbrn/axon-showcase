# Design

## Context

`openspec/config.yaml`'s `rules` are the per-artifact text the CLI injects when an artifact is drafted, and `ci.yml`'s
probe gates only their _shape_ — that each item is a string — never what they say. No capability spec covers their
content either: `merge-governance` owns the readability probe, and the `agent-skills` spec owns the `agents-auditor`'s
routing to them, not their wording.

Two claims about the mechanism decide the fix, and both were verified rather than assumed:

- The rules are injected **per artifact**: in a scratch root, `openspec instructions proposal --json` returned
  `rules.proposal` alone, and the proposal's instruction did not mention the `rules.design` item
  (`projectConfig.rules[artifactId]` in the CLI's `instruction-loader.js`).
- A `## DEPRECATED Requirements` header is **warned about and dropped at archive** while `validate` exits 0 (a scratch
  root; the CLI's `DELTA_HEADER` accepts only `ADDED|MODIFIED|REMOVED|RENAMED`).

## Goals / Non-Goals

**Goals:**

- Correct the config's rules where they misstate the CLI.
- Complete, at the artifact where the failure was recorded, the clauses whose absence let it recur.

**Non-Goals:**

- No change to the config's `context:` block, its `operations` guidance, or its schema.
- No semantic gate on the rules' content — the probe stays shape-only (the follow-up is parked by task 3.4).
- No restructuring of the rules into a shared source: the per-artifact injection makes a cross-artifact pointer useless.

## Decisions

**The count clause is repeated identically, not single-sourced.** A cross-artifact pointer cannot work: the drafting
agent sees only its own artifact's rules, so `rules.proposal` referencing `rules.design` would show it nothing. The
single-sourcing rule's own remedy for a site that cannot reference by name applies — repeat it identically — so the
clause is aligned to one wording and added where the recorded failure happened.

**`RENAMED` replaces `DEPRECATED`.** The CLI's `DELTA_HEADER` accepts only `ADDED|MODIFIED|REMOVED|RENAMED`, so a
`## DEPRECATED Requirements` section is not a delta operation. The rule names the `FROM:`/`TO:` form so the header is
unambiguous.

**The `Impact` rule widens to the artifact's own surface.** The config named only build/tests/deployment, while the
proposal template's Impact section covers "Affected code, APIs, dependencies, or systems" — so a change touching only an
API or a dependency satisfied the narrower wording while leaving its impact unstated.

**No `## Purpose` refresh is owed.** No capability spec's scope changes.

## Risks / Trade-offs

- **A wrong operation name can regress silently** → the shape probe cannot see it; a content check against the CLI's own
  operation set is parked by task 3.4.
- **The rules grow slightly** → four rule items added across `rules.proposal` and `rules.tasks`, each carrying a
  recorded failure.

## Migration Plan

None: the config takes effect on the next drafting command. No spec edit is owed (`skip_specs`), so the implementing
PR's diff is the config file, the change dir, this unit's records, and the docs the unit itself causes (`AGENTS.md`'s
capture and the `docs/ideas.md` sweep).

## Claims table

| Claim                                                                                                                             | Source                                                                                                                                                                                    |
| --------------------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| The rules are injected per artifact only                                                                                          | scratch root: `openspec instructions proposal --json` returned `rules.proposal` alone with no `rules.design` item; `projectConfig.rules[artifactId]` in the CLI's `instruction-loader.js` |
| `DEPRECATED` is not a delta operation, and a `## DEPRECATED Requirements` section is dropped at archive                           | scratch root: `openspec validate` warned "not a delta section, so it is ignored" and "not carried into the main spec by archive", exit 0; the CLI's `DELTA_HEADER` regex                  |
| The count clause diverges between `rules.proposal` and `rules.design`                                                             | `git show HEAD:openspec/config.yaml`                                                                                                                                                      |
| The CI probe gates the rules' shape, not their content                                                                            | `git show HEAD:.github/workflows/ci.yml` (the probe greps for `must be an array of strings\|could not parse`)                                                                             |
| The `Impact` rule named only build/tests/deployment                                                                               | `git show HEAD:openspec/config.yaml`                                                                                                                                                      |
| No capability spec covers the config's rules' content                                                                             | `grep -rn "per-artifact" openspec/specs/` — `merge-governance` (the readability probe) and `agent-skills` (the auditor's routing) only                                                    |
| The single-sourcing clause is absent from `rules.proposal` and `rules.tasks`; the count clause is absent from `rules.tasks` alone | `git show HEAD:openspec/config.yaml`; `git show HEAD:docs/ideas.md` (the parked entry this change removes)                                                                                |

**Dismissals** (scan candidates resolved to neither a row nor a fact, one reason per kind): **absolute quantifiers
(`Every`, `every`, `never`, `only`, `None`) and numeric prefixes** are modal, structural, or the change's own task and
section numbering, not facts about the repository; **counts in the change's own `Done` notes and command outcomes** (a
rule count, a row count, a validator total) are that command's result, reported by its class rather than enumerated;
**paths, commands, and cross-references** are name, path, or command references rather than claims about the repository;
**the `docs/review-rounds.tsv` candidates** are this unit's own appended record rows, a round number and a class label
in a one-line record, not a claim; **the `openspec/config.yaml` diff's own rule text** is the artifact this change
authors, so its quantifiers are the content being written, not claims about the repository.
