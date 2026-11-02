# Tasks

Workspace:
`unit=verify-unique-change-names; path=/Users/afanasyev/Development/axon-showcase-verify-unique-change-names; branch=verify-unique-change-names; writer=ses_e653ab5caffeGjp1myeRL03Emh`

## 1. The check

- [x] 1.1 `scripts/commit-hygiene.py` gains `archived_change_names(repo)`: each `openspec/changes/archive/*` directory's
      change name, taken through the existing `ARCHIVED_CHANGE_RE` (one leading `YYYY-MM-DD-` segment), grouped by name;
      an entry the regex does not match names no change and is skipped, and a missing archive directory raises
      `CheckFailure`, naming it. `change_names` builds its archived half from this helper, so the walk exists once, and
      it keeps its tolerance of an absent archive directory, so the pre-commit guard's behaviour is unchanged.
- [x] 1.2 `find_duplicate_change_names(repo)` reports each name more than one directory carries, with every directory
      carrying it, apart from a group the acknowledgement constant names exactly, with its reason.
- [x] 1.3 `check_unique_change_names(repo)` names each offender as
      `Archived changes share a name: '<name>' (<directory>, <directory>, …)`, listing every directory carrying the
      name, returns the count, and `--unique-change-names` dispatches to it.
- [x] 1.4 `MODE_FLAGS` gains the flag, the module docstring's mode enumeration states it beside its siblings, and
      `build_parser`'s `description` lists its subject.
- [x] 1.5 `scripts/test-commit-hygiene.py`'s `MODE_TEST_COVERAGE` maps the mode to `UniqueChangeNameTests` and to
      `find_duplicate_change_names`, so the mode-coverage control sees its class and its call.

## 2. The wiring

- [x] 2.1 `build.gradle.kts` registers `verifyUniqueChangeNames` like its siblings: `inputs.file` on
      `scripts/commit-hygiene.py`, `inputs.dir` on `openspec/changes/archive`, `outputs.upToDateWhen { false }`, and the
      mode's `commandLine`.
- [x] 2.2 The `check` block depends on it.

## 3. The controls

- [x] 3.1 Two archived directories carrying one name fail, naming the name and both directories.
- [x] 3.2 Two directories archived on different dates and sharing a name fail (the date prefix is stripped, not
      compared).
- [x] 3.3 Distinct names pass, and so does a name carried twice across days when they differ after the prefix.
- [x] 3.4 An `archive/` entry whose name carries no date prefix is not judged and joins no group: the fixture pairs it
      with the dated directory whose stripped name it equals, so a fallback to the entry's own name fails the case.
- [x] 3.5 The acknowledged pair passes; a third directory carrying the acknowledged name fails, naming that directory,
      and so does a second duplicate of another name.
- [x] 3.6 A missing archive directory fails, naming it, through the entry point.
- [x] 3.7 The entry point (`main([...])`) exits 1 and prints the offender beside the exit code it derives.
- [x] 3.8 The controls bite: the predicate returning no duplicate fails 5 of the class's 10 cases, emptying the
      acknowledgement table fails 4 (three errors and one failure), widening the exemption from the pair to the name
      fails only `test_a_third_directory_carrying_the_acknowledged_name_fails`, and letting a prefix-less entry fall
      back to its own name as the group fails only `test_an_entry_without_a_date_prefix_is_not_judged`; restored, 10
      pass.

## 4. The records

- [x] 4.1 The unit's process-defect record (`docs/process-defects/verify-unique-change-names.md`), or the statement that
      none was observed.
- [x] 4.2 The idea is removed from `docs/ideas.md`, its section keeping its other entries.
- [x] 4.3 The capability's `## Purpose` enumerates each build check this capability owns and a delta cannot carry one:
      add the archived-change-name gate to it in the archive commit.
- [x] 4.4 Docs refresh:
      `git grep -l 'verifyUniqueChangeNames\|verifyUniqueCronSchedules\|verifyUniqueReviewRounds\|unique-change-names' --     README.md AGENTS.md docs/adr .opencode`
      exits 1, so no human-facing or agent-facing document enumerates the `verifyUnique*` family or this mode and no
      edit is owed. The same grep without a path scope also matches the build script, both Python modules, the
      review-round ledger, this change dir, the archived change dirs of earlier gate changes, and the dated audit and
      process-defect records — none of which enumerates the gates a reader acts on. `README.md` describes the root
      `check` as adding "the repository-wide verification tasks" without listing them.

## 5. The claims

- [x] 5.1 Every row of `design.md`'s claims table whose Result reads `pending implementation` is re-derived from the
      implementation and states what its command yields.
- [x] 5.2 Re-derive the table after the unit's last append to its own record.

## 6. The review

- [x] 6.1 `review-quick` rounds, recorded in `docs/review-rounds.tsv` with their session fields: rounds 1–8, the closing
      one carrying no class.
- [x] 6.2 Deep-review routing: `REQUIRED`, signal `requirement-block-added` (the delta's ADDED requirement), derived
      with `scripts/review-routing.py --unit verify-unique-change-names --change verify-unique-change-names`.
- [x] 6.3 Deep review. Pass 1: one `should-fix` — the prefix-less fixture proved only that two such entries do not
      collide, so the scenario's "joins no name group" clause was unverified — `FIXED_AND_VERIFIED` by pass 2, which
      reproduced the regression (a prefix-less entry falling back to its own name fails exactly that case). Pass 2 also
      caught a stale "three mutations" in the claims row's Source column, now count-free. Zero findings remain.
