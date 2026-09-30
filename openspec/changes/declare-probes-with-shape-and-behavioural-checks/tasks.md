# Tasks

## 1. Declare the probes and validate the declaration's shape

- [x] 1.1 Add the probe table to `scripts/doctor.sh` — one row per probe, `key|class|kind|readme|binary|args|floor`,
      with the tool rows carrying the facts `check_tool` already takes and the class/kind/readme values as declared.
      Take the rows from the existing call sites so nothing changes meaning. Verify: the table names all 13 probes, and
      reading a row reproduces every value the call sites carry.
- [x] 1.2 Drive `check_tool` from a row (one runner parameterised by class) rather than five positional arguments per
      call site; drive the three report-shaped probes through a keyed lookup that takes their name and class from the
      row, keeping their logic. Verify: no literal `check_tool "name"` or `report "name"` call site remains, and the
      doctor's output is **byte-identical** to a capture taken before the change (record both).
- [x] 1.3 Write the shape validation, called at doctor load: every row has exactly the declared columns; the key is
      non-blank and **unique**; `class`/`kind`/`readme` are each in their allowed set; and a `kind=state` row carries no
      tool facts (and a `kind=tool` row that **is probed through a binary** names it — the three report-shaped probes
      are tools whose check is logic rather than a version call, so `kind` classifies what a probe needs documented and
      hinted, never how it is implemented; `compose` is a `tool` with no binary). Fail loudly naming the offending row.
      **Extract the rules into one function the check also invokes** — the check is `bash` and the doctor is strictly
      POSIX `sh`, so the
      shared rules live in the doctor (which the check sources the function from, or extracts and runs), never as two
      hand-written copies: the design's own risk is that duplicates drift. Verify: the doctor exits non-zero with a
      named row for each of — a short row, an extra column, a blank key, a duplicate key, a `class` typo, a `kind` typo,
      a `readme` typo, and a `state` row carrying a binary — and the check invokes the same function rather than
      restating the rules.

      (The table's `kind` column is therefore about the probe's documentation and hint needs — a `tool` is documented
      and hinted, a `state` step is neither — not about whether it is implemented by a version call.)
- [x] 1.4 Confirm the doctor still runs where nothing else does, since that property the whole capability rests on must
      survive the refactor. Verify: `env -i PATH=/usr/bin:/bin dash scripts/doctor.sh` reports all 13 probes; record the
      output.

## 2. Assert shape, behaviour, and agreement in the check

- [x] 2.1 Rewrite `scripts/test-doctor.sh` to **read the table** rather than grep the doctor's text, removing the
      `check_tool`/`report` patterns and the hand-copied name lists they fed. Verify: no regex over `doctor.sh` remains
      for the probe set; the check passes on the declaration.
- [x] 2.2 Assert **shape** in the check, rejecting the same malformations task 1.3 does — and confirm the two agree by
      running the check against a doctor carrying each malformation and seeing both fail. Verify: for every case in 1.3,
      the check exits non-zero. This is the assertion a content-only check cannot make.
- [x] 2.3 Assert **behaviour**: run `scripts/doctor.sh`, **capture its output**, and require that output to carry one
      line for every declared probe. **Ignore the doctor's exit code** — it is host-dependent (0 where every required
      tool is present, 1 where one is not), so depending on it would make this gate fail PRs for reasons they cannot
      remediate. Assert the output only, and take the _check's_ exit code directly rather than through a pipe. Verify:
      **the control that matters** — flip one row's `kind` from `tool` to `state` (which made the probe vanish while the
      abandoned attempt's check stayed green reporting "13 probes") and confirm the check now FAILS naming the missing
      probe; confirm it passes on the committed state; and confirm it exits 0 under `env -i PATH=/usr/bin:/bin` where
      the doctor itself exits 1. **The capture must not abort under the check's `set -eu`** — a bare
      `out=$(scripts/doctor.sh)` propagates the doctor's non-zero exit and kills the check before any assertion, so
      capture with `set +e` around it (or `|| true` applied to the command inside the substitution), and assert the
      captured output.
- [x] 2.4 Assert **agreement**: the declared set against the README's Prerequisites table and against the spec's
      required set — the class assertion the current check lacks, held independently of the table so the comparison is
      against the spec rather than the table itself. The README row→probe mapping survives: it is anchored to the
      README's own rows (one row may document two probes), **not** to the declaration, so a probe cannot erase its
      documentation by declaring itself `readme=no` and deleting the row — the mapping is the external anchor, and the
      `readme` column must agree with what it finds. Keep the per-platform install-hint assertion the current check
      carries (named here so it is not silently dropped in the rewrite). Verify: a probe whose class flips fails naming
      it; a README row no probe covers fails; **a probe whose README row is deleted while it still declares `readme=yes`
      fails** (the `readme`-vs-mapping disagreement); **a probe declared `readme=no` whose row still exists fails**; a
      blanked platform hint fails. Record each control's output.
