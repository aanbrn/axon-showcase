# Design

## Context

See `proposal.md` — Why. The state that constrains the approach:

- **The code contradicts a spec requirement that already exists.** `showcase/quality/toolchain-check`'s
  required/optional requirement exits non-zero only for "the default build-and-test path", and its repo-state
  requirement's two scenarios have the hooks and the daemon only _reported_. No spec sentence calls the hooks required —
  the classification was chosen in `scripts/doctor.sh` when the capability was introduced (#455), not written into the
  spec.
- **`git-hooks` and `docker-daemon` are not alike.** The hooks are a pre-commit guard: a clone without them still runs
  `./gradlew build`, `test`, `componentTest`, and even `integrationTest`. The Docker daemon _is_ on the test path —
  `integrationTest` runs Testcontainers, which needs a live daemon — so an unreachable daemon genuinely blocks the
  default verification.
- **The doctor's output already separates the classes**: it prints `Required:`, `Optional:`, and `Repo state:` blocks,
  and the repo-state block is what carries both probes (`AGENTS.md` records the output shape). So the fix is a per-probe
  class, not a new block.
- **`/check-tooling` already defers to the doctor's classification** (the docs unit `apply-agents-audit-docs-findings`
  removed the recitation of a fixed split), so making the classification correct needs no command edit.

## Goals / Non-Goals

**Goals:**

- Make the code match the capability's stated exit contract: exit non-zero only for the default build-and-test path.
- Keep the daemon required (the integration tests need it) and record that as the exception rather than a silent choice.

**Non-Goals:**

- **No change to the exit contract itself** — the requirement already says what it should do; the fix aligns the code to
  it rather than rewriting the contract.
- **No change to `/check-tooling` or the README** — the command defers to the doctor, and the README's "only Java and
  Docker are required" becomes consistent once the code is fixed.
- **No new block or output line.** The report keeps its three blocks; only one probe's class moves.

## Decisions

### The Git hooks become advisory; the Docker daemon stays required

`docker-daemon` is kept required because it is genuinely on the default path — `integrationTest` needs Testcontainers.
Making it advisory would let the diagnostic pass on a machine where the default verification cannot complete, which is
the failure mode the exit contract exists to prevent. Rejected: making both advisory (under-reports a real blocker);
rejected: keeping both required (over-reports, and is the defect being fixed).

### The class is expressed per probe, not by moving the probe to the `Optional:` block

The repo-state block is the doctor's place to report setup state, and the hooks belong there either way; what changes is
whether an unsatisfied repo-state check sets the failure flag. Rejected: printing the hooks under `Optional:` — it would
split the repo-state checks across two blocks and lose the "these are setup steps, not tools" grouping the spec
describes.

### The delta modifies the repo-state requirement, not the required/optional one

The exit contract ("only the build-and-test path") lives in the required/optional requirement and is already correct.
The missing rule is _which_ repo-state checks are on that path, which belongs to the repo-state requirement that names
them. Rejected: adding the rule to the required/optional requirement — it would separate the classification from the
probes it classifies.

## Risks / Trade-offs

- **A clone without hooks now gets a clean exit, so the hooks are easier to forget.** Mitigation: the diagnostic still
  prints the hooks as not installed with the install command, so the report nags; the pre-commit guard's own checks and
  `AGENTS.md`'s "run `./scripts/install-git-hooks.sh` once per clone" remain the enforcement path. The exit code is for
  the build-and-test path, not for setup hygiene.
- **The delta must carry both existing scenarios.** A `MODIFIED` block that omits one fails `openspec validate` and
  would drop behavior at archive. Mitigation: the block copies the current requirement's two scenarios verbatim and adds
  the two classification scenarios after them.
