# Tasks

Workspace: unit=match-python-sdk-by-install-directory;
path=/Users/afanasyev/Development/axon-showcase-match-python-sdk-by-install-directory;
branch=match-python-sdk-by-install-directory; writer=ses_e7545f286ffeYYnUnvdst4oiB0

## 1. Match an SDK entry by install directory

- [x] 1.1 Add `install_directory(interpreter)` to `scripts/ensure-idea-settings.py` — the interpreter path's parent
      directory with symlinked directories resolved — and match an SDK entry on the install directory instead of on the
      resolved binary. Verify with `python3 scripts/test-ensure-idea-settings.py`. **Done** — superseded by task 1.7,
      which pairs the directory with the binary after a consultation showed the directory alone matches a sibling
      interpreter.
- [x] 1.2 Stop `expand_home` from resolving the entry's path, since resolving it is what maps a virtual environment onto
      its base interpreter; the predicate owns the resolution now. Verify with the venv case — it reads the entry's
      unresolved path and would match if this were still resolved.
- [x] 1.3 Keep the reports' shape: the `CONFIGURED` and `NEEDS_ACTION` messages name the interpreter, its version, the
      matched entry's name, and the configuration directory. Verify by reading the messages the matching cases assert.
- [x] 1.4 Add four cases to `scripts/test-ensure-idea-settings.py`, each built from real directories and symlinks under
      the temporary root, and each asserting its own fixture:

        - a virtual environment's SDK does not match the base interpreter the probe resolved (a `versions/<env>` path
          that is a symlink to the environment, whose `bin/python` resolves through to the install's `bin/python3.12`),
          asserting first that the two resolved binaries are equal so the case fails if the fixture stops reproducing
          the aliasing;
        - that entry beside the base interpreter's entry produces `CONFIGURED` with the base entry's name, not an
          ambiguity;
        - a `bin/python3` entry still matches the `bin/python3.12` it symlinks to;
        - an install reached through a symlinked prefix still matches the probe's real path.

- [x] 1.5 Prove the controls can fail, one mutation of the rule at a time, restoring each before the next, and record
      every outcome here. **Done** — six mutations, each named by the edit it makes to `is_the_same_install`, measured
      against the 32-case suite:

        - delete the binary check: 1 failure, the sibling-interpreter case;
        - keep only the binary check: 4 failures — both venv cases and both venv-probe cases;
        - make the resolved target's directory unconditional (drop the environment gate): 2 failures, both venv-probe
          cases;
        - drop the reported-directory comparison and accept the target's directory for every probe: 4 failures — both
          venv-probe cases, the Homebrew-shaped case, and the venv-probed case;
        - drop the reported-directory comparison but keep the gate: 3 failures — the venv-probe-with-both-SDKs case, the
          Homebrew-shaped case, and the venv-probed case;
        - return the parent directory without resolving it: 2 failures, the target-entry and symlinked-prefix cases.

      Restored, 32 cases pass. Records #1, #3, #4, and #5 in
      `docs/process-defects/match-python-sdk-by-install-directory.md` describe the cases each round of fixing this
      exposed.

- [x] 1.6 Resolve the two pre-existing over-120-character lines in `scripts/ensure-idea-settings.py` (the canonical
      inspection constant) by respelling it as concatenated segments, so this change's file keeps the repository's
      120-column rule. Verify that the constant's value is unchanged — load the module and compare its `repr` with the
      base revision's — and that the file has no line over 120. **Done** — `repr` identical;
      `perl -CSD -lne 'print if length > 120'` over `scripts/*.py` prints nothing.