- [x] 2.5 Prove the general case the change exists for: a probe added as a row cannot be missed — including one whose
      name the abandoned attempts' regex would not have matched (a hyphen) and one whose `kind` is wrong. Verify: add
      each and confirm the check fails; remove and confirm it passes.

## 3. Gate the check

- [x] 3.1 Wire `scripts/test-doctor.sh` into `./gradlew check` as a task (an `Exec`, matching the existing `verify*`
      tasks), declaring its inputs — `scripts/doctor.sh`, `scripts/test-doctor.sh`, `README.md`. The spec's required set
      is **not** a file input: the check holds it as a literal derived from the `showcase/quality/toolchain-check`
      scenarios (named in a comment beside it), so a spec edit does not re-run the check — a reviewer comparing the two
      is the control, and the comment records the derivation. State that explicitly in the report rather than
declaring a file the check never reads. Verify: the check runs
      in `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` and is green.
- [x] 3.2 Confirm the gate needs no infrastructure, since it now runs the doctor. Verify: it passes under
      `env -i PATH=/usr/bin:/bin` (no Docker, network, or installed tool), and the task adds no such dependency.
- [x] 3.3 Prove the gate fails a PR that would previously have passed silently: on a scratch copy, make the change that
      shipped as #455 (flip a probe's class) and confirm `./gradlew` reports the failure from the new task, naming the
      probe — and separately the `kind`-flip, which no prior version caught. Record both outputs.

## 4. Documentation and the corpus

- [x] 4.1 Refresh the `AGENTS.md` doctor bullet: the doctor declares its probes as a validated table, the check asserts
      shape, behaviour, and agreement, and the check is a `check` member while the doctor is deliberately not one (the
      distinction is load-bearing — the doctor's exit code is host-dependent, the check asserts what is
      host-independent). Update the check-members enumeration in the Build & Test note. Verify: the bullet names the
      table, both assertions, and the gate; the enumeration lists the new task.
- [x] 4.2 Refresh `README.md`'s description of the check so it is not under-described (it now asserts the class too).
      Verify: the sentence matches what the check does.
- [x] 4.3 Remove the implemented idea from `docs/ideas.md`, rewording the note so the reframing is visible (the answer
      is declaration plus shape/behavioural checks, not derivation). Verify: `git diff docs/ideas.md` shows the entry
      gone.
- [x] 4.4 Sweep the surfaces the change affects for a now-wrong claim — the `showcase/quality/toolchain-check` Purpose,
      `openspec/config.yaml`'s context, and any `only`/`sole` statement about the check. Verify: read each and record
      the outcome; refresh the Purpose in the archive commit if this change falsifies it, as its own task.

## 5. Verification

- [x] 5.1 Run `openspec validate --changes` and confirm the delta validates with the five existing scenarios preserved
      in main-spec order plus the four new ones. Verify: exit 0, `1 passed`, scenario count read from the delta.
- [x] 5.2 Run `./gradlew spotlessApply` after the final edit to a Spotless-owned file (`AGENTS.md`, `README.md`,
      `docs/ideas.md`, the change dir, `build.gradle.kts`), then `./gradlew spotlessCheck`. The two shell scripts are
      not Spotless-owned: check them with `dash -n` and the manual 120-character rule. Verify: all green, and re-run
      `spotlessApply` after ticking this box.
- [x] 5.3 Run the Docker-free gate `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` and confirm green, the new
      task included. Verify: the task completes and appears in the output.
- [x] 5.4 Read the doctor's output as content against the shipped spec: every probe reported with its class and remedy
      exactly as before, the required set unchanged, no line moved, exit codes unchanged. Record the reading — this
      change is behaviour-preserving, so a difference is a defect.
- [x] 5.5 Run the `lesson-capture` subagent over the diff, giving it **both abandoned attempts' evidence** (they are the
      material: two non-converging review loops on one file), apply the durable proposals the main agent judges worth
      keeping, and record the applied net `AGENTS.md` delta on this task. Verify: the verdict is recorded and any
      applied edit is visible in `git diff AGENTS.md`.
      **Done — applied net `AGENTS.md` delta: two merges into existing bullets, no new top-level bullet.** (1) a new
      sub-bullet under the check-evidence bullet: an assertion about a declaration's content cannot see a value that never
      reaches the code — run the subject and assert its output, keeping a host-dependent exit code out; (2) an anchoring
      clause appended to the under-matching-search sub-bullet. One candidate was rejected as already covered (fix the
      class, not the instance); one (a mutating control harness must use a copy) was left to the change's risk note on
      its single-occurrence evidence.
