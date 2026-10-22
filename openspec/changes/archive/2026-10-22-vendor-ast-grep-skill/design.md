# Design

## Context

The repository already vendors one upstream skill set — the three `axon4to5-*` skills from `AxonIQ/agent-skills` — under
`.opencode/skills/`, with its provenance in `AGENTS.md` and the `showcase/quality/agent-skills` spec, and each vendored
tree excluded from the build's formatter, its marker and executable-bit checks, and its upstream-reference scan (the
declarations decision 2 lists). This change adds a second set the same way rather than inventing a mechanism.

## Goals / Non-Goals

**Goals:**

- Make structural search available to the agent, as a skill its description triggers on and as a stated tool preference.
- Keep `rg` for text, identifiers, and paths; keep the existing linters; add no CI gate.
- Prove the integration against this repository's own sources, including a positive control for each zero result.

**Non-Goals:**

- Any build, CI, or lint wiring — no build task runs a structural search, and `check`'s only contact with the CLI is the
  doctor's optional `--version` probe (`testDoctorCharter` runs the diagnostic, which probes every declared tool); the
  probe reports and never fails the build.
- A `sgconfig.yml`, project rules directory, or MCP server: the skill teaches inline `run`/`scan` usage, which is what a
  one-off query needs.
- Editing the upstream skill. It is copied byte-for-byte; the project-specific invocation lives in `AGENTS.md`, which is
  always loaded, so the vendored copy stays refreshable by `diff`.

## Decisions

1. **Vendor, do not modify, and state the preference separately.** Upstream's own README documents that installing a
   skill makes it discovered, not preferred — the agent also holds a text-search tool that needs no setup, so it takes
   that unless told otherwise. The tool-choice rule therefore lives in `AGENTS.md` (always loaded), not in an edited
   copy of `SKILL.md`.
2. **Every declaration of the set is updated, and `AGENTS.md` names them.** Vendored-set membership is declared in four
   places — the Spotless `targetExclude` and the marker task's input `fileTree` in `build.gradle.kts`,
   `VENDORED_SKILL_PREFIXES` in `scripts/commit-hygiene.py` (which scopes both the marker check and the executable-bit
   check), and `EXCLUDED_SKILL_PREFIXES` in `build-logic/src/main/kotlin/UpstreamReferenceScope.kt` — and a new set must
   join all four. Single-sourcing them is a cross-language refactor (Gradle, Python, Kotlin) that this unit does not
   undertake; the sweep is recorded in `docs/ideas.md` with its retrigger, and the `AGENTS.md` bullet names each site so
   the next set's author reads the list rather than guessing.
3. **The CLI joins the doctor as an optional probe.** The optional class already covers "the agent CLIs", and the
   diagnostic is not part of `check`, so a machine without ast-grep still builds and tests; a missing CLI is reported
   with its install command. The README Prerequisites row and the `scripts/test-doctor.sh` mapping are what the
   agreement check requires in the same edit.
