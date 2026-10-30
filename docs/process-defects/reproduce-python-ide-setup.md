# Process defects — reproduce-python-ide-setup

### reproduce-python-ide-setup#1 — The mutation control mutated a branch the tests never reach

**Recorded** — 2026-10-30, by the implementing agent.

**Stage** — implementation.

**Expected** — task 5.3 mutates the wrapper so it aborts on a helper failure and expects `scripts/test-setup-idea.sh` to
go red.

**Observed** — the first mutation reported GREEN. The edit replaced the guarded call in the wrapper's no-flag branch,
while every test case passes `--idea-config-dir` and therefore exercises the other branch.

**Evidence** — the mutation script printed `GREEN sh scripts/test-setup-idea.sh (setup-idea.sh: all cases passed)`;
re-running the mutation against the `--idea-config-dir` branch printed
`RED … ['FAIL: a running IDE was not reported as a skip', 'FAIL: a missing launcher was not reported', '3 check(s) failed']`.

**Consequence** — one extra mutation cycle. The corrected mutation proved the control, and both wrapper branches now
behave identically, so the gap was in the control run, not in the code.

**Knowledge boundary** — observed: the branch each test case reaches and the two mutation outcomes. Assumed: nothing
else in the wrapper differs between the branches. Not checked: whether a future rearrangement could reintroduce two
divergent branches.

- **Decision** — no-change: check-and-test-authoring's 'fixture must reach the state the assertion claims' mode already
  requires driving the branch's own operation and neutering the mechanism.

### reproduce-python-ide-setup#2 — PATH isolation did not stop the real IDE launcher from being invoked

**Recorded** — 2026-10-30, by the implementing agent.

**Stage** — implementation.

**Expected** — the isolated shell control should never invoke a real IntelliJ launcher, so a test run cannot install
plugins into the developer's IDE.

**Observed** — the "no launcher" case failed while the run log showed a real launcher being used: `find_idea` also globs
`$HOME/Applications/IntelliJ IDEA*.app/Contents/MacOS/idea`, so isolating `PATH` left that path reachable. The shell
test had already invoked `installPlugins` against the running IDE before the failure surfaced.

**Evidence** — the run printed
`Installing com.facebook.ktfmt_idea_plugin via launcher /Users/afanasyev/Applications/IntelliJ IDEA.app/Contents/MacOS/idea`
and `Only one instance of IDEA can be run at a time.`; `command -v idea` in the isolated `PATH` still resolved to the
Toolbox launcher. Adding `HOME="$tree/home"` and `IDEA_HOME=""` to the wrapper's environment made the case reachable and
the suite green.

**Consequence** — one wasted test cycle and one unintended `installPlugins` invocation against the running IDE, which
the IDE rejected with "Only one instance of IDEA can be run at a time." and which installed nothing; the test is now
isolated on `PATH`, `HOME` and `IDEA_HOME`.

**Knowledge boundary** — observed: the discovery path, the log lines, and that the IDE refused the invocation. Assumed:
that the refusal left the IDE's plugin state unchanged. Not checked: whether another host could reach a launcher through
a further mechanism the isolation does not cover.

- **Decision** — no-change: already parked — `docs/ideas.md` 2026-10-27 'Record that a `PATH` interception sees only the
  calls resolving through it'; this record is a second occurrence, and the entry records no reopening condition.