- [x] 1.7 Require both properties, after the owner's technical-advisor consultation showed the install directory alone
      matches a different interpreter installed beside the probe's (`/opt/homebrew/bin` holds `python3.13` and
      `python3.15`, each resolving outside that directory): `is_the_same_install(entry, interpreter)` compares the
      resolved binaries and requires the entry to sit in the probe's install — the reported path's directory or the
      directory that path resolves to — and `probe_interpreter` reports the path the interpreter names for itself so the
      probe's directory is the one the build selected. Add the four cases this needs — a sibling interpreter in the
      probe's directory, an entry invoked from `bin` with its target outside it, an entry naming the link's target, and
      a venv probed through its own interpreter — each asserting its fixture's property. The owner's re-consultation
      then found the mirror defect (a venv probe selecting the base installation's SDK), fixed by reporting whether the
      interpreter runs from an environment (`sys.prefix` against `sys.base_prefix`) and withholding the target's
      directory inside one, with two more cases. **Done** — 32 cases `OK`, and the mutations in task 1.5 discriminate
      every component.

- [x] 1.8 Move the module file to `.idea/python/scripts.iml` after the owner's live run showed a `CONFIGURED` setup
      leaving `scripts/` without Python analysis: IntelliJ's Gradle import owns and rebuilds `.idea/modules/`, so it
      rewrote `modules.xml` without the entry. Delete the legacy module file and its entry while reconciling, and update
      `scripts/test-setup-idea.sh` to the new path. Verify with a case that plants the legacy file and registration, and
      with a live Gradle sync. **Done** — 33 cases `OK`, `sh scripts/test-setup-idea.sh` passes, and the live project
      reports module `scripts` with the Python SDK after a full Gradle sync.

- [x] 1.9 Make the `NEEDS_ACTION` report actionable after the owner's run, in two passes: first name both remedies and
      the registered entries, then — after the owner's readability note and the third consultation — turn the report
      into a short outcome line plus an indented, action-first block (remedies, then the registered SDKs with their
      interpreter paths, then the configurations searched), with the ambiguity case mirrored. Mirror the shape in the
      `/setup-idea` skill, its command, and the README. Verify with layout assertions (remedy before inventory,
      interpreter paths listed) and live runs of both cases. **Done** — both blocks render as specified, the cases
      assert their structure, and the scratch runs quote them.

## 2. State the rule where the capability does

- [x] 2.1 Write the delta at
      `openspec/changes/match-python-sdk-by-install-directory/specs/showcase/quality/ide-config/spec.md`: a `MODIFIED`
      block for "The Python analysis interpreter is the one the build runs" carrying its full current body, the matching
      rule in its description (the same binary, installed in the install that binary belongs to, the two exclusions),
      the amended selection-rule scenario, and the scenarios the exclusions and the link's target need — the delta is
      their record, not this task; a `MODIFIED` block for "The setup reproduces the Python analysis module per working
      tree" carrying its full body plus a scenario for a build-model import keeping the module; and a `MODIFIED` block
      for "An SDK the IDE cannot resolve is reported, not written" whose first scenario points at the rule instead of
      restating a resolved-path comparison, with its other three scenarios carried unchanged. Verify with
      `openspec validate --all`. **Done** — 27 passed, 0 failed; the change validates.
- [x] 2.2 Derive the live sites that state how an SDK is matched, rather than assuming the spec is the only one:
      `git grep -n "real binary path\|resolved interpreter"` over the working tree. Confirm every live hit is a spec
      clause the delta amends or a sentence that stays true, and that the `setup-idea` skill, its command, and the
      README state only the outcomes and the contributor's action — no doc edit is owed, and the recipe records what it
      returned. **Done** — four hits, all in `openspec/specs/showcase/quality/ide-config/spec.md` (lines 167, 172, 183,
      188); lines 167 and 183 state the rule the delta amends, lines 172 and 188 stay true, and the skill, its command,
      and the README hold none.
- [x] 2.3 Read `showcase/quality/ide-config`'s `## Purpose` and confirm this change does not falsify it (it names the
      Python analysis module and the setup's reading of the IDE's own SDK registration); record the verdict so the
      archive step does not have to re-derive it. **Done** — the Purpose still describes exactly this capability; no
      refresh is owed.

## 3. Verify and pass the review gate

- [x] 3.1 Record the pre-write derivation pass and its claims table in `design.md`, seeded with
      `python3 scripts/claim-scan.py --base 289b562caf1dbac593653d4e2f40a7b37c7bf9e6`, with each candidate resolved into
      a row or a class dismissal. **Done** — the scan's four classes (`absolute`, `count`, `cross-ref`, `path`) each
      carry one dismissal reason, and the rows carry the facts the artifacts rest on.
