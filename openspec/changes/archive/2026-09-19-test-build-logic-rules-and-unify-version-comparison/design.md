# Design — testing the build-logic rule sets and unifying the version comparison

## Context

`build-logic` holds the pure rule sets behind the update checks and the infra-image gate. `ModuleDependencyRules`
already established the shape this change follows: a pure rule object that is unit-tested, plus a task that only adapts
Gradle's inputs and outputs to it (its own Javadoc says as much). Three rule sets never got that treatment, and the
version comparator was copied three times instead of shared. See `proposal.md` in this change directory.

The three copies, read from the sources, disagree on two points. The first is a candidate whose numeric segments equal
the current version's while its spelling is longer:

Below, **Tooling** is `ToolingVersions.isNewer` in `ToolingUpdatesTask.kt`, **Helm** is `HelmUpdatesTask.isNewer`, and
**Buildpack** is `BuildpackUpdatesTask.compareVersions` (whose sign is shown rather than a boolean).

| candidate vs current | Tooling | Helm  | Buildpack |
| -------------------- | ------- | ----- | --------- |
| `1.2.1` vs `1.2`     | true    | true  | 1         |
| `1.2.0` vs `1.2`     | true    | false | 1         |
| `1.2` vs `1.2.0`     | false   | false | -1        |

`ToolingVersions` and `BuildpackUpdatesTask` end their comparison with `candidateParts.size - currentParts.size` (or an
equivalent size test); `HelmUpdatesTask` ends with `false`. The second point is the `v` prefix: only `ToolingVersions`
strips it, so `v1.2.3` is comparable there and invisible to the other two. The shared `Versions.numericParts` strips it
for all three, which is a no-op on the Helm and buildpack paths — Helm removes the `v` at the source in `helmCliLatest`,
and the Docker Hub pattern requires a leading digit — so it widens what those checks tolerate rather than changing what
they see. Only the buildpack copy needs the length tiebreak, because it selects a tag to report from a list where a
provider publishes both `5.15` and `5.15.0`.

## Goals / Non-Goals

**Goals**: one shared, tested comparator; unit tests for the three untested rule sets; behavior preserved except the
phantom-update fix; the tasks reduced to adapters.

**Non-Goals**: the exec-bound classes (`PackBuildImageTask`, `AbstractHelmRepositoriesTask`) — a test there would assert
Gradle and PATH plumbing rather than a rule; no change to any task's registration, inputs or outputs; no new dependency;
no change to what the checks report. It does not fold in the `leadingInteger` in
`build-logic/src/main/kotlin/dependency-versions-conventions.gradle.kts`, where `isMajorBump` classifies a catalog
coordinate for the major-suppressed report: that helper returns null for a `v`-prefixed or non-numeric coordinate and is
consumed with a null fallback. It is a major-bump classifier rather than a version ordering, so folding it in would
blunt a deliberate leniency rather than remove a duplicate.

## Decisions

### D1 — Zero-padded equivalence, with the length tiebreak kept for selection only

`Versions.isNewer` compares numeric segments zero-padded and returns false when they are equal, so `5.15` and `5.15.0`
are the same version. `Versions.compare` adds the length tiebreak, and is used only to pick the tag to report, so the
canonical full spelling wins when both exist.

Alternatives considered:

- **Keep each copy's behavior and test the divergence.** Rejected: it would freeze a defect. The divergence's only
  observable effect is a phantom update for an equivalent spelling, which is exactly what a comparator must not report.
- **Keep the length tiebreak inside the comparison (today's buildpack and tooling rule).** Rejected: a pin in short form
  against a provider publishing the padded form then reads as an update for the same release, and the same defect is
  latent in the tooling check the moment a provider publishes both spellings.
- **Drop the tiebreak entirely.** Rejected: the buildpack check selects a tag from the Docker Hub list, and the
  [update-check gotcha](../../../AGENTS.md) records that the max comparison must prefer the longer tag or a stale pin is
  reported as the alias. Dropping it would make the selection depend on response order.

### D2 — One `internal object Versions` in its own file

Alternatives: a shared base task (rejected — the three tasks are different Gradle types, `DefaultTask` and
`AbstractHelmRepositoriesTask`, and they share no other behavior); keeping it inside `ToolingUpdatesTask.kt` and
importing it across files (rejected — the file name would no longer describe its contents, and the tooling file is about
one check, not the version grammar).

### D3 — Extraction follows `ModuleDependencyRules`: pure functions return messages, the task throws

`InfraImageVersionRules` exposes the top-level `image:`/`tag:` parse, the segment count, the floating-reference
rejection and the truncation match as pure functions whose results the task turns into a throw or a log line. The
alternative — testing through a Gradle `ProjectBuilder` fixture — was rejected as heavier and as testing the Gradle
wiring rather than the rule; the rule is what carries the decisions (the minor-granularity truncation and the
floating-reference rejection are both documented as deliberately subtle).

### D4 — Widen private members to `internal`, nothing further

`internal` is visible to `src/test` in the same module, so it is enough. Making the rule objects `public` was rejected:
`build-logic` is an included build and no other module consumes them.

### D5 — Harden the Docker Hub pattern, and say plainly that it is hardening

The Docker Hub tags API answers compact JSON — verified with a live request, zero occurrences of a space after `"name":`
— so today's no-space pattern is correct for that provider. It is nevertheless made tolerant of whitespace and moved to
a named constant, and pinned by a test using a real (trimmed) response body, because the just-captured AGENTS rule asks
a parse pattern to be tested against a real body rather than a hand-written string. The change artifact must not claim a
defect that does not exist.

## Risks / Trade-offs

- **Zero-padded equivalence could hide a genuine update** if a provider ever published `1.2` and `1.2.0` as _different_
  releases. The AGENTS update-check gotcha records them as alias/primary spellings of the same tag, and the verification
  tasks compare the live reports and check the alias pairing (same digest) before the equivalence rule is trusted.
- **Extracting code out of a task can change behavior silently.** Mitigation: the move is verbatim where possible, each
  rule object gets a test that asserts the behavior the raised exception messages describe, and the three live report
  tasks are run before and after and diffed.
- **Tests can encode cosmetics and lock them in.** Mitigation: each test asserts a decision the code deliberately
  encodes (the truncation granularity, the floating rejection, the alias preference), and the design names that decision
  where the test's intent is not obvious from its name.
