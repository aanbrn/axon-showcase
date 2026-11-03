# Process defects — match-python-sdk-by-install-directory

### match-python-sdk-by-install-directory#1 — A regression case's probe did not mirror the production probe

**Recorded** — 2026-10-30, by the implementing agent.

**Stage** — implementation.

**Expected** — task 1.5 mutates the matching rule back to the resolved-binary comparison it replaces, and the venv case
(task 1.4) goes red, because that case exists for exactly that regression.

**Observed** — the mutation left the venv case green and failed only its two siblings. The case injected a probe
returning the interpreter path as written, while `probe_interpreter` returns `Path(...).resolve()`; under the mutated
rule the resolved entry was compared with an unresolved probe, so the aliasing the case exists to exercise never
occurred.

**Evidence** — the mutation run printed
`FAIL: test_a_virtual_environment_entry_leaves_the_base_interpreter_unambiguous`,
`FAIL: test_an_entry_symlinked_to_a_sibling_interpreter_matches`, `Ran 25 tests … FAILED (failures=2)` with the venv
case absent; after the two cases' probes were changed to report `probe.resolve()`, the same mutation printed
`FAILED (failures=3)` including `FAIL: test_a_virtual_environment_sdk_does_not_match_the_interpreter_it_wraps`.

**Consequence** — one extra mutation cycle. The case is a check now, the restored suite reports 25 cases `OK`, and the
task's `Done` note records both mutation outcomes.

**Knowledge boundary** — observed: the two mutation outcomes and the injected probe's return value. Assumed: no other
new case depends on an input the production probe would produce differently. Not checked: whether a future change to the
probe's return type would invalidate the other fixtures.

- **Decision** — no-change: check-and-test-authoring's 'stand-in that does not hold the contract it replaces' mode
  already requires the fixture to reproduce the production source's contract.

### match-python-sdk-by-install-directory#2 — The install-directory rule matched a different interpreter in the same directory

**Recorded** — 2026-10-30, by the implementing agent.

**Stage** — review (technical-advisor consultation).

**Expected** — the quick-review loop's four rounds and the mutation battery had proved the matching rule, and the only
open question was whether the deep review runs.

**Observed** — the consultation's confirmed problem: comparing install directories alone discards the executable
filename, so a table registering `bin/python3.13` matches a probe running `bin/python3.15` in the same directory. The
design's state model had no such row and the suite no such case.

**Evidence** — the consultation reported "Given distinct, non-symlinked `/installation/bin/python3.12` and
`/installation/bin/python3.13`, probing the former while registering only the latter yields a match"; the rule then read
`is_the_same_install` → `install_directory(home) == probe_directory`, and `ls -l /opt/homebrew/bin/python3*` shows
`python3.13` and `python3.15` in one directory, each a symlink into a different `Cellar/…` directory.

**Consequence** — the rule became a conjunction of the resolved binary and the install directory (task 1.7), with a
sibling-interpreter case that fails under the directory-only rule; the change had been staged for the manual pass with
that gap in it.

**Knowledge boundary** — observed: the consultation's argument, the predicate's text, and the two interpreters in
`/opt/homebrew/bin`. Assumed: that an IDE configuration can register one of them while the build probes the other. Not
checked: how often a machine holds two versions in one directory.

- **Decision** — no-change: one occurrence, found by consultation and fixed in the unit; below the gate.

### match-python-sdk-by-install-directory#3 — Resolving the probe moved its install directory and broke two matching cases

**Recorded** — 2026-10-30, by the implementing agent.

**Stage** — implementation.