- [x] 3.2 Run the gates the change touches and fix what they report: `python3 scripts/test-ensure-idea-settings.py`,
      `python3 scripts/test-setup-idea.sh`, `./gradlew testEnsureIdeaSettings testSetupIdea`, then
      `./gradlew spotlessApply check -PskipITs -Pcoverage.gate.enabled=false`. **Done** — 33 cases `OK`, and the full
      `check` run is green (`BUILD SUCCESSFUL`), which executes `testEnsureIdeaSettings` and `testSetupIdea`.
- [x] 3.3 Verify the fix against the real SDK tables this machine holds: run the base script and this change's script
      over the same inputs in scratch copies, against the configuration that registers only the virtual environment's
      SDK, and against the configuration that registers this tree's pinned interpreter. Record the reports here.
      **Done** — scratch copies holding `config/`: the base script at `--idea-config-dir …/IntelliJIdea2024.3` printed
      `Python: CONFIGURED` naming the 3.12.1 interpreter and the venv SDK, and exited 0; this change's script over the
      same inputs printed `Python: NEEDS_ACTION` naming `…/3.12.1/bin/python3` (3.12.1) as not registered, and exited 1;
      the same copy pinned to 3.12.16 and pointed at `…/IntelliJIdea2026.2` printed `Python: CONFIGURED` naming the
      3.12.16 interpreter and `SDK '~/.pyenv/versions/3.12.16/bin/python3'`, exited 0, and wrote
      `jdkName="~/.pyenv/versions/3.12.16/bin/python3"`.
- [x] 3.4 Write `docs/process-defects/match-python-sdk-by-install-directory.md` from what the unit observes, or state
      that no process defect was observed. **Done** — seven records: the venv case's probe not mirroring
      `probe_interpreter`, the install-directory-only rule the first consultation caught, the probe's resolution hiding
      two matching cases, the target-entry direction the quick review found missing, the unconditional target allowance
      the second consultation found letting a venv probe select the base installation, the record drift that kept the
      loop open, and the module location a live run exposed.
- [x] 3.5 Run `python3 scripts/commit-hygiene.py --review-ready`, fix what it reports, then run the quick-review loop to
      no new observations, appending each round's row to `docs/review-rounds.tsv` and running `--unique-rounds`,
      `--session-continuity` and the closing `--expect-clean` after the rounds that apply. **Done** — rounds 1-4 closed
      the loop on the first rule; the first consultation's rule change re-opened it at round 5, the second at round 8,
      and the module relocation at round 11 — the rounds that close on the delivered state are named in the hand-over
      list.
- [x] 3.6 Derive the deep-review floor with
      `python3 scripts/review-routing.py --unit match-python-sdk-by-install-directory --change match-python-sdk-by-install-directory`,
      record the outcome and its signals here, and run the review when it is `REQUIRED` (or when the owner resolves a
      `RECOMMENDED` one). **Done** — `ROUTING: RECOMMENDED`, the single signal `repeated-class`; the owner's run-or-skip
      decision is asked for with the manual-review request, and the third consultation recommends one focused pass over
      the production path (probe, parse, stage, match) rather than another design round.
- [x] 3.7 Re-derive the claims table after the unit's last append to its own record.
- [x] 3.8 Consult the technical advisor on the direction and keep reviewing with it to agreement, per the owner's
      instruction; apply what the consultation establishes and re-consult on the result. **Done** — three consultations
      in `ses_e742eeedbffeuLstI6hlVP0101`: the first found the install-directory-only rule's sibling-interpreter match,
      the second the unconditional target allowance that let a venv probe select the base installation, and the third
      reports no confirmed problem while retaining the direction and recommending one focused deep pass over the
      production path. Each finding's fix carries a case and a mutation, and the third consultation states that further
      advisor rounds are unnecessary without new evidence.
