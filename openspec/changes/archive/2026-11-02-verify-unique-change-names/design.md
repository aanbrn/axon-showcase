# Design

## Context

`openspec/changes/archive/` holds every archived change under a `YYYY-MM-DD-` prefix, so the directory name is where a
change's name is recorded for good. The repository already strips exactly one date segment to resolve one: the
pre-commit guard's assigned-change rule maps a branch name to an archived directory through `ARCHIVED_CHANGE_RE`, and
`change_names` collects both live and archived names into a set.

A change's name is unique by convention only. `fix-openspec-config-rules` names two archived changes — `#218`
(`2026-09-14`) and `#624` (`2026-10-18`) — and the capture after the second one found the collision: `git log --grep`
matches both changes' commits, so a reader looking for "the change that fixed the config rules" gets two answers. The
generated `openspec-propose` skill guards a collision with an **active** change ("If a change with that name already
exists, ask if user wants to continue it or create a new one"), and the archive's date prefix puts archived names
outside what that guard compares. No verification compares archived names with each other, though the pre-commit guard
reads them for a different question: `change_names` resolves a branch name and `find_foreign_change_paths` judges a
staged change path, both at the commit boundary rather than at `check`.

The archive carries exactly one duplicated name today (claims table row 1), so a verification added without an exemption
would fail `check` on `main` at once — and that pair is the evidence the parked idea cites.

## Goals / Non-Goals

**Goals.**

- Two archived directories carrying the same change name fail the CI `build` gate, naming the name and the directories.
- The acknowledged historical pair stays as recorded, and the exemption covers that pair rather than the name.
- The date-prefix rule keeps its one implementation.

**Non-Goals.**

- Renaming an archived directory, or any other edit to the archived record.
- Comparing a live change's name with an archived one: this gate sees a name only once the live change is archived, so
  that collision surfaces at the archive's merge rather than at propose time, and `openspec-propose` compares active
  changes with each other only.
- Folding this into the pre-commit guard: a name collision is created by an archive, which `check` sees on every pull
  request, while the guard is per clone and bypassable.

## Decisions

**1. A new `--unique-change-names` mode and its own `check` task, beside its two siblings.**

- Chosen: a distinct mode, registered in `MODE_FLAGS`, with `verifyUniqueChangeNames` in the `check` block next to
  `verifyUniqueCronSchedules` and `verifyUniqueReviewRounds`.
- Rejected — a clause inside `--unique-rounds`: that mode's name and its requirement carry ledger row uniqueness,
  contiguity, `--expect`, and `--expect-clean`; a naming subject there makes one requirement state two subjects, and the
  mode-coverage control keys a test class to a mode.
- Rejected — the pre-commit guard: activated per clone and bypassable with `--no-verify`, so a duplicate would still
  reach `main`.

**2. A change name is the archived directory's name with one leading `YYYY-MM-DD-` removed, read through the existing
`ARCHIVED_CHANGE_RE`.**

- Chosen: one helper walks the archive and derives each directory's name through the regex `change_names` already
  applies, and `change_names` builds its archived half from that helper, so an archived directory's name has one
  derivation rather than two walks that agree by inspection. An `archive/` entry whose name carries no such prefix names
  no change and is not judged, matching the assigned-change rule's treatment of the same entry.
- Rejected — a second pattern for the same shape: two patterns drift, and the guard's branch resolution would then
  disagree with this gate about a directory's name.
- Rejected — a second walk beside `change_names`'s: the two would encode the same entry rules (a directory, a matching
  date segment) and drift the first time one of them changes.

**3. The one historical collision is acknowledged as that exact pair, with its reason, in the checker.**

- Chosen: an acknowledgement constant maps `fix-openspec-config-rules` to the two directory names and the reason; a
  group equal to that pair passes, and any other group — a third directory carrying the name, or a duplicate of another
  name — fails. The reason travels with the exemption, so a reader judges it where it applies. A control reads the live
  archive, so the constant cannot outlive its collision: renaming or removing a member of the pair fails the test, and
  the constant is retired with it rather than lingering as a permission for the name.
- Rejected — renaming the archived directory: the symptom the idea names is `git log --grep`, which matches **commit
  messages**, so a directory rename leaves both commits matching while the archive's name no longer agrees with the
  ledger's `fix-openspec-config-rules` rows and the commit subject. It buys nothing and rewrites the record.
- Rejected — a date boundary (every collision before the gate passes): wider than the one pair it is meant to cover, so
  a pre-boundary duplicate pair would stay invisible forever.
- Rejected — no exemption: `check` would fail on the tree that introduces it, inventing a gate that ships red.

**4. A missing `openspec/changes/archive/` fails, naming it.**

- Chosen: the verification's subject is that directory, so its absence is a failed read the check reports rather than a
  clean empty archive. `--ideas-sections` treats a missing `docs/ideas.md` the same way.
- Rejected — passing on absence: an empty read would be indistinguishable from an archive with no duplicates.

## Risks

- **The acknowledgement is a suppression surface.** A future duplicate could be "fixed" by widening it. The requirement
  states the narrow scope — one pair, with its reason, never a name generally — and the controls assert a third
  directory carrying the acknowledged name fails.
