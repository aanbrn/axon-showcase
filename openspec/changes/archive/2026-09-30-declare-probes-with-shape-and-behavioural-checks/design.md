# Design

## Context

See `proposal.md` — Why. What shapes the approach, verified against the repository:

- **The doctor reports every declared probe on any host**, which is what makes a behavioural check possible without
  controlling the machine. Verified: with `env -i PATH=/usr/bin:/bin` (nothing installed) the doctor still emits all 13
  probe lines — a missing tool is a `status`, never an omission. So "every declared probe appears in the output" is an
  assertion that holds everywhere, including on a build runner.
- **The ten tool probes are already uniform data**, written as calls:
  `check_tool "<name>" <class> <binary> <args> <floor>` (`scripts/doctor.sh`). The three report-shaped probes have real
  per-probe logic (a version compare, a path resolution, `docker info`), so only their identity and class are uniform.
  `kind` therefore classifies a probe's **documentation and hint needs** — a `tool` is documented and hinted, a `state`
  step is neither — not how it is implemented: `compose` is a tool (hinted, documented) with no binary of its own,
  checked through `docker compose`.
- **The check never runs the doctor today** — it parses the file. That is the gap both abandoned attempts left open, and
  it is why five and seven rounds of review each found another invisible probe.

### Evidence from the two abandoned attempts (so neither is re-derived)

Attempt 1 derived the probe set by regex over the doctor's text; attempt 2 declared it as a table and asserted things
about the data. Each round found another way a probe could slip through: a hyphenated name the pattern missed; a name
outside the pattern class; a probe moved into the output block; the hint table used as its own exemption; an undeclared
guard; an unbolded README row; a blank-key row skipped by `NF > 1`; a duplicated key; a class or kind typo the check did
not validate; a shortened row; and finally **a `kind` flipped from `tool` to `state`, which made the probe vanish from
the doctor's output while the check still reported "13 probes" and "required probes match"**.

The root cause is one thing, not many: **a content assertion cannot see a probe that never reaches the code, and a check
that never executes the diagnostic cannot see what it reports.** Declaring the set as data is necessary but not
sufficient — it removes the regex, and leaves the shape and the runtime path unverified.

## Goals / Non-Goals

**Goals:**

- One place names every probe, and a probe cannot exist without a row.
- The declaration's **shape** is validated where it is owned, by rules the check shares, so the two cannot disagree.
- The check asserts the doctor's **behaviour** — that every declared probe is actually reported — which is what the
  abandoned attempts could not.
- All of it runs in the build, so any regression fails rather than staying silent.

**Non-Goals:**

- **Not controlling the host.** The check asserts what is true on any machine (every probe reported, shape valid, sets
  agreeing); it does not assert which tools are _installed_, which is the machine's business and varies by runner.
- **Not a spreadsheet.** The table carries identity and class, and the tool facts `check_tool` already takes. Per-probe
  logic stays code.
- **Not a behaviour change.** Same probes, classes, output, exit codes.

## Decisions

### Validate shape where the table is owned, and share those rules with the check

The table lives in the doctor, so the doctor validates it at load (columns, non-blank and unique keys, allowed
class/kind/readme values, and that a `state` row carries no tool facts) — and the check **applies the same rules**
rather than trusting the doctor ran. Rejected: validating only in the doctor (the check never runs it on the
declaration, so a malformed table could pass the gate); rejected: validating only in the check (the doctor would
misbehave when run by a person or by `/check-tooling`).

**Every enumerated column's contract is stated here, in one place, so no reader has to infer an allowed value.** The 13
probes the table declares fall into these groups:

- **`class`** — `required` or `optional`, what an unsatisfied probe does to the default exit code. `required`: java,
  docker, compose, docker-daemon; the other nine are `optional`.
- **`kind`** — `tool` or `state`, describing a probe's documentation and hint needs, never its implementation. `state`:
  git-hooks, docker-daemon; the other eleven are `tool` — including `compose`, a tool with no binary of its own.
