# Decision record — the first run's resource budget

A dated record of a measurement and what it allows the README to say. `docs/ideas.md` parked a "first-run resource
passport": no source in the repository stated the machine budget a first Compose run needs, the builds included, and the
load-test baselines measure a deployed cluster rather than a minimum. The measurement is taken here so the README can
state a bounded fact instead of a guess; it binds no standing rule.

## The question

What does a person pay to run this stack for the first time on their own machine — wall-clock time, memory while it
runs, and disk — and which of those numbers is the repository able to state?

The chart's `helm/values/*/values-*.yaml` state a **cluster** budget (each component's CPU and memory requests and
limits, sized for a Kubernetes node), which is a different quantity: a Compose run has no scheduler, so its cost is the
containers' working set plus the images on disk.

## The probe

Everything below was measured on 2026-10-30 with the repository at `ac304bd9`, on one machine:

- **Host** — arm64, 32 GB RAM, 10 CPUs; Docker 29.5.2 with the containerd `overlayfs` snapshotter, daemon memory 15.58
  GiB.
- **Conditions** — the Kubernetes deployment (the `axon-showcase`, `kps`, and `tempo` releases) was running in the same
  daemon throughout, and the host load average was 6–7.5, so the numbers are a busy machine's rather than an idle one's.
  The images are amd64 and this host is arm64, so the containers ran under emulation — the shape the repository's pinned
  builder produces. The stack was **idle** while it was sampled: nothing exercised it beyond the health probe, and
  `composeDown` leaves the named volumes in place, so it carried whatever earlier runs had written.

| Measurement                                                          | Command                                                                                                            | Result                                                                                                                  |
| -------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------ | ----------------------------------------------------------------------------------------------------------------------- |
| Build the five images and start the stack                            | `./gradlew composeBuildAndUp`                                                                                      | **139 s**                                                                                                               |
| Start with the images present                                        | `./gradlew composeDown`, then `./gradlew composeUp`, timed to a `200` from `http://localhost:8080/actuator/health` | **29 s**                                                                                                                |
| Memory, sampled every 2 s from the `composeUp` invocation, for 208 s | `docker stats --no-stream` over the eight long-running containers                                                  | peak **3 509 MiB (3.4 GiB)** at 66 s after that invocation; the last 30 s held **3 464–3 472 MiB**, ending at 3 472 MiB |
| Disk, the five application images                                    | `docker images`                                                                                                    | **3 016 MB** (715 + 669 + 714 + 715 + 203), decimal units                                                               |
| Disk, the three infrastructure images the stack pulls                | `docker images`                                                                                                    | **3 240 MB** (`postgres` 666, `apache/kafka` 634, `opensearchproject/opensearch` 1 940)                                 |

At the peak sample (66 s after `composeUp` was invoked), per container: `os-views` 1 450 MiB, `kafka` 435 MiB,
`projection-service` 406 MiB, `api-gateway` 393 MiB, `query-service` 377 MiB, `command-service` 377 MiB, `db-events` 64
MiB, `web-ui` 7 MiB — 3 509 MiB together. The ninth compose service, `kafka-init`, is one-shot and does not stay up.

**What the measurement does not cover:**

- **A first build's time.** The 139 s build ran with the Cloud Native Buildpacks layer cache warm — its log reports
  `Reusing layer` for the Spring Boot and lifecycle layers — so it is a rebuild; no cold-build figure is claimed.
- **A minimum machine specification.** One 32 GB host is not a floor, and the run was a loaded one.
- **The peak's shape.** The peak is the highest 2-second sample, so a spike between samples would be missed, and the
  window ends 208 s after `composeUp`: the last 30 s sitting within an 8 MiB band shows the stack was not still climbing
  there, not that it had reached a formal steady state, and a slower host's bootstrap could still be rising at that
  point.
- **The daemon's store.** The disk figures are per-image sizes, decimal units as `docker images` prints them; how much
  the store actually grows on a first run was not measured. This run's `docker system df` moved 26.61 → 26.80 GB, but
  every image was already present and another workload was running in the same daemon, so that move is attributable to
  neither.

## The decision

The README's "Run the Stack with Docker" section states the measured facts with their source — about half a minute to
answer once the images exist, about 3.4 GiB of memory while the stack runs, and about 6 GB of images for a first run (3
GB of them the five application images) — and links this record for the conditions.

It states **no** first-build time and **no** minimum machine specification. Neither is measured: the build figure is a
warm rebuild, and one 32 GB host is not a floor.

## Reopening condition

Measure again — and let the re-measurement update the README — when any one of these holds:

- a first run is reported as failing or unusably slow on a machine smaller than this one, which makes a floor worth
  measuring;
- the stack's container set changes materially (a service added or removed, or an infrastructure image replaced with a
  heavier one);
- a cold build is measured on a daemon with an empty buildpack-layer cache or on an amd64 host, which would replace the
  warm figure with a first-build one.
