# Tasks

## 1. The doctor script

- [x] 1.1 Add `scripts/doctor.sh` with the executable bit set (`chmod +x`) and a leading comment describing its purpose
      and its non-goal (diagnose only, never install), matching the sibling `scripts/*.sh` headers — both are gated:
      `verifyExecutableBits` fails a `scripts/*.sh` that is not `100755`, and `spotlessCheck` owns the markdown only.
      Verify: `sh -n scripts/doctor.sh` is clean, `ls -l scripts/doctor.sh` shows `-rwxr-xr-x`, and
      `./gradlew verifyExecutableBits` passes once the file is tracked.
- [x] 1.2 Implement the three probe classes: tool presence (`java`, `docker`, `actionlint`, `pack`, `helm`, `kubectl`,
      `snyk`, `python3`, `gh`, `opencode`), version floors (Java 21+, Helm 4.x, Compose v2, Python 3), and repo state
      (Git hooks installed in the current clone; Docker daemon reachable). Verify: run the script and confirm each
      charter row appears exactly once, with a version where the tool is present and `unknown` where it is not readable.
- [x] 1.3 Implement the exit contract: exit non-zero when a **required** prerequisite (Java 21+, Docker with a reachable
      daemon, the repo-state checks) is unsatisfied; report optional tools (Helm, `kubectl`, Snyk, `gh`, `opencode`)
      without failing on them. Verify both directions against a **controlled** `PATH`, not the machine's own state: with
      a `PATH` that hides an optional tool while leaving every required one available the script exits 0 and names the
      tool; with a `PATH` that hides `java` it exits non-zero. Provide the controlled `PATH` in the invocation so the
      control does not depend on what this machine happens to have installed.
- [x] 1.4 Print an install hint for each unsatisfied or too-old prerequisite, so the report is actionable without a
      second lookup. Verify: for one deliberately-hidden tool, the printed hint matches the README's install column for
      the detected platform.
- [x] 1.5 Make the script strictly POSIX `sh` (no `readonly`, `local`, `[[`, arrays, or other bashisms) and verify it
      under `dash`, not only under macOS's `bash`-backed `sh` — macOS's `/bin/sh` is bash in POSIX mode, so it would
      mask a bashism. Verify: `dash -n scripts/doctor.sh` is clean and `dash ./scripts/doctor.sh` runs to completion.
- [x] 1.6 Detect the platform (`uname`; `/etc/os-release` for the Debian/Ubuntu family) and select the matching install
      hint, naming the tool without a hint on an unrecognized platform rather than prescribing a command that would not
      run there. Verify: exercise the platform and hint selection for macOS, Debian, and an unrecognized platform (the
      two non-native ones via a `uname`/`os-release` shim), confirming each selects its own hints and an unknown
      platform prints none.
- [x] 1.7 Widen the README's Prerequisites install column to one column per documented platform (macOS and
      Debian/Ubuntu) so the doctor's hints have a documented counterpart per platform, and keep every row. Verify: the
      table renders with both install columns and no row lost.

## 2. The README agreement test

- [x] 2.1 Add `scripts/test-doctor.sh` — a shell test asserting the doctor's charter names exactly the **probe-able**
      tools the README Prerequisites table names, and that every README table row is either covered by the charter or
      listed in the test's own explicit mapping table (so nothing is silently omitted). The mapping is by necessity not
      1:1 and the test states each case rather than implying equality: README rows that are not a probe-able binary map
      to their probe (`Java 21+` → `java`; `Docker & Compose` → `docker` + `docker compose`; `Kubernetes cluster` →
      `kubectl`; `OpenCode` → `opencode`; `` `pack` CLI `` → `pack`; `Snyk CLI` → `snyk`), and the charter's tools that
      have no README table row (`gh`, and the deployment probes) are asserted absent from the README table rather than
      expected in it. Verify: the test passes on the committed state, and is seen to **fail** in both directions — add a
      throwaway tool to the charter and confirm the test fails naming it, then remove a README-listed probe from the
      charter and confirm it fails naming that — then revert both.
- [x] 2.2 Prove the version-floor logic against a known-old input, not only a known-good one: run the doctor with a
      controlled `PATH` exposing a stub `java` reporting 17 and a stub `helm` reporting 3.14, and confirm each is
      reported `too old` with the required minimum stated; expose versions at and above the floors and confirm `ok`; and
      expose a stub that prints nothing parseable and confirm `unknown` (never `ok`). Verify: the doctor's own output
      for each of the three inputs.
- [x] 2.3 Extend `scripts/test-doctor.sh` to assert every charter tool has an install hint on each platform the README
      documents (`macos` and `debian`), or is a declared repo-state probe (which has no install hint). Verify: the test
      is seen to **fail** when a charter tool's hint is removed for a platform, then passes once restored.

## 3. The agent entry point

- [x] 3.1 Add `.opencode/commands/check-tooling.md` with a one-line `description:` frontmatter and a body that runs
      `./scripts/doctor.sh`, interprets the report, and tells the user exactly what to install or do — no skill, and no
      restating of the README table. Verify: the command file loads (frontmatter parses) and the body references the
      script path, not a copied tool list.