- **`readme`** — `yes` or `no`, whether the README Prerequisites table documents the probe. `yes`: the ten probes its
  nine rows map to (java, docker, compose, actionlint, pack, helm, kubectl, snyk, python3, opencode); `no`: gh (covered
  in prose only) and the two state steps.

The `readme` column is validated **against** the README's own mapping rather than trusted, so its value is a claim
checked in both directions — a probe cannot declare itself undocumented while a row documents it, nor documented while
its row is gone.

**The behavioural assertion keys on the report line's shape, not on the key alone.** It matches `^  <key> ` — two
leading spaces, the key, then padding — because that is the shape the doctor emits a status line with, whereas its
detail and remedy fields are single-line strings that can contain the key surrounded by spaces: a free-text line such as
`Note:  java  ...` satisfied an unanchored key match while `java` was never reported (reproduced through the build
gate), which is the invisible-probe class this change exists to close. The anchor is strong rather than absolute — a
_multi-line_ detail could in principle emit such a line — so a future probe whose detail is built from a multi-line
source must not defeat it; every field is currently single-line (`raw_version` pipes through `head -n 1`, and the hooks
path is reported only when `-x` on it succeeds).

**The key is constrained to a plain identifier** (`[a-z0-9][a-z0-9-]*`), because it is interpolated into the patterns
the assertions match with: a key containing whitespace or a glob/ERE metacharacter made the behavioural assertion match
vacuously (a single-space key passed while the probe was never reported — reproduced), which is the very invisible-probe
class this change exists to close. The constraint belongs in the shape validation, not in each pattern, so a key the
assertions cannot safely match is rejected where the table is owned.

### Assert behaviour by running the doctor, not by reading it

The check runs `scripts/doctor.sh` and asserts its output names every declared probe. This is the assertion the
abandoned attempts lacked, and the one that catches the `kind`-flipped case: the probe vanishes from the output, so the
check fails. Rejected: asserting the probe set against the doctor's _source_ in any form — both abandoned attempts did
this and it is the defect. The doctor needs no installation to run: it reports "missing" for an absent tool, so the
assertion is stable on a bare runner.

### Assert the declaration and the diagnostic agree: three assertions, not one

The check asserts (1) shape, (2) behaviour, and (3) agreement (README table, spec required set, per-probe class). Each
covers a failure the others cannot: a malformed row is shape, a probe that never reaches the code is behaviour, and a
wrong class or a README mismatch is agreement. Rejected: any single assertion — the abandoned attempts each relied on a
subset and each left a hole.

### Gate it, because the check is static in its inputs and stable in its assertion

The check reads the doctor's declaration and its own source, the README's rows, and runs the doctor — all local, with no
network, Docker, or installed prerequisite needed, and the behavioural assertion holds on any host (see Context). The
spec's required set is held as a **literal** in the check, derived from `showcase/quality/toolchain-check`'s scenarios
(named beside it in a comment) rather than read from the spec file, so the comparison is against the spec's meaning
rather than the declaration against itself — but a spec edit does not re-run the check, which is the accepted trade-off
of not parsing a prose spec. Rejected: leaving it manual (the status quo, which is why the holes were silent), and
rejected: gating the _doctor_ itself — its **exit code** depends on the host, so it fails for reasons a PR cannot
remediate; the check asserts what is host-independent, and that distinction is load-bearing.

## Risks / Trade-offs

- **A build-file change costs a one-time full CI rebuild** (the cache hash covers the build configuration), self-healing
  once `main` re-warms. Noted in the change's report so the first slow run is not read as a regression.
- **The check now executes the doctor**, so it inherits the doctor's runtime requirements (a POSIX shell and `awk`; no
  network or tools). Mitigation: verified under `env -i PATH=/usr/bin:/bin`, and the check declares its inputs so it
  re-runs when either script changes.
- **Sharing the shape rules risks the two copies drifting.** Mitigation: the rules live in one place the check invokes,
  and task-level controls assert both sides reject the same malformed row.
