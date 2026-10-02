# Design

## Context

See `proposal.md` — Why. Facts behind the approach (2026-10-02):

- The failure (`gh run view 36946385694`): `:showcase-web-ui:dockerBuildImage` — the `pack`-built Paketo image — errors
  with `failed to fetch base layers: open /tmp/imgutil.local.image.*/blobs/sha256/980d29ce…: no such file or directory`,
  the same blob hash on both failing runs. The four JVM `bootBuildImage` images build in the same job.
- The migration is a Docker 28 → 29 jump: `ubuntu-latest` (24.04) ships Docker 28.0.4, `ubuntu-26.04` ships 29.4.2. The
  failure signature matches `spring-projects/spring-boot#49251` exactly — the same `imgutil`
  `failed to fetch base layers` core — and that issue's trigger is Docker 29's default image store (`overlayfs`). **The
  mechanism is association, not proof:** I could not reproduce it on my own machine, which runs Docker **29.5.2 with the
  same `overlayfs` store** (the image builds cold and warm), so the driver alone does not explain it and the runner's
  Docker setup is part of the trigger. The `daemon.json` step is therefore the _documented_ workaround rather than a
  proven fix, and task 2.1's dispatch on `ubuntu-26.04` is what establishes whether it works.
- The documented workaround, and Spring Boot's own (`996664f3821ec7fd8216d404223eaca9582fb96b`), is exactly a
  `daemon.json` `{"storage-driver":"overlay2"}` plus a Docker restart in the workflow — three lines, no build change.
- Not our pins: `pack` 0.40.9, `builder-jammy-base:0.4.649` and the run image are identical across the passing and
  failing runs. Not reproducible locally on the same Docker 29.5.2 + `overlayfs`, so the runner's Docker setup is part
  of the trigger — which is why the fix is a runner step, not a build-config change.
- `docker info --format '{{.Driver}}'` on the runner reports the driver; `DOCKER_HOST`/socket is the default.

## Goals / Non-Goals

**Goals:**

- Make the smoke's image builds work on a Docker 29 runner, so the label can move without the smoke breaking.

**Non-Goals:**

- Pinning the runner image, or changing the buildpacks, builder, or `pack` version.
- Replacing `pack` with the Spring Boot plugin's exporter (a larger change, and the JVM path already works).
- Waiting for the upstream `imgutil`/buildpack fix.

## Decisions

- **Set the driver in the workflow, not in the build.** The failure is a daemon-store property, and the same build
  succeeds on an `overlay2` daemon; so the fix belongs where the daemon is configured — the runner — exactly as Spring
  Boot did. Options considered: a `--publish` build (it would write the image through the daemon's own export path
  rather than `pack`'s `imgutil` local store — the layer that fails — but it targets a registry, so it does not fit a
  local-daemon image here; kept as the fallback if the driver lever fails); switching the web UI to `bootBuildImage`
  (rejected — the JVM plugin's exporter is a different path, replacing `pack` for one image is a much larger change, and
  the `pack` task is deliberate); waiting for upstream (rejected — the label moves first).
- **Write `daemon.json`, restart Docker, and wait for it before the builds.** Spring Boot's commit writes exactly
  `{"storage-driver":"overlay2"}` to `/etc/docker/daemon.json` and `systemctl restart docker` (no wait); the restart is
  required because the driver is read at daemon start, and a bounded wait (`until docker info; do sleep 1; done`) is
  added here because the next Docker consumer is the `kind` action — Spring Boot's job starts with checkout and Gradle,
  so it did not need one. The only step before this one that touches Docker is `setup-gradle`'s cache service, which the
  restart follows; the wait covers the daemon coming back.
- **Keep the step unconditional rather than gating on the Docker version.** Detecting "is this Docker 29" adds a
  conditional that must itself be right, and 24.04 is _expected_ to be `overlay2` already (it is Docker 28.0.4), so the
  rewrite is a no-op there — but that expectation is not yet verified by a run, so task 2.2's default-path dispatch is
  what confirms the restart does not disturb the working Docker 28 path. Options considered: gate on `docker version`
  (rejected — the extra branch is a failure point for no benefit). Reconsider if a future runner has no `overlay2`
  snapshotter, or if 24.04 turns out to be affected by the restart.

## Risks / Trade-offs

- **A Docker restart early in the job could race the daemon.** → The step restarts Docker and then waits for it
  (`docker info`), the pattern Spring Boot's workflow uses; the builds are the next Docker consumers.
- **`overlay2` may itself disappear from a future runner image.** → The step is paired with the `AGENTS.md` note and the
  idea's re-test recipe, so a runner that drops `overlay2` is caught by the nightly smoke; the step then needs the
  reverse (accept the new default once `imgutil` handles it).
- **The build is slower on `overlay2` for large builder layers.** → Acceptable: the alternative is a failing build, and
  the pack#2272 performance note is about the containerd store, not this one.

## Migration Plan

- Apply: the step and the `AGENTS.md` note.
- Validate: dispatch the smoke on `ubuntu-26.04` (its `runner` input) and confirm all five images build and the smoke
  passes — the check this change exists for. Then the default-path dispatch confirms 24.04 still works.
- Rollback: revert the commit — the step disappears and the daemon is the runner's own again.
- Retire: when `imgutil`/the buildpack handles Docker 29's default store, drop the step — tracked by
  `buildpacks/pack#2527` (the owning venue, where it is emitted) with `spring-projects/spring-boot#49251` as the
  downstream thread; the `AGENTS.md` note names the same references.