4. **No spec delta on `toolchain-check`.** Its requirements state the probe mechanism and name the optional class ("the
   agent CLIs"); they do not enumerate the probe set, and the required set is unchanged.

5. **The commit anchors both sets; the hashes make the small one clone-free.** Neither upstream publishes a release tag
   or a release (`gh api repos/{AxonIQ/agent-skills,ast-grep/agent-skill}/tags` and `…/releases` → 0 for each; `0.2.2`
   and `1.0.0` are `plugin.json` fields), so a version alone pins neither copy. Both copies were then anchored
   clone-free through the tree API: every vendored AxonIQ file matches the upstream tree at `518acd865a` (the 0.2.2
   merge, where the manifest reads 0.2.2) — the same files at the 0.2.2 bump commit `edfb88b` differ in six, so the ref
   matters — and both `ast-grep` files match `f2175af`. The delta therefore requires the commit per set and lets the
   two-file set record its blob hashes as well, so that copy can be checked without fetching the upstream tree; it says
   nothing about how an update obtains the new snapshot.

## Risks / Trade-offs

- **A structural pattern is only as good as its node shape.** The failure mode is a wrong pattern read as an absence.
  The rule states the two mitigations: pick the qualified or bare form the code actually uses, and prove the pattern on
  a snippet before trusting a zero.
- **A vendored tree drifts if it is formatted.** The formatter control below shows the exclusion is load-bearing rather
  than decorative.
- **A skill the loader silently drops is indistinguishable from one that works**, which is why the discovery probe runs
  against the loader's own inventory instead of the file's presence.

## Verification and claims

Run against this repository's working tree; each row is the change's evidence for one stated claim, and each command
reproduces its result.

| Claim or check                           | Command and result                                                                                                                                                                                                                                                                     |
| ---------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Upstream provenance                      | `git log --oneline -1` in the clone at `f2175af` → `f2175af` (2026-09-13); `plugin.json` → `1.0.0`, `MIT`, and the same manifest read through `gh api …/contents/…?ref=f2175af` agrees; no `LICENSE` file in the tree                                                                  |
| The vendored copy is identical           | `diff -r <clone>/ast-grep/skills/ast-grep .opencode/skills/ast-grep` → no output; both files' blob hashes equal the `sha` the contents API returns at `ref=f2175af` (`SKILL.md` `1c6e3afd…`, `rule_reference.md` `9821e35f…`), and those hashes are recorded in `AGENTS.md`            |
| The AxonIQ copy corresponds to one ref   | `gh api repos/AxonIQ/agent-skills/git/trees/518acd865a?recursive=1` + `git hash-object` per vendored file → every file matches; at the 0.2.2 bump commit `edfb88b` six of them differ                                                                                                  |
| Neither upstream publishes a release tag | `gh api repos/{AxonIQ/agent-skills,ast-grep/agent-skill}/tags` and `…/releases` → 0 for each; `git ls-remote --tags` on both lists no ref                                                                                                                                              |
| The reference resolves from it           | `ls .opencode/skills/ast-grep/references/rule_reference.md` → present; `SKILL.md` names that path                                                                                                                                                                                      |
| The loader discovers the skill           | private server in the project dir, `GET /api/skill` → 35 skills incl. `ast-grep`; baseline 34 without it                                                                                                                                                                               |
| The doctor probes the CLI                | `./scripts/doctor.sh` → `ast-grep satisfied 0.51.0`; without it on `PATH` → `missing brew install ast-grep`                                                                                                                                                                            |
| The doctor/README agreement fires        | `./scripts/test-doctor.sh` → 14 probes; before the README row it failed on that row                                                                                                                                                                                                    |
| The formatter skips the vendored tree    | `spotlessCheck` green; fails on `ast-grep/SKILL.md` with the `targetExclude` line removed                                                                                                                                                                                              |
| The marker/exec-bit scope skips it       | `testCommitHygiene` green; the new scope case fails with the prefix removed                                                                                                                                                                                                            |
| The skill tree is on no build path       | `grep -n '\.opencode' build.gradle.kts` → the formatter's `target`/`targetExclude` entries, plus `upstreamReferences`, `verifyCapturedMarkers` and `verifyDocumentedPins` inputs; none is a source set. The CLI's only build-time execution is the doctor's optional `--version` probe |
| Q1 property surfaces                     | `ast-grep run -p '@ConfigurationProperties($$$ARGS)' --lang java --json . \| jq length` → 5; `rg` → 5                                                                                                                                                                                  |
| Q2 `@Bean` methods                       | `ast-grep run -p '@Bean' --lang java --json . \| jq length` → 56; `rg -o '@Bean'` → 56                                                                                                                                                                                                 |
| Q3 relational query                      | inline rule `all: [kind: method_declaration, has: {pattern: "@Test", stopBy: end}, has: {pattern: $X.block(), stopBy: end}]` → 8 sites in 4 files; `rg -l '\.block\(\)' --glob '*Test.java' --glob '*CT.java'` → 3 files                                                               |
| Q4 web UI hooks                          | `ast-grep run -p 'useMutation({ $$$B })' --lang ts --json . \| jq length` → 4                                                                                                                                                                                                          |
| Q5 a true absence                        | `@KafkaListener($$$ARGS)` → 0; `rg -c KafkaListener --glob '*.java'` → no file; the same pattern matches a `--stdin` snippet                                                                                                                                                           |
| Q6 qualified vs bare forms               | two one-line snippets: each pattern matches only its own form; in-repo the bare form finds 824, qualified 0                                                                                                                                                                            |

**Dismissals.** `scripts/claim-scan.py --base origin/main` fires on shape rather than on claimhood: a `[path]` or
`[cross-ref]` candidate is a pointer a reader resolves against the repository (or an upstream coordinate that is itself
a row's source), an `[absolute]` word inside `AGENTS.md`'s rule is the rule's normative content rather than an evidence
claim, the vendored `SKILL.md`'s words are upstream text this change copies, and the `[count]` candidate is the arrow in
"Axon 4→5", not a tally.
