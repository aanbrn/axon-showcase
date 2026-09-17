# Design: Hold the NGINX buildpack back at 1.2.0

## Context

The verification of a buildpack pin is the image build itself: the versions reach nothing a unit test can see. On the
primary development machine (arm64 macOS) `1.2.1` fails that build or produces an AArch64 `nginx` in an amd64 image,
while `1.2.0` passes both, back to back; x86 CI builds `1.2.1` and its container runs. The architecture symptom is filed
as paketo-buildpacks/nginx#1340; the build failure is not reported upstream, because its signature is the one this
repo's host-state gotcha attributes to emulation rather than to a buildpack.

## Decisions

**Hold the pin back rather than verify only in CI.** CI proves that 1.2.1 builds on x86; it says nothing about the
machine this repository is developed on, and a pin that blocks the owner from building the deliverable costs more than
the patch-level nginx update it buys (1.31.4 → 1.31.5). The run image stays floating, so base-OS security patches keep
flowing regardless.

**Record the reason and the close-out where the pin is described.** `buildpackUpdates` has no suppression file, so the
weekly report will keep naming this coordinate; the `AGENTS.md` note is what stops it being chased blindly, and its
close-out names the mechanism that can actually retire the hold — a container-runtime change (colima, Rosetta or `pack`)
re-tested on this machine, or #1340 being resolved — rather than a release of the buildpack, since the cause is
unsettled.
