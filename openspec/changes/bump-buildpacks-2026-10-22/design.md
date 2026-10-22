# Design

## Context

A Paketo builder is not a version bump like a chart pin: the buildpacks are resolved against the builder's own set, and
a pin the builder does not carry is fetched from the registry — resolving to the wrong architecture slice, which builds
cleanly and then fails at run time. So the unit's first step is establishing the pair, not applying the report's rows.

`pack builder inspect paketobuildpacks/builder-jammy-base:0.4.667` lists `paketo-buildpacks/nginx` at **1.3.0** only,
and `paketo-buildpacks/procfile` at 5.15.1, 5.15.2, 5.15.4 and 5.15.5. So the matched set is builder 0.4.667, nginx
1.3.0 (unchanged) and procfile 5.15.5; the report's nginx row is held.

The image is also not verified by its build: a buildpack release can install a binary of the wrong architecture and
still report a successful build with a metadata label naming the pinned version. The verification runs the image and
reads the binary's ELF header from the buildpack's layer.

## Goals / Non-Goals

**Goals:**

- Move the builder and the buildpacks it bundles together, and hold the one row the builder cannot carry yet.
- Verify the built image by running it and reading nginx's architecture from the layer.

**Non-Goals:**

- `paketo-nginx` 1.3.3 until a pinned builder bundles it (`docs/ideas.md`).
- The run image (`paketobuildpacks/run-jammy-base:latest`), which is deliberately left floating for base-OS security.

## Decisions

1. **The builder's own listing decides the pins.** `pack builder inspect` on the new builder is the authority; the
   update report's rows are candidates, and one of them is refused because the builder does not carry it.
2. **Hold, do not force.** Taking nginx 1.3.3 would trade a matched pair for a registry-added arm64 slice; the parked
   entry is the durable record of the hold, with a retrigger that the check itself will surface.
3. **Verify by running the image.** `docker run`, `GET /`, the rendered `config.js`, and the nginx binary's `e_machine`
   read from `/layers/paketo-buildpacks_nginx/nginx/sbin/nginx` (`3e 00` = x86-64), not from the runtime `PATH` and not
   from the metadata label.

## Risks / Trade-offs

- **The held row keeps the update check reporting an available update.** That is deliberate and recorded rather than
  suppressed: a red report row is better than an unmatched builder/buildpack pair.
- **The image is built for the host's daemon and run under emulation on an arm64 machine.** amd64 emulation was checked
  first (`docker run --rm --platform linux/amd64 alpine uname -m` → `x86_64`), because a host failure would mimic a pin
  defect.

## Verification

- `./gradlew buildpackUpdates` after the bump — the builder and procfile rows are gone; the held nginx row remains.
- `./gradlew verifyDocumentedPins` — the pack's documented builder and buildpack pins match the catalog.
- `./gradlew :showcase-web-ui:dockerBuildImage` — the image builds with the new builder.
- The built image run: `GET /` answers, `config.js` renders the configured API base URL, and nginx's ELF header from the
  buildpack layer reads `3e 00`; the build metadata names `paketo-buildpacks/nginx 1.3.0` and
  `paketo-buildpacks/procfile 5.15.5`.
- `./gradlew spotlessApply`/`spotlessCheck` and `openspec validate --all`.

## Claims

| Claim                                             | Source                                                                                                                                                                                                                      |
| ------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| The check named three rows before the bump        | `./gradlew buildpackUpdates` before the bump → builder `0.4.662 -> 0.4.667`, nginx `1.3.0 -> 1.3.3`, procfile `5.15.4 -> 5.15.5`                                                                                            |
| The builder's bundled nginx and procfile versions | `pack builder inspect paketobuildpacks/builder-jammy-base:0.4.667` — nginx `1.3.0` only; procfile `5.15.1`, `5.15.2`, `5.15.4`, `5.15.5`                                                                                    |
| The builder and procfile rows are cleared         | `./gradlew buildpackUpdates` after the bump → only `paketo-nginx: 1.3.0 -> 1.3.3` remains                                                                                                                                   |
| The documented pins match the catalog             | `./gradlew verifyDocumentedPins`                                                                                                                                                                                            |
| amd64 emulation works on this host                | `docker run --rm --platform linux/amd64 alpine uname -m` → `x86_64`                                                                                                                                                         |
| The image builds with the new builder             | `./gradlew :showcase-web-ui:dockerBuildImage` BUILD SUCCESSFUL                                                                                                                                                              |
| The image serves, and its nginx is x86-64         | `docker run` + `GET /` → 200; `od -An -tx1 -j18 -N2 /layers/paketo-buildpacks_nginx/nginx/sbin/nginx` → `3e 00`; the metadata label names nginx 1.3.0 and procfile 5.15.5                                                   |
| No live document states the old pins as current   | `git grep -n -e 0.4.662 -e 5.15.4 -- ':!openspec/changes/bump-buildpacks-2026-10-22'` → archived changes, `AGENTS.md`'s past-tense incident narration and `docs/audits/2026-10-19.md`; none states them as the current pins |

**Dismissals.** `scripts/claim-scan.py --base origin/main` fires on shape rather than on claimhood — paths and command
invocations, numbers, words like `only`/`every`/`never`, and repository-path/`#NNN` references. A candidate is a claim
only where a claims row states it; the rest are pointers a reader resolves against the repository.