**Expected** — pairing the binary with the install directory (record #2) would keep every matching case, since the suite
passed before and after the rule change.

**Observed** — the first conjunction took the probe's directory from its resolved path (`binary.parent`), so the
directory became the symlink target's: a Homebrew-style entry invoked from `bin` with its target outside that directory,
and a venv probed through its own interpreter, both stopped matching. Neither case existed when the rule was written; I
added both for this question, and the mutation restoring the resolved-path directory then failed them.

**Evidence** — with the rule reading `install_directory(entry) == binary.parent`, the suite printed
`FAIL: test_an_entry_in_the_directory_the_interpreter_was_run_from_matches` and
`FAIL: test_the_interpreter_a_virtual_environment_runs_matches_its_own_sdk`, `FAILED (failures=2)`.

**Consequence** — `probe_interpreter` now returns the path the interpreter names for itself and the predicate resolves
both sides; the report names the invoked path, and the scratch A/B evidence in task 3.3 was re-run and replaced.

**Knowledge boundary** — observed: the two failures and the rule's text at that moment. Assumed: that `sys.executable`
names the invoked path for every supported interpreter. Not checked: a wrapper that reports a path in a directory other
than the one it was invoked from, which the design records as a risk.

- **Decision** — no-change: one occurrence, fixed in the unit; below the gate.

### match-python-sdk-by-install-directory#4 — The rule dropped a matching direction the base rule had

**Recorded** — 2026-10-30, by the implementing agent.

**Stage** — review (quick-review round 5).

**Expected** — the conjunction plus the reported probe path keeps every pair the base rule matched, except the venv the
change exists to reject; the Migration Plan said so.

**Observed** — an SDK registered by the link's target (`…/Cellar/python@3.13/…/bin/python3.13`) stopped matching a probe
naming the link that points at it, though both resolve to one file — the base rule matched that pair. No case covered
the direction, and the design's risks recorded only the opposite one.

**Evidence** — round 5 reported it against `design.md`'s Migration Plan, and the added target-entry case fails under the
rule accepting only the reported path's directory:
`FAIL: test_an_entry_naming_the_target_of_a_link_matches_a_probe_naming_the_link`, `FAILED (failures=1)`.

**Consequence** — `is_the_same_install` accepts the resolved target's directory as well, a scenario and a case were
added, and the asymmetry that remains (a link-spelled entry against a build running the target's own path) is recorded
as a risk with its remedy.

**Knowledge boundary** — observed: the round-5 finding, the base rule's predicate, and the new case's failure. Assumed:
that registering the resolved target is a common enough configuration to preserve. Not checked: how many machines
register the target rather than the link.

- **Decision** — no-change: a second instance inside one unit, not independent; below the gate.

### match-python-sdk-by-install-directory#5 — The target-directory allowance let a venv probe select the base installation

**Recorded** — 2026-10-30, by the implementing agent.

**Stage** — review (second technical-advisor consultation).

**Expected** — accepting the resolved target's directory (record #4) restored the pair the base rule matched without
introducing a new one.

**Observed** — the allowance was unconditional, so a probe run from a virtual environment also matched the base
installation's SDK: both resolve to the base binary and the entry sits in its directory. With both SDKs registered the
stage reported an ambiguity the contributor could only clear by deleting an entry, and the delta's target-entry scenario
stated the allowance without the environment restriction.

**Evidence** — the consultation derived it against the unconditional
`in (install_directory(interpreter), binary.parent)` in `is_the_same_install`; the two cases added for it fail under
that predicate: `FAIL: test_a_virtual_environment_probe_does_not_match_the_base_install_s_sdk` and
`FAIL: test_a_virtual_environment_probe_prefers_its_own_entry`, `FAILED (failures=2)`.

**Consequence** — `probe_interpreter` reports `sys.prefix != sys.base_prefix` as a third fact, the predicate withholds
the target's directory inside an environment, the delta gains the restriction and a scenario, and the suite stands at 32
cases.

**Knowledge boundary** — observed: the consultation's derivation, the predicate's text, and the two cases' failures.
Assumed: that `sys.prefix`/`sys.base_prefix` report an environment for every supported Python. Not checked: an
interpreter that sets them equal inside a virtual environment.

- **Decision** — no-change: one occurrence, found by the second consultation and fixed in the unit; below the gate.

### match-python-sdk-by-install-directory#6 — Five of ten review rounds went to record artifacts that were patched rather than re-derived

**Recorded** — 2026-10-30, by the implementing agent.

**Stage** — review.

**Expected** — the quick-review loop converges once the rule settles: the first four rounds closed it on the first rule,
and each later rule change would re-open it for its own state.

**Observed** — after the rule changed, the record artifacts were patched item by item instead of being re-derived from
the new state: the mutation battery kept counts measured against a smaller suite, task 2.1 kept an older scenario list,
the migration plan kept a two-class changed set, and two statements kept the unconditional allowance. Rounds 8 and 9
raised only such residue, and round 6 did the same for an earlier revision.

**Evidence** — the ledger (`docs/review-rounds.tsv`) records rounds 6, 8, and 9 with classes
`contradiction`/`docs-residue`/`incomplete-enumeration` and round 10 clean; the round-8 report listed a mutation table
whose first three rows did not match a fresh run against the 32-case suite.

**Consequence** — three extra rounds and one extra mutation battery, all repaired; the final artifacts carry the
measured sets.

**Knowledge boundary** — observed: the ledger's rows and the reports' instances. Assumed: that re-deriving each record
from the suite and the delta would have shortened the loop. Not checked: how much of the residue a single full re-read
would have caught.

- **Decision** — no-change: review-gate §1's re-derivation rule and the sweep rule already require record artifacts
  re-derived from the new state, not patched at the sites a finding names.

### match-python-sdk-by-install-directory#7 — The module was written where IntelliJ's Gradle import owns the files

**Recorded** — 2026-10-30, by the implementing agent.

**Stage** — review (live verification after the owner's run).

**Expected** — after a `CONFIGURED` run, `scripts/` is a Python module in the open project.

**Observed** — the run reported `CONFIGURED` and wrote the module at `.idea/modules/scripts.iml`, but the open Gradle
project listed 82 build modules and none for `scripts`; the folder had no Python analysis. A Gradle reload then rewrote
`modules.xml` without the entry while the IML stayed on disk. Written instead under `.idea/python/`, the same module
survived a full Gradle sync and the file resolved to its Python SDK.

**Evidence** — the live project reported `count=82 withScripts=[]` and `file module=axon-showcase … inSource=false`
before, and after the relocation `count=83 withScripts=[scripts]`, type `PYTHON_MODULE`,
`file module=scripts pythonSdk=~/.pyenv/versions/3.12.16/bin/python3`; `modules.xml` held 43 entries with no `scripts`
line after the reload at 16:15, and 44 with one after the sync at 16:19.

**Consequence** — the module moved to `.idea/python/scripts.iml`, a reconcile now deletes the legacy file and entry, the
setup's shell control follows the path, and the delta states that a build-model import keeps the module.

**Knowledge boundary** — observed: the two module lists, the `modules.xml` contents, and the survival of a full sync.
Assumed: that a future import will not clean unknown module files. Not checked: other IntelliJ versions' import
behaviour.

- **Decision** — no-change: the setup script now writes the module under `.idea/python/`, which survives a Gradle
  import; the mechanism is fixed.