- [x] 3.2 Confirm the new command needs no `agents-auditor` or spec edit: the audit scopes by directory
      (`.opencode/commands/` is in its scope), so `/check-tooling` is covered on landing. Read
      `.opencode/agent/agents-auditor.md`'s scope and confirm it names `.opencode/commands/` as a directory rather than
      an enumerated command list; state the outcome in the report. No edit to the auditor definition or the
      `agent-skills` spec is expected — if the read shows an enumerated list that the new command is missing from, stop
      and raise it rather than editing. Verify: the scope clause is read and the conclusion recorded.

## 3a. README command surface

- [x] 3.3 Add the `/check-tooling` row to `README.md`'s Slash Commands table, matching the existing rows' shape — the
      table is the documented trigger list and a new command is otherwise invisible to it. Verify: the row renders in
      the table and its wording names the doctor, not a copied tool list.

## 4. Documentation and the corpus

- [x] 4.1 Add the doctor to `README.md`: a line in the Getting Started prerequisites section pointing at
      `./scripts/doctor.sh` (run it to check your machine), without copying the table's rows or logic. Verify: read the
      rendered section — the pointer names the script and adds no second tool list.
- [x] 4.2 Add an `AGENTS.md` bullet for the doctor: what it is (`scripts/doctor.sh`), what it covers (presence, version
      floors, repo state), that it is deliberately not a `check` member or CI job (the host is not a build input), and
      its `/check-tooling` trigger — extending the existing agent-tooling/`scripts/` guidance rather than accreting a
      new bullet, per the capture rule. Verify: the bullet names the script path and the non-goal; grep `AGENTS.md` for
      `doctor.sh`.
- [x] 4.3 Confirm no `agent-skills` Purpose refresh is owed: the capability scopes the auditor by directory
      (`.opencode/commands/` among them) and this change adds no new agent-tooling member that scope did not already
      cover, so no edit under `openspec/specs/` is expected in this change's diff. Verify: read the capability's
      requirement scope clause and record the conclusion in the report; if the read shows an enumerated command list the
      new command is absent from, stop and raise it rather than editing.
- [x] 4.4 Sweep `docs/ideas.md` for entries this change touches and update them in the same change: the adjacent "State
      once where agent-only tooling lives" idea now has a `scripts/doctor.sh` instance to name. Remove or refresh only
      what this change actually resolves; leave the rest as parked. Verify: `git diff docs/ideas.md` shows only
      change-caused edits.
- [x] 4.5 Verify the single-sourcing sweep over `openspec/config.yaml`'s `context:`/`rules` blocks: it restates the
      project facts, so confirm none of them moves with this change (no stack version, module count, or service list is
      touched), and refresh nothing if so. Verify: read the `context:` block against this change's diff and state the
      outcome in the report.

## 5. Verification

- [x] 5.1 Run `./gradlew spotlessApply` after the final edit to any Spotless-owned file — `AGENTS.md`, `README.md`,
      `docs/ideas.md`, `openspec/changes/check-toolchain-prerequisites/proposal.md`,
      `openspec/changes/check-toolchain-prerequisites/design.md`,
      `openspec/changes/check-toolchain-prerequisites/tasks.md`, and the delta spec
      (`specs/showcase/quality/toolchain-check/spec.md`) — then `./gradlew spotlessCheck`. `.openspec.yaml` is YAML and
      is not formatter-wrapped, so check it against the manual 120-character rule instead. Verify: both Gradle tasks
      succeed, and note that ticking this box is itself an edit, so re-run `spotlessApply` after it.
- [x] 5.2 Run the Docker-free gate `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` (the PR gate's exact form)
      and confirm it is green — the change touches no build input, so a failure is a pre-existing/remote cause, not this
      change's. Verify: the task completes successfully.
- [x] 5.3 Run `openspec validate --changes` and confirm the change validates. Verify: exit 0, `1 passed`.
- [x] 5.4 Read the doctor's full output once more as content, not as a green run: confirm no line reads "ok" without a
      version where one was readable, no required/optional classification is wrong against the README's "only Java and
      Docker are required" statement, and no status contradicts the machine's real state. Record the reading in the
      change's report.
- [x] 5.5 Run the `lesson-capture` subagent over the diff and the review findings, apply the durable proposals the main
      agent judges worth keeping, and record the applied net `AGENTS.md` delta (expected: an extension to an existing
      bullet, not net growth) on this task. Verify: the subagent's verdict is recorded and any applied edit is visible
      in `git diff AGENTS.md`. **Done — applied net `AGENTS.md` delta: +7 lines, 0 deletions (one appended extension to
      the existing "A control must perturb the surface the check actually reads." sub-bullet, adding the
      read-an-intermediary mode and the pipeline-sink-status mode; no new bullet, no retirement).** The subagent
      proposed 1 durable item and rejected 5 as already covered (the OR-condition control, the from-memory structure
      claim, the relative-CWD path, the computed-but -unemitted output, and the portability scope note), all with the
      covering bullet named.