- **Names only, not history.** The gate prevents a new collision; it cannot repair the acknowledged one, and it does not
  tell a reader which of the two archived changes a `git log` hit refers to. That repair is a naming decision the owner
  has not taken.
- **The build input is the archive tree, not the names the check reads.** `inputs.dir("openspec/changes/archive")`
  fingerprints every archived file, while the verification reads only directory names. A configuration-time list of
  those names would be cheaper but is replayed from the configuration cache, so a directory archived after the store
  would go unscanned; the tree input is the declaration that survives that. The fingerprint's cost is unmeasured.

## Claims

Base revision for the before-state rows: `git merge-base HEAD origin/main` = `f0486b6f`.

| #   | Claim                                                                            | Source                                                                                                                                                                        | Result                                                                                                                                                                                                                                                                                                                                                                                         |
| --- | -------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1   | The archive carries exactly one duplicated change name: the known pair           | `ls openspec/changes/archive \| wc -l` and the date-stripped `uniq -d`                                                                                                        | 445 directories; the only duplicate is `fix-openspec-config-rules`                                                                                                                                                                                                                                                                                                                             |
| 2   | The symptom the idea names reproduces                                            | `git log --oneline --grep fix-openspec-config-rules`                                                                                                                          | both changes' commits appear (`0c23ac46` #218, `9512cd41` #624)                                                                                                                                                                                                                                                                                                                                |
| 3   | The repository already resolves an archived directory's change name              | `ARCHIVED_CHANGE_RE` and `change_names` in `scripts/commit-hygiene.py`                                                                                                        | one date segment stripped, live and archived; the archived walk is now the one `archived_change_names` shares                                                                                                                                                                                                                                                                                  |
| 4   | No verification compares archived change names with each other                   | `git grep -n 'change_names\|ARCHIVED_CHANGE_RE' -- scripts`                                                                                                                   | two callers, both in the pre-commit guard's assignment and foreign-path resolution; neither compares a name with another, and no `check` task resolves an archived change name                                                                                                                                                                                                                 |
| 5   | The generated propose skill guards a collision with an active change             | `.opencode/skills/openspec-propose/SKILL.md`, its name-collision guardrail                                                                                                    | the step asks whether to continue the existing change or create a new one, and every name it compares is an active `openspec/changes/<name>/`, so an archived name is not among them                                                                                                                                                                                                           |
| 6   | The gate's bad state is reachable and it fires                                   | a planted `openspec/changes/archive/2026-11-03-gate-manual-review-readiness` beside the real `2026-10-28-gate-manual-review-readiness`                                        | `Task :verifyUniqueChangeNames FAILED`, naming `'gate-manual-review-readiness' (2026-10-28-gate-manual-review-readiness, 2026-11-03-gate-manual-review-readiness)`                                                                                                                                                                                                                             |
| 7   | The live tree passes                                                             | `./gradlew verifyUniqueChangeNames`                                                                                                                                           | BUILD SUCCESSFUL; the acknowledged pair exempt and every other archived name distinct                                                                                                                                                                                                                                                                                                          |
| 8   | The controls bite                                                                | mutation battery over `scripts/commit-hygiene.py`                                                                                                                             | predicate returning no duplicate → 5 of the class's 10 cases fail; acknowledgement table emptied → 4 (three errors, one failure); exemption widened from the pair to the name → 1 (`test_a_third_directory_carrying_the_acknowledged_name_fails`); a prefix-less entry falling back to its own name as the group → 1 (`test_an_entry_without_a_date_prefix_is_not_judged`); restored → 10 pass |
| 9   | The mode joins the script's exposed modes and their test coverage                | the equality control plus the module run                                                                                                                                      | the mapping names every mode the checker exposes; `Ran 286 tests ... OK`                                                                                                                                                                                                                                                                                                                       |
| 10  | The verification is part of the standard build                                   | `build.gradle.kts` and `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`                                                                                              | `dependsOn("verifyUniqueChangeNames")`, and the Docker-free `check` BUILD SUCCESSFUL                                                                                                                                                                                                                                                                                                           |
| 11  | The build input is the archive tree while the verification reads directory names | `build.gradle.kts` (`inputs.dir("openspec/changes/archive")`), `archived_change_names` in `scripts/commit-hygiene.py`, and the `gradle-build` pack's configuration-cache rule | `iterdir()` yields names only; the task declares the tree; a configuration-time list is replayed from the cache, so a directory archived after the store is not scanned                                                                                                                                                                                                                        |

Dismissals, one reason per candidate kind `scripts/claim-scan.py` reports:

- `[count]` — a figure a named row derives (an archive tally, a test count, a gate's exit status); each is stated with
  that row's source, never in this list.
- `[absolute]` — a universal qualifier inside a requirement, a decision, or a control scenario is normative text rather
  than a claim about the repository; the factual absolutes (the single duplicated name, the absence of a name-comparing
  verification, the propose guard's active-change scope, and the tree input beside the name-only read) are rows 1, 4, 5,
  and 11, each derived by its own source.
- `[cross-ref]` — `#218` and `#624` name the two pull requests the collision involves, and every spec or requirement
  reference points at the artifact that holds the text.
- `[path]` — a path candidate is a file this change reads or edits, or a fixture the controls construct; none is a claim
  about a file the repository does not track.