- [x] 3.9 Ask the owner for the manual review pass and end the turn without committing. **Done** — asked alongside the
      deep-review decision; the owner answered "отгружай" after reviewing the delivered block.

## Review hand-overs

- Round 1 (planning artifacts and implementation, one pass) — reviewer `review-quick`, session
  `ses_e74402778ffeNa65NASdhnnEi8`, inputs: `proposal.md`, `design.md`, `tasks.md`,
  `specs/showcase/quality/ide-config/spec.md`, and the implementation diff; base
  `289b562caf1dbac593653d4e2f40a7b37c7bf9e6`; bound 20 minutes; 6 findings — `formatting` 2, `test-control` 2,
  `structured-writing` 1, `wording` 1.
- Round 2 (continuation) — same reviewer session, same inputs and base; bound 20 minutes; 2 findings — `contradiction`,
  `formatting` (the latter repeating round 1's class); the hand-over relayed each round-1 instance and its fix.
- Round 3 (continuation) — same inputs and base; bound 20 minutes; 1 finding — `contradiction`, repeating round 2's
  class on the same bullet.
- Round 4 (continuation) — same inputs and base; bound 20 minutes; no new observations, closing the loop for the state
  before the consultation.
- Round 5 (continuation) — inputs as round 1 plus the rule change from task 1.7 (both properties, the probe's reported
  path, three added cases); same base; bound 20 minutes; 7 findings — `contradiction` 2, `docs-residue` 2, `formatting`
  2, `claim-unreproducible` 1; among them the target-entry direction the rule had dropped.

- Round 6 (continuation) — inputs as round 5 plus the accepted resolved-target directory, its scenario, and the fourth
  added case; same base; bound 20 minutes; 3 findings — `contradiction` (the mutation battery's second run),
  `formatting` (two docstring lines at 121 characters), `incomplete-enumeration` (the changed-state set and the
  asymmetry risk named only the link direction).
- Round 7 (continuation) — inputs as round 6 plus the corrected mutation record, the wrapped docstrings, and the
  generalized migration/asymmetry statements; same base; bound 20 minutes; no new observations, closing the loop for the
  state before the second consultation.

- Round 8 (continuation) — inputs as round 7 plus the environment condition from the second consultation (the probe's
  third fact, the withheld target directory, two added cases) and the artifacts that state it; same base; bound 20
  minutes; 2 findings — `contradiction` (the battery, not re-derived against the 32 cases), `docs-residue` (the
  proposal's capability bullet and the asymmetry Risk, both still describing the unconditional allowance).

- Round 9 (continuation) — inputs as round 8 plus the re-derived six-mutation battery, the environment restriction in
  the proposal's capability bullet and the asymmetry Risk, and the corrected loop record; same base; bound 20 minutes; 1
  finding — `incomplete-enumeration` (task 2.1 still listed three appended scenarios where the delta holds four).
- Round 10 (continuation) — inputs as round 9 plus task 2.1's scenario list replaced by a pointer to the delta; same
  base; bound 20 minutes; no new observations, closing the loop on the final rule.

- Round 11 (continuation) — inputs as round 10 plus the module relocation (`.idea/python/scripts.iml`, legacy removal,
  the shell control's path), the third `MODIFIED` requirement with its build-model-import scenario, design D4, record
  #7, and task 1.8; same base; bound 20 minutes; 3 findings — `claim-unreproducible` (a claims row quoting the old
  module path), `contradiction` (record #6 naming round 10 as residue-bearing), `formatting` (a 123-character docstring
  line).

- Round 12 (continuation) — inputs as round 11 plus the three residue fixes (the claims row's module path, record #6's
  round set, and the module docstring's width); same base; bound 20 minutes; 1 finding — `missing-task` (the relocated
  module's production path had no deep-review pass), resolved by scheduling deep-review pass 3.

- Round 13 (continuation) — inputs as round 12 plus the pass-3 fixes, the actionable `NEEDS_ACTION` report with its
  `setup-idea` skill and command text, and the updated records; same base; bound 20 minutes; 3 findings —
  `contradiction` (the proposal still claimed no documentation change), `over-claim` (`--idea-config-dir` presented as
  the remedy for ambiguous entries), `docs-residue` (the README paragraph still described the old message).

- Round 14 (continuation) — inputs as round 13 plus the corrected documentation bullet, the case-scoped flag advice in
  the skill and command, and the refreshed README paragraph; same base; bound 20 minutes; 2 findings — `wording` (the
  README described one `NEEDS_ACTION` cause while naming both) and `structured-writing` (round 13's entry sat outside
  the hand-over list).
- Round 15 (continuation) — inputs as round 14 plus the split README causes and the restored hand-over order; same base;
  bound 20 minutes; no new observations, closing the loop for that state.

- Round 16 (continuation) — inputs as round 15 plus the `NEEDS_ACTION` block (short outcome line, action-first detail,
  interpreter paths in the inventory), its layout assertions, and the mirrored documentation; same base; bound 20
  minutes; 4 findings — `contradiction` (the entries remedy said "rename", which does not change matching),
  `test-control` (the settings page and the entries remedy were unasserted), `docs-residue` (the command still described
  the old sentence), `missing-task` (this hand-over).

- Round 17 (continuation) — inputs as round 16 plus the corrected entries remedy, the settings-page and entries-remedy
  assertions, and the README's remedy wording; same base; bound 20 minutes.

- Round 18 (continuation) — inputs as round 17 plus the command's block description; same base; bound 20 minutes.

- Round 19 (continuation) — inputs as round 18 plus the corrected round-17 hand-over entry; same base; bound 20 minutes;
  no new observations, closing the loop on the delivered state.

## Deep review

Routing: `RECOMMENDED`, one signal `repeated-class`. The owner's instruction to carry out the advisor's recommendation
resolved it to a run, and the third consultation's charter scoped it to the production path.

- Pass 1 (initial, session `ses_e74085427ffebhWgiuRi5MR67u`, base `289b562caf1dbac593653d4e2f40a7b37c7bf9e6`): 1 finding
  — `N-1` nitpick at `tasks.md` task 3.4's and task 3.5's `Done` notes (record count and closing round stale under the
  patched-not-re-derived pattern record #6 describes) — `FIXED_AND_VERIFIED` in pass 2. Everything else passed: the
  probe-to-predicate trace, the decision model at zero/one/several matches across every alias form and both environment
  regimes, implementation–delta alignment, the conservative boundary, the tests' controls, the mutation battery
  (including mutation 6's `/var` symlink dependency), the docs sweep, and the conventions.
- Pass 2 (fix verification, same session `ses_e74085427ffebhWgiuRi5MR67u`): `nothing remains` for the state it saw —
  both corrected notes reproduce, the staged set was unchanged at that point, and the probe-to-predicate trace, decision
  model, alignment, tests, and conventions were re-checked independently.
- Pass 3 (extension over the module relocation, same session, recorded decision): the delivered diff gained the module
  write and registration path (`.idea/python/scripts.iml`, legacy file and entry removal) and a third `MODIFIED`
  requirement after pass 2, and no pass had reviewed that code. The residual material risk — a production path whose
  only verification is a live IDE observation and one committed case — outweighed one focused pass, so the third pass
  ran under the command's budget rule. It reported 2 findings, both `FIXED_AND_VERIFIED` here: `S-1` (the proposal's
  `Modified Capabilities` named two of the three `MODIFIED` requirements) and `N-2` (round 12's hand-over omitted its
  finding). Its scope — the module-write and registration path, its legacy migration, its case, and the third delta
  requirement — otherwise passed.
- A fourth pass was considered and not run: after pass 3 the change is message formatting with committed structure
  assertions, documentation text, and no new decision or state transition. Its residual risk is presentation, which the
  quick review screens on the delivered state, so another deep pass has no expected value beyond the recorded budget
  rule.

## Workflow follow-up

- After the owner approves: commit, push, run CI, archive with
  `openspec archive match-python-sdk-by-install-directory --yes`, and merge, per the `ship-change` skill.
