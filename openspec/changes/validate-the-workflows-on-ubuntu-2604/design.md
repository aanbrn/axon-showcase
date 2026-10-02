# Design

## Context

See `proposal.md` — Why. Facts that shape the approach (verified 2026-10-02):

- `actions/runner-images#14748`: `ubuntu-latest` becomes Ubuntu 26.04 over a rollout beginning 2026-10-19, planned
  complete by 2026-11-19. Its stated impact is "Ubuntu 24.04-specific software, package versions, system libraries,
  compilers, or prebuilt binaries". Kernel 6.17 → 7.0, systemd 255 → 259; Docker Buildx, Minikube, AWS/Azure/GCloud
  CLIs, Rust and Firefox are unchanged between the two images, and Java stays 17 by default (the jobs install Temurin 21
  via `setup-java`).
- `ubuntu-26.04` is generally available as its own label (`actions/runner-images#14747`), so the new image can be run
  now rather than waited for.
- All 12 jobs across 11 files use `ubuntu-latest`, and `merge-governance` names it in nine requirement clauses — so
  pinning is a spec-level decision, not a workflow edit, while _validating_ is not (the requirement's outcome is
  unchanged).
- The image-dependent surface in this repo is concentrated in `deployment-smoke.yml`: `helm/kind-action` (kind),
  `kubectl` for the failure diagnostics, `docker images`/`kind load docker-image`, `pack`/`bootBuildImage`, and
  `sudo tee -a /etc/hosts`. The respective tools are installed by actions or the build (not by `apt-get`), so the
  exposure is the base image's libraries and the `sudo`/kernel paths rather than a package list the repo owns.
- Only `deployment-smoke.yml` is dispatched by the `ci` release target and installs a live cluster, and it is the run
  whose failure most needs attributing; that (`ci`-target-gated, no merge-gate role) is why it is the first adopter
  rather than the eight other dispatch workflows that merely share the `workflow_dispatch` trigger.

## Goals / Non-Goals

**Goals:**

- A way to run a job on the new image _before_ the label moves, so the decision to keep `ubuntu-latest` is backed by a
  run rather than an assumption.

**Non-Goals:**

- Pinning any runner image (the owner's decision: keep the label).
- Converting every workflow to the input: only the smoke takes it now, so the mechanism exists and is proven, and a
  second adopter adds one line.
- Handling the Ubuntu 22.04 deprecation (`#14254`) — a separate, later decision.

## Decisions

- **Keep `ubuntu-latest`; make the image a dispatch input on the smoke rather than pin.** The label is what
  `merge-governance` requires, and pinning would owe nine deltas plus an untracked bump process. An input gives the
  validation the decision needs without changing any job's default. Options considered: pin all 12 jobs (rejected — the
  spec churn and the missing bump process); pin only the smoke (rejected — same churn for one clause, and it would
  freeze the job least likely to need it); do nothing and watch (rejected — a mid-rollout failure would then arrive
  unattributed).
- **Put the input on `runs-on` for the whole job.** A job-level `runs-on` cannot be overridden per step, so the input
  must select the job's image; the `||` fallback is required at runtime rather than by the lint gate — on the schedule
  the input is empty, so a bare `inputs.runner` would resolve to an empty string and fail the job. (Actionlint accepts
  the bare form when the input _is_ declared; it only rejects an undeclared one.)
- **Name the image in the run.** The first step prints the OS name, the requested label, and `/etc/os-release`'s name
  and version — the version being what makes a migration-window failure attributable to the image rather than to the
  change under test (a point the rollout's own "possible impact" makes necessary, and one the label alone cannot give
  while it is still moving).
- **Record the decision and the how in `AGENTS.md`, not a spec.** The requirement's outcome is unchanged, so the
  rationale that the label is deliberate and how to validate it is agent-facing prose; if a future change pins, that
  edit owes the deltas.

## Risks / Trade-offs

- **A dispatch input that only one workflow takes.** → Deliberate and stated: the smoke proves the mechanism, and other
  workflows adopt it when they need it (the input is one line plus the `runs-on` expression).
- **The validation is only as good as what runs on it — and it found a real break.** → The smoke is the repo's broadest
  single job (builds five images, boots the full pipeline, drives the ingress). Dispatched against `ubuntu-26.04` it
  **fails**: `:showcase-web-ui:dockerBuildImage` (the `pack`-built Paketo image) errors with
  `failed to fetch base layers … no such file or directory`, reproducibly, while the four JVM images build. So the repo
  is not yet safe on 26.04, and the fix is parked in `docs/ideas.md` (2026-10-02) to land before the rollout completes —
  the input did its job on the first dispatch.
- **The rollout may still surprise a job the smoke does not cover.** → The nightly smoke runs on the migrated label
  after 2026-10-19, and every other scheduled workflow keeps running on it weekly, so a surprise surfaces in the
  observational workflows rather than in a merge gate. (The 26.04 break is already known, so this is now the _residual_
  risk rather than the headline.)

## Migration Plan

- Apply: the input, the `runs-on` expression, the image logging, and the `AGENTS.md` note.
- Validate: the smoke was dispatched against `ubuntu-26.04` (runs 36944943462, 36946385694) — **it fails** at the web
  UI's `pack` build, reproducibly; the default-path dispatch (36946374564) succeeds, so the label's behaviour is
  unchanged. The 26.04 fix is parked, not part of this change.
- Rollback: revert the commit — the input disappears and every job is back on the label.
