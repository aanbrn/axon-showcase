# Design: Bump the Paketo builder and buildpacks

## Context

`buildpackUpdates` reports the three pinned Paketo coordinates stale; the open buildpack-updates issue (#211) named only
the builder, at the already-stale `0.4.641`. The pins are catalog-owned (`paketo-nginx`, `paketo-procfile`,
`paketo-builder-jammy-base`) and consumed by `frontend-conventions`' `dockerBuildImage` task, which passes both
buildpacks to `pack` as versioned references over the pinned builder. The run image
(`paketobuildpacks/run-jammy-base:latest`) is deliberately unpinned and is not part of this bump.

## Decisions

**Verify by building the image, and read the result out of the image's own metadata.** The versions reach nothing an
`openspec validate` or a unit test can see — they are baked into the produced image — so the check must be the build
itself: `./gradlew :showcase-web-ui:dockerBuildImage`, then the built image's `io.buildpacks.build.metadata` label,
which names `paketo-buildpacks/nginx 1.2.1` and `paketo-buildpacks/procfile 5.15.0` independently of anything the log
prints. The builder is the exception — the label carries none — so `0.4.642` rests on the build log's pull line plus
`frontend-conventions` wiring the catalog pin. The log alone is not the evidence; it is where the misleading failures
appeared.

**Dismiss the earlier failures as host state, and record why.** An earlier round of the same command failed with a
missing buildpackage label, an analyzer `panic: could not parse '0.12' as version`, `flate: closed writer` and a Go GC
fault, in varying combinations, including on the unmodified baseline. The cause was the host: colima's template enables
Rosetta, the macOS 27 upgrade removed the Rosetta runtime, colima fell back to QEMU for amd64 containers, and the amd64
lifecycle crashes under QEMU. With Rosetta reinstalled the same pins build — and the nginx digest that reported "could
not find label" is the identical digest this build used. No pin was ever at fault, and the failures mimicked a pin
defect precisely because the pin decides which image gets pulled.

**Take the builder and both buildpacks together.** They are one toolchain — the builder bundles the lifecycle that runs
the buildpacks — so splitting the bump would verify a combination nothing ships.
