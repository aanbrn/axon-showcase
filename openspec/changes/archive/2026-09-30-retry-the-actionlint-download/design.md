# Design

## Context

See `proposal.md` — Why. What constrains the approach, verified against the upstream installer and this repository:

- **The failure is inside the installer script, not the fetch around it.** `download-actionlint.bash` downloads the
  release asset with `curl -L "${url}" | tar xvz` (its line 125) — no retry flag, no environment variable honoured — so
  the 504 that failed the gate hit that command. Verified by reading the pinned script.
- **The step passes `latest`, so there is no version pin to preserve.** The install names no version, so the retry does
  not interact with the tooling-currency checks (the `1.7.12` in the script is its own default, not this repository's
  pin).
- **The workflow file is linted but not tested.** `workflowLint` (actionlint, part of `check`) reads it; no test
  exercises it. (actionlint may invoke `shellcheck` when it happens to be on `PATH`, but this repository neither
  installs nor documents it, so the gate is actionlint alone and the loop's exit-code logic is not covered by it.) So
  the retry's own correctness rests on reading it — which the tasks make an explicit step rather than an assumption.
- **A retry must not mask a genuine failure.** The point is to absorb a _transient_ download error, not to turn a real
  misconfiguration into a slow pass: the loop is bounded and the step still fails once the attempts are exhausted,
  naming what happened.

## Goals / Non-Goals

**Goals:**

- A transient failure of either download in the install (the installer script, or the release asset) is retried before
  the merge gate fails.
- A genuine failure still fails the step, after a bounded number of attempts, with the error visible.

**Non-Goals:**

- **Not pinning the actionlint version.** That is a separate decision (the step deliberately takes `latest`, and the
  tooling-currency checks do not track it); this change leaves the version argument `latest`.
- **Not retrying every unretried fetch in the job.** Two others exist — `npm install --global @fission-ai/openspec` and
  `curl -fsSL https://opencode.ai/install | bash` in the config probe — and they carry the same transient-failure
  exposure. They are deliberately out of scope for this change so its diff stays one reviewed fix; the design's report
  names them, so the generalisation is a recorded decision rather than an oversight. (The quality gates themselves _are_
  deterministic; it is these downloads around them that are not, which the Non-Goal states.)
- **Not fixing the installer script upstream.** It is a dependency's behaviour; the workaround belongs here, and the
  upstream gap is reported rather than patched locally.
- **Not retrying the quality gates.** They run no download, so they have nothing transient to absorb.

## Decisions

### Retry the whole step, not the outer fetch

The step's work is wrapped in a bounded loop (a small `for`/`until` in the step's own shell) that retries the install
when it fails, so both downloads are covered. Rejected: `curl --retry` on the installer-script fetch — it would absorb
nothing, because the failure is in the script's own inner download (verified above); the entry recorded this explicitly
so the cheaper fix is not re-tried. Rejected: downloading the release asset directly in the workflow with `--retry` — it
would duplicate the script's URL and platform logic, making the workflow a second owner of how the tool is obtained.

### Bound the attempts and fail loudly when they are exhausted

A fixed, small number of attempts (three) with the failing output left visible, then a non-zero exit. Rejected: an
unbounded retry — it could hang the gate; rejected: a silent single retry — it would hide a repeatable failure. The
step's exit status stays the signal, so a real failure still reddens the build.

### Report the upstream gap rather than patching the dependency

The installer script honouring no retry flag is the dependency's limitation, so it is worth reporting upstream with the
evidence; the workaround here is ours to keep. The change's report names the gap and where the reference lives.

## Risks / Trade-offs

- **The retry loop adds shell to a gate step, and that shell is only linted, not tested.** Mitigation: keep it small and
  reviewable, run `workflowLint` over it, and have the tasks read the loop rather than assume it; the failure mode of a
  wrong loop is a slow failure, not a false pass.
- **Retrying masks a genuinely broken download** (e.g. a moved release). Mitigation: bounded attempts and the error
  output kept, so the final failure names the cause rather than reporting a timeout.
