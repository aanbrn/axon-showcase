## Context

The weekly update checks each open or update a tracker issue. On 2026-09-21 the Helm issue named `91.4.0` → `91.4.1`,
the buildpack issue named builder `0.4.642` → `0.4.644` and `paketo-nginx` `1.2.0` → `1.2.1`. `AGENTS.md` records
`paketo-nginx` as **held back at 1.2.0** because 1.2.1 "does not work" on the arm64 development machine, tracked as
`paketo-buildpacks/nginx#1340`.

## Goals / Non-Goals

**Goals:**

- Take all three actionable bumps, each verified by running the artifact, not by a green build.
- Correct the recorded constraint where the evidence shows it is mis-stated.

**Non-Goals:**

- Changing the update-check workflows.
- Chasing the upstream defect further — the observation is reported; whether the added buildpackage should resolve to
  the target platform is the maintainers' call.

## Decisions

### Take the observability chart bump

`91.4.1` is a patch step on the same line; the spec requires a live install + smoke test before merging. Verified:
`kube-prometheus-stack-91.4.1` deployed, all four monitoring pods Ready, Prometheus reporting all 6 of its active
targets up (on colima/k3s, whose control-plane endpoints are scrapable), Grafana datasources wired (Prometheus default +
Tempo), then uninstalled.

### Take the builder bump and the nginx bump together — the mismatch was the defect

The recorded constraint said nginx `1.2.1` does not work. Testing the combinations with a pack A/B/A/B showed that is
not the mechanism:

| builder   | nginx pin | nginx source            | untrusted-flow warning | nginx arch           | image runs              |
| --------- | --------- | ----------------------- | ---------------------- | -------------------- | ----------------------- |
| `0.4.642` | `1.2.0`   | on the builder          | no                     | x86-64               | yes                     |
| `0.4.644` | `1.2.0`   | **added from registry** | **yes**                | **AArch64**          | **no — exit 127**       |
| `0.4.644` | `1.2.1`   | on the builder          | no                     | **x86-64** (`3e 00`) | **yes — `GET /` → 200** |

Builders `0.4.643`+ bundle `paketo-buildpacks_nginx/1.2.1`. Keeping the `1.2.0` pin against such a builder means `1.2.0`
is no longer on the builder, so `pack` adds it from the registry — and the added buildpackage resolves to an **arm64**
slice (`sha256:0d6fedc4…`, verified `arch=arm64`) even though the build targets amd64, producing an AArch64 `nginx` in
an amd64 image. Bumping **both** pins together keeps nginx on the builder, and the image runs and serves a proper x86-64
nginx. The earlier conclusion — hold both pins — rested on testing only the mismatched pair; the matched pair is the one
that clears `buildpackUpdates`.

The explicit pin is honoured in every case (the build log and image metadata both name the pinned version), so the
`1.2.1`-specific portion of the recorded reason is corrected: it is the mismatch and the registry-add path, not the
buildpack version.

### Report upstream rather than only working around it

Posted as a comment on `paketo-buildpacks/nginx#1340` with a runnable reproduction, the combination table, the `od`
AArch64 evidence, and the resolved arm64 digest. Two earlier drafts were refuted in review — one asserting the bundled
`1.2.1` overrode the pin (the logs show it did not), one whose reproduction would not reproduce the failure (no
`BP_WEB_SERVER=nginx`) — so the posted version is observation-only and asks whether the added buildpackage should
resolve to the target platform's slice. That question is why the _mismatched_ combination must not be used, even though
the _matched_ one is fine.

## Risks / Trade-offs

- **Two Paketo pins move at once** — they are moved together precisely because moving them apart is what breaks; the
  verification is running the built image (`GET /` → 200, nginx `3e 00`), per the "a buildpack pin is verified by
  running the image" rule.
- **The chart patch bump** is the low-risk one, mitigated by the mandated smoke test.

## Migration Plan

None — no data, API, or deployment change.

## Open Questions

None. (Whether the added buildpackage should target the requested platform is the upstream question, not a repo one.)
