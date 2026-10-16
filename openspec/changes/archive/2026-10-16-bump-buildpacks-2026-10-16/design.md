# Design

## Context

See `proposal.md` — Why. The `buildpackUpdates` check names three rows; the deriver for what to actually take is the
builder's own bundle list, not the registry:

- `pack builder inspect paketobuildpacks/builder-jammy-base:0.4.662` lists `paketo-buildpacks/nginx` **1.3.0** and
  `paketo-buildpacks/procfile` **5.15.1/5.15.2/5.15.4**.
- The builder the catalog pins today (`0.4.659`) lists `nginx` **1.3.0** and `procfile` **5.15.1/5.15.2/5.15.3**.

So procfile moves to `5.15.4` (the new builder carries it) and nginx stays at `1.3.0` (the new builder does not carry
the report's `1.3.2`). The same situation, and the same resolution, as the 2026-10-12 bump of this pair.

## Goals / Non-Goals

**Goals:**

- Take the builder and the buildpack it genuinely bundles to the new version, as one pair.
- Keep the deliberately-held pin honest: nginx stays at the version the builder carries, with the reason recorded.
- Verify by building and running the image, since a successful build can carry a wrong-architecture binary.

**Non-Goals:**

- `paketo-nginx` `1.3.2`: not in the builder, so taking it is the arm64-slice mismatch the pairing rule names.
- Holding the builder itself back: nothing in the stack needs it.

## Decisions

**Hold nginx, and record why rather than parking it.** The report's `1.3.2` is a registry release the builder does not
bundle; pinning a version absent from the builder makes `pack` add it from the registry and resolve the added
buildpackage to an arm64 slice even though the builder is amd64-only. That is a mechanism, not a preference, so it is
stated where the pin lives (the `AGENTS.md` gotcha already carries the rule) and in this change's Why.

**Both pins in one unit.** The builder and its bundled buildpack are one pair by the rule; a unit that moved only the
builder would ship the mismatch.

**Verify by running the built image and reading nginx's architecture from the buildpack's layer.** The repo's rule is
explicit that a metadata label or a clean build does not clear a buildpack pin: the label names the _buildpack version_,
while a wrong-architecture binary comes from the resolved _buildpackage slice_. So `docker run` the image, `GET /`, and
decode the ELF `e_machine` of `/layers/paketo-buildpacks_nginx/nginx/sbin/nginx` (the runtime `PATH` probe returns
not-found there).

## Risks / Trade-offs

- **The hold masks a real update** → the row stays in the report, so the next bump re-surfaces it; its clearance is the
  builder bundling that version, which each bump re-checks with `pack builder inspect`.
- **The build succeeds with a wrong-architecture binary** → the run plus the layer's ELF check is the verification, and
  a clean build is explicitly not accepted as it.
- **The documented pins drift** → `verifyDocumentedPins` (in `check`) fails on exactly that, and it passes with nginx
  unchanged and procfile/builder moved.

## Migration Plan

None: version values only, taking effect on the next image build.

## Claims table

| Claim                                                            | Source                                                                                                                                                                    |
| ---------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| The check names these three rows                                 | this change's own `./gradlew buildpackUpdates` run (`paketo-builder-jammy-base: 0.4.659 -> 0.4.662`, `paketo-nginx: 1.3.0 -> 1.3.2`, `paketo-procfile: 5.15.3 -> 5.15.4`) |
| The new builder bundles nginx `1.3.0` and procfile `5.15.4`      | `pack builder inspect paketobuildpacks/builder-jammy-base:0.4.662` (its Buildpacks table)                                                                                 |
| The previous builder bundled procfile `5.15.3` and nginx `1.3.0` | the same inspect against `0.4.659`                                                                                                                                        |
| The pairing rule is a mechanism, not a preference                | the `AGENTS.md` buildpack-pin gotcha, which states the absent-pin → arm64-slice → `exec: nginx: not found` chain                                                          |
| The documented pins are asserted                                 | `showcase/quality/infra-image-versions`'s Paketo-pin scenario, and `./gradlew verifyDocumentedPins`                                                                       |
| No suppression list covers buildpacks                            | the three `config/*-updates/major-disabled.txt` lists are dependency, Helm, and web-UI npm; no buildpack equivalent exists                                                |
